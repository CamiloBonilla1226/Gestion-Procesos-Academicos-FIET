# 14 - Hallazgos y deuda técnica

Fuentes: los documentos 02, 03, 04, 05, 06, 07, 11 y verificaciones propias con grep. Actualizado el 2026-10-21 (T12.37): se marca lo que quedó resuelto o cambió de T12.21 a T12.35 y se agregan los hallazgos del correo electrónico (sección nueva al final). Rutas relativas a `backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes` (B) salvo indicación.

## Seguridad

| # | Hallazgo | Evidencia | Origen |
|---|---|---|---|
| S1 | No existe `@EnableMethodSecurity`: los `@PreAuthorize` no se evalúan; la protección real son los `requestMatchers` por URL | grep de `@EnableMethodSecurity` en main: 0 coincidencias; 124 líneas con `@PreAuthorize` en los controladores (el subagente de seguridad reportó 138; conciliado en `VERIFICACION-SEGURIDAD.md`: son 124) | [BASE] y [EXTENSIÓN] |
| S2 | Al corte, 22 endpoints [BASE] tenían una regla efectiva distinta de su `@PreAuthorize`, corregidos en T12.21 con reglas por URL y control de dueño (`VERIFICACION-SEGURIDAD.md`) (por ejemplo `GET tipos/solicitudes/paginado` y `/filtro` públicos; `PATCH respuestas/{uuidRespuesta}/archivo` cae en `anyRequest().authenticated()`) | 03, anexo A; calculado con script, NO VERIFICADO en ejecución | [BASE] |
| S3 | Clave de firma del JWT escrita en el código. Mitigado en T12.21: sale de `JWT_SECRET`. Resuelto en T12.40: sin valor por defecto en ningún archivo versionado, obligatoria desde `backend/.env` y con aviso claro al arrancar; la clave anterior sigue en el historial de git | `JwtServicio` (`@Value("${jwt.secreto}")`) | [BASE], mitigado por [EXTENSIÓN] |
| S4 | Sin refresh ni revocación de token; token en `localStorage`; vigencia 24 h (la monografía dice 5 h) | `JwtServicio.java:36`; `frontend/src/app/core/auth/guards/auth-guard.ts` | [BASE] |
| S5 | El filtro solo captura `ExpiredJwtException`; el resultado con firma inválida es NO VERIFICADO | `JwtFiltroAutenticacion.java` | [BASE] |
| S6 | El handler genérico devuelve `ex.getMessage()` crudo | `RestApiExcepcion` (06) | [BASE] |
| S7 | `spring.jpa.show-sql=true` siempre activo | `backend/solicitudes/src/main/resources/application.properties:19` | [BASE] |
| S8 | `docker-compose.yml` trae contraseña de MySQL por defecto y publica el puerto 3307 | `backend/docker-compose.yml:33,35` | [BASE] |
| S9 | CORS con un único origen fijo `http://localhost:4200` | 06 | [BASE] |
| S10 | Sin límite de intentos de login (sigue pendiente). El estado habilitado del usuario, que al corte solo se revisaba al iniciar sesión, desde T12.21 se revisa en cada petición en `JwtFiltroAutenticacion` | 06 | [BASE] |

## Arquitectura y consistencia

| # | Hallazgo | Evidencia |
|---|---|---|
| A1 | El dominio importa Spring en 4 archivos [BASE] (`RespuestaCUImplAdaptador`, `SolicitudCUImplAdaptador`, `Anexo`, `RespuestaCUIntPuerto`) por `@Value` y `MultipartFile` | 02 |
| A2 | El dominio importa infraestructura en 37 archivos (40 imports) al 2026-10-21: `MensajesError` (37), `CodigoError` (1), `AlmacenadorArchivos` (2). La extensión replica el patrón (29 de los 37 archivos). Al corte eran 33 y 36 | 02 |
| A3 | Todos los errores de negocio responden HTTP 500; solo cambia `codigoError`. El código 9 de `CodigoError` nunca se emite | 03, 06 |
| A4 | Los casos de uso no llevan `@Transactional`; la transacción la abren los controladores (95 usos en el main al corte, 127 líneas con la anotación al 2026-10-21). La tarea programada de la comunicación de la Resolución no pasa por controlador y se apoya en `UPDATE` condicionales | 04 |
| A5 | Siete mapeadores de entrada `Mapper*InfraestructuraDominio` y otras dos clases siguen usando ModelMapper | grep `ModelMapper` en main: 9 archivos |
| A6 | Los casos de uso base escriben el log antes de guardar; los de la extensión después | `UsuarioCU.crearUsuario`, `SolicitudCU.crearSolicitud` (04) |
| A7 | `EstudianteEntidad` usa `@OneToMany` con `cascade = ALL` y `fetch = EAGER` | `B/infraestructura/output/persistencia/entidades/EstudianteEntidad.java:45` |

## Base de datos

| # | Hallazgo | Evidencia |
|---|---|---|
| D1 | Las tablas de Julián no tienen DDL ni diccionario; longitudes y tipos exactos NO VERIFICADO | 05 |
| D2 | PK de Julián `varchar(150)` y de la extensión `varchar(100)`; FK de la extensión hacia `usuarios` con `columnDefinition varchar(100)` | 05 |
| D3 | Sin `ON DELETE` en ninguna FK; el borrado en cascada depende de JPA | 05 |
| D4 | Reglas sin CHECK ni UNIQUE: estados de `ASIGNATURA_MATRICULADA`, cruce solo con `tipoCausa='cruce'`, nombre de asignatura único por programa, `facultad='FIET'` | 05 |
| D5 | `Usuario_has_Roles` no tiene clave primaria ni entidad | 05 |
| D6 | Los seeds se contradicen: `migracion_cargo_situaciones_notas.sql` y los `UPDATE` finales de `seed-procesos-academicos.sql` dejan etiquetas distintas para FUNCIONARIO; el resultado depende del orden de ejecución | 05 |
| D7 | `MYSQL_DATABASE: cfiet_database` en compose contra esquema `cfiet` en la URL | 05 |
| D8 | `ddl-auto=update` en la única configuración | `application.properties:18` |

## Documentación y código muerto

| # | Hallazgo | Evidencia |
|---|---|---|
| C1 | Fragmento `%sytg` en el mensaje de error del anexo: error tipográfico | `B/dominio/casosdeuso/SolicitudCUImplAdaptador.java:359` |
| C2 | `README.md` raíz decía que el frontend no consume endpoints de los procesos académicos; el código muestra lo contrario. Corregido en T12.40 | 11 |
| C3 | `etapas-por-proceso.md`: §3.2 "Requisitos verificados" en Remitir al Decano contradice P49; P14 y P46 se contradicen sobre la observación de la evaluación. Al 2026-10-21 tampoco reflejan las decisiones posteriores la tabla 3.1 (rechazo con Resolución, datos obligatorios, situación del Decano en Matrícula), P9, P17, P44, la sección 4.3 (fecha acordada), la 5.1 (seis anexos) y la nota de 3.2 sobre DARCA (04, D10 a D13) Corregido en T12.40 (tablas 3.1 y 3.2, secciones 4.3, 5.1 y 5.4 y decisiones P9, P21, P44 y P58). | 04 |
| C4 | La documentación habla de cuatro condiciones académicas; el código tiene un solo booleano `cumpleCondiciones` por asignatura | 04 |
| C5 | `backend/CLAUDE.md` dice 1604 pruebas (ejecución completa de T12.40); el conteo estático da 909 métodos. Antes 1580 contra 889 (T12.32) y, al corte, 1233 contra 696 | 11 |
| C6 | `CLAUDE.md` raíz y el prompt discrepaban en tablas nuevas (19 contra 18): eran 19; el 18 es histórico. Hoy son 21 (`NOTIFICACION_DOCENTE` y `NOTIFICACION_RESOLUCION`) | 05 |
| C7 | `RespuestasService.descargarRespuesta` arma una URL con el host duplicado y nadie la llama | 07 |
| C8 | `DescargarRespuestaComponent` lee la clave `token` en vez de `authToken` (el interceptor lo compensa) | 07 |
| C9 | PrimeFlex está en `package.json` y nada lo carga; el `Avatar` de PrimeNG no se importa | 07 |
| C10 | Presupuesto del bundle: `CLAUDE.md` dice 840 kB, `angular.json` fuerza 500 kB (aviso) y 1 MB (error) | 07 |
| C11 | Sin TODO ni FIXME en el código fuente (grep: las únicas coincidencias son la constante `TODOS`) | grep propio |

## Frontend

| # | Hallazgo | Evidencia |
|---|---|---|
| F1 | Sin ruta comodín ni verificación local de expiración del JWT; `RoleGuard` consulta el usuario al backend en cada navegación | 07 |
| F2 | Un solo `environment.ts`, sin versión de producción | 07 |
| F3 | La pantalla pública `usuario-publico/solicitudes` solo lista tipos; el formulario público vive en la pantalla del Funcionario | 08 |
| F4 | El Decano accede por URL a pantallas de Usuario FIET | 08 |
| F5 | 3 `console.log` en código no de prueba | grep propio |

## Pruebas y despliegue

| # | Hallazgo | Evidencia |
|---|---|---|
| P1 | Solo 1 de los 51 archivos de test del backend es de Julián y necesita base de datos para arrancar Spring | 11 |
| P2 | 15 scripts de humo en `backend/pruebas` (con `t9_correo_respuesta.ps1`), sin colección Postman; requieren backend y Docker corriendo. `t5_solicitudes.ps1` tiene 15 fallos conocidos porque sube un anexo de Matrícula retirado en T12.31 (pendiente según `change.md`, T12.34) | 11 |
| P3 | Sin pruebas e2e en el frontend | 11 |
| P4 | `mem_limit` solo en el servicio backend; MySQL sin límite | `backend/docker-compose.yml:15` |
| P5 | Rama única `main`; la monografía describe Prod/Dev | `git branch -a` |

## Qué mejoraría un revisor de tesis

1. Habilitar `@EnableMethodSecurity` o eliminar los `@PreAuthorize` para no aparentar una protección que no existe (las 22 reglas divergentes se alinearon por URL en T12.21).
2. Quitar los valores por defecto de la clave JWT y del destinatario de DARCA de los archivos versionados, mover la contraseña de MySQL a variables de entorno y corregir la vigencia documentada (24 h).
3. Mapear los errores de negocio a códigos HTTP 4xx en lugar de 500.
4. Pasar `@Transactional` a la capa de casos de uso o documentar la decisión.
5. Añadir CHECK y UNIQUE a las reglas hoy solo en código y DDL para las tablas de Julián.
6. Unificar la documentación contradictoria (`etapas-por-proceso.md`, `README.md`, `backend/CLAUDE.md`).
7. Medir y reportar la cobertura real de pruebas en vez de conteos estáticos.
8. Para producción, pasar el correo de la cuenta de Gmail de prueba al SMTP institucional y resolver la constancia que DARCA recibe cuando falla el correo al estudiante (sección siguiente).

## Correo electrónico (agregado el 2026-10-21)

Detalle en [correo-electronico.md](correo-electronico.md), sección de decisiones y limitaciones.

| # | Hallazgo | Evidencia |
|---|---|---|
| M1 | Si falla el correo al estudiante de la comunicación programada, DARCA recibe igual la constancia, que dice que no se pudo enviar; DARCA queda marcado como enviado y el reintento solo repite el del estudiante, así que DARCA no recibe una constancia del envío que sí ocurra después | `NotificacionResolucionCUImplAdaptador.procesar`; `GeneradorPdfRespuesta` (texto "No se pudo enviar; el envío se reintentará") |
| M2 | Los correos inmediatos se envían una sola vez, en segundo plano y después del commit; si fallan solo quedan en el log de la aplicación, sin reintento ni aviso en la aplicación | `CorreoImplAdaptador`, `ServicioCorreo.enviar` |
| M3 | La cuenta remitente es una cuenta de Gmail de prueba con contraseña de aplicación; en producción se necesita el SMTP institucional | `application.properties` (`spring.mail.host`), `backend/CLAUDE.md` |
| M4 | La firma, el sufijo de la Resolución y el destinatario por defecto de DARCA están escritos en `application.properties` y `docker-compose.yml`; cambiar de funcionario o de destinatario exige editar archivos | `application.properties` |
| M5 | La tarea programada corre en cada instancia del backend; los `UPDATE` condicionales evitan duplicados, pero no hay un registro de envíos distinto de la propia fila de `NOTIFICACION_RESOLUCION` | `NotificacionResolucionRepositorio.reclamar` |
