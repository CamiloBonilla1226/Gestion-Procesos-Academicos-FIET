# Contrato de la API de los procesos académicos

Este documento describe los 66 endpoints que la extensión agrega al backend:
programas académicos y catálogo de asignaturas, estudiantes, funcionarios académicos, catálogos
académicos, solicitudes académicas y los tres procesos (Cancelación de
Matrícula, Cancelación de Asignatura y Examen Supletorio). Es la misma lista
que comprueba `backend/pruebas/matriz-permisos.ps1`, y cada dato sale de los
controladores, DTO, casos de uso y `ConfiguracionSeguridad` del código.

Todas las rutas cuelgan de `/api/unicauca/fiet/consejo/` (con Docker, en
`http://localhost:8080/api/unicauca/fiet/consejo/`). En las tablas se omite
ese prefijo.

## 1. Convenciones

### 1.1 Autenticación

`POST sesiones` (de Julián, abierto) recibe `{"username": "...", "password":
"..."}` y responde `{"uuidUsuario": "...", "token": "..."}`. El resto de
llamadas lleva la cabecera `Authorization: Bearer <token>`. El token vence a
las 24 horas.

El backend deduce del token quién llama y con qué rol actúa en cada
solicitud. El cliente nunca envía el rol ni el uuid del actor.

Los roles que usan estos endpoints son `Secretario General` y `Decano` (de
Julián), `Estudiante` y `Funcionario Académico`. El rol `Funcionario` de
Julián solo llega a los GET de `catalogos-academicos`, que están abiertos a
cualquier usuario autenticado.

### 1.2 Respuestas de error

| Situación | HTTP | Cuerpo |
| --- | --- | --- |
| Sin token o con token inválido | 401 | `{"codigoError": "401", "mensaje": "Error, sin acceso", ...}` |
| Token vencido | 401 | `{"codigoError": "401", "mensaje": "Error, token expirado", ...}` |
| Rol que la ruta no admite | 403 | `{"codigoError": "403", "mensaje": "No tiene permisos para acceder a este recurso", ...}` |
| Regla de negocio | 500 | `{"codigoError": "<1 a 9>", "mensaje": "...", "httpCodigo": 500, "url": "...", "metodo": "..."}` |
| Validación de anotaciones del DTO (`@NotBlank`, `@Size`, `@Email`) | 400 | Mapa `{"campo": "mensaje"}` |
| Fallo de la base de datos al guardar | 500 | `{"mensaje": "Error insertando en la base de datos....", "error": "..."}`, sin `codigoError` |

Los errores de negocio llegan siempre como HTTP 500; el frontend debe mirar
`codigoError`:

| codigoError | Significado | Ejemplos |
| --- | --- | --- |
| 1 | Error genérico | JSON mal formado, falta un parámetro de consulta o una parte multipart obligatoria, token sin username, no se pudo guardar el archivo |
| 2 | La entidad ya existe | Nombre de asignatura repetido en su programa, código estudiantil, documento, correo o username repetidos contra la base |
| 3 | La entidad no existe | uuid inexistente, o una solicitud que el usuario no puede ver (responde igual que una inexistente) |
| 4 | Regla de negocio violada | Etapa que no admite la acción, anexo faltante, plazo vencido, solicitud en curso, asignatura de otro programa al matricular |
| 6 | Mal formato | Paginación inválida, fecha mal escrita, nota o faltas fuera de rango, archivo no permitido, errores de Excel |
| 7 | Sin información | Carga masiva sin filas |

Los códigos 5, 8 y 9 son de Julián (credenciales, acceso y token) y estos
endpoints no los usan.

Los mensajes terminan en `...`. En este documento, `<x>` marca la parte que
cambia en cada caso.

Una solicitud que existe pero que el usuario no puede ver responde con
`codigoError` 3 y el mismo mensaje que una inexistente:
`Solicitud académica con id <uuid> no fue encontrado en el sistema...`.

### 1.3 Formatos

- Rutas con uuid: el segmento debe tener 36 caracteres hexadecimales o
  guiones.
- Fechas que envía el cliente (solo Examen Supletorio): texto `AAAA-MM-DD`.
  Otro formato responde `codigoError` 6: `El valor <valor> de <dato> no es una
  fecha con el formato AAAA-MM-DD...`.
- Fechas de detalle del supletorio en la respuesta: texto `AAAA-MM-DD`.
- Fechas y horas que genera el servidor (`fechaCreacion`, `fechaSubida`,
  `fecha` del historial): `AAAA-MM-DDTHH:mm:ss`, sin zona, en hora de Colombia.
- En los mensajes de error del supletorio las fechas salen como `dd/MM/aaaa`.
- Hora del examen cruzado: `HH:mm` de 24 horas.
- `nota`: número de 0.0 a 5.0 con un decimal.
- Listados paginados: parámetros `pagina` (desde 0) y `tamanio` (desde 1);
  otro valor responde `codigoError` 6 `Error en la paginación o tamaño de la
  pagina...`. La respuesta es `{"content": [...], "totalElements": n}`. Los
  listados sin resultados devuelven `content` vacío.

### 1.4 Archivos

- Tamaño máximo por archivo: 5 MB (5242880 bytes). Más de eso responde
  `codigoError` 6: `El archivo pesa <n> bytes y el máximo permitido es 5242880
  bytes (5 MB)...`. Spring además corta cualquier archivo o petición de más de
  20 MB.
- El archivo no puede venir vacío (`El archivo adjunto está vacío...`, código
  6).
- La extensión debe estar en los formatos del tipo de anexo y el contenido
  debe corresponder: un PDF debe empezar con `%PDF-`, un PNG o un JPG con su
  firma. `jpg` y `jpeg` son el mismo formato. Mayúsculas y espacios en la
  extensión no importan.
  - Formato no permitido: `El formato <ext> no está permitido para <anexo>, se
    aceptan <formatos>...` (código 6).
  - Contenido que no coincide: `El contenido del archivo no corresponde a un
    archivo <ext>...` (código 6).
- Formatos por tipo de anexo (sembrados en `TIPO_ANEXO_ACADEMICO`):

| Proceso | Anexo | Formatos | Obligatorio |
| --- | --- | --- | --- |
| Cancelación de Matrícula | Paz y salvo - División de Bibliotecas | pdf | Sí |
| Cancelación de Matrícula | Paz y salvo - División de Deportes y Recreación | pdf | Sí |
| Cancelación de Matrícula | Paz y salvo - División de Salud Integral | pdf | Sí |
| Cancelación de Matrícula | Cupón de Confirmación de la Intervención Psicosocial - División de Salud Integral | pdf | Sí |
| Cancelación de Matrícula | Paz y salvo - División Financiera | pdf | Sí |
| Cancelación de Matrícula | Carné estudiantil o constancia de no trámite - DARCA | pdf, jpg, jpeg, png | Sí |
| Examen Supletorio | Formato PM-FO-4-FOR-23 firmado por el docente que orienta la asignatura | pdf, jpg, png | Sí |
| Examen Supletorio | Soporte de la justificación de la no presentación | pdf, jpg, png | Si la causa es `otra` |
| Examen Supletorio | Formato firmado por el docente de la asignatura con la que se cruza | pdf, jpg, png | Si la causa es `cruce` |
| Examen Supletorio | Recibo de pago | pdf | Lo sube el Funcionario para pasar a PENDIENTE_PAGO |
| Examen Supletorio | Comprobante de pago | pdf, jpg, png | Lo sube el Estudiante para pasar a EN_VERIFICACION_PAGO |
| Cancelaciones | Soporte libre (sin tipo) | pdf, jpg, jpeg, png | No |
| Cancelaciones | Escaneo de la Resolución | pdf | Para rechazar en el Funcionario y para enviar la respuesta |

Los uuid de los tipos de anexo los entregan los formularios de radicación y
`GET catalogos-academicos/tipos-solicitud/{uuidTipo}/tipos-anexo`.

- Las descargas responden el archivo binario con `Content-Type` del archivo
  y `Content-Disposition: attachment` con el nombre original.

### 1.5 Etapas

`etapaCodigo` es estable y sirve para decidir en el frontend; `etiqueta` es el
texto para mostrar al rol que consulta. Un rol no ve las solicitudes de una
etapa sin etiqueta para él.

Al Estudiante, en `APROBADA_POR_DECANO` y `RECHAZADA_POR_DECANO`, la bandeja,
el detalle común y el detalle de cada proceso le devuelven `etapaCodigo`
`EN_REVISION_DECANO`, con la etiqueta "En trámite": no se entera de la
decisión hasta la respuesta del Funcionario (o el recibo de pago en el
supletorio aprobado). Funcionario Académico y Decano reciben el código real.
`PENDIENTE_PAGO`, `EN_VERIFICACION_PAGO`, `APROBADA` y `RECHAZADA` no se
enmascaran.

| etapaCodigo | Procesos | Etiqueta Estudiante | Etiqueta Funcionario | Etiqueta Decano |
| --- | --- | --- | --- | --- |
| RADICADA | los tres | En trámite | Pendiente | (no la ve) |
| EN_REVISION_DECANO | los tres | En trámite | En Gestión | Pendiente |
| APROBADA_POR_DECANO | los tres | En trámite | Aprobada por el Decano | Respondida |
| RECHAZADA_POR_DECANO | los tres | En trámite | Rechazada por el Decano | Respondida |
| PENDIENTE_PAGO | supletorio | Pendiente de pago | En Gestión | Respondida |
| EN_VERIFICACION_PAGO | supletorio | En verificación de pago | Pendiente de Verificación | Respondida |
| APROBADA | los tres (final) | Aprobada | Respondida | Respondida |
| RECHAZADA | los tres (final) | Rechazada | Respondida | Respondida |

Las etiquetas salen de la tabla `ETAPA_ETIQUETA_ROL` y se pueden consultar con
`GET catalogos-academicos/etiquetas`.

### 1.6 Acciones

El detalle de cada solicitud trae `accionesDisponibles`: los códigos de las
acciones que el rol que consulta puede ejecutar en la etapa actual. Cada
código se ejecuta con este endpoint (`<proceso>` es `cancelaciones-matricula`,
`cancelaciones-asignatura` o `examenes-supletorios`):

| Acción | Endpoint | Rol | Exige |
| --- | --- | --- | --- |
| RECHAZAR_FUNCIONARIO | `POST <proceso>/{uuid}/funcionario/rechazar` | Funcionario Académico | Observación; en cancelaciones, escaneo de la Resolución subido antes |
| REMITIR_DECANO | `POST <proceso>/{uuid}/funcionario/remitir` | Funcionario Académico | Evaluaciones (cancelaciones) o `requisitosVerificados` (supletorio). Una sola `observacion`, opcional, para el Decano |
| APROBAR_DECANO | `POST <proceso>/{uuid}/decano/aprobar` | Decano | Situaciones al cancelar (matrícula) o `aceptarEvaluacion` o `decisiones` por asignatura (asignatura). `observacion` opcional |
| RECHAZAR_DECANO | `POST <proceso>/{uuid}/decano/rechazar` | Decano | Nada: `observacion` opcional |
| ENVIAR_RESPUESTA | `POST <proceso>/{uuid}/funcionario/responder` | Funcionario Académico | En cancelaciones, escaneo de la Resolución subido antes. `observacion`: mensaje para el Estudiante, opcional salvo en Cancelación de Asignatura con rechazos del Decano; en ese proceso, además, `ajustes` (sección 3.7) |
| ENVIAR_RECIBO | `POST examenes-supletorios/{uuid}/funcionario/recibo` | Funcionario Académico | Recibo de pago subido antes |
| SUBIR_COMPROBANTE | `POST examenes-supletorios/{uuid}/estudiante/comprobante` | Estudiante | Comprobante de pago subido antes |
| APROBAR_COMPROBANTE | `POST examenes-supletorios/{uuid}/funcionario/comprobante/aprobar` | Funcionario Académico | Fecha acordada opcional |
| RECHAZAR_COMPROBANTE | `POST examenes-supletorios/{uuid}/funcionario/comprobante/rechazar` | Funcionario Académico | Observación |

La observación de una acción admite hasta 500 caracteres. Las observaciones
de `REMITIR_DECANO`, `APROBAR_DECANO` y `RECHAZAR_DECANO` son opcionales y
solo las ven el Funcionario Académico y el Decano: al Estudiante el historial
le entrega la fila `REMITIR_DECANO` con `observaciones` nulo y no le entrega
las filas `APROBAR_DECANO` ni `RECHAZAR_DECANO`, en ninguno de los tres
procesos. La de `ENVIAR_RESPUESTA` es el mensaje del Funcionario al
Estudiante y él sí la ve. La de `RECHAZAR_FUNCIONARIO` es obligatoria, es
para el Estudiante y también la ve en su historial.

Los endpoints cuyo cuerpo es opcional (`funcionario/responder`,
`decano/rechazar` y `decano/aprobar` del supletorio, `comprobante/aprobar`)
aceptan JSON, `{}` o ningún cuerpo sin `Content-Type`. Un POST sin cuerpo pero
con otro `Content-Type` (por ejemplo `application/x-www-form-urlencoded`, que
Windows PowerShell 5.1 manda por defecto) responde código 1 `Content-Type
'...' is not supported`.

Las acciones responden el detalle del proceso ya actualizado. Errores comunes
a todas:

| codigoError | Mensaje |
| --- | --- |
| 3 | `Solicitud de cancelación de matrícula con id <uuid> no fue encontrado en el sistema...` (o `Solicitud de cancelación de asignatura` o `Solicitud de examen supletorio`), también si el uuid es de otro proceso |
| 4 | `La acción <ACCION> no está permitida desde la etapa <ETAPA> en <proceso>...` |
| 4 | `La solicitud está en la etapa final <ETAPA> y no admite más acciones...` |
| 4 | `La acción <ACCION> exige <lo que falta>...`, donde lo que falta es `una observación`, `el escaneo de la Resolución`, `el recibo de pago` o `el comprobante de pago`, unidos con " y " |
| 4 | `El tipo de solicitud <nombre> no está asignado al funcionario académico <uuid>...` |
| 4 | `La solicitud <radicado> no pertenece al estudiante <uuid>...` |
| 6 | `La observación supera los 500 caracteres permitidos...` |

## 2. Objetos que se repiten

**AsignaturaDTORespuesta**: `uuidAsignatura`, `codigoAsignatura` (generado,
por ejemplo `ISI-001`), `nombreAsignatura`, `uuidProgramaAcademico` y
`programaAcademico` (nombre del programa) (texto).

**ProgramaAcademicoDTORespuesta**: `uuidProgramaAcademico`, `nombre`, `sigla`
(texto).

**AsignaturaMatriculadaDTORespuesta**: `uuidAsignaturaMatriculada`,
`uuidAsignatura`, `codigoAsignatura`, `nombreAsignatura`, `grupo`, `estado`
(texto; `activa`, `cancelada`, `aprobada` o `perdida`).

**EstudianteDTORespuesta**: `uuidUsuario`, `nombres`, `apellidos`,
`tipoDocumento`, `numeroDocumento`, `telefono`, `correoElectronico`,
`username`, `codigoEstudiantil`, `uuidProgramaAcademico`,
`programaAcademico` (nombre del programa), `semestre`, `facultad` (texto, siempre `FIET`), `estado` (booleano, usuario activo) y `asignaturasMatriculadas`
(lista de AsignaturaMatriculadaDTORespuesta). Nunca trae la contraseña.

**FuncionarioAcademicoDTORespuesta**: `uuidUsuario`, `nombres`, `apellidos`,
`tipoDocumento`, `numeroDocumento`, `telefono`, `correoElectronico`,
`username`, `cargo` (texto), `estado` (booleano) y `tiposSolicitud`
(lista de `{uuidTipoSolicitudAcademica, nombre}` que atiende).

**TipoSolicitudAcademicaDTORespuesta**: `uuidTipoSolicitudAcademica`,
`nombre`, `descripcion`, `uuidFuncionarioAcademico`,
`nombreFuncionarioAcademico` (nombres y apellidos) (texto).

**SolicitudAcademicaResumenDTORespuesta** (bandejas):
`uuidSolicitudAcademica`, `radicado`, `uuidTipoSolicitudAcademica`,
`tipoSolicitud` (nombre del tipo), `fechaCreacion` (fecha y hora),
`etiqueta`, `etapaCodigo`, `nombreEstudiante` (nombres y apellidos),
`codigoEstudiantil`, `numeroDocumento` (número de documento del estudiante;
llega en las bandejas del Funcionario Académico y del Decano y sale `null` en
la del Estudiante, P42). Las bandejas no tienen parámetros de búsqueda: el
filtro se hace en el cliente.

**SolicitudAcademicaDetalleDTORespuesta** (detalle común):

| Campo | Tipo | Descripción |
| --- | --- | --- |
| `uuidSolicitudAcademica`, `radicado`, `uuidTipoSolicitudAcademica`, `tipoSolicitud` | texto | |
| `fechaCreacion` | fecha y hora | |
| `etiqueta` | texto | Etiqueta de la etapa para el rol que consulta |
| `etapaCodigo` | texto | Código de la etapa (sección 1.5); al Estudiante, `EN_REVISION_DECANO` en lugar de la decisión del Decano |
| `numeroDocumento` | texto | Número de documento del estudiante; llega al Funcionario Académico y al Decano y sale `null` al Estudiante (P43). Viene también en el `solicitud` de los detalles de cada proceso |
| `codigoEstudiantil` | texto | Código estudiantil del estudiante dueño de la solicitud, para los tres roles. Viene también en el `solicitud` de los detalles de cada proceso |
| `estudiante` | objeto | `uuidUsuario`, `nombres`, `apellidos`, `codigoEstudiantil`, `programaAcademico`, `semestre`, `correoElectronico` |
| `anexos` | lista | AnexoAcademicoDTORespuesta, de la subida más antigua a la más reciente |
| `tieneResolucion` | booleano | Hay escaneo de la Resolución |
| `puedeDescargarResolucion` | booleano | Hay escaneo y el rol puede bajarlo (el Estudiante solo en etapa final) |
| `accionesDisponibles` | lista de texto | Códigos de la sección 1.6 que el rol puede ejecutar ahora |

**AnexoAcademicoDTORespuesta**: `uuidAnexoAcademico`, `nombreArchivo`
(nombre original), `uuidTipoAnexoAcademico` y `tipoAnexo` (nulos en un
soporte libre), `tipoArchivo` (`application/pdf`, `image/jpeg` o
`image/png`), `tamanioBytes` (número), `fechaSubida` (fecha y hora). Nunca
trae la ruta en disco.

**HistorialSolicitudAcademicaDTORespuesta**: `accion` (código de la sección
1.6 o `RADICAR`), `etapaCodigo` (etapa a la que llevó esa acción; nulo si no
encaja en el recorrido), `observaciones`, `fecha` (fecha y hora),
`nombresUsuario`, `apellidosUsuario`. Para el Estudiante, `observaciones`
llega nulo en la fila `REMITIR_DECANO` y las filas `APROBAR_DECANO` y
`RECHAZAR_DECANO` no vienen en la lista; las demás filas conservan su
`etapaCodigo` y ninguna trae `APROBADA_POR_DECANO` ni `RECHAZADA_POR_DECANO`,
así que la última fila coincide con el `etapaCodigo` del detalle. El Estudiante se entera de la decisión del Decano por la fila
`ENVIAR_RESPUESTA` (en el supletorio aprobado, por `ENVIAR_RECIBO`). El
Funcionario Académico y el Decano reciben todas las filas.

**SituacionAsignaturaDTORespuesta**: `uuidSituacionAcademica`, `codigo`
(R0 a R3), `nombre`: R0 No cursada, R1 Cursada por primera vez, R2 Cursada
por segunda vez, R3 Cursada por tercera vez.

## 3. Endpoints

### 3.1 Asignaturas (7)

| Método y ruta | Roles | Parámetros | Cuerpo | Respuesta |
| --- | --- | --- | --- | --- |
| `POST asignaturas` | Secretario General, Decano | | `{nombreAsignatura, uuidProgramaAcademico}` | AsignaturaDTORespuesta con el código generado |
| `POST asignaturas/cargar/archivo` | Secretario General, Decano | | Multipart, parte `file` (.xlsx) | Lista de AsignaturaDTORespuesta creadas, en el orden del archivo |
| `GET asignaturas/paginado` | Secretario General, Decano, Funcionario Académico | `pagina`, `tamanio` | | Página de AsignaturaDTORespuesta, por sigla del programa y consecutivo |
| `GET asignaturas/filtro` | Secretario General, Decano, Funcionario Académico | `texto` (opcional; busca en código y nombre, sin distinguir mayúsculas), `uuidProgramaAcademico` (opcional), `pagina`, `tamanio` | | Página de AsignaturaDTORespuesta, en el mismo orden |
| `GET asignaturas/programa/{uuidPrograma}` | Secretario General, Decano, Funcionario Académico | | | Lista sin paginar de AsignaturaDTORespuesta del programa, por consecutivo (`ISI-999` antes de `ISI-1000`) |
| `GET asignaturas/{uuidAsignatura}` | Secretario General, Decano, Funcionario Académico | | | AsignaturaDTORespuesta |
| `PUT asignaturas/{uuidAsignatura}` | Secretario General, Decano | | `{nombreAsignatura}` | AsignaturaDTORespuesta |

Cuerpo de `POST asignaturas`: `nombreAsignatura` obligatorio, máximo 150;
`uuidProgramaAcademico` obligatorio, máximo 100 (400 si no se cumple). El
cuerpo no lleva código: el backend lo genera con la sigla del programa, un
guion y el siguiente consecutivo del programa con al menos tres dígitos
(`ISI-001`, ..., `ISI-999`, `ISI-1000`). El código nunca se edita ni se
reutiliza. El nombre se guarda sin espacios sobrantes y no se repite dentro
del mismo programa sin distinguir mayúsculas ni espacios.

`PUT asignaturas/{uuid}`: `nombreAsignatura` obligatorio, máximo 150. Solo
cambia el nombre; si el cuerpo trae `codigoAsignatura` o
`uuidProgramaAcademico`, se ignoran.

Excel de asignaturas: hoja 1, fila 1 con los encabezados `nombreAsignatura,
programaAcademico`, en ese orden. `programaAcademico` es el nombre exacto de
uno de los cinco programas (sección 3.4), sin distinguir mayúsculas ni
espacios al inicio o al final. Termina en la primera fila vacía. Es todo o
nada: si una fila falla no se crea ninguna. Hay una plantilla en
`backend/pruebas/plantilla-asignaturas.xlsx`.

Errores:

- 400: mapa `{"campo": "mensaje"}` por las anotaciones del cuerpo; en la
  carga, `{"campo": "Fila <n>, columna <letra> (<campo>): <mensaje>"}` con
  `El nombre de la asignatura no puede estar vacío`, `El nombre de la
  asignatura debe tener máximo 150 caracteres` o `El programa académico no
  puede estar vacío`.
- 2: `La asignatura <nombre> ya existe en el programa <programa> con el
  código <codigo>...`; en la carga, `Fila <n>, columna A (nombreAsignatura):
  la asignatura <nombre> ya existe en el programa <programa> con el código
  <codigo>...`.
- 3: `Asignatura con id <uuid> no fue encontrado en el sistema...`;
  `Programa académico con id <uuid> no fue encontrado en el sistema...`
  (al crear, en `filtro` y en `programa/{uuidPrograma}`).
- 6: `El nombre de la asignatura no puede estar vacío...`; `El nombre de la
  asignatura supera los 150 caracteres permitidos...`; `Debe indicar el
  programa académico...`; `La petición trae la asignatura <nombre> más de
  una vez...`; paginación inválida.
- 6 en la carga: `El archivo debe ser un Excel .xlsx valido con una
  hoja...`; `Fila 1, columna <letra>: se esperaba el encabezado <campo> y se
  encontró '<valor>'...`; `El archivo no tiene filas de datos despues del
  encabezado...`; `Fila <n>, columna B (programaAcademico): el programa
  '<valor>' no existe, los programas permitidos son <lista>...`; `Fila <n>,
  columna A (nombreAsignatura): la asignatura <nombre> ya aparece en la fila
  <m> para el programa <programa>...`.

### 3.2 Estudiantes (9)

| Método y ruta | Roles | Parámetros | Cuerpo | Respuesta |
| --- | --- | --- | --- | --- |
| `POST estudiantes` | Secretario General, Decano | | EstudianteDTOPeticion | EstudianteDTORespuesta |
| `POST estudiantes/cargar/archivo` | Secretario General, Decano | | Multipart, parte `file` (.xlsx) | Lista de EstudianteDTORespuesta |
| `GET estudiantes/paginado` | Secretario General, Decano, Funcionario Académico | `pagina`, `tamanio` | | Página de EstudianteDTORespuesta, por código estudiantil |
| `GET estudiantes/filtro` | Secretario General, Decano, Funcionario Académico | `nombre`, `apellido`, `codigo` (opcionales; contienen, sin distinguir mayúsculas), `pagina`, `tamanio` | | Página de EstudianteDTORespuesta |
| `GET estudiantes/mis-asignaturas` | Estudiante | | | Lista de AsignaturaMatriculadaDTORespuesta activas del estudiante autenticado, por código |
| `GET estudiantes/{uuidEstudiante}` | Secretario General, Decano, Funcionario Académico | | | EstudianteDTORespuesta |
| `PUT estudiantes/{uuidEstudiante}` | Secretario General, Decano | | `{nombres, apellidos, tipoDocumento, numeroDocumento, telefono, correoElectronico, codigoEstudiantil, semestre}`, todos opcionales; los vacíos no cambian | EstudianteDTORespuesta |
| `POST estudiantes/{uuidEstudiante}/asignaturas` | Secretario General, Decano | | `{codigoAsignatura, grupo}` | AsignaturaMatriculadaDTORespuesta, en estado `activa` |
| `PATCH estudiantes/{uuidEstudiante}/asignaturas/{uuidMatricula}/estado` | Secretario General, Decano | | `{estado}` | AsignaturaMatriculadaDTORespuesta |

EstudianteDTOPeticion: `nombres`, `apellidos` (obligatorios, máximo 1000),
`tipoDocumento` (`Cédula de ciudadanía`, `Tarjeta de identidad` o `Cédula de
extranjería`, sin distinguir mayúsculas), `numeroDocumento` (5 a 255),
`telefono` (5 a 1000), `correoElectronico` (correo válido), `username` (5 a
255), `password` (5 a 255), `codigoEstudiantil` (máximo 45),
`uuidProgramaAcademico` (obligatorio, máximo 100), `semestre` (máximo 10),
`asignaturas` (al menos una: `{codigoAsignatura, grupo}`; código máximo 45 y
grupo máximo 20). El rol, el tipo de usuario y la facultad (`FIET`) los
asigna el backend; si llega `facultad`, se ignora.

Edición (`PUT estudiantes/{uuid}`): cada campo es opcional y uno vacío o
ausente no cambia. Mismos límites que al crear (`tipoDocumento` y `telefono`
de 5 a 1000, `numeroDocumento` de 5 a 255, correo válido). `username`,
programa y facultad son de solo lectura: si llegan, se ignoran. Documento,
correo y código estudiantil no pueden ser de otro usuario o estudiante
(código 2); el propio registro no cuenta como repetido. Nombres, apellidos y
datos de contacto se guardan en `usuarios` y `usuariosLivianos`.

El programa se elige al crear el estudiante y no se cambia después: `PUT`
no lo recibe (si llega, se ignora). Cada `codigoAsignatura` se busca en el
catálogo sin distinguir mayúsculas ni espacios y debe ser una asignatura del
programa del estudiante; ningún endpoint de estudiantes crea asignaturas.
La misma regla aplica a `POST estudiantes/{uuid}/asignaturas`.

Excel de estudiantes: hoja 1, fila 1 con estos encabezados en este orden:
`nombres, apellidos, tipoDocumento, numeroDocumento, telefono,
correoElectronico, username, password, codigoEstudiantil, programaAcademico,
semestre, codigoAsignatura, grupo` (13 columnas, sin `facultad`). `programaAcademico` es el
nombre exacto de uno de los cinco programas, sin distinguir mayúsculas. Una
fila por asignatura matriculada; las filas del mismo `numeroDocumento` se
juntan en un estudiante y deben repetir los mismos datos, programa incluido.
Termina en la primera fila vacía. Hay una plantilla en
`backend/pruebas/plantilla-estudiantes.xlsx`.

Errores principales:

- 400 en la carga: mapa `{"campo": "Fila <n>, columna <letra> (<campo>): <mensaje>"}`;
  un programa vacío sale en la columna J (`programaAcademico`).
- 2: `Estudiante con codigo estudiantil: <x> existe en el sistema...`, `Usuario
  con numero de documento: <x> existe...`, `... correo electronico ...`,
  `... username ...`.
- 3: `Estudiante con id <uuid> no fue encontrado en el sistema...`; en
  `mis-asignaturas`, `Estudiante con username: <x> no fue encontrado en el
  sistema...`; `Asignatura matriculada con id <uuid> no fue encontrado...`;
  `Programa académico con id <uuid> no fue encontrado en el sistema...`;
  `Asignatura con codigo: <codigo> no fue encontrado en el sistema...`.
- 4: `La asignatura <codigo> (<nombre>) es del programa <programa de la
  asignatura> y el estudiante es del programa <programa del estudiante>...`.
- 6: `La petición trae <dato> <valor> más de una vez...`; `El estudiante ya
  tiene activa la asignatura <codigo> en el grupo <grupo>...`; `El estado <x>
  no es valido, los estados permitidos son activa, cancelada, aprobada,
  perdida...`; `No se puede cambiar la asignatura matriculada de <actual> a
  <nuevo>...` (solo se cambia desde `activa`); `Tipo de documento invalido...`;
  `Debe indicar el programa académico...`.
- 6 en la carga: `El archivo debe ser un Excel .xlsx valido con una hoja...`;
  `Fila 1, columna <letra>: se esperaba el encabezado <campo> y se encontró
  '<valor>'...`; `El archivo no tiene filas de estudiantes despues del
  encabezado...`; `Fila <n>, columna J (programaAcademico): el programa
  '<valor>' no existe, los programas permitidos son <lista>...`; `Fila <n>,
  columna L (codigoAsignatura): la asignatura con código <codigo> no existe
  en el catálogo...`; `Fila <n>, columna L (codigoAsignatura): la asignatura
  <codigo> es del programa <programa> y el estudiante es del programa
  <programa>...`; `Fila <n>, columna <letra> (<campo>): el estudiante con
  documento <doc> tiene un valor distinto al de la fila <m>...` (también si
  cambia el programa); `... la asignatura <codigo> en el grupo <grupo> ya
  aparece en la fila <m> para el mismo estudiante...`; `... el valor <x> ya
  lo usa otro estudiante en la fila <m>...`.

### 3.3 Funcionarios académicos (6)

| Método y ruta | Roles | Parámetros | Cuerpo | Respuesta |
| --- | --- | --- | --- | --- |
| `POST funcionarios-academicos` | Secretario General, Decano | | FuncionarioAcademicoDTOPeticion | FuncionarioAcademicoDTORespuesta |
| `POST funcionarios-academicos/cargar/archivo` | Secretario General, Decano | | Multipart, parte `file` (.xlsx) | Lista de FuncionarioAcademicoDTORespuesta |
| `GET funcionarios-academicos/paginado` | Secretario General, Decano, Funcionario Académico | `pagina`, `tamanio` | | Página, por apellidos y nombres |
| `GET funcionarios-academicos/filtro` | Secretario General, Decano, Funcionario Académico | `nombre`, `apellido`, `cargo` (opcionales; contienen, sin distinguir mayúsculas), `pagina`, `tamanio` | | Página |
| `GET funcionarios-academicos/{uuidFuncionario}` | Secretario General, Decano, Funcionario Académico | | | FuncionarioAcademicoDTORespuesta |
| `PUT funcionarios-academicos/{uuidFuncionario}` | Secretario General, Decano | | `{nombres, apellidos, tipoDocumento, numeroDocumento, telefono, correoElectronico, cargo}`, todos opcionales; los vacíos no cambian | FuncionarioAcademicoDTORespuesta |

FuncionarioAcademicoDTOPeticion: los mismos datos personales y de acceso que
el estudiante (`nombres`, `apellidos`, `tipoDocumento`, `numeroDocumento`,
`telefono`, `correoElectronico`, `username`, `password`) más `cargo`
(obligatorio al crear, máximo 100; antes se llamaba `dependencia`). El
backend le asigna el rol `Funcionario Académico` y el tipo `Empleado FIET -
Funcionario`. La edición sigue las mismas reglas que la del estudiante:
campos opcionales, `username` de solo lectura y documento y correo únicos sin
contar al propio registro (código 2).

Excel: encabezados `nombres, apellidos, tipoDocumento, numeroDocumento,
telefono, correoElectronico, username, password, cargo`; una fila por
funcionario. Plantilla en
`backend/pruebas/plantilla-funcionarios-academicos.xlsx`.

Errores: 400 como en estudiantes; 2 por documento, correo o username en la
base; 3 `Funcionario Académico con id <uuid> no fue encontrado en el
sistema...`; 6 por repetidos dentro de la petición, archivo no válido,
encabezado distinto, `El archivo no tiene filas de datos despues del
encabezado...` y `Fila <n>, columna <letra> (<campo>): el valor <x> ya
aparece en la fila <m> del archivo...`.

### 3.4 Catálogos académicos (7)

| Método y ruta | Roles | Parámetros | Cuerpo | Respuesta |
| --- | --- | --- | --- | --- |
| `GET catalogos-academicos/tipos-solicitud` | Cualquier usuario autenticado | | | Lista de TipoSolicitudAcademicaDTORespuesta, por nombre |
| `GET catalogos-academicos/tipos-solicitud/{uuidTipo}/etapas` | Cualquier usuario autenticado | | | Lista de `{uuidEtapa, codigo, uuidTipoSolicitudAcademica}` (universales y propias del tipo), por código |
| `GET catalogos-academicos/tipos-solicitud/{uuidTipo}/tipos-anexo` | Cualquier usuario autenticado | | | Lista de `{uuidTipoAnexoAcademico, uuidTipoSolicitudAcademica, nombre, formatosPermitidos, obligatorio}`, por nombre |
| `GET catalogos-academicos/etiquetas` | Cualquier usuario autenticado | `rol`: `ESTUDIANTE`, `FUNCIONARIO` o `DECANO` (sin distinguir mayúsculas) | | Lista de `{uuidEtapa, codigoEtapa, rol, etiqueta}`, por código de etapa |
| `GET catalogos-academicos/situaciones` | Cualquier usuario autenticado | | | Lista de SituacionAsignaturaDTORespuesta, por código |
| `GET catalogos-academicos/programas` | Cualquier usuario autenticado | | | Lista de ProgramaAcademicoDTORespuesta de los cinco programas, por nombre |
| `PUT catalogos-academicos/tipos-solicitud/{uuidTipo}/funcionario` | Secretario General, Decano | | `{funcionarioUuid}` (obligatorio, máximo 100) | TipoSolicitudAcademicaDTORespuesta con el nuevo responsable |

Errores: 3 `Tipo de solicitud académica con id <uuid> no fue encontrado en el
sistema...`; 3 `Funcionario Académico con id <uuid> no fue encontrado...` (el
uuid no tiene fila de funcionario académico); 6 `El rol <x> no es valido, los
roles permitidos son ESTUDIANTE, FUNCIONARIO, DECANO...`; 400 si
`funcionarioUuid` llega vacío.

### 3.5 Solicitudes académicas (9)

Endpoints comunes a los tres procesos.

| Método y ruta | Roles | Parámetros | Cuerpo | Respuesta |
| --- | --- | --- | --- | --- |
| `GET solicitudes-academicas/estudiante` | Estudiante | | | Lista de SolicitudAcademicaResumenDTORespuesta propias |
| `GET solicitudes-academicas/funcionario` | Funcionario Académico | | | Lista de las solicitudes de los tipos asignados a él |
| `GET solicitudes-academicas/decano` | Decano | | | Lista de todas las solicitudes con etiqueta para el Decano (no trae RADICADA) |
| `GET solicitudes-academicas/{uuidSolicitud}` | Estudiante, Funcionario Académico, Decano | | | SolicitudAcademicaDetalleDTORespuesta |
| `GET solicitudes-academicas/{uuidSolicitud}/historial` | Estudiante, Funcionario Académico, Decano | | | Lista de HistorialSolicitudAcademicaDTORespuesta, de la más antigua a la más reciente |
| `POST solicitudes-academicas/{uuidSolicitud}/anexos` | Estudiante, Funcionario Académico | | Multipart: `archivo` (obligatorio) y `tipoAnexo` (uuid del tipo de anexo; vacío o ausente = soporte libre) | AnexoAcademicoDTORespuesta |
| `GET solicitudes-academicas/{uuidSolicitud}/anexos/{uuidAnexo}` | Estudiante, Funcionario Académico, Decano | | | Archivo |
| `POST solicitudes-academicas/{uuidSolicitud}/resolucion` | Funcionario Académico | | Multipart: `archivo` (PDF) | `{uuidSolicitudAcademica, nombreArchivo, fechaSubida}` |
| `GET solicitudes-academicas/{uuidSolicitud}/resolucion` | Estudiante, Funcionario Académico, Decano | | | Archivo PDF |

Las bandejas no se paginan y van de la más reciente a la más antigua.

Rol con que actúa el usuario en una solicitud: Estudiante si es el dueño, si
no Funcionario Académico si el tipo está asignado a él, si no Decano si
tiene ese rol. Con ese rol se filtra lo que ve; si no le corresponde ninguno o
la etapa no tiene etiqueta para su rol, responde código 3 como inexistente.

Subida de anexos (código 4 salvo que se indique):

- Quién y cuándo: los anexos del Estudiante y los soportes libres, solo el
  Estudiante dueño en RADICADA; el recibo de pago, solo el Funcionario
  asignado en APROBADA_POR_DECANO; el comprobante de pago, solo el Estudiante
  dueño en PENDIENTE_PAGO.
  - `El anexo <nombre> solo lo puede subir el rol <ESTUDIANTE|FUNCIONARIO>...`
  - `El anexo <nombre> solo se puede subir en la etapa <ETAPA> y la solicitud
    está en <ETAPA>...`
- `El tipo de solicitud Examen Supletorio no admite soportes libres...`
- `El tipo de anexo <nombre> no pertenece al tipo de solicitud <tipo>...`
- Código 3 `Tipo de anexo académico con id <uuid> no fue encontrado...`.
- Errores de archivo de la sección 1.4.
- Código 1 `No se pudo guardar el archivo del anexo...`.

Escaneo de la Resolución (código 4 salvo que se indique):

- Solo en las cancelaciones: `El tipo de solicitud Examen Supletorio no
  produce Resolución, solo la producen las cancelaciones de matrícula y de
  asignatura...`
- Lo sube el Funcionario asignado en RADICADA, APROBADA_POR_DECANO o
  RECHAZADA_POR_DECANO. Subirlo otra vez reemplaza el anterior.
  - `El escaneo de la Resolución solo se sube en las etapas RADICADA,
    APROBADA_POR_DECANO, RECHAZADA_POR_DECANO y la solicitud está en
    <ETAPA>...`
  - `La solicitud <radicado> está en la etapa final <ETAPA> y su Resolución ya
    no se puede subir ni reemplazar...`
- Remitir al Decano retira el escaneo que hubiera.
- Descarga del Estudiante antes de APROBADA o RECHAZADA: `La Resolución de la
  solicitud <radicado> estará disponible cuando la solicitud termine...`
- Código 3 `Resolución académica con id <uuid> no fue encontrado...` si no
  hay escaneo.

Descarga de anexos: código 3 `Anexo académico con id <uuid> no fue encontrado
en el sistema...` si el anexo no es de esa solicitud.

### 3.6 Cancelaciones de matrícula (8)

| Método y ruta | Roles | Cuerpo | Respuesta |
| --- | --- | --- | --- |
| `GET cancelaciones-matricula/formulario` | Estudiante | | `{anexosRequeridos: [{uuidTipoAnexoAcademico, nombre, formatosPermitidos, obligatorio}], asignaturas: [{codigoAsignatura, nombreAsignatura}]}` |
| `POST cancelaciones-matricula` | Estudiante | Multipart (ver abajo) | `{uuidSolicitudAcademica, radicado}` |
| `GET cancelaciones-matricula/{uuidSolicitud}` | Estudiante, Funcionario Académico, Decano | | CancelacionMatriculaDetalle |
| `POST cancelaciones-matricula/{uuidSolicitud}/funcionario/rechazar` | Funcionario Académico | `{observacion}` | CancelacionMatriculaDetalle |
| `POST cancelaciones-matricula/{uuidSolicitud}/funcionario/remitir` | Funcionario Académico | `{observacion, evaluaciones}` | CancelacionMatriculaDetalle |
| `POST cancelaciones-matricula/{uuidSolicitud}/funcionario/responder` | Funcionario Académico | `{observacion}` opcional (mensaje al Estudiante, máximo 500), `{}` o sin cuerpo | CancelacionMatriculaDetalle |
| `POST cancelaciones-matricula/{uuidSolicitud}/decano/aprobar` | Decano | `{situaciones, observacion}`; `observacion` opcional | CancelacionMatriculaDetalle |
| `POST cancelaciones-matricula/{uuidSolicitud}/decano/rechazar` | Decano | `{observacion}` opcional, `{}` o sin cuerpo | CancelacionMatriculaDetalle |

El formulario lista los seis anexos obligatorios y las asignaturas activas
del estudiante; la solicitud incluye todas esas asignaturas.

Radicación (`multipart/form-data`):

- `motivo`: texto obligatorio, máximo 255.
- Una parte de archivo por anexo, cuyo nombre es el `uuidTipoAnexoAcademico`
  del formulario. Los seis son obligatorios.
- Partes `soporte` para soportes libres opcionales (pdf, jpg, jpeg, png).

Errores de la radicación, en el orden en que se revisan:

| codigoError | Mensaje |
| --- | --- |
| 4 | `El usuario <uuid> no tiene el rol Estudiante...` (también si no tiene fila de estudiante) |
| 4 | `El estudiante <uuid> no tiene asignaturas activas para cancelar...` |
| 4 | `El motivo de la cancelación es obligatorio...` |
| 6 | `El motivo de la cancelación supera los 255 caracteres permitidos...` |
| 4 | `El tipo de anexo <nombre> no pertenece al tipo de solicitud Cancelación de Matrícula...` |
| 4 | `Falta el anexo obligatorio: <nombres separados por "; ">...` |
| 6 | Errores de archivo de la sección 1.4 |
| 4 | `Ya tienes una solicitud de Cancelación de Matrícula en curso (<radicado>), debe terminar antes de radicar otra...` |
| 4 | `No se pudo generar el radicado, intente de nuevo...` |

**CancelacionMatriculaDetalle**: `solicitud`
(SolicitudAcademicaDetalleDTORespuesta), `motivoCancelacion` y `asignaturas`,
una por cada fila de la solicitud: `uuidAsignaturaSolicitud`,
`codigoAsignatura`, `nombreAsignatura`, `numeroFaltas` (texto o nulo),
`nota` (texto o nulo), `situacionMatricula` y `situacionCancelar`
(SituacionAsignaturaDTORespuesta o nulo).

Cuerpos de las acciones:

- `remitir`: `observacion` opcional y `evaluaciones`, una por asignatura de la
  solicitud: `{asignaturaSolicitudUuid, numeroFaltas, nota,
  situacionMatriculaUuid, situacionCancelarUuid}`. `numeroFaltas` y `nota` se envían como texto o
  como número y se guardan como texto de hasta 20 caracteres: si son
  numéricos, `numeroFaltas` debe ser entero mayor o igual a 0 y `nota` de 0.0
  a 5.0 con un decimal, con punto o coma (se guarda con punto, `3,5` queda
  `3.5`); si no son numéricos (por ejemplo `Sin registro`) se aceptan tal
  cual. `situacionMatriculaUuid` sale de `GET catalogos-academicos/situaciones`.
  `situacionCancelarUuid` es opcional (P44): si llega se valida contra el
  mismo catálogo y se guarda como situación al cancelar; el Decano la
  confirma o la cambia al aprobar.
  La `observacion` de la remisión es la única observación general del cuerpo,
  va para el Decano y el Estudiante no la ve.
- `aprobar`: `situaciones`, una por asignatura:
  `{asignaturaSolicitudUuid, situacionCancelarUuid}`, y `observacion`
  opcional.
- `rechazar` del Funcionario: `observacion` obligatoria y escaneo de la
  Resolución, subido antes con `POST solicitudes-academicas/{uuid}/resolucion`.
  `rechazar` del Decano: `observacion` opcional.
- `responder`: `observacion` opcional (máximo 500), mensaje para el Estudiante
  que queda en el historial; exige el escaneo de la Resolución. Desde
  APROBADA_POR_DECANO lleva a APROBADA y las asignaturas de la solicitud que
  sigan activas pasan a `cancelada`; desde RECHAZADA_POR_DECANO lleva a
  RECHAZADA.

Errores de las evaluaciones y situaciones (además de los de la sección 1.6):

| codigoError | Mensaje |
| --- | --- |
| 4 | `Faltan datos de las asignaturas de la solicitud: <nombres separados por "; ">...` |
| 4 | `La asignatura <nombre> viene más de una vez...` |
| 4 | `La asignatura <uuid> no pertenece a la solicitud <radicado>...` |
| 4 | `Falta <el número de faltas, la nota, la situación en la matrícula o la situación al cancelar> de la asignatura <nombre>...` |
| 6 | `El número de faltas de la asignatura <nombre> debe ser un entero mayor o igual a 0...` |
| 6 | `La nota de la asignatura <nombre> debe estar entre 0.0 y 5.0 con un decimal...` |
| 3 | `Situación académica de asignatura con id <uuid> no fue encontrado en el sistema...` |

### 3.7 Cancelaciones de asignatura (8)

| Método y ruta | Roles | Cuerpo | Respuesta |
| --- | --- | --- | --- |
| `GET cancelaciones-asignatura/formulario` | Estudiante | | `{asignaturas: [{uuidAsignaturaMatriculada, codigoAsignatura, nombreAsignatura, grupo}], soportes: [{uuidTipoAnexoAcademico: null, nombre: "Soporte libre", formatosPermitidos: "pdf,jpg,jpeg,png", tamanioMaximoBytes: 5242880, obligatorio: false}]}` |
| `POST cancelaciones-asignatura` | Estudiante | Multipart (ver abajo) | `{uuidSolicitudAcademica, radicado}` |
| `GET cancelaciones-asignatura/{uuidSolicitud}` | Estudiante, Funcionario Académico, Decano | | CancelacionAsignaturaDetalle |
| `POST cancelaciones-asignatura/{uuidSolicitud}/funcionario/rechazar` | Funcionario Académico | `{observacion}` | CancelacionAsignaturaDetalle |
| `POST cancelaciones-asignatura/{uuidSolicitud}/funcionario/remitir` | Funcionario Académico | `{observacion, evaluaciones}` | CancelacionAsignaturaDetalle |
| `POST cancelaciones-asignatura/{uuidSolicitud}/funcionario/responder` | Funcionario Académico | `{observacion, ajustes}` (ver abajo), `{}` o sin cuerpo | CancelacionAsignaturaDetalle |
| `POST cancelaciones-asignatura/{uuidSolicitud}/decano/aprobar` | Decano | `{aceptarEvaluacion}` o `{decisiones}`, con `observacion` opcional | CancelacionAsignaturaDetalle |
| `POST cancelaciones-asignatura/{uuidSolicitud}/decano/rechazar` | Decano | `{observacion}` opcional, `{}` o sin cuerpo | CancelacionAsignaturaDetalle |

Radicación (`multipart/form-data`):

- `motivo`: texto obligatorio, máximo 255.
- `asignaturas`: un campo de texto por cada `uuidAsignaturaMatriculada`
  elegido del formulario; al menos uno.
- Partes `soporte` para soportes libres opcionales. Una parte de archivo con
  cualquier otro nombre se toma como tipo de anexo y se rechaza.

Errores de la radicación, en orden:

| codigoError | Mensaje |
| --- | --- |
| 4 | `El usuario <uuid> no tiene el rol Estudiante...` |
| 4 | `El motivo de la cancelación es obligatorio...` |
| 6 | `El motivo de la cancelación supera los 255 caracteres permitidos...` |
| 4 | `Debe elegir al menos una asignatura para cancelar...` |
| 4 | `La asignatura <uuid> no es una asignatura matriculada del estudiante <uuid>...` (también para una asignatura de otro estudiante) |
| 4 | `La asignatura <codigo - nombre> viene más de una vez...` |
| 4 | `La asignatura <codigo - nombre> está <estado> y solo se pueden cancelar asignaturas activas...` |
| 4 | `La cancelación de asignatura solo admite soportes libres, sin tipo de anexo, y llegó uno de tipo <nombre de la parte>...` |
| 6 | Errores de archivo de la sección 1.4 |
| 4 | `Ya tienes una solicitud de Cancelación de Asignatura en curso (<radicado>), debe terminar antes de radicar otra...` |

**CancelacionAsignaturaDetalle**: `solicitud`, `motivoCancelacion` y
`asignaturas` con `uuidAsignaturaSolicitud`, `codigoAsignatura`,
`nombreAsignatura`, `numeroFaltas`, `nota`, `situacionMatricula`,
`cumpleCondiciones` (booleano), `observacionEvaluacion`, `aprobadaPorDecano`
(booleano), `observacionDecision`, `decisionCambiadaPorDecano` (booleano) y
`situacionCancelar`. `decisionCambiadaPorDecano` se calcula, no se guarda: es
`true` cuando `aprobadaPorDecano` es distinto de `cumpleCondiciones`, `false`
cuando coinciden y `null` mientras el Decano no decide. Lo que llega depende
del rol (sección 5).

Cuerpos de las acciones:

- `remitir`: `observacion` opcional y `evaluaciones`, una por asignatura:
  `{asignaturaSolicitudUuid, numeroFaltas, nota, situacionMatriculaUuid,
  situacionCancelarUuid, cumpleCondiciones, observacionEvaluacion}`. Faltas,
  nota y `situacionCancelarUuid` (opcional, P44) como en Cancelación de
  Matrícula. `cumpleCondiciones` es obligatorio; con una nota
  numérica menor a 3.0 no puede ser `true` (una nota de texto no tiene esa
  restricción); si es `false`,
  `observacionEvaluacion` es obligatoria (máximo 255). Al menos una
  asignatura debe cumplir.
- `aprobar` (P14): una de dos formas, nunca las dos ni ninguna.
  - `{aceptarEvaluacion: true}`: cada asignatura queda con `aprobadaPorDecano`
    igual a su `cumpleCondiciones`.
  - `{decisiones}`: una por asignatura, `{asignaturaSolicitudUuid, aprobada}`,
    con `aprobada` obligatorio. El Decano puede aprobar una que el
    Funcionario marcó como que no cumple y rechazar una que marcó como que
    cumple. Al menos una debe aprobarse; para negarlas todas se usa
    `decano/rechazar`.

  El cuerpo ya no lleva `situacionCancelarUuid` ni `observacionDecision`. La
  `observacion` general es opcional y solo la ven el Funcionario y el Decano.
  Las rechazadas pierden la situación al cancelar; las aprobadas conservan la
  que dejó el Funcionario al remitir (P44, P9).
- `rechazar`: como en Cancelación de Matrícula.
- `responder` (P45): `observacion` (máximo 500, la ve el Estudiante) y
  `ajustes`, una entrada por asignatura `{asignaturaSolicitudUuid,
  situacionCancelarUuid, observacionDecision}` (observación de máximo 255).
  Reglas:
  - Cada asignatura con `decisionCambiadaPorDecano` necesita su ajuste. Las
    demás pueden traer uno, con las mismas reglas.
  - El ajuste de una aprobada por el Decano exige `situacionCancelarUuid`
    (`observacionDecision` opcional).
  - El ajuste de una rechazada exige `observacionDecision` y no acepta
    situación. Esa observación la ve el Estudiante en APROBADA o RECHAZADA.
  - Si el Decano rechazó alguna asignatura, o la solicitud completa, la
    `observacion` general es obligatoria.
  - Desde RECHAZADA_POR_DECANO no se aceptan ajustes.

  Desde APROBADA_POR_DECANO cancela solo las asignaturas aprobadas que sigan
  activas.

Errores propios de esta evaluación y decisión (además de los de la tabla de
la sección 3.6 y de la 1.6):

| codigoError | Mensaje |
| --- | --- |
| 4 | `Falta la confirmación de las condiciones de la asignatura <nombre>...` |
| 4 | `La asignatura <nombre> tiene nota <nota>, menor a 3.0, y no puede marcarse como que cumple las condiciones...` |
| 4 | `La asignatura <nombre> no cumple las condiciones y requiere una observación de la evaluación...` |
| 4 | `Ninguna asignatura cumple las condiciones: en lugar de remitir la solicitud al Decano debe rechazarla...` |
| 4 | `Debe aceptar la evaluación del funcionario (aceptarEvaluacion) o enviar la decisión de cada asignatura (decisiones), una de las dos...` |
| 4 | `Faltan datos de las asignaturas de la solicitud: <nombres>...` (falta la decisión de alguna asignatura) |
| 4 | `Falta la decisión del Decano de la asignatura <nombre>...` (`aprobada` nulo) |
| 4 | `No se aprobó ninguna asignatura: para negar la solicitud completa use el rechazo del Decano...` |
| 4 | `El Decano cambió la evaluación de la asignatura <nombre> y falta su ajuste...` |
| 4 | `Falta la situación al cancelar de la asignatura <nombre>...` (ajuste de una aprobada) |
| 4 | `Falta la observación de la decisión de la asignatura <nombre>...` (ajuste de una rechazada) |
| 4 | `La asignatura <nombre> fue rechazada por el Decano y no lleva situación al cancelar...` |
| 4 | `La solicitud <radicado> fue rechazada por el Decano y no admite ajustes por asignatura...` |
| 4 | `El Decano rechazó la solicitud o alguna de sus asignaturas: la respuesta al estudiante requiere una observación...` |
| 4 | `La asignatura <uuid> no pertenece a la solicitud <radicado>...` y `La asignatura <nombre> viene más de una vez...` (decisiones y ajustes) |
| 6 | `La observación de la evaluación de la asignatura <nombre> supera los 255 caracteres permitidos...` (o `La observación de la decisión`) |
| 6 | `El número de faltas de la asignatura <nombre> es numérico y debe ser un entero mayor o igual a 0; si no es un número se guarda como texto...` |
| 6 | `La nota de la asignatura <nombre> es numérica y debe estar entre 0.0 y 5.0 con un decimal, con punto o coma; si no es un número se guarda como texto...` |
| 6 | `En la asignatura <nombre>, el número de faltas supera los 20 caracteres permitidos...` (o `la nota`) |

### 3.8 Exámenes supletorios (12)

| Método y ruta | Roles | Cuerpo | Respuesta |
| --- | --- | --- | --- |
| `GET examenes-supletorios/formulario` | Estudiante | | FormularioSupletorio |
| `POST examenes-supletorios` | Estudiante | Multipart (ver abajo) | `{uuidSolicitudAcademica, radicado}` |
| `GET examenes-supletorios/{uuidSolicitud}` | Estudiante, Funcionario Académico, Decano | | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/funcionario/rechazar` | Funcionario Académico | `{observacion}` | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/funcionario/remitir` | Funcionario Académico | `{requisitosVerificados, observacion}` | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/funcionario/responder` | Funcionario Académico | `{observacion}` opcional (mensaje al Estudiante, máximo 500), `{}` o sin cuerpo | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/funcionario/recibo` | Funcionario Académico | Sin cuerpo | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/funcionario/comprobante/aprobar` | Funcionario Académico | `{fechaAcordadaExamen}` opcional, o sin cuerpo | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/funcionario/comprobante/rechazar` | Funcionario Académico | `{observacion}` | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/decano/aprobar` | Decano | `{observacion}` opcional, o sin cuerpo | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/decano/rechazar` | Decano | `{observacion}` opcional, `{}` o sin cuerpo | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/estudiante/comprobante` | Estudiante | Sin cuerpo | ExamenSupletorioDetalle |

**FormularioSupletorio**: `asignaturas` (activas:
`{uuidAsignaturaMatriculada, codigoAsignatura, nombreAsignatura, grupo}`),
`causas` (`["cruce", "otra"]`), `anexos` (los tres de radicación:
`{uuidTipoAnexoAcademico, nombre, formatosPermitidos, tamanioMaximoBytes,
causas}`, donde `causas` dice con qué causa se exige: el FOR-23 con las dos,
el soporte de justificación con `otra` y el formato del docente cruzado con
`cruce`) y `plazoDiasHabiles` (3). No trae recibo ni comprobante.

Radicación (`multipart/form-data`):

- `asignaturaMatriculada`: uuid de la asignatura del examen no presentado.
- `fechaExamenNoPresentado`: `AAAA-MM-DD`.
- `tipoCausa`: `cruce` u `otra`, sin distinguir mayúsculas.
- Con `cruce`: `asignaturaCruzada` (otra asignatura activa del estudiante),
  `fechaExamenCruzada` (`AAAA-MM-DD`) y `horaExamenCruzada` (`HH:mm`). Con
  `otra` no se envían.
- Una parte de archivo por anexo, cuyo nombre es el `uuidTipoAnexoAcademico`:
  el FOR-23 siempre, más el de la causa. No se aceptan soportes libres, el
  recibo, el comprobante, el anexo de la otra causa ni anexos repetidos.

Plazo: se cuentan los días lunes a viernes posteriores al examen hasta hoy
inclusive, en hora de Colombia y sin festivos; más de 3 se rechaza. El mismo
día cuenta 0.

Errores de la radicación, en orden:

| codigoError | Mensaje |
| --- | --- |
| 4 | `El usuario <uuid> no tiene el rol Estudiante...` |
| 6 | `El valor <valor> de la fecha del examen no presentado no es una fecha con el formato AAAA-MM-DD...` (o `de la fecha del examen cruzado`) |
| 4 | `Ya tienes una solicitud de Examen Supletorio en curso (<radicado>), debe terminar antes de radicar otra...` |
| 4 | `Debe elegir la asignatura del examen no presentado...` |
| 4 | `La asignatura <uuid> no es una asignatura matriculada del estudiante <uuid>...` |
| 4 | `La asignatura <codigo - nombre> está <estado> y solo se aceptan asignaturas activas...` |
| 4 | `La causa <x> no es válida, debe ser cruce u otra...` |
| 4 | `La fecha del examen no presentado es obligatoria...` |
| 4 | `La fecha del examen no presentado (<dd/MM/aaaa>) no puede ser posterior a hoy (<dd/MM/aaaa>)...` |
| 4 | `El plazo para pedir el supletorio del examen del <dd/MM/aaaa> venció: el último día permitido era el <dd/MM/aaaa> (3 días hábiles)...` |
| 4 | `Con causa cruce debe elegir la asignatura con la que se cruza el examen...` |
| 4 | `La asignatura con la que se cruza debe ser distinta de la asignatura del examen no presentado...` |
| 4 | `Con causa cruce se requieren la fecha y la hora del examen de la asignatura con la que se cruza...` |
| 6 | `La hora del examen cruzado admite máximo 10 caracteres...` y `La hora del examen cruzado <x> no tiene el formato HH:mm...` |
| 4 | `Con causa otra no se aceptan la asignatura, la fecha ni la hora de un examen cruzado...` |
| 4 | `El tipo de solicitud Examen Supletorio no admite soportes libres...` |
| 4 | `El tipo de anexo <nombre> no pertenece al tipo de solicitud Examen Supletorio...` |
| 4 | `El anexo <Recibo de pago o Comprobante de pago> no se entrega al radicar el examen supletorio...` |
| 4 | `El anexo <nombre> no corresponde a la causa <cruce u otra>...` |
| 4 | `El anexo <nombre> viene más de una vez...` |
| 4 | `Falta el anexo obligatorio: <nombres separados por "; ">...` |
| 6 | Errores de archivo de la sección 1.4 |

**ExamenSupletorioDetalle**: `solicitud` (SolicitudAcademicaDetalleDTORespuesta),
`asignatura` (`{uuidAsignaturaMatriculada, codigoAsignatura, nombreAsignatura,
grupo}`), `tipoCausa` (`cruce` u `otra`), `fechaExamenNoPresentado`
(`AAAA-MM-DD`), `cruce` (nulo con causa `otra`; si no, `{asignatura,
fechaExamenCruzada, horaExamenCruzada}`) y `fechaAcordadaExamen`
(`AAAA-MM-DD` o nulo). Es igual para los tres roles.

Cuerpos y reglas de las acciones:

- `funcionario/rechazar`: `observacion` obligatoria; no lleva Resolución.
- `funcionario/remitir`: el cuerpo es obligatorio; `requisitosVerificados`
  debe ser `true`, si no responde 4 `Para remitir la solicitud <radicado> al
  Decano debe confirmar que verificó los requisitos; si no se cumplen, debe
  rechazar la solicitud en lugar de remitirla...`. `observacion` es opcional.
- `decano/aprobar` y `decano/rechazar`: `observacion` opcional, que el
  Estudiante no ve.
- `funcionario/responder`: de RECHAZADA_POR_DECANO a RECHAZADA, con
  `observacion` opcional (mensaje al Estudiante, máximo 500).
- `funcionario/recibo`: de APROBADA_POR_DECANO a PENDIENTE_PAGO. El recibo se
  sube antes con `POST solicitudes-academicas/{uuid}/anexos` y `tipoAnexo` =
  uuid de `Recibo de pago`.
- `estudiante/comprobante`: de PENDIENTE_PAGO a EN_VERIFICACION_PAGO. El
  comprobante se sube antes del mismo modo, con el uuid de `Comprobante de
  pago`.
- `funcionario/comprobante/aprobar`: a APROBADA. `fechaAcordadaExamen`
  (`AAAA-MM-DD`) es opcional y no puede ser anterior a la fecha del examen no
  presentado: 4 `La fecha acordada para el supletorio (<dd/MM/aaaa>) no puede
  ser anterior a la fecha del examen no presentado (<dd/MM/aaaa>)...`.
- `funcionario/comprobante/rechazar`: `observacion` obligatoria, a RECHAZADA.
  El estudiante debe radicar una solicitud nueva.

Ninguna acción del supletorio cambia el estado de las asignaturas
matriculadas.

## 4. Recorridos típicos

En todos los recorridos, después de cada acción conviene volver a leer el
detalle (las acciones ya lo devuelven) y mostrar los botones según
`accionesDisponibles`.

### 4.1 Cancelación de Matrícula

| Paso | Rol | Llamadas |
| --- | --- | --- |
| 1 | Estudiante | `GET cancelaciones-matricula/formulario` |
| 2 | Estudiante | `POST cancelaciones-matricula` con `motivo`, los seis anexos y soportes opcionales. Queda en RADICADA |
| 3 | Funcionario Académico | `GET solicitudes-academicas/funcionario`, luego `GET cancelaciones-matricula/{uuid}`; descarga anexos con `GET solicitudes-academicas/{uuid}/anexos/{uuidAnexo}` |
| 4a | Funcionario Académico | Rechazo: `POST solicitudes-academicas/{uuid}/resolucion` con el escaneo y `POST cancelaciones-matricula/{uuid}/funcionario/rechazar`. Queda en RECHAZADA |
| 4b | Funcionario Académico | Remisión: `GET catalogos-academicos/situaciones` y `POST cancelaciones-matricula/{uuid}/funcionario/remitir` con las evaluaciones. Queda en EN_REVISION_DECANO |
| 5 | Decano | `GET solicitudes-academicas/decano`, `GET cancelaciones-matricula/{uuid}` y `POST .../decano/aprobar` con las situaciones al cancelar, o `POST .../decano/rechazar` con observación |
| 6 | Funcionario Académico | `POST solicitudes-academicas/{uuid}/resolucion` con el escaneo firmado y `POST cancelaciones-matricula/{uuid}/funcionario/responder`. Queda en APROBADA (asignaturas canceladas) o RECHAZADA |
| 7 | Estudiante | `GET solicitudes-academicas/estudiante`, `GET cancelaciones-matricula/{uuid}` y, en etapa final, `GET solicitudes-academicas/{uuid}/resolucion` |

### 4.2 Cancelación de Asignatura

| Paso | Rol | Llamadas |
| --- | --- | --- |
| 1 | Estudiante | `GET cancelaciones-asignatura/formulario` |
| 2 | Estudiante | `POST cancelaciones-asignatura` con `motivo`, uno o más `asignaturas` y soportes opcionales. Queda en RADICADA |
| 3 | Funcionario Académico | `GET solicitudes-academicas/funcionario` y `GET cancelaciones-asignatura/{uuid}` |
| 4a | Funcionario Académico | Rechazo completo: escaneo con `POST solicitudes-academicas/{uuid}/resolucion` y `POST .../funcionario/rechazar` |
| 4b | Funcionario Académico | Remisión: `POST .../funcionario/remitir` con faltas, nota, situación, `cumpleCondiciones` y observación de cada asignatura |
| 5 | Decano | `POST .../decano/aprobar` con `aceptarEvaluacion: true` o con la decisión de cada asignatura (aprobación parcial), o `POST .../decano/rechazar` para negar la solicitud completa |
| 6 | Funcionario Académico | Escaneo y `POST .../funcionario/responder` con `observacion` y un ajuste por cada asignatura con `decisionCambiadaPorDecano`. En APROBADA solo se cancelan las asignaturas aprobadas |
| 7 | Estudiante | `GET cancelaciones-asignatura/{uuid}` y, en etapa final, la Resolución |

### 4.3 Examen Supletorio

| Paso | Rol | Llamadas |
| --- | --- | --- |
| 1 | Estudiante | `GET examenes-supletorios/formulario` |
| 2 | Estudiante | `POST examenes-supletorios` con asignatura, fecha, causa, datos de cruce si aplica y anexos de la causa, dentro de los 3 días hábiles. Queda en RADICADA |
| 3a | Funcionario Académico | Rechazo: `POST .../funcionario/rechazar` con observación (sin Resolución) |
| 3b | Funcionario Académico | Remisión: `POST .../funcionario/remitir` con `requisitosVerificados: true` |
| 4 | Decano | `POST .../decano/aprobar` (observación opcional) o `POST .../decano/rechazar` |
| 5a | Funcionario Académico | Si el Decano rechazó: `POST .../funcionario/responder`. Queda en RECHAZADA |
| 5b | Funcionario Académico | Si aprobó: `POST solicitudes-academicas/{uuid}/anexos` con el recibo (`tipoAnexo` = uuid de `Recibo de pago`) y `POST .../funcionario/recibo`. Queda en PENDIENTE_PAGO |
| 6 | Estudiante | Descarga el recibo con `GET solicitudes-academicas/{uuid}/anexos/{uuidAnexo}`, paga, sube el comprobante con `POST solicitudes-academicas/{uuid}/anexos` (`tipoAnexo` = uuid de `Comprobante de pago`) y llama `POST .../estudiante/comprobante`. Queda en EN_VERIFICACION_PAGO |
| 7 | Funcionario Académico | `POST .../funcionario/comprobante/aprobar` con la fecha acordada opcional (APROBADA) o `POST .../funcionario/comprobante/rechazar` con observación (RECHAZADA) |

## 5. Lo que ve cada rol en el detalle

| Proceso | Estudiante dueño | Funcionario asignado | Decano |
| --- | --- | --- | --- |
| Común (`solicitudes-academicas/{uuid}`) | Todo el detalle desde RADICADA; `puedeDescargarResolucion` solo en APROBADA o RECHAZADA | Todo, en todas las etapas | Todo, desde EN_REVISION_DECANO; no ve RADICADA |
| Cancelación de Matrícula | Todo, salvo `situacionCancelar`, que llega nula hasta que la solicitud está en APROBADA o RECHAZADA | Todo | Todo |
| Cancelación de Asignatura | Antes de la etapa final, de cada asignatura solo `uuidAsignaturaSolicitud`, `codigoAsignatura` y `nombreAsignatura`. En APROBADA o RECHAZADA, además `aprobadaPorDecano` y `observacionDecision` (la que escribe el Funcionario en el ajuste). Nunca faltas, nota, situaciones, `cumpleCondiciones`, `observacionEvaluacion` ni `decisionCambiadaPorDecano` (llega nulo) | Todo | Todo |
| Examen Supletorio | Todo | Todo | Todo |

El historial y las descargas de anexos siguen la misma visibilidad que el
detalle común: un rol que no ve la solicitud recibe el error de inexistente.
Un Funcionario Académico que no tiene asignado el tipo no ve sus
solicitudes, salvo que también tenga el rol Decano.
