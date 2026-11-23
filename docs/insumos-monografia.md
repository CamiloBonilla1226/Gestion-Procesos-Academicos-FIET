# Insumos técnicos para la monografía

Documento de apoyo para redactar la monografía del trabajo de grado "Gestión de Procesos
Académicos FIET". Se armó el 2026-11-11 leyendo el código del backend y del frontend, su
configuración (`pom.xml`, `package.json`, `application.properties`, `docker-compose.yml`,
Dockerfiles), la base de datos local (`SHOW TABLES`) y la documentación de `docs/` que ya se
había contrastado con el código. Lo que no se pudo confirmar en el código lleva la marca
**[por confirmar]**.

Convención: **[BASE]** es el prototipo de Julián David Camacho Erazo (primer commit `d3a48cc`);
**[EXTENSIÓN]** es lo que agrega este trabajo de grado.

---

## 1. Tecnologías y versiones

### 1.1 Backend (`backend/solicitudes/pom.xml`)

| Elemento | Versión | Uso |
| --- | --- | --- |
| Java | 17 (`java.version`) | Lenguaje |
| Spring Boot (`spring-boot-starter-parent`) | 3.5.4 | Framework base |
| spring-boot-starter-web | la de Spring Boot | API REST |
| spring-boot-starter-data-jpa (Hibernate) | la de Spring Boot | Persistencia ORM |
| spring-boot-starter-security | la de Spring Boot | Autenticación y autorización |
| spring-boot-starter-mail | la de Spring Boot | Envío de correo SMTP [EXTENSIÓN] |
| jjwt-api / jjwt-impl / jjwt-jackson | 0.11.2 | Tokens JWT |
| Apache POI (`poi-ooxml`) | 5.2.5 | Lectura de Excel (cargas masivas) y escritura .docx |
| OpenPDF (`com.github.librepdf:openpdf`) | 2.0.5 | PDF del radicado y constancia de la comunicación [EXTENSIÓN] |
| springdoc-openapi-starter-webmvc-ui | 2.8.10 | Swagger UI |
| ModelMapper | 2.4.5 | Conversión de objetos |
| mysql-connector-j | la de Spring Boot | Driver de MySQL |
| Lombok | 1.18.42 (fijada en el `pom.xml`) | Generación de código |
| spring-boot-starter-test (JUnit 5, Mockito) | la de Spring Boot | Pruebas |

Base de datos: **MySQL 8.0.33** (imagen `mysql:8.0.33` en `backend/docker-compose.yml`).

### 1.2 Frontend (`frontend/package.json`)

| Dependencia | Versión | Uso |
| --- | --- | --- |
| Angular (`@angular/core`, `common`, `forms`, `router`, `platform-server`) | ^20.2.0 | Framework SPA |
| @angular/ssr + express | ^20.2.1 / ^5.1.0 | Renderizado en servidor |
| PrimeNG + @primeuix/themes | ^20.1.1 / ^1.2.3 | Componentes de interfaz |
| PrimeFlex, PrimeIcons | ^4.0.0 / ^7.0.0 | Utilidades CSS e iconos |
| Bootstrap | ^5.3.8 | Estilos |
| SweetAlert2 | ^11.23.0 | Diálogos de confirmación |
| RxJS | ~7.8.0 | Programación reactiva |
| TypeScript | ~5.9.2 | Lenguaje |
| Jasmine + Karma | ~5.9.0 / ~6.4.0 | Pruebas unitarias |

Node: el `frontend/dockerfile` usa `node:20-alpine`. El README pide Node 20.19+ o 22.12+.

### 1.3 Otras herramientas del repositorio

- PowerShell 5.1 para los scripts de humo de `backend/pruebas/`.
- Python 3 para `backend/pruebas/smtp_prueba.py`, un servidor SMTP local que captura los correos en las pruebas.
- Docker y Docker Compose para el despliegue local.

---

## 2. Arquitectura

### 2.1 Backend: arquitectura hexagonal (puertos y adaptadores)

Paquete raíz: `com.unicauca.cfiet.solicitudes`. Es el mismo estilo de Julián, que la extensión
sigue y amplía.

| Capa | Paquete | Contenido |
| --- | --- | --- |
| Aplicación (puertos) | `aplicacion/input` | Interfaces de los casos de uso (`*CUIntPuerto`) |
| | `aplicacion/output` | Interfaces hacia afuera: gateways de persistencia, almacenamiento de archivos, correo (`CorreoIntPuerto`, `PreparacionCorreoIntPuerto`), generador de PDF, codificador de contraseñas |
| Dominio | `dominio/casosdeuso` | 36 implementaciones de casos de uso (`*CUImplAdaptador`) |
| | `dominio/modelos` | Modelos de dominio (POJO) |
| | `dominio/servicios` | Servicios de dominio de la extensión: `MaquinaEtapas`, `TramiteSolicitud`, `TramiteCancelacion`, `ValidadorActorSolicitud`, `ValidadorArchivoAdjunto`, `ReglasAsignatura`, `NotificadorRespuestaFinal`, `ComunicacionResolucion` |
| Infraestructura | `infraestructura/input` | 16 controladores REST (`controlador*`, cada uno con `controlador/`, `DTOPeticion/`, `DTORespuesta/`, `mapeador/`) y la tarea programada `programadorNotificaciones` |
| | `infraestructura/output` | Persistencia JPA (entidades, repositorios, gateways, mapeadores), almacenamiento de archivos, correo (`ServicioCorreo`, `ColaCorreos`), PDF (`GeneradorPdfRespuesta`), manejador de excepciones (`RestApiExcepcion`, `MensajesError`) |
| | `infraestructura/configuracion` | `BeanConfiguracion` (registra los casos de uso como `@Bean`), seguridad (JWT, `ConfiguracionSeguridad`), lectores de Excel (`lectorArchivos/`), validadores de archivos |

Cifras actuales del árbol de trabajo (conteo del 2026-11-11):

| Concepto | Total | [BASE] | [EXTENSIÓN] |
| --- | --- | --- | --- |
| Clases `.java` en `src/main` | 534 | 175 | 359 |
| Endpoints REST | 141 | 62 | 79 |
| Controladores | 16 | 7 | 9 |
| Entidades JPA (`@Entity`) | 35 | 13 | 22 |
| Tablas en la base (`SHOW TABLES`) | 36 | 14 | 22 |
| Casos de uso (`dominio/casosdeuso`) | 36 | 8 | 28 |

El reparto de clases [BASE] y [EXTENSIÓN] sale de `git ls-tree d3a48cc`: 175 archivos `.java`
estaban en el primer commit. El de casos de uso y entidades, de `docs/analisis/00` más los
agregados posteriores (`EliminacionRespuestasCUImplAdaptador`, la entidad de `CORREO_PENDIENTE`)
**[por confirmar uno a uno]**.

Decisiones de diseño que se ven en el código:

- Los casos de uso se registran como `@Bean` en `BeanConfiguracion`, no con `@Service`, igual que en Julián.
- La transición de etapas se centraliza en `dominio/servicios/MaquinaEtapas.java`: define, por proceso, qué acción lleva de qué etapa a cuál y con qué rol.
- El correo y el PDF entran al dominio por puertos (`CorreoIntPuerto`, `GeneradorPdfRespuestaIntPuerto`); el dominio no conoce Spring Mail ni OpenPDF.
- Los mensajes de error de negocio están en `infraestructura/output/manejadorExcepciones/MensajesError.java`. Los usa el dominio, que en eso depende de la infraestructura (hallazgo documentado en `docs/analisis/14-hallazgos-deuda-tecnica.md`).
- Las transacciones las abren los controladores, no los casos de uso (`docs/analisis/00`).

### 2.2 Frontend: estructura

Angular 20 con componentes standalone, rutas en `frontend/src/app/app.routes.ts` y SSR con
Express (`frontend/src/server.ts`).

| Carpeta | Contenido |
| --- | --- |
| `src/app/core/<rol>/` | Páginas y barra lateral de cada rol: `estudiante`, `funcionario-academico`, `decano`, `docente` [EXTENSIÓN]; `secretario-general`, `funcionario`, `secretariaFiet`, `usuario fiet`, `usuario publico` [BASE] |
| `src/app/core/auth/` | Login, guards (`AuthGuard`, `RoleGuard`, `InicioGuard`) e interceptor HTTP |
| `src/app/core/services/` | Servicios HTTP por recurso (`providedIn: 'root'`) |
| `src/app/core/models/` | Interfaces TypeScript de los DTO |
| `src/app/core/constantes/`, `core/utils/` | Constantes de procesos (acciones, roles) y utilidades de formato y validación |
| `src/app/shared/` | Componentes reutilizables: bandeja de solicitudes, detalle de solicitud, componentes de cada proceso (`*-proceso-component`), carga de Excel, diálogos genéricos, encabezado, barra lateral |
| `src/app/layouts/` | Plantillas de página (`app-page-component`, `app-page-simple-component`) |

Cifras (2026-11-11): 160 componentes (`@Component`), 21 clases `@Injectable`, 153 archivos
`.spec.ts` con 654 casos `it(`, 37 rutas (36 con pantalla más la raíz).

### 2.3 Cómo se comunican

- REST sobre HTTP con JSON. Archivos por `multipart/form-data` y descargas binarias con `Content-Disposition: attachment`.
- Prefijo común de las rutas: `/api/unicauca/fiet/consejo/` (`url.application` en `application.properties`). El frontend lo toma de `frontend/src/enviroments/environment.ts` (`apiUrl: 'http://localhost:8080/api/unicauca/fiet/consejo'`).
- El login (`POST sesiones`) devuelve un JWT. El frontend lo guarda en `localStorage` (`authToken`, `currentUser`) y el interceptor `authInterceptor` lo agrega como `Authorization: Bearer <token>`. Ante un 401, `SesionVencidaService` [EXTENSIÓN] cierra la sesión y lleva al login.
- CORS permite el origen `url.frontend` (`http://localhost:4200`).
- Los botones de acción de cada proceso no se calculan en el cliente: el backend devuelve `accionesDisponibles` en el detalle de la solicitud y el frontend muestra solo esas.

### 2.4 Qué se reutilizó y qué se extendió del proyecto de Julián

| Se reutilizó sin cambios de comportamiento | Se extendió (solo líneas agregadas, salvo T12.21) | Es nuevo |
| --- | --- | --- |
| Usuarios, roles, tipos de usuario, sesiones y JWT, logs, solicitudes al Consejo, tipos de solicitud, orden del día, respuestas, exportación .docx, carga masiva de usuarios, Dockerfile del backend, layout y componentes genéricos del frontend | `ApplicationConstantes`, `BeanConfiguracion`, `ConfiguracionSeguridad` (reglas por URL), `JwtFiltroAutenticacion`, `MensajesError`, `AlmacenadorArchivos`, `SolicitudCUImplAdaptador`, `docker-compose.yml` (variables y zona horaria), `data.sql` (roles nuevos), `app.routes.ts` | Programas y asignaturas, estudiantes y funcionarios académicos con carga masiva, los tres procesos, máquina de etapas, anexos y Resolución, correo, PDF, comunicación programada, notificaciones al docente, pruebas automatizadas |

Los roles `Decano` y `Docente` y el tipo de usuario `Empleado FIET - Funcionario` ya existían en
Julián y se reutilizan. Se agregaron los roles `Estudiante` y `Funcionario Académico` y el tipo
de usuario `Estudiante`. Once archivos de Julián tienen cambios; cuatro de ellos con líneas
reemplazadas por la tarea de seguridad T12.21, que lo pidió de forma explícita (`docs/analisis/02`).

---

## 3. Modelo de datos

Hibernate crea las tablas desde las entidades (`spring.jpa.hibernate.ddl-auto=update`). El DDL de
referencia de la extensión está en `docs/database/script_bd_extension.sql` y el diccionario campo
por campo en `docs/database/diccionario-datos-extension.md`. Las 14 tablas de Julián no cambian de
estructura. Las tablas nuevas se enlazan con `usuarios (uuidUsuario)`.

### 3.1 Tablas nuevas (22)

| Tabla | Propósito | Relaciones principales |
| --- | --- | --- |
| `PROGRAMA_ACADEMICO` | Catálogo fijo de los 5 programas (PIET, PIS, PIAI, TTM, PIAC) y el consecutivo para el código de asignatura | 1 a N con `ASIGNATURA` y `ESTUDIANTE` |
| `ESTUDIANTE` | Datos académicos del usuario estudiante (código, programa, semestre, facultad) | 1 a 1 con `usuarios`; N a 1 con `PROGRAMA_ACADEMICO` |
| `FUNCIONARIO_ACADEMICO` | Usuario con rol Funcionario Académico y su cargo | 1 a 1 con `usuarios`; 1 a N con `TIPO_SOLICITUD_ACADEMICA` (responsable) |
| `ASIGNATURA` | Catálogo de asignaturas; código generado (`PIS-001`), único y no reutilizable | N a 1 con `PROGRAMA_ACADEMICO` |
| `ASIGNATURA_MATRICULADA` | Matrícula de un estudiante en una asignatura y grupo; estado `activa`, `cancelada`, `aprobada` o `perdida` | N a 1 con `ESTUDIANTE` y `ASIGNATURA` |
| `TIPO_SOLICITUD_ACADEMICA` | Los 3 procesos y el Funcionario responsable de cada uno | N a 1 con `FUNCIONARIO_ACADEMICO` |
| `ETAPA_SOLICITUD_ACADEMICA` | Etapas (RADICADA, EN_REVISION_DECANO, ..., APROBADA, RECHAZADA) | Opcionalmente ligada a un tipo (las del pago, al supletorio) |
| `ETAPA_ETIQUETA_ROL` | Texto que ve cada rol en cada etapa | N a 1 con `ETAPA_SOLICITUD_ACADEMICA` |
| `SOLICITUD_ACADEMICA` | Cabecera común: radicado, estudiante, tipo, etapa actual, fecha | N a 1 con `ESTUDIANTE`, `TIPO_SOLICITUD_ACADEMICA`, `ETAPA_SOLICITUD_ACADEMICA` |
| `SOLICITUD_CANCELACION_MATRICULA` | Especialización: motivo | 1 a 1 con `SOLICITUD_ACADEMICA` |
| `SOLICITUD_CANCELACION_ASIGNATURA` | Especialización: motivo | 1 a 1 con `SOLICITUD_ACADEMICA` |
| `SOLICITUD_EXAMEN_SUPLETORIO` | Especialización: asignatura, fecha del examen, causa, profesor asignado | 1 a 1 con `SOLICITUD_ACADEMICA`; N a 1 con `ASIGNATURA_MATRICULADA` y con el docente |
| `SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA` | Datos del examen cruzado (asignatura, fecha y hora) | 1 a 0..1 con `SOLICITUD_EXAMEN_SUPLETORIO` |
| `SITUACION_ACADEMICA_ASIGNATURA` | Catálogo de las situaciones (formato PA-GA-4.2-FOR-9): ámbito MATRICULA y CANCELACION | Referenciada desde `ASIGNATURA_SOLICITUD_ACADEMICA` |
| `ASIGNATURA_SOLICITUD_ACADEMICA` | Cada asignatura de una cancelación: faltas, nota, situaciones, decisión del Decano por asignatura | N a 1 con `SOLICITUD_ACADEMICA`, `ASIGNATURA_MATRICULADA` y `SITUACION_ACADEMICA_ASIGNATURA` |
| `TIPO_ANEXO_ACADEMICO` | Catálogo de anexos por proceso: formatos, obligatoriedad, tamaño máximo, vigencia | N a 1 con `TIPO_SOLICITUD_ACADEMICA` |
| `ANEXO_ACADEMICO` | Archivo subido a una solicitud (ruta en disco, nombre original, tipo, tamaño) | N a 1 con `SOLICITUD_ACADEMICA` y `TIPO_ANEXO_ACADEMICO` (nulo en soportes libres) |
| `HISTORIAL_SOLICITUD_ACADEMICA` | Una fila por acción: quién, cuándo, a qué etapa, observación | N a 1 con `SOLICITUD_ACADEMICA` y `usuarios` |
| `RESOLUCION_ACADEMICA` | Escaneo de la Resolución física firmada (solo en las cancelaciones) | 1 a 0..1 con `SOLICITUD_ACADEMICA` |
| `NOTIFICACION_DOCENTE` | Aviso al profesor asignado al supletorio aprobado (leída o no) | N a 1 con `SOLICITUD_ACADEMICA` y `usuarios` |
| `NOTIFICACION_RESOLUCION` | Comunicación programada de la Resolución en Cancelación de Asignatura: fecha, número, año, decisión, textos, estado (PROGRAMADA, EN_PROCESO, ENVIADA, FALLIDA), firma | 1 a 0..1 con `SOLICITUD_ACADEMICA` |
| `CORREO_PENDIENTE` | Cola de correos inmediatos con reintento (estado, intentos, último error) | Sin llaves foráneas |

`CORREO_PENDIENTE` está en `script_bd_extension.sql` y en la base, pero el diccionario de datos
no la documenta todavía, y el `CLAUDE.md` raíz sigue hablando de 21 tablas.

### 3.2 Tablas de Julián que usa la extensión (sin cambiar su estructura)

`usuarios`, `usuariosLivianos` (nombres, apellidos, estado), `roles`, `Usuario_has_Roles`,
`tiposUsuario`, `funcionarios` y `logs`. Las demás (`solicitudes`, `tiposSolicitudes`,
`respuestas`, `ordenesDelDia`, `anexos`, `tiposAnexos`, `informacionSolicitantes`) son del
proceso del Consejo de Facultad. La extensión no las usa.

### 3.3 Datos sembrados

- `backend/solicitudes/src/main/resources/data.sql`: roles, tipos de usuario y el usuario inicial `rootfiet` (Secretario General).
- `docs/database/seed-procesos-academicos.sql`: programas, tipos de solicitud, etapas, etiquetas por rol, tipos de anexo y situaciones académicas.
- `spring.sql.init.mode=never`: nada se siembra solo; los scripts se corren a mano (README).

---

## 4. API

Todas las rutas cuelgan de `/api/unicauca/fiet/consejo/`. El contrato detallado de la extensión,
con cuerpos y errores, está en `docs/api/contrato-api-procesos-academicos.md`.

### 4.1 Endpoints de la extensión (79)

**Asignaturas (7)**

| Método | Ruta | Roles | Descripción |
| --- | --- | --- | --- |
| POST | `asignaturas` | Secretario General, Decano | Crea una asignatura; el código lo genera el backend |
| POST | `asignaturas/cargar/archivo` | Secretario General, Decano | Carga masiva desde Excel (todo o nada) |
| GET | `asignaturas/paginado` | Sec. General, Decano, Funcionario Académico | Lista paginada |
| GET | `asignaturas/filtro` | Sec. General, Decano, Funcionario Académico | Búsqueda por texto y programa |
| GET | `asignaturas/programa/{uuidPrograma}` | Sec. General, Decano, Funcionario Académico | Asignaturas de un programa |
| GET | `asignaturas/{uuid}` | Sec. General, Decano, Funcionario Académico | Una asignatura |
| PUT | `asignaturas/{uuid}` | Secretario General, Decano | Cambia solo el nombre |

**Estudiantes (9)**

| Método | Ruta | Roles | Descripción |
| --- | --- | --- | --- |
| POST | `estudiantes` | Secretario General, Decano | Crea estudiante con sus asignaturas matriculadas |
| POST | `estudiantes/cargar/archivo` | Secretario General, Decano | Carga masiva desde Excel (una fila por asignatura) |
| GET | `estudiantes/paginado` | Sec. General, Decano, Funcionario Académico | Lista paginada |
| GET | `estudiantes/filtro` | Sec. General, Decano, Funcionario Académico | Búsqueda por nombre, apellido o código |
| GET | `estudiantes/mis-asignaturas` | Estudiante | Asignaturas activas del estudiante autenticado |
| GET | `estudiantes/{uuid}` | Sec. General, Decano, Funcionario Académico | Un estudiante |
| PUT | `estudiantes/{uuid}` | Secretario General, Decano | Edita datos personales y académicos |
| POST | `estudiantes/{uuid}/asignaturas` | Secretario General, Decano | Matricula una asignatura de su programa |
| PATCH | `estudiantes/{uuid}/asignaturas/{uuidMatricula}/estado` | Secretario General, Decano | Cambia el estado de una matrícula activa |

**Funcionarios académicos (6)**

| Método | Ruta | Roles | Descripción |
| --- | --- | --- | --- |
| POST | `funcionarios-academicos` | Secretario General, Decano | Crea un funcionario académico |
| POST | `funcionarios-academicos/cargar/archivo` | Secretario General, Decano | Carga masiva desde Excel |
| GET | `funcionarios-academicos/paginado` | Sec. General, Decano, Funcionario Académico | Lista paginada |
| GET | `funcionarios-academicos/filtro` | Sec. General, Decano, Funcionario Académico | Búsqueda por nombre, apellido o cargo |
| GET | `funcionarios-academicos/{uuid}` | Sec. General, Decano, Funcionario Académico | Un funcionario |
| PUT | `funcionarios-academicos/{uuid}` | Secretario General, Decano | Edita sus datos |

**Catálogos académicos (7)**

| Método | Ruta | Roles | Descripción |
| --- | --- | --- | --- |
| GET | `catalogos-academicos/tipos-solicitud` | Cualquier autenticado | Los 3 procesos y su responsable |
| GET | `catalogos-academicos/tipos-solicitud/{uuid}/etapas` | Cualquier autenticado | Etapas de un proceso |
| GET | `catalogos-academicos/tipos-solicitud/{uuid}/tipos-anexo` | Cualquier autenticado | Anexos de un proceso |
| GET | `catalogos-academicos/etiquetas` | Cualquier autenticado | Etiquetas de etapa por rol |
| GET | `catalogos-academicos/situaciones` | Cualquier autenticado | Situaciones académicas |
| GET | `catalogos-academicos/programas` | Cualquier autenticado | Los 5 programas |
| PUT | `catalogos-academicos/tipos-solicitud/{uuid}/funcionario` | Secretario General, Decano | Asigna el Funcionario responsable de un proceso |

**Solicitudes académicas, comunes a los tres procesos (12)**

| Método | Ruta | Roles | Descripción |
| --- | --- | --- | --- |
| GET | `solicitudes-academicas/estudiante` | Estudiante | Bandeja del estudiante |
| GET | `solicitudes-academicas/funcionario` | Funcionario Académico | Bandeja del funcionario (sus procesos) |
| GET | `solicitudes-academicas/funcionario/correos-pendientes` | Funcionario Académico | Cantidad de correos en cola de reintento |
| GET | `solicitudes-academicas/funcionario/respuestas/conteo` | Funcionario Académico | Cuántas respuestas se borrarían en un rango |
| DELETE | `solicitudes-academicas/funcionario/respuestas` | Funcionario Académico | Borra respuestas cerradas en un rango de fechas |
| GET | `solicitudes-academicas/decano` | Decano | Bandeja del Decano |
| GET | `solicitudes-academicas/{uuid}` | Estudiante, Funcionario Académico, Decano | Detalle común con `accionesDisponibles` |
| GET | `solicitudes-academicas/{uuid}/historial` | Estudiante, Funcionario Académico, Decano | Historial filtrado por rol |
| POST | `solicitudes-academicas/{uuid}/anexos` | Estudiante, Funcionario Académico | Sube un anexo (recibo, comprobante, soporte) |
| GET | `solicitudes-academicas/{uuid}/anexos/{uuidAnexo}` | Estudiante, Funcionario Académico, Decano | Descarga un anexo |
| POST | `solicitudes-academicas/{uuid}/resolucion` | Funcionario Académico | Sube el escaneo de la Resolución |
| GET | `solicitudes-academicas/{uuid}/resolucion` | Estudiante, Funcionario Académico, Decano | Descarga la Resolución |

**Cancelación de Matrícula (8)**

| Método | Ruta | Roles | Descripción |
| --- | --- | --- | --- |
| GET | `cancelaciones-matricula/formulario` | Estudiante | Anexo requerido y asignaturas activas |
| POST | `cancelaciones-matricula` | Estudiante | Radica (motivo, PDF de paz y salvos, soportes) |
| GET | `cancelaciones-matricula/{uuid}` | Estudiante, Funcionario Académico, Decano | Detalle del proceso |
| POST | `cancelaciones-matricula/{uuid}/funcionario/rechazar` | Funcionario Académico | Rechazo directo con observación |
| POST | `cancelaciones-matricula/{uuid}/funcionario/remitir` | Funcionario Académico | Remite al Decano con faltas, nota y situaciones |
| POST | `cancelaciones-matricula/{uuid}/funcionario/responder` | Funcionario Académico | Respuesta final (exige Resolución) |
| POST | `cancelaciones-matricula/{uuid}/decano/aprobar` | Decano | Aprueba |
| POST | `cancelaciones-matricula/{uuid}/decano/rechazar` | Decano | Rechaza |

**Cancelación de Asignatura (13)**

| Método | Ruta | Roles | Descripción |
| --- | --- | --- | --- |
| GET | `cancelaciones-asignatura/formulario` | Estudiante | Asignaturas activas y soportes permitidos |
| POST | `cancelaciones-asignatura` | Estudiante | Radica (motivo, asignaturas, soportes) |
| GET | `cancelaciones-asignatura/{uuid}` | Estudiante, Funcionario Académico, Decano | Detalle del proceso |
| POST | `cancelaciones-asignatura/{uuid}/funcionario/rechazar` | Funcionario Académico | Rechazo directo |
| POST | `cancelaciones-asignatura/{uuid}/funcionario/remitir` | Funcionario Académico | Remite al Decano |
| POST | `cancelaciones-asignatura/{uuid}/funcionario/responder` | Funcionario Académico | Respuesta final con situaciones, mensaje inmediato y comunicación programada |
| GET | `cancelaciones-asignatura/{uuid}/funcionario/notificacion/plantilla` | Funcionario Académico asignado | Textos y decisión propuestos para la comunicación |
| GET | `cancelaciones-asignatura/{uuid}/funcionario/notificacion` | Funcionario Académico asignado | Estado de la comunicación programada |
| PUT | `cancelaciones-asignatura/{uuid}/funcionario/notificacion` | Funcionario Académico asignado | Reprograma o corrige número, año y decisión |
| POST | `cancelaciones-asignatura/{uuid}/funcionario/notificacion/firma` | Funcionario Académico asignado | Registra la Resolución firmada por el estudiante y cierra la comunicación |
| POST | `cancelaciones-asignatura/{uuid}/funcionario/notificacion/reintentar` | Funcionario Académico asignado | Reintenta una comunicación FALLIDA |
| POST | `cancelaciones-asignatura/{uuid}/decano/aprobar` | Decano | Decisión por asignatura (aprobación parcial) |
| POST | `cancelaciones-asignatura/{uuid}/decano/rechazar` | Decano | Rechaza la solicitud completa (comentario obligatorio) |

**Examen Supletorio (13)**

| Método | Ruta | Roles | Descripción |
| --- | --- | --- | --- |
| GET | `examenes-supletorios/formulario` | Estudiante | Asignaturas, causas, anexos y plazo |
| POST | `examenes-supletorios` | Estudiante | Radica dentro de 3 días hábiles |
| GET | `examenes-supletorios/{uuid}` | Estudiante, Funcionario Académico, Decano | Detalle del proceso |
| POST | `examenes-supletorios/{uuid}/funcionario/rechazar` | Funcionario Académico | Rechazo directo |
| POST | `examenes-supletorios/{uuid}/funcionario/remitir` | Funcionario Académico | Remite al Decano |
| POST | `examenes-supletorios/{uuid}/funcionario/responder` | Funcionario Académico | Respuesta tras el rechazo del Decano |
| POST | `examenes-supletorios/{uuid}/funcionario/recibo` | Funcionario Académico | Envía el recibo de pago |
| POST | `examenes-supletorios/{uuid}/funcionario/comprobante/aprobar` | Funcionario Académico | Aprueba el pago y asigna el profesor |
| POST | `examenes-supletorios/{uuid}/funcionario/comprobante/rechazar` | Funcionario Académico | Rechaza el pago |
| POST | `examenes-supletorios/{uuid}/decano/aprobar` | Decano | Aprueba |
| POST | `examenes-supletorios/{uuid}/decano/rechazar` | Decano | Rechaza |
| POST | `examenes-supletorios/{uuid}/estudiante/comprobante` | Estudiante | Confirma el comprobante subido |
| GET | `examenes-supletorios/docentes` | Funcionario Académico | Búsqueda paginada de profesores |

**Notificaciones del docente (4)**

| Método | Ruta | Roles | Descripción |
| --- | --- | --- | --- |
| GET | `notificaciones-docente` | Docente | Sus notificaciones |
| GET | `notificaciones-docente/{uuid}` | Docente | Detalle de una notificación |
| PATCH | `notificaciones-docente/{uuid}/leida` | Docente | Marca como leída |
| GET | `notificaciones-docente/{uuid}/anexos/{uuidAnexo}` | Docente | Descarga un anexo de la solicitud |

### 4.2 Endpoints de Julián (62), por controlador

| Controlador | Endpoints | Recurso |
| --- | --- | --- |
| `controladorSolicitudes` | 24 | Solicitudes al Consejo de Facultad |
| `controladorUsuarios` | 10 | Usuarios (incluye carga masiva) |
| `controladorTiposSolicitud` | 10 | Tipos de solicitud del Consejo |
| `controladorRespuestas` | 10 | Respuestas del Consejo |
| `controladorRoles` | 4 | Roles |
| `controladorLog` | 3 | Bitácora |
| `controladorSesion` | 1 | `POST sesiones` (login) |

El detalle de cada uno está en `docs/analisis/03-backend-endpoints.md`.

### 4.3 Errores

Los errores de negocio responden **HTTP 500** con un `codigoError`: 1 genérico, 2 ya existe,
3 no existe o no visible, 4 regla de negocio, 6 formato, 7 sin información. Las validaciones de
anotaciones responden 400 con un mapa campo-mensaje, la falta de token 401 y un rol no permitido
403. Así lo manda el manejador de Julián (`RestApiExcepcion`).

---

## 5. Seguridad

| Aspecto | Implementación | Dónde |
| --- | --- | --- |
| Autenticación | Usuario y contraseña en `POST sesiones`; responde un JWT firmado con `JWT_SECRET` | `controladorSesion`, `JwtServicio` |
| Vigencia del token | 24 horas (`24 * 60 * 60 * 1000` ms) | `JwtServicio.java`, línea 38 |
| Contraseñas | BCrypt (`BCryptPasswordEncoder`) | `ConfiguracionAplicacion.java` |
| Sesión | Sin estado (JWT por petición); CSRF deshabilitado; CORS solo para `url.frontend` | `ConfiguracionSeguridad.java` |
| Filtro | `JwtFiltroAutenticacion` valida el token y carga los roles como autoridades | `seguridad/jwt/` |
| Autorización | Reglas por URL y método (`requestMatchers(...).hasAnyAuthority(...)`), con `denyAll()` al final de cada grupo. No hay `@EnableMethodSecurity`, así que los `@PreAuthorize` no se evalúan: la seguridad real son las reglas por URL | `ConfiguracionSeguridad.java`, líneas 49-140 y siguientes |
| Control de pertenencia | En el dominio: el estudiante solo ve las suyas, el Funcionario solo los procesos asignados a él, el Decano no ve las RADICADAS; una solicitud no visible responde como inexistente (código 3) | `ValidadorActorSolicitud`, `TramiteSolicitud` |
| Validación de archivos | Tamaño máximo 5 MB (10 MB el PDF de paz y salvos), archivo no vacío, extensión del catálogo del anexo y firma del contenido (`%PDF-`, PNG, JPG); Spring corta cualquier petición de más de 20 MB | `ValidadorArchivoAdjunto`, `validadoresArchivos/`, `application.properties` |
| Excel | Solo `.xlsx` válido, encabezados exactos, todo o nada | `lectorArchivos/ProcesarArchivo*Impl.java` |
| Secretos | `JWT_SECRET` y `CORREO_DARCA` obligatorios desde `backend/.env`; sin valor por defecto en archivos versionados; el backend no arranca si faltan | `VariablesEntornoObligatorias`, `docker-compose.yml` |
| Archivos en disco | Se guardan con nombre generado (uuid y marca de tiempo), no con el nombre original; la ruta se normaliza y no sale de la carpeta base | `AlmacenamientoAnexosImplAdaptador.java` |

Roles del sistema que usa la extensión: Secretario General, Decano, Funcionario Académico,
Estudiante y Docente. La matriz completa rol por endpoint se comprueba con
`backend/pruebas/matriz-permisos.ps1`.

---

## 6. Procesos implementados

### 6.1 Etapas comunes

| Etapa | Procesos | Quién actúa | Ve el Estudiante | Ve el Funcionario | Ve el Decano |
| --- | --- | --- | --- | --- | --- |
| RADICADA | los 3 | Funcionario | En trámite | Pendiente | no la ve |
| EN_REVISION_DECANO | los 3 | Decano | En trámite | En Gestión | Pendiente |
| APROBADA_POR_DECANO | los 3 | Funcionario | En trámite | Pendiente Notificar (Enviar Recibo en supletorio) | Respondida |
| RECHAZADA_POR_DECANO | los 3 | Funcionario | En trámite | Pendiente Notificar | Respondida |
| PENDIENTE_PAGO | supletorio | Estudiante | Pendiente de pago | En Gestión | Respondida |
| EN_VERIFICACION_PAGO | supletorio | Funcionario | En verificación de pago | Recibo Pagado | Respondida |
| APROBADA / RECHAZADA | los 3 (finales) | nadie | Aprobada / Rechazada | Respondida | Respondida |

Las transiciones válidas están en `MaquinaEtapas`. Cada acción escribe una fila en
`HISTORIAL_SOLICITUD_ACADEMICA`. El estudiante no se entera de la decisión del Decano hasta la
respuesta del Funcionario.

### 6.2 Cancelación de Matrícula

```
RADICADA --rechazar (Funcionario)--> RECHAZADA
RADICADA --remitir (Funcionario)--> EN_REVISION_DECANO --aprobar/rechazar (Decano)--> APROBADA_POR_DECANO / RECHAZADA_POR_DECANO
APROBADA_POR_DECANO / RECHAZADA_POR_DECANO --responder con Resolución (Funcionario)--> APROBADA / RECHAZADA
```

Reglas que aplica el código:

- Un solo anexo obligatorio: un PDF con todos los paz y salvos, de hasta 10 MB. Los seis anexos anteriores quedan retirados (`vigente = 0`).
- La solicitud incluye todas las asignaturas activas del estudiante; al aprobar, todas pasan a `cancelada`.
- Una sola solicitud en curso por estudiante y proceso.
- Al remitir, faltas, nota (0.0 a 5.0, se acepta coma) y situaciones son opcionales, y las situaciones salen de listas cerradas.
- El rechazo directo del Funcionario exige observación y no lleva Resolución. La respuesta final después del Decano exige el escaneo de la Resolución.
- El estudiante solo descarga la Resolución cuando la solicitud termina.

### 6.3 Cancelación de Asignatura

```
RADICADA --rechazar--> RECHAZADA (sin Resolución)
RADICADA --remitir--> EN_REVISION_DECANO --aprobar por asignatura / rechazar todo--> APROBADA_POR_DECANO / RECHAZADA_POR_DECANO
--responder (Resolución sin firmar + mensaje inmediato + comunicación programada)--> APROBADA / RECHAZADA
```

Reglas que aplica el código:

- Aprobación parcial: el Decano decide por asignatura y debe aprobar al menos una. Si rechaza alguna, o la solicitud completa, el comentario para el Funcionario es obligatorio.
- El Funcionario remite sin evaluar si "cumple condiciones" (P67).
- Al responder, cada asignatura aprobada exige situación en la matrícula, situación al cancelar y estado académico para cuando la matricule de nuevo. Solo se cancelan las aprobadas.
- La respuesta después del Decano exige la Resolución sin firmar, un mensaje inmediato (sale siempre, sin adjuntos) y la programación de la comunicación: fecha y hora futuras en hora de Colombia, número con prefijo `8.4.2-4.10/`, año y decisión (hasta 500 caracteres). La decisión propuesta nombra las asignaturas aprobadas y las negadas.
- Visibilidad diferida: hasta la firma o la fecha programada, el estudiante ve "En trámite" y no recibe la respuesta, el resultado ni la Resolución.
- La firma del estudiante en la Decanatura, registrada antes de la fecha, reemplaza la Resolución, cierra la comunicación y envía de inmediato el correo a DARCA. Después no se puede editar ni subir otra.
- Situaciones: "situación de la asignatura" (Cursada por primera vez, (R1), (R2), (R3)) y "situación al cancelar" (No cursada, Como perdida); el backend rechaza otros valores con código 6.

### 6.4 Examen Supletorio

```
RADICADA --rechazar--> RECHAZADA
RADICADA --remitir--> EN_REVISION_DECANO --rechazar--> RECHAZADA_POR_DECANO --responder--> RECHAZADA
EN_REVISION_DECANO --aprobar--> APROBADA_POR_DECANO --recibo--> PENDIENTE_PAGO --comprobante (Estudiante)--> EN_VERIFICACION_PAGO
EN_VERIFICACION_PAGO --aprobar pago + profesor--> APROBADA | --rechazar pago--> RECHAZADA
```

Reglas que aplica el código:

- Plazo de 3 días hábiles (lunes a viernes, sin festivos) desde el examen no presentado, en hora de Colombia.
- Causa `cruce` u `otra`. El formato firmado por el docente siempre; el soporte de la justificación con `otra`; con `cruce`, la asignatura, la fecha y la hora del examen cruzado. No se admiten soportes libres.
- No produce Resolución.
- Aprobar el pago exige elegir un profesor activo con rol Docente; el profesor recibe una notificación en su perfil y un correo.
- Ninguna acción del supletorio cambia el estado de las asignaturas matriculadas.

### 6.5 Reglas comunes

- Radicado con formato `AAAA-<CM|CA|ES>-NNNN` (por ejemplo `2026-CA-0020`).
- Observaciones de hasta 500 caracteres. Las del Decano y la de la remisión no las ve el estudiante.
- Plazos de Cancelación de Matrícula y de Asignatura: el sistema no valida fechas (`etapas-por-proceso.md`, sección 6).
- Borrado de respuestas por rango (solo el Funcionario y solo solicitudes cerradas de sus procesos, sin comunicación pendiente).

---

## 7. Frontend: vistas por rol

| Rol | Rutas | Qué hace |
| --- | --- | --- |
| Estudiante | `estudiante`, `estudiante/solicitudes`, `estudiante/catalogo`, `estudiante/solicitudes/:uuid`, `estudiante/cancelacion-matricula`, `estudiante/cancelacion-asignatura`, `estudiante/examen-supletorio` | Inicio, bandeja, catálogo de procesos, detalle con historial y descargas, y los tres formularios de radicación |
| Funcionario Académico | `funcionario-academico`, `funcionario-academico/solicitudes`, `funcionario-academico/respuestas`, `funcionario-academico/solicitudes/:uuid` | Bandeja de pendientes, "Solicitudes respondidas" (con borrado por rango), detalle con acciones, tabla de asignaturas, panel "Notificación de la Resolución" y aviso de correos pendientes |
| Decano | `decano`, `decano/solicitudes`, `decano/solicitudes/:uuid`, `decano/solicitudes-consejo`, `decano/administracion-academica` | Bandeja, detalle con "Enviar decisión" (más "Aprobar" y "Rechazar" en asignatura), solicitudes del Consejo [BASE] y administración académica |
| Secretario General | `sec-general/administracion-academica` [EXTENSIÓN] más sus 8 pantallas [BASE] | Asignaturas, estudiantes y funcionarios académicos (formularios y carga de Excel) y asignación de responsables |
| Docente | `docente/notificaciones`, `docente/notificaciones/:uuid` | Notificaciones de supletorios aprobados y descarga de anexos |

Componentes principales de la extensión (en `src/app/shared/` salvo que se indique otra cosa):

| Componente | Papel |
| --- | --- |
| `bandeja-solicitudes-academicas-component` | Bandeja de los tres roles; modo `solicitudes` o `respuestas` |
| `detalle-solicitud-academica-component` | Detalle común: datos generales, anexos, historial, Resolución; tarjetas plegables para el Funcionario |
| `cancelacion-matricula-proceso-component`, `cancelacion-asignatura-proceso-component`, `examen-supletorio-proceso-component` | Acciones de cada proceso según `accionesDisponibles` |
| `programacion-resolucion-component` | Programación de la comunicación (fecha, número, decisión, textos) |
| `carga-excel-academica-component` | Carga masiva de asignaturas, estudiantes y funcionarios |
| `aviso-correos-pendientes-component` | Aviso de correos en cola de reintento |
| `admin-*-content-component` | Administración académica (asignaturas, estudiantes, funcionarios) |
| `est-*-content-component` | Formularios del estudiante |

Mecanismos: `AuthGuard` y `RoleGuard` protegen las rutas por rol (`data.roles`). Las bandejas y
los detalles usan los servicios HTTP de `core/services`. Los diálogos son genéricos
(`app-generic-dialog-*`). Las alertas usan SweetAlert2 y el `ToastService`.

---

## 8. Correos y archivos

### 8.1 Notificaciones por correo

Solo salen correos cuando la solicitud termina, más el mensaje inmediato de Cancelación de
Asignatura. Se envían por SMTP con Spring Mail (Gmail, credenciales en `MAIL_USER` y
`MAIL_PASSWORD`).

| Momento | Estudiante | DARCA (`CORREO_DARCA`) | Profesor |
| --- | --- | --- | --- |
| Cancelación de Matrícula: rechazo directo | Aviso de respuesta | PDF del radicado | no aplica |
| Cancelación de Matrícula: respuesta tras el Decano | Aviso + Resolución | PDF del radicado + Resolución | no aplica |
| Cancelación de Asignatura: rechazo directo | Aviso de respuesta | PDF del radicado + soportes | no aplica |
| Cancelación de Asignatura: respuesta tras el Decano | Mensaje inmediato sin adjuntos | nada en ese momento | no aplica |
| Cancelación de Asignatura: fecha programada, sin firma | Comunicación de la Resolución (cita la Ley 1437 de 2011) + Resolución | 3 PDF: radicado, Resolución y constancia de la comunicación | no aplica |
| Cancelación de Asignatura: firma registrada antes de la fecha | nada | De inmediato: radicado + Resolución firmada | no aplica |
| Examen Supletorio: cualquier cierre | Aviso de respuesta | PDF del radicado + soporte de la justificación | Solo al aprobar el pago: aviso con el radicado |

El PDF del radicado de DARCA lleva siempre el motivo (en el supletorio, la causa) y el correo
adjunta los soportes del estudiante (P64).

Cómo se envían:

- Los correos inmediatos se guardan en `CORREO_PENDIENTE` dentro de la transacción de la respuesta y salen en segundo plano después del commit (`CorreoImplAdaptador`, `ColaCorreos`). Si no hay conexión, quedan PENDIENTES y una tarea los reintenta cada 60 s (`correo.pendientes.intervalo-ms`) sin gastar intentos. Tras 5 rechazos del servidor quedan abandonados.
- La comunicación programada la procesa `TareaNotificacionResolucion` cada 60 s (`correo.resolucion.intervalo-ms`), con hasta 3 intentos (`MAXIMO_INTENTOS` en `NotificacionResolucionCUImplAdaptador`). Luego queda FALLIDA, con reintento manual desde el panel. Los fallos de conexión no cuentan como intento: la comunicación sigue PROGRAMADA y se reintenta en la siguiente pasada.
- Un fallo de correo nunca revierte la respuesta.
- La firma del correo (nombre, cargo, dependencia, facultad) y el prefijo de la Resolución se leen de `application.properties` (`correo.firma.*`, `correo.resolucion.prefijo`).

### 8.2 Anexos y Resoluciones

- Se guardan en disco bajo `app.uploads.base-path` (`UPLOADS_PATH`, por defecto `/app/uploads`), en `anexos/<uuidSolicitud>/`, con nombre `<uuid>_<AAAAMMDDHHmmss>.<ext>`. En Docker esa carpeta es el volumen `./uploads:/app/uploads`, es decir, `backend/uploads`.
- En la base solo va la referencia (`ANEXO_ACADEMICO` y `RESOLUCION_ACADEMICA`): ruta, nombre original, tipo y tamaño. Las respuestas de la API nunca exponen la ruta.
- La Resolución es el escaneo de un documento físico firmado a mano; no hay firma digital. Vive en `RESOLUCION_ACADEMICA`, separada de los anexos, y subir otra la reemplaza mientras la etapa lo permita.
- Al borrar respuestas por rango se borran también los archivos, después del commit.

---

## 9. Pruebas

| Tipo | Dónde | Cantidad | Qué cubre |
| --- | --- | --- | --- |
| Unitarias del backend (JUnit 5 + Mockito) | `backend/solicitudes/src/test/java` | 52 archivos, 925 métodos (`@Test` + `@ParameterizedTest`) | Casos de uso (camino feliz y cada error, comparando el texto exacto del mensaje), máquina de etapas, validadores, lectores de Excel, almacenamiento con `@TempDir`, mapeadores, correo (MIME, adjuntos), PDF, tarea programada, reglas de seguridad por URL (`ConfiguracionSeguridadRolesTest`) |
| Integración del backend | `SolicitudesApplicationTests` | 1 (`contextLoads`, con `@SpringBootTest`) | Que el contexto completo arranque contra la base |
| Unitarias del frontend (Jasmine + Karma) | `frontend/src/app/**/*.spec.ts` | 153 archivos, 654 casos `it(` | Componentes, servicios HTTP con `HttpTestingController`, interceptor, guards y utilidades |
| Humo / integración por API (PowerShell) | `backend/pruebas/` | 23 scripts `.ps1` (`t1` a `t10`, `matriz-permisos`, `todo.ps1`, `comun.ps1` y generadores de plantillas) | Cada endpoint contra el backend real y MySQL de Docker: catálogos, estudiantes, funcionarios, cargas de Excel, los tres procesos, permisos rol por endpoint, correo contra `smtp_prueba.py` (`t9`) y borrado por rango (`t10`). `todo.ps1` compara una foto de la base antes y después |
| Prueba manual de aceptación | `docs/frontend/checklist-recorrido-completo.md` | Lista de recorrido | Recorrido por pantallas **[por confirmar si se ejecutó y cuándo]** |

Últimos resultados registrados en `change.md` (2026-10-27; no se volvieron a correr para este
documento): backend 1622 de 1622 ejecuciones en verde; frontend en verde salvo 58 specs
`should create` de Julián que fallan con NG0201 (inyección); humo `todo.ps1` 1404 PASS y 0 FAIL;
`t9` 448 PASS y 0 FAIL.

No hay pruebas end-to-end en el navegador (Cypress, Playwright), ni `@WebMvcTest` o
`@DataJpaTest`, ni colección de Postman.

---

## 10. Despliegue

### 10.1 Docker (opción recomendada)

- `backend/Dockerfile` [BASE]: imagen `eclipse-temurin:17-jdk-jammy`. Copia el jar ya compilado (`solicitudes/target/solicitudes-0.0.1.jar`) y lo ejecuta en el puerto 8080. Antes hay que compilar con `./mvnw clean install -DskipTests`.
- `backend/docker-compose.yml`: dos servicios.
  - `solicitudes-consejo-de-facultad`: puerto `8080:8080`, `mem_limit: 512m`, volumen `./uploads:/app/uploads`, `restart: always`, espera a que MySQL esté sano.
  - `cfiet_database`: `mysql:8.0.33`, puerto `3307:3306`, `--default-time-zone=-05:00`, healthcheck con `mysqladmin ping`.
- Zona horaria: `TZ: America/Bogota` en los dos contenedores y `serverTimezone=America/Bogota` en la conexión.
- `frontend/dockerfile`: dos etapas con `node:20-alpine`, sirve el SSR (`node dist/front-fiet-sc/server/server.mjs`) en el puerto 4200. No está en el compose; en desarrollo se usa `npm start`.

### 10.2 Variables de entorno

| Variable | Obligatoria | Para qué |
| --- | --- | --- |
| `JWT_SECRET` | Sí | Clave de firma de los tokens (Base64 o hexadecimal de al menos 64 caracteres) |
| `CORREO_DARCA` | Sí | Buzón de DARCA |
| `MAIL_USER`, `MAIL_PASSWORD` | No | Cuenta de Gmail y contraseña de aplicación; sin ellas el envío solo deja el error en el log |
| `MAIL_EHLO` | No (por defecto `fiet-unicauca.local`) | Nombre con que el backend se presenta al servidor SMTP |
| `JAVA_TOOL_OPTIONS` | No | Con `-Djava.net.preferIPv6Addresses=true`, el correo sale por IPv6 (la red `cfiet_red` del compose tiene IPv6) |
| `SERVER_PORT`, `DB_URL`, `DB_USER_NAME`, `DB_PASSWORD` | Sí (el compose las fija) | Puerto y conexión a MySQL |
| `UPLOADS_PATH` | No (por defecto `/app/uploads`) | Carpeta de archivos |
| `CORREO_RESOLUCION_INTERVALO_MS`, `CORREO_PENDIENTES_INTERVALO_MS` y sus retrasos iniciales | No (60000 ms) | Frecuencia de las tareas de correo |

Se definen en `backend/.env`, que no se versiona (plantilla en `backend/.env.example`). Si falta
`JWT_SECRET` o `CORREO_DARCA`, ni el compose ni el backend arrancan.

### 10.3 Pasos

1. `cd backend/solicitudes && ./mvnw clean install -DskipTests`
2. `cd .. && docker compose up --build` (backend en `localhost:8080`, MySQL en `localhost:3307`)
3. Con la base vacía: correr `data.sql`, crear el Funcionario Académico y correr `docs/database/seed-procesos-academicos.sql`.
4. `cd ../frontend && npm install && npm start` (frontend en `localhost:4200`).

Sin Docker: JDK 17, MySQL 8 y Node 20.19+ instalados, con las mismas variables (README, opción B).

---

## 11. Puntos a tener en cuenta al redactar

Diferencias entre la documentación y el código que conviene no trasladar a la monografía:

- El token dura **24 horas**, no 5 (`JwtServicio.java:38`).
- Los `@PreAuthorize` no se evalúan (no hay `@EnableMethodSecurity`); la autorización real es por URL.
- Los errores de negocio responden HTTP 500 con `codigoError`, no 4xx.
- Las ramas Prod y Dev no existen en el repositorio; solo hay `main`.
- Sí hay pruebas automatizadas, además de Postman y UAT: ver la sección 9. De hecho no hay colección de Postman en el repositorio.
- `docs/api/contrato-api-procesos-academicos.md`, sección 4.1, todavía habla de "los seis anexos" y de subir el escaneo para el rechazo directo; el código pide un solo PDF de paz y salvos y no pide Resolución en el rechazo directo (P55, P56).
- `docs/database/etapas-por-proceso.md`, sección 3.1, todavía menciona "si cumple las condiciones" al remitir una Cancelación de Asignatura; desde P67 el código no lo pide.
- `CORREO_PENDIENTE` no está en el diccionario de datos y el `CLAUDE.md` raíz dice 21 tablas; en la base hay 22 de la extensión.
- La carga masiva lee las celdas con `DataFormatter` de Apache POI. Un número de 12 o más dígitos escrito como número en Excel (por ejemplo, el código estudiantil `104619010001` de la plantilla) se guarda como `1.04619E+11`. Lo confirmó la simulación del 2026-11-11 (`Simulacion-Pruebas-2026-11-11/REPORTE-SIMULACION.md`). Mientras no se corrija, esas columnas deben ir como texto en el Excel.
