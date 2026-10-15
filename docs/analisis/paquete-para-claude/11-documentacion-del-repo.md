

===== ARCHIVO: CLAUDE.md =====

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
- `docs/api/contrato-api-procesos-academicos.md` — contrato de los 66
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

`docs/database/diccionario-datos-extension.md` tiene las 19 tablas nuevas
campo por campo (programas académicos, etapas de la solicitud, anexos
obligatorios por tipo, situación académica, Resolución física escaneada), y
`docs/database/script_bd_extension.sql` su DDL de referencia: no se ejecuta,
las tablas las crea Hibernate desde las entidades (`ddl-auto=update`), igual
que en Julián. Las 19 tablas coinciden con la base real (`SHOW TABLES` y
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
  Cancelación de Asignatura, y se sube en el punto donde termina el proceso:
  rechazo en el Funcionario o decisión final después del Decano. Vive en
  `RESOLUCION_ACADEMICA` (1 a 0..1 con `SOLICITUD_ACADEMICA`), separada de
  `ANEXO_ACADEMICO`.
- En Cancelación de Matrícula y Cancelación de Asignatura, un rechazo en la
  etapa del Funcionario siempre produce una Resolución formal firmada
  físicamente por el Decano: el backend no deja rechazar sin el escaneo.
- Cancelación de Asignatura tiene aprobación parcial: el Decano aprueba o
  rechaza cada asignatura por separado y solo se cancelan las aprobadas
  (decisión P14 de `docs/database/etapas-por-proceso.md`).
- Examen Supletorio no produce Resolución y tiene un plazo de tres días
  hábiles. Tras la aprobación del Decano, el Funcionario sube el recibo de
  pago y el Estudiante el comprobante; el Funcionario lo aprueba o lo rechaza.
- Cancelación de Matrícula tiene seis anexos obligatorios. Cancelación de
  Asignatura tiene cuatro condiciones académicas.
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



===== ARCHIVO: README.md =====

# Gestión de Procesos Académicos FIET

Trabajo de grado — Universidad del Cauca, Facultad de Ingeniería Electrónica y
Telecomunicaciones (FIET).

## Objetivo

Semi-automatizar tres procesos académicos que actualmente gestiona
manualmente la Decanatura FIET:

1. **Cancelación de Matrícula**
2. **Cancelación de Asignatura**
3. **Expedición de Exámenes Supletorios**

Los tres procesos comparten el mismo flujo general (Estudiante → Funcionario
Académico → [Decano, según el proceso] → respuesta al Estudiante), por lo que
se implementan como una extensión de un sistema de solicitudes ya existente,
en lugar de construir una aplicación aislada por proceso.

## Origen del código base

Este repositorio **parte de la base construida por Julián David Camacho
Erazo** para su propio trabajo de grado, "Prototipo para la Gestión de
Solicitudes dirigidas al Consejo de Facultad FIET":

- Backend original: https://github.com/jdacamacho/back-fiet-sc
- Frontend original: https://github.com/jdacamacho/front-fiet-sc

Esa base ya resuelve la gestión genérica de usuarios, roles, tipos de
solicitud e historial. El trabajo de grado de este repositorio consiste en
**extender** esa base con los tres procesos académicos listados arriba,
manteniendo sin modificar las tablas y funcionalidades ya existentes de
Julián (ver `docs/database/`).

## Estructura del repositorio

```
.
├── backend/    Spring Boot 3 + MySQL (Java 17) — API REST
│   └── pruebas/   Scripts de humo en PowerShell (todo.ps1 los corre todos)
├── frontend/   Angular 20 + PrimeNG — SPA
└── docs/
    ├── api/
    │   └── contrato-api-procesos-academicos.md   Contrato de los 66 endpoints académicos
    ├── database/
    │   ├── script_bd_extension.sql          DDL de referencia de las 19 tablas nuevas (no se ejecuta)
    │   ├── diccionario-datos-extension.md   Diccionario de datos de la extensión
    │   ├── etapas-por-proceso.md            Etapas, transiciones, anexos, plazos y decisiones
    │   ├── seed-roles-extension.sql         Roles y tipo de usuario de la extensión
    │   ├── seed-procesos-academicos.sql     Programas, tipos de solicitud, etapas, etiquetas, anexos y situaciones
    │   └── migracion_programas_asignaturas.sql  Pone al día una base con datos anteriores a los programas
    └── informe-discrepancias-documentacion.md   Correcciones hechas a la documentación
```

> `docs/database/script_bd_extension.sql` es el DDL de referencia de las
> tablas nuevas y no se ejecuta: igual que en la base de Julián, las tablas
> las crea Hibernate al arrancar el backend (`ddl-auto=update`) a partir de
> las entidades JPA, que deben calcar ese DDL. Asume las tablas de Julián
> (`usuarios`, `usuariosLivianos`, `tiposUsuario`, `roles`, en camelCase) y no
> las modifica; solo agrega las tablas nuevas con sus llaves foráneas hacia
> `usuarios (uuidUsuario)`.

## Requisitos para ejecutar en local

### Opción A — Con Docker (recomendada, la misma que usa la base original)

- **Docker Desktop** (incluye Docker Compose)
- **JDK 17** (solo para compilar el backend antes de construir la imagen; el
  backend incluye Maven Wrapper, así que no hace falta instalar Maven aparte)

```bash
# 1) Backend: generar el jar y levantar backend + MySQL con Docker
cd backend/solicitudes
./mvnw clean install -DskipTests
cd ..
docker compose up --build
# Backend disponible en http://localhost:8080
# MySQL expuesto en localhost:3307 (root / mysql)

# 2) Frontend
cd ../frontend
npm install
npm start
# Frontend disponible en http://localhost:4200
```

Primer arranque: Hibernate crea las tablas al levantar el backend, pero
nada se siembra solo (`spring.sql.init.mode=never`). Con una base vacía, en
este orden:

1. Correr `backend/solicitudes/src/main/resources/data.sql` una sola vez
   contra la base (`docker compose exec cfiet_database mysql -u root -p<REDACTADO>
   cfiet` y pegar su contenido). Carga los roles y tipos de usuario de
   Julián, los roles `Estudiante` y `Funcionario Académico` y el tipo de
   usuario `Estudiante` de la extensión, y el usuario `rootfiet` (Secretario
   General, contraseña `rootfiet1234`, la
   misma que usan los scripts de `backend/pruebas`). Sin ese paso no existen
   roles ni login.
2. Crear el Funcionario Académico que atenderá los tres procesos con
   `POST /api/unicauca/fiet/consejo/funcionarios-academicos`, autenticado
   como `rootfiet`.
3. Correr `docs/database/seed-procesos-academicos.sql` con la variable
   `@funcionario_uuid` apuntando a ese funcionario; el encabezado del
   archivo trae el comando para Docker. Siembra los cinco programas
   académicos, los tres tipos de solicitud, sus etapas, etiquetas por rol,
   tipos de anexo y el catálogo de situaciones académicas. Se puede correr
   más de una vez.

Si la base ya tenía el `data.sql` original de Julián, los roles y el tipo de
usuario de la extensión se agregan con `docs/database/seed-roles-extension.sql`.

Si la base ya tenía estudiantes y asignaturas de antes de los programas
académicos, hay que aplicar `docs/database/migracion_programas_asignaturas.sql`
con el backend detenido y antes de levantar la versión nueva. Los comandos
están al final de `docs/database/diccionario-datos-extension.md`.

Los archivos subidos quedan en `backend/uploads`, que `docker-compose.yml`
monta en el contenedor como `/app/uploads`.

El frontend también tiene su propio `dockerfile` si se prefiere
contenerizarlo en vez de correrlo con `npm start`.

### Opción B — Sin Docker (todo instalado en el equipo)

- **JDK 17**
- **MySQL 8** corriendo en local, con una base de datos creada para el
  proyecto
- **Node.js 20.19+** (o 22.12+; requisito de Angular 20)

`backend/solicitudes/src/main/resources/application.properties` no trae
valores fijos de conexión: lee las variables de entorno `SERVER_PORT`,
`DB_URL`, `DB_USER_NAME`, `DB_PASSWORD` y `UPLOADS_PATH` (esta última por
defecto `/app/uploads`). Hay que definirlas para apuntar al MySQL local en
vez del contenedor `cfiet_database`.

## Pruebas

Desde `backend/solicitudes`, con la base arriba y las variables de entorno de
la conexión definidas (el detalle está en `backend/CLAUDE.md`, sección
"Pruebas"):

```
.\mvnw.cmd test
```

Con el backend corriendo en Docker, desde `backend`:

```
powershell -File .\pruebas\todo.ps1
```

`todo.ps1` corre los scripts de humo de `backend/pruebas` y la matriz de
permisos de los endpoints académicos. Además comprueba que cada script deje
el conteo de filas de todas las tablas (salvo la de logs), los responsables
de cada tipo de solicitud y la carpeta `uploads` como estaban.

## Estado del proyecto

Este repositorio se inicializó con el código base de Julián tal como está en
su rama de producción. Sobre esa base, el backend ya implementa los tres
procesos académicos: catálogo de asignaturas, estudiantes y funcionarios
académicos (por formulario y por carga masiva desde Excel), catálogos
académicos, radicación, trámite por etapas, anexos, Resolución escaneada y
consultas por rol. El contrato de la API está en
`docs/api/contrato-api-procesos-academicos.md`. El frontend todavía no
consume los endpoints de los procesos académicos.



===== ARCHIVO: docs/database/diccionario-datos-extension.md =====

# Diccionario de Datos

Detalle campo por campo de las 19 tablas nuevas de la extensión. `usuarios`,
`usuariosLivianos` y `tiposUsuario` son de Julián Camacho (nombres reales,
verificados contra el código de `back-fiet-sc`, no un supuesto) y no se
listan aquí porque no se modifican.

Los tipos, nulos, valores por defecto, llaves únicas y llaves foráneas de esta
página coinciden con `script_bd_extension.sql` y con `DESCRIBE` sobre la base
que crea Hibernate (`docker compose exec cfiet_database mysql -u root -p<REDACTADO>
cfiet`). Las llaves foráneas llevan el nombre que se indica entre paréntesis.
Las reglas de negocio que usan estas columnas (etapas, transiciones, anexos y
plazos) están en `etapas-por-proceso.md`.

## PROGRAMA\_ACADEMICO

Catálogo fijo de los cinco programas de la FIET. Se siembra con
`seed-procesos-academicos.sql` (y con `migracion_programas_asignaturas.sql`
en una base que ya tenía datos), con UUID fijos e `INSERT IGNORE`. No hay
endpoint para crearlos ni editarlos; se leen con `GET
catalogos-academicos/programas`.

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidProgramaAcademico | varchar(100) | PK | No | Identificador del programa |
| nombre | varchar(100) | UK (uk\_programa\_nombre) | No | Nombre oficial. En los Excel se escribe exacto, sin distinguir mayúsculas |
| sigla | varchar(10) | UK (uk\_programa\_sigla) | No | Prefijo del código de sus asignaturas |
| ultimoConsecutivoAsignatura | int |  | No | Último consecutivo usado en el código de sus asignaturas. Valor por defecto 0. El backend lo lee con bloqueo de fila y lo sube en la misma transacción que crea la asignatura; nunca baja, así que un código no se reutiliza |

| Programa | Sigla | uuid |
| --- | --- | --- |
| Ingeniería Electrónica y Telecomunicaciones | IET | `14299e0f-8da8-4c73-ad2b-b61e0c52b7f5` |
| Ingeniería de Sistemas | ISI | `5d73d656-2504-44cc-ab6e-5cd10b500abf` |
| Ingeniería en Automática Industrial | IAI | `8606cf01-c111-4aef-92cd-a111cb7b7a38` |
| Tecnología en Telemática | TTE | `7ebedec6-aeab-403b-8a97-9312061bd4b7` |
| Ingeniería en Inteligencia Artificial y Ciencia de Datos | IAC | `d21ec0bf-cee1-4a06-8ae7-00477e88cfe6` |

## ESTUDIANTE

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| Usuario\_uuid | varchar(100) | PK, FK -> usuarios (uuidUsuario) (fk\_estudiante\_usuario) | No | Comparte identificador con Usuario |
| codigoEstudiantil | varchar(45) | UK (uk\_estudiante\_codigo) | No | Código estudiantil, único entre estudiantes |
| ProgramaAcademico\_uuid | varchar(100) | FK -> PROGRAMA\_ACADEMICO (fk\_estudiante\_programa) | No | Programa que cursa. Se elige al crearlo y no se edita. Solo puede matricular asignaturas de este programa |
| semestre | varchar(10) |  | No | Semestre actual |
| facultad | varchar(100) |  | No | Siempre `FIET`. La asigna el backend al crear y al editar; no se recibe en el formulario, en el `PUT` ni en el Excel |

## FUNCIONARIO\_ACADEMICO

Extensión propia de este trabajo de grado, distinta del `Funcionario` /
tabla `funcionarios` de Julián (ese es un rol de comité de facultad, sin
relación con los tres procesos académicos). Cubre solo al Funcionario
Académico, es decir, al Técnico Administrativo de Procesos Académicos que
verifica la información académica.

El Decano no tiene fila en esta tabla ni en ninguna otra de la extensión.
Como en Julián, es un `Usuario` con el rol `Decano` y el tipo de usuario
`Maxima autoridad FIET - Decano`, que ya existen en su `data.sql`, y se
identifica por ese rol. Ninguna FK de la extensión apunta al Decano: el
historial apunta a `usuarios`.

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| Usuario\_uuid | varchar(100) | PK, FK -> usuarios (uuidUsuario) (fk\_funcionarioacademico\_usuario) | No | Comparte identificador con Usuario |
| cargo | varchar(100) |  | No | Cargo que ocupa el Técnico Administrativo de Procesos Académicos (antes `dependencia`); no distingue roles. Editable por `PUT funcionarios-academicos/{uuid}` |

## ASIGNATURA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidAsignatura | varchar(100) | PK | No | Identificador de la materia |
| codigoAsignatura | varchar(45) | UK (uk\_asignatura\_codigo) | No | Lo genera el backend: sigla del programa, guion y consecutivo del programa con al menos tres dígitos (`ISI-001`, `ISI-1000`). No se edita ni se reutiliza |
| nombreAsignatura | varchar(150) |  | No | Nombre de la materia, sin espacios sobrantes. No se repite dentro del mismo programa sin distinguir mayúsculas ni espacios (regla del backend, no hay índice) |
| ProgramaAcademico\_uuid | varchar(100) | FK -> PROGRAMA\_ACADEMICO (fk\_asignatura\_programa) | No | Programa al que pertenece. Obligatorio y fijo desde la creación |

## ASIGNATURA\_MATRICULADA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidAsignaturaMatriculada | varchar(100) | PK | No | Identificador de la matrícula puntual |
| Estudiante\_uuid | varchar(100) | FK -> ESTUDIANTE (fk\_asigmat\_estudiante) | No | Estudiante que la tiene matriculada |
| Asignatura\_uuid | varchar(100) | FK -> ASIGNATURA (fk\_asigmat\_asignatura) | No | Materia matriculada |
| grupo | varchar(20) |  | No | Grupo en el que está matriculada |
| estado | varchar(20) |  | No | activa, cancelada, aprobada o perdida. Valor por defecto `activa`. Solo pasa de activa a uno de los otros tres |

## TIPO\_SOLICITUD\_ACADEMICA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidTipoSolicitudAcademica | varchar(100) | PK | No | Identificador del tipo |
| nombre | varchar(100) |  | No | Cancelación de Matrícula / Cancelación de Asignatura / Examen Supletorio. El backend asocia cada tipo a su proceso por este nombre exacto, así que renombrarlo rompe la asociación |
| descripcion | varchar(255) |  | Sí | Descripción del tipo |
| FuncionarioAcademico\_uuid | varchar(100) | FK -> FUNCIONARIO\_ACADEMICO (fk\_tiposolicitud\_funcionario) | No | Funcionario que atiende este tipo; lo cambian el Secretario General o el Decano |

## ETAPA\_SOLICITUD\_ACADEMICA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidEtapa | varchar(100) | PK | No | Identificador de la etapa |
| codigo | varchar(60) |  | No | RADICADA, EN\_REVISION\_DECANO, APROBADA\_POR\_DECANO, RECHAZADA\_POR\_DECANO, PENDIENTE\_PAGO, EN\_VERIFICACION\_PAGO, APROBADA o RECHAZADA |
| TipoSolicitudAcademica\_uuid | varchar(100) | FK -> TIPO\_SOLICITUD\_ACADEMICA (fk\_etapa\_tiposolicitud) | Sí | Nulo = etapa universal, aplica a los 3 procesos. PENDIENTE\_PAGO y EN\_VERIFICACION\_PAGO apuntan a Examen Supletorio |

## ETAPA\_ETIQUETA\_ROL

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| Etapa\_uuid | varchar(100) | PK (compuesta), FK -> ETAPA\_SOLICITUD\_ACADEMICA (fk\_etiqueta\_etapa) | No | Etapa que se traduce |
| rol | enum('ESTUDIANTE','FUNCIONARIO','DECANO') | PK (compuesta) | No | Rol que ve la etiqueta |
| etiqueta | varchar(60) |  | No | Texto que ve ese rol para esa etapa. Una etapa sin fila para un rol no es visible para ese rol (el Decano no tiene fila en RADICADA) |

## SOLICITUD\_ACADEMICA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidSolicitudAcademica | varchar(100) | PK | No | Identificador de la solicitud |
| radicado | varchar(20) | UK (uk\_solacad\_radicado) | No | Identificador legible: AAAA-CM-0001, AAAA-CA-0001 o AAAA-ES-0001 (año, tipo y consecutivo de cuatro dígitos por año y tipo) |
| Estudiante\_uuid | varchar(100) | FK -> ESTUDIANTE (fk\_solacad\_estudiante) | No | Estudiante que radica |
| TipoSolicitudAcademica\_uuid | varchar(100) | FK -> TIPO\_SOLICITUD\_ACADEMICA (fk\_solacad\_tiposolicitud) | No | Tipo de trámite |
| fechaCreacion | datetime |  | No | Fecha de radicación. Valor por defecto CURRENT\_TIMESTAMP; el backend la escribe con la hora de Colombia |
| Etapa\_uuid | varchar(100) | FK -> ETAPA\_SOLICITUD\_ACADEMICA (fk\_solacad\_etapa) | No | Etapa interna actual |

## SOLICITUD\_CANCELACION\_MATRICULA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| SolicitudAcademica\_uuid | varchar(100) | PK, FK -> SOLICITUD\_ACADEMICA (fk\_solcm\_solacad) | No | Comparte identificador con la cabecera |
| motivoCancelacion | varchar(255) |  | No | Motivo alegado por el estudiante |

## SOLICITUD\_CANCELACION\_ASIGNATURA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| SolicitudAcademica\_uuid | varchar(100) | PK, FK -> SOLICITUD\_ACADEMICA (fk\_solca\_solacad) | No | Comparte identificador con la cabecera |
| motivoCancelacion | varchar(255) |  | No | Motivo alegado por el estudiante |

## SOLICITUD\_EXAMEN\_SUPLETORIO

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| SolicitudAcademica\_uuid | varchar(100) | PK, FK -> SOLICITUD\_ACADEMICA (fk\_solsup\_solacad) | No | Comparte identificador con la cabecera |
| AsignaturaMatriculada\_uuid | varchar(100) | FK -> ASIGNATURA\_MATRICULADA (fk\_solsup\_asigmat) | No | Asignatura del examen no presentado |
| fechaExamenNoPresentado | datetime |  | No | Fecha del examen original, guardada a las 00:00 |
| fechaAcordadaExamen | datetime |  | Sí | Fecha acordada para el supletorio, guardada a las 00:00. Nula hasta que el Funcionario la registra al aprobar el comprobante (decisión P17 de `etapas-por-proceso.md`) |
| tipoCausa | enum('cruce','otra') |  | No | Causa de la no presentación |

## SOLICITUD\_SUPLETORIO\_CRUCE\_ASIGNATURA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| SolicitudAcademica\_uuid | varchar(100) | PK, FK -> SOLICITUD\_EXAMEN\_SUPLETORIO (fk\_solcruce\_solsup) | No | Existe solo si tipoCausa = 'cruce' |
| AsignaturaMatriculadaCruzada\_uuid | varchar(100) | FK -> ASIGNATURA\_MATRICULADA (fk\_solcruce\_asigmat) | No | Asignatura con la que se cruza |
| fechaExamenCruzada | datetime |  | No | Fecha del examen cruzado, guardada a las 00:00 |
| horaExamenCruzada | varchar(10) |  | No | Hora del examen cruzado en formato HH:mm |

## SITUACION\_ACADEMICA\_ASIGNATURA

Catálogo fijo con los códigos R0-R3 del formato oficial PA-GA-4.2-FOR-9. Se
siembra una sola vez y se reutiliza tanto para "situación en la matrícula"
como "situación al cancelar" en `ASIGNATURA_SOLICITUD_ACADEMICA`.

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidSituacionAcademica | varchar(100) | PK | No | Identificador de la situación |
| codigo | varchar(10) |  | No | R0, R1, R2 o R3 |
| nombre | varchar(100) |  | No | R0 No cursada, R1 Cursada por primera vez, R2 Cursada por segunda vez, R3 Cursada por tercera vez |

## ASIGNATURA\_SOLICITUD\_ACADEMICA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidAsignaturaSolicitud | varchar(100) | PK | No | Identificador de la fila |
| SolicitudAcademica\_uuid | varchar(100) | FK -> SOLICITUD\_ACADEMICA (fk\_asigsol\_solacad) | No | Solicitud a la que pertenece |
| AsignaturaMatriculada\_uuid | varchar(100) | FK -> ASIGNATURA\_MATRICULADA (fk\_asigsol\_asigmat) | No | Asignatura incluida |
| numeroFaltas | varchar(20) |  | Sí | Nulo hasta que el funcionario evalúa. Si es numérico debe ser un entero mayor o igual a 0 y se guarda normalizado (`03` queda `3`); si no es numérico se guarda el texto sin espacios sobrantes |
| nota | varchar(20) |  | Sí | Nulo hasta evaluación. Si es numérica (con punto o coma) debe estar entre 0.0 y 5.0 con un decimal y se guarda con punto y un decimal (`3,5` queda `3.5`); si no es numérica se guarda el texto. La regla "nota menor a 3.0 no puede cumplir" solo aplica a notas numéricas |
| SituacionMatricula\_uuid | varchar(100) | FK -> SITUACION\_ACADEMICA\_ASIGNATURA (fk\_asigsol\_situmatricula) | Sí | Nulo hasta evaluación |
| SituacionCancelar\_uuid | varchar(100) | FK -> SITUACION\_ACADEMICA\_ASIGNATURA (fk\_asigsol\_situcancelar) | Sí | Nulo hasta resolución del Decano; en Cancelación de Asignatura solo en las aprobadas |
| cumpleCondiciones | tinyint(1) |  | Sí | Solo Cancelación de Asignatura: el Funcionario confirma que verificó las condiciones de la sección 4.2 de `etapas-por-proceso.md`. Nulo hasta evaluación |
| observacionEvaluacion | varchar(255) |  | Sí | Solo Cancelación de Asignatura: obligatoria si la asignatura no cumple las condiciones. La ven el Funcionario y el Decano, nunca el Estudiante |
| aprobadaPorDecano | tinyint(1) |  | Sí | Solo Cancelación de Asignatura: decisión del Decano sobre esa asignatura (P14). Nulo hasta su decisión |
| observacionDecision | varchar(255) |  | Sí | Solo Cancelación de Asignatura: la escribe el Funcionario en el ajuste de una asignatura rechazada al responder, opcional (P45, P47). La ven el Funcionario y el Decano, y el Estudiante en APROBADA o RECHAZADA |

## TIPO\_ANEXO\_ACADEMICO

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidTipoAnexoAcademico | varchar(100) | PK | No | Identificador del tipo de anexo |
| TipoSolicitudAcademica\_uuid | varchar(100) | FK -> TIPO\_SOLICITUD\_ACADEMICA (fk\_tipoanexo\_tiposolicitud) | No | Proceso al que pertenece este anexo |
| nombre | varchar(150) |  | No | Nombre del anexo requerido. El recibo, el comprobante, el soporte de justificación y el formato del docente cruzado se reconocen por este nombre sembrado |
| formatosPermitidos | varchar(60) |  | No | Extensiones separadas por coma, en minúscula y sin espacios. Ej. "pdf,jpg,png" |
| obligatorio | tinyint(1) |  | No | 1 = obligatorio, 0 = opcional. Valor por defecto 1 |

## ANEXO\_ACADEMICO

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidAnexoAcademico | varchar(100) | PK | No | Identificador del archivo adjunto |
| SolicitudAcademica\_uuid | varchar(100) | FK -> SOLICITUD\_ACADEMICA (fk\_anexo\_solacad) | No | Solicitud a la que se adjunta |
| TipoAnexoAcademico\_uuid | varchar(100) | FK -> TIPO\_ANEXO\_ACADEMICO (fk\_anexo\_tipoanexo) | Sí | Nulo = soporte libre, sin requisito fijo (solo en las cancelaciones) |
| nombreArchivo | varchar(255) |  | No | Nombre original del archivo subido, sin ruta ni caracteres de control (solo para mostrar; en disco se guarda con un nombre generado) |
| urlArchivo | varchar(400) |  | No | Ubicación del archivo, con el nombre generado por el sistema |
| tipoArchivo | varchar(100) |  | No | Tipo de contenido deducido de la extensión validada (application/pdf, image/jpeg, image/png) |
| tamanioBytes | bigint |  | No | Tamaño del archivo en bytes (máximo 5 MB) |
| Usuario\_uuid | varchar(100) | FK -> usuarios (uuidUsuario) (fk\_anexo\_usuario) | No | Quién subió el archivo |
| fechaSubida | datetime |  | No | Fecha y hora de la subida. Valor por defecto CURRENT\_TIMESTAMP |

## HISTORIAL\_SOLICITUD\_ACADEMICA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| uuidHistorial | varchar(100) | PK | No | Identificador del evento |
| SolicitudAcademica\_uuid | varchar(100) | FK -> SOLICITUD\_ACADEMICA (fk\_historial\_solacad) | No | Solicitud sobre la que ocurrió la acción |
| Usuario\_uuid | varchar(100) | FK -> usuarios (uuidUsuario) (fk\_historial\_usuario) | No | Quién ejecutó la acción |
| accion | varchar(150) |  | No | Código de la acción de la máquina de etapas: RADICAR, RECHAZAR\_FUNCIONARIO, REMITIR\_DECANO, APROBAR\_DECANO, RECHAZAR\_DECANO, ENVIAR\_RESPUESTA, ENVIAR\_RECIBO, SUBIR\_COMPROBANTE, APROBAR\_COMPROBANTE o RECHAZAR\_COMPROBANTE. La tabla no guarda la etapa: la API la reconstruye (decisión P18) |
| observaciones | varchar(500) |  | Sí | Observación de la acción, cuando aplica (máximo 500 caracteres). En `REMITIR_DECANO`, `APROBAR_DECANO` y `RECHAZAR_DECANO` es opcional y solo la ven el Funcionario y el Decano: la API se la entrega vacía al Estudiante. En `ENVIAR_RESPUESTA` es el mensaje opcional del Funcionario al Estudiante, que sí lo ve |
| fecha | datetime |  | No | Fecha y hora del evento, con precisión de segundos. Valor por defecto CURRENT\_TIMESTAMP |

## RESOLUCION\_ACADEMICA

| Campo | Tipo | Llave | Nulo | Descripción |
| --- | --- | --- | --- | --- |
| SolicitudAcademica\_uuid | varchar(100) | PK, FK -> SOLICITUD\_ACADEMICA (fk\_resolucion\_solacad) | No | Solo existe para Cancelación de Matrícula y Cancelación de Asignatura; una por solicitud |
| urlArchivo | varchar(400) |  | No | Ubicación del escaneo de la Resolución firmada físicamente |
| nombreArchivo | varchar(255) |  | No | Nombre del archivo subido |
| fechaSubida | datetime |  | No | Fecha en que se subió el escaneo. Valor por defecto CURRENT\_TIMESTAMP |
| FuncionarioAcademico\_uuid | varchar(100) | FK -> FUNCIONARIO\_ACADEMICO (fk\_resolucion\_funcionario) | No | Funcionario que subió el escaneo |

## Migración de una base con datos anteriores a PROGRAMA\_ACADEMICO

`docs/database/migracion_programas_asignaturas.sql` pone al día una base que
ya tiene estudiantes y asignaturas sin programa. Es idempotente: se puede
correr varias veces y también sobre una base donde el backend nuevo ya
arrancó y Hibernate agregó las columnas vacías. Hace, en este orden:

1. Crea `PROGRAMA_ACADEMICO` si no existe y siembra los cinco programas.
2. Agrega `ESTUDIANTE.ProgramaAcademico_uuid` y asigna a cada estudiante el
   programa cuyo nombre coincide con su texto `programaAcademico` (con la
   collation de la base, sin distinguir mayúsculas ni tildes). Los que no
   coinciden quedan en Ingeniería de Sistemas. La deja obligatoria, crea
   `fk_estudiante_programa` y borra la columna de texto `programaAcademico`.
3. Agrega `ASIGNATURA.ProgramaAcademico_uuid`, pasa a Ingeniería de Sistemas
   todas las asignaturas que aún no tienen programa válido y les da los
   códigos `ISI-001`, `ISI-002`, ... en el orden de su código anterior. Las
   matrículas y solicitudes apuntan por uuid, así que no se pierden. La deja
   obligatoria y crea `fk_asignatura_programa`.
4. Deja `ultimoConsecutivoAsignatura` de cada programa en el mayor
   consecutivo usado por sus asignaturas.

Cómo aplicarla con Docker, desde `backend/` (PowerShell), con el backend
detenido y la base arriba:

```
docker compose stop solicitudes-consejo-de-facultad
docker compose exec -T cfiet_database sh -c "mysqldump -u root -p<REDACTADO> --default-character-set=utf8mb4 --single-transaction cfiet > /tmp/respaldo_cfiet.sql"
docker compose cp cfiet_database:/tmp/respaldo_cfiet.sql ./respaldo_cfiet.sql
docker compose cp ../docs/database/migracion_programas_asignaturas.sql cfiet_database:/tmp/migracion.sql
docker compose exec -T cfiet_database mysql -u root -p<REDACTADO> --default-character-set=utf8mb4 cfiet -e "source /tmp/migracion.sql"
```

Sin Docker, contra MySQL en `localhost:3307` (usuario `root`, clave
`<REDACTADO>`): `mysql -h 127.0.0.1 -P 3307 -u root -p<REDACTADO>
--default-character-set=utf8mb4 cfiet < docs/database/migracion_programas_asignaturas.sql`.

Después se reconstruye y se levanta el backend (`./mvnw clean install
-DskipTests` en `backend/solicitudes` y `docker compose up --build` en
`backend/`). Si el backend nuevo arranca sin la migración, no falla al
iniciar: Hibernate crea `PROGRAMA_ACADEMICO` vacía, agrega las dos columnas
con texto vacío y deja en el log dos WARN porque no puede crear las llaves
foráneas. Mientras tanto la lista de programas sale vacía, las asignaturas
viejas no aparecen y los listados de estudiantes responden 500. Correr la
migración después corrige ese estado.

Una base nueva no necesita la migración: Hibernate crea las tablas y los
programas se siembran con `seed-procesos-academicos.sql`.

## Migración de cargo, situaciones, faltas y nota (T12.1)

`docs/database/migracion_cargo_situaciones_notas.sql` pone al día una base
anterior a T12.1. Es idempotente y no borra datos. Hace, en este orden:

1. Renombra `FUNCIONARIO_ACADEMICO.dependencia` a `cargo`. Si el backend
   nuevo ya arrancó y Hibernate creó `cargo` vacía, copia `dependencia` en
   las filas sin cargo y borra `dependencia`.
2. Deja la `facultad` de todos los estudiantes en `FIET`.
3. Cambia `ASIGNATURA_SOLICITUD_ACADEMICA.numeroFaltas` y `nota` a
   `varchar(20)`. Los valores existentes se conservan como texto (`4.0`,
   `0`). Hibernate no cambia el tipo de una columna existente, por eso la
   migración es obligatoria en bases con datos.
4. Renombra las situaciones por código: R0 No cursada, R1 Cursada por
   primera vez, R2 Cursada por segunda vez, R3 Cursada por tercera vez. Los
   uuid no cambian, así que una fila que ya apuntaba a R1 pasa a mostrarse
   como "Cursada por primera vez".
5. Cambia las etiquetas del rol `FUNCIONARIO` en `APROBADA_POR_DECANO`
   ("Aprobada por el Decano") y `RECHAZADA_POR_DECANO` ("Rechazada por el
   Decano"). Con P46 de `etapas-por-proceso.md` esas dos pasan a "Pendiente
   Notificar" y `EN_VERIFICACION_PAGO` a "Recibo Pagado"; las dos sentencias
   `UPDATE` al final de `seed-procesos-academicos.sql` ponen al día una base
   existente.

Se aplica igual que la anterior, antes de levantar el backend nuevo y después
de sacar un respaldo:

```
docker compose cp ../docs/database/migracion_cargo_situaciones_notas.sql cfiet_database:/tmp/migracion_t12.sql
docker compose exec -T cfiet_database mysql -u root -p<REDACTADO> --default-character-set=utf8mb4 cfiet -e "source /tmp/migracion_t12.sql"
```



===== ARCHIVO: docs/database/etapas-por-proceso.md =====

# Etapas, transiciones, anexos y plazos por proceso

Documento de reglas de los tres procesos académicos de la extensión. Complementa a `diccionario-datos-extension.md` (que dice qué se guarda) con lo que ese diccionario no dice: en qué etapas está una solicitud, quién puede moverla, qué se exige en cada paso, qué anexos lleva cada proceso y qué plazos aplican.

Las reglas salen de estas fuentes:

- Documentos de proceso: Cancelación de Matrícula, Cancelación de Materia y Examen Supletorio.
- Formatos oficiales PA-GA-4.2-FOR-14, PA-GA-4.2-FOR-9 y PM-FO-4-FOR-23, y procedimientos PA-GA-4.2-PR-7 y PM-FO-4-PR-28.
- Acuerdo 002 de 1988 (Reglamento Estudiantil), artículos 66 a 70 y capítulo de supletorios.
- Los prototipos `estudiante.html`, `funcionario.html` y los tres formularios, solo para vocabulario y pantallas.

Las historias de usuario HU-V2 están desactualizadas y no se tienen en cuenta. Donde un prototipo contradice una decisión de este documento, manda este documento.

Lo que no aparece en ninguna fuente no se inventa: se pregunta antes de decidir y la decisión queda en la sección 8. Hoy esa sección no tiene decisiones pendientes.

## 1. Actores

| Actor | Rol en la aplicación | Qué hace en estos procesos |
| --- | --- | --- |
| Estudiante | Estudiante | Radica la solicitud, adjunta anexos, sube el comprobante de pago (solo supletorio), descarga la Resolución escaneada (solo cancelaciones). |
| Funcionario Académico | Funcionario Académico | Revisa requisitos, evalúa las asignaturas, rechaza o remite al Decano, responde al estudiante, sube el escaneo de la Resolución y verifica el pago. Solo ve los tipos de solicitud que tiene asignados en `TIPO_SOLICITUD_ACADEMICA`. |
| Decano | Decano | Aprueba o rechaza lo que el Funcionario le remite. Es un Usuario común con rol Decano, sin fila en `FUNCIONARIO_ACADEMICO`. |

El Secretario General no interviene en estos tres procesos.

## 2. Modelo de estados

Cada solicitud tiene una etapa interna (`ETAPA_SOLICITUD_ACADEMICA`). De la etapa se deduce quién debe actuar (responsable actual) y qué texto ve cada rol (`ETAPA_ETIQUETA_ROL`). La decisión del Decano queda en la etapa misma (`APROBADA_POR_DECANO` o `RECHAZADA_POR_DECANO`), por eso no hace falta una columna de decisión.

| Código de etapa | Alcance | Responsable actual | Ve el Estudiante | Ve el Funcionario | Ve el Decano |
| --- | --- | --- | --- | --- | --- |
| RADICADA | Los 3 procesos | Funcionario | En trámite | Pendiente | no la ve |
| EN_REVISION_DECANO | Los 3 procesos | Decano | En trámite | En Gestión | Pendiente |
| APROBADA_POR_DECANO | Los 3 procesos | Funcionario | En trámite | Pendiente Notificar (Enviar Recibo en Examen Supletorio, P46) | Respondida |
| RECHAZADA_POR_DECANO | Los 3 procesos | Funcionario | En trámite | Pendiente Notificar | Respondida |
| PENDIENTE_PAGO | Solo Examen Supletorio | Estudiante | Pendiente de pago | En Gestión | Respondida |
| EN_VERIFICACION_PAGO | Solo Examen Supletorio | Funcionario | En verificación de pago | Recibo Pagado | Respondida |
| APROBADA | Los 3 procesos (final) | Ninguno | Aprobada | Respondida | Respondida |
| RECHAZADA | Los 3 procesos (final) | Ninguno | Rechazada | Respondida | Respondida |

Notas sobre las etiquetas:

- Las etiquetas del Estudiante y del Funcionario se tomaron de los prototipos `estudiante.html` y `funcionario.html`. Como son datos en `ETAPA_ETIQUETA_ROL`, cambiarlas no exige tocar código. En T12.1 las del Funcionario en APROBADA_POR_DECANO y RECHAZADA_POR_DECANO pasaron de "Pendiente de Respuesta" a "Aprobada por el Decano" y "Rechazada por el Decano", para que distinga la decisión sin abrir la solicitud. Con P46 vuelven a ser estados de gestión del Funcionario (Pendiente Notificar, Enviar Recibo, Recibo Pagado) y la decisión del Decano se ve solo en el detalle.
- Las etiquetas del Decano (Pendiente, En Gestión, Respondida) son una propuesta, porque no hay prototipo del Decano entre los documentos.
- Las etapas con alcance "Los 3 procesos" tienen `TipoSolicitudAcademica_uuid` nulo. Las de solo supletorio se enlazan al tipo Examen Supletorio.
- Etapas finales: APROBADA y RECHAZADA. Desde ellas no sale ninguna transición.

## 3. Transiciones permitidas

Toda transición exige que el usuario tenga el rol indicado, que la solicitud esté en la etapa de origen y, para el Funcionario, que el tipo de solicitud esté asignado a él. Toda transición escribe una fila en `HISTORIAL_SOLICITUD_ACADEMICA`. Cualquier otra combinación se rechaza.

### 3.1 Cancelación de Matrícula y Cancelación de Asignatura

| De | Acción | A | Actor | Se exige |
| --- | --- | --- | --- | --- |
| (nueva) | Radicar | RADICADA | Estudiante | Validaciones de la sección 4 y anexos de la sección 5. |
| RADICADA | Rechazar | RECHAZADA | Funcionario | Observación obligatoria y escaneo de la Resolución (PDF). La Resolución la firma el Decano a mano. |
| RADICADA | Remitir al Decano | EN_REVISION_DECANO | Funcionario | Cada asignatura evaluada: número de faltas, nota y situación en la matrícula; en Cancelación de Asignatura, además, si cumple las condiciones (P14). Faltas y nota pueden ser texto (P38). Una sola observación, opcional, para el Decano; el Estudiante no la ve (P16). Sin archivo de Resolución en este paso; si había un escaneo, se retira (P21). |
| EN_REVISION_DECANO | Aprobar | APROBADA_POR_DECANO | Decano | En Cancelación de Matrícula, situación al cancelar de cada asignatura (P9). En Cancelación de Asignatura, aceptar la evaluación del Funcionario o decidir cada asignatura, sin situación al cancelar (P14). Observación opcional, que el Estudiante no ve (P16). |
| EN_REVISION_DECANO | Rechazar | RECHAZADA_POR_DECANO | Decano | Observación opcional, que el Estudiante no ve (P16). |
| APROBADA_POR_DECANO | Enviar respuesta | APROBADA | Funcionario | Escaneo de la Resolución firmada (PDF). Queda descargable para el Estudiante. Mensaje al Estudiante (P39); en Cancelación de Asignatura, ajustes de las decisiones cambiadas y mensaje obligatorio si el Decano rechazó alguna asignatura (P45). |
| RECHAZADA_POR_DECANO | Enviar respuesta | RECHAZADA | Funcionario | Escaneo de la Resolución firmada (PDF). Mensaje al Estudiante (P39), obligatorio en Cancelación de Asignatura (P45); la observación del Decano no le llega. |

### 3.2 Examen Supletorio

| De | Acción | A | Actor | Se exige |
| --- | --- | --- | --- | --- |
| (nueva) | Radicar | RADICADA | Estudiante | Validaciones de la sección 4, plazo de 3 días hábiles y anexos de la sección 5. |
| RADICADA | Rechazar | RECHAZADA | Funcionario | Observación obligatoria. No genera Resolución. |
| RADICADA | Remitir al Decano | EN_REVISION_DECANO | Funcionario | Requisitos verificados. Observación opcional para el Decano (P16). |
| EN_REVISION_DECANO | Aprobar | APROBADA_POR_DECANO | Decano | Observación opcional (P16). |
| EN_REVISION_DECANO | Rechazar | RECHAZADA_POR_DECANO | Decano | Observación opcional (P16). |
| RECHAZADA_POR_DECANO | Enviar respuesta | RECHAZADA | Funcionario | Mensaje opcional al Estudiante (P39). No genera Resolución. |
| APROBADA_POR_DECANO | Enviar recibo de pago | PENDIENTE_PAGO | Funcionario | Archivo del recibo obligatorio. |
| PENDIENTE_PAGO | Subir comprobante | EN_VERIFICACION_PAGO | Estudiante | Archivo del comprobante obligatorio. |
| EN_VERIFICACION_PAGO | Aprobar comprobante | APROBADA | Funcionario | Ninguno más. |
| EN_VERIFICACION_PAGO | Rechazar comprobante | RECHAZADA | Funcionario | Observación obligatoria. Ver punto abierto P4. |

Tras la aprobación final, el Funcionario notifica al docente y a DARCA fuera de la aplicación, tal como dice el documento del proceso.

No hay en las fuentes una acción de desistimiento del Estudiante, así que no se modela.

## 4. Reglas al radicar

Comunes a los tres procesos:

- El solicitante debe tener fila en `ESTUDIANTE`.
- Cada solicitud recibe un identificador único legible que funciona como radicado (sección 7).
- Cada estudiante puede tener una sola solicitud en curso por tipo (el prototipo bloquea con "Ya tienes una solicitud de este trámite en curso"). En curso significa en cualquier etapa distinta de APROBADA y RECHAZADA.
- Las asignaturas se eligen entre las `ASIGNATURA_MATRICULADA` del propio estudiante con estado `activa`. No se acepta código ni nombre digitado.
- Solo el estudiante dueño de la solicitud la ve.

### 4.1 Cancelación de Matrícula

- Motivo de la cancelación obligatorio.
- Se incluyen todas las asignaturas activas del estudiante (una fila en `ASIGNATURA_SOLICITUD_ACADEMICA` por cada una).
- Plazo: hasta el último día de clases del periodo (Acuerdo 002 de 1988, capítulo VIII). El sistema no valida fechas; la apertura y cierre del proceso se controla con el interruptor de la sección 9.
- Advertencia, solo en la interfaz: si el estudiante cursa su primer periodo de carrera, al cancelar solo podrá volver al programa por el proceso de admisión vigente. El prototipo pide confirmarla con una casilla.
- Al evaluar, el Funcionario aplica los criterios del artículo 2 del formato FOR-14 para la situación al cancelar: aprobada con nota definitiva igual o mayor a 3.0; perdida por faltas de asistencia; perdida si el promedio ponderado de parciales es menor a 2.0 o la nota final menor a 3.0; no cursada en los demás casos.

### 4.2 Cancelación de Asignatura

- Motivo de la cancelación obligatorio y al menos una asignatura.
- Plazo: antes de la terminación del periodo académico. El sistema no valida fechas; ver sección 9.
- Cuatro condiciones académicas (Acuerdo 002 de 1988, capítulo VIII, artículo 5, y formato FOR-9). Las verifica el Funcionario al revisar:
  1. La solicitud se hace antes de terminar el periodo académico.
  2. La asignatura no se cursa en calidad de repitente.
  3. No se violan condiciones de co-requisitos.
  4. La nota promedio es igual o mayor a 3.0.
- El resto de la matrícula permanece activa.
- El Funcionario marca al remitir si cada asignatura cumple las condiciones. El Decano acepta esa evaluación tal como está o aprueba o rechaza cada asignatura por separado, y puede contradecir al Funcionario en los dos sentidos (decisión P14 de la sección 8).
- Cuando la decisión del Decano sobre una asignatura no coincide con la evaluación del Funcionario, el Funcionario la ajusta al enviar la respuesta: situación al cancelar si quedó aprobada; si quedó rechazada no necesita ajuste y el motivo va en la observación general de la respuesta (P45, P47).

### 4.3 Examen Supletorio

- Una asignatura matriculada activa, la fecha del examen no presentado y la causa: `cruce` u `otra`.
- Plazo: máximo 3 días hábiles después de la fecha del examen al que no concurrió. El plazo se cuenta por separado para cada examen.
- Días hábiles son lunes a viernes. Los festivos no se descuentan (así lo hace el prototipo). Ver punto abierto P8.
- Si la causa es `cruce`: también la asignatura con la que se cruza (otra matriculada activa del estudiante), la fecha y la hora de ese examen. Se guarda en `SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA`.
- La fecha acordada para el supletorio puede quedar nula hasta acordarse.
- Si el Funcionario rechaza el comprobante de pago, el estudiante debe radicar una solicitud nueva (según el prototipo). Ver P4.

## 5. Anexos por proceso

Tamaño máximo por archivo: 5 MB, tal como validan los prototipos. Salvo que se indique otra cosa, los formatos permitidos son PDF, JPG y PNG. Los anexos se registran como filas de `TIPO_ANEXO_ACADEMICO` por proceso, con su columna `formatosPermitidos` y `obligatorio`.

### 5.1 Cancelación de Matrícula

Seis anexos obligatorios, sin excepción (el prototipo marcaba el 6 como opcional solo para probar la pantalla), tomados del formato FOR-14:

| N | Anexo | Entidad | Formatos |
| --- | --- | --- | --- |
| 1 | Paz y salvo | División de Bibliotecas | pdf |
| 2 | Paz y salvo | División de Deportes y Recreación | pdf |
| 3 | Paz y salvo | División de Salud Integral | pdf |
| 4 | Cupón de Confirmación de la Intervención Psicosocial | División de Salud Integral | pdf |
| 5 | Paz y salvo | División Financiera | pdf |
| 6 | Carné estudiantil o constancia de no trámite | DARCA | pdf, jpg, jpeg, png |

Además, un soporte libre del motivo, opcional (`TipoAnexoAcademico_uuid` nulo).

Vigencia: el formato FOR-14 dice que los paz y salvo valen 5 días calendario desde su expedición. El sistema no lo valida y no guarda fecha de expedición: el Funcionario revisa los PDF adjuntos junto con el formulario y decide.

El pago de 12.000 pesos del paz y salvo universitario y las gestiones con cada división ocurren fuera de la aplicación; la aplicación solo recibe los documentos resultantes.

### 5.2 Cancelación de Asignatura

Ningún documento oficial obligatorio. Se pide el motivo y, de forma opcional, un soporte libre que lo respalde (incapacidad, certificación laboral u otro), `TipoAnexoAcademico_uuid` nulo.

### 5.3 Examen Supletorio

| Anexo | Cuándo se exige | Quién lo sube | Formatos |
| --- | --- | --- | --- |
| Formato firmado por el docente que orienta la asignatura (formato institucional PM-FO-4-FOR-23; nombre en `TIPO_ANEXO_ACADEMICO` desde T12.16) | Siempre | Estudiante, al radicar | pdf, jpg, png |
| Soporte de la justificación de la no presentación | Si la causa es `otra` | Estudiante, al radicar | pdf, jpg, png |
| Formato firmado por el docente de la asignatura con la que se cruza | Si la causa es `cruce` | Estudiante, al radicar | pdf, jpg, png |
| Recibo de pago | En la transición APROBADA_POR_DECANO a PENDIENTE_PAGO | Funcionario | pdf |
| Comprobante de pago | En la transición PENDIENTE_PAGO a EN_VERIFICACION_PAGO | Estudiante | pdf, jpg, png |

Los anexos condicionales no se pueden expresar con la columna `obligatorio` sola: se crean como tipos con `obligatorio` en 0 y el motor los exige según la causa. El recibo y el comprobante también son tipos de anexo propios del supletorio, exigidos por el motor en su transición.

### 5.4 Resolución escaneada

Solo para Cancelación de Matrícula y Cancelación de Asignatura. La Resolución es un documento físico firmado a mano por el Decano; en la aplicación solo vive su escaneo.

- Lo sube el Funcionario al cerrar el trámite: al rechazar él mismo o al enviar la respuesta final tras la decisión del Decano. Por eso se acepta en RADICADA, APROBADA_POR_DECANO y RECHAZADA_POR_DECANO (P21).
- Formato PDF. Se guarda en `RESOLUCION_ACADEMICA` con el funcionario que lo subió.
- El Estudiante la descarga desde el detalle de su solicitud cuando esta llega a APROBADA o RECHAZADA (P21).
- Examen Supletorio no produce Resolución.
- Las copias físicas de la Resolución se reparten en tres ejemplares fuera de la aplicación.

## 6. Plazos

| Proceso | Plazo | Quién lo controla |
| --- | --- | --- |
| Cancelación de Matrícula | Hasta el último día de clases del periodo | Sin validación de fecha en el sistema. Ver sección 9. |
| Cancelación de Asignatura | Antes de terminar el periodo académico | Sin validación de fecha en el sistema. Ver sección 9. |
| Examen Supletorio | 3 días hábiles después del examen no presentado | El sistema, con la fecha del examen que da el estudiante. |
| Paz y salvo de matrícula | 5 días calendario desde su expedición | El Funcionario, al revisar los PDF. El sistema no lo valida. |

## 7. Cambios al modelo de datos que se desprenden de este documento

El diccionario original no tenía estos elementos y el prototipo los usa:

- Identificador legible de la solicitud, que funciona como radicado. Se agrega `radicado` (varchar, único) a `SOLICITUD_ACADEMICA`, con formato `AAAA-CM-0001`, `AAAA-CA-0001` o `AAAA-ES-0001` (año, tipo y consecutivo; un contador por año y tipo). El `uuid` sigue siendo la llave primaria interna.
- Programas académicos: se agrega `PROGRAMA_ACADEMICO` y la llave foránea obligatoria al programa en `ASIGNATURA` y en `ESTUDIANTE`, que reemplaza la columna de texto `programaAcademico` (P28 a P34).

No se agrega fecha de expedición a los anexos ni fechas límite del periodo.

## 8. Decisiones tomadas

Confirmadas por el autor:

- El radicado es el identificador de cada solicitud.
- El sistema no valida la vigencia de los paz y salvo; lo hace el Funcionario.
- No hay fechas límite de periodo en el sistema.
- Todos los documentos oficiales de matrícula son obligatorios, incluido el de DARCA, y el soporte de justificación del supletorio es obligatorio cuando la causa es `otra`.
- Las historias de usuario no se usan; la Resolución solo se escanea y se sube al cerrar el trámite (sección 5.4).

Propuestas adoptadas por defecto (cambiables antes de implementar la tarea que las usa):

| N | Tema | Se adopta |
| --- | --- | --- |
| P4 | Rechazo del comprobante de pago | La solicitud se cierra como RECHAZADA con observación obligatoria; el estudiante radica una nueva. |
| P8 | Festivos en los días hábiles | Lunes a viernes sin festivos. Limitación aceptada del prototipo. |
| P9 | Quién registra la situación al cancelar de cada asignatura | En Cancelación de Matrícula, el Decano al decidir. En Cancelación de Asignatura, desde T12.9, el Funcionario Académico: opcional al remitir (P44) o en el ajuste al responder, pero toda asignatura aprobada debe tenerla antes de pasar a APROBADA (P45). El Decano ya no la registra en Cancelación de Asignatura. |
| P11 | Las cuatro condiciones de Cancelación de Asignatura | La lista de la sección 4.2. |
| P12 | Tamaño del escaneo de la Resolución y del recibo | 5 MB, igual que los demás archivos. |
| P13 | Soporte libre del motivo en matrícula y asignatura | Sigue siendo opcional. Solo los documentos oficiales son obligatorios. |
| P14 | Decisión del Decano en Cancelación de Asignatura | Confirmada por el autor; supera cualquier texto anterior. Aprobación parcial: el Decano aprueba o rechaza cada asignatura por separado. Al remitir, el Funcionario confirma por asignatura si cumple las condiciones de la sección 4.2 (una nota numérica menor a 3.0 no puede marcarse como que cumple, y una nota de texto no tiene esa restricción (P38); si no cumple, la observación de la evaluación es obligatoria) y al menos una debe cumplir. Desde T12.9 el Decano aprueba de una de dos formas: acepta la evaluación tal como está (`aceptarEvaluacion`, y cada asignatura queda aprobada si cumple y rechazada si no) o envía la decisión de cada asignatura (`decisiones`), una por asignatura, sin faltar ninguna. Con decisiones puede aprobar una que el Funcionario marcó como que no cumple y rechazar una que marcó como que cumple. No registra situación al cancelar (P9) ni observación por asignatura; conserva la observación general opcional, que solo ve el Funcionario (P16). Una asignatura tiene la decisión cambiada cuando `aprobadaPorDecano` es distinto de `cumpleCondiciones`; se calcula, sin columna, y se expone como `decisionCambiadaPorDecano` al Funcionario y al Decano. Con al menos una aprobada la solicitud pasa a APROBADA_POR_DECANO, y si no aprueba ninguna usa el rechazo de la solicitud completa. Las rechazadas pierden la situación al cancelar que hubiera dejado el Funcionario. Al enviar la respuesta aprobada solo se cancelan las asignaturas aprobadas que sigan activas (P45). Cancelación de Matrícula no cambia. Se guarda en `ASIGNATURA_SOLICITUD_ACADEMICA` (`cumpleCondiciones`, `observacionEvaluacion`, `aprobadaPorDecano`, `observacionDecision`). |

Decisiones de implementación, tomadas al construir el backend y registradas en `change.md`. Describen lo que hace el código hoy:

| N | Tema | Se adopta |
| --- | --- | --- |
| P15 | Lo que ve el Estudiante en Cancelación de Asignatura | Antes de APROBADA o RECHAZADA ve de cada asignatura solo el uuid de la fila, el código y el nombre. En APROBADA o RECHAZADA ve además `aprobadaPorDecano` y, desde T12.9, `observacionDecision`, que ya no escribe el Decano sino el Funcionario en el ajuste (P45). Desde P46 ve también la situación al cancelar de las asignaturas aprobadas. Nunca ve faltas, nota, situación en la matrícula, `cumpleCondiciones`, `observacionEvaluacion` ni `decisionCambiadaPorDecano`. El Funcionario asignado y el Decano ven todo. |
| P16 | Observaciones de la remisión y del Decano | Desde T12.1, en los tres procesos: al remitir, el cuerpo lleva una sola observación (`observacion`), opcional, para el Decano; al aprobar y al rechazar, la observación del Decano es opcional (en las cancelaciones la aprobación también acepta `observacion`). Esas observaciones solo las ven el Funcionario y el Decano: en el historial del Estudiante la fila `REMITIR_DECANO` aparece con la observación vacía y las filas `APROBAR_DECANO` y `RECHAZAR_DECANO` no aparecen (P40). |
| P17 | Fecha acordada del supletorio | La registra el Funcionario asignado al aprobar el comprobante (EN_VERIFICACION_PAGO a APROBADA). Es opcional, no puede ser anterior a la fecha del examen no presentado y se guarda a las 00:00 en `fechaAcordadaExamen`. |
| P18 | Código de etapa y reconstrucción del historial | Bandejas, detalles e historial devuelven, además de la etiqueta del rol, el código estable de la etapa (`etapaCodigo`). El historial no guarda la etapa y su fecha tiene precisión de segundos, así que la etapa de cada fila se reconstruye encadenando las transiciones de la sección 3 desde RADICAR, sin depender del orden por fecha. Una acción que no encaja en el recorrido queda sin código. Para el Estudiante el código de la decisión del Decano se enmascara (P41). |
| P19 | Visibilidad por rol | El rol con que actúa un usuario lo decide el servidor: dueño con rol Estudiante, si no Funcionario asignado al tipo, si no Decano. Un rol solo ve las solicitudes cuya etapa tiene etiqueta para él en `ETAPA_ETIQUETA_ROL`, en bandejas, detalle, historial y descargas. Una solicitud ajena o sin etiqueta responde igual que una inexistente. |
| P20 | Quién sube cada anexo y cuándo | Los anexos del Estudiante y los soportes libres los sube el Estudiante dueño en RADICADA; el recibo de pago, el Funcionario asignado en APROBADA_POR_DECANO; el comprobante, el Estudiante dueño en PENDIENTE_PAGO. Examen Supletorio no admite soportes libres. No se limita la cantidad de archivos por tipo. jpg y jpeg son el mismo formato, y además de la extensión se valida la firma del contenido (pdf, png, jpg). |
| P21 | Escaneo de la Resolución | Solo en las cancelaciones. Lo sube el Funcionario asignado, en PDF, en RADICADA, APROBADA_POR_DECANO o RECHAZADA_POR_DECANO. Subirlo otra vez reemplaza el anterior; en etapa final ya no se sube ni se reemplaza. Remitir al Decano retira el escaneo que hubiera. El Estudiante dueño lo descarga solo en etapa final; el Funcionario asignado y el Decano, en cualquier etapa. |
| P22 | Situación al cancelar en Cancelación de Matrícula | El Estudiante la ve solo cuando la solicitud está en etapa final; antes llega vacía. |
| P23 | Asignaturas que se cancelan al aprobar | Al enviar la respuesta aprobada de Cancelación de Matrícula pasan a `cancelada` las asignaturas de la solicitud que sigan `activa`; las que tengan otro estado no se tocan. Ninguna acción del supletorio cambia asignaturas. |
| P24 | Concurrencia al radicar | La regla de una solicitud en curso por tipo se valida con una consulta previa, sin bloqueo. El radicado se calcula como el último del año y tipo más uno; si dos radicaciones chocan, la segunda se rechaza con "No se pudo generar el radicado, intente de nuevo" y no deja nada escrito. |
| P25 | Conteo del plazo del supletorio | Con la hora de Colombia, se cuentan los días lunes a viernes posteriores al examen hasta hoy inclusive. El mismo día cuenta 0 y un examen en fin de semana empieza a contar el lunes. La fecha del examen no puede ser posterior a hoy. |
| P26 | Límites de texto | Motivo de cancelación: 255 caracteres. Observación de una acción: 500. Observación de evaluación y de decisión por asignatura: 255. Faltas y nota: 20. Hora del examen cruzado: formato HH:mm. |
| P27 | Remitir el supletorio al Decano | El Funcionario debe confirmar `requisitosVerificados`; sin esa confirmación se rechaza la acción y se le indica que rechace la solicitud. La confirmación se revisa después del actor y la etapa. Reemplazada por P49 desde T12.16. |
| P28 | Programas académicos | Catálogo fijo de cinco programas, cada uno con su sigla: Ingeniería Electrónica y Telecomunicaciones (IET), Ingeniería de Sistemas (ISI), Ingeniería en Automática Industrial (IAI), Tecnología en Telemática (TTE) e Ingeniería en Inteligencia Artificial y Ciencia de Datos (IAC). Viven en `PROGRAMA_ACADEMICO`, se siembran con UUID fijos y no hay endpoint para crearlos ni editarlos; cualquier usuario autenticado los lista con `GET catalogos-academicos/programas`. |
| P29 | Programa de una asignatura | Cada asignatura pertenece a un solo programa, obligatorio al crearla y que no se cambia después. `PUT asignaturas/{uuid}` solo cambia el nombre. |
| P30 | Código de la asignatura | Lo genera el backend: sigla del programa, guion y consecutivo del programa con al menos tres dígitos (`ISI-001`, ..., `ISI-999`, `ISI-1000`). No se edita ni se reutiliza: el contador `ultimoConsecutivoAsignatura` del programa solo sube, aunque una asignatura se borre o una creación se revierta antes de confirmarse. Para que dos creaciones simultáneas no tomen el mismo número, el contador se lee con bloqueo de fila (`SELECT ... FOR UPDATE`) dentro de la transacción del controlador, y `uk_asignatura_codigo` queda como última defensa. Una carga por Excel con varios programas los bloquea en orden de sigla. |
| P31 | Nombre de la asignatura | Obligatorio, de hasta 150 caracteres, y se guarda sin espacios sobrantes. No se repite dentro del mismo programa sin distinguir mayúsculas ni espacios; el mismo nombre en otro programa sí se permite. La regla se revisa con el programa bloqueado, así que dos creaciones simultáneas con el mismo nombre no pasan ambas. |
| P32 | Programa del estudiante | Obligatorio, se elige al crearlo (por formulario con `uuidProgramaAcademico`, por Excel con el nombre exacto del programa) y no se edita: `PUT estudiantes/{uuid}` ya no lo recibe. |
| P33 | Asignaturas que matricula un estudiante | Solo asignaturas que ya existen en el catálogo y que son de su programa, al crearlo, al agregarle una matrícula y en la carga por Excel. Se buscan por código, sin distinguir mayúsculas ni espacios. Ningún flujo de estudiantes crea asignaturas. Por formulario, un código inexistente responde código 3 y uno de otro programa código 4; en el Excel ambos son código 6 con fila y columna. |
| P34 | Datos anteriores a los programas | `migracion_programas_asignaturas.sql` asigna a cada estudiante el programa cuyo nombre coincide con su texto anterior (sin distinguir mayúsculas ni tildes) y a los demás Ingeniería de Sistemas; pasa todas las asignaturas existentes a Ingeniería de Sistemas con códigos `ISI-001` en adelante, en el orden de su código anterior. Las matrículas que ya existían se conservan aunque no cumplan P33; la regla aplica a las matrículas nuevas. |
| P35 | Programas y procesos | Los programas no cambian ninguna regla de los tres procesos: radicación, etapas y bandejas siguen igual y solo usan las matrículas activas del estudiante. |
| P36 | Facultad del estudiante | Siempre FIET. El backend la asigna al crear y al editar; no se recibe en el formulario, en el `PUT` ni en el Excel de estudiantes, que queda con 13 columnas. La columna `ESTUDIANTE.facultad` se conserva con ese valor. |
| P37 | Edición de estudiantes y funcionarios académicos | `PUT estudiantes/{uuid}` acepta nombres, apellidos, tipo y número de documento, teléfono, correo, código estudiantil y semestre; `PUT funcionarios-academicos/{uuid}` acepta los mismos datos personales y el cargo. Cada campo es opcional y uno vacío no cambia. Username, programa y facultad son de solo lectura. La unicidad de documento, correo y código estudiantil excluye al propio registro (código de error 2). Los datos personales se escriben en `usuarios` y `usuariosLivianos` con un gateway de la extensión, sin tocar el código de Julián. `dependencia` pasa a llamarse `cargo` en tabla, API, filtro y Excel. |
| P38 | Faltas y nota como texto | `numeroFaltas` y `nota` son texto de hasta 20 caracteres. Si el valor es numérico se valida como antes (faltas entero mayor o igual a 0; nota de 0.0 a 5.0 con un decimal, con punto o coma) y se guarda normalizado; si no, se acepta el texto. La regla de la nota menor a 3.0 en Cancelación de Asignatura solo aplica a notas numéricas. |
| P39 | Mensaje del Funcionario al Estudiante | `funcionario/responder` acepta un cuerpo opcional `{observacion}` de hasta 500 caracteres (en Cancelación de Asignatura, obligatorio cuando hay rechazos del Decano, P45). Se guarda en la fila `ENVIAR_RESPUESTA` del historial y el Estudiante sí lo ve. |
| P40 | Decisión del Decano oculta al Estudiante | Desde T12.3, en los tres procesos, el historial que recibe el Estudiante omite las filas `APROBAR_DECANO` y `RECHAZAR_DECANO`; el Funcionario y el Decano las siguen viendo con su observación. En las cancelaciones y en el rechazo del supletorio el Estudiante se entera de la decisión por la fila `ENVIAR_RESPUESTA`; en el supletorio aprobado, por la fila `ENVIAR_RECIBO`, que es la que sigue a la aprobación. La etapa de cada fila se reconstruye con el historial completo antes de filtrar, así que las filas visibles conservan su `etapaCodigo` (P18). |
| P41 | Código de etapa enmascarado para el Estudiante | Desde T12.4, cuando la etapa es APROBADA_POR_DECANO o RECHAZADA_POR_DECANO, la bandeja, el detalle común y el detalle de cada proceso le devuelven al Estudiante `etapaCodigo` EN_REVISION_DECANO; la etiqueta sigue siendo "En trámite". Funcionario y Decano ven el código real. PENDIENTE_PAGO, EN_VERIFICACION_PAGO, APROBADA y RECHAZADA no cambian. En el historial del Estudiante ninguna fila trae el código de la decisión (las filas del Decano ya no aparecen, P40), así que su última fila coincide con el detalle. Solo cambia la respuesta: la etapa guardada y las reglas que dependen de ella (descarga de la Resolución, situación al cancelar, acciones disponibles) usan el código real. |
| P42 | Número de documento en las bandejas | Desde T12.5 el resumen de las bandejas trae `numeroDocumento`, el documento del estudiante que radicó. Lo reciben el Funcionario Académico y el Decano; en la bandeja del Estudiante sale nulo. El caso de uso lo decide por rol (`ResumenSolicitudAcademica.numeroDocumento`) y el mapper lo copia sin leerlo de la solicitud. Las bandejas no tienen búsqueda en el servidor; el filtro por documento, si se agrega, es del cliente. No cambia ninguna tabla. |
| P43 | Número de documento en el detalle | Desde T12.6 el detalle común (`GET solicitudes-academicas/{uuid}`) y el bloque `solicitud` del detalle de cada proceso traen `numeroDocumento`, el documento del estudiante que radicó. Lo reciben el Funcionario Académico y el Decano; al Estudiante le sale nulo. Lo decide `getDetalle` por el rol con que se resolvió el acceso (`DetalleSolicitudAcademica.numeroDocumento`) y el mapper lo copia de ahí, sin leerlo del usuario de la solicitud. Va en el nivel superior del detalle, como en el resumen (P42), y no dentro del objeto `estudiante`. No cambia ninguna tabla. |
| P44 | Situación al cancelar registrada por el Funcionario | Desde T12.8, al remitir una Cancelación de Matrícula o de Asignatura el Funcionario Académico puede enviar en cada evaluación `situacionCancelarUuid`, del mismo catálogo R0 a R3 que la situación en la matrícula. Es opcional; si llega se valida contra el catálogo y se guarda en `SituacionCancelar_uuid`. En Cancelación de Matrícula el Decano la sigue registrando al aprobar (obligatoria), con lo que el Funcionario dejó como valor inicial. En Cancelación de Asignatura, desde T12.9, el Decano ya no la registra: queda la del Funcionario, y la de una asignatura aprobada contra la evaluación se pide en el ajuste (P45). Al Estudiante no se le muestra antes de la etapa final. En la interfaz de Cancelación de Asignatura la observación de la evaluación se escribe una sola vez y se envía como `observacionEvaluacion` de cada asignatura que no cumple. No cambia ninguna tabla. |
| P45 | Respuesta del Funcionario en Cancelación de Asignatura | Desde T12.9, `funcionario/responder` acepta `observacion` (máximo 500, la ve el Estudiante en el historial) y `ajustes`, una entrada por asignatura con `situacionCancelarUuid` y `observacionDecision` (máximo 255). Cada asignatura aprobada con `decisionCambiadaPorDecano` exige su ajuste (desde T12.11, una rechazada ya no, P47); las demás lo admiten con las mismas reglas. Desde T12.9b, en APROBADA_POR_DECANO toda asignatura aprobada por el Decano debe tener situación al cancelar, la guardada al remitir (P44) o la que trae su ajuste, que la reemplaza; si falta, se rechaza nombrando la asignatura, haya cambiado o no la decisión. En el ajuste de una rechazada `observacionDecision` es opcional desde T12.11 (P47) y no acepta situación; si llega, es la que ve el Estudiante en APROBADA o RECHAZADA (P15). Si el Decano rechazó alguna asignatura (`aprobadaPorDecano` falso, coincida o no con la evaluación) o la solicitud completa, la observación general es obligatoria; desde RECHAZADA_POR_DECANO no se aceptan ajustes. Todo se valida antes de cambiar la etapa y con código de error 4. Cancelación de Matrícula y Examen Supletorio no cambian: su observación de respuesta sigue siendo opcional, y en Cancelación de Matrícula no hace falta la regla de la situación porque `decano/aprobar` ya la exige en todas las asignaturas y es la única vía a APROBADA_POR_DECANO. El código estudiantil del dueño sale en las bandejas y, desde T12.9, también en el nivel superior del detalle común (`codigoEstudiantil`), para los tres roles; las bandejas siguen sin búsqueda en el servidor. No cambia ninguna tabla. |
| P46 | Ajustes de la Cancelación de Asignatura y estados del Funcionario | `observacionEvaluacion` deja de ser obligatoria cuando una asignatura no cumple: al remitir hay una sola observación por solicitud, "Observaciones para el Decano", opcional, que no se copia a las asignaturas ni se repite en el historial del frontend (se ve en su tarjeta del detalle). En APROBADA o RECHAZADA el Estudiante recibe `situacionCancelar` de las asignaturas aprobadas. Los tres `GET .../formulario` traen `codigoEstudiantil`. Las etiquetas del Funcionario quedan en Pendiente, En Gestión, Pendiente Notificar, Enviar Recibo, Recibo Pagado y Respondida; como `ETAPA_ETIQUETA_ROL` no distingue proceso, "Enviar Recibo" (supletorio en APROBADA_POR_DECANO) lo pone `ConsultaSolicitudAcademicaCUImplAdaptador`. La decisión del Decano la ve el Funcionario solo en el detalle. |
| P47 | Decisión del Decano en las bandejas y respuesta sin observación por asignatura | Desde T12.11 el resumen de las bandejas del Funcionario y del Decano trae `decisionDecano`: `APROBADA`, `RECHAZADA` o, en Cancelación de Asignatura con alguna asignatura rechazada por el Decano, `APROBADA_PARCIAL`; `null` si el Decano no ha decidido o el Funcionario rechazó antes, y siempre `null` para el Estudiante. Sale de la última fila `APROBAR_DECANO` o `RECHAZAR_DECANO` del historial, así que se conserva en APROBADA, RECHAZADA y en las etapas de pago del supletorio (un comprobante rechazado sigue siendo `APROBADA`). Los estados de la bandeja no cambian (P46). En `funcionario/responder` de Cancelación de Asignatura, `observacionDecision` de una rechazada pasa a ser opcional (máximo 255) y una rechazada cambiada ya no exige ajuste; la observación general sigue siendo obligatoria (máximo 500) con cualquier rechazo del Decano, y las aprobadas siguen exigiendo situación al cancelar si no la tienen guardada. No cambia ninguna tabla. |
| P48 | Resultado por asignatura en las respuestas del Funcionario | Desde T12.13 el resumen de la bandeja del Funcionario Académico asignado trae `resultadosAsignaturas`, una entrada `{asignaturaSolicitudUuid, aprobada}` por asignatura, solo en Cancelación de Asignatura en APROBADA o RECHAZADA. `aprobada` es verdadero solo con `aprobadaPorDecano` verdadero; falso o nulo (rechazo del Funcionario antes del Decano o rechazo total del Decano) es falso. Se carga con una sola consulta para todas las solicitudes de la bandeja, ordenada por código de asignatura. Para el Estudiante, el Decano, los otros procesos y las etapas no finales es nulo. Con esto la pantalla Respuestas del Funcionario muestra una fila por asignatura con su resultado; sigue siendo una sola solicitud, con un radicado y una Resolución. No cambia ninguna tabla. |
| P49 | Remitir el supletorio sin confirmar requisitos | Desde T12.16, `funcionario/remitir` de Examen Supletorio ya no exige `requisitosVerificados` (reemplaza P27): el cuerpo solo lee `observacion`, opcional, para el Decano, y si llega `requisitosVerificados`, verdadero o falso, se ignora sin error. Se quitan el error 4 y el mensaje `REQUISITOS_NO_CONFIRMADOS`. El modal del Funcionario ya no tiene la casilla. Las cancelaciones no cambian: siguen exigiendo la evaluación por asignatura al remitir. No cambia ninguna tabla. |

## 9. Funcionalidad opcional, al final del proyecto

Interruptor para abrir y cerrar cada proceso: el Decano activa o desactiva un tipo de solicitud para los estudiantes, porque el periodo no tiene fecha fija y cambia. Con el proceso desactivado, el estudiante no puede radicar. No es obligatoria y solo se construye si alcanza el tiempo. Mientras no exista, todos los procesos están activos.



===== ARCHIVO: docs/api/contrato-api-procesos-academicos.md =====

# Contrato de la API de los procesos académicos

Este documento describe los 66 endpoints que la extensión agrega al backend:
programas académicos y catálogo de asignaturas, estudiantes, funcionarios académicos, catálogos
académicos, solicitudes académicas y los tres procesos (Cancelación de
Matrícula, Cancelación de Asignatura y Examen Supletorio). Es la misma lista
que comprueba `backend/pruebas/matriz-permisos.ps1`, y cada dato sale de los
controladores, DTO, casos de uso y `ConfiguracionSeguridad` del código.

Todas las rutas cuelgan de `/api/unicauca/fiet/consejo/` (con Docker, en
`http://localhost:8080/api/unicauca/fiet/consejo/`). En las tablas se omite
ese prefijo.

## 1. Convenciones

### 1.1 Autenticación

`POST sesiones` (de Julián, abierto) recibe `{"username": "...", "password":
"..."}` y responde `{"uuidUsuario": "...", "token": "..."}`. El resto de
llamadas lleva la cabecera `Authorization: Bearer <token>`. El token vence a
las 24 horas.

El backend deduce del token quién llama y con qué rol actúa en cada
solicitud. El cliente nunca envía el rol ni el uuid del actor.

Los roles que usan estos endpoints son `Secretario General` y `Decano` (de
Julián), `Estudiante` y `Funcionario Académico`. El rol `Funcionario` de
Julián solo llega a los GET de `catalogos-academicos`, que están abiertos a
cualquier usuario autenticado.

### 1.2 Respuestas de error

| Situación | HTTP | Cuerpo |
| --- | --- | --- |
| Sin token o con token inválido | 401 | `{"codigoError": "401", "mensaje": "Error, sin acceso", ...}` |
| Token vencido | 401 | `{"codigoError": "401", "mensaje": "Error, token expirado", ...}` |
| Rol que la ruta no admite | 403 | `{"codigoError": "403", "mensaje": "No tiene permisos para acceder a este recurso", ...}` |
| Regla de negocio | 500 | `{"codigoError": "<1 a 9>", "mensaje": "...", "httpCodigo": 500, "url": "...", "metodo": "..."}` |
| Validación de anotaciones del DTO (`@NotBlank`, `@Size`, `@Email`) | 400 | Mapa `{"campo": "mensaje"}` |
| Fallo de la base de datos al guardar | 500 | `{"mensaje": "Error insertando en la base de datos....", "error": "..."}`, sin `codigoError` |

Los errores de negocio llegan siempre como HTTP 500; el frontend debe mirar
`codigoError`:

| codigoError | Significado | Ejemplos |
| --- | --- | --- |
| 1 | Error genérico | JSON mal formado, falta un parámetro de consulta o una parte multipart obligatoria, token sin username, no se pudo guardar el archivo |
| 2 | La entidad ya existe | Nombre de asignatura repetido en su programa, código estudiantil, documento, correo o username repetidos contra la base |
| 3 | La entidad no existe | uuid inexistente, o una solicitud que el usuario no puede ver (responde igual que una inexistente) |
| 4 | Regla de negocio violada | Etapa que no admite la acción, anexo faltante, plazo vencido, solicitud en curso, asignatura de otro programa al matricular |
| 6 | Mal formato | Paginación inválida, fecha mal escrita, nota o faltas fuera de rango, archivo no permitido, errores de Excel |
| 7 | Sin información | Carga masiva sin filas |

Los códigos 5, 8 y 9 son de Julián (credenciales, acceso y token) y estos
endpoints no los usan.

Los mensajes terminan en `...`. En este documento, `<x>` marca la parte que
cambia en cada caso.

Una solicitud que existe pero que el usuario no puede ver responde con
`codigoError` 3 y el mismo mensaje que una inexistente:
`Solicitud académica con id <uuid> no fue encontrado en el sistema...`.

### 1.3 Formatos

- Rutas con uuid: el segmento debe tener 36 caracteres hexadecimales o
  guiones.
- Fechas que envía el cliente (solo Examen Supletorio): texto `AAAA-MM-DD`.
  Otro formato responde `codigoError` 6: `El valor <valor> de <dato> no es una
  fecha con el formato AAAA-MM-DD...`.
- Fechas de detalle del supletorio en la respuesta: texto `AAAA-MM-DD`.
- Fechas y horas que genera el servidor (`fechaCreacion`, `fechaSubida`,
  `fecha` del historial): `AAAA-MM-DDTHH:mm:ss`, sin zona, en hora de Colombia.
- En los mensajes de error del supletorio las fechas salen como `dd/MM/aaaa`.
- Hora del examen cruzado: `HH:mm` de 24 horas.
- `nota`: número de 0.0 a 5.0 con un decimal.
- Listados paginados: parámetros `pagina` (desde 0) y `tamanio` (desde 1);
  otro valor responde `codigoError` 6 `Error en la paginación o tamaño de la
  pagina...`. La respuesta es `{"content": [...], "totalElements": n}`. Los
  listados sin resultados devuelven `content` vacío.

### 1.4 Archivos

- Tamaño máximo por archivo: 5 MB (5242880 bytes). Más de eso responde
  `codigoError` 6: `El archivo pesa <n> bytes y el máximo permitido es 5242880
  bytes (5 MB)...`. Spring además corta cualquier archivo o petición de más de
  20 MB.
- El archivo no puede venir vacío (`El archivo adjunto está vacío...`, código
  6).
- La extensión debe estar en los formatos del tipo de anexo y el contenido
  debe corresponder: un PDF debe empezar con `%PDF-`, un PNG o un JPG con su
  firma. `jpg` y `jpeg` son el mismo formato. Mayúsculas y espacios en la
  extensión no importan.
  - Formato no permitido: `El formato <ext> no está permitido para <anexo>, se
    aceptan <formatos>...` (código 6).
  - Contenido que no coincide: `El contenido del archivo no corresponde a un
    archivo <ext>...` (código 6).
- Formatos por tipo de anexo (sembrados en `TIPO_ANEXO_ACADEMICO`):

| Proceso | Anexo | Formatos | Obligatorio |
| --- | --- | --- | --- |
| Cancelación de Matrícula | Paz y salvo - División de Bibliotecas | pdf | Sí |
| Cancelación de Matrícula | Paz y salvo - División de Deportes y Recreación | pdf | Sí |
| Cancelación de Matrícula | Paz y salvo - División de Salud Integral | pdf | Sí |
| Cancelación de Matrícula | Cupón de Confirmación de la Intervención Psicosocial - División de Salud Integral | pdf | Sí |
| Cancelación de Matrícula | Paz y salvo - División Financiera | pdf | Sí |
| Cancelación de Matrícula | Carné estudiantil o constancia de no trámite - DARCA | pdf, jpg, jpeg, png | Sí |
| Examen Supletorio | Formato firmado por el docente que orienta la asignatura | pdf, jpg, png | Sí |
| Examen Supletorio | Soporte de la justificación de la no presentación | pdf, jpg, png | Si la causa es `otra` |
| Examen Supletorio | Formato firmado por el docente de la asignatura con la que se cruza | pdf, jpg, png | Si la causa es `cruce` |
| Examen Supletorio | Recibo de pago | pdf | Lo sube el Funcionario para pasar a PENDIENTE_PAGO |
| Examen Supletorio | Comprobante de pago | pdf, jpg, png | Lo sube el Estudiante para pasar a EN_VERIFICACION_PAGO |
| Cancelaciones | Soporte libre (sin tipo) | pdf, jpg, jpeg, png | No |
| Cancelaciones | Escaneo de la Resolución | pdf | Para rechazar en el Funcionario y para enviar la respuesta |

Los uuid de los tipos de anexo los entregan los formularios de radicación y
`GET catalogos-academicos/tipos-solicitud/{uuidTipo}/tipos-anexo`.

- Las descargas responden el archivo binario con `Content-Type` del archivo
  y `Content-Disposition: attachment` con el nombre original.

### 1.5 Etapas

`etapaCodigo` es estable y sirve para decidir en el frontend; `etiqueta` es el
texto para mostrar al rol que consulta. Un rol no ve las solicitudes de una
etapa sin etiqueta para él.

Al Estudiante, en `APROBADA_POR_DECANO` y `RECHAZADA_POR_DECANO`, la bandeja,
el detalle común y el detalle de cada proceso le devuelven `etapaCodigo`
`EN_REVISION_DECANO`, con la etiqueta "En trámite": no se entera de la
decisión hasta la respuesta del Funcionario (o el recibo de pago en el
supletorio aprobado). Funcionario Académico y Decano reciben el código real.
`PENDIENTE_PAGO`, `EN_VERIFICACION_PAGO`, `APROBADA` y `RECHAZADA` no se
enmascaran.

| etapaCodigo | Procesos | Etiqueta Estudiante | Etiqueta Funcionario | Etiqueta Decano |
| --- | --- | --- | --- | --- |
| RADICADA | los tres | En trámite | Pendiente | (no la ve) |
| EN_REVISION_DECANO | los tres | En trámite | En Gestión | Pendiente |
| APROBADA_POR_DECANO | los tres | En trámite | Pendiente Notificar (Enviar Recibo en el supletorio, P46) | Respondida |
| RECHAZADA_POR_DECANO | los tres | En trámite | Pendiente Notificar | Respondida |
| PENDIENTE_PAGO | supletorio | Pendiente de pago | En Gestión | Respondida |
| EN_VERIFICACION_PAGO | supletorio | En verificación de pago | Recibo Pagado | Respondida |
| APROBADA | los tres (final) | Aprobada | Respondida | Respondida |
| RECHAZADA | los tres (final) | Rechazada | Respondida | Respondida |

Las etiquetas salen de la tabla `ETAPA_ETIQUETA_ROL` y se pueden consultar con
`GET catalogos-academicos/etiquetas`.

### 1.6 Acciones

El detalle de cada solicitud trae `accionesDisponibles`: los códigos de las
acciones que el rol que consulta puede ejecutar en la etapa actual. Cada
código se ejecuta con este endpoint (`<proceso>` es `cancelaciones-matricula`,
`cancelaciones-asignatura` o `examenes-supletorios`):

| Acción | Endpoint | Rol | Exige |
| --- | --- | --- | --- |
| RECHAZAR_FUNCIONARIO | `POST <proceso>/{uuid}/funcionario/rechazar` | Funcionario Académico | Observación; en cancelaciones, escaneo de la Resolución subido antes |
| REMITIR_DECANO | `POST <proceso>/{uuid}/funcionario/remitir` | Funcionario Académico | Evaluaciones (cancelaciones); en el supletorio nada más (desde T12.16 no hay `requisitosVerificados`, P49). Una sola `observacion`, opcional, para el Decano |
| APROBAR_DECANO | `POST <proceso>/{uuid}/decano/aprobar` | Decano | Situaciones al cancelar (matrícula) o `aceptarEvaluacion` o `decisiones` por asignatura (asignatura). `observacion` opcional |
| RECHAZAR_DECANO | `POST <proceso>/{uuid}/decano/rechazar` | Decano | Nada: `observacion` opcional |
| ENVIAR_RESPUESTA | `POST <proceso>/{uuid}/funcionario/responder` | Funcionario Académico | En cancelaciones, escaneo de la Resolución subido antes. `observacion`: mensaje para el Estudiante, opcional salvo en Cancelación de Asignatura con rechazos del Decano; en ese proceso, además, `ajustes` (sección 3.7) |
| ENVIAR_RECIBO | `POST examenes-supletorios/{uuid}/funcionario/recibo` | Funcionario Académico | Recibo de pago subido antes |
| SUBIR_COMPROBANTE | `POST examenes-supletorios/{uuid}/estudiante/comprobante` | Estudiante | Comprobante de pago subido antes |
| APROBAR_COMPROBANTE | `POST examenes-supletorios/{uuid}/funcionario/comprobante/aprobar` | Funcionario Académico | Fecha acordada opcional |
| RECHAZAR_COMPROBANTE | `POST examenes-supletorios/{uuid}/funcionario/comprobante/rechazar` | Funcionario Académico | Observación |

La observación de una acción admite hasta 500 caracteres. Las observaciones
de `REMITIR_DECANO`, `APROBAR_DECANO` y `RECHAZAR_DECANO` son opcionales y
solo las ven el Funcionario Académico y el Decano: al Estudiante el historial
le entrega la fila `REMITIR_DECANO` con `observaciones` nulo y no le entrega
las filas `APROBAR_DECANO` ni `RECHAZAR_DECANO`, en ninguno de los tres
procesos. La de `ENVIAR_RESPUESTA` es el mensaje del Funcionario al
Estudiante y él sí la ve. La de `RECHAZAR_FUNCIONARIO` es obligatoria, es
para el Estudiante y también la ve en su historial.

Los endpoints cuyo cuerpo es opcional (`funcionario/responder`,
`decano/rechazar` y `decano/aprobar` del supletorio, `comprobante/aprobar`)
aceptan JSON, `{}` o ningún cuerpo sin `Content-Type`. Un POST sin cuerpo pero
con otro `Content-Type` (por ejemplo `application/x-www-form-urlencoded`, que
Windows PowerShell 5.1 manda por defecto) responde código 1 `Content-Type
'...' is not supported`.

Las acciones responden el detalle del proceso ya actualizado. Errores comunes
a todas:

| codigoError | Mensaje |
| --- | --- |
| 3 | `Solicitud de cancelación de matrícula con id <uuid> no fue encontrado en el sistema...` (o `Solicitud de cancelación de asignatura` o `Solicitud de examen supletorio`), también si el uuid es de otro proceso |
| 4 | `La acción <ACCION> no está permitida desde la etapa <ETAPA> en <proceso>...` |
| 4 | `La solicitud está en la etapa final <ETAPA> y no admite más acciones...` |
| 4 | `La acción <ACCION> exige <lo que falta>...`, donde lo que falta es `una observación`, `el escaneo de la Resolución`, `el recibo de pago` o `el comprobante de pago`, unidos con " y " |
| 4 | `El tipo de solicitud <nombre> no está asignado al funcionario académico <uuid>...` |
| 4 | `La solicitud <radicado> no pertenece al estudiante <uuid>...` |
| 6 | `La observación supera los 500 caracteres permitidos...` |

## 2. Objetos que se repiten

**AsignaturaDTORespuesta**: `uuidAsignatura`, `codigoAsignatura` (generado,
por ejemplo `ISI-001`), `nombreAsignatura`, `uuidProgramaAcademico` y
`programaAcademico` (nombre del programa) (texto).

**ProgramaAcademicoDTORespuesta**: `uuidProgramaAcademico`, `nombre`, `sigla`
(texto).

**AsignaturaMatriculadaDTORespuesta**: `uuidAsignaturaMatriculada`,
`uuidAsignatura`, `codigoAsignatura`, `nombreAsignatura`, `grupo`, `estado`
(texto; `activa`, `cancelada`, `aprobada` o `perdida`).

**EstudianteDTORespuesta**: `uuidUsuario`, `nombres`, `apellidos`,
`tipoDocumento`, `numeroDocumento`, `telefono`, `correoElectronico`,
`username`, `codigoEstudiantil`, `uuidProgramaAcademico`,
`programaAcademico` (nombre del programa), `semestre`, `facultad` (texto, siempre `FIET`), `estado` (booleano, usuario activo) y `asignaturasMatriculadas`
(lista de AsignaturaMatriculadaDTORespuesta). Nunca trae la contraseña.

**FuncionarioAcademicoDTORespuesta**: `uuidUsuario`, `nombres`, `apellidos`,
`tipoDocumento`, `numeroDocumento`, `telefono`, `correoElectronico`,
`username`, `cargo` (texto), `estado` (booleano) y `tiposSolicitud`
(lista de `{uuidTipoSolicitudAcademica, nombre}` que atiende).

**TipoSolicitudAcademicaDTORespuesta**: `uuidTipoSolicitudAcademica`,
`nombre`, `descripcion`, `uuidFuncionarioAcademico`,
`nombreFuncionarioAcademico` (nombres y apellidos) (texto).

**SolicitudAcademicaResumenDTORespuesta** (bandejas):
`uuidSolicitudAcademica`, `radicado`, `uuidTipoSolicitudAcademica`,
`tipoSolicitud` (nombre del tipo), `fechaCreacion` (fecha y hora),
`etiqueta`, `etapaCodigo`, `nombreEstudiante` (nombres y apellidos),
`codigoEstudiantil`, `numeroDocumento` (número de documento del estudiante;
llega en las bandejas del Funcionario Académico y del Decano y sale `null` en
la del Estudiante, P42) y `decisionDecano` (P47): `APROBADA`, `RECHAZADA`,
`APROBADA_PARCIAL` o `null`. Sale de la última acción `APROBAR_DECANO` o
`RECHAZAR_DECANO` del historial, así que se conserva después de que el
Funcionario responde (también en APROBADA o RECHAZADA, y en el supletorio
con el comprobante rechazado sigue siendo `APROBADA`). En Cancelación de
Asignatura, una aprobación con alguna asignatura rechazada por el Decano es
`APROBADA_PARCIAL`. Es `null` si el Decano no ha decidido o si el Funcionario
rechazó antes de remitir, y siempre `null` en la bandeja del Estudiante. Las
bandejas no tienen parámetros de búsqueda: el filtro se hace en el cliente.
`resultadosAsignaturas` (P48): lista de `{asignaturaSolicitudUuid, aprobada}`,
una por asignatura de la solicitud, ordenada por código de asignatura. Solo
llega en la bandeja del Funcionario Académico asignado, para Cancelación de
Asignatura en APROBADA o RECHAZADA; `aprobada` es `true` solo si
`aprobadaPorDecano` es verdadero (falso o `null`, por ejemplo con el rechazo
del Funcionario antes del Decano, es `false`). En los demás procesos, etapas y
en las bandejas del Estudiante y del Decano es `null`.

**SolicitudAcademicaDetalleDTORespuesta** (detalle común):

| Campo | Tipo | Descripción |
| --- | --- | --- |
| `uuidSolicitudAcademica`, `radicado`, `uuidTipoSolicitudAcademica`, `tipoSolicitud` | texto | |
| `fechaCreacion` | fecha y hora | |
| `etiqueta` | texto | Etiqueta de la etapa para el rol que consulta |
| `etapaCodigo` | texto | Código de la etapa (sección 1.5); al Estudiante, `EN_REVISION_DECANO` en lugar de la decisión del Decano |
| `numeroDocumento` | texto | Número de documento del estudiante; llega al Funcionario Académico y al Decano y sale `null` al Estudiante (P43). Viene también en el `solicitud` de los detalles de cada proceso |
| `codigoEstudiantil` | texto | Código estudiantil del estudiante dueño de la solicitud, para los tres roles. Viene también en el `solicitud` de los detalles de cada proceso |
| `estudiante` | objeto | `uuidUsuario`, `nombres`, `apellidos`, `codigoEstudiantil`, `programaAcademico`, `semestre`, `correoElectronico` |
| `anexos` | lista | AnexoAcademicoDTORespuesta, de la subida más antigua a la más reciente |
| `tieneResolucion` | booleano | Hay escaneo de la Resolución |
| `puedeDescargarResolucion` | booleano | Hay escaneo y el rol puede bajarlo (el Estudiante solo en etapa final) |
| `accionesDisponibles` | lista de texto | Códigos de la sección 1.6 que el rol puede ejecutar ahora |

**AnexoAcademicoDTORespuesta**: `uuidAnexoAcademico`, `nombreArchivo`
(nombre original), `uuidTipoAnexoAcademico` y `tipoAnexo` (nulos en un
soporte libre), `tipoArchivo` (`application/pdf`, `image/jpeg` o
`image/png`), `tamanioBytes` (número), `fechaSubida` (fecha y hora). Nunca
trae la ruta en disco.

**HistorialSolicitudAcademicaDTORespuesta**: `accion` (código de la sección
1.6 o `RADICAR`), `etapaCodigo` (etapa a la que llevó esa acción; nulo si no
encaja en el recorrido), `observaciones`, `fecha` (fecha y hora),
`nombresUsuario`, `apellidosUsuario`. Para el Estudiante, `observaciones`
llega nulo en la fila `REMITIR_DECANO` y las filas `APROBAR_DECANO` y
`RECHAZAR_DECANO` no vienen en la lista; las demás filas conservan su
`etapaCodigo` y ninguna trae `APROBADA_POR_DECANO` ni `RECHAZADA_POR_DECANO`,
así que la última fila coincide con el `etapaCodigo` del detalle. El Estudiante se entera de la decisión del Decano por la fila
`ENVIAR_RESPUESTA` (en el supletorio aprobado, por `ENVIAR_RECIBO`). El
Funcionario Académico y el Decano reciben todas las filas.

**SituacionAsignaturaDTORespuesta**: `uuidSituacionAcademica`, `codigo`
(R0 a R3), `nombre`: R0 No cursada, R1 Cursada por primera vez, R2 Cursada
por segunda vez, R3 Cursada por tercera vez.

## 3. Endpoints

### 3.1 Asignaturas (7)

| Método y ruta | Roles | Parámetros | Cuerpo | Respuesta |
| --- | --- | --- | --- | --- |
| `POST asignaturas` | Secretario General, Decano | | `{nombreAsignatura, uuidProgramaAcademico}` | AsignaturaDTORespuesta con el código generado |
| `POST asignaturas/cargar/archivo` | Secretario General, Decano | | Multipart, parte `file` (.xlsx) | Lista de AsignaturaDTORespuesta creadas, en el orden del archivo |
| `GET asignaturas/paginado` | Secretario General, Decano, Funcionario Académico | `pagina`, `tamanio` | | Página de AsignaturaDTORespuesta, por sigla del programa y consecutivo |
| `GET asignaturas/filtro` | Secretario General, Decano, Funcionario Académico | `texto` (opcional; busca en código y nombre, sin distinguir mayúsculas), `uuidProgramaAcademico` (opcional), `pagina`, `tamanio` | | Página de AsignaturaDTORespuesta, en el mismo orden |
| `GET asignaturas/programa/{uuidPrograma}` | Secretario General, Decano, Funcionario Académico | | | Lista sin paginar de AsignaturaDTORespuesta del programa, por consecutivo (`ISI-999` antes de `ISI-1000`) |
| `GET asignaturas/{uuidAsignatura}` | Secretario General, Decano, Funcionario Académico | | | AsignaturaDTORespuesta |
| `PUT asignaturas/{uuidAsignatura}` | Secretario General, Decano | | `{nombreAsignatura}` | AsignaturaDTORespuesta |

Cuerpo de `POST asignaturas`: `nombreAsignatura` obligatorio, máximo 150;
`uuidProgramaAcademico` obligatorio, máximo 100 (400 si no se cumple). El
cuerpo no lleva código: el backend lo genera con la sigla del programa, un
guion y el siguiente consecutivo del programa con al menos tres dígitos
(`ISI-001`, ..., `ISI-999`, `ISI-1000`). El código nunca se edita ni se
reutiliza. El nombre se guarda sin espacios sobrantes y no se repite dentro
del mismo programa sin distinguir mayúsculas ni espacios.

`PUT asignaturas/{uuid}`: `nombreAsignatura` obligatorio, máximo 150. Solo
cambia el nombre; si el cuerpo trae `codigoAsignatura` o
`uuidProgramaAcademico`, se ignoran.

Excel de asignaturas: hoja 1, fila 1 con los encabezados `nombreAsignatura,
programaAcademico`, en ese orden. `programaAcademico` es el nombre exacto de
uno de los cinco programas (sección 3.4), sin distinguir mayúsculas ni
espacios al inicio o al final. Termina en la primera fila vacía. Es todo o
nada: si una fila falla no se crea ninguna. Hay una plantilla en
`backend/pruebas/plantilla-asignaturas.xlsx`.

Errores:

- 400: mapa `{"campo": "mensaje"}` por las anotaciones del cuerpo; en la
  carga, `{"campo": "Fila <n>, columna <letra> (<campo>): <mensaje>"}` con
  `El nombre de la asignatura no puede estar vacío`, `El nombre de la
  asignatura debe tener máximo 150 caracteres` o `El programa académico no
  puede estar vacío`.
- 2: `La asignatura <nombre> ya existe en el programa <programa> con el
  código <codigo>...`; en la carga, `Fila <n>, columna A (nombreAsignatura):
  la asignatura <nombre> ya existe en el programa <programa> con el código
  <codigo>...`.
- 3: `Asignatura con id <uuid> no fue encontrado en el sistema...`;
  `Programa académico con id <uuid> no fue encontrado en el sistema...`
  (al crear, en `filtro` y en `programa/{uuidPrograma}`).
- 6: `El nombre de la asignatura no puede estar vacío...`; `El nombre de la
  asignatura supera los 150 caracteres permitidos...`; `Debe indicar el
  programa académico...`; `La petición trae la asignatura <nombre> más de
  una vez...`; paginación inválida.
- 6 en la carga: `El archivo debe ser un Excel .xlsx valido con una
  hoja...`; `Fila 1, columna <letra>: se esperaba el encabezado <campo> y se
  encontró '<valor>'...`; `El archivo no tiene filas de datos despues del
  encabezado...`; `Fila <n>, columna B (programaAcademico): el programa
  '<valor>' no existe, los programas permitidos son <lista>...`; `Fila <n>,
  columna A (nombreAsignatura): la asignatura <nombre> ya aparece en la fila
  <m> para el programa <programa>...`.

### 3.2 Estudiantes (9)

| Método y ruta | Roles | Parámetros | Cuerpo | Respuesta |
| --- | --- | --- | --- | --- |
| `POST estudiantes` | Secretario General, Decano | | EstudianteDTOPeticion | EstudianteDTORespuesta |
| `POST estudiantes/cargar/archivo` | Secretario General, Decano | | Multipart, parte `file` (.xlsx) | Lista de EstudianteDTORespuesta |
| `GET estudiantes/paginado` | Secretario General, Decano, Funcionario Académico | `pagina`, `tamanio` | | Página de EstudianteDTORespuesta, por código estudiantil |
| `GET estudiantes/filtro` | Secretario General, Decano, Funcionario Académico | `nombre`, `apellido`, `codigo` (opcionales; contienen, sin distinguir mayúsculas), `pagina`, `tamanio` | | Página de EstudianteDTORespuesta |
| `GET estudiantes/mis-asignaturas` | Estudiante | | | Lista de AsignaturaMatriculadaDTORespuesta activas del estudiante autenticado, por código |
| `GET estudiantes/{uuidEstudiante}` | Secretario General, Decano, Funcionario Académico | | | EstudianteDTORespuesta |
| `PUT estudiantes/{uuidEstudiante}` | Secretario General, Decano | | `{nombres, apellidos, tipoDocumento, numeroDocumento, telefono, correoElectronico, codigoEstudiantil, semestre}`, todos opcionales; los vacíos no cambian | EstudianteDTORespuesta |
| `POST estudiantes/{uuidEstudiante}/asignaturas` | Secretario General, Decano | | `{codigoAsignatura, grupo}` | AsignaturaMatriculadaDTORespuesta, en estado `activa` |
| `PATCH estudiantes/{uuidEstudiante}/asignaturas/{uuidMatricula}/estado` | Secretario General, Decano | | `{estado}` | AsignaturaMatriculadaDTORespuesta |

EstudianteDTOPeticion: `nombres`, `apellidos` (obligatorios, máximo 1000),
`tipoDocumento` (`Cédula de ciudadanía`, `Tarjeta de identidad` o `Cédula de
extranjería`, sin distinguir mayúsculas), `numeroDocumento` (5 a 255),
`telefono` (5 a 1000), `correoElectronico` (correo válido), `username` (5 a
255), `password` (5 a 255), `codigoEstudiantil` (máximo 45),
`uuidProgramaAcademico` (obligatorio, máximo 100), `semestre` (máximo 10),
`asignaturas` (al menos una: `{codigoAsignatura, grupo}`; código máximo 45 y
grupo máximo 20). El rol, el tipo de usuario y la facultad (`FIET`) los
asigna el backend; si llega `facultad`, se ignora.

Edición (`PUT estudiantes/{uuid}`): cada campo es opcional y uno vacío o
ausente no cambia. Mismos límites que al crear (`tipoDocumento` y `telefono`
de 5 a 1000, `numeroDocumento` de 5 a 255, correo válido). `username`,
programa y facultad son de solo lectura: si llegan, se ignoran. Documento,
correo y código estudiantil no pueden ser de otro usuario o estudiante
(código 2); el propio registro no cuenta como repetido. Nombres, apellidos y
datos de contacto se guardan en `usuarios` y `usuariosLivianos`.

El programa se elige al crear el estudiante y no se cambia después: `PUT`
no lo recibe (si llega, se ignora). Cada `codigoAsignatura` se busca en el
catálogo sin distinguir mayúsculas ni espacios y debe ser una asignatura del
programa del estudiante; ningún endpoint de estudiantes crea asignaturas.
La misma regla aplica a `POST estudiantes/{uuid}/asignaturas`.

Excel de estudiantes: hoja 1, fila 1 con estos encabezados en este orden:
`nombres, apellidos, tipoDocumento, numeroDocumento, telefono,
correoElectronico, username, password, codigoEstudiantil, programaAcademico,
semestre, codigoAsignatura, grupo` (13 columnas, sin `facultad`). `programaAcademico` es el
nombre exacto de uno de los cinco programas, sin distinguir mayúsculas. Una
fila por asignatura matriculada; las filas del mismo `numeroDocumento` se
juntan en un estudiante y deben repetir los mismos datos, programa incluido.
Termina en la primera fila vacía. Hay una plantilla en
`backend/pruebas/plantilla-estudiantes.xlsx`.

Errores principales:

- 400 en la carga: mapa `{"campo": "Fila <n>, columna <letra> (<campo>): <mensaje>"}`;
  un programa vacío sale en la columna J (`programaAcademico`).
- 2: `Estudiante con codigo estudiantil: <x> existe en el sistema...`, `Usuario
  con numero de documento: <x> existe...`, `... correo electronico ...`,
  `... username ...`.
- 3: `Estudiante con id <uuid> no fue encontrado en el sistema...`; en
  `mis-asignaturas`, `Estudiante con username: <x> no fue encontrado en el
  sistema...`; `Asignatura matriculada con id <uuid> no fue encontrado...`;
  `Programa académico con id <uuid> no fue encontrado en el sistema...`;
  `Asignatura con codigo: <codigo> no fue encontrado en el sistema...`.
- 4: `La asignatura <codigo> (<nombre>) es del programa <programa de la
  asignatura> y el estudiante es del programa <programa del estudiante>...`.
- 6: `La petición trae <dato> <valor> más de una vez...`; `El estudiante ya
  tiene activa la asignatura <codigo> en el grupo <grupo>...`; `El estado <x>
  no es valido, los estados permitidos son activa, cancelada, aprobada,
  perdida...`; `No se puede cambiar la asignatura matriculada de <actual> a
  <nuevo>...` (solo se cambia desde `activa`); `Tipo de documento invalido...`;
  `Debe indicar el programa académico...`.
- 6 en la carga: `El archivo debe ser un Excel .xlsx valido con una hoja...`;
  `Fila 1, columna <letra>: se esperaba el encabezado <campo> y se encontró
  '<valor>'...`; `El archivo no tiene filas de estudiantes despues del
  encabezado...`; `Fila <n>, columna J (programaAcademico): el programa
  '<valor>' no existe, los programas permitidos son <lista>...`; `Fila <n>,
  columna L (codigoAsignatura): la asignatura con código <codigo> no existe
  en el catálogo...`; `Fila <n>, columna L (codigoAsignatura): la asignatura
  <codigo> es del programa <programa> y el estudiante es del programa
  <programa>...`; `Fila <n>, columna <letra> (<campo>): el estudiante con
  documento <doc> tiene un valor distinto al de la fila <m>...` (también si
  cambia el programa); `... la asignatura <codigo> en el grupo <grupo> ya
  aparece en la fila <m> para el mismo estudiante...`; `... el valor <x> ya
  lo usa otro estudiante en la fila <m>...`.

### 3.3 Funcionarios académicos (6)

| Método y ruta | Roles | Parámetros | Cuerpo | Respuesta |
| --- | --- | --- | --- | --- |
| `POST funcionarios-academicos` | Secretario General, Decano | | FuncionarioAcademicoDTOPeticion | FuncionarioAcademicoDTORespuesta |
| `POST funcionarios-academicos/cargar/archivo` | Secretario General, Decano | | Multipart, parte `file` (.xlsx) | Lista de FuncionarioAcademicoDTORespuesta |
| `GET funcionarios-academicos/paginado` | Secretario General, Decano, Funcionario Académico | `pagina`, `tamanio` | | Página, por apellidos y nombres |
| `GET funcionarios-academicos/filtro` | Secretario General, Decano, Funcionario Académico | `nombre`, `apellido`, `cargo` (opcionales; contienen, sin distinguir mayúsculas), `pagina`, `tamanio` | | Página |
| `GET funcionarios-academicos/{uuidFuncionario}` | Secretario General, Decano, Funcionario Académico | | | FuncionarioAcademicoDTORespuesta |
| `PUT funcionarios-academicos/{uuidFuncionario}` | Secretario General, Decano | | `{nombres, apellidos, tipoDocumento, numeroDocumento, telefono, correoElectronico, cargo}`, todos opcionales; los vacíos no cambian | FuncionarioAcademicoDTORespuesta |

FuncionarioAcademicoDTOPeticion: los mismos datos personales y de acceso que
el estudiante (`nombres`, `apellidos`, `tipoDocumento`, `numeroDocumento`,
`telefono`, `correoElectronico`, `username`, `password`) más `cargo`
(obligatorio al crear, máximo 100; antes se llamaba `dependencia`). El
backend le asigna el rol `Funcionario Académico` y el tipo `Empleado FIET -
Funcionario`. La edición sigue las mismas reglas que la del estudiante:
campos opcionales, `username` de solo lectura y documento y correo únicos sin
contar al propio registro (código 2).

Excel: encabezados `nombres, apellidos, tipoDocumento, numeroDocumento,
telefono, correoElectronico, username, password, cargo`; una fila por
funcionario. Plantilla en
`backend/pruebas/plantilla-funcionarios-academicos.xlsx`.

Errores: 400 como en estudiantes; 2 por documento, correo o username en la
base; 3 `Funcionario Académico con id <uuid> no fue encontrado en el
sistema...`; 6 por repetidos dentro de la petición, archivo no válido,
encabezado distinto, `El archivo no tiene filas de datos despues del
encabezado...` y `Fila <n>, columna <letra> (<campo>): el valor <x> ya
aparece en la fila <m> del archivo...`.

### 3.4 Catálogos académicos (7)

| Método y ruta | Roles | Parámetros | Cuerpo | Respuesta |
| --- | --- | --- | --- | --- |
| `GET catalogos-academicos/tipos-solicitud` | Cualquier usuario autenticado | | | Lista de TipoSolicitudAcademicaDTORespuesta, por nombre |
| `GET catalogos-academicos/tipos-solicitud/{uuidTipo}/etapas` | Cualquier usuario autenticado | | | Lista de `{uuidEtapa, codigo, uuidTipoSolicitudAcademica}` (universales y propias del tipo), por código |
| `GET catalogos-academicos/tipos-solicitud/{uuidTipo}/tipos-anexo` | Cualquier usuario autenticado | | | Lista de `{uuidTipoAnexoAcademico, uuidTipoSolicitudAcademica, nombre, formatosPermitidos, obligatorio}`, por nombre |
| `GET catalogos-academicos/etiquetas` | Cualquier usuario autenticado | `rol`: `ESTUDIANTE`, `FUNCIONARIO` o `DECANO` (sin distinguir mayúsculas) | | Lista de `{uuidEtapa, codigoEtapa, rol, etiqueta}`, por código de etapa |
| `GET catalogos-academicos/situaciones` | Cualquier usuario autenticado | | | Lista de SituacionAsignaturaDTORespuesta, por código |
| `GET catalogos-academicos/programas` | Cualquier usuario autenticado | | | Lista de ProgramaAcademicoDTORespuesta de los cinco programas, por nombre |
| `PUT catalogos-academicos/tipos-solicitud/{uuidTipo}/funcionario` | Secretario General, Decano | | `{funcionarioUuid}` (obligatorio, máximo 100) | TipoSolicitudAcademicaDTORespuesta con el nuevo responsable |

Errores: 3 `Tipo de solicitud académica con id <uuid> no fue encontrado en el
sistema...`; 3 `Funcionario Académico con id <uuid> no fue encontrado...` (el
uuid no tiene fila de funcionario académico); 6 `El rol <x> no es valido, los
roles permitidos son ESTUDIANTE, FUNCIONARIO, DECANO...`; 400 si
`funcionarioUuid` llega vacío.

### 3.5 Solicitudes académicas (9)

Endpoints comunes a los tres procesos.

| Método y ruta | Roles | Parámetros | Cuerpo | Respuesta |
| --- | --- | --- | --- | --- |
| `GET solicitudes-academicas/estudiante` | Estudiante | | | Lista de SolicitudAcademicaResumenDTORespuesta propias |
| `GET solicitudes-academicas/funcionario` | Funcionario Académico | | | Lista de las solicitudes de los tipos asignados a él |
| `GET solicitudes-academicas/decano` | Decano | | | Lista de todas las solicitudes con etiqueta para el Decano (no trae RADICADA) |
| `GET solicitudes-academicas/{uuidSolicitud}` | Estudiante, Funcionario Académico, Decano | | | SolicitudAcademicaDetalleDTORespuesta |
| `GET solicitudes-academicas/{uuidSolicitud}/historial` | Estudiante, Funcionario Académico, Decano | | | Lista de HistorialSolicitudAcademicaDTORespuesta, de la más antigua a la más reciente |
| `POST solicitudes-academicas/{uuidSolicitud}/anexos` | Estudiante, Funcionario Académico | | Multipart: `archivo` (obligatorio) y `tipoAnexo` (uuid del tipo de anexo; vacío o ausente = soporte libre) | AnexoAcademicoDTORespuesta |
| `GET solicitudes-academicas/{uuidSolicitud}/anexos/{uuidAnexo}` | Estudiante, Funcionario Académico, Decano | | | Archivo |
| `POST solicitudes-academicas/{uuidSolicitud}/resolucion` | Funcionario Académico | | Multipart: `archivo` (PDF) | `{uuidSolicitudAcademica, nombreArchivo, fechaSubida}` |
| `GET solicitudes-academicas/{uuidSolicitud}/resolucion` | Estudiante, Funcionario Académico, Decano | | | Archivo PDF |

Las bandejas no se paginan y van de la más reciente a la más antigua.

Rol con que actúa el usuario en una solicitud: Estudiante si es el dueño, si
no Funcionario Académico si el tipo está asignado a él, si no Decano si
tiene ese rol. Con ese rol se filtra lo que ve; si no le corresponde ninguno o
la etapa no tiene etiqueta para su rol, responde código 3 como inexistente.

Subida de anexos (código 4 salvo que se indique):

- Quién y cuándo: los anexos del Estudiante y los soportes libres, solo el
  Estudiante dueño en RADICADA; el recibo de pago, solo el Funcionario
  asignado en APROBADA_POR_DECANO; el comprobante de pago, solo el Estudiante
  dueño en PENDIENTE_PAGO.
  - `El anexo <nombre> solo lo puede subir el rol <ESTUDIANTE|FUNCIONARIO>...`
  - `El anexo <nombre> solo se puede subir en la etapa <ETAPA> y la solicitud
    está en <ETAPA>...`
- `El tipo de solicitud Examen Supletorio no admite soportes libres...`
- `El tipo de anexo <nombre> no pertenece al tipo de solicitud <tipo>...`
- Código 3 `Tipo de anexo académico con id <uuid> no fue encontrado...`.
- Errores de archivo de la sección 1.4.
- Código 1 `No se pudo guardar el archivo del anexo...`.

Escaneo de la Resolución (código 4 salvo que se indique):

- Solo en las cancelaciones: `El tipo de solicitud Examen Supletorio no
  produce Resolución, solo la producen las cancelaciones de matrícula y de
  asignatura...`
- Lo sube el Funcionario asignado en RADICADA, APROBADA_POR_DECANO o
  RECHAZADA_POR_DECANO. Subirlo otra vez reemplaza el anterior.
  - `El escaneo de la Resolución solo se sube en las etapas RADICADA,
    APROBADA_POR_DECANO, RECHAZADA_POR_DECANO y la solicitud está en
    <ETAPA>...`
  - `La solicitud <radicado> está en la etapa final <ETAPA> y su Resolución ya
    no se puede subir ni reemplazar...`
- Remitir al Decano retira el escaneo que hubiera.
- Descarga del Estudiante antes de APROBADA o RECHAZADA: `La Resolución de la
  solicitud <radicado> estará disponible cuando la solicitud termine...`
- Código 3 `Resolución académica con id <uuid> no fue encontrado...` si no
  hay escaneo.

Descarga de anexos: código 3 `Anexo académico con id <uuid> no fue encontrado
en el sistema...` si el anexo no es de esa solicitud.

### 3.6 Cancelaciones de matrícula (8)

| Método y ruta | Roles | Cuerpo | Respuesta |
| --- | --- | --- | --- |
| `GET cancelaciones-matricula/formulario` | Estudiante | | `{codigoEstudiantil, anexosRequeridos: [{uuidTipoAnexoAcademico, nombre, formatosPermitidos, obligatorio}], asignaturas: [{codigoAsignatura, nombreAsignatura}]}` |
| `POST cancelaciones-matricula` | Estudiante | Multipart (ver abajo) | `{uuidSolicitudAcademica, radicado}` |
| `GET cancelaciones-matricula/{uuidSolicitud}` | Estudiante, Funcionario Académico, Decano | | CancelacionMatriculaDetalle |
| `POST cancelaciones-matricula/{uuidSolicitud}/funcionario/rechazar` | Funcionario Académico | `{observacion}` | CancelacionMatriculaDetalle |
| `POST cancelaciones-matricula/{uuidSolicitud}/funcionario/remitir` | Funcionario Académico | `{observacion, evaluaciones}` | CancelacionMatriculaDetalle |
| `POST cancelaciones-matricula/{uuidSolicitud}/funcionario/responder` | Funcionario Académico | `{observacion}` opcional (mensaje al Estudiante, máximo 500), `{}` o sin cuerpo | CancelacionMatriculaDetalle |
| `POST cancelaciones-matricula/{uuidSolicitud}/decano/aprobar` | Decano | `{situaciones, observacion}`; `observacion` opcional | CancelacionMatriculaDetalle |
| `POST cancelaciones-matricula/{uuidSolicitud}/decano/rechazar` | Decano | `{observacion}` opcional, `{}` o sin cuerpo | CancelacionMatriculaDetalle |

El formulario lista los seis anexos obligatorios y las asignaturas activas
del estudiante; la solicitud incluye todas esas asignaturas.

Radicación (`multipart/form-data`):

- `motivo`: texto obligatorio, máximo 255.
- Una parte de archivo por anexo, cuyo nombre es el `uuidTipoAnexoAcademico`
  del formulario. Los seis son obligatorios.
- Partes `soporte` para soportes libres opcionales (pdf, jpg, jpeg, png).

Errores de la radicación, en el orden en que se revisan:

| codigoError | Mensaje |
| --- | --- |
| 4 | `El usuario <uuid> no tiene el rol Estudiante...` (también si no tiene fila de estudiante) |
| 4 | `El estudiante <uuid> no tiene asignaturas activas para cancelar...` |
| 4 | `El motivo de la cancelación es obligatorio...` |
| 6 | `El motivo de la cancelación supera los 255 caracteres permitidos...` |
| 4 | `El tipo de anexo <nombre> no pertenece al tipo de solicitud Cancelación de Matrícula...` |
| 4 | `Falta el anexo obligatorio: <nombres separados por "; ">...` |
| 6 | Errores de archivo de la sección 1.4 |
| 4 | `Ya tienes una solicitud de Cancelación de Matrícula en curso (<radicado>), debe terminar antes de radicar otra...` |
| 4 | `No se pudo generar el radicado, intente de nuevo...` |

**CancelacionMatriculaDetalle**: `solicitud`
(SolicitudAcademicaDetalleDTORespuesta), `motivoCancelacion` y `asignaturas`,
una por cada fila de la solicitud: `uuidAsignaturaSolicitud`,
`codigoAsignatura`, `nombreAsignatura`, `numeroFaltas` (texto o nulo),
`nota` (texto o nulo), `situacionMatricula` y `situacionCancelar`
(SituacionAsignaturaDTORespuesta o nulo).

Cuerpos de las acciones:

- `remitir`: `observacion` opcional y `evaluaciones`, una por asignatura de la
  solicitud: `{asignaturaSolicitudUuid, numeroFaltas, nota,
  situacionMatriculaUuid, situacionCancelarUuid}`. `numeroFaltas` y `nota` se envían como texto o
  como número y se guardan como texto de hasta 20 caracteres: si son
  numéricos, `numeroFaltas` debe ser entero mayor o igual a 0 y `nota` de 0.0
  a 5.0 con un decimal, con punto o coma (se guarda con punto, `3,5` queda
  `3.5`); si no son numéricos (por ejemplo `Sin registro`) se aceptan tal
  cual. `situacionMatriculaUuid` sale de `GET catalogos-academicos/situaciones`.
  `situacionCancelarUuid` es opcional (P44): si llega se valida contra el
  mismo catálogo y se guarda como situación al cancelar; el Decano la
  confirma o la cambia al aprobar.
  La `observacion` de la remisión es la única observación general del cuerpo,
  va para el Decano y el Estudiante no la ve.
- `aprobar`: `situaciones`, una por asignatura:
  `{asignaturaSolicitudUuid, situacionCancelarUuid}`, y `observacion`
  opcional.
- `rechazar` del Funcionario: `observacion` obligatoria y escaneo de la
  Resolución, subido antes con `POST solicitudes-academicas/{uuid}/resolucion`.
  `rechazar` del Decano: `observacion` opcional.
- `responder`: `observacion` opcional (máximo 500), mensaje para el Estudiante
  que queda en el historial; exige el escaneo de la Resolución. Desde
  APROBADA_POR_DECANO lleva a APROBADA y las asignaturas de la solicitud que
  sigan activas pasan a `cancelada`; desde RECHAZADA_POR_DECANO lleva a
  RECHAZADA.

Errores de las evaluaciones y situaciones (además de los de la sección 1.6):

| codigoError | Mensaje |
| --- | --- |
| 4 | `Faltan datos de las asignaturas de la solicitud: <nombres separados por "; ">...` |
| 4 | `La asignatura <nombre> viene más de una vez...` |
| 4 | `La asignatura <uuid> no pertenece a la solicitud <radicado>...` |
| 4 | `Falta <el número de faltas, la nota, la situación en la matrícula o la situación al cancelar> de la asignatura <nombre>...` |
| 6 | `El número de faltas de la asignatura <nombre> debe ser un entero mayor o igual a 0...` |
| 6 | `La nota de la asignatura <nombre> debe estar entre 0.0 y 5.0 con un decimal...` |
| 3 | `Situación académica de asignatura con id <uuid> no fue encontrado en el sistema...` |

### 3.7 Cancelaciones de asignatura (8)

| Método y ruta | Roles | Cuerpo | Respuesta |
| --- | --- | --- | --- |
| `GET cancelaciones-asignatura/formulario` | Estudiante | | `{codigoEstudiantil, asignaturas: [{uuidAsignaturaMatriculada, codigoAsignatura, nombreAsignatura, grupo}], soportes: [{uuidTipoAnexoAcademico: null, nombre: "Soporte libre", formatosPermitidos: "pdf,jpg,jpeg,png", tamanioMaximoBytes: 5242880, obligatorio: false}]}` |
| `POST cancelaciones-asignatura` | Estudiante | Multipart (ver abajo) | `{uuidSolicitudAcademica, radicado}` |
| `GET cancelaciones-asignatura/{uuidSolicitud}` | Estudiante, Funcionario Académico, Decano | | CancelacionAsignaturaDetalle |
| `POST cancelaciones-asignatura/{uuidSolicitud}/funcionario/rechazar` | Funcionario Académico | `{observacion}` | CancelacionAsignaturaDetalle |
| `POST cancelaciones-asignatura/{uuidSolicitud}/funcionario/remitir` | Funcionario Académico | `{observacion, evaluaciones}` | CancelacionAsignaturaDetalle |
| `POST cancelaciones-asignatura/{uuidSolicitud}/funcionario/responder` | Funcionario Académico | `{observacion, ajustes}` (ver abajo), `{}` o sin cuerpo | CancelacionAsignaturaDetalle |
| `POST cancelaciones-asignatura/{uuidSolicitud}/decano/aprobar` | Decano | `{aceptarEvaluacion}` o `{decisiones}`, con `observacion` opcional | CancelacionAsignaturaDetalle |
| `POST cancelaciones-asignatura/{uuidSolicitud}/decano/rechazar` | Decano | `{observacion}` opcional, `{}` o sin cuerpo | CancelacionAsignaturaDetalle |

Radicación (`multipart/form-data`):

- `motivo`: texto obligatorio, máximo 255.
- `asignaturas`: un campo de texto por cada `uuidAsignaturaMatriculada`
  elegido del formulario; al menos uno.
- Partes `soporte` para soportes libres opcionales. Una parte de archivo con
  cualquier otro nombre se toma como tipo de anexo y se rechaza.

Errores de la radicación, en orden:

| codigoError | Mensaje |
| --- | --- |
| 4 | `El usuario <uuid> no tiene el rol Estudiante...` |
| 4 | `El motivo de la cancelación es obligatorio...` |
| 6 | `El motivo de la cancelación supera los 255 caracteres permitidos...` |
| 4 | `Debe elegir al menos una asignatura para cancelar...` |
| 4 | `La asignatura <uuid> no es una asignatura matriculada del estudiante <uuid>...` (también para una asignatura de otro estudiante) |
| 4 | `La asignatura <codigo - nombre> viene más de una vez...` |
| 4 | `La asignatura <codigo - nombre> está <estado> y solo se pueden cancelar asignaturas activas...` |
| 4 | `La cancelación de asignatura solo admite soportes libres, sin tipo de anexo, y llegó uno de tipo <nombre de la parte>...` |
| 6 | Errores de archivo de la sección 1.4 |
| 4 | `Ya tienes una solicitud de Cancelación de Asignatura en curso (<radicado>), debe terminar antes de radicar otra...` |

**CancelacionAsignaturaDetalle**: `solicitud`, `motivoCancelacion` y
`asignaturas` con `uuidAsignaturaSolicitud`, `codigoAsignatura`,
`nombreAsignatura`, `numeroFaltas`, `nota`, `situacionMatricula`,
`cumpleCondiciones` (booleano), `observacionEvaluacion`, `aprobadaPorDecano`
(booleano), `observacionDecision`, `decisionCambiadaPorDecano` (booleano) y
`situacionCancelar`. `decisionCambiadaPorDecano` se calcula, no se guarda: es
`true` cuando `aprobadaPorDecano` es distinto de `cumpleCondiciones`, `false`
cuando coinciden y `null` mientras el Decano no decide. Lo que llega depende
del rol (sección 5).

Cuerpos de las acciones:

- `remitir`: `observacion` opcional y `evaluaciones`, una por asignatura:
  `{asignaturaSolicitudUuid, numeroFaltas, nota, situacionMatriculaUuid,
  situacionCancelarUuid, cumpleCondiciones, observacionEvaluacion}`. Faltas,
  nota y `situacionCancelarUuid` (opcional, P44) como en Cancelación de
  Matrícula. `cumpleCondiciones` es obligatorio; con una nota
  numérica menor a 3.0 no puede ser `true` (una nota de texto no tiene esa
  restricción). `observacionEvaluacion` es opcional (máximo 255) aunque
  no cumpla (P46). Al menos una asignatura debe cumplir.
- `aprobar` (P14): una de dos formas, nunca las dos ni ninguna.
  - `{aceptarEvaluacion: true}`: cada asignatura queda con `aprobadaPorDecano`
    igual a su `cumpleCondiciones`.
  - `{decisiones}`: una por asignatura, `{asignaturaSolicitudUuid, aprobada}`,
    con `aprobada` obligatorio. El Decano puede aprobar una que el
    Funcionario marcó como que no cumple y rechazar una que marcó como que
    cumple. Al menos una debe aprobarse; para negarlas todas se usa
    `decano/rechazar`.

  El cuerpo ya no lleva `situacionCancelarUuid` ni `observacionDecision`. La
  `observacion` general es opcional y solo la ven el Funcionario y el Decano.
  Las rechazadas pierden la situación al cancelar; las aprobadas conservan la
  que dejó el Funcionario al remitir (P44, P9).
- `rechazar`: como en Cancelación de Matrícula.
- `responder` (P45): `observacion` (máximo 500, la ve el Estudiante) y
  `ajustes`, una entrada por asignatura `{asignaturaSolicitudUuid,
  situacionCancelarUuid, observacionDecision}` (observación de máximo 255).
  Reglas:
  - Cada asignatura aprobada con `decisionCambiadaPorDecano` necesita su
    ajuste. Las demás pueden traer uno, con las mismas reglas; una rechazada
    no necesita ajuste aunque el Decano haya cambiado la evaluación (P47).
  - Desde APROBADA_POR_DECANO, toda asignatura aprobada por el Decano debe
    tener situación al cancelar: la guardada al remitir o el
    `situacionCancelarUuid` de su ajuste, que la reemplaza. Si falta, código
    4 con el nombre de la asignatura, haya cambiado o no la decisión. En el
    ajuste de una aprobada `observacionDecision` es opcional.
  - En el ajuste de una rechazada `observacionDecision` es opcional (máximo
    255, P47) y no acepta situación. Si llega, la ve el Estudiante en
    APROBADA o RECHAZADA.
  - Si el Decano rechazó alguna asignatura (`aprobadaPorDecano` falso,
    coincida o no con la evaluación), o la solicitud completa, la
    `observacion` general es obligatoria.
  - Desde RECHAZADA_POR_DECANO no se aceptan ajustes.

  Desde APROBADA_POR_DECANO cancela solo las asignaturas aprobadas que sigan
  activas.

Errores propios de esta evaluación y decisión (además de los de la tabla de
la sección 3.6 y de la 1.6):

| codigoError | Mensaje |
| --- | --- |
| 4 | `Falta la confirmación de las condiciones de la asignatura <nombre>...` |
| 4 | `La asignatura <nombre> tiene nota <nota>, menor a 3.0, y no puede marcarse como que cumple las condiciones...` |
| 4 | `Ninguna asignatura cumple las condiciones: en lugar de remitir la solicitud al Decano debe rechazarla...` |
| 4 | `Debe aceptar la evaluación del funcionario (aceptarEvaluacion) o enviar la decisión de cada asignatura (decisiones), una de las dos...` |
| 4 | `Faltan datos de las asignaturas de la solicitud: <nombres>...` (falta la decisión de alguna asignatura) |
| 4 | `Falta la decisión del Decano de la asignatura <nombre>...` (`aprobada` nulo) |
| 4 | `No se aprobó ninguna asignatura: para negar la solicitud completa use el rechazo del Decano...` |
| 4 | `El Decano cambió la evaluación de la asignatura <nombre> y falta su ajuste...` (solo una aprobada) |
| 4 | `Falta la situación al cancelar de la asignatura <nombre>...` (aprobada sin situación guardada ni en su ajuste) |
| 4 | `La asignatura <nombre> fue rechazada por el Decano y no lleva situación al cancelar...` |
| 4 | `La solicitud <radicado> fue rechazada por el Decano y no admite ajustes por asignatura...` |
| 4 | `El Decano rechazó la solicitud o alguna de sus asignaturas: la respuesta al estudiante requiere una observación...` |
| 4 | `La asignatura <uuid> no pertenece a la solicitud <radicado>...` y `La asignatura <nombre> viene más de una vez...` (decisiones y ajustes) |
| 6 | `La observación de la evaluación de la asignatura <nombre> supera los 255 caracteres permitidos...` (o `La observación de la decisión`) |
| 6 | `El número de faltas de la asignatura <nombre> es numérico y debe ser un entero mayor o igual a 0; si no es un número se guarda como texto...` |
| 6 | `La nota de la asignatura <nombre> es numérica y debe estar entre 0.0 y 5.0 con un decimal, con punto o coma; si no es un número se guarda como texto...` |
| 6 | `En la asignatura <nombre>, el número de faltas supera los 20 caracteres permitidos...` (o `la nota`) |

### 3.8 Exámenes supletorios (12)

| Método y ruta | Roles | Cuerpo | Respuesta |
| --- | --- | --- | --- |
| `GET examenes-supletorios/formulario` | Estudiante | | FormularioSupletorio, con `codigoEstudiantil` |
| `POST examenes-supletorios` | Estudiante | Multipart (ver abajo) | `{uuidSolicitudAcademica, radicado}` |
| `GET examenes-supletorios/{uuidSolicitud}` | Estudiante, Funcionario Académico, Decano | | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/funcionario/rechazar` | Funcionario Académico | `{observacion}` | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/funcionario/remitir` | Funcionario Académico | `{observacion}` | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/funcionario/responder` | Funcionario Académico | `{observacion}` opcional (mensaje al Estudiante, máximo 500), `{}` o sin cuerpo | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/funcionario/recibo` | Funcionario Académico | Sin cuerpo | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/funcionario/comprobante/aprobar` | Funcionario Académico | `{fechaAcordadaExamen}` opcional, o sin cuerpo | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/funcionario/comprobante/rechazar` | Funcionario Académico | `{observacion}` | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/decano/aprobar` | Decano | `{observacion}` opcional, o sin cuerpo | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/decano/rechazar` | Decano | `{observacion}` opcional, `{}` o sin cuerpo | ExamenSupletorioDetalle |
| `POST examenes-supletorios/{uuidSolicitud}/estudiante/comprobante` | Estudiante | Sin cuerpo | ExamenSupletorioDetalle |

**FormularioSupletorio**: `codigoEstudiantil` del estudiante autenticado, `asignaturas` (activas:
`{uuidAsignaturaMatriculada, codigoAsignatura, nombreAsignatura, grupo}`),
`causas` (`["cruce", "otra"]`), `anexos` (los tres de radicación:
`{uuidTipoAnexoAcademico, nombre, formatosPermitidos, tamanioMaximoBytes,
causas}`, donde `causas` dice con qué causa se exige: el FOR-23 con las dos,
el soporte de justificación con `otra` y el formato del docente cruzado con
`cruce`) y `plazoDiasHabiles` (3). No trae recibo ni comprobante.

Radicación (`multipart/form-data`):

- `asignaturaMatriculada`: uuid de la asignatura del examen no presentado.
- `fechaExamenNoPresentado`: `AAAA-MM-DD`.
- `tipoCausa`: `cruce` u `otra`, sin distinguir mayúsculas.
- Con `cruce`: `asignaturaCruzada` (otra asignatura activa del estudiante),
  `fechaExamenCruzada` (`AAAA-MM-DD`) y `horaExamenCruzada` (`HH:mm`). Con
  `otra` no se envían.
- Una parte de archivo por anexo, cuyo nombre es el `uuidTipoAnexoAcademico`:
  el FOR-23 siempre, más el de la causa. No se aceptan soportes libres, el
  recibo, el comprobante, el anexo de la otra causa ni anexos repetidos.

Plazo: se cuentan los días lunes a viernes posteriores al examen hasta hoy
inclusive, en hora de Colombia y sin festivos; más de 3 se rechaza. El mismo
día cuenta 0.

Errores de la radicación, en orden:

| codigoError | Mensaje |
| --- | --- |
| 4 | `El usuario <uuid> no tiene el rol Estudiante...` |
| 6 | `El valor <valor> de la fecha del examen no presentado no es una fecha con el formato AAAA-MM-DD...` (o `de la fecha del examen cruzado`) |
| 4 | `Ya tienes una solicitud de Examen Supletorio en curso (<radicado>), debe terminar antes de radicar otra...` |
| 4 | `Debe elegir la asignatura del examen no presentado...` |
| 4 | `La asignatura <uuid> no es una asignatura matriculada del estudiante <uuid>...` |
| 4 | `La asignatura <codigo - nombre> está <estado> y solo se aceptan asignaturas activas...` |
| 4 | `La causa <x> no es válida, debe ser cruce u otra...` |
| 4 | `La fecha del examen no presentado es obligatoria...` |
| 4 | `La fecha del examen no presentado (<dd/MM/aaaa>) no puede ser posterior a hoy (<dd/MM/aaaa>)...` |
| 4 | `El plazo para pedir el supletorio del examen del <dd/MM/aaaa> venció: el último día permitido era el <dd/MM/aaaa> (3 días hábiles)...` |
| 4 | `Con causa cruce debe elegir la asignatura con la que se cruza el examen...` |
| 4 | `La asignatura con la que se cruza debe ser distinta de la asignatura del examen no presentado...` |
| 4 | `Con causa cruce se requieren la fecha y la hora del examen de la asignatura con la que se cruza...` |
| 6 | `La hora del examen cruzado admite máximo 10 caracteres...` y `La hora del examen cruzado <x> no tiene el formato HH:mm...` |
| 4 | `Con causa otra no se aceptan la asignatura, la fecha ni la hora de un examen cruzado...` |
| 4 | `El tipo de solicitud Examen Supletorio no admite soportes libres...` |
| 4 | `El tipo de anexo <nombre> no pertenece al tipo de solicitud Examen Supletorio...` |
| 4 | `El anexo <Recibo de pago o Comprobante de pago> no se entrega al radicar el examen supletorio...` |
| 4 | `El anexo <nombre> no corresponde a la causa <cruce u otra>...` |
| 4 | `El anexo <nombre> viene más de una vez...` |
| 4 | `Falta el anexo obligatorio: <nombres separados por "; ">...` |
| 6 | Errores de archivo de la sección 1.4 |

**ExamenSupletorioDetalle**: `solicitud` (SolicitudAcademicaDetalleDTORespuesta),
`asignatura` (`{uuidAsignaturaMatriculada, codigoAsignatura, nombreAsignatura,
grupo}`), `tipoCausa` (`cruce` u `otra`), `fechaExamenNoPresentado`
(`AAAA-MM-DD`), `cruce` (nulo con causa `otra`; si no, `{asignatura,
fechaExamenCruzada, horaExamenCruzada}`) y `fechaAcordadaExamen`
(`AAAA-MM-DD` o nulo). Es igual para los tres roles.

Cuerpos y reglas de las acciones:

- `funcionario/rechazar`: `observacion` obligatoria; no lleva Resolución.
- `funcionario/remitir`: el cuerpo es obligatorio (puede ser `{}`) y solo lee
  `observacion`, opcional, para el Decano. Desde T12.16 (P49) no se exige
  confirmar requisitos: si llega `requisitosVerificados`, verdadero o falso, se
  ignora sin error, y ya no existe el error 4 que lo pedía.
- `decano/aprobar` y `decano/rechazar`: `observacion` opcional, que el
  Estudiante no ve.
- `funcionario/responder`: de RECHAZADA_POR_DECANO a RECHAZADA, con
  `observacion` opcional (mensaje al Estudiante, máximo 500).
- `funcionario/recibo`: de APROBADA_POR_DECANO a PENDIENTE_PAGO. El recibo se
  sube antes con `POST solicitudes-academicas/{uuid}/anexos` y `tipoAnexo` =
  uuid de `Recibo de pago`.
- `estudiante/comprobante`: de PENDIENTE_PAGO a EN_VERIFICACION_PAGO. El
  comprobante se sube antes del mismo modo, con el uuid de `Comprobante de
  pago`.
- `funcionario/comprobante/aprobar`: a APROBADA. `fechaAcordadaExamen`
  (`AAAA-MM-DD`) es opcional y no puede ser anterior a la fecha del examen no
  presentado: 4 `La fecha acordada para el supletorio (<dd/MM/aaaa>) no puede
  ser anterior a la fecha del examen no presentado (<dd/MM/aaaa>)...`.
- `funcionario/comprobante/rechazar`: `observacion` obligatoria, a RECHAZADA.
  El estudiante debe radicar una solicitud nueva.

Ninguna acción del supletorio cambia el estado de las asignaturas
matriculadas.

## 4. Recorridos típicos

En todos los recorridos, después de cada acción conviene volver a leer el
detalle (las acciones ya lo devuelven) y mostrar los botones según
`accionesDisponibles`.

### 4.1 Cancelación de Matrícula

| Paso | Rol | Llamadas |
| --- | --- | --- |
| 1 | Estudiante | `GET cancelaciones-matricula/formulario` |
| 2 | Estudiante | `POST cancelaciones-matricula` con `motivo`, los seis anexos y soportes opcionales. Queda en RADICADA |
| 3 | Funcionario Académico | `GET solicitudes-academicas/funcionario`, luego `GET cancelaciones-matricula/{uuid}`; descarga anexos con `GET solicitudes-academicas/{uuid}/anexos/{uuidAnexo}` |
| 4a | Funcionario Académico | Rechazo: `POST solicitudes-academicas/{uuid}/resolucion` con el escaneo y `POST cancelaciones-matricula/{uuid}/funcionario/rechazar`. Queda en RECHAZADA |
| 4b | Funcionario Académico | Remisión: `GET catalogos-academicos/situaciones` y `POST cancelaciones-matricula/{uuid}/funcionario/remitir` con las evaluaciones. Queda en EN_REVISION_DECANO |
| 5 | Decano | `GET solicitudes-academicas/decano`, `GET cancelaciones-matricula/{uuid}` y `POST .../decano/aprobar` con las situaciones al cancelar, o `POST .../decano/rechazar` con observación |
| 6 | Funcionario Académico | `POST solicitudes-academicas/{uuid}/resolucion` con el escaneo firmado y `POST cancelaciones-matricula/{uuid}/funcionario/responder`. Queda en APROBADA (asignaturas canceladas) o RECHAZADA |
| 7 | Estudiante | `GET solicitudes-academicas/estudiante`, `GET cancelaciones-matricula/{uuid}` y, en etapa final, `GET solicitudes-academicas/{uuid}/resolucion` |

### 4.2 Cancelación de Asignatura

| Paso | Rol | Llamadas |
| --- | --- | --- |
| 1 | Estudiante | `GET cancelaciones-asignatura/formulario` |
| 2 | Estudiante | `POST cancelaciones-asignatura` con `motivo`, uno o más `asignaturas` y soportes opcionales. Queda en RADICADA |
| 3 | Funcionario Académico | `GET solicitudes-academicas/funcionario` y `GET cancelaciones-asignatura/{uuid}` |
| 4a | Funcionario Académico | Rechazo completo: escaneo con `POST solicitudes-academicas/{uuid}/resolucion` y `POST .../funcionario/rechazar` |
| 4b | Funcionario Académico | Remisión: `POST .../funcionario/remitir` con faltas, nota, situación, `cumpleCondiciones` y observación de cada asignatura |
| 5 | Decano | `POST .../decano/aprobar` con `aceptarEvaluacion: true` o con la decisión de cada asignatura (aprobación parcial), o `POST .../decano/rechazar` para negar la solicitud completa |
| 6 | Funcionario Académico | Escaneo y `POST .../funcionario/responder` con `observacion` y un ajuste por cada asignatura aprobada con `decisionCambiadaPorDecano`. En APROBADA solo se cancelan las asignaturas aprobadas |
| 7 | Estudiante | `GET cancelaciones-asignatura/{uuid}` y, en etapa final, la Resolución |

### 4.3 Examen Supletorio

| Paso | Rol | Llamadas |
| --- | --- | --- |
| 1 | Estudiante | `GET examenes-supletorios/formulario` |
| 2 | Estudiante | `POST examenes-supletorios` con asignatura, fecha, causa, datos de cruce si aplica y anexos de la causa, dentro de los 3 días hábiles. Queda en RADICADA |
| 3a | Funcionario Académico | Rechazo: `POST .../funcionario/rechazar` con observación (sin Resolución) |
| 3b | Funcionario Académico | Remisión: `POST .../funcionario/remitir` con `observacion` opcional |
| 4 | Decano | `POST .../decano/aprobar` (observación opcional) o `POST .../decano/rechazar` |
| 5a | Funcionario Académico | Si el Decano rechazó: `POST .../funcionario/responder`. Queda en RECHAZADA |
| 5b | Funcionario Académico | Si aprobó: `POST solicitudes-academicas/{uuid}/anexos` con el recibo (`tipoAnexo` = uuid de `Recibo de pago`) y `POST .../funcionario/recibo`. Queda en PENDIENTE_PAGO |
| 6 | Estudiante | Descarga el recibo con `GET solicitudes-academicas/{uuid}/anexos/{uuidAnexo}`, paga, sube el comprobante con `POST solicitudes-academicas/{uuid}/anexos` (`tipoAnexo` = uuid de `Comprobante de pago`) y llama `POST .../estudiante/comprobante`. Queda en EN_VERIFICACION_PAGO |
| 7 | Funcionario Académico | `POST .../funcionario/comprobante/aprobar` con la fecha acordada opcional (APROBADA) o `POST .../funcionario/comprobante/rechazar` con observación (RECHAZADA) |

## 5. Lo que ve cada rol en el detalle

| Proceso | Estudiante dueño | Funcionario asignado | Decano |
| --- | --- | --- | --- |
| Común (`solicitudes-academicas/{uuid}`) | Todo el detalle desde RADICADA; `puedeDescargarResolucion` solo en APROBADA o RECHAZADA | Todo, en todas las etapas | Todo, desde EN_REVISION_DECANO; no ve RADICADA |
| Cancelación de Matrícula | Todo, salvo `situacionCancelar`, que llega nula hasta que la solicitud está en APROBADA o RECHAZADA | Todo | Todo |
| Cancelación de Asignatura | Antes de la etapa final, de cada asignatura solo `uuidAsignaturaSolicitud`, `codigoAsignatura` y `nombreAsignatura`. En APROBADA o RECHAZADA, además `aprobadaPorDecano`, `observacionDecision` (la que escribe el Funcionario en el ajuste) y, solo en las aprobadas, `situacionCancelar` (P46). Nunca faltas, nota, situación en la matrícula, `cumpleCondiciones`, `observacionEvaluacion` ni `decisionCambiadaPorDecano` (llega nulo) | Todo | Todo |
| Examen Supletorio | Todo | Todo | Todo |

El historial y las descargas de anexos siguen la misma visibilidad que el
detalle común: un rol que no ve la solicitud recibe el error de inexistente.
Un Funcionario Académico que no tiene asignado el tipo no ve sus
solicitudes, salvo que también tenga el rol Decano.

