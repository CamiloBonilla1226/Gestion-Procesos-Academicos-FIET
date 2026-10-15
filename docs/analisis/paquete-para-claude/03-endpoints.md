

===== 03-backend-endpoints.md =====

# 03 - Backend: catálogo de endpoints

Análisis estático de solo lectura. Etiquetas: [BASE] = código ya presente en el primer commit `d3a48cc` (prototipo de Julián David Camacho Erazo); [EXTENSIÓN] = agregado después. Todo lo marcado NO VERIFICADO no se comprobó en ejecución (no se arrancó la aplicación, no se corrió ningún test).

## 1. Resumen y verificación del total

**Total: 128 endpoints** en 15 controladores: **62 [BASE]** (7 controladores) y **66 [EXTENSIÓN]** (8 controladores). Los 66 coinciden con el total declarado en `docs/api/contrato-api-procesos-academicos.md` (línea 3: "describe los 66 endpoints").

Verificación con grep sobre `backend/solicitudes/src/main/java` (ninguna anotación de mapeo está comentada: 0 coincidencias en líneas de comentario):

| Anotación | Ocurrencias | Dónde |
|---|---|---|
| `@GetMapping` | 75 | solo en los 15 `*RestController` |
| `@PostMapping` | 41 | idem |
| `@PutMapping` | 9 | idem |
| `@PatchMapping` | 3 | idem |
| `@DeleteMapping` | 0 | no existe ningún DELETE en la API |
| **Suma de métodos HTTP** | **128** | 75 + 41 + 9 + 0 + 3 |
| `@RequestMapping` | 15 | una por controlador, a nivel de clase (`${url.application}<recurso>`); ninguna a nivel de método, por eso no suman endpoints |
| `@PreAuthorize` | 124 | 128 endpoints menos 4 sin anotación: `POST sesiones`, `GET tipos/solicitudes/perfil`, `GET usuarios/{uuidUsuario}`, `PATCH usuarios/{uuidUsuario}` |

Comandos usados: `grep -rEn "@(Get|Post|Put|Delete|Patch)Mapping" . | wc -l` (128), el mismo conteo agrupado por archivo (coincide con la tabla de abajo) y `grep -rn "@RequestMapping" . | wc -l` (15). Además se extrajeron las 128 firmas con un parser propio (script temporal fuera del repositorio) y se cruzó cada una con las reglas de `ConfiguracionSeguridad`; el parser también arrojó 128.

| Controlador | Origen | GET | POST | PUT | PATCH | Total |
|---|---|---|---|---|---|---|
| `SesionRestController` (Sesión) | [BASE] | 0 | 1 | 0 | 0 | 1 |
| `RolRestController` (Roles) | [BASE] | 3 | 0 | 1 | 0 | 4 |
| `LogRestController` (Logs) | [BASE] | 3 | 0 | 0 | 0 | 3 |
| `UsuarioRestController` (Usuarios) | [BASE] | 6 | 2 | 1 | 1 | 10 |
| `TiposSolicitudRestController` (Tipos de solicitud (Julián)) | [BASE] | 7 | 2 | 1 | 0 | 10 |
| `SolicitudesRestController` (Solicitudes y Orden del día (Julián)) | [BASE] | 19 | 3 | 2 | 0 | 24 |
| `RespuestaRestController` (Respuestas (Julián)) | [BASE] | 7 | 2 | 0 | 1 | 10 |
| `AsignaturaRestController` (Asignaturas) | [EXTENSIÓN] | 4 | 2 | 1 | 0 | 7 |
| `EstudianteRestController` (Estudiantes) | [EXTENSIÓN] | 4 | 3 | 1 | 1 | 9 |
| `FuncionarioAcademicoRestController` (Funcionarios académicos) | [EXTENSIÓN] | 3 | 2 | 1 | 0 | 6 |
| `CatalogoAcademicoRestController` (Catálogos académicos) | [EXTENSIÓN] | 6 | 0 | 1 | 0 | 7 |
| `SolicitudAcademicaRestController` (Solicitudes académicas (motor común)) | [EXTENSIÓN] | 7 | 2 | 0 | 0 | 9 |
| `CancelacionMatriculaRestController` (Cancelación de Matrícula) | [EXTENSIÓN] | 2 | 6 | 0 | 0 | 8 |
| `CancelacionAsignaturaRestController` (Cancelación de Asignatura) | [EXTENSIÓN] | 2 | 6 | 0 | 0 | 8 |
| `ExamenSupletorioRestController` (Examen Supletorio) | [EXTENSIÓN] | 2 | 10 | 0 | 0 | 12 |
| **Total** | | **75** | **41** | **9** | **3** | **128** |

BASE: 62 endpoints; EXTENSIÓN: 66 endpoints.

## 2. Convenciones y cómo leer las tablas

- **Ruta completa**: `/api/unicauca/fiet/consejo/` es el valor de `url.application` en `backend/solicitudes/src/main/resources/application.properties` (línea 4); no hay `server.servlet.context-path`. Cada controlador lo antepone con `@RequestMapping("${url.application}<recurso>")`. Las rutas con uuid de la extensión llevan la expresión regular `[0-9a-fA-F\-]{36}` en el código; aquí se muestran sin ella.
- **Rol efectivo**: resultado de evaluar la ruta contra los 78 `requestMatchers` de `ConfiguracionSeguridad.securityFilterChain` (infraestructura/configuracion/seguridad/configuracion), en orden y gana el primero que coincide; si ninguno coincide cae en `anyRequest().authenticated()`. Siglas: SG = Secretario General, DEC = Decano, FUN = Funcionario (rol de comité de Julián), FA = Funcionario Académico, EST = Estudiante, AUTH = cualquier usuario autenticado, PÚBLICO = sin token, DENEGADO = `denyAll()`. La evaluación la hizo un script que replica el orden y los patrones (`{var}`, `**`); no se comprobó con peticiones reales (NO VERIFICADO en ejecución).
- **@PreAuthorize**: roles de la anotación del método, según las constantes de `ApplicationConstantes`. No protege nada: no existe `@EnableMethodSecurity` ni `@EnableGlobalMethodSecurity` en el código (grep sin resultados), lo que coincide con `backend/CLAUDE.md` (sección Seguridad). Se muestra para detectar divergencias con la regla efectiva (sección 5). "—" = el método no tiene anotación.
- **DTO petición**: tipo del `@RequestBody`/`@RequestPart` o parámetros `@RequestParam` (nombre entre comillas invertidas; "(opc.)" = `required = false`). Los path variables no se listan. `multipart con partes de archivo dinámicas` = `MultipartHttpServletRequest` (cada parte de archivo lleva como nombre el uuid del tipo de anexo; ver el mapper del módulo).
- **DTO respuesta**: tipo devuelto; `X[]` = lista; `PaginacionRespuestaDTO<X>` = `{contenido, total}` (`dominio/helper/PaginacionRespuestaDTO`). Se dedujo del tipo del `ResponseEntity` y, cuando el controlador usa `ResponseEntity<?>`, del tipo de retorno del método `mapearXARespuesta` del `Mapper*InfraestructuraDominio`.
- **Caso de uso**: `Interfaz.método` (sin el sufijo `IntPuerto`; la implementación es `*CUImplAdaptador`, ver documento 02). En los trámites de las tres cancelaciones y del supletorio se invoca además `Consulta*CU.getDetalle` para devolver el detalle actualizado.
- **Tablas**: `E:` = tablas que escribe (incluye el insert en `logs` cuando el caso de uso registra log) y `L×n` = número de tablas que solo lee, sin contar las que ya figuran en E (la lista está en el anexo C). Es la unión estática de las tablas de los repositorios usados por los métodos de gateway alcanzables desde el caso de uso: `endpoint -> *RestController -> CU -> (servicios de dominio) -> *GatewayIntPuerto -> *GatewayImplAdaptador -> *Repositorio -> @Table de la entidad`. Es una cota superior: no distingue ramas, salvo que se quitó `RESOLUCION_ACADEMICA` de las escrituras de los trámites que no son `remitir` (en `SolicitudAcademicaCUImplAdaptador.cambiarEtapa` solo `REMITIR_DECANO` borra el escaneo). Una escritura por seguimiento de entidad (`find*` seguido de setters sobre una variable `*Entidad` sin `save`) se cuenta como escritura. `RESOLUCION_ACADEMICA` figura en las escrituras de `remitir` del Examen Supletorio solo por el código genérico de `cambiarEtapa` (según `backend/CLAUDE.md` el supletorio no tiene Resolución; el borrado solo se dispara si existe la fila). No incluye lo que Hibernate cargue por relaciones perezosas ni cascadas (NO VERIFICADO). Los nombres en minúscula/camelCase son tablas de Julián (`logs`, `usuarios`, `roles`...); los MAYÚSCULA con guion bajo son de la extensión.
- **Códigos de error**: números 1 a 9 de `CodigoError` (infraestructura/output/manejadorExcepciones/estructura), deducidos de las llamadas `ExcepcionesFormateadorIntPuerto.lanzar*` alcanzables desde el caso de uso: 1 genérico, 2 entidad existe, 3 entidad no existe, 4 regla de negocio violada, 5 credenciales, 6 mal formato, 7 sin información, 8 sin acceso. El 9 (token expirado) no lo emite ningún caso de uso: `ErrorTokenExpiradoExcepcion` nunca se lanza (grep) y el filtro JWT responde 401 con `codigoError` "401". Se agrega 1 cuando el endpoint usa almacenamiento de archivos (fallo de IO). Tras el punto medio: `400` = tiene `@Valid` sobre el cuerpo (mapa campo->mensaje, `handleValidationExceptions`), `500-BD` = el controlador captura `DataAccessException` y responde 500 con `{mensaje, error}` sin `codigoError`, `401/403` = pasa por el filtro de seguridad (no aplica a rutas públicas). El detalle por método está en el anexo B.
- **Comportamiento HTTP de los errores** (`RestApiExcepcion`, `@ControllerAdvice`): todas las excepciones propias (códigos 1 a 8) se devuelven como **HTTP 500** con cuerpo `{codigoError, mensaje, httpCodigo: 500, url, metodo}`, salvo `AuthenticationException` y `AccessDeniedException` (403) y `MethodArgumentNotValidException`/`ConstraintViolationException` (400). `ErrorGenericoExcepcion` y los `RuntimeException` planos caen en el handler genérico de `Exception` (500, código 1). Los 401 y 403 de la cadena de filtros los escriben `CustomAuthenticationEntryPoint` (401, `codigoError` "401") y `CustomAccessDeniedHandler` (403). Coincide con `docs/api/contrato-api-procesos-academicos.md` líneas 35-42.
- Transacciones: los endpoints de escritura de la extensión llevan `@Transactional`; los GET de `solicitudes-academicas`, `cancelaciones-*` y `examenes-supletorios` llevan `@Transactional(readOnly = true)`; los GET de `asignaturas`, `estudiantes`, `funcionarios-academicos` y `catalogos-academicos` no llevan transacción (consistente con "Patrón de los controladores de la extensión" en `backend/CLAUDE.md`).

Conteo de `@Transactional` por endpoint: BASE sin transacción: 46; BASE transacción de escritura: 16; EXTENSIÓN sin transacción: 17; EXTENSIÓN readOnly: 13; EXTENSIÓN transacción de escritura: 36.

## 3. Catálogo por módulo

### 3.1 Sesión [BASE]

- Controlador: `SesionRestController` (`backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/input/controladorSesion/controlador/SesionRestController.java`), base `${url.application}sesiones`, 1 endpoints.
- Inyecta: `SesionCUIntPuerto`, `MapperSesionInfraestructuraDominio`.
- Nota: Autenticación. Única ruta con `permitAll` para todos los métodos HTTP.

| # | Mét. | Ruta completa | Rol efectivo | @PreAuthorize | Método del controlador | DTO petición | DTO respuesta | Caso de uso | Tablas | Códigos de error |
|---|---|---|---|---|---|---|---|---|---|---|
| 1 | POST | `/api/unicauca/fiet/consejo/sesiones` | PÚBLICO | — | `auth` | SesionDTOPeticion | UsuarioTokenizadoDTORespuesta | SesionCU.login | E: logs · L×1 | 1, 3, 5, 8 · 400 |

### 3.2 Roles [BASE]

- Controlador: `RolRestController` (`backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/input/controladorRoles/controlador/RolRestController.java`), base `${url.application}roles`, 4 endpoints.
- Inyecta: `RolCUIntPuerto`, `MapperRolInfraestructuraDominio`.
- Nota: Roles de Julián.

| # | Mét. | Ruta completa | Rol efectivo | @PreAuthorize | Método del controlador | DTO petición | DTO respuesta | Caso de uso | Tablas | Códigos de error |
|---|---|---|---|---|---|---|---|---|---|---|
| 2 | GET | `/api/unicauca/fiet/consejo/roles` | SG, DEC | SG, DEC | `index` | — | RolDTORespuesta[] | RolCU.getRoles | solo lectura · L×1 | 6, 7 · 401/403 |
| 3 | GET | `/api/unicauca/fiet/consejo/roles/paginado` | SG, DEC | SG, DEC | `indexPaginado` | `pagina`; `tamanio` | RolDTORespuesta[] | RolCU.getRoles | solo lectura · L×1 | 6, 7 · 401/403 |
| 4 | GET | `/api/unicauca/fiet/consejo/roles/{uuidRol}` | SG, DEC | SG, DEC | `getRol` | — | RolDTORespuesta | RolCU.getRol | solo lectura · L×1 | 3 · 401/403 |
| 5 | PUT | `/api/unicauca/fiet/consejo/roles/{uuidRol}` | SG, DEC | SG, DEC | `actualizarRol` | RolDTOPeticion | RolDTORespuesta | RolCU.actualizarRol | E: logs, roles · L×1 | 1, 3 · 400/500-BD/401/403 |

### 3.3 Logs [BASE]

- Controlador: `LogRestController` (`backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/input/controladorLog/controlador/LogRestController.java`), base `${url.application}logs`, 3 endpoints.
- Inyecta: `LogCUIntPuerto`, `MapperLogInfraestructuraDominio`.
- Nota: Logs de Julián.

| # | Mét. | Ruta completa | Rol efectivo | @PreAuthorize | Método del controlador | DTO petición | DTO respuesta | Caso de uso | Tablas | Códigos de error |
|---|---|---|---|---|---|---|---|---|---|---|
| 6 | GET | `/api/unicauca/fiet/consejo/logs` | SG, DEC | SG, DEC | `index` | — | LogDTORespuesta[] | LogCU.getLogs | solo lectura · L×1 | 6, 7 · 401/403 |
| 7 | GET | `/api/unicauca/fiet/consejo/logs/paginado` | SG, DEC | SG, DEC | `indexPaginado` | `pagina`; `tamanio` | PaginacionRespuestaDTO<LogDTORespuesta> | LogCU.getLogs | solo lectura · L×1 | 6, 7 · 401/403 |
| 8 | GET | `/api/unicauca/fiet/consejo/logs/filtro` | SG, DEC | SG, DEC | `filtrarLogs` | `responsable` (opc.); `fecha` (opc.); `pagina`; `tamanio` | PaginacionRespuestaDTO<LogDTORespuesta> | LogCU.getLogs | solo lectura · L×1 | 6, 7 · 401/403 |

### 3.4 Usuarios [BASE]

- Controlador: `UsuarioRestController` (`backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/input/controladorUsuarios/controlador/UsuarioRestController.java`), base `${url.application}usuarios`, 10 endpoints.
- Inyecta: `UsuarioCUIntPuerto`, `MapperUsuarioInfraestructuraDominio`, `ProcesadorArchivos<UsuarioDTOPeticion>`, `ValidadorPeticionesExcel<UsuarioDTOPeticion>`.
- Nota: Usuarios de Julián. Las reglas `GET usuarios`, `paginado`, `filtro`, `funcionarios` y `tipos` restringidas a SG/DEC son una adición [EXTENSIÓN] a `ConfiguracionSeguridad` (commit `ac4fb00`); en el primer commit `GET usuarios/**` era `authenticated()`.

| # | Mét. | Ruta completa | Rol efectivo | @PreAuthorize | Método del controlador | DTO petición | DTO respuesta | Caso de uso | Tablas | Códigos de error |
|---|---|---|---|---|---|---|---|---|---|---|
| 9 | GET | `/api/unicauca/fiet/consejo/usuarios/tipos` | SG, DEC | SG, DEC | `getTiposUsuarios` | — | TipoUsuarioDTORespuesta[] | UsuarioCU.getTiposUsuario | solo lectura · L×2 | 7 · 401/403 |
| 10 | GET | `/api/unicauca/fiet/consejo/usuarios/paginado` | SG, DEC | SG, DEC | `indexPaginado` | `pagina`; `tamanio` | PaginacionRespuestaDTO<UsuarioLivianoDTORespuesta> | UsuarioCU.getUsuarios | solo lectura · L×1 | 6, 7 · 401/403 |
| 11 | GET | `/api/unicauca/fiet/consejo/usuarios/filtro` | SG, DEC | SG, DEC | `getUsuariosByNombresApellidos` | `nombreCompleto` (opc.); `pagina`; `tamanio` | PaginacionRespuestaDTO<UsuarioLivianoDTORespuesta> | UsuarioCU.getUsuariosByNombreCompleto | solo lectura · L×1 | 6, 7 · 401/403 |
| 12 | GET | `/api/unicauca/fiet/consejo/usuarios` | SG, DEC | SG, DEC | `index` | — | UsuarioLivianoDTORespuesta[] | UsuarioCU.getUsuarios | solo lectura · L×1 | 6, 7 · 401/403 |
| 13 | GET | `/api/unicauca/fiet/consejo/usuarios/funcionarios` | SG, DEC | SG, DEC | `getFuncionarios` | — | UsuarioLivianoDTORespuesta[] | UsuarioCU.getFuncionarios | solo lectura · L×1 | — · 401/403 |
| 14 | GET | `/api/unicauca/fiet/consejo/usuarios/{uuidUsuario}` | AUTH | — | `getUsuario` | — | UsuarioDTORespuesta | UsuarioCU.getUsuario | solo lectura · L×1 | 3 · 401/403 |
| 15 | POST | `/api/unicauca/fiet/consejo/usuarios` | SG, DEC | SG, DEC | `crearUsuario` | UsuarioDTOPeticion; `tipoUsuario` | UsuarioDTORespuesta | UsuarioCU.crearUsuario | E: logs, usuarios · L×2 | 1, 2, 3, 4, 6 · 400/500-BD/401/403 |
| 16 | POST | `/api/unicauca/fiet/consejo/usuarios/cargar/archivo` | SG, DEC | SG, DEC | `crearUsuarios` | archivo `file` | UsuarioDTORespuesta[] | UsuarioCU.crearUsuarios | E: logs, usuarios · L×2 | 1, 2, 3, 4, 6, 7 · 500-BD/401/403 |
| 17 | PUT | `/api/unicauca/fiet/consejo/usuarios/{uuidUsuario}` | SG, DEC | SG, DEC | `actualizarUsuario` | UsuarioActualizarDTOPeticion | UsuarioDTORespuesta | UsuarioCU.actualizarUsuario | E: logs, usuarios · L×1 | 1, 2, 3, 4, 6 · 400/500-BD/401/403 |
| 18 | PATCH | `/api/unicauca/fiet/consejo/usuarios/{uuidUsuario}` | AUTH | — | `actualizarContraseña` | CambioContraseñaDTOPeticion | 200 sin cuerpo | UsuarioCU.cambiarContraseña | E: logs, usuarios · L×0 | 1, 3, 5 · 400/500-BD/401/403 |

### 3.5 Tipos de solicitud (Julián) [BASE]

- Controlador: `TiposSolicitudRestController` (`backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/input/controladorTiposSolicitud/controlador/TiposSolicitudRestController.java`), base `${url.application}tipos/solicitudes`, 10 endpoints.
- Inyecta: `TipoSolicitudCUIntPuerto`, `MapperTipoSolicitudInfraestructuraDominio`, `ProcesadorArchivos<TipoSolicitudDTOPeticion>`, `ValidadorPeticionesExcel<TipoSolicitudDTOPeticion>`.
- Nota: Tipos de solicitud de Julián. Ver la observación 1 de la sección 5 sobre el orden de matchers.

| # | Mét. | Ruta completa | Rol efectivo | @PreAuthorize | Método del controlador | DTO petición | DTO respuesta | Caso de uso | Tablas | Códigos de error |
|---|---|---|---|---|---|---|---|---|---|---|
| 19 | GET | `/api/unicauca/fiet/consejo/tipos/solicitudes/paginado` | PÚBLICO | SG, DEC | `indexPaginado` | `pagina`; `tamanio` | PaginacionRespuestaDTO<TipoSolicitudDTORespuesta> | TipoSolicitudCU.getTiposSolicitud | solo lectura · L×1 | 6, 7 |
| 20 | GET | `/api/unicauca/fiet/consejo/tipos/solicitudes/filtro` | PÚBLICO | SG, DEC | `getTiposDeSolicitudFiltrado` | `nombreSolicitud` (opc.); `funcionario` (opc.); `pagina`; `tamanio` | PaginacionRespuestaDTO<TipoSolicitudDTORespuesta> | TipoSolicitudCU.getTiposSolicitud | solo lectura · L×1 | 6, 7 |
| 21 | GET | `/api/unicauca/fiet/consejo/tipos/solicitudes` | SG, DEC | SG, DEC | `index` | — | TipoSolicitudDTORespuesta[] | TipoSolicitudCU.getTiposSolicitud | solo lectura · L×1 | 6, 7 · 401/403 |
| 22 | GET | `/api/unicauca/fiet/consejo/tipos/solicitudes/{uuidTipoSolicitud}` | PÚBLICO | AUTH | `getTipoSolicitud` | — | TipoSolicitudDTORespuesta | TipoSolicitudCU.getTipoSolicitud | solo lectura · L×1 | 3 |
| 23 | POST | `/api/unicauca/fiet/consejo/tipos/solicitudes` | SG, DEC | SG, DEC | `crearTipoSolicitud` | TipoSolicitudDTOPeticion | TipoSolicitudDTORespuesta | TipoSolicitudCU.crearTipoSolicitud | E: logs, tiposSolicitudes · L×2 | 1, 3, 4 · 400/500-BD/401/403 |
| 24 | POST | `/api/unicauca/fiet/consejo/tipos/solicitudes/cargar/archivo` | SG, DEC | SG, DEC | `crearTiposSolicitud` | archivo `file` | TipoSolicitudDTORespuesta[] | TipoSolicitudCU.crearTiposSolicitud | E: logs, tiposSolicitudes · L×2 | 1, 3, 4 · 500-BD/401/403 |
| 25 | PUT | `/api/unicauca/fiet/consejo/tipos/solicitudes/{uuidTipoSolicitud}` | SG, DEC | SG, DEC | `actualizarTipoSolicitud` | TipoSolicitudDTOPeticion | TipoSolicitudDTORespuesta | TipoSolicitudCU.actualizarTipoSolicitud | E: logs, tiposSolicitudes · L×2 | 1, 3, 4 · 400/500-BD/401/403 |
| 26 | GET | `/api/unicauca/fiet/consejo/tipos/solicitudes/perfil` | PÚBLICO | — | `getTiposPorPerfil` | `perfil` | TipoSolicitudDTORespuesta[] | TipoSolicitudCU.getTiposSolicitudesPorPerfil | solo lectura · L×1 | 7 |
| 27 | GET | `/api/unicauca/fiet/consejo/tipos/solicitudes/perfil/paginado` | PÚBLICO | AUTH | `getTiposSolicitudesPorPerfilSolicitante` | `perfil`; `pagina`; `tamanio` | PaginacionRespuestaDTO<TipoSolicitudDTORespuesta> | TipoSolicitudCU.getTiposSolicitudesPorPerfilSolicitante | solo lectura · L×1 | 6, 7 |
| 28 | GET | `/api/unicauca/fiet/consejo/tipos/solicitudes/perfil/filtro` | PÚBLICO | AUTH | `getTiposSolicitudesPorNombreYPerfilSolicitante` | `nombre` (opc.); `perfil`; `pagina`; `tamanio` | PaginacionRespuestaDTO<TipoSolicitudDTORespuesta> | TipoSolicitudCU.getTiposSolicitudesPorNombreYPerfilSolicitante | solo lectura · L×1 | 6, 7 |

### 3.6 Solicitudes y Orden del día (Julián) [BASE]

- Controlador: `SolicitudesRestController` (`backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/input/controladorSolicitudes/controlador/SolicitudesRestController.java`), base `${url.application}solicitudes`, 24 endpoints.
- Inyecta: `OrdenDelDiaCUIntPuerto`, `SolicitudCUintPuerto`, `MapperSolicitudesInfraestructuraDominio`, `ValidadorAnexosService`.
- Nota: Solicitudes del Consejo de Facultad y órdenes del día de Julián. Usa `ValidadorAnexosService` (en `infraestructura/input/controladorSolicitudes`) para casar anexos con archivos antes de llamar al caso de uso; ese servicio lanza `ErrorMalFormatoExcepcion` directamente (código 6) sin pasar por el formateador. El método Java del `PUT /solicitudes/{uuidSolicitud}` se llama `actualizarOrdenDelDia` (nombre heredado, sobrecarga el del PUT de órdenes del día). `GET fun/buscar/` está declarado sin barra inicial y con barra final (`@GetMapping("fun/buscar/")`); Spring antepone la barra inicial al combinar, así que la ruta efectiva sería `.../solicitudes/fun/buscar/` (inferido, NO VERIFICADO en ejecución).

| # | Mét. | Ruta completa | Rol efectivo | @PreAuthorize | Método del controlador | DTO petición | DTO respuesta | Caso de uso | Tablas | Códigos de error |
|---|---|---|---|---|---|---|---|---|---|---|
| 29 | GET | `/api/unicauca/fiet/consejo/solicitudes/orden-del-dia/paginado` | SG, DEC | SG, DEC | `indexPaginado` | `pagina`; `tamanio` | PaginacionRespuestaDTO<OrdenDelDiaDTORespuesta> | OrdenDelDiaCU.getOrdenesDelDia | solo lectura · L×1 | 6, 7 · 401/403 |
| 30 | GET | `/api/unicauca/fiet/consejo/solicitudes/orden-del-dia` | SG, DEC | SG, DEC | `index` | — | OrdenDelDiaDTORespuesta[] | OrdenDelDiaCU.getOrdenesDelDia | solo lectura · L×1 | 6, 7 · 401/403 |
| 31 | GET | `/api/unicauca/fiet/consejo/solicitudes/orden-del-dia/{uuidOrdenDelDia}` | SG, DEC | SG, DEC | `getOrdenDelDia` | — | OrdenDelDiaDTORespuesta | OrdenDelDiaCU.getOrdenDelDia | solo lectura · L×1 | 3 · 401/403 |
| 32 | POST | `/api/unicauca/fiet/consejo/solicitudes/orden-del-dia` | SG, DEC | SG, DEC | `crearOrdenDelDia` | OrdenDelDiaDTOPeticion | OrdenDelDiaDTORespuesta | OrdenDelDiaCU.crearOrdenDelDia | E: logs, ordenesDelDia · L×1 | 1, 3 · 400/500-BD/401/403 |
| 33 | PUT | `/api/unicauca/fiet/consejo/solicitudes/orden-del-dia/{uuidOrdenDelDia}` | SG, DEC | SG, DEC | `actualizarOrdenDelDia` | OrdenDelDiaDTOPeticion | OrdenDelDiaDTORespuesta | OrdenDelDiaCU.actualizarOrdenDelDia | E: logs, ordenesDelDia · L×1 | 1, 3 · 400/500-BD/401/403 |
| 34 | GET | `/api/unicauca/fiet/consejo/solicitudes/paginado` | AUTH | SG, FUN, DEC | `solicitudesPaginado` | `pagina`; `tamanio` | PaginacionRespuestaDTO<SolicitudDTORespuesta> | SolicitudCU.getSolicitudes | solo lectura · L×1 | 6, 7 · 401/403 |
| 35 | GET | `/api/unicauca/fiet/consejo/solicitudes` | AUTH | SG, FUN, DEC | `solicitudesIndex` | — | SolicitudDTORespuesta[] | SolicitudCU.getSolicitudes | solo lectura · L×1 | 6, 7 · 401/403 |
| 36 | GET | `/api/unicauca/fiet/consejo/solicitudes/{uuidSolicitud}` | AUTH | SG, FUN, DEC | `getSolicitud` | — | SolicitudDTORespuesta | SolicitudCU.getSolicitud | solo lectura · L×1 | 3 · 401/403 |
| 37 | POST | `/api/unicauca/fiet/consejo/solicitudes` | AUTH | AUTH | `enviarSolicitud` | parte `solicitud`: SolicitudDTOPeticion; parte `archivos`: List<MultipartFile> | SolicitudDTORespuesta | SolicitudCU.crearSolicitud | E: logs, solicitudes · L×3 | 1, 3, 6 · 400/500-BD/401/403 |
| 38 | POST | `/api/unicauca/fiet/consejo/solicitudes/public` | PÚBLICO | SG, FUN, DEC | `enviarSolicitudPublica` | parte `solicitud`: SolicitudPublicaDTOPeticion; parte `archivos`: List<MultipartFile> | SolicitudDTORespuesta | SolicitudCU.crearSolicitudPublica | E: logs, solicitudes · L×2 | 1, 3 · 400/500-BD |
| 39 | PUT | `/api/unicauca/fiet/consejo/solicitudes/{uuidSolicitud}` | AUTH | SG, DEC | `actualizarOrdenDelDia` | SolicitudActualizarDTOPeticion | SolicitudDTORespuesta | SolicitudCU.actualizarSolicitud | E: logs, solicitudes · L×2 | 1, 3, 6 · 400/500-BD/401/403 |
| 40 | GET | `/api/unicauca/fiet/consejo/solicitudes/funcionario/{uuidFuncionario}` | AUTH | SG, DEC | `getSolicitudesPorFuncionario` | `pagina`; `tamanio` | PaginacionRespuestaDTO<SolicitudDTORespuesta> | SolicitudCU.getSolicitudesPorFuncionario | solo lectura · L×1 | 6, 7 · 401/403 |
| 41 | GET | `/api/unicauca/fiet/consejo/solicitudes/orden-del-dia/{uuidOrdenDelDia}/solicitudes` | SG, DEC | SG, DEC | `getSolicitudesPorOrdenDelDia` | — | SolicitudDTORespuesta[] | SolicitudCU.getSolicitudesPorOrdenDelDia | solo lectura · L×1 | 7 · 401/403 |
| 42 | GET | `/api/unicauca/fiet/consejo/solicitudes/estado` | AUTH | AUTH | `getSolicitudesPorEstado` | `estado` | SolicitudDTORespuesta[] | SolicitudCU.getSolicitudesPorEstado | solo lectura · L×1 | 7 · 401/403 |
| 43 | GET | `/api/unicauca/fiet/consejo/solicitudes/buscar` | AUTH | SG, FUN, DEC | `buscarSolicitudesPorNombre` | `filtro`; `pagina`; `tamanio` | PaginacionRespuestaDTO<SolicitudDTORespuesta> | SolicitudCU.buscarSolicitudesPorNombre | solo lectura · L×1 | 6, 7 · 401/403 |
| 44 | GET | `/api/unicauca/fiet/consejo/solicitudes/buscar/solicitante` | AUTH | SG, DEC | `buscarSolicitudesPorSolicitante` | `solicitud`; `solicitante`; `estado`; `pagina`; `tamanio` | PaginacionRespuestaDTO<SolicitudDTORespuesta> | SolicitudCU.buscarSolicitudesPorSolicitante | solo lectura · L×1 | 6, 7 · 401/403 |
| 45 | GET | `/api/unicauca/fiet/consejo/solicitudes/fun/buscar/` | AUTH | SG, FUN, DEC | `buscarSolicitudesPorNombreYFuncionario` | `uuidFuncionario`; `filtro`; `pagina`; `tamanio` | PaginacionRespuestaDTO<SolicitudDTORespuesta> | SolicitudCU.buscarSolicitudesPorNombreYFuncionario | solo lectura · L×1 | 6, 7 · 401/403 |
| 46 | GET | `/api/unicauca/fiet/consejo/solicitudes/orden-del-dia/buscar` | SG, DEC | SG, DEC | `buscarOrdenDelDiaPorNumeroActa` | `filtro`; `pagina`; `tamanio` | PaginacionRespuestaDTO<OrdenDelDiaDTORespuesta> | OrdenDelDiaCU.buscarOrdenDelDiaPorNumeroActa | solo lectura · L×1 | 7 · 401/403 |
| 47 | GET | `/api/unicauca/fiet/consejo/solicitudes/anexos/{uuidSolicitud}/{fileName}` | AUTH | AUTH | `descargarAnexo` | — | archivo (descarga binaria) | — | ninguna (lee `uploads/` en el controlador) | — · 401/403 |
| 48 | GET | `/api/unicauca/fiet/consejo/solicitudes/anexos/download` | AUTH | SG, DEC | `descargarAnexos` | `uuidOrden`; `nombreOrden` | archivo .zip (byte[]) | SolicitudCU.generarZipAnexosPorOrdenDelDia | solo lectura · L×1 | 6, 7 · 401/403 |
| 49 | GET | `/api/unicauca/fiet/consejo/solicitudes/exportar` | AUTH | SG, DEC | `exportarOrden` | `uuidOrden` | archivo .docx (byte[]) | OrdenDelDiaCU.generarOrdenDelDia | solo lectura · L×3 | 3 · 401/403 |
| 50 | GET | `/api/unicauca/fiet/consejo/solicitudes/exportar/respuestas` | AUTH | SG, DEC | `exportarOrdenRespuestas` | `uuidOrden` | archivo .docx (byte[]) | OrdenDelDiaCU.generarOrdenDelDiaConRespuestas | solo lectura · L×3 | 3 · 401/403 |
| 51 | GET | `/api/unicauca/fiet/consejo/solicitudes/exportar/reunion` | AUTH | SG, DEC | `exportarOrdenDesarrolloReunion` | `uuidOrden` | archivo .docx (byte[]) | OrdenDelDiaCU.generarOrdenDelDiaMerge | solo lectura · L×3 | 3 · 401/403 |
| 52 | GET | `/api/unicauca/fiet/consejo/solicitudes/orden-del-dia/estado` | AUTH | AUTH | `getOrdenesDelDiaPorEstado` | `estado` | OrdenDelDiaDTORespuesta[] | OrdenDelDiaCU.getOrdenesDelDiaPorEstado | solo lectura · L×1 | 7 · 401/403 |

### 3.7 Respuestas (Julián) [BASE]

- Controlador: `RespuestaRestController` (`backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/input/controladorRespuestas/controlador/RespuestaRestController.java`), base `${url.application}respuestas`, 10 endpoints.
- Inyecta: `RespuestaCUIntPuerto`, `MapperRespuestaInfraestructuraDominio`.
- Nota: Respuestas a solicitudes de Julián. `descargarRespuesta` lee el archivo directo de `${app.uploads.base-path}/respuestas/...` dentro del controlador (sin caso de uso).

| # | Mét. | Ruta completa | Rol efectivo | @PreAuthorize | Método del controlador | DTO petición | DTO respuesta | Caso de uso | Tablas | Códigos de error |
|---|---|---|---|---|---|---|---|---|---|---|
| 53 | GET | `/api/unicauca/fiet/consejo/respuestas/paginado` | AUTH | AUTH | `indexPaginado` | `pagina`; `tamanio` | PaginacionRespuestaDTO<RespuestaDTORespuesta> | RespuestaCU.getRespuestas | solo lectura · L×1 | 6, 7 · 401/403 |
| 54 | GET | `/api/unicauca/fiet/consejo/respuestas/paginado/nombre-solicitud` | AUTH | AUTH | `indexPaginadoPorNombreSolicitud` | `pagina`; `tamanio`; `nombreSolicitud` | PaginacionRespuestaDTO<RespuestaDTORespuesta> | RespuestaCU.getRespuestasPorNombreSolicitud | solo lectura · L×1 | 6, 7 · 401/403 |
| 55 | GET | `/api/unicauca/fiet/consejo/respuestas/paginado/funcionario` | AUTH | SG, FUN, DEC | `indexPaginadoPorFuncionario` | `pagina`; `tamanio`; `uuidFuncionario` | PaginacionRespuestaDTO<RespuestaDTORespuesta> | RespuestaCU.getRespuestasPorFuncionario | solo lectura · L×1 | 6, 7 · 401/403 |
| 56 | GET | `/api/unicauca/fiet/consejo/respuestas/paginado/funcionario-nombre-solicitud` | AUTH | SG, FUN, DEC | `indexPaginadoPorFuncionarioYNombreSolicitud` | `pagina`; `tamanio`; `uuidFuncionario`; `nombreSolicitud` | PaginacionRespuestaDTO<RespuestaDTORespuesta> | RespuestaCU.getRespuestasPorFuncionarioNombreSolicitud | solo lectura · L×1 | 6, 7 · 401/403 |
| 57 | GET | `/api/unicauca/fiet/consejo/respuestas/{uuidRespuesta}` | AUTH | AUTH | `getRespuesta` | — | RespuestaDTORespuesta | RespuestaCU.getRespuesta | solo lectura · L×1 | 3 · 401/403 |
| 58 | GET | `/api/unicauca/fiet/consejo/respuestas/solicitud/{uuidSolicitud}` | AUTH | AUTH | `getRespuestaPorSolicitud` | — | RespuestaDTORespuesta | RespuestaCU.getRespuestaPorSolicitud | solo lectura · L×1 | 3 · 401/403 |
| 59 | POST | `/api/unicauca/fiet/consejo/respuestas/{uuidSolicitud}` | SG, DEC, FUN | SG, DEC | `registrarRespuesta` | RespuestaDTOPeticion | RespuestaDTORespuesta | RespuestaCU.registrarRespuesta | E: logs, respuestas · L×2 | 1, 3, 4 · 400/500-BD/401/403 |
| 60 | POST | `/api/unicauca/fiet/consejo/respuestas/{uuidRespuesta}/archivo` | SG, DEC, FUN | SG, FUN, DEC | `responderSolicitud` | archivo `archivo` | RespuestaDTORespuesta | RespuestaCU.responderSolicitud | E: logs, respuestas · L×1 | 1, 3, 4 · 500-BD/401/403 |
| 61 | PATCH | `/api/unicauca/fiet/consejo/respuestas/{uuidRespuesta}/archivo` | AUTH | SG, FUN, DEC | `eliminarArchivoRespuesta` | — | RespuestaDTORespuesta | RespuestaCU.eliminarArchivoRespuesta | E: logs, respuestas · L×1 | 1, 3, 4 · 500-BD/401/403 |
| 62 | GET | `/api/unicauca/fiet/consejo/respuestas/{uuidRespuesta}/{fileName}` | AUTH | AUTH | `descargarRespuesta` | — | archivo (descarga binaria) | — | ninguna (lee `uploads/` en el controlador) | — · 401/403 |

### 3.8 Asignaturas [EXTENSIÓN]

- Controlador: `AsignaturaRestController` (`backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/input/controladorAsignaturas/controlador/AsignaturaRestController.java`), base `${url.application}asignaturas`, 7 endpoints.
- Inyecta: `AsignaturaCUIntPuerto`, `MapperAsignaturaInfraestructuraDominio`, `ProcesadorArchivos<FilaAsignaturaExcelDTOPeticion>`, `ValidadorPeticionesExcel<FilaAsignaturaExcelDTOPeticion>`, `CatalogoAcademicoExcel`.
- Nota: Catálogo de asignaturas (el código `ISI-001` lo genera el backend). Carga masiva Excel con `ProcesadorArchivos` + `ValidadorPeticionesExcel` + `CatalogoAcademicoExcel`.

| # | Mét. | Ruta completa | Rol efectivo | @PreAuthorize | Método del controlador | DTO petición | DTO respuesta | Caso de uso | Tablas | Códigos de error |
|---|---|---|---|---|---|---|---|---|---|---|
| 63 | POST | `/api/unicauca/fiet/consejo/asignaturas/cargar/archivo` | SG, DEC | SG, DEC | `crearAsignaturas` | archivo `file` | AsignaturaDTORespuesta[] | AsignaturaCU.crearAsignaturas | E: ASIGNATURA, logs · L×2 | 1, 2, 3, 6, 7 · 500-BD/401/403 |
| 64 | POST | `/api/unicauca/fiet/consejo/asignaturas` | SG, DEC | SG, DEC | `crearAsignatura` | AsignaturaDTOPeticion | AsignaturaDTORespuesta | AsignaturaCU.crearAsignatura | E: ASIGNATURA, logs · L×2 | 1, 2, 3, 6, 7 · 400/500-BD/401/403 |
| 65 | GET | `/api/unicauca/fiet/consejo/asignaturas/paginado` | SG, DEC, FA | SG, DEC, FA | `getAsignaturasPaginado` | `pagina`; `tamanio` | PaginacionRespuestaDTO<AsignaturaDTORespuesta> | AsignaturaCU.getAsignaturasPaginado | solo lectura · L×1 | 6 · 401/403 |
| 66 | GET | `/api/unicauca/fiet/consejo/asignaturas/filtro` | SG, DEC, FA | SG, DEC, FA | `getAsignaturasPorFiltro` | `texto` (opc.); `uuidProgramaAcademico` (opc.); `pagina`; `tamanio` | PaginacionRespuestaDTO<AsignaturaDTORespuesta> | AsignaturaCU.getAsignaturasPorFiltro | solo lectura · L×2 | 3, 6 · 401/403 |
| 67 | GET | `/api/unicauca/fiet/consejo/asignaturas/programa/{uuidPrograma}` | SG, DEC, FA | SG, DEC, FA | `getAsignaturasPorPrograma` | — | AsignaturaDTORespuesta[] | AsignaturaCU.getAsignaturasPorPrograma | solo lectura · L×2 | 3 · 401/403 |
| 68 | GET | `/api/unicauca/fiet/consejo/asignaturas/{uuidAsignatura}` | SG, DEC, FA | SG, DEC, FA | `getAsignatura` | — | AsignaturaDTORespuesta | AsignaturaCU.getAsignatura | solo lectura · L×1 | 3 · 401/403 |
| 69 | PUT | `/api/unicauca/fiet/consejo/asignaturas/{uuidAsignatura}` | SG, DEC | SG, DEC | `actualizarAsignatura` | AsignaturaActualizarDTOPeticion | AsignaturaDTORespuesta | AsignaturaCU.actualizarAsignatura | E: ASIGNATURA, logs · L×2 | 1, 2, 3, 6 · 400/500-BD/401/403 |

### 3.9 Estudiantes [EXTENSIÓN]

- Controlador: `EstudianteRestController` (`backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/input/controladorEstudiantes/controlador/EstudianteRestController.java`), base `${url.application}estudiantes`, 9 endpoints.
- Inyecta: `EstudianteCUIntPuerto`, `MapperEstudianteInfraestructuraDominio`, `ProcesadorArchivos<FilaEstudianteExcelDTOPeticion>`, `ValidadorPeticionesExcel<FilaEstudianteExcelDTOPeticion>`, `AgrupadorEstudiantesExcel`, `CatalogoAcademicoExcel`.
- Nota: Estudiantes y su información de materias; carga masiva Excel (`AgrupadorEstudiantesExcel` agrupa filas por estudiante). Crear un estudiante pasa por `UsuarioCUIntPuerto.crearUsuario` de Julián (reutiliza el caso de uso de usuarios).

| # | Mét. | Ruta completa | Rol efectivo | @PreAuthorize | Método del controlador | DTO petición | DTO respuesta | Caso de uso | Tablas | Códigos de error |
|---|---|---|---|---|---|---|---|---|---|---|
| 70 | POST | `/api/unicauca/fiet/consejo/estudiantes/cargar/archivo` | SG, DEC | SG, DEC | `crearEstudiantes` | archivo `file` | EstudianteDTORespuesta | EstudianteCU.crearEstudiantes | E: ESTUDIANTE, logs, usuarios · L×4 | 1, 2, 3, 4, 6, 7 · 500-BD/401/403 |
| 71 | POST | `/api/unicauca/fiet/consejo/estudiantes` | SG, DEC | SG, DEC | `crearEstudiante` | EstudianteDTOPeticion | EstudianteDTORespuesta | EstudianteCU.crearEstudiante | E: ESTUDIANTE, logs, usuarios · L×4 | 1, 2, 3, 4, 6 · 400/500-BD/401/403 |
| 72 | GET | `/api/unicauca/fiet/consejo/estudiantes/paginado` | SG, DEC, FA | SG, DEC, FA | `getEstudiantesPaginado` | `pagina`; `tamanio` | PaginacionRespuestaDTO<EstudianteDTORespuesta> | EstudianteCU.getEstudiantesPaginado | solo lectura · L×1 | 6 · 401/403 |
| 73 | GET | `/api/unicauca/fiet/consejo/estudiantes/filtro` | SG, DEC, FA | SG, DEC, FA | `getEstudiantesPorFiltro` | `nombre` (opc.); `apellido` (opc.); `codigo` (opc.); `pagina`; `tamanio` | PaginacionRespuestaDTO<EstudianteDTORespuesta> | EstudianteCU.getEstudiantesPorFiltro | solo lectura · L×1 | 6 · 401/403 |
| 74 | GET | `/api/unicauca/fiet/consejo/estudiantes/mis-asignaturas` | EST | EST | `getMisAsignaturas` | — | AsignaturaMatriculadaDTORespuesta[] | EstudianteCU.getMisAsignaturas | solo lectura · L×3 | 1, 3 · 401/403 |
| 75 | GET | `/api/unicauca/fiet/consejo/estudiantes/{uuidEstudiante}` | SG, DEC, FA | SG, DEC, FA | `getEstudiante` | — | EstudianteDTORespuesta | EstudianteCU.getEstudiante | solo lectura · L×1 | 3 · 401/403 |
| 76 | PUT | `/api/unicauca/fiet/consejo/estudiantes/{uuidEstudiante}` | SG, DEC | SG, DEC | `actualizarEstudiante` | EstudianteActualizarDTOPeticion | EstudianteDTORespuesta | EstudianteCU.actualizarEstudiante | E: ESTUDIANTE, logs, usuarios · L×2 | 1, 2, 3, 6 · 400/500-BD/401/403 |
| 77 | POST | `/api/unicauca/fiet/consejo/estudiantes/{uuidEstudiante}/asignaturas` | SG, DEC | SG, DEC | `agregarAsignaturaMatriculada` | AsignaturaMatriculadaDTOPeticion | AsignaturaMatriculadaDTORespuesta | EstudianteCU.agregarAsignaturaMatriculada | E: ASIGNATURA_MATRICULADA, logs · L×4 | 1, 3, 4, 6 · 400/500-BD/401/403 |
| 78 | PATCH | `/api/unicauca/fiet/consejo/estudiantes/{uuidEstudiante}/asignaturas/{uuidMatricula}/estado` | SG, DEC | SG, DEC | `cambiarEstadoAsignatura` | CambioEstadoAsignaturaDTOPeticion | AsignaturaMatriculadaDTORespuesta | EstudianteCU.cambiarEstadoAsignatura | E: ASIGNATURA_MATRICULADA, logs · L×4 | 1, 3, 6 · 400/500-BD/401/403 |

### 3.10 Funcionarios académicos [EXTENSIÓN]

- Controlador: `FuncionarioAcademicoRestController` (`backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/input/controladorFuncionariosAcademicos/controlador/FuncionarioAcademicoRestController.java`), base `${url.application}funcionarios-academicos`, 6 endpoints.
- Inyecta: `FuncionarioAcademicoCUIntPuerto`, `MapperFuncionarioAcademicoInfraestructuraDominio`, `ProcesadorArchivos<FilaFuncionarioAcademicoExcelDTOPeticion>`, `ValidadorPeticionesExcel<FilaFuncionarioAcademicoExcelDTOPeticion>`, `DuplicadosFuncionariosAcademicosExcel`.
- Nota: Funcionarios académicos; crear pasa por `UsuarioCUIntPuerto.crearUsuario` de Julián.

| # | Mét. | Ruta completa | Rol efectivo | @PreAuthorize | Método del controlador | DTO petición | DTO respuesta | Caso de uso | Tablas | Códigos de error |
|---|---|---|---|---|---|---|---|---|---|---|
| 79 | POST | `/api/unicauca/fiet/consejo/funcionarios-academicos` | SG, DEC | SG, DEC | `crearFuncionarioAcademico` | FuncionarioAcademicoDTOPeticion | FuncionarioAcademicoDTORespuesta | FuncionarioAcademicoCU.crearFuncionarioAcademico | E: FUNCIONARIO_ACADEMICO, logs, usuarios · L×2 | 1, 2, 3, 4, 6 · 400/500-BD/401/403 |
| 80 | POST | `/api/unicauca/fiet/consejo/funcionarios-academicos/cargar/archivo` | SG, DEC | SG, DEC | `crearFuncionariosAcademicos` | archivo `file` | FuncionarioAcademicoDTORespuesta[] | FuncionarioAcademicoCU.crearFuncionariosAcademicos | E: FUNCIONARIO_ACADEMICO, logs, usuarios · L×2 | 1, 2, 3, 4, 6, 7 · 500-BD/401/403 |
| 81 | GET | `/api/unicauca/fiet/consejo/funcionarios-academicos/paginado` | SG, DEC, FA | SG, DEC, FA | `getFuncionariosAcademicosPaginado` | `pagina`; `tamanio` | PaginacionRespuestaDTO<FuncionarioAcademicoDTORespuesta> | FuncionarioAcademicoCU.getFuncionariosAcademicosPaginado | solo lectura · L×1 | 6 · 401/403 |
| 82 | GET | `/api/unicauca/fiet/consejo/funcionarios-academicos/filtro` | SG, DEC, FA | SG, DEC, FA | `getFuncionariosAcademicosPorFiltro` | `nombre` (opc.); `apellido` (opc.); `cargo` (opc.); `pagina`; `tamanio` | PaginacionRespuestaDTO<FuncionarioAcademicoDTORespuesta> | FuncionarioAcademicoCU.getFuncionariosAcademicosPorFiltro | solo lectura · L×1 | 6 · 401/403 |
| 83 | GET | `/api/unicauca/fiet/consejo/funcionarios-academicos/{uuidFuncionario}` | SG, DEC, FA | SG, DEC, FA | `getFuncionarioAcademico` | — | FuncionarioAcademicoDTORespuesta | FuncionarioAcademicoCU.getFuncionarioAcademico | solo lectura · L×1 | 3 · 401/403 |
| 84 | PUT | `/api/unicauca/fiet/consejo/funcionarios-academicos/{uuidFuncionario}` | SG, DEC | SG, DEC | `actualizarFuncionarioAcademico` | FuncionarioAcademicoActualizarDTOPeticion | FuncionarioAcademicoDTORespuesta | FuncionarioAcademicoCU.actualizarFuncionarioAcademico | E: FUNCIONARIO_ACADEMICO, logs, usuarios · L×0 | 1, 2, 3, 6 · 400/500-BD/401/403 |

### 3.11 Catálogos académicos [EXTENSIÓN]

- Controlador: `CatalogoAcademicoRestController` (`backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/input/controladorCatalogosAcademicos/controlador/CatalogoAcademicoRestController.java`), base `${url.application}catalogos-academicos`, 7 endpoints.
- Inyecta: `TipoSolicitudAcademicaCUIntPuerto`, `EtapaSolicitudAcademicaCUIntPuerto`, `TipoAnexoAcademicoCUIntPuerto`, `SituacionAcademicaAsignaturaCUIntPuerto`, `AsignacionFuncionarioAcademicoCUIntPuerto`, `ProgramaAcademicoCUIntPuerto`, `MapperCatalogoAcademicoInfraestructuraDominio`.
- Nota: Catálogos de solo lectura más la asignación del funcionario por tipo. Los GET son AUTH (cualquier usuario autenticado).

| # | Mét. | Ruta completa | Rol efectivo | @PreAuthorize | Método del controlador | DTO petición | DTO respuesta | Caso de uso | Tablas | Códigos de error |
|---|---|---|---|---|---|---|---|---|---|---|
| 85 | GET | `/api/unicauca/fiet/consejo/catalogos-academicos/tipos-solicitud` | AUTH | AUTH | `getTiposSolicitud` | — | TipoSolicitudAcademicaDTORespuesta[] | TipoSolicitudAcademicaCU.getTiposSolicitudAcademica | solo lectura · L×1 | — · 401/403 |
| 86 | GET | `/api/unicauca/fiet/consejo/catalogos-academicos/tipos-solicitud/{uuidTipo}/etapas` | AUTH | AUTH | `getEtapasPorTipo` | — | EtapaSolicitudAcademicaDTORespuesta[] | EtapaSolicitudAcademicaCU.getEtapasPorTipo | solo lectura · L×2 | 3 · 401/403 |
| 87 | GET | `/api/unicauca/fiet/consejo/catalogos-academicos/tipos-solicitud/{uuidTipo}/tipos-anexo` | AUTH | AUTH | `getTiposAnexoPorTipo` | — | TipoAnexoAcademicoDTORespuesta[] | TipoAnexoAcademicoCU.getTiposAnexoPorTipo | solo lectura · L×2 | 3 · 401/403 |
| 88 | GET | `/api/unicauca/fiet/consejo/catalogos-academicos/etiquetas` | AUTH | AUTH | `getEtiquetasPorRol` | `rol` | EtapaEtiquetaRolDTORespuesta[] | EtapaSolicitudAcademicaCU.getEtiquetasPorRol | solo lectura · L×1 | 6 · 401/403 |
| 89 | GET | `/api/unicauca/fiet/consejo/catalogos-academicos/situaciones` | AUTH | AUTH | `getSituaciones` | — | SituacionAcademicaAsignaturaDTORespuesta[] | SituacionAcademicaAsignaturaCU.getSituaciones | solo lectura · L×1 | — · 401/403 |
| 90 | GET | `/api/unicauca/fiet/consejo/catalogos-academicos/programas` | AUTH | AUTH | `getProgramas` | — | ProgramaAcademicoDTORespuesta[] | ProgramaAcademicoCU.getProgramasAcademicos | solo lectura · L×1 | — · 401/403 |
| 91 | PUT | `/api/unicauca/fiet/consejo/catalogos-academicos/tipos-solicitud/{uuidTipo}/funcionario` | SG, DEC | SG, DEC | `asignarFuncionarioAcademico` | AsignacionFuncionarioAcademicoDTOPeticion | TipoSolicitudAcademicaDTORespuesta | AsignacionFuncionarioAcademicoCU.asignarFuncionarioAcademico | E: TIPO_SOLICITUD_ACADEMICA, logs · L×2 | 1, 3 · 400/500-BD/401/403 |

### 3.12 Solicitudes académicas (motor común) [EXTENSIÓN]

- Controlador: `SolicitudAcademicaRestController` (`backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/input/controladorSolicitudesAcademicas/controlador/SolicitudAcademicaRestController.java`), base `${url.application}solicitudes-academicas`, 9 endpoints.
- Inyecta: `ConsultaSolicitudAcademicaCUIntPuerto`, `AnexoAcademicoCUIntPuerto`, `ResolucionAcademicaCUIntPuerto`, `MapperSolicitudAcademicaInfraestructuraDominio`.
- Nota: Motor común: bandejas por rol, detalle, historial, anexos y escaneo de la Resolución. El rol del actor nunca llega del cliente: `ConsultaSolicitudAcademicaCUIntPuerto.resolverActor` lo deduce del token.

| # | Mét. | Ruta completa | Rol efectivo | @PreAuthorize | Método del controlador | DTO petición | DTO respuesta | Caso de uso | Tablas | Códigos de error |
|---|---|---|---|---|---|---|---|---|---|---|
| 92 | GET | `/api/unicauca/fiet/consejo/solicitudes-academicas/estudiante` | EST | EST | `getBandejaEstudiante` | — | SolicitudAcademicaResumenDTORespuesta[] | ConsultaSolicitudAcademicaCU.getBandejaEstudiante | solo lectura · L×5 | 1, 3, 4 · 401/403 |
| 93 | GET | `/api/unicauca/fiet/consejo/solicitudes-academicas/funcionario` | FA | FA | `getBandejaFuncionario` | — | SolicitudAcademicaResumenDTORespuesta[] | ConsultaSolicitudAcademicaCU.getBandejaFuncionario | solo lectura · L×5 | 1, 3, 4 · 401/403 |
| 94 | GET | `/api/unicauca/fiet/consejo/solicitudes-academicas/decano` | DEC | DEC | `getBandejaDecano` | — | SolicitudAcademicaResumenDTORespuesta[] | ConsultaSolicitudAcademicaCU.getBandejaDecano | solo lectura · L×5 | 1, 3, 4 · 401/403 |
| 95 | GET | `/api/unicauca/fiet/consejo/solicitudes-academicas/{uuidSolicitud}` | EST, FA, DEC | EST, FA, DEC | `getDetalle` | — | SolicitudAcademicaDetalleDTORespuesta | ConsultaSolicitudAcademicaCU.getDetalle | solo lectura · L×5 | 1, 3, 4 · 401/403 |
| 96 | GET | `/api/unicauca/fiet/consejo/solicitudes-academicas/{uuidSolicitud}/historial` | EST, FA, DEC | EST, FA, DEC | `getHistorial` | — | HistorialSolicitudAcademicaDTORespuesta[] | ConsultaSolicitudAcademicaCU.getHistorial | solo lectura · L×4 | 1, 3 · 401/403 |
| 97 | POST | `/api/unicauca/fiet/consejo/solicitudes-academicas/{uuidSolicitud}/anexos` | EST, FA | EST, FA | `adjuntarAnexo` | archivo `archivo`; `tipoAnexo` (opc.) | AnexoAcademicoDTORespuesta | ConsultaSolicitudAcademicaCU.resolverActor<br>AnexoAcademicoCU.adjuntarAnexo | E: ANEXO_ACADEMICO, logs · L×4 | 1, 3, 4, 6 · 500-BD/401/403 |
| 98 | GET | `/api/unicauca/fiet/consejo/solicitudes-academicas/{uuidSolicitud}/anexos/{uuidAnexo}` | EST, FA, DEC | EST, FA, DEC | `descargarAnexo` | — | archivo (descarga binaria) | ConsultaSolicitudAcademicaCU.descargarAnexo | ninguna (lee `uploads/` en el controlador) | 1, 3, 4 · 401/403 |
| 99 | POST | `/api/unicauca/fiet/consejo/solicitudes-academicas/{uuidSolicitud}/resolucion` | FA | FA | `adjuntarResolucion` | archivo `archivo` | ResolucionAcademicaDTORespuesta | ConsultaSolicitudAcademicaCU.resolverActor<br>ResolucionAcademicaCU.adjuntarResolucion | E: RESOLUCION_ACADEMICA, logs · L×4 | 1, 3, 4, 6 · 500-BD/401/403 |
| 100 | GET | `/api/unicauca/fiet/consejo/solicitudes-academicas/{uuidSolicitud}/resolucion` | EST, FA, DEC | EST, FA, DEC | `descargarResolucion` | — | archivo (descarga binaria) | ConsultaSolicitudAcademicaCU.resolverActor<br>ResolucionAcademicaCU.obtenerResolucion | solo lectura · L×4 | 1, 3, 4 · 401/403 |

### 3.13 Cancelación de Matrícula [EXTENSIÓN]

- Controlador: `CancelacionMatriculaRestController` (`backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/input/controladorCancelacionesMatricula/controlador/CancelacionMatriculaRestController.java`), base `${url.application}cancelaciones-matricula`, 8 endpoints.
- Inyecta: `CancelacionMatriculaCUIntPuerto`, `ConsultaCancelacionMatriculaCUIntPuerto`, `TramiteCancelacionMatriculaCUIntPuerto`, `MapperCancelacionMatriculaInfraestructuraDominio`.
- Nota: Radicación y trámite de Cancelación de Matrícula. `{uuidSolicitud}/funcionario/**` solo FA y `{uuidSolicitud}/decano/**` solo DEC. `ObservacionDTOPeticion` vive en el paquete de este módulo y lo reutilizan los controladores de Cancelación de Asignatura y de Examen Supletorio.

| # | Mét. | Ruta completa | Rol efectivo | @PreAuthorize | Método del controlador | DTO petición | DTO respuesta | Caso de uso | Tablas | Códigos de error |
|---|---|---|---|---|---|---|---|---|---|---|
| 101 | GET | `/api/unicauca/fiet/consejo/cancelaciones-matricula/formulario` | EST | EST | `getFormulario` | — | FormularioCancelacionMatriculaDTORespuesta | ConsultaCancelacionMatriculaCU.getFormulario | solo lectura · L×5 | 1, 3, 4 · 401/403 |
| 102 | POST | `/api/unicauca/fiet/consejo/cancelaciones-matricula` | EST | EST | `radicar` | `motivo` (opc.); multipart con partes de archivo dinámicas | RadicacionCancelacionMatriculaDTORespuesta | ConsultaCancelacionMatriculaCU.getEstudianteAutenticado<br>CancelacionMatriculaCU.radicarCancelacionMatricula | E: ANEXO_ACADEMICO, ASIGNATURA_SOLICITUD_ACADEMICA, HISTORIAL_SOLICITUD_ACADEMICA, SOLICITUD_ACADEMICA, SOLICITUD_CANCELACION_MATRICULA, logs · L×6 | 1, 3, 4, 6 · 500-BD/401/403 |
| 103 | GET | `/api/unicauca/fiet/consejo/cancelaciones-matricula/{uuidSolicitud}` | EST, FA, DEC | EST, FA, DEC | `getDetalle` | — | CancelacionMatriculaDetalleDTORespuesta | ConsultaCancelacionMatriculaCU.getDetalle | solo lectura · L×7 | 1, 3, 4 · 401/403 |
| 104 | POST | `/api/unicauca/fiet/consejo/cancelaciones-matricula/{uuidSolicitud}/funcionario/rechazar` | FA | FA | `rechazarPorFuncionario` | ObservacionDTOPeticion | CancelacionMatriculaDetalleDTORespuesta | TramiteCancelacionMatriculaCU.rechazarPorFuncionario<br>ConsultaCancelacionMatriculaCU.getDetalle | E: HISTORIAL_SOLICITUD_ACADEMICA, SOLICITUD_ACADEMICA, logs · L×7 | 1, 3, 4, 6 · 500-BD/401/403 |
| 105 | POST | `/api/unicauca/fiet/consejo/cancelaciones-matricula/{uuidSolicitud}/funcionario/remitir` | FA | FA | `remitirADecano` | RemisionDecanoDTOPeticion | CancelacionMatriculaDetalleDTORespuesta | TramiteCancelacionMatriculaCU.remitirADecano<br>ConsultaCancelacionMatriculaCU.getDetalle | E: ASIGNATURA_SOLICITUD_ACADEMICA, HISTORIAL_SOLICITUD_ACADEMICA, RESOLUCION_ACADEMICA, SOLICITUD_ACADEMICA, logs · L×6 | 1, 3, 4, 6 · 400/500-BD/401/403 |
| 106 | POST | `/api/unicauca/fiet/consejo/cancelaciones-matricula/{uuidSolicitud}/funcionario/responder` | FA | FA | `enviarRespuesta` | ObservacionDTOPeticion (cuerpo opcional) | CancelacionMatriculaDetalleDTORespuesta | TramiteCancelacionMatriculaCU.enviarRespuesta<br>ConsultaCancelacionMatriculaCU.getDetalle | E: ASIGNATURA_MATRICULADA, HISTORIAL_SOLICITUD_ACADEMICA, SOLICITUD_ACADEMICA, logs · L×7 | 1, 3, 4, 6 · 500-BD/401/403 |
| 107 | POST | `/api/unicauca/fiet/consejo/cancelaciones-matricula/{uuidSolicitud}/decano/aprobar` | DEC | DEC | `aprobarPorDecano` | AprobacionDecanoDTOPeticion | CancelacionMatriculaDetalleDTORespuesta | TramiteCancelacionMatriculaCU.aprobarPorDecano<br>ConsultaCancelacionMatriculaCU.getDetalle | E: ASIGNATURA_SOLICITUD_ACADEMICA, HISTORIAL_SOLICITUD_ACADEMICA, SOLICITUD_ACADEMICA, logs · L×7 | 1, 3, 4, 6 · 400/500-BD/401/403 |
| 108 | POST | `/api/unicauca/fiet/consejo/cancelaciones-matricula/{uuidSolicitud}/decano/rechazar` | DEC | DEC | `rechazarPorDecano` | ObservacionDTOPeticion (cuerpo opcional) | CancelacionMatriculaDetalleDTORespuesta | TramiteCancelacionMatriculaCU.rechazarPorDecano<br>ConsultaCancelacionMatriculaCU.getDetalle | E: HISTORIAL_SOLICITUD_ACADEMICA, SOLICITUD_ACADEMICA, logs · L×7 | 1, 3, 4, 6 · 500-BD/401/403 |

### 3.14 Cancelación de Asignatura [EXTENSIÓN]

- Controlador: `CancelacionAsignaturaRestController` (`backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/input/controladorCancelacionesAsignatura/controlador/CancelacionAsignaturaRestController.java`), base `${url.application}cancelaciones-asignatura`, 8 endpoints.
- Inyecta: `CancelacionAsignaturaCUIntPuerto`, `ConsultaCancelacionAsignaturaCUIntPuerto`, `TramiteCancelacionAsignaturaCUIntPuerto`, `MapperCancelacionAsignaturaInfraestructuraDominio`.
- Nota: Radicación y trámite de Cancelación de Asignatura (aprobación parcial por asignatura, decisión P14).

| # | Mét. | Ruta completa | Rol efectivo | @PreAuthorize | Método del controlador | DTO petición | DTO respuesta | Caso de uso | Tablas | Códigos de error |
|---|---|---|---|---|---|---|---|---|---|---|
| 109 | GET | `/api/unicauca/fiet/consejo/cancelaciones-asignatura/formulario` | EST | EST | `getFormulario` | — | FormularioCancelacionAsignaturaDTORespuesta | ConsultaCancelacionAsignaturaCU.getFormulario | solo lectura · L×3 | 1, 3, 4 · 401/403 |
| 110 | POST | `/api/unicauca/fiet/consejo/cancelaciones-asignatura` | EST | EST | `radicar` | `motivo` (opc.); `asignaturas` (opc.); multipart con partes de archivo dinámicas | RadicacionCancelacionAsignaturaDTORespuesta | ConsultaCancelacionAsignaturaCU.getEstudianteAutenticado<br>CancelacionAsignaturaCU.radicarCancelacionAsignatura | E: ANEXO_ACADEMICO, ASIGNATURA_SOLICITUD_ACADEMICA, HISTORIAL_SOLICITUD_ACADEMICA, SOLICITUD_ACADEMICA, SOLICITUD_CANCELACION_ASIGNATURA, logs · L×6 | 1, 3, 4, 6 · 500-BD/401/403 |
| 111 | GET | `/api/unicauca/fiet/consejo/cancelaciones-asignatura/{uuidSolicitud}` | EST, FA, DEC | EST, FA, DEC | `getDetalle` | — | CancelacionAsignaturaDetalleDTORespuesta | ConsultaCancelacionAsignaturaCU.getDetalle | solo lectura · L×7 | 1, 3, 4 · 401/403 |
| 112 | POST | `/api/unicauca/fiet/consejo/cancelaciones-asignatura/{uuidSolicitud}/funcionario/rechazar` | FA | FA | `rechazarPorFuncionario` | ObservacionDTOPeticion | CancelacionAsignaturaDetalleDTORespuesta | TramiteCancelacionAsignaturaCU.rechazarPorFuncionario<br>ConsultaCancelacionAsignaturaCU.getDetalle | E: HISTORIAL_SOLICITUD_ACADEMICA, SOLICITUD_ACADEMICA, logs · L×7 | 1, 3, 4, 6 · 500-BD/401/403 |
| 113 | POST | `/api/unicauca/fiet/consejo/cancelaciones-asignatura/{uuidSolicitud}/funcionario/remitir` | FA | FA | `remitirADecano` | RemisionCancelacionAsignaturaDTOPeticion | CancelacionAsignaturaDetalleDTORespuesta | TramiteCancelacionAsignaturaCU.remitirADecano<br>ConsultaCancelacionAsignaturaCU.getDetalle | E: ASIGNATURA_SOLICITUD_ACADEMICA, HISTORIAL_SOLICITUD_ACADEMICA, RESOLUCION_ACADEMICA, SOLICITUD_ACADEMICA, logs · L×6 | 1, 3, 4, 6 · 400/500-BD/401/403 |
| 114 | POST | `/api/unicauca/fiet/consejo/cancelaciones-asignatura/{uuidSolicitud}/funcionario/responder` | FA | FA | `enviarRespuesta` | RespuestaCancelacionAsignaturaDTOPeticion (cuerpo opcional) | CancelacionAsignaturaDetalleDTORespuesta | TramiteCancelacionAsignaturaCU.enviarRespuesta<br>ConsultaCancelacionAsignaturaCU.getDetalle | E: ASIGNATURA_MATRICULADA, ASIGNATURA_SOLICITUD_ACADEMICA, HISTORIAL_SOLICITUD_ACADEMICA, SOLICITUD_ACADEMICA, logs · L×7 | 1, 3, 4, 6 · 500-BD/401/403 |
| 115 | POST | `/api/unicauca/fiet/consejo/cancelaciones-asignatura/{uuidSolicitud}/decano/aprobar` | DEC | DEC | `aprobarPorDecano` | AprobacionCancelacionAsignaturaDTOPeticion | CancelacionAsignaturaDetalleDTORespuesta | TramiteCancelacionAsignaturaCU.aprobarPorDecano<br>ConsultaCancelacionAsignaturaCU.getDetalle | E: ASIGNATURA_SOLICITUD_ACADEMICA, HISTORIAL_SOLICITUD_ACADEMICA, SOLICITUD_ACADEMICA, logs · L×7 | 1, 3, 4, 6 · 400/500-BD/401/403 |
| 116 | POST | `/api/unicauca/fiet/consejo/cancelaciones-asignatura/{uuidSolicitud}/decano/rechazar` | DEC | DEC | `rechazarPorDecano` | ObservacionDTOPeticion (cuerpo opcional) | CancelacionAsignaturaDetalleDTORespuesta | TramiteCancelacionAsignaturaCU.rechazarPorDecano<br>ConsultaCancelacionAsignaturaCU.getDetalle | E: HISTORIAL_SOLICITUD_ACADEMICA, SOLICITUD_ACADEMICA, logs · L×7 | 1, 3, 4, 6 · 500-BD/401/403 |

### 3.15 Examen Supletorio [EXTENSIÓN]

- Controlador: `ExamenSupletorioRestController` (`backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/infraestructura/input/controladorExamenesSupletorios/controlador/ExamenSupletorioRestController.java`), base `${url.application}examenes-supletorios`, 12 endpoints.
- Inyecta: `ExamenSupletorioCUIntPuerto`, `ConsultaExamenSupletorioCUIntPuerto`, `TramiteExamenSupletorioCUIntPuerto`, `MapperExamenSupletorioInfraestructuraDominio`.
- Nota: Radicación y trámite de Examen Supletorio. Único con rutas `estudiante/**` y con las acciones de recibo y comprobante de pago; el recibo y el comprobante se suben antes con `POST solicitudes-academicas/{uuid}/anexos`.

| # | Mét. | Ruta completa | Rol efectivo | @PreAuthorize | Método del controlador | DTO petición | DTO respuesta | Caso de uso | Tablas | Códigos de error |
|---|---|---|---|---|---|---|---|---|---|---|
| 117 | GET | `/api/unicauca/fiet/consejo/examenes-supletorios/formulario` | EST | EST | `getFormulario` | — | FormularioExamenSupletorioDTORespuesta | ConsultaExamenSupletorioCU.getFormulario | solo lectura · L×5 | 1, 3, 4 · 401/403 |
| 118 | POST | `/api/unicauca/fiet/consejo/examenes-supletorios` | EST | EST | `radicar` | `asignaturaMatriculada` (opc.); `fechaExamenNoPresentado` (opc.); `tipoCausa` (opc.); `asignaturaCruzada` (opc.); `fechaExamenCruzada` (opc.); `horaExamenCruzada` (opc.); multipart con partes de archivo dinámicas | RadicacionExamenSupletorioDTORespuesta | ConsultaExamenSupletorioCU.getEstudianteAutenticado<br>ExamenSupletorioCU.radicarExamenSupletorio | E: ANEXO_ACADEMICO, HISTORIAL_SOLICITUD_ACADEMICA, SOLICITUD_ACADEMICA, SOLICITUD_EXAMEN_SUPLETORIO, SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA, logs · L×6 | 1, 3, 4, 6 · 500-BD/401/403 |
| 119 | GET | `/api/unicauca/fiet/consejo/examenes-supletorios/{uuidSolicitud}` | EST, FA, DEC | EST, FA, DEC | `getDetalle` | — | ExamenSupletorioDetalleDTORespuesta | ConsultaExamenSupletorioCU.getDetalle | solo lectura · L×7 | 1, 3, 4 · 401/403 |
| 120 | POST | `/api/unicauca/fiet/consejo/examenes-supletorios/{uuidSolicitud}/funcionario/rechazar` | FA | FA | `rechazarPorFuncionario` | ObservacionDTOPeticion | ExamenSupletorioDetalleDTORespuesta | TramiteExamenSupletorioCU.rechazarPorFuncionario<br>ConsultaExamenSupletorioCU.getDetalle | E: HISTORIAL_SOLICITUD_ACADEMICA, SOLICITUD_ACADEMICA, logs · L×7 | 1, 3, 4, 6 · 500-BD/401/403 |
| 121 | POST | `/api/unicauca/fiet/consejo/examenes-supletorios/{uuidSolicitud}/funcionario/remitir` | FA | FA | `remitirADecano` | RemisionSupletorioDTOPeticion | ExamenSupletorioDetalleDTORespuesta | TramiteExamenSupletorioCU.remitirADecano<br>ConsultaExamenSupletorioCU.getDetalle | E: HISTORIAL_SOLICITUD_ACADEMICA, RESOLUCION_ACADEMICA, SOLICITUD_ACADEMICA, logs · L×6 | 1, 3, 4, 6 · 500-BD/401/403 |
| 122 | POST | `/api/unicauca/fiet/consejo/examenes-supletorios/{uuidSolicitud}/funcionario/responder` | FA | FA | `enviarRespuesta` | ObservacionDTOPeticion (cuerpo opcional) | ExamenSupletorioDetalleDTORespuesta | TramiteExamenSupletorioCU.enviarRespuesta<br>ConsultaExamenSupletorioCU.getDetalle | E: HISTORIAL_SOLICITUD_ACADEMICA, SOLICITUD_ACADEMICA, logs · L×7 | 1, 3, 4, 6 · 500-BD/401/403 |
| 123 | POST | `/api/unicauca/fiet/consejo/examenes-supletorios/{uuidSolicitud}/funcionario/recibo` | FA | FA | `enviarRecibo` | — | ExamenSupletorioDetalleDTORespuesta | TramiteExamenSupletorioCU.enviarRecibo<br>ConsultaExamenSupletorioCU.getDetalle | E: HISTORIAL_SOLICITUD_ACADEMICA, SOLICITUD_ACADEMICA, logs · L×7 | 1, 3, 4, 6 · 500-BD/401/403 |
| 124 | POST | `/api/unicauca/fiet/consejo/examenes-supletorios/{uuidSolicitud}/funcionario/comprobante/aprobar` | FA | FA | `aprobarComprobante` | AprobacionComprobanteDTOPeticion (cuerpo opcional) | ExamenSupletorioDetalleDTORespuesta | TramiteExamenSupletorioCU.aprobarComprobante<br>ConsultaExamenSupletorioCU.getDetalle | E: HISTORIAL_SOLICITUD_ACADEMICA, SOLICITUD_ACADEMICA, SOLICITUD_EXAMEN_SUPLETORIO, logs · L×6 | 1, 3, 4, 6 · 500-BD/401/403 |
| 125 | POST | `/api/unicauca/fiet/consejo/examenes-supletorios/{uuidSolicitud}/funcionario/comprobante/rechazar` | FA | FA | `rechazarComprobante` | ObservacionDTOPeticion | ExamenSupletorioDetalleDTORespuesta | TramiteExamenSupletorioCU.rechazarComprobante<br>ConsultaExamenSupletorioCU.getDetalle | E: HISTORIAL_SOLICITUD_ACADEMICA, SOLICITUD_ACADEMICA, logs · L×7 | 1, 3, 4, 6 · 500-BD/401/403 |
| 126 | POST | `/api/unicauca/fiet/consejo/examenes-supletorios/{uuidSolicitud}/decano/aprobar` | DEC | DEC | `aprobarPorDecano` | ObservacionDTOPeticion (cuerpo opcional) | ExamenSupletorioDetalleDTORespuesta | TramiteExamenSupletorioCU.aprobarPorDecano<br>ConsultaExamenSupletorioCU.getDetalle | E: HISTORIAL_SOLICITUD_ACADEMICA, SOLICITUD_ACADEMICA, logs · L×7 | 1, 3, 4, 6 · 500-BD/401/403 |
| 127 | POST | `/api/unicauca/fiet/consejo/examenes-supletorios/{uuidSolicitud}/decano/rechazar` | DEC | DEC | `rechazarPorDecano` | ObservacionDTOPeticion (cuerpo opcional) | ExamenSupletorioDetalleDTORespuesta | TramiteExamenSupletorioCU.rechazarPorDecano<br>ConsultaExamenSupletorioCU.getDetalle | E: HISTORIAL_SOLICITUD_ACADEMICA, SOLICITUD_ACADEMICA, logs · L×7 | 1, 3, 4, 6 · 500-BD/401/403 |
| 128 | POST | `/api/unicauca/fiet/consejo/examenes-supletorios/{uuidSolicitud}/estudiante/comprobante` | EST | EST | `subirComprobante` | — | ExamenSupletorioDetalleDTORespuesta | TramiteExamenSupletorioCU.subirComprobante<br>ConsultaExamenSupletorioCU.getDetalle | E: HISTORIAL_SOLICITUD_ACADEMICA, SOLICITUD_ACADEMICA, logs · L×7 | 1, 3, 4, 6 · 500-BD/401/403 |

## 4. Alcance de las reglas de seguridad

Las reglas de la extensión terminan cada prefijo en `denyAll()` (asignaturas, estudiantes, funcionarios-academicos, catalogos-academicos, solicitudes-academicas, cancelaciones-matricula, cancelaciones-asignatura, examenes-supletorios): una ruta no declarada de esos recursos responde 403 aunque el usuario esté autenticado. Las reglas de Julián no usan `denyAll`: `solicitudes/**` y `respuestas/**` terminan en `authenticated()`, y `tipos/solicitudes/**`, `logs/**`, `roles/**` en SG/DEC.

## 5. Hallazgos de seguridad por endpoint (regla efectiva frente a @PreAuthorize)

De los 124 endpoints con `@PreAuthorize`, **22** tienen una regla efectiva distinta de la anotación; **22 (todos) son [BASE]** y los 66 de la extensión no tienen ninguna divergencia (verificado por el script con los mismos 78 matchers). Como `@PreAuthorize` no se evalúa, manda la regla efectiva:

| # | Endpoint | @PreAuthorize | Regla efectiva (matcher que gana) |
|---|---|---|---|
| 19 | GET `/tipos/solicitudes/paginado` | SG, DEC | PÚBLICO (`tipos/solicitudes/{uuidTipoSolicitud}`) |
| 20 | GET `/tipos/solicitudes/filtro` | SG, DEC | PÚBLICO (`tipos/solicitudes/{uuidTipoSolicitud}`) |
| 22 | GET `/tipos/solicitudes/{uuidTipoSolicitud}` | AUTH | PÚBLICO (`tipos/solicitudes/{uuidTipoSolicitud}`) |
| 27 | GET `/tipos/solicitudes/perfil/paginado` | AUTH | PÚBLICO (`tipos/solicitudes/perfil/paginado`) |
| 28 | GET `/tipos/solicitudes/perfil/filtro` | AUTH | PÚBLICO (`tipos/solicitudes/perfil/filtro`) |
| 34 | GET `/solicitudes/paginado` | SG, FUN, DEC | AUTH (`solicitudes/**`) |
| 35 | GET `/solicitudes` | SG, FUN, DEC | AUTH (`solicitudes/**`) |
| 36 | GET `/solicitudes/{uuidSolicitud}` | SG, FUN, DEC | AUTH (`solicitudes/**`) |
| 38 | POST `/solicitudes/public` | SG, FUN, DEC | PÚBLICO (`solicitudes/public`) |
| 39 | PUT `/solicitudes/{uuidSolicitud}` | SG, DEC | AUTH (`solicitudes/**`) |
| 40 | GET `/solicitudes/funcionario/{uuidFuncionario}` | SG, DEC | AUTH (`solicitudes/**`) |
| 43 | GET `/solicitudes/buscar` | SG, FUN, DEC | AUTH (`solicitudes/**`) |
| 44 | GET `/solicitudes/buscar/solicitante` | SG, DEC | AUTH (`solicitudes/**`) |
| 45 | GET `/solicitudes/fun/buscar` | SG, FUN, DEC | AUTH (`solicitudes/**`) |
| 48 | GET `/solicitudes/anexos/download` | SG, DEC | AUTH (`solicitudes/**`) |
| 49 | GET `/solicitudes/exportar` | SG, DEC | AUTH (`solicitudes/**`) |
| 50 | GET `/solicitudes/exportar/respuestas` | SG, DEC | AUTH (`solicitudes/**`) |
| 51 | GET `/solicitudes/exportar/reunion` | SG, DEC | AUTH (`solicitudes/**`) |
| 55 | GET `/respuestas/paginado/funcionario` | SG, FUN, DEC | AUTH (`respuestas/**`) |
| 56 | GET `/respuestas/paginado/funcionario-nombre-solicitud` | SG, FUN, DEC | AUTH (`respuestas/**`) |
| 59 | POST `/respuestas/{uuidSolicitud}` | SG, DEC | SG, DEC, FUN (`respuestas/**`) |
| 61 | PATCH `/respuestas/{uuidRespuesta}/archivo` | SG, FUN, DEC | AUTH (`anyRequest().authenticated()`) |

Observaciones derivadas del orden de los matchers (inferidas, NO VERIFICADO en ejecución):

1. `GET tipos/solicitudes/paginado` y `GET tipos/solicitudes/filtro` coinciden con el patrón `tipos/solicitudes/{uuidTipoSolicitud}` (regla 6, `permitAll`) antes de llegar a `tipos/solicitudes/**` (regla 7, SG/DEC), por lo que quedarían públicos aunque su anotación diga SG/DEC. `GET tipos/solicitudes` (sin segmento) sí cae en la regla 7.
2. `PATCH respuestas/{uuidRespuesta}/archivo` no tiene matcher propio (las reglas de `respuestas` solo cubren GET y POST) y cae en `anyRequest().authenticated()`; la anotación dice SG/FUN/DEC.
3. `POST respuestas/{uuidSolicitud}` está anotado SG/DEC pero el matcher `POST respuestas/**` admite también el rol `Funcionario` (FUN).
4. Todo `solicitudes/**` salvo `solicitudes/public` y `solicitudes/orden-del-dia/**` es `authenticated()`: `exportar*`, `anexos/download`, `buscar/solicitante`, `funcionario/{uuid}` y `PUT {uuid}` quedan abiertos a cualquier autenticado pese a anotaciones SG/DEC. `POST solicitudes/public` es `permitAll` (el controlador lo anota SG/FUN/DEC).
5. `GET tipos/solicitudes/{uuid}` y `GET tipos/solicitudes/perfil/paginado|filtro` son `permitAll` y su anotación es `AUTHENTICATED`.

## 6. Endpoints sin caso de uso o con acceso a disco desde el controlador

- `GET respuestas/{uuidRespuesta}/{fileName}` (`RespuestaRestController.descargarRespuesta`) y `GET solicitudes/anexos/{uuidSolicitud}/{fileName}` (`SolicitudesRestController.descargarAnexo`) [BASE]: construyen un `java.io.File` con `${app.uploads.base-path}` y devuelven el recurso sin caso de uso ni gateway; ante error responden 404/500 vacíos en vez del formato de error común.
- `GET solicitudes/anexos/download`, `GET solicitudes/exportar`, `.../exportar/respuestas` y `.../exportar/reunion` [BASE] generan ZIP y .docx en memoria (`byte[]`); no usan DTO de respuesta.
- Descargas de la extensión (`GET solicitudes-academicas/{uuid}/anexos/{uuidAnexo}` y `.../resolucion`): pasan por casos de uso y devuelven `ByteArrayResource` con `Content-Disposition: attachment`; la ruta de disco nunca sale en la respuesta.

## Anexo A. Reglas de `ConfiguracionSeguridad` (78 `requestMatchers` + `anyRequest`)

Origen: [B] = presente en el primer commit `d3a48cc` (17 reglas); [E] = agregada después. Se evalúan en este orden.

| Regla | Método | Patrón (tras `${url.application}`) | Autorización | Origen |
|---|---|---|---|---|
| 1 | todos | `sesiones` | PÚBLICO | B |
| 2 | POST | `solicitudes/public` | PÚBLICO | B |
| 3 | GET | `tipos/solicitudes/perfil` | PÚBLICO | B |
| 4 | GET | `tipos/solicitudes/perfil/paginado` | PÚBLICO | B |
| 5 | GET | `tipos/solicitudes/perfil/filtro` | PÚBLICO | B |
| 6 | GET | `tipos/solicitudes/{uuidTipoSolicitud}` | PÚBLICO | B |
| 7 | todos | `tipos/solicitudes/**` | SG, DEC | B |
| 8 | GET | `solicitudes/orden-del-dia/estado` | AUTH | B |
| 9 | todos | `solicitudes/orden-del-dia/**` | SG, DEC | B |
| 10 | todos | `logs/**` | SG, DEC | B |
| 11 | todos | `roles/**` | SG, DEC | B |
| 12 | GET | `usuarios` | SG, DEC | E |
| 13 | GET | `usuarios/paginado` | SG, DEC | E |
| 14 | GET | `usuarios/filtro` | SG, DEC | E |
| 15 | GET | `usuarios/funcionarios` | SG, DEC | E |
| 16 | GET | `usuarios/tipos` | SG, DEC | E |
| 17 | GET | `usuarios/**` | AUTH | B |
| 18 | PATCH | `usuarios/**` | AUTH | B |
| 19 | todos | `usuarios/**` | SG, DEC | B |
| 20 | todos | `solicitudes/**` | AUTH | B |
| 21 | GET | `respuestas/**` | AUTH | B |
| 22 | POST | `respuestas/**` | SG, DEC, FUN | B |
| 23 | GET | `asignaturas/programa/{uuidPrograma}` | SG, DEC, FA | E |
| 24 | POST | `asignaturas/cargar/archivo` | SG, DEC | E |
| 25 | GET | `asignaturas/**` | SG, DEC, FA | E |
| 26 | POST | `asignaturas` | SG, DEC | E |
| 27 | PUT | `asignaturas/**` | SG, DEC | E |
| 28 | todos | `asignaturas/**` | DENEGADO | E |
| 29 | POST | `estudiantes/cargar/archivo` | SG, DEC | E |
| 30 | GET | `estudiantes/mis-asignaturas` | EST | E |
| 31 | GET | `estudiantes/paginado` | SG, DEC, FA | E |
| 32 | GET | `estudiantes/filtro` | SG, DEC, FA | E |
| 33 | GET | `estudiantes/{uuidEstudiante}` | SG, DEC, FA | E |
| 34 | POST | `estudiantes` | SG, DEC | E |
| 35 | PUT | `estudiantes/{uuidEstudiante}` | SG, DEC | E |
| 36 | POST | `estudiantes/{uuidEstudiante}/asignaturas` | SG, DEC | E |
| 37 | PATCH | `estudiantes/{uuidEstudiante}/asignaturas/{uuidMatricula}/estado` | SG, DEC | E |
| 38 | todos | `estudiantes/**` | DENEGADO | E |
| 39 | POST | `funcionarios-academicos/cargar/archivo` | SG, DEC | E |
| 40 | GET | `funcionarios-academicos/paginado` | SG, DEC, FA | E |
| 41 | GET | `funcionarios-academicos/filtro` | SG, DEC, FA | E |
| 42 | GET | `funcionarios-academicos/{uuidFuncionario}` | SG, DEC, FA | E |
| 43 | POST | `funcionarios-academicos` | SG, DEC | E |
| 44 | PUT | `funcionarios-academicos/{uuidFuncionario}` | SG, DEC | E |
| 45 | todos | `funcionarios-academicos/**` | DENEGADO | E |
| 46 | PUT | `catalogos-academicos/tipos-solicitud/{uuidTipo}/funcionario` | SG, DEC | E |
| 47 | GET | `catalogos-academicos/programas` | AUTH | E |
| 48 | GET | `catalogos-academicos/**` | AUTH | E |
| 49 | todos | `catalogos-academicos/**` | DENEGADO | E |
| 50 | GET | `solicitudes-academicas/estudiante` | EST | E |
| 51 | GET | `solicitudes-academicas/funcionario` | FA | E |
| 52 | GET | `solicitudes-academicas/decano` | DEC | E |
| 53 | GET | `solicitudes-academicas/{uuidSolicitud}` | EST, FA, DEC | E |
| 54 | GET | `solicitudes-academicas/{uuidSolicitud}/historial` | EST, FA, DEC | E |
| 55 | GET | `solicitudes-academicas/{uuidSolicitud}/anexos/{uuidAnexo}` | EST, FA, DEC | E |
| 56 | GET | `solicitudes-academicas/{uuidSolicitud}/resolucion` | EST, FA, DEC | E |
| 57 | POST | `solicitudes-academicas/{uuidSolicitud}/anexos` | EST, FA | E |
| 58 | POST | `solicitudes-academicas/{uuidSolicitud}/resolucion` | FA | E |
| 59 | todos | `solicitudes-academicas/**` | DENEGADO | E |
| 60 | GET | `cancelaciones-matricula/formulario` | EST | E |
| 61 | POST | `cancelaciones-matricula` | EST | E |
| 62 | POST | `cancelaciones-matricula/{uuidSolicitud}/funcionario/**` | FA | E |
| 63 | POST | `cancelaciones-matricula/{uuidSolicitud}/decano/**` | DEC | E |
| 64 | GET | `cancelaciones-matricula/{uuidSolicitud}` | EST, FA, DEC | E |
| 65 | todos | `cancelaciones-matricula/**` | DENEGADO | E |
| 66 | GET | `cancelaciones-asignatura/formulario` | EST | E |
| 67 | POST | `cancelaciones-asignatura` | EST | E |
| 68 | POST | `cancelaciones-asignatura/{uuidSolicitud}/funcionario/**` | FA | E |
| 69 | POST | `cancelaciones-asignatura/{uuidSolicitud}/decano/**` | DEC | E |
| 70 | GET | `cancelaciones-asignatura/{uuidSolicitud}` | EST, FA, DEC | E |
| 71 | todos | `cancelaciones-asignatura/**` | DENEGADO | E |
| 72 | GET | `examenes-supletorios/formulario` | EST | E |
| 73 | POST | `examenes-supletorios` | EST | E |
| 74 | POST | `examenes-supletorios/{uuidSolicitud}/funcionario/**` | FA | E |
| 75 | POST | `examenes-supletorios/{uuidSolicitud}/decano/**` | DEC | E |
| 76 | POST | `examenes-supletorios/{uuidSolicitud}/estudiante/**` | EST | E |
| 77 | GET | `examenes-supletorios/{uuidSolicitud}` | EST, FA, DEC | E |
| 78 | todos | `examenes-supletorios/**` | DENEGADO | E |
| 79 | todos | cualquier otra | AUTH | B (`anyRequest().authenticated()`) |

Conteo verificado por programa: 17 reglas [B] y 61 reglas [E] (suman 78). Las [E] incluyen las 5 restricciones de listados de `usuarios` (commit `ac4fb00`) y las de los ocho recursos académicos.

## Anexo B. Constantes de `MensajesError` alcanzables por método de caso de uso

Unión estática por método (incluye helpers privados, servicios de dominio y otros casos de uso llamados). El tipo es el método `lanzar*` del formateador. Los métodos sobrecargados se unen por nombre.

| Caso de uso.método | Constantes por tipo (código) |
|---|---|
| AnexoAcademicoCU.adjuntarAnexo | **ErrorGenerico(1)**: (mensaje variable), `ERROR_GUARDANDO_ARCHIVO`, `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `ANEXO_ACTOR_NO_PERMITIDO`, `ANEXO_DE_OTRO_TIPO`, `ANEXO_ETAPA_NO_PERMITIDA`, `DATOS_TRANSICION_INCOMPLETOS`, `SOLICITUD_AJENA`, `SOPORTE_LIBRE_NO_PERMITIDO`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `TIPO_SOLICITUD_SIN_PROCESO`; **MalFormato(6)**: `ARCHIVO_MUY_GRANDE`, `ARCHIVO_VACIO`, `CONTENIDO_ANEXO_NO_COINCIDE`, `FORMATO_ANEXO_NO_PERMITIDO` |
| AsignacionFuncionarioAcademicoCU.asignarFuncionarioAcademico | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO` |
| AsignaturaCU.actualizarAsignatura | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadExiste(2)**: `ASIGNATURA_NOMBRE_EXISTE`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **MalFormato(6)**: `NOMBRE_ASIGNATURA_MUY_LARGO`, `NOMBRE_ASIGNATURA_REQUERIDO` |
| AsignaturaCU.crearAsignatura | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadExiste(2)**: `ASIGNATURA_NOMBRE_EXISTE`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **MalFormato(6)**: `NOMBRE_ASIGNATURA_MUY_LARGO`, `NOMBRE_ASIGNATURA_REQUERIDO`, `PROGRAMA_ACADEMICO_REQUERIDO`, `VALOR_REPETIDO_PETICION`; **SinInformacion(7)**: `ARCHIVO_EXCEL_VACIO` |
| AsignaturaCU.crearAsignaturas | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadExiste(2)**: `ASIGNATURA_NOMBRE_EXISTE`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **MalFormato(6)**: `NOMBRE_ASIGNATURA_MUY_LARGO`, `NOMBRE_ASIGNATURA_REQUERIDO`, `PROGRAMA_ACADEMICO_REQUERIDO`, `VALOR_REPETIDO_PETICION`; **SinInformacion(7)**: `ARCHIVO_EXCEL_VACIO` |
| AsignaturaCU.getAsignatura | **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA` |
| AsignaturaCU.getAsignaturasPaginado | **MalFormato(6)**: `PAGINACION_ERROR` |
| AsignaturaCU.getAsignaturasPorFiltro | **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`; **MalFormato(6)**: `PAGINACION_ERROR` |
| AsignaturaCU.getAsignaturasPorPrograma | **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA` |
| CancelacionAsignaturaCU.radicarCancelacionAsignatura | **ErrorGenerico(1)**: (mensaje variable), `ERROR_GUARDANDO_ARCHIVO`, `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `ANEXO_ACTOR_NO_PERMITIDO`, `ANEXO_DE_OTRO_TIPO`, `ANEXO_ETAPA_NO_PERMITIDA`, `ASIGNATURA_NO_ACTIVA`, `ASIGNATURA_NO_MATRICULADA`, `ASIGNATURA_REPETIDA`, `DATOS_TRANSICION_INCOMPLETOS`, `ETAPA_FINAL_SIN_ACCIONES`, `ETAPA_NO_VALIDA`, `MOTIVO_CANCELACION_REQUERIDO`, `ROL_TRANSICION_NO_PERMITIDO`, `SIN_ASIGNATURAS_ELEGIDAS`, `SOLICITUD_AJENA`, `SOLICITUD_EN_CURSO`, `SOLO_SOPORTES_LIBRES`, `SOPORTE_LIBRE_NO_PERMITIDO`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `TIPO_SOLICITUD_SIN_PROCESO`, `TRANSICION_NO_PERMITIDA`; **MalFormato(6)**: `ARCHIVO_MUY_GRANDE`, `ARCHIVO_VACIO`, `CONTENIDO_ANEXO_NO_COINCIDE`, `FORMATO_ANEXO_NO_PERMITIDO`, `MOTIVO_CANCELACION_MUY_LARGO` |
| CancelacionMatriculaCU.radicarCancelacionMatricula | **ErrorGenerico(1)**: (mensaje variable), `ERROR_GUARDANDO_ARCHIVO`, `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `ANEXOS_OBLIGATORIOS_FALTANTES`, `ANEXO_ACTOR_NO_PERMITIDO`, `ANEXO_DE_OTRO_TIPO`, `ANEXO_ETAPA_NO_PERMITIDA`, `DATOS_TRANSICION_INCOMPLETOS`, `ETAPA_FINAL_SIN_ACCIONES`, `ETAPA_NO_VALIDA`, `MOTIVO_CANCELACION_REQUERIDO`, `ROL_TRANSICION_NO_PERMITIDO`, `SIN_ASIGNATURAS_ACTIVAS`, `SOLICITUD_AJENA`, `SOLICITUD_EN_CURSO`, `SOPORTE_LIBRE_NO_PERMITIDO`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `TIPO_SOLICITUD_SIN_PROCESO`, `TRANSICION_NO_PERMITIDA`; **MalFormato(6)**: `ARCHIVO_MUY_GRANDE`, `ARCHIVO_VACIO`, `CONTENIDO_ANEXO_NO_COINCIDE`, `FORMATO_ANEXO_NO_PERMITIDO`, `MOTIVO_CANCELACION_MUY_LARGO` |
| ConsultaCancelacionAsignaturaCU.getDetalle | **ErrorGenerico(1)**: `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `DATOS_TRANSICION_INCOMPLETOS`, `ETAPA_NO_VALIDA` |
| ConsultaCancelacionAsignaturaCU.getEstudianteAutenticado | **ErrorGenerico(1)**: `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL` |
| ConsultaCancelacionAsignaturaCU.getFormulario | **ErrorGenerico(1)**: `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL` |
| ConsultaCancelacionMatriculaCU.getDetalle | **ErrorGenerico(1)**: `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `DATOS_TRANSICION_INCOMPLETOS`, `ETAPA_NO_VALIDA` |
| ConsultaCancelacionMatriculaCU.getEstudianteAutenticado | **ErrorGenerico(1)**: `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL` |
| ConsultaCancelacionMatriculaCU.getFormulario | **ErrorGenerico(1)**: `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL` |
| ConsultaExamenSupletorioCU.getDetalle | **ErrorGenerico(1)**: `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `DATOS_TRANSICION_INCOMPLETOS`, `ETAPA_NO_VALIDA` |
| ConsultaExamenSupletorioCU.getEstudianteAutenticado | **ErrorGenerico(1)**: `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL` |
| ConsultaExamenSupletorioCU.getFormulario | **ErrorGenerico(1)**: `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL` |
| ConsultaSolicitudAcademicaCU.descargarAnexo | **ErrorGenerico(1)**: `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `DATOS_TRANSICION_INCOMPLETOS`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO` |
| ConsultaSolicitudAcademicaCU.getBandejaDecano | **ErrorGenerico(1)**: `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL` |
| ConsultaSolicitudAcademicaCU.getBandejaEstudiante | **ErrorGenerico(1)**: `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL` |
| ConsultaSolicitudAcademicaCU.getBandejaFuncionario | **ErrorGenerico(1)**: `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL` |
| ConsultaSolicitudAcademicaCU.getDetalle | **ErrorGenerico(1)**: `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `DATOS_TRANSICION_INCOMPLETOS`, `ETAPA_NO_VALIDA` |
| ConsultaSolicitudAcademicaCU.getHistorial | **ErrorGenerico(1)**: `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO` |
| ConsultaSolicitudAcademicaCU.resolverActor | **ErrorGenerico(1)**: `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO` |
| EstudianteCU.actualizarEstudiante | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadExiste(2)**: `ATRIBUTO_UNICO_YA_EXISTE`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **MalFormato(6)**: `TIPO_DOCUMENTO_ERRONEO` |
| EstudianteCU.agregarAsignaturaMatriculada | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ASIGNATURA_OTRO_PROGRAMA`; **MalFormato(6)**: `ASIGNATURA_YA_MATRICULADA` |
| EstudianteCU.cambiarEstadoAsignatura | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **MalFormato(6)**: `CAMBIO_ESTADO_NO_PERMITIDO`, `ESTADO_ASIGNATURA_NO_VALIDO` |
| EstudianteCU.crearEstudiante | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadExiste(2)**: `ATRIBUTO_UNICO_YA_EXISTE`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ASIGNATURA_OTRO_PROGRAMA`, `INSTANCIA_NO_VALIDA`, `ROLES_DUPLICADOS_USUARIO`, `ROLES_NO_VALIDOS`; **MalFormato(6)**: `PROGRAMA_ACADEMICO_REQUERIDO`, `TIPO_DOCUMENTO_ERRONEO`, `VALOR_REPETIDO_PETICION` |
| EstudianteCU.crearEstudiantes | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadExiste(2)**: `ATRIBUTO_UNICO_YA_EXISTE`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ASIGNATURA_OTRO_PROGRAMA`, `INSTANCIA_NO_VALIDA`, `ROLES_DUPLICADOS_USUARIO`, `ROLES_NO_VALIDOS`; **MalFormato(6)**: `PROGRAMA_ACADEMICO_REQUERIDO`, `TIPO_DOCUMENTO_ERRONEO`, `VALOR_REPETIDO_PETICION`; **SinInformacion(7)**: `ARCHIVO_EXCEL_VACIO` |
| EstudianteCU.getEstudiante | **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA` |
| EstudianteCU.getEstudiantesPaginado | **MalFormato(6)**: `PAGINACION_ERROR` |
| EstudianteCU.getEstudiantesPorFiltro | **MalFormato(6)**: `PAGINACION_ERROR` |
| EstudianteCU.getMisAsignaturas | **ErrorGenerico(1)**: `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA_FILTRO` |
| EtapaSolicitudAcademicaCU.getEtapasPorTipo | **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA` |
| EtapaSolicitudAcademicaCU.getEtiquetasPorRol | **MalFormato(6)**: `ROL_ETIQUETA_NO_VALIDO` |
| ExamenSupletorioCU.radicarExamenSupletorio | **ErrorGenerico(1)**: (mensaje variable), `ERROR_GUARDANDO_ARCHIVO`, `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `ANEXOS_OBLIGATORIOS_FALTANTES`, `ANEXO_ACTOR_NO_PERMITIDO`, `ANEXO_DE_OTRO_TIPO`, `ANEXO_ETAPA_NO_PERMITIDA`, `ANEXO_NO_CORRESPONDE_CAUSA`, `ANEXO_NO_PERMITIDO_AL_RADICAR`, `ANEXO_REPETIDO`, `ASIGNATURA_CRUZADA_IGUAL`, `ASIGNATURA_CRUZADA_REQUERIDA`, `ASIGNATURA_NO_MATRICULADA`, `ASIGNATURA_SUPLETORIO_NO_ACTIVA`, `ASIGNATURA_SUPLETORIO_REQUERIDA`, `CAUSA_SUPLETORIO_INVALIDA`, `DATOS_CRUCE_NO_PERMITIDOS`, `DATOS_CRUCE_REQUERIDOS`, `DATOS_TRANSICION_INCOMPLETOS`, `ETAPA_FINAL_SIN_ACCIONES`, `ETAPA_NO_VALIDA`, `FECHA_EXAMEN_FUTURA`, `FECHA_EXAMEN_REQUERIDA`, `PLAZO_SUPLETORIO_VENCIDO`, `ROL_TRANSICION_NO_PERMITIDO`, `SOLICITUD_AJENA`, `SOLICITUD_EN_CURSO`, `SOPORTE_LIBRE_NO_PERMITIDO`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `TIPO_SOLICITUD_SIN_PROCESO`, `TRANSICION_NO_PERMITIDA`; **MalFormato(6)**: `ARCHIVO_MUY_GRANDE`, `ARCHIVO_VACIO`, `CONTENIDO_ANEXO_NO_COINCIDE`, `FORMATO_ANEXO_NO_PERMITIDO`, `HORA_CRUCE_MAL_FORMADA`, `HORA_CRUCE_MUY_LARGA` |
| FuncionarioAcademicoCU.actualizarFuncionarioAcademico | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadExiste(2)**: `ATRIBUTO_UNICO_YA_EXISTE`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **MalFormato(6)**: `TIPO_DOCUMENTO_ERRONEO` |
| FuncionarioAcademicoCU.crearFuncionarioAcademico | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadExiste(2)**: `ATRIBUTO_UNICO_YA_EXISTE`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `INSTANCIA_NO_VALIDA`, `ROLES_DUPLICADOS_USUARIO`, `ROLES_NO_VALIDOS`; **MalFormato(6)**: `TIPO_DOCUMENTO_ERRONEO` |
| FuncionarioAcademicoCU.crearFuncionariosAcademicos | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadExiste(2)**: `ATRIBUTO_UNICO_YA_EXISTE`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `INSTANCIA_NO_VALIDA`, `ROLES_DUPLICADOS_USUARIO`, `ROLES_NO_VALIDOS`; **MalFormato(6)**: `TIPO_DOCUMENTO_ERRONEO`, `VALOR_REPETIDO_PETICION`; **SinInformacion(7)**: `ARCHIVO_EXCEL_VACIO` |
| FuncionarioAcademicoCU.getFuncionarioAcademico | **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA` |
| FuncionarioAcademicoCU.getFuncionariosAcademicosPaginado | **MalFormato(6)**: `PAGINACION_ERROR` |
| FuncionarioAcademicoCU.getFuncionariosAcademicosPorFiltro | **MalFormato(6)**: `PAGINACION_ERROR` |
| LogCU.getLogs | **MalFormato(6)**: `PAGINACION_ERROR`; **SinInformacion(7)**: `SIN_REGISTROS` |
| OrdenDelDiaCU.actualizarOrdenDelDia | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO` |
| OrdenDelDiaCU.buscarOrdenDelDiaPorNumeroActa | **SinInformacion(7)**: (mensaje variable) |
| OrdenDelDiaCU.crearOrdenDelDia | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA_FILTRO` |
| OrdenDelDiaCU.generarOrdenDelDia | **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA` |
| OrdenDelDiaCU.generarOrdenDelDiaConRespuestas | **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA` |
| OrdenDelDiaCU.generarOrdenDelDiaMerge | **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA` |
| OrdenDelDiaCU.getOrdenDelDia | **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA` |
| OrdenDelDiaCU.getOrdenesDelDia | **MalFormato(6)**: `PAGINACION_ERROR`; **SinInformacion(7)**: `SIN_REGISTROS` |
| OrdenDelDiaCU.getOrdenesDelDiaPorEstado | **SinInformacion(7)**: `SIN_REGISTROS` |
| ProgramaAcademicoCU.getProgramasAcademicos | — |
| ResolucionAcademicaCU.adjuntarResolucion | **ErrorGenerico(1)**: (mensaje variable), `ERROR_GUARDANDO_ARCHIVO`, `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `DATOS_TRANSICION_INCOMPLETOS`, `RESOLUCION_ETAPA_FINAL`, `RESOLUCION_ETAPA_NO_PERMITIDA`, `RESOLUCION_SOLO_CANCELACIONES`, `RESOLUCION_SOLO_FUNCIONARIO`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `TIPO_SOLICITUD_SIN_PROCESO`; **MalFormato(6)**: `ARCHIVO_MUY_GRANDE`, `ARCHIVO_VACIO`, `CONTENIDO_ANEXO_NO_COINCIDE`, `FORMATO_ANEXO_NO_PERMITIDO` |
| ResolucionAcademicaCU.obtenerResolucion | **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `DATOS_TRANSICION_INCOMPLETOS`, `RESOLUCION_NO_DISPONIBLE`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO` |
| RespuestaCU.eliminarArchivoRespuesta | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: (mensaje variable) |
| RespuestaCU.getRespuesta | **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA` |
| RespuestaCU.getRespuestaPorSolicitud | **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA` |
| RespuestaCU.getRespuestas | **MalFormato(6)**: `PAGINACION_ERROR`; **SinInformacion(7)**: `SIN_REGISTROS` |
| RespuestaCU.getRespuestasPorFuncionario | **MalFormato(6)**: `PAGINACION_ERROR`; **SinInformacion(7)**: `SIN_REGISTROS` |
| RespuestaCU.getRespuestasPorFuncionarioNombreSolicitud | **MalFormato(6)**: `PAGINACION_ERROR`; **SinInformacion(7)**: `SIN_REGISTROS` |
| RespuestaCU.getRespuestasPorNombreSolicitud | **MalFormato(6)**: `PAGINACION_ERROR`; **SinInformacion(7)**: `SIN_REGISTROS` |
| RespuestaCU.registrarRespuesta | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `TIPO_RESPUESTA_NO_VALIDO` |
| RespuestaCU.responderSolicitud | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `FORMATO_RESPUESTA_NO_VALIDO` |
| RolCU.actualizarRol | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO` |
| RolCU.getRol | **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA` |
| RolCU.getRoles | **MalFormato(6)**: `PAGINACION_ERROR`; **SinInformacion(7)**: `SIN_REGISTROS` |
| SesionCU.login | **ErrorGenerico(1)**: (mensaje variable); **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA_FILTRO`; **CredencialesErroneas(5)**: `CREDENCIALES_ERRONEAS`; **SinAcceso(8)**: `NO_ACCESO`, `ROL_NO_HABILITADO` |
| SituacionAcademicaAsignaturaCU.getSituaciones | — |
| SolicitudCU.actualizarSolicitud | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **MalFormato(6)**: `TIPO_DE_USUARIO_NO_VALIDO` |
| SolicitudCU.buscarSolicitudesPorNombre | **MalFormato(6)**: `PAGINACION_ERROR`; **SinInformacion(7)**: (mensaje variable) |
| SolicitudCU.buscarSolicitudesPorNombreYFuncionario | **MalFormato(6)**: `PAGINACION_ERROR`; **SinInformacion(7)**: (mensaje variable) |
| SolicitudCU.buscarSolicitudesPorSolicitante | **MalFormato(6)**: `PAGINACION_ERROR`; **SinInformacion(7)**: (mensaje variable) |
| SolicitudCU.crearSolicitud | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **MalFormato(6)**: (mensaje variable) |
| SolicitudCU.crearSolicitudPublica | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO` |
| SolicitudCU.generarZipAnexosPorOrdenDelDia | **MalFormato(6)**: (mensaje variable); **SinInformacion(7)**: (mensaje variable) |
| SolicitudCU.getSolicitud | **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA` |
| SolicitudCU.getSolicitudes | **MalFormato(6)**: `PAGINACION_ERROR`; **SinInformacion(7)**: `SIN_REGISTROS` |
| SolicitudCU.getSolicitudesPorEstado | **SinInformacion(7)**: (mensaje variable) |
| SolicitudCU.getSolicitudesPorFuncionario | **MalFormato(6)**: `PAGINACION_ERROR`; **SinInformacion(7)**: (mensaje variable) |
| SolicitudCU.getSolicitudesPorOrdenDelDia | **SinInformacion(7)**: (mensaje variable) |
| TipoAnexoAcademicoCU.getTiposAnexoPorTipo | **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA` |
| TipoSolicitudAcademicaCU.getTiposSolicitudAcademica | — |
| TipoSolicitudCU.actualizarTipoSolicitud | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `MAL_ASIGNACION`, `MAL_FORMATO_ANEXO`, `PERFIL_SOLICITANTE_NO_VALIDO`, `SECCION_NO_EXISTENTE` |
| TipoSolicitudCU.crearTipoSolicitud | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `MAL_ASIGNACION`, `MAL_FORMATO_ANEXO`, `PERFIL_SOLICITANTE_NO_VALIDO`, `SECCION_NO_EXISTENTE` |
| TipoSolicitudCU.crearTiposSolicitud | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `MAL_ASIGNACION`, `MAL_FORMATO_ANEXO`, `PERFIL_SOLICITANTE_NO_VALIDO`, `SECCION_NO_EXISTENTE` |
| TipoSolicitudCU.getTipoSolicitud | **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA` |
| TipoSolicitudCU.getTiposSolicitud | **MalFormato(6)**: `PAGINACION_ERROR`; **SinInformacion(7)**: `SIN_REGISTROS` |
| TipoSolicitudCU.getTiposSolicitudesPorNombreYPerfilSolicitante | **MalFormato(6)**: `PAGINACION_ERROR`; **SinInformacion(7)**: `SIN_REGISTROS` |
| TipoSolicitudCU.getTiposSolicitudesPorPerfil | **SinInformacion(7)**: (mensaje variable) |
| TipoSolicitudCU.getTiposSolicitudesPorPerfilSolicitante | **MalFormato(6)**: `PAGINACION_ERROR`; **SinInformacion(7)**: `SIN_REGISTROS` |
| TramiteCancelacionAsignaturaCU.aprobarPorDecano | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `ASIGNATURAS_SIN_EVALUAR`, `ASIGNATURA_AJENA`, `ASIGNATURA_REPETIDA`, `DATOS_TRANSICION_INCOMPLETOS`, `DATO_ASIGNATURA_FALTANTE`, `ETAPA_FINAL_SIN_ACCIONES`, `ETAPA_NO_VALIDA`, `FORMA_APROBACION_DECANO`, `NINGUNA_ASIGNATURA_APROBADA`, `REQUISITO_TRANSICION_FALTANTE`, `ROL_TRANSICION_NO_PERMITIDO`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `TIPO_SOLICITUD_SIN_PROCESO`, `TRANSICION_NO_PERMITIDA`; **MalFormato(6)**: `OBSERVACION_MUY_LARGA` |
| TramiteCancelacionAsignaturaCU.enviarRespuesta | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `AJUSTES_SOLICITUD_RECHAZADA`, `AJUSTE_ASIGNATURA_FALTANTE`, `ASIGNATURA_AJENA`, `ASIGNATURA_REPETIDA`, `DATOS_TRANSICION_INCOMPLETOS`, `DATO_ASIGNATURA_FALTANTE`, `ETAPA_FINAL_SIN_ACCIONES`, `ETAPA_NO_VALIDA`, `OBSERVACION_RESPUESTA_REQUERIDA`, `REQUISITO_TRANSICION_FALTANTE`, `ROL_TRANSICION_NO_PERMITIDO`, `SITUACION_CANCELAR_RECHAZADA`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `TIPO_SOLICITUD_SIN_PROCESO`, `TRANSICION_NO_PERMITIDA`; **MalFormato(6)**: `OBSERVACION_ASIGNATURA_MUY_LARGA`, `OBSERVACION_MUY_LARGA` |
| TramiteCancelacionAsignaturaCU.rechazarPorDecano | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `DATOS_TRANSICION_INCOMPLETOS`, `ETAPA_FINAL_SIN_ACCIONES`, `ETAPA_NO_VALIDA`, `REQUISITO_TRANSICION_FALTANTE`, `ROL_TRANSICION_NO_PERMITIDO`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `TIPO_SOLICITUD_SIN_PROCESO`, `TRANSICION_NO_PERMITIDA`; **MalFormato(6)**: `OBSERVACION_MUY_LARGA` |
| TramiteCancelacionAsignaturaCU.rechazarPorFuncionario | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `DATOS_TRANSICION_INCOMPLETOS`, `ETAPA_FINAL_SIN_ACCIONES`, `ETAPA_NO_VALIDA`, `REQUISITO_TRANSICION_FALTANTE`, `ROL_TRANSICION_NO_PERMITIDO`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `TIPO_SOLICITUD_SIN_PROCESO`, `TRANSICION_NO_PERMITIDA`; **MalFormato(6)**: `OBSERVACION_MUY_LARGA` |
| TramiteCancelacionAsignaturaCU.remitirADecano | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `ASIGNATURAS_SIN_EVALUAR`, `ASIGNATURA_AJENA`, `ASIGNATURA_REPETIDA`, `DATOS_TRANSICION_INCOMPLETOS`, `DATO_ASIGNATURA_FALTANTE`, `ETAPA_FINAL_SIN_ACCIONES`, `ETAPA_NO_VALIDA`, `NINGUNA_ASIGNATURA_CUMPLE`, `NOTA_INSUFICIENTE_CUMPLE`, `REQUISITO_TRANSICION_FALTANTE`, `ROL_TRANSICION_NO_PERMITIDO`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `TIPO_SOLICITUD_SIN_PROCESO`, `TRANSICION_NO_PERMITIDA`; **MalFormato(6)**: `NOTA_NO_VALIDA`, `NUMERO_FALTAS_NO_VALIDO`, `OBSERVACION_ASIGNATURA_MUY_LARGA`, `OBSERVACION_MUY_LARGA`, `VALOR_ASIGNATURA_MUY_LARGO` |
| TramiteCancelacionMatriculaCU.aprobarPorDecano | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `ASIGNATURAS_SIN_EVALUAR`, `ASIGNATURA_AJENA`, `ASIGNATURA_REPETIDA`, `DATOS_TRANSICION_INCOMPLETOS`, `DATO_ASIGNATURA_FALTANTE`, `ETAPA_FINAL_SIN_ACCIONES`, `ETAPA_NO_VALIDA`, `REQUISITO_TRANSICION_FALTANTE`, `ROL_TRANSICION_NO_PERMITIDO`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `TIPO_SOLICITUD_SIN_PROCESO`, `TRANSICION_NO_PERMITIDA`; **MalFormato(6)**: `OBSERVACION_MUY_LARGA` |
| TramiteCancelacionMatriculaCU.enviarRespuesta | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `DATOS_TRANSICION_INCOMPLETOS`, `ETAPA_FINAL_SIN_ACCIONES`, `ETAPA_NO_VALIDA`, `REQUISITO_TRANSICION_FALTANTE`, `ROL_TRANSICION_NO_PERMITIDO`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `TIPO_SOLICITUD_SIN_PROCESO`, `TRANSICION_NO_PERMITIDA`; **MalFormato(6)**: `OBSERVACION_MUY_LARGA` |
| TramiteCancelacionMatriculaCU.rechazarPorDecano | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `DATOS_TRANSICION_INCOMPLETOS`, `ETAPA_FINAL_SIN_ACCIONES`, `ETAPA_NO_VALIDA`, `REQUISITO_TRANSICION_FALTANTE`, `ROL_TRANSICION_NO_PERMITIDO`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `TIPO_SOLICITUD_SIN_PROCESO`, `TRANSICION_NO_PERMITIDA`; **MalFormato(6)**: `OBSERVACION_MUY_LARGA` |
| TramiteCancelacionMatriculaCU.rechazarPorFuncionario | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `DATOS_TRANSICION_INCOMPLETOS`, `ETAPA_FINAL_SIN_ACCIONES`, `ETAPA_NO_VALIDA`, `REQUISITO_TRANSICION_FALTANTE`, `ROL_TRANSICION_NO_PERMITIDO`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `TIPO_SOLICITUD_SIN_PROCESO`, `TRANSICION_NO_PERMITIDA`; **MalFormato(6)**: `OBSERVACION_MUY_LARGA` |
| TramiteCancelacionMatriculaCU.remitirADecano | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `ASIGNATURAS_SIN_EVALUAR`, `ASIGNATURA_AJENA`, `ASIGNATURA_REPETIDA`, `DATOS_TRANSICION_INCOMPLETOS`, `DATO_ASIGNATURA_FALTANTE`, `ETAPA_FINAL_SIN_ACCIONES`, `ETAPA_NO_VALIDA`, `REQUISITO_TRANSICION_FALTANTE`, `ROL_TRANSICION_NO_PERMITIDO`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `TIPO_SOLICITUD_SIN_PROCESO`, `TRANSICION_NO_PERMITIDA`; **MalFormato(6)**: `NOTA_NO_VALIDA`, `NUMERO_FALTAS_NO_VALIDO`, `OBSERVACION_MUY_LARGA`, `VALOR_ASIGNATURA_MUY_LARGO` |
| TramiteExamenSupletorioCU.aprobarComprobante | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `DATOS_TRANSICION_INCOMPLETOS`, `ETAPA_FINAL_SIN_ACCIONES`, `ETAPA_NO_VALIDA`, `FECHA_ACORDADA_ANTERIOR`, `REQUISITO_TRANSICION_FALTANTE`, `ROL_TRANSICION_NO_PERMITIDO`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `TIPO_SOLICITUD_SIN_PROCESO`, `TRANSICION_NO_PERMITIDA`; **MalFormato(6)**: `OBSERVACION_MUY_LARGA` |
| TramiteExamenSupletorioCU.aprobarPorDecano | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `DATOS_TRANSICION_INCOMPLETOS`, `ETAPA_FINAL_SIN_ACCIONES`, `ETAPA_NO_VALIDA`, `REQUISITO_TRANSICION_FALTANTE`, `ROL_TRANSICION_NO_PERMITIDO`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `TIPO_SOLICITUD_SIN_PROCESO`, `TRANSICION_NO_PERMITIDA`; **MalFormato(6)**: `OBSERVACION_MUY_LARGA` |
| TramiteExamenSupletorioCU.enviarRecibo | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `DATOS_TRANSICION_INCOMPLETOS`, `ETAPA_FINAL_SIN_ACCIONES`, `ETAPA_NO_VALIDA`, `REQUISITO_TRANSICION_FALTANTE`, `ROL_TRANSICION_NO_PERMITIDO`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `TIPO_SOLICITUD_SIN_PROCESO`, `TRANSICION_NO_PERMITIDA`; **MalFormato(6)**: `OBSERVACION_MUY_LARGA` |
| TramiteExamenSupletorioCU.enviarRespuesta | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `DATOS_TRANSICION_INCOMPLETOS`, `ETAPA_FINAL_SIN_ACCIONES`, `ETAPA_NO_VALIDA`, `REQUISITO_TRANSICION_FALTANTE`, `ROL_TRANSICION_NO_PERMITIDO`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `TIPO_SOLICITUD_SIN_PROCESO`, `TRANSICION_NO_PERMITIDA`; **MalFormato(6)**: `OBSERVACION_MUY_LARGA` |
| TramiteExamenSupletorioCU.rechazarComprobante | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `DATOS_TRANSICION_INCOMPLETOS`, `ETAPA_FINAL_SIN_ACCIONES`, `ETAPA_NO_VALIDA`, `REQUISITO_TRANSICION_FALTANTE`, `ROL_TRANSICION_NO_PERMITIDO`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `TIPO_SOLICITUD_SIN_PROCESO`, `TRANSICION_NO_PERMITIDA`; **MalFormato(6)**: `OBSERVACION_MUY_LARGA` |
| TramiteExamenSupletorioCU.rechazarPorDecano | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `DATOS_TRANSICION_INCOMPLETOS`, `ETAPA_FINAL_SIN_ACCIONES`, `ETAPA_NO_VALIDA`, `REQUISITO_TRANSICION_FALTANTE`, `ROL_TRANSICION_NO_PERMITIDO`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `TIPO_SOLICITUD_SIN_PROCESO`, `TRANSICION_NO_PERMITIDA`; **MalFormato(6)**: `OBSERVACION_MUY_LARGA` |
| TramiteExamenSupletorioCU.rechazarPorFuncionario | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `DATOS_TRANSICION_INCOMPLETOS`, `ETAPA_FINAL_SIN_ACCIONES`, `ETAPA_NO_VALIDA`, `REQUISITO_TRANSICION_FALTANTE`, `ROL_TRANSICION_NO_PERMITIDO`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `TIPO_SOLICITUD_SIN_PROCESO`, `TRANSICION_NO_PERMITIDA`; **MalFormato(6)**: `OBSERVACION_MUY_LARGA` |
| TramiteExamenSupletorioCU.remitirADecano | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `DATOS_TRANSICION_INCOMPLETOS`, `ETAPA_FINAL_SIN_ACCIONES`, `ETAPA_NO_VALIDA`, `REQUISITO_TRANSICION_FALTANTE`, `ROL_TRANSICION_NO_PERMITIDO`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `TIPO_SOLICITUD_SIN_PROCESO`, `TRANSICION_NO_PERMITIDA`; **MalFormato(6)**: `OBSERVACION_MUY_LARGA` |
| TramiteExamenSupletorioCU.subirComprobante | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ACTOR_SIN_ROL`, `DATOS_TRANSICION_INCOMPLETOS`, `ETAPA_FINAL_SIN_ACCIONES`, `ETAPA_NO_VALIDA`, `REQUISITO_TRANSICION_FALTANTE`, `ROL_TRANSICION_NO_PERMITIDO`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `TIPO_SOLICITUD_SIN_PROCESO`, `TRANSICION_NO_PERMITIDA`; **MalFormato(6)**: `OBSERVACION_MUY_LARGA` |
| UsuarioCU.actualizarUsuario | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadExiste(2)**: `ATRIBUTO_UNICO_YA_EXISTE`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `ROLES_DUPLICADOS_USUARIO`, `ROLES_NO_VALIDOS`; **MalFormato(6)**: `TIPO_DOCUMENTO_ERRONEO` |
| UsuarioCU.cambiarContraseña | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA`, `ENTIDAD_NO_ENCONTRADA_FILTRO`; **CredencialesErroneas(5)**: `CONTRASEÑA_INCORRECTA` |
| UsuarioCU.crearUsuario | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadExiste(2)**: `ATRIBUTO_UNICO_YA_EXISTE`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `INSTANCIA_NO_VALIDA`, `ROLES_DUPLICADOS_USUARIO`, `ROLES_NO_VALIDOS`; **MalFormato(6)**: `TIPO_DOCUMENTO_ERRONEO` |
| UsuarioCU.crearUsuarios | **ErrorGenerico(1)**: (mensaje variable), `USERNAME_TOKEN`; **EntidadExiste(2)**: `ATRIBUTO_UNICO_YA_EXISTE`; **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA_FILTRO`; **ReglaNegocioViolada(4)**: `INSTANCIA_NO_VALIDA`, `ROLES_DUPLICADOS_USUARIO`, `ROLES_NO_VALIDOS`; **MalFormato(6)**: `TIPO_DOCUMENTO_ERRONEO`; **SinInformacion(7)**: `ARCHIVO_EXCEL_VACIO` |
| UsuarioCU.getFuncionarios | — |
| UsuarioCU.getTiposUsuario | **SinInformacion(7)**: `SIN_REGISTROS` |
| UsuarioCU.getUsuario | **EntidadNoExiste(3)**: `ENTIDAD_NO_ENCONTRADA` |
| UsuarioCU.getUsuarios | **MalFormato(6)**: `PAGINACION_ERROR`; **SinInformacion(7)**: `SIN_REGISTROS` |
| UsuarioCU.getUsuariosByNombreCompleto | **MalFormato(6)**: `PAGINACION_ERROR`; **SinInformacion(7)**: `SIN_REGISTROS` |

## Anexo C. Tablas leídas por endpoint

| # | Endpoint | Tablas leídas (L) |
|---|---|---|
| 1 | POST `/sesiones` | usuarios |
| 2 | GET `/roles` | roles |
| 3 | GET `/roles/paginado` | roles |
| 4 | GET `/roles/{uuidRol}` | roles |
| 5 | PUT `/roles/{uuidRol}` | usuarios |
| 6 | GET `/logs` | logs |
| 7 | GET `/logs/paginado` | logs |
| 8 | GET `/logs/filtro` | logs |
| 9 | GET `/usuarios/tipos` | tiposUsuario, usuarios |
| 10 | GET `/usuarios/paginado` | usuariosLivianos |
| 11 | GET `/usuarios/filtro` | usuariosLivianos |
| 12 | GET `/usuarios` | usuariosLivianos |
| 13 | GET `/usuarios/funcionarios` | funcionarios |
| 14 | GET `/usuarios/{uuidUsuario}` | usuarios |
| 15 | POST `/usuarios` | roles, tiposUsuario |
| 16 | POST `/usuarios/cargar/archivo` | roles, tiposUsuario |
| 17 | PUT `/usuarios/{uuidUsuario}` | roles |
| 18 | PATCH `/usuarios/{uuidUsuario}` | — |
| 19 | GET `/tipos/solicitudes/paginado` | tiposSolicitudes |
| 20 | GET `/tipos/solicitudes/filtro` | tiposSolicitudes |
| 21 | GET `/tipos/solicitudes` | tiposSolicitudes |
| 22 | GET `/tipos/solicitudes/{uuidTipoSolicitud}` | tiposSolicitudes |
| 23 | POST `/tipos/solicitudes` | roles, usuarios |
| 24 | POST `/tipos/solicitudes/cargar/archivo` | roles, usuarios |
| 25 | PUT `/tipos/solicitudes/{uuidTipoSolicitud}` | roles, usuarios |
| 26 | GET `/tipos/solicitudes/perfil` | tiposSolicitudes |
| 27 | GET `/tipos/solicitudes/perfil/paginado` | tiposSolicitudes |
| 28 | GET `/tipos/solicitudes/perfil/filtro` | tiposSolicitudes |
| 29 | GET `/solicitudes/orden-del-dia/paginado` | ordenesDelDia |
| 30 | GET `/solicitudes/orden-del-dia` | ordenesDelDia |
| 31 | GET `/solicitudes/orden-del-dia/{uuidOrdenDelDia}` | ordenesDelDia |
| 32 | POST `/solicitudes/orden-del-dia` | usuarios |
| 33 | PUT `/solicitudes/orden-del-dia/{uuidOrdenDelDia}` | usuarios |
| 34 | GET `/solicitudes/paginado` | solicitudes |
| 35 | GET `/solicitudes` | solicitudes |
| 36 | GET `/solicitudes/{uuidSolicitud}` | solicitudes |
| 37 | POST `/solicitudes` | ordenesDelDia, tiposSolicitudes, usuarios |
| 38 | POST `/solicitudes/public` | tiposSolicitudes, usuarios |
| 39 | PUT `/solicitudes/{uuidSolicitud}` | ordenesDelDia, usuarios |
| 40 | GET `/solicitudes/funcionario/{uuidFuncionario}` | solicitudes |
| 41 | GET `/solicitudes/orden-del-dia/{uuidOrdenDelDia}/solicitudes` | solicitudes |
| 42 | GET `/solicitudes/estado` | solicitudes |
| 43 | GET `/solicitudes/buscar` | solicitudes |
| 44 | GET `/solicitudes/buscar/solicitante` | solicitudes |
| 45 | GET `/solicitudes/fun/buscar` | solicitudes |
| 46 | GET `/solicitudes/orden-del-dia/buscar` | ordenesDelDia |
| 47 | GET `/solicitudes/anexos/{uuidSolicitud}/{fileName}` | — |
| 48 | GET `/solicitudes/anexos/download` | solicitudes |
| 49 | GET `/solicitudes/exportar` | ordenesDelDia, respuestas, solicitudes |
| 50 | GET `/solicitudes/exportar/respuestas` | ordenesDelDia, respuestas, solicitudes |
| 51 | GET `/solicitudes/exportar/reunion` | ordenesDelDia, respuestas, solicitudes |
| 52 | GET `/solicitudes/orden-del-dia/estado` | ordenesDelDia |
| 53 | GET `/respuestas/paginado` | respuestas |
| 54 | GET `/respuestas/paginado/nombre-solicitud` | respuestas |
| 55 | GET `/respuestas/paginado/funcionario` | respuestas |
| 56 | GET `/respuestas/paginado/funcionario-nombre-solicitud` | respuestas |
| 57 | GET `/respuestas/{uuidRespuesta}` | respuestas |
| 58 | GET `/respuestas/solicitud/{uuidSolicitud}` | respuestas |
| 59 | POST `/respuestas/{uuidSolicitud}` | solicitudes, usuarios |
| 60 | POST `/respuestas/{uuidRespuesta}/archivo` | usuarios |
| 61 | PATCH `/respuestas/{uuidRespuesta}/archivo` | usuarios |
| 62 | GET `/respuestas/{uuidRespuesta}/{fileName}` | — |
| 63 | POST `/asignaturas/cargar/archivo` | PROGRAMA_ACADEMICO, usuarios |
| 64 | POST `/asignaturas` | PROGRAMA_ACADEMICO, usuarios |
| 65 | GET `/asignaturas/paginado` | ASIGNATURA |
| 66 | GET `/asignaturas/filtro` | ASIGNATURA, PROGRAMA_ACADEMICO |
| 67 | GET `/asignaturas/programa/{uuidPrograma}` | ASIGNATURA, PROGRAMA_ACADEMICO |
| 68 | GET `/asignaturas/{uuidAsignatura}` | ASIGNATURA |
| 69 | PUT `/asignaturas/{uuidAsignatura}` | PROGRAMA_ACADEMICO, usuarios |
| 70 | POST `/estudiantes/cargar/archivo` | ASIGNATURA, PROGRAMA_ACADEMICO, roles, tiposUsuario |
| 71 | POST `/estudiantes` | ASIGNATURA, PROGRAMA_ACADEMICO, roles, tiposUsuario |
| 72 | GET `/estudiantes/paginado` | ESTUDIANTE |
| 73 | GET `/estudiantes/filtro` | ESTUDIANTE |
| 74 | GET `/estudiantes/mis-asignaturas` | ASIGNATURA_MATRICULADA, ESTUDIANTE, usuarios |
| 75 | GET `/estudiantes/{uuidEstudiante}` | ESTUDIANTE |
| 76 | PUT `/estudiantes/{uuidEstudiante}` | ASIGNATURA, PROGRAMA_ACADEMICO |
| 77 | POST `/estudiantes/{uuidEstudiante}/asignaturas` | ASIGNATURA, ESTUDIANTE, PROGRAMA_ACADEMICO, usuarios |
| 78 | PATCH `/estudiantes/{uuidEstudiante}/asignaturas/{uuidMatricula}/estado` | ASIGNATURA, ESTUDIANTE, PROGRAMA_ACADEMICO, usuarios |
| 79 | POST `/funcionarios-academicos` | roles, tiposUsuario |
| 80 | POST `/funcionarios-academicos/cargar/archivo` | roles, tiposUsuario |
| 81 | GET `/funcionarios-academicos/paginado` | FUNCIONARIO_ACADEMICO |
| 82 | GET `/funcionarios-academicos/filtro` | FUNCIONARIO_ACADEMICO |
| 83 | GET `/funcionarios-academicos/{uuidFuncionario}` | FUNCIONARIO_ACADEMICO |
| 84 | PUT `/funcionarios-academicos/{uuidFuncionario}` | — |
| 85 | GET `/catalogos-academicos/tipos-solicitud` | TIPO_SOLICITUD_ACADEMICA |
| 86 | GET `/catalogos-academicos/tipos-solicitud/{uuidTipo}/etapas` | ETAPA_SOLICITUD_ACADEMICA, TIPO_SOLICITUD_ACADEMICA |
| 87 | GET `/catalogos-academicos/tipos-solicitud/{uuidTipo}/tipos-anexo` | TIPO_ANEXO_ACADEMICO, TIPO_SOLICITUD_ACADEMICA |
| 88 | GET `/catalogos-academicos/etiquetas` | ETAPA_ETIQUETA_ROL |
| 89 | GET `/catalogos-academicos/situaciones` | SITUACION_ACADEMICA_ASIGNATURA |
| 90 | GET `/catalogos-academicos/programas` | PROGRAMA_ACADEMICO |
| 91 | PUT `/catalogos-academicos/tipos-solicitud/{uuidTipo}/funcionario` | FUNCIONARIO_ACADEMICO, usuarios |
| 92 | GET `/solicitudes-academicas/estudiante` | ASIGNATURA_SOLICITUD_ACADEMICA, ETAPA_ETIQUETA_ROL, HISTORIAL_SOLICITUD_ACADEMICA, SOLICITUD_ACADEMICA, usuarios |
| 93 | GET `/solicitudes-academicas/funcionario` | ASIGNATURA_SOLICITUD_ACADEMICA, ETAPA_ETIQUETA_ROL, HISTORIAL_SOLICITUD_ACADEMICA, SOLICITUD_ACADEMICA, usuarios |
| 94 | GET `/solicitudes-academicas/decano` | ASIGNATURA_SOLICITUD_ACADEMICA, ETAPA_ETIQUETA_ROL, HISTORIAL_SOLICITUD_ACADEMICA, SOLICITUD_ACADEMICA, usuarios |
| 95 | GET `/solicitudes-academicas/{uuidSolicitud}` | ANEXO_ACADEMICO, ETAPA_ETIQUETA_ROL, RESOLUCION_ACADEMICA, SOLICITUD_ACADEMICA, usuarios |
| 96 | GET `/solicitudes-academicas/{uuidSolicitud}/historial` | ETAPA_ETIQUETA_ROL, HISTORIAL_SOLICITUD_ACADEMICA, SOLICITUD_ACADEMICA, usuarios |
| 97 | POST `/solicitudes-academicas/{uuidSolicitud}/anexos` | ETAPA_ETIQUETA_ROL, SOLICITUD_ACADEMICA, TIPO_ANEXO_ACADEMICO, usuarios |
| 98 | GET `/solicitudes-academicas/{uuidSolicitud}/anexos/{uuidAnexo}` | ANEXO_ACADEMICO, ETAPA_ETIQUETA_ROL, SOLICITUD_ACADEMICA, usuarios |
| 99 | POST `/solicitudes-academicas/{uuidSolicitud}/resolucion` | ETAPA_ETIQUETA_ROL, FUNCIONARIO_ACADEMICO, SOLICITUD_ACADEMICA, usuarios |
| 100 | GET `/solicitudes-academicas/{uuidSolicitud}/resolucion` | ETAPA_ETIQUETA_ROL, RESOLUCION_ACADEMICA, SOLICITUD_ACADEMICA, usuarios |
| 101 | GET `/cancelaciones-matricula/formulario` | ASIGNATURA_MATRICULADA, ESTUDIANTE, TIPO_ANEXO_ACADEMICO, TIPO_SOLICITUD_ACADEMICA, usuarios |
| 102 | POST `/cancelaciones-matricula` | ASIGNATURA_MATRICULADA, ESTUDIANTE, ETAPA_SOLICITUD_ACADEMICA, TIPO_ANEXO_ACADEMICO, TIPO_SOLICITUD_ACADEMICA, usuarios |
| 103 | GET `/cancelaciones-matricula/{uuidSolicitud}` | ANEXO_ACADEMICO, ASIGNATURA_SOLICITUD_ACADEMICA, ETAPA_ETIQUETA_ROL, RESOLUCION_ACADEMICA, SOLICITUD_ACADEMICA, SOLICITUD_CANCELACION_MATRICULA, usuarios |
| 104 | POST `/cancelaciones-matricula/{uuidSolicitud}/funcionario/rechazar` | ANEXO_ACADEMICO, ASIGNATURA_SOLICITUD_ACADEMICA, ETAPA_ETIQUETA_ROL, ETAPA_SOLICITUD_ACADEMICA, RESOLUCION_ACADEMICA, SOLICITUD_CANCELACION_MATRICULA, usuarios |
| 105 | POST `/cancelaciones-matricula/{uuidSolicitud}/funcionario/remitir` | ANEXO_ACADEMICO, ETAPA_ETIQUETA_ROL, ETAPA_SOLICITUD_ACADEMICA, SITUACION_ACADEMICA_ASIGNATURA, SOLICITUD_CANCELACION_MATRICULA, usuarios |
| 106 | POST `/cancelaciones-matricula/{uuidSolicitud}/funcionario/responder` | ANEXO_ACADEMICO, ASIGNATURA_SOLICITUD_ACADEMICA, ETAPA_ETIQUETA_ROL, ETAPA_SOLICITUD_ACADEMICA, RESOLUCION_ACADEMICA, SOLICITUD_CANCELACION_MATRICULA, usuarios |
| 107 | POST `/cancelaciones-matricula/{uuidSolicitud}/decano/aprobar` | ANEXO_ACADEMICO, ETAPA_ETIQUETA_ROL, ETAPA_SOLICITUD_ACADEMICA, RESOLUCION_ACADEMICA, SITUACION_ACADEMICA_ASIGNATURA, SOLICITUD_CANCELACION_MATRICULA, usuarios |
| 108 | POST `/cancelaciones-matricula/{uuidSolicitud}/decano/rechazar` | ANEXO_ACADEMICO, ASIGNATURA_SOLICITUD_ACADEMICA, ETAPA_ETIQUETA_ROL, ETAPA_SOLICITUD_ACADEMICA, RESOLUCION_ACADEMICA, SOLICITUD_CANCELACION_MATRICULA, usuarios |
| 109 | GET `/cancelaciones-asignatura/formulario` | ASIGNATURA_MATRICULADA, ESTUDIANTE, usuarios |
| 110 | POST `/cancelaciones-asignatura` | ASIGNATURA_MATRICULADA, ESTUDIANTE, ETAPA_SOLICITUD_ACADEMICA, TIPO_ANEXO_ACADEMICO, TIPO_SOLICITUD_ACADEMICA, usuarios |
| 111 | GET `/cancelaciones-asignatura/{uuidSolicitud}` | ANEXO_ACADEMICO, ASIGNATURA_SOLICITUD_ACADEMICA, ETAPA_ETIQUETA_ROL, RESOLUCION_ACADEMICA, SOLICITUD_ACADEMICA, SOLICITUD_CANCELACION_ASIGNATURA, usuarios |
| 112 | POST `/cancelaciones-asignatura/{uuidSolicitud}/funcionario/rechazar` | ANEXO_ACADEMICO, ASIGNATURA_SOLICITUD_ACADEMICA, ETAPA_ETIQUETA_ROL, ETAPA_SOLICITUD_ACADEMICA, RESOLUCION_ACADEMICA, SOLICITUD_CANCELACION_ASIGNATURA, usuarios |
| 113 | POST `/cancelaciones-asignatura/{uuidSolicitud}/funcionario/remitir` | ANEXO_ACADEMICO, ETAPA_ETIQUETA_ROL, ETAPA_SOLICITUD_ACADEMICA, SITUACION_ACADEMICA_ASIGNATURA, SOLICITUD_CANCELACION_ASIGNATURA, usuarios |
| 114 | POST `/cancelaciones-asignatura/{uuidSolicitud}/funcionario/responder` | ANEXO_ACADEMICO, ETAPA_ETIQUETA_ROL, ETAPA_SOLICITUD_ACADEMICA, RESOLUCION_ACADEMICA, SITUACION_ACADEMICA_ASIGNATURA, SOLICITUD_CANCELACION_ASIGNATURA, usuarios |
| 115 | POST `/cancelaciones-asignatura/{uuidSolicitud}/decano/aprobar` | ANEXO_ACADEMICO, ETAPA_ETIQUETA_ROL, ETAPA_SOLICITUD_ACADEMICA, RESOLUCION_ACADEMICA, SITUACION_ACADEMICA_ASIGNATURA, SOLICITUD_CANCELACION_ASIGNATURA, usuarios |
| 116 | POST `/cancelaciones-asignatura/{uuidSolicitud}/decano/rechazar` | ANEXO_ACADEMICO, ASIGNATURA_SOLICITUD_ACADEMICA, ETAPA_ETIQUETA_ROL, ETAPA_SOLICITUD_ACADEMICA, RESOLUCION_ACADEMICA, SOLICITUD_CANCELACION_ASIGNATURA, usuarios |
| 117 | GET `/examenes-supletorios/formulario` | ASIGNATURA_MATRICULADA, ESTUDIANTE, TIPO_ANEXO_ACADEMICO, TIPO_SOLICITUD_ACADEMICA, usuarios |
| 118 | POST `/examenes-supletorios` | ASIGNATURA_MATRICULADA, ESTUDIANTE, ETAPA_SOLICITUD_ACADEMICA, TIPO_ANEXO_ACADEMICO, TIPO_SOLICITUD_ACADEMICA, usuarios |
| 119 | GET `/examenes-supletorios/{uuidSolicitud}` | ANEXO_ACADEMICO, ETAPA_ETIQUETA_ROL, RESOLUCION_ACADEMICA, SOLICITUD_ACADEMICA, SOLICITUD_EXAMEN_SUPLETORIO, SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA, usuarios |
| 120 | POST `/examenes-supletorios/{uuidSolicitud}/funcionario/rechazar` | ANEXO_ACADEMICO, ETAPA_ETIQUETA_ROL, ETAPA_SOLICITUD_ACADEMICA, RESOLUCION_ACADEMICA, SOLICITUD_EXAMEN_SUPLETORIO, SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA, usuarios |
| 121 | POST `/examenes-supletorios/{uuidSolicitud}/funcionario/remitir` | ANEXO_ACADEMICO, ETAPA_ETIQUETA_ROL, ETAPA_SOLICITUD_ACADEMICA, SOLICITUD_EXAMEN_SUPLETORIO, SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA, usuarios |
| 122 | POST `/examenes-supletorios/{uuidSolicitud}/funcionario/responder` | ANEXO_ACADEMICO, ETAPA_ETIQUETA_ROL, ETAPA_SOLICITUD_ACADEMICA, RESOLUCION_ACADEMICA, SOLICITUD_EXAMEN_SUPLETORIO, SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA, usuarios |
| 123 | POST `/examenes-supletorios/{uuidSolicitud}/funcionario/recibo` | ANEXO_ACADEMICO, ETAPA_ETIQUETA_ROL, ETAPA_SOLICITUD_ACADEMICA, RESOLUCION_ACADEMICA, SOLICITUD_EXAMEN_SUPLETORIO, SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA, usuarios |
| 124 | POST `/examenes-supletorios/{uuidSolicitud}/funcionario/comprobante/aprobar` | ANEXO_ACADEMICO, ETAPA_ETIQUETA_ROL, ETAPA_SOLICITUD_ACADEMICA, RESOLUCION_ACADEMICA, SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA, usuarios |
| 125 | POST `/examenes-supletorios/{uuidSolicitud}/funcionario/comprobante/rechazar` | ANEXO_ACADEMICO, ETAPA_ETIQUETA_ROL, ETAPA_SOLICITUD_ACADEMICA, RESOLUCION_ACADEMICA, SOLICITUD_EXAMEN_SUPLETORIO, SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA, usuarios |
| 126 | POST `/examenes-supletorios/{uuidSolicitud}/decano/aprobar` | ANEXO_ACADEMICO, ETAPA_ETIQUETA_ROL, ETAPA_SOLICITUD_ACADEMICA, RESOLUCION_ACADEMICA, SOLICITUD_EXAMEN_SUPLETORIO, SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA, usuarios |
| 127 | POST `/examenes-supletorios/{uuidSolicitud}/decano/rechazar` | ANEXO_ACADEMICO, ETAPA_ETIQUETA_ROL, ETAPA_SOLICITUD_ACADEMICA, RESOLUCION_ACADEMICA, SOLICITUD_EXAMEN_SUPLETORIO, SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA, usuarios |
| 128 | POST `/examenes-supletorios/{uuidSolicitud}/estudiante/comprobante` | ANEXO_ACADEMICO, ETAPA_ETIQUETA_ROL, ETAPA_SOLICITUD_ACADEMICA, RESOLUCION_ACADEMICA, SOLICITUD_EXAMEN_SUPLETORIO, SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA, usuarios |
