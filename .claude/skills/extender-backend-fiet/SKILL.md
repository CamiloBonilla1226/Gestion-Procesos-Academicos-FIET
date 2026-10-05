---
name: extender-backend-fiet
description: Usar al agregar o modificar cualquier funcionalidad del backend de Gestión de Procesos Académicos FIET (nuevo endpoint, nuevo caso de uso, nueva entidad, nueva regla de negocio de Cancelación de Matrícula, Cancelación de Asignatura o Examen Supletorio).
---

# Extender el backend de Gestión de Procesos Académicos FIET

Antes de escribir una sola línea, lee `backend/CLAUDE.md` completo. Esta
skill da el orden concreto de pasos para agregar una funcionalidad nueva
siguiendo la arquitectura hexagonal de Julián Camacho sin saltarse capas.

No se agrega ningún comentario ni emoji en el código que se escribe o
modifica en esta skill, en ningún paso, salvo los comentarios de `docs/database/script_bd_extension.sql`, que se conservan.

## Paso 1 — Confirmar el modelo de datos

Revisa `docs/database/diccionario-datos-extension.md` y
`docs/database/script_bd_extension.sql`. Si la tabla que necesitas ya
existe ahí, úsala tal cual (mismos nombres de columna). Si hace falta una
tabla o columna nueva, agrégala primero a esos dos archivos de
documentación antes de tocar código Java — la base de datos documentada es
la fuente de verdad, no al revés. Las tablas las crea Hibernate
desde las entidades (`ddl-auto=update`), como en la base de Julián; el script
de `docs/database` es referencia y no se ejecuta. Cada entidad del paso 5
calca el DDL documentado (nombre de tabla, columnas, tipos y FK).

## Paso 2 — Modelo de dominio

Crea el modelo en `dominio/modelos/` con Lombok (`@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder`, o `@SuperBuilder` si el
modelo nuevo hereda de otro, como `Funcionario extends Usuario`). Sin
anotaciones de JPA ni de Bean Validation — esas no pertenecen al dominio.
El actor Funcionario Académico se llama `FuncionarioAcademico`: `Funcionario`
es del comité de Julián y no se reutiliza ni se extiende.

## Paso 3 — Puertos (interfaces)

- `aplicacion/input/<X>CUIntPuerto.java` — un método por operación que el
  caso de uso va a exponer.
- `aplicacion/output/<X>GatewayIntPuerto.java` — un método por operación
  que el caso de uso necesita contra la persistencia.

## Paso 4 — Caso de uso

`dominio/casosdeuso/<X>CUImplAdaptador.java`, implementa el puerto de
entrada del paso 3. Constructor explícito (no es un `@Component`, se
instancia como bean manual en `infraestructura/configuracion/
BeanConfiguracion.java` — agrega ahí un método `@Bean` nuevo que haga
`new <X>CUImplAdaptador(...)` pasando exactamente los gateways que ese
caso de uso necesita, ni más ni menos, como ya hace con los existentes).
El método se llama `crear<X>CU`, devuelve la clase concreta
`<X>CUImplAdaptador` (no el puerto) y sus parámetros van en el mismo orden
que el constructor del caso de uso, que no es el mismo entre casos de uso.
Si el caso de uso registra historial, recibe `LogCUIntPuerto`, que lo
satisface el bean `crearLogCU`; no hace falta nada más entre beans.
Las validaciones de negocio van acá, usando `ExcepcionesFormateadorIntPuerto`
y constantes nuevas en `MensajesError` si hace falta un mensaje que no
existe todavía. Si la operación debe quedar en el historial, llama a
`LogCUIntPuerto.crearLog`. Un fallo técnico de IO (por ejemplo guardar o
borrar un archivo adjunto) no se formatea con
`ExcepcionesFormateadorIntPuerto` — se envuelve en un `RuntimeException`
plano con el mensaje y la causa original, igual que hacen
`SolicitudCUImplAdaptador` y `RespuestaCUImplAdaptador`.

## Paso 5 — Persistencia

- `infraestructura/output/persistencia/entidades/<X>Entidad.java` — `@Entity`,
  Lombok completo, `@Table(name = "...")` con el nombre exacto de la tabla
  del script (por ejemplo `ESTUDIANTE`). Los campos cuyo nombre Java no sea
  idéntico a la columna (por ejemplo `Usuario_uuid`) llevan
  `@Column(name = "...")` o `@JoinColumn(name = "...")`, porque Hibernate
  no convierte nombres en este proyecto.
- `infraestructura/output/persistencia/repositorios/<X>Repositorio.java` —
  `extends JpaRepository<XEntidad, String>` (o el tipo de ID que
  corresponda).
- `infraestructura/output/persistencia/mapeador/ownMapper/<X>OwnMapper.java`
  — `@Service`, implementa `OwnMapper<Dominio, Entidad>`, mapeo manual con
  dos métodos (`toDominio`, `toEntidad`), sin librería.
- `infraestructura/output/persistencia/gateway/<X>GatewayImplAdaptador.java`
  — `@Service`, `@RequiredArgsConstructor`, implementa el puerto de salida
  del paso 3 usando el repositorio y el own mapper.

## Paso 6 — Excepciones nuevas si hacen falta

Si la regla de negocio necesita un tipo de error que no está en
`infraestructura/output/manejadorExcepciones/excepcionesPropias/`,
créalo ahí (`extends RuntimeException`), agrégale su entrada en
`CodigoError` y su `@ExceptionHandler` en `RestApiExcepcion`. No reuses un
tipo de excepción existente para un significado distinto.

## Paso 7 — Capa de entrada HTTP

Carpeta `infraestructura/input/controlador<X>/` con sus cuatro subcarpetas:

- `DTOPeticion/<X>DTOPeticion.java` — con las anotaciones de Bean
  Validation (`@NotNull`, `@NotBlank`, etc.) que correspondan.
- `DTORespuesta/<X>DTORespuesta.java`
- `mapeador/MapperXInfraestructuraDominio.java` — usa `ModelMapper`
  (`@Qualifier("mapeadorSimple")`), igual que `MapperRolInfraestructuraDominio`.
- `controlador/<X>RestController.java` — `@RestController`,
  `@RequestMapping("${url.application}<recurso>")`,
  `@CrossOrigin(origins = "${url.frontend}")`, `@RequiredArgsConstructor`,
  `@Tag` de Swagger. Cada método con `@PreAuthorize` usando las constantes
  de `ApplicationConstantes` que correspondan al rol permitido.

Solo si el recurso necesita carga masiva desde Excel, sigue el patrón que
ya existe para usuarios y tipos de solicitud (detalle en la sección "Carga
masiva por Excel" de `backend/CLAUDE.md`): un `ProcesadorArchivos<T>` y un
`ValidadorPeticionesExcel<T>` como `@Service` con nombre, inyectados con
`@Qualifier` por constructor explícito, y un endpoint `POST cargar/archivo`
con `@Transactional`. Si el recurso no necesita carga masiva, no se agrega.

## Paso 8 — Seguridad

`ApplicationConstantes` todavía no tiene constantes de rol para "Estudiante"
ni para "Funcionario Académico" (solo tiene las de Julián:
`SECRETARIO_GENERAL`, `DECANO`, `FUNCIONARIO_ROL`). El Decano de los procesos
nuevos usa el rol `Decano` existente: se usa `DECANO`, no se crea otro rol. El Decano es un `Usuario` normal con ese
rol, sin fila en `FUNCIONARIO_ACADEMICO`.

Si el endpoint nuevo necesita restringirse a Estudiante o a Funcionario
Académico:

1. Agrega la constante del rol y, si hace falta, la constante compuesta de
   `hasAnyAuthority(...)`, siguiendo el mismo patrón que ya existe ahí.
2. Agrega el rol y su tipo de usuario como filas nuevas en `data.sql`
   (`roles` y `tiposUsuario`; `crearUsuario` busca el tipo por nombre y falla
   si no existe). Cierra con `;` la última sentencia actual del archivo, que
   no la tiene.
3. Agrega la regla de acceso en `ConfiguracionSeguridad.securityFilterChain`
   (en `infraestructura/configuracion/seguridad/configuracion`, no en
   `infraestructura/output`). Los matchers se evalúan en orden y gana el
   primero que coincide: la regla nueva va antes de `usuarios/**`,
   `solicitudes/**` y `anyRequest()`. Decide si es `permitAll()`,
   `authenticated()` o `hasAnyAuthority(...)` según quién debe poder llamarlo,
   y que coincida con el `@PreAuthorize` del controlador.

Solo se agregan líneas; las reglas y constantes existentes no se editan.

## Caso especial — crear Estudiante o Funcionario Académico

El `crearUsuario` de Julián no crea las filas de `ESTUDIANTE` ni de
`FUNCIONARIO_ACADEMICO`: `Usuario.crearInstancia` solo reconoce `FUNCIONARIO`
y cualquier otro rol produce un `Usuario` plano. Estos dos actores se crean
con casos de uso propios:

- El caso de uso valida y guarda el usuario con `UsuarioGatewayIntPuerto`
  (el de Julián, sin modificarlo) y guarda la extensión (y las materias del
  estudiante) con un gateway nuevo.
- La atomicidad va en un método del adaptador de persistencia con
  `@Transactional`, no en el caso de uso: el dominio no depende de Spring.
- Se crean por formulario y por carga masiva desde Excel (patrón de la
  sección "Carga masiva por Excel" de `backend/CLAUDE.md`); el estudiante se
  crea con sus asignaturas matriculadas.
- Los endpoints se restringen a `SECRETARIO_GENERAL` y `DECANO`, igual que
  `usuarios/**`.

## Paso 9 — Verificar

```
cd backend/solicitudes
./mvnw clean install -DskipTests
cd ..
docker compose up --build
```

En una base nueva: levanta el backend (Hibernate crea las tablas), ejecuta
`data.sql` a mano (`spring.sql.init.mode=never`) y recién entonces prueba.

Prueba el endpoint nuevo con una petición real (curl, Postman o el
frontend) antes de dar la tarea por terminada.

El proyecto no tiene tests de casos de uso, de controladores ni de
gateways: solo existe `SolicitudesApplicationTests` (`@SpringBootTest`,
JUnit 5, `contextLoads`). No hay convención previa que seguir, así que la
verificación de este paso es manual.
