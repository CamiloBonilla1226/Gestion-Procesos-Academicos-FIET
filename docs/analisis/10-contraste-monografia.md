# 10 - Contraste con la monografía de Julián

Raíz de rutas: `B = backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes`. Estados: CONFIRMADO, DIVERGENTE, NO ENCONTRADO, PARCIAL. Las afirmaciones provienen de `docs/PROMPT_ANALISIS.md`; la monografía misma no estaba disponible.

## Tabla de afirmaciones

| # | Afirmación | Estado | Evidencia | Comentario |
|---|---|---|---|---|
| 1 | Capas aplicación (input/output), dominio (casosdeuso, helper, modelos) e infraestructura (configuracion, input, output) | CONFIRMADO | `B/aplicacion/{input,output}`, `B/dominio/{casosdeuso,helper,modelos,servicios}`, `B/infraestructura/{configuracion,input,output}` | El dominio tiene además `servicios` ([EXTENSIÓN]: `MaquinaEtapas`, `ReglasAsignatura`, `TramiteCancelacion`). Ver 02 para el árbol completo y violaciones de dependencia. |
| 2 | Mapeador propio con interfaz de contrato que sustituyó a ModelMapper | PARCIAL | `B/infraestructura/output/persistencia/mapeador/` contiene `*OwnMapper` y `Mapper.java`; `B/infraestructura/input/*/mapeador/Mapper*InfraestructuraDominio.java` usan `org.modelmapper.ModelMapper` | Persistencia usa mapeadores propios; los mapeadores de la capa de entrada (DTO a dominio) siguen con ModelMapper inyectado con `@Qualifier("mapeadorSimple")` (p. ej. `MapperUsuarioInfraestructuraDominio` líneas 12 y 26). |
| 3 | Eliminaron @OneToMany/@ManyToOne redundantes | PARCIAL | Solo 5 `@OneToMany` en todo el main: `EstudianteEntidad:45` (cascade ALL, EAGER), `SolicitudEntidad:33`, `TipoSolicitudEntidad:30`, `TipoUsuarioEntidad:23` (LAZY); 48 anotaciones de relación en total | Quedan relaciones bidireccionales; `EstudianteEntidad` [EXTENSIÓN] usa EAGER con cascade ALL. |
| 4 | JWT con vigencia de 5 horas | DIVERGENTE | `B/infraestructura/configuracion/seguridad/jwt/JwtServicio.java:36` `24 * 60 * 60 * 1000` | La vigencia es de 24 horas. Firma HS256; al corte la clave estaba fija en la clase, y desde T12.21 sale de la variable `JWT_SECRET` (con un valor por defecto local). |
| 5 | JWT firmado con secreto y roles dentro del token | PARCIAL | `JwtServicio.getToken(Map extraClaim, UserDetails)` | Firma confirmada. Los roles solo entran si el llamador los pasa en `extraClaim`; ver 06 para los claims reales. |
| 6 | Filtro `JwtFiltroAutenticacion`, respuesta de error sin permiso, usuarios deshabilitables | CONFIRMADO | `B/infraestructura/configuracion/seguridad/jwt/JwtFiltroAutenticacion.java`, `.../configuracion/CustomAccessDeniedHandler.java` (403), `CustomAuthenticationEntryPoint.java` (401) | Habilitación de usuarios: ver 06. |
| 7 | Patrón Controller | CONFIRMADO | 15 clases `*RestController` en `B/infraestructura/input/*/controlador/` | |
| 8 | Patrón Repository: `UsuarioRepositorio` extiende `JpaRepository` | CONFIRMADO | `B/infraestructura/output/persistencia/repositorios/UsuarioRepositorio.java:17` `extends JpaRepository<UsuarioEntidad, String>` | 29 archivos en el paquete de repositorios. |
| 9 | Inyección de dependencias por constructor | CONFIRMADO | `UsuarioCUImplAdaptador` campos `final` (línea 24 `private final UsuarioGatewayIntPuerto gateway`) | `UsuarioCUImplAdaptador` no lleva anotaciones de Spring ni Lombok: es una clase plana con constructor, instanciada desde `BeanConfiguracion` (ver 02). |
| 10 | Facade (gateway de Logs) | NO VERIFICADO | `LogGatewayIntPuerto`, `LogGatewayImplAdaptador` | No hay nomenclatura Facade en el código (grep "Facade" sin resultados); es un adaptador de persistencia. La calificación como Facade es interpretación de la monografía. |
| 11 | DTO (`RespuestaDTORespuesta`) | CONFIRMADO | `B/infraestructura/input/controladorRespuestas/DTORespuesta/RespuestaDTORespuesta.java` | |
| 12 | Singleton vía @Service (`TipoSolicitudExcelService`) | CONFIRMADO | `B/infraestructura/configuracion/lectorArchivos/` contiene `TipoSolicitudExcelService` y `UsuarioExcelService` | Los `@Service` de Spring son singletons por defecto. |
| 13 | SRP en `JwtFiltroAutenticacion` | CONFIRMADO | Solo extrae y valida el token y puebla el contexto de seguridad | Interpretación; la clase también arma la respuesta de error (línea 82 `httpCodigo(status)`). |
| 14 | OCP con `ProcesadorArchivos<T>` | CONFIRMADO | `lectorArchivos/ProcesadorArchivos.java`; implementan `ProcesarArchivoTiposSolicitudesImpl` y `ProcesarArchivoUsuariosImpl` [BASE], y [EXTENSIÓN] `ProcesarArchivoAsignaturasImpl`, `ProcesarArchivoEstudiantesImpl`, `ProcesarArchivoFuncionariosAcademicosImpl` | La extensión demuestra el OCP: se agregaron tres implementaciones sin tocar la interfaz. |
| 15 | LSP: `UsuarioLiviano`, `Usuario`, `Funcionario` | CONFIRMADO | `B/dominio/modelos/Usuario.java:22 extends UsuarioLiviano`, `Funcionario.java:15 extends Usuario` | Herencia presente; el cumplimiento estricto de LSP no se verificó. |
| 16 | ISP en `ExcepcionesFormateadorIntPuerto` | PARCIAL | `B/aplicacion/output/ExcepcionesFormateadorIntPuerto.java` | Es una interfaz de métodos pequeños y cohesivos (`lanzarErrorGenerico`, `lanzarEntidadNoExiste`, ...), pero con muchos métodos de la misma familia. |
| 17 | DIP: casos de uso dependen de `*IntPuerto` | CONFIRMADO | `TipoSolicitudCUImplAdaptador` importa `TipoSolicitudGatewayIntPuerto`; `UsuarioCUImplAdaptador` importa `UsuarioGatewayIntPuerto` y `LogCUIntPuerto` | |
| 18 | MySQL, Hibernate vía Spring Data JPA | CONFIRMADO | `pom.xml` (`mysql-connector-j`, `spring-boot-starter-data-jpa`); `application.properties:18` `spring.jpa.hibernate.ddl-auto=update` | |
| 19 | Entidades: usuarios, roles, solicitudes, tipos, orden del día, respuestas, consecutivos, logs | PARCIAL | `B/infraestructura/output/persistencia/entidades/` (33 archivos) incluye `UsuarioEntidad`, `RolEntidad`, `SolicitudEntidad`, `TipoSolicitudEntidad`, `OrdenDelDiaEntidad`, `RespuestaEntidad`, `LogEntidad` | "Consecutivos": ver 05; NO VERIFICADO si existe como entidad propia. |
| 20 | Errores de negocio como HTTP 500 con JSON `{codigoError, mensaje, httpCodigo, url, metodo}` | CONFIRMADO | `*RestController` devuelven `HttpStatus.INTERNAL_SERVER_ERROR` (p. ej. `AsignaturaRestController.java:151`); `CustomAccessDeniedHandler` usa el campo `httpCodigo` | Detalle del JSON en 06. |
| 21 | Ruta base `/api/unicauca/fiet/consejo` | CONFIRMADO | `application.properties:4` `url.application=/api/unicauca/fiet/consejo/` | Los controladores usan `${url.application}`. |
| 22 | Carga masiva de usuarios y tipos de solicitud por Excel | CONFIRMADO | `ProcesarArchivoUsuariosImpl`, `ProcesarArchivoTiposSolicitudesImpl`, `poi-ooxml` 5.2.5 | La extensión suma carga de estudiantes, asignaturas y funcionarios académicos. |
| 23 | Exportación del Orden del Día en .docx | CONFIRMADO | `B/infraestructura/output/exportador/OrdenDelDiaExportadorImpl.java` (nombre de clase); puerto `OrdenDelDiaExportador` | Ruta: `B/infraestructura/output/exportador/`; puerto en `B/aplicacion/output/OrdenDelDiaExportador.java`. |
| 24 | Subida de anexos PDF con validación de formato | CONFIRMADO | `B/dominio/servicios/ValidadorArchivoAdjunto.java`, `AlmacenadorArchivos` | |
| 25 | Frontend con core, layouts, shared, environments | PARCIAL | `frontend/src/app/{core,layouts,shared}` existen | `environments`: ver 07 (NO VERIFICADO aquí). |
| 26 | `app-page-component` como esqueleto de vistas | CONFIRMADO | `frontend/src/app/shared/pages/page-component/page-component.ts` selector `app-page-component` | Existe también `app-page-simple-component`. |
| 27 | Pruebas solo Postman y UAT | DIVERGENTE | Backend: 38 archivos de test y 615 anotaciones `@Test` al corte (51 archivos, 789 `@Test` y 100 `@ParameterizedTest` al 2026-10-21) en `backend/solicitudes/src/test`; frontend: 136 `.spec.ts` al corte (149 al 2026-10-21) en `frontend/src/app` | Hay tests automatizados reales (JUnit/Mockito, Karma/Jasmine). Postman: no se encontró colección (`find -iname "*postman*"` sin resultados); existen scripts `backend/pruebas/*.ps1`. Detalle en 11. |
| 28 | Dockerfile backend (Java 17, 8080, jar compilado) | CONFIRMADO | `backend/Dockerfile`: `FROM eclipse-temurin:17-jdk-jammy`, `EXPOSE 8080` | |
| 29 | Dockerfile frontend multi-stage, Node alpine, 4200 | CONFIRMADO | `frontend/dockerfile`: dos `FROM node:20-alpine`, `EXPOSE 4200` | |
| 30 | docker-compose con MySQL healthcheck, volumen, depends_on, memoria, restart | PARCIAL | `backend/docker-compose.yml`: `depends_on` (27), `healthcheck` (39), `restart: always` (26, 38), `mem_limit: 512m` (15) | `mem_limit: 512m` solo en el servicio backend; MySQL no tiene límite (ver 11). |
| 31 | Ramas Git Prod y Dev, feature/, hotfix/ | NO ENCONTRADO | `git branch -a`: solo `main` y `origin/main` | Pueden existir en el repositorio de Julián, no en este. |

## Patrones de diseño presentes (con evidencia)

| Patrón | Evidencia |
|---|---|
| Hexagonal (puertos y adaptadores) | `B/aplicacion/input/*CUIntPuerto`, `B/aplicacion/output/*GatewayIntPuerto`, implementaciones `*CUImplAdaptador` y `*GatewayImplAdaptador` |
| Adapter | `B/infraestructura/output/persistencia/gateway/*GatewayImplAdaptador` adapta repositorios JPA a los puertos de salida |
| Mapper | `B/infraestructura/output/persistencia/mapeador/*OwnMapper`; `B/infraestructura/input/*/mapeador/Mapper*InfraestructuraDominio` |
| Builder | Lombok `@Builder` en 133 archivos, p. ej. `B/dominio/modelos/ActorSolicitud.java`, `Anexo.java` |
| State / máquina de estados tabular [EXTENSIÓN] | `B/dominio/servicios/MaquinaEtapas.java` define `Transicion(tipos, origen, accion, destino, rol, ...)` en una tabla de transiciones |
| Strategy (parcial) | `ProcesadorArchivos<T>` con una implementación por tipo de archivo Excel |
| Template Method | NO VERIFICADO; no se identificó clase abstracta con método plantilla |
| Domain Service | `B/dominio/servicios/` (`ReglasAsignatura`, `TramiteCancelacion`, `TramiteSolicitud`, `ValidadorActorSolicitud`) |
| Dependency Injection por constructor | Todos los `*CUImplAdaptador` |
| Repository | `B/infraestructura/output/persistencia/repositorios` (29 archivos) |
| Controller y DTO | `*RestController`, paquetes `DTOPeticion` y `DTORespuesta` |

## Diferencias de estilo entre Julián y la extensión

| Aspecto | Julián [BASE] | Extensión |
|---|---|---|
| Comentarios | Javadoc en casi todo con `@author Julian David Camacho Erazo` (ejemplo `ExcepcionesFormateadorIntPuerto`) | Sin comentarios ni Javadoc por regla de `CLAUDE.md` |
| Servicios de dominio | No existe `dominio/servicios` con reglas | Aparece `MaquinaEtapas`, `ReglasAsignatura`, `TramiteCancelacion` |
| Lógica de etapas | Estados simples de solicitud | Tabla de transiciones por tipo de proceso, rol y condiciones |
| Carga masiva | Usuarios y tipos de solicitud | Estudiantes, asignaturas, funcionarios académicos, con agrupadores y detección de duplicados |
| Modelos | Pocos modelos con Lombok | Más modelos con `@Builder` (133 archivos en total) |
| Pruebas | Sin pruebas en la monografía | 615 `@Test` al corte; 789 `@Test` y 100 parametrizadas al 2026-10-21, más scripts de integración, entre ellos el del correo con un SMTP local |
| Notificación al estudiante | Respuestas en la aplicación (y descarga del archivo de respuesta) | Además, correo electrónico al estudiante, a DARCA y al profesor del supletorio, con PDF generado, y comunicación programada de la Resolución ([correo-electronico.md](correo-electronico.md)) |
| Relaciones JPA | Pocas bidireccionales | Mezcla; `EstudianteEntidad` con cascade ALL y EAGER |
