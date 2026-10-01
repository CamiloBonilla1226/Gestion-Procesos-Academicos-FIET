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
src/app/
├── core/
│   ├── auth/              login, guards (auth-guard, role-guard), interceptor JWT
│   ├── models/<Recurso>/DTORequest|DTOResponse/   interfaces TS, un archivo por DTO
│   ├── services/           *-service.ts, uno por recurso, inyectados en 'root'
│   ├── constantes/          constantes compartidas (estados de solicitud, etc.)
│   └── <rol>/              una carpeta por rol de usuario (funcionario, secretario-general,
│                            secretariaFiet, usuario fiet, usuario publico...)
│       ├── components/      piezas de ese rol (sidebar, etc.)
│       └── pages/            una página por ruta de ese rol
├── layouts/                 layout general de la aplicación
└── shared/
    ├── pages/                page-component (compone sidebar+header+breadcrumb+main+footer)
    ├── buttons/, inputs/, search/, paginator/, others/
    ├── generic-dialog-form-component/, generic-dialog-info-component/
    ├── table-generic-component/, card-main-component/
    └── headers/, footers/, breadcrumb/
```

## El patrón de composición de páginas

Una ruta no arma su HTML a mano: una página (`core/<rol>/pages/...`) solo
declara qué componentes de `shared/` va a usar, como propiedades de clase,
y se las pasa a `app-page-component` como `[sidebarComponent]`,
`[headerComponent]`, `[breadcrumbComponent]`, `[mainComponent]`. El
"contenido principal" casi siempre tiene un nivel intermedio: un wrapper
de rol (p.ej. `FunSolicitudesComponent`) que a su vez delega en un
componente de contenido real en `shared/pages/content/` (p.ej.
`FunSolicitudesContentComponent`), que es donde está la lógica — llamadas
a servicios, estado, manejo de diálogos. Un módulo nuevo de un proceso
académico sigue ese mismo esquema de tres niveles, no mete la lógica
directo en la página de `core/<rol>/pages`.

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
armada sobre `environment.apiUrl` y un método por endpoint, devolviendo
siempre `Observable<...>` (nunca se suscribe dentro del propio servicio).
Los modelos van en `core/models/<Recurso>/DTORequest/` y
`core/models/<Recurso>/DTOResponse/`, una interfaz por archivo, con el
mismo nombre que su DTO equivalente en el backend
(`RolDTOPeticion` ↔ `RolDTOPeticion`, `RolDTORespuesta` ↔
`RolDTORespuesta`). Un recurso nuevo del backend (por ejemplo
`SolicitudAcademica`) sigue exactamente ese mismo mapeo 1 a 1.

## Autenticación

El token JWT se guarda en `localStorage` bajo la clave `authToken`, y el
usuario actual bajo `currentUser`. `authInterceptor`
(`core/auth/interceptor`) lo agrega como header `Authorization: Bearer` a
toda petición saliente. `AuthGuard` valida sesión, `RoleGuard` valida que
el usuario tenga alguno de los roles declarados en `route.data['roles']`
de `app.routes.ts`. Toda vista nueva que dependa de un rol concreto se
protege con `RoleGuard` y declara sus roles permitidos en la ruta, no con
un `if` dentro del componente.

Como la app usa SSR (Express + hidratación), cualquier acceso a
`localStorage` o `window` fuera de un guard ya preparado para eso debe
comprobar `typeof window !== 'undefined'` antes, igual que hace
`RoleGuard` — si no, rompe en el render del servidor.

## Feedback al usuario

Los mensajes de éxito/error puntuales van por `ToastService`. Los errores
de petición HTTP se centralizan en `ErrorHandlerService`, no se hace
`catchError` suelto mostrando el mensaje crudo del backend en cada
componente.

## Qué no se hace

- No se agregan comentarios de ningún tipo (JSDoc, `//`, `/* */`) ni
  emojis en archivos nuevos o modificados. El código de Julián sí los
  trae — es su estilo original, no se imita en el código nuevo.
- No se crea un componente de tabla, diálogo, input o botón propio si el
  que ya existe en `shared/` sirve para el caso.
- No se llama a `HttpClient` directo desde un componente — siempre a
  través de un servicio de `core/services`.
- No se usan NgModules ni `declarations` — todo standalone, como ya está.
