# Backend — Gestión de Procesos Académicos FIET

Spring Boot 3, Java 17, MySQL 8. Hereda la arquitectura hexagonal (puertos y
adaptadores) de Julián Camacho tal cual está en `solicitudes/src/main/java/
com/unicauca/cfiet/solicitudes`. Todo lo que se agrega acá sigue exactamente
esa misma estructura de paquetes — no se introduce una variante ni un
patrón distinto para los módulos nuevos. Del código de Julián solo se
agregan líneas (constantes, métodos `@Bean`, reglas de seguridad,
excepciones, mensajes de error, filas de `data.sql`); no se editan ni se
borran las existentes, salvo que la tarea lo pida de forma explícita.

## Comandos

```
cd solicitudes
./mvnw clean install -DskipTests    # genera target/solicitudes-0.0.1.jar
cd ..
docker compose up --build           # backend (8080) + MySQL (3307 host / 3306 contenedor)
```

Sin Docker: JDK 17 + MySQL 8 local. `application.properties` no trae valores
fijos: lee las variables de entorno `SERVER_PORT`, `DB_URL`, `DB_USER_NAME`,
`DB_PASSWORD`, `UPLOADS_PATH` (por defecto `/app/uploads`) y `JWT_SECRET`
(clave de firma del JWT; sin ella se usa el valor local por defecto). En Docker las
define `docker-compose.yml` y la carpeta `backend/uploads` del host se monta
en `/app/uploads`.

`MAIL_USER` y `MAIL_PASSWORD` son la cuenta de Gmail (y su contraseña de
aplicación) que envía el correo al estudiante cuando su solicitud académica
llega a `APROBADA` o `RECHAZADA` (SMTP `smtp.gmail.com:587`, auth y
STARTTLS). No tienen valor en el repositorio: `docker-compose.yml` las toma
del entorno del host. Sin ellas el backend arranca igual y el envío solo deja
el error en el log. El correo lo arma `SolicitudAcademicaCUImplAdaptador.cambiarEtapa`
(asunto y texto por proceso, escaneo de la Resolución en las cancelaciones y
el anexo "Documento adicional de la respuesta" si existe, P54) y lo pasa a
`CorreoIntPuerto` con `AdjuntoCorreo` (nombre, tipo y ruta, sin bytes).
`CorreoImplAdaptador` espera al commit de la transacción y llama a
`ServicioCorreo` (`@Async`, `@EnableAsync` en `ConfiguracionAsincrona`), que
lee los archivos con `AlmacenamientoAnexosIntPuerto.leer` en el hilo
asíncrono y envía un `MimeMessage`. Si falta un archivo, el destinatario no
tiene correo o el SMTP falla, solo se registra el error; la respuesta nunca
se revierte.

Para no definirlas en cada consola, `MAIL_USER`, `MAIL_PASSWORD` y
`CORREO_DARCA` pueden ir en `backend/.env`, que Docker Compose carga solo al
lado de `docker-compose.yml`: se copia `backend/.env.example` como `.env` y
se completan los valores. `.env` está en los `.gitignore` y no se sube al
repositorio; `.env.example` sí se sube y no lleva credenciales reales. Las
variables también se pueden definir en PowerShell con `$env:MAIL_USER="..."`
antes de `docker compose up` (lo definido en la consola manda sobre el
`.env`). Si cambian los valores del `.env`, basta con `docker compose up`
para recrear el contenedor; no hace falta reconstruir la imagen, porque
el `.env` no entra en ella (el `Dockerfile` solo copia el jar).

En la misma respuesta final salen otros dos correos, independientes del del
estudiante:

- DARCA, en los tres procesos. El destinatario es la propiedad
  `correo.darca.destinatario=${CORREO_DARCA:camilobonillab26@gmail.com}` (no
  es un secreto; `docker-compose.yml` pasa `CORREO_DARCA` con ese mismo valor
  por defecto). Asunto `DARCA - Notificación de <proceso> - Radicado <radicado>`,
  carta corta con la fecha y una frase de resumen (con varias asignaturas,
  cuántas se aprobaron y cuántas se rechazaron) y, como adjuntos, el PDF
  `Radicado-<radicado>.pdf`, la Resolución en las cancelaciones y el documento
  adicional si existe.
- El profesor, solo en Examen Supletorio y solo al aprobar el comprobante,
  justo después de crearle la notificación en su perfil
  (`TramiteExamenSupletorioCUImplAdaptador.aprobarComprobante`). Va al
  `correoElectronico` de su usuario con el asunto `Notificación de examen
  supletorio - Radicado <radicado>` y el mismo PDF.

Los arma `NotificadorRespuestaFinal` (`dominio/servicios`, bean en
`BeanConfiguracion`): `SolicitudAcademicaCUImplAdaptador.cambiarEtapa` llama a
`avisarDarca` después del correo del estudiante, y el trámite del supletorio
llama a `avisarProfesor`. El armado se difiere con `PreparacionCorreoIntPuerto`
(`PreparacionCorreoImplAdaptador`), que lo corre en `beforeCommit`: así lee los
datos que el trámite guarda después de `cambiarEtapa` (ajustes de las
asignaturas en Cancelación de Asignatura, profesor asignado en el supletorio).
Si el armado falla (por ejemplo el PDF), se registra `No se pudo preparar el
correo a DARCA|al profesor de la solicitud <radicado>` y los demás correos y la
respuesta siguen igual. El envío sigue el camino de siempre (después del
commit, `@Async`, un `try/catch` por correo).

El PDF lo genera `GeneradorPdfRespuesta` (`infraestructura/output/pdf`, puerto
`GeneradorPdfRespuestaIntPuerto`) con OpenPDF a partir de `ResumenRespuesta`:
radicado, tipo, fecha, estudiante, código, programa y estado final; en
Cancelación de Asignatura la tabla de asignaturas con la decisión de cada una y
la situación académica al cancelar de las aprobadas; en Cancelación de
Matrícula la decisión; en el supletorio la asignatura, la decisión sobre el
comprobante y el profesor. No lleva observaciones ni historial. El PDF viaja en
memoria (`AdjuntoCorreo.contenido`), sin escribirse en disco.

### Orden de arranque con una base nueva

`spring.sql.init.mode=never`: nada se siembra solo. Con una base vacía:

1. `docker compose up --build`. Hibernate crea las tablas de Julián y las 20
   de la extensión al arrancar.
2. Correr `solicitudes/src/main/resources/data.sql` una sola vez (usa
   `INSERT` sin `IGNORE`), con `docker compose exec cfiet_database mysql -u
   root -pmysql cfiet` y pegando su contenido. Carga los roles y tipos de
   usuario de Julián, el usuario `rootfiet` (Secretario General), los roles
   `Estudiante` y `Funcionario Académico` y el tipo de usuario `Estudiante`.
   Todas sus sentencias terminan en `;`. En una base que ya tenía el
   `data.sql` de Julián, los roles y el tipo nuevos se cargan con
   `docs/database/seed-roles-extension.sql` (`INSERT IGNORE`).
3. Crear el Funcionario Académico que atenderá los tres procesos, con
   `POST funcionarios-academicos` como `rootfiet` o como Decano.
4. Correr `docs/database/seed-procesos-academicos.sql` con
   `@funcionario_uuid` apuntando a ese funcionario (el archivo trae el
   comando exacto para Docker). Siembra los cinco programas académicos, los
   tres tipos de solicitud, las 8 etapas, las 23 etiquetas por rol, los 14
   tipos de anexo y las situaciones R0 a R3. Usa `INSERT IGNORE` con UUID
   fijos y se puede repetir; sin `@funcionario_uuid` no se insertan los
   tipos ni lo que depende de ellos (los programas sí).

Sin programas no se pueden crear asignaturas ni estudiantes. Una base que ya
tenía estudiantes y asignaturas antes de `PROGRAMA_ACADEMICO` se pone al día
con `docs/database/migracion_programas_asignaturas.sql`, con el backend
detenido y antes de levantar el backend nuevo; el diccionario de datos trae
los comandos. Si el backend nuevo arranca sin la migración no falla al
iniciar, pero Hibernate deja las columnas nuevas vacías y no puede crear sus
llaves foráneas: el catálogo de programas sale vacío y los listados de
estudiantes responden 500 hasta correr la migración.

El Funcionario Académico usa el tipo `Empleado FIET - Funcionario` de
Julián, que ya existe en `data.sql`.

### Volver a una base limpia en local

**Regla: los procesos académicos son datos predefinidos y no se borran en
ninguna limpieza.** `TIPO_SOLICITUD_ACADEMICA` (los tres procesos),
`ETAPA_SOLICITUD_ACADEMICA` (las 8 etapas, con y sin tipo),
`ETAPA_ETIQUETA_ROL` (23 etiquetas) y `TIPO_ANEXO_ACADEMICO` (14 tipos de
anexo) se conservan siempre, igual que `roles`, `tiposUsuario`,
`PROGRAMA_ACADEMICO` y `SITUACION_ACADEMICA_ASIGNATURA`. Ningún script de
limpieza, de pruebas ni manual les hace `DELETE`; si un proceso necesita otro
responsable, se reasigna (`PUT catalogos-academicos/tipos-solicitud/{uuid}/funcionario`
o `UPDATE` de `FuncionarioAcademico_uuid`). Como
`TIPO_SOLICITUD_ACADEMICA.FuncionarioAcademico_uuid` es obligatorio y apunta a
`FUNCIONARIO_ACADEMICO`, borrar al último funcionario académico sin reasignar
antes los procesos los borraría con él o haría fallar la limpieza (T12.27).

`limpieza_local.sql`, en la raíz y fuera de git (`.gitignore`), deja la base
local como nueva: borra todos los usuarios menos `rootfiet` (con sus filas en
`ESTUDIANTE`, `FUNCIONARIO_ACADEMICO`, `funcionarios` y `Usuario_has_Roles`),
asignaturas y matrículas, todas las solicitudes de Julián y de los tres
procesos con anexos, respuestas, resoluciones, historiales y notificaciones
del docente, órdenes del día, tipos de solicitud de Julián con sus anexos y
`logs`. Antes de borrar a los funcionarios académicos le da a `rootfiet` una
fila en `FUNCIONARIO_ACADEMICO` (cargo "Responsable temporal de los
procesos") y le reasigna los tres procesos, así el catálogo nunca queda sin
responsable. Esa fila solo sostiene la llave foránea: `rootfiet` no tiene el
rol `Funcionario Académico` y no puede tramitar solicitudes. Conserva
`rootfiet` sin tocar su contraseña, `roles` (11), `tiposUsuario` (8),
`PROGRAMA_ACADEMICO` (5, con `ultimoConsecutivoAsignatura` en 0),
`SITUACION_ACADEMICA_ASIGNATURA` (R0 a R3) y todo el catálogo de procesos, y
al final muestra los conteos de procesos, etapas, etiquetas y tipos de
anexo (3, 8, 23 y 14). Busca a `rootfiet` por `username` y, si no existe, no
borra nada. Es una sola transacción, en orden de llaves foráneas, y se puede
repetir. No hay columnas `AUTO_INCREMENT` en las tablas que vacía.

Antes de correrlo: respaldo con `mysqldump` fuera del repositorio y backend
detenido. Desde `backend/`:

```
docker compose stop solicitudes-consejo-de-facultad
docker compose cp ../limpieza_local.sql cfiet_database:/tmp/limpieza.sql
docker compose exec cfiet_database sh -c "mysql -u root -p --default-character-set=utf8mb4 cfiet < /tmp/limpieza.sql"
```

Luego se vacía `backend/uploads` dejando solo su `README` (el backend crea
las subcarpetas al subir), y se levanta el backend. Para dejar el sistema
listo después de la limpieza:

1. Iniciar sesión con `rootfiet` (`POST sesiones`).
2. Crear el Funcionario Académico con `POST funcionarios-academicos`.
3. Asignarle los tres procesos en Administración académica, pestaña
   Responsable por proceso (`PUT catalogos-academicos/tipos-solicitud/{uuid}/funcionario`).
   El catálogo ya está: no hace falta el seed. Si una base vieja perdió los
   procesos, `docs/database/seed-procesos-academicos.sql` con
   `SET @funcionario_uuid` los vuelve a crear con los mismos UUID
   (`INSERT IGNORE`, lo que existe no se duplica).
4. Crear las asignaturas de cada programa y después los estudiantes, que
   solo matriculan asignaturas existentes de su programa.

La base usa `spring.jpa.hibernate.naming.physical-strategy=
PhysicalNamingStrategyStandardImpl`: Hibernate NO convierte a snake_case,
las tablas reales quedan en camelCase exacto al nombre del campo Java
(`usuarios`, `usuariosLivianos`, `tiposUsuario`), nunca en mayúsculas ni
con guiones bajos. Esto importa al escribir SQL a mano contra esas tablas.

`spring.jpa.hibernate.ddl-auto=update`: Hibernate crea y altera tablas desde
las entidades en cada arranque. Las tablas nuevas las crea Hibernate desde las
entidades, igual que las de Julián (su repo no trae scripts DDL, solo
`data.sql`). `script_bd_extension.sql` es documentación de referencia y no se
ejecuta: cada entidad calca su DDL (nombre de tabla, columnas, tipos y FK) y
cualquier cambio de tabla o columna se hace en la entidad y en esa
documentación a la vez.

## Las tres capas, en orden de dependencia

```
dominio/            no depende de nada de Spring ni de JPA
├── modelos/         modelos de dominio con Lombok (@Getter @Setter, a veces
│                     @SuperBuilder si hay herencia) pero SIN anotaciones de
│                     persistencia ni de validación — nunca ven JPA
├── casosdeuso/       *CUImplAdaptador — implementa los puertos de entrada
├── servicios/        reglas de dominio que no son un caso de uso (MaquinaEtapas,
│                     TramiteSolicitud, TramiteCancelacion, ValidadorActorSolicitud,
│                     ValidadorArchivoAdjunto), sin Spring ni JPA
└── helper/constantes/ constantes compartidas (roles, etc.)

aplicacion/          interfaces únicamente — son los "puertos"
├── input/            *CUIntPuerto — lo que expone un caso de uso
└── output/            *GatewayIntPuerto — lo que un caso de uso necesita de afuera

infraestructura/
├── configuracion/
│   └── seguridad/
│       ├── configuracion/  ConfiguracionSeguridad, CustomAuthenticationEntryPoint, CustomAccessDeniedHandler
│       └── jwt/             JwtFiltroAutenticacion, JwtServicio
├── input/controlador<Recurso>/
│   ├── controlador/      *RestController
│   ├── DTOPeticion/       *DTOPeticion (lo que llega en el body)
│   ├── DTORespuesta/      *DTORespuesta (lo que se devuelve)
│   └── mapeador/          Mapper*InfraestructuraDominio (DTO <-> modelo de dominio; con ModelMapper
│                           en Julián, a mano en la extensión)
└── output/
    ├── persistencia/
    │   ├── entidades/      *Entidad (JPA, @Entity)
    │   ├── repositorios/   *Repositorio extends JpaRepository
    │   ├── mapeador/ownMapper/ *OwnMapper, implementan la interfaz OwnMapper<D,E>
    │   │                        (toDominio/toEntidad), son @Service
    │   └── gateway/        *GatewayImplAdaptador — implementa los *GatewayIntPuerto
    ├── almacenamiento/  AlmacenamientoAnexosImplAdaptador (archivos de anexos
    │                     académicos y de la Resolución escaneada en disco,
    │                     reutiliza AlmacenadorArchivos)
    └── manejadorExcepciones/
        ├── RestApiExcepcion    @ControllerAdvice, un @ExceptionHandler por excepción propia
        ├── MensajesError       constantes de texto con %s, se usan con String.format
        └── excepcionesPropias/ Error*Excepcion, todas extends RuntimeException
```

`infraestructura/configuracion/seguridad` (no `infraestructura/output/...`)
es donde vive todo lo de JWT y Spring Security.

Una petición HTTP entra por el `*RestController` (infraestructura/input),
se mapea de DTO a modelo de dominio, pasa al `*CUIntPuerto` (puerto de
entrada, definido en `aplicacion/input` e implementado por el
`*CUImplAdaptador` en `dominio/casosdeuso`). El caso de uso valida reglas de
negocio y llama al `*GatewayIntPuerto` (puerto de salida, definido en
`aplicacion/output`), cuya implementación real vive en
`infraestructura/output/persistencia/gateway` y habla con JPA a través de
entidades y repositorios. El caso de uso nunca ve una entidad JPA ni un DTO
— solo modelos de dominio.

Los `*CUImplAdaptador` NO son `@Component`/`@Service` — se registran como
`@Bean` a mano en `infraestructura/configuracion/BeanConfiguracion`, uno por
caso de uso, pasando en el constructor justo el subconjunto de gateways que
ese caso de uso necesita (no siempre son los mismos). Un caso de uso nuevo
necesita su propio método `@Bean` ahí, con el `new XCUImplAdaptador(...)`.

Así se ve un `@Bean` real de `BeanConfiguracion` (clase `@Configuration`,
sin más estado que los métodos):

```java
@Bean
public RespuestaCUImplAdaptador crearRespuestaCU(RespuestaGatewayIntPuerto gateway,
                                                 SolicitudGatewayIntPuerto solicitudGateway,
                                                 ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                 LogCUIntPuerto log,
                                                 AlmacenadorArchivos almacenadorArchivos){
    return new RespuestaCUImplAdaptador(gateway, solicitudGateway, formateadorExcepciones, log, almacenadorArchivos);
}
```

- Nombre del método: `crear<X>CU` (`crearSolicitudCU`, `crearRespuestaCU`),
  aunque los tres primeros de Julián usan `create<X>CU` (`createRolCU`,
  `createUsuarioCU`, `createSesionCU`). Para los nuevos se usa `crear`.
- Tipo de retorno: la clase concreta `XCUImplAdaptador`, no el puerto
  `XCUIntPuerto`.
- Parámetros: los puertos de salida (`*GatewayIntPuerto`), más
  `ExcepcionesFormateadorIntPuerto`, más `LogCUIntPuerto` cuando el caso de
  uso registra historial, más lo que haga falta de infraestructura
  (`IJwtServicio`, `OrdenDelDiaExportador`, `AlmacenadorArchivos`,
  `AlmacenamientoAnexosIntPuerto`). Los casos de uso que miden plazos o fechan
  registros reciben un `Clock`, que el método crea con
  `Clock.system(ZoneId.of("America/Bogota"))`.
- Orden de los argumentos del `new`: es el mismo orden de los parámetros del
  método, que a su vez es el orden del constructor del caso de uso. No hay un
  orden común entre casos de uso (`RolCUImplAdaptador` recibe gateway,
  formateador, log; `SesionCUImplAdaptador` recibe gateway, log, formateador;
  `LogCUImplAdaptador` recibe gateway, formateador, jwt). Se copia el orden
  del constructor del caso de uso nuevo, no el de otro bean.
- Dependencia entre beans de la misma clase: no hay ninguna inyección
  explícita de un método `@Bean` a otro (ningún método llama a otro ni recibe
  un `XCUImplAdaptador` como parámetro). Las dependencias entre beans son
  implícitas, por el puerto de entrada que pide cada método:
  - `LogCUIntPuerto`, que satisface `crearLogCU`, para todo caso de uso que
    escribe en el log de Julián.
  - `UsuarioCUIntPuerto` (`createUsuarioCU`) en los casos de uso que crean
    Estudiante y Funcionario Académico (ver "Crear Estudiante y Funcionario
    Académico").
  - `SolicitudAcademicaCUIntPuerto` y `AnexoAcademicoCUIntPuerto` en las
    radicaciones y los trámites, y `ConsultaSolicitudAcademicaCUIntPuerto` en
    las consultas de cada proceso.
  - `MaquinaEtapas`, que también es un `@Bean` de esta clase
    (`crearMaquinaEtapas`).

  Spring resuelve el orden de creación solo.
- Todo el resto de parámetros (gateways, `IJwtServicio`,
  `OrdenDelDiaExportador`, `AlmacenadorArchivos`,
  `AlmacenamientoAnexosIntPuerto`) son `@Service` definidos fuera de esta
  clase.

## Convención de nombres

| Elemento | Sufijo | Carpeta |
|---|---|---|
| Puerto de entrada | `XCUIntPuerto` | `aplicacion/input` |
| Caso de uso | `XCUImplAdaptador` | `dominio/casosdeuso` |
| Puerto de salida | `XGatewayIntPuerto` | `aplicacion/output` |
| Adaptador de persistencia | `XGatewayImplAdaptador` | `infraestructura/output/persistencia/gateway` |
| Entidad JPA | `XEntidad` | `infraestructura/output/persistencia/entidades` |
| Mapper entidad-dominio | `XOwnMapper`, implementa `OwnMapper<Dominio, Entidad>` | `infraestructura/output/persistencia/mapeador/ownMapper` |
| Repositorio | `XRepositorio` | `infraestructura/output/persistencia/repositorios` |
| Controlador REST | `XRestController` | `infraestructura/input/controlador<X>/controlador` |
| DTO de entrada | `XDTOPeticion` | `infraestructura/input/controlador<X>/DTOPeticion` |
| DTO de salida | `XDTORespuesta` | `infraestructura/input/controlador<X>/DTORespuesta` |
| Mapper DTO-dominio | `MapperXInfraestructuraDominio` | `infraestructura/input/controlador<X>/mapeador` |

Esta tabla es la que se sigue para todo lo nuevo. El código de Julián no es
100% consistente con su propia convención en un puñado de clases viejas
(`SolicitudCUintPuerto` con "int" en minúscula, `UsuarioGatewayImplAdapter`
sin "ador", `RolOwnMapperImpl` con sufijo "Impl" de más) — esas
irregularidades no se corrigen porque no es código que toquemos, pero
tampoco se replican en clases nuevas.

Los nombres de clase, de paquete y de variable van en español, igual que en
el código de Julián (`RolCUIntPuerto`, `getRoles`, `uuidRol`). No se mezcla
con nombres en inglés salvo las palabras reservadas del framework.

## Manejo de errores

Las reglas de negocio se señalizan con el puerto
`ExcepcionesFormateadorIntPuerto`, que tiene un método por tipo de error
(`lanzarEntidadNoExiste`, `lanzarSinInformacion`, `lanzarMalFormato`,
`lanzarReglaNegocioViolada`, etc.) y arma el mensaje con las constantes de
`MensajesError` vía `String.format`. Si un mensaje nuevo hace falta, se
agrega como constante en `MensajesError`, no como string literal suelto en
el caso de uso. El `RestApiExcepcion` (`@ControllerAdvice`) ya intercepta
todas las excepciones propias — un tipo de error nuevo necesita su propia
excepción en `excepcionesPropias/`, su entrada en `CodigoError` y su
`@ExceptionHandler` correspondiente.

Una excepción aparte es el manejo de fallos de IO al guardar o borrar un
archivo adjunto (`SolicitudCUImplAdaptador`, `RespuestaCUImplAdaptador`):
ahí sí se envuelve el error real en un `RuntimeException` plano, porque no
es una regla de negocio violada sino un fallo técnico de almacenamiento.

En la extensión el fallo técnico se separa en dos capas.
`AlmacenamientoAnexosImplAdaptador` envuelve el `IOException` en un
`UncheckedIOException` y no deja restos en disco.
`AnexoAcademicoCUImplAdaptador` y `ResolucionAcademicaCUImplAdaptador`
capturan ese `RuntimeException` al guardar y lanzan
`lanzarErrorGenerico(MensajesError.ERROR_GUARDANDO_ARCHIVO)` (código 1, "No
se pudo guardar el archivo del anexo..."). Así el cliente recibe el formato
de error común sin ver la causa interna. Las reglas de negocio sobre el
archivo (vacío, tamaño, formato, contenido) siguen siendo
`lanzarMalFormato`.

## Seguridad

JWT sin estado (`SessionCreationPolicy.STATELESS`). La única protección
real de acceso son los `requestMatchers` de
`ConfiguracionSeguridad.securityFilterChain`
(`infraestructura/configuracion/seguridad/configuracion`).

`@PreAuthorize` no protege nada en este proyecto: la clase solo tiene
`@EnableWebSecurity` y no existe `@EnableMethodSecurity` en ninguna parte,
así que Spring no evalúa las anotaciones de método. Lo mostró
`backend/pruebas/t1_preauthorize.ps1` cuando se escribió: un usuario con rol
`Funcionario` recibía 200 en `GET usuarios`, `usuarios/paginado` y
`usuarios/funcionarios`, aunque los tres llevan
`@PreAuthorize(SECRETARIO_DECANO_ACCESO)`. Después se agregaron
`requestMatchers` para esos listados (y para `usuarios/filtro` y
`usuarios/tipos`) que los restringen a Secretario General y Decano; hoy el
403 lo da esa regla, no la anotación.

En consecuencia:

- Todo endpoint nuevo bajo `${url.application}` (que resuelve a
  `/api/unicauca/fiet/consejo/`) necesita su `requestMatchers` explícito,
  con método HTTP y ruta, y `permitAll()`, `authenticated()` o
  `hasAnyAuthority(...)` con las constantes de `ApplicationConstantes`.
- Una ruta sin regla propia no queda cerrada: cae en una regla genérica
  (`usuarios/**`, `solicitudes/**`) o en `anyRequest().authenticated()`, y la
  puede llamar cualquier usuario autenticado, sea cual sea su rol.
- `@PreAuthorize(...)` se sigue poniendo sobre cada método del controlador,
  con la misma expresión que su `requestMatchers`, pero solo para documentar
  la intención. No reemplaza la regla. No se habilita
  `@EnableMethodSecurity`, porque cambiaría el comportamiento de los
  endpoints de Julián.
- La matriz de permisos de cada endpoint nuevo (rol permitido 200, rol no
  permitido 403, sin token 401) se comprueba con el script de humo de la
  tarea (ver "Pruebas").

`ApplicationConstantes` tiene los roles de Julián (`SECRETARIO_GENERAL`,
`DECANO`, `FUNCIONARIO_ROL`) y los de la extensión (`ESTUDIANTE_ROL`,
`FUNCIONARIO_ACADEMICO_ROL`), más los perfiles compuestos que usan los
`@PreAuthorize`: de Julián `SECRETARIO_DECANO_ACCESO`,
`SECRETARIO_DECANO_FUNCIONARIO_ACCESO` y `AUTHENTICATED`, y de la extensión
`SECRETARIO_DECANO_FUNCIONARIO_ACADEMICO_ACCESO`, `ESTUDIANTE_ACCESO`,
`FUNCIONARIO_ACADEMICO_ACCESO`, `DECANO_ACCESO`,
`ESTUDIANTE_FUNCIONARIO_ACADEMICO_ACCESO` y
`ESTUDIANTE_FUNCIONARIO_ACADEMICO_DECANO_ACCESO`. Desde T12.28 también
`DOCENTE_ROL` (`Docente`, rol de Julián que ya existe) y `DOCENTE_ACCESO`. Un perfil nuevo sigue el
mismo patrón de `hasAnyAuthority(...)` armado con las constantes de rol.

Las reglas de los nueve prefijos de la extensión, en ese orden dentro de
`securityFilterChain`, van después de las de Julián y antes de
`anyRequest().authenticated()`, y cada prefijo termina en `denyAll()`:

| Prefijo | Reglas |
|---|---|
| `asignaturas` | GET `programa/{uuidPrograma}` y GET `asignaturas/**`: Secretario General, Decano, Funcionario Académico. POST `cargar/archivo`, POST `asignaturas` y PUT `asignaturas/**`: Secretario General, Decano |
| `estudiantes` | GET `mis-asignaturas`: Estudiante. GET `paginado`, `filtro`, `{uuidEstudiante}`: Secretario General, Decano, Funcionario Académico. POST, PUT, POST de asignaturas, PATCH de estado y `cargar/archivo`: Secretario General, Decano |
| `funcionarios-academicos` | GET `paginado`, `filtro`, `{uuidFuncionario}`: Secretario General, Decano, Funcionario Académico. POST, PUT y `cargar/archivo`: Secretario General, Decano |
| `catalogos-academicos` | PUT `tipos-solicitud/{uuidTipo}/funcionario`: Secretario General, Decano. GET `programas` y GET `catalogos-academicos/**`: cualquier autenticado |
| `solicitudes-academicas` | GET `estudiante`: Estudiante. GET `funcionario`: Funcionario Académico. GET `decano`: Decano. GET detalle, historial, descarga de anexo y de Resolución: Estudiante, Funcionario Académico, Decano. POST anexos: Estudiante, Funcionario Académico. POST Resolución: Funcionario Académico |
| `cancelaciones-matricula`, `cancelaciones-asignatura`, `examenes-supletorios` | GET `formulario` y POST de radicación: Estudiante. POST `{uuid}/funcionario/**`: Funcionario Académico. POST `{uuid}/decano/**`: Decano. POST `{uuid}/estudiante/**` (solo supletorio): Estudiante. GET `{uuid}`: Estudiante, Funcionario Académico, Decano. GET `examenes-supletorios/docentes`: Funcionario Académico, antes de la regla de `{uuidSolicitud}` (T12.28) |
| `notificaciones-docente` | GET la lista, GET `{uuidNotificacion}`, PATCH `{uuidNotificacion}/leida` y GET `{uuidNotificacion}/anexos/{uuidAnexo}`: Docente. Ningún otro rol (T12.28) |

El Secretario General no tiene acceso a ninguna ruta de solicitudes
académicas: no interviene en los tres procesos. El detalle de cada endpoint
está en `docs/api/contrato-api-procesos-academicos.md`.

El Decano de los procesos académicos nuevos entra con el rol `Decano` que ya
existe: se usa `ApplicationConstantes.DECANO`, no se crea otro rol. Los roles
nuevos son `Estudiante` y `Funcionario Académico`; su constante y su fila en
`roles` de `data.sql` se crean juntas. `crearUsuario` busca el tipo de
usuario por nombre y falla si no existe: el Estudiante usa el tipo nuevo
`Estudiante` (fila en `tiposUsuario` de `data.sql`) y el Funcionario
Académico usa el tipo `Empleado FIET - Funcionario` de Julián
(`79105584-1091-4a4e-ba8e-9cbdd1c85b91`), con el rol `Funcionario Académico`.
No se reutiliza el rol `Funcionario` de Julián ni existe un tipo de usuario
`Funcionario Académico`.

Los `requestMatchers` se evalúan en orden y gana el primero que coincide: una
regla nueva va antes de las genéricas (`usuarios/**`, `solicitudes/**`,
`anyRequest()`). De Julián, hoy `GET usuarios`, `usuarios/paginado`,
`usuarios/filtro`, `usuarios/funcionarios` y `usuarios/tipos` son solo para
Secretario General y Decano; el resto de `GET usuarios/**` (por ejemplo
`usuarios/{uuid}`) y `PATCH usuarios/**` quedan en `authenticated()`, y los
demás métodos sobre `usuarios/**` son para Secretario General y Decano.
Estudiantes y funcionarios académicos se crean por sus propios endpoints
(`estudiantes` y `funcionarios-academicos`), no por `usuarios`.

Desde T12.21 los endpoints de Julián que solo protegía su `@PreAuthorize`
tienen regla propia, antes de `solicitudes/**`, de `GET respuestas/**` y de
`GET usuarios/**`. La regla sigue el uso real del frontend, que en algunos
casos incluye roles que la anotación no tiene (las anotaciones no se
cambiaron):

- `GET solicitudes`, `paginado`, `buscar` y `fun/**`: Secretario General,
  Decano, Funcionario. `buscar/solicitante`, `anexos/download` y
  `exportar/**`: Secretario General, Decano.
- `GET solicitudes/funcionario/{uuid}`: Secretario General, Decano y
  Funcionario (lo usa `funcionario/solicitudes`).
- `GET solicitudes/estado`: `authenticated()`, explícito para que no lo tome
  la regla de `{uuidSolicitud}`.
- `GET` y `PUT solicitudes/{uuidSolicitud}`, `GET respuestas/paginado/funcionario`,
  `GET respuestas/paginado/funcionario-nombre-solicitud` y
  `PATCH respuestas/{uuid}/archivo`: Secretario General, Decano y Funcionario.
  La Secretaria Decanatura FIET solo usa, desde `sec-fiet/respuestas`,
  `respuestas/paginado`, `paginado/nombre-solicitud`, `respuestas/{uuid}` y la
  descarga, que siguen en `GET respuestas/**`.
- En `PUT solicitudes/{uuidSolicitud}` el controlador llama antes a
  `AccesoRecursoConsejoCUIntPuerto.verificarActualizacionSolicitud`: una
  solicitud inexistente da ENTIDAD_NO_ENCONTRADA (código 3); el Secretario
  General y el Decano siguen igual, incluida la reasignación; el Funcionario
  solo edita las solicitudes que tiene asignadas (si no, ENTIDAD_NO_ENCONTRADA)
  y no puede reasignar (REGLA_NEGOCIO_VIOLADA con `REASIGNACION_NO_PERMITIDA`).
- `GET` y `PATCH usuarios/{uuidUsuario}`: `authenticated()` explícito; el
  dueño lo controla `AccesoRecursoConsejoCUImplAdaptador`.

Con eso un token de Estudiante, Funcionario Académico o Docente recibe 403
en los listados, búsquedas, exportaciones y edición de solicitudes al
Consejo. Siguen públicos `tipos/solicitudes/paginado` y `filtro` (los usa
solo el Secretario General, no la vista pública).

`AccesoRecursoConsejoCUImplAdaptador` (bean `crearAccesoRecursoConsejoCU`)
controla el dueño en cuatro endpoints de Julián: `GET usuarios/{uuid}` (el
propio usuario o el Secretario General; si no, ENTIDAD_NO_ENCONTRADA),
`PATCH usuarios/{uuid}` (igual; si no, REGLA_NEGOCIO_VIOLADA con
`USUARIO_AJENO`) y la descarga de anexos y de respuestas (solicitante por tipo
y número de documento, Funcionario asignado, o Secretario General, Decano y
Secretaria Decanatura FIET; si no, ENTIDAD_NO_ENCONTRADA). Las descargas
resuelven el archivo con `AlmacenadorArchivos.resolverArchivo`, que normaliza
la ruta y devuelve `null` si sale de `uploads/<carpeta>/<uuid>/`.

`JwtFiltroAutenticacion` responde 401 con `NO_ACCESO` cuando el usuario del
token está deshabilitado (`estado` en falso), aunque el token siga vigente.
La clave de firma de `JwtServicio` sale de `jwt.secreto`, que lee la variable
`JWT_SECRET`; `application.properties` y `docker-compose.yml` traen el valor
anterior solo como valor por defecto local.

`solicitudes/src/test/.../seguridad/configuracion/ConfiguracionSeguridadRolesTest`
prueba la cadena real (`@WebMvcTest` con `ConfiguracionSeguridad`,
`JwtFiltroAutenticacion` y `JwtServicio`, y un controlador comodín) sin base
de datos: 403, 401 y 200 por rol. Un endpoint nuevo de Julián o un cambio de
regla agrega su fila ahí.

## Persistencia

Las entidades JPA son `@Entity` con Lombok (`@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder`, o `@SuperBuilder` cuando
hay herencia JPA de por medio, como `UsuarioEntidad extends
UsuarioLivianoEntidad`), nunca anotaciones de validación de Bean Validation
en la entidad — esas van en el DTO de petición. La relación entidad-dominio
se mapea a mano en una clase que implementa `OwnMapper<Dominio, Entidad>`
(dos métodos, `toDominio` y `toEntidad`, sin librería), marcada `@Service`.

Las tablas nuevas de este trabajo de grado (`ESTUDIANTE`,
`FUNCIONARIO_ACADEMICO`, `ASIGNATURA`, `SOLICITUD_ACADEMICA`,
`RESOLUCION_ACADEMICA`, etc.) están documentadas en
`docs/database/diccionario-datos-extension.md` y su DDL en
`docs/database/script_bd_extension.sql` (20 tablas; referencia, no se ejecuta). Las únicas FK hacia
las tablas de Julián apuntan a `usuarios (uuidUsuario)`, desde `ESTUDIANTE`,
`FUNCIONARIO_ACADEMICO`, `HISTORIAL_SOLICITUD_ACADEMICA`, `ANEXO_ACADEMICO`,
`SOLICITUD_EXAMEN_SUPLETORIO` (`Docente_uuid`) y `NOTIFICACION_DOCENTE`; `usuariosLivianos`
y `tiposUsuario` existen en camelCase pero las tablas nuevas no apuntan a
ellas. Ninguna tabla de Julián se modifica. Como la estrategia de nombres
no convierte nada, un campo Java cuyo nombre difiera de la columna del
script (`Usuario_uuid`, `Estudiante_uuid`, `TipoSolicitudAcademica_uuid`)
lleva `@Column(name = "...")` o `@JoinColumn(name = "...")` con el nombre
exacto de la columna, y `@Table(name = "...")` con el nombre exacto de la
tabla en mayúsculas. `FUNCIONARIO_ACADEMICO` es
intencionalmente una tabla distinta de `Funcionario`/`funcionarios` (el rol
de comité de facultad de Julián) — cubre solo al funcionario que verifica
la información académica. El Decano, como en Julián, es un `Usuario` con rol
`Decano` y tipo `Maxima autoridad FIET - Decano`, sin fila en esta tabla;
ninguna FK de la extensión apunta al Decano (el historial apunta a
`usuarios`).

## Procesos académicos: piezas y máquina de etapas

Las reglas de negocio de los tres procesos (etapas, transiciones, anexos,
plazos y decisiones P4 a P47) están en `docs/database/etapas-por-proceso.md`;
el contrato HTTP, en `docs/api/contrato-api-procesos-academicos.md`.

### Casos de uso por área

| Área | Casos de uso (`dominio/casosdeuso`) |
|---|---|
| Catálogo y actores | `AsignaturaCUImplAdaptador`, `EstudianteCUImplAdaptador`, `FuncionarioAcademicoCUImplAdaptador`, `AsignacionFuncionarioAcademicoCUImplAdaptador`, `DocenteCUImplAdaptador` (búsqueda de docentes, T12.28) |
| Docente | `NotificacionDocenteCUImplAdaptador`: lista, detalle, marcar leída y descarga de anexos, con control de dueño (T12.28) |
| Catálogos académicos (solo lectura) | `TipoSolicitudAcademicaCUImplAdaptador`, `EtapaSolicitudAcademicaCUImplAdaptador`, `TipoAnexoAcademicoCUImplAdaptador`, `SituacionAcademicaAsignaturaCUImplAdaptador`, `ProgramaAcademicoCUImplAdaptador` |
| Motor común | `SolicitudAcademicaCUImplAdaptador` (crear y cambiar etapa), `AnexoAcademicoCUImplAdaptador`, `ResolucionAcademicaCUImplAdaptador`, `ConsultaSolicitudAcademicaCUImplAdaptador` (bandejas, detalle, historial, descargas y rol del actor) |
| Cada proceso | Radicación (`CancelacionMatricula`, `CancelacionAsignatura`, `ExamenSupletorio`), trámite (`TramiteCancelacionMatricula`, `TramiteCancelacionAsignatura`, `TramiteExamenSupletorio`) y consulta (`ConsultaCancelacionMatricula`, `ConsultaCancelacionAsignatura`, `ConsultaExamenSupletorio`), todos con sufijo `CUImplAdaptador` |

Las radicaciones validan todo y después llaman a
`SolicitudAcademicaCUIntPuerto.crearSolicitud` y a `adjuntarAnexo`. Los
trámites pasan cada acción por `cambiarEtapa`. Las consultas de cada proceso
arman su detalle sobre `ConsultaSolicitudAcademicaCUIntPuerto.getDetalle`.

### Servicios de dominio (`dominio/servicios`)

No son beans de Spring, salvo `MaquinaEtapas`: se crean con `new` en el
constructor del caso de uso o del servicio que los usa (por ejemplo,
`TramiteCancelacion` crea su `TramiteSolicitud`).

- `MaquinaEtapas`: tabla de transiciones (tipo de proceso, etapa de origen,
  acción, etapa destino, rol y lo que exige: observación, escaneo de la
  Resolución, recibo o comprobante). Expone `siguienteEtapa`, `transicionar`
  (además verifica lo exigido), `accionesDisponibles(tipo, etapa, rol)`,
  `etapaDestino`, `responsableActual` y `esFinal`.
- `TramiteSolicitud`: saca el actor del token con el rol que fija la acción
  (`actorDe`) y devuelve la solicitud con el actor y la transición ya
  validados (`solicitudValidada`), antes de escribir nada.
- `TramiteCancelacion`: lo común de los dos trámites de cancelación sobre
  `TramiteSolicitud`: cobertura de todas las asignaturas de la solicitud,
  faltas, nota, situaciones del catálogo, guardado de filas y cancelación de
  las matriculadas que siguen activas.
- `ValidadorActorSolicitud`: rol real del usuario, Estudiante dueño y
  Funcionario asignado al tipo.
- `ValidadorArchivoAdjunto`: archivo vacío, 5 MB, extensión permitida por el
  tipo de anexo y firma del contenido (pdf, png, jpg).
- `DatosPersonales`: valida y aplica los cambios de datos personales al
  editar un estudiante o un funcionario académico (ver "Crear Estudiante y
  Funcionario Académico").

Además, `ConsultaSolicitudAcademicaCUImplAdaptador.getHistorial` deja nula
para el Estudiante la observación de `REMITIR_DECANO` (P16) y le quita las
filas `APROBAR_DECANO` y `RECHAZAR_DECANO` (`FILAS_OCULTAS_ESTUDIANTE`, P40).
Filtra después de reconstruir las etapas con el historial completo, así que
las filas que quedan conservan su `etapaCodigo`. Funcionario y Decano reciben
todas las filas.

Para el Estudiante, `etapaVisible` cambia `APROBADA_POR_DECANO` y
`RECHAZADA_POR_DECANO` por `EN_REVISION_DECANO` (P41). El código visible va en
`ResumenSolicitudAcademica.etapaCodigo` y `DetalleSolicitudAcademica.etapaCodigo`,
y `MapperSolicitudAcademicaInfraestructuraDominio` lo prefiere al de la etapa.
La `SolicitudAcademica` no se toca: las reglas que dependen de la etapa usan
el código real.

El resumen de las bandejas lleva `numeroDocumento` del estudiante para
Funcionario y Decano y nulo para el Estudiante (P42). Lo decide `resumir` en
el caso de uso y va en `ResumenSolicitudAcademica.numeroDocumento`; el mapper
lo copia de ahí, no del usuario de la solicitud. `codigoEstudiantil` va en el
resumen y en el nivel superior del detalle común para los tres roles; el
mapper lo toma del estudiante de la solicitud. El detalle común hace lo
mismo con el documento (P43): `getDetalle` lo pone en `DetalleSolicitudAcademica.numeroDocumento`
según el rol del acceso y los detalles de los tres procesos lo heredan en su
bloque `solicitud`.

Los archivos los guarda `AlmacenamientoAnexosImplAdaptador`
(`infraestructura/output/almacenamiento`) sobre el `AlmacenadorArchivos` de
Julián (ver "Solicitudes académicas, historial, anexos y Resolución").

### Acciones de la máquina de etapas

| Acción (`AccionEtapa`) | De | A | Rol | Procesos | Exige |
|---|---|---|---|---|---|
| `RADICAR` | (nueva) | RADICADA | Estudiante | los tres | |
| `RECHAZAR_FUNCIONARIO` | RADICADA | RECHAZADA | Funcionario | los tres | observación; escaneo en las cancelaciones |
| `REMITIR_DECANO` | RADICADA | EN_REVISION_DECANO | Funcionario | los tres | |
| `APROBAR_DECANO` | EN_REVISION_DECANO | APROBADA_POR_DECANO | Decano | los tres | |
| `RECHAZAR_DECANO` | EN_REVISION_DECANO | RECHAZADA_POR_DECANO | Decano | los tres | |
| `ENVIAR_RESPUESTA` | APROBADA_POR_DECANO | APROBADA | Funcionario | cancelaciones | escaneo |
| `ENVIAR_RESPUESTA` | RECHAZADA_POR_DECANO | RECHAZADA | Funcionario | los tres | escaneo en las cancelaciones |
| `ENVIAR_RECIBO` | APROBADA_POR_DECANO | PENDIENTE_PAGO | Funcionario | supletorio | recibo de pago |
| `SUBIR_COMPROBANTE` | PENDIENTE_PAGO | EN_VERIFICACION_PAGO | Estudiante | supletorio | comprobante de pago |
| `APROBAR_COMPROBANTE` | EN_VERIFICACION_PAGO | APROBADA | Funcionario | supletorio | |
| `RECHAZAR_COMPROBANTE` | EN_VERIFICACION_PAGO | RECHAZADA | Funcionario | supletorio | observación |

APROBADA y RECHAZADA son finales. Los casos de uso de trámite agregan sus
propias exigencias encima de la tabla (evaluaciones, situaciones, decisiones
por asignatura).

### Patrón de los controladores de la extensión

- Todos los métodos de `solicitudes-academicas`, `cancelaciones-matricula`,
  `cancelaciones-asignatura` y `examenes-supletorios` llevan
  `@Transactional` (los GET `readOnly = true`), y las escrituras de
  `asignaturas`, `estudiantes`, `funcionarios-academicos` y la asignación de
  `catalogos-academicos` también. Hay dos razones:
  - `UsuarioGatewayIntPuerto.getUsuario` de Julián, que usa
    `ValidadorActorSolicitud`, recorre colecciones perezosas y fuera de una
    transacción lanza `LazyInitializationException`.
  - Una radicación o una acción escribe en varias tablas, en el log y en
    disco. Si algo falla, la transacción revierte todo y el almacenamiento
    borra los archivos nuevos al revertir.
- El rol nunca llega del cliente: se deduce del token y de la relación con la
  solicitud. Los DTO no llevan rol ni tipo de usuario.
- Las acciones de los trámites responden el detalle del proceso ya
  actualizado. Una `DataAccessException` se captura y responde 500 con
  `mensaje` y `error`, como en Julián.
- Las rutas con uuid llevan la expresión regular `[0-9a-fA-F\-]{36}`.

## Solicitudes académicas, historial, anexos y Resolución

- `SolicitudAcademicaCUImplAdaptador` crea la solicitud y cambia su etapa con
  `MaquinaEtapas`. `AnexoAcademicoCUImplAdaptador` adjunta, descarga y
  revisa anexos obligatorios. `ResolucionAcademicaCUImplAdaptador` adjunta y
  descarga el escaneo de la Resolución. Los tres validan el actor con
  `ValidadorActorSolicitud` (`dominio/servicios`): rol real del usuario,
  Estudiante dueño de la solicitud, Funcionario asignado al tipo. Anexos y
  Resolución validan el archivo con `ValidadorArchivoAdjunto` (vacío, 5 MB,
  extensión permitida y firma del contenido).
- `cambiarEtapa(uuid, accion, actor, observacion)` no recibe del llamador si
  hay escaneo, recibo o comprobante: lo consulta en la base
  (`RESOLUCION_ACADEMICA` y los anexos `Recibo de pago` y `Comprobante de
  pago` de `ANEXO_ACADEMICO`) y se lo pasa a `MaquinaEtapas`. Por eso el
  archivo se sube antes de pedir la transición.
- `REMITIR_DECANO` retira el escaneo de la Resolución si había uno: borra su
  fila de `RESOLUCION_ACADEMICA` en la misma transacción del cambio de etapa y
  el archivo con `eliminarTrasConfirmar` (solo después del commit; si la
  transacción se revierte no se borra nada). Rechazar, aprobar o enviar la
  respuesta no lo tocan.
- `crearSolicitud` rechaza si el estudiante ya tiene una solicitud del mismo
  tipo fuera de `APROBADA` y `RECHAZADA` (`SOLICITUD_EN_CURSO`, con el radicado
  de la que está en curso). Puede tener una en curso de cada tipo.
- El radicado se calcula como el último del año y tipo más uno, sin bloqueo.
  Si al guardar choca con `uk_solacad_radicado`,
  `SolicitudAcademicaGatewayImplAdaptador.crear` lo convierte en regla de
  negocio violada (`RADICADO_NO_GENERADO`, "No se pudo generar el radicado,
  intente de nuevo") antes de escribir el historial. No se reintenta solo,
  para que la radicación, que junta solicitud, especialización y anexos en una
  sola transacción, siga siendo atómica.
- `crearSolicitud`, `cambiarEtapa`, `adjuntarAnexo` y `adjuntarResolucion`
  reciben el `token` y escriben en el log de Julián con
  `LogCUIntPuerto.crearLog(accion, resultado, token)` después de guardar, igual
  que los casos de uso de Julián. El resultado lleva el radicado y la acción.
  Si la base falla no se escribe el log; si el log falla, la transacción del
  controlador revierte todo.
- `MaquinaEtapas.accionesDisponibles(tipo, etapa, rol)` devuelve las acciones
  que ese rol puede ejecutar en esa etapa, filtrando la misma tabla de
  transiciones. Una etapa final devuelve lista vacía y una etapa nula o en
  blanco se trata como solicitud nueva (`RADICAR`).
- La Resolución (solo cancelaciones de matrícula y de asignatura) la sube el
  Funcionario asignado, en PDF, en `RADICADA`, `APROBADA_POR_DECANO` o
  `RECHAZADA_POR_DECANO`. Hay una por solicitud (PK `SolicitudAcademica_uuid`):
  subirla otra vez reemplaza la fila y el archivo viejo se borra al confirmar
  la transacción. En `APROBADA` o `RECHAZADA` ya no se sube ni se reemplaza.
  La descargan el Estudiante dueño solo en etapa final, y el Funcionario
  asignado y el Decano en cualquier etapa.
- `ValidadorActorSolicitud` usa `UsuarioGatewayIntPuerto.getUsuario` de
  Julián, cuyo mapper recorre `UsuarioEntidad.logs` (perezosa). Por eso estos
  casos de uso deben correr dentro de una transacción: el método del
  controlador lleva `@Transactional` (D6), como el resto de escrituras. Fuera
  de una transacción lanzan `LazyInitializationException`.
- La solicitud o el cambio de etapa y su fila de historial se guardan en un
  solo método `@Transactional` del gateway, después de todas las
  validaciones.
- Los archivos de anexos se escriben con `AlmacenadorArchivos` en
  `${app.uploads.base-path}/anexos/<uuidSolicitud>/<uuidAnexo>_<fecha>.<ext>`;
  el nombre del usuario nunca llega al disco y solo queda en
  `ANEXO_ACADEMICO.nombreArchivo`. La Resolución usa la misma carpeta, con
  nombre `<uuid>_<fecha>.pdf` generado por el sistema. Si falla el guardado
  en base, el caso de uso borra el archivo; si falla el disco, no se crea la
  ficha. Dentro de una transacción, `AlmacenamientoAnexosImplAdaptador` borra
  el archivo nuevo si la transacción se revierte, y `eliminarTrasConfirmar`
  borra el archivo reemplazado solo después del commit.

### Endpoints `solicitudes-academicas`

`SolicitudAcademicaRestController` (`infraestructura/input/controladorSolicitudesAcademicas`)
expone consulta y archivos; los cambios de etapa están en los controladores
de cada proceso (`cancelaciones-matricula`, `cancelaciones-asignatura` y
`examenes-supletorios`). Todos sus métodos llevan `@Transactional` (los GET
`readOnly`), porque leer el usuario recorre colecciones perezosas.

| Método y ruta | Roles en `ConfiguracionSeguridad` | Qué hace |
|---|---|---|
| `GET /estudiante` | Estudiante | Solicitudes del estudiante autenticado |
| `GET /funcionario` | Funcionario Académico | Solicitudes de los tipos asignados a él |
| `GET /decano` | Decano | Todas las que tienen etiqueta para el Decano |
| `GET /{uuid}` | los tres | Detalle: radicado, tipo, fecha, etiqueta, `etapaCodigo`, estudiante, anexos, Resolución, acciones |
| `GET /{uuid}/historial` | los tres | Historial en orden de fecha, con el `etapaCodigo` al que llevó cada acción |
| `POST /{uuid}/anexos` | Estudiante, Funcionario Académico | Multipart `archivo` y `tipoAnexo` opcional; usa `adjuntarAnexo` |
| `GET /{uuid}/anexos/{uuidAnexo}` | los tres | Descarga del anexo de esa solicitud |
| `POST /{uuid}/resolucion` | Funcionario Académico | Multipart `archivo`; usa `adjuntarResolucion` |
| `GET /{uuid}/resolucion` | los tres | Descarga con las reglas de `obtenerResolucion` |

El resto de `solicitudes-academicas/**` es `denyAll()`.

- `ConsultaSolicitudAcademicaCUImplAdaptador` saca el usuario del token
  (`IJwtServicio` y `SesionGatewayIntPuerto`, como `getMisAsignaturas`) y
  decide el rol: dueño de la solicitud con rol Estudiante actúa como
  Estudiante; si no, el Funcionario Académico asignado al tipo actúa como
  Funcionario; si no, quien tenga rol Decano actúa como Decano. El cliente
  nunca envía el rol y los DTO no lo llevan.
- Un rol solo ve las solicitudes cuya etapa tiene fila en
  `ETAPA_ETIQUETA_ROL` para él (el Decano no ve `RADICADA`), en bandejas y en
  detalle, historial y descargas. Sin rol resuelto o sin etiqueta se lanza la
  misma `lanzarEntidadNoExiste` que para un uuid inexistente, así una
  solicitud ajena no revela que existe.
- `ETAPA_ETIQUETA_ROL` no distingue proceso. Por eso `etiquetaDe` cambia la
  etiqueta del Funcionario en un Examen Supletorio en APROBADA_POR_DECANO por
  `ETIQUETA_FUNCIONARIO_SUPLETORIO_APROBADO` ("Enviar Recibo"); en las
  cancelaciones queda la de la tabla, "Pendiente Notificar" (P46). Vale para
  bandeja y detalle; `GET catalogos-academicos/etiquetas` devuelve la tabla tal
  cual.
- Los tres `GET .../formulario` traen el `codigoEstudiantil` del estudiante
  autenticado (P46).
- El resumen de las bandejas del Funcionario y del Decano trae
  `decisionDecano` (P47): `resumir` pide en dos consultas por lote
  (`SolicitudAcademicaGatewayIntPuerto.getUltimaAccion` sobre el historial y
  `getConAsignaturaNoAprobadaPorDecano`) la última acción del Decano y las
  cancelaciones de asignatura con alguna rechazada, sin una consulta por
  solicitud. Para el Estudiante no se consulta y sale nulo.
- Las bandejas devuelven listas sin paginar, de la más reciente a la más
  antigua (`fechaCreacion` y luego `radicado`), con la etiqueta del rol en
  `etiqueta` y el código estable de la etapa en `etapaCodigo` (por ejemplo
  `PENDIENTE_PAGO`).
- `etapaCodigo` sale en las bandejas, en el detalle de `solicitudes-academicas`
  y, dentro de `solicitud`, en los detalles de `cancelaciones-matricula`,
  `cancelaciones-asignatura` y `examenes-supletorios`. Lo arma el mapper
  común desde `solicitud.etapa.codigo`, así que solo aparece en lo que el rol
  ya puede ver. La etiqueta es el texto para mostrar y el código es para que
  el frontend decida; ninguno reemplaza al otro.
- `HISTORIAL_SOLICITUD_ACADEMICA` no guarda la etapa y su `fecha` tiene
  precisión de segundos, así que varias filas pueden empatar. Sin cambiar el
  esquema, `getHistorial` reconstruye la etapa de cada fila encadenando
  transiciones con `MaquinaEtapas.etapaDestino(tipo, etapa, accion)`:
  1. Parte de `RADICAR` desde una solicitud nueva.
  2. Busca la fila cuya acción sale de la etapa actual.
  3. Avanza a la etapa siguiente y repite.

  La máquina no tiene ciclos y cada etapa se visita una vez, así que el
  resultado no depende del orden por fecha; el orden de las filas no cambia.
  Una acción que no encaja en el recorrido queda con `etapaCodigo` nulo.
- Los POST resuelven el actor con `resolverActor` y llaman a
  `adjuntarAnexo` o `adjuntarResolucion` con el token sin `Bearer ` para el
  log de Julián. Las respuestas nunca traen `urlArchivo` ni rutas de disco.
- `backend/pruebas/t5_solicitudes.ps1` inserta las solicitudes de prueba
  directo en MySQL (`docker compose exec -T cfiet_database mysql ...`), sin
  pasar por los endpoints de radicación (el script es anterior a ellos), y al
  final borra solicitudes,
  usuarios, logs de esos usuarios y carpetas de `uploads/anexos`.

### Radicación de Cancelación de Matrícula

- `CancelacionMatriculaCUImplAdaptador.radicarCancelacionMatricula(uuidEstudiante,
  motivo, anexos, token)` (bean `crearCancelacionMatriculaCU`; endpoint
  `POST cancelaciones-matricula`). `anexos` es una lista de `AnexoRadicacion` (uuid del tipo de
  anexo y `ArchivoAdjunto`); el soporte libre va con tipo nulo.
- Antes de escribir nada valida, en este orden: usuario con rol Estudiante y
  fila en `ESTUDIANTE`; al menos una asignatura matriculada `activa`; motivo
  obligatorio de hasta 255 caracteres (la columna); que cada tipo de anexo sea
  de Cancelación de Matrícula; que estén los obligatorios de
  `TIPO_ANEXO_ACADEMICO` (el error nombra los que faltan); y cada archivo con
  `ValidadorArchivoAdjunto`.
- Después llama a `crearSolicitud` (radicado, una en curso por tipo, primer
  historial y su log), guarda `SOLICITUD_CANCELACION_MATRICULA` y una fila de
  `ASIGNATURA_SOLICITUD_ACADEMICA` por cada asignatura activa (evaluación en
  nulo), sube cada anexo con `adjuntarAnexo` y escribe el log "Radicar
  cancelación de matrícula".
- Todo corre en la transacción del controlador (D6). Si algo falla, se revierte
  todo y `AlmacenamientoAnexosImplAdaptador` borra los archivos ya escritos al
  revertir; no quedan solicitud, historial, especialización, anexos ni
  archivos.

### Trámite de Cancelación de Matrícula (Funcionario y Decano)

- `TramiteCancelacionMatriculaCUImplAdaptador` (bean
  `crearTramiteCancelacionMatriculaCU`; endpoints `funcionario/*` y
  `decano/*` de `cancelaciones-matricula`), con el
  token al final de cada método. El actor sale del token
  (`IJwtServicio.getUsername` y `SesionGatewayIntPuerto.getUsuario`) y el rol
  lo fija la acción: Funcionario Académico o Decano. Una solicitud sin fila en
  `SOLICITUD_CANCELACION_MATRICULA` responde como inexistente.
- Todas las acciones pasan por `SolicitudAcademicaCUIntPuerto.cambiarEtapa`,
  que con `MaquinaEtapas` valida rol, etapa, funcionario asignado,
  observación y escaneo, y escribe historial y log:
  - `rechazarPorFuncionario(uuid, observacion, token)`: RADICADA a
    RECHAZADA, con observación y escaneo de la Resolución.
  - `remitirADecano(uuid, evaluaciones, observacion, token)`: RADICADA a
    EN_REVISION_DECANO. Cada `EvaluacionAsignatura` trae
    `uuidAsignaturaSolicitud` (fila de `ASIGNATURA_SOLICITUD_ACADEMICA`),
    `numeroFaltas` y `nota` (texto de hasta 20; si son numéricos, faltas
    entero >= 0 y nota de 0.0 a 5.0 con un decimal, con punto o coma, y se
    guardan normalizados; si no, se guarda el texto; lo valida
    `TramiteCancelacion.faltasDe` y `notaDe`) y `uuidSituacionMatricula`.
    `cambiarEtapa` borra el escaneo viejo (T5.7a).
  - `aprobarPorDecano(uuid, observacion, token)`: EN_REVISION_DECANO a
    APROBADA_POR_DECANO con observación opcional. Desde T12.28 (P51) el
    Decano no registra situaciones ni toca las asignaturas; un `situaciones`
    en el cuerpo se ignora porque el DTO (`ObservacionDTOPeticion`) no lo tiene.
  - `rechazarPorDecano(uuid, observacion, token)`: EN_REVISION_DECANO a
    RECHAZADA_POR_DECANO, sin tocar las situaciones. La observación es
    opcional.
  - `enviarRespuesta(uuid, observacion, token)`: APROBADA_POR_DECANO a
    APROBADA o RECHAZADA_POR_DECANO a RECHAZADA, con escaneo. La observación
    es el mensaje opcional al Estudiante (máximo 500, lo valida
    `cambiarEtapa`). Solo en APROBADA las
    `ASIGNATURA_MATRICULADA` de la solicitud que sigan `activa` pasan a
    `cancelada`, en la misma transacción; las que ya tengan otro estado no se
    tocan.
- Las evaluaciones deben cubrir todas las asignaturas de la solicitud, sin
  faltar, repetir ni incluir ajenas. Desde T12.28 (P50) faltas, nota,
  situación en la matrícula y situación al cancelar son opcionales:
  `TramiteCancelacion.faltasDe`, `notaDe` y `situacionDe` devuelven nulo con
  un valor vacío y solo validan lo que llega. Las situaciones solo se validan
  contra `SITUACION_ACADEMICA_ASIGNATURA`, el sistema no las calcula. Remitir y aprobar validan actor, etapa y datos antes de escribir;
  después cambian la etapa y guardan las filas con
  `SolicitudCancelacionMatriculaGatewayIntPuerto.actualizarAsignaturas`.
- Todo corre en la transacción del controlador (D6): si algo falla no queda
  etapa cambiada, evaluación guardada ni asignatura cancelada.

### Endpoints `cancelaciones-matricula`

`CancelacionMatriculaRestController` (`infraestructura/input/controladorCancelacionesMatricula`).
Todos sus métodos llevan `@Transactional` (los GET `readOnly`): de eso depende
que la radicación y cada acción no dejen nada a medias. Los DTO no llevan rol
ni tipo de usuario.

| Método y ruta | Roles en `ConfiguracionSeguridad` | Qué hace |
|---|---|---|
| `GET /formulario` | Estudiante | Anexos de Cancelación de Matrícula (uuid del tipo, nombre, formatos, obligatorio) y asignaturas activas del estudiante autenticado (código y nombre) |
| `POST /` | Estudiante | Multipart: `motivo`, una parte por archivo con el uuid de su tipo de anexo como nombre y partes `soporte` para los soportes libres. Usa `radicarCancelacionMatricula` con el uuid del usuario autenticado; responde uuid y radicado |
| `GET /{uuid}` | los tres | Detalle de `solicitudes-academicas` más motivo y asignaturas (uuid de la fila de `ASIGNATURA_SOLICITUD_ACADEMICA`, código, nombre, faltas, nota y las dos situaciones) |
| `POST /{uuid}/funcionario/rechazar` | Funcionario Académico | Cuerpo `observacion` |
| `POST /{uuid}/funcionario/remitir` | Funcionario Académico | Cuerpo `observacion` opcional y `evaluaciones` (`asignaturaSolicitudUuid`, `numeroFaltas`, `nota`, `situacionMatriculaUuid`) |
| `POST /{uuid}/funcionario/responder` | Funcionario Académico | Cuerpo opcional `observacion` (`@RequestBody(required = false)`) |
| `POST /{uuid}/decano/aprobar` | Decano | Cuerpo opcional `observacion`; `situaciones` se ignora (P51) |
| `POST /{uuid}/decano/rechazar` | Decano | Cuerpo opcional `observacion` |

El resto de `cancelaciones-matricula/**` es `denyAll()`.

- `ConsultaCancelacionMatriculaCUImplAdaptador` (bean
  `crearConsultaCancelacionMatriculaCU`) saca el estudiante del token para
  el formulario y la radicación, y arma el detalle sobre
  `ConsultaSolicitudAcademicaCUIntPuerto.getDetalle`, así reutiliza la
  visibilidad por `ETAPA_ETIQUETA_ROL` y la respuesta de inexistente. Una
  solicitud sin fila en `SOLICITUD_CANCELACION_MATRICULA` responde con el
  mismo error que una inexistente.
- La situación al cancelar solo la ven el Funcionario y el Decano; el
  Estudiante la ve cuando la solicitud está en etapa final.
- Las acciones responden el detalle de la cancelación ya actualizado. El
  escaneo de la Resolución se sigue subiendo con
  `POST solicitudes-academicas/{uuid}/resolucion` antes de rechazar o
  responder.
- El catálogo para los desplegables es `GET catalogos-academicos/situaciones`
  de T5.1 (cualquier usuario autenticado); este controlador no tiene
  `/situaciones`.
- `backend/pruebas/t6_cancelacion_matricula.ps1` recorre el proceso con
  usuarios reales (permisos, formulario, radicación, recorrido de aprobación,
  rechazo del Funcionario y rechazo del Decano) y al final borra solicitudes,
  usuarios, asignaturas, sus logs y carpetas de `uploads/anexos`, y deja el
  responsable del tipo como estaba. Los logs que escribe root al crear los
  usuarios se conservan, como en `t5_solicitudes.ps1`.

### Radicación de Cancelación de Asignatura

- `CancelacionAsignaturaCUImplAdaptador.radicarCancelacionAsignatura(uuidEstudiante,
  motivo, uuidsAsignaturaMatriculada, anexos, token)` (bean
  `crearCancelacionAsignaturaCU`; endpoint `POST cancelaciones-asignatura`).
  `SOLICITUD_CANCELACION_ASIGNATURA` comparte PK con la solicitud (`@MapsId`,
  `fk_solca_solacad`), como la de matrícula.
- Antes de escribir nada valida, en este orden: usuario con rol Estudiante y
  fila en `ESTUDIANTE`; motivo obligatorio de hasta 255 caracteres; al menos
  una asignatura, sin repetidas, cada una `ASIGNATURA_MATRICULADA` del propio
  estudiante y `activa` (una ajena y una inexistente dan el mismo mensaje con
  el uuid recibido; una repetida o no activa se nombra con código y nombre);
  que los anexos sean solo soportes libres (sin tipo; no hay obligatorios) y
  cada archivo con `ValidadorArchivoAdjunto`.
- Después llama a `crearSolicitud`, guarda la especialización con
  `SolicitudCancelacionAsignaturaGatewayIntPuerto.guardar`, una fila de
  `ASIGNATURA_SOLICITUD_ACADEMICA` por asignatura elegida (evaluación en nulo)
  con `SolicitudCancelacionMatriculaGatewayIntPuerto.guardarAsignaturas`
  (el gateway de esa tabla, compartido con matrícula), sube cada soporte con
  `adjuntarAnexo` y escribe el log "Radicar cancelación de asignatura".
- Todo corre en la transacción del controlador (D6); si algo falla no quedan
  solicitud, historial, especialización, asignaturas vinculadas, anexos ni
  archivos.

### Trámite de Cancelación de Asignatura (aprobación parcial, P14)

- `TramiteCancelacionAsignaturaCUImplAdaptador` (bean
  `crearTramiteCancelacionAsignaturaCU`; endpoints `funcionario/*` y
  `decano/*` de `cancelaciones-asignatura`) tiene las
  mismas cinco acciones que el de matrícula, con el token al final. Lo común
  (actor desde el token, solicitud y transición validadas antes de escribir,
  cobertura de todas las asignaturas, faltas, nota, situaciones del catálogo,
  guardar filas y cancelar las activas) vive en el servicio de dominio
  `dominio/servicios/TramiteCancelacion`, que también usa
  `TramiteCancelacionMatriculaCUImplAdaptador` sin cambiar su comportamiento.
- `ASIGNATURA_SOLICITUD_ACADEMICA` suma `cumpleCondiciones`,
  `observacionEvaluacion`, `aprobadaPorDecano` y `observacionDecision`, todas
  nulas; en matrícula quedan en `null`.
- `remitirADecano`: cada `EvaluacionAsignatura` trae además
  `cumpleCondiciones` (obligatorio) y `observacionEvaluacion`. Una nota
  numérica menor a 3.0 no puede venir con `cumpleCondiciones` verdadero (una
  nota de texto no tiene esa restricción); la observación de la evaluación
  es opcional aunque no cumpla (máximo 255, P46); al menos una debe cumplir,
  si no el error pide rechazar la solicitud.
- `aprobarPorDecano(uuid, aceptarEvaluacion, decisiones, observacion, token)`:
  viene `aceptarEvaluacion` verdadero (cada asignatura queda con
  `aprobadaPorDecano` igual a `cumpleCondiciones`) o `decisiones`, una
  `DecisionAsignatura` (`uuidAsignaturaSolicitud`, `aprobada`) por
  asignatura; nunca las dos ni ninguna. El Decano puede contradecir la
  evaluación en los dos sentidos. No recibe situación ni observación por
  asignatura: las rechazadas pierden la situación al cancelar y todas quedan
  con `observacionDecision` nula. Al menos una aprobada, si no el error pide
  usar `rechazarPorDecano`. La etapa pasa por APROBAR_DECANO como en
  matrícula.
- `AsignaturaSolicitudAcademica.decisionCambiadaPorDecano()` es nulo sin
  decisión o sin evaluación y verdadero cuando `aprobadaPorDecano` difiere de
  `cumpleCondiciones`. No tiene columna.
- `rechazarPorFuncionario` y `rechazarPorDecano` actúan sobre la solicitud
  completa, como en matrícula.
- `enviarRespuesta(uuid, observacion, ajustes, token)` (P45, P50): valida
  actor y transición con `solicitudValidada` antes de leer los ajustes. Cada
  `AjusteAsignatura` (`uuidAsignaturaSolicitud`, `uuidSituacionCancelar`,
  `observacionDecision`) es de una asignatura propia y sin repetir. Desde
  T12.28 ningún ajuste es obligatorio y una aprobada puede quedar sin
  situación al cancelar; si su ajuste trae situación se valida contra el
  catálogo y reemplaza la guardada. El de una rechazada admite una
  observación opcional (255) y no admite situación (P47). Con alguna
  rechazada o desde RECHAZADA_POR_DECANO la observación general es
  obligatoria, y desde RECHAZADA_POR_DECANO no se aceptan ajustes. Después de
  `cambiarEtapa` guarda las filas si hubo ajustes. En APROBADA pasan a
  `cancelada` solo las asignaturas con `aprobadaPorDecano` verdadero que sigan
  `activa`; con rechazo no se toca ninguna.

### Endpoints `cancelaciones-asignatura`

`CancelacionAsignaturaRestController` (`infraestructura/input/controladorCancelacionesAsignatura`),
con el mismo patrón que `cancelaciones-matricula`: todos sus métodos llevan
`@Transactional` (los GET `readOnly`) y los DTO no llevan rol ni tipo de
usuario.

| Método y ruta | Roles en `ConfiguracionSeguridad` | Qué hace |
|---|---|---|
| `GET /formulario` | Estudiante | Asignaturas activas del estudiante autenticado (`uuidAsignaturaMatriculada`, código, nombre, grupo) y `soportes`: el soporte libre opcional (tipo nulo, formatos y `tamanioMaximoBytes`) |
| `POST /` | Estudiante | Multipart: `motivo`, un campo `asignaturas` por cada uuid elegido y partes `soporte`. Usa `radicarCancelacionAsignatura` con el uuid del usuario autenticado; responde uuid y radicado |
| `GET /{uuid}` | los tres | Detalle de `solicitudes-academicas` más motivo y asignaturas, según rol y etapa |
| `POST /{uuid}/funcionario/rechazar` | Funcionario Académico | Cuerpo `observacion` |
| `POST /{uuid}/funcionario/remitir` | Funcionario Académico | Cuerpo `observacion` opcional y `evaluaciones` (`asignaturaSolicitudUuid`, `numeroFaltas`, `nota`, `situacionMatriculaUuid`, `cumpleCondiciones`, `observacionEvaluacion`) |
| `POST /{uuid}/funcionario/responder` | Funcionario Académico | Cuerpo opcional `observacion` y `ajustes` (`asignaturaSolicitudUuid`, `situacionCancelarUuid`, `observacionDecision`) |
| `POST /{uuid}/decano/aprobar` | Decano | Cuerpo `aceptarEvaluacion` o `decisiones` (`asignaturaSolicitudUuid`, `aprobada`), y `observacion` opcional |
| `POST /{uuid}/decano/rechazar` | Decano | Cuerpo opcional `observacion` |

El resto de `cancelaciones-asignatura/**` es `denyAll()`.

- `ConsultaCancelacionAsignaturaCUImplAdaptador` (bean
  `crearConsultaCancelacionAsignaturaCU`) saca el estudiante del token y arma
  el detalle sobre `ConsultaSolicitudAcademicaCUIntPuerto.getDetalle`, con la
  misma visibilidad por `ETAPA_ETIQUETA_ROL` y la misma respuesta de
  inexistente; una solicitud sin fila en `SOLICITUD_CANCELACION_ASIGNATURA`
  también responde como inexistente.
- El Funcionario asignado y el Decano ven de cada asignatura faltas, nota,
  situación en la matrícula, `cumpleCondiciones`, `observacionEvaluacion` y,
  cuando existen, `aprobadaPorDecano`, `observacionDecision`,
  `decisionCambiadaPorDecano` y la situación al cancelar. El Estudiante ve
  solo uuid de la fila, código y nombre; en `APROBADA` o `RECHAZADA` ve además
  `aprobadaPorDecano`, `observacionDecision` (la del ajuste del Funcionario) y,
  solo en las aprobadas, la situación al cancelar (P46).
  Nunca ve `observacionEvaluacion` ni `decisionCambiadaPorDecano`, que sale
  nulo porque su copia no lleva `cumpleCondiciones`. La vista del
  Estudiante es una copia: no modifica las filas leídas.
- No hay filas de `TIPO_ANEXO_ACADEMICO` para este proceso; el soporte libre
  del formulario sale de `AnexoAcademicoConstantes`
  (`FORMATOS_SOPORTE_LIBRE`, `TAMANIO_MAXIMO_BYTES`), las mismas constantes
  con que se valida. Una parte con otro nombre se toma como tipo de anexo y
  la radicación la rechaza.
- Las acciones responden el detalle actualizado; el escaneo de la Resolución
  se sube antes con `POST solicitudes-academicas/{uuid}/resolucion`. Las
  situaciones salen de `GET catalogos-academicos/situaciones` (T5.1).
- `backend/pruebas/t7_cancelacion_asignatura.ps1` recorre el proceso con
  usuarios reales: permisos, formulario, radicación, recorrido con aprobación
  parcial, rechazo del Funcionario, rechazo total del Decano y una falla a
  mitad de camino provocada con un trigger temporal de MySQL sobre
  `ANEXO_ACADEMICO`, que se borra al final. Deja la base, el responsable del
  tipo y `uploads/anexos` como estaban, salvo los logs de root.

### Radicación de Examen Supletorio

- `ExamenSupletorioCUImplAdaptador.radicarExamenSupletorio(uuidEstudiante,
  uuidAsignaturaMatriculada, fechaExamenNoPresentado, tipoCausa,
  uuidAsignaturaCruzada, fechaExamenCruzada, horaExamenCruzada, anexos, token)`
  (bean `crearExamenSupletorioCU`; endpoint `POST examenes-supletorios`). Las
  fechas son
  `LocalDate` y se guardan a las 00:00 en las columnas DATETIME; `anexos` es
  una lista de `AnexoRadicacion` con el uuid de su tipo.
  `SOLICITUD_EXAMEN_SUPLETORIO` comparte PK con la solicitud
  (`fk_solsup_solacad`) y `SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA` con la del
  supletorio (`fk_solcruce_solsup`); el gateway guarda ambas.
- Antes de escribir nada valida, en este orden: usuario con rol Estudiante y
  fila en `ESTUDIANTE`; solicitud en curso con
  `SolicitudAcademicaCUIntPuerto.verificarSinSolicitudEnCurso` (la misma
  regla que aplica `crearSolicitud`); asignatura obligatoria, propia y
  `activa` (ajena e inexistente dan el mismo mensaje); causa `cruce` u
  `otra`, sin distinguir mayúsculas; fecha del examen obligatoria, no futura y
  dentro del plazo; datos de cruce; anexos y cada archivo con
  `ValidadorArchivoAdjunto`.
- Plazo (P8): con el `Clock` inyectado (America/Bogota en el bean) se cuentan
  los días lunes a viernes posteriores al examen hasta hoy inclusive, sin
  festivos; más de 3 se rechaza nombrando la fecha del examen y el último día
  permitido. El mismo día cuenta 0; un examen en fin de semana empieza a
  contar el lunes.
- Con causa `cruce` la asignatura cruzada es obligatoria, distinta, propia y
  `activa`, y la fecha y la hora (`HH:mm`, máximo 10 caracteres) son
  obligatorias. Con causa `otra` no se acepta ningún dato de cruce.
- Anexos, por nombre sembrado (`AnexoAcademicoConstantes`): el FOR-23 siempre;
  con `otra` el soporte de la justificación, con `cruce` el formato del
  docente de la asignatura cruzada, y el de la otra causa se rechaza. No se
  aceptan recibo ni comprobante, soportes libres, tipos de otro proceso ni
  tipos repetidos. `Docente_uuid` queda en null (y `fechaAcordadaExamen`,
  en desuso desde T12.28, también).
- Después llama a `crearSolicitud`, guarda la especialización (y el cruce),
  sube cada anexo con `adjuntarAnexo` y escribe el log "Radicar examen
  supletorio". Todo corre en la transacción del controlador (D6): si algo
  falla no quedan filas ni archivos.

### Trámite de Examen Supletorio

- `TramiteExamenSupletorioCUImplAdaptador` (bean
  `crearTramiteExamenSupletorioCU`; endpoints `funcionario/*`, `decano/*` y
  `estudiante/*` de `examenes-supletorios`), con el token al
  final de cada método. Usa el servicio de dominio `TramiteSolicitud`
  (actor desde el token con el rol que fija la acción, incluido el
  Estudiante, y solicitud con la transición validada antes de escribir), que
  también usa `TramiteCancelacion` por dentro. Una solicitud sin fila en
  `SOLICITUD_EXAMEN_SUPLETORIO` responde como inexistente.
- Todas las acciones pasan por `cambiarEtapa`, que valida rol, Funcionario
  asignado, Estudiante dueño, etapa, observación y anexos, y escribe historial
  y log:
  - `rechazarPorFuncionario(uuid, observacion, token)`: RADICADA a
    RECHAZADA, sin escaneo.
  - `remitirADecano(uuid, observacion, token)`: RADICADA a
    EN_REVISION_DECANO, con observación opcional para el Decano. Desde T12.16
    no exige confirmar requisitos (P49).
  - `aprobarPorDecano(uuid, observacion, token)`: observación opcional, a
    APROBADA_POR_DECANO. `rechazarPorDecano`: observación opcional, a
    RECHAZADA_POR_DECANO.
  - `enviarRespuesta(uuid, observacion, token)`: RECHAZADA_POR_DECANO a
    RECHAZADA, sin escaneo, con el mensaje opcional al Estudiante.
  - `enviarRecibo(uuid, token)`: APROBADA_POR_DECANO a PENDIENTE_PAGO.
    `subirComprobante(uuid, token)`: PENDIENTE_PAGO a EN_VERIFICACION_PAGO,
    solo el Estudiante dueño. Los archivos se suben antes con
    `adjuntarAnexo` (reglas de T5.5) y el motor verifica en base que estén.
  - `aprobarComprobante(uuid, observacion, uuidDocente, token)` (T12.28, P52,
    P53): a APROBADA. Después de validar actor y transición exige
    `uuidDocente` (sin él, regla de negocio `DOCENTE_SUPLETORIO_REQUERIDO`) y
    que sea un usuario activo con rol Docente
    (`DocenteGatewayIntPuerto.getActivo`; si no, ENTIDAD_NO_ENCONTRADA
    `Docente`). `observacion` es el mensaje opcional al Estudiante y queda en
    la fila `APROBAR_COMPROBANTE` del historial. Tras `cambiarEtapa`, en la
    misma transacción, guarda el profesor con
    `SolicitudExamenSupletorioGatewayIntPuerto.asignarDocente`, crea la fila
    de `NOTIFICACION_DOCENTE` (`NotificacionDocenteGatewayIntPuerto.crear`,
    título `Examen supletorio <aprobado o rechazado>, radicado <radicado>`
    según la etapa final, fecha con el `Clock` del bean) y escribe el log
    "Asignar profesor de examen supletorio". Ya no recibe ni guarda la fecha
    acordada: la columna queda en desuso y fuera del modelo de dominio.
  - `rechazarComprobante(uuid, observacion, token)`: observación obligatoria,
    a RECHAZADA (P4: el estudiante radica una nueva).
- Ninguna acción cambia el estado de las asignaturas matriculadas. Todo corre
  en la transacción del controlador (D6).

### Endpoints `examenes-supletorios`

`ExamenSupletorioRestController` (`infraestructura/input/controladorExamenesSupletorios`),
con el patrón de `cancelaciones-matricula`: todos sus métodos llevan
`@Transactional` (los GET `readOnly`) y los DTO no llevan rol ni tipo de
usuario. Las fechas viajan como texto `AAAA-MM-DD`; otro formato es error de
formato.

| Método y ruta | Roles en `ConfiguracionSeguridad` | Qué hace |
|---|---|---|
| `GET /formulario` | Estudiante | Asignaturas activas, causas (`cruce`, `otra`), anexos de radicación (FOR-23, soporte de justificación y formato del docente cruzado) con formatos, `tamanioMaximoBytes` y las causas que los exigen, y `plazoDiasHabiles`. Sin recibo ni comprobante |
| `POST /` | Estudiante | Multipart: `asignaturaMatriculada`, `fechaExamenNoPresentado`, `tipoCausa`, y con cruce `asignaturaCruzada`, `fechaExamenCruzada`, `horaExamenCruzada`; una parte por archivo con el uuid de su tipo como nombre. Responde uuid y radicado |
| `GET /{uuid}` | los tres | Detalle de `solicitudes-academicas` (radicado, etiqueta, anexos con su tipo, acciones) más asignatura, causa, fecha del examen, cruce si lo hay y `docente` (`uuidUsuario`, `nombres`, `apellidos`) si ya se asignó |
| `POST /{uuid}/funcionario/rechazar` | Funcionario Académico | Cuerpo `observacion` |
| `POST /{uuid}/funcionario/remitir` | Funcionario Académico | Cuerpo con `observacion` opcional (`requisitosVerificados` se ignora, P49) |
| `POST /{uuid}/funcionario/responder` | Funcionario Académico | Cuerpo opcional `observacion` |
| `POST /{uuid}/funcionario/recibo` | Funcionario Académico | Sin cuerpo; usa `enviarRecibo` |
| `POST /{uuid}/funcionario/comprobante/aprobar` | Funcionario Académico | Cuerpo `docenteUuid` (obligatorio para la regla) y `observacion` opcional |
| `GET /docentes` | Funcionario Académico | Búsqueda paginada de docentes activos: `nombre`, `numeroDocumento`, `username` opcionales, `pagina` y `tamanio` (1 a 100); usa `DocenteCUIntPuerto` |
| `POST /{uuid}/funcionario/comprobante/rechazar` | Funcionario Académico | Cuerpo `observacion` |
| `POST /{uuid}/decano/aprobar` | Decano | Cuerpo `observacion` opcional |
| `POST /{uuid}/decano/rechazar` | Decano | Cuerpo opcional `observacion` |
| `POST /{uuid}/estudiante/comprobante` | Estudiante | Sin cuerpo; usa `subirComprobante` |

El resto de `examenes-supletorios/**` es `denyAll()`.

- `ConsultaExamenSupletorioCUImplAdaptador` (bean
  `crearConsultaExamenSupletorioCU`) arma el formulario y el detalle sobre
  `ConsultaSolicitudAcademicaCUIntPuerto.getDetalle`, con la misma visibilidad
  por `ETAPA_ETIQUETA_ROL`, Funcionario asignado y Estudiante dueño. Una
  solicitud sin fila en `SOLICITUD_EXAMEN_SUPLETORIO` responde con el mismo
  mensaje que una inexistente ("Solicitud académica").
- El recibo y el comprobante se cargan antes con
  `POST solicitudes-academicas/{uuid}/anexos` (reglas de T5.5) y la acción
  solo confirma la transición. Se descargan con el endpoint genérico de
  anexos: el Estudiante dueño, el Funcionario asignado y el Decano; nadie más.
- Las acciones responden el detalle ya actualizado.
- `backend/pruebas/t8_examen_supletorio.ps1` recorre el proceso con usuarios
  reales y fechas calculadas a partir de hoy en hora de Colombia: permisos,
  formulario, radicación por cruce y por otra con sus errores, recorrido hasta
  APROBADA con recibo, comprobante, descargas, profesor y su notificación, rechazo del
  Funcionario, rechazo del Decano, comprobante rechazado y una falla a mitad de
  camino con un trigger temporal sobre `ANEXO_ACADEMICO`, que se borra al final.
  Deja la base, el responsable del tipo y `uploads/anexos` como estaban, salvo
  los logs de root.

### Notificaciones del docente (T12.28)

`NotificacionDocenteRestController` (`infraestructura/input/controladorNotificacionesDocente`,
prefijo `notificaciones-docente`), solo para el rol Docente. Todos sus métodos
llevan `@Transactional` (los GET `readOnly`).

| Método y ruta | Roles en `ConfiguracionSeguridad` | Qué hace |
|---|---|---|
| `GET /` | Docente | Notificaciones del docente autenticado, de la más reciente a la más antigua, con radicado, estudiante, asignatura, resultado y fechas |
| `GET /{uuidNotificacion}` | Docente | Detalle de solo lectura: datos del estudiante, del supletorio y anexos; sin historial ni observaciones |
| `PATCH /{uuidNotificacion}/leida` | Docente | Guarda la fecha de lectura la primera vez y responde el detalle |
| `GET /{uuidNotificacion}/anexos/{uuidAnexo}` | Docente | Descarga un anexo de la solicitud de esa notificación |

El resto de `notificaciones-docente/**` es `denyAll()`.

- `NotificacionDocenteCUImplAdaptador` (bean `crearNotificacionDocenteCU`)
  sigue el patrón de dueño de `AccesoRecursoConsejoCUImplAdaptador`: saca el
  usuario del token (`IJwtServicio`, `SesionGatewayIntPuerto`) y solo deja ver
  una notificación si su `Docente_uuid` es ese usuario y tiene rol Docente.
  Una ajena, una inexistente o un anexo de otra solicitud responden
  ENTIDAD_NO_ENCONTRADA (código 3), así no revela que existen.
- `DocenteGatewayImplAdaptador` busca sobre `UsuarioEntidad` con
  `DocenteRepositorio` (rol `Docente` y `estado` verdadero) y mapea con
  `DocenteOwnMapper`, sin el mapper de Julián.
- La tabla `NOTIFICACION_DOCENTE` y la columna
  `SOLICITUD_EXAMEN_SUPLETORIO.Docente_uuid` están en
  `docs/database/script_bd_extension.sql` (secciones 6, 12 y 13) y en el
  diccionario.

## Crear Estudiante y Funcionario Académico

`UsuarioCUImplAdaptador.crearUsuario` y `crearUsuarios` no insertan nada en
`ESTUDIANTE` ni en `FUNCIONARIO_ACADEMICO`. `Usuario.crearInstancia` solo
reconoce `FUNCIONARIO` (la carga masiva deriva el tipo del primer rol, sin
espacios y en mayúscula), y cualquier otro valor cae en un `Usuario` plano.
Julián modela su subtipo con herencia `JOINED` (`usuariosLivianos` ->
`usuarios` -> `funcionarios`); las tablas nuevas apuntan por FK a
`usuarios`, sin herencia JPA.

Por eso Estudiante y Funcionario Académico se modelan por composición, no
por herencia, y tienen su propio caso de uso y su propio gateway:

- El usuario de acceso se crea con el caso de uso de Julián,
  `UsuarioCUIntPuerto.crearUsuario(usuario, tipoUsuario, token)`, que el
  caso de uso nuevo recibe por constructor. Ese método ya valida tipo de
  documento, unicidad de documento, correo y username, tipo de usuario y
  roles, encripta la contraseña y registra el log. Con un `tipoUsuario`
  distinto de `FUNCIONARIO`, `crearInstancia` devuelve un `Usuario` plano,
  que es lo que se quiere. No se modifica `Usuario.java` ni
  `UsuarioGatewayImplAdapter`.
- Con el `uuidUsuario` que devuelve, el gateway nuevo guarda la fila de
  `ESTUDIANTE` o `FUNCIONARIO_ACADEMICO` (PK `Usuario_uuid`) y, para el
  estudiante, sus asignaturas matriculadas (`ASIGNATURA_MATRICULADA`).
- El DTO de petición no recibe roles ni tipo de usuario: el caso de uso los
  asigna por nombre. El cliente no decide sus permisos.
- La transacción va en el método del controlador que crea el estudiante o el
  funcionario académico (`@Transactional`), igual que en
  `UsuarioRestController.crearUsuario` y `crearUsuarios`. Así, si falla la
  fila especializada, también se revierte el usuario. El caso de uso y el
  gateway no llevan `@Transactional`, y el dominio no depende de Spring.

Los crea el Secretario General o el Decano, por formulario y por carga
masiva desde Excel; el estudiante se crea con su información de asignaturas
matriculadas. La facultad del estudiante siempre es `FIET`: la asigna
`EstudianteCUImplAdaptador` al crear y al editar y no llega del cliente.

La edición (`actualizarEstudiante`, `actualizarFuncionarioAcademico`) cambia
también los datos personales, que viven en `usuarios` y `usuariosLivianos`.
No pasa por `UsuarioCUIntPuerto.actualizarUsuario` de Julián (exige roles y,
si no llegan, deja al usuario sin roles): usa el servicio de dominio
`DatosPersonales` (`dominio/servicios`), que valida con el puerto
`DatosPersonalesGatewayIntPuerto` (documento y correo únicos sin contar al
propio usuario; tipo de documento con `Usuario.revisarTipoDocumento`) y
escribe con `DatosPersonalesGatewayImplAdaptador` sobre un repositorio propio
de `UsuarioEntidad` (`DatosPersonalesRepositorio`). Un campo vacío no cambia;
el username no se edita. El código estudiantil se revisa con
`existePorCodigoEstudiantilEnOtro`. Todo se valida antes de escribir.

## Programas académicos y código de asignatura

Reglas P28 a P35 de `docs/database/etapas-por-proceso.md`:

- `PROGRAMA_ACADEMICO` es un catálogo fijo de cinco programas (IET, ISI, IAI,
  TTE, IAC) sembrado con UUID fijos. `ProgramaAcademicoCUImplAdaptador`
  (bean `crearProgramaAcademicoCU`) solo los lista, en `GET
  catalogos-academicos/programas`.
- `ASIGNATURA` y `ESTUDIANTE` llevan `ProgramaAcademico_uuid` obligatorio
  (`fk_asignatura_programa`, `fk_estudiante_programa`). Ninguno de los dos
  cambia de programa después de creado: los `PUT` no lo reciben.
- `AsignaturaCUImplAdaptador` (bean `crearAsignaturaCU`, recibe también
  `ProgramaAcademicoGatewayIntPuerto`) genera el código con
  `ReglasAsignatura.generarCodigo` (`dominio/servicios`): sigla, guion y
  consecutivo con al menos tres dígitos. `crearAsignatura` y
  `crearAsignaturas` (Excel) siguen el mismo camino:
  1. Validan nombre (sin espacios sobrantes, hasta 150), programa existente
     y nombres repetidos dentro de la lista.
  2. Bloquean cada programa con `ProgramaAcademicoGatewayIntPuerto.bloquear`
     (`SELECT ... FOR UPDATE`), en orden de sigla para no cruzar bloqueos.
  3. Con el programa bloqueado revisan que el nombre no exista en él
     (`ReglasAsignatura.claveNombre`: sin mayúsculas ni espacios sobrantes).
  4. Asignan los consecutivos, guardan `ultimoConsecutivoAsignatura` con
     `guardarUltimoConsecutivo` y guardan las asignaturas.

  El bloqueo dura hasta el final de la transacción del controlador, así que
  dos creaciones simultáneas en el mismo programa se atienden una detrás de
  otra; `uk_asignatura_codigo` queda como última defensa. El contador nunca
  baja: un código no se reutiliza aunque se borre la asignatura o se revierta
  la creación. `actualizarAsignatura` solo cambia el nombre, también con el
  programa bloqueado.
- `EstudianteCUImplAdaptador` (recibe `ProgramaAcademicoGatewayIntPuerto`)
  ya no crea asignaturas: al crear un estudiante, en la carga por Excel y en
  `agregarAsignaturaMatriculada`, cada código se busca con
  `ReglasAsignatura.normalizarCodigo` y debe existir (si no, código 3) y ser
  del programa del estudiante (si no, código 4, `ASIGNATURA_OTRO_PROGRAMA`).
  `actualizarEstudiante` no toca el programa.
- Las asignaturas se listan por sigla y consecutivo (`LENGTH` y código, para
  que `ISI-999` quede antes de `ISI-1000`); `GET asignaturas/programa/{uuid}`
  devuelve las del programa sin paginar y `filtro` acepta
  `uuidProgramaAcademico`.

En Java el modelo del actor se llama `FuncionarioAcademico`. `Funcionario`
(modelo, entidad y rol `Funcionario`) es del comité de Julián y no se
reutiliza.

## Carga masiva por Excel (existe en Julián, para usuarios y tipos de solicitud)

El `pom.xml` trae `org.apache.poi:poi-ooxml`; se usa `XSSFWorkbook`, o sea
solo `.xlsx`. El flujo vive en `infraestructura/configuracion/lectorArchivos`
y se dispara desde el controlador, no desde el caso de uso:

1. `POST .../usuarios/cargar/archivo` y `POST .../tipos/solicitudes/cargar/archivo`,
   con `@RequestParam("file") MultipartFile file`, `@RequestHeader("Authorization")`,
   `@Transactional` y `@PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)`.
2. Lectura: `ProcesadorArchivos<T>` (`List<T> procesarArchivo(MultipartFile)`),
   con una implementación `@Service` con nombre por recurso
   (`"archivos-usuarios"` en `ProcesarArchivoUsuariosImpl`,
   `"archivos-tipos-solicitudes"` en `ProcesarArchivoTiposSolicitudesImpl`).
   Lee solo la hoja 0, salta la fila 0 (encabezado), toma cada celda por
   índice de columna y se detiene (`break`) en la primera fila vacía. Las
   celdas numéricas se convierten a `long` y luego a `String` (documento,
   teléfono). Devuelve `DTOPeticion` del recurso; los nombres de tipo de
   usuario y de rol los resuelve contra la base con un caché `HashMap` y, si
   no existe el nombre, queda `null`.
3. Validación: `ValidadorPeticionesExcel<T>` (`Map<String,String> validar(T)`),
   con implementación `@Service` nombrada (`"validador-usuarios"`,
   `"validador-tipos-solicitud"`) que usa el `jakarta.validation.Validator`
   sobre las anotaciones del DTO de petición y devuelve `null` si no hay
   violaciones, o un mapa `propiedad -> mensaje` si las hay.
4. En el controlador, los dos colaboradores entran por constructor explícito
   con `@Qualifier("archivos-...")` y `@Qualifier("validador-...")`. Se
   valida cada petición en un `for`; la primera que falle corta con `400` y
   el mapa de errores. El mapa no trae número de fila. Como se valida todo
   antes de llamar al caso de uso, nada se guarda si alguna fila es inválida.
5. Si todas pasan, se mapea a modelos de dominio y se llama al caso de uso
   (`crearUsuarios`, `crearTiposSolicitud`) con el token sin `Bearer `.
   Un `DataAccessException` se captura en el controlador y responde `500`
   con `mensaje` y `error`; `@Transactional` revierte el lote.

Si falla la lectura del archivo en sí (archivo corrupto, no `.xlsx`), el
`ProcesadorArchivos` captura `Exception`, hace `printStackTrace()` y devuelve
la lista que alcanzó a leer, sin propagar el error. Es el comportamiento real
de Julián; no hay ningún `ErrorMalFormatoExcepcion` ni respuesta `400` para
ese caso.

Si un proceso nuevo llegara a necesitar carga masiva, se sigue este mismo
trío (`ProcesadorArchivos`, `ValidadorPeticionesExcel`, endpoint
`cargar/archivo`) en vez de crear un patrón distinto.

La extensión lo sigue para estudiantes (`POST estudiantes/cargar/archivo`,
`"archivos-estudiantes"` y `"validador-estudiantes"`), asignaturas (`POST
asignaturas/cargar/archivo`, `"archivos-asignaturas"` y
`"validador-asignaturas"`) y funcionarios académicos (`POST
funcionarios-academicos/cargar/archivo`, `"archivos-funcionarios-academicos"`
y `"validador-funcionarios-academicos"`), con tres diferencias frente a
Julián:

- Sus lectores no se tragan los errores: un archivo que no es `.xlsx`, vacío,
  ilegible, sin hojas, con un encabezado distinto al esperado o sin filas de
  datos lanza mal formato (500, código 6).
- El mapa de errores de validación sí trae la fila: cada valor dice
  `Fila N, columna X (campo): mensaje`.
- Antes de llamar al caso de uso, `AgrupadorEstudiantesExcel` junta las filas
  del mismo estudiante (una por asignatura matriculada) y
  `DuplicadosFuncionariosAcademicosExcel` rechaza documento, correo o username
  repetidos dentro del archivo, también con fila y columna. Los conflictos
  con datos que ya están en la base los detecta el caso de uso y responden
  500 con código 2, sin número de fila.

En los Excel de asignaturas y estudiantes, `programaAcademico` es el nombre
exacto de un programa, sin distinguir mayúsculas. `CatalogoAcademicoExcel`
(`infraestructura/configuracion/lectorArchivos`, usa los puertos de entrada
de programas y asignaturas) trabaja en dos momentos del controlador:

- Antes del validador traduce el nombre al uuid del programa (desconocido:
  código 6 con fila y columna; vacío: lo reporta el validador en la columna
  del programa).
- Después del validador revisa, con fila y columna, los nombres de
  asignatura repetidos en el archivo (código 6) o que ya existen en su
  programa (código 2), y en estudiantes que cada `codigoAsignatura` exista y
  sea del programa de la fila (código 6).

El Excel de estudiantes ya no trae `nombreAsignatura` y sus columnas son
`... programaAcademico, semestre, codigoAsignatura, grupo` (13 columnas, sin `facultad`); el de
asignaturas, `nombreAsignatura, programaAcademico`. Las plantillas están en
`backend/pruebas` y las generan los `generar_plantilla_*.ps1`.

## Tests

`solicitudes/src/test` empezó con un único archivo de Julián,
`com/unicauca/cfiet/solicitudes/SolicitudesApplicationTests.java`, el test
que genera Spring Initializr. Hoy tiene además las pruebas unitarias de la
extensión (ver "Pruebas"):

```java
@SpringBootTest
class SolicitudesApplicationTests {

	@Test
	void contextLoads() {
	}

}
```

- Framework: JUnit 5 (`org.junit.jupiter.api.Test`) con `@SpringBootTest`,
  que llega por `spring-boot-starter-test` (ese starter trae también
  Mockito). El código de Julián no usa Mockito.
- Nombre de clase: `<Algo>Tests`, en plural, en el mismo paquete raíz.
  Nombre de método: camelCase en inglés (`contextLoads`), generado por el
  Initializr, no una convención propia de Julián.
- Julián no tiene ningún test de caso de uso, de controlador ni de gateway.
  La convención para el código nuevo es la de la sección "Pruebas".
- `application.properties` usa `${SERVER_PORT}`, `${DB_URL}`,
  `${DB_USER_NAME}` y `${DB_PASSWORD}` sin valor por defecto, así que
  `contextLoads` falla si esas variables no están definidas o la base no
  está arriba (ver "Pruebas").

## Pruebas

El código nuevo se prueba en tres niveles. Las pruebas tampoco llevan
comentarios ni emojis.

### Nivel A: unitarias

JUnit 5 y Mockito. Una clase de prueba por caso de uso en
`solicitudes/src/test/java/com/unicauca/cfiet/solicitudes/dominio/casosdeuso/<X>CUImplAdaptadorTest.java`,
con los gateways y demás puertos simulados con Mockito. Cubren el camino
feliz y cada fallo de cada regla de negocio. Se ejecutan desde
`backend\solicitudes` con `.\mvnw.cmd test`.

Los servicios de dominio y las piezas de archivos y Excel también tienen su
propia clase de prueba. Usan el `ExcepcionesFormateadorImplAdaptador` y la
`MaquinaEtapas` reales y comparan el mensaje exacto de `MensajesError`:

- `dominio/servicios/TramiteSolicitudTest`: actor desde el token, solicitud
  inexistente, etapa, rol de la transición, Funcionario no asignado y
  Estudiante ajeno, sin escrituras cuando algo falla.
- `dominio/servicios/TramiteCancelacionTest`: cobertura de asignaturas,
  faltas, nota, situaciones del catálogo, guardado de filas y cancelación de
  las matriculadas activas.
- `dominio/servicios/ValidadorActorSolicitudTest` y
  `dominio/servicios/ValidadorArchivoAdjuntoTest`.
- `infraestructura/configuracion/lectorArchivos/almacenador/AlmacenadorArchivosTest`,
  con `@TempDir`: nombre generado por el sistema y nombres maliciosos. La
  lectura, el borrado y el borrado tras commit o rollback están en
  `AlmacenamientoAnexosImplAdaptadorTest`.
- `dominio/servicios/ReglasAsignaturaTest`: formato del código, nombre y
  código normalizados.
- `infraestructura/configuracion/lectorArchivos/CatalogoAcademicoExcelTest`:
  programa desconocido, nombre repetido o existente y asignatura inexistente
  o de otro programa, con fila y columna.
- `infraestructura/configuracion/lectorArchivos/AgrupadorEstudiantesExcelTest`
  y `DuplicadosFuncionariosAcademicosExcelTest`.

Hoy `.\mvnw.cmd test` corre 1233 pruebas. `AsignaturaCUImplAdaptadorTest`
cubre el consecutivo, el paso de 999 a 1000, la no reutilización, el orden
bloqueo y luego nombre, y 40 creaciones simultáneas sobre un gateway falso con
bloqueo que no repiten código.

`.\mvnw.cmd test` también ejecuta `SolicitudesApplicationTests`
(`@SpringBootTest`), que levanta el contexto completo y necesita la base de
datos. Antes de correrlo hay que tener arriba la base (`docker compose up`
desde `backend`, MySQL en el puerto 3307 del host) y definir en la misma
sesión de PowerShell:

```
$env:SERVER_PORT = "8081"
$env:DB_URL = "jdbc:mysql://localhost:3307/cfiet?createDatabaseIfNotExist=true&serverTimezone=UTC"
$env:DB_USER_NAME = "root"
$env:DB_PASSWORD = "mysql"
.\mvnw.cmd test
```

### Nivel B: humo contra el backend corriendo

Scripts de PowerShell en `backend/pruebas/`, uno por tarea, que cargan
`backend/pruebas/comun.ps1` (`Invoke-Api`, `Iniciar-Sesion`,
`Escribir-Resultado`) y escriben `PASS` o `FAIL` por cada verificación.
Cubren el camino feliz, los datos inválidos (400 o 409) y la matriz de
permisos de cada endpoint: rol permitido 200, rol no permitido 403, sin
token 401. Los datos se generan únicos en cada ejecución para que el
script se pueda repetir. Se ejecutan desde `backend`:

```
powershell -File .\pruebas\<script>.ps1
```

Scripts que hay hoy:

| Script | Qué cubre |
|---|---|
| `t1_lectura_usuarios.ps1`, `t1_preauthorize.ps1` | Listados de usuarios de Julián cerrados a Secretario General y Decano; `@PreAuthorize` inerte |
| `t2_asignaturas.ps1` | Catálogo de programas y de asignaturas: código generado, consecutivo, nombre repetido, filtro por programa, lista por programa, edición solo del nombre y carga por Excel |
| `t3_estudiantes.ps1`, `t3_excel.ps1` | Estudiantes por formulario y por Excel |
| `t4_funcionarios_academicos.ps1`, `t4_excel.ps1` | Funcionarios académicos por formulario y por Excel |
| `t5_catalogos.ps1`, `t5_asignacion.ps1` | Catálogos sembrados y asignación de tipos a funcionarios |
| `t5_solicitudes.ps1` | Bandejas, detalle, historial, anexos y Resolución de `solicitudes-academicas` |
| `t6_cancelacion_matricula.ps1`, `t7_cancelacion_asignatura.ps1`, `t8_examen_supletorio.ps1` | Recorrido completo de cada proceso con usuarios reales |
| `matriz-permisos.ps1` | Matriz de los 71 endpoints por rol |
| `t9_correo_respuesta.ps1` | Integración de los correos de la respuesta final (P54) con el Funcionario: en cada proceso y cada acción que cierra la solicitud sube la Resolución y el documento adicional, verifica la etapa, el correo al estudiante, el de DARCA (asunto, frase, PDF generado con su contenido y adjuntos) y en el supletorio el del profesor; además los permisos de subida y que un correo rechazado por el servidor (del estudiante o del profesor) no revierte la respuesta ni frena los demás |
| `todo.ps1` | Corre todos los anteriores y verifica la limpieza; `t9` solo si está definida `PRUEBAS_CORREOS` |

`comun.ps1` tiene las funciones compartidas y `generar_plantilla_*.ps1`
genera las plantillas `.xlsx` de carga masiva que están en la misma carpeta.
`$Global:BaseUrl` sale de `PRUEBAS_BASE_URL` si está definida (por defecto
`http://localhost:8080/api/unicauca/fiet/consejo/`).

`t9_correo_respuesta.ps1` no habla con Gmail: necesita `smtp_prueba.py`, un
SMTP local que guarda cada mensaje recibido como JSON (remitente,
destinatarios, asunto, texto y adjuntos; de cada PDF adjunto guarda además si
es un PDF válido y el texto que trae) y rechaza con 550 a cualquier
destinatario que contenga `smtp-rechaza`. Se corre con un backend apuntando a
ese SMTP; Spring toma `SPRING_MAIL_HOST` y `SPRING_MAIL_PORT` del entorno
por encima de `application.properties`. Desde `backend/`, con la base de
Docker arriba y el jar construido (`./mvnw clean package -DskipTests`):

```
python -I pruebas\smtp_prueba.py 2525 C:\ruta\correos
$env:SERVER_PORT="8091"; $env:DB_URL="jdbc:mysql://localhost:3307/cfiet?serverTimezone=UTC"; $env:DB_USER_NAME="root"; $env:DB_PASSWORD="mysql"
$env:UPLOADS_PATH="$PWD\uploads"; $env:MAIL_USER="fiet.prueba@localhost"; $env:MAIL_PASSWORD="prueba"
$env:SPRING_MAIL_HOST="127.0.0.1"; $env:SPRING_MAIL_PORT="2525"; $env:CORREO_DARCA="darca-t9@unicauca.edu.co"
java -jar solicitudes\target\solicitudes-0.0.1.jar > C:\ruta\backend.log
$env:PRUEBAS_BASE_URL="http://localhost:8091/api/unicauca/fiet/consejo/"; $env:PRUEBAS_CORREOS="C:\ruta\correos"; $env:PRUEBAS_LOG_BACKEND="C:\ruta\backend.log"
$env:PRUEBAS_CORREO_DARCA="darca-t9@unicauca.edu.co"
powershell -File .\pruebas\t9_correo_respuesta.ps1
```

Cada comando largo va en su propia consola (el SMTP y el backend se quedan
corriendo). `PRUEBAS_CORREO_DARCA` es obligatoria y debe ser igual al
`CORREO_DARCA` del backend (no se usa la dirección real de DARCA en la
prueba). Con `PRUEBAS_LOG_BACKEND` además comprueba que el fallo del correo
quedó en el log.

Los `.ps1` se escriben en ASCII puro, sin tildes ni eñes, ni siquiera en
los textos que se envían. Windows PowerShell 5.1 lee un `.ps1` sin BOM como
ANSI, así que un literal como `Cédula` llega al backend como `CÃ©dula`.
Cuando un valor necesita tildes, se arma con `[char]` (por ejemplo
`$Global:CedulaCiudadania` en `comun.ps1`).

### Matriz de permisos de los endpoints académicos

`backend/pruebas/matriz-permisos.ps1` lleva escrita la matriz esperada: una
fila por endpoint de los controladores de la extensión (`asignaturas`,
`estudiantes`, `funcionarios-academicos`, `catalogos-academicos`,
`solicitudes-academicas`, `cancelaciones-matricula`,
`cancelaciones-asignatura` y `examenes-supletorios`) y una columna por
actor: sin token, Secretario General, Decano, Funcionario Académico y
Estudiante. Hoy son 71 filas; las cuatro de `notificaciones-docente` quedan
denegadas para todas las columnas porque la matriz no tiene columna Docente
(eso lo cubren `ConfiguracionSeguridadRolesTest` y `t8_examen_supletorio.ps1`).
El script:

- Prueba cada celda por HTTP. Una celda denegada exige 401 sin token y 403
  con token. Una permitida se llama con uuid inexistente, JSON malformado,
  multipart sin archivo o parámetros faltantes, y no puede responder 401,
  403 ni ruta no encontrada. Los errores de Julián llegan como 500 con
  `codigoError`.
- Lee los `@RequestMapping` y `@XxxMapping` de todos los `*RestController`
  cuyo prefijo no es de Julián. Marca FAIL si una ruta del código falta en la
  matriz o si una ruta de la matriz no existe en el código.
- Comprueba que cada prefijo nuevo termina en `denyAll()`, con `DELETE` y
  `POST` a rutas no declaradas: 401 sin token y 403 con cualquier rol.
  También comprueba que los únicos `permitAll()` son los seis de Julián.
- Crea su propio Secretario General, Decano, Funcionario Académico y
  Estudiante. Al final exige que no aparezcan solicitudes, anexos,
  resoluciones, historial ni archivos, y deja usuarios, asignaturas y
  responsables como estaban; solo quedan los logs de root.
- Imprime el conteo de PASS y FAIL y sale con código 1 si hay algún FAIL.

Al agregar un endpoint académico se agrega su fila a la matriz; si no, el
script falla.

### Ejecución completa y limpieza de los scripts

`backend/pruebas/todo.ps1` corre en orden t1 a t8 y `matriz-permisos.ps1`,
cada uno en su propio proceso de PowerShell, desde `backend`:

```
powershell -File .\pruebas\todo.ps1
```

- Antes de empezar inicia sesión como rootfiet y hace `select 1` en
  `cfiet_database`. Si algo falla, dice qué no responde y sale con código 2
  sin correr nada.
- Cuenta las líneas `PASS` y `FAIL` de cada script e imprime una tabla con el
  total.
- Marca ERROR a un script que no existe, que no imprime ninguna verificación
  o que sale con código distinto de cero sin ningún FAIL (se cayó). Termina
  con código 1 si hay algún FAIL o ERROR.
- Avisa si en `pruebas` hay un `t*_*.ps1` que no está en su lista.
- Un script nuevo se agrega a `$Global:Scripts`.
- Antes y después de cada script toma una foto con el `COUNT(*)` de cada
  tabla de `cfiet` (la lista sale de `information_schema.tables`; solo se
  excluye `logs`), el responsable de cada tipo de `TIPO_SOLICITUD_ACADEMICA`
  y lo que hay en `uploads`. Cada diferencia es un FAIL de ese script, con la
  tabla y los valores antes y después, y suma al total; la tabla final tiene
  una columna `Difs`. Si no puede leer la base cuenta un FAIL.

`comun.ps1` tiene la limpieza que comparten los scripts que crean usuarios por
la API:

- `Inicio-Corrida` toma `now()` de MySQL antes de crear nada.
- `Foto-Base` resume los conteos que deben volver a su valor: usuarios,
  roles, `funcionarios` de Julián, estudiantes, funcionarios académicos,
  programas, asignaturas, matriculadas, solicitudes, anexos, logs de quien
  no es rootfiet y responsables de los tipos.
- `Obtener-Programas`, `Programa-PorSigla` y `Crear-AsignaturaPrueba`
  leen el catálogo de programas y crean asignaturas por la API. Todo script
  que matricula estudiantes crea antes sus asignaturas (en ISI), guarda el
  código generado para la limpieza y matricula por código.
- `Limpiar-DatosPrueba -Desde -Usernames -CodigosAsignatura` borra, en una
  sola transacción:
  - los usuarios cuyo username exacto usó el script y que se crearon desde
    `-Desde`, con sus logs, roles, filas de `funcionarios`, `ESTUDIANTE`,
    `FUNCIONARIO_ACADEMICO` y matriculadas;
  - las asignaturas con el código exacto que generó el script y que ya
    nadie usa.

  Nunca borra por patrón; los usernames y códigos llevan un sufijo único por
  corrida.

`t1_lectura_usuarios`, `t1_preauthorize`, `t2_asignaturas`,
`t3_estudiantes`, `t3_excel`, `t4_funcionarios_academicos`, `t4_excel`,
`t5_catalogos` y `t5_asignacion` guardan cada username y código antes de
crearlo. En un `finally` limpian y exigen que `Foto-Base` coincida con la
del inicio; esa es su última verificación. Los logs de rootfiet sí quedan.
`ASIGNATURA` no tiene fecha de creación, así que una asignatura se borra solo
por el código exacto que le asignó el backend en esa corrida y si ninguna
matriculada la usa. El contador `ultimoConsecutivoAsignatura` de los
programas no se devuelve: es la regla de no reutilizar códigos, y las fotos
comparan conteos de filas, no ese valor.
`t5_asignacion` primero devuelve cada tipo a su responsable original y
después borra sus funcionarios académicos, porque `TIPO_SOLICITUD_ACADEMICA`
los referencia. `t1` y `t5_catalogos` no crean asignaturas y no pasan
`-CodigosAsignatura`; en `t5_catalogos` `$codigos` guarda códigos de etapa.

Las comprobaciones sobre el historial comparan las parejas acción:etapa
ordenadas, porque `fecha` tiene precisión de segundos y dos transiciones
seguidas pueden empatar.

### Nivel C: base de datos

Después de `docker compose up --build`, se comprueba que las tablas que
creó Hibernate coinciden con `docs/database/script_bd_extension.sql`:

```
docker compose exec cfiet_database mysql -u root -pmysql --default-character-set=utf8mb4 cfiet -e "SHOW TABLES; DESCRIBE ESTUDIANTE;"
```

Para eso cada entidad lleva `@Table(name = "...")` y `@Column` /
`@JoinColumn` con el nombre exacto del script (ver "Persistencia").

### Compuerta de aceptación de cada subtarea

1. `.\mvnw.cmd clean install -DskipTests` termina en `BUILD SUCCESS`.
2. `.\mvnw.cmd test` en verde, con las variables de entorno de arriba.
3. `docker compose up --build` arranca y el log muestra
   `Started SolicitudesApplication`. Nunca se usa `docker compose down`.
4. El script de humo de la tarea imprime solo `PASS`.
5. La verificación de base de datos coincide con el script.

## Qué no se hace

- No se agrega Javadoc, ni comentarios de una línea, ni emojis en ningún
  archivo nuevo o modificado, salvo los comentarios de
  `docs/database/script_bd_extension.sql`, que se conservan.
- No se cambia la arquitectura de capas ni se salta una capa (por ejemplo,
  un controlador llamando directo a un repositorio).
- No se editan ni se borran tablas, clases o líneas existentes de Julián;
  solo se agregan líneas nuevas, salvo que la tarea lo pida
  explícitamente.
- No se usan anotaciones `@Autowired` en campos — la inyección es siempre
  por constructor (`@RequiredArgsConstructor` de Lombok en los `@Service`/
  `@Component`, o constructor explícito en los casos de uso, que no son
  beans de Spring por sí mismos sino que se registran a mano en
  `BeanConfiguracion`).
