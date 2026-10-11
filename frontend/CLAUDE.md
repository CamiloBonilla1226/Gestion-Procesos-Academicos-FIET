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
`decano/solicitudes/:uuid`), porque una ruta con `:uuid` no se puede
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
| Funcionario Académico | `funcionario-academico`, `funcionario-academico/solicitudes`, `funcionario-academico/solicitudes/:uuid` | Mi Usuario, Solicitudes |
| Decano | `decano`, `decano/solicitudes`, `decano/solicitudes/:uuid`, `decano/solicitudes-consejo`, `decano/administracion-academica` | Mi Usuario, Solicitudes académicas, Solicitudes del Consejo, Administración académica |
| Secretario General | `sec-general/administracion-academica` (más las de Julián) | Una línea agregada al menú de Julián: Administración académica |

El login (`shared/login-form-component`) redirige por rol en este orden:
Secretario General, Funcionario (Julián), Secretaria Decanatura FIET, Decano,
Funcionario Académico, Estudiante y luego los usuarios FIET. El Decano va
antes que el Funcionario Académico y que el Estudiante.

### Capa API

Un servicio por recurso en `core/services`: `asignatura-service`,
`estudiante-service`, `funcionario-academico-service`,
`catalogo-academico-service`, `solicitud-academica-service`,
`cancelacion-matricula-service`, `cancelacion-asignatura-service` y
`examen-supletorio-service`. Cubren los 63 endpoints del contrato. Los
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
- Los textos y avisos dependen de `etapaCodigo`, no de `etiqueta` (la
  etiqueta es solo para mostrar). La insignia usa `claseInsigniaEtapa`:
  verde en APROBADA, rojo en RECHAZADA, amarillo en PENDIENTE_PAGO y
  EN_VERIFICACION_PAGO (D-F9), gris en el resto.
- Tras cada acción el componente de proceso llama a `recargar()`, que vuelve a
  pedir el detalle y el historial; así cambian la etapa, los anexos y los
  botones.
- Cancelación de Asignatura: el Estudiante ve solo código y nombre antes de
  la etapa final, y en APROBADA o RECHAZADA también la decisión del Decano y
  su observación. Las columnas sin ningún dato se ocultan (también en la
  vista del Estudiante de Matrícula).

### Diálogos

Todas las confirmaciones y formularios usan `GenericDialogFormComponent`. La
extensión le agregó el `@Input() textoBoton` (por defecto `Guardar`, con el
ancho de 70px de siempre, así los diálogos de Julián no cambian) (D-F12).
Nuestros diálogos pasan un texto por acción: "Rechazar solicitud",
"Remitir al Decano", "Aprobar solicitud", "Registrar decisiones" (decisión
por asignatura), "Enviar respuesta", "Enviar recibo", "Subir comprobante",
"Aprobar comprobante", "Rechazar comprobante", "Crear", "Guardar cambios",
"Cambiar responsable" y "Cargar archivo". Cada envío lleva su propia bandera
`enviando` que bloquea el doble envío (D-F7).

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

### Decisiones del frontend

Los números D-F que se conocen en el repositorio:

| Decisión | Contenido |
| --- | --- |
| D-F1 | Modelos como `interface` con el nombre exacto del DTO del backend |
| D-F5 | El tamaño de los archivos se valida en nuestros componentes, porque el componente de Julián no lo hace |
| D-F7 | Doble envío bloqueado con una bandera propia en cada componente |
| D-F8 | 401 fuera del login: cerrar sesión, avisar una vez y redirigir a `/login` |
| D-F9 | PENDIENTE_PAGO y EN_VERIFICACION_PAGO con insignia amarilla |
| D-F12 | Texto del botón de los diálogos por acción, con `Guardar` por defecto |
| D-F13 | Sin aviso previo de solicitud en curso; se muestra el error del backend |
| D-F14 | Las pruebas del frontend no llaman al backend real: specs con `HttpTestingController` o espías |

Otras decisiones tomadas en las tareas (el texto numerado de D-F2 a D-F4,
D-F6, D-F10 y D-F11 no está registrado en el repositorio):

- La columna Estado de las bandejas es texto plano, sin insignia.
- Cancelación de Matrícula: advertencia de primer periodo con casilla
  obligatoria. Cancelación de Asignatura no la tiene.
- Tras radicar, la pantalla reemplaza el formulario por el radicado y un botón
  "Ver solicitud", y además navega al detalle.
- Examen Supletorio:
  - El uuid de "Recibo de pago" y de "Comprobante de pago" se busca por nombre
    en `GET catalogos-academicos/tipos-solicitud/{uuidTipo}/tipos-anexo`, con
    los nombres de `ANEXOS_PAGO_SUPLETORIO`; no hay uuid fijo.
  - Recibo y comprobante: primero se sube el anexo y luego se llama la acción.
    Si la acción falla después de subir el archivo, el reintento no lo sube otra
    vez.
  - El aviso de comprobante rechazado se deduce de la etapa RECHAZADA más un
    anexo de tipo "Comprobante de pago" en la solicitud.
  - El plazo de 3 días hábiles (`plazoDiasHabiles`) es solo informativo; el
    vencimiento lo decide el backend.
  - No se envían los campos de las maquetas que el contrato no tiene.
- Administración académica:
  - Una página con cuatro pestañas (Asignaturas, Estudiantes, Funcionarios
    académicos, Responsable por proceso) para el Decano y para el Secretario
    General, en rutas separadas por rol.
  - Los filtros se aplican con un botón "Buscar", no al escribir.
  - El paginador de Julián es de base 1; se convierte a base 0 al llamar al
    backend, y la tabla recibe solo la página actual.
  - El responsable de un proceso se elige entre los primeros 100 funcionarios
    académicos (`paginado` con tamaño 100); el diálogo avisa si hay más.
  - No hay borrado: el contrato no tiene endpoints para eso.

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
  la consulta del usuario, pero no limpia la sesión.
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
