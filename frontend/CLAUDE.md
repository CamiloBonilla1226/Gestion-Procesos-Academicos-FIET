# Frontend — Gestión de Procesos Académicos FIET

Angular 20 (standalone components, sin NgModules), Bootstrap 5 como base
visual, PrimeNG de forma puntual, SweetAlert2, SSR con Express (PrimeFlex
está instalado pero no se carga; ver "Estilos"). Hereda la estructura de Julián
Camacho en `src/app`. Todo lo nuevo sigue esa misma organización y
reutiliza los componentes compartidos que ya existen, no se crean
versiones propias de una tabla, un diálogo o un botón si ya hay uno en
`shared/`.

## Comandos

```
npm install
npm start          # ng serve, http://localhost:4200
npm run build
```

## Estructura

```
src/
├── enviroments/             ojo: la carpeta se llama así, con ese typo
│                              ("enviroments", no "environments") — los
│                              imports existentes usan esa ruta, y
│                              cualquier import nuevo debe usar la misma
└── app/
    ├── core/
    │   ├── auth/              login, guards (auth-guard, role-guard), interceptor JWT
    │   │                        y sesion-vencida-service (manejo del 401, ver "Sesión vencida")
    │   ├── models/<Recurso>/DTORequest|DTOResponse/   interfaces TS, un archivo por DTO
    │   ├── services/           *-service.ts, uno por recurso, inyectados en 'root'
    │   ├── constantes/          constantes.ts de Julián y procesos-academicos.ts de la extensión
    │   ├── utils/               funciones de la extensión: validaciones, formato, errores HTTP,
    │   │                         descargas, parámetros de consulta, bandejas, usuarios académicos
    │   ├── estudiante/          rol Estudiante (extensión)
    │   ├── funcionario-academico/  rol Funcionario Académico (extensión)
    │   ├── decano/              rol Decano en los procesos académicos (extensión)
    │   └── <rol>/              roles de Julián (funcionario, secretario-general,
    │                            secretariaFiet, "usuario fiet", "usuario publico"...)
    │       ├── components/      piezas de ese rol (sidebar, etc.)
    │       └── pages/            una página por ruta de ese rol
    ├── layouts/                 layout general de la aplicación
    └── shared/
        ├── pages/                page-component (compone sidebar+header+breadcrumb+main+footer)
        │   ├── <x>-component/     wrappers delgados (est-*, fun-acad-*, dec-*, admin-academica-component)
        │   └── content/           componentes de contenido; ahí viven también los componentes
        │                           de proceso (*-proceso-component), el detalle común, la bandeja
        │                           común y las pestañas de administración (admin-*)
        ├── others/               piezas compartidas: anexos-view (Julián), anexos-academicos,
        │                          datos-usuario-academico, carga-excel-academica
        ├── buttons/, inputs/, search/, paginator/, sidebars/
        ├── generic-dialog-form-component/, generic-dialog-info-component/
        ├── table-generic-component/, card-main-component/
        └── headers/, footers/, breadcrumb/
```

Dos carpetas de rol existentes llevan espacio en el nombre:
`core/usuario fiet/` y `core/usuario publico/`. No se renombran (romperían
todos los imports existentes), pero un IDE o una terminal pueden tratarlas
distinto a una ruta sin espacio — tenlo presente al crear archivos ahí
dentro o al referenciarlas desde una tarea de build.

Estudiante y Funcionario Académico llevan carpeta de rol propia en `core/`;
`core/funcionario` es del comité de Julián y no se reutiliza.

También hay un typo heredado en `core/models/Sesión/DTPResponse/` (debería
decir `DTOResponse`, falta la "O", y el nombre de carpeta lleva tilde). No
se corrige porque ya hay imports reales apuntando ahí — un modelo nuevo que
no sea de Sesión sí va en `DTOResponse` bien escrito, como el resto.

## El patrón de composición de páginas

Una ruta no arma su HTML a mano: una página (`core/<rol>/pages/...`) solo
declara qué componentes de `shared/` va a usar, como propiedades de clase,
y se las pasa a `app-page-component` como `[sidebarComponent]`,
`[headerComponent]`, `[breadcrumbComponent]`, `[mainComponent]` (y
opcionalmente `[breadcrumbInputs]`, si el breadcrumb necesita datos). El
"contenido principal" casi siempre tiene un nivel intermedio: un wrapper de
rol (p.ej. `FunSolicitudesComponent`), delgado, que solo referencia un
componente de contenido en `shared/pages/content/` (p.ej.
`FunSolicitudesContentComponent`) a través de una propiedad `content`, y
cuyo HTML lo renderiza con
`<app-content-component [title]="'...'" [bodyComponent]="content">`
(`ContentComponent`, en `shared/pages/content/content-component`). El componente de contenido es donde está la
lógica real — llamadas a servicios, estado, manejo de diálogos. Un módulo
nuevo de un proceso académico sigue ese mismo esquema de tres niveles, no
mete la lógica directo en la página de `core/<rol>/pages`.

Componentes reutilizables clave, ya existentes, a usar en vez de crear
nuevos:

- `TableGenericComponent` — tablas con paginado
- `CardMainComponent` — tarjeta contenedora con botones de acción
- `GenericDialogFormComponent` / `GenericDialogInfoComponent` — diálogos de
  formulario y de solo lectura
- `Paginator`, `BarraBusquedaComponent`
- `InputTextComponent`, `InputSelectComponent`, `InputTextTareaComponent`,
  `InputDateComponent`
- `AnexosViewComponent` — visor/descarga de anexos adjuntos

## Servicios y modelos

Un servicio por recurso en `core/services/<recurso>-service.ts`, con la URL
armada sobre `environment.apiUrl` (importado desde `../../../enviroments/
environment`) y un método por endpoint, devolviendo `Observable<...>`. La
mayoría de servicios no se suscriben a sí mismos y dejan que el componente
se suscriba, pero hay excepciones reales en el propio código de Julián
(`AuthService` sí usa `pipe`/`tap`/`switchMap`/`catchError` internamente
para mantener el estado de sesión en un `BehaviorSubject`) — esa excepción
es válida cuando el servicio necesita mantener estado compartido, no
cuando es un CRUD simple. Los modelos van en `core/models/<Recurso>/
DTORequest/` y `core/models/<Recurso>/DTOResponse/`, una interfaz por
archivo, con el mismo nombre que su DTO equivalente en el backend
(`RolDTOPeticion` ↔ `RolDTOPeticion`, `RolDTORespuesta` ↔
`RolDTORespuesta`). Un recurso nuevo del backend (por ejemplo
`SolicitudAcademica`) sigue exactamente ese mismo mapeo 1 a 1.

## Autenticación

El token JWT se guarda en `localStorage` bajo la clave `authToken`, y el
usuario actual bajo `currentUser`. `authInterceptor`
(`core/auth/interceptor`) lo agrega como header `Authorization: Bearer` a
toda petición saliente. Las rutas protegidas usan **los dos guards juntos**,
`canActivate: [AuthGuard, RoleGuard]`, con `data: { roles: [...] }` listando
los roles permitidos como strings exactos (`'Secretario General'`,
`'Funcionario'`, `'Secretaria Decanatura FIET'`, etc., igual a como están
en la tabla `roles` de la base). El Decano de los procesos académicos nuevos usa
el rol existente `'Decano'`. Los roles nuevos son `'Estudiante'` y
`'Funcionario Académico'` (se crean en el `data.sql` del backend): el string
de `data.roles` debe coincidir carácter por carácter con la columna `nombre`
de la tabla `roles`.

Como la app usa SSR (Express + hidratación), cualquier acceso nuevo a
`localStorage` o `window` debe comprobar `typeof window !== 'undefined'`
antes, igual que ya hace `RoleGuard`. Ojo: no todo el código existente
cumple esto (el `authInterceptor` y partes de `AuthService` acceden a
`localStorage` sin esa comprobación) — es una inconsistencia real del
código base, no un patrón a imitar. El código nuevo sí la respeta siempre.

## Feedback al usuario

Los mensajes de éxito/error puntuales van por `ToastService`. Los errores
de petición HTTP se centralizan en `ErrorHandlerService`. La mayoría de
componentes de contenido siguen esto, aunque alguno de los componentes más
específicos (por ejemplo los de visualización/descarga de anexos) llama a
`HttpClient` o maneja el error de forma más directa — para código nuevo, la
regla por defecto sigue siendo: servicio en `core/services` + `ToastService`
+ `ErrorHandlerService`, no `HttpClient` suelto en el componente.

## app.config.ts

`src/app/app.config.ts` exporta `appConfig: ApplicationConfig` con estos
providers, en este orden exacto:

1. `MessageService` (de `primeng/api`, lo usa `ToastService`)
2. `provideBrowserGlobalErrorListeners()`
3. `provideZoneChangeDetection({ eventCoalescing: true })`
4. `provideRouter(routes)`
5. `provideClientHydration(withEventReplay())`
6. `provideAnimationsAsync()`
7. `providePrimeNG({ theme: { preset: Aura } })`, con `Aura` importado de
   `@primeuix/themes/aura`
8. `provideHttpClient(withInterceptors([authInterceptor]))`

El `authInterceptor` sí se registra ahí, con `withInterceptors([...])`
dentro de `provideHttpClient`, y es el único interceptor. No hay
`withFetch()`. Un interceptor HTTP nuevo se agregaría al arreglo de
`withInterceptors([...])` en ese mismo lugar; los servicios son
`providedIn: 'root'` y no se registran en ningún provider.

SSR: `app.config.server.ts` mezcla `appConfig` con
`provideServerRendering(withRoutes(serverRoutes))` mediante
`mergeApplicationConfig`. `app.routes.server.ts` de Julián declaraba una sola
ruta `'**'` con `RenderMode.Prerender`; la extensión le agrega antes tres
rutas con `RenderMode.Client`, una por cada detalle con parámetro
(`estudiante/solicitudes/:uuid`, `funcionario-academico/solicitudes/:uuid`,
`decano/solicitudes/:uuid` y, desde T12.28, `docente/notificaciones/:uuid`), porque una ruta con `:uuid` no se puede
prerenderizar sin `getPrerenderParams`. Una ruta nueva con parámetro necesita
su entrada `RenderMode.Client` ahí.

## Estilos: PrimeNG, PrimeFlex y Bootstrap

No hay un reparto limpio entre las tres librerías; el patrón real es:

- **Bootstrap 5** es la base visual. Se carga como CSS y JS global en
  `angular.json` (`bootstrap.min.css`, `bootstrap.min.js`, `popper`). Aporta
  el layout y las utilidades (`d-flex`, `row`, `col-md-6`, `container`,
  `gap-2`, `mt-3`, `align-items-center`, `text-center`) y también los
  componentes de formulario y tabla: los inputs son `form-control` /
  `form-floating` / `form-select` (por ejemplo `InputTextComponent`) y
  `TableGenericComponent` es un `<table class="table table-bordered">`.
- **PrimeNG** se usa solo para unas pocas piezas: `p-dialog`
  (`DialogModule`, base de `GenericDialogFormComponent` y
  `GenericDialogInfoComponent`), `ButtonModule`, `FileUpload`,
  `FloatLabelModule` (solo en `InputPasswordComponent`) y `Avatar`. No se usa
  `p-table`, `p-dropdown`, `p-inputtext` ni otros componentes de formulario
  de PrimeNG. El tema es Aura, registrado en `app.config.ts`.
- **PrimeFlex** está en `package.json`, pero no se importa en
  `angular.json`, en `styles.css` ni en ningún componente. Las clases de
  layout que se ven en los HTML (`d-flex`, `gap-2`, `align-items-center`)
  son de Bootstrap, no de PrimeFlex. No se puede confirmar que PrimeFlex
  tenga ningún efecto en la aplicación; no se usa para código nuevo.
- Lo demás son estilos propios por componente en su `.css` (colores
  de marca como `#1E257B` pasados incluso como `@Input` a
  `SimpleButtonComponent`) y `src/styles.css`, que importa `primeicons`,
  resetea `html, body` y, desde T12.2-B, define los tokens del diseño en
  `:root` (`--navy-900`, `--navy-700`, `--navy-ink`, `--accent`, `--panel`,
  `--line`, `--text`, `--muted`, `--sidebar-w`, `--fuente`...). Todo lo demás
  va en el CSS de cada componente: el CSS global entra en el bundle inicial y
  el de los componentes no (D-F27).

Regla para código nuevo: layout, utilidades, tablas y campos con Bootstrap;
diálogos y botones con los componentes compartidos que ya envuelven PrimeNG;
no introducir PrimeFlex ni otros componentes de PrimeNG.

## Formularios

Julián no usa `ReactiveFormsModule` en ninguna parte: no hay `FormGroup`,
`FormControl` ni `FormBuilder` en todo `src/app`. Tampoco hay formularios
template-driven con `<form>` y `ngForm`. El patrón real es otro, y es el que
se sigue en los formularios nuevos:

- Cada campo es un componente compartido de `shared/inputs/`
  (`InputTextComponent`, `InputTextTareaComponent`, `InputSelectComponent`,
  `InputDateComponent`, `InputPasswordComponent`) con `@Input() label`,
  `@Input() required`, `@Input() value` y `@Output() valueChange`
  (`InputSelectComponent` además recibe `options`). Se enlaza con two-way
  binding de componente: `[(value)]="modelo.campo"`. `forceValidation` solo
  existe en los de texto, textarea y select. `InputAnexoUploadComponent` es
  la excepción: no usa `value`, recibe `tipoAnexo` y `uploadedFile` y emite
  `fileSelected` / `fileRemoved`. `ngModel` aparece solo dentro de los
  componentes de `shared/inputs/` y de `BarraBusquedaComponent`, no en las
  páginas ni en los componentes de contenido.
- El estado del formulario es un objeto plano en el componente (por ejemplo
  `nuevaSolicitud: SolicitudDTOPeticion` en
  `EnviarSolicitudUsuarioFietComponent`, o `selectedRolForm` en
  `RolesContentComponent`), no un `FormGroup`.
- Validación: cada input expone `touched` e `isInvalid()` (obligatorio y
  vacío). Antes de guardar, el componente de contenido obtiene los inputs con
  `@ViewChild`, marca `touched = true` y consulta `isInvalid()`; si alguno es
  inválido muestra `toastService.showError(...)` y retorna. Referencia:
  `guardarRolActualizado()` en
  `shared/pages/content/roles-content-component/roles-content-component.ts`.
  No hay validadores personalizados ni mensajes de error por tipo de regla:
  solo "obligatorio".
- Formulario en diálogo: `GenericDialogFormComponent` (un paso, evento
  `(save)`) o `GenericDialogStepsFormComponent` (varios pasos con
  `steps[].contentTemplate` y `canContinue`). Referencia de varios pasos:
  `core/usuario fiet/components/enviar-solicitud-usuario-fiet-component`,
  que valida el paso 1 con un `canContinueStep1()` manual.
- Envío: se arma el DTO de petición, se llama al servicio con
  `.subscribe({ next, error })` y el error va a `ErrorHandlerService`.

Referencias revisadas: `LoginFormComponent` (`shared/login-form-component`),
`RolesContentComponent` y `EnviarSolicitudUsuarioFietComponent`. Los tres
enlazan igual (`[(value)]` sobre variables u objetos planos, sin
`FormGroup`), así que esa parte sí está confirmada. La validación varía:
`RolesContentComponent` usa `@ViewChild` + `isInvalid()`,
`EnviarSolicitudUsuarioFietComponent` usa `canContinue` por paso, y
`LoginFormComponent` solo marca `[required]` en los inputs y no valida antes
de llamar a `onSubmit()`. Ningún formulario de Julián valida reglas que no
sean "obligatorio" (rangos, fechas, plazos, formato de anexos). Los
formularios de la extensión no usan `@ViewChild` + `isInvalid()`: validan el
objeto plano con las funciones de `core/utils` antes de enviar y pintan cada
error debajo de su campo (ver "Validación de archivos y campos").

## Procesos académicos y administración (extensión del trabajo de grado)

Esta sección describe lo que la extensión agregó sobre el código de Julián:
los tres procesos (Cancelación de Matrícula, Cancelación de Asignatura y
Examen Supletorio) y la administración académica. El contrato HTTP está en
`docs/api/contrato-api-procesos-academicos.md` y manda sobre cualquier
maqueta HTML.

### Rutas y menús por rol

Todas las rutas usan `canActivate: [AuthGuard, RoleGuard]` y `data.roles`
armado con `ROLES_ACADEMICOS` (`core/constantes/procesos-academicos.ts`).
Todas se cargan con `loadComponent` (carga diferida); el bundle inicial no
debe pasar de 840 kB.

| Rol | Rutas | Ítems del menú |
| --- | --- | --- |
| Estudiante | `estudiante`, `estudiante/solicitudes`, `estudiante/solicitudes/:uuid`, `estudiante/cancelacion-matricula`, `estudiante/cancelacion-asignatura`, `estudiante/examen-supletorio` | Mi Usuario, Mis solicitudes, Cancelar matrícula, Cancelar asignatura, Examen supletorio |
| Funcionario Académico | `funcionario-academico`, `funcionario-academico/solicitudes`, `funcionario-academico/respuestas`, `funcionario-academico/solicitudes/:uuid` | Mi Usuario, Gestión de solicitudes (Solicitudes, Respuestas) |
| Decano | `decano`, `decano/solicitudes`, `decano/solicitudes/:uuid`, `decano/solicitudes-consejo`, `decano/administracion-academica` | Mi Usuario, Gestión académica (Solicitudes académicas, Administración académica), Solicitudes del Consejo |
| Secretario General | `sec-general/administracion-academica` (más las de Julián) | Una línea agregada al menú de Julián: Administración académica |
| Docente | `docente/notificaciones`, `docente/notificaciones/:uuid` (más las de Julián en `usuario-fiet`) | El menú de Julián de `usuario-fiet` (Mi Usuario, Solicitudes) suma Notificaciones solo cuando el usuario tiene rol Docente (D-F44) |

El login (`shared/login-form-component`) redirige por rol en este orden:
Secretario General, Funcionario (Julián), Secretaria Decanatura FIET, Decano,
Funcionario Académico, Estudiante y luego los usuarios FIET. El Decano va
antes que el Funcionario Académico y que el Estudiante. Esa lógica vive en
`rutaDeInicio` (`core/utils/rutas-por-rol.ts`) y la reutiliza `inicioGuard` en la
ruta raíz. "Cerrar sesión" (`avatar-sesion-component`) llama a
`AuthService.logout()`, evita el `href="#"` del enlace y navega a `/login`.

### Capa API

Un servicio por recurso en `core/services`: `asignatura-service`,
`estudiante-service`, `funcionario-academico-service`,
`catalogo-academico-service`, `solicitud-academica-service`,
`cancelacion-matricula-service`, `cancelacion-asignatura-service` y
`examen-supletorio-service` y, desde T12.28, `notificacion-docente-service`.
Cubren los 76 endpoints del contrato. Los
modelos son `interface` con el mismo nombre del DTO Java, en
`core/models/<Recurso>/DTORequest|DTOResponse` (D-F1).

- Los `FormData` se arman dentro del servicio (radicaciones, anexos, escaneo,
  Excel); el componente le pasa archivos y valores planos. Nunca se pone
  `Content-Type` a mano.
- Los parámetros de consulta se arman con `armarParametros`
  (`core/utils/parametros-http.ts`), que omite nulos y vacíos.
- Las descargas piden `responseType: 'blob'` y pasan por `parsearErrorBlob`
  (`core/utils/descargas.ts`), que convierte un error en blob de vuelta en
  JSON para poder leer `codigoError`; `guardarArchivo` dispara la descarga.

### Detalle común y punto de extensión `COMPONENTES_PROCESO`

Las tres bandejas usan `bandeja-solicitudes-academicas-component` (filtro y
paginado en el cliente con `core/utils/bandeja-solicitudes.ts`). Los tres
detalles usan `detalle-solicitud-academica-component`, que recibe `rol`
(`ESTUDIANTE`, `FUNCIONARIO` o `DECANO`) desde el wrapper de cada rol. El
detalle común muestra datos generales, estudiante, anexos (con descarga),
Resolución (si `puedeDescargarResolucion`) e historial, y debajo inserta el
componente del proceso:

```ts
export const COMPONENTES_PROCESO = {
  'cancelaciones-matricula': () => import('../cancelacion-matricula-proceso-component/...'),
  'cancelaciones-asignatura': () => import('../cancelacion-asignatura-proceso-component/...'),
  'examenes-supletorios': () => import('../examen-supletorio-proceso-component/...')
};
```

El proceso sale de `tipoSolicitud` con `procesoDeTipoSolicitud`. El detalle
inserta el componente con `ngComponentOutlet` y cuatro entradas:
`uuidSolicitud`, `solicitud`, `rol` y `recargar`. Un proceso nuevo es un
componente con esas cuatro entradas y una línea en ese mapa; cada uno queda
en su propio chunk diferido.

### Cómo decide la pantalla qué mostrar

- Los botones salen de `solicitud.accionesDisponibles` filtradas por las
  acciones que maneja el proceso; nunca se deducen del rol en el cliente.
- Funcionario y Decano: los textos y avisos dependen de `etapaCodigo`, no de
  `etiqueta`. La insignia usa `claseInsigniaEtapa`: verde en APROBADA, rojo en
  RECHAZADA, amarillo en PENDIENTE_PAGO y EN_VERIFICACION_PAGO (D-F9), gris en
  el resto.
- Estudiante (D-F23): ninguna vista suya muestra ni usa `etapaCodigo` para
  decidir colores, íconos, textos o pasos; solo `etiqueta` y
  `accionesDisponibles`. Las etiquetas del Estudiante están en
  `ETIQUETAS_ESTUDIANTE` (`core/constantes/etiquetas-academicas.ts`) y la
  insignia sale de `claseInsigniaEtiquetaEstudiante`. Su historial no muestra
  la etapa de cada fila. El backend además le enmascara la decisión del Decano
  (P40 y P41 de `docs/database/etapas-por-proceso.md`).
- Tras cada acción el componente de proceso llama a `recargar()`, que vuelve a
  pedir el detalle y el historial; así cambian la etapa, los anexos y los
  botones.
- Cancelación de Asignatura: el Estudiante ve solo código y nombre antes de
  la etapa final, y con la etiqueta "Aprobada" o "Rechazada" también la
  decisión del Decano por asignatura, nunca su observación. Las columnas sin
  ningún dato se ocultan (también en la vista del Estudiante de Matrícula).
- Faltas y nota (las dos cancelaciones): texto o número de hasta 20
  caracteres. Si es número se valida como antes (`errorDeFaltas`,
  `errorDeNotaAsignatura`); la regla "nota menor a 3.0 no cumple" solo aplica
  a una nota numérica (`numeroDeValor`). Se envían tal como se escriben,
  recortadas; el backend normaliza "3,5" a "3.5".
- Bandejas (D-F28): la columna Estado (Decisión en el Decano) se pinta con
  ícono y texto según `categoriaEstado(etiqueta)`
  (`core/constantes/etiquetas-academicas.ts`), en los tres roles y sin mirar
  `etapaCodigo`: reloj ámbar (Pendiente), flechas azules (En Gestión, En
  trámite, verificación), flecha morada (por notificar, enviar recibo), check
  verde (Aprobada, Aprobada por el Decano, Respondida), x roja (Rechazada,
  Rechazada por el Decano) y exclamación ámbar (Pendiente de pago). Desde
  D-F36 los estados del Funcionario son Pendiente, En Gestión, Pendiente
  Notificar, Enviar Recibo, Recibo Pagado (flechas azules) y Respondida; la
  decisión del Decano ya no es un estado. El detalle del Funcionario muestra el bloque
  "Decisión del Decano" con la observación que trae la última fila
  `APROBAR_DECANO` o `RECHAZAR_DECANO` del historial.

### Diálogos

Todas las confirmaciones y formularios usan `GenericDialogFormComponent`. La
extensión le agregó el `@Input() textoBoton` (por defecto `Guardar`, con el
ancho de 70px de siempre, así los diálogos de Julián no cambian) (D-F12), y en
T12.2-C `textoCancelar` y `cerrarConEscape` (D-F31), que los diálogos de los
procesos pasan como "Cancelar" y `true`.
Nuestros diálogos pasan un texto por acción: "Rechazar solicitud",
"Remitir al Decano", "Aprobar solicitud", "Aceptar evaluación" y "Enviar
decisión" (Decano en Cancelación de Asignatura), "Enviar respuesta", "Enviar recibo", "Subir comprobante",
"Aprobar comprobante", "Rechazar comprobante", "Crear", "Guardar cambios",
"Cambiar responsable" y "Cargar archivo"; en el panel de la notificación de la Resolución,
"Guardar cambios", "Registrar firma" y "Reintentar" (D-F47). Cada envío lleva su propia bandera
`enviando` que bloquea el doble envío (D-F7).

El campo de observación de cada acción sale de `campoObservacion`
(`core/constantes/etiquetas-academicas.ts`), con la misma etiqueta en los tres
procesos (D-F35), y se valida con `errorDeObservacion`: "Observación solo para
el estudiante (se entrega como respuesta)" obligatoria al rechazar en el
Funcionario y al rechazar el comprobante, y opcional al enviar la respuesta
(hasta 500); "Observación para el estudiante (opcional)" al aprobar el comprobante del
supletorio (D-F44); "Observaciones para el Decano" opcional al remitir, y es el único
campo de observación al remitir (D-F36); "Observación (opcional)" al aprobar y rechazar
en el Decano, con el aviso "Esta observación solo será visible para el
Funcionario Académico."; y "Observación" obligatoria al rechazar el
comprobante. Cancelación de Asignatura cambia la obligatoriedad según la tabla
(ver D-F35). Las situaciones académicas salen del catálogo, sin lista fija.

Un diálogo que depende de un catálogo (situaciones académicas para remitir o
aprobar una cancelación, tipos de anexo para el recibo y el comprobante) se
abre solo después de que el catálogo llega; si falla o viene vacío se
muestra el error por `ErrorHandlerService` y el diálogo no abre.

### Errores

- Regla de negocio: HTTP 500 con `codigoError`. Se lee con `leerCodigoError`
  (`core/utils/errores-http.ts`) y el mensaje lo muestra
  `ErrorHandlerService`. Los códigos están en `CODIGOS_ERROR`.
- Validación del DTO: HTTP 400 con un mapa `{campo: mensaje}`. Se lee con
  `erroresDeCampos` y cada mensaje se pinta junto a su campo; los que no
  corresponden a un campo se listan en un bloque `data-error="servidor"`.
- En administración, `mensajesDeError` (`core/utils/usuarios-academicos.ts`)
  devuelve la lista para mostrar: el `mensaje` de un 500 o los valores del
  mapa de un 400. La carga por Excel los muestra en una lista dentro del
  diálogo, no solo en el toast.
- Una solicitud en curso del mismo tipo no se avisa antes de radicar; se
  muestra el error 4 del backend al radicar (D-F13).
- Las contraseñas nunca se escriben en consola ni en mensajes.

### Sesión vencida (401)

`authInterceptor` (Julián) sigue poniendo `Authorization`. La extensión le
agregó una línea que pasa los errores a `SesionVencidaService`
(`core/auth/interceptor/sesion-vencida-service.ts`) (D-F8). Ante un 401 que no
sea `POST sesiones`, si la petición llevaba el token vigente: llama a
`AuthService.logout()`, muestra "Tu sesión venció, inicia sesión de nuevo" y
navega a `/login`. Las demás peticiones que fallen a la vez llevan un token
que ya no es el vigente y se ignoran, así hay un solo aviso y una sola
redirección. Un 403 y un 500 no cierran la sesión. El servicio carga
`ToastService` con un `import()` diferido: el interceptor está en el bundle
inicial y una importación directa arrastra SweetAlert2 a ese bundle (subía
de 832 kB a 911 kB).

### Validación de archivos y campos

Las reglas que no son "obligatorio" viven en
`core/utils/validaciones-academicas.ts` y `core/utils/usuarios-academicos.ts`:

- `errorDeArchivo(archivo, formatos, maximo)`: vacío, tamaño (nunca más de
  5 MB, o el `tamanioMaximoBytes` del formulario si es menor) y extensión, con
  `jpg` y `jpeg` equivalentes. El tamaño se valida en nuestro componente
  porque `InputAnexoUploadComponent` de Julián no lo valida (D-F5). Además se
  controla el total de 20 MB por envío.
- `InputAnexoUploadComponent` se usa con el adaptador `comoTipoAnexo`. Ese
  componente pinta "Este anexo es obligatorio" en cuanto no hay archivo, sin
  esperar el intento de envío; es comportamiento de Julián y no se cambia.
- `errorDeTexto` (obligatorio y máximo, con concordancia de género),
  `leerNota` (0.0 a 5.0 con un decimal), `leerFaltas` (entero mayor o igual a
  0), `esFechaValida` (AAAA-MM-DD real), `esHoraValida` (HH:mm) y
  `fechaDeHoy`.
- `validarDatosUsuario`: límites del contrato para nombres, apellidos,
  documento, teléfono, correo, username y contraseña.
- Edición de estudiantes y funcionarios: `datosUsuarioDe` llena el formulario
  con los valores actuales, `cambiosDeTexto` arma el `PUT` solo con los campos
  que cambiaron (uno vacío no cambia) y `validarCambiosPersonales` valida solo
  esos campos.

### Decisiones del frontend

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
| D-F23 | En las vistas del Estudiante nunca se muestra ni se usa `etapaCodigo`; solo `etiqueta` y `accionesDisponibles` |
| D-F24 | La edición de estudiante y funcionario académico abre con los valores actuales y envía solo lo que cambió; `DatosUsuarioAcademicoComponent` recibe `edicion` para ocultar usuario y contraseña y quitar los obligatorios |
| D-F25 | Los procesos a cargo se ven de solo lectura al editar un funcionario: el `PUT` no los recibe y se cambian en la pestaña Responsable por proceso |
| D-F26 | La facultad del estudiante es siempre FIET: se muestra de solo lectura y no se envía |
| D-F27 | Rediseño visual (T12.2-B): tokens en `:root` de `src/styles.css` y el resto del estilo en el CSS de cada componente compartido, que carga en diferido, para no subir el bundle inicial. Fuente `var(--fuente)` en todos los `.css` |
| D-F28 | Estado de las bandejas con ícono y texto por `categoriaEstado(etiqueta)`, nunca por `etapaCodigo`; sin insignia aparte de decisión del Decano |
| D-F29 | `TableGenericComponent` acepta `cellTemplate` opcional por columna (contexto `$implicit` = fila, `valor` = celda); sin plantilla pinta el texto como antes |
| D-F30 | `SidebarComponent` acepta `children` por ítem (submenú desplegable con ">") y resalta la ruta más específica que coincide con la URL. El Decano agrupa Solicitudes académicas y Administración académica en "Gestión académica" |
| D-F31 | Entradas opcionales nuevas en componentes de Julián, sin cambiar a quien no las pasa: `SimpleButtonComponent.contorno`, `GenericDialogFormComponent.textoCancelar` y `cerrarConEscape`, `TableGenericComponent.encabezadoAgrupado` |
| D-F32 | El estilo compartido de detalle, procesos y formularios vive en `shared/pages/content/estilos-academicos.css` y `estilos-formularios.css`, que cada componente incluye en `styleUrls` (carga diferida, no toca el bundle inicial) |
| D-F33 | Los formularios del Estudiante siguen siendo de una sola página; el stepper solo guía. Los datos del primer bloque salen del usuario de la sesión, no del formulario |
| D-F34 | Catálogo del Estudiante en `/estudiante/catalogo` (ítem "Solicitudes" del menú), con los textos oficiales en `TRAMITES_ESTUDIANTE`. Sus botones son `<a href>` con `router.navigateByUrl` en `(click)`, no `routerLink`: `RouterLink` subía 4,5 kB el bundle inicial. `BotonSidebar.activaEn` (opcional) marca un ítem en rutas que no cuelgan de la suya |
| D-F35 | Observaciones y decisión por asignatura (T12.10). Una sola observación por solicitud al remitir, sin campo bajo la tabla (etiqueta y obligatoriedad actualizadas en D-F36). Decano: interruptor Aprobar / Rechazar por fila que parte de `cumpleCondiciones`; "Aceptar tal como está" (`aceptarEvaluacion`, solo sin cambios) o "Enviar decisión modificada" (`decisiones`, solo con cambios, y `decano/rechazar` si todas quedan en Rechazar); sin situación al cancelar ni observación por asignatura. Funcionario: "Cambiada por el Decano" en la tabla; la respuesta pide situación a las aprobadas sin ella y observación a las rechazadas cambiadas (`ajustes`), y la observación general es obligatoria con cualquier rechazo del Decano. Código estudiantil del nivel superior del detalle |
| D-F36 | Ajustes posteriores a T12.10 (P46 del backend). Al remitir, "Observaciones para el Decano", siempre opcional (500): ya no se copia a `observacionEvaluacion` (va nula) y la tabla no tiene la columna "Observación de la evaluación"; el historial del detalle no muestra la observación de `REMITIR_DECANO`, que queda solo en la tarjeta "Observación para el Decano". El Funcionario ve un aviso arriba de la tabla si el Decano cambió alguna decisión. El Estudiante, en la etapa final de Cancelación de Asignatura, ve "Situación académica al cancelar" de las aprobadas. El formulario del Estudiante muestra su código estudiantil (`codigoEstudiantil` del `GET .../formulario`). La bandeja del Funcionario suma la columna Código, y sus estados son Pendiente, En Gestión, Pendiente Notificar, Enviar Recibo, Recibo Pagado y Respondida (datos del backend); la decisión del Decano no es un estado (ver D-F37) |
| D-F37 | T12.11 (P47 del backend). La columna "Decisión del Decano" que se agregó en la bandeja del Funcionario se quitó en T12.12 (D-F38); `decisionDecano` sigue en el modelo del resumen pero no se muestra. En "Enviar respuesta" de Cancelación de Asignatura solo las aprobadas sin situación guardada o cambiadas por el Decano llevan selector de situación; las rechazadas no llevan campo propio ni van en `ajustes`, y el motivo va en la observación general, obligatoria si hay alguna rechazada o el Decano rechazó la solicitud |
| D-F38 | T12.12. El Funcionario tiene dos vistas de la misma bandeja (`BandejaSolicitudesAcademicasComponent` con `@Input() modo`, `solicitudes` por defecto), sin duplicar el componente: "Solicitudes" (`funcionario-academico/solicitudes`) muestra solo las no finalizadas (`etapaCodigo` fuera de APROBADA y RECHAZADA), así que su filtro de estado ya no ofrece "Respondida"; "Respuestas" (`funcionario-academico/respuestas`, `data.modo = 'respuestas'`, página propia con la miga "Respuestas") muestra solo las finalizadas, con la tarjeta "Solicitudes respondidas", las mismas columnas y filtros, y en Estado "Aprobada" o "Rechazada" según `etapaCodigo` con los íconos de siempre; su filtro de estado ofrece solo esas dos. `FunAcadSolicitudesContentComponent` lee el modo de `ActivatedRoute.data` como signal y la bandeja lo recalcula en `ngOnChanges` (T12.12b). El ojo abre el mismo detalle (`funcionario-academico/solicitudes/:uuid`). El menú agrupa las dos opciones bajo "Gestión de solicitudes" con los `children` del sidebar. El modo solo afecta al Funcionario. Sin columna "Decisión del Decano"; el bloque del detalle se queda |
| D-F39 | T12.13 (P48 del backend), agrupado en T12.14. En Respuestas, cada Cancelación de Asignatura da como máximo dos filas, agrupando `resultadosAsignaturas` por `aprobada`: una "Aprobada" si hay alguna aprobada y una "Rechazada" si hay alguna rechazada (una sola si todas coinciden), sin columna nueva. Matrícula, Supletorio y una Cancelación de Asignatura sin resultados siguen con una fila y el estado por `etapaCodigo`. El filtro de estado se aplica por fila; tipo, documento y código se aplican a la solicitud y a todas sus filas. Clave por fila: `uuidSolicitud:resultado` (en el ojo, `data-clave`). El pie cuenta solicitudes distintas, no filas. La fila agrupada abre el detalle con `?resultado=aprobada` o `?resultado=rechazada` (`PARAMETRO_RESULTADO`, `RESULTADOS_FILA`, `resultadoFilaValido`); `CancelacionAsignaturaProcesoComponent` lo lee de forma reactiva y muestra todas las asignaturas: las del resultado de la fila primero y resaltadas, las demás después, atenuadas y con "Otra asignatura de la solicitud", y arriba de la tabla "Se resaltan las asignaturas aprobadas (o rechazadas) de la solicitud". El grupo de cada asignatura se calcula como en el backend (aprobada solo con `aprobadaPorDecano` verdadero), pero la celda "Asignatura" no lleva distintivo de resultado (quitado en T12.15b): el resultado de cada asignatura se lee en la columna "Decisión del Decano" (Aprobada / No aprobada), a la derecha, que ven el Funcionario, el Decano y el Estudiante en etapa final. Si el Funcionario rechazó antes del Decano, ninguna asignatura tiene decisión y esa columna no aparece; el estado de la solicitud dice Rechazada. El resaltado solo aplica al Funcionario en etapa final. Sin parámetro, con uno inválido, sin asignaturas de ese resultado o fuera de etapa final, la tabla se ve como desde Solicitudes. El resto del detalle y Solicitudes no cambian |
| D-F40 | Historial sin comentarios (T12.14, punto 6). Cada fila del historial muestra solo la acción, el usuario, la fecha y, fuera del Estudiante, la insignia de etapa; ninguna observación, para los tres roles y los tres procesos. Las observaciones viven en sus bloques: "Observación para el Decano" (`REMITIR_DECANO`, Funcionario y Decano), "Decisión del Decano" (última `APROBAR_DECANO` o `RECHAZAR_DECANO`, ahora también para el Decano, que si no dejaría de ver su propia observación) y "Mensaje para el estudiante" (última `ENVIAR_RESPUESTA`, `RECHAZAR_FUNCIONARIO`, `RECHAZAR_COMPROBANTE` o, desde T12.28, `APROBAR_COMPROBANTE`, para los tres roles; todas cierran la solicitud, así que solo aplica una). Antes, el rechazo del Funcionario y el del comprobante del supletorio solo se veían en el historial. Sin cambios en el backend |
| D-F41 | Formatos de Examen Supletorio (T12.16). Son archivos fijos en `public/formatos/` (`formato-firma-docente.doc` y `formato-firma-docente-cruce.doc`), publicados por el `assets` de `angular.json` en `formatos/`; no se generan ni se editan. `EnlaceFormatoComponent` (`shared/others`) pinta "Descargar formato" como `<a>` con `href` relativo `formatos/<archivo>` (resuelve contra `<base href>`) y `download` con el nombre del archivo: sin router, sin otra pestaña. Constantes en `core/constantes/formatos-supletorio.ts`; el formulario elige el archivo por el nombre del tipo de anexo (`FORMATOS_POR_ANEXO`) y la ficha del catálogo por `AnexoCatalogo.formato`. El nombre que ve el estudiante, "Formato firmado por el docente que orienta la asignatura", viene del backend (`TIPO_ANEXO_ACADEMICO`), que lo cambió en el seed y en `AnexoAcademicoConstantes.FORMATO_FOR_23` |
| D-F42 | Remitir el supletorio sin casilla (T12.16, P49 del backend). El diálogo "Remitir al Decano" de Examen Supletorio solo tiene el resumen, el campo opcional "Observaciones para el Decano" y los botones; ya no hay casilla de requisitos verificados ni su validación, y `RemisionSupletorioDTOPeticion` es `{observacion}`. Los diálogos de remitir de las cancelaciones no tenían casilla equivalente: exigen la evaluación de cada asignatura en la tabla |
| D-F44 | T12.28. Cancelaciones: faltas, nota, situación en la matrícula y situación al cancelar son opcionales (`errorDeFaltas` y `errorDeNotaAsignatura` aceptan vacío y solo validan lo que llega); al remitir ya no se resaltan por faltantes y viajan como `null`; en la respuesta de Cancelación de Asignatura el selector de situación de las aprobadas es "Situación al cancelar (opcional)". Funcionario y Decano ven siempre las columnas de evaluación de Cancelación de Asignatura, con "—" si no hay dato, y el Estudiante en etapa final ve "Situación académica al cancelar" de las aprobadas o "—". Cancelación de Matrícula: el Decano no registra situaciones; aprobar abre el diálogo sin pedir el catálogo, solo con la observación, y la tabla es de solo lectura. Examen Supletorio: el diálogo "Aprobar comprobante" ya no tiene fecha acordada (ni el detalle la muestra); tiene el selector de profesor, obligatorio, y "Observación para el estudiante (opcional)", que sale en "Mensaje para el estudiante" y no en el historial. El selector (`SelectorDocenteComponent`, `shared/others`) va dentro de un `@defer (on immediate)`, así queda en su propio chunk: filtros de nombre, cédula y usuario, búsqueda en el servidor con "Buscar", tabla Nombre, Cédula, Usuario y paginador de Julián (5 por página). Funcionario y Decano ven "Profesor asignado" en el detalle. Docente: opción "Notificaciones" en el menú de `usuario-fiet` (el sidebar de Julián recibe sus botones de una propiedad que suma la opción si `AuthService.usuario$` trae el rol Docente); `docente/notificaciones` lista radicado, estudiante, asignatura, resultado y fecha, la más reciente primero y las no leídas en negrita con punto; el detalle (`docente/notificaciones/:uuid`) llama `PATCH .../leida` al abrir y muestra la solicitud de solo lectura con anexos descargables, sin historial ni observaciones. Páginas y wrappers con el patrón de tres niveles (`core/docente/pages`, `shared/pages/doc-*`), todo con `loadComponent`. El bundle inicial queda en 838,77 kB |
| D-F43 | Textos informativos de Cancelación de Matrícula (T12.18), sin cambiar reglas ni validaciones, todos en `core/constantes/textos-cancelacion-matricula.ts`. `VIGENCIA_PAZ_Y_SALVOS`: `nota-aviso` solo en la sección de anexos del formulario, antes de los campos de carga; el catálogo del estudiante no la muestra (T12.18b). `NOTA_SIMCA`: `nota-aviso` encima de la tabla de asignaturas de `CancelacionMatriculaProcesoComponent`, solo para Funcionario y Decano; ni el Estudiante ni Cancelación de Asignatura la ven. `NORMATIVA_CANCELACION_MATRICULA`: tarjeta "Normativa aplicable" al final del formulario, antes de los botones, con encabezados en negrita, literales a a d y un cuerpo de alto máximo 260 px con scroll propio (`.normativa` en `estilos-formularios.css`, enfocable con teclado) para no empujar los botones. La advertencia y la casilla del artículo 3 (D-F11) no cambian. Cancelación de Asignatura (T12.19, distribución del formato PA-GA-4.2-FOR-9), en `core/constantes/textos-cancelacion-asignatura.ts`: `ARTICULO_5_CANCELACION_ASIGNATURA` como `nota-aviso` al inicio del formulario, antes del stepper y de los datos del estudiante, con el encabezado en negrita; `NOTIFICACION_CORREO_INSTITUCIONAL` como `nota-gris` justo debajo de la lista de asignaturas (y de su error), antes del motivo, los soportes y los botones; `NOTA_SIMCA` (la misma constante de Matrícula, sin duplicar) justo encima de la tabla del detalle, solo para Funcionario y Decano. El formulario no lleva la nota SIMCA porque el estudiante solo elige asignaturas, no escribe faltas ni nota. Sin bloque "Normativa aplicable" en Cancelación de Asignatura; Matrícula y Supletorio no muestran estos textos |
| D-F45 | Documento adicional de la respuesta (P54 del backend). Desde T12.33 solo en Matrícula y Supletorio (D-F48). En las acciones del Funcionario que cierran la solicitud (`ACCIONES_CON_DOCUMENTO_ADICIONAL`: Rechazar y Enviar respuesta, Aprobar y Rechazar comprobante en el supletorio) el diálogo trae `DocumentoAdicionalComponent` (`shared/others`), que envuelve `InputAnexoUploadComponent` como "Documento adicional (opcional)": PDF de hasta 5 MB validado en el componente, nota de que va al estudiante por correo y aviso si ya hay uno cargado (lo reemplaza). `subirDocumentoAdicional` (`core/utils/documento-adicional.ts`) busca el uuid del tipo "Documento adicional de la respuesta" por nombre en `getTiposAnexoPorTipo` (sin uuid fijo) y lo sube con `adjuntarAnexo`. Orden: escaneo de la Resolución, documento adicional y la acción, con `switchMap`; tras subirlo se limpia el archivo para que un reintento no lo suba otra vez. Si el catálogo no tiene el tipo, error por `ErrorHandlerService` y la acción no se envía. Al remitir, el diálogo avisa que el documento cargado se retira. El correo lo envía el backend; el frontend no cambia nada más |
| D-F46 | T12.31 (P55, P56). El formulario de Cancelación de Matrícula muestra los anexos que manda el backend, hoy uno: "Paz y salvos (un solo PDF con todos los paz y salvos)"; cada anexo se valida con su `tamanioMaximoBytes` (10 MB para ese PDF; `errorDeArchivo` acepta hasta `TAMANIO_MAXIMO_PAZ_Y_SALVOS_BYTES` y el resto sigue en 5 MB) y la ayuda lo dice. En Cancelación de Matrícula y de Asignatura el diálogo "Rechazar la solicitud" ya no trae el campo de la Resolución ni la exige; solo "Enviar respuesta" la pide. En el detalle, si una cancelación quedó RECHAZADA y el historial tiene `RECHAZAR_FUNCIONARIO`, el Estudiante ve un aviso de que fue rechazada sin Resolución y no ve el botón de descargarla (Funcionario y Decano la siguen viendo si existe) |
| D-F49 | T12.34 (P60 del backend). Situaciones como listas cerradas en las dos cancelaciones. `GET catalogos-academicos/situaciones` trae `ambito` (`MATRICULA`, `CANCELACION` o nulo para la retirada R0) y `core/utils/situaciones-academicas.ts` arma las opciones (`opcionesDeSituacion`, solo el nombre, en el orden del backend) y el texto (`textoSituacion`, solo el nombre, sin "R1 -"). `CancelacionMatriculaProcesoComponent` y `CancelacionAsignaturaProcesoComponent` tienen `opcionesSituacion` (situación de la asignatura: Cursada por primera vez, Recursada por primera vez, Cursada por segunda vez, Cursada por tercera vez) y `opcionesSituacionCancelar` (No vista, Perdida), usados en la tabla al remitir y en "Situación al cancelar (opcional)" de la respuesta; si alguna lista llega vacía, error y el diálogo no abre. Siguen opcionales, con "—". Las tablas del Estudiante, Funcionario y Decano muestran solo el nombre |
| D-F48 | T12.33 (P59 del backend). Cancelación de Asignatura ya no usa el documento adicional: `CancelacionAsignaturaProcesoComponent` no importa `DocumentoAdicionalComponent` ni `subirDocumentoAdicional`, ni Rechazar ni Enviar respuesta lo ofrecen, y el aviso de que remitir lo retira desapareció; Matrícula y Supletorio siguen con D-F45. En "Enviar la respuesta al estudiante" el campo es "Resolución sin firmar por el estudiante" (obligatorio si no hay una cargada; error "Debes subir la Resolución sin firmar por el estudiante (PDF).") con la nota `data-nota-resolucion-sin-firmar`. `NotificacionResolucionComponent` muestra "Resolución vigente" (`data-resolucion-vigente`: "Sin firmar por el estudiante" o "Firmada por el estudiante", según `firmada`) en lugar de "Firmada por el estudiante: Sí/No"; con la firmada vigente el botón pasa a "Volver a subir la firmada" y el diálogo a "Volver a subir la Resolución firmada", disponible mientras `editable`. Sin cambios en el contrato. Bundle inicial sin cambio: 838,94 kB |
| D-F47 | T12.32 (P58 del backend). En Cancelación de Asignatura, el diálogo "Enviar la respuesta al estudiante" pide antes la plantilla (`GET .../funcionario/notificacion/plantilla`; si falla, error y no abre) y suma `ComunicacionRespuestaComponent` (`shared/others`): casilla "Enviar mensaje al estudiante" marcada, cuadro con la plantilla editable (obligatorio con la casilla, máximo 2000, con contador) y la comunicación programada obligatoria con `ProgramacionResolucionComponent` (fecha y hora con `InputDateComponent` en modo `datetime-local`, mínimo la hora actual de Colombia y la fecha escrita en español debajo; número, año por defecto el actual y decisión propuesta editable; vista previa con el sufijo). Las reglas viven en `core/utils/notificacion-resolucion.ts` (`ahoraEnColombia`, `erroresDeProgramacion`, `errorDeMensajeInicial`, `programacionAPeticion`). El cuerpo de `responder` lleva `enviarMensaje`, `mensaje` (nulo sin la casilla) y `programacion`. `InputDateComponent` de Julián recibe dos entradas opcionales, `tipo` (`date` por defecto) y `minimo`, y lleva `lang="es-CO"`; quien no las pasa se ve igual (D-F31). El detalle del Funcionario en etapa final inserta, dentro de un `@defer (on immediate)` del componente del proceso, `NotificacionResolucionComponent` (`shared/others`, chunk propio): tarjeta "Notificación de la Resolución" con estado, fecha programada, Resolución, decisión, firma, intentos, fechas de envío y de liberación, mensaje inicial y último error; con `editable` del backend ofrece "Reprogramar o editar" y "El estudiante firmó" (sube el PDF firmado), en FALLIDA solo "Reintentar", y en ENVIADA es de solo lectura. Un 204 no pinta nada. El Estudiante y el Decano no lo ven. La retención para el Estudiante la hace el backend (etiqueta "En trámite" y `etapaCodigo` EN_REVISION_DECANO), y las pantallas del Estudiante ya la manejan por `etiqueta` (D-F23). Bundle inicial sin cambio: 838,94 kB |

Otras decisiones tomadas en las tareas, sin número:

- Bandejas: una tarjeta por rol ("Solicitudes remitidas por el Funcionario
  Académico", "Gestión de solicitudes", "Mis trámites radicados"). Filtros de
  tipo de proceso, estado (decisión en el Decano, con las etiquetas presentes)
  y "Número de identificación" (no en el Estudiante), con "Limpiar filtros". El
  Funcionario tiene además "Código estudiantil" (T12.10), en el cliente y con
  coincidencia parcial sobre `codigoEstudiantil`.
  Columnas: Radicado, Estudiante, Documento (`numeroDocumento` del resumen,
  P42 del backend), Código (solo el Funcionario, D-F36), Tipo de proceso,
  Fecha de radicación, Estado y Acciones (ojo); el Estudiante ve Radicado,
  Tipo de proceso, Fecha de radicación, Estado y Acciones. Fecha y Estado se
  ordenan con ⇅. El resumen no trae fechas de remisión ni de decisión, por eso
  se usa la fecha de radicación. La búsqueda cubre radicado, nombre, código y
  documento.
- Detalle (T12.2-C): "← Volver", "Solicitud de {tipo}", "Radicado {número}" y
  la pill de estado por `categoriaEstado(etiqueta)`. Orden de tarjetas: Datos
  generales (solo los campos que llegan del backend; "Número de CC" con
  `numeroDocumento`, P43, solo para Funcionario y Decano), anexos (título por proceso: "Documentación requerida",
  "Soporte adjunto" o "Anexos adjuntos", con contador, y enlaces "Visualizar" y
  "Descargar"), "Observación para el Decano" (Funcionario y Decano), "Decisión
  del Decano" (Funcionario y Decano, D-F40), "Mensaje para el estudiante" (si
  existe, de la última fila `ENVIAR_RESPUESTA`, `RECHAZAR_FUNCIONARIO`,
  `RECHAZAR_COMPROBANTE` o `APROBAR_COMPROBANTE`, D-F40 y D-F44), las tarjetas del proceso con la tarjeta de
  acciones al final y el historial, sin comentarios (D-F40). El prototipo pone motivo y tabla antes de
  los anexos, pero motivo, tabla y acciones viven en el mismo componente de
  proceso; reordenar solo con CSS rompería el orden del foco, así que el DOM se
  deja así (T12.7). Los botones de rechazo van en contorno rojo
  y el resto en navy; textos y visibilidad siguen saliendo de
  `accionesDisponibles`.
- Las tablas de asignaturas de las dos cancelaciones usan el encabezado
  agrupado del formato de la Facultad (`agruparEncabezados` en
  `core/utils/encabezado-agrupado.ts` y `RUTAS_ENCABEZADO_ASIGNATURAS`). En
  Cancelación de Asignatura "Situación al cancelar" va junto a "Situación en la
  matrícula".
- Tabla de asignaturas en línea (T12.8): cada componente de proceso pinta su
  propia tabla (ya no usa `TableGenericComponent`) con el estilo de
  `shared/pages/content/tabla-asignaturas.css`: encabezado gris en mayúscula
  pequeña y celdas con "—" cuando no hay dato. Si `accionesDisponibles` trae
  `REMITIR_DECANO`, la tabla es editable: No. faltas y Nota como texto, y la
  situación en la matrícula y al cancelar como select del mismo catálogo (al
  cancelar es opcional y viaja como `situacionCancelarUuid`, P44; desde T12.28
  todos los datos de la Facultad son opcionales, D-F44). En
  Cancelación de Asignatura se suma la columna "Cumple condiciones" bajo
  "Evaluación del Funcionario". Al pulsar "Remitir al Decano" se validan las
  filas con los mismos mensajes, que se pintan en la celda; si todo está bien
  se abre el diálogo con un solo campo de observación (D-F35). El Decano y el
  Funcionario en la respuesta final siguen D-F35. Bajo 992 px la tabla hace
  scroll dentro de su tarjeta.
- Modales: barra navy con ×, resumen de la solicitud
  (`ResumenSolicitudComponent`: Estudiante, Número de CC si llega, Código
  estudiantil, Tipo de proceso, Número de radicado), notas grises y botones Cancelar y confirmar. Cierran con
  Escape. Al aprobar, el Decano ve `NOTA_APROBAR_DECANO`.
- Formularios del Estudiante: stepper guía (`StepperFormularioComponent`) sobre
  las secciones que ya existían, todo en una sola página; cada paso queda verde
  cuando su sección es válida y el primero pendiente en navy. El primer bloque
  (`DatosEstudianteSesionComponent`) muestra de solo lectura los datos del
  usuario de la sesión, el código estudiantil que trae el `GET .../formulario`
  (D-F36) y la Facultad FIET, con la nota de DARCA. El Estudiante no tiene
  endpoint para su programa ni su semestre. Pie con "Volver" (a
  Mis solicitudes) y "Radicar solicitud".
- Catálogo del Estudiante (T12.7): tres tarjetas con barra navy, descripción,
  requisitos, anexos requeridos e "Iniciar solicitud →" hacia el formulario que
  ya existía. El menú del Estudiante queda en Mi Usuario, Solicitudes y Mis
  solicitudes, y las migas de los formularios apuntan al catálogo. Los
  formatos de Examen Supletorio se descargan como archivos fijos (D-F41). No existe el
  interruptor de apertura; la solicitud en curso la sigue avisando el backend
  al radicar (D-F13).
- Tras radicar, la pantalla reemplaza el formulario por el radicado y un botón
  "Ver solicitud", y además navega al detalle.
- Examen Supletorio:
  - El uuid de "Recibo de pago" y de "Comprobante de pago" se busca por nombre
    en `GET catalogos-academicos/tipos-solicitud/{uuidTipo}/tipos-anexo`, con
    los nombres de `ANEXOS_PAGO_SUPLETORIO`; no hay uuid fijo.
  - Recibo y comprobante: primero se sube el anexo y luego se llama la acción.
    Si la acción falla después de subir el archivo, el reintento no lo sube otra
    vez.
  - El aviso de comprobante rechazado se deduce de la etapa RECHAZADA (para el
    Estudiante, de la etiqueta "Rechazada") más un anexo de tipo "Comprobante
    de pago" en la solicitud.
  - El plazo de 3 días hábiles (`plazoDiasHabiles`) es solo informativo; el
    vencimiento lo decide el backend.
  - No se envían los campos de las maquetas que el contrato no tiene.
- Administración académica:
  - Una página con cuatro pestañas (Asignaturas, Estudiantes, Funcionarios
    académicos, Responsable por proceso) para el Decano y para el Secretario
    General, en rutas separadas por rol. Programas y asignaturas: ver la
    sección siguiente.
  - Los filtros se aplican con un botón "Buscar", no al escribir.
  - El paginador de Julián es de base 1; se convierte a base 0 al llamar al
    backend, y la tabla recibe solo la página actual.
  - El responsable de un proceso se elige entre los primeros 100 funcionarios
    académicos (`paginado` con tamaño 100); el diálogo avisa si hay más.
  - No hay borrado: el contrato no tiene endpoints para eso.

### Programas académicos y asignaturas

Hay cinco programas fijos (PIET, PIS, PIAI, TTM, PIAC desde T12.34; las siglas
salen del catálogo, no del código). El frontend los toma
siempre de `GET catalogos-academicos/programas`
(`CatalogoAcademicoService.getProgramas`); no hay una lista fija en el código.
Las opciones del select se arman con `opcionesDePrograma` y las de asignaturas
con `opcionesDeAsignatura` (`core/utils/programas-academicos.ts`).

- Asignaturas: la tabla muestra código, nombre y programa. Filtros de texto y
  de programa (opción "Todos", que no manda el parámetro) con "Buscar". Al crear
  se piden nombre y programa; el código lo genera el backend (`PIS-294`) y el
  toast lo avisa: "Asignatura creada con el código PIS-294". Al editar solo se
  envía `{nombreAsignatura}`; código y programa se ven de solo lectura.
- Carga Excel de asignaturas con `CargaExcelAcademicaComponent`: encabezados
  `nombreAsignatura, programaAcademico`, los nombres válidos del catálogo y la
  regla de que el código se genera solo.
- Estudiantes, alta: el select de programa va primero. La sección de
  asignaturas es un `<fieldset>` deshabilitado hasta que hay programa y este
  tiene asignaturas (`GET asignaturas/programa/{uuid}`). Cada fila es un select
  "código - nombre" más el grupo; una asignatura elegida en una fila no aparece
  en las demás y la validación también rechaza repetidas. Si el programa no
  tiene asignaturas se avisa que deben crearse primero en la pestaña
  Asignaturas. Cambiar el programa con filas llenas pide confirmación dentro del
  diálogo ("Cambiar programa" o "Conservar el anterior") y, si se confirma,
  limpia las filas. Se envía `uuidProgramaAcademico` y `asignaturas` como
  `{codigoAsignatura, grupo}`.
- Estudiantes, alta: la facultad se muestra fija, FIET, y no se envía.
- Estudiantes, edición: nombres, apellidos, tipo y número de documento,
  teléfono, correo, código estudiantil y semestre; usuario, programa y
  facultad de solo lectura (D-F24, D-F26). El error de código 2 (unicidad) se
  muestra dentro del diálogo.
- Funcionarios académicos: el campo es `cargo` (antes `dependencia`) en
  tabla, filtro (`?cargo=`), alta, edición y Excel. La edición cambia los datos
  personales y el cargo (D-F24, D-F25).
- Estudiantes, detalle: "Agregar asignatura" elige entre las asignaturas del
  programa del estudiante y el grupo. Si ya la tiene activa, lo dice el backend.
- Excel de estudiantes: 13 encabezados (sin `nombreAsignatura` ni `facultad`), los nombres
  válidos de programa y la regla de que cada `codigoAsignatura` existe y es del
  programa.
- Las pantallas que muestran el programa de un estudiante (tabla de
  estudiantes, detalle de una solicitud) usan `programaAcademico`, que sigue
  siendo el nombre.
- Los catálogos que abren un formulario (programas para el alta y el Excel) se
  piden al abrir si todavía no están; si fallan o vienen vacíos, error por
  `ErrorHandlerService` y el diálogo no abre.

`CargaExcelAcademicaComponent` recibe además, opcionales, `reglas` (lista de
textos), `tituloValores` y `valores` (lista de valores permitidos). Los
funcionarios académicos lo siguen usando solo con `regla`.

### Pruebas

Karma con ChromeHeadless:

```
npx ng test --watch=false --browsers=ChromeHeadless
npx ng test --watch=false --browsers=ChromeHeadless --include=src/app/shared/pages/content/<carpeta>
```

- Un `--include` por carpeta; un comodín como `admin-*` no toma todas.
- Los specs que montan `InputAnexoUploadComponent` necesitan
  `provideHttpClient()` y `provideHttpClientTesting()`, porque su `p-fileUpload`
  inyecta `HttpClient`.
- Los que abren diálogos de PrimeNG necesitan `provideNoopAnimations()`.
- Para probar cuerpos, partes multipart y parámetros se usa el servicio real
  con `HttpTestingController`.
- Para probar una navegación de verdad se usa `RouterTestingHarness` con
  rutas reales, no un espía de `Router`.

## Limitaciones conocidas

- 58 specs generados de Julián fallan con
  `NG0201: No provider found for _HttpClient` (los `should create` de sus
  componentes y los dos de `App`). Son siempre los mismos; se compara la lista
  en cada tarea y no se modifican esos archivos.
- El componente del Consejo de Julián que usa `decano/solicitudes-consejo`
  (`usuario-fiet-solicitudes-privadas-content-component`) lee
  `usuario.roles[0]` sin esperar a que el usuario termine de cargar.
  `header-main-component` y `enviar-solicitud-usuario-fiet-component` también
  leen `roles[0]`.
- `RoleGuard` de Julián redirige a `/login` cuando el rol no coincide o falla
  la consulta del usuario, sin mensaje y sin limpiar la sesión. Lo usan todas
  las rutas protegidas; no se cambió.
- Por debajo de 992 px el encabezado de Julián (`navbar-expand-lg` con
  `navbar-collapse` y sin botón para desplegarlo) oculta el avatar y con él
  "Cerrar sesión".
- El menú del chip de usuario tiene "Mi usuario" (inicio del rol actual con
  `rutaDeInicio`) y "Cerrar sesión"; ya no lista los roles.
- Mi usuario muestra solo lo que entrega la API: datos del usuario y roles; el
  Funcionario Académico suma Cargo y procesos asignados
  (`GET funcionarios-academicos/{uuid}`). El Estudiante no tiene endpoint para
  leer su programa ni su semestre, y ningún DTO trae la fecha de creación
  ("Te uniste el"), así que no se muestran.
- D-F13: no hay aviso previo de que el estudiante ya tiene una solicitud en
  curso del mismo tipo; se entera por el error del backend al radicar.
- El reintento de la acción de recibo o de comprobante no consulta si el
  archivo subido en el intento anterior sigue en el servidor: confía en la
  bandera local `anexoPagoSubido` y en la lista de anexos que trajo el
  detalle.
- `authInterceptor` y partes de `AuthService` acceden a `localStorage` sin
  comprobar `window` (ver "Autenticación").

## Qué no se hace

- No se agregan comentarios de ningún tipo (JSDoc, `//`, `/* */`) ni
  emojis en archivos nuevos o modificados. El código de Julián sí los
  trae — es su estilo original, no se imita en el código nuevo.
- No se crea un componente de tabla, diálogo, input o botón propio si el
  que ya existe en `shared/` sirve para el caso.
- No se usan NgModules ni `declarations` — todo standalone, como ya está.
- No se corrige la ortografía de rutas o carpetas heredadas
  (`enviroments`, `Sesión/DTPResponse`, las carpetas con espacio) por
  cuenta propia — tocarlas rompe imports existentes en todo el proyecto de
  Julián.
