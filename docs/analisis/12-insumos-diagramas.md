# 12 - Insumos para diagramas

Listas concretas de los elementos de cada diagrama de la monografía. El código Mermaid correspondiente está en `13-diagramas-fuente.md` con la misma numeración (41 diagramas). Etiquetas: [BASE] = prototipo de Julián David Camacho Erazo; [EXTENSIÓN] = este trabajo de grado; NO VERIFICADO = no comprobado por lectura. Fuentes: documentos 02 a 08 y 11 de `docs/analisis/`, contrastados con el código en `backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/` (abreviado `<J>/`), `backend/docker-compose.yml`, `frontend/dockerfile`.

Actualizado el 2026-10-21 (T12.37): actores Docente y DARCA, correo, comunicación programada de la Resolución, reglas que cambiaron de T12.28 a T12.35 y cifras. Los diagramas de la mensajería (secuencia del correo, estados de `NOTIFICACION_RESOLUCION`) están en [correo-electronico.md](correo-electronico.md). Los bloques Mermaid de `13-diagramas-fuente.md` llevan una nota con lo que cambió.

Nota sobre el documento 09 (flujos y estados): no existía al escribir este; las secuencias y estados de aquí salen del código (`<J>/dominio/servicios/MaquinaEtapas.java`, casos de uso) y de los documentos 03 y 04. Si 09 difiere, prevalece lo verificado en el código.

## 0. Convenciones

- Casos de uso, C4, BPMN y actividades se dibujan con `flowchart` (Mermaid no los trae nativos). `include`: del caso base al incluido. `extend`: del caso que extiende al base.
- Todos los atributos de dominio son `private` con `@Getter @Setter` de Lombok (verificado en `<J>/dominio/modelos/*.java`); los métodos citados son `public`. Los tipos de columna están en el documento 05.
- Precondición común a todo caso de uso salvo "Consultar tipos de solicitud" y "Enviar solicitud pública": Iniciar sesión (JWT HS256, 24 h; documento 06).
- El rol del actor nunca viaja desde el cliente: se deduce del token (`ConsultaSolicitudAcademicaCU.resolverActor`).

## 1. Casos de uso (CU-01 a CU-05)

### 1.1 Actores
Estudiante (rol `Estudiante`), Funcionario Académico (rol `Funcionario Académico`, tipo de usuario `Empleado FIET - Funcionario`, fila en `FUNCIONARIO_ACADEMICO`), Decano (rol `Decano`, sin fila en `FUNCIONARIO_ACADEMICO`), Secretario General [BASE], y del Consejo [BASE]: Funcionario (Consejo), Secretaria Decanatura FIET, Usuario FIET (Coordinador Pregrado, Coordinador Posgrados, Jefe de Departamento, Decano, Docente) y solicitante público sin sesión. Fuente: `CLAUDE.md` raíz; reglas por URL en `<J>/infraestructura/configuracion/seguridad/configuracion/ConfiguracionSeguridad.java` (documento 06, sección 6). El Secretario General no tiene acceso a `solicitudes-academicas`, `cancelaciones-*` ni `examenes-supletorios`.

### 1.2 CU-01 Estudiante [EXTENSIÓN]
Casos: iniciar sesión; consultar catálogo de trámites (contenido estático, `core/constantes/catalogo-estudiante.ts`); radicar Cancelación de Matrícula, Cancelación de Asignatura y Examen Supletorio; consultar formulario del trámite (`GET .../formulario`); consultar mis solicitudes (`GET solicitudes-academicas/estudiante`); consultar detalle; consultar historial; descargar anexo; descargar Resolución escaneada (solo si la etapa es final, `ResolucionAcademicaCU.obtenerResolucion`); subir comprobante de pago; consultar mis asignaturas activas (`GET estudiantes/mis-asignaturas`; que el frontend lo use en los formularios es NO VERIFICADO, los formularios traen las asignaturas en `formulario`); cambiar contraseña (`PATCH usuarios/{uuid}`).

| Relación | Origen | Destino | Evidencia |
|---|---|---|---|
| include | Radicar CM, CA, ES | Consultar formulario | los tres `getFormulario` |
| include | Radicar CM | Adjuntar el PDF único de paz y salvos (desde T12.31; antes 6 anexos) | `CancelacionMatriculaCUImplAdaptador.validarAnexos` |
| include | Radicar ES | Adjuntar anexos obligatorios (según causa) | `ExamenSupletorioCUImplAdaptador.validarAnexos` |
| extend | Adjuntar soporte libre | Radicar CM y CA | tipo de anexo nulo; prohibido en ES |
| extend | Registrar cruce de exámenes | Radicar ES | solo si `tipoCausa = cruce` |
| include | Consultar detalle | Consultar historial | `forkJoin` en el frontend |
| extend | Descargar anexo, Descargar Resolución | Consultar detalle | `puedeDescargarResolucion`; en CA después del Decano solo desde que se envía la comunicación programada (P58) |
| (fuera del sistema) | Recibir correo de la respuesta | Estudiante | el estudiante no actúa: recibe el correo ([correo-electronico.md](correo-electronico.md)) |
| include | Subir comprobante de pago | Adjuntar comprobante de pago | primero `POST .../anexos`, luego `estudiante/comprobante` |

### 1.3 CU-02 Funcionario Académico [EXTENSIÓN]
Casos: iniciar sesión; consultar solicitudes en trámite y respondidas (`GET solicitudes-academicas/funcionario`, filtrado en el cliente); detalle e historial; descargar anexo y Resolución (en cualquier etapa); rechazar solicitud (3 procesos; sin Resolución desde T12.31); subir escaneo de la Resolución (solo CM y CA, en la respuesta después del Decano; en CA la sin firmar por el estudiante); adjuntar documento adicional (opcional, solo CM y ES); remitir al Decano (en CA aunque ninguna asignatura cumpla, desde T12.35); evaluar asignaturas (faltas, nota y situaciones opcionales, de listas cerradas; en CA además `cumpleCondiciones`); enviar respuesta (en CM y CA exige escaneo; en CA además programa la comunicación de la Resolución y puede enviar un mensaje inmediato); gestionar la notificación de la Resolución (solo CA: reprogramar, registrar la firma del estudiante, reintentar); ajustar situación al cancelar (solo CA, `ajustes`, opcionales); enviar recibo de pago y adjuntar recibo (solo ES); aprobar comprobante con profesor obligatorio y observación opcional para el estudiante (desde T12.28; ya no se acuerda fecha); rechazar comprobante (observación obligatoria); consultar estudiantes, asignaturas y funcionarios (solo lectura); consultar procesos asignados (`GET funcionarios-academicos/{uuid}`); cambiar contraseña. Restricción: solo actúa sobre los tipos de solicitud que tiene asignados (`ValidadorActorSolicitud.validarAlcance`).

| Relación | Origen | Destino |
|---|---|---|
| include | Enviar respuesta | Subir escaneo (CM y CA) |
| include | Enviar respuesta (CA después del Decano) | Programar comunicación de la Resolución |
| extend | Enviar mensaje inmediato | Enviar respuesta (CA) |
| extend | Adjuntar documento adicional | Rechazar solicitud, Enviar respuesta, Aprobar o rechazar comprobante (CM y ES) |
| include | Remitir al Decano | Evaluar asignaturas (CM y CA) |
| extend | Ajustar situación al cancelar | Enviar respuesta (CA, opcional desde T12.28) |
| include | Enviar recibo | Adjuntar recibo |
| include | Elegir profesor (buscador de docentes) | Aprobar comprobante (desde T12.28; reemplaza "Acordar fecha del examen") |
| include / extend | Detalle incluye historial; anexo y Resolución extienden detalle | |

### 1.4 CU-03 Decano, procesos académicos [EXTENSIÓN]
Casos: iniciar sesión; consultar solicitudes remitidas (`GET .../decano`, no ve `RADICADA`); detalle, historial, descargar anexo y Resolución; aprobar CM (desde T12.28 sin registrar situaciones); rechazar solicitud; aprobar CA decidiendo asignatura por asignatura (desde T12.35 el frontend ya no usa "aceptar la evaluación"; si rechaza todas se usa rechazar la solicitud); aprobar ES; en los tres procesos un solo botón "Enviar decisión"; cambiar contraseña; administrar catálogos académicos (CU-04); consultar y enviar solicitudes del Consejo (CU-05, rutas `decano/solicitudes-consejo` y `usuario-fiet`). Fuente: `TramiteCancelacion*CUImplAdaptador.aprobarPorDecano`.

### 1.4b Docente y DARCA [EXTENSIÓN]
Docente (rol `Docente` de Julián): consultar sus notificaciones de exámenes supletorios, ver el detalle de solo lectura (se marca como leída) y descargar anexos (`notificaciones-docente`, T12.28); recibe un correo cuando se aprueba el pago. DARCA no usa la aplicación: es un actor externo que recibe los correos de la respuesta final con el PDF del radicado. La tarea programada (`TareaNotificacionResolucion`) puede dibujarse como actor de sistema que envía la comunicación de la Resolución.

### 1.5 CU-04 Administración académica (Decano y Secretario General) [EXTENSIÓN]
Casos (cuatro pestañas de `AdminAcademicaContentComponent`): asignaturas (crear con código generado `PIS-294`, con la sigla del programa; las anteriores a T12.34 conservan `ISI-001`, editar solo el nombre, cargar Excel, consultar); estudiantes (crear con materias, editar, agregar asignatura matriculada, cambiar estado de matrícula, cargar Excel, consultar); funcionarios académicos (crear, editar, cargar Excel, consultar); asignar responsable por proceso (`PUT catalogos-academicos/tipos-solicitud/{uuid}/funcionario`); consultar catálogos. Include: crear estudiante y funcionario incluyen "crear usuario" (`UsuarioCU.crearUsuario`, reutilizado de Julián); las tres cargas Excel incluyen "validar archivo Excel" (`ValidadorPeticionesExcel`).

### 1.6 CU-05 Consejo de Facultad [BASE]
Casos y actores (reglas efectivas, documento 03): gestionar usuarios (SG, DEC) con cargar Excel como extend; gestionar roles (SG, DEC); consultar historial de acciones `logs` (SG, DEC); configurar tipos de solicitud (SG, DEC) con cargar Excel como extend e include definir anexos; gestionar órdenes del día (SG, DEC) con exportar Word (3 variantes) y descargar anexos ZIP como extend; gestionar solicitudes (pantalla del SG); registrar respuesta (SG, DEC, FUN) con cargar archivo de respuesta (PDF o DOCX) como extend; consultar respuestas (SG, FUN, Secretaria FIET solo lectura); enviar solicitud al Consejo (Usuario FIET, incluye consultar tipos de solicitud); enviar solicitud pública (Funcionario de Julián en nombre del solicitante público, `POST solicitudes/public` es pública); consultar tipos disponibles (público y Usuario FIET); cambiar contraseña (cualquier autenticado).

## 2. Clases de dominio (CL-01 a CL-08, ER-01)

Todas las clases y atributos están en los bloques CL de `13-diagramas-fuente.md`. Resumen de lo que se debe dibujar:

### 2.1 Herencia y estereotipos
- `UsuarioLiviano` (uuidUsuario, nombres, apellidos, estado: Boolean) <- `Usuario` <- `Funcionario` [BASE]. En JPA la jerarquía es JOINED: `usuariosLivianos`, `usuarios`, `funcionarios`.
- `Estudiante` y `FuncionarioAcademico` no heredan de `Usuario`: lo referencian (`usuario`) y comparten clave (`@MapsId`, `Usuario_uuid`). Igual las especializaciones de solicitud con `SolicitudAcademica`.
- Enumeraciones: `AccionEtapa` (10), `ResponsableEtapa` (4), `RolEtiquetaEtapa` (3), `CausaSupletorio` (CRUCE, OTRA), `TipoProcesoAcademico` (CM, CA, ES con nombre y código).
- `Usuario.crearInstancia` solo distingue `FUNCIONARIO`: Estudiante y Funcionario Académico generan un `Usuario` simple (documento 04, 11.1).

### 2.2 Relaciones y multiplicidades (fuente: documento 05, secciones 5 y 7)
| Relación | Tipo | Multiplicidad |
|---|---|---|
| TipoUsuario - Usuario | asociación bidireccional | 1 a 0..* |
| Usuario - Rol | asociación (tabla `Usuario_has_Roles`) | N a M, EAGER |
| Usuario - Log | asociación | 1 a 0..* |
| Estudiante - Usuario, FuncionarioAcademico - Usuario | asociación 1 a 1 con clave compartida | 1 a 1 |
| Estudiante - ProgramaAcademico; Asignatura - ProgramaAcademico | asociación | 0..* a 1 |
| Estudiante - AsignaturaMatriculada | composición (`cascade=ALL`) | 1 a 0..* |
| AsignaturaMatriculada - Asignatura | asociación | 0..* a 1 |
| TipoSolicitudAcademica - FuncionarioAcademico | asociación (la colección inversa existe en el modelo) | 0..* a 1 |
| TipoSolicitudAcademica - EtapaSolicitudAcademica | asociación opcional (NULL = etapa común) | 0..1 a 0..* |
| EtapaSolicitudAcademica - EtapaEtiquetaRol | composición (PK compuesta etapa+rol) | 1 a 0..3 |
| TipoSolicitudAcademica - TipoAnexoAcademico | asociación | 1 a 0..* |
| SolicitudAcademica - Estudiante, TipoSolicitudAcademica, EtapaSolicitudAcademica | asociación | 0..* a 1 cada una |
| SolicitudAcademica - SolicitudCancelacionMatricula, SolicitudCancelacionAsignatura, SolicitudExamenSupletorio, ResolucionAcademica | asociación con clave compartida | 1 a 0..1 |
| Solicitud de CM o CA - AsignaturaSolicitudAcademica | composición en el dominio (en la base la FK está en la fila hija) | 1 a 1..* |
| SolicitudAcademica - AnexoAcademico, HistorialSolicitudAcademica | asociación (FK hija) | 1 a 0..* |
| AnexoAcademico - TipoAnexoAcademico | asociación (FK nullable) | 0..* a 0..1 |
| SolicitudExamenSupletorio - CruceSupletorio | composición | 1 a 0..1 |
| AsignaturaSolicitudAcademica - SituacionAcademicaAsignatura | dos asociaciones (matrícula, al cancelar) | 0..* a 0..1 cada una |
| [BASE] TipoSolicitud - TipoAnexo; Solicitud - Anexo; Solicitud - InformacionSolicitante | composición (`cascade=ALL`) | 1 a 0..*; 1 a 0..1 |
| [BASE] Solicitud - Respuesta | asociación | 1 a 0..1 |
| [BASE] Solicitud - OrdenDelDia; Solicitud y TipoSolicitud - Funcionario | asociación | 0..* a 0..1 |

Observaciones: `Anexo` de Julián tiene un campo `MultipartFile` en el dominio (única fuga de Spring en un modelo, documento 02, 5.3). `InformacionSolicitante` es `@OneToOne` en JPA pero la unicidad en base es NO VERIFICADO. ER-01 recoge las 19 tablas de extensión con claves; el ER completo de 33 tablas está en el documento 05, sección 10.

### 2.3 Servicios de dominio (CL-06)
`MaquinaEtapas` (6 métodos públicos, tabla de 13 transiciones y 8 responsables, bean registrado), `TramiteSolicitud`, `TramiteCancelacion` (crea `TramiteSolicitud`, que crea `ValidadorActorSolicitud`), `ValidadorArchivoAdjunto` (máx. 5 MB, extensión y firma), `DatosPersonales`, `ReglasAsignatura` (estática). Sin Spring; se crean con `new`.

### 2.4 Patrón hexagonal (CL-08)
Controlador -> `*CUIntPuerto` <- `*CUImplAdaptador` -> `*GatewayIntPuerto` <- `*GatewayImplAdaptador` -> `*Repositorio` y `*OwnMapper` -> `*Entidad`; `BeanConfiguracion` instancia los 30 casos de uso (31 beans con `MaquinaEtapas`). 30 puertos de entrada, 30 de salida, una implementación cada uno (documento 02).

## 3. Componentes y paquetes (PK-01, C4-01 a C4-05)

### 3.1 C4-01 Contexto
Personas: Estudiante, Funcionario Académico, Decano, Secretario General, usuarios del Consejo [BASE]. Sistema: Gestión de Procesos Académicos FIET. Sistema/proceso externo: la Resolución física firmada a mano (original en papel, tres copias; solo el escaneo en PDF entra al sistema). No hay integración con otros sistemas institucionales; la referencia a SIMCA en el frontend (`NOTA_SIMCA`) es un texto informativo, su alcance es NO VERIFICADO.

### 3.2 C4-02 Contenedores
| Contenedor | Tecnología | Puerto | Evidencia |
|---|---|---|---|
| Frontend | Angular 20.2, PrimeNG 20.1, SSR con Express 5 en Node 20 | 4200 | `frontend/package.json`, `frontend/dockerfile` |
| Backend | Spring Boot 3.5.4, Java 17, JPA, Spring Security, jjwt 0.11.2, Apache POI 5.2.5 | 8080, prefijo `/api/unicauca/fiet/consejo` | `backend/solicitudes/pom.xml`, `application.properties` |
| Base de datos | MySQL 8.0.33, esquema `cfiet`, 35 tablas (33 al corte) | 3306 (host 3307) | `docker-compose.yml`, documento 05 |
| Archivos | carpeta `/app/uploads` (`UPLOADS_PATH`) | | `application.properties` |
| Servidor de correo (externo) | SMTP con STARTTLS en el puerto 587; en desarrollo una cuenta de Gmail de prueba | 587 | `application.properties` (`spring.mail.*`), [correo-electronico.md](correo-electronico.md) |
Enlaces: el backend envía correo al servidor SMTP (estudiante, DARCA, profesor); el navegador llama al backend directamente (`environment.apiUrl = http://localhost:8080/...`, el servidor SSR no hace de proxy: deducido, NO VERIFICADO en ejecución); backend a MySQL por JDBC; backend a disco con `AlmacenadorArchivos`.

### 3.3 PK-01 y C4-03 Paquetes y capas del backend
Raíz `com.unicauca.cfiet.solicitudes` (cifras al 2026-10-21, documento 02): `aplicacion` (input 34, output 36), `dominio` (casosdeuso 34, modelos 74, servicios 9, helper 5), `infraestructura` (input 140: controladores, DTO, mapeadores y `programadorNotificaciones`; output 156: persistencia, excepciones, JWT, almacenamiento, correo y pdf; configuracion 28: `BeanConfiguracion`, seguridad, `lectorArchivos`). Dirección permitida: infraestructura -> aplicacion/dominio; dominio -> aplicacion. Desviaciones verificadas: el dominio importa `MensajesError` en 33 archivos (8 BASE, 25 EXTENSIÓN); 4 archivos [BASE] importan Spring (`@Value`, `MultipartFile`); 0 violaciones de JPA (documento 02, 5.3).

### 3.4 C4-04 Módulos de la extensión
- Administración: 4 controladores (Asignatura, Estudiante, FuncionarioAcademico, CatalogoAcademico) con 9 casos de uso; crear usuarios reutiliza `UsuarioCU` y todos escriben `LogCU` [BASE].
- Motor común: `SolicitudAcademicaRestController` -> `ConsultaSolicitudAcademicaCU`, `AnexoAcademicoCU`, `ResolucionAcademicaCU`; `SolicitudAcademicaCU` (crear y cambiar etapa) y `MaquinaEtapas`.
- Notificaciones (después del corte): `NotificacionResolucionCU` con `TareaNotificacionResolucion`, `NotificadorRespuestaFinal`, `ComunicacionResolucion`, `CorreoIntPuerto` y `GeneradorPdfRespuestaIntPuerto` (correo y PDF); `DocenteCU` y `NotificacionDocenteCU` con `NotificacionDocenteRestController`.
- Procesos: por proceso un controlador y tres casos de uso (radicación, trámite, consulta); radicaciones y trámites dependen de `SolicitudAcademicaCU` (y las radicaciones de `AnexoAcademicoCU`); consultas de `ConsultaSolicitudAcademicaCU`.

### 3.5 C4-05 Frontend
Navegación (`app.routes.ts`: 37 rutas, con las 2 del Docente; `AuthGuard`, `RoleGuard`, `inicioGuard`; `authInterceptor`, `SesionVencidaService`; `AuthService`), marco (`LayoutComponent`, `LayoutSimpleComponent`, `PageComponent`), páginas por rol en `core/` (composición página -> envoltorio -> contenido), `shared` (bandeja y detalle comunes, tres componentes de proceso cargados por `COMPONENTES_PROCESO`, genéricos), datos (17 servicios, 101 modelos, utils y constantes). Sin librería de estado (documento 07, sección 8).

## 4. Despliegue (DP-01)
Elementos verificados en `backend/docker-compose.yml` (versión '3'), `backend/Dockerfile` y `frontend/dockerfile`:
- Servicio `solicitudes-consejo-de-facultad`: `build: .` (imagen `eclipse-temurin:17-jdk-jammy` con `app_cfiet.jar` precompilado), puerto `8080:8080`, `mem_limit: 512m`, `restart: always`, `depends_on` con `service_healthy`, variables `SERVER_PORT`, `DB_URL` (`jdbc:mysql://cfiet_database:3306/cfiet?createDatabaseIfNotExist=true&serverTimezone=UTC`), `DB_USER_NAME`, `DB_PASSWORD` (valor omitido), `UPLOADS_PATH=/app/uploads`.
- Al 2026-10-21 el backend recibe además `JWT_SECRET`, `MAIL_USER`, `MAIL_PASSWORD` y `CORREO_DARCA` (desde `backend/.env` o el entorno) y se conecta a un servidor SMTP externo.
- Servicio `cfiet_database`: `mysql:8.0.33`, puerto `3307:3306`, `restart: always`, healthcheck `mysqladmin ping` (timeout 10 s, 10 reintentos), `MYSQL_DATABASE=cfiet_database` (la base efectiva es `cfiet`).
- Volúmenes: bind mount `./uploads:/app/uploads` (backend); MySQL sin volumen declarado (volumen anónimo de la imagen). Redes: ninguna declarada; Compose crea la red por defecto del proyecto (comportamiento estándar, no verificado en ejecución).
- Frontend: fuera del compose; `npm start` en 4200, o imagen `node:20-alpine` multietapa con `EXPOSE 4200` y `node dist/front-fiet-sc/server/server.mjs`.
- Docker no estaba corriendo en el análisis: nada se ejecutó.

## 5. Secuencias (SQ-01 a SQ-10)
Participantes y pasos clave (detalle completo en el bloque Mermaid; mensajes verificados contra el documento 04 y, para los trámites, contra el código de `Tramite*CUImplAdaptador`):

| ID | Flujo | Participantes principales | Puntos a destacar |
|---|---|---|---|
| SQ-01 | Login y primera petición autenticada [BASE] | LoginForm, AuthService, authInterceptor, JwtFiltroAutenticacion, SesionRestController, SesionCU, SesionGateway, AuthenticationManager, JwtServicio, LogCU, UsuarioRestController | error 5 si no existe; roles leídos de BD en cada petición; log de sesión; rutaDeInicio |
| SQ-02 | Radicar Cancelación de Matrícula | Estudiante, servicio, controlador, ConsultaCU, CancelacionMatriculaCU, SolicitudAcademicaCU, MaquinaEtapas, AnexoAcademicoCU, Almacenamiento, gateways, LogCU | validar antes de escribir; una transacción; radicado AAAA-CM-0001; 2 + N logs. Radicar CA es igual salvo: sin anexos obligatorios, lista de asignaturas elegidas y solo soportes libres |
| SQ-03 | Radicar Examen Supletorio | ... ExamenSupletorioCU, Clock Bogotá | plazo de 3 días hábiles lunes a viernes sin festivos; cruce condicional |
| SQ-04 | Funcionario remite CM | TramiteCancelacionMatriculaCU, TramiteCancelacion, MaquinaEtapas, SolicitudAcademicaCU | orden real: validar, cambiarEtapa, guardarAsignaturas, log; retira escaneo previo |
| SQ-05 | Decano aprueba CA parcialmente | TramiteCancelacionAsignaturaCU | desde T12.35 el frontend envía siempre `decisiones` (un solo botón); al menos una aprobada o se usa rechazar; rechazadas pierden situación |
| SQ-06 | Enviar respuesta con Resolución y descarga | SolicitudAcademicaRestController, ResolucionAcademicaCU, TramiteCM | dos llamadas: escaneo y luego responder; cancelarActivas solo en APROBADA; al confirmar, correo al estudiante y a DARCA (en segundo plano) |
| SQ-06b | Comunicación programada de la Resolución (CA) | TramiteCancelacionAsignaturaCU, NotificacionResolucionCU, TareaNotificacionResolucion, NotificadorRespuestaFinal, GeneradorPdfRespuesta, ServicioCorreo | programar en la respuesta; la tarea reclama con `UPDATE` condicional, envía al estudiante y a DARCA y libera la respuesta. Diagrama en [correo-electronico.md](correo-electronico.md) |
| SQ-07 | Pago del supletorio | AnexoAcademicoCU, TramiteExamenSupletorioCU, DocenteCU | archivo antes (`POST anexos`), transición después; aprobar con profesor (crea `NOTIFICACION_DOCENTE` y correo al profesor) o rechazar |
| SQ-08 | Crear estudiante | EstudianteCU, UsuarioCU, PasswordEncoder | reutiliza `crearUsuario`; facultad FIET; matrículas `activa` |
| SQ-09 | Carga masiva de estudiantes | EstudianteRestController, ProcesadorArchivos, CatalogoAcademicoExcel, ValidadorPeticionesExcel, AgrupadorEstudiantesExcel | 400 con mapa de errores por fila; agrupa filas por estudiante (verificado en `EstudianteRestController.crearEstudiantes`) |
| SQ-10 | Detalle por rol | ConsultaSolicitudAcademicaCU, MaquinaEtapas | `acceso()`; sin etiqueta equivale a inexistente; enmascarado para Estudiante |

## 6. Actividades y BPMN (AC-01 a AC-04)
Calles: Estudiante, Funcionario Académico, Decano, Sistema.
- AC-01 CM (actualizado al 2026-10-21): inicio; abrir formulario y normativa; motivo y el PDF único de paz y salvos; radicar; decisión de validaciones (si falla vuelve); RADICADA; Funcionario decide procede; no: rechazo sin Resolución, RECHAZADA, correo al estudiante y a DARCA; sí: evaluar asignaturas, remitir, EN_REVISION_DECANO; Decano aprueba (sin situaciones desde T12.28) o rechaza; Funcionario sube escaneo y envía respuesta; el Sistema cancela las matrículas activas si es aprobación; el Estudiante descarga la Resolución; fin.
- AC-02 CA: igual con asignaturas elegidas y soportes libres; el Funcionario marca `cumpleCondiciones` (desde T12.35 puede remitir aunque ninguna cumpla); el Decano decide por asignatura con un solo botón (decisión P14, P62); regla de al menos una aprobada, si no solo cabe rechazar la solicitud; ajustes de situación; solo se cancelan las aprobadas.
- AC-03 ES: radicar con causa y plazo; el Funcionario rechaza (sin Resolución) o remite; el Decano decide; rechazo: enviar respuesta; aprobación: recibo, `PENDIENTE_PAGO`, comprobante, `EN_VERIFICACION_PAGO`; aprobar (profesor obligatorio, notificación y correo al profesor; sin fecha acordada desde T12.28) o rechazar (el Estudiante debe radicar de nuevo).
- AC-04: cadena de validaciones de la radicación del supletorio, orden de la sección 9.1 del documento 04.
Limitación: el sistema no valida fechas de periodo ni vigencia de paz y salvo en CM y CA (documento 04, D4 y D5).

## 7. Estados (ST-01 a ST-06)
- ST-01 etapas de CM y CA: `RADICADA` -> `RECHAZADA` (RECHAZAR_FUNCIONARIO, observación; sin escaneo desde T12.31), `RADICADA` -> `EN_REVISION_DECANO` -> `APROBADA_POR_DECANO` o `RECHAZADA_POR_DECANO` -> `APROBADA` o `RECHAZADA` (ENVIAR_RESPUESTA con escaneo). Finales: `APROBADA`, `RECHAZADA`. Fuente: tabla `TRANSICIONES` en `<J>/dominio/servicios/MaquinaEtapas.java` (documento 04, sección 10).
- ST-02 etapas de ES: las anteriores sin escaneo, más `APROBADA_POR_DECANO` -> `PENDIENTE_PAGO` (exige recibo) -> `EN_VERIFICACION_PAGO` (exige comprobante) -> `APROBADA` o `RECHAZADA` (observación); `RECHAZADA_POR_DECANO` -> `RECHAZADA`. Responsables: `PENDIENTE_PAGO` Estudiante, `EN_VERIFICACION_PAGO` Funcionario.
- ST-03 asignatura matriculada: `activa` -> `cancelada` (al aprobarse una cancelación), `aprobada`, `perdida` (cambio manual); solo se sale de `activa` (`EstudianteCU.cambiarEstadoAsignatura`).
- ST-04 escaneo de la Resolución: sin escaneo, con escaneo (reemplazable), retirado por REMITIR_DECANO, entregado al finalizar; en CA, sin firmar por el estudiante hasta que se reemplaza por la firmada (P59).
- ST-07 (nuevo) comunicación programada de la Resolución: PROGRAMADA -> EN_PROCESO -> ENVIADA, o de vuelta a PROGRAMADA tras un error y FALLIDA en el tercer intento; FALLIDA -> PROGRAMADA con el reintento manual. Diagrama en [correo-electronico.md](correo-electronico.md).
- ST-05 decisión por asignatura en CA: sin evaluar, cumple o no cumple, aprobada o rechazada por el Decano, cancelada si aprobada.
- ST-06 sesión del frontend: sin sesión, autenticada, vencida (401 distinto del login), cierre. No hay verificación local de `exp` (documento 07, sección 7).

## 8. Pendientes y puntos NO VERIFICADO relevantes
- Orden exacto de los logs frente a `cambiarEtapa` en radicaciones: verificado en trámites (SQ-04, SQ-05); para el resto se sigue el documento 04.
- Frontend llama directamente al backend desde el navegador: deducido de `environment.ts`.
- Nombres exactos de métodos de lectura de los servicios [BASE] del frontend: NO VERIFICADO (documento 07, 6.2).
- Los diagramas no se renderizaron; el código solo se revisó por lectura de sintaxis.
- El análisis original no modificó `change.md` (ver `SUPUESTOS.md`); la actualización T12.37 sí lo registra.

## 9. Índice de identificadores
CU-01 a CU-05; CL-01 usuarios, CL-02 estructura académica, CL-03 núcleo de la solicitud, CL-04 especializaciones, CL-05 lectura y formularios, CL-06 comandos y servicios, CL-07 Consejo, CL-08 patrón hexagonal; ER-01; PK-01; C4-01 a C4-05; DP-01; SQ-01 a SQ-10; AC-01 a AC-04; ST-01 a ST-06. Total 41.
