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

Crea el POJO plano en `dominio/modelos/` (sin anotaciones de ningún tipo,
como `Rol.java` o `Usuario.java`). Solo campos, getters/setters (Lombok
`@Getter @Setter` está bien, es lo que ya usa el dominio en varias clases).

## Paso 3 — Puertos (interfaces)

- `aplicacion/input/<X>CUIntPuerto.java` — un método por operación que el
  caso de uso va a exponer.
- `aplicacion/output/<X>GatewayIntPuerto.java` — un método por operación
  que el caso de uso necesita contra la persistencia.

## Paso 4 — Caso de uso

`dominio/casosdeuso/<X>CUImplAdaptador.java`, implementa el puerto de
entrada del paso 3. Constructor explícito (no es un `@Component`, se
instancia como bean manual — revisa cómo lo hacen los casos de uso
existentes en `infraestructura/configuracion/BeanConfiguracion.java` y
replica ese registro para la clase nueva). Las validaciones de negocio van
acá, usando `ExcepcionesFormateadorIntPuerto` y constantes nuevas en
`MensajesError` si hace falta un mensaje que no existe todavía. Si la
operación debe quedar en el historial, llama a `LogCUIntPuerto.crearLog`.

## Paso 5 — Persistencia

- `infraestructura/output/persistencia/entidades/<X>Entidad.java` — `@Entity`,
  Lombok completo, `@Table(name = "...")` con el nombre real de la tabla.
- `infraestructura/output/persistencia/repositorios/<X>Repositorio.java` —
  `extends JpaRepository<XEntidad, String>` (o el tipo de ID que
  corresponda).
- `infraestructura/output/persistencia/mapeador/ownMapper/<X>OwnMapperImpl.java`
  — mapeo manual Entidad <-> modelo de dominio, dos métodos (`toDominio`,
  `toEntidad`), sin librería.
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

Agrega la regla de acceso del endpoint nuevo en
`ConfiguracionSeguridad.securityFilterChain` (en
`infraestructura/configuracion/seguridad/configuracion`). Decide si es
`permitAll()`, `authenticated()` o `hasAnyAuthority(...)` según quién debe
poder llamarlo, y que coincida con el `@PreAuthorize` del controlador.

## Paso 9 — Verificar

```
cd backend/solicitudes
./mvnw clean install -DskipTests
cd ..
docker compose up --build
```

Prueba el endpoint nuevo con una petición real (curl, Postman o el
frontend) antes de dar la tarea por terminada.
