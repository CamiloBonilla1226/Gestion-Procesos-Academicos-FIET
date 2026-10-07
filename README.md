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
    │   └── contrato-api-procesos-academicos.md   Contrato de los 63 endpoints académicos
    ├── database/
    │   ├── script_bd_extension.sql          DDL de referencia de las 18 tablas nuevas (no se ejecuta)
    │   ├── diccionario-datos-extension.md   Diccionario de datos de la extensión
    │   ├── etapas-por-proceso.md            Etapas, transiciones, anexos, plazos y decisiones
    │   ├── seed-roles-extension.sql         Roles y tipo de usuario de la extensión
    │   └── seed-procesos-academicos.sql     Tipos de solicitud, etapas, etiquetas, anexos y situaciones
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
   contra la base (`docker compose exec cfiet_database mysql -u root -pmysql
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
   archivo trae el comando para Docker. Siembra los tres tipos de solicitud,
   sus etapas, etiquetas por rol, tipos de anexo y el catálogo de situaciones
   académicas. Se puede correr más de una vez.

Si la base ya tenía el `data.sql` original de Julián, los roles y el tipo de
usuario de la extensión se agregan con `docs/database/seed-roles-extension.sql`.

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
