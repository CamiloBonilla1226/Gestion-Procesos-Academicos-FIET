---
name: extender-frontend-fiet
description: Usar al agregar o modificar cualquier pantalla, componente o servicio del frontend de Gestión de Procesos Académicos FIET (nueva vista de un proceso académico, nuevo formulario, nueva tabla, nueva integración con un endpoint del backend).
---

# Extender el frontend de Gestión de Procesos Académicos FIET

Antes de escribir una sola línea, lee `frontend/CLAUDE.md` completo. Esta
skill da el orden concreto de pasos para agregar una pantalla o
funcionalidad nueva siguiendo el mismo patrón de composición y los mismos
componentes compartidos que ya usa Julián Camacho.

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

## Paso 2 — Servicio

`core/services/<recurso>-service.ts`, `@Injectable({ providedIn: 'root' })`,
URL armada sobre `environment.apiUrl` (importado desde
`../../../enviroments/environment` — esa carpeta se llama así, con ese
typo, no "environments"), un método por endpoint del controlador del
backend, devolviendo `Observable<...>` con los tipos del paso 1. Revisa
`roles-service.ts` como referencia de estilo y de nombres de método
(`getX`, `getXPaginados`, `actualizarX`, etc.).

## Paso 3 — Revisar qué componente compartido reusar

Antes de crear cualquier componente visual nuevo, revisa si ya existe en
`shared/` lo que necesitas:

- tabla con paginado -> `TableGenericComponent` + `Paginator`
- tarjeta con botones de acción -> `CardMainComponent`
- formulario en diálogo -> `GenericDialogFormComponent`
- ver información de solo lectura en diálogo -> `GenericDialogInfoComponent`
- campo de texto, select o textarea -> `InputTextComponent`,
  `InputSelectComponent`, `InputTextTareaComponent`
- búsqueda -> `BarraBusquedaComponent`
- descarga/visualización de anexos -> `AnexosViewComponent`

Solo se crea un componente nuevo en `shared/` cuando ninguno de los
existentes cubre el caso, y en ese caso va en la subcarpeta que le
corresponda por tipo (no suelto en `shared/`).

## Paso 4 — Componente de contenido (la lógica real)

Va en `shared/pages/content/<nombre>-content-component/`. Acá se inyectan
los servicios del paso 2, se maneja el estado (listas, paginación,
filtros, visibilidad de diálogos) y se arma la plantilla con los
componentes del paso 3. Sigue el estilo de
`fun-solicitudes-content-component.ts` como referencia de organización
(propiedades de estado arriba, métodos de carga de datos, métodos de
manejo de diálogos, métodos de acciones).

## Paso 5 — Componente wrapper de rol

Va en `shared/pages/<nombre>-component/`. Es delgado: solo referencia al
componente de contenido del paso 4 como propiedad (`content = ...`), igual
que `fun-solicitudes-component.ts`.

## Paso 6 — Página de la ruta

Va en `core/<rol>/pages/<nombre>/`. Referencia como propiedades el sidebar
del rol correspondiente, el header, el breadcrumb y el wrapper del paso 5,
y se los pasa a `app-page-component` en el HTML (`[sidebarComponent]`,
`[headerComponent]`, `[breadcrumbComponent]`, `[mainComponent]`), igual que
`funcionario-solicitudes.ts`.

## Paso 7 — Ruta

Agrega la ruta en `app.routes.ts`, con `canActivate: [AuthGuard, RoleGuard]`
(los dos guards juntos, es el patrón real de todas las rutas protegidas
existentes, no solo `RoleGuard`) y `data: { roles: [...] }` listando los
roles que pueden entrar como strings exactos, a menos que la vista sea
pública. Si la vista es de un proceso académico nuevo y el rol
correspondiente ("Estudiante", el del funcionario académico, etc.) todavía
no existe como fila en la tabla `roles` del backend, eso se resuelve primero
en el backend — el string de `data.roles` tiene que coincidir carácter por
carácter con el `nombre` real de esa fila.

## Paso 8 — Feedback y errores

Mensajes de éxito/error puntuales con `ToastService`. Errores de petición
HTTP con `ErrorHandlerService`, nunca mostrando el error crudo del backend
directo al usuario.

## Paso 9 — SSR

Si el componente nuevo toca `localStorage`, `window` o `document` fuera de
un guard o interceptor ya existente, protégelo con
`typeof window !== 'undefined'` antes de usarlo, para no romper el render
del servidor (SSR con Express).

## Paso 10 — Verificar

```
cd frontend
npm start
```

Navega a la ruta nueva con cada rol que deba tener acceso y confirma que
el `RoleGuard` bloquea a los que no deberían entrar, antes de dar la tarea
por terminada.
