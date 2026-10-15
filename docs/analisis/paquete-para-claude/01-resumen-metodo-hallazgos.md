

===== 00-RESUMEN-EJECUTIVO.md =====

# 00 - Resumen ejecutivo

## Qué es el sistema

Aplicación web de la FIET (Universidad del Cauca) para gestionar solicitudes académicas. [BASE] es el prototipo de Julián David Camacho Erazo: solicitudes dirigidas al Consejo de Facultad, con usuarios, roles, tipos de solicitud, orden del día, respuestas, historial, carga masiva por Excel y exportación .docx. [EXTENSIÓN] agrega tres procesos: Cancelación de Matrícula, Cancelación de Asignatura y Examen Supletorio, con los actores Estudiante, Funcionario Académico y Decano.

## Arquitectura

Backend Spring Boot 3.5.4 (Java 17) en arquitectura hexagonal: `aplicacion` (30 puertos de entrada y 30 de salida), `dominio` (30 casos de uso, 58 modelos, 7 servicios) e `infraestructura` (15 controladores REST, 25 gateways, 29 repositorios JPA, 32 mapeadores propios, seguridad JWT). Frontend Angular 20 con PrimeNG, organizado por perfil en `core`, más `layouts` y `shared`. Base MySQL 8.0.33 con Hibernate `ddl-auto=update`. Despliegue con Docker Compose (backend y MySQL) y un Dockerfile de frontend.

## Módulos

Usuarios y roles, sesiones, solicitudes y tipos de solicitud, orden del día, respuestas, historial [BASE]; estudiantes, funcionarios académicos, programas y asignaturas, catálogos académicos, solicitudes académicas, cancelación de matrícula, cancelación de asignatura y exámenes supletorios [EXTENSIÓN]. Máquina de etapas en `dominio/servicios/MaquinaEtapas.java`.

## Cifras clave

| Concepto | Total | [BASE] | [EXTENSIÓN] |
|---|---|---|---|
| Endpoints (grep de anotaciones de mapeo) | 128 | 62 | 66 |
| Clases `.java` en main | 459 | 175 | 284 |
| Entidades `@Entity` | 32 | 13 | 19 |
| Tablas físicas | 33 | 14 | 19 |
| Casos de uso / operaciones | 30 / 138 | 8 / 62 | 22 / 76 |
| Componentes Angular | 145 | 87 | 58 |
| Rutas (34 con pantalla, más la raíz) | 35 | 18 | 17 |
| Servicios HTTP del frontend | 17 | 9 | 8 |
| Tests backend (`@Test` + `@ParameterizedTest`) | 696 en 38 archivos | 1 archivo | 37 archivos |
| Specs frontend | 136 (524 `it(`) | 87 | 49 |
| Constantes de `MensajesError` | 115 | 22 | 93 |


## División entre Julián y lo nuevo

El primer commit (`d3a48cc`) contiene el código de Julián; la extensión solo agregó líneas a cuatro de sus archivos (`ApplicationConstantes`, `BeanConfiguracion`, `ConfiguracionSeguridad`, `MensajesError`). La extensión replica el estilo de puertos y casos de uso, pero sin comentarios, y agrega pruebas automatizadas que el prototipo casi no tenía.

## Los 10 hallazgos más importantes

1. La monografía dice JWT de 5 horas; el código usa 24 horas (`JwtServicio.java:36`).
2. La clave de firma del JWT está escrita en el código fuente.
3. No hay `@EnableMethodSecurity`: los `@PreAuthorize` no se evalúan; la seguridad real son las reglas por URL, y 22 endpoints [BASE] difieren de su anotación.
4. Todos los errores de negocio devuelven HTTP 500, diferenciados solo por `codigoError`.
5. La monografía dice que solo hay Postman y UAT; existen 696 métodos de test en backend y 136 specs en frontend.
6. El dominio depende de infraestructura (33 archivos) y de Spring (4 archivos [BASE]).
7. Los casos de uso no abren transacciones; lo hacen los controladores.
8. Faltan restricciones en base de datos (sin `ON DELETE`, sin CHECK) y las tablas de Julián no tienen DDL documentado.
9. La documentación se contradice (`etapas-por-proceso.md` P14 contra P46, README contra el código, 18 contra 19 tablas, 1233 contra 696 pruebas).
10. Las ramas Prod y Dev de la monografía no existen en el repositorio; solo hay `main`.


===== 00-indice-y-metodo.md =====

# 00 - Índice y método

## Índice de documentos

| Archivo | Contenido |
|---|---|
| 00-RESUMEN-EJECUTIVO.md | Resumen de tres páginas |
| 01-vision-general-y-stack | Versiones, dependencias, estructura, líneas de código, mapa BASE/EXTENSIÓN |
| 02-backend-arquitectura | Paquetes, capas, inventario, inyección de dependencias |
| 03-backend-endpoints | Catálogo de endpoints |
| 04-backend-casos-de-uso | Reglas de negocio por caso de uso y por proceso |
| 05-base-de-datos | Tablas, relaciones, E-R |
| 06-seguridad-y-errores | Seguridad, JWT, errores, historial |
| 07-frontend-arquitectura | Rutas, guards, servicios, componentes compartidos |
| 08-frontend-pantallas | Catálogo de pantallas por rol |
| 09-flujos-y-estados | Flujos extremo a extremo y máquinas de estado |
| 10-contraste-monografia | Afirmaciones de la monografía de Julián contra el código |
| 11-pruebas-despliegue-configuracion | Tests, Docker, configuración |
| 12-insumos-diagramas / 13-diagramas-fuente | Insumos y código Mermaid de los diagramas |
| 14-hallazgos-deuda-tecnica / 15-glosario | Hallazgos y glosario |
| 16-archivos-para-subir-a-claude | Paquete para la asistente de redacción |
| SUPUESTOS.md, PROGRESO.md, VERIFICACION.md | Control del análisis |

## Método

- Lectura estática del repositorio con Glob, Grep y lectura de archivos. No se ejecutó ningún build, test ni instalación, ni se usó internet.
- Git solo en modo lectura (log, blame, show, status).
- Docker Desktop no estaba corriendo, por lo que el esquema se dedujo de entidades JPA, `docs/database/` y scripts SQL.
- Cada afirmación lleva ruta de archivo y clase o método. Lo no comprobado se marca NO VERIFICADO.
- Se trabajó por fases; las fases 2 a 8 y 11 se repartieron entre subagentes y se consolidaron.

## Convención de etiquetas

- [BASE]: código, tablas o datos del prototipo de Julián David Camacho Erazo.
- [EXTENSIÓN]: lo agregado en este trabajo (Cancelación de Matrícula, Cancelación de Asignatura, Examen Supletorio, Estudiante, Funcionario Académico, Decano, programas, asignaturas, sesiones).

## Cómo se distinguió BASE de EXTENSIÓN

El historial de git sirve solo en parte: el primer commit (`d3a48cc`, 2026-09-30, "Inicializa el repositorio del trabajo de grado") importó 647 archivos y 38 051 líneas, y los 86 commits son de un solo autor. Para el árbol Java de main, el contenido de `git ls-tree d3a48cc` separa exactamente: 175 archivos [BASE] y 284 [EXTENSIÓN] (ver 02). Para el resto se usaron tres criterios, en este orden:

1. Lo que `CLAUDE.md` raíz, `backend/CLAUDE.md` y `frontend/CLAUDE.md` declaran como de Julián o como nuevo.
2. Nombres de clases, paquetes y tablas con vocabulario de la extensión (Academic, Cancelacion, Supletorio, Estudiante, Asignatura, Programa, Resolucion, Sesion, Situacion, Etapa, Anexo).
3. Archivos de la lista de Julián (Usuario, Rol, Solicitud, TipoSolicitud, OrdenDelDia, Respuesta, Log, Funcionario, seguridad JWT, carga masiva Excel, exportación .docx).

Límite: archivos de Julián tocados por la extensión (por ejemplo `ConfiguracionSeguridad`, `MensajesError`, `BeanConfiguracion`, `data.sql`) figuran como [BASE] con adiciones [EXTENSIÓN]; la clasificación por nombre es aproximada.

## Límites del análisis

- Sin ejecución: comportamiento en tiempo de ejecución y cobertura real de tests no medidos.
- No se tiene acceso a la monografía de Julián; se contrastan solo las afirmaciones listadas en `docs/PROMPT_ANALISIS.md`.
- Ramas Prod y Dev: solo existe `main` (ver SUPUESTOS.md).


===== 01-vision-general-y-stack.md =====

# 01 - Visión general y stack

## Qué es

Sistema web para gestionar solicitudes académicas de la FIET (Universidad del Cauca). [BASE] gestiona solicitudes dirigidas al Consejo de Facultad (usuarios, roles, tipos de solicitud, orden del día, respuestas, historial). [EXTENSIÓN] agrega tres procesos: Cancelación de Matrícula, Cancelación de Asignatura y Expedición de Exámenes Supletorios, con los actores Estudiante, Funcionario Académico y Decano. Fuente: `CLAUDE.md`.

## Versiones exactas (backend/solicitudes/pom.xml)

| Elemento | Versión |
|---|---|
| spring-boot-starter-parent | 3.5.4 |
| java.version | 17 |
| artefacto | com.unicauca.cfiet:solicitudes 0.0.1 |

| Dependencia | Versión | Para qué sirve |
|---|---|---|
| spring-boot-starter-web | gestionada | API REST |
| spring-boot-starter-data-jpa | gestionada | Persistencia con Hibernate |
| spring-boot-starter-security | gestionada | Autenticación y autorización |
| spring-boot-starter-test | gestionada | JUnit 5, Mockito |
| jjwt-api / jjwt-impl / jjwt-jackson | 0.11.2 | Emisión y validación de JWT |
| poi-ooxml | 5.2.5 | Lectura de Excel (carga masiva) y escritura .docx |
| springdoc-openapi-starter-webmvc-ui | 2.8.10 | Swagger UI |
| modelmapper | 2.4.5 | Conversiones simples |
| mysql-connector-j | gestionada | Driver MySQL |
| lombok | gestionada | Generación de código (procesador en maven-compiler-plugin) |

## Versiones exactas (frontend/package.json)

| Dependencia | Versión | Para qué sirve |
|---|---|---|
| @angular/common, compiler, core, forms, platform-browser, platform-server, router | ^20.2.0 | Framework |
| @angular/ssr | ^20.2.1 | Renderizado en servidor |
| primeng | ^20.1.1 | Componentes de UI |
| @primeuix/themes | ^1.2.3 | Temas PrimeNG |
| primeflex | ^4.0.0 | Utilidades CSS |
| primeicons | ^7.0.0 | Iconos |
| bootstrap | ^5.3.8 | Estilos |
| sweetalert2 | ^11.23.0 | Diálogos de confirmación |
| express | ^5.1.0 | Servidor SSR |
| rxjs | ~7.8.0 | Programación reactiva |
| zone.js | ~0.15.0 | Detección de cambios |
| tslib | ^2.3.0 | Soporte TypeScript |
| typescript (dev) | ~5.9.2 | Compilador |
| @angular/build, cli, compiler-cli (dev) | ^20.2.x | Toolchain |
| jasmine-core ~5.9.0, karma ~6.4.0, karma-chrome-launcher ~3.2.0, karma-coverage ~2.2.0, karma-jasmine ~5.1.0, karma-jasmine-html-reporter ~2.1.0 (dev) | varias | Pruebas |

## Estructura del repositorio (3 niveles)

```
Gestion-Procesos-Academicos-FIET/
  backend/
    solicitudes/ (pom.xml, src/main, src/test, target)
    pruebas/ (scripts .ps1)
    uploads/
    Dockerfile, docker-compose.yml, CLAUDE.md, README.md
  frontend/
    src/app/ (core, layouts, shared)
    public/, dist/, angular.json, package.json, dockerfile, CLAUDE.md
  docs/
    api/, database/, frontend/, analisis/
  CLAUDE.md, README.md, change.md, limpieza_local.sql
```

Backend: `backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/{aplicacion,dominio,infraestructura}`.
Frontend: `frontend/src/app/core/{auth,constantes,decano,estudiante,funcionario,funcionario-academico,models,secretariaFiet,secretario-general,services,usuario fiet,usuario publico,utils}`, `layouts`, `shared`.

## Líneas de código (cat + wc -l)

| Módulo | Líneas |
|---|---|
| Backend main: aplicacion | 2 014 |
| Backend main: dominio | 7 074 |
| Backend main: infraestructura | 14 634 |
| Backend test | 11 754 |
| Frontend .ts (con specs) | 25 606 (parcial) |
| Frontend .html | 4 167 (parcial) |
| Frontend .css/.scss | 3 157 (parcial) |

Archivos: backend main 459 .java; frontend 292 .ts sin specs y 136 .spec.ts bajo `frontend/src/app`. El conteo de líneas del frontend omitió los directorios con espacio en el nombre (`usuario fiet`, `usuario publico`) por un error de shell, así que esas cifras de líneas quedan por debajo del real (NO VERIFICADO el total exacto). Los conteos de archivos sí los incluyen.

## Mapa BASE y EXTENSIÓN

Backend, según el commit inicial: de 459 archivos .java en main, 284 son [EXTENSIÓN] y 175 [BASE] (criterio exacto: `git ls-tree d3a48cc`, ver 02). Archivos [BASE] típicos: casos de uso `Usuario`, `Rol`, `Solicitud`, `TipoSolicitud`, `OrdenDelDia`, `Respuesta`, `Log`; `ConfiguracionSeguridad`, `JwtFiltroAutenticacion`, `JwtServicio`, `BeanConfiguracion`, `ProcesadorArchivos`, `UsuarioExcelService`, `TipoSolicitudExcelService`, `OrdenDelDiaExportadorImpl`, excepciones `Error*Excepcion`, mapeadores `*OwnMapper`.

Módulos de controladores en `infraestructura/input`:

| Paquete | Origen |
|---|---|
| controladorUsuarios, controladorRoles, controladorSolicitudes, controladorTiposSolicitud, controladorRespuestas, controladorLog | [BASE] |
| controladorAsignaturas, controladorCancelacionesAsignatura, controladorCancelacionesMatricula, controladorCatalogosAcademicos, controladorEstudiantes, controladorExamenesSupletorios, controladorFuncionariosAcademicos, controladorSesion, controladorSolicitudesAcademicas | [EXTENSIÓN] |

Frontend: carpetas [BASE]: `core/funcionario`, `core/secretariaFiet`, `core/secretario-general`, `core/usuario fiet`, `core/usuario publico`, `core/auth`; [EXTENSIÓN]: `core/decano`, `core/estudiante`, `core/funcionario-academico` y los modelos Asignatura, CancelacionAsignatura, CancelacionMatricula, CatalogoAcademico, Estudiante, ExamenSupletorio, FuncionarioAcademico, Sesión, SolicitudAcademica. Detalle en 07 y 08.

Datos: [EXTENSIÓN] documentada en `docs/database/` (19 tablas nuevas según `CLAUDE.md`; el prompt dice 18, se resuelve en 05-base-de-datos.md).

Historial git: 86 commits de un único autor entre 2026-09-30 y 2026-10-13; el primero importa todo el código.


===== 10-contraste-monografia.md =====

# 10 - Contraste con la monografía de Julián

Raíz de rutas: `B = backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes`. Estados: CONFIRMADO, DIVERGENTE, NO ENCONTRADO, PARCIAL. Las afirmaciones provienen de `docs/PROMPT_ANALISIS.md`; la monografía misma no estaba disponible.

## Tabla de afirmaciones

| # | Afirmación | Estado | Evidencia | Comentario |
|---|---|---|---|---|
| 1 | Capas aplicación (input/output), dominio (casosdeuso, helper, modelos) e infraestructura (configuracion, input, output) | CONFIRMADO | `B/aplicacion/{input,output}`, `B/dominio/{casosdeuso,helper,modelos,servicios}`, `B/infraestructura/{configuracion,input,output}` | El dominio tiene además `servicios` ([EXTENSIÓN]: `MaquinaEtapas`, `ReglasAsignatura`, `TramiteCancelacion`). Ver 02 para el árbol completo y violaciones de dependencia. |
| 2 | Mapeador propio con interfaz de contrato que sustituyó a ModelMapper | PARCIAL | `B/infraestructura/output/persistencia/mapeador/` contiene `*OwnMapper` y `Mapper.java`; `B/infraestructura/input/*/mapeador/Mapper*InfraestructuraDominio.java` usan `org.modelmapper.ModelMapper` | Persistencia usa mapeadores propios; los mapeadores de la capa de entrada (DTO a dominio) siguen con ModelMapper inyectado con `@Qualifier("mapeadorSimple")` (p. ej. `MapperUsuarioInfraestructuraDominio` líneas 12 y 26). |
| 3 | Eliminaron @OneToMany/@ManyToOne redundantes | PARCIAL | Solo 5 `@OneToMany` en todo el main: `EstudianteEntidad:45` (cascade ALL, EAGER), `SolicitudEntidad:33`, `TipoSolicitudEntidad:30`, `TipoUsuarioEntidad:23` (LAZY); 48 anotaciones de relación en total | Quedan relaciones bidireccionales; `EstudianteEntidad` [EXTENSIÓN] usa EAGER con cascade ALL. |
| 4 | JWT con vigencia de 5 horas | DIVERGENTE | `B/infraestructura/configuracion/seguridad/jwt/JwtServicio.java:36` `24 * 60 * 60 * 1000` | La vigencia es de 24 horas. Firma HS256 con `SECRET_KEY` fija en la clase (línea 23), no en configuración. |
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
| 27 | Pruebas solo Postman y UAT | DIVERGENTE | Backend: 38 archivos de test y 615 anotaciones `@Test` en `backend/solicitudes/src/test`; frontend: 136 `.spec.ts` en `frontend/src/app` | Hay tests automatizados reales (JUnit/Mockito, Karma/Jasmine). Postman: no se encontró colección (`find -iname "*postman*"` sin resultados); existen scripts `backend/pruebas/*.ps1`. Detalle en 11. |
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
| Pruebas | Sin pruebas en la monografía | 615 `@Test` |
| Relaciones JPA | Pocas bidireccionales | Mezcla; `EstudianteEntidad` con cascade ALL y EAGER |


===== 14-hallazgos-deuda-tecnica.md =====

# 14 - Hallazgos y deuda técnica

Fuentes: los documentos 02, 03, 04, 05, 06, 07, 11 y verificaciones propias con grep. Rutas relativas a `backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes` (B) salvo indicación.

## Seguridad

| # | Hallazgo | Evidencia | Origen |
|---|---|---|---|
| S1 | No existe `@EnableMethodSecurity`: los `@PreAuthorize` no se evalúan; la protección real son los `requestMatchers` por URL | grep de `@EnableMethodSecurity` en main: 0 coincidencias; 124 líneas con `@PreAuthorize` (el subagente de seguridad reportó 138; diferencia de conteo no conciliada) | [BASE] y [EXTENSIÓN] |
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


===== 15-glosario.md =====

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
| Cancelación de Matrícula | [EXTENSIÓN] Proceso de retiro del estudiante del semestre, con seis anexos obligatorios |
| Cancelación de Asignatura | [EXTENSIÓN] Proceso de cancelar una o varias asignaturas; el Decano aprueba o rechaza cada una |
| Examen Supletorio | [EXTENSIÓN] Examen que reemplaza uno no presentado; plazo de tres días hábiles, sin Resolución |
| Estudiante | [EXTENSIÓN] Actor que radica solicitudes académicas |
| Funcionario Académico | [EXTENSIÓN] Técnico Administrativo de Procesos Académicos; revisa y remite al Decano |
| Decano | Máxima autoridad FIET; aprueba o rechaza |
| Secretario General | [BASE] Rol administrativo que crea usuarios |
| Radicar | Presentar formalmente la solicitud (acción `RADICAR`) |
| Etapa | Estado de una solicitud académica en su proceso (por ejemplo RADICADA, EN_REVISION_DECANO) |
| Etapa etiqueta por rol | Texto que cada rol ve para una misma etapa (`EtapaEtiquetaRol`) |
| Resolución | Documento físico firmado por el Decano; se sube su copia escaneada |
| Anexo académico | Archivo adjunto a una solicitud académica, por tipo obligatorio |
| Programa académico | Uno de cinco fijos: IET, ISI, IAI, TTE, IAC |
| Asignatura matriculada | Asignatura inscrita por un estudiante, con estado y notas |
| Situación académica | Datos de faltas, notas y estado que sustentan una cancelación |
| Paz y salvo | Anexo exigido en Cancelación de Matrícula, con vigencia |
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
| JWT | Token firmado HS256, vigencia 24 h, enviado como `Authorization: Bearer` |
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


===== SUPUESTOS.md =====

# Supuestos y decisiones

1. El prompt prohíbe modificar archivos fuera de docs/analisis/; la regla de CLAUDE.md de registrar en change.md choca con eso. Se priorizó el prompt explícito: change.md NO se modificó. Se debe agregar manualmente una entrada si se desea.
2. Docker no está corriendo (no hay API de Docker disponible): el esquema se deduce de entidades JPA, docs/database y scripts SQL.
3. Solo existe la rama main (local y origin); las ramas Prod/Dev de la monografía no se encuentran en el repo.
4. La regla de no comentarios aplica a código; los .md del análisis sí llevan texto explicativo.


===== VERIFICACION.md =====

# Verificación cruzada

| Comprobación | Resultado | Método |
|---|---|---|
| Total de endpoints del catálogo (03) contra anotaciones de mapeo | CUADRA: 128 en el catálogo y 128 por grep de `@Get/Post/Put/Patch/DeleteMapping` en main (75 GET, 41 POST, 9 PUT, 3 PATCH, 0 DELETE) | grep de líneas con la anotación, ejecutado por el subagente y repetido en el cierre |
| Endpoints de la extensión contra `docs/api/contrato-api-procesos-academicos.md` | CUADRA: 66 | subagente de fase 3 |
| Total de tablas contra entidades y SQL | CUADRA: 32 `@Entity` (grep en el cierre), 19 `CREATE TABLE` en `docs/database/script_bd_extension.sql` (grep en el cierre) y 19 entidades de extensión; las tablas físicas son 33 por `Usuario_has_Roles`, que sale de un `@JoinTable` | grep y lectura (05) |
| 18 contra 19 tablas nuevas | RESUELTO: son 19; el 18 es un conteo histórico en `change.md` y `docs/informe-discrepancias-documentacion.md` | 05 |
| Pantallas del catálogo (08) contra el routing | CUADRA: las 35 rutas `path` de `frontend/src/app/app.routes.ts` aparecen todas en 08 | extracción de `path:` y búsqueda de cada una en el documento |
| Tests: 38 archivos, 615 `@Test` | CUADRA con 11; no se ejecutaron | grep |
| Specs frontend: 136 | CUADRA con 07/08/11 | `find *.spec.ts` |
| Componentes Angular: 145 | CUADRA con 07 | grep de `@Component(` sin specs |
| Diagramas Mermaid de 13 | 41 bloques; sintaxis no renderizada (NO VERIFICADO) | conteo de bloques |

## Discrepancias detectadas y corregidas

- `00-indice-y-metodo.md` y `01-vision-general-y-stack.md` decían que la separación BASE/EXTENSIÓN era heurística; el subagente de fase 2 demostró que `git ls-tree d3a48cc` la separa exactamente en el árbol Java (175 y 284). Se corrigieron ambos.
- `10-contraste-monografia.md` afirmaba un límite de memoria ambiguo en compose; se corrigió a "solo backend".

## Discrepancias abiertas

- Conteo de `@PreAuthorize`: 124 líneas por grep del cierre contra 138 reportado por el subagente de seguridad; no conciliado.
- Líneas de código del frontend subestimadas por directorios con espacios (01).
- Tests ejecutados (1233 según `backend/CLAUDE.md`) contra 696 métodos contados: NO VERIFICADO.
- No se ejecutó nada: ni builds, ni tests, ni diagramas renderizados, ni consultas a la base (Docker apagado).
- Las clasificaciones [BASE] de archivos de tests y del frontend parten del commit inicial; no hay historial anterior de Julián.
