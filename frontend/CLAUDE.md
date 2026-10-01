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
    │   ├── models/<Recurso>/DTORequest|DTOResponse/   interfaces TS, un archivo por DTO
    │   ├── services/           *-service.ts, uno por recurso, inyectados en 'root'
    │   ├── constantes/          constantes compartidas (estados de solicitud, etc.)
    │   └── <rol>/              una carpeta por rol de usuario (funcionario, secretario-general,
    │                            secretariaFiet, "usuario fiet", "usuario publico"...)
    │       ├── components/      piezas de ese rol (sidebar, etc.)
    │       └── pages/            una página por ruta de ese rol
    ├── layouts/                 layout general de la aplicación
    └── shared/
        ├── pages/                page-component (compone sidebar+header+breadcrumb+main+footer)
        ├── buttons/, inputs/, search/, paginator/, others/, sidebars/
        ├── generic-dialog-form-component/, generic-dialog-info-component/
        ├── table-generic-component/, card-main-component/
        └── headers/, footers/, breadcrumb/
```

Dos carpetas de rol existentes llevan espacio en el nombre:
`core/usuario fiet/` y `core/usuario publico/`. No se renombran (romperían
todos los imports existentes), pero un IDE o una terminal pueden tratarlas
distinto a una ruta sin espacio — tenlo presente al crear archivos ahí
dentro o al referenciarlas desde una tarea de build.

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
- `InputTextComponent`, `InputSelectComponent`, `InputTextTareaComponent`
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
en la tabla `roles` de la base). No existen todavía los roles `'Estudiante'`
ni un rol para el funcionario/decano de los procesos académicos nuevos —
hay que confirmar con el backend qué nombre exacto de rol se usa ahí antes
de proteger una ruta nueva con `RoleGuard`, para que el string coincida
carácter por carácter.

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
`mergeApplicationConfig`, y `app.routes.server.ts` declara una sola ruta
`'**'` con `RenderMode.Prerender`. No hay rutas de servidor por página.

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
  `SimpleButtonComponent`) y `src/styles.css`, que solo importa
  `primeicons` y resetea `html, body`.

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
de llamar a `onSubmit()`. Lo que el código no resuelve y habría que decidir para los procesos nuevos son
las validaciones que no sean "obligatorio" (rangos de fechas, plazos,
formato de anexos), porque ningún formulario existente las implementa.

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
