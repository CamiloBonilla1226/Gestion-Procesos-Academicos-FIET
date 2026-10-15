---
name: extender-frontend-fiet
description: Usar al agregar o modificar cualquier pantalla, componente o servicio del frontend de Gestión de Procesos Académicos FIET (nueva vista de un proceso académico, nuevo formulario, nueva tabla, nueva integración con un endpoint del backend).
---

# Extender el frontend de Gestión de Procesos Académicos FIET

Antes de escribir una sola línea, lee `frontend/CLAUDE.md` completo. Esta
skill da el orden concreto de pasos para agregar una pantalla o
funcionalidad nueva siguiendo el mismo patrón de composición y los mismos
componentes compartidos que ya usa Julián Camacho.

Si la tarea toca un proceso académico (Cancelación de Matrícula, Cancelación
de Asignatura, Examen Supletorio) o la administración académica, lee además la
sección "Procesos académicos y administración" de `frontend/CLAUDE.md` y el
contrato `docs/api/contrato-api-procesos-academicos.md`, que manda sobre las
maquetas HTML. Casi todo lo que hace falta ya existe: revisa primero los pasos
1 a 3 antes de crear algo.

No se agrega ningún comentario ni emoji en el código que se escribe o
modifica en esta skill, en ningún paso.

## Paso 1 — Modelos

Por cada DTO nuevo del backend, crea su interfaz TypeScript en
`core/models/<Recurso>/DTORequest/<X>DTOPeticion.ts` o
`core/models/<Recurso>/DTOResponse/<X>DTORespuesta.ts`, con exactamente los
mismos campos que el DTO Java correspondiente. Un archivo por interfaz. No
uses "Sesión" (con tilde) ni "DTPResponse" (sin la O) como referencia de
nombre de carpeta — es un typo heredado de un módulo existente, un recurso
nuevo usa siempre `DTOResponse` bien escrito.

Los 71 endpoints académicos ya tienen sus modelos en `core/models`
(`Asignatura`, `Estudiante`, `FuncionarioAcademico`, `CatalogoAcademico`,
`SolicitudAcademica`, `CancelacionMatricula`, `CancelacionAsignatura`,
`ExamenSupletorio`). Busca ahí antes de crear uno; los modelos son siempre
`interface` con el nombre exacto del DTO Java (D-F1). El programa académico es
`ProgramaAcademicoDTORespuesta` (en `CatalogoAcademico`); la asignatura se crea
con `AsignaturaDTOPeticion` `{nombreAsignatura, uuidProgramaAcademico}` y se
edita con `AsignaturaActualizarDTOPeticion` `{nombreAsignatura}`; la matrícula
es `{codigoAsignatura, grupo}`.

## Paso 2 — Servicio

`core/services/<recurso>-service.ts`, `@Injectable({ providedIn: 'root' })`,
URL armada sobre `environment.apiUrl` (importado desde
`../../../enviroments/environment` — esa carpeta se llama así, con ese
typo, no "environments"), un método por endpoint del controlador del
backend, devolviendo `Observable<...>` con los tipos del paso 1. Revisa
`roles-service.ts` como referencia de estilo y de nombres de método
(`getX`, `getXPaginados`, `actualizarX`, etc.).

Los servicios académicos ya existen (`asignatura`, `estudiante`,
`funcionario-academico`, `catalogo-academico`, `solicitud-academica`,
`cancelacion-matricula`, `cancelacion-asignatura`, `examen-supletorio`).
Reglas de la capa API de la extensión:

- El `FormData` se arma dentro del servicio; nunca se pone `Content-Type` a
  mano. Una parte de anexo se nombra con el `uuidTipoAnexoAcademico`.
- Los parámetros de consulta se arman con `armarParametros`
  (`core/utils/parametros-http.ts`).
- Una descarga usa `responseType: 'blob'` y `.pipe(parsearErrorBlob())`
  (`core/utils/descargas.ts`); el componente llama a `guardarArchivo`.

## Paso 3 — Revisar qué componente compartido reusar

Antes de crear cualquier componente visual nuevo, revisa si ya existe en
`shared/` lo que necesitas:

- tabla con paginado -> `TableGenericComponent` + `Paginator`
- tarjeta con botones de acción -> `CardMainComponent`
- formulario en diálogo -> `GenericDialogFormComponent`
- ver información de solo lectura en diálogo -> `GenericDialogInfoComponent`
- campo de texto, select, textarea o fecha -> `InputTextComponent`,
  `InputSelectComponent`, `InputTextTareaComponent`, `InputDateComponent`
- búsqueda -> `BarraBusquedaComponent`
- descarga/visualización de anexos -> `AnexosViewComponent` (Julián) o
  `AnexosAcademicosComponent` (anexos de una solicitud académica)
- subir un archivo -> `InputAnexoUploadComponent` con el adaptador
  `comoTipoAnexo(uuid, nombre, formatos, obligatorio)`
- datos personales y de acceso de un usuario académico ->
  `DatosUsuarioAcademicoComponent` (`shared/others`)
- carga masiva por Excel -> `CargaExcelAcademicaComponent` (`shared/others`),
  con los encabezados de `ENCABEZADOS_EXCEL_*` y una función `cargar`; si el
  archivo tiene una columna con valores cerrados, pásale `reglas`,
  `tituloValores` y `valores` (como los programas en asignaturas y estudiantes)
- select de programa o de asignaturas de un programa -> `InputSelectComponent`
  con `opcionesDePrograma` / `opcionesDeAsignatura`
  (`core/utils/programas-academicos.ts`), sobre `CatalogoAcademicoService.getProgramas`
  y `AsignaturaService.getAsignaturasPorPrograma`; nunca una lista fija de
  programas
- bandeja de solicitudes académicas -> `bandeja-solicitudes-academicas-component`
- detalle de una solicitud académica -> `detalle-solicitud-academica-component`
  más un componente de proceso (paso 4)

`GenericDialogFormComponent` tiene `@Input() textoBoton` (por defecto
`Guardar`). En los diálogos nuevos pásale el texto de la acción ("Remitir al
Decano", "Crear", "Guardar cambios"...); no lo cambies en los de Julián.

Solo se crea un componente nuevo en `shared/` cuando ninguno de los
existentes cubre el caso, y en ese caso va en la subcarpeta que le
corresponda por tipo (no suelto en `shared/`).

Estilos: layout, utilidades, tablas y campos con clases de Bootstrap 5
(`d-flex`, `row`, `col-md-6`, `gap-2`, `mt-3`); diálogos y botones con los
componentes compartidos que envuelven PrimeNG. No uses PrimeFlex (está en
`package.json` pero no se carga en ningún lado) ni agregues componentes de
PrimeNG que el proyecto no usa (`p-table`, `p-dropdown`, `p-inputtext`).

Formularios: no hay `ReactiveFormsModule` en el proyecto. Los campos son los
componentes de `shared/inputs/` con `[(value)]="modelo.campo"` sobre un
objeto plano del componente de contenido (`InputAnexoUploadComponent` es la
excepción: usa `tipoAnexo`, `uploadedFile`, `fileSelected` y
`fileRemoved`). Para validar, obtén los inputs con
`@ViewChild`, marca `touched = true`, consulta `isInvalid()` y si falla
muestra `toastService.showError(...)`, como en `guardarRolActualizado()` de
`roles-content-component.ts`. Para varios pasos usa
`GenericDialogStepsFormComponent` con `canContinue`, como
`EnviarSolicitudUsuarioFietComponent`.

En los formularios de la extensión la validación es otra: el objeto plano se
valida antes de enviar con las funciones de `core/utils/validaciones-academicas.ts`
(`errorDeTexto`, `errorDeArchivo`, `leerNota`, `leerFaltas`,
`esFechaValida`, `esHoraValida`) y `core/utils/usuarios-academicos.ts`
(`validarDatosUsuario`). Cada error se pinta debajo de su campo. Los archivos
se validan en nuestro componente (vacío, 5 MB o el `tamanioMaximoBytes` del
formulario, extensión con `jpg` = `jpeg`, y 20 MB por envío), porque el
componente de Julián no valida el tamaño (D-F5).

## Paso 4 — Componente de contenido (la lógica real)

Va en `shared/pages/content/<nombre>-content-component/`. Acá se inyectan
los servicios del paso 2, se maneja el estado (listas, paginación,
filtros, visibilidad de diálogos) y se arma la plantilla con los
componentes del paso 3. Sigue el estilo de
`fun-solicitudes-content-component.ts` como referencia de organización
(propiedades de estado arriba, métodos de carga de datos, métodos de
manejo de diálogos, métodos de acciones).

Reglas que siguen todos los componentes de contenido de la extensión:

- Estados de carga, vacío y error (con botón Reintentar).
- Una bandera `enviando` por envío que bloquea el doble envío (D-F7).
- Un diálogo que necesita un catálogo (situaciones, tipos de anexo) se abre
  solo cuando el catálogo llega; si falla o viene vacío, error por
  `ErrorHandlerService` y el diálogo no abre.
- Tras radicar se reemplaza el formulario por el radicado y "Ver solicitud",
  y se navega al detalle.
- En administración, los filtros se aplican con un botón "Buscar" y el
  paginador de Julián (base 1) se convierte a base 0 al llamar al backend.

### Si es un componente de proceso académico

Un proceso se integra al detalle común, no con una página propia:

1. Crea `shared/pages/content/<proceso>-proceso-component/` con cuatro
   `@Input({ required: true })`: `uuidSolicitud`, `solicitud`, `rol` y
   `recargar`.
2. Regístralo con una línea en `COMPONENTES_PROCESO`
   (`detalle-solicitud-academica-component/componentes-proceso.ts`), con un
   `import()` diferido.
3. Los botones salen de `solicitud.accionesDisponibles` filtradas por las
   acciones del proceso; nunca del rol. Los textos y avisos dependen de
   `solicitud.etapaCodigo`, no de `etiqueta`.
4. Cada acción abre un `GenericDialogFormComponent` con su `textoBoton`;
   tras el éxito, toast y `recargar()`.
5. Si la acción exige un archivo (escaneo de la Resolución, recibo,
   comprobante), se sube primero con `SolicitudAcademicaService` y luego, con
   `switchMap`, se llama la acción. Guarda que ya se subió, para que un
   reintento no lo suba otra vez.
6. Los uuid de tipos de anexo que no trae el formulario (recibo, comprobante)
   se buscan por nombre en
   `CatalogoAcademicoService.getTiposAnexoPorTipo(solicitud.uuidTipoSolicitudAcademica)`;
   nunca un uuid fijo.

## Paso 5 — Componente wrapper de rol

Va en `shared/pages/<nombre>-component/`. Es delgado: solo referencia al
componente de contenido del paso 4 como propiedad (`content = ...`), igual
que `fun-solicitudes-component.ts`.

## Paso 6 — Página de la ruta

Va en `core/<rol>/pages/<nombre>/` (Estudiante y Funcionario Académico llevan
carpeta de rol propia; `core/funcionario` es del comité de Julián y no se
reutiliza). Referencia como propiedades el sidebar
del rol correspondiente, el header, el breadcrumb y el wrapper del paso 5,
y se los pasa a `app-page-component` en el HTML (`[sidebarComponent]`,
`[headerComponent]`, `[breadcrumbComponent]`, `[mainComponent]`), igual que
`funcionario-solicitudes.ts`.

Para varias secciones de un mismo rol se usa un solo componente de contenido
con pestañas Bootstrap (`nav-tabs`) y un `@switch`, como
`admin-academica-content-component`, en vez de una ruta por sección.

Los servicios nuevos son `providedIn: 'root'` y no se registran en
`app.config.ts`. Ahí el único interceptor registrado es `authInterceptor`,
con `provideHttpClient(withInterceptors([authInterceptor]))`; uno nuevo se
agregaría a ese arreglo. El manejo del 401 no es un interceptor aparte: vive en
`SesionVencidaService` y `authInterceptor` lo llama.

## Paso 7 — Ruta

Agrega la ruta en `app.routes.ts`, con `canActivate: [AuthGuard, RoleGuard]`
(los dos guards juntos, es el patrón real de todas las rutas protegidas
existentes, no solo `RoleGuard`) y `data: { roles: [...] }` listando los
roles que pueden entrar como strings exactos, a menos que la vista sea
pública. El Decano usa el rol existente "Decano". Los roles
"Estudiante" y "Funcionario Académico" se crean primero en el backend (filas
nuevas en `data.sql`) — el string de `data.roles` tiene que coincidir
carácter por carácter con el `nombre` de esa fila.

En la extensión los roles salen de `ROLES_ACADEMICOS`
(`core/constantes/procesos-academicos.ts`). Además:

- Siempre `loadComponent` (carga diferida). El bundle inicial no puede pasar de
  840 kB; cada entrada de ruta suma algo al bundle inicial.
- Lo que se importa desde `app.config.ts`, el interceptor o los guards queda en el
  bundle inicial. No importes ahí `ToastService` (SweetAlert2) ni componentes;
  usa un `import()` diferido, como `SesionVencidaService`.
- Una ruta con parámetro (`:uuid`) necesita su entrada `RenderMode.Client` en
  `app.routes.server.ts`, antes de `**`.
- El ítem de menú se agrega con una línea en el arreglo `[buttons]` del sidebar
  del rol. En el sidebar de Julián (Secretario General) solo se agrega la línea,
  no se reorganiza.

## Paso 8 — Feedback y errores

Mensajes de éxito/error puntuales con `ToastService`. Errores de petición
HTTP con `ErrorHandlerService`, nunca mostrando el error crudo del backend
directo al usuario.

- 500 con `codigoError`: `leerCodigoError` (`core/utils/errores-http.ts`) y
  `ErrorHandlerService`.
- 400 con mapa `{campo: mensaje}`: `erroresDeCampos`; cada mensaje junto a su
  campo y el resto en un bloque `data-error="servidor"`. En administración,
  `mensajesDeError` devuelve la lista para mostrar dentro del diálogo.
- Una solicitud en curso no se avisa antes de radicar; se muestra el error del
  backend (D-F13).
- El 401 lo atiende `SesionVencidaService` (cierra sesión, un solo aviso,
  `/login`). No lo manejes en el componente.
- Nunca escribas contraseñas en consola ni en mensajes.

## Paso 9 — SSR

Si el componente nuevo toca `localStorage`, `window` o `document`, protégelo
con
`typeof window !== 'undefined'` antes de usarlo, para no romper el render
del servidor (SSR con Express). `RoleGuard` ya lo hace; `authInterceptor` y
partes de `AuthService` no, y no son patrón a imitar.

## Decisiones del frontend

Respeta estas decisiones al extender; el detalle está en `frontend/CLAUDE.md`.

| Decisión | Contenido |
| --- | --- |
| D-F1 | Modelos como `interface` con el nombre exacto del DTO del backend |
| D-F2 | Ramas nuevas en el login, solo agregando líneas. Orden: Secretario General, Funcionario, Secretaria Decanatura FIET, Decano a `/decano`, Funcionario Académico a `/funcionario-academico`, Estudiante a `/estudiante`, el resto a `usuariosFiet` |
| D-F3 | Carpetas de rol nuevas `core/estudiante`, `core/funcionario-academico` y `core/decano`, con el patrón de tres niveles (página, wrapper, contenido) |
| D-F4 | La Resolución se descarga como `Resolucion-<radicado>.pdf`; los anexos, con el `nombreArchivo` del DTO |
| D-F5 | El tamaño de los archivos se valida en nuestros componentes, porque el componente de Julián no lo hace |
| D-F6 | `InputAnexoUploadComponent` se reutiliza con un adaptador (`comoTipoAnexo`); jpg y jpeg son equivalentes; 5 MB por archivo y 20 MB por envío se validan en nuestro componente. Su mensaje "Este anexo es obligatorio" es de Julián y se deja como está |
| D-F7 | Doble envío bloqueado con una bandera propia en cada componente |
| D-F8 | 401 fuera del login: cerrar sesión, avisar una vez y redirigir a `/login`. Resuelta en T10.8 con `SesionVencidaService` |
| D-F9 | PENDIENTE_PAGO y EN_VERIFICACION_PAGO con insignia amarilla |
| D-F10 | Punto de extensión del detalle: `COMPONENTES_PROCESO`, una línea por proceso, carga diferida y cuatro inputs (`uuidSolicitud`, `solicitud`, `rol`, `recargar`). Registrados los tres procesos |
| D-F11 | Cancelación de Matrícula: advertencia de primer periodo visible para todos, con casilla obligatoria, solo de interfaz (no se envía al backend). Cancelación de Asignatura no la tiene |
| D-F12 | Texto del botón de los diálogos por acción, con `Guardar` por defecto. Resuelta en T10.8 con el input `textoBoton` de `GenericDialogFormComponent` |
| D-F13 | Sin aviso previo de solicitud en curso; se muestra el error del backend |
| D-F14 | Las pruebas del frontend no llaman al backend real: specs con `HttpTestingController` o espías |
| D-F15 | Los programas salen siempre del catálogo `GET catalogos-academicos/programas`, sin lista fija; el select muestra "Nombre (SIGLA)" y los diálogos de Excel listan los nombres tal como los trae el catálogo |
| D-F16 | El código de la asignatura no se pide ni se edita: lo genera el backend y el toast de alta lo muestra. La edición manda solo `{nombreAsignatura}` |
| D-F17 | Alta de estudiante: programa primero; asignaturas en un `<fieldset>` deshabilitado hasta tener programa con asignaturas; select por fila sin repetidas. Cambiar el programa con filas llenas pide confirmación en línea dentro del diálogo (no un segundo diálogo) y limpia las filas al confirmar |
| D-F18 | El programa del estudiante no se edita en ninguna pantalla. La matrícula desde el detalle elige entre las asignaturas del programa del estudiante; si ya la tiene activa lo decide el backend |
| D-F19 | `CargaExcelAcademicaComponent` se generaliza con entradas opcionales (`reglas`, `tituloValores`, `valores`); los usos que no las pasan se ven igual que antes |
| D-F20 | La raíz (`/`) pasa por `inicioGuard`: sin sesión va a `/login`; con token y usuario guardados va al inicio de su rol con `rutaDeInicio` (`core/utils/rutas-por-rol.ts`), la misma función que usa el login. `usuario-publico/solicitudes` sigue existiendo pero ya no es la entrada |
| D-F21 | El login es un `<form>` con `(ngSubmit)` y botón `type="submit"` (`SimpleButtonComponent` recibe `tipo`); Enter envía y una bandera `enviando` bloquea el doble envío |
| D-F22 | Tras una carga Excel exitosa el diálogo muestra solo el resultado (y, si se pasa `describir`, la lista de lo creado, como los códigos de asignatura); el botón pasa a "Cerrar" y la tabla se refresca al cerrar |

## Paso 10 — Verificar

No se prueba contra el backend real (D-F14). La verificación es:

```
cd frontend
npx ng test --watch=false --browsers=ChromeHeadless
npm run build
```

- Specs con el servicio real y `HttpTestingController` para cuerpos, partes
  multipart y parámetros. `RouterTestingHarness` con rutas reales para una
  navegación, no un espía de `Router`.
- Los specs que montan `InputAnexoUploadComponent` necesitan
  `provideHttpClient()` y `provideHttpClientTesting()`; los que abren
  diálogos, `provideNoopAnimations()`.
- Un `--include` por carpeta; un comodín no las toma todas.
- 58 specs de Julián fallan siempre con `NG0201` (falta `HttpClient`). Compara
  la lista con la de la tarea anterior: tiene que ser idéntica. No se tocan esos
  archivos.
- `npm run build` con código 0 y el bundle inicial bajo 840 kB.
- Archivos en CRLF; sin comentarios ni emojis en el código.
- Registra la tarea al principio de `change.md`.

Para revisar a mano la pantalla con cada rol, usa
`docs/frontend/checklist-recorrido-completo.md`.
