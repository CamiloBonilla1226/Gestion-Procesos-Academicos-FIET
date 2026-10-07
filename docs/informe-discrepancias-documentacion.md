# Informe de discrepancias de la documentación

Revisión de la documentación del backend contra el código, `ConfiguracionSeguridad`, las entidades, los scripts de `backend/pruebas`, `docs/database` y la base MySQL real (`SHOW TABLES` y `DESCRIBE` de las 18 tablas de la extensión, con sus llaves foráneas y únicas). Cada fila es una afirmación que el código no confirmaba y cómo quedó.

## backend/CLAUDE.md

| Afirmación que decía | Lo que dice el código | Cómo quedó |
| --- | --- | --- |
| La última sentencia de `data.sql` (`INSERT INTO Usuario_has_Roles`) no termina en `;` y hay que cerrarla. | Todas las sentencias de `data.sql` terminan en `;`. | Se reemplazó por la sección "Orden de arranque con una base nueva" (data.sql, Funcionario Académico y `seed-procesos-academicos.sql`). |
| Sin Docker se ajustan `DB_URL`, `DB_USER_NAME` y `DB_PASSWORD` en `application.properties`. | `application.properties` no trae valores: lee las variables de entorno `SERVER_PORT`, `DB_URL`, `DB_USER_NAME`, `DB_PASSWORD` y `UPLOADS_PATH`. | Corregido. |
| `dominio/servicios` tiene `MaquinaEtapas`, `ValidadorActorSolicitud` y `ValidadorArchivoAdjunto`. | También tiene `TramiteSolicitud` y `TramiteCancelacion`. | Agregados al árbol y descritos en la sección nueva "Procesos académicos: piezas y máquina de etapas". |
| Los mapeadores de entrada usan ModelMapper. | Solo los de Julián y el de asignaturas; los demás de la extensión mapean a mano. | Corregido. |
| La única dependencia implícita entre beans es `LogCUIntPuerto` (más `UsuarioCUIntPuerto`). | Los beans también piden `SolicitudAcademicaCUIntPuerto`, `AnexoAcademicoCUIntPuerto`, `ConsultaSolicitudAcademicaCUIntPuerto` y `MaquinaEtapas`, que es un `@Bean` (`crearMaquinaEtapas`); los casos de uso con plazos reciben un `Clock` de America/Bogota. | Corregido. |
| `t1_preauthorize.ps1` muestra que un `Funcionario` recibe 200 en `GET usuarios`, `usuarios/paginado` y `usuarios/funcionarios`. | Esos listados (y `usuarios/filtro` y `usuarios/tipos`) tienen hoy un `requestMatchers` que los limita a Secretario General y Decano; `@PreAuthorize` sigue inerte porque no existe `@EnableMethodSecurity`. | Se dejó como antecedente y se aclaró que hoy el 403 lo da la regla. |
| `ApplicationConstantes` solo tiene roles de Julián y no existen constantes para Estudiante ni Funcionario Académico. | Existen `ESTUDIANTE_ROL`, `FUNCIONARIO_ACADEMICO_ROL` y seis perfiles compuestos de la extensión. | Corregido, con una tabla de las reglas de los ocho prefijos de la extensión. |
| Hoy `GET usuarios/**` está abierto a cualquier autenticado y `POST`/`PUT usuarios/**` es de quienes crean estudiantes y funcionarios académicos. | Cinco listados de `usuarios` son solo para Secretario General y Decano; estudiantes y funcionarios académicos se crean por `estudiantes` y `funcionarios-academicos`. | Corregido. |
| `solicitudes-academicas` no tiene endpoint de cambio de etapa todavía (T6 a T8). | Los cambios de etapa están en `cancelaciones-matricula`, `cancelaciones-asignatura` y `examenes-supletorios`. | Corregido. |
| `t5_solicitudes.ps1` inserta en MySQL porque todavía no hay endpoint para radicar. | Los endpoints de radicación existen; el script sigue insertando directo porque es anterior a ellos. | Corregido. |
| Seis casos de uso de radicación y trámite figuraban "sin endpoint hasta T6.3", "T7.3" o "T8.3". | Los seis tienen endpoints. | Cada uno nombra ahora su endpoint. |
| `solicitudes/src/test` tiene un único archivo, `SolicitudesApplicationTests`. | Hay 36 clases de prueba y `mvnw test` corre 1091 pruebas. | Corregido. |
| La carga por Excel no da 400 con número de fila ni lanza mal formato si el archivo no se puede leer. | Es cierto para Julián, pero los lectores de estudiantes y funcionarios académicos lanzan mal formato (código 6) y su mapa de errores trae la fila. | Se agregaron las diferencias de la extensión. |
| Los casos de uso nuevos que manejan archivos envuelven el fallo técnico de IO en un `RuntimeException` plano, sin pasar por `ExcepcionesFormateadorIntPuerto`. | `AnexoAcademicoCUImplAdaptador` y `ResolucionAcademicaCUImplAdaptador` capturan el fallo del almacenamiento y lanzan `lanzarErrorGenerico(ERROR_GUARDANDO_ARCHIVO)` (código 1); el `UncheckedIOException` queda dentro de `AlmacenamientoAnexosImplAdaptador`. | Corregido; el criterio del `RuntimeException` plano queda solo para el código de Julián. |
| No describía la máquina de etapas, la estructura de casos de uso ni el patrón de controladores. | Falta de información, no un error. | Sección nueva con casos de uso por área, servicios de dominio, tabla de acciones y por qué los controladores llevan `@Transactional`; también la lista de scripts de `backend/pruebas`. |

## CLAUDE.md (raíz)

| Afirmación que decía | Lo que dice el código | Cómo quedó |
| --- | --- | --- |
| El diccionario no documenta plazos ni reglas de negocio en prosa; están en la sección siguiente. | El diccionario anota en cada columna la regla que la afecta, y las etapas, anexos, plazos y decisiones están en `docs/database/etapas-por-proceso.md`, que el archivo no mencionaba. | Corregido. El número de tablas nuevas (18) se comprobó contra la base y se mantuvo. |
| Un rechazo en la etapa del Funcionario siempre produce una Resolución firmada por el Decano. | Solo en las cancelaciones: `MaquinaEtapas` exige el escaneo para `RECHAZAR_FUNCIONARIO` en cancelaciones y no en el supletorio, que no produce Resolución. | Corregido. Se agregó la aprobación parcial de Cancelación de Asignatura y el paso de recibo y comprobante del supletorio. |
| `docs/database/` tiene el script y el diccionario. | También tiene `etapas-por-proceso.md` y los scripts de siembra; ahora existe `docs/api`. | Corregido. |

## .claude/skills/extender-backend-fiet/SKILL.md

| Afirmación que decía | Lo que dice el código | Cómo quedó |
| --- | --- | --- |
| `ApplicationConstantes` todavía no tiene constantes de rol para Estudiante ni Funcionario Académico. | Ya existen, con sus perfiles compuestos. | Corregido: revisar las existentes antes de crear una. |
| `t1_preauthorize.ps1` muestra un `Funcionario` con 200 en endpoints anotados. | Hoy esos listados los cierra un `requestMatchers`. | Se dejó como antecedente. |
| Hay que cerrar con `;` la última sentencia de `data.sql`, que no lo tiene. | Ya lo tiene. | Corregido, y se indica agregar la fila también en `seed-roles-extension.sql`. |
| Para un endpoint de Estudiante o Funcionario Académico hay que agregar la constante del rol y su fila en `roles`. | Las constantes, los roles y el tipo `Estudiante` ya existen. | Corregido: se usan los existentes y solo un rol nuevo agrega fila. |
| El mapeador de entrada usa ModelMapper. | La mayoría de los de la extensión mapean a mano. | Corregido. |
| Un caso de uso solo depende de otros beans por `LogCUIntPuerto`; "no hace falta nada más entre beans". | Las radicaciones y los trámites piden `SolicitudAcademicaCUIntPuerto` y `AnexoAcademicoCUIntPuerto`. | Corregido. |
| Un fallo técnico de IO se envuelve en un `RuntimeException` plano, como en Julián. | En la extensión el caso de uso lanza `lanzarErrorGenerico(ERROR_GUARDANDO_ARCHIVO)`. | Corregido. |
| En una base nueva basta con `data.sql` para probar. | Los procesos necesitan el Funcionario Académico y `seed-procesos-academicos.sql`. | Corregido. |

## README.md

| Afirmación que decía | Lo que dice el código | Cómo quedó |
| --- | --- | --- |
| `docs/` solo tiene el script y el diccionario de `database`. | Hay además reglas por proceso, siembras, el contrato de la API y este informe. | Corregido. |
| El primer arranque solo requiere `data.sql`. | Además hay que crear el Funcionario Académico y correr `seed-procesos-academicos.sql`. | Corregido, con el orden y el usuario `rootfiet`. |
| Sin Docker se ajusta `application.properties` "o las variables que use el proyecto". | El archivo solo lee variables de entorno. | Corregido con las cinco variables. |
| La implementación de los tres procesos "se desarrolla de aquí en adelante". | El backend ya los implementa; el frontend todavía no consume sus endpoints. | Corregido, y se agregó cómo correr las pruebas. |

## docs/database/diccionario-datos-extension.md

Las 18 tablas y todas sus columnas, nulos, llaves primarias, únicas y foráneas coinciden con `script_bd_extension.sql` y con `DESCRIBE`. Las cuatro columnas de aprobación parcial y las dos tablas del supletorio también.

| Afirmación que decía | Lo que dice el código | Cómo quedó |
| --- | --- | --- |
| Tipos genéricos (`varchar`, `decimal`, `enum`) sin longitud ni valores por defecto. | La base tiene longitudes, `decimal(3,1)`, los valores de cada `enum` y valores por defecto (`activa`, `1`, `CURRENT_TIMESTAMP`). | Se pusieron los tipos exactos, los valores por defecto y el nombre de cada llave foránea. |
| `HISTORIAL_SOLICITUD_ACADEMICA.accion` es un "rótulo corto de la acción". | Guarda el código de la acción de la máquina de etapas (`RADICAR`, `REMITIR_DECANO`, etc.). | Corregido. |
| `fechaAcordadaExamen` es la "fecha propuesta", nula hasta acordarse. | La registra el Funcionario al aprobar el comprobante, opcional, a las 00:00. | Precisado. |
| `ETAPA_SOLICITUD_ACADEMICA.codigo`: "RADICADA, EN_REVISION_DECANO, APROBADA, RECHAZADA, etc." | Son ocho códigos. | Lista completa. |

## docs/database/etapas-por-proceso.md

| Afirmación que decía | Lo que dice el código | Cómo quedó |
| --- | --- | --- |
| En las cancelaciones, Aprobar del Decano no exige nada. | Exige la situación al cancelar de cada asignatura (matrícula) o la decisión por asignatura (asignatura). | Corregido en la tabla 3.1. |
| Remitir al Decano exige "requisitos verificados". | En las cancelaciones no hay esa confirmación: se exige la evaluación de cada asignatura (y `cumpleCondiciones` en asignatura), y remitir retira el escaneo de la Resolución. La confirmación `requisitosVerificados` es del supletorio. | Corregido en la tabla 3.1 y en la decisión P27. |
| El Funcionario sube el escaneo al cerrar el trámite y el Estudiante lo descarga desde el detalle. | Se acepta en RADICADA, APROBADA_POR_DECANO y RECHAZADA_POR_DECANO, y el Estudiante solo lo descarga en etapa final. | Corregido en la sección 5.4. |
| Faltaban las decisiones tomadas al implementar. | Están en el código y en `change.md`. | Agregadas como P15 a P27 en la sección 8, sin cambiar las existentes. |
| Lo que no está en las fuentes queda en la sección 8 "como decisión pendiente". | La sección 8 no tiene decisiones pendientes: todas están adoptadas e implementadas. | Corregida la frase de la introducción. |

## Documentos sin discrepancias

- `docs/database/script_bd_extension.sql`: coincide con la base en las 18 tablas, tipos, nulos, valores por defecto y nombres de restricciones. No se modificó.
- `docs/database/seed-roles-extension.sql` y `docs/database/seed-procesos-academicos.sql`: coinciden con los UUID y nombres que usa el código. No se modificaron.

## Documentos nuevos

- `docs/api/contrato-api-procesos-academicos.md`: contrato de los 63 endpoints. Sus rutas se compararon contra `backend/pruebas/matriz-permisos.ps1` y no falta ni sobra ninguna.
- Este informe.
