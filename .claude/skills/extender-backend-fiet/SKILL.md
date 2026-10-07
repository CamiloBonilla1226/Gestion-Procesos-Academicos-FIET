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
satisface el bean `crearLogCU`. Si necesita otro caso de uso (por ejemplo
`SolicitudAcademicaCUIntPuerto` o `AnexoAcademicoCUIntPuerto`, como las
radicaciones), lo pide por su puerto de entrada; Spring resuelve el orden.
Las validaciones de negocio van acá, usando `ExcepcionesFormateadorIntPuerto`
y constantes nuevas en `MensajesError` si hace falta un mensaje que no
existe todavía. Si la operación debe quedar en el historial, llama a
`LogCUIntPuerto.crearLog`. Un fallo técnico de IO (por ejemplo guardar o
borrar un archivo adjunto) en el código de Julián se envuelve en un
`RuntimeException` plano con el mensaje y la causa original
(`SolicitudCUImplAdaptador`, `RespuestaCUImplAdaptador`). En la extensión,
los archivos se guardan con `AlmacenamientoAnexosIntPuerto` y, si falla, el
caso de uso lanza `lanzarErrorGenerico(MensajesError.ERROR_GUARDANDO_ARCHIVO)`,
como hacen `AnexoAcademicoCUImplAdaptador` y
`ResolucionAcademicaCUImplAdaptador`.

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
  (`@Qualifier("mapeadorSimple")`), igual que `MapperRolInfraestructuraDominio`
  y `MapperAsignaturaInfraestructuraDominio`, cuando el DTO es plano. Si los
  nombres de campo quedan ambiguos o hay objetos anidados, el mapeo se arma a
  mano en un `@Component`, como en los demás controladores de la extensión
  (estudiantes, funcionarios académicos y los procesos académicos).
- `controlador/<X>RestController.java` — `@RestController`,
  `@RequestMapping("${url.application}<recurso>")`,
  `@CrossOrigin(origins = "${url.frontend}")`, `@RequiredArgsConstructor`,
  `@Tag` de Swagger. Cada método lleva `@PreAuthorize` con las constantes
  de `ApplicationConstantes` del rol permitido, pero solo para documentar la
  intención: en este proyecto no existe `@EnableMethodSecurity` y
  `@PreAuthorize` no protege nada. La protección real es el
  `requestMatchers` del paso 8, que todo endpoint necesita. Las escrituras
  llevan `@Transactional` en el método del controlador, y en los
  controladores de solicitudes académicas también los GET
  (`@Transactional(readOnly = true)`): leer el usuario recorre colecciones
  perezosas de Julián y, sin transacción, falla con
  `LazyInitializationException`. Las rutas con uuid usan la expresión
  regular `[0-9a-fA-F\\-]{36}`.

Solo si el recurso necesita carga masiva desde Excel, sigue el patrón que
ya existe para usuarios y tipos de solicitud (detalle en la sección "Carga
masiva por Excel" de `backend/CLAUDE.md`): un `ProcesadorArchivos<T>` y un
`ValidadorPeticionesExcel<T>` como `@Service` con nombre, inyectados con
`@Qualifier` por constructor explícito, y un endpoint `POST cargar/archivo`
con `@Transactional`. Si el recurso no necesita carga masiva, no se agrega.

## Paso 8 — Seguridad

`ApplicationConstantes` ya tiene las constantes de rol de Julián
(`SECRETARIO_GENERAL`, `DECANO`, `FUNCIONARIO_ROL`) y las de la extensión
(`ESTUDIANTE_ROL`, `FUNCIONARIO_ACADEMICO_ROL`), con sus perfiles
compuestos de `hasAnyAuthority(...)` (lista completa en la sección
"Seguridad" de `backend/CLAUDE.md`). Antes de crear una constante, revisa si
ya existe. El Decano de los procesos
nuevos usa el rol `Decano` existente: se usa `DECANO`, no se crea otro rol. El Decano es un `Usuario` normal con ese
rol, sin fila en `FUNCIONARIO_ACADEMICO`.

La única protección real de acceso son los `requestMatchers` de
`ConfiguracionSeguridad.securityFilterChain`. `@PreAuthorize` no protege
nada, porque no existe `@EnableMethodSecurity`; lo mostró
`backend/pruebas/t1_preauthorize.ps1` cuando se escribió, con un
`Funcionario` que recibía 200 en endpoints anotados para Secretario General
y Decano (hoy esos listados los cierra un `requestMatchers`). Una ruta sin regla
propia cae en una regla genérica (`usuarios/**`, `solicitudes/**`) o en
`anyRequest().authenticated()`, y la puede llamar cualquier usuario
autenticado. No se habilita `@EnableMethodSecurity`.

Para todo endpoint nuevo:

1. Si se restringe a Estudiante o a Funcionario Académico, usa sus
   constantes de rol (`ESTUDIANTE_ROL`, `FUNCIONARIO_ACADEMICO_ROL`) y el
   perfil compuesto que ya exista; si la combinación de roles no tiene
   perfil, agrega la constante de `hasAnyAuthority(...)` siguiendo el mismo
   patrón.
2. Los roles `Estudiante` y `Funcionario Académico` y el tipo de usuario
   `Estudiante` ya están en `data.sql` y en
   `docs/database/seed-roles-extension.sql`. `crearUsuario` busca el tipo de
   usuario por nombre y falla si no existe: el Estudiante usa el tipo
   `Estudiante` y el Funcionario Académico usa el tipo `Empleado FIET -
   Funcionario` de Julián, con el rol `Funcionario Académico` (no el rol
   `Funcionario`). Si una tarea necesitara un rol nuevo, va en una sentencia
   nueva al final de `data.sql` (todas sus sentencias terminan en `;`) y
   también en `seed-roles-extension.sql` con `INSERT IGNORE`, para las bases
   que ya tienen el `data.sql` cargado.
3. Siempre, agrega su regla explícita en
   `ConfiguracionSeguridad.securityFilterChain` (en
   `infraestructura/configuracion/seguridad/configuracion`, no en
   `infraestructura/output`), con método HTTP y ruta. Los matchers se
   evalúan en orden y gana el primero que coincide: la regla nueva va antes
   de `usuarios/**`, `solicitudes/**` y `anyRequest()`. Decide si es
   `permitAll()`, `authenticated()` o `hasAnyAuthority(...)` según quién
   debe poder llamarlo. El `@PreAuthorize` del controlador repite la misma
   expresión como documentación.
4. Comprueba la matriz de permisos en el script de humo de la tarea: rol
   permitido 200, rol no permitido 403, sin token 401.

Solo se agregan líneas; las reglas y constantes existentes no se editan.

## Caso especial — crear Estudiante o Funcionario Académico

El `crearUsuario` de Julián no crea las filas de `ESTUDIANTE` ni de
`FUNCIONARIO_ACADEMICO`: `Usuario.crearInstancia` solo reconoce `FUNCIONARIO`
y cualquier otro rol produce un `Usuario` plano. Estos dos actores se crean
con casos de uso propios:

- Composición, no herencia: tablas propias `ESTUDIANTE` y
  `FUNCIONARIO_ACADEMICO` con PK `Usuario_uuid` hacia `usuarios`. No se
  modifica `Usuario.java` ni `UsuarioGatewayImplAdapter`.
- El caso de uso nuevo recibe `UsuarioCUIntPuerto` por constructor y crea el
  usuario de acceso con `crearUsuario(usuario, tipoUsuario, token)`, que ya
  valida, encripta y registra el log. Un `tipoUsuario` distinto de
  `FUNCIONARIO` produce un `Usuario` plano. Con el `uuidUsuario` devuelto,
  guarda la extensión (y las materias del estudiante) con un gateway nuevo.
- El DTO de petición no recibe roles ni tipo de usuario: el caso de uso los
  asigna por nombre.
- La transacción va en el método del controlador que crea el estudiante o el
  funcionario académico (`@Transactional`, como en
  `UsuarioRestController.crearUsuario`), para que si falla la fila
  especializada también se revierta el usuario. El caso de uso y el gateway
  no llevan `@Transactional`: el dominio no depende de Spring.
- Se crean por formulario y por carga masiva desde Excel (patrón de la
  sección "Carga masiva por Excel" de `backend/CLAUDE.md`); el estudiante se
  crea con sus asignaturas matriculadas.
- Los endpoints se restringen a `SECRETARIO_GENERAL` y `DECANO` con su
  propio `requestMatchers`, como `POST`/`PUT` de `usuarios/**`.

## Paso 9 — Verificar

```
cd backend/solicitudes
./mvnw clean install -DskipTests
cd ..
docker compose up --build
```

En una base nueva: levanta el backend (Hibernate crea las tablas), ejecuta
`data.sql` a mano (`spring.sql.init.mode=never`), crea el Funcionario
Académico y corre `docs/database/seed-procesos-academicos.sql` con su uuid,
y recién entonces prueba (detalle en "Orden de arranque con una base nueva"
de `backend/CLAUDE.md`).

Sigue la sección "Pruebas" de `backend/CLAUDE.md`: prueba unitaria del caso
de uso con JUnit 5 y Mockito (nivel A), script de humo en `backend/pruebas/`
en ASCII puro con `PASS`/`FAIL` y la matriz de permisos (nivel B), y la
comprobación de tablas contra el script (nivel C). Un endpoint académico
nuevo agrega su fila a `backend/pruebas/matriz-permisos.ps1` (si no, la
matriz falla), un script nuevo se agrega a `$Global:Scripts` de
`backend/pruebas/todo.ps1`, y todo script borra al final lo que creó. La tarea no termina hasta
que pasa la compuerta de aceptación de esa sección. Para `.\mvnw.cmd test`
hay que definir antes `SERVER_PORT`, `DB_URL`, `DB_USER_NAME` y
`DB_PASSWORD`, con la base corriendo.
