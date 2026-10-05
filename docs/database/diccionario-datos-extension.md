# Diccionario de Datos

Detalle campo por campo de las 18 tablas nuevas de la extensión. `usuarios`,
`usuariosLivianos` y `tiposUsuario` son de Julián Camacho (nombres reales,
verificados contra el código de `back-fiet-sc`, no un supuesto) y no se
listan aquí porque no se modifican.

## ESTUDIANTE

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| Usuario\_uuid | varchar | PK, FK -> usuarios (uuidUsuario) | No | Comparte identificador con Usuario |
| codigoEstudiantil | varchar |  | No | Código estudiantil |
| programaAcademico | varchar |  | No | Programa académico que cursa |
| semestre | varchar |  | No | Semestre actual |
| facultad | varchar |  | No | Facultad a la que pertenece |

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
| Usuario\_uuid | varchar | PK, FK -> usuarios (uuidUsuario) | No | Comparte identificador con Usuario |
| dependencia | varchar |  | No | Dependencia u oficina a la que pertenece el Técnico Administrativo de Procesos Académicos; no distingue roles |

## ASIGNATURA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidAsignatura | varchar | PK | No | Identificador de la materia |
| codigoAsignatura | varchar | UK (uk\_asignatura\_codigo) | No | Código de la materia, único en el catálogo |
| nombreAsignatura | varchar |  | No | Nombre de la materia |

## ASIGNATURA\_MATRICULADA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidAsignaturaMatriculada | varchar | PK | No | Identificador de la matrícula puntual |
| Estudiante\_uuid | varchar | FK -> ESTUDIANTE | No | Estudiante que la tiene matriculada |
| Asignatura\_uuid | varchar | FK -> ASIGNATURA | No | Materia matriculada |
| grupo | varchar |  | No | Grupo en el que está matriculada |
| estado | varchar |  | No | activa, cancelada, aprobada o perdida |

## TIPO\_SOLICITUD\_ACADEMICA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidTipoSolicitudAcademica | varchar | PK | No | Identificador del tipo |
| nombre | varchar |  | No | Cancelación de Matrícula / Cancelación de Asignatura / Examen Supletorio |
| descripcion | varchar |  | Sí | Descripción del tipo |
| FuncionarioAcademico\_uuid | varchar | FK -> FUNCIONARIO\_ACADEMICO | No | Funcionario que atiende este tipo |

## ETAPA\_SOLICITUD\_ACADEMICA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidEtapa | varchar | PK | No | Identificador de la etapa |
| codigo | varchar |  | No | RADICADA, EN\_REVISION\_DECANO, APROBADA, RECHAZADA, etc. |
| TipoSolicitudAcademica\_uuid | varchar | FK -> TIPO\_SOLICITUD\_ACADEMICA | Sí | Nulo = etapa universal, aplica a los 3 procesos |

## ETAPA\_ETIQUETA\_ROL

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| Etapa\_uuid | varchar | PK (compuesta), FK -> ETAPA\_SOLICITUD\_ACADEMICA | No | Etapa que se traduce |
| rol | enum | PK (compuesta) | No | ESTUDIANTE, FUNCIONARIO o DECANO |
| etiqueta | varchar |  | No | Texto que ve ese rol para esa etapa |

## SOLICITUD\_ACADEMICA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidSolicitudAcademica | varchar | PK | No | Identificador de la solicitud |
| Estudiante\_uuid | varchar | FK -> ESTUDIANTE | No | Estudiante que radica |
| TipoSolicitudAcademica\_uuid | varchar | FK -> TIPO\_SOLICITUD\_ACADEMICA | No | Tipo de trámite |
| fechaCreacion | datetime |  | No | Fecha de radicación |
| Etapa\_uuid | varchar | FK -> ETAPA\_SOLICITUD\_ACADEMICA | No | Etapa interna actual |

## SOLICITUD\_CANCELACION\_MATRICULA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| SolicitudAcademica\_uuid | varchar | PK, FK -> SOLICITUD\_ACADEMICA | No | Comparte identificador con la cabecera |
| motivoCancelacion | varchar |  | No | Motivo alegado por el estudiante |

## SOLICITUD\_CANCELACION\_ASIGNATURA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| SolicitudAcademica\_uuid | varchar | PK, FK -> SOLICITUD\_ACADEMICA | No | Comparte identificador con la cabecera |
| motivoCancelacion | varchar |  | No | Motivo alegado por el estudiante |

## SOLICITUD\_EXAMEN\_SUPLETORIO

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| SolicitudAcademica\_uuid | varchar | PK, FK -> SOLICITUD\_ACADEMICA | No | Comparte identificador con la cabecera |
| AsignaturaMatriculada\_uuid | varchar | FK -> ASIGNATURA\_MATRICULADA | No | Asignatura del examen no presentado |
| fechaExamenNoPresentado | datetime |  | No | Fecha del examen original |
| fechaAcordadaExamen | datetime |  | Sí | Fecha propuesta para el supletorio, nula hasta acordarse |
| tipoCausa | enum |  | No | 'cruce' u 'otra' |

## SOLICITUD\_SUPLETORIO\_CRUCE\_ASIGNATURA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| SolicitudAcademica\_uuid | varchar | PK, FK -> SOLICITUD\_EXAMEN\_SUPLETORIO | No | Existe solo si tipoCausa = 'cruce' |
| AsignaturaMatriculadaCruzada\_uuid | varchar | FK -> ASIGNATURA\_MATRICULADA | No | Asignatura con la que se cruza |
| fechaExamenCruzada | datetime |  | No | Fecha del examen cruzado |
| horaExamenCruzada | varchar |  | No | Hora del examen cruzado |

## SITUACION\_ACADEMICA\_ASIGNATURA

Catálogo fijo con los códigos R0-R3 del formato oficial PA-GA-4.2-FOR-9. Se
siembra una sola vez y se reutiliza tanto para "situación en la matrícula"
como "situación al cancelar" en `ASIGNATURA_SOLICITUD_ACADEMICA`.

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidSituacionAcademica | varchar | PK | No | Identificador de la situación |
| codigo | varchar |  | No | R0, R1, R2 o R3 |
| nombre | varchar |  | No | Cursada por primera / segunda / tercera / cuarta vez |

## ASIGNATURA\_SOLICITUD\_ACADEMICA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidAsignaturaSolicitud | varchar | PK | No | Identificador de la fila |
| SolicitudAcademica\_uuid | varchar | FK -> SOLICITUD\_ACADEMICA | No | Solicitud a la que pertenece |
| AsignaturaMatriculada\_uuid | varchar | FK -> ASIGNATURA\_MATRICULADA | No | Asignatura incluida |
| numeroFaltas | int |  | Sí | Nulo hasta que el funcionario evalúa |
| nota | decimal |  | Sí | Nulo hasta evaluación |
| SituacionMatricula\_uuid | varchar | FK -> SITUACION\_ACADEMICA\_ASIGNATURA | Sí | Nulo hasta evaluación |
| SituacionCancelar\_uuid | varchar | FK -> SITUACION\_ACADEMICA\_ASIGNATURA | Sí | Nulo hasta resolución del Decano |

## TIPO\_ANEXO\_ACADEMICO

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidTipoAnexoAcademico | varchar | PK | No | Identificador del tipo de anexo |
| TipoSolicitudAcademica\_uuid | varchar | FK -> TIPO\_SOLICITUD\_ACADEMICA | No | Proceso al que pertenece este anexo |
| nombre | varchar |  | No | Nombre del anexo requerido |
| formatosPermitidos | varchar |  | No | Ej. "pdf,jpg,png" |
| obligatorio | tinyint |  | No | 1 = obligatorio, 0 = opcional |

## ANEXO\_ACADEMICO

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidAnexoAcademico | varchar | PK | No | Identificador del archivo adjunto |
| SolicitudAcademica\_uuid | varchar | FK -> SOLICITUD\_ACADEMICA | No | Solicitud a la que se adjunta |
| TipoAnexoAcademico\_uuid | varchar | FK -> TIPO\_ANEXO\_ACADEMICO | Sí | Nulo = soporte libre, sin requisito fijo |
| nombreArchivo | varchar |  | No | Nombre del archivo subido |
| urlArchivo | varchar |  | No | Ubicación del archivo |

## HISTORIAL\_SOLICITUD\_ACADEMICA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidHistorial | varchar | PK | No | Identificador del evento |
| SolicitudAcademica\_uuid | varchar | FK -> SOLICITUD\_ACADEMICA | No | Solicitud sobre la que ocurrió la acción |
| Usuario\_uuid | varchar | FK -> usuarios (uuidUsuario) | No | Quién ejecutó la acción |
| accion | varchar |  | No | Rótulo corto de la acción |
| observaciones | varchar |  | Sí | Detalle o razón, cuando aplica |
| fecha | datetime |  | No | Fecha y hora del evento |

## RESOLUCION\_ACADEMICA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| SolicitudAcademica\_uuid | varchar | PK, FK -> SOLICITUD\_ACADEMICA | No | Solo existe para Cancelación de Matrícula y Cancelación de Asignatura |
| urlArchivo | varchar |  | No | Ubicación del escaneo de la Resolución firmada físicamente |
| nombreArchivo | varchar |  | No | Nombre del archivo subido |
| fechaSubida | datetime |  | No | Fecha en que se subió el escaneo |
| FuncionarioAcademico\_uuid | varchar | FK -> FUNCIONARIO\_ACADEMICO | No | Funcionario que subió el escaneo |
