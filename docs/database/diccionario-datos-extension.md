# Diccionario de Datos

Detalle campo por campo de las 16 tablas nuevas de la extensión. `USUARIO` y `TIPO_USUARIO` son de Julián Camacho y no se listan aquí.

## ESTUDIANTE

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| Usuario\_uuid | varchar | PK, FK -> USUARIO | No | Comparte identificador con Usuario |
| codigoEstudiantil | varchar |  | No | Código estudiantil |
| programaAcademico | varchar |  | No | Programa académico que cursa |
| semestre | varchar |  | No | Semestre actual |
| facultad | varchar |  | No | Facultad a la que pertenece |

## FUNCIONARIO\_ACADEMICO

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| Usuario\_uuid | varchar | PK, FK -> USUARIO | No | Comparte identificador con Usuario |

## ASIGNATURA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidAsignatura | varchar | PK | No | Identificador de la materia |
| codigoAsignatura | varchar |  | No | Código de la materia |
| nombreAsignatura | varchar |  | No | Nombre de la materia |

## ASIGNATURA\_MATRICULADA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidAsignaturaMatriculada | varchar | PK | No | Identificador de la matrícula puntual |
| Estudiante\_uuid | varchar | FK -> ESTUDIANTE | No | Estudiante que la tiene matriculada |
| Asignatura\_uuid | varchar | FK -> ASIGNATURA | No | Materia matriculada |
| grupo | varchar |  | No | Grupo en el que está matriculada |
| estado | tinyint |  | No | 1 = activa, 0 = cancelada |

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
| rol | varchar | PK (compuesta) | No | ESTUDIANTE, FUNCIONARIO o DECANO |
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
| fechaAcordadaExamen | datetime |  | No | Fecha propuesta para el supletorio |
| tipoCausa | varchar |  | No | 'cruce' u 'otra' |

## SOLICITUD\_SUPLETORIO\_CRUCE\_ASIGNATURA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| SolicitudAcademica\_uuid | varchar | PK, FK -> SOLICITUD\_EXAMEN\_SUPLETORIO | No | Existe solo si tipoCausa = 'cruce' |
| AsignaturaMatriculadaCruzada\_uuid | varchar | FK -> ASIGNATURA\_MATRICULADA | No | Asignatura con la que se cruza |
| fechaExamenCruzada | datetime |  | No | Fecha del examen cruzado |
| horaExamenCruzada | varchar |  | No | Hora del examen cruzado |

## ASIGNATURA\_SOLICITUD\_ACADEMICA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidAsignaturaSolicitud | varchar | PK | No | Identificador de la fila |
| SolicitudAcademica\_uuid | varchar | FK -> SOLICITUD\_ACADEMICA | No | Solicitud a la que pertenece |
| AsignaturaMatriculada\_uuid | varchar | FK -> ASIGNATURA\_MATRICULADA | No | Asignatura incluida |
| numeroFaltas | int |  | Sí | Nulo hasta que el funcionario evalúa |
| nota | decimal |  | Sí | Nulo hasta evaluación |
| situacionMatricula | varchar |  | Sí | Texto libre; nulo hasta evaluación |
| situacionCancelar | varchar |  | Sí | Texto libre; nulo hasta resolución del Decano |

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
| Usuario\_uuid | varchar | FK -> USUARIO | No | Quién ejecutó la acción |
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
