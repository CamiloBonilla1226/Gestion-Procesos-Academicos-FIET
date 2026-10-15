

===== 09-flujos-y-estados.md =====

# 09. Flujos de extremo a extremo y máquinas de estado

Análisis de solo lectura. Estado analizado: árbol de trabajo de la rama `main` con cambios sin confirmar (entre ellos los del Examen Supletorio, decisión P49). No se ejecutó ni compiló nada: todo lo que dice "verificado" es lectura estática del código o de los documentos 02 a 08. Lo que no se pudo comprobar así se marca NO VERIFICADO.

## 0. Convenciones

- Marca de origen: [BASE] es el prototipo de Julián Camacho (commit inicial `d3a48cc`); [EXTENSIÓN] es el trabajo de grado. El criterio es el de los documentos 02, 04 y 07.
- Rutas de código. Backend: `backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/`, abreviado `bk/`; dentro de él, `infraestructura/input/controlador<X>/controlador/` contiene los `*RestController`, `dominio/casosdeuso/` los `*CUImplAdaptador`, `dominio/servicios/` las reglas de dominio y `infraestructura/output/persistencia/gateway/` los `*GatewayImplAdaptador`. Frontend: `frontend/src/app/`, abreviado `fe/`; los servicios HTTP están en `fe/core/services/`.
- Cadena que recorre toda petición (documento 02 y `backend/CLAUDE.md`): componente Angular, servicio Angular, `authInterceptor` (agrega `Authorization: Bearer`), `*RestController`, puerto `*CUIntPuerto`, `*CUImplAdaptador`, puerto `*GatewayIntPuerto`, `*GatewayImplAdaptador`, repositorio JPA, tabla.
- Prefijo de todas las rutas HTTP: `/api/unicauca/fiet/consejo` (`environment.apiUrl` en el frontend; `url.application` en el backend). En las tablas se omite.
- Columnas de las tablas de secuencia: `#`, pantalla o componente, servicio Angular y método, endpoint, controlador y caso de uso, gateway(s), tablas. En tablas, "E" significa que escribe y "L" que solo lee. Los nombres en minúscula o camelCase son tablas de Julián; los de MAYÚSCULA con guion bajo son de la extensión (estrategia `PhysicalNamingStrategyStandardImpl`, `backend/CLAUDE.md`).
- Dos mecanismos de historial que no hay que confundir (documento 04, sección 12): la tabla `logs` (auditoría de Julián, la escribe `LogCUIntPuerto`) y `HISTORIAL_SOLICITUD_ACADEMICA` (historial funcional de la extensión, solo lo escribe `SolicitudAcademicaCUImplAdaptador`).
- Errores: las excepciones propias salen como HTTP 500 con `codigoError` (1 a 8); la validación de DTO sale 400; `AuthenticationException` y `AccessDeniedException` salen 403 (documento 04, sección 0). En las tablas, "E3", "E4", etc. son esos códigos.

## 1. Flujos del prototipo de Julián [BASE]

### 1.1 Inicio de sesión

Pantalla `LoginComponent` (`fe/core/auth/pages/login-component`) con `LoginFormComponent` (`fe/shared/login-form-component`). Ruta `login`, sin guard.

| # | Pantalla / componente | Servicio Angular | Endpoint | Controlador y caso de uso | Gateway | Tablas |
|---|---|---|---|---|---|---|
| 1 | `LoginFormComponent.onSubmit` (bandera `enviando`) | `AuthService.login` (`auth-service.ts`) | `POST /sesiones` (única ruta `permitAll` para todos los métodos, `ConfiguracionSeguridad` línea 49) | `SesionRestController.auth` (sin `@Transactional`), `SesionCUImplAdaptador.login` | `SesionGatewayIntPuerto.login` implementado por `SesionGatewayImplAdaptador`: `UsuarioRepositorio.findByUsername`, `AuthenticationManager.authenticate` (`DaoAuthenticationProvider` con `BCryptPasswordEncoder`, `UserDetailsService` de `ConfiguracionAplicacion`) y `JwtServicio.getToken` | L: `usuarios`, `usuariosLivianos`, `Usuario_has_Roles` (carga `EAGER`), `roles` |
| 2 | (mismo caso de uso) | | | `SesionCUImplAdaptador.login`: si el token es nulo, E5; usuario inactivo, E8 `NO_ACCESO`; algún rol inactivo, E8 `ROL_NO_HABILITADO` | `SesionGatewayIntPuerto.getUsuario` | L: `usuarios` y relacionadas |
| 3 | (mismo caso de uso) | | | `LogCUIntPuerto.crearLogSesion("Inicio de sesión", ...)` | `LogGatewayIntPuerto.crearLog` (`LogGatewayImplAdaptador`, `LogRepositorio.save`) | E: `logs` |
| 4 | `AuthService.login`, operador `tap` | | | Respuesta `UsuarioTokenizadoDTORespuesta` (uuid y token) | | `localStorage`: `currentUser` y `authToken` |
| 5 | `AuthService.login`, operador `switchMap` | `UsuariosService.getUsuario` | `GET /usuarios/{uuid}` (regla `GET usuarios/**` solo autenticado) | `UsuarioRestController.getUsuario`, `UsuarioCUImplAdaptador.getUsuario` | `UsuarioGatewayIntPuerto.getUsuario` (`UsuarioGatewayImplAdapter`) | L: `usuarios`, `usuariosLivianos`, `Usuario_has_Roles`, `roles`, `tiposUsuario` |
| 6 | `LoginFormComponent.onSubmit`, `next` | `rutaDeInicio` (`fe/core/utils/rutas-por-rol.ts`) | | Navega a la ruta del primer rol que coincida: Secretario General, Funcionario, Secretaria Decanatura FIET, Decano, Funcionario Académico, Estudiante, `ROLES_FIET`; sin coincidencia, toast "El usuario no tiene un rol con vistas definidas en el sistema." | | |

Peticiones posteriores: `authInterceptor` agrega el `Bearer`; `JwtFiltroAutenticacion.doFilterInternal` valida el token y carga el `UserDetails`; el token es HS256 y vence a las 24 horas (`JwtServicio.getToken`). `RoleGuard` repite `GET /usuarios/{uuid}` en cada navegación a ruta protegida (documento 07, sección 4). Un 401 en cualquier petición distinta de `POST sesiones` cierra la sesión (`SesionVencidaService`) [EXTENSIÓN].

Observación: una contraseña errónea no llega a E5. `AuthenticationManager.authenticate` lanza una `AuthenticationException` dentro del gateway, que el manejador responde como 403; E5 solo ocurre si el username no existe (el gateway devuelve nulo). El 403 es lectura de código; NO VERIFICADO en ejecución.

### 1.2 Crear una solicitud del Consejo con anexos

Pantalla `usuario-fiet/solicitudes` (`UsuarioFietSolicitudesPrivadasContentComponent`) con el diálogo de pasos `EnviarSolicitudUsuarioFietComponent` (`fe/core/usuario fiet/components/...`). Roles de la ruta: los cinco roles FIET (documento 08, sección 9).

| # | Pantalla / componente | Servicio Angular | Endpoint | Controlador y caso de uso | Gateway | Tablas |
|---|---|---|---|---|---|---|
| 1 | Al abrir el diálogo, selección del tipo (Paso 1) | `TipoSolicitudService.getTiposSolicitudPorPerfil` | `GET /tipos/solicitudes/perfil?perfil=` (`permitAll`) | `TiposSolicitudRestController.getTiposPorPerfil`, `TipoSolicitudCUImplAdaptador.getTiposSolicitudesPorPerfil` | `TipoSolicitudGatewayIntPuerto` | L: `tiposSolicitudes`, `tiposAnexos` |
| 2 | Selección de orden del día | `SolicitudesService.getOrdenesDelDiaPorEstado(true)` | `GET /solicitudes/orden-del-dia/estado?estado=true` | `SolicitudesRestController.getOrdenesDelDiaPorEstado`, `OrdenDelDiaCUImplAdaptador.getOrdenesDelDiaPorEstado` | `OrdenDelDiaGatewayIntPuerto` | L: `ordenesDelDia` |
| 3 | Paso 2 (Tipos de Anexos), `enviarSolicitud()`: valida obligatoriedad en cliente y ordena archivos | `SolicitudesService.enviarSolicitud` (`FormData`: parte `solicitud` como `Blob` JSON, partes `archivos`) | `POST /solicitudes` (multipart) | `SolicitudesRestController.enviarSolicitud` (`@Transactional`); primero `ValidadorAnexosService.validarYAsignarArchivos` (E6 directo) | | |
| 4 | | | | `SolicitudCUImplAdaptador.crearSolicitud` -> `checkSolicitud`: tipo existe (E3); cada anexo obligatorio presente y con archivo (E6); extensión igual al formato del tipo (E6); orden del día existe si viene (E3) y estado `AGREGADO EN EL ORDEN DEL DÍA`, si no `SIN RESPONDER`; funcionario encargado tomado del tipo; solicitante copiado del usuario del token | `TipoSolicitudGatewayIntPuerto.getTipoSolicitud`, `OrdenDelDiaGatewayIntPuerto.getOrdenDelDia`, `SesionGatewayIntPuerto.getUsuario`, `IJwtServicio.getUsername` | L: `tiposSolicitudes`, `tiposAnexos`, `ordenesDelDia`, `funcionarios`, `usuarios` |
| 5 | | | | Genera uuid de la solicitud, de la información del solicitante y de cada anexo; guarda cada archivo con `AlmacenadorArchivos.guardarArchivo` en `${app.uploads.base-path}/anexos/<uuidSolicitud>/`; un `IOException` se envuelve en `RuntimeException` plano | | Disco |
| 6 | | | | `LogCUIntPuerto.crearLog("Solicitud Creada", ...)` ANTES de guardar (diferencia base/extensión, documento 04, sección 2, punto 7) | `LogGatewayIntPuerto` | E: `logs` |
| 7 | | | | `SolicitudGatewayIntPuerto.guardarSolicitud` | `SolicitudGatewayImplAdaptador` -> `SolicitudRepositorio.save`; `SolicitudEntidad` tiene `cascade = ALL` hacia `anexos` e `informacionSolicitantes` | E: `solicitudes`, `anexos`, `informacionSolicitantes` |
| 8 | `next`: toast "Solicitud enviada correctamente" y cierre del diálogo | | | | | |

Variante pública: el Funcionario (rol de Julián) puede enviar en nombre de un solicitante público con `SolicitudesService.enviarSolicitudPublica` -> `POST /solicitudes/public` -> `SolicitudesRestController.enviarSolicitudPublica` -> `SolicitudCUImplAdaptador.crearSolicitudPublica` (estado `SIN RESPONDER`, no valida anexos obligatorios). La regla de seguridad `POST solicitudes/public` es `permitAll`, aunque la anotación `@PreAuthorize` dice Secretario General, Funcionario y Decano (documento 03, fila 38).

### 1.3 Revisión del Funcionario (flujo del Consejo)

El Funcionario de Julián (rol `Funcionario`) ve las solicitudes de los tipos que tiene a cargo, las abre y cambia su estado. Pantalla `funcionario/solicitudes` (`FunSolicitudesContentComponent`).

| # | Pantalla / componente | Servicio Angular | Endpoint | Controlador y caso de uso | Gateway | Tablas |
|---|---|---|---|---|---|---|
| 1 | `loadSolicitudes` (con o sin búsqueda) | `SolicitudesService.getSolicitudesPorFuncionario` o `buscarSolicitudesPorNombreYFuncionario` | `GET /solicitudes/funcionario/{uuidFuncionario}` o `GET /solicitudes/fun/buscar/` | `SolicitudesRestController.getSolicitudesPorFuncionario` o `buscarSolicitudesPorNombreYFuncionario`; `SolicitudCUImplAdaptador.getSolicitudesPorFuncionario` o `buscarSolicitudesPorNombreYFuncionario` (E6 paginación, E7 sin resultados) | `SolicitudGatewayIntPuerto` | L: `solicitudes` y relacionadas |
| 2 | Ícono "ver" | `SolicitudesService.getSolicitud` | `GET /solicitudes/{uuid}` | `SolicitudCUImplAdaptador.getSolicitud` (E3) | `SolicitudGatewayIntPuerto.getSolicitud` | L: `solicitudes`, `anexos`, `informacionSolicitantes` |
| 3 | Ícono "editar", diálogo "Actualizar Solicitud": nombre, consecutivo, descripción, estado (`ESTADOS_SOLICITUD`) | `SolicitudesService.actualizarSolicitud` | `PUT /solicitudes/{uuidSolicitud}` | `SolicitudesRestController.actualizarOrdenDelDia` (nombre heredado, `@Transactional`), `SolicitudCUImplAdaptador.actualizarSolicitud`: si llega `uuidFuncionario` distinto, debe existir (E3) y ser `Funcionario` (E6); si `uuidOrdenDelDia` llega vacío la solicitud queda sin orden; `Solicitud.actualizar` copia consecutivo, nombre, descripción y estado sin validar el valor del estado | `SolicitudGatewayIntPuerto`, `UsuarioGatewayIntPuerto`, `OrdenDelDiaGatewayIntPuerto` | E: `solicitudes`, `logs` ("Solicitud modificada") |

Observaciones verificadas: (a) la anotación `@PreAuthorize` de `actualizarOrdenDelDia` dice Secretario General y Decano, pero la regla efectiva `solicitudes/**` es `authenticated()` (`ConfiguracionSeguridad` línea 68), por eso el Funcionario puede llamarla; (b) el Funcionario Académico de la extensión no usa este flujo: su revisión es la de los procesos académicos (sección 2.2); (c) el cambio de estado no tiene máquina en el servidor, ver sección 6.5.

### 1.4 Orden del día: crear, poblar y retirar solicitudes

Pantalla `sec-general/orden-del-dia` (`OrdenDelDiaContentComponent`) y su visualizador `OrdenDelDiaVisualizadorComponent` (`fe/core/secretario-general/components/orden-del-dia-visualizador-component`). Reglas de seguridad: `solicitudes/orden-del-dia/**` solo Secretario General y Decano (línea 57), salvo `GET orden-del-dia/estado` (autenticado, línea 56).

| # | Pantalla / componente | Servicio Angular | Endpoint | Controlador y caso de uso | Gateway | Tablas |
|---|---|---|---|---|---|---|
| 1 | Botón de tarjeta "Crear" (diálogo: nombre, número de acta, descripción, ciudad, fecha, hora inicio, hora fin, lugar) | `SolicitudesService.crearOrdenDelDia` | `POST /solicitudes/orden-del-dia` | `SolicitudesRestController.crearOrdenDelDia` (`@Transactional`), `OrdenDelDiaCUImplAdaptador.crearOrdenDelDia`: uuid nuevo, `estado = true`; log antes de guardar | `OrdenDelDiaGatewayIntPuerto.guardarOrdenDelDia` | E: `ordenesDelDia`, `logs` |
| 2 | Ícono "editar" | `SolicitudesService.actualizarOrdenDelDia` | `PUT /solicitudes/orden-del-dia/{uuid}` | `OrdenDelDiaCUImplAdaptador.actualizarOrdenDelDia` (E3 si no existe) | `OrdenDelDiaGatewayIntPuerto` | E: `ordenesDelDia`, `logs` |
| 3 | Ícono "abrir" (carpeta) abre el visualizador; `cargarSolicitudes` | `SolicitudesService.getSolicitudesPorOrdenDelDia` | `GET /solicitudes/orden-del-dia/{uuid}/solicitudes` | `SolicitudCUImplAdaptador.getSolicitudesPorOrdenDelDia` (E7 si no hay) | `SolicitudGatewayIntPuerto` | L: `solicitudes`, `tiposSolicitudes` |
| 4 | `cargarSolicitudesDisponibles` | `SolicitudesService.getSolicitudesPorEstado('PENDIENTE AL ORDEN DEL DÍA')` | `GET /solicitudes/estado?estado=` | `SolicitudCUImplAdaptador.getSolicitudesPorEstado` (reemplaza `_` por espacio; E7 si no hay) | `SolicitudGatewayIntPuerto` | L: `solicitudes` |
| 5 | `agregarSolicitudSeleccionada` y `registrarSolicitudesEnOrdenDelDia`: agrupa por sección del tipo y envía estado `AGREGADO EN EL ORDEN DEL DÍA` | `SolicitudesService.getSolicitud`, luego `actualizarSolicitud` | `GET /solicitudes/{uuid}` y `PUT /solicitudes/{uuid}` | `SolicitudCUImplAdaptador.actualizarSolicitud` con `uuidOrdenDelDia` (valida que la orden exista, E3) | `SolicitudGatewayIntPuerto`, `OrdenDelDiaGatewayIntPuerto` | E: `solicitudes`, `logs` |
| 6 | `removerSolicitud`: envía estado `PENDIENTE AL ORDEN DEL DÍA` y `uuidOrdenDelDia` vacío | `SolicitudesService.actualizarSolicitud` | `PUT /solicitudes/{uuid}` | igual; con `uuidOrdenDelDia` vacío `actualizarSolicitud` pone `setObjOrdenDelDia(null)` | igual | E: `solicitudes`, `logs` |
| 7 | `descargarAnexos` | `SolicitudesService.descargarAnexosOrdenDelDia` | `GET /solicitudes/anexos/download?uuidOrden&nombreOrden` | `SolicitudesRestController.descargarAnexos`, `SolicitudCUImplAdaptador.generarZipAnexosPorOrdenDelDia`: arma un ZIP con carpeta por sección y solicitud leyendo `${app.uploads.base-path}/anexos/<uuid>/` (E6 si falta carpeta o archivo, E7 si la orden no tiene solicitudes) | `SolicitudGatewayIntPuerto` | L: `solicitudes`, `anexos`; lee disco |

### 1.5 Registro de respuestas

Dos pasos separados, ambos de Julián: registrar el texto de la respuesta del Consejo y cargar el archivo de respuesta (que cambia el estado de la solicitud).

| # | Pantalla / componente | Servicio Angular | Endpoint | Controlador y caso de uso | Gateway | Tablas |
|---|---|---|---|---|---|---|
| 1 | Visualizador de la orden, "Asignar Respuesta" (`abrirFormularioRespuesta`): precarga la respuesta existente | `RespuestasService.getRespuestaPorSolicitud` | `GET /respuestas/solicitud/{uuidSolicitud}` | `RespuestaRestController.getRespuestaPorSolicitud`, `RespuestaCUImplAdaptador.getRespuestaPorSolicitud` (E3 si no hay; el componente ignora el error) | `RespuestaGatewayIntPuerto` | L: `respuestas` |
| 2 | `modificarSolicitud`: tipo (`Oficio` o `Resolución`), consecutivo FIET, respuesta del Consejo, indicaciones | `RespuestasService.registrarRespuesta` | `POST /respuestas/{uuidSolicitud}` (reglas: Secretario General, Decano, Funcionario) | `RespuestaRestController.registrarRespuesta` (`@Transactional`), `RespuestaCUImplAdaptador.registrarRespuesta`: solicitud existe (E3); tipo válido (E4); si no hay respuesta la crea, si hay la modifica; log antes de guardar | `SolicitudGatewayIntPuerto.getSolicitud`, `RespuestaGatewayIntPuerto.solicitudTieneRespuesta` y `guardarRespuesta` | E: `respuestas`, `logs` ("Registrar respuesta de Solicitud" o "Modificar respuesta de Solicitud") |
| 3 | Pantalla `sec-general/respuestas` (`SecRespuestasContentComponent`), editar abre `DialogGestionRespuestaComponent`; `cargarRespuesta` | `RespuestasService.getRespuesta` | `GET /respuestas/{uuid}` | `RespuestaCUImplAdaptador.getRespuesta` (E3) | `RespuestaGatewayIntPuerto` | L: `respuestas` |
| 4 | `onArchivoSeleccionado` (`CargarRespuestaComponent`) | `RespuestasService.responderSolicitud` (`FormData` con `archivo`) | `POST /respuestas/{uuidRespuesta}/archivo` | `RespuestaCUImplAdaptador.responderSolicitud`: solo PDF o DOCX por extensión (E4); borra el archivo anterior, guarda con `AlmacenadorArchivos.guardarArchivoRespuesta` en `${app.uploads.base-path}/respuestas/<uuidRespuesta>/`, fija `urlRespuesta`; IO envuelto en `RuntimeException` plano | `RespuestaGatewayIntPuerto.guardarRespuesta` | E: `respuestas`, `logs` ("Responder Solicitud"); disco |
| 5 | Mismo componente, `actualizarEstadoSolicitud(uuidRespuesta, 'RESPONDIDA')`: lee la respuesta, lee la solicitud y la reenvía con el estado nuevo | `RespuestasService.getRespuesta`, `SolicitudesService.getSolicitud`, `SolicitudesService.actualizarSolicitud` | `GET /respuestas/{uuid}`, `GET /solicitudes/{uuid}`, `PUT /solicitudes/{uuid}` | `SolicitudCUImplAdaptador.actualizarSolicitud` | `SolicitudGatewayIntPuerto` | E: `solicitudes`, `logs` |
| 6 | Quitar archivo: `onArchivoEliminado` | `RespuestasService.eliminarArchivoRespuesta`, luego estado `AGREGADO EN EL ORDEN DEL DÍA` | `PATCH /respuestas/{uuid}/archivo` y `PUT /solicitudes/{uuid}` | `RespuestaCUImplAdaptador.eliminarArchivoRespuesta` (E4 si no hay archivo) | `RespuestaGatewayIntPuerto` | E: `respuestas`, `solicitudes`, `logs` |

Observación: el cambio a `RESPONDIDA` o de vuelta a `AGREGADO EN EL ORDEN DEL DÍA` lo hace el componente Angular encadenando dos llamadas (`switchMap`); el backend no lo hace dentro de `responderSolicitud`. Si la segunda llamada falla, el archivo queda guardado y la solicitud conserva el estado anterior (cada endpoint tiene su propia transacción). NO VERIFICADO en ejecución.

### 1.6 Carga masiva por Excel

Patrón común de Julián, replicado en la extensión (`backend/CLAUDE.md`, sección "Carga masiva por Excel"): `ProcesadorArchivos` lee, `ValidadorPeticionesExcel` valida, el caso de uso escribe, todo dentro de la transacción del controlador. Solo se admite `.xlsx` (Apache POI `XSSFWorkbook`).

#### 1.6.1 Usuarios [BASE]

| # | Pantalla / componente | Servicio Angular | Endpoint | Controlador y caso de uso | Gateway | Tablas |
|---|---|---|---|---|---|---|
| 1 | `sec-general/usuarios`, botón de tarjeta "Cargar archivo" abre `GenericDialogUploadFileComponent` y llama `crearUsuariosDesdeArchivo` | `UsuariosService.crearUsuariosDesdeArchivo` (`FormData`, campo `file`) | `POST /usuarios/cargar/archivo` (Secretario General o Decano) | `UsuarioRestController.crearUsuarios` (`@Transactional`) | | |
| 2 | | | | `ProcesarArchivoUsuariosImpl.procesarArchivo` (bean `archivos-usuarios`): hoja 0, salta la fila 0, corta en la primera fila vacía, resuelve tipo de usuario y rol contra un caché; ante un archivo ilegible captura la excepción y devuelve lo leído hasta ahí | `UsuarioGatewayIntPuerto` (caché de tipos y roles) | L: `tiposUsuario`, `roles` |
| 3 | | | | `ValidadorPeticionesExcel` (bean `validador-usuarios`) sobre las anotaciones del DTO; la primera fila inválida corta con 400 y mapa campo-mensaje, sin número de fila | | |
| 4 | | | | `UsuarioCUImplAdaptador.crearUsuarios`: lista no vacía (E7); por usuario tipo de documento, documento, correo y username únicos (E6, E2), tipo de usuario existente (E3), roles sin duplicar y válidos (E4); cifra la contraseña; `crearInstancia` según el primer rol (solo `FUNCIONARIO` genera subclase); un log por usuario después de guardar | `UsuarioGatewayIntPuerto`, `PasswordEncoderGatewayIntPuerto`, `LogCUIntPuerto` | E: `usuarios`, `usuariosLivianos`, `funcionarios` (solo rol Funcionario), `Usuario_has_Roles`, `logs` |
| 5 | Resultado | | | `DataAccessException` se captura en el controlador y responde 500 con `mensaje` y `error`; `@Transactional` revierte el lote | | |

#### 1.6.2 Estudiantes [EXTENSIÓN] (misma plantilla, otras diferencias)

| # | Pantalla / componente | Servicio Angular | Endpoint | Controlador y caso de uso | Gateway | Tablas |
|---|---|---|---|---|---|---|
| 1 | `decano/administracion-academica` o `sec-general/administracion-academica`, pestaña Estudiantes, botón "Cargar Excel": `CargaExcelAcademicaComponent` recibe la función `cargarExcel` | `EstudianteService.crearEstudiantesDesdeArchivo` | `POST /estudiantes/cargar/archivo` | `EstudianteRestController.crearEstudiantes` (`@Transactional`) | | |
| 2 | | | | `ProcesarArchivoEstudiantesImpl.procesarArchivo` (bean `archivos-estudiantes`): `abrir` falla con E6 `EXCEL_NO_VALIDO`, `validarEncabezado` compara las 13 columnas (E6 `EXCEL_ENCABEZADO_INVALIDO`), una fila por asignatura matriculada | | |
| 3 | | | | `CatalogoAcademicoExcel.resolverProgramasEstudiantes` (nombre de programa a uuid; E6 con fila y columna), validación por fila con fila y columna en cada mensaje, `CatalogoAcademicoExcel.verificarAsignaturasEstudiantes` (código existente y del programa de la fila) | `ProgramaAcademicoCUIntPuerto`, `AsignaturaCUIntPuerto` | L: `PROGRAMA_ACADEMICO`, `ASIGNATURA` |
| 4 | | | | `AgrupadorEstudiantesExcel.agrupar` junta las filas del mismo estudiante; `EstudianteCUImplAdaptador.crearEstudiantes`: lista no vacía (E7), repetidos dentro del lote (código estudiantil, documento, correo, username), luego `registrar` uno a uno; cada uno llama a `UsuarioCUIntPuerto.crearUsuario` con el tipo `ESTUDIANTE` | `EstudianteGatewayIntPuerto.guardarTodos`, `UsuarioCUIntPuerto`, `LogCUIntPuerto` | E: `usuarios`, `usuariosLivianos`, `Usuario_has_Roles`, `ESTUDIANTE`, `ASIGNATURA_MATRICULADA`, `logs` |
| 5 | Resultado | | | La función de `CargaExcelAcademicaComponent` muestra el resultado y el botón pasa a "Cerrar" (D-F22); los errores se listan dentro del diálogo | | |

Los Excel de asignaturas (`POST /asignaturas/cargar/archivo`, `AsignaturaCUImplAdaptador.crearAsignaturas`, que bloquea cada programa con `ProgramaAcademicoGatewayIntPuerto.bloquear` y genera `SIGLA-nnn`) y de funcionarios académicos (`POST /funcionarios-academicos/cargar/archivo`) siguen el mismo esquema [EXTENSIÓN].

### 1.7 Exportación a `.docx` (orden del día)

Tres variantes en el visualizador de la orden, más el ZIP de anexos de la sección 1.4. El exportador es `OrdenDelDiaExportadorImpl` (`bk/infraestructura/output/exportador/`), puerto `OrdenDelDiaExportador`, con Apache POI (`XWPFDocument`) sobre plantillas del classpath.

| # | Pantalla / componente | Servicio Angular | Endpoint | Controlador y caso de uso | Gateway | Tablas |
|---|---|---|---|---|---|---|
| 1 | `exportarOrdenDelDia`: orden simple | `SolicitudesService.exportarOrdenDelDia` (`responseType: 'blob'`) | `GET /solicitudes/exportar?uuidOrden=` | `SolicitudesRestController.exportarOrden`, `OrdenDelDiaCUImplAdaptador.generarOrdenDelDia`: `getOrdenDelDia` (E3), `getSolicitudesPorOrdenDelDia`, `procesarSolicitudes(solicitudes, false)` | `OrdenDelDiaGatewayIntPuerto`, `SolicitudGatewayIntPuerto` | L: `ordenesDelDia`, `solicitudes`, `tiposSolicitudes` |
| 2 | `exportarOrdenDelDiaConRespuestas` | `SolicitudesService.exportarOrdenDelDiaConRespuestas` | `GET /solicitudes/exportar/respuestas` | `OrdenDelDiaCUImplAdaptador.generarOrdenDelDiaConRespuestas`: igual, con `RespuestaGatewayIntPuerto.getRespuestaPorSolicitud` por solicitud y plantilla `templates/orden_del_dia_info.docx` | `OrdenDelDiaGatewayIntPuerto`, `SolicitudGatewayIntPuerto`, `RespuestaGatewayIntPuerto` | L: `ordenesDelDia`, `solicitudes`, `respuestas` |
| 3 | `exportarOrdenDelDiaMerge`: desarrollo de la reunión | `SolicitudesService.exportarOrdenDelDiaMerge` | `GET /solicitudes/exportar/reunion` | `OrdenDelDiaCUImplAdaptador.generarOrdenDelDiaMerge`: `procesarSolicitudesMerge` (nombre, descripción y, en el bloque aparte, respuesta del Consejo); plantilla `templates/orden_del_dia_temas_resueltos.docx` | igual | igual |
| 4 | (los tres) | | | `inicializarDatosOrden` carga ciudad, día, mes, año, horas, lugar y acta; `procesarSolicitudes*` numera por sección (1 asuntos decano, 2 pregrado, 3 posgrados, 4 delegados en decano, 5 y 6 comisiones, 7 informe, 8 varios) y arma los marcadores `{{...}}` | | |
| 5 | (los tres) | | | `OrdenDelDiaExportadorImpl.exportarOrdenDelDia(datos, plantilla)`: reemplaza marcadores en párrafos y tablas y devuelve `byte[]`; `IOException` se envuelve en `RuntimeException` | `ClassPathResource` | Archivo en `backend/solicitudes/src/main/resources/templates/` |
| 6 | Cliente | | | La respuesta es un `Blob`; el componente crea un enlace temporal y descarga `<nombre de la orden normalizado>.docx` (el backend envía `Orden_Del_Dia.docx` en `Content-Disposition`, el nombre final lo fija el cliente) | | |

Observaciones verificadas en el código: (a) las rutas `solicitudes/exportar*` y `solicitudes/anexos/download` caen en la regla `solicitudes/**` autenticado, de modo que cualquier usuario autenticado puede llamarlas aunque la anotación diga Secretario General y Decano (documento 03, filas 48 a 51); (b) `OrdenDelDiaExportadorImpl.reemplazarTexto` hace `return` tras el primer marcador sustituido en un párrafo, lo que supone un marcador por párrafo en la plantilla; la plantilla no se abrió, NO VERIFICADO cuántos marcadores hay por párrafo; (c) las secciones fuera de las ocho constantes se saltan (`default: continue`), esas solicitudes no aparecen en el documento.

## 2. Piezas comunes de los procesos académicos [EXTENSIÓN]

Los tres procesos comparten el motor de etapas (`MaquinaEtapas`), la bandeja, el detalle, el almacenamiento de anexos y de la Resolución. Se describen una vez aquí y los flujos de las secciones 3 a 5 solo indican qué cambia.

### 2.1 Bandeja y detalle

| # | Pantalla / componente | Servicio Angular | Endpoint | Controlador y caso de uso | Gateway | Tablas |
|---|---|---|---|---|---|---|
| 1 | `estudiante/solicitudes`, `funcionario-academico/solicitudes|respuestas`, `decano/solicitudes`: `BandejaSolicitudesAcademicasComponent` (`rol`, `modo`) | `SolicitudAcademicaService.getSolicitudesEstudiante` o `getSolicitudesFuncionario` o `getSolicitudesDecano` | `GET /solicitudes-academicas/estudiante` o `/funcionario` o `/decano` (una regla por rol) | `SolicitudAcademicaRestController.getBandejaEstudiante|Funcionario|Decano` (`@Transactional(readOnly = true)`), `ConsultaSolicitudAcademicaCUImplAdaptador.getBandeja*`: usuario del token con el rol exigido (E1, E3, E4); filtra a las etapas que tienen etiqueta para ese rol; en Funcionario y Decano agrega `decisionDecano` y, para Cancelación de Asignatura final, `resultadosAsignaturas` | `SesionGatewayIntPuerto`, `SolicitudAcademicaGatewayIntPuerto` (`getPorEstudiante`, `getPorFuncionarioAcademico`, `getTodas`, `getUltimaAccion`, `getConAsignaturaNoAprobadaPorDecano`, `getResultadosAsignaturas`), `EtapaEtiquetaRolGatewayIntPuerto` | L: `usuarios`, `SOLICITUD_ACADEMICA`, `TIPO_SOLICITUD_ACADEMICA`, `ETAPA_SOLICITUD_ACADEMICA`, `ETAPA_ETIQUETA_ROL`, `HISTORIAL_SOLICITUD_ACADEMICA`, `ASIGNATURA_SOLICITUD_ACADEMICA` |
| 2 | Clic en "Ver solicitud": `DetalleSolicitudAcademicaComponent.cargar` (`forkJoin`) | `SolicitudAcademicaService.getSolicitud` y `getHistorial` | `GET /solicitudes-academicas/{uuid}` y `GET /solicitudes-academicas/{uuid}/historial` | `SolicitudAcademicaRestController.getDetalle|getHistorial`; `ConsultaSolicitudAcademicaCUImplAdaptador.getDetalle` decide el rol por `acceso()` (dueño con rol Estudiante, o Funcionario asignado al tipo, o Decano), exige etiqueta de la etapa para ese rol y si no responde como inexistente (E3), enmascara `etapaCodigo` para el Estudiante (`etapaVisible`) y trae `accionesDisponibles` de `MaquinaEtapas.accionesDisponibles`; `getHistorial` reconstruye la etapa de cada fila con `MaquinaEtapas.etapaDestino` y oculta al Estudiante las filas `APROBAR_DECANO` y `RECHAZAR_DECANO` | `SolicitudAcademicaGatewayIntPuerto`, `AnexoAcademicoGatewayIntPuerto`, `ResolucionAcademicaGatewayIntPuerto`, `HistorialSolicitudAcademicaGatewayIntPuerto` | L: `SOLICITUD_ACADEMICA`, `ANEXO_ACADEMICO`, `RESOLUCION_ACADEMICA`, `HISTORIAL_SOLICITUD_ACADEMICA`, `ETAPA_ETIQUETA_ROL`, `usuarios` |
| 3 | `COMPONENTES_PROCESO` monta el componente del proceso (`ngComponentOutlet`), que pide su detalle al cambiar `uuidSolicitud` | `CancelacionMatriculaService.getDetalle` o `CancelacionAsignaturaService.getDetalle` o `ExamenSupletorioService.getDetalle` | `GET /cancelaciones-matricula/{uuid}` o `/cancelaciones-asignatura/{uuid}` o `/examenes-supletorios/{uuid}` | `Consulta<Proceso>CUImplAdaptador.getDetalle` sobre `ConsultaSolicitudAcademicaCUIntPuerto.getDetalle` y `resolverActor`; E3 si no hay fila de especialización | `SolicitudCancelacionMatriculaGatewayIntPuerto` o `SolicitudCancelacionAsignaturaGatewayIntPuerto` o `SolicitudExamenSupletorioGatewayIntPuerto` | L: `SOLICITUD_CANCELACION_MATRICULA` o `SOLICITUD_CANCELACION_ASIGNATURA` o `SOLICITUD_EXAMEN_SUPLETORIO` (más `SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA`), `ASIGNATURA_SOLICITUD_ACADEMICA` |
| 4 | Botones de acción (`accionesVisibles`): salen del arreglo `accionesDisponibles` que entrega el backend, filtrado por `ACCIONES_DEL_PROCESO` | | | `MaquinaEtapas.accionesDisponibles(proceso, etapa, rol)` filtra la tabla `TRANSICIONES` por proceso, etapa de origen y rol | | |
| 5 | Descargar anexo (Visualizar o Descargar) | `SolicitudAcademicaService.descargarAnexo` (`blob`) | `GET /solicitudes-academicas/{uuid}/anexos/{uuidAnexo}` | `ConsultaSolicitudAcademicaCUImplAdaptador.descargarAnexo` (acceso válido y el anexo debe ser de esa solicitud), luego `AnexoAcademicoCUImplAdaptador.obtenerAnexo` | `AnexoAcademicoGatewayIntPuerto`, `AlmacenamientoAnexosIntPuerto` | L: `ANEXO_ACADEMICO`; lee disco |
| 6 | Descargar Resolución (visible si `puedeDescargarResolucion`) | `SolicitudAcademicaService.descargarResolucion` (`blob`) | `GET /solicitudes-academicas/{uuid}/resolucion` | `ResolucionAcademicaCUImplAdaptador.obtenerResolucion`: el Estudiante solo si la solicitud está en etapa final; Funcionario y Decano en cualquier etapa (E4 `RESOLUCION_NO_DISPONIBLE`) | `ResolucionAcademicaGatewayIntPuerto`, `AlmacenamientoAnexosIntPuerto` | L: `RESOLUCION_ACADEMICA`; lee disco |

### 2.2 Secuencia común de una acción de trámite

Es lo que ocurre en cualquier acción de las secciones 3.2 a 5.2 (rechazar, remitir, aprobar, enviar respuesta, recibo, comprobante). Se verificó en `SolicitudAcademicaCUImplAdaptador.cambiarEtapa` y `MaquinaEtapas`.

| # | Capa | Qué hace |
|---|---|---|
| 1 | `*RestController` (`@Transactional`) | Recibe el DTO; el rol nunca llega del cliente |
| 2 | `Tramite<Proceso>CUImplAdaptador.<acción>` | `TramiteSolicitud.actorDe(token, rol)` (usuario del token con el rol fijo de la acción); busca la fila de especialización (E3 si no existe); validaciones propias del proceso antes de escribir |
| 3 | `SolicitudAcademicaCUImplAdaptador.cambiarEtapa` | Solicitud existe (E3); acción no nula; `ValidadorActorSolicitud.validar` (rol real, Estudiante dueño, Funcionario asignado al tipo); observación recortada, máximo 500 (E6) |
| 4 | `MaquinaEtapas.transicionar` | Busca la fila de `TRANSICIONES` por proceso, etapa de origen y acción (E4 `TRANSICION_NO_PERMITIDA`); comprueba el rol (E4 `ROL_TRANSICION_NO_PERMITIDO`); exige observación, escaneo, recibo o comprobante según la fila, leídos de la base (`evidencias`: observación recibida, `ResolucionAcademicaGateway.existePorSolicitud`, anexos "Recibo de pago" y "Comprobante de pago") (E4 `REQUISITO_TRANSICION_FALTANTE`); una etapa final lanza E4 `ETAPA_FINAL_SIN_ACCIONES` |
| 5 | `SolicitudAcademicaGatewayImplAdaptador.actualizarEtapa` (`@Transactional`) | Cambia `SOLICITUD_ACADEMICA.Etapa_uuid` y escribe una fila en `HISTORIAL_SOLICITUD_ACADEMICA` (acción, usuario, observación, fecha); no guarda la etapa en el historial |
| 6 | `cambiarEtapa` solo en `REMITIR_DECANO` | Si había escaneo de la Resolución, `ResolucionAcademicaGateway.eliminarPorSolicitud` y `AlmacenamientoAnexosIntPuerto.eliminarTrasConfirmar` (el archivo se borra solo después del commit) |
| 7 | `LogCUIntPuerto.crearLog("Cambiar etapa de solicitud académica", ...)` | Una fila en `logs` con radicado, acción y etapas |
| 8 | Controlador | Responde el detalle del proceso ya actualizado (`Consulta<Proceso>CU.getDetalle`) |
| 9 | Cliente | `ProcesoComponent.ejecutar(accion)`, luego `recargar()` (vuelve a pedir solicitud e historial) y toast de éxito; la bandera `enviando` evita el doble clic (D-F7) |

Cuando la acción exige un archivo (Resolución en cancelaciones, recibo o comprobante en el supletorio), el cliente lo sube antes con una llamada aparte y encadena la acción con `switchMap`:

| Archivo | Servicio Angular | Endpoint | Caso de uso | Regla | Tablas |
|---|---|---|---|---|---|
| Escaneo de la Resolución | `SolicitudAcademicaService.adjuntarResolucion` (`FormData`, parte `archivo`) | `POST /solicitudes-academicas/{uuid}/resolucion` (Funcionario Académico) | `ResolucionAcademicaCUImplAdaptador.adjuntarResolucion`: no es Supletorio (E4), el actor es Funcionario, la etapa es `RADICADA`, `APROBADA_POR_DECANO` o `RECHAZADA_POR_DECANO` (E4), solo `pdf` con firma coherente (E6); reemplaza la fila si existía | E: `RESOLUCION_ACADEMICA`, `logs`; disco |
| Recibo o comprobante de pago | `SolicitudAcademicaService.adjuntarAnexo` (parte `archivo` y `tipoAnexo`) | `POST /solicitudes-academicas/{uuid}/anexos` (Estudiante o Funcionario Académico) | `AnexoAcademicoCUImplAdaptador.adjuntarAnexo`: "Recibo de pago" solo Funcionario en `APROBADA_POR_DECANO`; "Comprobante de pago" solo Estudiante en `PENDIENTE_PAGO`; formato y firma por `ValidadorArchivoAdjunto`; guarda el archivo y luego la ficha, y si la ficha falla borra el archivo | E: `ANEXO_ACADEMICO`, `logs`; disco |

Atomicidad (documento 04, sección 2): subir el archivo y ejecutar la acción son dos transacciones distintas (dos peticiones HTTP). Si el archivo se sube y la acción falla, el archivo queda guardado; el cliente lo recuerda (`resolucionSubida`, `anexoPagoSubido`) para no volver a subirlo en el reintento. Dentro de cada petición, el almacenamiento borra el archivo recién escrito si la transacción se revierte (`AlmacenamientoAnexosImplAdaptador`, `TransactionSynchronizationManager`).

## 3. Cancelación de Matrícula (CM) [EXTENSIÓN]

Tipo de proceso `CANCELACION_MATRICULA` (sigla `CM`, radicado `AAAA-CM-0001`). Seis anexos obligatorios en el seed. Termina con Resolución física firmada y escaneada.

### 3.1 Radicación por el Estudiante

| # | Pantalla / componente | Servicio Angular | Endpoint | Controlador y caso de uso | Gateway | Tablas |
|---|---|---|---|---|---|---|
| 1 | `estudiante/catalogo`, tarjeta "Cancelación de Matrícula", "Iniciar solicitud" navega a `estudiante/cancelacion-matricula` | ninguno (contenido estático `TRAMITES_ESTUDIANTE`) | | | | |
| 2 | `EstCancelacionMatriculaContentComponent.cargarFormulario` | `CancelacionMatriculaService.getFormulario` | `GET /cancelaciones-matricula/formulario` | `CancelacionMatriculaRestController.getFormulario`, `ConsultaCancelacionMatriculaCUImplAdaptador.getFormulario`: estudiante del token, tipo del proceso (E3), matrículas `activa`, anexos del tipo | `SesionGatewayIntPuerto`, `EstudianteGatewayIntPuerto`, `TipoSolicitudAcademicaGatewayIntPuerto`, `TipoAnexoAcademicoGatewayIntPuerto` | L: `usuarios`, `ESTUDIANTE`, `ASIGNATURA_MATRICULADA`, `TIPO_SOLICITUD_ACADEMICA`, `TIPO_ANEXO_ACADEMICO` |
| 3 | `radicar()`: valida en cliente, arma `FormData` con `motivo`, una parte por anexo con el uuid del tipo como nombre y partes `soporte` | `CancelacionMatriculaService.radicar` (`armarFormDataRadicacion`) | `POST /cancelaciones-matricula` (multipart) | `CancelacionMatriculaRestController.radicar` (`@Transactional`): `getEstudianteAutenticado` y `CancelacionMatriculaCUImplAdaptador.radicarCancelacionMatricula` | | |
| 4 | | | | Validación en orden: estudiante con rol y fila (E3, E4); al menos una matrícula activa (E4); motivo obligatorio y máximo 255 (E4, E6); tipo existente (E3); anexos del tipo, todos los obligatorios presentes (E4 `ANEXOS_OBLIGATORIOS_FALTANTES`), cada archivo con `ValidadorArchivoAdjunto` (E6) | `EstudianteGatewayIntPuerto`, `UsuarioGatewayIntPuerto`, `TipoSolicitudAcademicaGatewayIntPuerto`, `TipoAnexoAcademicoGatewayIntPuerto` | L: `usuarios`, `ESTUDIANTE`, `ASIGNATURA_MATRICULADA`, `TIPO_ANEXO_ACADEMICO` |
| 5 | | | | `SolicitudAcademicaCUImplAdaptador.crearSolicitud`: sin otra solicitud del mismo tipo fuera de `APROBADA` y `RECHAZADA` (E4 `SOLICITUD_EN_CURSO`); `MaquinaEtapas.siguienteEtapa(CM, null, RADICAR, ESTUDIANTE)` da `RADICADA`; radicado = último del año y tipo más 1 | `SolicitudAcademicaGatewayIntPuerto.crear` (`@Transactional`, `saveAndFlush`; colisión de `uk_solacad_radicado` se convierte en E4 `RADICADO_NO_GENERADO`), `EtapaSolicitudAcademicaGatewayIntPuerto` | E: `SOLICITUD_ACADEMICA`, `HISTORIAL_SOLICITUD_ACADEMICA` (acción `RADICAR`), `logs` ("Radicar solicitud académica") |
| 6 | | | | `SolicitudCancelacionMatriculaGatewayIntPuerto.guardar` y `guardarAsignaturas`: una `ASIGNATURA_SOLICITUD_ACADEMICA` por cada matrícula activa, evaluación vacía | `SolicitudCancelacionMatriculaGatewayImplAdaptador` | E: `SOLICITUD_CANCELACION_MATRICULA`, `ASIGNATURA_SOLICITUD_ACADEMICA` |
| 7 | | | | `AnexoAcademicoCUIntPuerto.adjuntarAnexo` por cada anexo (rol Estudiante, etapa `RADICADA`); log "Radicar cancelación de matrícula" | `AnexoAcademicoGatewayIntPuerto`, `AlmacenamientoAnexosIntPuerto` | E: `ANEXO_ACADEMICO`, `logs`; disco |
| 8 | `next`: toast con el radicado y `irAlDetalle` | | | Responde uuid y radicado | | |

Un radicado de CM genera como mínimo `2 + N` filas de `logs` (N = anexos): "Radicar solicitud académica", un "Adjuntar anexo académico" por anexo y "Radicar cancelación de matrícula". Todo se confirma o se revierte junto (transacción del controlador).

### 3.2 Revisión del Funcionario Académico

Pantalla `funcionario-academico/solicitudes/:uuid` con `CancelacionMatriculaProcesoComponent` (`rol = FUNCIONARIO`). Antes, la bandeja y el detalle de la sección 2.1.

| # | Acción (botón) | Servicio Angular | Endpoint | Caso de uso (`TramiteCancelacionMatriculaCUImplAdaptador`) | Tablas escritas |
|---|---|---|---|---|---|
| 1 | Rechazar solicitud (`RECHAZAR_FUNCIONARIO`): escaneo PDF obligatorio y observación obligatoria | `SolicitudAcademicaService.adjuntarResolucion`, luego `CancelacionMatriculaService.rechazarPorFuncionario` | `POST /solicitudes-academicas/{uuid}/resolucion`, luego `POST /cancelaciones-matricula/{uuid}/funcionario/rechazar` | `rechazarPorFuncionario`: `cambiarEtapa` exige observación y escaneo; `RADICADA` a `RECHAZADA` | `RESOLUCION_ACADEMICA`, `SOLICITUD_ACADEMICA`, `HISTORIAL_SOLICITUD_ACADEMICA`, `logs` |
| 2 | Remitir al Decano (`REMITIR_DECANO`): la tabla pasa a modo editable (faltas, nota, situación en la matrícula, situación al cancelar opcional) | `CatalogoAcademicoService.getSituaciones` (antes de abrir), luego `CancelacionMatriculaService.remitirADecano` | `GET /catalogos-academicos/situaciones`, luego `POST /cancelaciones-matricula/{uuid}/funcionario/remitir` | `remitirADecano`: `solicitudValidada` antes de escribir; `TramiteCancelacion.cobertura` (una evaluación por asignatura, sin ajenas, repetidas ni faltantes); por asignatura faltas (texto máximo 20, entero >= 0 si es numérico), nota (máximo 20, 0.0 a 5.0 con un decimal si es numérica), situación en la matrícula del catálogo (obligatoria); `RADICADA` a `EN_REVISION_DECANO`; retira el escaneo de la Resolución si existía | `ASIGNATURA_SOLICITUD_ACADEMICA`, `SOLICITUD_ACADEMICA`, `HISTORIAL_SOLICITUD_ACADEMICA`, `RESOLUCION_ACADEMICA` (borra), `logs` ("Cambiar etapa..." y "Evaluar asignaturas de cancelación de matrícula") |

### 3.3 Decisión del Decano

Pantalla `decano/solicitudes/:uuid` con el mismo componente (`rol = DECANO`).

| # | Acción (botón) | Servicio Angular | Endpoint | Caso de uso | Tablas escritas |
|---|---|---|---|---|---|
| 1 | Aprobar (`APROBAR_DECANO`): elige la situación al cancelar de cada asignatura (obligatoria) y observación opcional | `CancelacionMatriculaService.aprobarPorDecano` (`{situaciones, observacion}`) | `POST /cancelaciones-matricula/{uuid}/decano/aprobar` | `aprobarPorDecano`: `cobertura` de todas las asignaturas, situación al cancelar obligatoria y del catálogo (E4 `DATO_ASIGNATURA_FALTANTE`); `EN_REVISION_DECANO` a `APROBADA_POR_DECANO` | `ASIGNATURA_SOLICITUD_ACADEMICA`, `SOLICITUD_ACADEMICA`, `HISTORIAL_SOLICITUD_ACADEMICA`, `logs` ("Registrar situación al cancelar") |
| 2 | Rechazar (`RECHAZAR_DECANO`): observación opcional | `CancelacionMatriculaService.rechazarPorDecano` | `POST /cancelaciones-matricula/{uuid}/decano/rechazar` | `rechazarPorDecano`: `EN_REVISION_DECANO` a `RECHAZADA_POR_DECANO`; no toca las situaciones | `SOLICITUD_ACADEMICA`, `HISTORIAL_SOLICITUD_ACADEMICA`, `logs` |

### 3.4 Cierre: respuesta al Estudiante

| # | Acción | Servicio Angular | Endpoint | Caso de uso | Tablas escritas |
|---|---|---|---|---|---|
| 1 | Enviar respuesta (`ENVIAR_RESPUESTA`) por el Funcionario Académico: escaneo de la Resolución firmada (obligatorio si no existe) y observación opcional (máximo 500) | `SolicitudAcademicaService.adjuntarResolucion` (si hay archivo nuevo), luego `CancelacionMatriculaService.enviarRespuesta` | `POST /solicitudes-academicas/{uuid}/resolucion`, luego `POST /cancelaciones-matricula/{uuid}/funcionario/responder` | `enviarRespuesta`: `APROBADA_POR_DECANO` a `APROBADA` o `RECHAZADA_POR_DECANO` a `RECHAZADA`, ambas con escaneo; solo en `APROBADA`, `TramiteCancelacion.cancelarActivas` pasa a `cancelada` las matrículas de la solicitud que sigan `activa` (log "Cancelar asignaturas matriculadas") | `RESOLUCION_ACADEMICA`, `SOLICITUD_ACADEMICA`, `HISTORIAL_SOLICITUD_ACADEMICA`, `ASIGNATURA_MATRICULADA` (solo aprobada), `logs` |
| 2 | El Estudiante ve el resultado y descarga la Resolución | `SolicitudAcademicaService.descargarResolucion` | `GET /solicitudes-academicas/{uuid}/resolucion` | `ResolucionAcademicaCUImplAdaptador.obtenerResolucion` (solo en etapa final) | lectura |

El Estudiante no tiene botones de acción en CM: `accionesDisponibles` de su rol viene vacío en todas las etapas de este proceso porque ninguna fila de `TRANSICIONES` de CM tiene rol Estudiante salvo `RADICAR` (origen nulo). En CM no se oculta al Estudiante `numeroFaltas`, `nota` ni `situacionMatricula`, solo `situacionCancelar` hasta la etapa final (documento 04, D6; NO VERIFICADO si el DTO de respuesta los filtra).

## 4. Cancelación de Asignatura (CA) [EXTENSIÓN]

Tipo `CANCELACION_ASIGNATURA` (sigla `CA`). Sin anexos obligatorios; aprobación parcial por asignatura (decisión P14). Termina con Resolución escaneada.

### 4.1 Radicación por el Estudiante

| # | Pantalla / componente | Servicio Angular | Endpoint | Controlador y caso de uso | Gateway | Tablas |
|---|---|---|---|---|---|---|
| 1 | `estudiante/cancelacion-asignatura`: `EstCancelacionAsignaturaContentComponent` carga el formulario | `CancelacionAsignaturaService.getFormulario` | `GET /cancelaciones-asignatura/formulario` | `CancelacionAsignaturaRestController.getFormulario`, `ConsultaCancelacionAsignaturaCUImplAdaptador.getFormulario`: asignaturas activas y un único soporte libre opcional (`pdf,jpg,jpeg,png`, 5 MB) tomado de `AnexoAcademicoConstantes`, sin consultar catálogo de anexos | `SesionGatewayIntPuerto`, `EstudianteGatewayIntPuerto` | L: `usuarios`, `ESTUDIANTE`, `ASIGNATURA_MATRICULADA` |
| 2 | `radicar()`: `{motivo, asignaturas, soportes}` | `CancelacionAsignaturaService.radicar` | `POST /cancelaciones-asignatura` (multipart: `motivo`, un campo `asignaturas` por uuid elegido, partes `soporte`) | `CancelacionAsignaturaRestController.radicar` (`@Transactional`), `CancelacionAsignaturaCUImplAdaptador.radicarCancelacionAsignatura` | | |
| 3 | | | | Validación en orden: estudiante con rol y fila; motivo obligatorio y máximo 255; lista de asignaturas no vacía (E4 `SIN_ASIGNATURAS_ELEGIDAS`), cada uuid es una matrícula propia (E4 `ASIGNATURA_NO_MATRICULADA`, mismo mensaje para ajena e inexistente), sin repetir (E4) y `activa` (E4 `ASIGNATURA_NO_ACTIVA`); tipo del proceso existente; solo soportes libres (E4 `SOLO_SOPORTES_LIBRES`) y archivos válidos (E6) | `EstudianteGatewayIntPuerto`, `UsuarioGatewayIntPuerto`, `TipoSolicitudAcademicaGatewayIntPuerto` | L: `usuarios`, `ESTUDIANTE`, `ASIGNATURA_MATRICULADA` |
| 4 | | | | `SolicitudAcademicaCUImplAdaptador.crearSolicitud` (igual que 3.1, pasos 5) | `SolicitudAcademicaGatewayIntPuerto.crear` | E: `SOLICITUD_ACADEMICA`, `HISTORIAL_SOLICITUD_ACADEMICA`, `logs` |
| 5 | | | | `SolicitudCancelacionAsignaturaGatewayIntPuerto.guardar` (motivo) y `SolicitudCancelacionMatriculaGatewayIntPuerto.guardarAsignaturas` (gateway de esa tabla, compartido); `adjuntarAnexo` con tipo nulo por cada soporte; log "Radicar cancelación de asignatura" | `SolicitudCancelacionAsignaturaGatewayImplAdaptador`, `SolicitudCancelacionMatriculaGatewayImplAdaptador`, `AnexoAcademicoGatewayIntPuerto` | E: `SOLICITUD_CANCELACION_ASIGNATURA`, `ASIGNATURA_SOLICITUD_ACADEMICA`, `ANEXO_ACADEMICO`, `logs`; disco |

### 4.2 Revisión del Funcionario Académico

| # | Acción (botón) | Servicio Angular | Endpoint | Caso de uso (`TramiteCancelacionAsignaturaCUImplAdaptador`) | Tablas escritas |
|---|---|---|---|---|---|
| 1 | Rechazar solicitud (`RECHAZAR_FUNCIONARIO`) con escaneo y observación | `adjuntarResolucion` y `CancelacionAsignaturaService.rechazarPorFuncionario` | `POST /solicitudes-academicas/{uuid}/resolucion` y `POST /cancelaciones-asignatura/{uuid}/funcionario/rechazar` | `rechazarPorFuncionario`: `RADICADA` a `RECHAZADA` (rechaza la solicitud completa) | como CM |
| 2 | Remitir al Decano (`REMITIR_DECANO`): por fila faltas, nota, situación en la matrícula, "Cumple condiciones", observación de la evaluación (opcional, máximo 255) | `CatalogoAcademicoService.getSituaciones`, `CancelacionAsignaturaService.remitirADecano` | `GET /catalogos-academicos/situaciones`, `POST /cancelaciones-asignatura/{uuid}/funcionario/remitir` | `remitirADecano`: `cobertura`; `cumpleCondiciones` obligatorio (E4 `DATO_ASIGNATURA_FALTANTE`); si cumple y la nota es numérica menor a 3.0, E4 `NOTA_INSUFICIENTE_CUMPLE`; al menos una debe cumplir (E4 `NINGUNA_ASIGNATURA_CUMPLE`); `RADICADA` a `EN_REVISION_DECANO` | `ASIGNATURA_SOLICITUD_ACADEMICA`, `SOLICITUD_ACADEMICA`, `HISTORIAL_SOLICITUD_ACADEMICA`, `RESOLUCION_ACADEMICA` (borra), `logs` ("Evaluar asignaturas de cancelación de asignatura") |

### 4.3 Decisión del Decano (aprobación parcial)

| # | Acción (botón) | Servicio Angular | Endpoint | Caso de uso | Tablas escritas |
|---|---|---|---|---|---|
| 1 | "Aceptar tal como está" | `CancelacionAsignaturaService.aprobarPorDecano` con `aceptarEvaluacion = true` | `POST /cancelaciones-asignatura/{uuid}/decano/aprobar` | `aprobarPorDecano`: cada asignatura queda con `aprobadaPorDecano = cumpleCondiciones`; `EN_REVISION_DECANO` a `APROBADA_POR_DECANO` | `ASIGNATURA_SOLICITUD_ACADEMICA`, `SOLICITUD_ACADEMICA`, `HISTORIAL_SOLICITUD_ACADEMICA`, `logs` ("Registrar decisión del Decano por asignatura") |
| 2 | "Enviar decisión modificada": interruptor Aprobar o Rechazar por fila | igual con `decisiones` | igual | exactamente una de las dos formas (E4 `FORMA_APROBACION_DECANO`); `cobertura`; cada `aprobada` no nulo; al menos una aprobada (E4 `NINGUNA_ASIGNATURA_APROBADA`); las rechazadas pierden la situación al cancelar | igual |
| 3 | Rechazar solicitud completa (`RECHAZAR_DECANO`): el componente lo abre cuando todas las filas quedaron rechazadas | `CancelacionAsignaturaService.rechazarPorDecano` | `POST /cancelaciones-asignatura/{uuid}/decano/rechazar` | `EN_REVISION_DECANO` a `RECHAZADA_POR_DECANO` | `SOLICITUD_ACADEMICA`, `HISTORIAL_SOLICITUD_ACADEMICA`, `logs` |

### 4.4 Cierre: respuesta al Estudiante

| # | Acción | Servicio Angular | Endpoint | Caso de uso | Tablas escritas |
|---|---|---|---|---|---|
| 1 | Enviar respuesta: escaneo, observación general y `ajustes` de situación para las aprobadas sin situación o cuya decisión cambió | `adjuntarResolucion`, `CancelacionAsignaturaService.enviarRespuesta` (`ajustes` solo si hay) | `POST /solicitudes-academicas/{uuid}/resolucion`, `POST /cancelaciones-asignatura/{uuid}/funcionario/responder` | `enviarRespuesta`: desde `RECHAZADA_POR_DECANO` no se aceptan ajustes (E4 `AJUSTES_SOLICITUD_RECHAZADA`); desde `APROBADA_POR_DECANO` `aplicarAjustes` (E4 `AJUSTE_ASIGNATURA_FALTANTE`, `SITUACION_CANCELAR_RECHAZADA`, `DATO_ASIGNATURA_FALTANTE`); si el Decano rechazó la solicitud o alguna asignatura, la observación es obligatoria (E4 `OBSERVACION_RESPUESTA_REQUERIDA`); `APROBADA_POR_DECANO` a `APROBADA` o `RECHAZADA_POR_DECANO` a `RECHAZADA`, ambas con escaneo; en `APROBADA` solo se pasan a `cancelada` las asignaturas con `aprobadaPorDecano = true` que sigan `activa` | `RESOLUCION_ACADEMICA`, `ASIGNATURA_SOLICITUD_ACADEMICA` (si hay ajustes), `SOLICITUD_ACADEMICA`, `HISTORIAL_SOLICITUD_ACADEMICA`, `ASIGNATURA_MATRICULADA`, `logs` |

Visibilidad: el Estudiante ve solo uuid, asignatura matriculada y, en etapa final, `aprobadaPorDecano`, `observacionDecision` y `situacionCancelar` (esta última solo en las aprobadas) (`ConsultaCancelacionAsignaturaCUImplAdaptador.vistaEstudiante`). En la bandeja Respuestas del Funcionario, una CA final puede ocupar hasta dos filas (aprobadas y rechazadas) según `resultadosAsignaturas`.

## 5. Examen Supletorio (ES) [EXTENSIÓN]

Tipo `EXAMEN_SUPLETORIO` (sigla `ES`). Sin Resolución. Plazo de 3 días hábiles. Tras la aprobación del Decano hay una fase de pago con dos etapas exclusivas del proceso: `PENDIENTE_PAGO` y `EN_VERIFICACION_PAGO`.

### 5.1 Radicación por el Estudiante

| # | Pantalla / componente | Servicio Angular | Endpoint | Controlador y caso de uso | Gateway | Tablas |
|---|---|---|---|---|---|---|
| 1 | `estudiante/examen-supletorio`: `EstExamenSupletorioContentComponent` carga el formulario | `ExamenSupletorioService.getFormulario` | `GET /examenes-supletorios/formulario` | `ExamenSupletorioRestController.getFormulario`, `ConsultaExamenSupletorioCUImplAdaptador.getFormulario`: asignaturas activas, causas `cruce` y `otra`, anexos de radicación (los tres de `CAUSAS_POR_ANEXO`, en el orden de `ORDEN_ANEXOS`), `plazoDiasHabiles = 3` | `SesionGatewayIntPuerto`, `EstudianteGatewayIntPuerto`, `TipoSolicitudAcademicaGatewayIntPuerto`, `TipoAnexoAcademicoGatewayIntPuerto` | L: `usuarios`, `ESTUDIANTE`, `ASIGNATURA_MATRICULADA`, `TIPO_SOLICITUD_ACADEMICA`, `TIPO_ANEXO_ACADEMICO` |
| 2 | `radicar()`: asignatura, fecha del examen no presentado, causa, y con cruce asignatura cruzada, fecha y hora | `ExamenSupletorioService.radicar` | `POST /examenes-supletorios` (multipart; fechas `AAAA-MM-DD`) | `ExamenSupletorioRestController.radicar` (`@Transactional`), `ExamenSupletorioCUImplAdaptador.radicarExamenSupletorio` | | |
| 3 | | | | Validación en orden: estudiante con rol y fila; tipo existente; sin solicitud en curso (`verificarSinSolicitudEnCurso`, E4); asignatura obligatoria, propia y activa (E4); causa `cruce` u `otra` sin distinguir mayúsculas (E4 `CAUSA_SUPLETORIO_INVALIDA`); `validarPlazo` con el `Clock` de Bogotá: fecha obligatoria, no futura (E4 `FECHA_EXAMEN_FUTURA`) y como máximo 3 días de lunes a viernes posteriores al examen hasta hoy inclusive, sin festivos (E4 `PLAZO_SUPLETORIO_VENCIDO`); con `cruce`, asignatura cruzada distinta, propia y activa, fecha y hora `HH:mm` (E4, E6), con `otra` ningún dato de cruce (E4); anexos: solo con tipo, del proceso, sin recibo ni comprobante, sin el de la otra causa, sin repetidos, con el FOR-23 siempre y el de la causa (E4 `ANEXOS_OBLIGATORIOS_FALTANTES`) | `EstudianteGatewayIntPuerto`, `TipoAnexoAcademicoGatewayIntPuerto` | L: `ASIGNATURA_MATRICULADA`, `TIPO_ANEXO_ACADEMICO` |
| 4 | | | | `SolicitudAcademicaCUImplAdaptador.crearSolicitud`; `SolicitudExamenSupletorioGatewayIntPuerto.guardar` (con cruce si aplica); `adjuntarAnexo` por anexo; log "Radicar examen supletorio" | `SolicitudExamenSupletorioGatewayImplAdaptador`, `AnexoAcademicoGatewayIntPuerto` | E: `SOLICITUD_ACADEMICA`, `HISTORIAL_SOLICITUD_ACADEMICA`, `SOLICITUD_EXAMEN_SUPLETORIO`, `SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA`, `ANEXO_ACADEMICO`, `logs`; disco |

### 5.2 Trámite del Funcionario Académico, del Decano y del Estudiante

Caso de uso: `TramiteExamenSupletorioCUImplAdaptador`; siete de las nueve operaciones pasan por el método privado `accion(...)` (documento 04, 9.2). Pantalla: `ExamenSupletorioProcesoComponent` según el rol.

| # | Quién y acción | Servicio Angular | Endpoint | Transición y reglas | Tablas escritas |
|---|---|---|---|---|---|
| 1 | Funcionario: Rechazar (`RECHAZAR_FUNCIONARIO`), observación obligatoria, sin escaneo | `ExamenSupletorioService.rechazarPorFuncionario` | `POST /examenes-supletorios/{uuid}/funcionario/rechazar` | `RADICADA` a `RECHAZADA` | `SOLICITUD_ACADEMICA`, `HISTORIAL_SOLICITUD_ACADEMICA`, `logs` |
| 2 | Funcionario: Remitir al Decano (`REMITIR_DECANO`), observación opcional | `ExamenSupletorioService.remitirADecano` | `POST /examenes-supletorios/{uuid}/funcionario/remitir` | `RADICADA` a `EN_REVISION_DECANO`; no exige nada más (P49, el campo `requisitosVerificados` se ignora) | igual |
| 3 | Decano: Aprobar o Rechazar | `ExamenSupletorioService.aprobarPorDecano` o `rechazarPorDecano` | `POST /examenes-supletorios/{uuid}/decano/aprobar` o `.../decano/rechazar` | `EN_REVISION_DECANO` a `APROBADA_POR_DECANO` o `RECHAZADA_POR_DECANO`; observación opcional | igual |
| 4 | Funcionario: Enviar respuesta tras rechazo del Decano | `ExamenSupletorioService.enviarRespuesta` | `POST /examenes-supletorios/{uuid}/funcionario/responder` | `RECHAZADA_POR_DECANO` a `RECHAZADA`; sin escaneo | igual |
| 5 | Funcionario: Enviar recibo de pago (`ENVIAR_RECIBO`) | `SolicitudAcademicaService.adjuntarAnexo` (tipo "Recibo de pago"), luego `ExamenSupletorioService.enviarRecibo` | `POST /solicitudes-academicas/{uuid}/anexos`, luego `POST /examenes-supletorios/{uuid}/funcionario/recibo` | `APROBADA_POR_DECANO` a `PENDIENTE_PAGO`; exige el anexo "Recibo de pago" ya cargado | `ANEXO_ACADEMICO`, `SOLICITUD_ACADEMICA`, `HISTORIAL_SOLICITUD_ACADEMICA`, `logs` |
| 6 | Estudiante: Subir comprobante de pago (`SUBIR_COMPROBANTE`) | `SolicitudAcademicaService.adjuntarAnexo` (tipo "Comprobante de pago"), luego `ExamenSupletorioService.subirComprobante` | `POST /solicitudes-academicas/{uuid}/anexos`, luego `POST /examenes-supletorios/{uuid}/estudiante/comprobante` | `PENDIENTE_PAGO` a `EN_VERIFICACION_PAGO`; exige el comprobante cargado; solo el Estudiante dueño | igual |
| 7 | Funcionario: Aprobar comprobante (`APROBAR_COMPROBANTE`), fecha acordada opcional | `ExamenSupletorioService.aprobarComprobante` | `POST /examenes-supletorios/{uuid}/funcionario/comprobante/aprobar` | `EN_VERIFICACION_PAGO` a `APROBADA`; la fecha no puede ser anterior a `fechaExamenNoPresentado` (E4 `FECHA_ACORDADA_ANTERIOR`); si llega, `SolicitudExamenSupletorioGatewayIntPuerto.actualizarFechaAcordada` y log "Acordar fecha de examen supletorio" | además `SOLICITUD_EXAMEN_SUPLETORIO` |
| 8 | Funcionario: Rechazar comprobante (`RECHAZAR_COMPROBANTE`), observación obligatoria | `ExamenSupletorioService.rechazarComprobante` | `POST /examenes-supletorios/{uuid}/funcionario/comprobante/rechazar` | `EN_VERIFICACION_PAGO` a `RECHAZADA`; el Estudiante debe radicar una solicitud nueva (P4) | `SOLICITUD_ACADEMICA`, `HISTORIAL_SOLICITUD_ACADEMICA`, `logs` |

Ninguna acción del supletorio cambia el estado de las asignaturas matriculadas. `REMITIR_DECANO` ejecuta el mismo `cambiarEtapa` genérico que en las cancelaciones y por eso el caso `RESOLUCION_ACADEMICA` aparece como tabla alcanzable en el documento 03 (fila 121), pero el borrado solo ocurre si existe la fila, y el supletorio no sube escaneo (`ResolucionAcademicaCU.adjuntarResolucion` lo rechaza con E4 `RESOLUCION_SOLO_CANCELACIONES`).

La etiqueta del Funcionario en `APROBADA_POR_DECANO` para el supletorio es "Enviar Recibo" (sobrescrita en `ConsultaSolicitudAcademicaCUImplAdaptador.etiquetaDe`); en las cancelaciones queda "Pendiente Notificar" porque `ETAPA_ETIQUETA_ROL` no distingue proceso.

## 6. Máquinas de estado

Fuente única de las transiciones de los tres procesos: la constante `TRANSICIONES` de `MaquinaEtapas` (`bk/dominio/servicios/MaquinaEtapas.java`), 13 filas, y `RESPONSABLES`, 8 etapas. Las etapas se guardan en `ETAPA_SOLICITUD_ACADEMICA` (6 universales con `TipoSolicitudAcademica_uuid` nulo y 2 propias del supletorio, según el seed). El estado vigente de una solicitud es `SOLICITUD_ACADEMICA.Etapa_uuid`; el historial no guarda la etapa, se reconstruye al leer encadenando `MaquinaEtapas.etapaDestino`.

### 6.1 Tabla maestra de las 13 transiciones

| Procesos | Origen | Acción (`AccionEtapa`) | Destino | Rol | Exige observación | Exige escaneo de Resolución | Exige recibo | Exige comprobante |
|---|---|---|---|---|---|---|---|---|
| CM, CA, ES | (nueva) | `RADICAR` | `RADICADA` | Estudiante | no | no | no | no |
| CM, CA | `RADICADA` | `RECHAZAR_FUNCIONARIO` | `RECHAZADA` | Funcionario | sí | sí | no | no |
| ES | `RADICADA` | `RECHAZAR_FUNCIONARIO` | `RECHAZADA` | Funcionario | sí | no | no | no |
| CM, CA, ES | `RADICADA` | `REMITIR_DECANO` | `EN_REVISION_DECANO` | Funcionario | no | no | no | no |
| CM, CA, ES | `EN_REVISION_DECANO` | `APROBAR_DECANO` | `APROBADA_POR_DECANO` | Decano | no | no | no | no |
| CM, CA, ES | `EN_REVISION_DECANO` | `RECHAZAR_DECANO` | `RECHAZADA_POR_DECANO` | Decano | no | no | no | no |
| CM, CA | `APROBADA_POR_DECANO` | `ENVIAR_RESPUESTA` | `APROBADA` | Funcionario | no | sí | no | no |
| CM, CA | `RECHAZADA_POR_DECANO` | `ENVIAR_RESPUESTA` | `RECHAZADA` | Funcionario | no | sí | no | no |
| ES | `RECHAZADA_POR_DECANO` | `ENVIAR_RESPUESTA` | `RECHAZADA` | Funcionario | no | no | no | no |
| ES | `APROBADA_POR_DECANO` | `ENVIAR_RECIBO` | `PENDIENTE_PAGO` | Funcionario | no | no | sí | no |
| ES | `PENDIENTE_PAGO` | `SUBIR_COMPROBANTE` | `EN_VERIFICACION_PAGO` | Estudiante | no | no | no | sí |
| ES | `EN_VERIFICACION_PAGO` | `APROBAR_COMPROBANTE` | `APROBADA` | Funcionario | no | no | no | no |
| ES | `EN_VERIFICACION_PAGO` | `RECHAZAR_COMPROBANTE` | `RECHAZADA` | Funcionario | sí | no | no | no |

Responsable de cada etapa (`RESPONSABLES`): `RADICADA` Funcionario; `EN_REVISION_DECANO` Decano; `APROBADA_POR_DECANO` Funcionario; `RECHAZADA_POR_DECANO` Funcionario; `PENDIENTE_PAGO` Estudiante; `EN_VERIFICACION_PAGO` Funcionario; `APROBADA` y `RECHAZADA` Ninguno (finales). Conteo por proceso: CM 7 transiciones (4 compartidas + 3 de cancelación), CA 7, ES 10 (4 compartidas + 6 propias). 4 + 3 + 6 = 13.

Reglas transversales que no están en la tabla: quien actúa debe ser el Estudiante dueño, el Funcionario Académico asignado al tipo en `TIPO_SOLICITUD_ACADEMICA.FuncionarioAcademico_uuid`, o un usuario con rol `Decano` (`ValidadorActorSolicitud`); una solicitud solo puede tener otra del mismo tipo si la anterior está en `APROBADA` o `RECHAZADA`; el Estudiante ve la etapa enmascarada (`APROBADA_POR_DECANO` y `RECHAZADA_POR_DECANO` se muestran como `EN_REVISION_DECANO`).

### 6.2 Cancelación de Matrícula

| Estado origen | Acción | Estado destino | Quién | Condición (`MaquinaEtapas` y caso de uso) |
|---|---|---|---|---|
| (inicio) | `RADICAR` | `RADICADA` | Estudiante | al menos una matrícula activa; motivo de hasta 255; seis anexos obligatorios presentes; sin solicitud CM en curso |
| `RADICADA` | `RECHAZAR_FUNCIONARIO` | `RECHAZADA` | Funcionario Académico asignado | observación y escaneo de la Resolución ya subido |
| `RADICADA` | `REMITIR_DECANO` | `EN_REVISION_DECANO` | Funcionario Académico asignado | evaluación completa de todas las asignaturas (faltas, nota, situación en la matrícula); retira el escaneo previo |
| `EN_REVISION_DECANO` | `APROBAR_DECANO` | `APROBADA_POR_DECANO` | Decano | situación al cancelar para todas las asignaturas |
| `EN_REVISION_DECANO` | `RECHAZAR_DECANO` | `RECHAZADA_POR_DECANO` | Decano | ninguna (observación opcional) |
| `APROBADA_POR_DECANO` | `ENVIAR_RESPUESTA` | `APROBADA` | Funcionario Académico asignado | escaneo de la Resolución; cancela las matrículas activas de la solicitud |
| `RECHAZADA_POR_DECANO` | `ENVIAR_RESPUESTA` | `RECHAZADA` | Funcionario Académico asignado | escaneo de la Resolución |

```mermaid
stateDiagram-v2
    [*] --> RADICADA : RADICAR - Estudiante
    RADICADA --> RECHAZADA : RECHAZAR_FUNCIONARIO - Funcionario, con escaneo
    RADICADA --> EN_REVISION_DECANO : REMITIR_DECANO - Funcionario
    EN_REVISION_DECANO --> APROBADA_POR_DECANO : APROBAR_DECANO - Decano
    EN_REVISION_DECANO --> RECHAZADA_POR_DECANO : RECHAZAR_DECANO - Decano
    APROBADA_POR_DECANO --> APROBADA : ENVIAR_RESPUESTA - Funcionario, con escaneo
    RECHAZADA_POR_DECANO --> RECHAZADA : ENVIAR_RESPUESTA - Funcionario, con escaneo
    APROBADA --> [*]
    RECHAZADA --> [*]
```

### 6.3 Cancelación de Asignatura

La máquina de etapas es idéntica a la de CM; cambian las condiciones de los casos de uso.

| Estado origen | Acción | Estado destino | Quién | Condición |
|---|---|---|---|---|
| (inicio) | `RADICAR` | `RADICADA` | Estudiante | al menos una asignatura activa y propia, sin repetir; solo soportes libres; motivo de hasta 255; sin solicitud CA en curso |
| `RADICADA` | `RECHAZAR_FUNCIONARIO` | `RECHAZADA` | Funcionario Académico asignado | observación y escaneo de la Resolución |
| `RADICADA` | `REMITIR_DECANO` | `EN_REVISION_DECANO` | Funcionario Académico asignado | evaluación de todas las asignaturas con `cumpleCondiciones`; una nota numérica menor a 3.0 no puede cumplir; al menos una cumple; retira el escaneo previo |
| `EN_REVISION_DECANO` | `APROBAR_DECANO` | `APROBADA_POR_DECANO` | Decano | `aceptarEvaluacion` o `decisiones` por asignatura (una de las dos); al menos una aprobada |
| `EN_REVISION_DECANO` | `RECHAZAR_DECANO` | `RECHAZADA_POR_DECANO` | Decano | ninguna (observación opcional) |
| `APROBADA_POR_DECANO` | `ENVIAR_RESPUESTA` | `APROBADA` | Funcionario Académico asignado | escaneo; ajustes de situación donde la decisión cambió o falta la situación; observación obligatoria si alguna asignatura quedó rechazada; cancela solo las asignaturas con `aprobadaPorDecano = true` |
| `RECHAZADA_POR_DECANO` | `ENVIAR_RESPUESTA` | `RECHAZADA` | Funcionario Académico asignado | escaneo; sin ajustes; observación obligatoria |

```mermaid
stateDiagram-v2
    [*] --> RADICADA : RADICAR - Estudiante
    RADICADA --> RECHAZADA : RECHAZAR_FUNCIONARIO - Funcionario, con escaneo
    RADICADA --> EN_REVISION_DECANO : REMITIR_DECANO - Funcionario, al menos una cumple
    EN_REVISION_DECANO --> APROBADA_POR_DECANO : APROBAR_DECANO - Decano, al menos una aprobada
    EN_REVISION_DECANO --> RECHAZADA_POR_DECANO : RECHAZAR_DECANO - Decano
    APROBADA_POR_DECANO --> APROBADA : ENVIAR_RESPUESTA - Funcionario, cancela las aprobadas
    RECHAZADA_POR_DECANO --> RECHAZADA : ENVIAR_RESPUESTA - Funcionario, con escaneo
    APROBADA --> [*]
    RECHAZADA --> [*]
```

La aprobación es por asignatura (campo `aprobadaPorDecano` de `ASIGNATURA_SOLICITUD_ACADEMICA`); el estado final de la solicitud completa es `APROBADA` aunque alguna asignatura haya sido rechazada. El estado de cada asignatura se refleja en la bandeja Respuestas mediante `resultadosAsignaturas`, no en una etapa propia.

### 6.4 Examen Supletorio

| Estado origen | Acción | Estado destino | Quién | Condición |
|---|---|---|---|---|
| (inicio) | `RADICAR` | `RADICADA` | Estudiante | plazo de 3 días hábiles desde el examen no presentado; causa `cruce` u `otra` con sus datos y anexos; sin solicitud ES en curso |
| `RADICADA` | `RECHAZAR_FUNCIONARIO` | `RECHAZADA` | Funcionario Académico asignado | observación; sin escaneo |
| `RADICADA` | `REMITIR_DECANO` | `EN_REVISION_DECANO` | Funcionario Académico asignado | ninguna (observación opcional) |
| `EN_REVISION_DECANO` | `APROBAR_DECANO` | `APROBADA_POR_DECANO` | Decano | ninguna (observación opcional) |
| `EN_REVISION_DECANO` | `RECHAZAR_DECANO` | `RECHAZADA_POR_DECANO` | Decano | ninguna (observación opcional) |
| `RECHAZADA_POR_DECANO` | `ENVIAR_RESPUESTA` | `RECHAZADA` | Funcionario Académico asignado | ninguna (mensaje opcional); sin escaneo |
| `APROBADA_POR_DECANO` | `ENVIAR_RECIBO` | `PENDIENTE_PAGO` | Funcionario Académico asignado | anexo "Recibo de pago" cargado |
| `PENDIENTE_PAGO` | `SUBIR_COMPROBANTE` | `EN_VERIFICACION_PAGO` | Estudiante dueño | anexo "Comprobante de pago" cargado |
| `EN_VERIFICACION_PAGO` | `APROBAR_COMPROBANTE` | `APROBADA` | Funcionario Académico asignado | fecha acordada opcional y no anterior a la fecha del examen no presentado |
| `EN_VERIFICACION_PAGO` | `RECHAZAR_COMPROBANTE` | `RECHAZADA` | Funcionario Académico asignado | observación obligatoria |

```mermaid
stateDiagram-v2
    [*] --> RADICADA : RADICAR - Estudiante
    RADICADA --> RECHAZADA : RECHAZAR_FUNCIONARIO - Funcionario, con observacion
    RADICADA --> EN_REVISION_DECANO : REMITIR_DECANO - Funcionario
    EN_REVISION_DECANO --> APROBADA_POR_DECANO : APROBAR_DECANO - Decano
    EN_REVISION_DECANO --> RECHAZADA_POR_DECANO : RECHAZAR_DECANO - Decano
    RECHAZADA_POR_DECANO --> RECHAZADA : ENVIAR_RESPUESTA - Funcionario
    APROBADA_POR_DECANO --> PENDIENTE_PAGO : ENVIAR_RECIBO - Funcionario, con recibo
    PENDIENTE_PAGO --> EN_VERIFICACION_PAGO : SUBIR_COMPROBANTE - Estudiante, con comprobante
    EN_VERIFICACION_PAGO --> APROBADA : APROBAR_COMPROBANTE - Funcionario
    EN_VERIFICACION_PAGO --> RECHAZADA : RECHAZAR_COMPROBANTE - Funcionario, con observacion
    APROBADA --> [*]
    RECHAZADA --> [*]
```

### 6.5 Etiquetas visibles por rol (`ETAPA_ETIQUETA_ROL`, seed)

Fuente: `docs/database/seed-procesos-academicos.sql` (23 filas, incluidas las dos sentencias `UPDATE` finales). No es la fuente de las transiciones, solo de lo que se muestra.

| Etapa | Estudiante | Funcionario | Decano |
|---|---|---|---|
| `RADICADA` | En trámite | Pendiente | sin fila (no la ve) |
| `EN_REVISION_DECANO` | En trámite | En Gestión | Pendiente |
| `APROBADA_POR_DECANO` | En trámite | Pendiente Notificar (en ES, "Enviar Recibo" por `etiquetaDe`) | Respondida |
| `RECHAZADA_POR_DECANO` | En trámite | Pendiente Notificar | Respondida |
| `PENDIENTE_PAGO` (solo ES) | Pendiente de pago | En Gestión | Respondida |
| `EN_VERIFICACION_PAGO` (solo ES) | En verificación de pago | Recibo Pagado | Respondida |
| `APROBADA` | Aprobada | Respondida | Respondida |
| `RECHAZADA` | Rechazada | Respondida | Respondida |

### 6.6 Estado de la solicitud del Consejo [BASE] (sin máquina en el servidor)

A diferencia de los procesos académicos, las solicitudes del Consejo de Julián (`solicitudes.estado`, cadena libre) no tienen tabla de transiciones. El backend solo fija el estado inicial (`SolicitudCUImplAdaptador.crearSolicitud` y `crearSolicitudPublica`: `AGREGADO EN EL ORDEN DEL DÍA` o `SIN RESPONDER`, constantes de `ApplicationConstantes`). Todo cambio posterior ocurre por `PUT /solicitudes/{uuid}`, cuyo DTO solo exige entre 5 y 1000 caracteres, y por tanto acepta cualquier cadena. Las cinco cadenas conocidas y sus cambios son convención del frontend (`fe/core/constantes/constantes.ts`: `ESTADOS_SOLICITUD`, `PENDIENTE_AL_ORDEN_DEL_DIA`, `AGREGADO_EN_EL_ORDEN_DEL_DIA`, `RESPONDIDA`).

| Estado | Cómo se llega | Quién | Mecanismo |
|---|---|---|---|
| `SIN RESPONDER` | Al crear la solicitud sin orden del día; o por edición manual | Backend al crear; Funcionario o Secretario General al editar | `SolicitudCUImplAdaptador.crearSolicitud`; `PUT /solicitudes/{uuid}` |
| `AGREGADO EN EL ORDEN DEL DÍA` | Al crear la solicitud con orden del día; al asignarla en el visualizador; al eliminar el archivo de respuesta | Backend al crear; Secretario General o Decano en la pantalla de orden del día | `crearSolicitud`; `registrarSolicitudesEnOrdenDelDia`; `DialogGestionRespuestaComponent.onArchivoEliminado` |
| `PENDIENTE AL ORDEN DEL DÍA` | Al retirar una solicitud de la orden del día (se envía `uuidOrdenDelDia` vacío); o por edición manual | Secretario General o Decano; Funcionario al editar | `removerSolicitud`; `PUT /solicitudes/{uuid}` |
| `RESPONDIDA` | Al cargar el archivo de respuesta | Secretario General o Decano (pantalla de respuestas) | `DialogGestionRespuestaComponent.onArchivoSeleccionado` |
| `RECHAZADO` | Solo por edición manual desde el selector de estados | Funcionario o Secretario General | `PUT /solicitudes/{uuid}` |

```mermaid
stateDiagram-v2
    state "SIN RESPONDER" as SIN_RESPONDER
    state "AGREGADO EN EL ORDEN DEL DIA" as AGREGADO
    state "PENDIENTE AL ORDEN DEL DIA" as PENDIENTE
    state "RESPONDIDA" as RESPONDIDA
    state "RECHAZADO" as RECHAZADO
    [*] --> SIN_RESPONDER : crear sin orden del dia
    [*] --> AGREGADO : crear con orden del dia
    SIN_RESPONDER --> PENDIENTE : edicion del Funcionario
    SIN_RESPONDER --> RECHAZADO : edicion del Funcionario
    PENDIENTE --> AGREGADO : agregar a la orden del dia
    AGREGADO --> PENDIENTE : retirar de la orden del dia
    AGREGADO --> RESPONDIDA : cargar archivo de respuesta
    RESPONDIDA --> AGREGADO : eliminar archivo de respuesta
```

El diagrama muestra solo los cambios que el código de la interfaz produce por una acción nombrada; el selector de estados del formulario "Actualizar Solicitud" permite pasar de cualquier estado a cualquier otro de los cinco. Que "SIN RESPONDER" pase a "PENDIENTE AL ORDEN DEL DÍA" por edición del Funcionario es inferencia: el visualizador solo lista solicitudes en `PENDIENTE AL ORDEN DEL DÍA` y ningún código fija ese estado salvo `removerSolicitud` y la edición manual. NO VERIFICADO si existe un procedimiento operativo documentado fuera del código.

### 6.7 Estado de `ASIGNATURA_MATRICULADA` (afectado por los procesos)

Constantes en `EstadoAsignaturaMatriculadaConstantes`: `activa`, `cancelada`, `aprobada`, `perdida`. El estado inicial es `activa` (`EstudianteCUImplAdaptador.agregarAsignaturaMatriculada` y creación). Solo se permite salir de `activa` (`cambiarEstadoAsignatura`: E6 `CAMBIO_ESTADO_NO_PERMITIDO` si se intenta volver a `activa`).

| Origen | Destino | Quién y mecanismo | Condición |
|---|---|---|---|
| `activa` | `cancelada` | Funcionario Académico, al `enviarRespuesta` que deja la solicitud en `APROBADA` (`TramiteCancelacion.cancelarActivas`, `SolicitudCancelacionMatriculaGatewayIntPuerto.cambiarEstadoAsignaturasMatriculadas`) | CM: todas las de la solicitud que sigan activas; CA: solo las con `aprobadaPorDecano = true` |
| `activa` | `cancelada`, `aprobada` o `perdida` | Secretario General o Decano, `PATCH /estudiantes/{uuid}/asignaturas/{uuidMatricula}/estado` | estado nuevo en el conjunto de cuatro y origen `activa` |

```mermaid
stateDiagram-v2
    [*] --> activa : matricular
    activa --> cancelada : envio de respuesta aprobada en CM o CA, o cambio de estado administrativo
    activa --> aprobada : cambio de estado administrativo
    activa --> perdida : cambio de estado administrativo
    cancelada --> [*]
    aprobada --> [*]
    perdida --> [*]
```

El Examen Supletorio no modifica este estado. Radicar CM, CA o ES solo exige que la matrícula esté `activa`.

## 7. Hallazgos y puntos NO VERIFICADO

Hallazgos nuevos o de contraste, todos con evidencia de código, no de ejecución:

1. Subir un archivo y ejecutar la acción son dos transacciones: Resolución (cancelaciones) y recibo o comprobante (supletorio) se suben con `POST solicitudes-academicas/{uuid}/resolucion|anexos` antes de la acción. Un fallo entre las dos deja el archivo guardado con la solicitud en la etapa anterior. El cliente lo compensa con `resolucionSubida` y `anexoPagoSubido`; el servidor lo permite porque `adjuntarResolucion` reemplaza la fila existente.
2. En Julián, el cambio a `RESPONDIDA` se hace con dos peticiones encadenadas desde Angular (archivo y luego estado); el backend no lo hace de forma atómica (sección 1.5).
3. `PUT /solicitudes/{uuid}`, `GET /solicitudes/exportar*` y `GET /solicitudes/anexos/download` están abiertos a cualquier autenticado por la regla `solicitudes/**`, aunque sus anotaciones digan Secretario General y Decano; sin `@EnableMethodSecurity` la anotación no se evalúa (documento 03, sección 5; `backend/CLAUDE.md`).
4. Una contraseña errónea en el login termina en la rama de `AuthenticationException` (403), no en E5 (sección 1.1). Lectura de código.
5. La máquina de estados de los procesos académicos vive en una tabla en memoria (`MaquinaEtapas.TRANSICIONES`), no en base de datos; en base solo están las 8 etapas y las etiquetas por rol. Cambiar una transición exige recompilar. El estado del Consejo, por el contrario, no está modelado en ningún lado del servidor (sección 6.6).
6. Documentación desactualizada ya señalada en 04 (D1: remisión del supletorio con "requisitos verificados" en `etapas-por-proceso.md` 3.2, que P49 deja sin efecto) sigue vigente en el árbol de trabajo.

NO VERIFICADO en este documento:

- Comportamiento en ejecución de cualquier flujo (no se corrió el backend, el frontend ni las pruebas `backend/pruebas/*.ps1`).
- Nombres exactos de los gateways y repositorios por caso de uso más allá de los citados; las tablas listadas como "L" son la unión estática del documento 03, anexo C, una cota superior.
- Número de marcadores por párrafo en las plantillas `.docx` y contenido de las plantillas (no se abrieron).
- Si `DetalleSolicitudAcademicaComponent` o los DTO de respuesta de CM ocultan `numeroFaltas` y `nota` al Estudiante (documento 04, D6).
- Procedimiento operativo para pasar una solicitud del Consejo a `PENDIENTE AL ORDEN DEL DÍA` (sección 6.6).


===== 11-pruebas-despliegue-configuracion.md =====

# 11 - Pruebas, despliegue y configuración

Análisis de solo lectura. Etiquetas: **[BASE]** prototipo de Julián (commit `d3a48cc`), **[EXTENSIÓN]** agregado por este trabajo de grado. No se ejecutó ninguna prueba, build, contenedor ni script: toda cifra de "pruebas que pasan" es NO VERIFICADO; las cifras de este documento son conteos estáticos del árbol de trabajo (que tiene cambios sin confirmar en `git status`, incluidos varios archivos de test del backend).

## 1. Pruebas automatizadas del backend

Ubicación: `backend/solicitudes/src/test/java/com/unicauca/cfiet/solicitudes/`. Marco: JUnit 5 + Mockito, que llegan con `spring-boot-starter-test` (`backend/solicitudes/pom.xml`, alcance test).

### 1.1 Cifras

| Medida | Valor | Cómo se obtuvo |
|---|---|---|
| Archivos en `src/test` | 38 (todos `.java`; no hay recursos de test) | `find src/test -type f` |
| Anotaciones `@Test` | 615 | `grep -rho "@Test\b"` |
| Anotaciones `@ParameterizedTest` | 81 | ídem (fuentes: 41 `@ValueSource`, 19 `@CsvSource`, 16 `@MethodSource`, 4 `@EnumSource`, 3 `@NullSource`) |
| Métodos de prueba (`@Test` + `@ParameterizedTest`) | 696 | suma |
| `@RepeatedTest` / `@Disabled` | 0 / 0 | grep |
| Invocaciones totales de `mvnw test` | 1233 según `backend/CLAUDE.md` ("Hoy `.\mvnw.cmd test` corre 1233 pruebas") | NO VERIFICADO: no se ejecutó; la diferencia con 696 se explica por los casos de las pruebas parametrizadas, pero no se comprobó el cálculo |
| Archivos con Mockito | 27 de 38; 26 con `@ExtendWith(MockitoExtension.class)` | grep |
| Pruebas de integración con contexto Spring | 1 (`SolicitudesApplicationTests`, `@SpringBootTest`, un `@Test` `contextLoads`) | lectura |

Origen: de los 38 archivos, solo `SolicitudesApplicationTests.java` [BASE] (generado por el Initializr, 13 líneas insertadas en `d3a48cc`); los otros 37 son [EXTENSIÓN]. Julián no escribió pruebas de caso de uso, de controlador ni de gateway (`backend/CLAUDE.md`).

### 1.2 Detalle por archivo (cantidad de `@Test`; los parametrizados se cuentan aparte en el total)

| Paquete (bajo `.../solicitudes/`) | Archivo | `@Test` |
|---|---|---|
| raíz | `SolicitudesApplicationTests` | 1 |
| `dominio/casosdeuso` | `AnexoAcademicoCUImplAdaptadorTest` | 38 |
| | `AsignacionFuncionarioAcademicoCUImplAdaptadorTest` | 6 |
| | `AsignaturaCUImplAdaptadorTest` | 29 |
| | `CancelacionAsignaturaCUImplAdaptadorTest` | 14 |
| | `CancelacionMatriculaCUImplAdaptadorTest` | 18 |
| | `ConsultaCancelacionAsignaturaCUImplAdaptadorTest` | 9 |
| | `ConsultaCancelacionMatriculaCUImplAdaptadorTest` | 9 |
| | `ConsultaExamenSupletorioCUImplAdaptadorTest` | 7 |
| | `ConsultaSolicitudAcademicaCUImplAdaptadorTest` | 40 |
| | `EstudianteCUImplAdaptadorTest` | 51 |
| | `EtapaSolicitudAcademicaCUImplAdaptadorTest` | 3 |
| | `ExamenSupletorioCUImplAdaptadorTest` | 28 |
| | `FuncionarioAcademicoCUImplAdaptadorTest` | 26 |
| | `ResolucionAcademicaCUImplAdaptadorTest` | 20 |
| | `SituacionAcademicaAsignaturaCUImplAdaptadorTest` | 2 |
| | `SolicitudAcademicaCUImplAdaptadorTest` | 42 |
| | `TipoAnexoAcademicoCUImplAdaptadorTest` | 3 |
| | `TipoSolicitudAcademicaCUImplAdaptadorTest` | 2 |
| | `TramiteCancelacionAsignaturaCUImplAdaptadorTest` | 46 |
| | `TramiteCancelacionMatriculaCUImplAdaptadorTest` | 41 |
| | `TramiteExamenSupletorioCUImplAdaptadorTest` | 14 |
| `dominio/servicios` | `MaquinaEtapasTest` | 19 |
| | `ReglasAsignaturaTest` | 3 |
| | `TramiteCancelacionTest` | 19 |
| | `TramiteSolicitudTest` | 20 |
| | `ValidadorActorSolicitudTest` | 10 |
| | `ValidadorArchivoAdjuntoTest` | 17 |
| `infraestructura/configuracion/lectorArchivos` | `AgrupadorEstudiantesExcelTest` | 10 |
| | `CatalogoAcademicoExcelTest` | 10 |
| | `DuplicadosFuncionariosAcademicosExcelTest` | 7 |
| | `almacenador/AlmacenadorArchivosTest` | 5 |
| `infraestructura/input/controlador*` | `MapperCancelacionAsignaturaInfraestructuraDominioTest` | 7 |
| | `MapperCancelacionMatriculaInfraestructuraDominioTest` | 6 |
| | `MapperExamenSupletorioInfraestructuraDominioTest` | 6 |
| | `MapperSolicitudAcademicaInfraestructuraDominioTest` | 10 |
| `infraestructura/output/**` | `AlmacenamientoAnexosImplAdaptadorTest` | 9 |
| | `SolicitudAcademicaGatewayImplAdaptadorTest` | 8 |
| Total | 38 archivos | 615 |

(Rutas verificadas con `find` para los mappers (`infraestructura/input/controlador*/`), `AlmacenadorArchivosTest` (`.../lectorArchivos/almacenador/`), `AlmacenamientoAnexosImplAdaptadorTest` (`infraestructura/output/almacenamiento/`) y `SolicitudAcademicaGatewayImplAdaptadorTest` (`infraestructura/output/persistencia/gateway/`); los paquetes de casos de uso y servicios siguen la descripción de `backend/CLAUDE.md`.)

### 1.3 Qué cubren

- **Casos de uso** (21 archivos `*CUImplAdaptadorTest`, más `Tramite*`): camino feliz y cada fallo de regla de negocio, con gateways simulados con Mockito; comparan el texto exacto de `MensajesError`. Incluyen radicación, trámite por etapas, anexos, Resolución escaneada, catálogo de asignaturas (consecutivo `ISI-001`, no reutilización, 40 creaciones simultáneas contra un gateway falso con bloqueo), estudiantes, funcionarios académicos.
- **Servicios de dominio**: máquina de etapas (`MaquinaEtapasTest`), actor y pertenencia (`ValidadorActorSolicitudTest`, `TramiteSolicitudTest`), validación de archivos adjuntos (`ValidadorArchivoAdjuntoTest`), reglas de cancelación (`TramiteCancelacionTest`).
- **Infraestructura**: lectura de Excel (`CatalogoAcademicoExcelTest`, `AgrupadorEstudiantesExcelTest`, `DuplicadosFuncionariosAcademicosExcelTest`), almacenamiento de archivos con `@TempDir` (`AlmacenadorArchivosTest`, `AlmacenamientoAnexosImplAdaptadorTest`), mapeadores de DTO a dominio, un gateway de persistencia.
- **No cubierto** (no se encontró): controladores REST con `MockMvc`/`@WebMvcTest`, reglas de `ConfiguracionSeguridad`, filtro JWT, repositorios con base de datos real (`@DataJpaTest`), manejador `RestApiExcepcion`. Las rutas y los permisos se prueban solo con los scripts de humo (sección 3).

### 1.4 Cómo se ejecutan

Desde `backend/solicitudes` (`backend/CLAUDE.md`, sección "Pruebas"): `.\mvnw.cmd test`. Las 37 clases de prueba unitaria no necesitan base de datos. `SolicitudesApplicationTests` levanta el contexto completo y exige base de datos arriba y estas variables en la sesión: `SERVER_PORT`, `DB_URL`, `DB_USER_NAME`, `DB_PASSWORD` (porque `application.properties` las usa sin valor por defecto); la documentación sugiere `SERVER_PORT=8081`, MySQL en el puerto 3307 del host y credenciales `<REDACTADO>`. Maven Wrapper en `backend/solicitudes/.mvn/wrapper/maven-wrapper.properties`; no hace falta instalar Maven. No se encontró carpeta `target/surefire-reports` (0 archivos), es decir, no hay informes de una ejecución previa en el árbol.

## 2. Pruebas automatizadas del frontend

Ubicación: `frontend/src/app/**`.

| Medida | Valor | Cómo se obtuvo |
|---|---|---|
| Archivos `.spec.ts` | 136 | `find src -name "*.spec.ts"` |
| Casos `it(` | 524 | `grep -rhE "\bit\(" src --include=*.spec.ts` (mismo resultado con `^\s*it\(`) |
| Bloques `describe(` | 159 | grep |
| `xit` / `fit` / `xdescribe` / `fdescribe` | 0 | grep |
| Specs que usan `TestBed` | 126 | grep |
| Specs que usan `HttpTestingController` | 19 | grep |
| Marco | Jasmine (`@types/jasmine` en `frontend/package.json`) + Karma (`angular.json`, target `test`, builder `@angular/build:karma`, polyfills `zone.js` y `zone.js/testing`, `tsconfig.spec.json`) | lectura |

Origen: en `d3a48cc` había 87 archivos `.spec.ts` y 88 casos `it(` [BASE] (`git ls-tree` / `git grep`); por diferencia, 49 specs y 436 casos son [EXTENSIÓN] (los specs de Julián pueden haber sido modificados; el cálculo es por conteo, no por archivo). Karma es el predeterminado del esqueleto Angular. Por nombre, son claramente de la extensión: servicios `cancelacion-matricula-service`, `examen-supletorio-service`, `catalogo-academico-service`, `funcionario-academico-service`, `asignatura-service`; utilidades en `core/utils`; constantes académicas; componentes `*-proceso-component`, `est-*-content-component`, `admin-*-content-component`, `bandeja-solicitudes-academicas-component`, `detalle-solicitud-academica-component`, `carga-excel-academica-component` y `app-rutas-academicas.spec.ts`.

Specs con más casos: `cancelacion-asignatura-proceso-component` (50), `detalle-solicitud-academica-component` (33), `cancelacion-matricula-proceso-component` (33), `bandeja-solicitudes-academicas-component` (29), `examen-supletorio-proceso-component` (25), `admin-estudiantes-content-component` (24).

Qué cubren: componentes de pantalla (la mayor parte), servicios HTTP (con `HttpTestingController`), interceptor (`auth-interceptor.spec.ts`, 6 casos), guards (`inicio-guard.spec.ts`), utilidades de formato y validación. No hay pruebas end-to-end (no se encontró Cypress, Playwright ni Protractor en `package.json`).

Ejecución (`frontend/CLAUDE.md`, líneas 660-664): `npx ng test --watch=false --browsers=ChromeHeadless`, o con `--include=src/app/shared/pages/content/<carpeta>` para un subconjunto. `frontend/README.md` documenta `ng test`. Requiere Chrome instalado. Resultados de pasa/falla: NO VERIFICADO.

## 3. Scripts de humo del backend (`backend/pruebas/`)

[EXTENSIÓN] (la carpeta no existe en `d3a48cc`). Son 22 archivos: 14 scripts de pruebas (13 `tN_*.ps1` y `matriz-permisos.ps1`) listados en `todo.ps1`, `comun.ps1`, 3 generadores de plantilla Excel (`generar_plantilla_*.ps1`) y 3 plantillas `.xlsx`. Total aproximado 4490 líneas de PowerShell.

| Script | Qué prueba | Llamadas a `Escribir-Resultado` |
|---|---|---|
| `t1_lectura_usuarios` | restricción de listados de usuarios | 7 |
| `t1_preauthorize` | comportamiento de `@PreAuthorize` | 1 |
| `t2_asignaturas` | catálogo de asignaturas | 48 |
| `t3_estudiantes` | CRUD de estudiantes y materias | 58 |
| `t3_excel` | carga masiva de estudiantes | 32 |
| `t4_funcionarios_academicos` | funcionarios académicos | 38 |
| `t4_excel` | carga masiva de funcionarios | 20 |
| `t5_catalogos` | catálogos académicos | 20 |
| `t5_asignacion` | asignación de funcionario a tipo | 20 |
| `t5_solicitudes` | radicación y consulta genérica | 58 |
| `t6_cancelacion_matricula` | proceso de matrícula | 69 |
| `t7_cancelacion_asignatura` | proceso de asignatura | 105 |
| `t8_examen_supletorio` | proceso de supletorio | 88 |
| `matriz-permisos` | matriz rol por endpoint | 1 (dentro de ciclos) |

Los números son llamadas en el código fuente; el número real de verificaciones por ejecución puede ser mayor (bucles en `matriz-permisos`) y no se ejecutó nada.

Funcionamiento: `comun.ps1` define `Invoke-Api`, `Iniciar-Sesion`, `Crear-UsuarioPrueba`, `Obtener-CodigoError`, `Subir-Archivo`, `Nuevo-ArchivoXlsx`, `Consultar-Base`, `Limpiar-DatosPrueba` y `Escribir-Resultado` (imprime `PASS`/`FAIL`); la URL base está fija en `http://localhost:8080/api/unicauca/fiet/consejo/`. Usa el usuario inicial `rootfiet` con la contraseña `<REDACTADO>` y consulta la base con `docker compose exec -T cfiet_database mysql -u root -p<REDACTADO> ... cfiet`, por lo que **requieren el entorno Docker del `docker-compose.yml`**. `todo.ps1` ejecuta cada script en un proceso `powershell` aparte, toma una "foto" antes y después (conteo de filas de todas las tablas excepto `logs`, responsable de cada tipo de solicitud, contenido de `uploads/`) y marca diferencias como fallo, de modo que las pruebas no deben dejar rastros. Ejecución: desde `backend`, `powershell -File .\pruebas\todo.ps1` o un script individual.

Colección Postman: no existe (`find` de `*postman*` sin resultados). La documentación de la API es el contrato Markdown `docs/api/contrato-api-procesos-academicos.md` y Swagger (`springdoc-openapi-starter-webmvc-ui` 2.8.10 en `pom.xml`; ruta por defecto de springdoc, NO VERIFICADO en ejecución).

## 4. Docker y orquestación

### 4.1 Dockerfile del backend (`backend/Dockerfile`) [BASE, sin diferencias con `d3a48cc`]

- Imagen `eclipse-temurin:17-jdk-jammy` (una sola etapa, sin multi-stage).
- `WORKDIR /app`, `ENV UPLOADS_PATH=/app/uploads`, `RUN mkdir -p /app/uploads`.
- Copia un jar ya compilado (`ARG JAR_FILE=solicitudes/target/solicitudes-0.0.1.jar`, copiado como `app_cfiet.jar`): hay que generar el jar antes con `./mvnw clean install -DskipTests`, el Dockerfile no compila.
- `EXPOSE 8080`, `ENTRYPOINT ["java","-jar","app_cfiet.jar"]`. Sin usuario no root, sin `HEALTHCHECK`.
- El `.dockerignore` raíz del backend existe (contenido no leído). El jar existe en el árbol (`backend/solicitudes/target/solicitudes-0.0.1.jar`).

### 4.2 Dockerfile del frontend (`frontend/dockerfile`)

- Multi-etapa con `node:20-alpine`: etapa `build` (`npm install`, `npm run build`) y etapa final (copia `dist` y `package*.json`, `npm install --omit=dev`).
- `EXPOSE 4200`, `CMD ["node","dist/front-fiet-sc/server/server.mjs"]`: sirve el SSR de Angular (Express, `frontend/src/server.ts`).
- El frontend **no** está en el `docker-compose.yml`; el README indica `npm start` para desarrollo.
- `frontend/.dockerignore` excluye `node_modules`, `.angular`, `dist`, `.git`.
- La URL del backend está fija en `frontend/src/enviroments/environment.ts`: `apiUrl: 'http://localhost:8080/api/unicauca/fiet/consejo'` (la carpeta se llama `enviroments`, con ese error ortográfico).

### 4.3 `backend/docker-compose.yml` [BASE; `git diff d3a48cc` del archivo, del Dockerfile y de `application.properties` sale vacío]

Formato `version: '3'`. Dos servicios:

| Aspecto | `solicitudes-consejo-de-facultad` (backend) | `cfiet_database` (MySQL) |
|---|---|---|
| Origen de la imagen | `build: .` (el Dockerfile de arriba) | `mysql:8.0.33` |
| Puertos | `8080:8080` | `3307:3306` |
| Variables de entorno | `SERVER_PORT=8080`; `DB_URL=jdbc:mysql://cfiet_database:3306/cfiet?createDatabaseIfNotExist=true&serverTimezone=UTC`; `DB_USER_NAME=root`; `DB_PASSWORD=<REDACTADO>`; `UPLOADS_PATH=/app/uploads` | `MYSQL_ROOT_PASSWORD=<REDACTADO>`; `MYSQL_PASSWORD=<REDACTADO>`; `MYSQL_DATABASE=cfiet_database` |
| Volúmenes | bind mount `./uploads:/app/uploads` (archivos subidos, carpeta `backend/uploads`, ignorada por git salvo un `README`) | ninguno declarado: los datos de MySQL quedan en el volumen anónimo que crea la imagen; no hay volumen con nombre |
| `depends_on` | `cfiet_database` con `condition: service_healthy` | no aplica |
| Healthcheck | no tiene | `test: ["CMD","mysqladmin","ping","-h","localhost"]`, `timeout: 10s`, `retries: 10` (sin `interval`, queda el valor por defecto de Compose, 30 s; no se declaró `start_period`) |
| Límite de memoria | `mem_limit: 512m` | ninguno |
| `restart` | `always` | `always` |

Observaciones leídas del archivo:

- Inconsistencia de nombres: `MYSQL_DATABASE=cfiet_database` crea esa base, pero el backend usa `.../cfiet?createDatabaseIfNotExist=true` y los scripts consultan la base `cfiet`; la base efectiva del sistema es `cfiet`, creada por el propio driver; `cfiet_database` queda como base vacía adicional (inferencia; NO VERIFICADO sin levantar el contenedor).
- `MYSQL_PASSWORD` se define sin `MYSQL_USER`; la imagen oficial solo usa `MYSQL_PASSWORD` junto con `MYSQL_USER` (NO VERIFICADO el efecto; probablemente se ignora).
- 512 MB para una JVM con Hibernate y Apache POI es ajustado; no hay `-Xmx` en el `ENTRYPOINT` (JVM 17 calcula el heap a partir del límite del contenedor, un 25 % por defecto: NO VERIFICADO el comportamiento real).
- El mapeo `3307:3306` expone MySQL con la contraseña conocida al host (ver hallazgo H9 en `06-seguridad-y-errores.md`).
- Docker no estaba corriendo en esta sesión; nada de lo anterior se ejecutó.

## 5. Configuración y variables de entorno

### 5.1 Archivos de configuración

Solo existe **un** archivo: `backend/solicitudes/src/main/resources/application.properties` [BASE] (no hay `application.yml`, `application-*.properties` ni perfiles Spring; `grep` de `spring.profiles` y `@Profile` no se ejecutó, pero `src/main/resources` contiene únicamente `application.properties`, `data.sql` y `templates`). Frontend: `frontend/src/enviroments/environment.ts` (un solo archivo, sin variantes por entorno en el árbol visto).

| Propiedad | Valor | Origen |
|---|---|---|
| `spring.application.name` | `solicitudes-consejo-de-facultad` | [BASE] |
| `url.application` | `/api/unicauca/fiet/consejo/` | [BASE] |
| `url.backend` | `http://localhost:8080` | [BASE] |
| `url.frontend` | `http://localhost:4200` (usada por CORS) | [BASE] |
| `server.port` | `${SERVER_PORT}` (obligatoria, sin valor por defecto) | [BASE] |
| `spring.datasource.driver-class-name` | `com.mysql.cj.jdbc.Driver` | [BASE] |
| `spring.datasource.url` / `username` / `password` | `${DB_URL}` / `${DB_USER_NAME}` / `${DB_PASSWORD}` (obligatorias) | [BASE] |
| `spring.jpa.hibernate.ddl-auto` | `update` | [BASE] |
| `spring.jpa.show-sql` | `true` | [BASE] |
| `spring.sql.init.mode` | `never` | [BASE] |
| `spring.jpa.defer-datasource-initialization` | `true` | [BASE] |
| `spring.jpa.hibernate.naming.physical-strategy` | `PhysicalNamingStrategyStandardImpl` (nombres de tabla tal cual) | [BASE] |
| `app.uploads.base-path` | `${UPLOADS_PATH:/app/uploads}` | [BASE] (presente en `d3a48cc`) |
| `spring.servlet.multipart.max-file-size` / `max-request-size` | `20MB` / `20MB` | [BASE] (presente en `d3a48cc`) |

### 5.2 Variables de entorno

| Variable | Obligatoria | Uso | Valor en el compose |
|---|---|---|---|
| `SERVER_PORT` | sí | `server.port` | `8080` |
| `DB_URL` | sí | URL JDBC de MySQL | `jdbc:mysql://cfiet_database:3306/cfiet?createDatabaseIfNotExist=true&serverTimezone=UTC` |
| `DB_USER_NAME` | sí | usuario de base | `root` |
| `DB_PASSWORD` | sí | contraseña de base | `<REDACTADO>` |
| `UPLOADS_PATH` | no (por defecto `/app/uploads`) | carpeta de archivos subidos; en la imagen se declara con `ENV` | `/app/uploads` |

Sin las cuatro primeras el contexto Spring no arranca (el placeholder no se resuelve), incluida `SolicitudesApplicationTests`. La clave del JWT **no** es configurable: está fija en el código (`JwtServicio.SECRET_KEY`, `<REDACTADO>`).

### 5.3 Datos iniciales y esquema

- Las tablas las crea Hibernate al arrancar (`ddl-auto=update`); nada se siembra solo (`spring.sql.init.mode=never`).
- Siembra manual, en este orden (README raíz): `backend/solicitudes/src/main/resources/data.sql` (roles, tipos de usuario y el usuario `rootfiet`; contiene 7 sentencias `INSERT`), crear un Funcionario Académico por API y correr `docs/database/seed-procesos-academicos.sql` con `@funcionario_uuid`. Archivos auxiliares en `docs/database/`: `seed-roles-extension.sql`, `migracion_programas_asignaturas.sql`, `migracion_cargo_situaciones_notas.sql`, `script_bd_extension.sql` (DDL de referencia, no se ejecuta). En la raíz hay un `limpieza_local.sql` ignorado por git.
- `.gitignore` excluye `backend/**/target/`, `backend/uploads/*` (salvo `README`), `frontend/node_modules/`, `dist/`, `.angular/`, `.claude/settings.local.json`, `limpieza_local.sql`.

## 6. Cómo correr el proyecto (según `README.md` raíz)

**Opción A, con Docker** (requiere Docker Desktop y JDK 17 solo para compilar el jar):

1. `cd backend/solicitudes && ./mvnw clean install -DskipTests`.
2. `cd .. && docker compose up --build` (backend en `http://localhost:8080`, MySQL en `localhost:3307`).
3. Sembrar la base (sección 5.3).
4. Frontend: `cd frontend && npm install && npm start` (`http://localhost:4200`). Requiere Node 20.19+ o 22.12+ (Angular 20).

**Opción B, sin Docker**: JDK 17, MySQL 8 local con una base creada, Node 20.19+; definir `SERVER_PORT`, `DB_URL`, `DB_USER_NAME`, `DB_PASSWORD` (y opcionalmente `UPLOADS_PATH`) para el MySQL local.

Scripts de `package.json` del frontend: `ng`, `start` (`ng serve`), `build`, `watch`, `test` (`ng test`), `serve:ssr:front-fiet-sc`. Dependencias principales: Angular 20.2, PrimeNG 20.1, `@angular/ssr` 20.2, Bootstrap 5.3, SweetAlert2 11.23, Express 5.1.

## 7. Desajustes entre documentación y árbol

- El `README.md` raíz dice en "Estado del proyecto": "El frontend todavía no consume los endpoints de los procesos académicos", pero el árbol contiene servicios y pantallas de los tres procesos (`cancelacion-matricula-service`, `examen-supletorio-service`, `*-proceso-component`) y los últimos commits son `feat(frontend)` de esos procesos. La frase del README está desactualizada.
- `README.md` y `CLAUDE.md` mencionan el "contrato de los 66 endpoints académicos": no se contó en este análisis (NO VERIFICADO).
- `backend/CLAUDE.md` declara 1233 pruebas; el conteo estático da 696 métodos de prueba (615 + 81 parametrizados). Ver sección 1.1.
- La propia documentación señala que los tests de `SolicitudesApplicationTests` fallan sin base y sin variables; no existe perfil de pruebas con base en memoria.

## 8. Cifras clave

- Backend: 38 archivos de test, 615 `@Test`, 81 `@ParameterizedTest`, 696 métodos; 1233 invocaciones declaradas en `backend/CLAUDE.md` (NO VERIFICADO).
- Frontend: 136 `.spec.ts` (87 [BASE] + 49 [EXTENSIÓN]), 524 `it(` (88 [BASE]), 159 `describe(`; Jasmine + Karma, sin e2e.
- Scripts de humo: 14 scripts de prueba más soporte en `backend/pruebas`; sin colección Postman.
- Docker: 2 Dockerfiles (backend de una etapa con jar precompilado, frontend multi-etapa), 1 `docker-compose.yml` con 2 servicios (backend con `mem_limit: 512m`, `restart: always`, `depends_on` con `service_healthy`; MySQL 8.0.33 con healthcheck `mysqladmin ping`).
- Configuración: 1 `application.properties`, 0 perfiles, 5 variables de entorno (4 obligatorias).
