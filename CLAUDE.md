# Gestión de Procesos Académicos FIET

Trabajo de grado — Universidad del Cauca, FIET. Extiende el prototipo de
Julián David Camacho Erazo ("Gestión de Solicitudes Consejo de Facultad")
con tres procesos académicos nuevos: Cancelación de Matrícula, Cancelación
de Asignatura y Expedición de Exámenes Supletorios.

Este repositorio tiene dos aplicaciones independientes que comparten la
misma base de datos:

- `backend/` — Spring Boot 3 (Java 17), arquitectura hexagonal. Ver
  `backend/CLAUDE.md`.
- `frontend/` — Angular 20 + PrimeNG. Ver `frontend/CLAUDE.md`.
- `docs/database/` — script de extensión del esquema y diccionario de datos
  de las tablas nuevas (no tocan las tablas de Julián).

## Regla de estilo, válida para todo el repositorio

El código que se agrega aquí (java, ts, html, sql) se escribe sin
comentarios y sin emojis, en ningún caso. Nada de Javadoc, nada de `//` o
`/* */` explicativos, nada de JSDoc en los `.ts`. El código base de Julián
sí trae comentarios y Javadoc en casi todo — eso es parte de su estilo
original y no se toca ni se imita en el código nuevo. El código nuevo debe
leerse como si lo hubiera escrito una persona trabajando rápido en su
propio proyecto: nombres de variables y métodos claros, funciones cortas,
sin explicaciones de más. Si algo necesita explicarse, se explica en el
mensaje del commit o en la documentación de `docs/`, no en el código.

Queda fuera de esta regla `docs/database/script_bd_extension.sql`: es documentación del esquema en formato ejecutable, no código de aplicación, y sus comentarios se conservan.

Antes de tocar cualquiera de las dos aplicaciones, lee el `CLAUDE.md` de esa
carpeta — ahí está la arquitectura real, extraída del código de Julián, no
un resumen genérico.

## Cómo correr el proyecto en local

Ver `README.md` en la raíz (Opción A con Docker, Opción B sin Docker).

## Dónde está la info de los tres procesos

`docs/database/diccionario-datos-extension.md` tiene las 18 tablas nuevas
campo por campo (etapas de la solicitud, anexos obligatorios por tipo,
situación académica, Resolución física escaneada), y
`docs/database/script_bd_extension.sql` su DDL. Esa documentación es la
fuente de verdad del modelo de datos — antes de modelar una entidad o
endpoint nuevo, revisa ahí primero. No documenta plazos ni reglas de
negocio en prosa; solo está lo que se deduce de las tablas y columnas.
