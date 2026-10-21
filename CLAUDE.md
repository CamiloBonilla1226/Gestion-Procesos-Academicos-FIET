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
- `docs/database/` — script de extensión del esquema, diccionario de datos
  de las tablas nuevas (no tocan las tablas de Julián), reglas por proceso
  (`etapas-por-proceso.md`) y scripts de siembra.
- `docs/api/contrato-api-procesos-academicos.md` — contrato de los 76
  endpoints académicos para quien construya el frontend.

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

`docs/database/diccionario-datos-extension.md` tiene las 21 tablas nuevas
campo por campo (programas académicos, etapas de la solicitud, anexos
obligatorios por tipo, situación académica, Resolución física escaneada), y
`docs/database/script_bd_extension.sql` su DDL de referencia: no se ejecuta,
las tablas las crea Hibernate desde las entidades (`ddl-auto=update`), igual
que en Julián. Las 21 tablas coinciden con la base real (`SHOW TABLES` y
`DESCRIBE`). Esa documentación es la fuente de verdad del modelo de datos — antes de modelar una entidad o
endpoint nuevo, revisa ahí primero. El diccionario anota en cada columna la
regla que la afecta (rangos, valores permitidos, cuándo es obligatoria), pero
las etapas, transiciones, anexos por proceso, plazos y decisiones tomadas
están en `docs/database/etapas-por-proceso.md`; las reglas que no se
revierten están en la sección siguiente.

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
  Cancelación de Asignatura, y se sube cuando la solicitud pasó por el Decano,
  en la respuesta final (aprobada o rechazada). Vive en
  `RESOLUCION_ACADEMICA` (1 a 0..1 con `SOLICITUD_ACADEMICA`), separada de
  `ANEXO_ACADEMICO`.
- En Cancelación de Matrícula y Cancelación de Asignatura, el rechazo directo
  del Funcionario (sin remitir al Decano) no lleva Resolución: el backend no la
  pide y el correo no la adjunta. La respuesta final después del Decano sí la
  exige, como antes (P56, desde T12.31).
- Cancelación de Asignatura que pasó por el Decano (P58, desde T12.32): al
  enviar la respuesta final no salen los correos inmediatos al estudiante con
  la Resolución ni a DARCA. Sale, si el Funcionario lo deja marcado, un mensaje
  inmediato sin adjuntos ni resultado, y la comunicación de la Resolución es
  obligatoria y programada (fecha y hora futuras en hora de Colombia, número,
  año y decisión; sufijo `/178` y firma desde propiedades). Una tarea
  programada la envía en la fecha: al estudiante con la Resolución y a DARCA
  con tres PDF (respuesta, Resolución y constancia de la comunicación); si el
  estudiante firmó en la Decanatura antes de la fecha, no se le envía nada y
  DARCA recibe dos PDF con la Resolución firmada. Correos independientes, hasta
  3 intentos y luego FALLIDA con reintento manual. Matrícula, Supletorio y el
  rechazo directo siguen como antes.
- Visibilidad diferida (P58): en ese caso, hasta que la tarea toma la
  comunicación por primera vez, el Estudiante ve la solicitud "En trámite" y el
  backend no le devuelve la respuesta, el resultado por asignatura, la
  Resolución ni su descarga, el documento adicional ni las filas de decisión y
  respuesta del historial. Después no se vuelve a ocultar. Funcionario y Decano
  ven todo siempre.
- Cancelación de Asignatura tiene aprobación parcial: el Decano aprueba o
  rechaza cada asignatura por separado y solo se cancelan las aprobadas
  (decisión P14 de `docs/database/etapas-por-proceso.md`).
- Examen Supletorio no produce Resolución y tiene un plazo de tres días
  hábiles. Tras la aprobación del Decano, el Funcionario sube el recibo de
  pago y el Estudiante el comprobante; el Funcionario lo aprueba o lo rechaza.
- Cancelación de Matrícula tiene un solo anexo obligatorio: un PDF con todos
  los paz y salvos, de hasta 10 MB. Los seis anexos anteriores quedan retirados
  en el catálogo (`vigente = 0`) para conservar las solicitudes ya radicadas
  (P55, desde T12.31). Cancelación de Asignatura tiene cuatro condiciones
  académicas.
- Secretario General o Decano crean los usuarios Estudiante y Funcionario
  Académico, por formulario y por carga masiva desde Excel. Para crear un
  Estudiante se registra también su información de materias.
- Hay cinco programas académicos fijos (IET, ISI, IAI, TTE, IAC). Cada
  asignatura y cada estudiante pertenecen a uno, obligatorio y que no cambia.
  El código de la asignatura lo genera el backend (`ISI-001`) y no se edita
  ni se reutiliza. Un estudiante solo matricula asignaturas existentes de su
  programa; ningún flujo de estudiantes crea asignaturas (P28 a P35).

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
