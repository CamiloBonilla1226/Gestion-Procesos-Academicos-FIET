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

Sin Docker: JDK 17 + MySQL 8 local, ajustando `DB_URL`, `DB_USER_NAME`,
`DB_PASSWORD` en `solicitudes/src/main/resources/application.properties`.

`spring.sql.init.mode=never` — el `data.sql` (roles, tipos de usuario,
usuario root) no se ejecuta solo, hay que correrlo a mano contra la base
la primera vez (`docker compose exec cfiet_database mysql -u root -pmysql
cfiet` y pegar el contenido de `data.sql`). Hay que correrlo después del
primer arranque, porque necesita las tablas que crea Hibernate. Los roles
`Estudiante` y `Funcionario Académico` y el tipo de usuario `Estudiante` se
agregan en ese mismo `data.sql`; el Funcionario Académico usa el tipo
`Empleado FIET - Funcionario` de Julián, que ya existe. Su última sentencia (`INSERT INTO Usuario_has_Roles`)
no termina en `;`: hay que cerrarla antes de agregar sentencias nuevas.

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
│                     ValidadorActorSolicitud, ValidadorArchivoAdjunto), sin Spring ni JPA
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
│   └── mapeador/          Mapper*InfraestructuraDominio (DTO <-> modelo de dominio, con ModelMapper)
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
  (`IJwtServicio`, `OrdenDelDiaExportador`, `AlmacenadorArchivos`).
- Orden de los argumentos del `new`: es el mismo orden de los parámetros del
  método, que a su vez es el orden del constructor del caso de uso. No hay un
  orden común entre casos de uso (`RolCUImplAdaptador` recibe gateway,
  formateador, log; `SesionCUImplAdaptador` recibe gateway, log, formateador;
  `LogCUImplAdaptador` recibe gateway, formateador, jwt). Se copia el orden
  del constructor del caso de uso nuevo, no el de otro bean.
- Dependencia entre beans de la misma clase: no hay ninguna inyección
  explícita de un método `@Bean` a otro (ningún método llama a otro ni recibe
  un `XCUImplAdaptador` como parámetro). La única dependencia implícita es
  `LogCUIntPuerto`: la satisface el bean `crearLogCU`
  (`LogCUImplAdaptador implements LogCUIntPuerto`) y la reciben `Rol`,
  `Usuario`, `Sesion`, `TipoSolicitud`, `OrdenDelDia`, `Solicitud` y
  `Respuesta`. Spring resuelve el orden de creación solo; un caso de uso
  nuevo que registre historial pide `LogCUIntPuerto` como parámetro y listo.
  Lo mismo pasa con los casos de uso que crean Estudiante y Funcionario
  Académico: piden `UsuarioCUIntPuerto` como parámetro, y lo satisface el
  bean `createUsuarioCU` (ver "Crear Estudiante y Funcionario Académico").
- Todo el resto de parámetros (gateways, `IJwtServicio`,
  `OrdenDelDiaExportador`, `AlmacenadorArchivos`) son `@Service` definidos
  fuera de esta clase.

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
es una regla de negocio violada sino un fallo técnico de almacenamiento. Se
sigue ese mismo criterio para casos de uso nuevos que también manejen
archivos: fallo de negocio -> `ExcepcionesFormateadorIntPuerto`; fallo
técnico de IO -> `RuntimeException` con el mensaje y la causa original.

## Seguridad

JWT sin estado (`SessionCreationPolicy.STATELESS`). La única protección
real de acceso son los `requestMatchers` de
`ConfiguracionSeguridad.securityFilterChain`
(`infraestructura/configuracion/seguridad/configuracion`).

`@PreAuthorize` no protege nada en este proyecto: la clase solo tiene
`@EnableWebSecurity` y no existe `@EnableMethodSecurity` en ninguna parte,
así que Spring no evalúa las anotaciones de método. Lo confirmó la prueba
`backend/pruebas/t1_preauthorize.ps1`: un usuario con rol `Funcionario`
recibe 200 en `GET usuarios`, `usuarios/paginado` y `usuarios/funcionarios`,
aunque los tres llevan `@PreAuthorize(SECRETARIO_DECANO_ACCESO)`, porque
la regla `GET usuarios/**` es `authenticated()`.

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

`ApplicationConstantes` solo tiene roles de Julián (`SECRETARIO_GENERAL`,
`DECANO`, `FUNCIONARIO_ROL`) y los perfiles compuestos
`SECRETARIO_DECANO_ACCESO` / `SECRETARIO_DECANO_FUNCIONARIO_ACCESO`. No
existen constantes para "Estudiante" ni para "Funcionario Académico" — hay
que agregarlas ahí (siguiendo el mismo patrón de constante de rol +
constante de `hasAnyAuthority(...)` armada con ellas) para usarlas en el
`requestMatchers` de `ConfiguracionSeguridad` y en el `@PreAuthorize`
que documenta el endpoint.

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
`anyRequest()`). Hoy `GET usuarios/**` está abierto a cualquier usuario
autenticado, y `POST`/`PUT` sobre `usuarios/**` está restringido a Secretario
General y Decano, que son quienes crean estudiantes y funcionarios
académicos.

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
`docs/database/script_bd_extension.sql` (18 tablas; referencia, no se ejecuta). Las únicas FK hacia
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
  para que la operación siga siendo atómica cuando T6 junte solicitud y anexos.
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
expone consulta y archivos; el cambio de etapa no tiene endpoint todavía (T6 a
T8). Todos sus métodos llevan `@Transactional` (los GET `readOnly`), porque
leer el usuario recorre colecciones perezosas.

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
  directo en MySQL (`docker compose exec -T cfiet_database mysql ...`), porque
  todavía no hay endpoint para radicar, y al final borra solicitudes,
  usuarios, logs de esos usuarios y carpetas de `uploads/anexos`.

### Radicación de Cancelación de Matrícula

- `CancelacionMatriculaCUImplAdaptador.radicarCancelacionMatricula(uuidEstudiante,
  motivo, anexos, token)` (bean `crearCancelacionMatriculaCU`, sin endpoint
  hasta T6.3). `anexos` es una lista de `AnexoRadicacion` (uuid del tipo de
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
  `crearTramiteCancelacionMatriculaCU`, sin endpoints hasta T6.3), con el
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
    `numeroFaltas` (>= 0), `nota` (0.0 a 5.0 con un decimal) y
    `uuidSituacionMatricula`. `cambiarEtapa` borra el escaneo viejo (T5.7a).
  - `aprobarPorDecano(uuid, situacionesAlCancelar, token)`:
    EN_REVISION_DECANO a APROBADA_POR_DECANO. Cada
    `SituacionCancelarAsignatura` trae `uuidAsignaturaSolicitud` y
    `uuidSituacionCancelar` (P9: la registra el Decano).
  - `rechazarPorDecano(uuid, observacion, token)`: EN_REVISION_DECANO a
    RECHAZADA_POR_DECANO, sin tocar las situaciones.
  - `enviarRespuesta(uuid, token)`: APROBADA_POR_DECANO a APROBADA o
    RECHAZADA_POR_DECANO a RECHAZADA, con escaneo. Solo en APROBADA las
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
| `POST /{uuid}/funcionario/responder` | Funcionario Académico | Sin cuerpo |
| `POST /{uuid}/decano/aprobar` | Decano | Cuerpo `situaciones` (`asignaturaSolicitudUuid`, `situacionCancelarUuid`) |
| `POST /{uuid}/decano/rechazar` | Decano | Cuerpo `observacion` |

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
  `crearCancelacionAsignaturaCU`, sin endpoint hasta T7.3).
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
  `crearTramiteCancelacionAsignaturaCU`, sin endpoints hasta T7.3) tiene las
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
  `cumpleCondiciones` (obligatorio) y `observacionEvaluacion`. Una nota menor
  a 3.0 no puede venir con `cumpleCondiciones` verdadero; si no cumple, la
  observación es obligatoria (máximo 255); al menos una debe cumplir, si no
  el error pide rechazar la solicitud.
- `aprobarPorDecano(uuid, decisiones, token)`: cada `DecisionAsignatura` trae
  `aprobada`, `uuidSituacionCancelar` y `observacionDecision`. Solo se
  aprueba una asignatura que cumple; rechazar exige observación; la
  situación al cancelar se guarda solo en las aprobadas; al menos una
  aprobada, si no el error pide usar `rechazarPorDecano`. La etapa pasa por
  APROBAR_DECANO como en matrícula.
- `rechazarPorFuncionario` y `rechazarPorDecano` actúan sobre la solicitud
  completa, como en matrícula.
- `enviarRespuesta`: en APROBADA pasan a `cancelada` solo las asignaturas con
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
| `POST /{uuid}/funcionario/responder` | Funcionario Académico | Sin cuerpo |
| `POST /{uuid}/decano/aprobar` | Decano | Cuerpo `decisiones` (`asignaturaSolicitudUuid`, `aprobada`, `situacionCancelarUuid`, `observacionDecision`) |
| `POST /{uuid}/decano/rechazar` | Decano | Cuerpo `observacion` |

El resto de `cancelaciones-asignatura/**` es `denyAll()`.

- `ConsultaCancelacionAsignaturaCUImplAdaptador` (bean
  `crearConsultaCancelacionAsignaturaCU`) saca el estudiante del token y arma
  el detalle sobre `ConsultaSolicitudAcademicaCUIntPuerto.getDetalle`, con la
  misma visibilidad por `ETAPA_ETIQUETA_ROL` y la misma respuesta de
  inexistente; una solicitud sin fila en `SOLICITUD_CANCELACION_ASIGNATURA`
  también responde como inexistente.
- El Funcionario asignado y el Decano ven de cada asignatura faltas, nota,
  situación en la matrícula, `cumpleCondiciones`, `observacionEvaluacion` y,
  cuando existen, `aprobadaPorDecano`, `observacionDecision` y la situación al
  cancelar. El Estudiante ve solo uuid de la fila, código y nombre; en
  `APROBADA` o `RECHAZADA` ve además `aprobadaPorDecano` y
  `observacionDecision`. Nunca ve `observacionEvaluacion`. La vista del
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
  (bean `crearExamenSupletorioCU`, sin endpoint hasta T8.3). Las fechas son
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
  `crearTramiteExamenSupletorioCU`, sin endpoints hasta T8.3), con el token al
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
  - `remitirADecano(uuid, requisitosVerificados, observacion, token)`:
    RADICADA a EN_REVISION_DECANO. Sin `requisitosVerificados` verdadero se
    rechaza (después de validar actor y etapa) pidiendo rechazar la solicitud
    en lugar de remitirla.
  - `aprobarPorDecano(uuid, observacion, token)`: observación opcional, a
    APROBADA_POR_DECANO. `rechazarPorDecano`: observación obligatoria, a
    RECHAZADA_POR_DECANO.
  - `enviarRespuesta(uuid, token)`: RECHAZADA_POR_DECANO a RECHAZADA, sin
    escaneo.
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
| `POST /{uuid}/funcionario/remitir` | Funcionario Académico | Cuerpo `requisitosVerificados` y `observacion` opcional |
| `POST /{uuid}/funcionario/responder` | Funcionario Académico | Sin cuerpo |
| `POST /{uuid}/funcionario/recibo` | Funcionario Académico | Sin cuerpo; usa `enviarRecibo` |
| `POST /{uuid}/funcionario/comprobante/aprobar` | Funcionario Académico | Cuerpo `fechaAcordadaExamen` opcional |
| `POST /{uuid}/funcionario/comprobante/rechazar` | Funcionario Académico | Cuerpo `observacion` |
| `POST /{uuid}/decano/aprobar` | Decano | Cuerpo `observacion` opcional |
| `POST /{uuid}/decano/rechazar` | Decano | Cuerpo `observacion` |
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
matriculadas.

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

## Tests

`solicitudes/src/test` tiene un único archivo:
`com/unicauca/cfiet/solicitudes/SolicitudesApplicationTests.java`, el test
que genera Spring Initializr:

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
Estudiante. Hoy son 63 filas. El script:

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
