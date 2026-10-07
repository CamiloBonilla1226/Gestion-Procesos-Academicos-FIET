# Diccionario de Datos

Detalle campo por campo de las 18 tablas nuevas de la extensión. `usuarios`,
`usuariosLivianos` y `tiposUsuario` son de Julián Camacho (nombres reales,
verificados contra el código de `back-fiet-sc`, no un supuesto) y no se
listan aquí porque no se modifican.

Los tipos, nulos, valores por defecto, llaves únicas y llaves foráneas de esta
página coinciden con `script_bd_extension.sql` y con `DESCRIBE` sobre la base
que crea Hibernate (`docker compose exec cfiet_database mysql -u root -pmysql
cfiet`). Las llaves foráneas llevan el nombre que se indica entre paréntesis.
Las reglas de negocio que usan estas columnas (etapas, transiciones, anexos y
plazos) están en `etapas-por-proceso.md`.

## ESTUDIANTE

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| Usuario\_uuid | varchar(100) | PK, FK -> usuarios (uuidUsuario) (fk\_estudiante\_usuario) | No | Comparte identificador con Usuario |
| codigoEstudiantil | varchar(45) | UK (uk\_estudiante\_codigo) | No | Código estudiantil, único entre estudiantes |
| programaAcademico | varchar(100) |  | No | Programa académico que cursa |
| semestre | varchar(10) |  | No | Semestre actual |
| facultad | varchar(100) |  | No | Facultad a la que pertenece |

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
| dependencia | varchar(100) |  | No | Dependencia u oficina a la que pertenece el Técnico Administrativo de Procesos Académicos; no distingue roles |

## ASIGNATURA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidAsignatura | varchar(100) | PK | No | Identificador de la materia |
| codigoAsignatura | varchar(45) | UK (uk\_asignatura\_codigo) | No | Código de la materia, único en el catálogo |
| nombreAsignatura | varchar(150) |  | No | Nombre de la materia |

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
| fechaAcordadaExamen | datetime |  | Sí | Fecha acordada para el supletorio, guardada a las 00:00. Nula hasta que el Funcionario la registra al aprobar el comprobante (decisión P17 de `etapas-por-proceso.md`) |
| tipoCausa | enum('cruce','otra') |  | No | Causa de la no presentación |

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
| nombre | varchar(100) |  | No | Cursada por primera / segunda / tercera / cuarta vez |

## ASIGNATURA\_SOLICITUD\_ACADEMICA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidAsignaturaSolicitud | varchar(100) | PK | No | Identificador de la fila |
| SolicitudAcademica\_uuid | varchar(100) | FK -> SOLICITUD\_ACADEMICA (fk\_asigsol\_solacad) | No | Solicitud a la que pertenece |
| AsignaturaMatriculada\_uuid | varchar(100) | FK -> ASIGNATURA\_MATRICULADA (fk\_asigsol\_asigmat) | No | Asignatura incluida |
| numeroFaltas | int |  | Sí | Nulo hasta que el funcionario evalúa; entero mayor o igual a 0 |
| nota | decimal(3,1) |  | Sí | Nulo hasta evaluación; de 0.0 a 5.0 con un decimal |
| SituacionMatricula\_uuid | varchar(100) | FK -> SITUACION\_ACADEMICA\_ASIGNATURA (fk\_asigsol\_situmatricula) | Sí | Nulo hasta evaluación |
| SituacionCancelar\_uuid | varchar(100) | FK -> SITUACION\_ACADEMICA\_ASIGNATURA (fk\_asigsol\_situcancelar) | Sí | Nulo hasta resolución del Decano; en Cancelación de Asignatura solo en las aprobadas |
| cumpleCondiciones | tinyint(1) |  | Sí | Solo Cancelación de Asignatura: el Funcionario confirma que verificó las condiciones de la sección 4.2 de `etapas-por-proceso.md`. Nulo hasta evaluación |
| observacionEvaluacion | varchar(255) |  | Sí | Solo Cancelación de Asignatura: obligatoria si la asignatura no cumple las condiciones |
| aprobadaPorDecano | tinyint(1) |  | Sí | Solo Cancelación de Asignatura: decisión del Decano sobre esa asignatura (P14). Nulo hasta su decisión |
| observacionDecision | varchar(255) |  | Sí | Solo Cancelación de Asignatura: obligatoria si el Decano rechaza la asignatura |

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
| observaciones | varchar(500) |  | Sí | Observación de la acción, cuando aplica (máximo 500 caracteres) |
| fecha | datetime |  | No | Fecha y hora del evento, con precisión de segundos. Valor por defecto CURRENT\_TIMESTAMP |

## RESOLUCION\_ACADEMICA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| SolicitudAcademica\_uuid | varchar(100) | PK, FK -> SOLICITUD\_ACADEMICA (fk\_resolucion\_solacad) | No | Solo existe para Cancelación de Matrícula y Cancelación de Asignatura; una por solicitud |
| urlArchivo | varchar(400) |  | No | Ubicación del escaneo de la Resolución firmada físicamente |
| nombreArchivo | varchar(255) |  | No | Nombre del archivo subido |
| fechaSubida | datetime |  | No | Fecha en que se subió el escaneo. Valor por defecto CURRENT\_TIMESTAMP |
| FuncionarioAcademico\_uuid | varchar(100) | FK -> FUNCIONARIO\_ACADEMICO (fk\_resolucion\_funcionario) | No | Funcionario que subió el escaneo |
