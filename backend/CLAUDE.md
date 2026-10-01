# Backend — Gestión de Procesos Académicos FIET

Spring Boot 3, Java 17, MySQL 8. Hereda la arquitectura hexagonal (puertos y
adaptadores) de Julián Camacho tal cual está en `solicitudes/src/main/java/
com/unicauca/cfiet/solicitudes`. Todo lo que se agrega acá sigue exactamente
esa misma estructura de paquetes — no se introduce una variante ni un
patrón distinto para los módulos nuevos. Nada de Julián se modifica para
lograr esto.

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
cfiet` y pegar el contenido de `data.sql`).

La base usa `spring.jpa.hibernate.naming.physical-strategy=
PhysicalNamingStrategyStandardImpl`: Hibernate NO convierte a snake_case,
las tablas reales quedan en camelCase exacto al nombre del campo Java
(`usuarios`, `usuariosLivianos`, `tiposUsuario`), nunca en mayúsculas ni
con guiones bajos. Esto importa al escribir SQL a mano contra esas tablas.

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

JWT sin estado (`SessionCreationPolicy.STATELESS`). Las reglas de acceso
por URL están centralizadas en `ConfiguracionSeguridad.securityFilterChain`
(`infraestructura/configuracion/seguridad/configuracion`) — cualquier
endpoint nuevo bajo `${url.application}` (que resuelve a
`/api/unicauca/fiet/consejo/`) tiene que agregar ahí su regla
(`permitAll()`, `authenticated()` o `hasAnyAuthority(...)` con las
constantes de `ApplicationConstantes`). Dentro del controlador, además, se
usa `@PreAuthorize(...)` sobre cada método cuando aplica. Las dos cosas van
juntas, no una sola.

`ApplicationConstantes` solo tiene roles de Julián (`SECRETARIO_GENERAL`,
`DECANO`, `FUNCIONARIO_ROL`) y los perfiles compuestos
`SECRETARIO_DECANO_ACCESO` / `SECRETARIO_DECANO_FUNCIONARIO_ACCESO`. No
existen constantes para "Estudiante" ni para "Funcionario Académico" — hay
que agregarlas ahí (siguiendo el mismo patrón de constante de rol +
constante de `hasAnyAuthority(...)` armada con ellas) antes de poder
proteger un endpoint de los procesos académicos nuevos con
`@PreAuthorize`.

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
`docs/database/script_bd_extension.sql` — son FK hacia las tablas reales de
Julián (`usuarios`, `usuariosLivianos`, `tiposUsuario`, todas en camelCase,
no en mayúsculas), que no se modifican. `FUNCIONARIO_ACADEMICO` es
intencionalmente una tabla distinta de `Funcionario`/`funcionarios` (el rol
de comité de facultad de Julián) — cubre tanto al funcionario que verifica
la información académica como al decano que aprueba o rechaza,
diferenciados por la columna `dependencia`.

## Qué no se hace

- No se agrega Javadoc, ni comentarios de una línea, ni emojis en ningún
  archivo nuevo o modificado.
- No se cambia la arquitectura de capas ni se salta una capa (por ejemplo,
  un controlador llamando directo a un repositorio).
- No se tocan las tablas ni las clases ya existentes de Julián salvo que la
  tarea lo pida explícitamente.
- No se usan anotaciones `@Autowired` en campos — la inyección es siempre
  por constructor (`@RequiredArgsConstructor` de Lombok en los `@Service`/
  `@Component`, o constructor explícito en los casos de uso, que no son
  beans de Spring por sí mismos sino que se registran a mano en
  `BeanConfiguracion`).
