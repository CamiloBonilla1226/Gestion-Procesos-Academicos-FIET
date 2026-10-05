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
`Estudiante` y `Funcionario Académico` y sus tipos de usuario se agregan en
ese mismo `data.sql`. Su última sentencia (`INSERT INTO Usuario_has_Roles`)
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
nuevos son `Estudiante` y `Funcionario Académico`; su constante y sus filas
en `data.sql` (`roles` y `tiposUsuario`) se crean juntas, porque
`crearUsuario` busca el tipo de usuario por nombre y falla si no existe.

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
`FUNCIONARIO_ACADEMICO` e `HISTORIAL_SOLICITUD_ACADEMICA`; `usuariosLivianos`
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
