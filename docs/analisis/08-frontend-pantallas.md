# 08 - Catálogo de pantallas y diálogos del frontend

Alcance: todas las rutas de `frontend/src/app/app.routes.ts` y los diálogos que abre cada una. Análisis de solo lectura. Rutas de archivo relativas a `frontend/src/app/` salvo indicación. Etiquetas: [BASE] = prototipo de Julián (commit `d3a48cc`); [EXTENSIÓN] = trabajo de grado; NO VERIFICADO = no comprobado por lectura estática (no se ejecutó la aplicación).

## 1. Cifras

| Concepto | Total | [BASE] | [EXTENSIÓN] |
| --- | --- | --- | --- |
| Rutas con pantalla (sin la raíz `''`) | 34 | 17 | 17 |
| Pantallas con layout completo (`app-page-component`) | 32 | 16 | 16 |
| Pantallas con layout simple (`app-page-simple-component`) | 2 (`login`, `usuario-publico/solicitudes`) | 2 | 0 |
| Instancias de diálogo genérico en plantillas (`app-generic-dialog-form/info/steps-form/upload-file-component`) | 39 | 28 | 11 |
| Diálogo propio con `p-dialog` directo (`DialogGestionRespuestaComponent`) | 1 | 1 | 0 |
| Componentes totales | 145 | 87 | 58 |
| Specs (`*.spec.ts`) | 136 (524 `it(`) | 87 | 49 |

Pantallas por rol (rutas): Secretario General 9 (8 [BASE] + 1 [EXTENSIÓN]), Funcionario 3, Secretaria Decanatura FIET 2, Usuario FIET 2, Público 2 (incluye `login`), Estudiante 7, Funcionario Académico 4, Decano 5. Suma 34.

Nota sobre los diálogos: un mismo `app-generic-dialog-form-component` de los tres componentes de proceso (`*-proceso-component`) sirve para todas las acciones; cambia `title`, `textoBoton` y el contenido con `@if`. Por eso el recuento de instancias (39) es menor que el de diálogos visibles para el usuario (ver secciones 5 a 8).

## 2. Cómo se decide qué botones aparecen

Resumen por familia de pantalla, verificado en el código:

| Familia | Mecanismo | Referencia |
| --- | --- | --- |
| Acceso a la ruta | `AuthGuard` + `RoleGuard` con `data.roles` (no se calculan botones aquí) | `app.routes.ts` |
| Menú lateral | Cada rol tiene su `side-bar-*-component` con una lista fija de ítems; el rol decide el menú porque la página de ese rol monta su barra | `core/<rol>/components/side-bar-*/*.html` |
| Procesos académicos (los tres) | Los botones de acción salen de `solicitud.accionesDisponibles` (lista de códigos que entrega el backend), filtrada por las acciones que maneja cada proceso (`ACCIONES_DEL_PROCESO`). El cliente nunca deduce las acciones del rol | `*-proceso-component.ts`, getter `accionesVisibles` |
| Excepción dentro de los procesos | En `CancelacionAsignaturaProcesoComponent.accionesVisibles`, si `rol === ESTUDIANTE` devuelve `[]` antes de filtrar | `cancelacion-asignatura-proceso-component.ts` línea ~243 |
| Texto, avisos y colores | Funcionario y Decano: dependen de `etapaCodigo`. Estudiante: solo de `etiqueta` y `accionesDisponibles`, nunca de `etapaCodigo` (D-F23) | `detalle-solicitud-academica-component.ts`; `examen-supletorio-proceso-component.ts` (`avisoEtapa`, `enEtapa`) |
| Descarga de la Resolución | Botón visible si `solicitud.puedeDescargarResolucion` | `detalle-solicitud-academica-component.html` (`data-boton-resolucion`) |
| Escaneo de la Resolución | El campo de carga del escaneo aparece solo cuando `accionActiva` es `RECHAZAR_FUNCIONARIO` o `ENVIAR_RESPUESTA` en Cancelación de Matrícula y Cancelación de Asignatura. Supletorio no lo tiene | `*-proceso-component.html` bloque `data-campo-resolucion` |
| Modo editable de la tabla de asignaturas | `editable` es verdadero si `accionesVisibles` incluye `REMITIR_DECANO` (el Funcionario evalúa por fila) | `cancelacion-*-proceso-component.ts`, getter `editable` |
| Decisión por fila (Decano, Cancelación de Asignatura) | `decide` es verdadero si `accionesVisibles` incluye `APROBAR_DECANO`; entonces se muestran "Aceptar tal como está" y "Enviar decisión modificada" en lugar de un único botón de aprobación | `cancelacion-asignatura-proceso-component.html` (`data-accion="ACEPTAR_EVALUACION"` y `"DECISION_MODIFICADA"`) |
| Bandeja del Funcionario | `modo` (`solicitudes` o `respuestas`) decide filas y columnas; `filtraPorCodigo` solo para el Funcionario | `bandeja-solicitudes-academicas-component.ts` |
| Administración académica | Sin filtrado por rol dentro del componente: el mismo `AdminAcademicaComponent` se monta en `decano/administracion-academica` y en `sec-general/administracion-academica`; la restricción es la de la ruta | `app.routes.ts` |
| Pantallas de Julián | Botones fijos por pantalla (editar, ver, y los de tarjeta definidos en `buttonsCard` dentro de `ngOnInit`). No hay lógica por estado de la entidad en los botones, salvo habilitaciones puntuales de diálogo NO VERIFICADO | `shared/pages/content/*-content-component` |

Lista completa de acciones y su código: `ACCIONES_ACADEMICAS` en `core/constantes/procesos-academicos.ts` (`RADICAR`, `RECHAZAR_FUNCIONARIO`, `REMITIR_DECANO`, `APROBAR_DECANO`, `RECHAZAR_DECANO`, `ENVIAR_RESPUESTA`, `ENVIAR_RECIBO`, `SUBIR_COMPROBANTE`, `APROBAR_COMPROBANTE`, `RECHAZAR_COMPROBANTE`).

Acciones manejadas por proceso (constante `ACCIONES_DEL_PROCESO` en cada componente de proceso):

| Proceso | Acciones |
| --- | --- |
| Cancelación de Matrícula | RECHAZAR_FUNCIONARIO, REMITIR_DECANO, APROBAR_DECANO, RECHAZAR_DECANO, ENVIAR_RESPUESTA |
| Cancelación de Asignatura | RECHAZAR_FUNCIONARIO, REMITIR_DECANO, APROBAR_DECANO, RECHAZAR_DECANO, ENVIAR_RESPUESTA |
| Examen Supletorio | las cinco anteriores más ENVIAR_RECIBO, SUBIR_COMPROBANTE, APROBAR_COMPROBANTE, RECHAZAR_COMPROBANTE |

## 3. Elementos comunes a todas las pantallas con layout completo

| Elemento | Qué muestra | Referencia |
| --- | --- | --- |
| Cabecera | `HeaderMainComponent`: título "Gestión de Procesos Académicos FIET" y `AvatarSesionComponent` con iniciales, nombre y rol (`roles[0].nombre`) | `shared/headers/header-main-component` |
| Menú de avatar | "Mi usuario" (navega a `rutaDeInicio` de los roles del usuario) y "Cerrar sesión" (`AuthService.logout()` y navegación a `/login`) | `shared/avatar/avatar-sesion-component` |
| Barra lateral | Ítems por rol (sección 4) | `SidebarComponent` |
| Migas | `BreadcrumbComponent` con `role` y `breadcrumbs` fijados en el HTML de cada página | `core/<rol>/pages/*/*.html` |
| Título de contenido | `ContentComponent` con `title` fijo en cada envoltorio | `shared/pages/*-component` |
| Pie | `FooterSimpleComponent` | `PageComponent` |

## 4. Menús laterales por rol

| Rol | Componente | Ítems (ruta) | Origen |
| --- | --- | --- | --- |
| Secretario General | `SideBarSecretarioGeneralComponent` | Mi Usuario (`/sec-general`), Usuarios, Roles, Solicitudes, Orden del Día, Respuestas, Configuración (`/sec-general/tipos/solicitudes`), Administración académica, Historial (`/sec-general/logs`) | [BASE]; ítem "Administración académica" [EXTENSIÓN] |
| Funcionario | `SideBarFuncionarioComponent` | Mi Usuario (`/funcionario`), Solicitudes, Respuestas | [BASE] |
| Secretaria Decanatura FIET | `SideBarSecretariaFietComponent` | Mi Usuario (`/sec-fiet`), Respuestas | [BASE] |
| Usuario FIET | `SideBarUsuarioFietComponent` | Mi Usuario (`/usuario-fiet`), Solicitudes | [BASE] |
| Estudiante | `SideBarEstudianteComponent` | Mi Usuario (`/estudiante`), Solicitudes (`/estudiante/catalogo`, con `activaEn` para los tres formularios), Mis solicitudes (`/estudiante/solicitudes`) | [EXTENSIÓN] |
| Funcionario Académico | `SideBarFuncionarioAcademicoComponent` | Mi Usuario (`/funcionario-academico`); grupo "Gestión de solicitudes" con Solicitudes y Respuestas | [EXTENSIÓN] |
| Decano | `SideBarDecanoComponent` | Mi Usuario (`/decano`); grupo "Gestión académica" con Solicitudes académicas y Administración académica; Solicitudes del Consejo (`/decano/solicitudes-consejo`) | [EXTENSIÓN] |

Los submenús usan `BotonSidebar.children`; el ítem activo es la ruta más específica que coincide con la URL (`SidebarComponent.rutaActiva`).

## 5. Estudiante [EXTENSIÓN] (rol 'Estudiante')

Todas las rutas con `AuthGuard` + `RoleGuard`, roles `[ROLES_ACADEMICOS.ESTUDIANTE]`. Cadena de componentes: página en `core/estudiante/pages/...` -> envoltorio `shared/pages/est-*-component` -> contenido `shared/pages/content/est-*-content-component` (o componente común).

| Ruta | Página -> envoltorio (título) -> contenido | Qué muestra | Acciones | Endpoints |
| --- | --- | --- | --- | --- |
| `estudiante` | `EstudianteHomeComponent` -> `InfoUsuarioComponent` ("Información de Usuario") -> `InfoUsuarioContentComponent` | Tarjeta "Bienvenid@", iniciales, nombres, apellidos, documento, teléfono, usuario, correo, roles; incluye `app-change-password-component` | Cambiar contraseña | `PATCH /usuarios/{uuid}` (`UsuariosService.cambiarContraseña`); datos desde `AuthService.usuario$` (sin llamada adicional para este rol) |
| `estudiante/catalogo` | `EstudianteCatalogoComponent` -> `EstCatalogoSolicitudesComponent` ("Solicitudes") -> `EstCatalogoSolicitudesContentComponent` | Tres tarjetas (`TRAMITES_ESTUDIANTE` en `core/constantes/catalogo-estudiante.ts`): descripción, requisitos, anexos requeridos con enlace "Descargar formato" (`EnlaceFormatoComponent`) | "Iniciar solicitud" (navegación con `router.navigateByUrl` a la ruta del trámite) | Ninguno (contenido estático) |
| `estudiante/cancelacion-matricula` | `EstudianteCancelacionMatriculaComponent` -> `EstCancelacionMatriculaComponent` ("Cancelación de Matrícula") -> `EstCancelacionMatriculaContentComponent` | Formulario de una página con stepper (`StepperFormularioComponent`): Datos del estudiante (`DatosEstudianteSesionComponent`), Asignaturas que se cancelan, Motivo, Anexos obligatorios, Soportes opcionales y advertencia, Normativa aplicable; pantalla de éxito con radicado | Radicar solicitud, Volver (a Mis solicitudes), Ver solicitud (tras radicar), Quitar soporte, Reintentar | `GET /cancelaciones-matricula/formulario` (`getFormulario`, en `ngOnInit`/`cargarFormulario`); `POST /cancelaciones-matricula` (`radicar`, multipart) |
| `estudiante/cancelacion-asignatura` | `EstudianteCancelacionAsignaturaComponent` -> `EstCancelacionAsignaturaComponent` ("Cancelación de Asignatura") -> `EstCancelacionAsignaturaContentComponent` | Igual patrón: Datos del estudiante, Asignaturas que quieres cancelar, Motivo, Soportes opcionales; stepper de 4 pasos | Radicar solicitud, Volver, Ver solicitud, Quitar soporte, Reintentar | `GET /cancelaciones-asignatura/formulario`; `POST /cancelaciones-asignatura` |
| `estudiante/examen-supletorio` | `EstudianteExamenSupletorioComponent` -> `EstExamenSupletorioComponent` ("Examen Supletorio") -> `EstExamenSupletorioContentComponent` | Datos del estudiante; Examen no presentado (asignatura, fecha, causa); sección "Examen con el que se cruzó" (`data-seccion-cruce`) que aparece solo cuando la causa es cruce; Anexos con enlace al formato (`EnlaceFormatoComponent`); stepper con pasos Datos, Examen no presentado, Examen cruzado (solo cruce) y Anexos | Radicar solicitud, Volver, Ver solicitud, Reintentar | `GET /examenes-supletorios/formulario`; `POST /examenes-supletorios` |
| `estudiante/solicitudes` | `EstudianteSolicitudesComponent` -> `EstSolicitudesComponent` ("Mis solicitudes") -> `EstSolicitudesContentComponent` -> `BandejaSolicitudesAcademicasComponent rol="ESTUDIANTE"` | Tarjeta "Mis trámites radicados": filtros Tipo de proceso y Estado, "Limpiar filtros", tabla con Radicado, Tipo de proceso, Fecha de radicación, Estado (ícono y texto por `categoriaEstado(etiqueta)`) y acciones, paginación de 10 | Ver solicitud (ícono de ojo), ordenar por fecha o estado, limpiar filtros, Reintentar | `GET /solicitudes-academicas/estudiante` |
| `estudiante/solicitudes/:uuid` | `EstudianteDetalleSolicitudComponent` -> `EstDetalleSolicitudComponent` ("Detalle de la solicitud") -> `EstDetalleSolicitudContentComponent` -> `DetalleSolicitudAcademicaComponent rol="ESTUDIANTE"` | "Solicitud de {tipo}", radicado, insignia de estado; Datos generales; anexos; "Mensaje para el estudiante" si existe; componente del proceso; historial sin etapa ni comentarios | Volver, descargar Resolución (si `puedeDescargarResolucion`), Visualizar/Descargar anexos; acciones del proceso (ver 5.1) | `GET /solicitudes-academicas/{uuid}`, `GET .../{uuid}/historial`; proceso: `GET /<proceso>/{uuid}`; anexos: `GET .../{uuid}/anexos/{uuidAnexo}`; Resolución: `GET .../{uuid}/resolucion` |

### 5.1 Acciones del Estudiante dentro del detalle

- Cancelación de Matrícula y Cancelación de Asignatura: sin botones de acción. `CancelacionAsignaturaProcesoComponent.accionesVisibles` devuelve `[]` para el Estudiante; en Matrícula, `accionesDisponibles` del Estudiante no contiene acciones del proceso, por lo que el filtro deja la lista vacía (NO VERIFICADO con datos reales del backend). En Asignatura el Estudiante ve solo código y nombre de las asignaturas antes de la etapa final, y con "Aprobada"/"Rechazada" la decisión por asignatura sin observación (D-F23 y notas de `CLAUDE.md`; tabla `asignaturasTabla` y getter `esFinal`).
- Examen Supletorio: botón "Subir comprobante de pago" cuando `accionesDisponibles` incluye `SUBIR_COMPROBANTE`. Abre el diálogo "Subir el comprobante de pago" (`textoBoton` "Subir comprobante"), con `InputAnexoUploadComponent` tipado por el tipo de anexo "Comprobante de pago" (se busca por nombre en `GET /catalogos-academicos/tipos-solicitud/{uuidTipo}/tipos-anexo`, con `ANEXOS_PAGO_SUPLETORIO`). Al confirmar: primero `POST /solicitudes-academicas/{uuid}/anexos` y después `POST /examenes-supletorios/{uuid}/estudiante/comprobante`. Si la acción falla tras subir el archivo, el reintento no vuelve a subirlo (`anexoPagoSubido`). Avisos según etiqueta: pendiente de pago, en verificación y comprobante rechazado ("debes radicar una solicitud nueva").

## 6. Funcionario Académico [EXTENSIÓN] (rol 'Funcionario Académico')

| Ruta | Página -> envoltorio (título) -> contenido | Qué muestra | Acciones | Endpoints |
| --- | --- | --- | --- | --- |
| `funcionario-academico` | `FuncionarioAcademicoHomeComponent` -> `InfoUsuarioComponent` | Igual que Mi Usuario del Estudiante, más cargo y lista "Procesos académicos asignados" (`data-seccion="procesos-asignados"`) | Cambiar contraseña | `PATCH /usuarios/{uuid}`; `GET /funcionarios-academicos/{uuid}` (`InfoUsuarioContentComponent.cargarFuncionarioAcademico`, solo si el usuario tiene el rol) |
| `funcionario-academico/solicitudes` | `FuncionarioAcademicoSolicitudesComponent` -> `FunAcadSolicitudesComponent` ("Solicitudes académicas") -> `FunAcadSolicitudesContentComponent` -> `BandejaSolicitudesAcademicasComponent rol="FUNCIONARIO" modo="solicitudes"` | Tarjeta "Gestión de solicitudes": solo las no finalizadas (`etapaCodigo` fuera de APROBADA y RECHAZADA). Filtros Tipo, Estado, Número de identificación y Código estudiantil. Columnas Radicado, Estudiante, Documento, Código, Tipo de proceso, Fecha, Estado | Ver solicitud, ordenar, limpiar filtros | `GET /solicitudes-academicas/funcionario` |
| `funcionario-academico/respuestas` | `FuncionarioAcademicoRespuestasComponent` -> mismo contenido, `data.modo = 'respuestas'` | Tarjeta "Solicitudes respondidas": solo finalizadas; el filtro de estado ofrece Aprobada/Rechazada; una Cancelación de Asignatura da hasta dos filas (aprobadas/rechazadas) según `resultadosAsignaturas` (D-F39) | Ver solicitud (agrega query param `PARAMETRO_RESULTADO` en las filas por resultado) | `GET /solicitudes-academicas/funcionario` (mismo endpoint, filtrado en el cliente por `delModo`) |
| `funcionario-academico/solicitudes/:uuid` | `FuncionarioAcademicoDetalleSolicitudComponent` -> `FunAcadDetalleSolicitudComponent` ("Detalle de la solicitud") -> `DetalleSolicitudAcademicaComponent rol="FUNCIONARIO"` | Detalle completo: datos generales con Número de CC, anexos, "Observación para el Decano" (última `REMITIR_DECANO`), "Decisión del Decano" (última `APROBAR_DECANO` o `RECHAZAR_DECANO` con su observación), "Mensaje para el estudiante", componente del proceso, historial con insignia de etapa. El enlace "Volver" va a Respuestas si la solicitud es final (`rutaBandeja`) | Ver 6.1 | Ver 6.1 |

### 6.1 Acciones del Funcionario Académico en el detalle

| Acción (botón) | Proceso | Diálogo (título / `textoBoton`) | Contenido del diálogo | Endpoint |
| --- | --- | --- | --- | --- |
| Rechazar solicitud (`RECHAZAR_FUNCIONARIO`) | Matrícula | "Rechazar la solicitud" / "Rechazar solicitud" | Resumen (`ResumenSolicitudComponent`), carga del escaneo de la Resolución (PDF, obligatorio salvo que ya haya uno), observación obligatoria "Observación solo para el estudiante (se entrega como respuesta)" | `POST /solicitudes-academicas/{uuid}/resolucion` y luego `POST /cancelaciones-matricula/{uuid}/funcionario/rechazar` |
| Rechazar solicitud | Asignatura | igual | igual | `POST .../resolucion` y `POST /cancelaciones-asignatura/{uuid}/funcionario/rechazar` |
| Rechazar solicitud | Supletorio | igual sin escaneo | resumen y observación obligatoria | `POST /examenes-supletorios/{uuid}/funcionario/rechazar` |
| Remitir al Decano (`REMITIR_DECANO`) | Matrícula y Asignatura | "Remitir la solicitud al Decano" / "Remitir al Decano" | Antes: la tabla de asignaturas pasa a modo editable (No. faltas, Nota, situación en la matrícula, situación al cancelar; en Asignatura además "Cumple condiciones"); se validan filas con los mensajes en celda. En el diálogo: resumen y "Observaciones para el Decano" (opcional, hasta 500). Al abrir carga las situaciones del catálogo y no abre si el catálogo falla o viene vacío | `GET /catalogos-academicos/situaciones` (antes de abrir); `POST /cancelaciones-*/{uuid}/funcionario/remitir` con `{observacion, evaluaciones}` |
| Remitir al Decano | Supletorio | igual, sin tabla | resumen y observación opcional (sin casilla de requisitos, D-F42) | `POST /examenes-supletorios/{uuid}/funcionario/remitir` con `{observacion}` |
| Enviar respuesta (`ENVIAR_RESPUESTA`) | Matrícula | "Enviar la respuesta al estudiante" / "Enviar respuesta" | Escaneo de la Resolución firmada (obligatorio si no existe), observación opcional | `POST .../resolucion` y `POST /cancelaciones-matricula/{uuid}/funcionario/responder` |
| Enviar respuesta | Asignatura | igual | Escaneo, observación (obligatoria si `respuestaExigeObservacion`), y `ajustes` de situación para las aprobadas sin situación guardada o cambiadas por el Decano (D-F37) | `POST .../resolucion` y `POST /cancelaciones-asignatura/{uuid}/funcionario/responder` (cuerpo con `ajustes` solo si hay) |
| Enviar respuesta | Supletorio | igual sin escaneo | observación opcional | `POST /examenes-supletorios/{uuid}/funcionario/responder` |
| Enviar recibo de pago (`ENVIAR_RECIBO`) | Supletorio | "Enviar el recibo de pago" / "Enviar recibo" | Carga del anexo "Recibo de pago" (obligatoria si no está cargado) | `POST /solicitudes-academicas/{uuid}/anexos` y `POST /examenes-supletorios/{uuid}/funcionario/recibo` |
| Aprobar comprobante (`APROBAR_COMPROBANTE`) | Supletorio | "Aprobar el comprobante de pago" / "Aprobar comprobante" | Fecha acordada opcional (`app-input-date-component`); no puede ser anterior a la fecha del examen no presentado | `POST /examenes-supletorios/{uuid}/funcionario/comprobante/aprobar` |
| Rechazar comprobante (`RECHAZAR_COMPROBANTE`) | Supletorio | "Rechazar el comprobante de pago" / "Rechazar comprobante" | Observación obligatoria | `POST /examenes-supletorios/{uuid}/funcionario/comprobante/rechazar` |

Todos los envíos bloquean el doble clic con la bandera `enviando` (D-F7) y, al terminar, llaman `recargar()` y muestran un toast de éxito (`showSuccess`). Los errores 500 con `codigoError` y los 400 con mapa de campos se pintan en el diálogo (`data-error="servidor"` y bajo cada campo).

## 7. Decano [EXTENSIÓN] (rol 'Decano')

| Ruta | Página -> envoltorio (título) -> contenido | Qué muestra | Acciones | Endpoints |
| --- | --- | --- | --- | --- |
| `decano` | `DecanoHomeComponent` -> `InfoUsuarioComponent` | Mi Usuario (igual que el Estudiante; no carga datos de Funcionario Académico porque el rol no coincide) | Cambiar contraseña | `PATCH /usuarios/{uuid}` |
| `decano/solicitudes` | `DecanoSolicitudesComponent` -> `DecSolicitudesComponent` ("Solicitudes académicas") -> `DecSolicitudesContentComponent` -> `BandejaSolicitudesAcademicasComponent rol="DECANO"` | Tarjeta "Solicitudes remitidas por el Funcionario Académico"; columna "Decisión" en lugar de "Estado"; sin filtro de código | Ver solicitud, ordenar, limpiar filtros | `GET /solicitudes-academicas/decano` |
| `decano/solicitudes/:uuid` | `DecanoDetalleSolicitudComponent` -> `DecDetalleSolicitudComponent` ("Detalle de la solicitud") -> `DetalleSolicitudAcademicaComponent rol="DECANO"` | Igual que el Funcionario Académico (mismo componente con `rol="DECANO"`); ve "Observación para el Decano" y "Decisión del Decano" | Ver 7.1 | Ver 7.1 |
| `decano/solicitudes-consejo` | `DecanoSolicitudesConsejoComponent` -> `UsuarioFietSolicitudesPrivadasComponent` ("Solicitudes") -> `UsuarioFietSolicitudesPrivadasContentComponent` | Pantalla de Julián reutilizada: tarjeta "Envío de Solicitudes" con tabla de tipos de solicitud del perfil del usuario, búsqueda por nombre | Ver información del tipo (ojo), botón de tarjeta "campana" que abre el formulario de envío (`EnviarSolicitudUsuarioFietComponent`) | `GET /tipos/solicitudes` filtrados por perfil (`getTiposSolicitudPorPerfilSolicitante`, `getTiposSolicitudPorNombreYPerfilSolicitante`, `getTipoSolicitud`); envío: ver sección 9 |
| `decano/administracion-academica` | `DecanoAdministracionAcademicaComponent` -> `AdminAcademicaComponent` ("Administración académica") -> `AdminAcademicaContentComponent` | Cuatro pestañas (ver sección 10) | Ver sección 10 | Ver sección 10 |

### 7.1 Acciones del Decano en el detalle

| Acción (botón) | Proceso | Diálogo (título / `textoBoton`) | Contenido | Endpoint |
| --- | --- | --- | --- | --- |
| Aprobar (`APROBAR_DECANO`) | Matrícula | "Aprobar la cancelación de matrícula" / "Aprobar solicitud" | Resumen, nota `NOTA_APROBAR_DECANO`, selección de la situación al cancelar por asignatura (obligatoria), observación opcional con el aviso "Esta observación solo será visible para el Funcionario Académico." | `POST /cancelaciones-matricula/{uuid}/decano/aprobar` con `{situaciones, observacion}` |
| Rechazar (`RECHAZAR_DECANO`) | Matrícula | "Rechazar la cancelación de matrícula" / "Rechazar solicitud" | Resumen y observación opcional | `POST /cancelaciones-matricula/{uuid}/decano/rechazar` |
| Aprobar | Asignatura | Botones "Aceptar tal como está" (`aceptarEvaluacion`, solo sin cambios) y "Enviar decisión modificada" (`decisiones`, solo con cambios). Cada fila tiene un interruptor Aprobar/Rechazar que parte de `cumpleCondiciones` (D-F35) | Títulos desde `TITULOS_DECISION` y textos desde `TEXTOS_BOTON_DECISION`; observación opcional | `POST /cancelaciones-asignatura/{uuid}/decano/aprobar` con `armarAprobacion` |
| Rechazar solicitud completa (`RECHAZAR_DECANO`) | Asignatura | "Rechazar la solicitud completa" / "Rechazar solicitud" | `abrirDecision(modo)` abre este diálogo cuando el modo es `modificar` y todas las filas quedaron rechazadas (`todasRechazadas`); "Aceptar tal como está" se deshabilita si hay cambios (`hayCambiosDecano`) y "Enviar decisión modificada" se deshabilita si no los hay | `POST /cancelaciones-asignatura/{uuid}/decano/rechazar` |
| Aprobar / Rechazar | Supletorio | "Aprobar el examen supletorio" / "Rechazar el examen supletorio" | Resumen y observación opcional (`NOTA_APROBAR_DECANO` al aprobar) | `POST /examenes-supletorios/{uuid}/decano/aprobar` (cuerpo `null` si no hay observación) y `.../decano/rechazar` |

La tabla de asignaturas del Decano marca las filas cuya decisión difiere de la del Funcionario (`decisionDistinta`) y las atenúa si están rechazadas (`esAtenuada`).

## 8. Secretario General [BASE salvo la última fila] (rol 'Secretario General')

Cadena: `core/secretario-general/pages/...` -> envoltorio `shared/pages/...-component` -> contenido `shared/pages/content/...`. Las acciones de fila usan `app-button-component` con íconos `edit.svg` (editar), `eye.svg` (ver información) y otros; los botones de tarjeta se definen en `buttonsCard`.

| Ruta | Página -> envoltorio (título) -> contenido | Qué muestra | Acciones (diálogos) | Endpoints (servicio.método) |
| --- | --- | --- | --- | --- |
| `sec-general` | `SecretarioGeneralHomeComponent` -> `InfoUsuarioComponent` ("Información de Usuario") | Mi Usuario | Cambiar contraseña | `PATCH /usuarios/{uuid}` |
| `sec-general/usuarios` | `SecretarioGeneralUsuariosComponent` -> `UsuariosComponent` ("Usuarios") -> `UsuariosContentComponent` | Tarjeta "Gestión de Usuarios": tabla con búsqueda por nombre y estado, paginación | Botón de tarjeta Crear usuario (`add-user.svg`, diálogo "Crear Usuario"); botón Cargar archivo (`upload.svg`, diálogo "Subir archivo" con `GenericDialogUploadFileComponent`); por fila: editar ("Actualizar Usuario") y ver ("Información de Usuario") | `UsuariosService`: `getUsuariosPaginado`, `getUsuariosFiltrados`, `getUsuario`, `crearUsuario` (POST `?tipoUsuario=`), `crearUsuariosDesdeArchivo`, `actualizarUsuario`, `getTiposUsuario`; `RolesService.getRoles` |
| `sec-general/roles` | `SecretarioGeneralRolesComponent` -> `RolesComponent` ("Roles") -> `RolesContentComponent` | Tarjeta "Gestión de Roles": nombre, descripción y estado, con botones de ordenar (`ordenar.svg`) | Por fila: editar ("Actualizar Rol") y ver ("Información del Rol") | `RolesService.getRoles`, `actualizarRol` |
| `sec-general/solicitudes` | `SecretarioGeneralSolicitudes` -> `SecSolicitudesComponent` ("Gestión de Solicitudes") -> `SecContentComponent` | Tarjeta "Gestión de Solicitudes": Solicitud, Responsable, Estado; búsqueda por nombre | Por fila: editar ("Actualizar Solicitud": consecutivo, nombre, descripción, estado y funcionario responsable) y ver ("Información de Solicitud", con `AnexosViewComponent`) | `SolicitudesService.getSolicitudesPaginado`, `buscarSolicitudesPorNombre`, `getSolicitud`, `actualizarSolicitud`; `UsuariosService.getFuncionarios` |
| `sec-general/orden-del-dia` | `SecretarioGeneralOrdelDelDia` -> `OrdenDelDiaComponent` ("Gestión de Orden Del Día") -> `OrdenDelDiaContentComponent` | Tarjeta "Gestión Orden del Día": acta y estado | Botón de tarjeta Crear (`add.svg`, diálogo "Crear Orden del Día": nombre, número de acta, descripción, ciudad, fecha, hora inicio, hora fin, lugar); por fila: editar ("Actualizar Orden del Día"), ver ("Información Orden del Día") y abrir (`folder.svg`, abre `OrdenDelDiaVisualizadorComponent`) | `SolicitudesService.getOrdenesDelDiaPaginado`, `buscarOrdenesDelDia`, `getOrdenDelDia`, `crearOrdenDelDia`, `actualizarOrdenDelDia` |
| (diálogo del anterior) | `OrdenDelDiaVisualizadorComponent` (`core/secretario-general/components/orden-del-dia-visualizador-component`) | Dos diálogos: "Información de Solicitud" y "Asignar Respuesta"; agrupa solicitudes de una orden | Descargar anexos, exportar orden (variantes: simple, con respuestas, merge para reunión), asignar respuesta a una solicitud | `SolicitudesService.getSolicitudesPorOrdenDelDia`, `getSolicitudesPorEstado`, `getSolicitud`, `actualizarSolicitud`, `descargarAnexosOrdenDelDia`, `exportarOrdenDelDia`, `exportarOrdenDelDiaConRespuestas`, `exportarOrdenDelDiaMerge`; `RespuestasService.getRespuestaPorSolicitud` |
| `sec-general/respuestas` | `SecretarioGeneralRespuestasComponent` -> `SecRespuestasComponent` ("Gestión de Respuestas") -> `SecRespuestasContentComponent` | Tarjeta "Gestión de Respuestas": solicitud y estado | Por fila: ver ("Información de la Respuesta", con `DescargarRespuestaComponent`) y editar (abre `DialogGestionRespuestaComponent`, con `CargarRespuestaComponent`) | `RespuestasService.getRespuestas`, `getRespuestasPorNombreSolicitud`, `getRespuesta`, `responderSolicitud`, `eliminarArchivoRespuesta` |
| `sec-general/tipos/solicitudes` | `SecretarioGeneralTipoSolicitudesComponent` -> `TipoSolicitudesComponent` ("Configuración de Tipos de Solicitudes") -> `TipoSolicitudesContentComponent` | Tarjeta "Gestión de Tipos de Solicitudes": solicitud y funcionario; búsquedas por tipo y por funcionario | Botones de tarjeta: Crear (diálogo de pasos "Crear Tipo de Solicitud": Paso 1 información, Paso 2 anexos) y Cargar archivo; por fila: editar (pasos "Actualizar Tipo de Solicitud"), ver ("Información del Tipo de Solicitud") y asignar funcionario (`employee.svg`, "Asignar Funcionario") | `TipoSolicitudService` (lista, filtros, `crearTipoSolicitud`, creación desde archivo, `actualizarTipoSolicitud`, `getTipoSolicitud`); `RolesService.getRoles`; `UsuariosService.getFuncionarios` |
| `sec-general/logs` | `SecretarioGeneralLogComponent` -> `LogsComponent` ("Historial") -> `LogContentComponent` | Tabla Responsable, Fecha, Acción, Resultado; búsqueda por responsable y por fecha | Solo consulta y paginación | `LogService.getLogsPaginado`, `getLogsFiltrados` |
| `sec-general/administracion-academica` [EXTENSIÓN] | `SecretarioGeneralAdministracionAcademicaComponent` -> `AdminAcademicaComponent` | Igual que la del Decano | Ver sección 10 | Ver sección 10 |

El Secretario General no tiene una pantalla de solicitudes académicas (Matrícula/Asignatura/Supletorio): su única entrada a lo académico es la administración. Verificado: no existe ruta `sec-general/solicitudes-academicas`.

## 9. Funcionario (Julián), Secretaria Decanatura FIET, Usuario FIET y solicitante público [BASE]

| Ruta | Rol | Página -> envoltorio (título) -> contenido | Qué muestra | Acciones | Endpoints |
| --- | --- | --- | --- | --- | --- |
| `funcionario` | Funcionario | `FuncionarioHomeComponent` -> `InfoUsuarioComponent` | Mi Usuario | Cambiar contraseña | `PATCH /usuarios/{uuid}` |
| `funcionario/solicitudes` | Funcionario | `FuncionarioSolicitudes` -> `FunSolicitudesComponent` ("Gestión de Solicitudes") -> `FunSolicitudesContentComponent` | Tarjeta "Gestión de Solicitudes": solicitud y estado; búsqueda por nombre | Botón de tarjeta "campana" que abre `EnviarSolicitudUsuarioPublicoComponent` (diálogo "Enviar Solicitud Pública": Paso 1 Información del Solicitante, Paso 2 Información de la Solicitud); por fila: editar ("Actualizar Solicitud") y ver ("Información de Solicitud") | `SolicitudesService.getSolicitudesPorFuncionario` (GET `/funcionario/{uuid}`), `buscarSolicitudesPorNombreYFuncionario`, `getSolicitud`, `actualizarSolicitud`, `enviarSolicitudPublica` (POST `/solicitudes/public`); `TipoSolicitudService.getTiposSolicitudPorPerfil('Solicitante Publico')` |
| `funcionario/respuestas` | Funcionario | `FuncionarioRespuestasComponent` -> `FunRespuestasComponent` ("Gestión de Respuestas") -> `FunRespuestasContentComponent` | Respuestas de las solicitudes del funcionario | Ver ("Información de la Respuesta"), gestionar respuesta (`DialogGestionRespuestaComponent`) | `RespuestasService.getRespuestasPorFuncionario`, `getRespuestasPorFuncionarioYNombreSolicitud`, `getRespuesta` |
| `sec-fiet` | Secretaria Decanatura FIET | `SecretariaFietHomeComponent` -> `InfoUsuarioComponent` | Mi Usuario | Cambiar contraseña | `PATCH /usuarios/{uuid}` |
| `sec-fiet/respuestas` | Secretaria Decanatura FIET | `SecretariaFietRespuestasComponent` -> `SecFietRespuestasComponent` ("Gestión de Respuestas") -> `SecFietRespuestasContentComponent` | Respuestas (solo lectura) | Ver información y descargar archivo de la respuesta; no hay botón de edición | `RespuestasService.getRespuestas`, `getRespuestasPorNombreSolicitud`, `getRespuesta` |
| `usuario-fiet` | Coordinador Pregrado, Coordinador Posgrados, Jefe de Departamento, Decano, Docente | `UsuarioFietHomeComponent` -> `InfoUsuarioComponent` | Mi Usuario | Cambiar contraseña | `PATCH /usuarios/{uuid}` |
| `usuario-fiet/solicitudes` | los cinco roles anteriores | `UsuariofietSolicitudesComponent` -> `UsuarioFietSolicitudesPrivadasComponent` ("Solicitudes") -> `UsuarioFietSolicitudesPrivadasContentComponent` | Tarjeta "Envío de Solicitudes": tipos de solicitud disponibles para el perfil del usuario, con búsqueda | Ver información del tipo; botón de tarjeta "campana" que abre `EnviarSolicitudUsuarioFietComponent` (diálogo de pasos "Enviar Solicitud") | `TipoSolicitudService` (por perfil, por nombre y perfil, por uuid); `SolicitudesService.enviarSolicitud` (POST `/solicitudes`, multipart) y `getOrdenesDelDiaPorEstado`; perfil tomado de `AuthService` |
| `usuario-publico/solicitudes` | Solicitante público (sin sesión, sin guard) | `UsuarioPublicoSolicitudesComponent` (layout simple) -> `UsuarioPublicoSolicitudesContentComponent` | Tarjeta "Solicitudes Disponibles" con tipos de solicitud para el perfil público y búsqueda | Ver información del tipo (diálogo "Información del Tipo de Solicitud"); esta pantalla no contiene un botón ni el formulario de envío (el componente `EnviarSolicitudUsuarioPublicoComponent` solo se monta en la pantalla del Funcionario) | `TipoSolicitudService.getTiposSolicitudPorPerfilSolicitante`, `getTiposSolicitudPorNombreYPerfilSolicitante`, `getTipoSolicitud` |
| `login` | cualquiera | `LoginComponent` (layout simple) -> `LoginFormComponent` | Formulario con usuario y contraseña (`InputPasswordComponent`) | Iniciar sesión (Enter envía; bandera `enviando`, D-F21). Redirige con `rutaDeInicio`; si no hay ruta muestra el error "El usuario no tiene un rol con vistas definidas en el sistema." | `POST /sesiones`, luego `GET /usuarios/{uuid}` |

Notas:

- "Solicitante público" y "privado" en el sentido de Julián: el público no se autentica y solo consulta tipos de solicitud; el solicitante "privado" es el Usuario FIET autenticado, que sí envía solicitudes al Consejo. La clasificación proviene de los nombres de las pantallas (`usuario-publico`, `usuario-fiet-solicitudes-privadas`) y del perfil `'Solicitante Publico'` pasado al servicio; la lista exacta de perfiles que acepta el backend NO VERIFICADO desde el frontend.
- El Decano, además de las pantallas de procesos académicos, tiene acceso a `usuario-fiet` y `usuario-fiet/solicitudes` por URL directa (ver 07, sección 3.1), y su entrada de menú "Solicitudes del Consejo" reutiliza el mismo contenido.

## 10. Administración académica [EXTENSIÓN] (Decano y Secretario General)

Una sola pantalla con cuatro pestañas (`AdminAcademicaContentComponent.secciones`, cambio de pestaña con `elegir`, sin rutas hijas): Asignaturas, Estudiantes, Funcionarios académicos, Responsable por proceso. Rutas: `decano/administracion-academica` y `sec-general/administracion-academica`.

| Pestaña | Componente | Qué muestra | Acciones y diálogos | Endpoints |
| --- | --- | --- | --- | --- |
| Asignaturas | `AdminAsignaturasContentComponent` | Filtros por programa y texto ("Buscar por código o nombre"), tabla paginada con código, nombre y programa; estados cargando, vacío, error | "Nueva asignatura" (diálogo con programa y nombre; el código lo genera el backend, D-F16); "Editar" (solo nombre); "Cargar Excel" (`CargaExcelAcademicaComponent`); "Buscar" | `AsignaturaService.getAsignaturasFiltradas`, `crearAsignatura`, `actualizarAsignatura`, `crearAsignaturasDesdeArchivo`; `CatalogoAcademicoService.getProgramas` |
| Estudiantes | `AdminEstudiantesContentComponent` | Filtros de nombre, apellido, código; tabla; la facultad se muestra de solo lectura (FIET) | "Nuevo estudiante" (diálogo con `DatosUsuarioAcademicoComponent`, código estudiantil, semestre, grupo, programa primero y asignaturas habilitadas al elegir programa, D-F17); "Editar estudiante" (envía solo lo cambiado, D-F24; el programa no se edita, D-F18); "Asignaturas de {nombre}" (agregar asignatura, cambiar estado de matrícula); "Cargar Excel" | `EstudianteService.getEstudiantesFiltrados`, `crearEstudiante`, `actualizarEstudiante`, `crearEstudiantesDesdeArchivo`, `agregarAsignaturaMatriculada`, `cambiarEstadoAsignatura`; `AsignaturaService.getAsignaturasPorPrograma`; `CatalogoAcademicoService.getProgramas` |
| Funcionarios académicos | `AdminFuncionariosContentComponent` | Filtros de nombre, apellido y cargo; tabla; los procesos a cargo se ven de solo lectura al editar (D-F25) | "Nuevo funcionario académico", "Editar funcionario académico", "Cargar Excel" | `FuncionarioAcademicoService.getFuncionariosAcademicosFiltrados`, `crearFuncionarioAcademico`, `actualizarFuncionarioAcademico`, `crearFuncionariosAcademicosDesdeArchivo` |
| Responsable por proceso | `AdminResponsablesContentComponent` | Tabla de los tipos de solicitud académica con su funcionario responsable | "Cambiar responsable" y "Nuevo responsable" (diálogo "Responsable de {tipo}" con select de funcionario) | `CatalogoAcademicoService.getTiposSolicitud`, `asignarFuncionarioAcademico`; `FuncionarioAcademicoService.getFuncionariosAcademicosPaginado` |

La carga por Excel (`CargaExcelAcademicaComponent`) muestra reglas del archivo, ejecuta `cargar` (función que cada pestaña le pasa) y, tras éxito, muestra solo el resultado y el botón pasa a "Cerrar" (D-F22). Los errores se listan dentro del diálogo mediante `mensajesDeError`.

Restricción de permisos por rol dentro de esta pantalla: ninguna (ambos roles ven y hacen lo mismo en el cliente). La diferencia de permisos, si existe, la impone el backend. NO VERIFICADO desde el frontend.

## 11. Componentes de proceso: estructura interna común [EXTENSIÓN]

Los tres componentes (`CancelacionMatriculaProcesoComponent`, `CancelacionAsignaturaProcesoComponent`, `ExamenSupletorioProcesoComponent`) comparten:

- Entradas: `uuidSolicitud`, `solicitud`, `rol`, `recargar`, todas obligatorias.
- Carga: `ngOnChanges` pide `GET /<proceso>/{uuid}` (`getDetalle`) y muestra estados `cargando`, `errorCarga` (con "Reintentar").
- Tarjeta de acciones `data-acciones` con un `SimpleButtonComponent` por acción visible (`[attr.data-accion]`), texto desde `etiquetaBoton`.
- Un solo `GenericDialogFormComponent` con `title` y `textoBoton` por acción, `textoCancelar="Cancelar"` y `cerrarConEscape` (D-F31), `ResumenSolicitudComponent` y los campos de cada acción.
- Validación local previa (`validar(accion)`), mensajes bajo cada campo y bloque `data-error="servidor"` para errores del backend.
- Cancelaciones: tabla propia de asignaturas con encabezado agrupado (`agruparEncabezados`, `RUTAS_ENCABEZADO_ASIGNATURAS`), estilo `tabla-asignaturas.css`, nota `NOTA_SIMCA` (`muestraNotaSimca`), columnas sin datos ocultas, scroll horizontal bajo 992 px.
- Supletorio: tarjeta de datos (fecha del examen, causa, fecha acordada si existe, bloque de cruce si `detalle.cruce`) y avisos de etapa (`avisoEtapa`).

## 12. Qué cambia entre roles en la misma pantalla de detalle

| Elemento del detalle | Estudiante | Funcionario Académico | Decano |
| --- | --- | --- | --- |
| "Número de CC" en Datos generales | no | sí | sí |
| Insignia de etapa en el historial | no | sí | sí |
| "Observación para el Decano" (`REMITIR_DECANO`) | no (`observacionRemision` devuelve `null` para el Estudiante) | sí | sí |
| "Decisión del Decano" (`decisionDecano`) | no (solo FUNCIONARIO y DECANO) | sí | sí |
| "Mensaje para el estudiante" (`mensajeEstudiante`) | sí, si existe | sí, si existe | sí, si existe |
| Etiqueta de estado | `ETIQUETAS_ESTUDIANTE` | etiqueta propia del backend para el rol | etiqueta propia del backend para el rol |
| "Volver" | `/estudiante/solicitudes` | `/funcionario-academico/solicitudes`, o `/funcionario-academico/respuestas` si la etapa es final | `/decano/solicitudes` |
| Botones de acción | los que reciba en `accionesDisponibles` (en Supletorio, "Subir comprobante de pago"; en Cancelaciones ninguno) | los que reciba en `accionesDisponibles` | los que reciba en `accionesDisponibles` |

Fuente: `detalle-solicitud-academica-component.ts` (`datosGenerales`, `observacionRemision`, `decisionDecano`, `rutaBandeja`) y su HTML (`@if (!esEstudiante)` en historial).

## 13. Observaciones y discrepancias para la monografía

- La pantalla pública `usuario-publico/solicitudes` solo lista tipos; el formulario público de envío vive en la pantalla del Funcionario (rol 'Funcionario' de Julián) y no en la pública. [BASE]
- Hay dos barras de menú con ítem "Solicitudes" para el Estudiante y Funcionario Académico con destinos distintos: en el Estudiante "Solicitudes" es el catálogo (`/estudiante/catalogo`) y "Mis solicitudes" la bandeja; en el Funcionario Académico "Solicitudes" es la bandeja de no finalizadas y "Respuestas" la de finalizadas.
- `funcionario-academico/respuestas` reutiliza el título de envoltorio "Solicitudes académicas" (el de `FunAcadSolicitudesComponent`), mientras la tarjeta interna dice "Solicitudes respondidas".
- La decisión de qué botones ven los roles académicos depende por completo del arreglo `accionesDisponibles` que devuelve el backend; si el backend no lo devuelve o lo devuelve vacío, ninguna acción se muestra. Se verificó el consumo, no el contenido real del backend.
- Los textos de los menús de Julián del Secretario General incluyen el prefijo `| ` en cada etiqueta (por ejemplo `'| Usuarios'`); `SidebarComponent.texto()` lo elimina al pintar.
- Total de diálogos por familia: Julián 28 instancias (más el `p-dialog` de gestión de respuesta), extensión 11 instancias.
