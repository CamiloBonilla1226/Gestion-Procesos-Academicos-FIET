# 04. Backend: casos de uso (`*CUImplAdaptador`)

Análisis de solo lectura del módulo `backend/solicitudes`. Estado analizado: árbol de trabajo en la rama `main` con cambios sin confirmar (entre ellos la eliminación de `requisitosVerificados` en la remisión del supletorio, decisión P49, visible en `git status`). Lo que no se pudo comprobar en el código se marca NO VERIFICADO.

## 0. Convenciones del documento

- Ruta base de los casos de uso: `backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/dominio/casosdeuso/`. En adelante, `CU/`.
- Otras rutas base: `dominio/servicios/` (reglas de dominio), `dominio/helper/constantes/`, `aplicacion/output/` (puertos de salida), `infraestructura/configuracion/BeanConfiguracion.java`, `infraestructura/output/manejadorExcepciones/MensajesError.java`.
- Marca de origen: [BASE] es código del prototipo de Julián Camacho; [EXTENSIÓN] es código agregado en este trabajo de grado. El criterio es verificable con git: los 8 casos de uso [BASE] aparecen por primera vez en el commit inicial `d3a48cc` y no tienen más commits (`git log --oneline -- <archivo>` devuelve una sola línea). Los 22 [EXTENSIÓN] nacen en commits posteriores (2026-10-05 a 2026-10-11). El repositorio no conserva el historial original de Julián (el primer commit ya es del trabajo de grado), de modo que "BASE" significa "presente en el commit inicial y nunca modificado", no autoría demostrada por git. NO VERIFICADO: la autoría de Julián sobre cada archivo base (se apoya en `CLAUDE.md` y en los `@author` de Javadoc).
- Cómo cuento las "operaciones": cada método público anotado `@Override` en el `*CUImplAdaptador` (implementación de un método del puerto `*CUIntPuerto`). Los métodos privados no se cuentan.
- Códigos de error: `ExcepcionesFormateadorIntPuerto` (implementado por `infraestructura/output/formateador/ExcepcionesFormateadorImplAdaptador.java`) lanza una excepción propia por tipo. `CodigoError` (`infraestructura/output/manejadorExcepciones/estructura/CodigoError.java`) los numera: 1 genérico (`lanzarErrorGenerico`), 2 entidad existe (`lanzarEntidadExiste`), 3 entidad no existe (`lanzarEntidadNoExiste`), 4 regla de negocio violada (`lanzarReglaNegocioViolada`), 5 credenciales (`lanzarCredencialesErroneas`), 6 mal formato (`lanzarMalFormato`), 7 sin información (`lanzarSinInformacion`), 8 sin acceso (`lanzarSinAcceso`), 9 token expirado. En `RestApiExcepcion` todos los handlers de excepciones propias responden HTTP 500 (solo `AuthenticationException` y `AccessDeniedException` dan 403, y la validación de DTO da 400); el contrato `docs/api/contrato-api-procesos-academicos.md` lo documenta igual (línea 38 y 42). En las tablas de este documento, "E3", "E4", "E6" etc. significan código de error 3, 4, 6.

## 1. Resumen cuantitativo

| Concepto | Valor |
|---|---|
| Casos de uso (`*CUImplAdaptador`) | 30 |
| [BASE] | 8 (62 operaciones) |
| [EXTENSIÓN] | 22 (76 operaciones) |
| Operaciones totales (`@Override`) | 138 |
| Casos de uso que escriben en el log de Julián | 20 de 30: 13 de la extensión y 7 base (ver sección 12) |
| Casos de uso con `@Transactional` propio | 0 (verificado: `grep Transactional dominio/` sin resultados) |
| Métodos `@Bean` de caso de uso en `BeanConfiguracion` | 30 (27 `crear<X>CU` y 3 `create<X>CU` de Julián: `createRolCU`, `createUsuarioCU`, `createSesionCU`) |

Conteo por caso de uso:

| Caso de uso | Origen | Operaciones |
|---|---|---|
| `AnexoAcademicoCUImplAdaptador` | EXTENSIÓN | 4 |
| `AsignacionFuncionarioAcademicoCUImplAdaptador` | EXTENSIÓN | 1 |
| `AsignaturaCUImplAdaptador` | EXTENSIÓN | 7 |
| `CancelacionAsignaturaCUImplAdaptador` | EXTENSIÓN | 1 |
| `CancelacionMatriculaCUImplAdaptador` | EXTENSIÓN | 1 |
| `ConsultaCancelacionAsignaturaCUImplAdaptador` | EXTENSIÓN | 3 |
| `ConsultaCancelacionMatriculaCUImplAdaptador` | EXTENSIÓN | 3 |
| `ConsultaExamenSupletorioCUImplAdaptador` | EXTENSIÓN | 3 |
| `ConsultaSolicitudAcademicaCUImplAdaptador` | EXTENSIÓN | 7 |
| `EstudianteCUImplAdaptador` | EXTENSIÓN | 9 |
| `EtapaSolicitudAcademicaCUImplAdaptador` | EXTENSIÓN | 2 |
| `ExamenSupletorioCUImplAdaptador` | EXTENSIÓN | 1 |
| `FuncionarioAcademicoCUImplAdaptador` | EXTENSIÓN | 6 |
| `ProgramaAcademicoCUImplAdaptador` | EXTENSIÓN | 1 |
| `ResolucionAcademicaCUImplAdaptador` | EXTENSIÓN | 2 |
| `SituacionAcademicaAsignaturaCUImplAdaptador` | EXTENSIÓN | 1 |
| `SolicitudAcademicaCUImplAdaptador` | EXTENSIÓN | 3 |
| `TipoAnexoAcademicoCUImplAdaptador` | EXTENSIÓN | 1 |
| `TipoSolicitudAcademicaCUImplAdaptador` | EXTENSIÓN | 1 |
| `TramiteCancelacionAsignaturaCUImplAdaptador` | EXTENSIÓN | 5 |
| `TramiteCancelacionMatriculaCUImplAdaptador` | EXTENSIÓN | 5 |
| `TramiteExamenSupletorioCUImplAdaptador` | EXTENSIÓN | 9 |
| `LogCUImplAdaptador` | BASE | 5 |
| `OrdenDelDiaCUImplAdaptador` | BASE | 10 |
| `RespuestaCUImplAdaptador` | BASE | 9 |
| `RolCUImplAdaptador` | BASE | 4 |
| `SesionCUImplAdaptador` | BASE | 1 |
| `SolicitudCUImplAdaptador` | BASE | 13 |
| `TipoSolicitudCUImplAdaptador` | BASE | 10 |
| `UsuarioCUImplAdaptador` | BASE | 10 |

## 2. Transaccionalidad (aplica a todos los casos de uso)

1. Ningún `*CUImplAdaptador` lleva `@Transactional` (de hecho no son `@Component`: se instancian con `new` en `BeanConfiguracion`). El límite transaccional lo ponen los controladores.
2. Controladores de la extensión (`infraestructura/input/controlador*/controlador/*RestController.java`): todos sus métodos llevan `@Transactional`, con `readOnly = true` en los GET. Ejemplo verificado: `ExamenSupletorioRestController` (12 anotaciones, incluido `getFormulario` y `getDetalle` con `readOnly = true`), `CancelacionMatriculaRestController` y `CancelacionAsignaturaRestController` (8 cada uno), `SolicitudAcademicaRestController` (9).
3. Controladores [BASE] (`SolicitudesRestController`, `RespuestaRestController`, `RolRestController`, `TiposSolicitudRestController`, `UsuarioRestController`): `@Transactional` solo en métodos de escritura (por ejemplo `crearOrdenDelDia`, `enviarSolicitud`, `registrarRespuesta`, `actualizarRol`), no en lecturas. Es una diferencia de criterio entre base y extensión.
4. Los gateways de la extensión también anotan sus métodos (`SolicitudAcademicaGatewayImplAdaptador.crear` y `actualizarEtapa` con `@Transactional`; lecturas con `readOnly = true`). Como el controlador ya abrió transacción, se unen a ella (propagación por defecto REQUIRED; esta propagación es comportamiento estándar de Spring, no verificada en configuración propia).
5. Archivos: `AlmacenamientoAnexosImplAdaptador` (`infraestructura/output/almacenamiento/`) usa `TransactionSynchronizationManager`: borra el archivo recién escrito si la transacción se revierte (`afterCompletion`) y borra el archivo reemplazado solo tras el commit (`afterCommit`, vía `eliminarTrasConfirmar`). Los casos de uso `AnexoAcademicoCU` y `ResolucionAcademicaCU` dependen de este comportamiento.
6. Consecuencia para los casos de uso: una radicación o acción de trámite ejecuta varios pasos (solicitud, historial, especialización, filas de asignaturas, anexos, logs, archivos) y todo se confirma o se revierte junto. Si falla una escritura de log (por ejemplo `LogCUImplAdaptador.crearLog` lanza E3 o E1), la excepción sube y el controlador revierte todo.
7. Orden "validar, luego escribir": los casos de uso de la extensión validan antes de escribir (verificado en las cuatro radicaciones y en los trámites). Excepción [BASE]: `UsuarioCUImplAdaptador.crearUsuario`, `SolicitudCUImplAdaptador.crearSolicitud`, `RespuestaCUImplAdaptador.registrarRespuesta` y `OrdenDelDiaCUImplAdaptador.crearOrdenDelDia` escriben el log ANTES de guardar la entidad; los de la extensión lo escriben después.

## 3. Servicios de dominio que los casos de uso usan (no son casos de uso)

Todos en `dominio/servicios/`, sin Spring ni JPA, creados con `new` dentro de los casos de uso. [EXTENSIÓN].

| Clase | Qué hace | Excepciones que lanza |
|---|---|---|
| `MaquinaEtapas` | Tabla `TRANSICIONES` (13 transiciones), `RESPONSABLES` (8 etapas). Métodos: `siguienteEtapa`, `transicionar` (además comprueba lo exigido), `accionesDisponibles`, `etapaDestino`, `responsableActual`, `esFinal`. Es `@Bean` (`crearMaquinaEtapas`). | E4: `DATOS_TRANSICION_INCOMPLETOS`, `ETAPA_FINAL_SIN_ACCIONES`, `TRANSICION_NO_PERMITIDA`, `ROL_TRANSICION_NO_PERMITIDO`, `REQUISITO_TRANSICION_FALTANTE`, `ETAPA_NO_VALIDA` |
| `TramiteSolicitud` | `actorDe(token, rol)` (usuario del token con el rol exigido), `solicitudValidada(proceso, uuid, accion, actor)` (existe, actor válido y transición permitida, sin escribir). | E1 `USERNAME_TOKEN`; E3 usuario o solicitud; E4 `ACTOR_SIN_ROL` |
| `TramiteCancelacion` | Lógica común de los dos trámites de cancelación: `cobertura` (todas las asignaturas, sin repetir ni ajenas), `faltasDe`, `notaDe`, `numero`, `situacionDe`, `guardarAsignaturas`, `cancelarActivas`, `textoAcotado`. | E4 `ASIGNATURA_AJENA`, `ASIGNATURA_REPETIDA`, `ASIGNATURAS_SIN_EVALUAR`, `DATO_ASIGNATURA_FALTANTE`; E6 `NUMERO_FALTAS_NO_VALIDO`, `NOTA_NO_VALIDA`, `VALOR_ASIGNATURA_MUY_LARGO`, `OBSERVACION_ASIGNATURA_MUY_LARGA`; E3 situación inexistente |
| `ValidadorActorSolicitud` | `validar` = `validarActorCompleto` + `validarRol` (rol real del usuario en BD) + `validarAlcance` (Estudiante dueño; Funcionario asignado al tipo). | E4 `DATOS_TRANSICION_INCOMPLETOS`, `ACTOR_SIN_ROL`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`; E3 usuario |
| `ValidadorArchivoAdjunto` | `validar(archivo, documento, formatos)`: no vacío, máximo 5 MB (`AnexoAcademicoConstantes.TAMANIO_MAXIMO_BYTES`), extensión permitida (jpeg se normaliza a jpg), firma del contenido (pdf, png, jpg). `nombreOriginalSeguro`. | E6: `ARCHIVO_VACIO`, `ARCHIVO_MUY_GRANDE`, `FORMATO_ANEXO_NO_PERMITIDO`, `CONTENIDO_ANEXO_NO_COINCIDE` |
| `DatosPersonales` | Valida y aplica cambios de datos personales en la edición de Estudiante y Funcionario Académico. NO VERIFICADO en detalle (no se leyó el archivo; se infiere por uso en `EstudianteCUImplAdaptador.actualizarEstudiante`). | n/d |
| `ReglasAsignatura` | Normalización de nombre y código, `generarCodigo(sigla, consecutivo)` con formato `%s-%03d`, máximo 150 caracteres. | ninguna (utilitaria) |

Constantes que gobiernan las reglas (`dominio/helper/constantes/`): `EtapaSolicitudAcademicaConstantes` (los 8 códigos de etapa, etiqueta "Enviar Recibo", decisiones `APROBADA`/`RECHAZADA`/`APROBADA_PARCIAL`), `AnexoAcademicoConstantes` (5 MB, `FORMATOS_SOPORTE_LIBRE = "pdf,jpg,jpeg,png"`, nombres de los anexos del supletorio), `EstadoAsignaturaMatriculadaConstantes` (`activa`, `cancelada`, `aprobada`, `perdida`). Modelos de apoyo: `AccionEtapa` (10 acciones), `TipoProcesoAcademico` (nombre y sigla: `CM`, `CA`, `ES`).

## 4. Casos de uso del motor común de solicitudes académicas [EXTENSIÓN]

### 4.1 `SolicitudAcademicaCUImplAdaptador` (3 operaciones)

Puertos de salida inyectados: `SolicitudAcademicaGatewayIntPuerto`, `EstudianteGatewayIntPuerto`, `TipoSolicitudAcademicaGatewayIntPuerto`, `EtapaSolicitudAcademicaGatewayIntPuerto`, `ResolucionAcademicaGatewayIntPuerto`, `AnexoAcademicoGatewayIntPuerto`, `UsuarioGatewayIntPuerto`, `AlmacenamientoAnexosIntPuerto`, `ExcepcionesFormateadorIntPuerto`, más `MaquinaEtapas`, `LogCUIntPuerto` y `Clock` (America/Bogota, definido en `BeanConfiguracion`).

| Operación | Reglas y validaciones (en orden) | Excepciones | Escritura / log |
|---|---|---|---|
| `crearSolicitud(uuidEstudiante, uuidTipo, token)` | Estudiante existe; tipo existe; tipo mapea a `TipoProcesoAcademico` (`porNombre`); no hay otra solicitud del mismo tipo fuera de `APROBADA`/`RECHAZADA` (`verificarSinSolicitudEnCurso`); `MaquinaEtapas.siguienteEtapa(proceso, null, RADICAR, ESTUDIANTE)` da `RADICADA`; la etapa se busca en el catálogo (`getPorTipoIncluyendoUniversales`); radicado `AAAA-CM-0001` = último del año y tipo más 1 (`siguienteRadicado`, sin bloqueo). | E3 estudiante, tipo, etapa; E4 `TIPO_SOLICITUD_SIN_PROCESO`, `SOLICITUD_EN_CURSO`; E4 `RADICADO_NO_GENERADO` lo lanza el gateway ante colisión (NO VERIFICADO aquí; solo documentado en `backend/CLAUDE.md`) | `gateway.crear(solicitud, historial RADICAR)`; log "Radicar solicitud académica" |
| `verificarSinSolicitudEnCurso(uuidEstudiante, tipo)` | Consulta `getRadicadoEnCurso` excluyendo etapas finales. | E4 `SOLICITUD_EN_CURSO` | solo lectura |
| `cambiarEtapa(uuid, accion, actor, observacion, token)` | Solicitud existe; acción no nula; `ValidadorActorSolicitud.validar`; observación recortada, máximo 500; `MaquinaEtapas.transicionar` con la evidencia leída de la BD (`evidencias`: observación, existencia de Resolución, anexos "Recibo de pago" y "Comprobante de pago"); en `REMITIR_DECANO` se retira el escaneo de la Resolución existente. | E3 solicitud, etapa; E4 todas las de `MaquinaEtapas` y `ValidadorActorSolicitud`; E6 `OBSERVACION_MUY_LARGA` | `gateway.actualizarEtapa(solicitud, historial)`; si había escaneo: `resolucionGateway.eliminarPorSolicitud` y `almacenamiento.eliminarTrasConfirmar`; log "Cambiar etapa de solicitud académica" (incluye "se retiró el escaneo" si aplica) |

### 4.2 `AnexoAcademicoCUImplAdaptador` (4 operaciones)

Dependencias: `AnexoAcademicoGatewayIntPuerto`, `SolicitudAcademicaGatewayIntPuerto`, `TipoSolicitudAcademicaGatewayIntPuerto`, `TipoAnexoAcademicoGatewayIntPuerto`, `UsuarioGatewayIntPuerto`, `AlmacenamientoAnexosIntPuerto`, formateador, `LogCUIntPuerto`, `Clock`.

| Operación | Reglas | Excepciones | Escritura / log |
|---|---|---|---|
| `adjuntarAnexo(uuidSolicitud, uuidTipoAnexo, archivo, actor, token)` | Solicitud existe; actor válido; el proceso se deduce del tipo; tipo de anexo: nulo = soporte libre (prohibido en Examen Supletorio), si viene debe existir y pertenecer al tipo de solicitud; quién y cuándo (`validarQuienYCuando`): por defecto Estudiante en `RADICADA`; "Recibo de pago" solo Funcionario en `APROBADA_POR_DECANO`; "Comprobante de pago" solo Estudiante en `PENDIENTE_PAGO`; archivo con `ValidadorArchivoAdjunto`. Guarda el archivo, luego la ficha; si la ficha falla, borra el archivo y relanza. | E3 solicitud, tipo de anexo; E4 `TIPO_SOLICITUD_SIN_PROCESO`, `SOPORTE_LIBRE_NO_PERMITIDO`, `ANEXO_DE_OTRO_TIPO`, `ANEXO_ACTOR_NO_PERMITIDO`, `ANEXO_ETAPA_NO_PERMITIDA`; E6 las del validador; E1 `ERROR_GUARDANDO_ARCHIVO` si el almacenamiento falla | `almacenamiento.guardar`, `anexoGateway.guardar`; log "Adjuntar anexo académico" |
| `obtenerAnexo(uuidAnexo, actor)` | Anexo existe; solicitud existe; actor válido; lee el archivo. | E3 anexo, solicitud, archivo | solo lectura, sin log |
| `getAnexosObligatorios(uuidTipo, causa)` | Tipo existe; delega en `requeridos`. | E3 tipo; E4 `CAUSA_SUPLETORIO_REQUERIDA` (supletorio sin causa) | lectura |
| `verificarAnexosObligatorios(uuidSolicitud, causa)` | Devuelve los tipos obligatorios aún no adjuntados. `requeridos` = tipos con `obligatorio=true`, más, solo en supletorio, "Soporte de la justificación..." si causa `OTRA` o "Formato firmado por el docente de la asignatura con la que se cruza" si causa `CRUCE`. | E3 solicitud; E4 `CAUSA_SUPLETORIO_REQUERIDA` | lectura |

Observación: `verificarAnexosObligatorios` y `getAnexosObligatorios` no son invocados por las radicaciones (cada radicación hace su propia validación de anexos). NO VERIFICADO si algún controlador o prueba los usa; no se buscó.

### 4.3 `ResolucionAcademicaCUImplAdaptador` (2 operaciones)

Dependencias: `ResolucionAcademicaGatewayIntPuerto`, `SolicitudAcademicaGatewayIntPuerto`, `UsuarioGatewayIntPuerto`, `AlmacenamientoAnexosIntPuerto`, formateador, `LogCUIntPuerto`, `Clock`.

| Operación | Reglas | Excepciones | Escritura / log |
|---|---|---|---|
| `adjuntarResolucion(uuidSolicitud, archivo, actor, token)` | Orden de validación: solicitud existe; actor válido; proceso no es Examen Supletorio; el actor es FUNCIONARIO; la solicitud no está en etapa final y su etapa es `RADICADA`, `APROBADA_POR_DECANO` o `RECHAZADA_POR_DECANO`; archivo solo `pdf` y firma coherente. Reemplaza la fila existente (clave = solicitud). | E3 solicitud; E4 `TIPO_SOLICITUD_SIN_PROCESO`, `RESOLUCION_SOLO_CANCELACIONES`, `RESOLUCION_SOLO_FUNCIONARIO`, `RESOLUCION_ETAPA_FINAL`, `RESOLUCION_ETAPA_NO_PERMITIDA`; E6 validador; E1 `ERROR_GUARDANDO_ARCHIVO` | guarda archivo, `gateway.guardar`; si hay anterior, `eliminarTrasConfirmar`; log "Adjuntar Resolución académica" (acción `ADJUNTAR_RESOLUCION` o `REEMPLAZAR_RESOLUCION`) |
| `obtenerResolucion(uuidSolicitud, actor)` | Actor válido; el Estudiante solo la obtiene si la solicitud está en etapa final; Funcionario y Decano, en cualquier etapa; la fila y el archivo deben existir. | E4 `RESOLUCION_NO_DISPONIBLE`; E3 resolución, archivo | lectura, sin log |

### 4.4 `ConsultaSolicitudAcademicaCUImplAdaptador` (7 operaciones)

Dependencias: `SolicitudAcademicaGatewayIntPuerto`, `EtapaEtiquetaRolGatewayIntPuerto`, `AnexoAcademicoGatewayIntPuerto`, `HistorialSolicitudAcademicaGatewayIntPuerto`, `ResolucionAcademicaGatewayIntPuerto`, `SesionGatewayIntPuerto`, `IJwtServicio`, `AnexoAcademicoCUIntPuerto`, `MaquinaEtapas`, formateador. Sin log, sin escritura.

| Operación | Reglas | Excepciones |
|---|---|---|
| `getBandejaEstudiante(token)` | Usuario del token con rol Estudiante; solicitudes propias, filtradas a las etapas con etiqueta para ese rol (`ETAPA_ETIQUETA_ROL`). | E1 `USERNAME_TOKEN`; E3 usuario; E4 `ACTOR_SIN_ROL` |
| `getBandejaFuncionario(token)` | Rol Funcionario Académico; solicitudes de los tipos asignados a él; trae `decisionDecano` (consulta por lote) y `resultadosAsignaturas` (solo cancelaciones de asignatura en `APROBADA`/`RECHAZADA`). | igual |
| `getBandejaDecano(token)` | Rol Decano; `gateway.getTodas()` filtrado por etiqueta del Decano (no ve `RADICADA`); trae `decisionDecano`. | igual |
| `getDetalle(uuid, token)` | `acceso()` decide el rol: dueño con rol Estudiante, si no Funcionario asignado al tipo, si no quien tenga rol Decano; exige etiqueta de la etapa para ese rol, y si no responde como inexistente. Incluye `etapaCodigo` enmascarado para el Estudiante, `numeroDocumento` solo para Funcionario y Decano, anexos, `tieneResolucion`, `puedeDescargarResolucion` y `accionesDisponibles` de `MaquinaEtapas`. | E3 solicitud (también para solicitud ajena, sin etiqueta o sin rol) |
| `getHistorial(uuid, token)` | Reconstruye la etapa de cada fila encadenando `MaquinaEtapas.etapaDestino` desde `RADICAR` (`asignarEtapas`). Para el Estudiante: observación nula en `REMITIR_DECANO` (`OCULTAS_ESTUDIANTE`), filas `APROBAR_DECANO` y `RECHAZAR_DECANO` eliminadas (`FILAS_OCULTAS_ESTUDIANTE`), `etapaCodigo` enmascarado. | E3 |
| `resolverActor(uuid, token)` | Devuelve el `ActorSolicitud` (usuario y rol) deducido por `acceso()`. | E3 |
| `descargarAnexo(uuidSolicitud, uuidAnexo, token)` | Acceso válido; el anexo debe pertenecer a esa solicitud; delega en `AnexoAcademicoCU.obtenerAnexo`. | E3 |

Etiqueta del Funcionario en Examen Supletorio: `etiquetaDe` sustituye la etiqueta de `APROBADA_POR_DECANO` por "Enviar Recibo" solo para ese proceso (la tabla `ETAPA_ETIQUETA_ROL` no distingue proceso).

### 4.5 Catálogos de solo lectura [EXTENSIÓN]

| Caso de uso | Operaciones | Puertos | Reglas y excepciones | Log |
|---|---|---|---|---|
| `TipoSolicitudAcademicaCUImplAdaptador` | `getTiposSolicitudAcademica` | `TipoSolicitudAcademicaGatewayIntPuerto` | ninguna | no |
| `EtapaSolicitudAcademicaCUImplAdaptador` | `getEtapasPorTipo(uuidTipo)`, `getEtiquetasPorRol(rol)` | `EtapaSolicitudAcademicaGatewayIntPuerto`, `EtapaEtiquetaRolGatewayIntPuerto`, `TipoSolicitudAcademicaGatewayIntPuerto`, formateador | tipo debe existir (E3); rol debe ser `ESTUDIANTE`, `FUNCIONARIO` o `DECANO` ignorando mayúsculas (E6 `ROL_ETIQUETA_NO_VALIDO`) | no |
| `TipoAnexoAcademicoCUImplAdaptador` | `getTiposAnexoPorTipo(uuidTipo)` | `TipoAnexoAcademicoGatewayIntPuerto`, `TipoSolicitudAcademicaGatewayIntPuerto`, formateador | tipo debe existir (E3) | no |
| `SituacionAcademicaAsignaturaCUImplAdaptador` | `getSituaciones` | `SituacionAcademicaAsignaturaGatewayIntPuerto` | ninguna | no |
| `ProgramaAcademicoCUImplAdaptador` | `getProgramasAcademicos` | `ProgramaAcademicoGatewayIntPuerto` | ninguna | no |

### 4.6 `AsignacionFuncionarioAcademicoCUImplAdaptador` (1 operación) [EXTENSIÓN]

`asignarFuncionarioAcademico(uuidTipo, uuidFuncionario, token)`. Puertos: `TipoSolicitudAcademicaGatewayIntPuerto`, `FuncionarioAcademicoGatewayIntPuerto`, formateador, `LogCUIntPuerto`. Valida que el tipo y el funcionario existan (E3 en ambos casos) y delega en `tipoSolicitudGateway.asignarFuncionarioAcademico`. Log "Asignar funcionario académico a tipo de solicitud" con el funcionario anterior. No valida que el funcionario esté activo ni que ya atienda otro tipo (NO hay código que lo haga).

## 5. Casos de uso de actores y catálogo académico [EXTENSIÓN]

### 5.1 `EstudianteCUImplAdaptador` (9 operaciones)

Puertos: `UsuarioCUIntPuerto` (caso de uso de Julián reutilizado), `EstudianteGatewayIntPuerto`, `AsignaturaGatewayIntPuerto`, `ProgramaAcademicoGatewayIntPuerto`, `RolGatewayIntPuerto`, `UsuarioGatewayIntPuerto`, `DatosPersonalesGatewayIntPuerto`, `SesionGatewayIntPuerto`, `IJwtServicio`, formateador, `LogCUIntPuerto`.

| Operación | Reglas | Excepciones | Log |
|---|---|---|---|
| `crearEstudiante(estudiante, token)` | `validarEstudiante`: código estudiantil, documento, correo y username únicos; materias sin repetir (código y grupo); programa obligatorio y existente; cada asignatura debe existir y ser del programa del estudiante. Rol `Estudiante` y tipo de usuario `Estudiante` deben existir. `registrar` crea el `Usuario` por `UsuarioCU.crearUsuario`, fuerza facultad "FIET" y matrículas `activa`. | E2 `ATRIBUTO_UNICO_YA_EXISTE`; E6 `VALOR_REPETIDO_PETICION`, `PROGRAMA_ACADEMICO_REQUERIDO`; E3 programa, asignatura (por código), rol, tipo de usuario; E4 `ASIGNATURA_OTRO_PROGRAMA` | "Crear estudiante" (más "Crear Usuario" del caso de uso base) |
| `crearEstudiantes(lista, token)` (Excel) | Lista no vacía; validación individual más repetidos dentro del lote (código estudiantil, documento, correo, username); luego registra uno a uno. | E7 `ARCHIVO_EXCEL_VACIO`; las anteriores | un par de logs por estudiante |
| `getEstudiantesPaginado(pagina, tamanio)` | `pagina >= 0` y `tamanio >= 1`. | E6 `PAGINACION_ERROR` | no |
| `getEstudiantesPorFiltro(nombre, apellido, codigo, pagina, tamanio)` | igual | E6 | no |
| `getEstudiante(uuid)` | existe | E3 | no |
| `getMisAsignaturas(token)` | Usuario del token debe ser Estudiante; devuelve solo matrículas `activa`. | E1 `USERNAME_TOKEN`; E3 | no |
| `actualizarEstudiante(uuid, datos, token)` | Código estudiantil nuevo no repetido en otro estudiante; `DatosPersonales.cambiosValidados`; semestre; facultad se vuelve a fijar a "FIET"; programa y username no se editan. | E2, y las de `DatosPersonales` (NO VERIFICADAS) | "Actualizar estudiante" |
| `agregarAsignaturaMatriculada(uuid, matricula, token)` | Estudiante existe; no puede tener ya activa la misma asignatura y grupo; asignatura debe existir y ser de su programa; estado inicial `activa`. | E3; E6 `ASIGNATURA_YA_MATRICULADA`; E4 `ASIGNATURA_OTRO_PROGRAMA` | "Agregar asignatura matriculada" |
| `cambiarEstadoAsignatura(uuid, uuidMatricula, estado, token)` | Estado nuevo en {activa, cancelada, aprobada, perdida}; solo se permite pasar de `activa` a otro estado (no se vuelve a `activa`). | E6 `ESTADO_ASIGNATURA_NO_VALIDO`, `CAMBIO_ESTADO_NO_PERMITIDO`; E3 | "Cambiar estado asignatura matriculada" |

### 5.2 `FuncionarioAcademicoCUImplAdaptador` (6 operaciones)

Puertos: `UsuarioCUIntPuerto`, `FuncionarioAcademicoGatewayIntPuerto`, `RolGatewayIntPuerto`, `UsuarioGatewayIntPuerto`, `DatosPersonalesGatewayIntPuerto`, formateador, `LogCUIntPuerto`.

| Operación | Reglas | Excepciones | Log |
|---|---|---|---|
| `crearFuncionarioAcademico` | documento, correo y username únicos; rol `Funcionario Académico` y tipo de usuario `Empleado FIET - Funcionario` deben existir; crea `Usuario` con `crearUsuario(usuario, "FUNCIONARIOACADEMICO", token)` y guarda la fila de `FUNCIONARIO_ACADEMICO`. | E2; E3 rol/tipo | "Crear funcionario académico" (más "Crear Usuario") |
| `crearFuncionariosAcademicos` (Excel) | lista no vacía y repetidos en lote | E7, E6 `VALOR_REPETIDO_PETICION`, E2 | por registro |
| `getFuncionariosAcademicosPaginado` / `getFuncionariosAcademicosPorFiltro` | paginación válida | E6 | no |
| `getFuncionarioAcademico(uuid)` | existe | E3 | no |
| `actualizarFuncionarioAcademico` | `DatosPersonales`, cargo | las de `DatosPersonales` | "Actualizar funcionario académico" |

### 5.3 `AsignaturaCUImplAdaptador` (7 operaciones)

Puertos: `AsignaturaGatewayIntPuerto`, `ProgramaAcademicoGatewayIntPuerto`, formateador, `LogCUIntPuerto`.

| Operación | Reglas | Excepciones | Log |
|---|---|---|---|
| `crearAsignatura` | delega en `crearAsignaturas` con una lista de un elemento | | |
| `crearAsignaturas(lista, token)` | lista no vacía; nombre normalizado, no vacío, máximo 150; programa obligatorio y existente; sin nombres repetidos en el lote por programa; bloquea cada programa en orden de sigla (`programaGateway.bloquear`), verifica que el nombre no exista ya en el programa, genera código `SIGLA-nnn` con el contador `ultimoConsecutivoAsignatura` y lo guarda. | E7 `ARCHIVO_EXCEL_VACIO`; E6 `NOMBRE_ASIGNATURA_REQUERIDO`, `NOMBRE_ASIGNATURA_MUY_LARGO`, `PROGRAMA_ACADEMICO_REQUERIDO`, `VALOR_REPETIDO_PETICION`; E3 programa; E2 `ASIGNATURA_NOMBRE_EXISTE` | "Crear asignatura" por cada una |
| `getAsignaturasPaginado`, `getAsignaturasPorFiltro(texto, uuidPrograma, ...)`, `getAsignaturasPorPrograma`, `getAsignatura` | paginación válida; programa existente si se filtra | E6, E3 | no |
| `actualizarAsignatura(uuid, datos, token)` | Solo cambia el nombre (programa y código no cambian); bloquea el programa y revisa nombre repetido en otras asignaturas del programa. | E3, E6, E2 | "Actualizar asignatura" |

## 6. Radicación y trámite por proceso: matriz de casos de uso [EXTENSIÓN]

Cada proceso tiene tres casos de uso: radicación (`Cancelacion*`/`ExamenSupletorio`), trámite (`Tramite*`) y consulta (`Consulta*`). Todos reciben `ExcepcionesFormateadorIntPuerto`; los de radicación y trámite reciben además `LogCUIntPuerto`.

| Proceso | Radicación | Trámite | Consulta |
|---|---|---|---|
| Cancelación de Matrícula | `CancelacionMatriculaCUImplAdaptador` | `TramiteCancelacionMatriculaCUImplAdaptador` | `ConsultaCancelacionMatriculaCUImplAdaptador` |
| Cancelación de Asignatura | `CancelacionAsignaturaCUImplAdaptador` | `TramiteCancelacionAsignaturaCUImplAdaptador` | `ConsultaCancelacionAsignaturaCUImplAdaptador` |
| Examen Supletorio | `ExamenSupletorioCUImplAdaptador` | `TramiteExamenSupletorioCUImplAdaptador` | `ConsultaExamenSupletorioCUImplAdaptador` |

Puertos de entrada que consumen entre sí (inyectados por `BeanConfiguracion`): las tres radicaciones usan `SolicitudAcademicaCUIntPuerto` y `AnexoAcademicoCUIntPuerto`; los tres trámites usan `SolicitudAcademicaCUIntPuerto` y crean internamente `TramiteCancelacion`/`TramiteSolicitud`; las tres consultas usan `ConsultaSolicitudAcademicaCUIntPuerto`. Los trámites de cancelación y el de supletorio también reciben `SesionGatewayIntPuerto` e `IJwtServicio` (para `actorDe`).

## 7. Proceso 1: Cancelación de Matrícula (código CM)

### 7.1 Radicación: `CancelacionMatriculaCUImplAdaptador.radicarCancelacionMatricula(uuidEstudiante, motivo, anexos, token)`

Dependencias: `SolicitudCancelacionMatriculaGatewayIntPuerto`, `EstudianteGatewayIntPuerto`, `UsuarioGatewayIntPuerto`, `TipoSolicitudAcademicaGatewayIntPuerto`, `TipoAnexoAcademicoGatewayIntPuerto`, `SolicitudAcademicaCUIntPuerto`, `AnexoAcademicoCUIntPuerto`, formateador, `LogCUIntPuerto`. No recibe `Clock`.

Orden de validaciones, verificado en el método:

1. `validarEstudiante`: el usuario existe, tiene rol `Estudiante` y existe su fila en `ESTUDIANTE` (E3 / E4 `ACTOR_SIN_ROL`).
2. `asignaturasActivas`: al menos una matrícula `activa` (E4 `SIN_ASIGNATURAS_ACTIVAS`).
3. `motivoDe`: obligatorio (E4 `MOTIVO_CANCELACION_REQUERIDO`), máximo 255 (E6 `MOTIVO_CANCELACION_MUY_LARGO`).
4. `tipoCancelacionMatricula`: el tipo "Cancelación de Matrícula" existe en el catálogo (E3).
5. `validarAnexos`: cada `uuidTipoAnexo` presente debe pertenecer al tipo (E4 `ANEXO_DE_OTRO_TIPO`, o E3 si no existe); deben estar todos los tipos con `obligatorio` (E4 `ANEXOS_OBLIGATORIOS_FALTANTES`, nombra los faltantes separados por "; "); cada archivo pasa por `ValidadorArchivoAdjunto` con los formatos del tipo, o `pdf,jpg,jpeg,png` si es soporte libre (tipo nulo) (E6).

Escritura: `solicitudCU.crearSolicitud` (radicado, "una en curso por tipo" E4 `SOLICITUD_EN_CURSO`, historial `RADICAR`, log); `gateway.guardar` de `SOLICITUD_CANCELACION_MATRICULA` con una `AsignaturaSolicitudAcademica` por cada matrícula activa (evaluación vacía); `anexoCU.adjuntarAnexo` por cada anexo (rol Estudiante, etapa `RADICADA`); log "Radicar cancelación de matrícula". Un radicado genera por tanto, como mínimo, 2 + N filas de log (N = anexos).

Anexos exigidos (seed `docs/database/seed-procesos-academicos.sql`, tabla `TIPO_ANEXO_ACADEMICO`, tipo `3b6dccc9-...`): 6 con `obligatorio = 1` (paz y salvo Bibliotecas, Deportes y Recreación, Salud Integral, Cupón de Confirmación de la Intervención Psicosocial, paz y salvo Financiera, y Carné estudiantil o constancia de no trámite de DARCA; los cinco primeros en `pdf`, el último en `pdf,jpg,jpeg,png`) y soporte libre opcional. El código no tiene los nombres de los anexos de matrícula: dependen de datos sembrados. No valida vigencia de los paz y salvo ni fechas del periodo (no existe código para ello; coincide con `etapas-por-proceso.md` secciones 4.1 y 5.1).

### 7.2 Trámite: `TramiteCancelacionMatriculaCUImplAdaptador` (5 operaciones)

Dependencias: `SolicitudCancelacionMatriculaGatewayIntPuerto`, `SolicitudAcademicaGatewayIntPuerto`, `SituacionAcademicaAsignaturaGatewayIntPuerto`, `UsuarioGatewayIntPuerto`, `SesionGatewayIntPuerto`, `IJwtServicio`, `SolicitudAcademicaCUIntPuerto`, `MaquinaEtapas`, formateador, `LogCUIntPuerto`. Patrón: `tramite.actorDe(token, rol)` (rol fijo por operación), `cancelacionDe(uuid)` (E3 si no hay fila en `SOLICITUD_CANCELACION_MATRICULA`), validación propia de datos donde aplica, y `solicitudCU.cambiarEtapa(...)`.

| Operación | Actor | Transición (`MaquinaEtapas`) | Validaciones propias | Logs |
|---|---|---|---|---|
| `rechazarPorFuncionario(uuid, observacion, token)` | Funcionario Académico | `RADICADA` a `RECHAZADA` | por `cambiarEtapa`: observación obligatoria y escaneo de la Resolución ya subido | el de `cambiarEtapa` |
| `remitirADecano(uuid, evaluaciones, observacion, token)` | Funcionario Académico | `RADICADA` a `EN_REVISION_DECANO` | `solicitudValidada` antes de mutar; `cobertura` (una evaluación por asignatura, ninguna ajena ni repetida ni faltante); por asignatura: faltas (obligatorio, texto de máximo 20, si es numérico entero >= 0), nota (obligatoria, máximo 20, si es numérica de 0.0 a 5.0 con un decimal, punto o coma), situación en la matrícula obligatoria y del catálogo, situación al cancelar opcional | "Cambiar etapa..." y "Evaluar asignaturas de cancelación de matrícula" |
| `aprobarPorDecano(uuid, situacionesAlCancelar, observacion, token)` | Decano | `EN_REVISION_DECANO` a `APROBADA_POR_DECANO` | `cobertura` de todas las asignaturas; situación al cancelar obligatoria para cada una y del catálogo (E4 `DATO_ASIGNATURA_FALTANTE`, E3) | "Cambiar etapa..." y "Registrar situación al cancelar" |
| `rechazarPorDecano(uuid, observacion, token)` | Decano | `EN_REVISION_DECANO` a `RECHAZADA_POR_DECANO` | observación opcional; no toca situaciones | el de `cambiarEtapa` |
| `enviarRespuesta(uuid, observacion, token)` | Funcionario Académico | `APROBADA_POR_DECANO` a `APROBADA` o `RECHAZADA_POR_DECANO` a `RECHAZADA`; ambas con escaneo | observación opcional máximo 500 (la valida `cambiarEtapa`) | "Cambiar etapa..." y, solo en `APROBADA`, "Cancelar asignaturas matriculadas" (`TramiteCancelacion.cancelarActivas` pasa a `cancelada` las matrículas de la solicitud que sigan `activa`) |

Etapas y quién actúa (tomado de `MaquinaEtapas.TRANSICIONES` y `RESPONSABLES`): `RADICADA` (Funcionario), `EN_REVISION_DECANO` (Decano), `APROBADA_POR_DECANO` (Funcionario), `RECHAZADA_POR_DECANO` (Funcionario), `APROBADA` y `RECHAZADA` (finales). No usa `PENDIENTE_PAGO` ni `EN_VERIFICACION_PAGO`.

### 7.3 Consulta: `ConsultaCancelacionMatriculaCUImplAdaptador` (3 operaciones)

- `getEstudianteAutenticado(token)`: usuario del token con rol `Estudiante` y fila en `ESTUDIANTE` (E1, E3, E4).
- `getFormulario(token)`: devuelve `codigoEstudiantil`, los tipos de anexo del proceso y las matrículas activas. E3 si el tipo no existe.
- `getDetalle(uuid, token)`: E3 si no hay fila de especialización; usa `ConsultaSolicitudAcademicaCU.getDetalle` y `resolverActor`. Al Estudiante le oculta `situacionCancelar` mientras la etapa no sea final (`MaquinaEtapas.esFinal`). Faltas, nota y situación en la matrícula NO se ocultan al Estudiante en este caso de uso (no hay código que lo haga).
- Sin log, sin escritura.

## 8. Proceso 2: Cancelación de Asignatura (código CA)

### 8.1 Radicación: `CancelacionAsignaturaCUImplAdaptador.radicarCancelacionAsignatura(uuidEstudiante, motivo, uuidsAsignaturaMatriculada, anexos, token)`

Dependencias: `SolicitudCancelacionAsignaturaGatewayIntPuerto`, `SolicitudCancelacionMatriculaGatewayIntPuerto` (reutilizado para `guardarAsignaturas`), `EstudianteGatewayIntPuerto`, `UsuarioGatewayIntPuerto`, `TipoSolicitudAcademicaGatewayIntPuerto`, `SolicitudAcademicaCUIntPuerto`, `AnexoAcademicoCUIntPuerto`, formateador, `LogCUIntPuerto`.

Validaciones en orden: (1) estudiante con rol y fila (E3, E4); (2) motivo obligatorio (E4) y máximo 255 (E6); (3) `asignaturasElegidas`: lista no vacía (E4 `SIN_ASIGNATURAS_ELEGIDAS`), cada uuid debe ser una matrícula del propio estudiante (E4 `ASIGNATURA_NO_MATRICULADA`, mismo mensaje para ajena e inexistente), sin repetidas (E4 `ASIGNATURA_REPETIDA`) y `activa` (E4 `ASIGNATURA_NO_ACTIVA`); (4) tipo del proceso existe (E3); (5) `validarSoportes`: todo anexo debe ser soporte libre, sin tipo (E4 `SOLO_SOPORTES_LIBRES`) y cada archivo válido con `pdf,jpg,jpeg,png` (E6).

Escritura: `crearSolicitud`; `gateway.guardar` de `SOLICITUD_CANCELACION_ASIGNATURA` (motivo); `asignaturaSolicitudGateway.guardarAsignaturas(uuidSolicitud, filas)`; `adjuntarAnexo` con tipo nulo por cada soporte; log "Radicar cancelación de asignatura". Sin anexos obligatorios.

### 8.2 Trámite: `TramiteCancelacionAsignaturaCUImplAdaptador` (5 operaciones)

Dependencias: `SolicitudCancelacionAsignaturaGatewayIntPuerto`, `SolicitudCancelacionMatriculaGatewayIntPuerto`, `SolicitudAcademicaGatewayIntPuerto`, `SituacionAcademicaAsignaturaGatewayIntPuerto`, `UsuarioGatewayIntPuerto`, `SesionGatewayIntPuerto`, `IJwtServicio`, `SolicitudAcademicaCUIntPuerto`, `MaquinaEtapas`, formateador, `LogCUIntPuerto`.

| Operación | Actor | Transición | Validaciones propias | Logs |
|---|---|---|---|---|
| `rechazarPorFuncionario` | Funcionario | `RADICADA` a `RECHAZADA` | como en matrícula: observación y escaneo | el de `cambiarEtapa` |
| `remitirADecano(uuid, evaluaciones, observacion, token)` | Funcionario | `RADICADA` a `EN_REVISION_DECANO` | `cobertura`; faltas, nota, situación en la matrícula como en matrícula; situación al cancelar opcional; `cumpleCondiciones` obligatorio por asignatura (E4 `DATO_ASIGNATURA_FALTANTE`); si cumple y la nota es numérica menor a 3.0 (E4 `NOTA_INSUFICIENTE_CUMPLE`); `observacionEvaluacion` opcional, máximo 255 (E6); al menos una asignatura debe cumplir (E4 `NINGUNA_ASIGNATURA_CUMPLE`) | "Cambiar etapa..." y "Evaluar asignaturas de cancelación de asignatura" |
| `aprobarPorDecano(uuid, aceptarEvaluacion, decisiones, observacion, token)` | Decano | `EN_REVISION_DECANO` a `APROBADA_POR_DECANO` | debe llegar exactamente una de dos formas: `aceptarEvaluacion` verdadero, o `decisiones` no vacío (E4 `FORMA_APROBACION_DECANO`); si acepta, cada asignatura queda aprobada si `cumpleCondiciones` y rechazada si no; si manda decisiones, `cobertura` y cada `aprobada` no nulo; al menos una aprobada (E4 `NINGUNA_ASIGNATURA_APROBADA`); las rechazadas pierden la situación al cancelar; `observacionDecision` se limpia | "Cambiar etapa..." y "Registrar decisión del Decano por asignatura" |
| `rechazarPorDecano` | Decano | `EN_REVISION_DECANO` a `RECHAZADA_POR_DECANO` | observación opcional | el de `cambiarEtapa` |
| `enviarRespuesta(uuid, observacion, ajustes, token)` | Funcionario | `APROBADA_POR_DECANO` a `APROBADA`, o `RECHAZADA_POR_DECANO` a `RECHAZADA`; con escaneo | si la solicitud viene de rechazo del Decano, no se aceptan ajustes (E4 `AJUSTES_SOLICITUD_RECHAZADA`); en aprobada, `aplicarAjustes`: ajuste ajeno o repetido (E4), ajuste obligatorio para una asignatura aprobada cuya decisión cambió respecto de la evaluación (E4 `AJUSTE_ASIGNATURA_FALTANTE`), en asignatura rechazada no se admite situación al cancelar (E4 `SITUACION_CANCELAR_RECHAZADA`), `observacionDecision` máximo 255, y toda asignatura aprobada debe terminar con situación al cancelar (E4 `DATO_ASIGNATURA_FALTANTE`); si el Decano rechazó la solicitud o alguna asignatura, la observación general es obligatoria (E4 `OBSERVACION_RESPUESTA_REQUERIDA`) | "Cambiar etapa...", "Ajustar asignaturas de cancelación de asignatura" (solo si hubo ajustes) y, en `APROBADA`, "Cancelar asignaturas matriculadas" (solo las `aprobadaPorDecano = true`) |

Aprobación parcial: confirmada en el código (`aprobarPorDecano`, `enviarRespuesta`); solo se cancelan en `ASIGNATURA_MATRICULADA` las aprobadas que sigan activas.

### 8.3 Consulta: `ConsultaCancelacionAsignaturaCUImplAdaptador` (3 operaciones)

Mismas operaciones que la de matrícula (`getEstudianteAutenticado`, `getFormulario`, `getDetalle`). `getFormulario` arma un único soporte permitido "Soporte libre" (no obligatorio, 5 MB, `pdf,jpg,jpeg,png`) sin consultar catálogo de anexos. Para el Estudiante `getDetalle` construye una vista reducida por asignatura (`vistaEstudiante`): solo uuid, asignatura matriculada y, en etapa final, `aprobadaPorDecano`, `observacionDecision` y `situacionCancelar` (esta última solo si fue aprobada). Funcionario y Decano ven todo.

## 9. Proceso 3: Examen Supletorio (código ES)

### 9.1 Radicación: `ExamenSupletorioCUImplAdaptador.radicarExamenSupletorio(uuidEstudiante, uuidAsignaturaMatriculada, fechaExamenNoPresentado, tipoCausa, uuidAsignaturaCruzada, fechaExamenCruzada, horaExamenCruzada, anexos, token)`

Dependencias: `SolicitudExamenSupletorioGatewayIntPuerto`, `EstudianteGatewayIntPuerto`, `UsuarioGatewayIntPuerto`, `TipoSolicitudAcademicaGatewayIntPuerto`, `TipoAnexoAcademicoGatewayIntPuerto`, `SolicitudAcademicaCUIntPuerto`, `AnexoAcademicoCUIntPuerto`, formateador, `LogCUIntPuerto`, `Clock` (única radicación con reloj, por el plazo).

Validaciones en orden: (1) estudiante con rol y fila; (2) tipo "Examen Supletorio" existe; (3) no hay otra solicitud del mismo tipo en curso (`verificarSinSolicitudEnCurso`, E4); (4) asignatura obligatoria (E4 `ASIGNATURA_SUPLETORIO_REQUERIDA`), matrícula propia (E4 `ASIGNATURA_NO_MATRICULADA`) y `activa` (E4 `ASIGNATURA_SUPLETORIO_NO_ACTIVA`); (5) causa `cruce` u `otra`, sin distinguir mayúsculas (E4 `CAUSA_SUPLETORIO_INVALIDA`); (6) `validarPlazo`: fecha obligatoria (E4), no posterior a hoy en hora de Bogotá (E4 `FECHA_EXAMEN_FUTURA`), y los días lunes a viernes posteriores al examen hasta hoy inclusive no pueden superar 3 (`DIAS_HABILES_PLAZO`) (E4 `PLAZO_SUPLETORIO_VENCIDO`); no se descuentan festivos; (7) si la causa es `cruce`: asignatura cruzada obligatoria, distinta de la principal, matrícula activa propia, fecha y hora obligatorias, hora con formato `HH:mm` y máximo 10 caracteres (E4 / E6 `HORA_CRUCE_*`); si es `otra`: no se aceptan datos de cruce (E4 `DATOS_CRUCE_NO_PERMITIDOS`); (8) `validarAnexos` (ver 9.4).

Escritura: `crearSolicitud`; `gateway.guardar` de la solicitud de supletorio con el cruce si aplica; `adjuntarAnexo` por cada anexo; log "Radicar examen supletorio".

### 9.2 Trámite: `TramiteExamenSupletorioCUImplAdaptador` (9 operaciones)

Dependencias: `SolicitudExamenSupletorioGatewayIntPuerto`, `SolicitudAcademicaGatewayIntPuerto`, `UsuarioGatewayIntPuerto`, `SesionGatewayIntPuerto`, `IJwtServicio`, `SolicitudAcademicaCUIntPuerto`, `MaquinaEtapas`, formateador, `LogCUIntPuerto`. Usa `TramiteSolicitud` (no `TramiteCancelacion`). Siete operaciones pasan por el método privado `accion(...)`: `actorDe`, `supletorioDe` (E3 si no hay fila de supletorio) y `solicitudCU.cambiarEtapa`.

| Operación | Actor | Transición | Exige (por `MaquinaEtapas` salvo nota) |
|---|---|---|---|
| `rechazarPorFuncionario(uuid, observacion, token)` | Funcionario | `RADICADA` a `RECHAZADA` | observación; sin escaneo (no hay Resolución) |
| `remitirADecano(uuid, observacion, token)` | Funcionario | `RADICADA` a `EN_REVISION_DECANO` | nada; observación opcional (el cambio del árbol de trabajo eliminó `requisitosVerificados`) |
| `aprobarPorDecano(uuid, observacion, token)` | Decano | `EN_REVISION_DECANO` a `APROBADA_POR_DECANO` | nada; observación opcional |
| `rechazarPorDecano(uuid, observacion, token)` | Decano | `EN_REVISION_DECANO` a `RECHAZADA_POR_DECANO` | nada; observación opcional |
| `enviarRespuesta(uuid, observacion, token)` | Funcionario | `RECHAZADA_POR_DECANO` a `RECHAZADA` | nada; mensaje opcional; sin escaneo |
| `enviarRecibo(uuid, token)` | Funcionario | `APROBADA_POR_DECANO` a `PENDIENTE_PAGO` | anexo "Recibo de pago" ya subido (`evidencias`) |
| `subirComprobante(uuid, token)` | Estudiante | `PENDIENTE_PAGO` a `EN_VERIFICACION_PAGO` | anexo "Comprobante de pago" ya subido |
| `aprobarComprobante(uuid, fechaAcordadaExamen, token)` | Funcionario | `EN_VERIFICACION_PAGO` a `APROBADA` | fecha acordada opcional y no anterior a la fecha del examen no presentado (E4 `FECHA_ACORDADA_ANTERIOR`); si llega, `gateway.actualizarFechaAcordada` a las 00:00 y log "Acordar fecha de examen supletorio" |
| `rechazarComprobante(uuid, observacion, token)` | Funcionario | `EN_VERIFICACION_PAGO` a `RECHAZADA` | observación obligatoria |

Importante: `enviarRecibo` y `subirComprobante` no reciben el archivo. El archivo se sube antes con `POST solicitudes-academicas/{uuid}/anexos` (`AnexoAcademicoCU.adjuntarAnexo`, con las reglas de rol y etapa de 4.2), y la transición solo comprueba que exista.

### 9.3 Consulta: `ConsultaExamenSupletorioCUImplAdaptador` (3 operaciones)

`getEstudianteAutenticado`, `getFormulario` (asignaturas activas, causas `cruce`/`otra`, anexos del formulario solo para los tres nombres de `CAUSAS_POR_ANEXO` ordenados por `ORDEN_ANEXOS`, `plazoDiasHabiles = 3` tomado de `ExamenSupletorioCUImplAdaptador.DIAS_HABILES_PLAZO`, `codigoEstudiantil`) y `getDetalle` (E3 si no hay fila de supletorio; detalle común más datos del supletorio). Sin log.

### 9.4 Anexos del supletorio verificados en `validarAnexos`

- Al radicar solo se aceptan anexos con tipo (E4 `SOPORTE_LIBRE_NO_PERMITIDO` si viene sin tipo), del catálogo del proceso (E4 `ANEXO_DE_OTRO_TIPO`).
- No se aceptan "Recibo de pago" ni "Comprobante de pago" al radicar (E4 `ANEXO_NO_PERMITIDO_AL_RADICAR`).
- No se acepta el anexo de la otra causa (E4 `ANEXO_NO_CORRESPONDE_CAUSA`) ni anexos repetidos por nombre (E4 `ANEXO_REPETIDO`).
- Obligatorios en radicación: "Formato firmado por el docente que orienta la asignatura" siempre; más "Formato firmado por el docente de la asignatura con la que se cruza" si la causa es `cruce`, o "Soporte de la justificación de la no presentación" si es `otra` (E4 `ANEXOS_OBLIGATORIOS_FALTANTES`).
- Formatos por anexo: los de `TIPO_ANEXO_ACADEMICO` en el seed: `pdf,jpg,png` para los tres de radicación y el comprobante; `pdf` para el recibo.
- Por ser anexos condicionales, el seed los siembra con `obligatorio = 0` salvo el formato del docente (`obligatorio = 1`); la exigencia condicional está en el código, no en la columna.

## 10. Tabla de etapas y transiciones verificada contra el código

Fuente: `dominio/servicios/MaquinaEtapas.java`, constante `TRANSICIONES` (cada fila es `procesos, origen, acción, destino, rol, observación, resolución, recibo, comprobante`).

| Procesos | De | Acción | A | Rol | Observación | Escaneo Resolución | Recibo | Comprobante |
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

Responsable por etapa (`RESPONSABLES`): `RADICADA` Funcionario; `EN_REVISION_DECANO` Decano; `APROBADA_POR_DECANO` Funcionario; `RECHAZADA_POR_DECANO` Funcionario; `PENDIENTE_PAGO` Estudiante; `EN_VERIFICACION_PAGO` Funcionario; `APROBADA` y `RECHAZADA` Ninguno (finales). En una etapa final, `siguienteEtapa` lanza E4 `ETAPA_FINAL_SIN_ACCIONES` y `accionesDisponibles` devuelve lista vacía.

Rol real: `TramiteSolicitud.ROLES` y `ValidadorActorSolicitud.ROLES` mapean `ESTUDIANTE` a `ApplicationConstantes.ESTUDIANTE_ROL`, `FUNCIONARIO` a `FUNCIONARIO_ACADEMICO_ROL` y `DECANO` a `ApplicationConstantes.DECANO`. El Funcionario además debe ser el asignado al tipo (`ValidadorActorSolicitud.validarAlcance`).

## 11. Casos de uso [BASE] (prototipo de Julián)

Se documentan con menos detalle porque no son parte del aporte de la extensión. Todos siguen el patrón: validar con el formateador, llamar al gateway, registrar log.

### 11.1 `UsuarioCUImplAdaptador` (10 operaciones)

Puertos: `UsuarioGatewayIntPuerto`, formateador, `LogCUIntPuerto`, `PasswordEncoderGatewayIntPuerto`.

| Operación | Reglas | Excepciones | Log |
|---|---|---|---|
| `getUsuarios()`, `getUsuarios(pagina, tamanio)`, `getUsuariosByNombreCompleto` | lista no vacía, paginación válida (`pagina >= 0`, `tamanio >= 0`) | E7 `SIN_REGISTROS`, E6 `PAGINACION_ERROR` | no |
| `getFuncionarios()` | sin validación | n/a | no |
| `getUsuario(uuid)` | existe | E3 | no |
| `crearUsuario(usuario, tipoUsuario, token)` | tipo de documento válido; número de documento, correo y username únicos; tipo de usuario existe por nombre; roles sin duplicados y válidos; contraseña cifrada; `crearInstancia(tipoUsuario)` genera `Funcionario` solo para `FUNCIONARIO`, si no un `Usuario` | E6 `TIPO_DOCUMENTO_ERRONEO`; E2; E3; E4 `ROLES_DUPLICADOS_USUARIO`, `ROLES_NO_VALIDOS`, `INSTANCIA_NO_VALIDA` | "Crear Usuario", antes de guardar |
| `crearUsuarios(lista, token)` | mismas reglas por usuario; el tipo de instancia sale del primer rol (`getRoles().get(0)`, sin espacios) | E7 y las anteriores | un "Crear Usuario" por usuario, después de guardar |
| `actualizarUsuario` | usuario existe; documento y roles válidos; unicidad solo si cambió el valor | E3, E6, E4, E2 | "Actualizar Usuario" |
| `cambiarContraseña` | la contraseña actual debe coincidir | E5 `CONTRASEÑA_INCORRECTA` | "Actualizar Usuario" |
| `getTiposUsuario()` | lista no vacía | E7 | no |

Nota para la extensión: `EstudianteCU` y `FuncionarioAcademicoCU` crean sus usuarios a través de `crearUsuario` con los códigos `"ESTUDIANTE"` y `"FUNCIONARIOACADEMICO"`; como `Usuario.crearInstancia` solo distingue `FUNCIONARIO`, ambos generan un `Usuario` simple (verificado en `dominio/modelos/Usuario.java`).

### 11.2 `SesionCUImplAdaptador` (1 operación)

`login(username, contraseña)`: `gateway.login` devuelve el token o nulo (E5 `CREDENCIALES_ERRONEAS`); el usuario debe estar activo (E8 `NO_ACCESO`) y todos sus roles habilitados (E8 `ROL_NO_HABILITADO`); escribe "Inicio de sesión" con `crearLogSesion`. Dependencias: `SesionGatewayIntPuerto`, `LogCUIntPuerto`, formateador.

### 11.3 `RolCUImplAdaptador` (4 operaciones)

`getRoles()`, `getRoles(pagina, tamanio)` (E7 si vacío, E6 paginación), `getRol(uuid)` (E3), `actualizarRol` (solo descripción y estado; log "Actualizar rol"). Dependencias: `RolGatewayIntPuerto`, formateador, `LogCUIntPuerto`.

### 11.4 `LogCUImplAdaptador` (5 operaciones)

Dependencias: `LogGatewayIntPuerto`, formateador, `IJwtServicio`. `crearLog(accion, resultado, token)` extrae el username del token (E1 si no se puede; E3 si no existe) y guarda el registro con fecha `yyyy-MM-dd HH:mm:ss` con `LocalDateTime.now()` del reloj del sistema (no usa el `Clock` de Bogotá de la extensión). `crearLogSesion(accion, resultado, username)`. `getLogs()` (E7 si vacío), `getLogs(pagina, tamanio)` (E6, E7), `getLogs(responsable, fecha, pagina, tamanio)` (E6). Este es el servicio de historial (tabla de logs) que usan todos los demás casos de uso mediante `LogCUIntPuerto`.

### 11.5 `TipoSolicitudCUImplAdaptador` (10 operaciones)

Dependencias: `TipoSolicitudGatewayIntPuerto`, `UsuarioGatewayIntPuerto`, formateador, `LogCUIntPuerto`, `RolGatewayIntPuerto`. Lecturas con E7/E6/E3; `crearTipoSolicitud` y `crearTiposSolicitud` validan funcionario encargado (debe ser `Funcionario` de Julián, si no E4 `MAL_ASIGNACION`), sección, perfil solicitante y formato de anexos (E4); `actualizarTipoSolicitud` reasigna funcionario y valida igual. Logs "Crear Tipo de Solicitud" y "Actualizar Tipo Solicitud". Este caso de uso es del módulo de Julián (tipos de solicitud del Consejo) y es distinto de `TipoSolicitudAcademicaCUImplAdaptador` de la extensión.

### 11.6 `SolicitudCUImplAdaptador` (13 operaciones)

Dependencias: `SolicitudGatewayIntPuerto`, formateador, `TipoSolicitudGatewayIntPuerto`, `OrdenDelDiaGatewayIntPuerto`, `UsuarioGatewayIntPuerto`, `LogCUIntPuerto`, `SesionGatewayIntPuerto`, `IJwtServicio`, `AlmacenadorArchivos`. Inyecta `@Value("${url.backend}")` y `@Value("${url.application}")` en campos aunque la clase se instancia con `new` en `BeanConfiguracion`. Operaciones: lecturas paginadas y filtradas (`getSolicitudes` x2, `getSolicitud`, `getSolicitudesPorFuncionario`, `getSolicitudesPorOrdenDelDia`, `getSolicitudesPorEstado`, `buscarSolicitudesPorNombre`, `buscarSolicitudesPorSolicitante`, `buscarSolicitudesPorNombreYFuncionario`) con E3/E6/E7; `crearSolicitud` y `crearSolicitudPublica` (verifican tipo, anexos obligatorios y formato por extensión, orden del día, asignan funcionario, guardan los archivos con `AlmacenadorArchivos` y envuelven un `IOException` en `RuntimeException` plano); `actualizarSolicitud` (reasigna funcionario y orden del día); `generarZipAnexosPorOrdenDelDia` (arma el ZIP leyendo los archivos del disco, E6 si faltan). Logs "Solicitud Creada", "Solicitud Publica Creada", "Solicitud modificada".

Hallazgos de código en esta clase: en `checkSolicitud` el mensaje de formato de anexo trae el fragmento literal `%sytg` (`"El anexo %s debe ser un archivo %sytg"`), que parece un error tipográfico; la comprobación de formato compara la extensión con `tipo.getFormato()` por igualdad exacta. No se corrige (código base).

### 11.7 `RespuestaCUImplAdaptador` (9 operaciones)

Dependencias: `RespuestaGatewayIntPuerto`, `SolicitudGatewayIntPuerto`, formateador, `LogCUIntPuerto`, `AlmacenadorArchivos`; también `@Value` para `url.backend`, `url.application` y `app.uploads.base-path`. Lecturas paginadas (E6, E7) y por uuid (E3); `registrarRespuesta` crea o modifica la respuesta de una solicitud (valida el tipo de respuesta, E4 `TIPO_RESPUESTA_NO_VALIDO`); `responderSolicitud` sube el archivo (solo PDF o DOCX por extensión, E4 `FORMATO_RESPUESTA_NO_VALIDO`) y borra el anterior; `eliminarArchivoRespuesta` (E4 si no hay archivo). Envuelve fallos de IO en `RuntimeException` plano. Logs "Registrar respuesta de Solicitud", "Modificar respuesta de Solicitud", "Responder Solicitud", "Eliminar archivo de respuesta".

### 11.8 `OrdenDelDiaCUImplAdaptador` (10 operaciones)

Dependencias: `OrdenDelDiaGatewayIntPuerto`, `SolicitudGatewayIntPuerto`, formateador, `LogCUIntPuerto`, `OrdenDelDiaExportador`, `RespuestaGatewayIntPuerto`. Operaciones: `getOrdenesDelDia()`, `getOrdenesDelDiaPorEstado(estado)`, `getOrdenesDelDia(pagina, tamanio)`, `getOrdenDelDia(uuid)`, `crearOrdenDelDia`, `actualizarOrdenDelDia`, `buscarOrdenDelDiaPorNumeroActa`, `generarOrdenDelDia`, `generarOrdenDelDiaConRespuestas`, `generarOrdenDelDiaMerge` (estas tres exportan `.docx` con plantillas `templates/orden_del_dia_*.docx`). Logs "Crear Orden del Día" y "Actualizar Orden del Día". No leí a fondo `procesarSolicitudes` ni `generarOrdenDelDiaMerge`: NO VERIFICADO el detalle de generación.

## 12. Resumen de logs al historial

Hay dos mecanismos distintos, no confundirlos:

1. Log de auditoría de Julián (`LogCUIntPuerto.crearLog(accion, resultado, token)`): lo usan los casos de uso que escriben. Lista de acciones de la extensión (cadena de `accion`): "Radicar solicitud académica", "Cambiar etapa de solicitud académica", "Adjuntar anexo académico", "Adjuntar Resolución académica", "Asignar funcionario académico a tipo de solicitud", "Crear estudiante", "Actualizar estudiante", "Agregar asignatura matriculada", "Cambiar estado asignatura matriculada", "Crear funcionario académico", "Actualizar funcionario académico", "Crear asignatura", "Actualizar asignatura", "Radicar cancelación de matrícula", "Radicar cancelación de asignatura", "Radicar examen supletorio", "Evaluar asignaturas de cancelación de matrícula", "Registrar situación al cancelar", "Cancelar asignaturas matriculadas", "Evaluar asignaturas de cancelación de asignatura", "Registrar decisión del Decano por asignatura", "Ajustar asignaturas de cancelación de asignatura", "Acordar fecha de examen supletorio". Casos de uso de la extensión que no escriben log: las tres consultas de proceso, `ConsultaSolicitudAcademicaCU`, y los cinco catálogos de solo lectura.
2. Historial funcional de la solicitud (`HISTORIAL_SOLICITUD_ACADEMICA`): lo escribe solo `SolicitudAcademicaCUImplAdaptador` mediante `gateway.crear` (acción `RADICAR`) y `gateway.actualizarEtapa` (cualquier otra acción), con `uuidHistorial`, solicitud, usuario, nombre de la acción (`AccionEtapa.name()`), observación y fecha (`LocalDateTime.now(Clock)`). No guarda la etapa; se reconstruye al leer (`ConsultaSolicitudAcademicaCU.getHistorial`).

Casos de uso [BASE] que escriben log: `UsuarioCU`, `SesionCU` (`crearLogSesion`), `RolCU`, `TipoSolicitudCU`, `SolicitudCU`, `RespuestaCU`, `OrdenDelDiaCU`. `LogCU` es el servicio que escribe y no se registra a sí mismo. Total de casos de uso que escriben log: 20 de 30. Extensión (13): `SolicitudAcademicaCU`, `AnexoAcademicoCU`, `ResolucionAcademicaCU`, `AsignacionFuncionarioAcademicoCU`, `EstudianteCU`, `FuncionarioAcademicoCU`, `AsignaturaCU`, las tres radicaciones y los tres trámites. Base (7): `UsuarioCU`, `SesionCU`, `RolCU`, `TipoSolicitudCU`, `SolicitudCU`, `RespuestaCU`, `OrdenDelDiaCU`.

## 13. Divergencias entre el código y la documentación

Solo se listan diferencias reales o textos desactualizados, verificados contra el código. Fuente documental: `docs/database/etapas-por-proceso.md` (EPP), `backend/CLAUDE.md`, `CLAUDE.md` raíz, `docs/api/contrato-api-procesos-academicos.md`.

| N | Tema | Documentación dice | Código hace | Gravedad |
|---|---|---|---|---|
| D1 | Remisión del supletorio, EPP sección 3.2, fila "RADICADA, Remitir al Decano" | "Requisitos verificados. Observación opcional" | `TramiteExamenSupletorioCUImplAdaptador.remitirADecano` y la transición `REMITIR_DECANO` de `MaquinaEtapas` no exigen nada más (la decisión P49, que está en la misma tabla de decisiones de EPP, lo confirma); el texto de la tabla 3.2 quedó sin actualizar | Documentación desactualizada (el código coincide con P49) |
| D2 | Observación de evaluación en Cancelación de Asignatura, EPP P14 | "si no cumple, la observación de la evaluación es obligatoria" | `TramiteCancelacionAsignaturaCUImplAdaptador.remitirADecano` la trata como opcional (`textoAcotado` devuelve nulo si está vacía); coincide con P46, que la hace opcional, pero P14 sigue sin reflejarlo | Documentación internamente contradictoria; el código sigue P46 |
| D3 | "Cuatro condiciones académicas" (`CLAUDE.md` raíz, EPP 4.2, P11) | Se presentan como cuatro condiciones | El código modela un único booleano `cumpleCondiciones` por asignatura; las cuatro condiciones no se registran ni validan por separado (solo se valida la de nota numérica menor a 3.0 cuando se marca "cumple") | Matiz: son criterios de revisión humana, no reglas codificadas |
| D4 | Plazo de Cancelación de Matrícula y de Asignatura | EPP lo define como regla del proceso (último día de clases / antes de terminar el periodo) y aclara que el sistema no valida fechas | Confirmado: ningún caso de uso de cancelación compara fechas. Es una limitación consciente, no una divergencia | Sin divergencia (se deja constancia) |
| D5 | Vigencia de paz y salvo | EPP 5.1: no se valida | Confirmado: no hay código | Sin divergencia |
| D6 | Visibilidad de datos de evaluación al Estudiante en Cancelación de Matrícula | `backend/CLAUDE.md` y EPP P22 hablan solo de la situación al cancelar | `ConsultaCancelacionMatriculaCUImplAdaptador.getDetalle` oculta solo `situacionCancelar`; `numeroFaltas`, `nota` y `situacionMatricula` los ve el Estudiante en cualquier etapa. En Cancelación de Asignatura sí se reduce la vista (P15). Los documentos no dicen que en matrícula el Estudiante vea faltas y nota, ni que no las vea | Posible laguna de especificación; NO VERIFICADO si el DTO de respuesta (`infraestructura/input`) las filtra |
| D7 | Orden de validaciones al radicar | `backend/CLAUDE.md` y EPP describen validaciones comunes | En supletorio la regla "una en curso" se evalúa explícitamente antes de validar la asignatura y el plazo (`verificarSinSolicitudEnCurso`) y otra vez dentro de `crearSolicitud`; en las cancelaciones solo ocurre dentro de `crearSolicitud`, después de validar archivos. Es una diferencia de orden de errores, no de resultado | Menor |
| D8 | Escritura del log antes o después de guardar | `backend/CLAUDE.md`: "después de guardar... si el log falla, la transacción del controlador revierte todo" | Vale para los casos de uso de la extensión. En [BASE] `UsuarioCU.crearUsuario`, `SolicitudCU.crearSolicitud`, `RespuestaCU.registrarRespuesta` y `OrdenDelDiaCU.crearOrdenDelDia` el log va antes de guardar | Informativo (diferencia base/extensión, la regla del CLAUDE.md es para el código nuevo) |
| D9 | Cifra de casos de uso del `CLAUDE.md` de backend | La tabla "Casos de uso por área" lista 22 casos de uso de la extensión | Coincide con el conteo (22 [EXTENSIÓN]); la tabla omite las 8 clases base, lo cual es esperado | Sin divergencia |

No se encontró divergencia entre la tabla de transiciones de EPP sección 3 y `MaquinaEtapas.TRANSICIONES` salvo D1; tampoco entre los anexos de EPP sección 5 y el seed `docs/database/seed-procesos-academicos.sql` (6 obligatorios de matrícula; 5 tipos del supletorio con los nombres y formatos documentados), ni entre las etiquetas de EPP sección 2 y `ETAPA_ETIQUETA_ROL` del seed (el Decano no tiene fila para `RADICADA`).

## 14. Pendientes de verificación

- NO VERIFICADO: el contenido de `dominio/servicios/DatosPersonales.java` y de los gateways (`SolicitudAcademicaGatewayImplAdaptador.crear`, colisión del radicado `uk_solacad_radicado`), fuera del alcance de esta tarea.
- NO VERIFICADO: si los DTO de respuesta de Cancelación de Matrícula filtran `numeroFaltas` y `nota` para el Estudiante (D6).
- NO VERIFICADO: uso de `AnexoAcademicoCU.getAnexosObligatorios` y `verificarAnexosObligatorios` por parte de algún controlador.
- El árbol de trabajo tiene cambios sin confirmar en los archivos del supletorio (`TramiteExamenSupletorioCUImplAdaptador`, `TramiteExamenSupletorioCUIntPuerto`, `AnexoAcademicoConstantes`, `MensajesError`, tests); lo descrito aquí es ese estado, no el de `HEAD`.
