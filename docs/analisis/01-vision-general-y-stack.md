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
