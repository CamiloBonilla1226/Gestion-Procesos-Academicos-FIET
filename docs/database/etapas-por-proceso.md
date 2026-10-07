# Etapas, transiciones, anexos y plazos por proceso

Documento de reglas de los tres procesos académicos de la extensión. Complementa a `diccionario-datos-extension.md` (que dice qué se guarda) con lo que ese diccionario no dice: en qué etapas está una solicitud, quién puede moverla, qué se exige en cada paso, qué anexos lleva cada proceso y qué plazos aplican.

Las reglas salen de estas fuentes:

- Documentos de proceso: Cancelación de Matrícula, Cancelación de Materia y Examen Supletorio.
- Formatos oficiales PA-GA-4.2-FOR-14, PA-GA-4.2-FOR-9 y PM-FO-4-FOR-23, y procedimientos PA-GA-4.2-PR-7 y PM-FO-4-PR-28.
- Acuerdo 002 de 1988 (Reglamento Estudiantil), artículos 66 a 70 y capítulo de supletorios.
- Los prototipos `estudiante.html`, `funcionario.html` y los tres formularios, solo para vocabulario y pantallas.

Las historias de usuario HU-V2 están desactualizadas y no se tienen en cuenta. Donde un prototipo contradice una decisión de este documento, manda este documento.

Lo que no aparece en ninguna fuente no se inventa: se pregunta antes de decidir y la decisión queda en la sección 8. Hoy esa sección no tiene decisiones pendientes.

## 1. Actores

| Actor | Rol en la aplicación | Qué hace en estos procesos |
| --- | --- | --- |
| Estudiante | Estudiante | Radica la solicitud, adjunta anexos, sube el comprobante de pago (solo supletorio), descarga la Resolución escaneada (solo cancelaciones). |
| Funcionario Académico | Funcionario Académico | Revisa requisitos, evalúa las asignaturas, rechaza o remite al Decano, responde al estudiante, sube el escaneo de la Resolución y verifica el pago. Solo ve los tipos de solicitud que tiene asignados en `TIPO_SOLICITUD_ACADEMICA`. |
| Decano | Decano | Aprueba o rechaza lo que el Funcionario le remite. Es un Usuario común con rol Decano, sin fila en `FUNCIONARIO_ACADEMICO`. |

El Secretario General no interviene en estos tres procesos.

## 2. Modelo de estados

Cada solicitud tiene una etapa interna (`ETAPA_SOLICITUD_ACADEMICA`). De la etapa se deduce quién debe actuar (responsable actual) y qué texto ve cada rol (`ETAPA_ETIQUETA_ROL`). La decisión del Decano queda en la etapa misma (`APROBADA_POR_DECANO` o `RECHAZADA_POR_DECANO`), por eso no hace falta una columna de decisión.

| Código de etapa | Alcance | Responsable actual | Ve el Estudiante | Ve el Funcionario | Ve el Decano |
| --- | --- | --- | --- | --- | --- |
| RADICADA | Los 3 procesos | Funcionario | En trámite | Pendiente | no la ve |
| EN_REVISION_DECANO | Los 3 procesos | Decano | En trámite | En Gestión | Pendiente |
| APROBADA_POR_DECANO | Los 3 procesos | Funcionario | En trámite | Pendiente de Respuesta | Respondida |
| RECHAZADA_POR_DECANO | Los 3 procesos | Funcionario | En trámite | Pendiente de Respuesta | Respondida |
| PENDIENTE_PAGO | Solo Examen Supletorio | Estudiante | Pendiente de pago | En Gestión | Respondida |
| EN_VERIFICACION_PAGO | Solo Examen Supletorio | Funcionario | En verificación de pago | Pendiente de Verificación | Respondida |
| APROBADA | Los 3 procesos (final) | Ninguno | Aprobada | Respondida | Respondida |
| RECHAZADA | Los 3 procesos (final) | Ninguno | Rechazada | Respondida | Respondida |

Notas sobre las etiquetas:

- Las etiquetas del Estudiante y del Funcionario se tomaron de los prototipos `estudiante.html` y `funcionario.html`. Como son datos en `ETAPA_ETIQUETA_ROL`, cambiarlas no exige tocar código.
- Las etiquetas del Decano (Pendiente, En Gestión, Respondida) son una propuesta, porque no hay prototipo del Decano entre los documentos.
- Las etapas con alcance "Los 3 procesos" tienen `TipoSolicitudAcademica_uuid` nulo. Las de solo supletorio se enlazan al tipo Examen Supletorio.
- Etapas finales: APROBADA y RECHAZADA. Desde ellas no sale ninguna transición.

## 3. Transiciones permitidas

Toda transición exige que el usuario tenga el rol indicado, que la solicitud esté en la etapa de origen y, para el Funcionario, que el tipo de solicitud esté asignado a él. Toda transición escribe una fila en `HISTORIAL_SOLICITUD_ACADEMICA`. Cualquier otra combinación se rechaza.

### 3.1 Cancelación de Matrícula y Cancelación de Asignatura

| De | Acción | A | Actor | Se exige |
| --- | --- | --- | --- | --- |
| (nueva) | Radicar | RADICADA | Estudiante | Validaciones de la sección 4 y anexos de la sección 5. |
| RADICADA | Rechazar | RECHAZADA | Funcionario | Observación obligatoria y escaneo de la Resolución (PDF). La Resolución la firma el Decano a mano. |
| RADICADA | Remitir al Decano | EN_REVISION_DECANO | Funcionario | Cada asignatura evaluada: número de faltas, nota y situación en la matrícula; en Cancelación de Asignatura, además, si cumple las condiciones (P14). Observación opcional. Sin archivo de Resolución en este paso; si había un escaneo, se retira (P21). |
| EN_REVISION_DECANO | Aprobar | APROBADA_POR_DECANO | Decano | Situación al cancelar de cada asignatura (P9). En Cancelación de Asignatura, la decisión por asignatura (P14). |
| EN_REVISION_DECANO | Rechazar | RECHAZADA_POR_DECANO | Decano | Observación obligatoria. |
| APROBADA_POR_DECANO | Enviar respuesta | APROBADA | Funcionario | Escaneo de la Resolución firmada (PDF). Queda descargable para el Estudiante. |
| RECHAZADA_POR_DECANO | Enviar respuesta | RECHAZADA | Funcionario | Escaneo de la Resolución firmada (PDF). La observación del Decano llega al Estudiante. |

### 3.2 Examen Supletorio

| De | Acción | A | Actor | Se exige |
| --- | --- | --- | --- | --- |
| (nueva) | Radicar | RADICADA | Estudiante | Validaciones de la sección 4, plazo de 3 días hábiles y anexos de la sección 5. |
| RADICADA | Rechazar | RECHAZADA | Funcionario | Observación obligatoria. No genera Resolución. |
| RADICADA | Remitir al Decano | EN_REVISION_DECANO | Funcionario | Requisitos verificados. |
| EN_REVISION_DECANO | Aprobar | APROBADA_POR_DECANO | Decano | Ninguno. |
| EN_REVISION_DECANO | Rechazar | RECHAZADA_POR_DECANO | Decano | Observación obligatoria. |
| RECHAZADA_POR_DECANO | Enviar respuesta | RECHAZADA | Funcionario | Ninguno más. No genera Resolución. |
| APROBADA_POR_DECANO | Enviar recibo de pago | PENDIENTE_PAGO | Funcionario | Archivo del recibo obligatorio. |
| PENDIENTE_PAGO | Subir comprobante | EN_VERIFICACION_PAGO | Estudiante | Archivo del comprobante obligatorio. |
| EN_VERIFICACION_PAGO | Aprobar comprobante | APROBADA | Funcionario | Ninguno más. |
| EN_VERIFICACION_PAGO | Rechazar comprobante | RECHAZADA | Funcionario | Observación obligatoria. Ver punto abierto P4. |

Tras la aprobación final, el Funcionario notifica al docente y a DARCA fuera de la aplicación, tal como dice el documento del proceso.

No hay en las fuentes una acción de desistimiento del Estudiante, así que no se modela.

## 4. Reglas al radicar

Comunes a los tres procesos:

- El solicitante debe tener fila en `ESTUDIANTE`.
- Cada solicitud recibe un identificador único legible que funciona como radicado (sección 7).
- Cada estudiante puede tener una sola solicitud en curso por tipo (el prototipo bloquea con "Ya tienes una solicitud de este trámite en curso"). En curso significa en cualquier etapa distinta de APROBADA y RECHAZADA.
- Las asignaturas se eligen entre las `ASIGNATURA_MATRICULADA` del propio estudiante con estado `activa`. No se acepta código ni nombre digitado.
- Solo el estudiante dueño de la solicitud la ve.

### 4.1 Cancelación de Matrícula

- Motivo de la cancelación obligatorio.
- Se incluyen todas las asignaturas activas del estudiante (una fila en `ASIGNATURA_SOLICITUD_ACADEMICA` por cada una).
- Plazo: hasta el último día de clases del periodo (Acuerdo 002 de 1988, capítulo VIII). El sistema no valida fechas; la apertura y cierre del proceso se controla con el interruptor de la sección 9.
- Advertencia, solo en la interfaz: si el estudiante cursa su primer periodo de carrera, al cancelar solo podrá volver al programa por el proceso de admisión vigente. El prototipo pide confirmarla con una casilla.
- Al evaluar, el Funcionario aplica los criterios del artículo 2 del formato FOR-14 para la situación al cancelar: aprobada con nota definitiva igual o mayor a 3.0; perdida por faltas de asistencia; perdida si el promedio ponderado de parciales es menor a 2.0 o la nota final menor a 3.0; no cursada en los demás casos.

### 4.2 Cancelación de Asignatura

- Motivo de la cancelación obligatorio y al menos una asignatura.
- Plazo: antes de la terminación del periodo académico. El sistema no valida fechas; ver sección 9.
- Cuatro condiciones académicas (Acuerdo 002 de 1988, capítulo VIII, artículo 5, y formato FOR-9). Las verifica el Funcionario al revisar:
  1. La solicitud se hace antes de terminar el periodo académico.
  2. La asignatura no se cursa en calidad de repitente.
  3. No se violan condiciones de co-requisitos.
  4. La nota promedio es igual o mayor a 3.0.
- El resto de la matrícula permanece activa.
- El Decano aprueba o rechaza cada asignatura por separado (decisión P14 de la sección 8).

### 4.3 Examen Supletorio

- Una asignatura matriculada activa, la fecha del examen no presentado y la causa: `cruce` u `otra`.
- Plazo: máximo 3 días hábiles después de la fecha del examen al que no concurrió. El plazo se cuenta por separado para cada examen.
- Días hábiles son lunes a viernes. Los festivos no se descuentan (así lo hace el prototipo). Ver punto abierto P8.
- Si la causa es `cruce`: también la asignatura con la que se cruza (otra matriculada activa del estudiante), la fecha y la hora de ese examen. Se guarda en `SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA`.
- La fecha acordada para el supletorio puede quedar nula hasta acordarse.
- Si el Funcionario rechaza el comprobante de pago, el estudiante debe radicar una solicitud nueva (según el prototipo). Ver P4.

## 5. Anexos por proceso

Tamaño máximo por archivo: 5 MB, tal como validan los prototipos. Salvo que se indique otra cosa, los formatos permitidos son PDF, JPG y PNG. Los anexos se registran como filas de `TIPO_ANEXO_ACADEMICO` por proceso, con su columna `formatosPermitidos` y `obligatorio`.

### 5.1 Cancelación de Matrícula

Seis anexos obligatorios, sin excepción (el prototipo marcaba el 6 como opcional solo para probar la pantalla), tomados del formato FOR-14:

| N | Anexo | Entidad | Formatos |
| --- | --- | --- | --- |
| 1 | Paz y salvo | División de Bibliotecas | pdf |
| 2 | Paz y salvo | División de Deportes y Recreación | pdf |
| 3 | Paz y salvo | División de Salud Integral | pdf |
| 4 | Cupón de Confirmación de la Intervención Psicosocial | División de Salud Integral | pdf |
| 5 | Paz y salvo | División Financiera | pdf |
| 6 | Carné estudiantil o constancia de no trámite | DARCA | pdf, jpg, jpeg, png |

Además, un soporte libre del motivo, opcional (`TipoAnexoAcademico_uuid` nulo).

Vigencia: el formato FOR-14 dice que los paz y salvo valen 5 días calendario desde su expedición. El sistema no lo valida y no guarda fecha de expedición: el Funcionario revisa los PDF adjuntos junto con el formulario y decide.

El pago de 12.000 pesos del paz y salvo universitario y las gestiones con cada división ocurren fuera de la aplicación; la aplicación solo recibe los documentos resultantes.

### 5.2 Cancelación de Asignatura

Ningún documento oficial obligatorio. Se pide el motivo y, de forma opcional, un soporte libre que lo respalde (incapacidad, certificación laboral u otro), `TipoAnexoAcademico_uuid` nulo.

### 5.3 Examen Supletorio

| Anexo | Cuándo se exige | Quién lo sube | Formatos |
| --- | --- | --- | --- |
| Formato PM-FO-4-FOR-23 firmado por el docente que orienta la asignatura | Siempre | Estudiante, al radicar | pdf, jpg, png |
| Soporte de la justificación de la no presentación | Si la causa es `otra` | Estudiante, al radicar | pdf, jpg, png |
| Formato firmado por el docente de la asignatura con la que se cruza | Si la causa es `cruce` | Estudiante, al radicar | pdf, jpg, png |
| Recibo de pago | En la transición APROBADA_POR_DECANO a PENDIENTE_PAGO | Funcionario | pdf |
| Comprobante de pago | En la transición PENDIENTE_PAGO a EN_VERIFICACION_PAGO | Estudiante | pdf, jpg, png |

Los anexos condicionales no se pueden expresar con la columna `obligatorio` sola: se crean como tipos con `obligatorio` en 0 y el motor los exige según la causa. El recibo y el comprobante también son tipos de anexo propios del supletorio, exigidos por el motor en su transición.

### 5.4 Resolución escaneada

Solo para Cancelación de Matrícula y Cancelación de Asignatura. La Resolución es un documento físico firmado a mano por el Decano; en la aplicación solo vive su escaneo.

- Lo sube el Funcionario al cerrar el trámite: al rechazar él mismo o al enviar la respuesta final tras la decisión del Decano. Por eso se acepta en RADICADA, APROBADA_POR_DECANO y RECHAZADA_POR_DECANO (P21).
- Formato PDF. Se guarda en `RESOLUCION_ACADEMICA` con el funcionario que lo subió.
- El Estudiante la descarga desde el detalle de su solicitud cuando esta llega a APROBADA o RECHAZADA (P21).
- Examen Supletorio no produce Resolución.
- Las copias físicas de la Resolución se reparten en tres ejemplares fuera de la aplicación.

## 6. Plazos

| Proceso | Plazo | Quién lo controla |
| --- | --- | --- |
| Cancelación de Matrícula | Hasta el último día de clases del periodo | Sin validación de fecha en el sistema. Ver sección 9. |
| Cancelación de Asignatura | Antes de terminar el periodo académico | Sin validación de fecha en el sistema. Ver sección 9. |
| Examen Supletorio | 3 días hábiles después del examen no presentado | El sistema, con la fecha del examen que da el estudiante. |
| Paz y salvo de matrícula | 5 días calendario desde su expedición | El Funcionario, al revisar los PDF. El sistema no lo valida. |

## 7. Cambios al modelo de datos que se desprenden de este documento

El diccionario no tiene esta columna y el prototipo la usa:

- Identificador legible de la solicitud, que funciona como radicado. Se agrega `radicado` (varchar, único) a `SOLICITUD_ACADEMICA`, con formato `AAAA-CM-0001`, `AAAA-CA-0001` o `AAAA-ES-0001` (año, tipo y consecutivo; un contador por año y tipo). El `uuid` sigue siendo la llave primaria interna.

No se agrega fecha de expedición a los anexos ni fechas límite del periodo.

## 8. Decisiones tomadas

Confirmadas por el autor:

- El radicado es el identificador de cada solicitud.
- El sistema no valida la vigencia de los paz y salvo; lo hace el Funcionario.
- No hay fechas límite de periodo en el sistema.
- Todos los documentos oficiales de matrícula son obligatorios, incluido el de DARCA, y el soporte de justificación del supletorio es obligatorio cuando la causa es `otra`.
- Las historias de usuario no se usan; la Resolución solo se escanea y se sube al cerrar el trámite (sección 5.4).

Propuestas adoptadas por defecto (cambiables antes de implementar la tarea que las usa):

| N | Tema | Se adopta |
| --- | --- | --- |
| P4 | Rechazo del comprobante de pago | La solicitud se cierra como RECHAZADA con observación obligatoria; el estudiante radica una nueva. |
| P8 | Festivos en los días hábiles | Lunes a viernes sin festivos. Limitación aceptada del prototipo. |
| P9 | Quién registra la situación al cancelar de cada asignatura | El Decano, al decidir. |
| P11 | Las cuatro condiciones de Cancelación de Asignatura | La lista de la sección 4.2. |
| P12 | Tamaño del escaneo de la Resolución y del recibo | 5 MB, igual que los demás archivos. |
| P13 | Soporte libre del motivo en matrícula y asignatura | Sigue siendo opcional. Solo los documentos oficiales son obligatorios. |
| P14 | Decisión del Decano en Cancelación de Asignatura | Confirmada por el autor; supera cualquier texto anterior. Aprobación parcial: el Decano aprueba o rechaza cada asignatura por separado. Al remitir, el Funcionario confirma por asignatura si cumple las condiciones de la sección 4.2 (una nota menor a 3.0 no puede marcarse como que cumple; si no cumple, la observación es obligatoria) y al menos una debe cumplir. El Decano solo aprueba asignaturas que cumplen, registra la situación al cancelar de las aprobadas (P9) y debe dar observación de las rechazadas; con al menos una aprobada la solicitud pasa a APROBADA_POR_DECANO, y si no aprueba ninguna usa el rechazo de la solicitud completa. Al enviar la respuesta aprobada solo se cancelan las asignaturas aprobadas que sigan activas. Cancelación de Matrícula no cambia. Se guarda en `ASIGNATURA_SOLICITUD_ACADEMICA` (`cumpleCondiciones`, `observacionEvaluacion`, `aprobadaPorDecano`, `observacionDecision`). |

Decisiones de implementación, tomadas al construir el backend y registradas en `change.md`. Describen lo que hace el código hoy:

| N | Tema | Se adopta |
| --- | --- | --- |
| P15 | Lo que ve el Estudiante en Cancelación de Asignatura | Antes de APROBADA o RECHAZADA ve de cada asignatura solo el uuid de la fila, el código y el nombre. En APROBADA o RECHAZADA ve además `aprobadaPorDecano` y `observacionDecision`. Nunca ve faltas, nota, situaciones, `cumpleCondiciones` ni `observacionEvaluacion`. El Funcionario asignado y el Decano ven todo. |
| P16 | Observación del Decano al aprobar el supletorio | Opcional. Al rechazar sigue siendo obligatoria. En las cancelaciones la aprobación del Decano no lleva observación. |
| P17 | Fecha acordada del supletorio | La registra el Funcionario asignado al aprobar el comprobante (EN_VERIFICACION_PAGO a APROBADA). Es opcional, no puede ser anterior a la fecha del examen no presentado y se guarda a las 00:00 en `fechaAcordadaExamen`. |
| P18 | Código de etapa y reconstrucción del historial | Bandejas, detalles e historial devuelven, además de la etiqueta del rol, el código estable de la etapa (`etapaCodigo`). El historial no guarda la etapa y su fecha tiene precisión de segundos, así que la etapa de cada fila se reconstruye encadenando las transiciones de la sección 3 desde RADICAR, sin depender del orden por fecha. Una acción que no encaja en el recorrido queda sin código. |
| P19 | Visibilidad por rol | El rol con que actúa un usuario lo decide el servidor: dueño con rol Estudiante, si no Funcionario asignado al tipo, si no Decano. Un rol solo ve las solicitudes cuya etapa tiene etiqueta para él en `ETAPA_ETIQUETA_ROL`, en bandejas, detalle, historial y descargas. Una solicitud ajena o sin etiqueta responde igual que una inexistente. |
| P20 | Quién sube cada anexo y cuándo | Los anexos del Estudiante y los soportes libres los sube el Estudiante dueño en RADICADA; el recibo de pago, el Funcionario asignado en APROBADA_POR_DECANO; el comprobante, el Estudiante dueño en PENDIENTE_PAGO. Examen Supletorio no admite soportes libres. No se limita la cantidad de archivos por tipo. jpg y jpeg son el mismo formato, y además de la extensión se valida la firma del contenido (pdf, png, jpg). |
| P21 | Escaneo de la Resolución | Solo en las cancelaciones. Lo sube el Funcionario asignado, en PDF, en RADICADA, APROBADA_POR_DECANO o RECHAZADA_POR_DECANO. Subirlo otra vez reemplaza el anterior; en etapa final ya no se sube ni se reemplaza. Remitir al Decano retira el escaneo que hubiera. El Estudiante dueño lo descarga solo en etapa final; el Funcionario asignado y el Decano, en cualquier etapa. |
| P22 | Situación al cancelar en Cancelación de Matrícula | El Estudiante la ve solo cuando la solicitud está en etapa final; antes llega vacía. |
| P23 | Asignaturas que se cancelan al aprobar | Al enviar la respuesta aprobada de Cancelación de Matrícula pasan a `cancelada` las asignaturas de la solicitud que sigan `activa`; las que tengan otro estado no se tocan. Ninguna acción del supletorio cambia asignaturas. |
| P24 | Concurrencia al radicar | La regla de una solicitud en curso por tipo se valida con una consulta previa, sin bloqueo. El radicado se calcula como el último del año y tipo más uno; si dos radicaciones chocan, la segunda se rechaza con "No se pudo generar el radicado, intente de nuevo" y no deja nada escrito. |
| P25 | Conteo del plazo del supletorio | Con la hora de Colombia, se cuentan los días lunes a viernes posteriores al examen hasta hoy inclusive. El mismo día cuenta 0 y un examen en fin de semana empieza a contar el lunes. La fecha del examen no puede ser posterior a hoy. |
| P26 | Límites de texto | Motivo de cancelación: 255 caracteres. Observación de una acción: 500. Observación de evaluación y de decisión por asignatura: 255. Hora del examen cruzado: formato HH:mm. |
| P27 | Remitir el supletorio al Decano | El Funcionario debe confirmar `requisitosVerificados`; sin esa confirmación se rechaza la acción y se le indica que rechace la solicitud. La confirmación se revisa después del actor y la etapa. |

## 9. Funcionalidad opcional, al final del proyecto

Interruptor para abrir y cerrar cada proceso: el Decano activa o desactiva un tipo de solicitud para los estudiantes, porque el periodo no tiene fecha fija y cambia. Con el proceso desactivado, el estudiante no puede radicar. No es obligatoria y solo se construye si alcanza el tiempo. Mientras no exista, todos los procesos están activos.
