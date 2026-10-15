# 14 - Hallazgos y deuda técnica

Fuentes: los documentos 02, 03, 04, 05, 06, 07, 11 y verificaciones propias con grep. Rutas relativas a `backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes` (B) salvo indicación.

## Seguridad

| # | Hallazgo | Evidencia | Origen |
|---|---|---|---|
| S1 | No existe `@EnableMethodSecurity`: los `@PreAuthorize` no se evalúan; la protección real son los `requestMatchers` por URL | grep de `@EnableMethodSecurity` en main: 0 coincidencias; 124 líneas con `@PreAuthorize` en los controladores (el subagente de seguridad reportó 138; conciliado en `VERIFICACION-SEGURIDAD.md`: son 124) | [BASE] y [EXTENSIÓN] |
| S2 | 22 endpoints [BASE] tienen una regla efectiva distinta de su `@PreAuthorize` (por ejemplo `GET tipos/solicitudes/paginado` y `/filtro` públicos; `PATCH respuestas/{uuidRespuesta}/archivo` cae en `anyRequest().authenticated()`) | 03, anexo A; calculado con script, NO VERIFICADO en ejecución | [BASE] |
| S3 | Clave de firma del JWT escrita en el código | `B/infraestructura/configuracion/seguridad/jwt/JwtServicio.java:23` (valor `<REDACTADO>`) | [BASE] |
| S4 | Sin refresh ni revocación de token; token en `localStorage`; vigencia 24 h (la monografía dice 5 h) | `JwtServicio.java:36`; `frontend/src/app/core/auth/guards/auth-guard.ts` | [BASE] |
| S5 | El filtro solo captura `ExpiredJwtException`; el resultado con firma inválida es NO VERIFICADO | `JwtFiltroAutenticacion.java` | [BASE] |
| S6 | El handler genérico devuelve `ex.getMessage()` crudo | `RestApiExcepcion` (06) | [BASE] |
| S7 | `spring.jpa.show-sql=true` siempre activo | `backend/solicitudes/src/main/resources/application.properties:19` | [BASE] |
| S8 | `docker-compose.yml` trae contraseña de MySQL por defecto y publica el puerto 3307 | `backend/docker-compose.yml:33,35` | [BASE] |
| S9 | CORS con un único origen fijo `http://localhost:4200` | 06 | [BASE] |
| S10 | Sin límite de intentos de login; el estado habilitado del usuario solo se revisa al iniciar sesión | 06 | [BASE] |

## Arquitectura y consistencia

| # | Hallazgo | Evidencia |
|---|---|---|
| A1 | El dominio importa Spring en 4 archivos [BASE] (`RespuestaCUImplAdaptador`, `SolicitudCUImplAdaptador`, `Anexo`, `RespuestaCUIntPuerto`) por `@Value` y `MultipartFile` | 02 |
| A2 | El dominio importa infraestructura en 33 archivos (36 imports): `MensajesError` (33), `CodigoError` (1), `AlmacenadorArchivos` (2). La extensión replica el patrón (25 de los 33 archivos) | 02 |
| A3 | Todos los errores de negocio responden HTTP 500; solo cambia `codigoError`. El código 9 de `CodigoError` nunca se emite | 03, 06 |
| A4 | Los casos de uso no llevan `@Transactional`; la transacción la abren los controladores (95 usos en el main) | 04 |
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
| C2 | `README.md` raíz dice que el frontend no consume endpoints de los procesos académicos; el código muestra lo contrario | 11 |
| C3 | `etapas-por-proceso.md`: §3.2 "Requisitos verificados" en Remitir al Decano contradice P49; P14 y P46 se contradicen sobre la observación de la evaluación | 04 |
| C4 | La documentación habla de cuatro condiciones académicas; el código tiene un solo booleano `cumpleCondiciones` por asignatura | 04 |
| C5 | `backend/CLAUDE.md` dice 1233 pruebas; el conteo estático da 696 métodos (615 `@Test` + 81 `@ParameterizedTest`); NO VERIFICADO el número ejecutado | 11 |
| C6 | `CLAUDE.md` raíz y el prompt discrepan en tablas nuevas (19 contra 18): son 19; el 18 es histórico (`change.md:787`, `docs/informe-discrepancias-documentacion.md`) | 05 |
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
| P1 | Solo 1 de los 38 archivos de test del backend es de Julián y necesita base de datos para arrancar Spring | 11 |
| P2 | 14 scripts de humo en `backend/pruebas`, sin colección Postman; requieren backend y Docker corriendo | 11 |
| P3 | Sin pruebas e2e en el frontend | 11 |
| P4 | `mem_limit` solo en el servicio backend; MySQL sin límite | `backend/docker-compose.yml:15` |
| P5 | Rama única `main`; la monografía describe Prod/Dev | `git branch -a` |

## Qué mejoraría un revisor de tesis

1. Habilitar `@EnableMethodSecurity` o eliminar los `@PreAuthorize` para no aparentar una protección que no existe, y alinear las 22 reglas divergentes.
2. Mover la clave JWT y la contraseña de MySQL a variables de entorno y corregir la vigencia documentada (24 h).
3. Mapear los errores de negocio a códigos HTTP 4xx en lugar de 500.
4. Pasar `@Transactional` a la capa de casos de uso o documentar la decisión.
5. Añadir CHECK y UNIQUE a las reglas hoy solo en código y DDL para las tablas de Julián.
6. Unificar la documentación contradictoria (`etapas-por-proceso.md`, `README.md`, `backend/CLAUDE.md`).
7. Medir y reportar la cobertura real de pruebas en vez de conteos estáticos.
