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
`docs/database/script_bd_extension.sql` su DDL de referencia: no se ejecuta,
las tablas las crea Hibernate desde las entidades (`ddl-auto=update`), igual
que en Julián. Esa documentación es la fuente de verdad del modelo de datos — antes de modelar una entidad o
endpoint nuevo, revisa ahí primero. No documenta plazos ni reglas de
negocio en prosa; esas reglas están en la sección siguiente.

## Reglas de negocio confirmadas (no revertir)

- Actores: Estudiante, Funcionario Académico (título formal: Técnico
  Administrativo de Procesos Académicos) y Decano. El Decano usa el rol
  `Decano` que ya existe en el sistema de Julián; no se crea otro. Como en
  Julián, el Decano es un `Usuario` con ese rol y el tipo de usuario
  `Maxima autoridad FIET - Decano`, sin fila en `FUNCIONARIO_ACADEMICO`.
- El Funcionario Académico es un `Usuario` con el rol nuevo `Funcionario
  Académico` y el tipo de usuario `Empleado FIET - Funcionario` que ya existe
  en Julián, más su fila en `FUNCIONARIO_ACADEMICO`. No se reutiliza el rol
  `Funcionario` de Julián ni se crea un tipo de usuario propio.
- Terminología: siempre semiautomatización o semi-automatizar, nunca
  automatización.
- El original legal de la Resolución es físico y firmado a mano. No hay firma
  digital ni documento nativo digital. Se distribuyen tres copias físicas.
- Una copia escaneada de la Resolución firmada la sube el Funcionario Académico
  y la descarga el Estudiante. Aplica solo a Cancelación de Matrícula y
  Cancelación de Asignatura, y se sube en el punto donde termina el proceso:
  rechazo en el Funcionario o decisión final después del Decano. Vive en
  `RESOLUCION_ACADEMICA` (1 a 0..1 con `SOLICITUD_ACADEMICA`), separada de
  `ANEXO_ACADEMICO`.
- Un rechazo en la etapa del Funcionario siempre produce una Resolución formal
  firmada físicamente por el Decano.
- Examen Supletorio no produce Resolución y tiene un plazo de tres días
  hábiles.
- Cancelación de Matrícula tiene seis anexos obligatorios. Cancelación de
  Asignatura tiene cuatro condiciones académicas.
- Secretario General o Decano crean los usuarios Estudiante y Funcionario
  Académico, por formulario y por carga masiva desde Excel. Para crear un
  Estudiante se registra también su información de materias.

## Cambios sobre el código de Julián

Del código y de los datos de Julián solo se agregan líneas nuevas
(constantes, métodos `@Bean`, reglas de seguridad, excepciones, mensajes de
error, filas de `data.sql`). No se editan ni se borran las existentes, salvo
que la tarea lo pida de forma explícita. Las tablas de Julián no cambian de
estructura.

## Registro de cambios (`change.md`)

Toda implementación o cambio que se haga en el repositorio se registra en
`change.md`, en la raíz, dentro de la misma tarea y antes de darla por
terminada. Si el archivo no existe, se crea. Cada entrada nueva va al
principio del archivo (la más reciente primero) con este formato:

```
## AAAA-MM-DD - Título corto
- Qué se hizo: resumen en una o dos líneas
- Archivos: `ruta` (creado | modificado | eliminado)
- Notas: decisiones tomadas o pendientes, si los hay
```

Se registran cambios de código, de documentación y de datos iniciales
(`data.sql`). No se registran lecturas ni consultas que no modifiquen nada.

## Git

No se hacen commits ni push, bajo ninguna forma (`git commit`, `git commit
--amend`, `git push`, `git push --force`). Esos comandos los ejecuta solo el
usuario. Sí se pueden consultar `git status`, `git diff` y `git log`. Al
terminar una tarea, los cambios quedan en el árbol de trabajo sin confirmar.
