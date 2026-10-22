# 07 - Arquitectura del frontend

Alcance: `frontend/` (Angular 20 con PrimeNG y Bootstrap 5). Análisis de solo lectura. Rutas relativas a `frontend/src/app/` salvo indicación.

Convenciones de este documento:

- [BASE] = existe en el commit inicial `d3a48cc` ("Inicializa el repositorio del trabajo de grado"), es decir, el prototipo de Julián David Camacho Erazo. La clasificación se obtuvo comparando `git ls-tree d3a48cc` contra el árbol de trabajo.
- [EXTENSIÓN] = aparece después de ese commit (trabajo de grado: Cancelación de Matrícula, Cancelación de Asignatura, Examen Supletorio y administración académica).
- NO VERIFICADO = no se pudo comprobar por lectura estática (no se ejecutó build, tests ni la aplicación).

Actualizado el 2026-10-21 (T12.37): cifras, rutas y servicios del Docente (T12.28), panel y diálogos de la comunicación programada de la Resolución (T12.32, T12.33), documento adicional de la respuesta (P54), selector del profesor del supletorio y componente único de decisión del Decano (T12.35). Las decisiones de diseño posteriores al corte están en `frontend/CLAUDE.md` como D-F44 a D-F50.

## 1. Cifras generales

| Concepto | Total | [BASE] | [EXTENSIÓN] | Cómo se midió |
| --- | --- | --- | --- | --- |
| Archivos bajo `frontend/src` (sin `node_modules` ni `dist`) | 675 al corte | 442 | 233 | `find` y `git ls-tree d3a48cc` (no se recontó el 2026-10-21) |
| Componentes (`@Component` en `.ts` que no son spec) | 157 (145 al corte) | 87 | 70 | `grep -l "@Component("`; los 12 nuevos se listan en 9.2 |
| Servicios HTTP/aplicación en `core/services` (`.ts` sin spec) | 18 (17 al corte) | 9 | 9 | listado de `core/services`; el nuevo es `NotificacionDocenteService` |
| Rutas en `app.routes.ts` (entradas `path:`) | 37 (36 con pantalla más la raíz `''`; 35 al corte) | 18 | 19 | `grep -c "path:"` y `git show d3a48cc:.../app.routes.ts` |
| Archivos `*.spec.ts` | 149 (136 al corte) | 87 | 62 | `find -name "*.spec.ts"` |
| Casos de prueba (`it(`) | 629 (524 al corte) | NO VERIFICADO por separado | NO VERIFICADO por separado | `grep -E "^\s*it\("` sobre todos los specs |
| Interfaces de modelo en `core/models/**/DTO*` | 101 archivos | NO VERIFICADO | NO VERIFICADO | `ls` |
| Plantillas `.html` / hojas `.css` en `src/app` | 144 / 97 | NO VERIFICADO | NO VERIFICADO | `find` |

Nota sobre rutas [EXTENSIÓN]: 7 del Estudiante, 4 del Funcionario Académico, 5 del Decano, 1 del Secretario General (`sec-general/administracion-academica`) y, desde T12.28, 2 del Docente (`docente/notificaciones` y `docente/notificaciones/:uuid`), total 19. Coincide con 18 + 19 = 37.

Fuente de las decisiones de diseño de la extensión: `frontend/CLAUDE.md` (decisiones D-F1 a D-F43 al corte; hoy llegan a D-F50). Todo lo que se afirma abajo se contrastó con el código; donde `CLAUDE.md` y el código difieren se señala en la sección 14.

## 2. Estructura de carpetas

`frontend/` (raíz del proyecto Angular):

| Elemento | Contenido | Origen |
| --- | --- | --- |
| `angular.json`, `package.json`, `tsconfig*.json`, `dockerfile` | configuración de build, dependencias y despliegue | [BASE] (`git diff d3a48cc` no muestra cambios en `angular.json` ni en `package.json`) |
| `public/` | `favicon.ico`, `buttons/*.svg` (26 íconos), `logos/` (4 png), `formatos/` (2 `.doc`) | [BASE] los íconos y logos; [EXTENSIÓN] `formatos/` (D-F41) NO VERIFICADO en detalle por archivo |
| `src/main.ts`, `src/main.server.ts`, `src/server.ts` | arranque cliente, arranque servidor, Express para SSR | [BASE] (`main.ts` lleva un comentario de autoría agregado) |
| `src/enviroments/environment.ts` | única configuración de entorno (ver sección 12) | [BASE]; la carpeta se llama `enviroments` (con el typo) |
| `src/styles.css` | estilos globales y tokens `:root` | [BASE] modificado: +36 líneas de tokens (D-F27) |
| `src/app/app.ts`, `app.html`, `app.css`, `app.config.ts`, `app.config.server.ts`, `app.routes.ts`, `app.routes.server.ts` | raíz de la aplicación | [BASE]; `app.routes.ts` ampliado (+109 líneas) y `app.routes.server.ts` ampliado con 3 rutas `RenderMode.Client` y la raíz |

Dentro de `src/app`:

| Carpeta | Qué contiene | Origen |
| --- | --- | --- |
| `core/auth/` | `pages/login-component`, `guards/` (`auth-guard`, `role-guard`, `inicio-guard`), `interceptor/` (`auth-interceptor`, `sesion-vencida-service`) | [BASE]; `inicio-guard` y `sesion-vencida-service` son [EXTENSIÓN] (D-F20, D-F8) |
| `core/services/` | 17 servicios `*-service.ts` (ver sección 6) | mixto |
| `core/models/<Recurso>/DTORequest|DTOResponse/` | una interfaz por archivo, mismo nombre que el DTO Java (D-F1). Carpeta heredada `Sesión/DTPResponse` con typo | mixto |
| `core/constantes/` | `constantes.ts` [BASE]; `procesos-academicos.ts`, `etiquetas-academicas.ts`, `catalogo-estudiante.ts`, `formatos-supletorio.ts`, `textos-cancelacion-*.ts` [EXTENSIÓN] | mixto |
| `core/utils/` | `bandeja-solicitudes`, `descargas`, `encabezado-agrupado`, `errores-http`, `formato`, `parametros-http`, `programas-academicos`, `rutas-por-rol`, `usuarios-academicos`, `validaciones-academicas` | [EXTENSIÓN] |
| `core/secretario-general/`, `core/funcionario/`, `core/secretariaFiet/`, `core/usuario fiet/`, `core/usuario publico/` | páginas (`pages/`) y piezas (`components/`) por rol de Julián. Dos carpetas llevan espacio en el nombre | [BASE] (excepto `secretario-general-administracion-academica-component`, [EXTENSIÓN]) |
| `core/estudiante/`, `core/funcionario-academico/`, `core/decano/` | rol nuevo: `pages/` (páginas delgadas) y `components/side-bar-*` | [EXTENSIÓN] |
| `layouts/layout-component`, `layouts/layout-simple-component` | marcos de página | [BASE] |
| `shared/pages/*-component` | envoltorios de segundo nivel (usan `app-content-component`) | mixto |
| `shared/pages/content/*` | componentes de contenido (lógica real) y componentes de proceso | mixto |
| `shared/others/`, `shared/buttons/`, `shared/inputs/`, `shared/search/`, `shared/paginator/`, `shared/sidebars/`, `shared/headers/`, `shared/footers/`, `shared/breadcrumb/`, `shared/avatar/`, `shared/table-generic-component/`, `shared/card-main-component/`, `shared/generic-dialog-*` | piezas reutilizables (sección 9) | mixto |

Patrón de composición en tres niveles (verificado en el código):

1. Página de rol: `core/<rol>/pages/<x>/<x>.ts`. Solo declara propiedades `sidebar`, `header`, `breadcrumb`, `main` (referencias a clases) y su `.html` pinta `<app-page-component [sidebarComponent] [headerComponent] [breadcrumbComponent] [breadcrumbInputs] [mainComponent]>`. Ejemplo: `core/estudiante/pages/estudiante-solicitudes-component/estudiante-solicitudes-component.ts`.
2. Envoltorio: `shared/pages/<x>-component/<x>-component.ts`. Define `content = <ContenidoComponent>` y su `.html` pinta `<app-content-component [title] [bodyComponent]="content">`. Ejemplo: `shared/pages/est-solicitudes-component` con título "Mis solicitudes".
3. Contenido: `shared/pages/content/<x>-content-component/`. Aquí está la lógica (servicios, estado, diálogos).

Excepciones observadas: (a) `LoginComponent` usa `app-page-simple-component` directamente con `loginFormComponent`; (b) `UsuarioPublicoSolicitudesComponent` también usa `app-page-simple-component` con `main = UsuarioPublicoSolicitudesContentComponent` (no pasa por `app-content-component`); (c) los envoltorios `est-solicitudes-content-component`, `dec-solicitudes-content-component`, `fun-acad-solicitudes-content-component` y los tres `*-detalle-solicitud-content-component` son de 9 a 20 líneas y solo montan `app-bandeja-solicitudes-academicas-component` o `app-detalle-solicitud-academica-component` con `rol="ESTUDIANTE|FUNCIONARIO|DECANO"`.

## 3. Enrutamiento

Fuente: `app.routes.ts`. Todas las rutas con pantalla usan `loadComponent` (carga diferida). Las protegidas usan `canActivate: [AuthGuard, RoleGuard]` con `data.roles`. `ROLES_ACADEMICOS` está en `core/constantes/procesos-academicos.ts` ('Estudiante', 'Funcionario Académico', 'Decano', 'Secretario General').

### 3.1 Tabla ruta -> componente -> rol -> guard

| # | Ruta | Componente de página | Roles permitidos (`data.roles`) | Guards | Origen |
| --- | --- | --- | --- | --- | --- |
| 1 | `login` | `LoginComponent` (`core/auth/pages/login-component`) | ninguno (pública) | ninguno | [BASE] |
| 2 | `usuario-publico/solicitudes` | `UsuarioPublicoSolicitudesComponent` | ninguno (pública) | ninguno | [BASE] |
| 3 | `''` (pathMatch full) | sin componente (`children: []`) | cualquiera | `inicioGuard` (redirige) | [BASE] la ruta; [EXTENSIÓN] el guard (D-F20) |
| 4 | `sec-general` | `SecretarioGeneralHomeComponent` | Secretario General | AuthGuard, RoleGuard | [BASE] |
| 5 | `sec-general/roles` | `SecretarioGeneralRolesComponent` | Secretario General | AuthGuard, RoleGuard | [BASE] |
| 6 | `sec-general/usuarios` | `SecretarioGeneralUsuariosComponent` | Secretario General | AuthGuard, RoleGuard | [BASE] |
| 7 | `sec-general/logs` | `SecretarioGeneralLogComponent` | Secretario General | AuthGuard, RoleGuard | [BASE] |
| 8 | `sec-general/tipos/solicitudes` | `SecretarioGeneralTipoSolicitudesComponent` | Secretario General | AuthGuard, RoleGuard | [BASE] |
| 9 | `sec-general/solicitudes` | `SecretarioGeneralSolicitudes` | Secretario General | AuthGuard, RoleGuard | [BASE] |
| 10 | `sec-general/orden-del-dia` | `SecretarioGeneralOrdelDelDia` | Secretario General | AuthGuard, RoleGuard | [BASE] |
| 11 | `sec-general/respuestas` | `SecretarioGeneralRespuestasComponent` | Secretario General | AuthGuard, RoleGuard | [BASE] |
| 12 | `sec-general/administracion-academica` | `SecretarioGeneralAdministracionAcademicaComponent` | Secretario General | AuthGuard, RoleGuard | [EXTENSIÓN] |
| 13 | `funcionario` | `FuncionarioHomeComponent` | Funcionario | AuthGuard, RoleGuard | [BASE] |
| 14 | `funcionario/solicitudes` | `FuncionarioSolicitudes` | Funcionario | AuthGuard, RoleGuard | [BASE] |
| 15 | `funcionario/respuestas` | `FuncionarioRespuestasComponent` | Funcionario | AuthGuard, RoleGuard | [BASE] |
| 16 | `sec-fiet` | `SecretariaFietHomeComponent` | Secretaria Decanatura FIET | AuthGuard, RoleGuard | [BASE] |
| 17 | `sec-fiet/respuestas` | `SecretariaFietRespuestasComponent` | Secretaria Decanatura FIET | AuthGuard, RoleGuard | [BASE] |
| 18 | `usuario-fiet` | `UsuarioFietHomeComponent` | Coordinador Pregrado, Coordinador Posgrados, Jefe de Departamento, Decano, Docente | AuthGuard, RoleGuard | [BASE] |
| 19 | `usuario-fiet/solicitudes` | `UsuariofietSolicitudesComponent` | los mismos cinco roles | AuthGuard, RoleGuard | [BASE] |
| 20 | `estudiante` | `EstudianteHomeComponent` | Estudiante | AuthGuard, RoleGuard | [EXTENSIÓN] |
| 21 | `estudiante/solicitudes` | `EstudianteSolicitudesComponent` | Estudiante | AuthGuard, RoleGuard | [EXTENSIÓN] |
| 22 | `estudiante/catalogo` | `EstudianteCatalogoComponent` | Estudiante | AuthGuard, RoleGuard | [EXTENSIÓN] |
| 23 | `estudiante/solicitudes/:uuid` | `EstudianteDetalleSolicitudComponent` | Estudiante | AuthGuard, RoleGuard | [EXTENSIÓN] |
| 24 | `estudiante/cancelacion-matricula` | `EstudianteCancelacionMatriculaComponent` | Estudiante | AuthGuard, RoleGuard | [EXTENSIÓN] |
| 25 | `estudiante/cancelacion-asignatura` | `EstudianteCancelacionAsignaturaComponent` | Estudiante | AuthGuard, RoleGuard | [EXTENSIÓN] |
| 26 | `estudiante/examen-supletorio` | `EstudianteExamenSupletorioComponent` | Estudiante | AuthGuard, RoleGuard | [EXTENSIÓN] |
| 27 | `funcionario-academico` | `FuncionarioAcademicoHomeComponent` | Funcionario Académico | AuthGuard, RoleGuard | [EXTENSIÓN] |
| 28 | `funcionario-academico/solicitudes` | `FuncionarioAcademicoSolicitudesComponent` | Funcionario Académico | AuthGuard, RoleGuard | [EXTENSIÓN] |
| 29 | `funcionario-academico/respuestas` | `FuncionarioAcademicoRespuestasComponent` (`data.modo = 'respuestas'`) | Funcionario Académico | AuthGuard, RoleGuard | [EXTENSIÓN] |
| 30 | `funcionario-academico/solicitudes/:uuid` | `FuncionarioAcademicoDetalleSolicitudComponent` | Funcionario Académico | AuthGuard, RoleGuard | [EXTENSIÓN] |
| 31 | `decano` | `DecanoHomeComponent` | Decano | AuthGuard, RoleGuard | [EXTENSIÓN] |
| 32 | `decano/solicitudes` | `DecanoSolicitudesComponent` | Decano | AuthGuard, RoleGuard | [EXTENSIÓN] |
| 33 | `decano/solicitudes/:uuid` | `DecanoDetalleSolicitudComponent` | Decano | AuthGuard, RoleGuard | [EXTENSIÓN] |
| 34 | `decano/solicitudes-consejo` | `DecanoSolicitudesConsejoComponent` | Decano | AuthGuard, RoleGuard | [EXTENSIÓN] |
| 35 | `decano/administracion-academica` | `DecanoAdministracionAcademicaComponent` | Decano | AuthGuard, RoleGuard | [EXTENSIÓN] |
| 36 | `docente/notificaciones` | `DocenteNotificacionesComponent` (`core/docente/pages`) | Docente | AuthGuard, RoleGuard | [EXTENSIÓN] (T12.28) |
| 37 | `docente/notificaciones/:uuid` | `DocenteDetalleNotificacionComponent` | Docente | AuthGuard, RoleGuard | [EXTENSIÓN] (T12.28) |

El Docente sigue usando las rutas de Julián de `usuario-fiet`; su barra lateral (`side-bar-usuario-fiet-component`, [BASE]) muestra la opción "Notificaciones" solo si el usuario tiene el rol Docente.

Observaciones verificadas:

- No hay ruta comodín (`**`) ni de "no encontrado" en `app.routes.ts`. Una URL desconocida no tiene componente asociado. NO VERIFICADO el comportamiento en ejecución.
- El rol Decano también está en `usuario-fiet` y `usuario-fiet/solicitudes` (rutas [BASE] de Julián), de modo que un Decano puede abrir esas pantallas por URL directa. El inicio por defecto lo manda a `/decano` (`rutaDeInicio`).
- Las rutas `funcionario-academico/solicitudes` y `funcionario-academico/respuestas` montan el mismo contenido (`FunAcadSolicitudesComponent` -> `FunAcadSolicitudesContentComponent`); la diferencia es `data.modo`, que el contenido lee con `ActivatedRoute` (`modoDe`) y pasa como `[modo]` a `BandejaSolicitudesAcademicasComponent` (D-F38).
- `RoleGuard` compara contra `route.data['roles']` con `some(...)`: basta uno de los roles. `sidebar` de la Secretaría FIET usa `route: 'sec-fiet/respuestas'` sin barra inicial (`core/secretariaFiet/components/side-bar-secretaria-fiet-component/...html`); `SidebarComponent.normalizar` agrega la barra.

### 3.2 Redirección de inicio

`core/utils/rutas-por-rol.ts` -> `rutaDeInicio(roles)`. Orden de evaluación: Secretario General (`/sec-general`), Funcionario (`/funcionario`), Secretaria Decanatura FIET (`/sec-fiet`), Decano (`/decano`), Funcionario Académico (`/funcionario-academico`), Estudiante (`/estudiante`), cualquier rol de `ROLES_FIET` (`/usuario-fiet`), de lo contrario `null`. La usan `LoginFormComponent.onSubmit`, `inicioGuard` y `AvatarSesionComponent` ("Mi usuario"). [EXTENSIÓN] como función; en el commit base la lógica estaba dentro de `LoginFormComponent` NO VERIFICADO el texto exacto previo.

### 3.3 Rutas de servidor (SSR)

`app.routes.server.ts` (`serverRoutes`): `estudiante/solicitudes/:uuid`, `funcionario-academico/solicitudes/:uuid` y `decano/solicitudes/:uuid` con `RenderMode.Client` (T12.28 también modificó este archivo por las rutas del Docente; el detalle no se volvió a leer); `''` con `RenderMode.Client`; `'**'` con `RenderMode.Prerender`. El commit base solo tenía `'**'` con `Prerender`. [BASE] + [EXTENSIÓN].

## 4. Guards

| Guard | Archivo | Qué hace | Origen |
| --- | --- | --- | --- |
| `AuthGuard` (clase `CanActivate`) | `core/auth/guards/auth-guard.ts`, método `canActivate()` | Si `window` existe y hay `authToken` en `localStorage` devuelve `true`; si no, navega a `/login` y devuelve `false`. No valida expiración ni firma. | [BASE] |
| `RoleGuard` (clase `CanActivate`) | `core/auth/guards/role-guard.ts`, `canActivate(route)` | Lee `currentUser` de `localStorage`, llama `UsuariosService.getUsuario(uuid)` (GET `/usuarios/{uuid}`), compara los nombres de rol con `route.data['roles']`. Sin coincidencia, sin `currentUser` o con error HTTP navega a `/login`. Hace una petición HTTP en cada navegación a ruta protegida (sin caché); NO VERIFICADO el impacto de rendimiento. | [BASE] |
| `inicioGuard` (`CanActivateFn`) | `core/auth/guards/inicio-guard.ts` | Sin token o sin `currentUser` devuelve `UrlTree` a `/login`; con ambos consulta el usuario y devuelve `UrlTree` a `rutaDeInicio(roles)` o a `/login` si no hay ruta o hay error. En servidor (sin `window`) va a `/login`. | [EXTENSIÓN] (D-F20) |

Las dos clases base son `@Injectable({ providedIn: 'root' })` con el patrón antiguo de `CanActivate`; `inicioGuard` usa el estilo funcional.

## 5. Interceptores HTTP y manejo de errores

Un solo interceptor registrado, en `app.config.ts`: `provideHttpClient(withInterceptors([authInterceptor]))`. [BASE] (el archivo no cambió respecto de `d3a48cc`).

| Pieza | Archivo | Comportamiento | Origen |
| --- | --- | --- | --- |
| `authInterceptor` (`HttpInterceptorFn`) | `core/auth/interceptor/auth-interceptor.ts` | Lee `localStorage.getItem('authToken')` sin comprobar `window` (inconsistente con la regla SSR del propio `CLAUDE.md`); si existe, agrega `Authorization: Bearer <token>` a toda petición saliente (incluida la de `POST sesiones` si ya hubiera token). Después de `next(authReq)` encadena `tap({ error })` hacia `SesionVencidaService.atender`. | [BASE] + 1 línea [EXTENSIÓN] |
| `SesionVencidaService.atender(peticion, error)` | `core/auth/interceptor/sesion-vencida-service.ts` | Solo actúa ante `HttpErrorResponse` 401 que no sea `POST .../sesiones`; compara el token enviado con el vigente en `localStorage` (así solo una de varias peticiones simultáneas dispara el cierre); llama `AuthService.logout()`, muestra el aviso "Tu sesión venció, inicia sesión de nuevo" y navega a `/login`. `ToastService` se carga con `import()` dinámico. | [EXTENSIÓN] (D-F8) |
| `ErrorHandlerService.handleError(err, titulo, mensaje)` | `core/services/error-handler-service.ts` | Hace `console.error`, compone el texto desde `err.error` (mapa campo/valor, `mensaje`, `descripcion` o `err.message`) y muestra un toast de error con `ToastService`. | [BASE] |
| `ToastService` | `core/services/toast-service.ts` | `showInfo`, `showSuccess`, `showError` sobre SweetAlert2 en modo toast (`position: 'top-end'`). | [BASE] |
| Utilidades de error | `core/utils/errores-http.ts` y `CODIGOS_ERROR` en `core/constantes/procesos-academicos.ts` | `leerCodigoError` extrae `codigoError` de un 500; `erroresDeCampos` lee el mapa de un 400. | [EXTENSIÓN] |
| `parsearErrorBlob` | `core/utils/descargas.ts` | Operador RxJS que convierte un error HTTP con cuerpo `Blob` de vuelta a JSON para leer `codigoError`. Lo usan `descargarAnexo` y `descargarResolucion`. | [EXTENSIÓN] |

Código HTTP 403 y 500 no cierran la sesión (según `SesionVencidaService`, solo 401).

## 6. Servicios HTTP: servicio -> endpoints

Base de URL: `environment.apiUrl` (= `http://localhost:8080/api/unicauca/fiet/consejo`). Todos los servicios son `providedIn: 'root'`. Al corte, los 66 endpoints [EXTENSIÓN] coincidían con la afirmación de `frontend/CLAUDE.md`; hoy `frontend/CLAUDE.md` dice "Cubren los 76 endpoints del contrato": a los 66 se suman los 5 del panel de la notificación de la Resolución en `CancelacionAsignaturaService`, `buscarDocentes` en `ExamenSupletorioService` y los 4 de `NotificacionDocenteService`. El cálculo del corte era 7+8+8+7+9+12+6+9 = 66, y se verificó contando las llamadas `this.http.*` (más dos llamadas que el `grep` literal no ve en `SolicitudAcademicaService`, porque usan `.get` en la línea siguiente).

### 6.1 [EXTENSIÓN]: 9 servicios, 78 endpoints (76 al corte T12.37; T12.38 agregó el conteo y el borrado de respuestas en `solicitud-academica-service`)

| Servicio (`core/services/`) | Prefijo | Método -> endpoint |
| --- | --- | --- |
| `AsignaturaService` (`asignatura-service.ts`) | `/asignaturas` | `crearAsignatura` POST `/`; `crearAsignaturasDesdeArchivo` POST `/cargar/archivo` (FormData); `getAsignaturasPaginado` GET `/paginado`; `getAsignaturasFiltradas` GET `/filtro`; `getAsignaturasPorPrograma` GET `/programa/{uuidPrograma}`; `getAsignatura` GET `/{uuid}`; `actualizarAsignatura` PUT `/{uuid}` |
| `EstudianteService` (`estudiante-service.ts`) | `/estudiantes` | `crearEstudiante` POST `/`; `crearEstudiantesDesdeArchivo` POST `/cargar/archivo`; `getEstudiantesPaginado` GET `/paginado`; `getEstudiantesFiltrados` GET `/filtro`; `getMisAsignaturas` GET `/mis-asignaturas`; `getEstudiante` GET `/{uuid}`; `actualizarEstudiante` PUT `/{uuid}`; `agregarAsignaturaMatriculada` POST `/{uuid}/asignaturas`; `cambiarEstadoAsignatura` PATCH `/{uuid}/asignaturas/{uuidMatricula}/estado` |
| `FuncionarioAcademicoService` (`funcionario-academico-service.ts`) | `/funcionarios-academicos` | `crearFuncionarioAcademico` POST `/`; `crearFuncionariosAcademicosDesdeArchivo` POST `/cargar/archivo`; `getFuncionariosAcademicosPaginado` GET `/paginado`; `getFuncionariosAcademicosFiltrados` GET `/filtro`; `getFuncionarioAcademico` GET `/{uuid}`; `actualizarFuncionarioAcademico` PUT `/{uuid}` |
| `CatalogoAcademicoService` (`catalogo-academico-service.ts`) | `/catalogos-academicos` | `getTiposSolicitud` GET `/tipos-solicitud`; `getEtapasPorTipo` GET `/tipos-solicitud/{uuidTipo}/etapas`; `getTiposAnexoPorTipo` GET `/tipos-solicitud/{uuidTipo}/tipos-anexo`; `getEtiquetasPorRol` GET `/etiquetas?rol=`; `getSituaciones` GET `/situaciones`; `getProgramas` GET `/programas`; `asignarFuncionarioAcademico` PUT `/tipos-solicitud/{uuidTipo}/funcionario` |
| `SolicitudAcademicaService` (`solicitud-academica-service.ts`) | `/solicitudes-academicas` | `getSolicitudesEstudiante` GET `/estudiante`; `getSolicitudesFuncionario` GET `/funcionario`; `getSolicitudesDecano` GET `/decano`; `getSolicitud` GET `/{uuid}`; `getHistorial` GET `/{uuid}/historial`; `adjuntarAnexo` POST `/{uuid}/anexos` (FormData); `descargarAnexo` GET `/{uuid}/anexos/{uuidAnexo}` (blob); `adjuntarResolucion` POST `/{uuid}/resolucion` (FormData); `descargarResolucion` GET `/{uuid}/resolucion` (blob) |
| `CancelacionMatriculaService` (`cancelacion-matricula-service.ts`) | `/cancelaciones-matricula` | `getFormulario` GET `/formulario`; `radicar` POST `/` (FormData armado en `armarFormDataRadicacion`); `getDetalle` GET `/{uuid}`; `rechazarPorFuncionario` POST `/{uuid}/funcionario/rechazar`; `remitirADecano` POST `/{uuid}/funcionario/remitir`; `enviarRespuesta` POST `/{uuid}/funcionario/responder`; `aprobarPorDecano` POST `/{uuid}/decano/aprobar`; `rechazarPorDecano` POST `/{uuid}/decano/rechazar` |
| `CancelacionAsignaturaService` (`cancelacion-asignatura-service.ts`) | `/cancelaciones-asignatura` | Mismos ocho métodos y rutas que el anterior, con prefijo propio, más (T12.32) `getPlantillaNotificacion` GET `/{uuid}/funcionario/notificacion/plantilla`; `getNotificacion` GET `/{uuid}/funcionario/notificacion`; `reprogramarNotificacion` PUT `/{uuid}/funcionario/notificacion`; `registrarFirma` POST `/{uuid}/funcionario/notificacion/firma` (FormData); `reintentarNotificacion` POST `/{uuid}/funcionario/notificacion/reintentar` |
| `ExamenSupletorioService` (`examen-supletorio-service.ts`) | `/examenes-supletorios` | `getFormulario` GET `/formulario`; `radicar` POST `/`; `getDetalle` GET `/{uuid}`; `rechazarPorFuncionario` POST `/{uuid}/funcionario/rechazar`; `remitirADecano` POST `/{uuid}/funcionario/remitir`; `enviarRespuesta` POST `/{uuid}/funcionario/responder`; `enviarRecibo` POST `/{uuid}/funcionario/recibo`; `aprobarComprobante` POST `/{uuid}/funcionario/comprobante/aprobar`; `rechazarComprobante` POST `/{uuid}/funcionario/comprobante/rechazar`; `aprobarPorDecano` POST `/{uuid}/decano/aprobar`; `rechazarPorDecano` POST `/{uuid}/decano/rechazar`; `subirComprobante` POST `/{uuid}/estudiante/comprobante`; desde T12.28 `buscarDocentes` GET `/docentes` (filtro por nombre, documento o usuario, paginado) |
| `NotificacionDocenteService` (`notificacion-docente-service.ts`, T12.28) | `/notificaciones-docente` | `getMisNotificaciones` GET `/`; `getNotificacion` GET `/{uuid}`; `marcarLeida` PATCH `/{uuid}/leida`; `descargarAnexo` GET `/{uuid}/anexos/{uuidAnexo}` (blob) |

### 6.2 [BASE]: 7 servicios de dominio, 61 llamadas HTTP, más 2 auxiliares

| Servicio | Prefijo | Métodos (nº de llamadas HTTP) |
| --- | --- | --- |
| `AuthService` (`auth-service.ts`) | `/sesiones` | `login` POST `/sesiones` (1). Estado en `BehaviorSubject` (sección 7) |
| `UsuariosService` (`usuarios-service.ts`) | `/usuarios` | 11: `getTotalUsuarios` `/total`; `getTiposUsuario` `/tipos`; `getUsuariosPaginado`; `getUsuariosFiltrados`; `getUsuarios`; `getFuncionarios` `/funcionarios`; `getUsuario` `/{uuid}`; `crearUsuario` POST `?tipoUsuario=`; `crearUsuariosDesdeArchivo` POST `/cargar/archivo`; `actualizarUsuario` PUT `/{uuid}`; `cambiarContraseña` PATCH `/{uuid}` |
| `RolesService` (`roles-service.ts`) | `/roles` | 4: `getRoles`, listado paginado/filtrado, `getRol`, `actualizarRol` PUT |
| `TipoSolicitudService` (`tipo-solicitud-service.ts`) | `/tipos/solicitudes` | 10: obtención (lista, por uuid, paginado, filtrado, por perfil del solicitante, por nombre y perfil), `crearTipoSolicitud` POST, creación desde archivo POST, `actualizarTipoSolicitud` PUT |
| `SolicitudesService` (`solicitudes-service.ts`) | `/solicitudes` | 22: órdenes del día (`orden-del-dia` GET lista/paginado/por uuid/por estado/búsqueda, POST crear, PUT actualizar), solicitudes (GET lista/paginado/por uuid/por estado/por funcionario/búsquedas, `enviarSolicitud` POST `/` multipart, `enviarSolicitudPublica` POST `/public` multipart, `actualizarSolicitud` PUT), descargas/exportaciones (`/anexos/download`, `/exportar`, `/exportar/respuestas`, `/exportar/reunion`, blob) |
| `RespuestasService` (`respuesta-service.ts`) | `/respuestas` | 10: listados (`/paginado`, `/paginado/nombre-solicitud`, `/paginado/funcionario`, `/paginado/funcionario-nombre-solicitud`), `getRespuesta`, `getRespuestaPorSolicitud`, crear (POST `/{uuidSolicitud}`), `responderSolicitud` POST `/{uuid}/archivo`, `descargarRespuesta`, `eliminarArchivoRespuesta` PATCH `/{uuid}/archivo` |
| `LogService` (`log-service.ts`) | `/logs` | 3: `getLogs`, `getLogsPaginado`, `getLogsFiltrados` |
| `ErrorHandlerService`, `ToastService` | sin HTTP | utilitarios (sección 5) |

Nombres exactos de los métodos de lectura y búsqueda de `UsuariosService`, `RolesService`, `TipoSolicitudService`, `SolicitudesService` y `RespuestasService` que no están en la lista anterior: NO VERIFICADO uno por uno; el conteo por servicio sí se obtuvo contando `this.http.<verbo>`.

### 6.3 Convenciones de la capa HTTP verificadas

- Los servicios devuelven `Observable<...>` y no se suscriben a sí mismos, salvo `AuthService` (estado con `tap`/`switchMap`/`catchError`).
- Los `FormData` se arman dentro del servicio ([EXTENSIÓN]: `armarFormDataRadicacion`, `armarFormDataAnexo`); en `SolicitudesService.enviarSolicitud` ([BASE]) el JSON va como `Blob` `application/json` en la parte `solicitud` y los archivos en `archivos`.
- Los query params de la extensión se arman con `armarParametros` (`core/utils/parametros-http.ts`); los de Julián se concatenan a mano en el string de la URL (por ejemplo `RespuestasService`).
- Peticiones directas con `HttpClient` fuera de `core/services` (excepción a la regla de `CLAUDE.md`): `DescargarRespuestaComponent.descargar` (`shared/descargar-respuesta-component`) y `AnexosViewComponent` (`shared/others/anexos-view-component`, arma la URL con `environment.domain`). [BASE].
- `RespuestasService.descargarRespuesta` arma `environment.domain + this.url + ...`, y `this.url` ya contiene el host completo de `apiUrl`; la URL resultante repetiría el host. Ningún componente llama a ese método (búsqueda de `descargarRespuesta` en `src/app` sin specs: solo su definición). Es código sin uso en el estado actual. [BASE]

## 7. Sesión y token

| Aspecto | Implementación | Referencia |
| --- | --- | --- |
| Almacenamiento | `localStorage`: `authToken` (JWT) y `currentUser` (uuid del usuario) | `AuthService.login`/`logout` |
| Login | `POST /sesiones` -> guarda `currentUser` y `authToken` -> `UsuariosService.getUsuario(uuid)` -> emite en `usuarioSubject` | `core/services/auth-service.ts`, `login()` |
| Estado reactivo | `BehaviorSubject<UsuarioDTORespuesta | null>` (`usuario$`) y `BehaviorSubject<boolean>` (`usuarioCargado$`) | `AuthService` |
| Rehidratación al recargar | `App.ngOnInit` (`app.ts`): si hay `authToken`, llama `AuthService.inicializarUsuario()`; el contenido se oculta 200 ms con `setTimeout` (`loading`) | `app.ts`, `app.html` |
| Cierre | `AuthService.logout()` limpia el subject y quita las dos claves; lo invocan `AvatarSesionComponent.cerrarSesion` y `SesionVencidaService` | `shared/avatar/avatar-sesion-component` |
| Expiración | Solo se detecta cuando el backend responde 401 (`SesionVencidaService`). No hay verificación local del `exp` del JWT (NO VERIFICADO que no exista en otro sitio; no se encontró en `core/auth`) | |
| Decisión de roles | Cada ruta consulta el usuario al backend (`RoleGuard`); `HeaderMainComponent` toma `roles[0].nombre` como rol actual | `shared/headers/header-main-component` |
| SSR | `AuthGuard`, `RoleGuard`, `inicioGuard` y `App` comprueban `typeof window`; `authInterceptor` y partes de `AuthService` no | sección 5 |

Un hallazgo menor: `DescargarRespuestaComponent.getToken()` lee la clave `token`, no `authToken`. Funciona de todas maneras porque `authInterceptor` vuelve a fijar el encabezado `Authorization` con `authToken` (`headers.set`). NO VERIFICADO en ejecución.

## 8. Estado de la aplicación

No hay librería de estado (NgRx, signals globales ni store). Verificado por `package.json` (dependencias: Angular, PrimeNG, `@primeuix/themes`, Bootstrap, primeicons, primeflex, rxjs, sweetalert2, express, tslib, zone.js). Estado presente:

- Global: `AuthService` (usuario y bandera de carga) con `BehaviorSubject`. [BASE]
- Local de componente: propiedades de clase en los componentes de contenido (`solicitudes`, `filtradas`, `dialogoVisible`, `enviando`, `errores`...), con objetos planos como modelo de formulario. No se usa `FormGroup` ni `ReactiveFormsModule` (verificado por D-F de `CLAUDE.md`; el patrón de formularios es `[(value)]` sobre los inputs de `shared/inputs`).
- Señales de Angular: uso puntual `toSignal` en `FunAcadSolicitudesContentComponent` (`modo`) y en `CancelacionAsignaturaProcesoComponent` (`resultadoPedido` desde `queryParamMap`). [EXTENSIÓN]
- Parámetros de URL: `:uuid` de la solicitud en las tres rutas de detalle; query param `PARAMETRO_RESULTADO` que `BandejaSolicitudesAcademicasComponent.verSolicitud` agrega para las filas "Aprobada/Rechazada" de una Cancelación de Asignatura en Respuestas. [EXTENSIÓN]
- Refresco tras acciones: el detalle común pasa `recargar: () => this.cargar()` al componente de proceso (`EntradasComponenteProceso`); este lo invoca tras cada acción y se vuelven a pedir solicitud e historial en `forkJoin`. [EXTENSIÓN]

## 9. Layouts, componentes compartidos y genéricos

### 9.1 Layouts

| Layout | Archivo | Estructura | Usado por |
| --- | --- | --- | --- |
| `LayoutComponent` (`app-layout-component`) | `layouts/layout-component` | `div.app` con `aside.sidebar` (`select="[sidebar]"`), y columna con `[header]`, `[breadcrumb]`, `[main]`, `[footer]` (proyección de contenido) | `PageComponent` |
| `LayoutSimpleComponent` (`app-layout-simple-component`) | `layouts/layout-simple-component` | Contenedor Bootstrap con `app-header-component`, `[main]` y `app-footer-component` | `PageSimpleComponent` |

Ambos [BASE]. `PageComponent` (`app-page-component`, `shared/pages/page-component`) proyecta cuatro `ng-container *ngComponentOutlet` para sidebar, header, breadcrumb (con `inputs: breadcrumbInputs`) y main, más `app-footer-simple-component`. `PageSimpleComponent` (`app-page-simple-component`) solo recibe `mainComponent`.

### 9.2 Tabla de componentes compartidos y genéricos

"Usos" = número de archivos `.html`/`.ts` (sin specs) fuera de la propia carpeta del componente que contienen su selector como etiqueta. Los que se montan con `ngComponentOutlet` o referencia de clase (no por etiqueta) aparecen con 0.

| Selector | Entradas y salidas | Usos | Dónde se reutiliza (ejemplos verificados) | Origen |
| --- | --- | --- | --- | --- |
| `app-page-component` | in: `sidebarComponent`, `headerComponent`, `breadcrumbComponent`, `breadcrumbInputs`, `mainComponent` | todas las páginas de `core/<rol>/pages` con layout completo (34 rutas menos login y pública = 32; `core/*/pages/*/*.html` lo usa) | las 32 páginas con barra lateral | [BASE] |
| `app-page-simple-component` | in: `mainComponent` | 2 | `login-component.html`, `usuario-publico-solicitudes-component.html` | [BASE] |
| `app-content-component` | in: `title`, `bodyComponent` | envoltorios de `shared/pages/*-component` | 23 envoltorios (títulos en 08) | [BASE] |
| `app-sidebar-component` | in: `buttons: BotonSidebar[]` (`label`, `imgUrl`, `route`, `activaEn`, `children`); out: `buttonClicked` | 7 | los siete `side-bar-*-component` (uno por rol) | [BASE]; `children`/`activaEn` [EXTENSIÓN] (D-F30, D-F34) |
| `app-header-main-component` | sin entradas; lee `AuthService.usuario$` | 0 por etiqueta (se pasa como clase `header`) | todas las páginas con layout completo | [BASE] |
| `app-header-component`, `app-footer-component`, `app-footer-simple-component` | sin entradas | 1 cada uno | `LayoutSimpleComponent`, `PageComponent` | [BASE] |
| `app-avatar-sesion-component` | in: `avatarLabel`, `nombre`, `roles`, `currentRole` | 1 | `HeaderMainComponent`; menú "Mi usuario" y "Cerrar sesión" | [BASE]; "Cerrar sesión" ajustado [EXTENSIÓN] |
| `app-breadcrumb-component` | in: `role`, `breadcrumbs: {label, url?}[]` | 0 por etiqueta (clase `breadcrumb`) | todas las páginas con layout | [BASE] |
| `app-card-main-component` | in: `title`, `buttons: ButtonConfig[]`, `bodyComponent`, `bodyInputs`, `preTitleComponent`, `preTitleInputs` | 12 | tarjetas de listado de Julián (usuarios, roles, tipos, órdenes, logs, respuestas, solicitudes) | [BASE] |
| `app-table-generic-component` | in: `headers: TableHeader[]`, `data`, `actionTemplate`, `encabezadoAgrupado` | 5 | `BandejaSolicitudesAcademicasComponent`, pestañas `admin-*` y listados de Julián | [BASE]; `cellTemplate` y `encabezadoAgrupado` [EXTENSIÓN] (D-F29, D-F31) |
| `app-paginator` | in: `totalPages`, `currentPage`; out: `pageChange` | 16 | casi todos los listados | [BASE] |
| `app-barra-busqueda-component` | in: `placeholder`, `value`; out: `valueChange` | 13 | filtros de listados y de la bandeja | [BASE] |
| `app-button-component` | in: `imgUrl`, `color`, `width`, `height`; out: `clicked` | 14 | botones de ícono en filas (`edit.svg`, `eye.svg`, `folder.svg`...) | [BASE] |
| `app-simple-button-component` | in: `label`, `color`, `width`, `height`, `disabled`, `tipo`, `contorno`; out: `clicked` | 20 | acciones de procesos académicos y administración | [BASE]; `contorno` y `tipo` [EXTENSIÓN] (D-F21, D-F31) |
| `app-button-group-component`, `app-simple-button-group-component`, `app-upload-component` | ver archivos | 0 / 1 / 1 | `OrdenDelDiaVisualizadorComponent` (grupo simple); `app-button-group-component` sin usos por etiqueta (NO VERIFICADO si se instancia dinámicamente) | [BASE] |
| `app-generic-dialog-form-component` | in: `visible`, `title`, `textoBoton`, `textoCancelar`, `cerrarConEscape`; out: `visibleChange`, `save` | 16 | todos los diálogos con formulario, incluidos los de los tres procesos | [BASE]; `textoBoton`, `textoCancelar`, `cerrarConEscape` [EXTENSIÓN] (D-F12, D-F31) |
| `app-generic-dialog-info-component` | in: `visible`, `title`, `data`, `showCustomContent`; out: `visibleChange` | 13 | diálogos de "Información de ..." (solo lectura) | [BASE] |
| `app-generic-dialog-steps-form-component` | in: `title`, `steps`; out: `visibleChange`, `save` | 3 | `TipoSolicitudesContentComponent` y los dos `enviar-solicitud-*` | [BASE] |
| `app-generic-dialog-upload-file-component` | in: `visible`, `title`, `uploadedFile`; out: `visibleChange`, `fileSelected`, `fileRemoved`, `upload` | 2 | cargas Excel de usuarios y tipos de solicitud | [BASE] |
| `app-input-text-component`, `app-input-text-tarea-component` | in: `label`, `value`, `required`, `forceValidation`; out: `valueChange` | 14 / 13 | todos los formularios | [BASE] |
| `app-input-select-component` | in: `label`, `options`, `value`, `required`, `forceValidation`; out: `valueChange`, `open` | 16 | formularios y diálogos | [BASE] |
| `app-input-date-component`, `app-input-password-component` | in: `label`, `value`, `required`; out: `valueChange` | 3 / 4 | órdenes del día, supletorio, login | [BASE] |
| `app-input-anexo-upload-component` | in: `tipoAnexo`, `uploadedFile`; out: `fileSelected`, `fileRemoved` | 9 | formularios del Estudiante, diálogos de procesos (`comoTipoAnexo` adapta el tipo; D-F6) | [BASE] |
| `app-anexos-view-component` | in: `anexos` | 3 | visor de anexos de Julián | [BASE] |
| `app-anexos-academicos-component` | in (requeridas): `uuidSolicitud`; in: `anexos` | 1 | detalle común (`DetalleSolicitudAcademicaComponent`): "Visualizar" y "Descargar" | [EXTENSIÓN] |
| `app-carga-excel-academica-component` | in: `visible`, `titulo`*, `encabezados`*, `regla`, `reglas`, `tituloValores`, `valores`, `describir`, `nombrePlural`, `cargar`*; out: `visibleChange`, `cargado` | 3 | pestañas Asignaturas, Estudiantes y Funcionarios académicos (D-F19, D-F22) | [EXTENSIÓN] |
| `app-datos-usuario-academico-component` | in: `datos`*, `errores`, `edicion` | 2 | altas y ediciones de Estudiante y Funcionario académico (D-F24) | [EXTENSIÓN] |
| `app-datos-estudiante-sesion-component` | in: `codigoEstudiantil` | 3 | primer bloque de los tres formularios del Estudiante (D-F33) | [EXTENSIÓN] |
| `app-resumen-solicitud-component` | in (requerida): `solicitud` | 3 | cabecera de los diálogos de acción de los tres procesos | [EXTENSIÓN] |
| `app-stepper-formulario-component` | in: `pasos` | 3 | formularios del Estudiante | [EXTENSIÓN] |
| `app-enlace-formato-component` | in (requerida): `archivo` | 2 | catálogo del Estudiante y formulario de Supletorio (D-F41) | [EXTENSIÓN] |
| `app-cargar-respuesta-component`, `app-descargar-respuesta-component`, `app-dialog-gestion-respuesta-component`, `app-change-password-component` | ver código | 1 / 4 / 2 / 1 | respuestas del flujo de Consejo y cambio de contraseña en "Mi usuario" | [BASE] |
| `app-bandeja-solicitudes-academicas-component` | in (requerida): `rol: RolEtiqueta`; in: `modo: 'solicitudes' | 'respuestas'` | 3 por etiqueta | `EstSolicitudesContent`, `DecSolicitudesContent`, `FunAcadSolicitudesContent` | [EXTENSIÓN] |
| `app-detalle-solicitud-academica-component` | in (requerida): `rol` | 3 por etiqueta | los tres `*-detalle-solicitud-content-component` | [EXTENSIÓN] |
| `app-cancelacion-matricula-proceso-component`, `app-cancelacion-asignatura-proceso-component`, `app-examen-supletorio-proceso-component` | in (todas requeridas): `uuidSolicitud`, `solicitud`, `rol`, `recargar` | 0 por etiqueta | cargados por `ngComponentOutlet` desde `COMPONENTES_PROCESO` (`detalle-solicitud-academica-component/componentes-proceso.ts`) | [EXTENSIÓN] |

Componentes agregados después del corte (12; se listan sin la columna de usos, que no se recalculó):

| Componente | Para qué | Origen |
| --- | --- | --- |
| `shared/others/documento-adicional-component` | Campo opcional "Documento adicional de la respuesta" (PDF, 5 MB) en las acciones del Funcionario que cierran la solicitud, solo en Matrícula y Supletorio (D-F45, D-F48) | [EXTENSIÓN] |
| `shared/others/selector-docente-component` | Buscador del profesor con rol Docente al aprobar el pago del supletorio (D-F44) | [EXTENSIÓN] (T12.28) |
| `shared/others/comunicacion-respuesta-component` | Mensaje inmediato al estudiante en el diálogo "Enviar la respuesta al estudiante" de Cancelación de Asignatura; desde T12.38 siempre se envía, sin casilla, y el diálogo suma los textos editables del estudiante y de DARCA (`TextoComunicacionComponent`) (D-F47, D-F51) | [EXTENSIÓN] (T12.32, T12.38) |
| `shared/others/texto-comunicacion-component` | Texto editable con vista "Así lo recibirá..." de la comunicación al estudiante y del correo a DARCA (D-F51) | [EXTENSIÓN] (T12.38) |
| `shared/others/eliminacion-respuestas-component` | Borrado de respuestas por rango en "Solicitudes respondidas" del Funcionario, con conteo y confirmación (D-F51) | [EXTENSIÓN] (T12.38) |
| `shared/others/programacion-resolucion-component` | Fecha y hora, número, año y decisión de la comunicación programada de la Resolución (D-F47) | [EXTENSIÓN] (T12.32) |
| `shared/others/notificacion-resolucion-component` | Panel "Notificación de la Resolución" del Funcionario: estado, Resolución vigente (sin firmar o firmada), reprogramar, registrar la firma y reintentar (D-F47, D-F48) | [EXTENSIÓN] (T12.32, T12.33) |
| `shared/others/decision-decano-component` | Decisión del Decano con un solo botón "Enviar decisión" en los tres procesos, sin preselección (D-F50) | [EXTENSIÓN] (T12.35, sin confirmar en git) |
| `core/docente/pages/docente-notificaciones-component`, `core/docente/pages/docente-detalle-notificacion-component` | Páginas del Docente | [EXTENSIÓN] (T12.28) |
| `shared/pages/doc-notificaciones-component`, `shared/pages/doc-detalle-notificacion-component` | Envoltorios de las páginas del Docente | [EXTENSIÓN] (T12.28) |
| `shared/pages/content/doc-notificaciones-content-component`, `shared/pages/content/doc-detalle-notificacion-content-component` | Lista de notificaciones y detalle de solo lectura de la solicitud | [EXTENSIÓN] (T12.28) |

Cifras al corte: 145 componentes en total; de ellos 2 son páginas sin guard (login y público), 32 páginas con `app-page-component`, 23 envoltorios `shared/pages/*-component`, 7 barras laterales, y el resto contenidos, diálogos y piezas. Al 2026-10-21 son 157: se suman 2 páginas, 2 envoltorios y 2 contenidos del Docente y 6 piezas en `shared/others`.

### 9.3 Punto de extensión `COMPONENTES_PROCESO`

`shared/pages/content/detalle-solicitud-academica-component/componentes-proceso.ts` exporta `COMPONENTES_PROCESO: Partial<Record<ProcesoAcademico, () => Promise<Type<unknown>>>>` con tres entradas (`'cancelaciones-matricula'`, `'cancelaciones-asignatura'`, `'examenes-supletorios'`), cada una un `import()` dinámico. `DetalleSolicitudAcademicaComponent.prepararProceso` calcula el proceso con `procesoDeTipoSolicitud(solicitud.tipoSolicitud)` y monta el componente con `*ngComponentOutlet="componenteProceso; inputs: entradasProceso"` (el HTML lo declara en el bloque `data-bloque-proceso`). Resultado: cada proceso queda en su propio chunk diferido.

## 10. PrimeNG, SweetAlert2 y Bootstrap

PrimeNG (verificado por `grep "from 'primeng/..."` sobre `src`):

| Módulo | Dónde se importa |
| --- | --- |
| `primeng/dialog` (`DialogModule`) | `generic-dialog-form`, `generic-dialog-info`, `generic-dialog-steps-form`, `generic-dialog-upload-file`, `dialog-gestion-respuesta`, `orden-del-dia-visualizador` (6 archivos) |
| `primeng/button` (`ButtonModule`) | 7 archivos (los diálogos genéricos, `upload-component`, `cargar-respuesta`, `orden-del-dia-visualizador`...) |
| `primeng/fileupload` (`FileUpload`) | 3 archivos (`upload-component`, `input-anexo-upload-component`, `cargar-respuesta-component`) |
| `primeng/floatlabel` (`FloatLabelModule`) | `input-password-component` |
| `primeng/api` (`MessageService`) | `app.config.ts` (provider) y consumidores |
| `primeng/config` (`providePrimeNG`) y `@primeuix/themes/aura` (`Aura`) | `app.config.ts` |

No se usa `p-table`, `p-dropdown` ni `p-inputtext` (no aparecen imports). El `Avatar` de PrimeNG que menciona `frontend/CLAUDE.md` no está importado: `AvatarSesionComponent` es un componente propio (Bootstrap `dropdown-menu`), discrepancia menor de la documentación.

Otros:

- Bootstrap 5.3: CSS y JS cargados desde `angular.json` (`styles`: `bootstrap.min.css`; `scripts`: `popper.min.js` y `bootstrap.min.js`); aporta rejilla, utilidades, `form-control`, `form-select`, `table`, `nav-tabs`, `dropdown` (menú del avatar).
- SweetAlert2: solo `ToastService` (`import Swal from 'sweetalert2'`).
- PrimeFlex 4: está en `package.json` pero ninguna hoja ni `angular.json` lo carga (`grep -rn primeflex src angular.json` sin resultados). Dependencia sin efecto.
- `primeicons`: se carga en `angular.json` y en `styles.css`.
- Íconos de la aplicación: SVG en `public/buttons/*.svg`.

## 11. Estilos

- `src/styles.css` (42 líneas): importa `primeicons`, resetea `html, body` y define los tokens en `:root` (`--navy-900`, `--navy-800`, `--navy-700`, `--navy-ink`, `--card-navy`, `--accent`, `--link`, `--green`, `--red`, `--panel`, `--line`, `--line-soft`, `--text`, `--muted`, `--zebra`, `--sidebar-w`, `--fuente`, más variables `--bs-*`). [BASE] + [EXTENSIÓN] (D-F27).
- Cada componente tiene su `.css`. Compartidos de la extensión en `shared/pages/content/`: `estilos-academicos.css`, `estilos-formularios.css` y `tabla-asignaturas.css`, incluidos mediante `styleUrls` en los componentes que los necesitan (D-F32) para que no entren al bundle inicial.
- El presupuesto `anyComponentStyle` (4 kB de aviso, 8 kB de error) aplica por componente; no se midió el tamaño de ninguna hoja.

## 12. Environments

Existe un solo archivo, `src/enviroments/environment.ts` [BASE]:

```
production: false
apiUrl: 'http://localhost:8080/api/unicauca/fiet/consejo'
domain: 'http://localhost:8080/'
```

No hay `environment.prod.ts` ni `fileReplacements` en `angular.json` (la configuración `production` no sustituye archivos). Por tanto, en el estado actual el build de producción usa la misma URL de `localhost:8080`. NO VERIFICADO si el despliegue la sobrescribe por otro mecanismo (el `dockerfile` no pasa variables de entorno al build). `environment.domain` se usa en `AnexosViewComponent`, `DescargarRespuestaComponent` y `RespuestasService.descargarRespuesta`.

## 13. Build, SSR, despliegue y presupuestos

Datos de `angular.json` (sin cambios respecto de `d3a48cc`):

| Elemento | Valor |
| --- | --- |
| Proyecto | `front-fiet-sc` |
| Builder de build | `@angular/build:application` con `browser: src/main.ts`, `server: src/main.server.ts`, `outputMode: server`, `ssr.entry: src/server.ts`, `polyfills: ["zone.js"]` |
| Assets | `{ glob: "**/*", input: "public" }` (todo `public/`, incluida la subcarpeta `formatos/`) |
| Styles | `src/styles.css`, `primeicons.css`, `bootstrap.min.css` |
| Scripts | `popper.min.js`, `bootstrap.min.js` |
| Configuración `production` (por defecto) | `outputHashing: all`; presupuestos: `initial` aviso 500 kB, error 1 MB; `anyComponentStyle` aviso 4 kB, error 8 kB |
| Configuración `development` | `optimization: false`, `extractLicenses: false`, `sourceMap: true` |
| `serve` | `@angular/build:dev-server`, por defecto `development` |
| `test` | `@angular/build:karma` con `zone.js/testing`, `tsconfig.spec.json`, assets de `public`, estilos `src/styles.css`. No hay `karma.conf.js` en la raíz de `frontend/` (NO VERIFICADO que use la configuración por defecto de Angular) |
| TypeScript | `strict`, `noImplicitReturns`, `strictTemplates`, target ES2022 (`tsconfig.json`) |
| Scripts npm | `start` (`ng serve`), `build` (`ng build`), `watch`, `test` (`ng test`), `serve:ssr:front-fiet-sc` (`node dist/front-fiet-sc/server/server.mjs`) |
| Docker | `dockerfile`: etapa `node:20-alpine` que corre `npm install` y `npm run build`; etapa final copia `dist`, instala `--omit=dev`, expone `4200` y ejecuta `node dist/front-fiet-sc/server/server.mjs` |
| Servidor Express | `src/server.ts` con `AngularNodeAppEngine`, `express.static(browser, maxAge '1y')` y manejador SSR para el resto |

Discrepancias a tener en cuenta:

- `frontend/CLAUDE.md` dice que el bundle inicial "no debe pasar de 840 kB". En `angular.json` el presupuesto efectivo es 500 kB (aviso) y 1 MB (error). El valor de 840 kB es una meta interna del equipo y no está forzado por la herramienta. El tamaño real del bundle NO VERIFICADO (no se ejecutó el build). El directorio `dist/` existe pero no se inspeccionó.
- `CLAUDE.md` indica que `angular.json` publica `formatos/` con una regla de `assets`; en realidad la única regla es el glob de todo `public/`, que ya incluye `public/formatos/`.
- Hay dos versiones de estilos de nombre de ruta de menú: `sec-fiet/respuestas` sin barra inicial en el menú de Secretaría FIET.

## 14. Resumen de arquitectura (para la monografía)

- Aplicación Angular 20 de componentes standalone, sin NgModules, con carga diferida por ruta y SSR con hidratación de eventos (`provideClientHydration(withEventReplay())` en `app.config.ts`).
- Seguridad de acceso en el cliente: `AuthGuard` (existe token) + `RoleGuard` (rol del usuario consultado al backend en cada navegación) y `inicioGuard` para la raíz. La autorización real está en el backend; el cliente decide botones con los datos que el backend devuelve (`accionesDisponibles`, `etiqueta`, ver 08).
- Reutilización: tres niveles (página, envoltorio, contenido), más bandeja y detalle comunes a los tres roles académicos, y un punto de extensión diferido para el componente de cada proceso.
- La extensión agregó 70 componentes, 9 servicios, 19 rutas y 62 specs (cifras al 2026-10-21) sin tocar los archivos de configuración de build ni `app.config.ts`.
- Las respuestas al estudiante salen también por correo; el frontend no envía correos, solo ofrece en los diálogos del Funcionario el documento adicional, el mensaje inmediato y la programación de la comunicación ([correo-electronico.md](correo-electronico.md)).
