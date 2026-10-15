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
