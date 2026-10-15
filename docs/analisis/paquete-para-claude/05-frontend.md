

===== 07-frontend-arquitectura.md =====

# 07 - Arquitectura del frontend

Alcance: `frontend/` (Angular 20 con PrimeNG y Bootstrap 5). Análisis de solo lectura. Rutas relativas a `frontend/src/app/` salvo indicación.

Convenciones de este documento:

- [BASE] = existe en el commit inicial `d3a48cc` ("Inicializa el repositorio del trabajo de grado"), es decir, el prototipo de Julián David Camacho Erazo. La clasificación se obtuvo comparando `git ls-tree d3a48cc` contra el árbol de trabajo.
- [EXTENSIÓN] = aparece después de ese commit (trabajo de grado: Cancelación de Matrícula, Cancelación de Asignatura, Examen Supletorio y administración académica).
- NO VERIFICADO = no se pudo comprobar por lectura estática (no se ejecutó build, tests ni la aplicación).

## 1. Cifras generales

| Concepto | Total | [BASE] | [EXTENSIÓN] | Cómo se midió |
| --- | --- | --- | --- | --- |
| Archivos bajo `frontend/src` (sin `node_modules` ni `dist`) | 675 | 442 | 233 | `find` y `git ls-tree d3a48cc` |
| Componentes (`@Component` en `.ts` que no son spec) | 145 | 87 | 58 | `grep -l "@Component("` en ambos árboles |
| Servicios HTTP/aplicación en `core/services` (`.ts` sin spec) | 17 | 9 | 8 | listado de `core/services` y de `d3a48cc` |
| Rutas en `app.routes.ts` (entradas `path:`) | 35 (34 con pantalla más la raíz `''`) | 18 | 17 | `grep -c "path:"` y `git show d3a48cc:.../app.routes.ts` |
| Archivos `*.spec.ts` | 136 | 87 | 49 (0 eliminados) | `comm` entre listas de specs |
| Casos de prueba (`it(`) | 524 | NO VERIFICADO por separado | NO VERIFICADO por separado | `grep -E "^\s*it\("` sobre todos los specs |
| Interfaces de modelo en `core/models/**/DTO*` | 101 archivos | NO VERIFICADO | NO VERIFICADO | `ls` |
| Plantillas `.html` / hojas `.css` en `src/app` | 144 / 97 | NO VERIFICADO | NO VERIFICADO | `find` |

Nota sobre rutas [EXTENSIÓN]: 7 del Estudiante, 4 del Funcionario Académico, 5 del Decano y 1 del Secretario General (`sec-general/administracion-academica`), total 17. Coincide con 18 + 17 = 35.

Fuente de las decisiones de diseño de la extensión: `frontend/CLAUDE.md` (decisiones D-F1 a D-F43). Todo lo que se afirma abajo se contrastó con el código; donde `CLAUDE.md` y el código difieren se señala en la sección 14.

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

Observaciones verificadas:

- No hay ruta comodín (`**`) ni de "no encontrado" en `app.routes.ts`. Una URL desconocida no tiene componente asociado. NO VERIFICADO el comportamiento en ejecución.
- El rol Decano también está en `usuario-fiet` y `usuario-fiet/solicitudes` (rutas [BASE] de Julián), de modo que un Decano puede abrir esas pantallas por URL directa. El inicio por defecto lo manda a `/decano` (`rutaDeInicio`).
- Las rutas `funcionario-academico/solicitudes` y `funcionario-academico/respuestas` montan el mismo contenido (`FunAcadSolicitudesComponent` -> `FunAcadSolicitudesContentComponent`); la diferencia es `data.modo`, que el contenido lee con `ActivatedRoute` (`modoDe`) y pasa como `[modo]` a `BandejaSolicitudesAcademicasComponent` (D-F38).
- `RoleGuard` compara contra `route.data['roles']` con `some(...)`: basta uno de los roles. `sidebar` de la Secretaría FIET usa `route: 'sec-fiet/respuestas'` sin barra inicial (`core/secretariaFiet/components/side-bar-secretaria-fiet-component/...html`); `SidebarComponent.normalizar` agrega la barra.

### 3.2 Redirección de inicio

`core/utils/rutas-por-rol.ts` -> `rutaDeInicio(roles)`. Orden de evaluación: Secretario General (`/sec-general`), Funcionario (`/funcionario`), Secretaria Decanatura FIET (`/sec-fiet`), Decano (`/decano`), Funcionario Académico (`/funcionario-academico`), Estudiante (`/estudiante`), cualquier rol de `ROLES_FIET` (`/usuario-fiet`), de lo contrario `null`. La usan `LoginFormComponent.onSubmit`, `inicioGuard` y `AvatarSesionComponent` ("Mi usuario"). [EXTENSIÓN] como función; en el commit base la lógica estaba dentro de `LoginFormComponent` NO VERIFICADO el texto exacto previo.

### 3.3 Rutas de servidor (SSR)

`app.routes.server.ts` (`serverRoutes`): `estudiante/solicitudes/:uuid`, `funcionario-academico/solicitudes/:uuid` y `decano/solicitudes/:uuid` con `RenderMode.Client`; `''` con `RenderMode.Client`; `'**'` con `RenderMode.Prerender`. El commit base solo tenía `'**'` con `Prerender`. [BASE] + [EXTENSIÓN].

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

Base de URL: `environment.apiUrl` (= `http://localhost:8080/api/unicauca/fiet/consejo`). Todos los servicios son `providedIn: 'root'`. Los 66 endpoints [EXTENSIÓN] coinciden con la afirmación de `frontend/CLAUDE.md` ("cubren los 66 endpoints del contrato"); el cálculo es 7+8+8+7+9+12+6+9 = 66, y se verificó contando las llamadas `this.http.*` (más dos llamadas que el `grep` literal no ve en `SolicitudAcademicaService`, porque usan `.get` en la línea siguiente).

### 6.1 [EXTENSIÓN]: 8 servicios, 66 endpoints

| Servicio (`core/services/`) | Prefijo | Método -> endpoint |
| --- | --- | --- |
| `AsignaturaService` (`asignatura-service.ts`) | `/asignaturas` | `crearAsignatura` POST `/`; `crearAsignaturasDesdeArchivo` POST `/cargar/archivo` (FormData); `getAsignaturasPaginado` GET `/paginado`; `getAsignaturasFiltradas` GET `/filtro`; `getAsignaturasPorPrograma` GET `/programa/{uuidPrograma}`; `getAsignatura` GET `/{uuid}`; `actualizarAsignatura` PUT `/{uuid}` |
| `EstudianteService` (`estudiante-service.ts`) | `/estudiantes` | `crearEstudiante` POST `/`; `crearEstudiantesDesdeArchivo` POST `/cargar/archivo`; `getEstudiantesPaginado` GET `/paginado`; `getEstudiantesFiltrados` GET `/filtro`; `getMisAsignaturas` GET `/mis-asignaturas`; `getEstudiante` GET `/{uuid}`; `actualizarEstudiante` PUT `/{uuid}`; `agregarAsignaturaMatriculada` POST `/{uuid}/asignaturas`; `cambiarEstadoAsignatura` PATCH `/{uuid}/asignaturas/{uuidMatricula}/estado` |
| `FuncionarioAcademicoService` (`funcionario-academico-service.ts`) | `/funcionarios-academicos` | `crearFuncionarioAcademico` POST `/`; `crearFuncionariosAcademicosDesdeArchivo` POST `/cargar/archivo`; `getFuncionariosAcademicosPaginado` GET `/paginado`; `getFuncionariosAcademicosFiltrados` GET `/filtro`; `getFuncionarioAcademico` GET `/{uuid}`; `actualizarFuncionarioAcademico` PUT `/{uuid}` |
| `CatalogoAcademicoService` (`catalogo-academico-service.ts`) | `/catalogos-academicos` | `getTiposSolicitud` GET `/tipos-solicitud`; `getEtapasPorTipo` GET `/tipos-solicitud/{uuidTipo}/etapas`; `getTiposAnexoPorTipo` GET `/tipos-solicitud/{uuidTipo}/tipos-anexo`; `getEtiquetasPorRol` GET `/etiquetas?rol=`; `getSituaciones` GET `/situaciones`; `getProgramas` GET `/programas`; `asignarFuncionarioAcademico` PUT `/tipos-solicitud/{uuidTipo}/funcionario` |
| `SolicitudAcademicaService` (`solicitud-academica-service.ts`) | `/solicitudes-academicas` | `getSolicitudesEstudiante` GET `/estudiante`; `getSolicitudesFuncionario` GET `/funcionario`; `getSolicitudesDecano` GET `/decano`; `getSolicitud` GET `/{uuid}`; `getHistorial` GET `/{uuid}/historial`; `adjuntarAnexo` POST `/{uuid}/anexos` (FormData); `descargarAnexo` GET `/{uuid}/anexos/{uuidAnexo}` (blob); `adjuntarResolucion` POST `/{uuid}/resolucion` (FormData); `descargarResolucion` GET `/{uuid}/resolucion` (blob) |
| `CancelacionMatriculaService` (`cancelacion-matricula-service.ts`) | `/cancelaciones-matricula` | `getFormulario` GET `/formulario`; `radicar` POST `/` (FormData armado en `armarFormDataRadicacion`); `getDetalle` GET `/{uuid}`; `rechazarPorFuncionario` POST `/{uuid}/funcionario/rechazar`; `remitirADecano` POST `/{uuid}/funcionario/remitir`; `enviarRespuesta` POST `/{uuid}/funcionario/responder`; `aprobarPorDecano` POST `/{uuid}/decano/aprobar`; `rechazarPorDecano` POST `/{uuid}/decano/rechazar` |
| `CancelacionAsignaturaService` (`cancelacion-asignatura-service.ts`) | `/cancelaciones-asignatura` | Mismos ocho métodos y rutas que el anterior, con prefijo propio |
| `ExamenSupletorioService` (`examen-supletorio-service.ts`) | `/examenes-supletorios` | `getFormulario` GET `/formulario`; `radicar` POST `/`; `getDetalle` GET `/{uuid}`; `rechazarPorFuncionario` POST `/{uuid}/funcionario/rechazar`; `remitirADecano` POST `/{uuid}/funcionario/remitir`; `enviarRespuesta` POST `/{uuid}/funcionario/responder`; `enviarRecibo` POST `/{uuid}/funcionario/recibo`; `aprobarComprobante` POST `/{uuid}/funcionario/comprobante/aprobar`; `rechazarComprobante` POST `/{uuid}/funcionario/comprobante/rechazar`; `aprobarPorDecano` POST `/{uuid}/decano/aprobar`; `rechazarPorDecano` POST `/{uuid}/decano/rechazar`; `subirComprobante` POST `/{uuid}/estudiante/comprobante` |

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

Cifras: 145 componentes en total; de ellos 2 son páginas sin guard (login y público), 32 páginas con `app-page-component`, 23 envoltorios `shared/pages/*-component`, 7 barras laterales, y el resto contenidos, diálogos y piezas.

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
- La extensión agregó 58 componentes, 8 servicios, 17 rutas y 49 specs sin tocar los archivos de configuración de build ni `app.config.ts`.


===== 08-frontend-pantallas.md =====

# 08 - Catálogo de pantallas y diálogos del frontend

Alcance: todas las rutas de `frontend/src/app/app.routes.ts` y los diálogos que abre cada una. Análisis de solo lectura. Rutas de archivo relativas a `frontend/src/app/` salvo indicación. Etiquetas: [BASE] = prototipo de Julián (commit `d3a48cc`); [EXTENSIÓN] = trabajo de grado; NO VERIFICADO = no comprobado por lectura estática (no se ejecutó la aplicación).

## 1. Cifras

| Concepto | Total | [BASE] | [EXTENSIÓN] |
| --- | --- | --- | --- |
| Rutas con pantalla (sin la raíz `''`) | 34 | 17 | 17 |
| Pantallas con layout completo (`app-page-component`) | 32 | 16 | 16 |
| Pantallas con layout simple (`app-page-simple-component`) | 2 (`login`, `usuario-publico/solicitudes`) | 2 | 0 |
| Instancias de diálogo genérico en plantillas (`app-generic-dialog-form/info/steps-form/upload-file-component`) | 39 | 28 | 11 |
| Diálogo propio con `p-dialog` directo (`DialogGestionRespuestaComponent`) | 1 | 1 | 0 |
| Componentes totales | 145 | 87 | 58 |
| Specs (`*.spec.ts`) | 136 (524 `it(`) | 87 | 49 |

Pantallas por rol (rutas): Secretario General 9 (8 [BASE] + 1 [EXTENSIÓN]), Funcionario 3, Secretaria Decanatura FIET 2, Usuario FIET 2, Público 2 (incluye `login`), Estudiante 7, Funcionario Académico 4, Decano 5. Suma 34.

Nota sobre los diálogos: un mismo `app-generic-dialog-form-component` de los tres componentes de proceso (`*-proceso-component`) sirve para todas las acciones; cambia `title`, `textoBoton` y el contenido con `@if`. Por eso el recuento de instancias (39) es menor que el de diálogos visibles para el usuario (ver secciones 5 a 8).

## 2. Cómo se decide qué botones aparecen

Resumen por familia de pantalla, verificado en el código:

| Familia | Mecanismo | Referencia |
| --- | --- | --- |
| Acceso a la ruta | `AuthGuard` + `RoleGuard` con `data.roles` (no se calculan botones aquí) | `app.routes.ts` |
| Menú lateral | Cada rol tiene su `side-bar-*-component` con una lista fija de ítems; el rol decide el menú porque la página de ese rol monta su barra | `core/<rol>/components/side-bar-*/*.html` |
| Procesos académicos (los tres) | Los botones de acción salen de `solicitud.accionesDisponibles` (lista de códigos que entrega el backend), filtrada por las acciones que maneja cada proceso (`ACCIONES_DEL_PROCESO`). El cliente nunca deduce las acciones del rol | `*-proceso-component.ts`, getter `accionesVisibles` |
| Excepción dentro de los procesos | En `CancelacionAsignaturaProcesoComponent.accionesVisibles`, si `rol === ESTUDIANTE` devuelve `[]` antes de filtrar | `cancelacion-asignatura-proceso-component.ts` línea ~243 |
| Texto, avisos y colores | Funcionario y Decano: dependen de `etapaCodigo`. Estudiante: solo de `etiqueta` y `accionesDisponibles`, nunca de `etapaCodigo` (D-F23) | `detalle-solicitud-academica-component.ts`; `examen-supletorio-proceso-component.ts` (`avisoEtapa`, `enEtapa`) |
| Descarga de la Resolución | Botón visible si `solicitud.puedeDescargarResolucion` | `detalle-solicitud-academica-component.html` (`data-boton-resolucion`) |
| Escaneo de la Resolución | El campo de carga del escaneo aparece solo cuando `accionActiva` es `RECHAZAR_FUNCIONARIO` o `ENVIAR_RESPUESTA` en Cancelación de Matrícula y Cancelación de Asignatura. Supletorio no lo tiene | `*-proceso-component.html` bloque `data-campo-resolucion` |
| Modo editable de la tabla de asignaturas | `editable` es verdadero si `accionesVisibles` incluye `REMITIR_DECANO` (el Funcionario evalúa por fila) | `cancelacion-*-proceso-component.ts`, getter `editable` |
| Decisión por fila (Decano, Cancelación de Asignatura) | `decide` es verdadero si `accionesVisibles` incluye `APROBAR_DECANO`; entonces se muestran "Aceptar tal como está" y "Enviar decisión modificada" en lugar de un único botón de aprobación | `cancelacion-asignatura-proceso-component.html` (`data-accion="ACEPTAR_EVALUACION"` y `"DECISION_MODIFICADA"`) |
| Bandeja del Funcionario | `modo` (`solicitudes` o `respuestas`) decide filas y columnas; `filtraPorCodigo` solo para el Funcionario | `bandeja-solicitudes-academicas-component.ts` |
| Administración académica | Sin filtrado por rol dentro del componente: el mismo `AdminAcademicaComponent` se monta en `decano/administracion-academica` y en `sec-general/administracion-academica`; la restricción es la de la ruta | `app.routes.ts` |
| Pantallas de Julián | Botones fijos por pantalla (editar, ver, y los de tarjeta definidos en `buttonsCard` dentro de `ngOnInit`). No hay lógica por estado de la entidad en los botones, salvo habilitaciones puntuales de diálogo NO VERIFICADO | `shared/pages/content/*-content-component` |

Lista completa de acciones y su código: `ACCIONES_ACADEMICAS` en `core/constantes/procesos-academicos.ts` (`RADICAR`, `RECHAZAR_FUNCIONARIO`, `REMITIR_DECANO`, `APROBAR_DECANO`, `RECHAZAR_DECANO`, `ENVIAR_RESPUESTA`, `ENVIAR_RECIBO`, `SUBIR_COMPROBANTE`, `APROBAR_COMPROBANTE`, `RECHAZAR_COMPROBANTE`).

Acciones manejadas por proceso (constante `ACCIONES_DEL_PROCESO` en cada componente de proceso):

| Proceso | Acciones |
| --- | --- |
| Cancelación de Matrícula | RECHAZAR_FUNCIONARIO, REMITIR_DECANO, APROBAR_DECANO, RECHAZAR_DECANO, ENVIAR_RESPUESTA |
| Cancelación de Asignatura | RECHAZAR_FUNCIONARIO, REMITIR_DECANO, APROBAR_DECANO, RECHAZAR_DECANO, ENVIAR_RESPUESTA |
| Examen Supletorio | las cinco anteriores más ENVIAR_RECIBO, SUBIR_COMPROBANTE, APROBAR_COMPROBANTE, RECHAZAR_COMPROBANTE |

## 3. Elementos comunes a todas las pantallas con layout completo

| Elemento | Qué muestra | Referencia |
| --- | --- | --- |
| Cabecera | `HeaderMainComponent`: título "Gestión de Procesos Académicos FIET" y `AvatarSesionComponent` con iniciales, nombre y rol (`roles[0].nombre`) | `shared/headers/header-main-component` |
| Menú de avatar | "Mi usuario" (navega a `rutaDeInicio` de los roles del usuario) y "Cerrar sesión" (`AuthService.logout()` y navegación a `/login`) | `shared/avatar/avatar-sesion-component` |
| Barra lateral | Ítems por rol (sección 4) | `SidebarComponent` |
| Migas | `BreadcrumbComponent` con `role` y `breadcrumbs` fijados en el HTML de cada página | `core/<rol>/pages/*/*.html` |
| Título de contenido | `ContentComponent` con `title` fijo en cada envoltorio | `shared/pages/*-component` |
| Pie | `FooterSimpleComponent` | `PageComponent` |

## 4. Menús laterales por rol

| Rol | Componente | Ítems (ruta) | Origen |
| --- | --- | --- | --- |
| Secretario General | `SideBarSecretarioGeneralComponent` | Mi Usuario (`/sec-general`), Usuarios, Roles, Solicitudes, Orden del Día, Respuestas, Configuración (`/sec-general/tipos/solicitudes`), Administración académica, Historial (`/sec-general/logs`) | [BASE]; ítem "Administración académica" [EXTENSIÓN] |
| Funcionario | `SideBarFuncionarioComponent` | Mi Usuario (`/funcionario`), Solicitudes, Respuestas | [BASE] |
| Secretaria Decanatura FIET | `SideBarSecretariaFietComponent` | Mi Usuario (`/sec-fiet`), Respuestas | [BASE] |
| Usuario FIET | `SideBarUsuarioFietComponent` | Mi Usuario (`/usuario-fiet`), Solicitudes | [BASE] |
| Estudiante | `SideBarEstudianteComponent` | Mi Usuario (`/estudiante`), Solicitudes (`/estudiante/catalogo`, con `activaEn` para los tres formularios), Mis solicitudes (`/estudiante/solicitudes`) | [EXTENSIÓN] |
| Funcionario Académico | `SideBarFuncionarioAcademicoComponent` | Mi Usuario (`/funcionario-academico`); grupo "Gestión de solicitudes" con Solicitudes y Respuestas | [EXTENSIÓN] |
| Decano | `SideBarDecanoComponent` | Mi Usuario (`/decano`); grupo "Gestión académica" con Solicitudes académicas y Administración académica; Solicitudes del Consejo (`/decano/solicitudes-consejo`) | [EXTENSIÓN] |

Los submenús usan `BotonSidebar.children`; el ítem activo es la ruta más específica que coincide con la URL (`SidebarComponent.rutaActiva`).

## 5. Estudiante [EXTENSIÓN] (rol 'Estudiante')

Todas las rutas con `AuthGuard` + `RoleGuard`, roles `[ROLES_ACADEMICOS.ESTUDIANTE]`. Cadena de componentes: página en `core/estudiante/pages/...` -> envoltorio `shared/pages/est-*-component` -> contenido `shared/pages/content/est-*-content-component` (o componente común).

| Ruta | Página -> envoltorio (título) -> contenido | Qué muestra | Acciones | Endpoints |
| --- | --- | --- | --- | --- |
| `estudiante` | `EstudianteHomeComponent` -> `InfoUsuarioComponent` ("Información de Usuario") -> `InfoUsuarioContentComponent` | Tarjeta "Bienvenid@", iniciales, nombres, apellidos, documento, teléfono, usuario, correo, roles; incluye `app-change-password-component` | Cambiar contraseña | `PATCH /usuarios/{uuid}` (`UsuariosService.cambiarContraseña`); datos desde `AuthService.usuario$` (sin llamada adicional para este rol) |
| `estudiante/catalogo` | `EstudianteCatalogoComponent` -> `EstCatalogoSolicitudesComponent` ("Solicitudes") -> `EstCatalogoSolicitudesContentComponent` | Tres tarjetas (`TRAMITES_ESTUDIANTE` en `core/constantes/catalogo-estudiante.ts`): descripción, requisitos, anexos requeridos con enlace "Descargar formato" (`EnlaceFormatoComponent`) | "Iniciar solicitud" (navegación con `router.navigateByUrl` a la ruta del trámite) | Ninguno (contenido estático) |
| `estudiante/cancelacion-matricula` | `EstudianteCancelacionMatriculaComponent` -> `EstCancelacionMatriculaComponent` ("Cancelación de Matrícula") -> `EstCancelacionMatriculaContentComponent` | Formulario de una página con stepper (`StepperFormularioComponent`): Datos del estudiante (`DatosEstudianteSesionComponent`), Asignaturas que se cancelan, Motivo, Anexos obligatorios, Soportes opcionales y advertencia, Normativa aplicable; pantalla de éxito con radicado | Radicar solicitud, Volver (a Mis solicitudes), Ver solicitud (tras radicar), Quitar soporte, Reintentar | `GET /cancelaciones-matricula/formulario` (`getFormulario`, en `ngOnInit`/`cargarFormulario`); `POST /cancelaciones-matricula` (`radicar`, multipart) |
| `estudiante/cancelacion-asignatura` | `EstudianteCancelacionAsignaturaComponent` -> `EstCancelacionAsignaturaComponent` ("Cancelación de Asignatura") -> `EstCancelacionAsignaturaContentComponent` | Igual patrón: Datos del estudiante, Asignaturas que quieres cancelar, Motivo, Soportes opcionales; stepper de 4 pasos | Radicar solicitud, Volver, Ver solicitud, Quitar soporte, Reintentar | `GET /cancelaciones-asignatura/formulario`; `POST /cancelaciones-asignatura` |
| `estudiante/examen-supletorio` | `EstudianteExamenSupletorioComponent` -> `EstExamenSupletorioComponent` ("Examen Supletorio") -> `EstExamenSupletorioContentComponent` | Datos del estudiante; Examen no presentado (asignatura, fecha, causa); sección "Examen con el que se cruzó" (`data-seccion-cruce`) que aparece solo cuando la causa es cruce; Anexos con enlace al formato (`EnlaceFormatoComponent`); stepper con pasos Datos, Examen no presentado, Examen cruzado (solo cruce) y Anexos | Radicar solicitud, Volver, Ver solicitud, Reintentar | `GET /examenes-supletorios/formulario`; `POST /examenes-supletorios` |
| `estudiante/solicitudes` | `EstudianteSolicitudesComponent` -> `EstSolicitudesComponent` ("Mis solicitudes") -> `EstSolicitudesContentComponent` -> `BandejaSolicitudesAcademicasComponent rol="ESTUDIANTE"` | Tarjeta "Mis trámites radicados": filtros Tipo de proceso y Estado, "Limpiar filtros", tabla con Radicado, Tipo de proceso, Fecha de radicación, Estado (ícono y texto por `categoriaEstado(etiqueta)`) y acciones, paginación de 10 | Ver solicitud (ícono de ojo), ordenar por fecha o estado, limpiar filtros, Reintentar | `GET /solicitudes-academicas/estudiante` |
| `estudiante/solicitudes/:uuid` | `EstudianteDetalleSolicitudComponent` -> `EstDetalleSolicitudComponent` ("Detalle de la solicitud") -> `EstDetalleSolicitudContentComponent` -> `DetalleSolicitudAcademicaComponent rol="ESTUDIANTE"` | "Solicitud de {tipo}", radicado, insignia de estado; Datos generales; anexos; "Mensaje para el estudiante" si existe; componente del proceso; historial sin etapa ni comentarios | Volver, descargar Resolución (si `puedeDescargarResolucion`), Visualizar/Descargar anexos; acciones del proceso (ver 5.1) | `GET /solicitudes-academicas/{uuid}`, `GET .../{uuid}/historial`; proceso: `GET /<proceso>/{uuid}`; anexos: `GET .../{uuid}/anexos/{uuidAnexo}`; Resolución: `GET .../{uuid}/resolucion` |

### 5.1 Acciones del Estudiante dentro del detalle

- Cancelación de Matrícula y Cancelación de Asignatura: sin botones de acción. `CancelacionAsignaturaProcesoComponent.accionesVisibles` devuelve `[]` para el Estudiante; en Matrícula, `accionesDisponibles` del Estudiante no contiene acciones del proceso, por lo que el filtro deja la lista vacía (NO VERIFICADO con datos reales del backend). En Asignatura el Estudiante ve solo código y nombre de las asignaturas antes de la etapa final, y con "Aprobada"/"Rechazada" la decisión por asignatura sin observación (D-F23 y notas de `CLAUDE.md`; tabla `asignaturasTabla` y getter `esFinal`).
- Examen Supletorio: botón "Subir comprobante de pago" cuando `accionesDisponibles` incluye `SUBIR_COMPROBANTE`. Abre el diálogo "Subir el comprobante de pago" (`textoBoton` "Subir comprobante"), con `InputAnexoUploadComponent` tipado por el tipo de anexo "Comprobante de pago" (se busca por nombre en `GET /catalogos-academicos/tipos-solicitud/{uuidTipo}/tipos-anexo`, con `ANEXOS_PAGO_SUPLETORIO`). Al confirmar: primero `POST /solicitudes-academicas/{uuid}/anexos` y después `POST /examenes-supletorios/{uuid}/estudiante/comprobante`. Si la acción falla tras subir el archivo, el reintento no vuelve a subirlo (`anexoPagoSubido`). Avisos según etiqueta: pendiente de pago, en verificación y comprobante rechazado ("debes radicar una solicitud nueva").

## 6. Funcionario Académico [EXTENSIÓN] (rol 'Funcionario Académico')

| Ruta | Página -> envoltorio (título) -> contenido | Qué muestra | Acciones | Endpoints |
| --- | --- | --- | --- | --- |
| `funcionario-academico` | `FuncionarioAcademicoHomeComponent` -> `InfoUsuarioComponent` | Igual que Mi Usuario del Estudiante, más cargo y lista "Procesos académicos asignados" (`data-seccion="procesos-asignados"`) | Cambiar contraseña | `PATCH /usuarios/{uuid}`; `GET /funcionarios-academicos/{uuid}` (`InfoUsuarioContentComponent.cargarFuncionarioAcademico`, solo si el usuario tiene el rol) |
| `funcionario-academico/solicitudes` | `FuncionarioAcademicoSolicitudesComponent` -> `FunAcadSolicitudesComponent` ("Solicitudes académicas") -> `FunAcadSolicitudesContentComponent` -> `BandejaSolicitudesAcademicasComponent rol="FUNCIONARIO" modo="solicitudes"` | Tarjeta "Gestión de solicitudes": solo las no finalizadas (`etapaCodigo` fuera de APROBADA y RECHAZADA). Filtros Tipo, Estado, Número de identificación y Código estudiantil. Columnas Radicado, Estudiante, Documento, Código, Tipo de proceso, Fecha, Estado | Ver solicitud, ordenar, limpiar filtros | `GET /solicitudes-academicas/funcionario` |
| `funcionario-academico/respuestas` | `FuncionarioAcademicoRespuestasComponent` -> mismo contenido, `data.modo = 'respuestas'` | Tarjeta "Solicitudes respondidas": solo finalizadas; el filtro de estado ofrece Aprobada/Rechazada; una Cancelación de Asignatura da hasta dos filas (aprobadas/rechazadas) según `resultadosAsignaturas` (D-F39) | Ver solicitud (agrega query param `PARAMETRO_RESULTADO` en las filas por resultado) | `GET /solicitudes-academicas/funcionario` (mismo endpoint, filtrado en el cliente por `delModo`) |
| `funcionario-academico/solicitudes/:uuid` | `FuncionarioAcademicoDetalleSolicitudComponent` -> `FunAcadDetalleSolicitudComponent` ("Detalle de la solicitud") -> `DetalleSolicitudAcademicaComponent rol="FUNCIONARIO"` | Detalle completo: datos generales con Número de CC, anexos, "Observación para el Decano" (última `REMITIR_DECANO`), "Decisión del Decano" (última `APROBAR_DECANO` o `RECHAZAR_DECANO` con su observación), "Mensaje para el estudiante", componente del proceso, historial con insignia de etapa. El enlace "Volver" va a Respuestas si la solicitud es final (`rutaBandeja`) | Ver 6.1 | Ver 6.1 |

### 6.1 Acciones del Funcionario Académico en el detalle

| Acción (botón) | Proceso | Diálogo (título / `textoBoton`) | Contenido del diálogo | Endpoint |
| --- | --- | --- | --- | --- |
| Rechazar solicitud (`RECHAZAR_FUNCIONARIO`) | Matrícula | "Rechazar la solicitud" / "Rechazar solicitud" | Resumen (`ResumenSolicitudComponent`), carga del escaneo de la Resolución (PDF, obligatorio salvo que ya haya uno), observación obligatoria "Observación solo para el estudiante (se entrega como respuesta)" | `POST /solicitudes-academicas/{uuid}/resolucion` y luego `POST /cancelaciones-matricula/{uuid}/funcionario/rechazar` |
| Rechazar solicitud | Asignatura | igual | igual | `POST .../resolucion` y `POST /cancelaciones-asignatura/{uuid}/funcionario/rechazar` |
| Rechazar solicitud | Supletorio | igual sin escaneo | resumen y observación obligatoria | `POST /examenes-supletorios/{uuid}/funcionario/rechazar` |
| Remitir al Decano (`REMITIR_DECANO`) | Matrícula y Asignatura | "Remitir la solicitud al Decano" / "Remitir al Decano" | Antes: la tabla de asignaturas pasa a modo editable (No. faltas, Nota, situación en la matrícula, situación al cancelar; en Asignatura además "Cumple condiciones"); se validan filas con los mensajes en celda. En el diálogo: resumen y "Observaciones para el Decano" (opcional, hasta 500). Al abrir carga las situaciones del catálogo y no abre si el catálogo falla o viene vacío | `GET /catalogos-academicos/situaciones` (antes de abrir); `POST /cancelaciones-*/{uuid}/funcionario/remitir` con `{observacion, evaluaciones}` |
| Remitir al Decano | Supletorio | igual, sin tabla | resumen y observación opcional (sin casilla de requisitos, D-F42) | `POST /examenes-supletorios/{uuid}/funcionario/remitir` con `{observacion}` |
| Enviar respuesta (`ENVIAR_RESPUESTA`) | Matrícula | "Enviar la respuesta al estudiante" / "Enviar respuesta" | Escaneo de la Resolución firmada (obligatorio si no existe), observación opcional | `POST .../resolucion` y `POST /cancelaciones-matricula/{uuid}/funcionario/responder` |
| Enviar respuesta | Asignatura | igual | Escaneo, observación (obligatoria si `respuestaExigeObservacion`), y `ajustes` de situación para las aprobadas sin situación guardada o cambiadas por el Decano (D-F37) | `POST .../resolucion` y `POST /cancelaciones-asignatura/{uuid}/funcionario/responder` (cuerpo con `ajustes` solo si hay) |
| Enviar respuesta | Supletorio | igual sin escaneo | observación opcional | `POST /examenes-supletorios/{uuid}/funcionario/responder` |
| Enviar recibo de pago (`ENVIAR_RECIBO`) | Supletorio | "Enviar el recibo de pago" / "Enviar recibo" | Carga del anexo "Recibo de pago" (obligatoria si no está cargado) | `POST /solicitudes-academicas/{uuid}/anexos` y `POST /examenes-supletorios/{uuid}/funcionario/recibo` |
| Aprobar comprobante (`APROBAR_COMPROBANTE`) | Supletorio | "Aprobar el comprobante de pago" / "Aprobar comprobante" | Fecha acordada opcional (`app-input-date-component`); no puede ser anterior a la fecha del examen no presentado | `POST /examenes-supletorios/{uuid}/funcionario/comprobante/aprobar` |
| Rechazar comprobante (`RECHAZAR_COMPROBANTE`) | Supletorio | "Rechazar el comprobante de pago" / "Rechazar comprobante" | Observación obligatoria | `POST /examenes-supletorios/{uuid}/funcionario/comprobante/rechazar` |

Todos los envíos bloquean el doble clic con la bandera `enviando` (D-F7) y, al terminar, llaman `recargar()` y muestran un toast de éxito (`showSuccess`). Los errores 500 con `codigoError` y los 400 con mapa de campos se pintan en el diálogo (`data-error="servidor"` y bajo cada campo).

## 7. Decano [EXTENSIÓN] (rol 'Decano')

| Ruta | Página -> envoltorio (título) -> contenido | Qué muestra | Acciones | Endpoints |
| --- | --- | --- | --- | --- |
| `decano` | `DecanoHomeComponent` -> `InfoUsuarioComponent` | Mi Usuario (igual que el Estudiante; no carga datos de Funcionario Académico porque el rol no coincide) | Cambiar contraseña | `PATCH /usuarios/{uuid}` |
| `decano/solicitudes` | `DecanoSolicitudesComponent` -> `DecSolicitudesComponent` ("Solicitudes académicas") -> `DecSolicitudesContentComponent` -> `BandejaSolicitudesAcademicasComponent rol="DECANO"` | Tarjeta "Solicitudes remitidas por el Funcionario Académico"; columna "Decisión" en lugar de "Estado"; sin filtro de código | Ver solicitud, ordenar, limpiar filtros | `GET /solicitudes-academicas/decano` |
| `decano/solicitudes/:uuid` | `DecanoDetalleSolicitudComponent` -> `DecDetalleSolicitudComponent` ("Detalle de la solicitud") -> `DetalleSolicitudAcademicaComponent rol="DECANO"` | Igual que el Funcionario Académico (mismo componente con `rol="DECANO"`); ve "Observación para el Decano" y "Decisión del Decano" | Ver 7.1 | Ver 7.1 |
| `decano/solicitudes-consejo` | `DecanoSolicitudesConsejoComponent` -> `UsuarioFietSolicitudesPrivadasComponent` ("Solicitudes") -> `UsuarioFietSolicitudesPrivadasContentComponent` | Pantalla de Julián reutilizada: tarjeta "Envío de Solicitudes" con tabla de tipos de solicitud del perfil del usuario, búsqueda por nombre | Ver información del tipo (ojo), botón de tarjeta "campana" que abre el formulario de envío (`EnviarSolicitudUsuarioFietComponent`) | `GET /tipos/solicitudes` filtrados por perfil (`getTiposSolicitudPorPerfilSolicitante`, `getTiposSolicitudPorNombreYPerfilSolicitante`, `getTipoSolicitud`); envío: ver sección 9 |
| `decano/administracion-academica` | `DecanoAdministracionAcademicaComponent` -> `AdminAcademicaComponent` ("Administración académica") -> `AdminAcademicaContentComponent` | Cuatro pestañas (ver sección 10) | Ver sección 10 | Ver sección 10 |

### 7.1 Acciones del Decano en el detalle

| Acción (botón) | Proceso | Diálogo (título / `textoBoton`) | Contenido | Endpoint |
| --- | --- | --- | --- | --- |
| Aprobar (`APROBAR_DECANO`) | Matrícula | "Aprobar la cancelación de matrícula" / "Aprobar solicitud" | Resumen, nota `NOTA_APROBAR_DECANO`, selección de la situación al cancelar por asignatura (obligatoria), observación opcional con el aviso "Esta observación solo será visible para el Funcionario Académico." | `POST /cancelaciones-matricula/{uuid}/decano/aprobar` con `{situaciones, observacion}` |
| Rechazar (`RECHAZAR_DECANO`) | Matrícula | "Rechazar la cancelación de matrícula" / "Rechazar solicitud" | Resumen y observación opcional | `POST /cancelaciones-matricula/{uuid}/decano/rechazar` |
| Aprobar | Asignatura | Botones "Aceptar tal como está" (`aceptarEvaluacion`, solo sin cambios) y "Enviar decisión modificada" (`decisiones`, solo con cambios). Cada fila tiene un interruptor Aprobar/Rechazar que parte de `cumpleCondiciones` (D-F35) | Títulos desde `TITULOS_DECISION` y textos desde `TEXTOS_BOTON_DECISION`; observación opcional | `POST /cancelaciones-asignatura/{uuid}/decano/aprobar` con `armarAprobacion` |
| Rechazar solicitud completa (`RECHAZAR_DECANO`) | Asignatura | "Rechazar la solicitud completa" / "Rechazar solicitud" | `abrirDecision(modo)` abre este diálogo cuando el modo es `modificar` y todas las filas quedaron rechazadas (`todasRechazadas`); "Aceptar tal como está" se deshabilita si hay cambios (`hayCambiosDecano`) y "Enviar decisión modificada" se deshabilita si no los hay | `POST /cancelaciones-asignatura/{uuid}/decano/rechazar` |
| Aprobar / Rechazar | Supletorio | "Aprobar el examen supletorio" / "Rechazar el examen supletorio" | Resumen y observación opcional (`NOTA_APROBAR_DECANO` al aprobar) | `POST /examenes-supletorios/{uuid}/decano/aprobar` (cuerpo `null` si no hay observación) y `.../decano/rechazar` |

La tabla de asignaturas del Decano marca las filas cuya decisión difiere de la del Funcionario (`decisionDistinta`) y las atenúa si están rechazadas (`esAtenuada`).

## 8. Secretario General [BASE salvo la última fila] (rol 'Secretario General')

Cadena: `core/secretario-general/pages/...` -> envoltorio `shared/pages/...-component` -> contenido `shared/pages/content/...`. Las acciones de fila usan `app-button-component` con íconos `edit.svg` (editar), `eye.svg` (ver información) y otros; los botones de tarjeta se definen en `buttonsCard`.

| Ruta | Página -> envoltorio (título) -> contenido | Qué muestra | Acciones (diálogos) | Endpoints (servicio.método) |
| --- | --- | --- | --- | --- |
| `sec-general` | `SecretarioGeneralHomeComponent` -> `InfoUsuarioComponent` ("Información de Usuario") | Mi Usuario | Cambiar contraseña | `PATCH /usuarios/{uuid}` |
| `sec-general/usuarios` | `SecretarioGeneralUsuariosComponent` -> `UsuariosComponent` ("Usuarios") -> `UsuariosContentComponent` | Tarjeta "Gestión de Usuarios": tabla con búsqueda por nombre y estado, paginación | Botón de tarjeta Crear usuario (`add-user.svg`, diálogo "Crear Usuario"); botón Cargar archivo (`upload.svg`, diálogo "Subir archivo" con `GenericDialogUploadFileComponent`); por fila: editar ("Actualizar Usuario") y ver ("Información de Usuario") | `UsuariosService`: `getUsuariosPaginado`, `getUsuariosFiltrados`, `getUsuario`, `crearUsuario` (POST `?tipoUsuario=`), `crearUsuariosDesdeArchivo`, `actualizarUsuario`, `getTiposUsuario`; `RolesService.getRoles` |
| `sec-general/roles` | `SecretarioGeneralRolesComponent` -> `RolesComponent` ("Roles") -> `RolesContentComponent` | Tarjeta "Gestión de Roles": nombre, descripción y estado, con botones de ordenar (`ordenar.svg`) | Por fila: editar ("Actualizar Rol") y ver ("Información del Rol") | `RolesService.getRoles`, `actualizarRol` |
| `sec-general/solicitudes` | `SecretarioGeneralSolicitudes` -> `SecSolicitudesComponent` ("Gestión de Solicitudes") -> `SecContentComponent` | Tarjeta "Gestión de Solicitudes": Solicitud, Responsable, Estado; búsqueda por nombre | Por fila: editar ("Actualizar Solicitud": consecutivo, nombre, descripción, estado y funcionario responsable) y ver ("Información de Solicitud", con `AnexosViewComponent`) | `SolicitudesService.getSolicitudesPaginado`, `buscarSolicitudesPorNombre`, `getSolicitud`, `actualizarSolicitud`; `UsuariosService.getFuncionarios` |
| `sec-general/orden-del-dia` | `SecretarioGeneralOrdelDelDia` -> `OrdenDelDiaComponent` ("Gestión de Orden Del Día") -> `OrdenDelDiaContentComponent` | Tarjeta "Gestión Orden del Día": acta y estado | Botón de tarjeta Crear (`add.svg`, diálogo "Crear Orden del Día": nombre, número de acta, descripción, ciudad, fecha, hora inicio, hora fin, lugar); por fila: editar ("Actualizar Orden del Día"), ver ("Información Orden del Día") y abrir (`folder.svg`, abre `OrdenDelDiaVisualizadorComponent`) | `SolicitudesService.getOrdenesDelDiaPaginado`, `buscarOrdenesDelDia`, `getOrdenDelDia`, `crearOrdenDelDia`, `actualizarOrdenDelDia` |
| (diálogo del anterior) | `OrdenDelDiaVisualizadorComponent` (`core/secretario-general/components/orden-del-dia-visualizador-component`) | Dos diálogos: "Información de Solicitud" y "Asignar Respuesta"; agrupa solicitudes de una orden | Descargar anexos, exportar orden (variantes: simple, con respuestas, merge para reunión), asignar respuesta a una solicitud | `SolicitudesService.getSolicitudesPorOrdenDelDia`, `getSolicitudesPorEstado`, `getSolicitud`, `actualizarSolicitud`, `descargarAnexosOrdenDelDia`, `exportarOrdenDelDia`, `exportarOrdenDelDiaConRespuestas`, `exportarOrdenDelDiaMerge`; `RespuestasService.getRespuestaPorSolicitud` |
| `sec-general/respuestas` | `SecretarioGeneralRespuestasComponent` -> `SecRespuestasComponent` ("Gestión de Respuestas") -> `SecRespuestasContentComponent` | Tarjeta "Gestión de Respuestas": solicitud y estado | Por fila: ver ("Información de la Respuesta", con `DescargarRespuestaComponent`) y editar (abre `DialogGestionRespuestaComponent`, con `CargarRespuestaComponent`) | `RespuestasService.getRespuestas`, `getRespuestasPorNombreSolicitud`, `getRespuesta`, `responderSolicitud`, `eliminarArchivoRespuesta` |
| `sec-general/tipos/solicitudes` | `SecretarioGeneralTipoSolicitudesComponent` -> `TipoSolicitudesComponent` ("Configuración de Tipos de Solicitudes") -> `TipoSolicitudesContentComponent` | Tarjeta "Gestión de Tipos de Solicitudes": solicitud y funcionario; búsquedas por tipo y por funcionario | Botones de tarjeta: Crear (diálogo de pasos "Crear Tipo de Solicitud": Paso 1 información, Paso 2 anexos) y Cargar archivo; por fila: editar (pasos "Actualizar Tipo de Solicitud"), ver ("Información del Tipo de Solicitud") y asignar funcionario (`employee.svg`, "Asignar Funcionario") | `TipoSolicitudService` (lista, filtros, `crearTipoSolicitud`, creación desde archivo, `actualizarTipoSolicitud`, `getTipoSolicitud`); `RolesService.getRoles`; `UsuariosService.getFuncionarios` |
| `sec-general/logs` | `SecretarioGeneralLogComponent` -> `LogsComponent` ("Historial") -> `LogContentComponent` | Tabla Responsable, Fecha, Acción, Resultado; búsqueda por responsable y por fecha | Solo consulta y paginación | `LogService.getLogsPaginado`, `getLogsFiltrados` |
| `sec-general/administracion-academica` [EXTENSIÓN] | `SecretarioGeneralAdministracionAcademicaComponent` -> `AdminAcademicaComponent` | Igual que la del Decano | Ver sección 10 | Ver sección 10 |

El Secretario General no tiene una pantalla de solicitudes académicas (Matrícula/Asignatura/Supletorio): su única entrada a lo académico es la administración. Verificado: no existe ruta `sec-general/solicitudes-academicas`.

## 9. Funcionario (Julián), Secretaria Decanatura FIET, Usuario FIET y solicitante público [BASE]

| Ruta | Rol | Página -> envoltorio (título) -> contenido | Qué muestra | Acciones | Endpoints |
| --- | --- | --- | --- | --- | --- |
| `funcionario` | Funcionario | `FuncionarioHomeComponent` -> `InfoUsuarioComponent` | Mi Usuario | Cambiar contraseña | `PATCH /usuarios/{uuid}` |
| `funcionario/solicitudes` | Funcionario | `FuncionarioSolicitudes` -> `FunSolicitudesComponent` ("Gestión de Solicitudes") -> `FunSolicitudesContentComponent` | Tarjeta "Gestión de Solicitudes": solicitud y estado; búsqueda por nombre | Botón de tarjeta "campana" que abre `EnviarSolicitudUsuarioPublicoComponent` (diálogo "Enviar Solicitud Pública": Paso 1 Información del Solicitante, Paso 2 Información de la Solicitud); por fila: editar ("Actualizar Solicitud") y ver ("Información de Solicitud") | `SolicitudesService.getSolicitudesPorFuncionario` (GET `/funcionario/{uuid}`), `buscarSolicitudesPorNombreYFuncionario`, `getSolicitud`, `actualizarSolicitud`, `enviarSolicitudPublica` (POST `/solicitudes/public`); `TipoSolicitudService.getTiposSolicitudPorPerfil('Solicitante Publico')` |
| `funcionario/respuestas` | Funcionario | `FuncionarioRespuestasComponent` -> `FunRespuestasComponent` ("Gestión de Respuestas") -> `FunRespuestasContentComponent` | Respuestas de las solicitudes del funcionario | Ver ("Información de la Respuesta"), gestionar respuesta (`DialogGestionRespuestaComponent`) | `RespuestasService.getRespuestasPorFuncionario`, `getRespuestasPorFuncionarioYNombreSolicitud`, `getRespuesta` |
| `sec-fiet` | Secretaria Decanatura FIET | `SecretariaFietHomeComponent` -> `InfoUsuarioComponent` | Mi Usuario | Cambiar contraseña | `PATCH /usuarios/{uuid}` |
| `sec-fiet/respuestas` | Secretaria Decanatura FIET | `SecretariaFietRespuestasComponent` -> `SecFietRespuestasComponent` ("Gestión de Respuestas") -> `SecFietRespuestasContentComponent` | Respuestas (solo lectura) | Ver información y descargar archivo de la respuesta; no hay botón de edición | `RespuestasService.getRespuestas`, `getRespuestasPorNombreSolicitud`, `getRespuesta` |
| `usuario-fiet` | Coordinador Pregrado, Coordinador Posgrados, Jefe de Departamento, Decano, Docente | `UsuarioFietHomeComponent` -> `InfoUsuarioComponent` | Mi Usuario | Cambiar contraseña | `PATCH /usuarios/{uuid}` |
| `usuario-fiet/solicitudes` | los cinco roles anteriores | `UsuariofietSolicitudesComponent` -> `UsuarioFietSolicitudesPrivadasComponent` ("Solicitudes") -> `UsuarioFietSolicitudesPrivadasContentComponent` | Tarjeta "Envío de Solicitudes": tipos de solicitud disponibles para el perfil del usuario, con búsqueda | Ver información del tipo; botón de tarjeta "campana" que abre `EnviarSolicitudUsuarioFietComponent` (diálogo de pasos "Enviar Solicitud") | `TipoSolicitudService` (por perfil, por nombre y perfil, por uuid); `SolicitudesService.enviarSolicitud` (POST `/solicitudes`, multipart) y `getOrdenesDelDiaPorEstado`; perfil tomado de `AuthService` |
| `usuario-publico/solicitudes` | Solicitante público (sin sesión, sin guard) | `UsuarioPublicoSolicitudesComponent` (layout simple) -> `UsuarioPublicoSolicitudesContentComponent` | Tarjeta "Solicitudes Disponibles" con tipos de solicitud para el perfil público y búsqueda | Ver información del tipo (diálogo "Información del Tipo de Solicitud"); esta pantalla no contiene un botón ni el formulario de envío (el componente `EnviarSolicitudUsuarioPublicoComponent` solo se monta en la pantalla del Funcionario) | `TipoSolicitudService.getTiposSolicitudPorPerfilSolicitante`, `getTiposSolicitudPorNombreYPerfilSolicitante`, `getTipoSolicitud` |
| `login` | cualquiera | `LoginComponent` (layout simple) -> `LoginFormComponent` | Formulario con usuario y contraseña (`InputPasswordComponent`) | Iniciar sesión (Enter envía; bandera `enviando`, D-F21). Redirige con `rutaDeInicio`; si no hay ruta muestra el error "El usuario no tiene un rol con vistas definidas en el sistema." | `POST /sesiones`, luego `GET /usuarios/{uuid}` |

Notas:

- "Solicitante público" y "privado" en el sentido de Julián: el público no se autentica y solo consulta tipos de solicitud; el solicitante "privado" es el Usuario FIET autenticado, que sí envía solicitudes al Consejo. La clasificación proviene de los nombres de las pantallas (`usuario-publico`, `usuario-fiet-solicitudes-privadas`) y del perfil `'Solicitante Publico'` pasado al servicio; la lista exacta de perfiles que acepta el backend NO VERIFICADO desde el frontend.
- El Decano, además de las pantallas de procesos académicos, tiene acceso a `usuario-fiet` y `usuario-fiet/solicitudes` por URL directa (ver 07, sección 3.1), y su entrada de menú "Solicitudes del Consejo" reutiliza el mismo contenido.

## 10. Administración académica [EXTENSIÓN] (Decano y Secretario General)

Una sola pantalla con cuatro pestañas (`AdminAcademicaContentComponent.secciones`, cambio de pestaña con `elegir`, sin rutas hijas): Asignaturas, Estudiantes, Funcionarios académicos, Responsable por proceso. Rutas: `decano/administracion-academica` y `sec-general/administracion-academica`.

| Pestaña | Componente | Qué muestra | Acciones y diálogos | Endpoints |
| --- | --- | --- | --- | --- |
| Asignaturas | `AdminAsignaturasContentComponent` | Filtros por programa y texto ("Buscar por código o nombre"), tabla paginada con código, nombre y programa; estados cargando, vacío, error | "Nueva asignatura" (diálogo con programa y nombre; el código lo genera el backend, D-F16); "Editar" (solo nombre); "Cargar Excel" (`CargaExcelAcademicaComponent`); "Buscar" | `AsignaturaService.getAsignaturasFiltradas`, `crearAsignatura`, `actualizarAsignatura`, `crearAsignaturasDesdeArchivo`; `CatalogoAcademicoService.getProgramas` |
| Estudiantes | `AdminEstudiantesContentComponent` | Filtros de nombre, apellido, código; tabla; la facultad se muestra de solo lectura (FIET) | "Nuevo estudiante" (diálogo con `DatosUsuarioAcademicoComponent`, código estudiantil, semestre, grupo, programa primero y asignaturas habilitadas al elegir programa, D-F17); "Editar estudiante" (envía solo lo cambiado, D-F24; el programa no se edita, D-F18); "Asignaturas de {nombre}" (agregar asignatura, cambiar estado de matrícula); "Cargar Excel" | `EstudianteService.getEstudiantesFiltrados`, `crearEstudiante`, `actualizarEstudiante`, `crearEstudiantesDesdeArchivo`, `agregarAsignaturaMatriculada`, `cambiarEstadoAsignatura`; `AsignaturaService.getAsignaturasPorPrograma`; `CatalogoAcademicoService.getProgramas` |
| Funcionarios académicos | `AdminFuncionariosContentComponent` | Filtros de nombre, apellido y cargo; tabla; los procesos a cargo se ven de solo lectura al editar (D-F25) | "Nuevo funcionario académico", "Editar funcionario académico", "Cargar Excel" | `FuncionarioAcademicoService.getFuncionariosAcademicosFiltrados`, `crearFuncionarioAcademico`, `actualizarFuncionarioAcademico`, `crearFuncionariosAcademicosDesdeArchivo` |
| Responsable por proceso | `AdminResponsablesContentComponent` | Tabla de los tipos de solicitud académica con su funcionario responsable | "Cambiar responsable" y "Nuevo responsable" (diálogo "Responsable de {tipo}" con select de funcionario) | `CatalogoAcademicoService.getTiposSolicitud`, `asignarFuncionarioAcademico`; `FuncionarioAcademicoService.getFuncionariosAcademicosPaginado` |

La carga por Excel (`CargaExcelAcademicaComponent`) muestra reglas del archivo, ejecuta `cargar` (función que cada pestaña le pasa) y, tras éxito, muestra solo el resultado y el botón pasa a "Cerrar" (D-F22). Los errores se listan dentro del diálogo mediante `mensajesDeError`.

Restricción de permisos por rol dentro de esta pantalla: ninguna (ambos roles ven y hacen lo mismo en el cliente). La diferencia de permisos, si existe, la impone el backend. NO VERIFICADO desde el frontend.

## 11. Componentes de proceso: estructura interna común [EXTENSIÓN]

Los tres componentes (`CancelacionMatriculaProcesoComponent`, `CancelacionAsignaturaProcesoComponent`, `ExamenSupletorioProcesoComponent`) comparten:

- Entradas: `uuidSolicitud`, `solicitud`, `rol`, `recargar`, todas obligatorias.
- Carga: `ngOnChanges` pide `GET /<proceso>/{uuid}` (`getDetalle`) y muestra estados `cargando`, `errorCarga` (con "Reintentar").
- Tarjeta de acciones `data-acciones` con un `SimpleButtonComponent` por acción visible (`[attr.data-accion]`), texto desde `etiquetaBoton`.
- Un solo `GenericDialogFormComponent` con `title` y `textoBoton` por acción, `textoCancelar="Cancelar"` y `cerrarConEscape` (D-F31), `ResumenSolicitudComponent` y los campos de cada acción.
- Validación local previa (`validar(accion)`), mensajes bajo cada campo y bloque `data-error="servidor"` para errores del backend.
- Cancelaciones: tabla propia de asignaturas con encabezado agrupado (`agruparEncabezados`, `RUTAS_ENCABEZADO_ASIGNATURAS`), estilo `tabla-asignaturas.css`, nota `NOTA_SIMCA` (`muestraNotaSimca`), columnas sin datos ocultas, scroll horizontal bajo 992 px.
- Supletorio: tarjeta de datos (fecha del examen, causa, fecha acordada si existe, bloque de cruce si `detalle.cruce`) y avisos de etapa (`avisoEtapa`).

## 12. Qué cambia entre roles en la misma pantalla de detalle

| Elemento del detalle | Estudiante | Funcionario Académico | Decano |
| --- | --- | --- | --- |
| "Número de CC" en Datos generales | no | sí | sí |
| Insignia de etapa en el historial | no | sí | sí |
| "Observación para el Decano" (`REMITIR_DECANO`) | no (`observacionRemision` devuelve `null` para el Estudiante) | sí | sí |
| "Decisión del Decano" (`decisionDecano`) | no (solo FUNCIONARIO y DECANO) | sí | sí |
| "Mensaje para el estudiante" (`mensajeEstudiante`) | sí, si existe | sí, si existe | sí, si existe |
| Etiqueta de estado | `ETIQUETAS_ESTUDIANTE` | etiqueta propia del backend para el rol | etiqueta propia del backend para el rol |
| "Volver" | `/estudiante/solicitudes` | `/funcionario-academico/solicitudes`, o `/funcionario-academico/respuestas` si la etapa es final | `/decano/solicitudes` |
| Botones de acción | los que reciba en `accionesDisponibles` (en Supletorio, "Subir comprobante de pago"; en Cancelaciones ninguno) | los que reciba en `accionesDisponibles` | los que reciba en `accionesDisponibles` |

Fuente: `detalle-solicitud-academica-component.ts` (`datosGenerales`, `observacionRemision`, `decisionDecano`, `rutaBandeja`) y su HTML (`@if (!esEstudiante)` en historial).

## 13. Observaciones y discrepancias para la monografía

- La pantalla pública `usuario-publico/solicitudes` solo lista tipos; el formulario público de envío vive en la pantalla del Funcionario (rol 'Funcionario' de Julián) y no en la pública. [BASE]
- Hay dos barras de menú con ítem "Solicitudes" para el Estudiante y Funcionario Académico con destinos distintos: en el Estudiante "Solicitudes" es el catálogo (`/estudiante/catalogo`) y "Mis solicitudes" la bandeja; en el Funcionario Académico "Solicitudes" es la bandeja de no finalizadas y "Respuestas" la de finalizadas.
- `funcionario-academico/respuestas` reutiliza el título de envoltorio "Solicitudes académicas" (el de `FunAcadSolicitudesComponent`), mientras la tarjeta interna dice "Solicitudes respondidas".
- La decisión de qué botones ven los roles académicos depende por completo del arreglo `accionesDisponibles` que devuelve el backend; si el backend no lo devuelve o lo devuelve vacío, ninguna acción se muestra. Se verificó el consumo, no el contenido real del backend.
- Los textos de los menús de Julián del Secretario General incluyen el prefijo `| ` en cada etiqueta (por ejemplo `'| Usuarios'`); `SidebarComponent.texto()` lo elimina al pintar.
- Total de diálogos por familia: Julián 28 instancias (más el `p-dialog` de gestión de respuesta), extensión 11 instancias.
