# Frontend — Gestión de Procesos Académicos FIET

Angular 20 (standalone components, sin NgModules), PrimeNG, PrimeFlex,
Bootstrap 5, SweetAlert2, SSR con Express. Hereda la estructura de Julián
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
cuyo HTML lo renderiza con `<app-content-component [component]="content">`
(o el nombre que tenga ese wrapper genérico, revisa `ContentComponent` en
`shared/pages/content/`). El componente de contenido es donde está la
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
