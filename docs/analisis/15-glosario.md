# 15 - Glosario

## Dominio

| Término | Significado |
|---|---|
| FIET | Facultad de Ingeniería Electrónica y Telecomunicaciones, Universidad del Cauca |
| Consejo de Facultad | Órgano ante el cual se presentan las solicitudes del sistema base de Julián |
| Solicitud | [BASE] Petición dirigida al Consejo, de un tipo de solicitud, con anexos |
| Tipo de solicitud | [BASE] Catálogo de clases de solicitud con sus anexos soportados |
| Orden del Día | [BASE] Documento (exportable a .docx) con las solicitudes que el Consejo tratará |
| Respuesta | [BASE] Decisión del Consejo sobre una solicitud, con archivo |
| Consecutivo | Numeración correlativa; NO VERIFICADO como entidad propia (ver 05) |
| Historial / Log | [BASE] Registro de acciones de usuarios (`LogEntidad`) |
| Solicitud académica | [EXTENSIÓN] Solicitud de uno de los tres procesos nuevos (`SOLICITUD_ACADEMICA`) |
| Cancelación de Matrícula | [EXTENSIÓN] Proceso de retiro del estudiante del semestre; desde T12.31 un solo anexo obligatorio, un PDF con todos los paz y salvos (antes seis anexos) |
| Cancelación de Asignatura | [EXTENSIÓN] Proceso de cancelar una o varias asignaturas; el Decano aprueba o rechaza cada una, y la Resolución se comunica al estudiante en una fecha programada |
| Examen Supletorio | [EXTENSIÓN] Examen que reemplaza uno no presentado; plazo de tres días hábiles, sin Resolución |
| Estudiante | [EXTENSIÓN] Actor que radica solicitudes académicas |
| Funcionario Académico | [EXTENSIÓN] Técnico Administrativo de Procesos Académicos; revisa y remite al Decano |
| Decano | Máxima autoridad FIET; aprueba o rechaza con un solo botón, "Enviar decisión" |
| Docente | [BASE] rol de Julián; en la extensión, el profesor que el Funcionario asigna al aprobar el pago del supletorio y que recibe una notificación interna y un correo |
| DARCA | Dependencia de la Universidad que recibe por correo la respuesta final de cada solicitud académica con un PDF generado. Las fuentes del repositorio solo usan la sigla; su nombre completo no se documenta aquí |
| Secretario General | [BASE] Rol administrativo que crea usuarios |
| Radicar | Presentar formalmente la solicitud (acción `RADICAR`) |
| Etapa | Estado de una solicitud académica en su proceso (por ejemplo RADICADA, EN_REVISION_DECANO) |
| Etapa etiqueta por rol | Texto que cada rol ve para una misma etapa (`EtapaEtiquetaRol`) |
| Resolución | Documento físico firmado por el Decano; se sube su copia escaneada. Solo en las cancelaciones y solo cuando la solicitud pasó por el Decano. En Cancelación de Asignatura se sube sin firmar por el estudiante y se reemplaza por la firmada si el estudiante firma antes de la fecha programada |
| Comunicación programada de la Resolución | [EXTENSIÓN] Correo al estudiante y a DARCA que una tarea programada envía en la fecha y hora que elige el Funcionario (Cancelación de Asignatura, `NOTIFICACION_RESOLUCION`) |
| Visibilidad diferida | [EXTENSIÓN] Mientras no se envía la comunicación programada, el estudiante ve su Cancelación de Asignatura "En trámite", sin respuesta ni Resolución |
| Documento adicional de la respuesta | [EXTENSIÓN] PDF opcional que el Funcionario adjunta al cerrar la solicitud y que va en los correos; solo en Matrícula y Supletorio |
| PDF del radicado | [EXTENSIÓN] Resumen de la respuesta (`Radicado-<radicado>.pdf`) que genera el sistema con OpenPDF para los correos |
| Anexo académico | Archivo adjunto a una solicitud académica, por tipo obligatorio |
| Programa académico | Uno de cinco fijos; siglas desde T12.34: PIET, PIS, PIAI, TTM y PIAC (antes IET, ISI, IAI, TTE, IAC) |
| Asignatura matriculada | Asignatura inscrita por un estudiante, con estado y notas |
| Situación académica | Dos listas cerradas y opcionales en las cancelaciones: situación de la asignatura (cursada por primera vez, recursada por primera vez, cursada por segunda vez, cursada por tercera vez) y situación al cancelar (No vista, Perdida). Faltas y nota también son opcionales |
| Paz y salvo | Constancia exigida en Cancelación de Matrícula, con vigencia que revisa el Funcionario; desde T12.31 todos van en un solo PDF de hasta 10 MB |
| Aprobación parcial | Decisión por asignatura en Cancelación de Asignatura (decisión P14) |
| Cruce | Causa de supletorio por coincidencia de horarios (`tipoCausa='cruce'`) |
| Semiautomatización | Término oficial del proyecto; nunca "automatización" |

## Técnicos

| Término | Significado |
|---|---|
| Arquitectura hexagonal | Puertos y adaptadores: dominio en el centro, infraestructura alrededor |
| Puerto de entrada | Interfaz `*CUIntPuerto` que expone un caso de uso |
| Puerto de salida | Interfaz `*GatewayIntPuerto` hacia persistencia u otros servicios |
| Caso de uso | Clase `*CUImplAdaptador` que implementa un puerto de entrada |
| Gateway | Adaptador `*GatewayImplAdaptador` que implementa un puerto de salida con repositorios JPA |
| Entidad | Clase `*Entidad` mapeada a tabla con JPA |
| Modelo de dominio | Clase del dominio sin anotaciones JPA |
| DTOPeticion / DTORespuesta | Objetos de transferencia de entrada y salida de la API |
| OwnMapper | Mapeador propio entre entidad y modelo de dominio |
| MaquinaEtapas | Servicio de dominio con la tabla de transiciones por proceso, rol y condición |
| MensajesError / CodigoError | Textos y códigos de las excepciones de negocio |
| JWT | Token firmado HS256, vigencia 24 h, enviado como `Authorization: Bearer`; clave por `JWT_SECRET` |
| SMTP | Protocolo de envío de correo; el backend usa Spring Mail (`JavaMailSender`) |
| `@Async` / `@Scheduled` | Anotaciones de Spring para ejecutar en segundo plano (envío de correo) y con un intervalo fijo (tarea de la comunicación programada) |
| ConfiguracionSeguridad | Reglas `requestMatchers` por URL y rol |
| ProcesadorArchivos<T> | Interfaz de lectura de Excel con una implementación por tipo |
| BeanConfiguracion | Clase que declara los `@Bean` de los casos de uso |
| ddl-auto=update | Hibernate crea o ajusta tablas al arrancar |
| Karma / Jasmine | Ejecutor y marco de pruebas del frontend |
| PrimeNG | Biblioteca de componentes de UI de Angular |
| Guard | Protección de rutas Angular (`AuthGuard`, `RoleGuard`, `inicioGuard`) |
| Interceptor | Agrega el token a las peticiones HTTP |
| SSR | Renderizado en servidor con Angular y Express |
| [BASE] / [EXTENSIÓN] | Etiquetas de origen: prototipo de Julián o este trabajo |
