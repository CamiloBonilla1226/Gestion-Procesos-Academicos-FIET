---
name: extender-backend-fiet
description: Usar al agregar o modificar cualquier funcionalidad del backend de Gestión de Procesos Académicos FIET (nuevo endpoint, nuevo caso de uso, nueva entidad, nueva regla de negocio de Cancelación de Matrícula, Cancelación de Asignatura o Examen Supletorio).
---

# Extender el backend de Gestión de Procesos Académicos FIET

Antes de escribir una sola línea, lee `backend/CLAUDE.md` completo. Esta
skill da el orden concreto de pasos para agregar una funcionalidad nueva
siguiendo la arquitectura hexagonal de Julián Camacho sin saltarse capas.

No se agrega ningún comentario ni emoji en el código que se escribe o
modifica en esta skill, en ningún paso.

## Paso 1 — Confirmar el modelo de datos

Revisa `docs/database/diccionario-datos-extension.md` y
`docs/database/script_bd_extension.sql`. Si la tabla que necesitas ya
existe ahí, úsala tal cual (mismos nombres de columna). Si hace falta una
tabla o columna nueva, agrégala primero a esos dos archivos de
documentación antes de tocar código Java — la base de datos documentada es
la fuente de verdad, no al revés.

## Paso 2 — Modelo de dominio

Crea el modelo en `dominio/modelos/` con Lombok (`@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder`, o `@SuperBuilder` si el
modelo nuevo hereda de otro, como `Funcionario extends Usuario`). Sin
anotaciones de JPA ni de Bean Validation — esas no pertenecen al dominio.

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
  Lombok completo, `@Table(name = "...")` con el nombre real de la tabla.
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

## Paso 8 — Seguridad

`ApplicationConstantes` todavía no tiene constantes de rol para
"Estudiante" ni para "Funcionario Académico" (solo tiene las de Julián:
`SECRETARIO_GENERAL`, `DECANO`, `FUNCIONARIO_ROL`). Si el endpoint nuevo
necesita restringirse a esos roles, agrega primero la constante del rol y,
si hace falta, la constante compuesta de `hasAnyAuthority(...)`, siguiendo
el mismo patrón que ya existe ahí. Luego agrega la regla de acceso del
endpoint nuevo en `ConfiguracionSeguridad.securityFilterChain` (en
`infraestructura/configuracion/seguridad/configuracion`, no en
`infraestructura/output`). Decide si es `permitAll()`, `authenticated()` o
`hasAnyAuthority(...)` según quién debe poder llamarlo, y que coincida con
el `@PreAuthorize` del controlador.

## Paso 9 — Verificar

```
cd backend/solicitudes
./mvnw clean install -DskipTests
cd ..
docker compose up --build
```

Prueba el endpoint nuevo con una petición real (curl, Postman o el
frontend) antes de dar la tarea por terminada.
