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
├── frontend/   Angular 20 + PrimeNG — SPA
└── docs/
    └── database/
        ├── script_bd_extension.sql          DDL de referencia de las tablas nuevas (no se ejecuta)
        └── diccionario-datos-extension.md   Diccionario de datos de la extensión
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

Primer arranque: Hibernate crea las tablas al levantar el backend, pero los
roles, los tipos de usuario y el usuario `root` no se cargan solos
(`spring.sql.init.mode=never`). Después del primer arranque hay que correr
`backend/solicitudes/src/main/resources/data.sql` una sola vez contra la base
(`docker compose exec cfiet_database mysql -u root -pmysql cfiet` y pegar su
contenido). Sin ese paso no existen roles ni login.

El frontend también tiene su propio `dockerfile` si se prefiere
contenerizarlo en vez de correrlo con `npm start`.

### Opción B — Sin Docker (todo instalado en el equipo)

- **JDK 17**
- **MySQL 8** corriendo en local, con una base de datos creada para el
  proyecto
- **Node.js 20.19+** (o 22.12+; requisito de Angular 20)

Ajustar en ese caso `backend/solicitudes/src/main/resources/application.properties`
(o las variables `DB_URL`, `DB_USER_NAME`, `DB_PASSWORD` que use el proyecto)
para apuntar al MySQL local en vez del contenedor `cfiet_database`.

## Estado del proyecto

Este repositorio se inicializa con el código base de Julián tal como está en
su rama de producción, más la documentación de base de datos ya definida
para la extensión (tabla `RESOLUCION_ACADEMICA` incluida). La implementación
de los tres módulos de proceso académico se desarrolla de aquí en adelante
sobre esta base.
