# Backend — Gestión de Procesos Académicos FIET

Spring Boot 3, Java 17, MySQL 8. Hereda la arquitectura hexagonal (puertos y
adaptadores) de Julián Camacho tal cual está en `solicitudes/src/main/java/
com/unicauca/cfiet/solicitudes`. Todo lo que se agrega acá sigue exactamente
esa misma estructura de paquetes y esos mismos sufijos de clase — no se
introduce una variante ni un patrón distinto para los módulos nuevos.

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
la primera vez.

## Las tres capas, en orden de dependencia

```
dominio/            no depende de nada de Spring ni de JPA
├── modelos/         POJOs planos del dominio (sin anotaciones)
├── casosdeuso/       *CUImplAdaptador — implementa los puertos de entrada
└── helper/constantes/ constantes compartidas (roles, etc.)

aplicacion/          interfaces únicamente — son los "puertos"
├── input/            *CUIntPuerto — lo que expone un caso de uso
└── output/            *GatewayIntPuerto — lo que un caso de uso necesita de afuera

infraestructura/     todo lo que depende de Spring, JPA, HTTP, JWT
├── input/controlador<Recurso>/
│   ├── controlador/      *RestController
│   ├── DTOPeticion/       *DTOPeticion (lo que llega en el body)
│   ├── DTORespuesta/      *DTORespuesta (lo que se devuelve)
│   └── mapeador/          Mapper*InfraestructuraDominio (DTO <-> modelo de dominio, con ModelMapper)
├── output/
│   ├── persistencia/
│   │   ├── entidades/      *Entidad (JPA, @Entity)
│   │   ├── repositorios/   *Repositorio extends JpaRepository
│   │   ├── mapeador/ownMapper/ *OwnMapperImpl (Entidad <-> modelo de dominio, a mano, sin ModelMapper)
│   │   └── gateway/        *GatewayImplAdaptador — implementa los *GatewayIntPuerto
│   ├── manejadorExcepciones/
│   │   ├── RestApiExcepcion    @ControllerAdvice, un @ExceptionHandler por excepción propia
│   │   ├── MensajesError       constantes de texto con %s, se usan con String.format
│   │   └── excepcionesPropias/ Error*Excepcion, todas extends RuntimeException
│   └── configuracion/seguridad/ JWT + Spring Security
```

Una petición HTTP entra por el `*RestController` (infraestructura/input),
se mapea de DTO a modelo de dominio, pasa al `*CUIntPuerto` (puerto de
entrada, definido en `aplicacion/input` e implementado por el
`*CUImplAdaptador` en `dominio/casosdeuso`). El caso de uso valida reglas de
negocio y llama al `*GatewayIntPuerto` (puerto de salida, definido en
`aplicacion/output`), cuya implementación real vive en
`infraestructura/output/persistencia/gateway` y habla con JPA a través de
entidades y repositorios. El caso de uso nunca ve una entidad JPA ni un DTO
— solo modelos de dominio.

## Convención de nombres (se respeta siempre, sin excepciones)

| Elemento | Sufijo | Carpeta |
|---|---|---|
| Puerto de entrada | `XCUIntPuerto` | `aplicacion/input` |
| Caso de uso | `XCUImplAdaptador` | `dominio/casosdeuso` |
| Puerto de salida | `XGatewayIntPuerto` | `aplicacion/output` |
| Adaptador de persistencia | `XGatewayImplAdaptador` | `infraestructura/output/persistencia/gateway` |
| Entidad JPA | `XEntidad` | `infraestructura/output/persistencia/entidades` |
| Mapper entidad-dominio | `XOwnMapperImpl` | `infraestructura/output/persistencia/mapeador/ownMapper` |
| Repositorio | `XRepositorio` | `infraestructura/output/persistencia/repositorios` |
| Controlador REST | `XRestController` | `infraestructura/input/controlador<X>/controlador` |
| DTO de entrada | `XDTOPeticion` | `infraestructura/input/controlador<X>/DTOPeticion` |
| DTO de salida | `XDTORespuesta` | `infraestructura/input/controlador<X>/DTORespuesta` |
| Mapper DTO-dominio | `MapperXInfraestructuraDominio` | `infraestructura/input/controlador<X>/mapeador` |

Los nombres de clase, de paquete y de variable van en español, igual que en
el código de Julián (`RolCUIntPuerto`, `getRoles`, `uuidRol`). No se mezcla
con nombres en inglés salvo las palabras reservadas del framework.

## Manejo de errores

Nunca se lanza una excepción de Java directamente desde un caso de uso. Se
usa el puerto `ExcepcionesFormateadorIntPuerto`, que tiene un método por
tipo de error (`lanzarEntidadNoExiste`, `lanzarSinInformacion`,
`lanzarMalFormato`, `lanzarReglaNegocioViolada`, etc.) y arma el mensaje con
las constantes de `MensajesError` vía `String.format`. Si un mensaje nuevo
hace falta, se agrega como constante en `MensajesError`, no como string
literal suelto en el caso de uso. El `RestApiExcepcion` (`@ControllerAdvice`)
ya intercepta todas las excepciones propias — un tipo de error nuevo
necesita su propia excepción en `excepcionesPropias/`, su entrada en
`CodigoError` y su `@ExceptionHandler` correspondiente.

## Seguridad

JWT sin estado (`SessionCreationPolicy.STATELESS`). Las reglas de acceso
por URL están centralizadas en `ConfiguracionSeguridad.securityFilterChain`
— cualquier endpoint nuevo bajo `${url.application}` tiene que agregar ahí
su regla (`permitAll()`, `authenticated()` o `hasAnyAuthority(...)` con las
constantes de `ApplicationConstantes`). Dentro del controlador, además, se
usa `@PreAuthorize(ApplicationConstantes.<ROL>_ACCESO)` sobre cada método
cuando aplica. Las dos cosas van juntas, no una sola.

## Persistencia

Las entidades JPA son `@Entity` con Lombok (`@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder`), nunca anotaciones de
validación de Bean Validation en la entidad — esas van en el DTO de
petición. La relación entidad-dominio se mapea a mano en un
`*OwnMapperImpl` (no con ModelMapper, que sí se usa pero solo entre DTO y
modelo de dominio en la capa de infraestructura/input). Las tablas nuevas
de este trabajo de grado (`ESTUDIANTE`, `FUNCIONARIO_ACADEMICO`,
`ASIGNATURA`, `SOLICITUD_ACADEMICA`, `RESOLUCION_ACADEMICA`, etc.) están
documentadas en `docs/database/diccionario-datos-extension.md` y su DDL en
`docs/database/script_bd_extension.sql` — son FK hacia las tablas de
Julián (`USUARIO`, `USUARIO_LIVIANO`, `TIPO_USUARIO`), que no se modifican.

## Qué no se hace

- No se agrega Javadoc, ni comentarios de una línea, ni emojis en ningún
  archivo nuevo o modificado.
- No se cambia la arquitectura de capas ni se salta una capa (por ejemplo,
  un controlador llamando directo a un repositorio).
- No se tocan las tablas ni las clases ya existentes de Julián salvo que la
  tarea lo pida explícitamente.
- No se usan anotaciones `@Autowired` en campos — la inyección es siempre
  por constructor (`@RequiredArgsConstructor` de Lombok, o constructor
  explícito como en los casos de uso de `dominio/casosdeuso`, que no son
  beans de Spring).
