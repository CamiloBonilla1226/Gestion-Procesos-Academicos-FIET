

===== 02-backend-arquitectura.md =====

# 02 - Backend: arquitectura

Análisis estático de solo lectura del backend (`backend/solicitudes`, Spring Boot 3, Java 17). Etiquetas: [BASE] = presente en el primer commit `d3a48cc` (prototipo de Julián David Camacho Erazo); [EXTENSIÓN] = agregado después. Todo lo no comprobado en ejecución se marca NO VERIFICADO.

## 1. Criterio BASE / EXTENSIÓN y alcance

- Fuente de las cifras: `backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes` (459 archivos `.java`) y `backend/solicitudes/src/test/java` (38 archivos). Las lecturas de `backend/CLAUDE.md` y del `CLAUDE.md` raíz se contrastaron contra el código.
- Criterio exacto de clasificación: `git ls-tree -r --name-only d3a48cc` (primer commit, 2026-09-30, "Inicializa el repositorio del trabajo de grado") lista **175** archivos `.java` bajo `backend/solicitudes/src/main/java`; los otros **284** aparecen en commits posteriores (41 commits tocan ese árbol después de `d3a48cc`). Ninguno de los 175 tiene vocabulario de la extensión (grep de `Academic|Cancel|Supletorio|Estudiante|Asignatura|Programa|Resolucion|Situacion|Etapa|Maquina` sobre sus nombres: 0 coincidencias) y todos los 284 sí. Se asume que `d3a48cc` es el estado del prototipo de Julián, coherente con lo que declaran los dos `CLAUDE.md`. Esto precisa lo dicho en `00-indice-y-metodo.md` ("el historial no sirve para separar"): para el árbol Java de main sí separa limpiamente porque la extensión llegó en commits posteriores al import inicial.
- Cuatro archivos [BASE] fueron ampliados por la extensión, **solo con líneas agregadas** (`git diff d3a48cc --numstat`: 0 líneas borradas en los cuatro): `dominio/helper/constantes/ApplicationConstantes.java` (+8), `infraestructura/configuracion/BeanConfiguracion.java` (+267), `infraestructura/configuracion/seguridad/configuracion/ConfiguracionSeguridad.java` (+61), `infraestructura/output/manejadorExcepciones/MensajesError.java` (+93). Cuenta como [BASE] el archivo y como [EXTENSIÓN] sus adiciones. Los demás 171 archivos [BASE] no cambiaron desde `d3a48cc`.
- Cada módulo funcional lleva su origen; las clases compartidas (por ejemplo `ObservacionDTOPeticion`, ubicada en el paquete de Cancelación de Matrícula y reutilizada por Cancelación de Asignatura y Examen Supletorio) se atribuyen al paquete donde viven.

## 2. Árbol completo de paquetes

Raíz Java: `backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes`. Solo se muestran directorios con código; entre corchetes, archivos `.java` (BASE / EXT).

```
solicitudes (raíz del paquete com.unicauca.cfiet.solicitudes)  [459 archivos en total (BASE 175, EXT 284); directos: 1 (BASE 1, EXT 0)]
├── aplicacion/  [60 archivos en total (BASE 20, EXT 40)]
│   ├── input/  [30 archivos en total (BASE 8, EXT 22)]
│   └── output/  [30 archivos en total (BASE 12, EXT 18)]
├── dominio/  [100 archivos en total (BASE 24, EXT 76)]
│   ├── casosdeuso/  [30 archivos en total (BASE 8, EXT 22)]
│   ├── helper/  [5 archivos en total (BASE 2, EXT 3); directos: 1 (BASE 1, EXT 0)]
│   │   └── constantes/  [4 archivos en total (BASE 1, EXT 3)]
│   ├── modelos/  [58 archivos en total (BASE 14, EXT 44)]
│   └── servicios/  [7 archivos en total (BASE 0, EXT 7)]
└── infraestructura/  [298 archivos en total (BASE 130, EXT 168)]
    ├── configuracion/  [26 archivos en total (BASE 14, EXT 12); directos: 1 (BASE 1, EXT 0)]
    │   ├── lectorArchivos/  [19 archivos en total (BASE 7, EXT 12); directos: 12 (BASE 3, EXT 9)]
    │   │   ├── almacenador/  [1 archivos en total (BASE 1, EXT 0)]
    │   │   └── validadoresArchivos/  [6 archivos en total (BASE 3, EXT 3)]
    │   └── seguridad/  [6 archivos en total (BASE 6, EXT 0)]
    │       ├── configuracion/  [4 archivos en total (BASE 4, EXT 0)]
    │       └── jwt/  [2 archivos en total (BASE 2, EXT 0)]
    ├── input/  [131 archivos en total (BASE 53, EXT 78)]
    │   ├── controladorAsignaturas/  [6 archivos en total (BASE 0, EXT 6)]
    │   │   ├── controlador/  [1 archivos en total (BASE 0, EXT 1)]
    │   │   ├── DTOPeticion/  [3 archivos en total (BASE 0, EXT 3)]
    │   │   ├── DTORespuesta/  [1 archivos en total (BASE 0, EXT 1)]
    │   │   └── mapeador/  [1 archivos en total (BASE 0, EXT 1)]
    │   ├── controladorCancelacionesAsignatura/  [14 archivos en total (BASE 0, EXT 14)]
    │   │   ├── controlador/  [1 archivos en total (BASE 0, EXT 1)]
    │   │   ├── DTOPeticion/  [6 archivos en total (BASE 0, EXT 6)]
    │   │   ├── DTORespuesta/  [6 archivos en total (BASE 0, EXT 6)]
    │   │   └── mapeador/  [1 archivos en total (BASE 0, EXT 1)]
    │   ├── controladorCancelacionesMatricula/  [14 archivos en total (BASE 0, EXT 14)]
    │   │   ├── controlador/  [1 archivos en total (BASE 0, EXT 1)]
    │   │   ├── DTOPeticion/  [5 archivos en total (BASE 0, EXT 5)]
    │   │   ├── DTORespuesta/  [7 archivos en total (BASE 0, EXT 7)]
    │   │   └── mapeador/  [1 archivos en total (BASE 0, EXT 1)]
    │   ├── controladorCatalogosAcademicos/  [9 archivos en total (BASE 0, EXT 9)]
    │   │   ├── controlador/  [1 archivos en total (BASE 0, EXT 1)]
    │   │   ├── DTOPeticion/  [1 archivos en total (BASE 0, EXT 1)]
    │   │   ├── DTORespuesta/  [6 archivos en total (BASE 0, EXT 6)]
    │   │   └── mapeador/  [1 archivos en total (BASE 0, EXT 1)]
    │   ├── controladorEstudiantes/  [9 archivos en total (BASE 0, EXT 9)]
    │   │   ├── controlador/  [1 archivos en total (BASE 0, EXT 1)]
    │   │   ├── DTOPeticion/  [5 archivos en total (BASE 0, EXT 5)]
    │   │   ├── DTORespuesta/  [2 archivos en total (BASE 0, EXT 2)]
    │   │   └── mapeador/  [1 archivos en total (BASE 0, EXT 1)]
    │   ├── controladorExamenesSupletorios/  [10 archivos en total (BASE 0, EXT 10)]
    │   │   ├── controlador/  [1 archivos en total (BASE 0, EXT 1)]
    │   │   ├── DTOPeticion/  [2 archivos en total (BASE 0, EXT 2)]
    │   │   ├── DTORespuesta/  [6 archivos en total (BASE 0, EXT 6)]
    │   │   └── mapeador/  [1 archivos en total (BASE 0, EXT 1)]
    │   ├── controladorFuncionariosAcademicos/  [7 archivos en total (BASE 0, EXT 7)]
    │   │   ├── controlador/  [1 archivos en total (BASE 0, EXT 1)]
    │   │   ├── DTOPeticion/  [3 archivos en total (BASE 0, EXT 3)]
    │   │   ├── DTORespuesta/  [2 archivos en total (BASE 0, EXT 2)]
    │   │   └── mapeador/  [1 archivos en total (BASE 0, EXT 1)]
    │   ├── controladorLog/  [5 archivos en total (BASE 5, EXT 0)]
    │   │   ├── controlador/  [1 archivos en total (BASE 1, EXT 0)]
    │   │   ├── DTORespuesta/  [3 archivos en total (BASE 3, EXT 0)]
    │   │   └── mapeador/  [1 archivos en total (BASE 1, EXT 0)]
    │   ├── controladorRespuestas/  [4 archivos en total (BASE 4, EXT 0)]
    │   │   ├── controlador/  [1 archivos en total (BASE 1, EXT 0)]
    │   │   ├── DTOPeticion/  [1 archivos en total (BASE 1, EXT 0)]
    │   │   ├── DTORespuesta/  [1 archivos en total (BASE 1, EXT 0)]
    │   │   └── mapeador/  [1 archivos en total (BASE 1, EXT 0)]
    │   ├── controladorRoles/  [4 archivos en total (BASE 4, EXT 0)]
    │   │   ├── controlador/  [1 archivos en total (BASE 1, EXT 0)]
    │   │   ├── DTOPeticion/  [1 archivos en total (BASE 1, EXT 0)]
    │   │   ├── DTORespuesta/  [1 archivos en total (BASE 1, EXT 0)]
    │   │   └── mapeador/  [1 archivos en total (BASE 1, EXT 0)]
    │   ├── controladorSesion/  [4 archivos en total (BASE 4, EXT 0)]
    │   │   ├── controlador/  [1 archivos en total (BASE 1, EXT 0)]
    │   │   ├── DTOPeticion/  [1 archivos en total (BASE 1, EXT 0)]
    │   │   ├── DTORespuesta/  [1 archivos en total (BASE 1, EXT 0)]
    │   │   └── mapeador/  [1 archivos en total (BASE 1, EXT 0)]
    │   ├── controladorSolicitudes/  [15 archivos en total (BASE 15, EXT 0); directos: 1 (BASE 1, EXT 0)]
    │   │   ├── controlador/  [1 archivos en total (BASE 1, EXT 0)]
    │   │   ├── DTOPeticion/  [6 archivos en total (BASE 6, EXT 0)]
    │   │   ├── DTORespuesta/  [6 archivos en total (BASE 6, EXT 0)]
    │   │   └── mapeador/  [1 archivos en total (BASE 1, EXT 0)]
    │   ├── controladorSolicitudesAcademicas/  [9 archivos en total (BASE 0, EXT 9)]
    │   │   ├── controlador/  [1 archivos en total (BASE 0, EXT 1)]
    │   │   ├── DTORespuesta/  [7 archivos en total (BASE 0, EXT 7)]
    │   │   └── mapeador/  [1 archivos en total (BASE 0, EXT 1)]
    │   ├── controladorTiposSolicitud/  [9 archivos en total (BASE 9, EXT 0)]
    │   │   ├── controlador/  [1 archivos en total (BASE 1, EXT 0)]
    │   │   ├── DTOPeticion/  [2 archivos en total (BASE 2, EXT 0)]
    │   │   ├── DTORespuesta/  [5 archivos en total (BASE 5, EXT 0)]
    │   │   └── mapeador/  [1 archivos en total (BASE 1, EXT 0)]
    │   └── controladorUsuarios/  [12 archivos en total (BASE 12, EXT 0)]
    │       ├── controlador/  [1 archivos en total (BASE 1, EXT 0)]
    │       ├── DTOPeticion/  [6 archivos en total (BASE 6, EXT 0)]
    │       ├── DTORespuesta/  [4 archivos en total (BASE 4, EXT 0)]
    │       └── mapeador/  [1 archivos en total (BASE 1, EXT 0)]
    └── output/  [141 archivos en total (BASE 63, EXT 78)]
        ├── almacenamiento/  [2 archivos en total (BASE 0, EXT 2)]
        ├── encoder/  [1 archivos en total (BASE 1, EXT 0)]
        ├── exportador/  [1 archivos en total (BASE 1, EXT 0)]
        ├── formateador/  [1 archivos en total (BASE 1, EXT 0)]
        ├── JWT/  [1 archivos en total (BASE 1, EXT 0)]
        ├── manejadorExcepciones/  [14 archivos en total (BASE 14, EXT 0); directos: 2 (BASE 2, EXT 0)]
        │   ├── estructura/  [3 archivos en total (BASE 3, EXT 0)]
        │   └── excepcionesPropias/  [9 archivos en total (BASE 9, EXT 0)]
        └── persistencia/  [121 archivos en total (BASE 45, EXT 76)]
            ├── entidades/  [33 archivos en total (BASE 13, EXT 20)]
            ├── gateway/  [25 archivos en total (BASE 8, EXT 17)]
            ├── mapeador/  [34 archivos en total (BASE 15, EXT 19); directos: 1 (BASE 1, EXT 0)]
            │   └── ownMapper/  [33 archivos en total (BASE 14, EXT 19)]
            └── repositorios/  [29 archivos en total (BASE 9, EXT 20)]
```

Recursos fuera del árbol Java: `backend/solicitudes/src/main/resources/application.properties`, `data.sql` y `templates/` (plantilla del .docx de la orden del día, NO VERIFICADO su contenido). Pruebas: `backend/solicitudes/src/test/java` (38 archivos: 1 [BASE] `SolicitudesApplicationTests` y 37 [EXTENSIÓN]); detalle en el documento 11.

## 3. Inventario por capa (conteos)

| Capa / paquete | BASE | EXTENSIÓN | Total |
|---|---|---|---|
| aplicacion/input (puertos de entrada `*CUIntPuerto`) | 8 | 22 | 30 |
| aplicacion/output (puertos de salida) | 12 | 18 | 30 |
| dominio/casosdeuso (`*CUImplAdaptador`) | 8 | 22 | 30 |
| dominio/modelos | 14 | 44 | 58 |
| dominio/servicios | 0 | 7 | 7 |
| dominio/helper (+ constantes) | 2 | 3 | 5 |
| infraestructura/input: controladores | 7 | 8 | 15 |
| infraestructura/input: DTOPeticion | 17 | 25 | 42 |
| infraestructura/input: DTORespuesta | 21 | 37 | 58 |
| infraestructura/input: mapeadores `Mapper*InfraestructuraDominio` | 7 | 8 | 15 |
| infraestructura/input: otros (ValidadorAnexosService) | 1 | 0 | 1 |
| infraestructura/output/persistencia: entidades | 13 | 20 | 33 |
| infraestructura/output/persistencia: repositorios | 9 | 20 | 29 |
| infraestructura/output/persistencia: gateways `*GatewayImplAdaptador` | 8 | 17 | 25 |
| infraestructura/output/persistencia: mapeadores (`OwnMapper` + impl + `Mapper` ModelMapper) | 15 | 19 | 34 |
| infraestructura/output: almacenamiento, encoder, exportador, formateador, JWT | 4 | 2 | 6 |
| infraestructura/output/manejadorExcepciones (`RestApiExcepcion`, `MensajesError`, `CodigoError`, excepciones) | 14 | 0 | 14 |
| infraestructura/configuracion: BeanConfiguracion | 1 | 0 | 1 |
| infraestructura/configuracion/lectorArchivos (Excel y almacenador) | 7 | 12 | 19 |
| infraestructura/configuracion/seguridad (Spring Security + JWT) | 6 | 0 | 6 |
| SolicitudesApplication | 1 | 0 | 1 |
| **Total archivos `.java` de main** | **175** | **284** | **459** |

Lectura de los conteos:

- **Puertos**: 30 de entrada (`aplicacion/input`, todos `*CUIntPuerto`) y 30 de salida (`aplicacion/output`): 26 llevan sufijo `GatewayIntPuerto` (25 de persistencia más `PasswordEncoderGatewayIntPuerto`, cuya implementación `PasswordEnconderGatewayImplAdaptador` está en `infraestructura/output/encoder`) y 4 son de otro tipo (`AlmacenamientoAnexosIntPuerto`, `ExcepcionesFormateadorIntPuerto`, `IJwtServicio`, `OrdenDelDiaExportador`). Se verificó con un parser que **cada uno de los 60 puertos tiene exactamente una implementación** y que los 30 `*CUImplAdaptador` implementan 1:1 los 30 puertos de entrada.
- **Casos de uso**: 30 `*CUImplAdaptador` (8 [BASE], 22 [EXTENSIÓN]), más 7 servicios de dominio en `dominio/servicios` (todos [EXTENSIÓN]): `DatosPersonales`, `MaquinaEtapas`, `ReglasAsignatura`, `TramiteCancelacion`, `TramiteSolicitud`, `ValidadorActorSolicitud`, `ValidadorArchivoAdjunto`.
- **Persistencia**: 33 clases en `entidades` (32 `@Entity` con `@Table` más `EtapaEtiquetaRolIdEntidad`, `@Embeddable`), 29 repositorios (`extends JpaRepository`), 25 gateways y 33 archivos en `mapeador/ownMapper` (32 implementaciones de `OwnMapper<D,E>` más la interfaz `OwnMapper`) más `Mapper` (configuración de `ModelMapper`, [BASE]).
- **Dominio**: 58 archivos en `modelos` (5 enums: `AccionEtapa`, `CausaSupletorio`, `ResponsableEtapa`, `RolEtiquetaEtapa`, `TipoProcesoAcademico`; el resto POJOs con Lombok). No hay interfaces en `dominio/`. `MaquinaEtapas` y `ConsultaSolicitudAcademicaCUImplAdaptador` declaran `record` internos.
- **Entrada HTTP**: 15 controladores, 42 DTO de petición (17 [BASE], 25 [EXTENSIÓN]), 58 DTO de respuesta (21 [BASE], 37 [EXTENSIÓN]), 15 mapeadores de entrada (7 [BASE] con `ModelMapper`, 8 [EXTENSIÓN] a mano, ver `backend/CLAUDE.md`), 1 servicio auxiliar (`ValidadorAnexosService`).
- Clases por capa (suma de archivos): aplicación = 60, dominio = 100 (30 casos de uso + 58 modelos + 7 servicios + 5 de helper/constantes), infraestructura = 298, raíz (`SolicitudesApplication`) = 1; total 459.

## 4. Inventario por módulo funcional

"Módulo" es la agrupación funcional que usan los controladores (`infraestructura/input/controlador<X>`) más las clases de dominio y persistencia que le corresponden por nombre. Las cifras son B/E = [BASE]/[EXTENSIÓN].

| Módulo | Puertos de entrada | Casos de uso | Puertos de salida (gateway) | Gateways | Repos | Entidades | Modelos | Servicios dominio | Controladores | DTO pet. | DTO resp. | Map. entrada | Map. persist. |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Sesión | 1/0 | 1/0 | 1/0 | 1/0 | 0 | 0 | 1/0 | 0 | 1/0 | 1/0 | 1/0 | 1/0 | 0 |
| Usuarios y Funcionario (comité) | 1/0 | 1/0 | 1/0 | 1/0 | 3/0 | 4/0 | 4/0 | 0 | 1/0 | 6/0 | 4/0 | 1/0 | 4/0 |
| Roles | 1/0 | 1/0 | 1/0 | 1/0 | 1/0 | 1/0 | 1/0 | 0 | 1/0 | 1/0 | 1/0 | 1/0 | 1/0 |
| Log | 1/0 | 1/0 | 1/0 | 1/0 | 1/0 | 1/0 | 1/0 | 0 | 1/0 | 0 | 3/0 | 1/0 | 1/0 |
| Tipos de solicitud (Julián) | 1/0 | 1/0 | 1/0 | 1/0 | 1/0 | 3/0 | 3/0 | 0 | 1/0 | 2/0 | 5/0 | 1/0 | 3/0 |
| Solicitudes y Orden del día (Julián) | 2/0 | 2/0 | 2/0 | 2/0 | 2/0 | 3/0 | 3/0 | 0 | 1/0 | 6/0 | 6/0 | 1/0 | 3/0 |
| Respuestas | 1/0 | 1/0 | 1/0 | 1/0 | 1/0 | 1/0 | 1/0 | 0 | 1/0 | 1/0 | 1/0 | 1/0 | 1/0 |
| Programas académicos | 0/1 | 0/1 | 0/1 | 0/1 | 0/1 | 0/1 | 0/1 | 0 | 0 | 0 | 0 | 0 | 0/1 |
| Asignaturas | 0/1 | 0/1 | 0/1 | 0/1 | 0/1 | 0/1 | 0/2 | 0/1 | 0/1 | 0/3 | 0/1 | 0/1 | 0/1 |
| Estudiantes | 0/1 | 0/1 | 0/2 | 0/2 | 0/3 | 0/2 | 0/2 | 0/1 | 0/1 | 0/5 | 0/2 | 0/1 | 0/2 |
| Funcionarios académicos | 0/1 | 0/1 | 0/1 | 0/1 | 0/1 | 0/1 | 0/1 | 0 | 0/1 | 0/3 | 0/2 | 0/1 | 0/1 |
| Catálogos académicos | 0/5 | 0/5 | 0/5 | 0/5 | 0/5 | 0/6 | 0/9 | 0 | 0/1 | 0/1 | 0/6 | 0/1 | 0/5 |
| Motor de solicitudes académicas | 0/4 | 0/4 | 0/4 | 0/4 | 0/5 | 0/5 | 0/15 | 0/5 | 0/1 | 0 | 0/7 | 0/1 | 0/5 |
| Cancelación de Matrícula | 0/3 | 0/3 | 0/1 | 0/1 | 0/1 | 0/1 | 0/3 | 0 | 0/1 | 0/5 | 0/7 | 0/1 | 0/1 |
| Cancelación de Asignatura | 0/3 | 0/3 | 0/1 | 0/1 | 0/1 | 0/1 | 0/5 | 0 | 0/1 | 0/6 | 0/6 | 0/1 | 0/1 |
| Examen Supletorio | 0/3 | 0/3 | 0/1 | 0/1 | 0/2 | 0/2 | 0/6 | 0 | 0/1 | 0/2 | 0/6 | 0/1 | 0/2 |
| Transversal | 0 | 0 | 1/0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 1/0 |
| **Total (B/E)** | 8/22 | 8/22 | 9/17 | 8/17 | 9/20 | 13/20 | 14/44 | 0/7 | 7/8 | 17/25 | 21/37 | 7/8 | 14/19 |

Notas: "Transversal" agrupa lo que no pertenece a un módulo (formateador de excepciones, JWT, encoder, exportador, `ApplicationConstantes`, `OwnMapper`, manejadores de excepciones, configuración); sus filas de puertos no contadas arriba (4 de salida distintos de gateway, 1 `PasswordEncoder`) están en la tabla de capas. Las filas con `0` no tienen clases de esa categoría. Las clases de "Programas académicos" no tienen controlador propio: se exponen por `catalogos-academicos/programas`.

### 4.1 Detalle por módulo

#### Sesión

Origen del módulo: BASE. Solo se marca la clase cuyo origen difiere del módulo.

- Puertos de entrada (1): `SesionCUIntPuerto`
- Casos de uso (1): `SesionCUImplAdaptador`
- Puertos de salida (gateway) (1): `SesionGatewayIntPuerto`
- Gateways (impl.) (1): `SesionGatewayImplAdaptador`
- Modelos de dominio (1): `UsuarioTokenizado`
- Controlador (1): `SesionRestController`
- Mapper de entrada (1): `MapperSesionInfraestructuraDominio`
- DTO de petición (1): `SesionDTOPeticion`
- DTO de respuesta (1): `UsuarioTokenizadoDTORespuesta`

#### Usuarios y Funcionario (comité)

Origen del módulo: BASE. Solo se marca la clase cuyo origen difiere del módulo.

- Puertos de entrada (1): `UsuarioCUIntPuerto`
- Casos de uso (1): `UsuarioCUImplAdaptador`
- Puertos de salida (gateway) (1): `UsuarioGatewayIntPuerto`
- Gateways (impl.) (1): `UsuarioGatewayImplAdapter`
- Repositorios (3): `FuncionarioRepositorio`, `UsuarioLivianoRepositorio`, `UsuarioRepositorio`
- Mappers persistencia (OwnMapper) (4): `FuncionarioOwnMapper`, `TipoUsuarioOwnMapper`, `UsuarioLivianoOwnMapper`, `UsuarioOwnMapper`
- Modelos de dominio (4): `Funcionario`, `TipoUsuario`, `Usuario`, `UsuarioLiviano`
- Controlador (1): `UsuarioRestController`
- Mapper de entrada (1): `MapperUsuarioInfraestructuraDominio`
- DTO de petición (6): `CambioContraseñaDTOPeticion`, `RolUsuarioDTOPeticion`, `TipoUsuarioDTOPeticion`, `UsuarioActualizarDTOPeticion`, `UsuarioDTOPeticion`, `UsuarioLivianoDTOPeticion`
- DTO de respuesta (4): `RolUsuarioDTORespuesta`, `TipoUsuarioDTORespuesta`, `UsuarioDTORespuesta`, `UsuarioLivianoDTORespuesta`
- Configuración (2): `ProcesarArchivoUsuariosImpl`, `UsuarioExcelService`
- Entidades JPA (4) y tabla: `FuncionarioEntidad` -> `funcionarios`, `TipoUsuarioEntidad` -> `tiposUsuario`, `UsuarioEntidad` -> `usuarios`, `UsuarioLivianoEntidad` -> `usuariosLivianos`

#### Roles

Origen del módulo: BASE. Solo se marca la clase cuyo origen difiere del módulo.

- Puertos de entrada (1): `RolCUIntPuerto`
- Casos de uso (1): `RolCUImplAdaptador`
- Puertos de salida (gateway) (1): `RolGatewayIntPuerto`
- Gateways (impl.) (1): `RolGatewayImplAdaptador`
- Repositorios (1): `RolRepositorio`
- Mappers persistencia (OwnMapper) (1): `RolOwnMapperImpl`
- Modelos de dominio (1): `Rol`
- Controlador (1): `RolRestController`
- Mapper de entrada (1): `MapperRolInfraestructuraDominio`
- DTO de petición (1): `RolDTOPeticion`
- DTO de respuesta (1): `RolDTORespuesta`
- Entidades JPA (1) y tabla: `RolEntidad` -> `roles`

#### Log

Origen del módulo: BASE. Solo se marca la clase cuyo origen difiere del módulo.

- Puertos de entrada (1): `LogCUIntPuerto`
- Casos de uso (1): `LogCUImplAdaptador`
- Puertos de salida (gateway) (1): `LogGatewayIntPuerto`
- Gateways (impl.) (1): `LogGatewayImplAdaptador`
- Repositorios (1): `LogRepositorio`
- Mappers persistencia (OwnMapper) (1): `LogOwnMapper`
- Modelos de dominio (1): `Log`
- Controlador (1): `LogRestController`
- Mapper de entrada (1): `MapperLogInfraestructuraDominio`
- DTO de respuesta (3): `LogDTORespuesta`, `UsuarioLogDTORespuesta`, `UsuarioLogLivianoDTORespuesta`
- Entidades JPA (1) y tabla: `LogEntidad` -> `logs`

#### Tipos de solicitud (Julián)

Origen del módulo: BASE. Solo se marca la clase cuyo origen difiere del módulo.

- Puertos de entrada (1): `TipoSolicitudCUIntPuerto`
- Casos de uso (1): `TipoSolicitudCUImplAdaptador`
- Puertos de salida (gateway) (1): `TipoSolicitudGatewayIntPuerto`
- Gateways (impl.) (1): `TipoSolicitudGatewayImplAdaptador`
- Repositorios (1): `TipoSolicitudRepositorio`
- Mappers persistencia (OwnMapper) (3): `AnexoOwnMapper`, `TipoAnexoOwnMapper`, `TipoSolicitudOwnMapper`
- Modelos de dominio (3): `Anexo`, `TipoAnexo`, `TipoSolicitud`
- Controlador (1): `TiposSolicitudRestController`
- Mapper de entrada (1): `MapperTipoSolicitudInfraestructuraDominio`
- DTO de petición (2): `TipoAnexoDTOPeticion`, `TipoSolicitudDTOPeticion`
- DTO de respuesta (5): `FuncionarioTipoSolicitudDTORespuesta`, `TipoAnexoDTORespuesta`, `TipoSolicitudDTORespuesta`, `UsuarioLivianoTipoSolicitudDTORespuesta`, `UsuarioTipoSolicitudDTORespuesta`
- Configuración (1): `TipoSolicitudExcelService`
- Entidades JPA (3) y tabla: `AnexoEntidad` -> `anexos`, `TipoAnexoEntidad` -> `tiposAnexos`, `TipoSolicitudEntidad` -> `tiposSolicitudes`

#### Solicitudes y Orden del día (Julián)

Origen del módulo: BASE. Solo se marca la clase cuyo origen difiere del módulo.

- Puertos de entrada (2): `OrdenDelDiaCUIntPuerto`, `SolicitudCUintPuerto`
- Casos de uso (2): `OrdenDelDiaCUImplAdaptador`, `SolicitudCUImplAdaptador`
- Puertos de salida (gateway) (2): `OrdenDelDiaGatewayIntPuerto`, `SolicitudGatewayIntPuerto`
- Otros puertos de salida (1): `OrdenDelDiaExportador`
- Gateways (impl.) (2): `OrdenDelDiaGatewayImplAdaptador`, `SolicitudGatewayImplAdaptador`
- Repositorios (2): `OrdenDelDiaRepositorio`, `SolicitudRepositorio`
- Mappers persistencia (OwnMapper) (3): `InformacionSolicitanteOwnMapper`, `OrdenDelDiaOwnMapper`, `SolicitudOwnMapper`
- Modelos de dominio (3): `InformacionSolicitante`, `OrdenDelDia`, `Solicitud`
- Controlador (1): `SolicitudesRestController`
- Mapper de entrada (1): `MapperSolicitudesInfraestructuraDominio`
- DTO de petición (6): `AnexoDTOPeticion`, `InformacionSolicitanteDTOPeticion`, `OrdenDelDiaDTOPeticion`, `SolicitudActualizarDTOPeticion`, `SolicitudDTOPeticion`, `SolicitudPublicaDTOPeticion`
- DTO de respuesta (6): `AnexoDTORespuesta`, `FuncionarioDTORespuesta`, `FuncionarioLivianoDTORespuesta`, `InformacionSolicitanteDTORespuesta`, `OrdenDelDiaDTORespuesta`, `SolicitudDTORespuesta`
- Otros (output/config) (1): `OrdenDelDiaExportadorImpl`
- Configuración (1): `ProcesarArchivoTiposSolicitudesImpl`
- Otros de entrada (1): `ValidadorAnexosService`
- Entidades JPA (3) y tabla: `InformacionSolicitanteEntidad` -> `informacionSolicitantes`, `OrdenDelDiaEntidad` -> `ordenesDelDia`, `SolicitudEntidad` -> `solicitudes`

#### Respuestas

Origen del módulo: BASE. Solo se marca la clase cuyo origen difiere del módulo.

- Puertos de entrada (1): `RespuestaCUIntPuerto`
- Casos de uso (1): `RespuestaCUImplAdaptador`
- Puertos de salida (gateway) (1): `RespuestaGatewayIntPuerto`
- Gateways (impl.) (1): `RespuestaGatewayImplAdaptador`
- Repositorios (1): `RespuestaRepositorio`
- Mappers persistencia (OwnMapper) (1): `RespuestaOwnMapper`
- Modelos de dominio (1): `Respuesta`
- Controlador (1): `RespuestaRestController`
- Mapper de entrada (1): `MapperRespuestaInfraestructuraDominio`
- DTO de petición (1): `RespuestaDTOPeticion`
- DTO de respuesta (1): `RespuestaDTORespuesta`
- Constantes / helper (1): `PaginacionRespuestaDTO`
- Entidades JPA (1) y tabla: `RespuestaEntidad` -> `respuestas`

#### Programas académicos

Origen del módulo: EXTENSIÓN. Solo se marca la clase cuyo origen difiere del módulo.

- Puertos de entrada (1): `ProgramaAcademicoCUIntPuerto`
- Casos de uso (1): `ProgramaAcademicoCUImplAdaptador`
- Puertos de salida (gateway) (1): `ProgramaAcademicoGatewayIntPuerto`
- Gateways (impl.) (1): `ProgramaAcademicoGatewayImplAdaptador`
- Repositorios (1): `ProgramaAcademicoRepositorio`
- Mappers persistencia (OwnMapper) (1): `ProgramaAcademicoOwnMapper`
- Modelos de dominio (1): `ProgramaAcademico`
- Entidades JPA (1) y tabla: `ProgramaAcademicoEntidad` -> `PROGRAMA_ACADEMICO`

#### Asignaturas

Origen del módulo: EXTENSIÓN. Solo se marca la clase cuyo origen difiere del módulo.

- Puertos de entrada (1): `AsignaturaCUIntPuerto`
- Casos de uso (1): `AsignaturaCUImplAdaptador`
- Servicios de dominio (1): `ReglasAsignatura`
- Puertos de salida (gateway) (1): `AsignaturaGatewayIntPuerto`
- Gateways (impl.) (1): `AsignaturaGatewayImplAdaptador`
- Repositorios (1): `AsignaturaRepositorio`
- Mappers persistencia (OwnMapper) (1): `AsignaturaOwnMapper`
- Modelos de dominio (2): `Asignatura`, `EvaluacionAsignatura`
- Controlador (1): `AsignaturaRestController`
- Mapper de entrada (1): `MapperAsignaturaInfraestructuraDominio`
- DTO de petición (3): `AsignaturaActualizarDTOPeticion`, `AsignaturaDTOPeticion`, `FilaAsignaturaExcelDTOPeticion`
- DTO de respuesta (1): `AsignaturaDTORespuesta`
- Configuración (3): `ColumnasExcelAsignaturas`, `ProcesarArchivoAsignaturasImpl`, `AsignaturaExcelService`
- Entidades JPA (1) y tabla: `AsignaturaEntidad` -> `ASIGNATURA`

#### Estudiantes

Origen del módulo: EXTENSIÓN. Solo se marca la clase cuyo origen difiere del módulo.

- Puertos de entrada (1): `EstudianteCUIntPuerto`
- Casos de uso (1): `EstudianteCUImplAdaptador`
- Servicios de dominio (1): `DatosPersonales`
- Puertos de salida (gateway) (2): `DatosPersonalesGatewayIntPuerto`, `EstudianteGatewayIntPuerto`
- Gateways (impl.) (2): `DatosPersonalesGatewayImplAdaptador`, `EstudianteGatewayImplAdaptador`
- Repositorios (3): `AsignaturaMatriculadaRepositorio`, `DatosPersonalesRepositorio`, `EstudianteRepositorio`
- Mappers persistencia (OwnMapper) (2): `AsignaturaMatriculadaOwnMapper`, `EstudianteOwnMapper`
- Modelos de dominio (2): `AsignaturaMatriculada`, `Estudiante`
- Controlador (1): `EstudianteRestController`
- Mapper de entrada (1): `MapperEstudianteInfraestructuraDominio`
- DTO de petición (5): `AsignaturaMatriculadaDTOPeticion`, `CambioEstadoAsignaturaDTOPeticion`, `EstudianteActualizarDTOPeticion`, `EstudianteDTOPeticion`, `FilaEstudianteExcelDTOPeticion`
- DTO de respuesta (2): `AsignaturaMatriculadaDTORespuesta`, `EstudianteDTORespuesta`
- Constantes / helper (1): `EstadoAsignaturaMatriculadaConstantes`
- Configuración (4): `AgrupadorEstudiantesExcel`, `ColumnasExcelEstudiantes`, `ProcesarArchivoEstudiantesImpl`, `EstudianteExcelService`
- Entidades JPA (2) y tabla: `AsignaturaMatriculadaEntidad` -> `ASIGNATURA_MATRICULADA`, `EstudianteEntidad` -> `ESTUDIANTE`

#### Funcionarios académicos

Origen del módulo: EXTENSIÓN. Solo se marca la clase cuyo origen difiere del módulo.

- Puertos de entrada (1): `FuncionarioAcademicoCUIntPuerto`
- Casos de uso (1): `FuncionarioAcademicoCUImplAdaptador`
- Puertos de salida (gateway) (1): `FuncionarioAcademicoGatewayIntPuerto`
- Gateways (impl.) (1): `FuncionarioAcademicoGatewayImplAdaptador`
- Repositorios (1): `FuncionarioAcademicoRepositorio`
- Mappers persistencia (OwnMapper) (1): `FuncionarioAcademicoOwnMapper`
- Modelos de dominio (1): `FuncionarioAcademico`
- Controlador (1): `FuncionarioAcademicoRestController`
- Mapper de entrada (1): `MapperFuncionarioAcademicoInfraestructuraDominio`
- DTO de petición (3): `FilaFuncionarioAcademicoExcelDTOPeticion`, `FuncionarioAcademicoActualizarDTOPeticion`, `FuncionarioAcademicoDTOPeticion`
- DTO de respuesta (2): `FuncionarioAcademicoDTORespuesta`, `TipoSolicitudAtendidaDTORespuesta`
- Configuración (4): `ColumnasExcelFuncionariosAcademicos`, `DuplicadosFuncionariosAcademicosExcel`, `ProcesarArchivoFuncionariosAcademicosImpl`, `FuncionarioAcademicoExcelService`
- Entidades JPA (1) y tabla: `FuncionarioAcademicoEntidad` -> `FUNCIONARIO_ACADEMICO`

#### Catálogos académicos

Origen del módulo: EXTENSIÓN. Solo se marca la clase cuyo origen difiere del módulo.

- Puertos de entrada (5): `AsignacionFuncionarioAcademicoCUIntPuerto`, `EtapaSolicitudAcademicaCUIntPuerto`, `SituacionAcademicaAsignaturaCUIntPuerto`, `TipoAnexoAcademicoCUIntPuerto`, `TipoSolicitudAcademicaCUIntPuerto`
- Casos de uso (5): `AsignacionFuncionarioAcademicoCUImplAdaptador`, `EtapaSolicitudAcademicaCUImplAdaptador`, `SituacionAcademicaAsignaturaCUImplAdaptador`, `TipoAnexoAcademicoCUImplAdaptador`, `TipoSolicitudAcademicaCUImplAdaptador`
- Puertos de salida (gateway) (5): `EtapaEtiquetaRolGatewayIntPuerto`, `EtapaSolicitudAcademicaGatewayIntPuerto`, `SituacionAcademicaAsignaturaGatewayIntPuerto`, `TipoAnexoAcademicoGatewayIntPuerto`, `TipoSolicitudAcademicaGatewayIntPuerto`
- Gateways (impl.) (5): `EtapaEtiquetaRolGatewayImplAdaptador`, `EtapaSolicitudAcademicaGatewayImplAdaptador`, `SituacionAcademicaAsignaturaGatewayImplAdaptador`, `TipoAnexoAcademicoGatewayImplAdaptador`, `TipoSolicitudAcademicaGatewayImplAdaptador`
- Repositorios (5): `EtapaEtiquetaRolRepositorio`, `EtapaSolicitudAcademicaRepositorio`, `SituacionAcademicaAsignaturaRepositorio`, `TipoAnexoAcademicoRepositorio`, `TipoSolicitudAcademicaRepositorio`
- Mappers persistencia (OwnMapper) (5): `EtapaEtiquetaRolOwnMapper`, `EtapaSolicitudAcademicaOwnMapper`, `SituacionAcademicaAsignaturaOwnMapper`, `TipoAnexoAcademicoOwnMapper`, `TipoSolicitudAcademicaOwnMapper`
- Modelos de dominio (9): `EtapaEtiquetaRol`, `EtapaSolicitudAcademica`, `ResponsableEtapa`, `RolEtiquetaEtapa`, `SituacionAcademicaAsignatura`, `SituacionCancelarAsignatura`, `SoportePermitido`, `TipoAnexoAcademico`, `TipoSolicitudAcademica`
- Controlador (1): `CatalogoAcademicoRestController`
- Mapper de entrada (1): `MapperCatalogoAcademicoInfraestructuraDominio`
- DTO de petición (1): `AsignacionFuncionarioAcademicoDTOPeticion`
- DTO de respuesta (6): `EtapaEtiquetaRolDTORespuesta`, `EtapaSolicitudAcademicaDTORespuesta`, `ProgramaAcademicoDTORespuesta`, `SituacionAcademicaAsignaturaDTORespuesta`, `TipoAnexoAcademicoDTORespuesta`, `TipoSolicitudAcademicaDTORespuesta`
- Constantes / helper (1): `EtapaSolicitudAcademicaConstantes`
- Entidades JPA (6) y tabla: `EtapaEtiquetaRolEntidad` -> `ETAPA_ETIQUETA_ROL`, `EtapaEtiquetaRolIdEntidad` -> (`@Embeddable`), `EtapaSolicitudAcademicaEntidad` -> `ETAPA_SOLICITUD_ACADEMICA`, `SituacionAcademicaAsignaturaEntidad` -> `SITUACION_ACADEMICA_ASIGNATURA`, `TipoAnexoAcademicoEntidad` -> `TIPO_ANEXO_ACADEMICO`, `TipoSolicitudAcademicaEntidad` -> `TIPO_SOLICITUD_ACADEMICA`

#### Motor de solicitudes académicas

Origen del módulo: EXTENSIÓN. Solo se marca la clase cuyo origen difiere del módulo.

- Puertos de entrada (4): `AnexoAcademicoCUIntPuerto`, `ConsultaSolicitudAcademicaCUIntPuerto`, `ResolucionAcademicaCUIntPuerto`, `SolicitudAcademicaCUIntPuerto`
- Casos de uso (4): `AnexoAcademicoCUImplAdaptador`, `ConsultaSolicitudAcademicaCUImplAdaptador`, `ResolucionAcademicaCUImplAdaptador`, `SolicitudAcademicaCUImplAdaptador`
- Servicios de dominio (5): `MaquinaEtapas`, `TramiteCancelacion`, `TramiteSolicitud`, `ValidadorActorSolicitud`, `ValidadorArchivoAdjunto`
- Puertos de salida (gateway) (4): `AnexoAcademicoGatewayIntPuerto`, `HistorialSolicitudAcademicaGatewayIntPuerto`, `ResolucionAcademicaGatewayIntPuerto`, `SolicitudAcademicaGatewayIntPuerto`
- Otros puertos de salida (1): `AlmacenamientoAnexosIntPuerto`
- Gateways (impl.) (4): `AnexoAcademicoGatewayImplAdaptador`, `HistorialSolicitudAcademicaGatewayImplAdaptador`, `ResolucionAcademicaGatewayImplAdaptador`, `SolicitudAcademicaGatewayImplAdaptador`
- Repositorios (5): `AnexoAcademicoRepositorio`, `AsignaturaSolicitudAcademicaRepositorio`, `HistorialSolicitudAcademicaRepositorio`, `ResolucionAcademicaRepositorio`, `SolicitudAcademicaRepositorio`
- Mappers persistencia (OwnMapper) (5): `AnexoAcademicoOwnMapper`, `AsignaturaSolicitudAcademicaOwnMapper`, `HistorialSolicitudAcademicaOwnMapper`, `ResolucionAcademicaOwnMapper`, `SolicitudAcademicaOwnMapper`
- Modelos de dominio (15): `AccionEtapa`, `ActorSolicitud`, `AnexoAcademico`, `AnexoRadicacion`, `ArchivoAdjunto`, `AsignaturaSolicitudAcademica`, `DetalleSolicitudAcademica`, `EntregaTransicion`, `HistorialSolicitudAcademica`, `ResolucionAcademica`, `ResultadoAsignaturaSolicitud`, `ResultadoTransicion`, `ResumenSolicitudAcademica`, `SolicitudAcademica`, `TipoProcesoAcademico`
- Controlador (1): `SolicitudAcademicaRestController`
- Mapper de entrada (1): `MapperSolicitudAcademicaInfraestructuraDominio`
- DTO de respuesta (7): `AnexoAcademicoDTORespuesta`, `EstudianteSolicitudDTORespuesta`, `HistorialSolicitudAcademicaDTORespuesta`, `ResolucionAcademicaDTORespuesta`, `ResultadoAsignaturaDTORespuesta`, `SolicitudAcademicaDetalleDTORespuesta`, `SolicitudAcademicaResumenDTORespuesta`
- Constantes / helper (1): `AnexoAcademicoConstantes`
- Otros (output/config) (2): `AlmacenamientoAnexosImplAdaptador`, `ArchivoEnMemoria`
- Entidades JPA (5) y tabla: `AnexoAcademicoEntidad` -> `ANEXO_ACADEMICO`, `AsignaturaSolicitudAcademicaEntidad` -> `ASIGNATURA_SOLICITUD_ACADEMICA`, `HistorialSolicitudAcademicaEntidad` -> `HISTORIAL_SOLICITUD_ACADEMICA`, `ResolucionAcademicaEntidad` -> `RESOLUCION_ACADEMICA`, `SolicitudAcademicaEntidad` -> `SOLICITUD_ACADEMICA`

#### Cancelación de Matrícula

Origen del módulo: EXTENSIÓN. Solo se marca la clase cuyo origen difiere del módulo.

- Puertos de entrada (3): `CancelacionMatriculaCUIntPuerto`, `ConsultaCancelacionMatriculaCUIntPuerto`, `TramiteCancelacionMatriculaCUIntPuerto`
- Casos de uso (3): `CancelacionMatriculaCUImplAdaptador`, `ConsultaCancelacionMatriculaCUImplAdaptador`, `TramiteCancelacionMatriculaCUImplAdaptador`
- Puertos de salida (gateway) (1): `SolicitudCancelacionMatriculaGatewayIntPuerto`
- Gateways (impl.) (1): `SolicitudCancelacionMatriculaGatewayImplAdaptador`
- Repositorios (1): `SolicitudCancelacionMatriculaRepositorio`
- Mappers persistencia (OwnMapper) (1): `SolicitudCancelacionMatriculaOwnMapper`
- Modelos de dominio (3): `DetalleCancelacionMatricula`, `FormularioCancelacionMatricula`, `SolicitudCancelacionMatricula`
- Controlador (1): `CancelacionMatriculaRestController`
- Mapper de entrada (1): `MapperCancelacionMatriculaInfraestructuraDominio`
- DTO de petición (5): `AprobacionDecanoDTOPeticion`, `EvaluacionAsignaturaDTOPeticion`, `ObservacionDTOPeticion`, `RemisionDecanoDTOPeticion`, `SituacionCancelarDTOPeticion`
- DTO de respuesta (7): `AnexoRequeridoDTORespuesta`, `AsignaturaCancelacionDTORespuesta`, `AsignaturaFormularioDTORespuesta`, `CancelacionMatriculaDetalleDTORespuesta`, `FormularioCancelacionMatriculaDTORespuesta`, `RadicacionCancelacionMatriculaDTORespuesta`, `SituacionAsignaturaDTORespuesta`
- Entidades JPA (1) y tabla: `SolicitudCancelacionMatriculaEntidad` -> `SOLICITUD_CANCELACION_MATRICULA`

#### Cancelación de Asignatura

Origen del módulo: EXTENSIÓN. Solo se marca la clase cuyo origen difiere del módulo.

- Puertos de entrada (3): `CancelacionAsignaturaCUIntPuerto`, `ConsultaCancelacionAsignaturaCUIntPuerto`, `TramiteCancelacionAsignaturaCUIntPuerto`
- Casos de uso (3): `CancelacionAsignaturaCUImplAdaptador`, `ConsultaCancelacionAsignaturaCUImplAdaptador`, `TramiteCancelacionAsignaturaCUImplAdaptador`
- Puertos de salida (gateway) (1): `SolicitudCancelacionAsignaturaGatewayIntPuerto`
- Gateways (impl.) (1): `SolicitudCancelacionAsignaturaGatewayImplAdaptador`
- Repositorios (1): `SolicitudCancelacionAsignaturaRepositorio`
- Mappers persistencia (OwnMapper) (1): `SolicitudCancelacionAsignaturaOwnMapper`
- Modelos de dominio (5): `AjusteAsignatura`, `DecisionAsignatura`, `DetalleCancelacionAsignatura`, `FormularioCancelacionAsignatura`, `SolicitudCancelacionAsignatura`
- Controlador (1): `CancelacionAsignaturaRestController`
- Mapper de entrada (1): `MapperCancelacionAsignaturaInfraestructuraDominio`
- DTO de petición (6): `AjusteAsignaturaDTOPeticion`, `AprobacionCancelacionAsignaturaDTOPeticion`, `DecisionAsignaturaDTOPeticion`, `EvaluacionCancelacionAsignaturaDTOPeticion`, `RemisionCancelacionAsignaturaDTOPeticion`, `RespuestaCancelacionAsignaturaDTOPeticion`
- DTO de respuesta (6): `AsignaturaElegibleDTORespuesta`, `AsignaturaSolicitadaDTORespuesta`, `CancelacionAsignaturaDetalleDTORespuesta`, `FormularioCancelacionAsignaturaDTORespuesta`, `RadicacionCancelacionAsignaturaDTORespuesta`, `SoportePermitidoDTORespuesta`
- Entidades JPA (1) y tabla: `SolicitudCancelacionAsignaturaEntidad` -> `SOLICITUD_CANCELACION_ASIGNATURA`

#### Examen Supletorio

Origen del módulo: EXTENSIÓN. Solo se marca la clase cuyo origen difiere del módulo.

- Puertos de entrada (3): `ConsultaExamenSupletorioCUIntPuerto`, `ExamenSupletorioCUIntPuerto`, `TramiteExamenSupletorioCUIntPuerto`
- Casos de uso (3): `ConsultaExamenSupletorioCUImplAdaptador`, `ExamenSupletorioCUImplAdaptador`, `TramiteExamenSupletorioCUImplAdaptador`
- Puertos de salida (gateway) (1): `SolicitudExamenSupletorioGatewayIntPuerto`
- Gateways (impl.) (1): `SolicitudExamenSupletorioGatewayImplAdaptador`
- Repositorios (2): `SolicitudExamenSupletorioRepositorio`, `SolicitudSupletorioCruceAsignaturaRepositorio`
- Mappers persistencia (OwnMapper) (2): `SolicitudExamenSupletorioOwnMapper`, `SolicitudSupletorioCruceAsignaturaOwnMapper`
- Modelos de dominio (6): `AnexoSupletorio`, `CausaSupletorio`, `CruceSupletorio`, `DetalleExamenSupletorio`, `FormularioExamenSupletorio`, `SolicitudExamenSupletorio`
- Controlador (1): `ExamenSupletorioRestController`
- Mapper de entrada (1): `MapperExamenSupletorioInfraestructuraDominio`
- DTO de petición (2): `AprobacionComprobanteDTOPeticion`, `RemisionSupletorioDTOPeticion`
- DTO de respuesta (6): `AnexoSupletorioDTORespuesta`, `AsignaturaSupletorioDTORespuesta`, `CruceSupletorioDTORespuesta`, `ExamenSupletorioDetalleDTORespuesta`, `FormularioExamenSupletorioDTORespuesta`, `RadicacionExamenSupletorioDTORespuesta`
- Entidades JPA (2) y tabla: `SolicitudExamenSupletorioEntidad` -> `SOLICITUD_EXAMEN_SUPLETORIO`, `SolicitudSupletorioCruceAsignaturaEntidad` -> `SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA`

#### Transversal

Origen del módulo: BASE. Solo se marca la clase cuyo origen difiere del módulo.

- Puertos de salida (gateway) (1): `PasswordEncoderGatewayIntPuerto`
- Otros puertos de salida (2): `ExcepcionesFormateadorIntPuerto`, `IJwtServicio`
- Mappers persistencia (OwnMapper) (1): `OwnMapper`
- Constantes / helper (1): `ApplicationConstantes`
- Otros (output/config) (18): `JwtServicioImpl`, `PasswordEnconderGatewayImplAdaptador`, `ExcepcionesFormateadorImplAdaptador`, `MensajesError`, `RestApiExcepcion`, `CodigoError`, `Error`, `UtilidadesError`, `ErrorCredencialesIncorrectasExcepcion`, `ErrorEntidadExisteExcepcion`, `ErrorEntidadNoExisteExcepcion`, `ErrorGenericoExcepcion`, `ErrorMalFormatoExcepcion`, `ErrorNoInformacionExcepcion`, `ErrorReglaNegocioVioladaExcepcion`, `ErrorSinAccesoExcepcion`, `ErrorTokenExpiradoExcepcion`, `Mapper`
- Configuración (11): `BeanConfiguracion`, `CatalogoAcademicoExcel [EXT]`, `ProcesadorArchivos`, `AlmacenadorArchivos`, `ValidadorPeticionesExcel`, `ConfiguracionAplicacion`, `ConfiguracionSeguridad`, `CustomAccessDeniedHandler`, `CustomAuthenticationEntryPoint`, `JwtFiltroAutenticacion`, `JwtServicio`

## 5. Regla de dependencias entre capas

### 5.1 Regla declarada

`backend/CLAUDE.md` ("Las tres capas, en orden de dependencia") fija: `dominio/` no depende de Spring ni de JPA; `aplicacion/` contiene solo interfaces; la infraestructura implementa los puertos. Una petición entra por `*RestController` -> `*CUIntPuerto` (implementado por `*CUImplAdaptador`) -> `*GatewayIntPuerto` (implementado en `infraestructura/output/persistencia/gateway`) -> repositorio JPA; "el caso de uso nunca ve una entidad JPA ni un DTO".

Sentido permitido de las flechas (de quien importa hacia lo importado): `infraestructura/input` -> `aplicacion/input` y `dominio/modelos`; `dominio/casosdeuso` y `dominio/servicios` -> `aplicacion/*` y `dominio/modelos`; `aplicacion/*` -> `dominio/modelos`; `infraestructura/output` -> `aplicacion/output` y `dominio/modelos`; `infraestructura/configuracion` puede ver todo (ensambla).

### 5.2 Matriz real de imports

Conteo de sentencias `import com.unicauca.cfiet.solicitudes.<capa>...` por capa de origen (filas) y destino (columnas), solo entre capas distintas, calculado leyendo los 459 archivos de main. "Archivos" = archivos distintos que contienen al menos un import hacia esa capa.

| Origen \ Destino | aplicacion | dominio | infra.input | infra.output | infra.configuracion |
|---|---|---|---|---|---|
| **aplicacion** | 0 | 108 (55 arch.) | 0 | 0 | 0 |
| **dominio** | 147 (36 arch.) | 0 | 0 | 34 (33 arch.) | 2 (2 arch.) |
| **infra.input** | 30 (16 arch.) | 80 (30 arch.) | 0 | 2 (2 arch.) | 14 (5 arch.) |
| **infra.output** | 31 (30 arch.) | 102 (59 arch.) | 0 | 0 | 3 (3 arch.) |
| **infra.configuracion** | 15 (8 arch.) | 6 (3 arch.) | 30 (13 arch.) | 15 (13 arch.) | 0 |

Lectura: `aplicacion` solo importa de `dominio` (modelos y `PaginacionRespuestaDTO`); `dominio` importa de `aplicacion` (puertos) y, por las excepciones del apartado 5.3, de `infra.output` e `infra.configuracion`; `infra.input` e `infra.output` importan de `aplicacion` y `dominio`; `infra.input` -> `infra.output` son 2 imports: `MensajesError` (no cuenta como violación de entrada) y `ErrorMalFormatoExcepcion` en `ValidadorAnexosService`. No hay ningún import de `infra.input` hacia `infra.output.persistencia`, ni de `infra.output` hacia `infra.input`; ningún controlador ni gateway importa un `casosdeuso`, salvo `BeanConfiguracion` (que instancia todos) y `CatalogoAcademicoExcel` (que usa `dominio/servicios`).

### 5.3 Verificación con grep: ¿el dominio importa Spring, JPA o infraestructura?

Comandos (sobre `dominio/` y `aplicacion/`): `grep -rn "org\.springframework\|jakarta\.\|javax\.\|org\.hibernate\|com\.fasterxml\|io\.jsonwebtoken\|org\.apache\|org\.modelmapper" dominio aplicacion` y `grep -rn "infraestructura\." dominio aplicacion`.

**JPA / Jakarta Persistence / Hibernate: 0 violaciones.** Ningún archivo de `dominio/` o `aplicacion/` importa `jakarta.*`, `javax.*` ni `org.hibernate`; los modelos de dominio no llevan anotaciones de persistencia ni de Bean Validation (esas están en entidades y DTO).

**Spring en dominio/aplicación: 4 archivos, todos [BASE]** (código de Julián, sin cambios desde `d3a48cc`):

| # | Archivo | Línea | Import / uso | Origen |
|---|---|---|---|---|
| 1 | `dominio/casosdeuso/RespuestaCUImplAdaptador.java` | 14, 15 | `org.springframework.beans.factory.annotation.Value` (usado en 3 campos `@Value("${url.backend}")`, `${url.application}`, `${app.uploads.base-path}`) y `org.springframework.web.multipart.MultipartFile` (parámetro de `responderSolicitud`, línea 163) | BASE |
| 2 | `dominio/casosdeuso/SolicitudCUImplAdaptador.java` | 11, 12 | `@Value` (`${url.backend}`, `${url.application}`) y `MultipartFile` (líneas 110, 140, 350) | BASE |
| 3 | `dominio/modelos/Anexo.java` | 4 | `MultipartFile anexoFile` (campo del modelo de dominio, línea 19) | BASE |
| 4 | `aplicacion/input/RespuestaCUIntPuerto.java` | 5 | `MultipartFile` en la firma `responderSolicitud(String, MultipartFile, String)` (línea 88) | BASE |

**Dominio -> infraestructura (violación de la dirección de dependencia): 36 imports en 33 archivos distintos**, distinta de Spring/JPA:

| Import | Archivos | BASE | EXT |
|---|---|---|---|
| `infraestructura.output.manejadorExcepciones.MensajesError` (constantes de texto de error) | 33 | 8 | 25 |
| `infraestructura.output.manejadorExcepciones.estructura.CodigoError` | 1 (`LogCUImplAdaptador`) | 1 | 0 |
| `infraestructura.configuracion.lectorArchivos.almacenador.AlmacenadorArchivos` (clase concreta con `@Service`, usada como dependencia del caso de uso) | 2 (`RespuestaCUImplAdaptador`, `SolicitudCUImplAdaptador`) | 2 | 0 |

Observaciones:

1. El dominio depende de `MensajesError` (clase de infraestructura) en 33 archivos: 8 [BASE] (`LogCU`, `OrdenDelDiaCU`, `RespuestaCU`, `RolCU`, `SesionCU`, `SolicitudCU`, `TipoSolicitudCU`, `UsuarioCU`) y 25 [EXTENSIÓN] (19 casos de uso y los 6 servicios de dominio que usan excepciones: `DatosPersonales`, `MaquinaEtapas`, `TramiteCancelacion`, `TramiteSolicitud`, `ValidadorActorSolicitud`, `ValidadorArchivoAdjunto`; no lo importan `ProgramaAcademicoCU`, `SituacionAcademicaAsignaturaCU` ni `TipoSolicitudAcademicaCU`). Es una dependencia a una clase de constantes, sin Spring ni JPA, pero rompe la regla de que `dominio` no mire hacia `infraestructura`. La extensión **replica** el patrón de Julián, tal como lo exige `backend/CLAUDE.md` ("se agrega como constante en `MensajesError`").
2. `AlmacenadorArchivos` es un `@Service` concreto en `infraestructura/configuracion/lectorArchivos/almacenador`; los casos de uso [BASE] de Respuestas y Solicitudes lo reciben por constructor (el `@Bean` correspondiente lo pasa), es decir, no hay puerto de salida para él. La extensión **sí** introduce un puerto (`AlmacenamientoAnexosIntPuerto`, `AlmacenamientoAnexosImplAdaptador`) que internamente reutiliza `AlmacenadorArchivos`.
3. Con el criterio estricto de la regla declarada ("dominio sin Spring ni JPA"), el resultado es: **JPA = 0 violaciones; Spring = 4 archivos [BASE] y 0 [EXTENSIÓN]**. La única dependencia hacia `infraestructura` de la que participa la extensión es `MensajesError`.
4. Ningún caso de uso, servicio ni modelo de la extensión importa Spring, JPA ni `MultipartFile`: los archivos entran al dominio como `ArchivoAdjunto` (modelo propio con `byte[]`, `nombreOriginal`, `tipoContenido`), construido por el mapper de entrada (`MapperSolicitudAcademicaInfraestructuraDominio.mapearArchivo`), y `MensajesError` es la única dependencia hacia infraestructura. Verificado con el grep anterior: los únicos `org.springframework` en `dominio/` y `aplicacion/` son los 4 archivos de la tabla.
5. `PaginacionRespuestaDTO` (en `dominio/helper`, [BASE]) es un tipo de transporte llamado "DTO" que los puertos de entrada y salida usan para paginar; el caso de uso devuelve así un tipo con nombre de DTO, aunque vive en dominio y no importa nada de framework. Se registra como matiz a "el caso de uso nunca ve un DTO".

### 5.4 Otras comprobaciones de la regla

- **Casos de uso sin estereotipo Spring**: ninguna clase de `dominio/` ni `aplicacion/` lleva `@Component`, `@Service`, `@Autowired`, `@Entity`, `@Transactional` ni `@Repository` (grep sin resultados). Los `*CUImplAdaptador` se registran solo en `BeanConfiguracion`.
- **Controladores**: ninguno importa una entidad, un repositorio ni un gateway; inyectan `*CUIntPuerto`, su `Mapper*InfraestructuraDominio` y, en los de carga masiva, `ProcesadorArchivos`, `ValidadorPeticionesExcel`, `CatalogoAcademicoExcel`, `AgrupadorEstudiantesExcel`, `DuplicadosFuncionariosAcademicosExcel`. Excepciones [BASE]: `RespuestaRestController.descargarRespuesta` y `SolicitudesRestController.descargarAnexo` leen archivos del disco directamente (`java.io.File`) sin pasar por un caso de uso; `SolicitudesRestController` además inyecta `ValidadorAnexosService`.
- **Gateways**: devuelven modelos de dominio mediante `*OwnMapper`; los repositorios y entidades no salen de `infraestructura/output/persistencia`. Ninguna clase de `infraestructura/input` importa `...persistencia...` (grep `import .*infraestructura.output` en `infraestructura/input` solo arroja `MensajesError` y `ErrorMalFormatoExcepcion`).
- **Mappers de entrada**: son `@Component` en [EXTENSIÓN] y [BASE]; los [BASE] reciben `ModelMapper` con `@Qualifier("mapeadorSimple")` (bean definido en `infraestructura/output/persistencia/mapeador/Mapper.java`). Los de [EXTENSIÓN] no usan `ModelMapper` (mapeo manual).

## 6. Inyección de dependencias

### 6.1 Cómo se arma

1. **Casos de uso: fábrica manual en `BeanConfiguracion`** (`infraestructura/configuracion/BeanConfiguracion.java`, `@Configuration`, 345 líneas, 31 métodos `@Bean`). Cada método recibe los puertos de salida que necesita el caso de uso (más `ExcepcionesFormateadorIntPuerto`, `LogCUIntPuerto`, etc.) y devuelve `new XCUImplAdaptador(...)` con el tipo de retorno **concreto**. Los 30 `*CUImplAdaptador` tienen su `@Bean`; el bean 31 es `crearMaquinaEtapas` (`MaquinaEtapas`, servicio de dominio con `@Bean`, no `@Component`). Verificado: el conjunto de tipos de retorno de los `@Bean` es exactamente los 30 casos de uso más `MaquinaEtapas`.
2. **Dependencias entre casos de uso: implícitas por tipo**. Ningún método `@Bean` llama a otro; cuando un caso de uso depende de otro recibe el puerto de entrada (`LogCUIntPuerto`, `UsuarioCUIntPuerto`, `SolicitudAcademicaCUIntPuerto`, `AnexoAcademicoCUIntPuerto`, `ConsultaSolicitudAcademicaCUIntPuerto`) y Spring resuelve el bean que lo implementa. Como cada puerto tiene una sola implementación no se usan `@Qualifier` para casos de uso. Spring ordena la creación solo (no hay ciclos: verificado por la existencia de los beans, no por arranque, NO VERIFICADO en ejecución).
3. **Puertos de salida: autodescubrimiento por estereotipo**. Los 25 gateways persistentes son `@Service` + `@RequiredArgsConstructor` (inyección por constructor) y se resuelven por el tipo del puerto. Igual `ExcepcionesFormateadorImplAdaptador`, `PasswordEnconderGatewayImplAdaptador`, `OrdenDelDiaExportadorImpl`, `JwtServicioImpl` (que envuelve a `JwtServicio` de seguridad) y, en la extensión, `AlmacenamientoAnexosImplAdaptador`. Los repositorios se generan con `@Repository`/`JpaRepository` y los `*OwnMapper` son `@Service`.
4. **Capa de entrada**: 15 `@RestController`, 15 mapeadores `@Component` y `ValidadorAnexosService` (`@Service`) se inyectan por constructor (`@RequiredArgsConstructor` o constructor explícito). Los controladores de carga masiva usan `@Qualifier` por nombre de bean para elegir `ProcesadorArchivos<T>` y `ValidadorPeticionesExcel<T>`: `archivos-usuarios`/`validador-usuarios`, `archivos-tipos-solicitudes`/`validador-tipos-solicitud` [BASE]; `archivos-asignaturas`/`validador-asignaturas`, `archivos-estudiantes`/`validador-estudiantes`, `archivos-funcionarios-academicos`/`validador-funcionarios-academicos` [EXTENSIÓN].
5. **Servicios de dominio sin Spring**: `TramiteSolicitud`, `TramiteCancelacion`, `ValidadorActorSolicitud`, `ValidadorArchivoAdjunto` y `DatosPersonales` se crean con `new` dentro del constructor del caso de uso o de otro servicio (por ejemplo `TramiteCancelacion` crea su `TramiteSolicitud`; `TramiteSolicitud` crea su `ValidadorActorSolicitud`; `TramiteExamenSupletorioCUImplAdaptador` crea su `TramiteSolicitud`). `ReglasAsignatura` es una clase `final` con constructor privado y métodos estáticos. El único servicio de dominio que se registra como bean es `MaquinaEtapas`.
6. **Reloj**: los casos de uso que fechan o miden plazos reciben un `Clock` que el propio método `@Bean` crea con `Clock.system(ZoneId.of("America/Bogota"))` (no es un bean compartido): 4 beans lo reciben (`crearSolicitudAcademicaCU`, `crearAnexoAcademicoCU`, `crearResolucionAcademicaCU`, `crearExamenSupletorioCU`; `grep -c "Clock.system"` = 4).
7. **Otros beans de configuración**: `ConfiguracionAplicacion` (`@Bean` `AuthenticationManager`, `UserDetailsService` que busca por `findByUsername` en `UsuarioRepositorio`, `AuthenticationProvider`, `PasswordEncoder` = `BCryptPasswordEncoder`), `ConfiguracionSeguridad` (`@Bean` `SecurityFilterChain` y `UrlBasedCorsConfigurationSource`, `@EnableWebSecurity`), `Mapper` (`@Bean("mapeadorSimple")` `ModelMapper`). Total de métodos `@Bean` en el proyecto: **38** (31 + 4 + 2 + 1).
8. **Estereotipos en infraestructura**: `@Service` 78 archivos (25 gateways, 32 `OwnMapper`, 14 de `lectorArchivos`: 5 `Procesar*Impl`, 5 validadores `*ExcelService`, `AlmacenadorArchivos`, `AgrupadorEstudiantesExcel`, `CatalogoAcademicoExcel`, `DuplicadosFuncionariosAcademicosExcel`; y 7 más: `JwtServicio`, `JwtServicioImpl`, `ValidadorAnexosService`, `AlmacenamientoAnexosImplAdaptador`, `PasswordEnconderGatewayImplAdaptador`, `OrdenDelDiaExportadorImpl`, `ExcepcionesFormateadorImplAdaptador`), `@Component` 18 (15 mapeadores de entrada + `CustomAccessDeniedHandler`, `CustomAuthenticationEntryPoint`, `JwtFiltroAutenticacion`), `@Configuration` 4, `@RestController` 15, `@ControllerAdvice` 1, `@Repository` 29, `@Entity` 32. Conteos por grep de la anotación a nivel de clase.

### 6.2 Los 31 métodos `@Bean` de `BeanConfiguracion`

"Deps." incluye el `Clock` que el método crea localmente. Origen = origen del caso de uso (el método vive en un archivo [BASE] ampliado por la extensión).

| Origen | Método `@Bean` | Retorno | Deps. | Parámetros (puertos y servicios inyectados) |
|---|---|---|---|---|
| BASE | createRolCU | RolCUImplAdaptador | 3 | RolGatewayIntPuerto, ExcepcionesFormateadorIntPuerto, LogCUIntPuerto |
| BASE | createUsuarioCU | UsuarioCUImplAdaptador | 4 | UsuarioGatewayIntPuerto, ExcepcionesFormateadorIntPuerto, LogCUIntPuerto, PasswordEncoderGatewayIntPuerto |
| BASE | createSesionCU | SesionCUImplAdaptador | 3 | SesionGatewayIntPuerto, LogCUIntPuerto, ExcepcionesFormateadorIntPuerto |
| BASE | crearLogCU | LogCUImplAdaptador | 3 | LogGatewayIntPuerto, ExcepcionesFormateadorIntPuerto, IJwtServicio |
| BASE | crearTipoSolicitudCU | TipoSolicitudCUImplAdaptador | 5 | TipoSolicitudGatewayIntPuerto, UsuarioGatewayIntPuerto, ExcepcionesFormateadorIntPuerto, LogCUIntPuerto, RolGatewayIntPuerto |
| BASE | crearOrdenDelDiaCU | OrdenDelDiaCUImplAdaptador | 6 | OrdenDelDiaGatewayIntPuerto, SolicitudGatewayIntPuerto, ExcepcionesFormateadorIntPuerto, LogCUIntPuerto, OrdenDelDiaExportador, RespuestaGatewayIntPuerto |
| BASE | crearSolicitudCU | SolicitudCUImplAdaptador | 9 | SolicitudGatewayIntPuerto, ExcepcionesFormateadorIntPuerto, TipoSolicitudGatewayIntPuerto, OrdenDelDiaGatewayIntPuerto, UsuarioGatewayIntPuerto, LogCUIntPuerto, SesionGatewayIntPuerto, IJwtServicio, AlmacenadorArchivos |
| BASE | crearRespuestaCU | RespuestaCUImplAdaptador | 5 | RespuestaGatewayIntPuerto, SolicitudGatewayIntPuerto, ExcepcionesFormateadorIntPuerto, LogCUIntPuerto, AlmacenadorArchivos |
| EXTENSIÓN | crearAsignaturaCU | AsignaturaCUImplAdaptador | 4 | AsignaturaGatewayIntPuerto, ProgramaAcademicoGatewayIntPuerto, ExcepcionesFormateadorIntPuerto, LogCUIntPuerto |
| EXTENSIÓN | crearProgramaAcademicoCU | ProgramaAcademicoCUImplAdaptador | 1 | ProgramaAcademicoGatewayIntPuerto |
| EXTENSIÓN | crearEstudianteCU | EstudianteCUImplAdaptador | 11 | UsuarioCUIntPuerto, EstudianteGatewayIntPuerto, AsignaturaGatewayIntPuerto, ProgramaAcademicoGatewayIntPuerto, RolGatewayIntPuerto, UsuarioGatewayIntPuerto, DatosPersonalesGatewayIntPuerto, SesionGatewayIntPuerto, IJwtServicio, ExcepcionesFormateadorIntPuerto, LogCUIntPuerto |
| EXTENSIÓN | crearFuncionarioAcademicoCU | FuncionarioAcademicoCUImplAdaptador | 7 | UsuarioCUIntPuerto, FuncionarioAcademicoGatewayIntPuerto, RolGatewayIntPuerto, UsuarioGatewayIntPuerto, DatosPersonalesGatewayIntPuerto, ExcepcionesFormateadorIntPuerto, LogCUIntPuerto |
| EXTENSIÓN | crearTipoSolicitudAcademicaCU | TipoSolicitudAcademicaCUImplAdaptador | 1 | TipoSolicitudAcademicaGatewayIntPuerto |
| EXTENSIÓN | crearEtapaSolicitudAcademicaCU | EtapaSolicitudAcademicaCUImplAdaptador | 4 | EtapaSolicitudAcademicaGatewayIntPuerto, EtapaEtiquetaRolGatewayIntPuerto, TipoSolicitudAcademicaGatewayIntPuerto, ExcepcionesFormateadorIntPuerto |
| EXTENSIÓN | crearTipoAnexoAcademicoCU | TipoAnexoAcademicoCUImplAdaptador | 3 | TipoAnexoAcademicoGatewayIntPuerto, TipoSolicitudAcademicaGatewayIntPuerto, ExcepcionesFormateadorIntPuerto |
| EXTENSIÓN | crearSituacionAcademicaAsignaturaCU | SituacionAcademicaAsignaturaCUImplAdaptador | 1 | SituacionAcademicaAsignaturaGatewayIntPuerto |
| EXTENSIÓN | crearAsignacionFuncionarioAcademicoCU | AsignacionFuncionarioAcademicoCUImplAdaptador | 4 | TipoSolicitudAcademicaGatewayIntPuerto, FuncionarioAcademicoGatewayIntPuerto, ExcepcionesFormateadorIntPuerto, LogCUIntPuerto |
| EXTENSIÓN | crearMaquinaEtapas | MaquinaEtapas | 1 | ExcepcionesFormateadorIntPuerto |
| EXTENSIÓN | crearSolicitudAcademicaCU | SolicitudAcademicaCUImplAdaptador | 12 | SolicitudAcademicaGatewayIntPuerto, EstudianteGatewayIntPuerto, TipoSolicitudAcademicaGatewayIntPuerto, EtapaSolicitudAcademicaGatewayIntPuerto, ResolucionAcademicaGatewayIntPuerto, AnexoAcademicoGatewayIntPuerto, UsuarioGatewayIntPuerto, AlmacenamientoAnexosIntPuerto, MaquinaEtapas, ExcepcionesFormateadorIntPuerto, LogCUIntPuerto, Clock(new) |
| EXTENSIÓN | crearAnexoAcademicoCU | AnexoAcademicoCUImplAdaptador | 9 | AnexoAcademicoGatewayIntPuerto, SolicitudAcademicaGatewayIntPuerto, TipoSolicitudAcademicaGatewayIntPuerto, TipoAnexoAcademicoGatewayIntPuerto, UsuarioGatewayIntPuerto, AlmacenamientoAnexosIntPuerto, ExcepcionesFormateadorIntPuerto, LogCUIntPuerto, Clock(new) |
| EXTENSIÓN | crearResolucionAcademicaCU | ResolucionAcademicaCUImplAdaptador | 7 | ResolucionAcademicaGatewayIntPuerto, SolicitudAcademicaGatewayIntPuerto, UsuarioGatewayIntPuerto, AlmacenamientoAnexosIntPuerto, ExcepcionesFormateadorIntPuerto, LogCUIntPuerto, Clock(new) |
| EXTENSIÓN | crearConsultaSolicitudAcademicaCU | ConsultaSolicitudAcademicaCUImplAdaptador | 10 | SolicitudAcademicaGatewayIntPuerto, EtapaEtiquetaRolGatewayIntPuerto, AnexoAcademicoGatewayIntPuerto, HistorialSolicitudAcademicaGatewayIntPuerto, ResolucionAcademicaGatewayIntPuerto, SesionGatewayIntPuerto, IJwtServicio, AnexoAcademicoCUIntPuerto, MaquinaEtapas, ExcepcionesFormateadorIntPuerto |
| EXTENSIÓN | crearCancelacionMatriculaCU | CancelacionMatriculaCUImplAdaptador | 9 | SolicitudCancelacionMatriculaGatewayIntPuerto, EstudianteGatewayIntPuerto, UsuarioGatewayIntPuerto, TipoSolicitudAcademicaGatewayIntPuerto, TipoAnexoAcademicoGatewayIntPuerto, SolicitudAcademicaCUIntPuerto, AnexoAcademicoCUIntPuerto, ExcepcionesFormateadorIntPuerto, LogCUIntPuerto |
| EXTENSIÓN | crearTramiteCancelacionMatriculaCU | TramiteCancelacionMatriculaCUImplAdaptador | 10 | SolicitudCancelacionMatriculaGatewayIntPuerto, SolicitudAcademicaGatewayIntPuerto, SituacionAcademicaAsignaturaGatewayIntPuerto, UsuarioGatewayIntPuerto, SesionGatewayIntPuerto, IJwtServicio, SolicitudAcademicaCUIntPuerto, MaquinaEtapas, ExcepcionesFormateadorIntPuerto, LogCUIntPuerto |
| EXTENSIÓN | crearConsultaCancelacionMatriculaCU | ConsultaCancelacionMatriculaCUImplAdaptador | 9 | SolicitudCancelacionMatriculaGatewayIntPuerto, EstudianteGatewayIntPuerto, TipoSolicitudAcademicaGatewayIntPuerto, TipoAnexoAcademicoGatewayIntPuerto, SesionGatewayIntPuerto, IJwtServicio, ConsultaSolicitudAcademicaCUIntPuerto, MaquinaEtapas, ExcepcionesFormateadorIntPuerto |
| EXTENSIÓN | crearCancelacionAsignaturaCU | CancelacionAsignaturaCUImplAdaptador | 9 | SolicitudCancelacionAsignaturaGatewayIntPuerto, SolicitudCancelacionMatriculaGatewayIntPuerto, EstudianteGatewayIntPuerto, UsuarioGatewayIntPuerto, TipoSolicitudAcademicaGatewayIntPuerto, SolicitudAcademicaCUIntPuerto, AnexoAcademicoCUIntPuerto, ExcepcionesFormateadorIntPuerto, LogCUIntPuerto |
| EXTENSIÓN | crearTramiteCancelacionAsignaturaCU | TramiteCancelacionAsignaturaCUImplAdaptador | 11 | SolicitudCancelacionAsignaturaGatewayIntPuerto, SolicitudCancelacionMatriculaGatewayIntPuerto, SolicitudAcademicaGatewayIntPuerto, SituacionAcademicaAsignaturaGatewayIntPuerto, UsuarioGatewayIntPuerto, SesionGatewayIntPuerto, IJwtServicio, SolicitudAcademicaCUIntPuerto, MaquinaEtapas, ExcepcionesFormateadorIntPuerto, LogCUIntPuerto |
| EXTENSIÓN | crearConsultaCancelacionAsignaturaCU | ConsultaCancelacionAsignaturaCUImplAdaptador | 7 | SolicitudCancelacionAsignaturaGatewayIntPuerto, EstudianteGatewayIntPuerto, SesionGatewayIntPuerto, IJwtServicio, ConsultaSolicitudAcademicaCUIntPuerto, MaquinaEtapas, ExcepcionesFormateadorIntPuerto |
| EXTENSIÓN | crearExamenSupletorioCU | ExamenSupletorioCUImplAdaptador | 10 | SolicitudExamenSupletorioGatewayIntPuerto, EstudianteGatewayIntPuerto, UsuarioGatewayIntPuerto, TipoSolicitudAcademicaGatewayIntPuerto, TipoAnexoAcademicoGatewayIntPuerto, SolicitudAcademicaCUIntPuerto, AnexoAcademicoCUIntPuerto, ExcepcionesFormateadorIntPuerto, LogCUIntPuerto, Clock(new) |
| EXTENSIÓN | crearTramiteExamenSupletorioCU | TramiteExamenSupletorioCUImplAdaptador | 9 | SolicitudExamenSupletorioGatewayIntPuerto, SolicitudAcademicaGatewayIntPuerto, UsuarioGatewayIntPuerto, SesionGatewayIntPuerto, IJwtServicio, SolicitudAcademicaCUIntPuerto, MaquinaEtapas, ExcepcionesFormateadorIntPuerto, LogCUIntPuerto |
| EXTENSIÓN | crearConsultaExamenSupletorioCU | ConsultaExamenSupletorioCUImplAdaptador | 8 | SolicitudExamenSupletorioGatewayIntPuerto, EstudianteGatewayIntPuerto, TipoSolicitudAcademicaGatewayIntPuerto, TipoAnexoAcademicoGatewayIntPuerto, SesionGatewayIntPuerto, IJwtServicio, ConsultaSolicitudAcademicaCUIntPuerto, ExcepcionesFormateadorIntPuerto |

Observaciones sobre los beans: (a) los nombres de los tres primeros de Julián usan `create...` y el resto `crear...` (inconsistencia ya señalada por `backend/CLAUDE.md`); (b) los casos de uso con más dependencias son `SolicitudAcademicaCUImplAdaptador` (12), `EstudianteCUImplAdaptador` (11) y `TramiteCancelacionAsignaturaCUImplAdaptador` (11); (c) el árbol de dependencias entre casos de uso en la extensión es: `Cancelacion*`/`ExamenSupletorio` -> `SolicitudAcademicaCU` + `AnexoAcademicoCU`; `Tramite*` -> `SolicitudAcademicaCU`; `Consulta*` -> `ConsultaSolicitudAcademicaCU`; `EstudianteCU`/`FuncionarioAcademicoCU` -> `UsuarioCU` (reutilizan la creación de usuarios de Julián); casi todos -> `LogCU`.

### 6.3 Cadena en tiempo de ejecución

`JwtFiltroAutenticacion` (filtro antes de `UsernamePasswordAuthenticationFilter`) valida el token con `JwtServicio` y carga el usuario con el `UserDetailsService` de `ConfiguracionAplicacion`; `ConfiguracionSeguridad.securityFilterChain` decide el acceso por ruta; el `*RestController` llama al `*CUIntPuerto` (bean de `BeanConfiguracion`), que llama a los `*GatewayIntPuerto` (`@Service` de persistencia); los errores de negocio los lanza `ExcepcionesFormateadorImplAdaptador` y los traduce `RestApiExcepcion`. La transacción la abre el controlador con `@Transactional` (los casos de uso no la declaran; algunos métodos de gateway también la declaran).

## 7. Hallazgos y desviaciones de la hexagonal

| # | Hallazgo | Evidencia | Origen |
|---|---|---|---|
| 1 | El dominio importa clases de infraestructura (`MensajesError`, `CodigoError`, `AlmacenadorArchivos`) | 36 imports en 33 archivos distintos de `dominio/` (apartado 5.3) | BASE 8 archivos / EXTENSIÓN 25 archivos (patrón replicado) |
| 2 | Spring en dominio/aplicación (`@Value`, `MultipartFile`) | `RespuestaCUImplAdaptador`, `SolicitudCUImplAdaptador`, `Anexo`, `RespuestaCUIntPuerto` | BASE |
| 3 | `@Transactional` en los 13 controladores que lo usan y en 8 clases de `infraestructura/output/persistencia` (gateways), nunca en `dominio/`; los casos de uso dependen de que la transacción del controlador exista (por `LazyInitializationException` en `UsuarioGatewayIntPuerto.getUsuario`) | `backend/CLAUDE.md` "Patrón de los controladores"; grep `@Transactional`: 13 controladores + 8 de persistencia + 0 en dominio | BASE y EXT |
| 4 | `@PreAuthorize` presente en 14 archivos pero sin `@EnableMethodSecurity`: no se evalúa | grep sin resultados de `EnableMethodSecurity`; ver documento 03 sección 5 | BASE/EXT |
| 5 | Dos controladores [BASE] leen disco sin pasar por un caso de uso | `RespuestaRestController.descargarRespuesta`, `SolicitudesRestController.descargarAnexo` | BASE |
| 6 | Dos servicios JWT: `JwtServicio` (seguridad, usado por el filtro) y `JwtServicioImpl` (implementa `IJwtServicio` y delega en el primero) | `infraestructura/configuracion/seguridad/jwt`, `infraestructura/output/JWT` | BASE |
| 7 | Irregularidades de nombres heredadas: `SolicitudCUintPuerto` (minúscula), `UsuarioGatewayImplAdapter` (sin "ador"), `RolOwnMapperImpl` (sufijo "Impl"), `PasswordEnconderGatewayImplAdaptador` ("Enconder"), carpeta `output/JWT` en mayúsculas, `crear`/`create` en beans | nombres de archivo; `backend/CLAUDE.md` "Convención de nombres" | BASE |
| 8 | Los 7 mapeadores de entrada de Julián dependen de `ModelMapper` (reflexión); la extensión mapea a mano | `backend/CLAUDE.md`; constructores con `@Qualifier("mapeadorSimple")` solo en [BASE] | BASE vs EXT |
| 9 | La extensión no agrega puerto para `AlmacenadorArchivos`, pero lo envuelve con `AlmacenamientoAnexosIntPuerto` | `AlmacenamientoAnexosImplAdaptador` | EXT |
| 10 | `ObservacionDTOPeticion` vive en el paquete de Cancelación de Matrícula y lo importan los controladores de Cancelación de Asignatura y Examen Supletorio (DTO compartido entre módulos) | imports de `CancelacionAsignaturaRestController`, `ExamenSupletorioRestController` | EXT |

## 8. Entidades JPA y tablas (referencia rápida)

| Entidad | Tabla (`@Table`) | Origen |
|---|---|---|
| `AnexoAcademicoEntidad` | `ANEXO_ACADEMICO` | EXTENSIÓN |
| `AnexoEntidad` | `anexos` | BASE |
| `AsignaturaEntidad` | `ASIGNATURA` | EXTENSIÓN |
| `AsignaturaMatriculadaEntidad` | `ASIGNATURA_MATRICULADA` | EXTENSIÓN |
| `AsignaturaSolicitudAcademicaEntidad` | `ASIGNATURA_SOLICITUD_ACADEMICA` | EXTENSIÓN |
| `EstudianteEntidad` | `ESTUDIANTE` | EXTENSIÓN |
| `EtapaEtiquetaRolEntidad` | `ETAPA_ETIQUETA_ROL` | EXTENSIÓN |
| `EtapaEtiquetaRolIdEntidad` | (embebible, sin tabla) | EXTENSIÓN |
| `EtapaSolicitudAcademicaEntidad` | `ETAPA_SOLICITUD_ACADEMICA` | EXTENSIÓN |
| `FuncionarioAcademicoEntidad` | `FUNCIONARIO_ACADEMICO` | EXTENSIÓN |
| `FuncionarioEntidad` | `funcionarios` | BASE |
| `HistorialSolicitudAcademicaEntidad` | `HISTORIAL_SOLICITUD_ACADEMICA` | EXTENSIÓN |
| `InformacionSolicitanteEntidad` | `informacionSolicitantes` | BASE |
| `LogEntidad` | `logs` | BASE |
| `OrdenDelDiaEntidad` | `ordenesDelDia` | BASE |
| `ProgramaAcademicoEntidad` | `PROGRAMA_ACADEMICO` | EXTENSIÓN |
| `ResolucionAcademicaEntidad` | `RESOLUCION_ACADEMICA` | EXTENSIÓN |
| `RespuestaEntidad` | `respuestas` | BASE |
| `RolEntidad` | `roles` | BASE |
| `SituacionAcademicaAsignaturaEntidad` | `SITUACION_ACADEMICA_ASIGNATURA` | EXTENSIÓN |
| `SolicitudAcademicaEntidad` | `SOLICITUD_ACADEMICA` | EXTENSIÓN |
| `SolicitudCancelacionAsignaturaEntidad` | `SOLICITUD_CANCELACION_ASIGNATURA` | EXTENSIÓN |
| `SolicitudCancelacionMatriculaEntidad` | `SOLICITUD_CANCELACION_MATRICULA` | EXTENSIÓN |
| `SolicitudEntidad` | `solicitudes` | BASE |
| `SolicitudExamenSupletorioEntidad` | `SOLICITUD_EXAMEN_SUPLETORIO` | EXTENSIÓN |
| `SolicitudSupletorioCruceAsignaturaEntidad` | `SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA` | EXTENSIÓN |
| `TipoAnexoAcademicoEntidad` | `TIPO_ANEXO_ACADEMICO` | EXTENSIÓN |
| `TipoAnexoEntidad` | `tiposAnexos` | BASE |
| `TipoSolicitudAcademicaEntidad` | `TIPO_SOLICITUD_ACADEMICA` | EXTENSIÓN |
| `TipoSolicitudEntidad` | `tiposSolicitudes` | BASE |
| `TipoUsuarioEntidad` | `tiposUsuario` | BASE |
| `UsuarioEntidad` | `usuarios` | BASE |
| `UsuarioLivianoEntidad` | `usuariosLivianos` | BASE |

Hay 13 entidades/tablas [BASE] (`anexos`, `funcionarios`, `informacionSolicitantes`, `logs`, `ordenesDelDia`, `respuestas`, `roles`, `solicitudes`, `tiposAnexos`, `tiposSolicitudes`, `tiposUsuario`, `usuarios`, `usuariosLivianos`) y 20 clases [EXTENSIÓN] (19 tablas con `@Table` en mayúsculas más el `@Embeddable`), de acuerdo con las "19 tablas nuevas" de `CLAUDE.md`. `UsuarioEntidad extends UsuarioLivianoEntidad` (herencia JPA, `@SuperBuilder`). La estrategia de nombres es `PhysicalNamingStrategyStandardImpl` (sin conversión). Detalle de columnas y relaciones: documento 05.


===== 06-seguridad-y-errores.md =====

# 06 - Seguridad, manejo de errores y registro de acciones

Análisis de solo lectura del backend (`backend/solicitudes`) y de la parte del frontend que toca la sesión. Etiquetas: **[BASE]** código del prototipo de Julián (commit `d3a48cc`, "Inicializa el repositorio"), **[EXTENSIÓN]** agregado por este trabajo de grado. Todo lo no comprobado leyendo código figura como NO VERIFICADO. No se ejecutó nada (ni backend, ni pruebas, ni base de datos).

Raíz de rutas Java: `backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/` (abreviada `<J>/`).

## 1. Piezas de seguridad

| Pieza | Ruta y clase | Origen |
|---|---|---|
| Cadena de filtros y reglas por URL | `<J>/infraestructura/configuracion/seguridad/configuracion/ConfiguracionSeguridad.java`, método `securityFilterChain` | [BASE] estructura, [EXTENSIÓN] reglas |
| Beans de autenticación | `.../seguridad/configuracion/ConfiguracionAplicacion.java` (`authenticationManager`, `userDetailService`, `authenticationProvider`, `passwordEncoder`) | [BASE] |
| Respuesta 401 | `.../seguridad/configuracion/CustomAuthenticationEntryPoint.java` | [BASE] |
| Respuesta 403 | `.../seguridad/configuracion/CustomAccessDeniedHandler.java` | [BASE] |
| Filtro JWT | `.../seguridad/jwt/JwtFiltroAutenticacion.java` (`OncePerRequestFilter`) | [BASE] |
| Servicio JWT | `.../seguridad/jwt/JwtServicio.java` | [BASE] |
| Puerto para extraer el username del token | `<J>/aplicacion/output/IJwtServicio.java`, implementado en `<J>/infraestructura/output/JWT/JwtServicioImpl.java` | [BASE] |
| Login | `<J>/infraestructura/input/controladorSesion/controlador/SesionRestController.java` (`POST ${url.application}sesiones`) -> `<J>/dominio/casosdeuso/SesionCUImplAdaptador.java` (`login`) -> `<J>/infraestructura/output/persistencia/gateway/SesionGatewayImplAdaptador.java` (`login`) | [BASE] |
| Codificador | `<J>/infraestructura/output/encoder/PasswordEnconderGatewayImplAdaptador.java` (`encode`, `matches`) | [BASE] |
| Roles como authorities | `UsuarioEntidad.getAuthorities()` (`<J>/infraestructura/output/persistencia/entidades/UsuarioEntidad.java`) | [BASE] |
| Constantes de rol y perfiles | `<J>/dominio/helper/constantes/ApplicationConstantes.java` | [BASE] los roles `Secretario General`, `Decano`, `Funcionario` y 3 perfiles; [EXTENSIÓN] `ESTUDIANTE_ROL`, `FUNCIONARIO_ACADEMICO_ROL` y 6 perfiles compuestos |

Dependencias (`backend/solicitudes/pom.xml`): Spring Boot 3.5.4 (`spring-boot-starter-parent`), `spring-boot-starter-security`, `jjwt-api/impl/jackson` 0.11.2.

## 2. Flujo de autenticación paso a paso [BASE]

1. El cliente envía `POST /api/unicauca/fiet/consejo/sesiones` con `username` y `password` (`SesionDTOPeticion`, validado con `@Valid` en `SesionRestController.auth`). La ruta es pública (`permitAll`).
2. `SesionCUImplAdaptador.login` llama a `SesionGatewayImplAdaptador.login`.
3. El gateway busca el usuario con `UsuarioRepositorio.findByUsername`. Si no existe devuelve `null`; el caso de uso lanza entonces `lanzarCredencialesErroneas(MensajesError.CREDENCIALES_ERRONEAS)`.
4. Si existe, ejecuta `authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(...))`. El `DaoAuthenticationProvider` (bean `authenticationProvider`) carga el usuario con el `UserDetailsService` y compara la contraseña con `BCryptPasswordEncoder`. Una contraseña errónea lanza `BadCredentialsException`.
5. Si autentica, `JwtServicio.getToken(usuario)` genera el JWT.
6. De vuelta en el caso de uso: si `usuario.getEstado()` es falso se lanza `lanzarSinAcceso(MensajesError.NO_ACCESO)`; si algún rol del usuario tiene `estado` falso se lanza `lanzarSinAcceso(MensajesError.ROL_NO_HABILITADO)`.
7. Se registra el log "Inicio de sesión" con `LogCUImplAdaptador.crearLogSesion` (el username llega directo porque todavía no hay token en el contexto).
8. La respuesta es `UsuarioTokenizadoDTORespuesta` con `uuidUsuario` y `token` (campos del modelo `dominio/modelos/UsuarioTokenizado.java`). El rol no viaja en el cuerpo; el frontend guarda `uuidUsuario` en `localStorage` y consulta el usuario aparte (`AuthService`, `frontend/src/app/core/services/auth-service.ts`; detalle de la consulta NO VERIFICADO).

Peticiones posteriores: `JwtFiltroAutenticacion.doFilterInternal` lee la cabecera `Authorization: Bearer <token>`; si no hay token deja pasar la petición sin autenticar (la decide después `authorizeHttpRequests`); si lo hay, extrae el `subject`, carga el `UserDetails`, valida con `JwtServicio.isTokenValid` (mismo username y no expirado) y coloca un `UsernamePasswordAuthenticationToken` en el `SecurityContextHolder` con las authorities del usuario (nombres de sus roles). Si el token expiró (`ExpiredJwtException`) el filtro responde él mismo 401 con el JSON `Error` y el mensaje de `CodigoError.ERROR_TOKEN_EXPIRADO`.

Los casos de uso que necesitan saber quién actúa reciben el token crudo desde el controlador (`@RequestHeader("Authorization") String token`, luego `token.substring(7)`) y lo resuelven con `IJwtServicio.getUsername`; por ejemplo `SolicitudAcademicaRestController.getDetalle` (patrón [EXTENSIÓN] repetido de la base).

## 3. El JWT [BASE]

Fuente: `JwtServicio.java`.

| Aspecto | Valor |
|---|---|
| Biblioteca | jjwt 0.11.2 (`Jwts.builder()` / `parserBuilder()`) |
| Algoritmo | HS256 (`SignatureAlgorithm.HS256`) |
| Clave | constante `SECRET_KEY` escrita en el código fuente, hexadecimal de 64 caracteres que se decodifica con `Decoders.BASE64`: `<REDACTADO>` |
| Claims propios | Ninguno. `getToken(UserDetails)` pasa `new HashMap<>()` como claims extra |
| Claims estándar | `sub` = username, `iat` = momento de emisión, `exp` = emisión + 24 h (`24 * 60 * 60 * 1000` ms) |
| Roles en el token | No. Las authorities se leen de la base de datos en cada petición vía `UserDetailsService` (`ConfiguracionAplicacion.userDetailService`), de modo que un cambio de rol o de estado aplica de inmediato aunque el token siga vigente |
| Refresco | No existe refresh token ni endpoint de renovación (no se encontró) |
| Revocación / logout en servidor | No existe; el logout del frontend solo borra el token del navegador |
| Validación | `isTokenValid`: username igual al del `UserDetails` y `!isTokenExpired` |

Observación: el estado del usuario (`estado`) solo se comprueba al iniciar sesión (`SesionCUImplAdaptador.login`); el filtro JWT no vuelve a revisarlo. Un usuario desactivado conserva acceso con un token ya emitido hasta que expire (NO VERIFICADO en ejecución; se infiere de `JwtFiltroAutenticacion`, que no consulta `estado`, y de que `UsuarioEntidad` no se vio sobreescribiendo `isEnabled`).

## 4. Contraseñas [BASE]

- `ConfiguracionAplicacion.passwordEncoder()` devuelve `new BCryptPasswordEncoder()` (fuerza por defecto de Spring Security, 10 rondas).
- `PasswordEnconderGatewayImplAdaptador.encode` y `.matches` se usan al crear usuarios y al cambiar contraseña (log "Actualizar Usuario - Usuario actualizo contraseña con éxito" en `UsuarioCUImplAdaptador`).
- `src/main/resources/data.sql` inserta el usuario inicial `rootfiet` (Secretario General) con la contraseña ya codificada con BCrypt; contraseña de texto plano documentada en el `README.md` raíz: `<REDACTADO>`.
- Los usuarios Estudiante y Funcionario Académico [EXTENSIÓN] se crean por el mismo `UsuarioCUImplAdaptador` (la contraseña pasa por el mismo codificador).

## 5. CORS y CSRF

Fuente: `ConfiguracionSeguridad.corsConfigurationSource()` [BASE].

| Parámetro | Valor |
|---|---|
| Orígenes permitidos | uno solo: `${url.frontend}` (en `application.properties`: `http://localhost:4200`); fijo, no cambia por entorno |
| Métodos | GET, POST, PUT, PATCH, DELETE, OPTIONS |
| Cabeceras permitidas | `*` |
| Credenciales | `allowCredentials = true` |
| Cabeceras expuestas | `Authorization` |
| Alcance | `/**` |

Además cada controlador trae `@CrossOrigin(origins = "${url.frontend}")` (por ejemplo `SesionRestController`, `LogRestController`) [BASE].

CSRF: deshabilitado (`csrf(AbstractHttpConfigurer::disable)`) y sesión `STATELESS` (`SessionCreationPolicy.STATELESS`). Es coherente con un API que autentica por cabecera Bearer.

## 6. Reglas de acceso por URL (`securityFilterChain`)

`baseUrl` = `${url.application}` = `/api/unicauca/fiet/consejo/`. Constantes de rol: SG = `Secretario General`, DEC = `Decano`, FUN = `Funcionario` (Julián), FA = `Funcionario Académico`, EST = `Estudiante`.

Cantidad de `requestMatchers`: 17 en el commit base (`git show d3a48cc:...ConfiguracionSeguridad.java`) y 78 hoy, es decir 61 agregados por la extensión. Spring evalúa en orden, gana la primera coincidencia.

### 6.1 Reglas heredadas [BASE] (17)

| Orden | Método y ruta | Acceso |
|---|---|---|
| 1 | cualquiera `sesiones` | pública |
| 2 | POST `solicitudes/public` | pública |
| 3-6 | GET `tipos/solicitudes/perfil`, `.../perfil/paginado`, `.../perfil/filtro`, `.../{uuidTipoSolicitud}` | públicas |
| 7 | `tipos/solicitudes/**` (resto) | SG, DEC |
| 8 | GET `solicitudes/orden-del-dia/estado` | autenticado |
| 9 | `solicitudes/orden-del-dia/**` | SG, DEC |
| 10 | `logs/**` | SG, DEC |
| 11 | `roles/**` | SG, DEC |
| 12 | GET `usuarios/**` | autenticado (en el commit base) |
| 13 | PATCH `usuarios/**` | autenticado (cambio de contraseña propia) |
| 14 | `usuarios/**` (resto) | SG, DEC |
| 15 | `solicitudes/**` | autenticado |
| 16 | GET `respuestas/**` | autenticado |
| 17 | POST `respuestas/**` | SG, DEC, FUN |
| final | `anyRequest()` | autenticado |

### 6.2 Reglas agregadas [EXTENSIÓN]

- `usuarios`: el commit `ac4fb00` ("restringir los listados de usuarios a Secretario General y Decano") antepone cinco GET a la regla genérica: `usuarios`, `usuarios/paginado`, `usuarios/filtro`, `usuarios/funcionarios` y `usuarios/tipos` con acceso SG, DEC. El resto de GET `usuarios/**` sigue autenticado.
- Ocho prefijos académicos, cada uno cerrado con `denyAll()` para que una ruta olvidada quede bloqueada en vez de caer en `anyRequest().authenticated()`:

| Prefijo | Reglas |
|---|---|
| `asignaturas` | GET `programa/{uuidPrograma}` y GET `asignaturas/**`: SG, DEC, FA. POST `cargar/archivo`, POST `asignaturas`, PUT `asignaturas/**`: SG, DEC. Resto: `denyAll` |
| `estudiantes` | GET `mis-asignaturas`: EST. GET `paginado`, `filtro`, `{uuidEstudiante}`: SG, DEC, FA. POST `cargar/archivo`, POST `estudiantes`, PUT `{uuid}`, POST `{uuid}/asignaturas`, PATCH `{uuid}/asignaturas/{uuidMatricula}/estado`: SG, DEC. Resto: `denyAll` |
| `funcionarios-academicos` | GET `paginado`, `filtro`, `{uuidFuncionario}`: SG, DEC, FA. POST `cargar/archivo`, POST, PUT `{uuid}`: SG, DEC. Resto: `denyAll` |
| `catalogos-academicos` | PUT `tipos-solicitud/{uuidTipo}/funcionario`: SG, DEC. GET `programas` y GET `catalogos-academicos/**`: autenticado. Resto: `denyAll` |
| `solicitudes-academicas` | GET `estudiante`: EST. GET `funcionario`: FA. GET `decano`: DEC. GET `{uuid}`, `{uuid}/historial`, `{uuid}/anexos/{uuidAnexo}`, `{uuid}/resolucion`: EST, FA, DEC. POST `{uuid}/anexos`: EST, FA. POST `{uuid}/resolucion`: FA. Resto: `denyAll` |
| `cancelaciones-matricula` | GET `formulario` y POST radicación: EST. POST `{uuid}/funcionario/**`: FA. POST `{uuid}/decano/**`: DEC. GET `{uuid}`: EST, FA, DEC. Resto: `denyAll` |
| `cancelaciones-asignatura` | Igual que el anterior |
| `examenes-supletorios` | Igual, más POST `{uuid}/estudiante/**`: EST |

Consecuencias que se leen en el código:

- El Secretario General no tiene acceso a ninguna ruta de `solicitudes-academicas`, `cancelaciones-*` ni `examenes-supletorios` (regla de negocio documentada en `backend/CLAUDE.md`).
- Orden de reglas: `solicitudes/**` (línea 15 de la tabla 6.1, `authenticated()`) se declara antes que `solicitudes-academicas/**`. Spring `requestMatchers` compara por ruta exacta de prefijo con `**`; `solicitudes/**` no coincide con `solicitudes-academicas/...` porque el segmento es distinto, así que no hay solapamiento (razonamiento sobre el patrón; NO VERIFICADO por ejecución, aunque `backend/pruebas/matriz-permisos.ps1` está diseñada para comprobarlo).

### 6.3 Seguridad por método (`@PreAuthorize`) [BASE] mecanismo, [EXTENSIÓN] perfiles

- 138 ocurrencias de `@PreAuthorize` en `<J>/infraestructura/input/**` (conteo con `grep -rn PreAuthorize`), con expresiones definidas en `ApplicationConstantes` (`SECRETARIO_DECANO_ACCESO`, `ESTUDIANTE_ACCESO`, `FUNCIONARIO_ACADEMICO_ACCESO`, `DECANO_ACCESO`, etc.).
- **Hallazgo H1:** no se encontró `@EnableMethodSecurity` ni `@EnableGlobalMethodSecurity` en todo `src/main` ni en `pom.xml` (`grep -rniE "MethodSecurity|PrePost"`). En Spring Security 6 las anotaciones `@PreAuthorize` no se evalúan sin esa habilitación. Por tanto, lo que efectivamente protege los endpoints es la lista de `requestMatchers` de la sección 6; las anotaciones por método serían decorativas. El script `backend/pruebas/t1_preauthorize.ps1` existe para probar este comportamiento, pero no se ejecutó (NO VERIFICADO en ejecución). Esto no abre un hueco mientras las reglas por URL estén completas, pero hay doble fuente de verdad que puede divergir.

## 7. Hallazgos de seguridad

| Id | Hallazgo | Evidencia | Origen |
|---|---|---|---|
| H1 | `@PreAuthorize` sin `@EnableMethodSecurity` (sección 6.3) | búsqueda en `src/main` sin resultados | [BASE] el mecanismo, [EXTENSIÓN] lo replica en los controladores nuevos |
| H2 | Clave de firma del JWT escrita en el código fuente y versionada | `JwtServicio.SECRET_KEY` (valor `<REDACTADO>`); no se lee de variable de entorno | [BASE] |
| H3 | Token de 24 h sin refresco ni revocación; el cierre de sesión es solo del cliente | `JwtServicio.getToken`; `AuthService.logout` borra `localStorage` | [BASE] |
| H4 | Token guardado en `localStorage` (`authToken`, `currentUser`) del navegador, expuesto a XSS | `frontend/src/app/core/services/auth-service.ts` líneas 97-98; lo lee `frontend/src/app/core/auth/interceptor/auth-interceptor.ts` | [BASE] |
| H5 | Un token con firma inválida o mal formado (`SignatureException`, `MalformedJwtException`) no se captura en el filtro (solo `ExpiredJwtException`); la excepción sale del filtro, fuera del alcance de `@ControllerAdvice`. Respuesta exacta NO VERIFICADO (probablemente 500 o la página de error de Spring) | `JwtFiltroAutenticacion.doFilterInternal` | [BASE] |
| H6 | Todo error de negocio responde HTTP 500 (ver sección 8); un cliente no puede distinguir 400/404/409 por el código HTTP, solo por `codigoError` | `RestApiExcepcion` | [BASE] el patrón; [EXTENSIÓN] lo hereda al reutilizar los mismos lanzadores |
| H7 | `ExcepcionesFormateador`/handler genérico devuelve `ex.getMessage()` crudo en 500 para cualquier `Exception` no mapeada (puede filtrar texto interno) | `RestApiExcepcion.manejarExcepcionGenerica` | [BASE] |
| H8 | `spring.jpa.show-sql=true` en el único `application.properties`; imprime SQL en logs en cualquier entorno | `backend/solicitudes/src/main/resources/application.properties` | [BASE] |
| H9 | Credenciales de base de datos por defecto (`root` / contraseña `<REDACTADO>`) en `docker-compose.yml`, y MySQL publicado en el puerto 3307 del host | `backend/docker-compose.yml` | [EXTENSIÓN] (el compose lleva el encabezado de la extensión) |
| H10 | CORS con un único origen fijo `http://localhost:4200`; no parametrizable por entorno para producción | `application.properties` `url.frontend` | [BASE] |
| H11 | Sin límite de intentos de inicio de sesión ni bloqueo (no se encontró) | `SesionCUImplAdaptador.login` | [BASE] |
| H12 | Subida de archivos: tamaño máximo 20 MB en multipart (`application.properties`) y regla propia de 5 MB (`MensajesError.ARCHIVO_MUY_GRANDE`); validación de formato y de contenido por firma (`CONTENIDO_ANEXO_NO_COINCIDE`) en `dominio/servicios/ValidadorArchivoAdjunto`; nombre de archivo generado por el sistema (`AlmacenadorArchivosTest` prueba nombres maliciosos) | `ValidadorArchivoAdjuntoTest`, `AlmacenadorArchivosTest` | [EXTENSIÓN] (control positivo) |
| H13 | Un cliente del flujo académico valida pertenencia: `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `ACTOR_SIN_ROL` en `ValidadorActorSolicitud` / `TramiteSolicitud` | `MensajesError`, pruebas `ValidadorActorSolicitudTest`, `TramiteSolicitudTest` | [EXTENSIÓN] (control positivo) |

## 8. Manejo global de excepciones

### 8.1 Estructura

- `@ControllerAdvice` único: `<J>/infraestructura/output/manejadorExcepciones/RestApiExcepcion.java` [BASE].
- Cuerpo de error: `estructura/Error.java` con cinco campos: `codigoError` (String), `mensaje`, `httpCodigo` (Integer), `url`, `metodo`. Se arma con `UtilidadesError.crearError(...)`.
- Los casos de uso no lanzan excepciones directamente: usan el puerto `ExcepcionesFormateadorIntPuerto` (implementado en `infraestructura/output/formateador/ExcepcionesFormateadorImplAdaptador.java`), con ocho lanzadores: `lanzarErrorGenerico`, `lanzarEntidadNoExiste`, `lanzarEntidadExiste`, `lanzarReglaNegocioViolada`, `lanzarCredencialesErroneas`, `lanzarMalFormato`, `lanzarSinInformacion`, `lanzarSinAcceso`. Cada uno lanza una clase de `excepcionesPropias/` (9 clases: las 8 de arriba más `ErrorTokenExpiradoExcepcion`).
- La extensión no agregó códigos ni clases de excepción; solo agregó mensajes (sección 9.2) y reutilizó sobre todo `lanzarReglaNegocioViolada`, `lanzarMalFormato`, `lanzarEntidadNoExiste`, `lanzarEntidadExiste`.

### 8.2 Tabla de códigos (`CodigoError`) [BASE]

Fuente: `estructura/CodigoError.java`. El `codigoError` es una cadena, no el HTTP.

| `codigoError` | Constante | Mensaje fijo | Excepción que lo produce | HTTP real |
|---|---|---|---|---|
| "1" | `ERROR_GENERICO` | Error generico | `ErrorGenericoExcepcion` y cualquier `Exception` sin handler (`manejarExcepcionGenerica`) | 500 |
| "2" | `ERROR_ENTIDAD_EXISTE` | Error, entidad ya existe | `ErrorEntidadExisteExcepcion` | 500 |
| "3" | `ERROR_ENTIDAD_NO_EXISTE` | Error, entidad no existe | `ErrorEntidadNoExisteExcepcion` | 500 |
| "4" | `ERROR_REGLA_DE_NEGOCIO_VIOLADA` | Error, regla de negocio violada | `ErrorReglaNegocioVioladaExcepcion` | 500 |
| "5" | `ERROR_CREDENCIALES` | Error, credenciales incorrectas | `BadCredentialsException` (y `ErrorCredencialesIncorrectasExcepcion`, que extiende `BadCredentialsException`) | 500 |
| "6" | `ERROR_MAL_FORMATO` | Error, mal formato | `ErrorMalFormatoExcepcion` | 500 |
| "7" | `ERROR_NO_INFORMACION` | Error, no se obtuvo información | `ErrorNoInformacionExcepcion` | 500 |
| "8" | `ERROR_SIN_ACCESO` | Error, sin acceso | `ErrorSinAccesoExcepcion` (500); `AccessDeniedException` y `AuthenticationException` en el advice (403) | 500 o 403 |
| "9" | `ERROR_TOKEN_EXPIRADO` | Error, token expirado | `ErrorTokenExpiradoExcepcion` (500, handler `manejarTokenExpirado`); el filtro JWT responde 401 sin pasar por el advice | 500 o 401 |

Total: 9 códigos. Para los códigos 2, 3, 4, 6, 7, 8 (cuando es `ErrorSinAccesoExcepcion`) y 9, el campo `mensaje` del cuerpo es el texto específico (`ex.getMessage()`, típicamente una constante de `MensajesError`). Para el 5 y el 8 de Spring Security el mensaje es el fijo de `CodigoError`.

### 8.3 Respuestas que no usan `RestApiExcepcion`

| Situación | Dónde | Código HTTP | Cuerpo |
|---|---|---|---|
| Sin autenticar en ruta protegida | `CustomAuthenticationEntryPoint` | 401 | `Error` con `codigoError` "401" y mensaje de `ERROR_SIN_ACCESO` ("Error, sin acceso") |
| Autenticado sin permiso (reglas por URL) | `CustomAccessDeniedHandler` | 403 | `Error` con `codigoError` "403" y mensaje "No tiene permisos para acceder a este recurso" |
| Token expirado | `JwtFiltroAutenticacion.sendErrorResponse` | 401 | `Error` con `codigoError` "401" y mensaje de `ERROR_TOKEN_EXPIRADO` |
| Validación `@Valid` (`MethodArgumentNotValidException`) | `RestApiExcepcion.handleValidationExceptions` | 400 | mapa `{campo: mensaje}`, no es un `Error` |
| `ConstraintViolationException` de Hibernate | `RestApiExcepcion.handleConstraintViolationException` | 400 | texto plano con el mensaje |
| Errores de base de datos en controladores académicos | método `errorBaseDeDatos(DataAccessException)` de los controladores (p. ej. `SolicitudAcademicaRestController.adjuntarAnexo`) | NO VERIFICADO el código HTTP exacto (no se abrió el método) | NO VERIFICADO |

Nota de coherencia: el mismo rechazo de seguridad puede salir con dos formas, `codigoError` "401"/"403" (filtros de Spring Security) o "8" con 403 (si la excepción se lanza dentro de un controlador y la atrapa `RestApiExcepcion`). El frontend solo reacciona al HTTP 401 (`SesionVencidaService.atender`: cierra sesión, avisa "Tu sesión venció" y redirige a `/login`, excepto en `POST /sesiones`).

### 8.4 Evidencia en el repositorio de la doble forma 500

`RestApiExcepcion` devuelve `HttpStatus.INTERNAL_SERVER_ERROR` en todos los handlers de excepciones propias (`manejarEntidadNoExiste`, `manejarEntidadExiste`, `manejarReglaNegocioViolada`, `manejarCredencialesIncorrectas`, `manejarMalFormato`, `manejarSinInformacion`, `manejarSinAcceso`, `manejarTokenExpirado`, `manejarExcepcionGenerica`). Solo `manejarNoAutenticacion` y `manejarNoAAcceso` devuelven 403, y los de validación 400. La documentación del contrato (`docs/api/contrato-api-procesos-academicos.md`) y los scripts `backend/pruebas/*.ps1` usan `Obtener-CodigoError` (`comun.ps1`) para distinguir los errores por `codigoError`, no por HTTP. Los textos del backend/CLAUDE.md que hablan de "400 o 409" corresponden a la intención, pero según este handler el HTTP real de una regla violada es 500 (a confirmar con una ejecución: NO VERIFICADO).

## 9. Catálogo de mensajes (`MensajesError`)

Fuente: `<J>/infraestructura/output/manejadorExcepciones/MensajesError.java`. **115 constantes**: 22 [BASE] (hasta `FORMATO_RESPUESTA_NO_VALIDO`) y 93 [EXTENSIÓN] (conteo con `git show d3a48cc:... | grep -c "public static final"` = 22, y 115 hoy). Los mensajes llevan marcadores `%s`/`%d` y terminan en `...`. Siempre viajan con alguno de los 9 códigos de la sección 8.2: la categoría la decide el lanzador que usa el caso de uso, no la constante (NO VERIFICADO el lanzador de cada una de las 115; se agrupan por significado).

### 9.1 [BASE] (22)

| Grupo | Constantes | Significado |
|---|---|---|
| Paginación y vacíos | `PAGINACION_ERROR`, `SIN_REGISTROS`, `ARCHIVO_EXCEL_VACIO` | página o tamaño negativos; lista vacía; parte del multipart faltante |
| Existencia | `ENTIDAD_NO_ENCONTRADA`, `ATRIBUTO_UNICO_YA_EXISTE`, `ENTIDAD_NO_ENCONTRADA_FILTRO`, `INSTANCIA_NO_VALIDA` | no existe por id o por filtro; valor único repetido; subclase no permitida |
| Usuarios y roles | `TIPO_DE_USUARIO_NO_VALIDO`, `ROLES_DUPLICADOS_USUARIO`, `ROLES_NO_VALIDOS`, `PERFIL_SOLICITANTE_NO_VALIDO`, `TIPO_DOCUMENTO_ERRONEO`, `MAL_ASIGNACION` | validaciones al crear usuarios y asignar tipos de solicitud |
| Sesión | `CONTRASEÑA_INCORRECTA`, `CREDENCIALES_ERRONEAS`, `USERNAME_TOKEN`, `NO_ACCESO`, `ROL_NO_HABILITADO` | login y extracción del username del token |
| Solicitudes y respuestas | `MAL_FORMATO_ANEXO`, `SECCION_NO_EXISTENTE`, `TIPO_RESPUESTA_NO_VALIDO`, `FORMATO_RESPUESTA_NO_VALIDO`, `VALOR_REPETIDO_PETICION` | anexos, secciones del orden del día y respuestas |

### 9.2 [EXTENSIÓN] (93)

| Grupo | Constantes |
|---|---|
| Matrícula de asignaturas y estados | `ASIGNATURA_YA_MATRICULADA`, `ESTADO_ASIGNATURA_NO_VALIDO`, `CAMBIO_ESTADO_NO_PERMITIDO` |
| Carga masiva Excel (14) | `EXCEL_NO_VALIDO`, `EXCEL_SIN_FILAS`, `EXCEL_SIN_DATOS`, `EXCEL_ENCABEZADO_INVALIDO`, `EXCEL_FILA_ERROR`, `EXCEL_FILA_DATO_DISTINTO`, `EXCEL_FILA_MATERIA_REPETIDA`, `EXCEL_FILA_VALOR_REPETIDO`, `EXCEL_FILA_VALOR_DUPLICADO`, `EXCEL_FILA_PROGRAMA_DESCONOCIDO`, `EXCEL_FILA_ASIGNATURA_REPETIDA`, `EXCEL_FILA_ASIGNATURA_EXISTE`, `EXCEL_FILA_ASIGNATURA_NO_EXISTE`, `EXCEL_FILA_ASIGNATURA_OTRO_PROGRAMA` (indican fila y columna) |
| Máquina de etapas | `ROL_ETIQUETA_NO_VALIDO`, `DATOS_TRANSICION_INCOMPLETOS`, `ETAPA_NO_VALIDA`, `ETAPA_FINAL_SIN_ACCIONES`, `TRANSICION_NO_PERMITIDA`, `ROL_TRANSICION_NO_PERMITIDO`, `REQUISITO_TRANSICION_FALTANTE`, `TIPO_SOLICITUD_SIN_PROCESO` |
| Actor y pertenencia | `ACTOR_SIN_ROL`, `SOLICITUD_AJENA`, `TIPO_NO_ASIGNADO_FUNCIONARIO`, `SOLICITUD_EN_CURSO`, `RADICADO_NO_GENERADO` |
| Archivos y anexos | `OBSERVACION_MUY_LARGA`, `ARCHIVO_VACIO`, `ARCHIVO_MUY_GRANDE` (5 MB), `FORMATO_ANEXO_NO_PERMITIDO`, `CONTENIDO_ANEXO_NO_COINCIDE`, `ANEXO_DE_OTRO_TIPO`, `SOPORTE_LIBRE_NO_PERMITIDO`, `ANEXO_ACTOR_NO_PERMITIDO`, `ANEXO_ETAPA_NO_PERMITIDA`, `ERROR_GUARDANDO_ARCHIVO`, `ANEXO_NO_PERMITIDO_AL_RADICAR`, `ANEXO_NO_CORRESPONDE_CAUSA`, `ANEXO_REPETIDO`, `ANEXOS_OBLIGATORIOS_FALTANTES`, `SOLO_SOPORTES_LIBRES` |
| Resolución escaneada | `RESOLUCION_SOLO_CANCELACIONES`, `RESOLUCION_SOLO_FUNCIONARIO`, `RESOLUCION_ETAPA_FINAL`, `RESOLUCION_ETAPA_NO_PERMITIDA`, `RESOLUCION_NO_DISPONIBLE` |
| Cancelación de matrícula y de asignatura | `MOTIVO_CANCELACION_REQUERIDO`, `MOTIVO_CANCELACION_MUY_LARGO`, `SIN_ASIGNATURAS_ACTIVAS`, `ASIGNATURAS_SIN_EVALUAR`, `ASIGNATURA_REPETIDA`, `ASIGNATURA_AJENA`, `DATO_ASIGNATURA_FALTANTE`, `NUMERO_FALTAS_NO_VALIDO`, `NOTA_NO_VALIDA`, `SIN_ASIGNATURAS_ELEGIDAS`, `ASIGNATURA_NO_MATRICULADA`, `ASIGNATURA_NO_ACTIVA`, `NOTA_INSUFICIENTE_CUMPLE` (nota menor a 3.0), `NINGUNA_ASIGNATURA_CUMPLE`, `NINGUNA_ASIGNATURA_APROBADA`, `FORMA_APROBACION_DECANO`, `AJUSTE_ASIGNATURA_FALTANTE`, `SITUACION_CANCELAR_RECHAZADA`, `AJUSTES_SOLICITUD_RECHAZADA`, `OBSERVACION_RESPUESTA_REQUERIDA`, `VALOR_ASIGNATURA_MUY_LARGO`, `OBSERVACION_ASIGNATURA_MUY_LARGA` |
| Examen supletorio | `CAUSA_SUPLETORIO_REQUERIDA`, `CAUSA_SUPLETORIO_INVALIDA`, `ASIGNATURA_SUPLETORIO_REQUERIDA`, `ASIGNATURA_SUPLETORIO_NO_ACTIVA`, `FECHA_EXAMEN_REQUERIDA`, `FECHA_EXAMEN_FUTURA`, `PLAZO_SUPLETORIO_VENCIDO` (3 días hábiles), `ASIGNATURA_CRUZADA_REQUERIDA`, `ASIGNATURA_CRUZADA_IGUAL`, `DATOS_CRUCE_REQUERIDOS`, `HORA_CRUCE_MUY_LARGA`, `HORA_CRUCE_MAL_FORMADA`, `DATOS_CRUCE_NO_PERMITIDOS`, `FECHA_ACORDADA_ANTERIOR`, `FECHA_MAL_FORMADA` |
| Programas y catálogo de asignaturas | `PROGRAMA_ACADEMICO_REQUERIDO`, `NOMBRE_ASIGNATURA_REQUERIDO`, `NOMBRE_ASIGNATURA_MUY_LARGO`, `ASIGNATURA_NOMBRE_EXISTE`, `ASIGNATURA_OTRO_PROGRAMA` |

La agrupación es semántica, hecha por lectura; solo los totales (22 y 115) se contaron por script.

## 10. Registro de acciones (logs) e historial

### 10.1 Log de auditoría general [BASE mecanismo, EXTENSIÓN usos]

| Aspecto | Detalle |
|---|---|
| Tabla / entidad | `logs`, `LogEntidad` (`<J>/infraestructura/output/persistencia/entidades/LogEntidad.java`) |
| Campos | `uuidLog` (PK, 150), `accion` (1000), `fecha` (String 1000, formato `yyyy-MM-dd HH:mm:ss`), `resultado` (1000), `uuidUsuario` (FK a `usuarios`, `@ManyToOne`, no nulo), `fechaCreacion` (`LocalDateTime`, no actualizable, se llena en `@PrePersist`) |
| Modelo de dominio | `dominio/modelos/Log.java` |
| Caso de uso | `dominio/casosdeuso/LogCUImplAdaptador.java`: `crearLog(accion, resultado, token)`, `crearLogSesion(accion, resultado, username)`, `getLogs()`, `getLogs(pagina, tamanio)`, `getLogs(responsable, fecha, pagina, tamanio)` |
| Cómo se llena | Cada caso de uso de escritura llama `log.crearLog(...)` con el token; el responsable se resuelve con `IJwtServicio.getUsername(token)` y `LogGatewayIntPuerto.getUsuarioUsername` |
| Cuándo se escribe | Después de la operación exitosa (el log no distingue fallos; no se encontró registro de intentos fallidos) |
| Endpoints | `GET /logs`, `GET /logs/paginado?pagina&tamanio`, `GET /logs/filtro?responsable&fecha&pagina&tamanio` (`LogRestController`); acceso SG y DEC por regla URL y por `@PreAuthorize` |
| Paginación | `PageRequest.of(pagina, tamanio, Sort.by("fechaCreacion").descending().and(Sort.by("uuidLog").ascending()))` en `LogGatewayImplAdaptador`; respuesta `PaginacionRespuestaDTO` con `content` y `totalElements`. Valores negativos lanzan `PAGINACION_ERROR`. La lista completa (`GET /logs`) ordena por `fechaCreacion` descendente |
| Filtro | `LogRepositorio.findByResponsableAndFecha`: coincidencia parcial sin distinguir mayúsculas sobre `nombres + ' ' + apellidos` del responsable y sobre el texto de `fecha`. La consulta incluye `ORDER BY l.fecha DESC` además del `Sort` del `Pageable` (doble ordenamiento; el efecto combinado queda NO VERIFICADO) |
| Lista vacía | `getLogs()` y `getLogs(pagina, tamanio)` lanzan `SIN_REGISTROS` ("No existen registrados logs en el sistema..."); el filtro no lo hace |

Acciones registradas (42 llamadas en `dominio/casosdeuso`; 18 [BASE], 24 [EXTENSIÓN], según `git grep` sobre `d3a48cc`):

- [BASE] (18): "Inicio de sesión"; "Crear Usuario" (2 puntos), "Actualizar Usuario" (2: datos y contraseña); "Solicitud Creada", "Solicitud Publica Creada", "Solicitud modificada"; "Registrar respuesta de Solicitud", "Modificar respuesta de Solicitud", "Responder Solicitud", "Eliminar archivo de respuesta"; "Crear Orden del Día", "Actualizar Orden del Día"; "Crear Tipo de Solicitud" (2 puntos), "Actualizar Tipo Solicitud"; "Actualizar rol".
- [EXTENSIÓN] (24): "Crear asignatura", "Actualizar asignatura"; "Crear estudiante", "Actualizar estudiante", "Agregar asignatura matriculada", "Cambiar estado asignatura matriculada"; "Crear funcionario académico", "Actualizar funcionario académico"; "Asignar funcionario académico a tipo de solicitud"; "Radicar solicitud académica", "Cambiar etapa de solicitud académica"; "Radicar cancelación de matrícula", "Radicar cancelación de asignatura", "Radicar examen supletorio"; "Adjuntar anexo académico", "Adjuntar Resolución académica"; "Evaluar asignaturas de cancelación de matrícula", "Registrar situación al cancelar", "Cancelar asignaturas matriculadas" (también en cancelación de asignatura), "Evaluar asignaturas de cancelación de asignatura", "Registrar decisión del Decano por asignatura", "Ajustar asignaturas de cancelación de asignatura"; "Acordar fecha de examen supletorio".

No se registra en `logs`: lecturas (GET), descargas de anexos y de Resolución, intentos de login fallidos, ni rechazos por regla de negocio.

### 10.2 Historial por solicitud académica [EXTENSIÓN]

| Aspecto | Detalle |
|---|---|
| Modelo | `dominio/modelos/HistorialSolicitudAcademica.java`: `uuidHistorial`, `solicitudAcademica`, `usuario`, `accion`, `observaciones`, `fecha` (`LocalDateTime`), `etapaCodigo` |
| Quién escribe | `SolicitudAcademicaCUImplAdaptador.historial(...)` crea la fila en cada radicación y cada cambio de etapa, con `accion = AccionEtapa.name()` y la observación recortada (máximo `MAXIMO_OBSERVACION`; si lo supera, `OBSERVACION_MUY_LARGA`) |
| Consulta | `GET /solicitudes-academicas/{uuidSolicitud}/historial` (`SolicitudAcademicaRestController.getHistorial`), acceso EST, FA, DEC; no está paginado (devuelve la lista completa) |
| DTO | `HistorialSolicitudAcademicaDTORespuesta`: `accion`, `etapaCodigo`, `observaciones`, `fecha`, `nombresUsuario`, `apellidosUsuario` |
| Filtrado por rol | `ConsultaSolicitudAcademicaCUImplAdaptador.getHistorial`: si el actor es Estudiante, se ocultan las observaciones de ciertas acciones (`OCULTAS_ESTUDIANTE`), se eliminan filas internas (`FILAS_OCULTAS_ESTUDIANTE`) y las etapas de decisión del Decano se muestran como `EN_REVISION_DECANO` (`etapaVisible`) |

Diferencia: el log general registra "quién hizo qué" a nivel de sistema; el historial registra la trayectoria de cada solicitud con etapa y observación y es la fuente que ve el Estudiante.

## 11. Cifras clave

- Códigos de error (`CodigoError`): 9. Clases de excepción propias: 9. Handlers en `RestApiExcepcion`: 13 (11 para excepciones de negocio/seguridad/genérica, 1 para `MethodArgumentNotValidException`, 1 para `ConstraintViolationException`).
- `MensajesError`: 115 constantes (22 [BASE], 93 [EXTENSIÓN]).
- `requestMatchers`: 78 hoy (17 [BASE], 61 [EXTENSIÓN]); ocho prefijos académicos cerrados con `denyAll`.
- `@PreAuthorize`: 138 usos, sin habilitación global de seguridad por método (H1).
- JWT: HS256, 24 h, sin claims propios, clave fija en código.
- Llamadas a `crearLog`/`crearLogSesion`: 42 (18 base, 24 extensión).
- Hallazgos de seguridad listados: 13 (H1 a H11 son debilidades o riesgos, H12 y H13 son controles positivos).
