# Diccionario de Datos

Detalle campo por campo de las 20 tablas nuevas de la extensión. `usuarios`,
`usuariosLivianos` y `tiposUsuario` son de Julián Camacho (nombres reales,
verificados contra el código de `back-fiet-sc`, no un supuesto) y no se
listan aquí porque no se modifican.

Los tipos, nulos, valores por defecto, llaves únicas y llaves foráneas de esta
página coinciden con `script_bd_extension.sql` y con `DESCRIBE` sobre la base
que crea Hibernate (`docker compose exec cfiet_database mysql -u root -pmysql
cfiet`). Las llaves foráneas llevan el nombre que se indica entre paréntesis.
Las reglas de negocio que usan estas columnas (etapas, transiciones, anexos y
plazos) están en `etapas-por-proceso.md`.

## PROGRAMA\_ACADEMICO

Catálogo fijo de los cinco programas de la FIET. Se siembra con
`seed-procesos-academicos.sql` (y con `migracion_programas_asignaturas.sql`
en una base que ya tenía datos), con UUID fijos e `INSERT IGNORE`. No hay
endpoint para crearlos ni editarlos; se leen con `GET
catalogos-academicos/programas`.

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidProgramaAcademico | varchar(100) | PK | No | Identificador del programa |
| nombre | varchar(100) | UK (uk\_programa\_nombre) | No | Nombre oficial. En los Excel se escribe exacto, sin distinguir mayúsculas |
| sigla | varchar(10) | UK (uk\_programa\_sigla) | No | Prefijo del código de sus asignaturas |
| ultimoConsecutivoAsignatura | int |  | No | Último consecutivo usado en el código de sus asignaturas. Valor por defecto 0. El backend lo lee con bloqueo de fila y lo sube en la misma transacción que crea la asignatura; nunca baja, así que un código no se reutiliza |

| Programa | Sigla | uuid |
| --- | --- | --- |
| Ingeniería Electrónica y Telecomunicaciones | IET | `14299e0f-8da8-4c73-ad2b-b61e0c52b7f5` |
| Ingeniería de Sistemas | ISI | `5d73d656-2504-44cc-ab6e-5cd10b500abf` |
| Ingeniería en Automática Industrial | IAI | `8606cf01-c111-4aef-92cd-a111cb7b7a38` |
| Tecnología en Telemática | TTE | `7ebedec6-aeab-403b-8a97-9312061bd4b7` |
| Ingeniería en Inteligencia Artificial y Ciencia de Datos | IAC | `d21ec0bf-cee1-4a06-8ae7-00477e88cfe6` |

## ESTUDIANTE

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| Usuario\_uuid | varchar(100) | PK, FK -> usuarios (uuidUsuario) (fk\_estudiante\_usuario) | No | Comparte identificador con Usuario |
| codigoEstudiantil | varchar(45) | UK (uk\_estudiante\_codigo) | No | Código estudiantil, único entre estudiantes |
| ProgramaAcademico\_uuid | varchar(100) | FK -> PROGRAMA\_ACADEMICO (fk\_estudiante\_programa) | No | Programa que cursa. Se elige al crearlo y no se edita. Solo puede matricular asignaturas de este programa |
| semestre | varchar(10) |  | No | Semestre actual |
| facultad | varchar(100) |  | No | Siempre `FIET`. La asigna el backend al crear y al editar; no se recibe en el formulario, en el `PUT` ni en el Excel |

## FUNCIONARIO\_ACADEMICO

Extensión propia de este trabajo de grado, distinta del `Funcionario` /
tabla `funcionarios` de Julián (ese es un rol de comité de facultad, sin
relación con los tres procesos académicos). Cubre solo al Funcionario
Académico, es decir, al Técnico Administrativo de Procesos Académicos que
verifica la información académica.

El Decano no tiene fila en esta tabla ni en ninguna otra de la extensión.
Como en Julián, es un `Usuario` con el rol `Decano` y el tipo de usuario
`Maxima autoridad FIET - Decano`, que ya existen en su `data.sql`, y se
identifica por ese rol. Ninguna FK de la extensión apunta al Decano: el
historial apunta a `usuarios`.

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| Usuario\_uuid | varchar(100) | PK, FK -> usuarios (uuidUsuario) (fk\_funcionarioacademico\_usuario) | No | Comparte identificador con Usuario |
| cargo | varchar(100) |  | No | Cargo que ocupa el Técnico Administrativo de Procesos Académicos (antes `dependencia`); no distingue roles. Editable por `PUT funcionarios-academicos/{uuid}` |

## ASIGNATURA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidAsignatura | varchar(100) | PK | No | Identificador de la materia |
| codigoAsignatura | varchar(45) | UK (uk\_asignatura\_codigo) | No | Lo genera el backend: sigla del programa, guion y consecutivo del programa con al menos tres dígitos (`ISI-001`, `ISI-1000`). No se edita ni se reutiliza |
| nombreAsignatura | varchar(150) |  | No | Nombre de la materia, sin espacios sobrantes. No se repite dentro del mismo programa sin distinguir mayúsculas ni espacios (regla del backend, no hay índice) |
| ProgramaAcademico\_uuid | varchar(100) | FK -> PROGRAMA\_ACADEMICO (fk\_asignatura\_programa) | No | Programa al que pertenece. Obligatorio y fijo desde la creación |

## ASIGNATURA\_MATRICULADA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidAsignaturaMatriculada | varchar(100) | PK | No | Identificador de la matrícula puntual |
| Estudiante\_uuid | varchar(100) | FK -> ESTUDIANTE (fk\_asigmat\_estudiante) | No | Estudiante que la tiene matriculada |
| Asignatura\_uuid | varchar(100) | FK -> ASIGNATURA (fk\_asigmat\_asignatura) | No | Materia matriculada |
| grupo | varchar(20) |  | No | Grupo en el que está matriculada |
| estado | varchar(20) |  | No | activa, cancelada, aprobada o perdida. Valor por defecto `activa`. Solo pasa de activa a uno de los otros tres |

## TIPO\_SOLICITUD\_ACADEMICA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidTipoSolicitudAcademica | varchar(100) | PK | No | Identificador del tipo |
| nombre | varchar(100) |  | No | Cancelación de Matrícula / Cancelación de Asignatura / Examen Supletorio. El backend asocia cada tipo a su proceso por este nombre exacto, así que renombrarlo rompe la asociación |
| descripcion | varchar(255) |  | Sí | Descripción del tipo |
| FuncionarioAcademico\_uuid | varchar(100) | FK -> FUNCIONARIO\_ACADEMICO (fk\_tiposolicitud\_funcionario) | No | Funcionario que atiende este tipo; lo cambian el Secretario General o el Decano |

## ETAPA\_SOLICITUD\_ACADEMICA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidEtapa | varchar(100) | PK | No | Identificador de la etapa |
| codigo | varchar(60) |  | No | RADICADA, EN\_REVISION\_DECANO, APROBADA\_POR\_DECANO, RECHAZADA\_POR\_DECANO, PENDIENTE\_PAGO, EN\_VERIFICACION\_PAGO, APROBADA o RECHAZADA |
| TipoSolicitudAcademica\_uuid | varchar(100) | FK -> TIPO\_SOLICITUD\_ACADEMICA (fk\_etapa\_tiposolicitud) | Sí | Nulo = etapa universal, aplica a los 3 procesos. PENDIENTE\_PAGO y EN\_VERIFICACION\_PAGO apuntan a Examen Supletorio |

## ETAPA\_ETIQUETA\_ROL

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| Etapa\_uuid | varchar(100) | PK (compuesta), FK -> ETAPA\_SOLICITUD\_ACADEMICA (fk\_etiqueta\_etapa) | No | Etapa que se traduce |
| rol | enum('ESTUDIANTE','FUNCIONARIO','DECANO') | PK (compuesta) | No | Rol que ve la etiqueta |
| etiqueta | varchar(60) |  | No | Texto que ve ese rol para esa etapa. Una etapa sin fila para un rol no es visible para ese rol (el Decano no tiene fila en RADICADA) |

## SOLICITUD\_ACADEMICA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidSolicitudAcademica | varchar(100) | PK | No | Identificador de la solicitud |
| radicado | varchar(20) | UK (uk\_solacad\_radicado) | No | Identificador legible: AAAA-CM-0001, AAAA-CA-0001 o AAAA-ES-0001 (año, tipo y consecutivo de cuatro dígitos por año y tipo) |
| Estudiante\_uuid | varchar(100) | FK -> ESTUDIANTE (fk\_solacad\_estudiante) | No | Estudiante que radica |
| TipoSolicitudAcademica\_uuid | varchar(100) | FK -> TIPO\_SOLICITUD\_ACADEMICA (fk\_solacad\_tiposolicitud) | No | Tipo de trámite |
| fechaCreacion | datetime |  | No | Fecha de radicación. Valor por defecto CURRENT\_TIMESTAMP; el backend la escribe con la hora de Colombia |
| Etapa\_uuid | varchar(100) | FK -> ETAPA\_SOLICITUD\_ACADEMICA (fk\_solacad\_etapa) | No | Etapa interna actual |

## SOLICITUD\_CANCELACION\_MATRICULA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| SolicitudAcademica\_uuid | varchar(100) | PK, FK -> SOLICITUD\_ACADEMICA (fk\_solcm\_solacad) | No | Comparte identificador con la cabecera |
| motivoCancelacion | varchar(255) |  | No | Motivo alegado por el estudiante |

## SOLICITUD\_CANCELACION\_ASIGNATURA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| SolicitudAcademica\_uuid | varchar(100) | PK, FK -> SOLICITUD\_ACADEMICA (fk\_solca\_solacad) | No | Comparte identificador con la cabecera |
| motivoCancelacion | varchar(255) |  | No | Motivo alegado por el estudiante |

## SOLICITUD\_EXAMEN\_SUPLETORIO

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| SolicitudAcademica\_uuid | varchar(100) | PK, FK -> SOLICITUD\_ACADEMICA (fk\_solsup\_solacad) | No | Comparte identificador con la cabecera |
| AsignaturaMatriculada\_uuid | varchar(100) | FK -> ASIGNATURA\_MATRICULADA (fk\_solsup\_asigmat) | No | Asignatura del examen no presentado |
| fechaExamenNoPresentado | datetime |  | No | Fecha del examen original, guardada a las 00:00 |
| fechaAcordadaExamen | datetime |  | Sí | En desuso desde T12.28 (P52): el paso de aprobar el comprobante ya no pide fecha y nada la lee ni la escribe. Se conserva para no perder datos anteriores |
| tipoCausa | enum('cruce','otra') |  | No | Causa de la no presentación |
| Docente\_uuid | varchar(100) | FK -> usuarios (uuidUsuario) (fk\_solsup\_docente) | Sí | Profesor, usuario con rol `Docente`, que el Funcionario elige al aprobar el comprobante de pago (P53). Nulo hasta ese paso y si el pago se rechaza |

## SOLICITUD\_SUPLETORIO\_CRUCE\_ASIGNATURA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| SolicitudAcademica\_uuid | varchar(100) | PK, FK -> SOLICITUD\_EXAMEN\_SUPLETORIO (fk\_solcruce\_solsup) | No | Existe solo si tipoCausa = 'cruce' |
| AsignaturaMatriculadaCruzada\_uuid | varchar(100) | FK -> ASIGNATURA\_MATRICULADA (fk\_solcruce\_asigmat) | No | Asignatura con la que se cruza |
| fechaExamenCruzada | datetime |  | No | Fecha del examen cruzado, guardada a las 00:00 |
| horaExamenCruzada | varchar(10) |  | No | Hora del examen cruzado en formato HH:mm |

## SITUACION\_ACADEMICA\_ASIGNATURA

Catálogo fijo con los códigos R0-R3 del formato oficial PA-GA-4.2-FOR-9. Se
siembra una sola vez y se reutiliza tanto para "situación en la matrícula"
como "situación al cancelar" en `ASIGNATURA_SOLICITUD_ACADEMICA`.

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidSituacionAcademica | varchar(100) | PK | No | Identificador de la situación |
| codigo | varchar(10) |  | No | R0, R1, R2 o R3 |
| nombre | varchar(100) |  | No | R0 No cursada, R1 Cursada por primera vez, R2 Cursada por segunda vez, R3 Cursada por tercera vez |

## ASIGNATURA\_SOLICITUD\_ACADEMICA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidAsignaturaSolicitud | varchar(100) | PK | No | Identificador de la fila |
| SolicitudAcademica\_uuid | varchar(100) | FK -> SOLICITUD\_ACADEMICA (fk\_asigsol\_solacad) | No | Solicitud a la que pertenece |
| AsignaturaMatriculada\_uuid | varchar(100) | FK -> ASIGNATURA\_MATRICULADA (fk\_asigsol\_asigmat) | No | Asignatura incluida |
| numeroFaltas | varchar(20) |  | Sí | Opcional desde T12.28 (P50): nulo si el funcionario no lo registra. Si es numérico debe ser un entero mayor o igual a 0 y se guarda normalizado (`03` queda `3`); si no es numérico se guarda el texto sin espacios sobrantes |
| nota | varchar(20) |  | Sí | Opcional desde T12.28 (P50). Si es numérica (con punto o coma) debe estar entre 0.0 y 5.0 con un decimal y se guarda con punto y un decimal (`3,5` queda `3.5`); si no es numérica se guarda el texto. La regla "nota menor a 3.0 no puede cumplir" solo aplica a notas numéricas |
| SituacionMatricula\_uuid | varchar(100) | FK -> SITUACION\_ACADEMICA\_ASIGNATURA (fk\_asigsol\_situmatricula) | Sí | Opcional desde T12.28 (P50); la registra el Funcionario al remitir |
| SituacionCancelar\_uuid | varchar(100) | FK -> SITUACION\_ACADEMICA\_ASIGNATURA (fk\_asigsol\_situcancelar) | Sí | Opcional desde T12.28 (P50, P51). Solo la registra el Funcionario, al remitir o en el ajuste de la respuesta de Cancelación de Asignatura; el Decano no la registra. En Cancelación de Asignatura solo en las aprobadas |
| cumpleCondiciones | tinyint(1) |  | Sí | Solo Cancelación de Asignatura: el Funcionario confirma que verificó las condiciones de la sección 4.2 de `etapas-por-proceso.md`. Nulo hasta evaluación |
| observacionEvaluacion | varchar(255) |  | Sí | Solo Cancelación de Asignatura: obligatoria si la asignatura no cumple las condiciones. La ven el Funcionario y el Decano, nunca el Estudiante |
| aprobadaPorDecano | tinyint(1) |  | Sí | Solo Cancelación de Asignatura: decisión del Decano sobre esa asignatura (P14). Nulo hasta su decisión |
| observacionDecision | varchar(255) |  | Sí | Solo Cancelación de Asignatura: la escribe el Funcionario en el ajuste de una asignatura rechazada al responder, opcional (P45, P47). La ven el Funcionario y el Decano, y el Estudiante en APROBADA o RECHAZADA |

## TIPO\_ANEXO\_ACADEMICO

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidTipoAnexoAcademico | varchar(100) | PK | No | Identificador del tipo de anexo |
| TipoSolicitudAcademica\_uuid | varchar(100) | FK -> TIPO\_SOLICITUD\_ACADEMICA (fk\_tipoanexo\_tiposolicitud) | No | Proceso al que pertenece este anexo |
| nombre | varchar(150) |  | No | Nombre del anexo requerido. El recibo, el comprobante, el soporte de justificación y el formato del docente cruzado se reconocen por este nombre sembrado |
| formatosPermitidos | varchar(60) |  | No | Extensiones separadas por coma, en minúscula y sin espacios. Ej. "pdf,jpg,png" |
| obligatorio | tinyint(1) |  | No | 1 = obligatorio, 0 = opcional. Valor por defecto 1 |

## ANEXO\_ACADEMICO

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidAnexoAcademico | varchar(100) | PK | No | Identificador del archivo adjunto |
| SolicitudAcademica\_uuid | varchar(100) | FK -> SOLICITUD\_ACADEMICA (fk\_anexo\_solacad) | No | Solicitud a la que se adjunta |
| TipoAnexoAcademico\_uuid | varchar(100) | FK -> TIPO\_ANEXO\_ACADEMICO (fk\_anexo\_tipoanexo) | Sí | Nulo = soporte libre, sin requisito fijo (solo en las cancelaciones) |
| nombreArchivo | varchar(255) |  | No | Nombre original del archivo subido, sin ruta ni caracteres de control (solo para mostrar; en disco se guarda con un nombre generado) |
| urlArchivo | varchar(400) |  | No | Ubicación del archivo, con el nombre generado por el sistema |
| tipoArchivo | varchar(100) |  | No | Tipo de contenido deducido de la extensión validada (application/pdf, image/jpeg, image/png) |
| tamanioBytes | bigint |  | No | Tamaño del archivo en bytes (máximo 5 MB) |
| Usuario\_uuid | varchar(100) | FK -> usuarios (uuidUsuario) (fk\_anexo\_usuario) | No | Quién subió el archivo |
| fechaSubida | datetime |  | No | Fecha y hora de la subida. Valor por defecto CURRENT\_TIMESTAMP |

## HISTORIAL\_SOLICITUD\_ACADEMICA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidHistorial | varchar(100) | PK | No | Identificador del evento |
| SolicitudAcademica\_uuid | varchar(100) | FK -> SOLICITUD\_ACADEMICA (fk\_historial\_solacad) | No | Solicitud sobre la que ocurrió la acción |
| Usuario\_uuid | varchar(100) | FK -> usuarios (uuidUsuario) (fk\_historial\_usuario) | No | Quién ejecutó la acción |
| accion | varchar(150) |  | No | Código de la acción de la máquina de etapas: RADICAR, RECHAZAR\_FUNCIONARIO, REMITIR\_DECANO, APROBAR\_DECANO, RECHAZAR\_DECANO, ENVIAR\_RESPUESTA, ENVIAR\_RECIBO, SUBIR\_COMPROBANTE, APROBAR\_COMPROBANTE o RECHAZAR\_COMPROBANTE. La tabla no guarda la etapa: la API la reconstruye (decisión P18) |
| observaciones | varchar(500) |  | Sí | Observación de la acción, cuando aplica (máximo 500 caracteres). En `REMITIR_DECANO`, `APROBAR_DECANO` y `RECHAZAR_DECANO` es opcional y solo la ven el Funcionario y el Decano: la API se la entrega vacía al Estudiante. En `ENVIAR_RESPUESTA` y en `APROBAR_COMPROBANTE` (P52) es el mensaje opcional del Funcionario al Estudiante, que sí lo ve |
| fecha | datetime |  | No | Fecha y hora del evento, con precisión de segundos. Valor por defecto CURRENT\_TIMESTAMP |

## RESOLUCION\_ACADEMICA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| SolicitudAcademica\_uuid | varchar(100) | PK, FK -> SOLICITUD\_ACADEMICA (fk\_resolucion\_solacad) | No | Solo existe para Cancelación de Matrícula y Cancelación de Asignatura; una por solicitud |
| urlArchivo | varchar(400) |  | No | Ubicación del escaneo de la Resolución firmada físicamente |
| nombreArchivo | varchar(255) |  | No | Nombre del archivo subido |
| fechaSubida | datetime |  | No | Fecha en que se subió el escaneo. Valor por defecto CURRENT\_TIMESTAMP |
| FuncionarioAcademico\_uuid | varchar(100) | FK -> FUNCIONARIO\_ACADEMICO (fk\_resolucion\_funcionario) | No | Funcionario que subió el escaneo |

## NOTIFICACION\_DOCENTE

Propia de la extensión (el sistema de Julián no tiene notificaciones). Una
fila por cada Examen Supletorio cuyo pago aprueba el Funcionario: avisa al
profesor elegido (P53). Solo la ve el docente destinatario.

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| idNotificacion | bigint | PK, AUTO\_INCREMENT | No | Identificador interno |
| uuidNotificacion | varchar(100) | UNIQUE (uk\_notifdoc\_uuid) | No | Identificador que usa la API |
| SolicitudAcademica\_uuid | varchar(100) | FK -> SOLICITUD\_ACADEMICA (fk\_notifdoc\_solacad) | No | Solicitud de Examen Supletorio notificada |
| Docente\_uuid | varchar(100) | FK -> usuarios (uuidUsuario) (fk\_notifdoc\_usuario) | No | Profesor destinatario |
| titulo | varchar(255) |  | No | Texto armado con el estado final de la solicitud y su radicado, por ejemplo `Examen supletorio aprobado, radicado 2026-ES-0001` |
| fechaCreacion | datetime |  | No | Fecha y hora en que se aprobó el pago, en hora de Colombia |
| fechaLectura | datetime |  | Sí | Primera vez que el docente abrió la notificación; nula mientras no la lea |

## Migración de una base con datos anteriores a PROGRAMA\_ACADEMICO

`docs/database/migracion_programas_asignaturas.sql` pone al día una base que
ya tiene estudiantes y asignaturas sin programa. Es idempotente: se puede
correr varias veces y también sobre una base donde el backend nuevo ya
arrancó y Hibernate agregó las columnas vacías. Hace, en este orden:

1. Crea `PROGRAMA_ACADEMICO` si no existe y siembra los cinco programas.
2. Agrega `ESTUDIANTE.ProgramaAcademico_uuid` y asigna a cada estudiante el
   programa cuyo nombre coincide con su texto `programaAcademico` (con la
   collation de la base, sin distinguir mayúsculas ni tildes). Los que no
   coinciden quedan en Ingeniería de Sistemas. La deja obligatoria, crea
   `fk_estudiante_programa` y borra la columna de texto `programaAcademico`.
3. Agrega `ASIGNATURA.ProgramaAcademico_uuid`, pasa a Ingeniería de Sistemas
   todas las asignaturas que aún no tienen programa válido y les da los
   códigos `ISI-001`, `ISI-002`, ... en el orden de su código anterior. Las
   matrículas y solicitudes apuntan por uuid, así que no se pierden. La deja
   obligatoria y crea `fk_asignatura_programa`.
4. Deja `ultimoConsecutivoAsignatura` de cada programa en el mayor
   consecutivo usado por sus asignaturas.

Cómo aplicarla con Docker, desde `backend/` (PowerShell), con el backend
detenido y la base arriba:

```
docker compose stop solicitudes-consejo-de-facultad
docker compose exec -T cfiet_database sh -c "mysqldump -u root -pmysql --default-character-set=utf8mb4 --single-transaction cfiet > /tmp/respaldo_cfiet.sql"
docker compose cp cfiet_database:/tmp/respaldo_cfiet.sql ./respaldo_cfiet.sql
docker compose cp ../docs/database/migracion_programas_asignaturas.sql cfiet_database:/tmp/migracion.sql
docker compose exec -T cfiet_database mysql -u root -pmysql --default-character-set=utf8mb4 cfiet -e "source /tmp/migracion.sql"
```

Sin Docker, contra MySQL en `localhost:3307` (usuario `root`, clave
`mysql`): `mysql -h 127.0.0.1 -P 3307 -u root -pmysql
--default-character-set=utf8mb4 cfiet < docs/database/migracion_programas_asignaturas.sql`.

Después se reconstruye y se levanta el backend (`./mvnw clean install
-DskipTests` en `backend/solicitudes` y `docker compose up --build` en
`backend/`). Si el backend nuevo arranca sin la migración, no falla al
iniciar: Hibernate crea `PROGRAMA_ACADEMICO` vacía, agrega las dos columnas
con texto vacío y deja en el log dos WARN porque no puede crear las llaves
foráneas. Mientras tanto la lista de programas sale vacía, las asignaturas
viejas no aparecen y los listados de estudiantes responden 500. Correr la
migración después corrige ese estado.

Una base nueva no necesita la migración: Hibernate crea las tablas y los
programas se siembran con `seed-procesos-academicos.sql`.

## Migración de cargo, situaciones, faltas y nota (T12.1)

`docs/database/migracion_cargo_situaciones_notas.sql` pone al día una base
anterior a T12.1. Es idempotente y no borra datos. Hace, en este orden:

1. Renombra `FUNCIONARIO_ACADEMICO.dependencia` a `cargo`. Si el backend
   nuevo ya arrancó y Hibernate creó `cargo` vacía, copia `dependencia` en
   las filas sin cargo y borra `dependencia`.
2. Deja la `facultad` de todos los estudiantes en `FIET`.
3. Cambia `ASIGNATURA_SOLICITUD_ACADEMICA.numeroFaltas` y `nota` a
   `varchar(20)`. Los valores existentes se conservan como texto (`4.0`,
   `0`). Hibernate no cambia el tipo de una columna existente, por eso la
   migración es obligatoria en bases con datos.
4. Renombra las situaciones por código: R0 No cursada, R1 Cursada por
   primera vez, R2 Cursada por segunda vez, R3 Cursada por tercera vez. Los
   uuid no cambian, así que una fila que ya apuntaba a R1 pasa a mostrarse
   como "Cursada por primera vez".
5. Cambia las etiquetas del rol `FUNCIONARIO` en `APROBADA_POR_DECANO`
   ("Aprobada por el Decano") y `RECHAZADA_POR_DECANO` ("Rechazada por el
   Decano"). Con P46 de `etapas-por-proceso.md` esas dos pasan a "Pendiente
   Notificar" y `EN_VERIFICACION_PAGO` a "Recibo Pagado"; las dos sentencias
   `UPDATE` al final de `seed-procesos-academicos.sql` ponen al día una base
   existente.

Se aplica igual que la anterior, antes de levantar el backend nuevo y después
de sacar un respaldo:

```
docker compose cp ../docs/database/migracion_cargo_situaciones_notas.sql cfiet_database:/tmp/migracion_t12.sql
docker compose exec -T cfiet_database mysql -u root -pmysql --default-character-set=utf8mb4 cfiet -e "source /tmp/migracion_t12.sql"
```
