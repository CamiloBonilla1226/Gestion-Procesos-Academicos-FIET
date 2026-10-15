# 06 - Seguridad, manejo de errores y registro de acciones

Análisis de solo lectura del backend (`backend/solicitudes`) y de la parte del frontend que toca la sesión. Etiquetas: **[BASE]** código del prototipo de Julián (commit `d3a48cc`, "Inicializa el repositorio"), **[EXTENSIÓN]** agregado por este trabajo de grado. Todo lo no comprobado leyendo código figura como NO VERIFICADO. No se ejecutó nada (ni backend, ni pruebas, ni base de datos).

Raíz de rutas Java: `backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/` (abreviada `<J>/`).

## 1. Piezas de seguridad

| Pieza | Ruta y clase | Origen |
|---|---|---|
| Cadena de filtros y reglas por URL | `<J>/infraestructura/configuracion/seguridad/configuracion/ConfiguracionSeguridad.java`, método `securityFilterChain` | [BASE] estructura, [EXTENSIÓN] reglas |
| Beans de autenticación | `.../seguridad/configuracion/ConfiguracionAplicacion.java` (`authenticationManager`, `userDetailService`, `authenticationProvider`, `passwordEncoder`) | [BASE] |
| Respuesta 401 | `.../seguridad/configuracion/CustomAuthenticationEntryPoint.java` | [BASE] |
| Respuesta 403 | `.../seguridad/configuracion/CustomAccessDeniedHandler.java` | [BASE] |
| Filtro JWT | `.../seguridad/jwt/JwtFiltroAutenticacion.java` (`OncePerRequestFilter`) | [BASE] |
| Servicio JWT | `.../seguridad/jwt/JwtServicio.java` | [BASE] |
| Puerto para extraer el username del token | `<J>/aplicacion/output/IJwtServicio.java`, implementado en `<J>/infraestructura/output/JWT/JwtServicioImpl.java` | [BASE] |
| Login | `<J>/infraestructura/input/controladorSesion/controlador/SesionRestController.java` (`POST ${url.application}sesiones`) -> `<J>/dominio/casosdeuso/SesionCUImplAdaptador.java` (`login`) -> `<J>/infraestructura/output/persistencia/gateway/SesionGatewayImplAdaptador.java` (`login`) | [BASE] |
| Codificador | `<J>/infraestructura/output/encoder/PasswordEnconderGatewayImplAdaptador.java` (`encode`, `matches`) | [BASE] |
| Roles como authorities | `UsuarioEntidad.getAuthorities()` (`<J>/infraestructura/output/persistencia/entidades/UsuarioEntidad.java`) | [BASE] |
| Constantes de rol y perfiles | `<J>/dominio/helper/constantes/ApplicationConstantes.java` | [BASE] los roles `Secretario General`, `Decano`, `Funcionario` y 3 perfiles; [EXTENSIÓN] `ESTUDIANTE_ROL`, `FUNCIONARIO_ACADEMICO_ROL` y 6 perfiles compuestos |

Dependencias (`backend/solicitudes/pom.xml`): Spring Boot 3.5.4 (`spring-boot-starter-parent`), `spring-boot-starter-security`, `jjwt-api/impl/jackson` 0.11.2.

## 2. Flujo de autenticación paso a paso [BASE]

1. El cliente envía `POST /api/unicauca/fiet/consejo/sesiones` con `username` y `password` (`SesionDTOPeticion`, validado con `@Valid` en `SesionRestController.auth`). La ruta es pública (`permitAll`).
2. `SesionCUImplAdaptador.login` llama a `SesionGatewayImplAdaptador.login`.
3. El gateway busca el usuario con `UsuarioRepositorio.findByUsername`. Si no existe devuelve `null`; el caso de uso lanza entonces `lanzarCredencialesErroneas(MensajesError.CREDENCIALES_ERRONEAS)`.
4. Si existe, ejecuta `authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(...))`. El `DaoAuthenticationProvider` (bean `authenticationProvider`) carga el usuario con el `UserDetailsService` y compara la contraseña con `BCryptPasswordEncoder`. Una contraseña errónea lanza `BadCredentialsException`.
5. Si autentica, `JwtServicio.getToken(usuario)` genera el JWT.
6. De vuelta en el caso de uso: si `usuario.getEstado()` es falso se lanza `lanzarSinAcceso(MensajesError.NO_ACCESO)`; si algún rol del usuario tiene `estado` falso se lanza `lanzarSinAcceso(MensajesError.ROL_NO_HABILITADO)`.
7. Se registra el log "Inicio de sesión" con `LogCUImplAdaptador.crearLogSesion` (el username llega directo porque todavía no hay token en el contexto).
8. La respuesta es `UsuarioTokenizadoDTORespuesta` con `uuidUsuario` y `token` (campos del modelo `dominio/modelos/UsuarioTokenizado.java`). El rol no viaja en el cuerpo; el frontend guarda `uuidUsuario` en `localStorage` y consulta el usuario aparte (`AuthService`, `frontend/src/app/core/services/auth-service.ts`; detalle de la consulta NO VERIFICADO).

Peticiones posteriores: `JwtFiltroAutenticacion.doFilterInternal` lee la cabecera `Authorization: Bearer <token>`; si no hay token deja pasar la petición sin autenticar (la decide después `authorizeHttpRequests`); si lo hay, extrae el `subject`, carga el `UserDetails`, valida con `JwtServicio.isTokenValid` (mismo username y no expirado) y coloca un `UsernamePasswordAuthenticationToken` en el `SecurityContextHolder` con las authorities del usuario (nombres de sus roles). Si el token expiró (`ExpiredJwtException`) el filtro responde él mismo 401 con el JSON `Error` y el mensaje de `CodigoError.ERROR_TOKEN_EXPIRADO`.

Los casos de uso que necesitan saber quién actúa reciben el token crudo desde el controlador (`@RequestHeader("Authorization") String token`, luego `token.substring(7)`) y lo resuelven con `IJwtServicio.getUsername`; por ejemplo `SolicitudAcademicaRestController.getDetalle` (patrón [EXTENSIÓN] repetido de la base).

## 3. El JWT [BASE]

Fuente: `JwtServicio.java`.

| Aspecto | Valor |
|---|---|
| Biblioteca | jjwt 0.11.2 (`Jwts.builder()` / `parserBuilder()`) |
| Algoritmo | HS256 (`SignatureAlgorithm.HS256`) |
| Clave | constante `SECRET_KEY` escrita en el código fuente, hexadecimal de 64 caracteres que se decodifica con `Decoders.BASE64`: `<REDACTADO>` |
| Claims propios | Ninguno. `getToken(UserDetails)` pasa `new HashMap<>()` como claims extra |
| Claims estándar | `sub` = username, `iat` = momento de emisión, `exp` = emisión + 24 h (`24 * 60 * 60 * 1000` ms) |
| Roles en el token | No. Las authorities se leen de la base de datos en cada petición vía `UserDetailsService` (`ConfiguracionAplicacion.userDetailService`), de modo que un cambio de rol o de estado aplica de inmediato aunque el token siga vigente |
| Refresco | No existe refresh token ni endpoint de renovación (no se encontró) |
| Revocación / logout en servidor | No existe; el logout del frontend solo borra el token del navegador |
| Validación | `isTokenValid`: username igual al del `UserDetails` y `!isTokenExpired` |

Observación: el estado del usuario (`estado`) solo se comprueba al iniciar sesión (`SesionCUImplAdaptador.login`); el filtro JWT no vuelve a revisarlo. Un usuario desactivado conserva acceso con un token ya emitido hasta que expire (NO VERIFICADO en ejecución; se infiere de `JwtFiltroAutenticacion`, que no consulta `estado`, y de que `UsuarioEntidad` no se vio sobreescribiendo `isEnabled`).

## 4. Contraseñas [BASE]

- `ConfiguracionAplicacion.passwordEncoder()` devuelve `new BCryptPasswordEncoder()` (fuerza por defecto de Spring Security, 10 rondas).
- `PasswordEnconderGatewayImplAdaptador.encode` y `.matches` se usan al crear usuarios y al cambiar contraseña (log "Actualizar Usuario - Usuario actualizo contraseña con éxito" en `UsuarioCUImplAdaptador`).
- `src/main/resources/data.sql` inserta el usuario inicial `rootfiet` (Secretario General) con la contraseña ya codificada con BCrypt; contraseña de texto plano documentada en el `README.md` raíz: `<REDACTADO>`.
- Los usuarios Estudiante y Funcionario Académico [EXTENSIÓN] se crean por el mismo `UsuarioCUImplAdaptador` (la contraseña pasa por el mismo codificador).

## 5. CORS y CSRF

Fuente: `ConfiguracionSeguridad.corsConfigurationSource()` [BASE].

| Parámetro | Valor |
|---|---|
| Orígenes permitidos | uno solo: `${url.frontend}` (en `application.properties`: `http://localhost:4200`); fijo, no cambia por entorno |
| Métodos | GET, POST, PUT, PATCH, DELETE, OPTIONS |
| Cabeceras permitidas | `*` |
| Credenciales | `allowCredentials = true` |
| Cabeceras expuestas | `Authorization` |
| Alcance | `/**` |

Además cada controlador trae `@CrossOrigin(origins = "${url.frontend}")` (por ejemplo `SesionRestController`, `LogRestController`) [BASE].

CSRF: deshabilitado (`csrf(AbstractHttpConfigurer::disable)`) y sesión `STATELESS` (`SessionCreationPolicy.STATELESS`). Es coherente con un API que autentica por cabecera Bearer.

## 6. Reglas de acceso por URL (`securityFilterChain`)

`baseUrl` = `${url.application}` = `/api/unicauca/fiet/consejo/`. Constantes de rol: SG = `Secretario General`, DEC = `Decano`, FUN = `Funcionario` (Julián), FA = `Funcionario Académico`, EST = `Estudiante`.

Cantidad de `requestMatchers`: 17 en el commit base (`git show d3a48cc:...ConfiguracionSeguridad.java`) y 78 hoy, es decir 61 agregados por la extensión. Spring evalúa en orden, gana la primera coincidencia.

### 6.1 Reglas heredadas [BASE] (17)

| Orden | Método y ruta | Acceso |
|---|---|---|
| 1 | cualquiera `sesiones` | pública |
| 2 | POST `solicitudes/public` | pública |
| 3-6 | GET `tipos/solicitudes/perfil`, `.../perfil/paginado`, `.../perfil/filtro`, `.../{uuidTipoSolicitud}` | públicas |
| 7 | `tipos/solicitudes/**` (resto) | SG, DEC |
| 8 | GET `solicitudes/orden-del-dia/estado` | autenticado |
| 9 | `solicitudes/orden-del-dia/**` | SG, DEC |
| 10 | `logs/**` | SG, DEC |
| 11 | `roles/**` | SG, DEC |
| 12 | GET `usuarios/**` | autenticado (en el commit base) |
| 13 | PATCH `usuarios/**` | autenticado (cambio de contraseña propia) |
| 14 | `usuarios/**` (resto) | SG, DEC |
| 15 | `solicitudes/**` | autenticado |
| 16 | GET `respuestas/**` | autenticado |
| 17 | POST `respuestas/**` | SG, DEC, FUN |
| final | `anyRequest()` | autenticado |

### 6.2 Reglas agregadas [EXTENSIÓN]

- `usuarios`: el commit `ac4fb00` ("restringir los listados de usuarios a Secretario General y Decano") antepone cinco GET a la regla genérica: `usuarios`, `usuarios/paginado`, `usuarios/filtro`, `usuarios/funcionarios` y `usuarios/tipos` con acceso SG, DEC. El resto de GET `usuarios/**` sigue autenticado.
- Ocho prefijos académicos, cada uno cerrado con `denyAll()` para que una ruta olvidada quede bloqueada en vez de caer en `anyRequest().authenticated()`:

| Prefijo | Reglas |
|---|---|
| `asignaturas` | GET `programa/{uuidPrograma}` y GET `asignaturas/**`: SG, DEC, FA. POST `cargar/archivo`, POST `asignaturas`, PUT `asignaturas/**`: SG, DEC. Resto: `denyAll` |
| `estudiantes` | GET `mis-asignaturas`: EST. GET `paginado`, `filtro`, `{uuidEstudiante}`: SG, DEC, FA. POST `cargar/archivo`, POST `estudiantes`, PUT `{uuid}`, POST `{uuid}/asignaturas`, PATCH `{uuid}/asignaturas/{uuidMatricula}/estado`: SG, DEC. Resto: `denyAll` |
| `funcionarios-academicos` | GET `paginado`, `filtro`, `{uuidFuncionario}`: SG, DEC, FA. POST `cargar/archivo`, POST, PUT `{uuid}`: SG, DEC. Resto: `denyAll` |
| `catalogos-academicos` | PUT `tipos-solicitud/{uuidTipo}/funcionario`: SG, DEC. GET `programas` y GET `catalogos-academicos/**`: autenticado. Resto: `denyAll` |
| `solicitudes-academicas` | GET `estudiante`: EST. GET `funcionario`: FA. GET `decano`: DEC. GET `{uuid}`, `{uuid}/historial`, `{uuid}/anexos/{uuidAnexo}`, `{uuid}/resolucion`: EST, FA, DEC. POST `{uuid}/anexos`: EST, FA. POST `{uuid}/resolucion`: FA. Resto: `denyAll` |
| `cancelaciones-matricula` | GET `formulario` y POST radicación: EST. POST `{uuid}/funcionario/**`: FA. POST `{uuid}/decano/**`: DEC. GET `{uuid}`: EST, FA, DEC. Resto: `denyAll` |
| `cancelaciones-asignatura` | Igual que el anterior |
| `examenes-supletorios` | Igual, más POST `{uuid}/estudiante/**`: EST |

Consecuencias que se leen en el código:

- El Secretario General no tiene acceso a ninguna ruta de `solicitudes-academicas`, `cancelaciones-*` ni `examenes-supletorios` (regla de negocio documentada en `backend/CLAUDE.md`).
- Orden de reglas: `solicitudes/**` (línea 15 de la tabla 6.1, `authenticated()`) se declara antes que `solicitudes-academicas/**`. Spring `requestMatchers` compara por ruta exacta de prefijo con `**`; `solicitudes/**` no coincide con `solicitudes-academicas/...` porque el segmento es distinto, así que no hay solapamiento (razonamiento sobre el patrón; NO VERIFICADO por ejecución, aunque `backend/pruebas/matriz-permisos.ps1` está diseñada para comprobarlo).

### 6.3 Seguridad por método (`@PreAuthorize`) [BASE] mecanismo, [EXTENSIÓN] perfiles

- 138 ocurrencias de `@PreAuthorize` en `<J>/infraestructura/input/**` (conteo con `grep -rn PreAuthorize`), con expresiones definidas en `ApplicationConstantes` (`SECRETARIO_DECANO_ACCESO`, `ESTUDIANTE_ACCESO`, `FUNCIONARIO_ACADEMICO_ACCESO`, `DECANO_ACCESO`, etc.).
- **Hallazgo H1:** no se encontró `@EnableMethodSecurity` ni `@EnableGlobalMethodSecurity` en todo `src/main` ni en `pom.xml` (`grep -rniE "MethodSecurity|PrePost"`). En Spring Security 6 las anotaciones `@PreAuthorize` no se evalúan sin esa habilitación. Por tanto, lo que efectivamente protege los endpoints es la lista de `requestMatchers` de la sección 6; las anotaciones por método serían decorativas. El script `backend/pruebas/t1_preauthorize.ps1` existe para probar este comportamiento, pero no se ejecutó (NO VERIFICADO en ejecución). Esto no abre un hueco mientras las reglas por URL estén completas, pero hay doble fuente de verdad que puede divergir.

## 7. Hallazgos de seguridad

| Id | Hallazgo | Evidencia | Origen |
|---|---|---|---|
| H1 | `@PreAuthorize` sin `@EnableMethodSecurity` (sección 6.3) | búsqueda en `src/main` sin resultados | [BASE] el mecanismo, [EXTENSIÓN] lo replica en los controladores nuevos |
| H2 | Clave de firma del JWT escrita en el código fuente y versionada | `JwtServicio.SECRET_KEY` (valor `<REDACTADO>`); no se lee de variable de entorno | [BASE] |
| H3 | Token de 24 h sin refresco ni revocación; el cierre de sesión es solo del cliente | `JwtServicio.getToken`; `AuthService.logout` borra `localStorage` | [BASE] |
| H4 | Token guardado en `localStorage` (`authToken`, `currentUser`) del navegador, expuesto a XSS | `frontend/src/app/core/services/auth-service.ts` líneas 97-98; lo lee `frontend/src/app/core/auth/interceptor/auth-interceptor.ts` | [BASE] |
| H5 | Un token con firma inválida o mal formado (`SignatureException`, `MalformedJwtException`) no se captura en el filtro (solo `ExpiredJwtException`); la excepción sale del filtro, fuera del alcance de `@ControllerAdvice`. Respuesta exacta NO VERIFICADO (probablemente 500 o la página de error de Spring) | `JwtFiltroAutenticacion.doFilterInternal` | [BASE] |
| H6 | Todo error de negocio responde HTTP 500 (ver sección 8); un cliente no puede distinguir 400/404/409 por el código HTTP, solo por `codigoError` | `RestApiExcepcion` | [BASE] el patrón; [EXTENSIÓN] lo hereda al reutilizar los mismos lanzadores |
| H7 | `ExcepcionesFormateador`/handler genérico devuelve `ex.getMessage()` crudo en 500 para cualquier `Exception` no mapeada (puede filtrar texto interno) | `RestApiExcepcion.manejarExcepcionGenerica` | [BASE] |
| H8 | `spring.jpa.show-sql=true` en el único `application.properties`; imprime SQL en logs en cualquier entorno | `backend/solicitudes/src/main/resources/application.properties` | [BASE] |
| H9 | Credenciales de base de datos por defecto (`root` / contraseña `<REDACTADO>`) en `docker-compose.yml`, y MySQL publicado en el puerto 3307 del host | `backend/docker-compose.yml` | [EXTENSIÓN] (el compose lleva el encabezado de la extensión) |
| H10 | CORS con un único origen fijo `http://localhost:4200`; no parametrizable por entorno para producción | `application.properties` `url.frontend` | [BASE] |
| H11 | Sin límite de intentos de inicio de sesión ni bloqueo (no se encontró) | `SesionCUImplAdaptador.login` | [BASE] |
| H12 | Subida de archivos: tamaño máximo 20 MB en multipart (`application.properties`) y regla propia de 5 MB (`MensajesError.ARCHIVO_MUY_GRANDE`); validación de formato y de contenido por firma (`CONTENIDO_ANEXO_NO_COINCIDE`) en `dominio/servicios/ValidadorArchivoAdjunto`; nombre de archivo generado por el sistema (`AlmacenadorArchivosTest` prueba nombres maliciosos) | `ValidadorArchivoAdjuntoTest`, `AlmacenadorArchivosTest` | [EXTENSIÓN] (control positivo) |
| H13 | Un cliente del flujo académico valida pertenencia: `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `ACTOR_SIN_ROL` en `ValidadorActorSolicitud` / `TramiteSolicitud` | `MensajesError`, pruebas `ValidadorActorSolicitudTest`, `TramiteSolicitudTest` | [EXTENSIÓN] (control positivo) |

## 8. Manejo global de excepciones

### 8.1 Estructura

- `@ControllerAdvice` único: `<J>/infraestructura/output/manejadorExcepciones/RestApiExcepcion.java` [BASE].
- Cuerpo de error: `estructura/Error.java` con cinco campos: `codigoError` (String), `mensaje`, `httpCodigo` (Integer), `url`, `metodo`. Se arma con `UtilidadesError.crearError(...)`.
- Los casos de uso no lanzan excepciones directamente: usan el puerto `ExcepcionesFormateadorIntPuerto` (implementado en `infraestructura/output/formateador/ExcepcionesFormateadorImplAdaptador.java`), con ocho lanzadores: `lanzarErrorGenerico`, `lanzarEntidadNoExiste`, `lanzarEntidadExiste`, `lanzarReglaNegocioViolada`, `lanzarCredencialesErroneas`, `lanzarMalFormato`, `lanzarSinInformacion`, `lanzarSinAcceso`. Cada uno lanza una clase de `excepcionesPropias/` (9 clases: las 8 de arriba más `ErrorTokenExpiradoExcepcion`).
- La extensión no agregó códigos ni clases de excepción; solo agregó mensajes (sección 9.2) y reutilizó sobre todo `lanzarReglaNegocioViolada`, `lanzarMalFormato`, `lanzarEntidadNoExiste`, `lanzarEntidadExiste`.

### 8.2 Tabla de códigos (`CodigoError`) [BASE]

Fuente: `estructura/CodigoError.java`. El `codigoError` es una cadena, no el HTTP.

| `codigoError` | Constante | Mensaje fijo | Excepción que lo produce | HTTP real |
|---|---|---|---|---|
| "1" | `ERROR_GENERICO` | Error generico | `ErrorGenericoExcepcion` y cualquier `Exception` sin handler (`manejarExcepcionGenerica`) | 500 |
| "2" | `ERROR_ENTIDAD_EXISTE` | Error, entidad ya existe | `ErrorEntidadExisteExcepcion` | 500 |
| "3" | `ERROR_ENTIDAD_NO_EXISTE` | Error, entidad no existe | `ErrorEntidadNoExisteExcepcion` | 500 |
| "4" | `ERROR_REGLA_DE_NEGOCIO_VIOLADA` | Error, regla de negocio violada | `ErrorReglaNegocioVioladaExcepcion` | 500 |
| "5" | `ERROR_CREDENCIALES` | Error, credenciales incorrectas | `BadCredentialsException` (y `ErrorCredencialesIncorrectasExcepcion`, que extiende `BadCredentialsException`) | 500 |
| "6" | `ERROR_MAL_FORMATO` | Error, mal formato | `ErrorMalFormatoExcepcion` | 500 |
| "7" | `ERROR_NO_INFORMACION` | Error, no se obtuvo información | `ErrorNoInformacionExcepcion` | 500 |
| "8" | `ERROR_SIN_ACCESO` | Error, sin acceso | `ErrorSinAccesoExcepcion` (500); `AccessDeniedException` y `AuthenticationException` en el advice (403) | 500 o 403 |
| "9" | `ERROR_TOKEN_EXPIRADO` | Error, token expirado | `ErrorTokenExpiradoExcepcion` (500, handler `manejarTokenExpirado`); el filtro JWT responde 401 sin pasar por el advice | 500 o 401 |

Total: 9 códigos. Para los códigos 2, 3, 4, 6, 7, 8 (cuando es `ErrorSinAccesoExcepcion`) y 9, el campo `mensaje` del cuerpo es el texto específico (`ex.getMessage()`, típicamente una constante de `MensajesError`). Para el 5 y el 8 de Spring Security el mensaje es el fijo de `CodigoError`.

### 8.3 Respuestas que no usan `RestApiExcepcion`

| Situación | Dónde | Código HTTP | Cuerpo |
|---|---|---|---|
| Sin autenticar en ruta protegida | `CustomAuthenticationEntryPoint` | 401 | `Error` con `codigoError` "401" y mensaje de `ERROR_SIN_ACCESO` ("Error, sin acceso") |
| Autenticado sin permiso (reglas por URL) | `CustomAccessDeniedHandler` | 403 | `Error` con `codigoError` "403" y mensaje "No tiene permisos para acceder a este recurso" |
| Token expirado | `JwtFiltroAutenticacion.sendErrorResponse` | 401 | `Error` con `codigoError` "401" y mensaje de `ERROR_TOKEN_EXPIRADO` |
| Validación `@Valid` (`MethodArgumentNotValidException`) | `RestApiExcepcion.handleValidationExceptions` | 400 | mapa `{campo: mensaje}`, no es un `Error` |
| `ConstraintViolationException` de Hibernate | `RestApiExcepcion.handleConstraintViolationException` | 400 | texto plano con el mensaje |
| Errores de base de datos en controladores académicos | método `errorBaseDeDatos(DataAccessException)` de los controladores (p. ej. `SolicitudAcademicaRestController.adjuntarAnexo`) | NO VERIFICADO el código HTTP exacto (no se abrió el método) | NO VERIFICADO |

Nota de coherencia: el mismo rechazo de seguridad puede salir con dos formas, `codigoError` "401"/"403" (filtros de Spring Security) o "8" con 403 (si la excepción se lanza dentro de un controlador y la atrapa `RestApiExcepcion`). El frontend solo reacciona al HTTP 401 (`SesionVencidaService.atender`: cierra sesión, avisa "Tu sesión venció" y redirige a `/login`, excepto en `POST /sesiones`).

### 8.4 Evidencia en el repositorio de la doble forma 500

`RestApiExcepcion` devuelve `HttpStatus.INTERNAL_SERVER_ERROR` en todos los handlers de excepciones propias (`manejarEntidadNoExiste`, `manejarEntidadExiste`, `manejarReglaNegocioViolada`, `manejarCredencialesIncorrectas`, `manejarMalFormato`, `manejarSinInformacion`, `manejarSinAcceso`, `manejarTokenExpirado`, `manejarExcepcionGenerica`). Solo `manejarNoAutenticacion` y `manejarNoAAcceso` devuelven 403, y los de validación 400. La documentación del contrato (`docs/api/contrato-api-procesos-academicos.md`) y los scripts `backend/pruebas/*.ps1` usan `Obtener-CodigoError` (`comun.ps1`) para distinguir los errores por `codigoError`, no por HTTP. Los textos del backend/CLAUDE.md que hablan de "400 o 409" corresponden a la intención, pero según este handler el HTTP real de una regla violada es 500 (a confirmar con una ejecución: NO VERIFICADO).

## 9. Catálogo de mensajes (`MensajesError`)

Fuente: `<J>/infraestructura/output/manejadorExcepciones/MensajesError.java`. **115 constantes**: 22 [BASE] (hasta `FORMATO_RESPUESTA_NO_VALIDO`) y 93 [EXTENSIÓN] (conteo con `git show d3a48cc:... | grep -c "public static final"` = 22, y 115 hoy). Los mensajes llevan marcadores `%s`/`%d` y terminan en `...`. Siempre viajan con alguno de los 9 códigos de la sección 8.2: la categoría la decide el lanzador que usa el caso de uso, no la constante (NO VERIFICADO el lanzador de cada una de las 115; se agrupan por significado).

### 9.1 [BASE] (22)

| Grupo | Constantes | Significado |
|---|---|---|
| Paginación y vacíos | `PAGINACION_ERROR`, `SIN_REGISTROS`, `ARCHIVO_EXCEL_VACIO` | página o tamaño negativos; lista vacía; parte del multipart faltante |
| Existencia | `ENTIDAD_NO_ENCONTRADA`, `ATRIBUTO_UNICO_YA_EXISTE`, `ENTIDAD_NO_ENCONTRADA_FILTRO`, `INSTANCIA_NO_VALIDA` | no existe por id o por filtro; valor único repetido; subclase no permitida |
| Usuarios y roles | `TIPO_DE_USUARIO_NO_VALIDO`, `ROLES_DUPLICADOS_USUARIO`, `ROLES_NO_VALIDOS`, `PERFIL_SOLICITANTE_NO_VALIDO`, `TIPO_DOCUMENTO_ERRONEO`, `MAL_ASIGNACION` | validaciones al crear usuarios y asignar tipos de solicitud |
| Sesión | `CONTRASEÑA_INCORRECTA`, `CREDENCIALES_ERRONEAS`, `USERNAME_TOKEN`, `NO_ACCESO`, `ROL_NO_HABILITADO` | login y extracción del username del token |
| Solicitudes y respuestas | `MAL_FORMATO_ANEXO`, `SECCION_NO_EXISTENTE`, `TIPO_RESPUESTA_NO_VALIDO`, `FORMATO_RESPUESTA_NO_VALIDO`, `VALOR_REPETIDO_PETICION` | anexos, secciones del orden del día y respuestas |

### 9.2 [EXTENSIÓN] (93)

| Grupo | Constantes |
|---|---|
| Matrícula de asignaturas y estados | `ASIGNATURA_YA_MATRICULADA`, `ESTADO_ASIGNATURA_NO_VALIDO`, `CAMBIO_ESTADO_NO_PERMITIDO` |
| Carga masiva Excel (14) | `EXCEL_NO_VALIDO`, `EXCEL_SIN_FILAS`, `EXCEL_SIN_DATOS`, `EXCEL_ENCABEZADO_INVALIDO`, `EXCEL_FILA_ERROR`, `EXCEL_FILA_DATO_DISTINTO`, `EXCEL_FILA_MATERIA_REPETIDA`, `EXCEL_FILA_VALOR_REPETIDO`, `EXCEL_FILA_VALOR_DUPLICADO`, `EXCEL_FILA_PROGRAMA_DESCONOCIDO`, `EXCEL_FILA_ASIGNATURA_REPETIDA`, `EXCEL_FILA_ASIGNATURA_EXISTE`, `EXCEL_FILA_ASIGNATURA_NO_EXISTE`, `EXCEL_FILA_ASIGNATURA_OTRO_PROGRAMA` (indican fila y columna) |
| Máquina de etapas | `ROL_ETIQUETA_NO_VALIDO`, `DATOS_TRANSICION_INCOMPLETOS`, `ETAPA_NO_VALIDA`, `ETAPA_FINAL_SIN_ACCIONES`, `TRANSICION_NO_PERMITIDA`, `ROL_TRANSICION_NO_PERMITIDO`, `REQUISITO_TRANSICION_FALTANTE`, `TIPO_SOLICITUD_SIN_PROCESO` |
| Actor y pertenencia | `ACTOR_SIN_ROL`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `SOLICITUD_EN_CURSO`, `RADICADO_NO_GENERADO` |
| Archivos y anexos | `OBSERVACION_MUY_LARGA`, `ARCHIVO_VACIO`, `ARCHIVO_MUY_GRANDE` (5 MB), `FORMATO_ANEXO_NO_PERMITIDO`, `CONTENIDO_ANEXO_NO_COINCIDE`, `ANEXO_DE_OTRO_TIPO`, `SOPORTE_LIBRE_NO_PERMITIDO`, `ANEXO_ACTOR_NO_PERMITIDO`, `ANEXO_ETAPA_NO_PERMITIDA`, `ERROR_GUARDANDO_ARCHIVO`, `ANEXO_NO_PERMITIDO_AL_RADICAR`, `ANEXO_NO_CORRESPONDE_CAUSA`, `ANEXO_REPETIDO`, `ANEXOS_OBLIGATORIOS_FALTANTES`, `SOLO_SOPORTES_LIBRES` |
| Resolución escaneada | `RESOLUCION_SOLO_CANCELACIONES`, `RESOLUCION_SOLO_FUNCIONARIO`, `RESOLUCION_ETAPA_FINAL`, `RESOLUCION_ETAPA_NO_PERMITIDA`, `RESOLUCION_NO_DISPONIBLE` |
| Cancelación de matrícula y de asignatura | `MOTIVO_CANCELACION_REQUERIDO`, `MOTIVO_CANCELACION_MUY_LARGO`, `SIN_ASIGNATURAS_ACTIVAS`, `ASIGNATURAS_SIN_EVALUAR`, `ASIGNATURA_REPETIDA`, `ASIGNATURA_AJENA`, `DATO_ASIGNATURA_FALTANTE`, `NUMERO_FALTAS_NO_VALIDO`, `NOTA_NO_VALIDA`, `SIN_ASIGNATURAS_ELEGIDAS`, `ASIGNATURA_NO_MATRICULADA`, `ASIGNATURA_NO_ACTIVA`, `NOTA_INSUFICIENTE_CUMPLE` (nota menor a 3.0), `NINGUNA_ASIGNATURA_CUMPLE`, `NINGUNA_ASIGNATURA_APROBADA`, `FORMA_APROBACION_DECANO`, `AJUSTE_ASIGNATURA_FALTANTE`, `SITUACION_CANCELAR_RECHAZADA`, `AJUSTES_SOLICITUD_RECHAZADA`, `OBSERVACION_RESPUESTA_REQUERIDA`, `VALOR_ASIGNATURA_MUY_LARGO`, `OBSERVACION_ASIGNATURA_MUY_LARGA` |
| Examen supletorio | `CAUSA_SUPLETORIO_REQUERIDA`, `CAUSA_SUPLETORIO_INVALIDA`, `ASIGNATURA_SUPLETORIO_REQUERIDA`, `ASIGNATURA_SUPLETORIO_NO_ACTIVA`, `FECHA_EXAMEN_REQUERIDA`, `FECHA_EXAMEN_FUTURA`, `PLAZO_SUPLETORIO_VENCIDO` (3 días hábiles), `ASIGNATURA_CRUZADA_REQUERIDA`, `ASIGNATURA_CRUZADA_IGUAL`, `DATOS_CRUCE_REQUERIDOS`, `HORA_CRUCE_MUY_LARGA`, `HORA_CRUCE_MAL_FORMADA`, `DATOS_CRUCE_NO_PERMITIDOS`, `FECHA_ACORDADA_ANTERIOR`, `FECHA_MAL_FORMADA` |
| Programas y catálogo de asignaturas | `PROGRAMA_ACADEMICO_REQUERIDO`, `NOMBRE_ASIGNATURA_REQUERIDO`, `NOMBRE_ASIGNATURA_MUY_LARGO`, `ASIGNATURA_NOMBRE_EXISTE`, `ASIGNATURA_OTRO_PROGRAMA` |

La agrupación es semántica, hecha por lectura; solo los totales (22 y 115) se contaron por script.

## 10. Registro de acciones (logs) e historial

### 10.1 Log de auditoría general [BASE mecanismo, EXTENSIÓN usos]

| Aspecto | Detalle |
|---|---|
| Tabla / entidad | `logs`, `LogEntidad` (`<J>/infraestructura/output/persistencia/entidades/LogEntidad.java`) |
| Campos | `uuidLog` (PK, 150), `accion` (1000), `fecha` (String 1000, formato `yyyy-MM-dd HH:mm:ss`), `resultado` (1000), `uuidUsuario` (FK a `usuarios`, `@ManyToOne`, no nulo), `fechaCreacion` (`LocalDateTime`, no actualizable, se llena en `@PrePersist`) |
| Modelo de dominio | `dominio/modelos/Log.java` |
| Caso de uso | `dominio/casosdeuso/LogCUImplAdaptador.java`: `crearLog(accion, resultado, token)`, `crearLogSesion(accion, resultado, username)`, `getLogs()`, `getLogs(pagina, tamanio)`, `getLogs(responsable, fecha, pagina, tamanio)` |
| Cómo se llena | Cada caso de uso de escritura llama `log.crearLog(...)` con el token; el responsable se resuelve con `IJwtServicio.getUsername(token)` y `LogGatewayIntPuerto.getUsuarioUsername` |
| Cuándo se escribe | Después de la operación exitosa (el log no distingue fallos; no se encontró registro de intentos fallidos) |
| Endpoints | `GET /logs`, `GET /logs/paginado?pagina&tamanio`, `GET /logs/filtro?responsable&fecha&pagina&tamanio` (`LogRestController`); acceso SG y DEC por regla URL y por `@PreAuthorize` |
| Paginación | `PageRequest.of(pagina, tamanio, Sort.by("fechaCreacion").descending().and(Sort.by("uuidLog").ascending()))` en `LogGatewayImplAdaptador`; respuesta `PaginacionRespuestaDTO` con `content` y `totalElements`. Valores negativos lanzan `PAGINACION_ERROR`. La lista completa (`GET /logs`) ordena por `fechaCreacion` descendente |
| Filtro | `LogRepositorio.findByResponsableAndFecha`: coincidencia parcial sin distinguir mayúsculas sobre `nombres + ' ' + apellidos` del responsable y sobre el texto de `fecha`. La consulta incluye `ORDER BY l.fecha DESC` además del `Sort` del `Pageable` (doble ordenamiento; el efecto combinado queda NO VERIFICADO) |
| Lista vacía | `getLogs()` y `getLogs(pagina, tamanio)` lanzan `SIN_REGISTROS` ("No existen registrados logs en el sistema..."); el filtro no lo hace |

Acciones registradas (42 llamadas en `dominio/casosdeuso`; 18 [BASE], 24 [EXTENSIÓN], según `git grep` sobre `d3a48cc`):

- [BASE] (18): "Inicio de sesión"; "Crear Usuario" (2 puntos), "Actualizar Usuario" (2: datos y contraseña); "Solicitud Creada", "Solicitud Publica Creada", "Solicitud modificada"; "Registrar respuesta de Solicitud", "Modificar respuesta de Solicitud", "Responder Solicitud", "Eliminar archivo de respuesta"; "Crear Orden del Día", "Actualizar Orden del Día"; "Crear Tipo de Solicitud" (2 puntos), "Actualizar Tipo Solicitud"; "Actualizar rol".
- [EXTENSIÓN] (24): "Crear asignatura", "Actualizar asignatura"; "Crear estudiante", "Actualizar estudiante", "Agregar asignatura matriculada", "Cambiar estado asignatura matriculada"; "Crear funcionario académico", "Actualizar funcionario académico"; "Asignar funcionario académico a tipo de solicitud"; "Radicar solicitud académica", "Cambiar etapa de solicitud académica"; "Radicar cancelación de matrícula", "Radicar cancelación de asignatura", "Radicar examen supletorio"; "Adjuntar anexo académico", "Adjuntar Resolución académica"; "Evaluar asignaturas de cancelación de matrícula", "Registrar situación al cancelar", "Cancelar asignaturas matriculadas" (también en cancelación de asignatura), "Evaluar asignaturas de cancelación de asignatura", "Registrar decisión del Decano por asignatura", "Ajustar asignaturas de cancelación de asignatura"; "Acordar fecha de examen supletorio".

No se registra en `logs`: lecturas (GET), descargas de anexos y de Resolución, intentos de login fallidos, ni rechazos por regla de negocio.

### 10.2 Historial por solicitud académica [EXTENSIÓN]

| Aspecto | Detalle |
|---|---|
| Modelo | `dominio/modelos/HistorialSolicitudAcademica.java`: `uuidHistorial`, `solicitudAcademica`, `usuario`, `accion`, `observaciones`, `fecha` (`LocalDateTime`), `etapaCodigo` |
| Quién escribe | `SolicitudAcademicaCUImplAdaptador.historial(...)` crea la fila en cada radicación y cada cambio de etapa, con `accion = AccionEtapa.name()` y la observación recortada (máximo `MAXIMO_OBSERVACION`; si lo supera, `OBSERVACION_MUY_LARGA`) |
| Consulta | `GET /solicitudes-academicas/{uuidSolicitud}/historial` (`SolicitudAcademicaRestController.getHistorial`), acceso EST, FA, DEC; no está paginado (devuelve la lista completa) |
| DTO | `HistorialSolicitudAcademicaDTORespuesta`: `accion`, `etapaCodigo`, `observaciones`, `fecha`, `nombresUsuario`, `apellidosUsuario` |
| Filtrado por rol | `ConsultaSolicitudAcademicaCUImplAdaptador.getHistorial`: si el actor es Estudiante, se ocultan las observaciones de ciertas acciones (`OCULTAS_ESTUDIANTE`), se eliminan filas internas (`FILAS_OCULTAS_ESTUDIANTE`) y las etapas de decisión del Decano se muestran como `EN_REVISION_DECANO` (`etapaVisible`) |

Diferencia: el log general registra "quién hizo qué" a nivel de sistema; el historial registra la trayectoria de cada solicitud con etapa y observación y es la fuente que ve el Estudiante.

## 11. Cifras clave

- Códigos de error (`CodigoError`): 9. Clases de excepción propias: 9. Handlers en `RestApiExcepcion`: 13 (11 para excepciones de negocio/seguridad/genérica, 1 para `MethodArgumentNotValidException`, 1 para `ConstraintViolationException`).
- `MensajesError`: 115 constantes (22 [BASE], 93 [EXTENSIÓN]).
- `requestMatchers`: 78 hoy (17 [BASE], 61 [EXTENSIÓN]); ocho prefijos académicos cerrados con `denyAll`.
- `@PreAuthorize`: 138 usos, sin habilitación global de seguridad por método (H1).
- JWT: HS256, 24 h, sin claims propios, clave fija en código.
- Llamadas a `crearLog`/`crearLogSesion`: 42 (18 base, 24 extensión).
- Hallazgos de seguridad listados: 13 (H1 a H11 son debilidades o riesgos, H12 y H13 son controles positivos).
