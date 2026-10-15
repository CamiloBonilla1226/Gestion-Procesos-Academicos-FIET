

===== ARCHIVO: backend/CLAUDE.md =====

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
`DB_PASSWORD` y `UPLOADS_PATH` (por defecto `/app/uploads`). En Docker las
define `docker-compose.yml` y la carpeta `backend/uploads` del host se monta
en `/app/uploads`.

### Orden de arranque con una base nueva

`spring.sql.init.mode=never`: nada se siembra solo. Con una base vacía:

1. `docker compose up --build`. Hibernate crea las tablas de Julián y las 19
   de la extensión al arrancar.
2. Correr `solicitudes/src/main/resources/data.sql` una sola vez (usa
   `INSERT` sin `IGNORE`), con `docker compose exec cfiet_database mysql -u
   root -p<REDACTADO> cfiet` y pegando su contenido. Carga los roles y tipos de
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
   tres tipos de solicitud, las 8 etapas, las 23 etiquetas por rol, los 11
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
`ESTUDIANTE_FUNCIONARIO_ACADEMICO_DECANO_ACCESO`. Un perfil nuevo sigue el
mismo patrón de `hasAnyAuthority(...)` armado con las constantes de rol.

Las reglas de los ocho prefijos de la extensión, en ese orden dentro de
`securityFilterChain`, van después de las de Julián y antes de
`anyRequest().authenticated()`, y cada prefijo termina en `denyAll()`:

| Prefijo | Reglas |
|---|---|
| `asignaturas` | GET `programa/{uuidPrograma}` y GET `asignaturas/**`: Secretario General, Decano, Funcionario Académico. POST `cargar/archivo`, POST `asignaturas` y PUT `asignaturas/**`: Secretario General, Decano |
| `estudiantes` | GET `mis-asignaturas`: Estudiante. GET `paginado`, `filtro`, `{uuidEstudiante}`: Secretario General, Decano, Funcionario Académico. POST, PUT, POST de asignaturas, PATCH de estado y `cargar/archivo`: Secretario General, Decano |
| `funcionarios-academicos` | GET `paginado`, `filtro`, `{uuidFuncionario}`: Secretario General, Decano, Funcionario Académico. POST, PUT y `cargar/archivo`: Secretario General, Decano |
| `catalogos-academicos` | PUT `tipos-solicitud/{uuidTipo}/funcionario`: Secretario General, Decano. GET `programas` y GET `catalogos-academicos/**`: cualquier autenticado |
| `solicitudes-academicas` | GET `estudiante`: Estudiante. GET `funcionario`: Funcionario Académico. GET `decano`: Decano. GET detalle, historial, descarga de anexo y de Resolución: Estudiante, Funcionario Académico, Decano. POST anexos: Estudiante, Funcionario Académico. POST Resolución: Funcionario Académico |
| `cancelaciones-matricula`, `cancelaciones-asignatura`, `examenes-supletorios` | GET `formulario` y POST de radicación: Estudiante. POST `{uuid}/funcionario/**`: Funcionario Académico. POST `{uuid}/decano/**`: Decano. POST `{uuid}/estudiante/**` (solo supletorio): Estudiante. GET `{uuid}`: Estudiante, Funcionario Académico, Decano |

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
`docs/database/script_bd_extension.sql` (19 tablas; referencia, no se ejecuta). Las únicas FK hacia
las tablas de Julián apuntan a `usuarios (uuidUsuario)`, desde `ESTUDIANTE`,
`FUNCIONARIO_ACADEMICO`, `HISTORIAL_SOLICITUD_ACADEMICA` y `ANEXO_ACADEMICO`; `usuariosLivianos`
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
| Catálogo y actores | `AsignaturaCUImplAdaptador`, `EstudianteCUImplAdaptador`, `FuncionarioAcademicoCUImplAdaptador`, `AsignacionFuncionarioAcademicoCUImplAdaptador` |
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
  - `aprobarPorDecano(uuid, situacionesAlCancelar, observacion, token)`:
    EN_REVISION_DECANO a APROBADA_POR_DECANO. Cada
    `SituacionCancelarAsignatura` trae `uuidAsignaturaSolicitud` y
    `uuidSituacionCancelar` (P9: la registra el Decano).
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
- Evaluaciones y situaciones al cancelar deben cubrir todas las asignaturas
  de la solicitud, sin faltar, repetir ni incluir ajenas; las situaciones solo
  se validan contra `SITUACION_ACADEMICA_ASIGNATURA`, el sistema no las
  calcula. Remitir y aprobar validan actor, etapa y datos antes de escribir;
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
| `POST /{uuid}/decano/aprobar` | Decano | Cuerpo `situaciones` (`asignaturaSolicitudUuid`, `situacionCancelarUuid`) y `observacion` opcional |
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
- `enviarRespuesta(uuid, observacion, ajustes, token)` (P45): valida actor y
  transición con `solicitudValidada` antes de leer los ajustes. Cada
  `AjusteAsignatura` (`uuidAsignaturaSolicitud`, `uuidSituacionCancelar`,
  `observacionDecision`) es de una asignatura propia y sin repetir. Toda
  asignatura aprobada con la decisión cambiada exige su ajuste; el de una
  aprobada exige situación del catálogo y el de una rechazada admite una
  observación opcional (255) y no admite situación (P47). Con alguna rechazada o desde RECHAZADA_POR_DECANO la
  observación general es obligatoria, y desde RECHAZADA_POR_DECANO no se
  aceptan ajustes. Después de `cambiarEtapa` guarda las filas si hubo
  ajustes. En APROBADA pasan a `cancelada` solo las asignaturas con
  `aprobadaPorDecano` verdadero que sigan `activa`; con rechazo no se toca
  ninguna.

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
  tipos repetidos. `fechaAcordadaExamen` queda en null.
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
  - `aprobarComprobante(uuid, fechaAcordadaExamen, token)`: a APROBADA; la
    fecha es opcional, no puede ser anterior a `fechaExamenNoPresentado` y se
    guarda a las 00:00 con `actualizarFechaAcordada` y su propio log.
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
| `GET /{uuid}` | los tres | Detalle de `solicitudes-academicas` (radicado, etiqueta, anexos con su tipo, acciones) más asignatura, causa, fecha del examen, cruce si lo hay y `fechaAcordadaExamen` si existe |
| `POST /{uuid}/funcionario/rechazar` | Funcionario Académico | Cuerpo `observacion` |
| `POST /{uuid}/funcionario/remitir` | Funcionario Académico | Cuerpo con `observacion` opcional (`requisitosVerificados` se ignora, P49) |
| `POST /{uuid}/funcionario/responder` | Funcionario Académico | Cuerpo opcional `observacion` |
| `POST /{uuid}/funcionario/recibo` | Funcionario Académico | Sin cuerpo; usa `enviarRecibo` |
| `POST /{uuid}/funcionario/comprobante/aprobar` | Funcionario Académico | Cuerpo `fechaAcordadaExamen` opcional |
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
  APROBADA con recibo, comprobante, descargas y fecha acordada, rechazo del
  Funcionario, rechazo del Decano, comprobante rechazado y una falla a mitad de
  camino con un trigger temporal sobre `ANEXO_ACADEMICO`, que se borra al final.
  Deja la base, el responsable del tipo y `uploads/anexos` como estaban, salvo
  los logs de root.

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
$env:DB_PASSWORD = <REDACTADO>
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
| `matriz-permisos.ps1` | Matriz de los 66 endpoints por rol |
| `todo.ps1` | Corre todos los anteriores y verifica la limpieza |

`comun.ps1` tiene las funciones compartidas y `generar_plantilla_*.ps1`
genera las plantillas `.xlsx` de carga masiva que están en la misma carpeta.

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
Estudiante. Hoy son 66 filas. El script:

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
docker compose exec cfiet_database mysql -u root -p<REDACTADO> --default-character-set=utf8mb4 cfiet -e "SHOW TABLES; DESCRIBE ESTUDIANTE;"
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



===== ARCHIVO: frontend/CLAUDE.md =====

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
  `SimpleButtonComponent`) y `src/styles.css`, que importa `primeicons`,
  resetea `html, body` y, desde T12.2-B, define los tokens del diseño en
  `:root` (`--navy-900`, `--navy-700`, `--navy-ink`, `--accent`, `--panel`,
  `--line`, `--text`, `--muted`, `--sidebar-w`, `--fuente`...). Todo lo demás
  va en el CSS de cada componente: el CSS global entra en el bundle inicial y
  el de los componentes no (D-F27).

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
| Funcionario Académico | `funcionario-academico`, `funcionario-academico/solicitudes`, `funcionario-academico/respuestas`, `funcionario-academico/solicitudes/:uuid` | Mi Usuario, Gestión de solicitudes (Solicitudes, Respuestas) |
| Decano | `decano`, `decano/solicitudes`, `decano/solicitudes/:uuid`, `decano/solicitudes-consejo`, `decano/administracion-academica` | Mi Usuario, Gestión académica (Solicitudes académicas, Administración académica), Solicitudes del Consejo |
| Secretario General | `sec-general/administracion-academica` (más las de Julián) | Una línea agregada al menú de Julián: Administración académica |

El login (`shared/login-form-component`) redirige por rol en este orden:
Secretario General, Funcionario (Julián), Secretaria Decanatura FIET, Decano,
Funcionario Académico, Estudiante y luego los usuarios FIET. El Decano va
antes que el Funcionario Académico y que el Estudiante. Esa lógica vive en
`rutaDeInicio` (`core/utils/rutas-por-rol.ts`) y la reutiliza `inicioGuard` en la
ruta raíz. "Cerrar sesión" (`avatar-sesion-component`) llama a
`AuthService.logout()`, evita el `href="#"` del enlace y navega a `/login`.

### Capa API

Un servicio por recurso en `core/services`: `asignatura-service`,
`estudiante-service`, `funcionario-academico-service`,
`catalogo-academico-service`, `solicitud-academica-service`,
`cancelacion-matricula-service`, `cancelacion-asignatura-service` y
`examen-supletorio-service`. Cubren los 66 endpoints del contrato. Los
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
- Funcionario y Decano: los textos y avisos dependen de `etapaCodigo`, no de
  `etiqueta`. La insignia usa `claseInsigniaEtapa`: verde en APROBADA, rojo en
  RECHAZADA, amarillo en PENDIENTE_PAGO y EN_VERIFICACION_PAGO (D-F9), gris en
  el resto.
- Estudiante (D-F23): ninguna vista suya muestra ni usa `etapaCodigo` para
  decidir colores, íconos, textos o pasos; solo `etiqueta` y
  `accionesDisponibles`. Las etiquetas del Estudiante están en
  `ETIQUETAS_ESTUDIANTE` (`core/constantes/etiquetas-academicas.ts`) y la
  insignia sale de `claseInsigniaEtiquetaEstudiante`. Su historial no muestra
  la etapa de cada fila. El backend además le enmascara la decisión del Decano
  (P40 y P41 de `docs/database/etapas-por-proceso.md`).
- Tras cada acción el componente de proceso llama a `recargar()`, que vuelve a
  pedir el detalle y el historial; así cambian la etapa, los anexos y los
  botones.
- Cancelación de Asignatura: el Estudiante ve solo código y nombre antes de
  la etapa final, y con la etiqueta "Aprobada" o "Rechazada" también la
  decisión del Decano por asignatura, nunca su observación. Las columnas sin
  ningún dato se ocultan (también en la vista del Estudiante de Matrícula).
- Faltas y nota (las dos cancelaciones): texto o número de hasta 20
  caracteres. Si es número se valida como antes (`errorDeFaltas`,
  `errorDeNotaAsignatura`); la regla "nota menor a 3.0 no cumple" solo aplica
  a una nota numérica (`numeroDeValor`). Se envían tal como se escriben,
  recortadas; el backend normaliza "3,5" a "3.5".
- Bandejas (D-F28): la columna Estado (Decisión en el Decano) se pinta con
  ícono y texto según `categoriaEstado(etiqueta)`
  (`core/constantes/etiquetas-academicas.ts`), en los tres roles y sin mirar
  `etapaCodigo`: reloj ámbar (Pendiente), flechas azules (En Gestión, En
  trámite, verificación), flecha morada (por notificar, enviar recibo), check
  verde (Aprobada, Aprobada por el Decano, Respondida), x roja (Rechazada,
  Rechazada por el Decano) y exclamación ámbar (Pendiente de pago). Desde
  D-F36 los estados del Funcionario son Pendiente, En Gestión, Pendiente
  Notificar, Enviar Recibo, Recibo Pagado (flechas azules) y Respondida; la
  decisión del Decano ya no es un estado. El detalle del Funcionario muestra el bloque
  "Decisión del Decano" con la observación que trae la última fila
  `APROBAR_DECANO` o `RECHAZAR_DECANO` del historial.

### Diálogos

Todas las confirmaciones y formularios usan `GenericDialogFormComponent`. La
extensión le agregó el `@Input() textoBoton` (por defecto `Guardar`, con el
ancho de 70px de siempre, así los diálogos de Julián no cambian) (D-F12), y en
T12.2-C `textoCancelar` y `cerrarConEscape` (D-F31), que los diálogos de los
procesos pasan como "Cancelar" y `true`.
Nuestros diálogos pasan un texto por acción: "Rechazar solicitud",
"Remitir al Decano", "Aprobar solicitud", "Aceptar evaluación" y "Enviar
decisión" (Decano en Cancelación de Asignatura), "Enviar respuesta", "Enviar recibo", "Subir comprobante",
"Aprobar comprobante", "Rechazar comprobante", "Crear", "Guardar cambios",
"Cambiar responsable" y "Cargar archivo". Cada envío lleva su propia bandera
`enviando` que bloquea el doble envío (D-F7).

El campo de observación de cada acción sale de `campoObservacion`
(`core/constantes/etiquetas-academicas.ts`), con la misma etiqueta en los tres
procesos (D-F35), y se valida con `errorDeObservacion`: "Observación solo para
el estudiante (se entrega como respuesta)" obligatoria al rechazar en el
Funcionario y al rechazar el comprobante, y opcional al enviar la respuesta
(hasta 500); "Observaciones para el Decano" opcional al remitir, y es el único
campo de observación al remitir (D-F36); "Observación (opcional)" al aprobar y rechazar
en el Decano, con el aviso "Esta observación solo será visible para el
Funcionario Académico."; y "Observación" obligatoria al rechazar el
comprobante. Cancelación de Asignatura cambia la obligatoriedad según la tabla
(ver D-F35). Las situaciones académicas salen del catálogo, sin lista fija.

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
- Edición de estudiantes y funcionarios: `datosUsuarioDe` llena el formulario
  con los valores actuales, `cambiosDeTexto` arma el `PUT` solo con los campos
  que cambiaron (uno vacío no cambia) y `validarCambiosPersonales` valida solo
  esos campos.

### Decisiones del frontend

| Decisión | Contenido |
| --- | --- |
| D-F1 | Modelos como `interface` con el nombre exacto del DTO del backend |
| D-F2 | Ramas nuevas en el login, solo agregando líneas. Orden: Secretario General, Funcionario, Secretaria Decanatura FIET, Decano a `/decano`, Funcionario Académico a `/funcionario-academico`, Estudiante a `/estudiante`, el resto a `usuariosFiet` |
| D-F3 | Carpetas de rol nuevas `core/estudiante`, `core/funcionario-academico` y `core/decano`, con el patrón de tres niveles (página, wrapper, contenido) |
| D-F4 | La Resolución se descarga como `Resolucion-<radicado>.pdf`; los anexos, con el `nombreArchivo` del DTO |
| D-F5 | El tamaño de los archivos se valida en nuestros componentes, porque el componente de Julián no lo hace |
| D-F6 | `InputAnexoUploadComponent` se reutiliza con un adaptador (`comoTipoAnexo`); jpg y jpeg son equivalentes; 5 MB por archivo y 20 MB por envío se validan en nuestro componente. Su mensaje "Este anexo es obligatorio" es de Julián y se deja como está |
| D-F7 | Doble envío bloqueado con una bandera propia en cada componente |
| D-F8 | 401 fuera del login: cerrar sesión, avisar una vez y redirigir a `/login`. Resuelta en T10.8 con `SesionVencidaService` |
| D-F9 | PENDIENTE_PAGO y EN_VERIFICACION_PAGO con insignia amarilla |
| D-F10 | Punto de extensión del detalle: `COMPONENTES_PROCESO`, una línea por proceso, carga diferida y cuatro inputs (`uuidSolicitud`, `solicitud`, `rol`, `recargar`). Registrados los tres procesos |
| D-F11 | Cancelación de Matrícula: advertencia de primer periodo visible para todos, con casilla obligatoria, solo de interfaz (no se envía al backend). Cancelación de Asignatura no la tiene |
| D-F12 | Texto del botón de los diálogos por acción, con `Guardar` por defecto. Resuelta en T10.8 con el input `textoBoton` de `GenericDialogFormComponent` |
| D-F13 | Sin aviso previo de solicitud en curso; se muestra el error del backend |
| D-F14 | Las pruebas del frontend no llaman al backend real: specs con `HttpTestingController` o espías |
| D-F15 | Los programas salen siempre del catálogo `GET catalogos-academicos/programas`, sin lista fija; el select muestra "Nombre (SIGLA)" y los diálogos de Excel listan los nombres tal como los trae el catálogo |
| D-F16 | El código de la asignatura no se pide ni se edita: lo genera el backend y el toast de alta lo muestra. La edición manda solo `{nombreAsignatura}` |
| D-F17 | Alta de estudiante: programa primero; asignaturas en un `<fieldset>` deshabilitado hasta tener programa con asignaturas; select por fila sin repetidas. Cambiar el programa con filas llenas pide confirmación en línea dentro del diálogo (no un segundo diálogo) y limpia las filas al confirmar |
| D-F18 | El programa del estudiante no se edita en ninguna pantalla. La matrícula desde el detalle elige entre las asignaturas del programa del estudiante; si ya la tiene activa lo decide el backend |
| D-F19 | `CargaExcelAcademicaComponent` se generaliza con entradas opcionales (`reglas`, `tituloValores`, `valores`); los usos que no las pasan se ven igual que antes |
| D-F20 | La raíz (`/`) pasa por `inicioGuard`: sin sesión va a `/login`; con token y usuario guardados va al inicio de su rol con `rutaDeInicio` (`core/utils/rutas-por-rol.ts`), la misma función que usa el login. `usuario-publico/solicitudes` sigue existiendo pero ya no es la entrada |
| D-F21 | El login es un `<form>` con `(ngSubmit)` y botón `type="submit"` (`SimpleButtonComponent` recibe `tipo`); Enter envía y una bandera `enviando` bloquea el doble envío |
| D-F22 | Tras una carga Excel exitosa el diálogo muestra solo el resultado (y, si se pasa `describir`, la lista de lo creado, como los códigos de asignatura); el botón pasa a "Cerrar" y la tabla se refresca al cerrar |
| D-F23 | En las vistas del Estudiante nunca se muestra ni se usa `etapaCodigo`; solo `etiqueta` y `accionesDisponibles` |
| D-F24 | La edición de estudiante y funcionario académico abre con los valores actuales y envía solo lo que cambió; `DatosUsuarioAcademicoComponent` recibe `edicion` para ocultar usuario y contraseña y quitar los obligatorios |
| D-F25 | Los procesos a cargo se ven de solo lectura al editar un funcionario: el `PUT` no los recibe y se cambian en la pestaña Responsable por proceso |
| D-F26 | La facultad del estudiante es siempre FIET: se muestra de solo lectura y no se envía |
| D-F27 | Rediseño visual (T12.2-B): tokens en `:root` de `src/styles.css` y el resto del estilo en el CSS de cada componente compartido, que carga en diferido, para no subir el bundle inicial. Fuente `var(--fuente)` en todos los `.css` |
| D-F28 | Estado de las bandejas con ícono y texto por `categoriaEstado(etiqueta)`, nunca por `etapaCodigo`; sin insignia aparte de decisión del Decano |
| D-F29 | `TableGenericComponent` acepta `cellTemplate` opcional por columna (contexto `$implicit` = fila, `valor` = celda); sin plantilla pinta el texto como antes |
| D-F30 | `SidebarComponent` acepta `children` por ítem (submenú desplegable con ">") y resalta la ruta más específica que coincide con la URL. El Decano agrupa Solicitudes académicas y Administración académica en "Gestión académica" |
| D-F31 | Entradas opcionales nuevas en componentes de Julián, sin cambiar a quien no las pasa: `SimpleButtonComponent.contorno`, `GenericDialogFormComponent.textoCancelar` y `cerrarConEscape`, `TableGenericComponent.encabezadoAgrupado` |
| D-F32 | El estilo compartido de detalle, procesos y formularios vive en `shared/pages/content/estilos-academicos.css` y `estilos-formularios.css`, que cada componente incluye en `styleUrls` (carga diferida, no toca el bundle inicial) |
| D-F33 | Los formularios del Estudiante siguen siendo de una sola página; el stepper solo guía. Los datos del primer bloque salen del usuario de la sesión, no del formulario |
| D-F34 | Catálogo del Estudiante en `/estudiante/catalogo` (ítem "Solicitudes" del menú), con los textos oficiales en `TRAMITES_ESTUDIANTE`. Sus botones son `<a href>` con `router.navigateByUrl` en `(click)`, no `routerLink`: `RouterLink` subía 4,5 kB el bundle inicial. `BotonSidebar.activaEn` (opcional) marca un ítem en rutas que no cuelgan de la suya |
| D-F35 | Observaciones y decisión por asignatura (T12.10). Una sola observación por solicitud al remitir, sin campo bajo la tabla (etiqueta y obligatoriedad actualizadas en D-F36). Decano: interruptor Aprobar / Rechazar por fila que parte de `cumpleCondiciones`; "Aceptar tal como está" (`aceptarEvaluacion`, solo sin cambios) o "Enviar decisión modificada" (`decisiones`, solo con cambios, y `decano/rechazar` si todas quedan en Rechazar); sin situación al cancelar ni observación por asignatura. Funcionario: "Cambiada por el Decano" en la tabla; la respuesta pide situación a las aprobadas sin ella y observación a las rechazadas cambiadas (`ajustes`), y la observación general es obligatoria con cualquier rechazo del Decano. Código estudiantil del nivel superior del detalle |
| D-F36 | Ajustes posteriores a T12.10 (P46 del backend). Al remitir, "Observaciones para el Decano", siempre opcional (500): ya no se copia a `observacionEvaluacion` (va nula) y la tabla no tiene la columna "Observación de la evaluación"; el historial del detalle no muestra la observación de `REMITIR_DECANO`, que queda solo en la tarjeta "Observación para el Decano". El Funcionario ve un aviso arriba de la tabla si el Decano cambió alguna decisión. El Estudiante, en la etapa final de Cancelación de Asignatura, ve "Situación académica al cancelar" de las aprobadas. El formulario del Estudiante muestra su código estudiantil (`codigoEstudiantil` del `GET .../formulario`). La bandeja del Funcionario suma la columna Código, y sus estados son Pendiente, En Gestión, Pendiente Notificar, Enviar Recibo, Recibo Pagado y Respondida (datos del backend); la decisión del Decano no es un estado (ver D-F37) |
| D-F37 | T12.11 (P47 del backend). La columna "Decisión del Decano" que se agregó en la bandeja del Funcionario se quitó en T12.12 (D-F38); `decisionDecano` sigue en el modelo del resumen pero no se muestra. En "Enviar respuesta" de Cancelación de Asignatura solo las aprobadas sin situación guardada o cambiadas por el Decano llevan selector de situación; las rechazadas no llevan campo propio ni van en `ajustes`, y el motivo va en la observación general, obligatoria si hay alguna rechazada o el Decano rechazó la solicitud |
| D-F38 | T12.12. El Funcionario tiene dos vistas de la misma bandeja (`BandejaSolicitudesAcademicasComponent` con `@Input() modo`, `solicitudes` por defecto), sin duplicar el componente: "Solicitudes" (`funcionario-academico/solicitudes`) muestra solo las no finalizadas (`etapaCodigo` fuera de APROBADA y RECHAZADA), así que su filtro de estado ya no ofrece "Respondida"; "Respuestas" (`funcionario-academico/respuestas`, `data.modo = 'respuestas'`, página propia con la miga "Respuestas") muestra solo las finalizadas, con la tarjeta "Solicitudes respondidas", las mismas columnas y filtros, y en Estado "Aprobada" o "Rechazada" según `etapaCodigo` con los íconos de siempre; su filtro de estado ofrece solo esas dos. `FunAcadSolicitudesContentComponent` lee el modo de `ActivatedRoute.data` como signal y la bandeja lo recalcula en `ngOnChanges` (T12.12b). El ojo abre el mismo detalle (`funcionario-academico/solicitudes/:uuid`). El menú agrupa las dos opciones bajo "Gestión de solicitudes" con los `children` del sidebar. El modo solo afecta al Funcionario. Sin columna "Decisión del Decano"; el bloque del detalle se queda |
| D-F39 | T12.13 (P48 del backend), agrupado en T12.14. En Respuestas, cada Cancelación de Asignatura da como máximo dos filas, agrupando `resultadosAsignaturas` por `aprobada`: una "Aprobada" si hay alguna aprobada y una "Rechazada" si hay alguna rechazada (una sola si todas coinciden), sin columna nueva. Matrícula, Supletorio y una Cancelación de Asignatura sin resultados siguen con una fila y el estado por `etapaCodigo`. El filtro de estado se aplica por fila; tipo, documento y código se aplican a la solicitud y a todas sus filas. Clave por fila: `uuidSolicitud:resultado` (en el ojo, `data-clave`). El pie cuenta solicitudes distintas, no filas. La fila agrupada abre el detalle con `?resultado=aprobada` o `?resultado=rechazada` (`PARAMETRO_RESULTADO`, `RESULTADOS_FILA`, `resultadoFilaValido`); `CancelacionAsignaturaProcesoComponent` lo lee de forma reactiva y muestra todas las asignaturas: las del resultado de la fila primero y resaltadas, las demás después, atenuadas y con "Otra asignatura de la solicitud", y arriba de la tabla "Se resaltan las asignaturas aprobadas (o rechazadas) de la solicitud". El grupo de cada asignatura se calcula como en el backend (aprobada solo con `aprobadaPorDecano` verdadero), pero la celda "Asignatura" no lleva distintivo de resultado (quitado en T12.15b): el resultado de cada asignatura se lee en la columna "Decisión del Decano" (Aprobada / No aprobada), a la derecha, que ven el Funcionario, el Decano y el Estudiante en etapa final. Si el Funcionario rechazó antes del Decano, ninguna asignatura tiene decisión y esa columna no aparece; el estado de la solicitud dice Rechazada. El resaltado solo aplica al Funcionario en etapa final. Sin parámetro, con uno inválido, sin asignaturas de ese resultado o fuera de etapa final, la tabla se ve como desde Solicitudes. El resto del detalle y Solicitudes no cambian |
| D-F40 | Historial sin comentarios (T12.14, punto 6). Cada fila del historial muestra solo la acción, el usuario, la fecha y, fuera del Estudiante, la insignia de etapa; ninguna observación, para los tres roles y los tres procesos. Las observaciones viven en sus bloques: "Observación para el Decano" (`REMITIR_DECANO`, Funcionario y Decano), "Decisión del Decano" (última `APROBAR_DECANO` o `RECHAZAR_DECANO`, ahora también para el Decano, que si no dejaría de ver su propia observación) y "Mensaje para el estudiante" (última `ENVIAR_RESPUESTA`, `RECHAZAR_FUNCIONARIO` o `RECHAZAR_COMPROBANTE`, para los tres roles; las tres cierran la solicitud, así que solo aplica una). Antes, el rechazo del Funcionario y el del comprobante del supletorio solo se veían en el historial. Sin cambios en el backend |
| D-F41 | Formatos de Examen Supletorio (T12.16). Son archivos fijos en `public/formatos/` (`formato-firma-docente.doc` y `formato-firma-docente-cruce.doc`), publicados por el `assets` de `angular.json` en `formatos/`; no se generan ni se editan. `EnlaceFormatoComponent` (`shared/others`) pinta "Descargar formato" como `<a>` con `href` relativo `formatos/<archivo>` (resuelve contra `<base href>`) y `download` con el nombre del archivo: sin router, sin otra pestaña. Constantes en `core/constantes/formatos-supletorio.ts`; el formulario elige el archivo por el nombre del tipo de anexo (`FORMATOS_POR_ANEXO`) y la ficha del catálogo por `AnexoCatalogo.formato`. El nombre que ve el estudiante, "Formato firmado por el docente que orienta la asignatura", viene del backend (`TIPO_ANEXO_ACADEMICO`), que lo cambió en el seed y en `AnexoAcademicoConstantes.FORMATO_FOR_23` |
| D-F42 | Remitir el supletorio sin casilla (T12.16, P49 del backend). El diálogo "Remitir al Decano" de Examen Supletorio solo tiene el resumen, el campo opcional "Observaciones para el Decano" y los botones; ya no hay casilla de requisitos verificados ni su validación, y `RemisionSupletorioDTOPeticion` es `{observacion}`. Los diálogos de remitir de las cancelaciones no tenían casilla equivalente: exigen la evaluación de cada asignatura en la tabla |
| D-F43 | Textos informativos de Cancelación de Matrícula (T12.18), sin cambiar reglas ni validaciones, todos en `core/constantes/textos-cancelacion-matricula.ts`. `VIGENCIA_PAZ_Y_SALVOS`: `nota-aviso` solo en la sección de anexos del formulario, antes de los campos de carga; el catálogo del estudiante no la muestra (T12.18b). `NOTA_SIMCA`: `nota-aviso` encima de la tabla de asignaturas de `CancelacionMatriculaProcesoComponent`, solo para Funcionario y Decano; ni el Estudiante ni Cancelación de Asignatura la ven. `NORMATIVA_CANCELACION_MATRICULA`: tarjeta "Normativa aplicable" al final del formulario, antes de los botones, con encabezados en negrita, literales a a d y un cuerpo de alto máximo 260 px con scroll propio (`.normativa` en `estilos-formularios.css`, enfocable con teclado) para no empujar los botones. La advertencia y la casilla del artículo 3 (D-F11) no cambian. Cancelación de Asignatura (T12.19, distribución del formato PA-GA-4.2-FOR-9), en `core/constantes/textos-cancelacion-asignatura.ts`: `ARTICULO_5_CANCELACION_ASIGNATURA` como `nota-aviso` al inicio del formulario, antes del stepper y de los datos del estudiante, con el encabezado en negrita; `NOTIFICACION_CORREO_INSTITUCIONAL` como `nota-gris` justo debajo de la lista de asignaturas (y de su error), antes del motivo, los soportes y los botones; `NOTA_SIMCA` (la misma constante de Matrícula, sin duplicar) justo encima de la tabla del detalle, solo para Funcionario y Decano. El formulario no lleva la nota SIMCA porque el estudiante solo elige asignaturas, no escribe faltas ni nota. Sin bloque "Normativa aplicable" en Cancelación de Asignatura; Matrícula y Supletorio no muestran estos textos |

Otras decisiones tomadas en las tareas, sin número:

- Bandejas: una tarjeta por rol ("Solicitudes remitidas por el Funcionario
  Académico", "Gestión de solicitudes", "Mis trámites radicados"). Filtros de
  tipo de proceso, estado (decisión en el Decano, con las etiquetas presentes)
  y "Número de identificación" (no en el Estudiante), con "Limpiar filtros". El
  Funcionario tiene además "Código estudiantil" (T12.10), en el cliente y con
  coincidencia parcial sobre `codigoEstudiantil`.
  Columnas: Radicado, Estudiante, Documento (`numeroDocumento` del resumen,
  P42 del backend), Código (solo el Funcionario, D-F36), Tipo de proceso,
  Fecha de radicación, Estado y Acciones (ojo); el Estudiante ve Radicado,
  Tipo de proceso, Fecha de radicación, Estado y Acciones. Fecha y Estado se
  ordenan con ⇅. El resumen no trae fechas de remisión ni de decisión, por eso
  se usa la fecha de radicación. La búsqueda cubre radicado, nombre, código y
  documento.
- Detalle (T12.2-C): "← Volver", "Solicitud de {tipo}", "Radicado {número}" y
  la pill de estado por `categoriaEstado(etiqueta)`. Orden de tarjetas: Datos
  generales (solo los campos que llegan del backend; "Número de CC" con
  `numeroDocumento`, P43, solo para Funcionario y Decano), anexos (título por proceso: "Documentación requerida",
  "Soporte adjunto" o "Anexos adjuntos", con contador, y enlaces "Visualizar" y
  "Descargar"), "Observación para el Decano" (Funcionario y Decano), "Decisión
  del Decano" (Funcionario y Decano, D-F40), "Mensaje para el estudiante" (si
  existe, de la última fila `ENVIAR_RESPUESTA`, `RECHAZAR_FUNCIONARIO` o
  `RECHAZAR_COMPROBANTE`, D-F40), las tarjetas del proceso con la tarjeta de
  acciones al final y el historial, sin comentarios (D-F40). El prototipo pone motivo y tabla antes de
  los anexos, pero motivo, tabla y acciones viven en el mismo componente de
  proceso; reordenar solo con CSS rompería el orden del foco, así que el DOM se
  deja así (T12.7). Los botones de rechazo van en contorno rojo
  y el resto en navy; textos y visibilidad siguen saliendo de
  `accionesDisponibles`.
- Las tablas de asignaturas de las dos cancelaciones usan el encabezado
  agrupado del formato de la Facultad (`agruparEncabezados` en
  `core/utils/encabezado-agrupado.ts` y `RUTAS_ENCABEZADO_ASIGNATURAS`). En
  Cancelación de Asignatura "Situación al cancelar" va junto a "Situación en la
  matrícula".
- Tabla de asignaturas en línea (T12.8): cada componente de proceso pinta su
  propia tabla (ya no usa `TableGenericComponent`) con el estilo de
  `shared/pages/content/tabla-asignaturas.css`: encabezado gris en mayúscula
  pequeña y celdas con "—" cuando no hay dato. Si `accionesDisponibles` trae
  `REMITIR_DECANO`, la tabla es editable: No. faltas y Nota como texto, y la
  situación en la matrícula y al cancelar como select del mismo catálogo (al
  cancelar es opcional y viaja como `situacionCancelarUuid`, P44). En
  Cancelación de Asignatura se suma la columna "Cumple condiciones" bajo
  "Evaluación del Funcionario". Al pulsar "Remitir al Decano" se validan las
  filas con los mismos mensajes, que se pintan en la celda; si todo está bien
  se abre el diálogo con un solo campo de observación (D-F35). El Decano y el
  Funcionario en la respuesta final siguen D-F35. Bajo 992 px la tabla hace
  scroll dentro de su tarjeta.
- Modales: barra navy con ×, resumen de la solicitud
  (`ResumenSolicitudComponent`: Estudiante, Número de CC si llega, Código
  estudiantil, Tipo de proceso, Número de radicado), notas grises y botones Cancelar y confirmar. Cierran con
  Escape. Al aprobar, el Decano ve `NOTA_APROBAR_DECANO`.
- Formularios del Estudiante: stepper guía (`StepperFormularioComponent`) sobre
  las secciones que ya existían, todo en una sola página; cada paso queda verde
  cuando su sección es válida y el primero pendiente en navy. El primer bloque
  (`DatosEstudianteSesionComponent`) muestra de solo lectura los datos del
  usuario de la sesión, el código estudiantil que trae el `GET .../formulario`
  (D-F36) y la Facultad FIET, con la nota de DARCA. El Estudiante no tiene
  endpoint para su programa ni su semestre. Pie con "Volver" (a
  Mis solicitudes) y "Radicar solicitud".
- Catálogo del Estudiante (T12.7): tres tarjetas con barra navy, descripción,
  requisitos, anexos requeridos e "Iniciar solicitud →" hacia el formulario que
  ya existía. El menú del Estudiante queda en Mi Usuario, Solicitudes y Mis
  solicitudes, y las migas de los formularios apuntan al catálogo. Los
  formatos de Examen Supletorio se descargan como archivos fijos (D-F41). No existe el
  interruptor de apertura; la solicitud en curso la sigue avisando el backend
  al radicar (D-F13).
- Tras radicar, la pantalla reemplaza el formulario por el radicado y un botón
  "Ver solicitud", y además navega al detalle.
- Examen Supletorio:
  - El uuid de "Recibo de pago" y de "Comprobante de pago" se busca por nombre
    en `GET catalogos-academicos/tipos-solicitud/{uuidTipo}/tipos-anexo`, con
    los nombres de `ANEXOS_PAGO_SUPLETORIO`; no hay uuid fijo.
  - Recibo y comprobante: primero se sube el anexo y luego se llama la acción.
    Si la acción falla después de subir el archivo, el reintento no lo sube otra
    vez.
  - El aviso de comprobante rechazado se deduce de la etapa RECHAZADA (para el
    Estudiante, de la etiqueta "Rechazada") más un anexo de tipo "Comprobante
    de pago" en la solicitud.
  - El plazo de 3 días hábiles (`plazoDiasHabiles`) es solo informativo; el
    vencimiento lo decide el backend.
  - No se envían los campos de las maquetas que el contrato no tiene.
- Administración académica:
  - Una página con cuatro pestañas (Asignaturas, Estudiantes, Funcionarios
    académicos, Responsable por proceso) para el Decano y para el Secretario
    General, en rutas separadas por rol. Programas y asignaturas: ver la
    sección siguiente.
  - Los filtros se aplican con un botón "Buscar", no al escribir.
  - El paginador de Julián es de base 1; se convierte a base 0 al llamar al
    backend, y la tabla recibe solo la página actual.
  - El responsable de un proceso se elige entre los primeros 100 funcionarios
    académicos (`paginado` con tamaño 100); el diálogo avisa si hay más.
  - No hay borrado: el contrato no tiene endpoints para eso.

### Programas académicos y asignaturas

Hay cinco programas fijos (IET, ISI, IAI, TTE, IAC). El frontend los toma
siempre de `GET catalogos-academicos/programas`
(`CatalogoAcademicoService.getProgramas`); no hay una lista fija en el código.
Las opciones del select se arman con `opcionesDePrograma` y las de asignaturas
con `opcionesDeAsignatura` (`core/utils/programas-academicos.ts`).

- Asignaturas: la tabla muestra código, nombre y programa. Filtros de texto y
  de programa (opción "Todos", que no manda el parámetro) con "Buscar". Al crear
  se piden nombre y programa; el código lo genera el backend (`ISI-001`) y el
  toast lo avisa: "Asignatura creada con el código ISI-001". Al editar solo se
  envía `{nombreAsignatura}`; código y programa se ven de solo lectura.
- Carga Excel de asignaturas con `CargaExcelAcademicaComponent`: encabezados
  `nombreAsignatura, programaAcademico`, los nombres válidos del catálogo y la
  regla de que el código se genera solo.
- Estudiantes, alta: el select de programa va primero. La sección de
  asignaturas es un `<fieldset>` deshabilitado hasta que hay programa y este
  tiene asignaturas (`GET asignaturas/programa/{uuid}`). Cada fila es un select
  "código - nombre" más el grupo; una asignatura elegida en una fila no aparece
  en las demás y la validación también rechaza repetidas. Si el programa no
  tiene asignaturas se avisa que deben crearse primero en la pestaña
  Asignaturas. Cambiar el programa con filas llenas pide confirmación dentro del
  diálogo ("Cambiar programa" o "Conservar el anterior") y, si se confirma,
  limpia las filas. Se envía `uuidProgramaAcademico` y `asignaturas` como
  `{codigoAsignatura, grupo}`.
- Estudiantes, alta: la facultad se muestra fija, FIET, y no se envía.
- Estudiantes, edición: nombres, apellidos, tipo y número de documento,
  teléfono, correo, código estudiantil y semestre; usuario, programa y
  facultad de solo lectura (D-F24, D-F26). El error de código 2 (unicidad) se
  muestra dentro del diálogo.
- Funcionarios académicos: el campo es `cargo` (antes `dependencia`) en
  tabla, filtro (`?cargo=`), alta, edición y Excel. La edición cambia los datos
  personales y el cargo (D-F24, D-F25).
- Estudiantes, detalle: "Agregar asignatura" elige entre las asignaturas del
  programa del estudiante y el grupo. Si ya la tiene activa, lo dice el backend.
- Excel de estudiantes: 13 encabezados (sin `nombreAsignatura` ni `facultad`), los nombres
  válidos de programa y la regla de que cada `codigoAsignatura` existe y es del
  programa.
- Las pantallas que muestran el programa de un estudiante (tabla de
  estudiantes, detalle de una solicitud) usan `programaAcademico`, que sigue
  siendo el nombre.
- Los catálogos que abren un formulario (programas para el alta y el Excel) se
  piden al abrir si todavía no están; si fallan o vienen vacíos, error por
  `ErrorHandlerService` y el diálogo no abre.

`CargaExcelAcademicaComponent` recibe además, opcionales, `reglas` (lista de
textos), `tituloValores` y `valores` (lista de valores permitidos). Los
funcionarios académicos lo siguen usando solo con `regla`.

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
  la consulta del usuario, sin mensaje y sin limpiar la sesión. Lo usan todas
  las rutas protegidas; no se cambió.
- Por debajo de 992 px el encabezado de Julián (`navbar-expand-lg` con
  `navbar-collapse` y sin botón para desplegarlo) oculta el avatar y con él
  "Cerrar sesión".
- El menú del chip de usuario tiene "Mi usuario" (inicio del rol actual con
  `rutaDeInicio`) y "Cerrar sesión"; ya no lista los roles.
- Mi usuario muestra solo lo que entrega la API: datos del usuario y roles; el
  Funcionario Académico suma Cargo y procesos asignados
  (`GET funcionarios-academicos/{uuid}`). El Estudiante no tiene endpoint para
  leer su programa ni su semestre, y ningún DTO trae la fecha de creación
  ("Te uniste el"), así que no se muestran.
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

