# Contrato de la API de los procesos académicos

Este documento describe los 63 endpoints que la extensión agrega al backend:
catálogo de asignaturas, estudiantes, funcionarios académicos, catálogos
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
| 2 | La entidad ya existe | Código de asignatura, código estudiantil, documento, correo o username repetidos contra la base |
| 3 | La entidad no existe | uuid inexistente, o una solicitud que el usuario no puede ver (responde igual que una inexistente) |
| 4 | Regla de negocio violada | Etapa que no admite la acción, anexo faltante, plazo vencido, solicitud en curso |
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

| etapaCodigo | Procesos | Etiqueta Estudiante | Etiqueta Funcionario | Etiqueta Decano |
| --- | --- | --- | --- | --- |
| RADICADA | los tres | En trámite | Pendiente | (no la ve) |
| EN_REVISION_DECANO | los tres | En trámite | En Gestión | Pendiente |
| APROBADA_POR_DECANO | los tres | En trámite | Pendiente de Respuesta | Respondida |
| RECHAZADA_POR_DECANO | los tres | En trámite | Pendiente de Respuesta | Respondida |
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
| REMITIR_DECANO | `POST <proceso>/{uuid}/funcionario/remitir` | Funcionario Académico | Evaluaciones (cancelaciones) o `requisitosVerificados` (supletorio) |
| APROBAR_DECANO | `POST <proceso>/{uuid}/decano/aprobar` | Decano | Situaciones al cancelar (matrícula) o decisiones por asignatura (asignatura) |
| RECHAZAR_DECANO | `POST <proceso>/{uuid}/decano/rechazar` | Decano | Observación |
| ENVIAR_RESPUESTA | `POST <proceso>/{uuid}/funcionario/responder` | Funcionario Académico | En cancelaciones, escaneo de la Resolución subido antes |
| ENVIAR_RECIBO | `POST examenes-supletorios/{uuid}/funcionario/recibo` | Funcionario Académico | Recibo de pago subido antes |
| SUBIR_COMPROBANTE | `POST examenes-supletorios/{uuid}/estudiante/comprobante` | Estudiante | Comprobante de pago subido antes |
| APROBAR_COMPROBANTE | `POST examenes-supletorios/{uuid}/funcionario/comprobante/aprobar` | Funcionario Académico | Fecha acordada opcional |
| RECHAZAR_COMPROBANTE | `POST examenes-supletorios/{uuid}/funcionario/comprobante/rechazar` | Funcionario Académico | Observación |

La observación de una acción admite hasta 500 caracteres. Las acciones
responden el detalle del proceso ya actualizado. Errores comunes a todas:

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

**AsignaturaDTORespuesta**: `uuidAsignatura`, `codigoAsignatura`,
`nombreAsignatura` (texto).

**AsignaturaMatriculadaDTORespuesta**: `uuidAsignaturaMatriculada`,
`uuidAsignatura`, `codigoAsignatura`, `nombreAsignatura`, `grupo`, `estado`
(texto; `activa`, `cancelada`, `aprobada` o `perdida`).

**EstudianteDTORespuesta**: `uuidUsuario`, `nombres`, `apellidos`,
`tipoDocumento`, `numeroDocumento`, `telefono`, `correoElectronico`,
`username`, `codigoEstudiantil`, `programaAcademico`, `semestre`, `facultad`
(texto), `estado` (booleano, usuario activo) y `asignaturasMatriculadas`
(lista de AsignaturaMatriculadaDTORespuesta). Nunca trae la contraseña.

**FuncionarioAcademicoDTORespuesta**: `uuidUsuario`, `nombres`, `apellidos`,
`tipoDocumento`, `numeroDocumento`, `telefono`, `correoElectronico`,
`username`, `dependencia` (texto), `estado` (booleano) y `tiposSolicitud`
(lista de `{uuidTipoSolicitudAcademica, nombre}` que atiende).

**TipoSolicitudAcademicaDTORespuesta**: `uuidTipoSolicitudAcademica`,
`nombre`, `descripcion`, `uuidFuncionarioAcademico`,
`nombreFuncionarioAcademico` (nombres y apellidos) (texto).

**SolicitudAcademicaResumenDTORespuesta** (bandejas):
`uuidSolicitudAcademica`, `radicado`, `uuidTipoSolicitudAcademica`,
`tipoSolicitud` (nombre del tipo), `fechaCreacion` (fecha y hora),
`etiqueta`, `etapaCodigo`, `nombreEstudiante` (nombres y apellidos),
`codigoEstudiantil`.

**SolicitudAcademicaDetalleDTORespuesta** (detalle común):

| Campo | Tipo | Descripción |
| --- | --- | --- |
| `uuidSolicitudAcademica`, `radicado`, `uuidTipoSolicitudAcademica`, `tipoSolicitud` | texto | |
| `fechaCreacion` | fecha y hora | |
| `etiqueta` | texto | Etiqueta de la etapa para el rol que consulta |
| `etapaCodigo` | texto | Código de la etapa (sección 1.5) |
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
`nombresUsuario`, `apellidosUsuario`.

**SituacionAsignaturaDTORespuesta**: `uuidSituacionAcademica`, `codigo`
(R0 a R3), `nombre`.

## 3. Endpoints

### 3.1 Asignaturas (5)

| Método y ruta | Roles | Parámetros | Cuerpo | Respuesta |
| --- | --- | --- | --- | --- |
| `POST asignaturas` | Secretario General, Decano | | `{codigoAsignatura, nombreAsignatura}` | AsignaturaDTORespuesta |
| `GET asignaturas/paginado` | Secretario General, Decano, Funcionario Académico | `pagina`, `tamanio` | | Página de AsignaturaDTORespuesta, por código |
| `GET asignaturas/filtro` | Secretario General, Decano, Funcionario Académico | `texto` (opcional; busca en código y nombre, sin distinguir mayúsculas), `pagina`, `tamanio` | | Página de AsignaturaDTORespuesta |
| `GET asignaturas/{uuidAsignatura}` | Secretario General, Decano, Funcionario Académico | | | AsignaturaDTORespuesta |
| `PUT asignaturas/{uuidAsignatura}` | Secretario General, Decano | | `{codigoAsignatura, nombreAsignatura}` | AsignaturaDTORespuesta |

Cuerpo: `codigoAsignatura` obligatorio, máximo 45; `nombreAsignatura`
obligatorio, máximo 150 (400 si no se cumple).

Errores: 2 `Asignatura con codigo: <codigo> existe en el sistema...`; 3
`Asignatura con id <uuid> no fue encontrado en el sistema...`.

### 3.2 Estudiantes (9)

| Método y ruta | Roles | Parámetros | Cuerpo | Respuesta |
| --- | --- | --- | --- | --- |
| `POST estudiantes` | Secretario General, Decano | | EstudianteDTOPeticion | EstudianteDTORespuesta |
| `POST estudiantes/cargar/archivo` | Secretario General, Decano | | Multipart, parte `file` (.xlsx) | Lista de EstudianteDTORespuesta |
| `GET estudiantes/paginado` | Secretario General, Decano, Funcionario Académico | `pagina`, `tamanio` | | Página de EstudianteDTORespuesta, por código estudiantil |
| `GET estudiantes/filtro` | Secretario General, Decano, Funcionario Académico | `nombre`, `apellido`, `codigo` (opcionales; contienen, sin distinguir mayúsculas), `pagina`, `tamanio` | | Página de EstudianteDTORespuesta |
| `GET estudiantes/mis-asignaturas` | Estudiante | | | Lista de AsignaturaMatriculadaDTORespuesta activas del estudiante autenticado, por código |
| `GET estudiantes/{uuidEstudiante}` | Secretario General, Decano, Funcionario Académico | | | EstudianteDTORespuesta |
| `PUT estudiantes/{uuidEstudiante}` | Secretario General, Decano | | `{codigoEstudiantil, programaAcademico, semestre, facultad}`, todos opcionales; los vacíos no cambian | EstudianteDTORespuesta |
| `POST estudiantes/{uuidEstudiante}/asignaturas` | Secretario General, Decano | | `{codigoAsignatura, nombreAsignatura, grupo}` | AsignaturaMatriculadaDTORespuesta, en estado `activa` |
| `PATCH estudiantes/{uuidEstudiante}/asignaturas/{uuidMatricula}/estado` | Secretario General, Decano | | `{estado}` | AsignaturaMatriculadaDTORespuesta |

EstudianteDTOPeticion: `nombres`, `apellidos` (obligatorios, máximo 1000),
`tipoDocumento` (`Cédula de ciudadanía`, `Tarjeta de identidad` o `Cédula de
extranjería`, sin distinguir mayúsculas), `numeroDocumento` (5 a 255),
`telefono` (5 a 1000), `correoElectronico` (correo válido), `username` (5 a
255), `password` (5 a 255), `codigoEstudiantil` (máximo 45),
`programaAcademico` (máximo 100), `semestre` (máximo 10), `facultad` (máximo
100), `asignaturas` (al menos una: `{codigoAsignatura, nombreAsignatura,
grupo}`). El rol y el tipo de usuario los asigna el backend. Una asignatura se
busca por su código en el catálogo y, si no existe, se crea.

Excel de estudiantes: hoja 1, fila 1 con estos encabezados en este orden:
`nombres, apellidos, tipoDocumento, numeroDocumento, telefono,
correoElectronico, username, password, codigoEstudiantil, programaAcademico,
semestre, facultad, codigoAsignatura, nombreAsignatura, grupo`. Una fila por
asignatura matriculada; las filas del mismo `numeroDocumento` se juntan en un
estudiante y deben repetir los mismos datos. Termina en la primera fila
vacía. Hay una plantilla en `backend/pruebas/plantilla-estudiantes.xlsx`.

Errores principales:

- 400 en la carga: mapa `{"campo": "Fila <n>, columna <letra> (<campo>): <mensaje>"}`.
- 2: `Estudiante con codigo estudiantil: <x> existe en el sistema...`, `Usuario
  con numero de documento: <x> existe...`, `... correo electronico ...`,
  `... username ...`.
- 3: `Estudiante con id <uuid> no fue encontrado en el sistema...`; en
  `mis-asignaturas`, `Estudiante con username: <x> no fue encontrado en el
  sistema...`; `Asignatura matriculada con id <uuid> no fue encontrado...`.
- 6: `La petición trae <dato> <valor> más de una vez...`; `El estudiante ya
  tiene activa la asignatura <codigo> en el grupo <grupo>...`; `El estado <x>
  no es valido, los estados permitidos son activa, cancelada, aprobada,
  perdida...`; `No se puede cambiar la asignatura matriculada de <actual> a
  <nuevo>...` (solo se cambia desde `activa`); `Tipo de documento invalido...`.
- 6 en la carga: `El archivo debe ser un Excel .xlsx valido con una hoja...`;
  `Fila 1, columna <letra>: se esperaba el encabezado <campo> y se encontró
  '<valor>'...`; `El archivo no tiene filas de estudiantes despues del
  encabezado...`; `Fila <n>, columna <letra> (<campo>): el estudiante con
  documento <doc> tiene un valor distinto al de la fila <m>...`; `... la
  asignatura <codigo> en el grupo <grupo> ya aparece en la fila <m> para el
  mismo estudiante...`; `... el valor <x> ya lo usa otro estudiante en la fila
  <m>...`.

### 3.3 Funcionarios académicos (6)

| Método y ruta | Roles | Parámetros | Cuerpo | Respuesta |
| --- | --- | --- | --- | --- |
| `POST funcionarios-academicos` | Secretario General, Decano | | FuncionarioAcademicoDTOPeticion | FuncionarioAcademicoDTORespuesta |
| `POST funcionarios-academicos/cargar/archivo` | Secretario General, Decano | | Multipart, parte `file` (.xlsx) | Lista de FuncionarioAcademicoDTORespuesta |
| `GET funcionarios-academicos/paginado` | Secretario General, Decano, Funcionario Académico | `pagina`, `tamanio` | | Página, por apellidos y nombres |
| `GET funcionarios-academicos/filtro` | Secretario General, Decano, Funcionario Académico | `nombre`, `apellido`, `dependencia` (opcionales; contienen, sin distinguir mayúsculas), `pagina`, `tamanio` | | Página |
| `GET funcionarios-academicos/{uuidFuncionario}` | Secretario General, Decano, Funcionario Académico | | | FuncionarioAcademicoDTORespuesta |
| `PUT funcionarios-academicos/{uuidFuncionario}` | Secretario General, Decano | | `{dependencia}` (obligatoria, máximo 100) | FuncionarioAcademicoDTORespuesta |

FuncionarioAcademicoDTOPeticion: los mismos datos personales y de acceso que
el estudiante (`nombres`, `apellidos`, `tipoDocumento`, `numeroDocumento`,
`telefono`, `correoElectronico`, `username`, `password`) más `dependencia`
(máximo 100). El backend le asigna el rol `Funcionario Académico` y el tipo
`Empleado FIET - Funcionario`.

Excel: encabezados `nombres, apellidos, tipoDocumento, numeroDocumento,
telefono, correoElectronico, username, password, dependencia`; una fila por
funcionario. Plantilla en
`backend/pruebas/plantilla-funcionarios-academicos.xlsx`.

Errores: 400 como en estudiantes; 2 por documento, correo o username en la
base; 3 `Funcionario Académico con id <uuid> no fue encontrado en el
sistema...`; 6 por repetidos dentro de la petición, archivo no válido,
encabezado distinto, `El archivo no tiene filas de datos despues del
encabezado...` y `Fila <n>, columna <letra> (<campo>): el valor <x> ya
aparece en la fila <m> del archivo...`.

### 3.4 Catálogos académicos (6)

| Método y ruta | Roles | Parámetros | Cuerpo | Respuesta |
| --- | --- | --- | --- | --- |
| `GET catalogos-academicos/tipos-solicitud` | Cualquier usuario autenticado | | | Lista de TipoSolicitudAcademicaDTORespuesta, por nombre |
| `GET catalogos-academicos/tipos-solicitud/{uuidTipo}/etapas` | Cualquier usuario autenticado | | | Lista de `{uuidEtapa, codigo, uuidTipoSolicitudAcademica}` (universales y propias del tipo), por código |
| `GET catalogos-academicos/tipos-solicitud/{uuidTipo}/tipos-anexo` | Cualquier usuario autenticado | | | Lista de `{uuidTipoAnexoAcademico, uuidTipoSolicitudAcademica, nombre, formatosPermitidos, obligatorio}`, por nombre |
| `GET catalogos-academicos/etiquetas` | Cualquier usuario autenticado | `rol`: `ESTUDIANTE`, `FUNCIONARIO` o `DECANO` (sin distinguir mayúsculas) | | Lista de `{uuidEtapa, codigoEtapa, rol, etiqueta}`, por código de etapa |
| `GET catalogos-academicos/situaciones` | Cualquier usuario autenticado | | | Lista de SituacionAsignaturaDTORespuesta, por código |
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
| `POST cancelaciones-matricula/{uuidSolicitud}/funcionario/responder` | Funcionario Académico | Sin cuerpo | CancelacionMatriculaDetalle |
| `POST cancelaciones-matricula/{uuidSolicitud}/decano/aprobar` | Decano | `{situaciones}` | CancelacionMatriculaDetalle |
| `POST cancelaciones-matricula/{uuidSolicitud}/decano/rechazar` | Decano | `{observacion}` | CancelacionMatriculaDetalle |

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
`codigoAsignatura`, `nombreAsignatura`, `numeroFaltas` (entero o nulo),
`nota` (número o nulo), `situacionMatricula` y `situacionCancelar`
(SituacionAsignaturaDTORespuesta o nulo).

Cuerpos de las acciones:

- `remitir`: `observacion` opcional y `evaluaciones`, una por asignatura de la
  solicitud: `{asignaturaSolicitudUuid, numeroFaltas, nota,
  situacionMatriculaUuid}`. `numeroFaltas` entero mayor o igual a 0, `nota` de
  0.0 a 5.0 con un decimal, `situacionMatriculaUuid` de
  `GET catalogos-academicos/situaciones`.
- `aprobar`: `situaciones`, una por asignatura:
  `{asignaturaSolicitudUuid, situacionCancelarUuid}`.
- `rechazar` (Funcionario y Decano): `observacion` obligatoria. El rechazo del
  Funcionario exige además el escaneo de la Resolución, subido antes con
  `POST solicitudes-academicas/{uuid}/resolucion`.
- `responder`: sin cuerpo; exige el escaneo de la Resolución. Desde
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
| `POST cancelaciones-asignatura/{uuidSolicitud}/funcionario/responder` | Funcionario Académico | Sin cuerpo | CancelacionAsignaturaDetalle |
| `POST cancelaciones-asignatura/{uuidSolicitud}/decano/aprobar` | Decano | `{decisiones}` | CancelacionAsignaturaDetalle |
| `POST cancelaciones-asignatura/{uuidSolicitud}/decano/rechazar` | Decano | `{observacion}` | CancelacionAsignaturaDetalle |

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
(booleano), `observacionDecision` y `situacionCancelar`. Lo que llega depende
del rol (sección 5).

Cuerpos de las acciones:

- `remitir`: `observacion` opcional y `evaluaciones`, una por asignatura:
  `{asignaturaSolicitudUuid, numeroFaltas, nota, situacionMatriculaUuid,
  cumpleCondiciones, observacionEvaluacion}`. `cumpleCondiciones` es
  obligatorio; con nota menor a 3.0 no puede ser `true`; si es `false`,
  `observacionEvaluacion` es obligatoria (máximo 255). Al menos una
  asignatura debe cumplir.
- `aprobar`: `decisiones`, una por asignatura: `{asignaturaSolicitudUuid,
  aprobada, situacionCancelarUuid, observacionDecision}`. `aprobada` es
  obligatorio; solo se aprueba una asignatura que cumple, y la aprobada exige
  `situacionCancelarUuid`; la rechazada exige `observacionDecision` (máximo
  255). Al menos una debe aprobarse; para negarlas todas se usa
  `decano/rechazar`.
- `rechazar` y `responder`: como en Cancelación de Matrícula. `responder`
  desde APROBADA_POR_DECANO cancela solo las asignaturas aprobadas que sigan
  activas.

Errores propios de esta evaluación y decisión (además de los de la tabla de
la sección 3.6 y de la 1.6):

| codigoError | Mensaje |
| --- | --- |
| 4 | `Falta la confirmación de las condiciones de la asignatura <nombre>...` |
| 4 | `La asignatura <nombre> tiene nota <nota>, menor a 3.0, y no puede marcarse como que cumple las condiciones...` |
| 4 | `La asignatura <nombre> no cumple las condiciones y requiere una observación de la evaluación...` |
| 4 | `Ninguna asignatura cumple las condiciones: en lugar de remitir la solicitud al Decano debe rechazarla...` |
| 4 | `Falta la decisión del Decano de la asignatura <nombre>...` |
| 4 | `La asignatura <nombre> no cumple las condiciones y su cancelación no se puede aprobar...` |
| 4 | `Rechazar la cancelación de la asignatura <nombre> exige una observación de la decisión...` |
| 4 | `No se aprobó ninguna asignatura: para negar la solicitud completa use el rechazo del Decano...` |
| 6 | `La observación de la evaluación de la asignatura <nombre> supera los 255 caracteres permitidos...` (o `La observación de la decisión`) |

### 3.8 Exámenes supletorios (12)

| Método y ruta | Roles | Cuerpo | Respuesta |
| --- | --- | --- | --- |
| `GET examenes-supletorios/formulario` | Estudiante | | FormularioSupletorio |
| `POST examenes-supletorios` | Estudiante | Multipart (ver abajo) | `{uuidSolicitudAcademica, radicado}` |
| `GET examenes-supletorios/{uuidSolicitud}` | Estudiante, Funcionario Académico, Decano | | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/funcionario/rechazar` | Funcionario Académico | `{observacion}` | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/funcionario/remitir` | Funcionario Académico | `{requisitosVerificados, observacion}` | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/funcionario/responder` | Funcionario Académico | Sin cuerpo | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/funcionario/recibo` | Funcionario Académico | Sin cuerpo | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/funcionario/comprobante/aprobar` | Funcionario Académico | `{fechaAcordadaExamen}` opcional, o sin cuerpo | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/funcionario/comprobante/rechazar` | Funcionario Académico | `{observacion}` | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/decano/aprobar` | Decano | `{observacion}` opcional, o sin cuerpo | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/decano/rechazar` | Decano | `{observacion}` | ExamenSupletorioDetalle |
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
- `decano/aprobar`: `observacion` opcional. `decano/rechazar`: obligatoria.
- `funcionario/responder`: de RECHAZADA_POR_DECANO a RECHAZADA.
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
| 5 | Decano | `POST .../decano/aprobar` con la decisión de cada asignatura (aprobación parcial), o `POST .../decano/rechazar` para negar la solicitud completa |
| 6 | Funcionario Académico | Escaneo y `POST .../funcionario/responder`. En APROBADA solo se cancelan las asignaturas aprobadas |
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
| Cancelación de Asignatura | Antes de la etapa final, de cada asignatura solo `uuidAsignaturaSolicitud`, `codigoAsignatura` y `nombreAsignatura`. En APROBADA o RECHAZADA, además `aprobadaPorDecano` y `observacionDecision`. Nunca faltas, nota, situaciones, `cumpleCondiciones` ni `observacionEvaluacion` | Todo | Todo |
| Examen Supletorio | Todo | Todo | Todo |

El historial y las descargas de anexos siguen la misma visibilidad que el
detalle común: un rol que no ve la solicitud recibe el error de inexistente.
Un Funcionario Académico que no tiene asignado el tipo no ve sus
solicitudes, salvo que también tenga el rol Decano.
