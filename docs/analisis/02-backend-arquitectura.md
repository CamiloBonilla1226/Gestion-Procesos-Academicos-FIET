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
