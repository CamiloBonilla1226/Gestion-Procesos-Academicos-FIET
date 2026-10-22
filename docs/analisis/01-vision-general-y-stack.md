# 01 - Visión general y stack

## Qué es

Sistema web para gestionar solicitudes académicas de la FIET (Universidad del Cauca). [BASE] gestiona solicitudes dirigidas al Consejo de Facultad (usuarios, roles, tipos de solicitud, orden del día, respuestas, historial). [EXTENSIÓN] agrega tres procesos: Cancelación de Matrícula, Cancelación de Asignatura y Expedición de Exámenes Supletorios, con los actores Estudiante, Funcionario Académico y Decano. Fuente: `CLAUDE.md`. La extensión notifica además la respuesta final por correo electrónico ([correo-electronico.md](correo-electronico.md)).

Actualizado el 2026-10-21 (T12.37): dependencias nuevas, cifras de archivos y mapa de módulos.

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
| lombok | 1.18.42 (fijada en el `pom.xml` desde el 2026-10-15; antes la de Spring Boot) | Generación de código (procesador en maven-compiler-plugin) |
| spring-boot-starter-mail | gestionada | Envío de correo por SMTP (`JavaMailSender`) [EXTENSIÓN] |
| openpdf (`com.github.librepdf`) | 2.0.5 | PDF del radicado y constancia de la comunicación [EXTENSIÓN] |

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

Desde el 2026-10-15 `backend/` tiene además `.env.example` (plantilla de las variables del correo; el `.env` real no se versiona) y `backend/pruebas/` tiene `smtp_prueba.py` y `t9_correo_respuesta.ps1`.

Backend: `backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/{aplicacion,dominio,infraestructura}`.
Frontend: `frontend/src/app/core/{auth,constantes,decano,docente,estudiante,funcionario,funcionario-academico,models,secretariaFiet,secretario-general,services,usuario fiet,usuario publico,utils}`, `layouts`, `shared` (`core/docente` desde T12.28).

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

Archivos al corte: backend main 459 .java; frontend 292 .ts sin specs y 136 .spec.ts bajo `frontend/src/app`. Al 2026-10-21: backend main 517 .java y 51 de test; frontend 149 .spec.ts (las líneas de código de la tabla no se recontaron). El conteo de líneas del frontend omitió los directorios con espacio en el nombre (`usuario fiet`, `usuario publico`) por un error de shell, así que esas cifras de líneas quedan por debajo del real (NO VERIFICADO el total exacto). Los conteos de archivos sí los incluyen.

## Mapa BASE y EXTENSIÓN

Backend, según el commit inicial: de 527 archivos .java en main al 2026-10-21 (T12.40; 517 en T12.37), 352 son [EXTENSIÓN] y 175 [BASE] (459, 284 y 175 al corte) (criterio exacto: `git ls-tree d3a48cc`, ver 02). Archivos [BASE] típicos: casos de uso `Usuario`, `Rol`, `Solicitud`, `TipoSolicitud`, `OrdenDelDia`, `Respuesta`, `Log`; `ConfiguracionSeguridad`, `JwtFiltroAutenticacion`, `JwtServicio`, `BeanConfiguracion`, `ProcesadorArchivos`, `UsuarioExcelService`, `TipoSolicitudExcelService`, `OrdenDelDiaExportadorImpl`, excepciones `Error*Excepcion`, mapeadores `*OwnMapper`.

Módulos de controladores en `infraestructura/input`:

| Paquete | Origen |
|---|---|
| controladorUsuarios, controladorRoles, controladorSolicitudes, controladorTiposSolicitud, controladorRespuestas, controladorLog | [BASE] |
| controladorAsignaturas, controladorCancelacionesAsignatura, controladorCancelacionesMatricula, controladorCatalogosAcademicos, controladorEstudiantes, controladorExamenesSupletorios, controladorFuncionariosAcademicos, controladorNotificacionesDocente (T12.28), controladorSolicitudesAcademicas, y la tarea `programadorNotificaciones` (T12.32) | [EXTENSIÓN] |
| controladorSesion | [BASE] según `git ls-tree d3a48cc` (estaba en el commit inicial; el corte lo listaba como [EXTENSIÓN]) |

Frontend: carpetas [BASE]: `core/funcionario`, `core/secretariaFiet`, `core/secretario-general`, `core/usuario fiet`, `core/usuario publico`, `core/auth`; [EXTENSIÓN]: `core/decano`, `core/docente`, `core/estudiante`, `core/funcionario-academico` y los modelos Asignatura, CancelacionAsignatura, CancelacionMatricula, CatalogoAcademico, Estudiante, ExamenSupletorio, FuncionarioAcademico, Sesión, SolicitudAcademica. Detalle en 07 y 08.

Datos: [EXTENSIÓN] documentada en `docs/database/` (21 tablas nuevas según `CLAUDE.md` al 2026-10-21; eran 19 al corte y el prompt decía 18, ver 05-base-de-datos.md).

Historial git: al corte, 86 commits de un único autor entre 2026-09-30 y 2026-10-13; el primero importa todo el código. El último commit al 2026-10-21 es del 2026-10-20 (`e96a55b`), y los cambios de T12.35 están en el árbol de trabajo sin confirmar.
