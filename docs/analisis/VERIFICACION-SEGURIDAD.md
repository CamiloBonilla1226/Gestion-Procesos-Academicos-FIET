# Verificación de seguridad por rol (T12.20)

Análisis estático de solo lectura. No se compiló, no se ejecutó nada y no se modificó ningún archivo fuera de `docs/analisis/`. Todo lo marcado NO VERIFICADO requeriría ejecutar el backend.

Rutas relativas a `backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes` (B). Base de la API: `/api/unicauca/fiet/consejo` (`application.properties:4`). Las tablas muestran las rutas sin esa base.

## 1. Cómo se autoriza hoy

1. `JwtFiltroAutenticacion` (`B/infraestructura/configuracion/seguridad/jwt/JwtFiltroAutenticacion.java`) toma `Authorization: Bearer`, extrae el usuario del token, carga el `UsuarioEntidad` desde la base con `UserDetailsService` (`ConfiguracionAplicacion.userDetailService`) y arma las autoridades con los nombres de los roles guardados en la base (`UsuarioEntidad.getAuthorities`, líneas 56 a 61). Los roles no viajan en el token: el JWT solo lleva `sub`, `iat` y `exp`.
2. `ConfiguracionSeguridad.securityFilterChain` define 78 reglas `requestMatchers` evaluadas en orden (la primera que coincide gana) y termina en `anyRequest().authenticated()`. Esas reglas sí se evalúan: es la barrera real.
3. No existe `@EnableMethodSecurity` ni `@EnableGlobalMethodSecurity` en ninguna clase (grep en `backend/solicitudes/src/main`: 0 coincidencias), por lo que los 124 `@PreAuthorize` de los controladores no se evalúan.
4. Sesión sin estado, CSRF deshabilitado, CORS con un solo origen (`url.frontend`).
5. Entre las reglas por URL no hay ningún paso por el dueño del recurso; ese control, cuando existe, está en los casos de uso (sección 5).

Metodología: un script de lectura analizó los 15 controladores (`B/infraestructura/input/*/controlador/*RestController.java`), reconstruyó la ruta completa de cada método (prefijo de clase, constantes `UUID_*` y sufijo), evaluó las 78 reglas en el mismo orden que Spring (el comodín `{variable}` equivale a un segmento y `/**` a cero o más segmentos) y comparó el resultado con el `@PreAuthorize` resuelto contra `ApplicationConstantes`. Cifras: 128 endpoints, 102 con la misma lista de roles en URL y `@PreAuthorize`, 22 divergentes, 4 sin `@PreAuthorize`.

## 2. Catálogo de los 128 endpoints

Columnas: regla de rutas que cubre el endpoint (número de orden en `ConfiguracionSeguridad`), rol exigido por esa regla, rol declarado en `@PreAuthorize`, resultado. "ÚNICA BARRERA" significa que la URL solo exige estar autenticado y el rol solo lo pide el `@PreAuthorize`, que hoy no se evalúa. Roles: SG = Secretario General, FA = Funcionario Académico, F = Funcionario (rol de Julián).

### AsignaturaRestController (EXTENSIÓN, 7 endpoints)

| Método | Ruta | Regla de rutas | Rol por URL | @PreAuthorize | Resultado |
|---|---|---|---|---|---|
| POST | `/asignaturas/cargar/archivo` | #24 POST asignaturas/cargar/archivo | Decano, SG | Decano, SG | coincide |
| POST | `/asignaturas` | #26 POST asignaturas | Decano, SG | Decano, SG | coincide |
| GET | `/asignaturas/paginado` | #25 GET asignaturas/** | Decano, FA, SG | Decano, FA, SG | coincide |
| GET | `/asignaturas/filtro` | #25 GET asignaturas/** | Decano, FA, SG | Decano, FA, SG | coincide |
| GET | `/asignaturas/programa/{uuidPrograma}` | #23 GET asignaturas/programa/{uuidPrograma} | Decano, FA, SG | Decano, FA, SG | coincide |
| GET | `/asignaturas/{uuidAsignatura}` | #25 GET asignaturas/** | Decano, FA, SG | Decano, FA, SG | coincide |
| PUT | `/asignaturas/{uuidAsignatura}` | #27 PUT asignaturas/** | Decano, SG | Decano, SG | coincide |

### CancelacionAsignaturaRestController (EXTENSIÓN, 8 endpoints)

| Método | Ruta | Regla de rutas | Rol por URL | @PreAuthorize | Resultado |
|---|---|---|---|---|---|
| GET | `/cancelaciones-asignatura/formulario` | #66 GET cancelaciones-asignatura/formulario | Estudiante | Estudiante | coincide |
| POST | `/cancelaciones-asignatura` | #67 POST cancelaciones-asignatura | Estudiante | Estudiante | coincide |
| GET | `/cancelaciones-asignatura/{uuidSolicitud}` | #70 GET cancelaciones-asignatura/{uuidSolicitud} | Decano, Estudiante, FA | Decano, Estudiante, FA | coincide |
| POST | `/cancelaciones-asignatura/{uuidSolicitud}/funcionario/rechazar` | #68 POST cancelaciones-asignatura/{uuidSolicitud}/funcionario/** | FA | FA | coincide |
| POST | `/cancelaciones-asignatura/{uuidSolicitud}/funcionario/remitir` | #68 POST cancelaciones-asignatura/{uuidSolicitud}/funcionario/** | FA | FA | coincide |
| POST | `/cancelaciones-asignatura/{uuidSolicitud}/funcionario/responder` | #68 POST cancelaciones-asignatura/{uuidSolicitud}/funcionario/** | FA | FA | coincide |
| POST | `/cancelaciones-asignatura/{uuidSolicitud}/decano/aprobar` | #69 POST cancelaciones-asignatura/{uuidSolicitud}/decano/** | Decano | Decano | coincide |
| POST | `/cancelaciones-asignatura/{uuidSolicitud}/decano/rechazar` | #69 POST cancelaciones-asignatura/{uuidSolicitud}/decano/** | Decano | Decano | coincide |

### CancelacionMatriculaRestController (EXTENSIÓN, 8 endpoints)

| Método | Ruta | Regla de rutas | Rol por URL | @PreAuthorize | Resultado |
|---|---|---|---|---|---|
| GET | `/cancelaciones-matricula/formulario` | #60 GET cancelaciones-matricula/formulario | Estudiante | Estudiante | coincide |
| POST | `/cancelaciones-matricula` | #61 POST cancelaciones-matricula | Estudiante | Estudiante | coincide |
| GET | `/cancelaciones-matricula/{uuidSolicitud}` | #64 GET cancelaciones-matricula/{uuidSolicitud} | Decano, Estudiante, FA | Decano, Estudiante, FA | coincide |
| POST | `/cancelaciones-matricula/{uuidSolicitud}/funcionario/rechazar` | #62 POST cancelaciones-matricula/{uuidSolicitud}/funcionario/** | FA | FA | coincide |
| POST | `/cancelaciones-matricula/{uuidSolicitud}/funcionario/remitir` | #62 POST cancelaciones-matricula/{uuidSolicitud}/funcionario/** | FA | FA | coincide |
| POST | `/cancelaciones-matricula/{uuidSolicitud}/funcionario/responder` | #62 POST cancelaciones-matricula/{uuidSolicitud}/funcionario/** | FA | FA | coincide |
| POST | `/cancelaciones-matricula/{uuidSolicitud}/decano/aprobar` | #63 POST cancelaciones-matricula/{uuidSolicitud}/decano/** | Decano | Decano | coincide |
| POST | `/cancelaciones-matricula/{uuidSolicitud}/decano/rechazar` | #63 POST cancelaciones-matricula/{uuidSolicitud}/decano/** | Decano | Decano | coincide |

### CatalogoAcademicoRestController (EXTENSIÓN, 7 endpoints)

| Método | Ruta | Regla de rutas | Rol por URL | @PreAuthorize | Resultado |
|---|---|---|---|---|---|
| GET | `/catalogos-academicos/tipos-solicitud` | #48 GET catalogos-academicos/** | cualquier autenticado | isAuthenticated() | coincide |
| GET | `/catalogos-academicos/tipos-solicitud/{uuidTipo}/etapas` | #48 GET catalogos-academicos/** | cualquier autenticado | isAuthenticated() | coincide |
| GET | `/catalogos-academicos/tipos-solicitud/{uuidTipo}/tipos-anexo` | #48 GET catalogos-academicos/** | cualquier autenticado | isAuthenticated() | coincide |
| GET | `/catalogos-academicos/etiquetas` | #48 GET catalogos-academicos/** | cualquier autenticado | isAuthenticated() | coincide |
| GET | `/catalogos-academicos/situaciones` | #48 GET catalogos-academicos/** | cualquier autenticado | isAuthenticated() | coincide |
| GET | `/catalogos-academicos/programas` | #47 GET catalogos-academicos/programas | cualquier autenticado | isAuthenticated() | coincide |
| PUT | `/catalogos-academicos/tipos-solicitud/{uuidTipo}/funcionario` | #46 PUT catalogos-academicos/tipos-solicitud/{uuidTipo}/funcionario | Decano, SG | Decano, SG | coincide |

### EstudianteRestController (EXTENSIÓN, 9 endpoints)

| Método | Ruta | Regla de rutas | Rol por URL | @PreAuthorize | Resultado |
|---|---|---|---|---|---|
| POST | `/estudiantes/cargar/archivo` | #29 POST estudiantes/cargar/archivo | Decano, SG | Decano, SG | coincide |
| POST | `/estudiantes` | #34 POST estudiantes | Decano, SG | Decano, SG | coincide |
| GET | `/estudiantes/paginado` | #31 GET estudiantes/paginado | Decano, FA, SG | Decano, FA, SG | coincide |
| GET | `/estudiantes/filtro` | #32 GET estudiantes/filtro | Decano, FA, SG | Decano, FA, SG | coincide |
| GET | `/estudiantes/mis-asignaturas` | #30 GET estudiantes/mis-asignaturas | Estudiante | Estudiante | coincide |
| GET | `/estudiantes/{uuidEstudiante}` | #33 GET estudiantes/{uuidEstudiante} | Decano, FA, SG | Decano, FA, SG | coincide |
| PUT | `/estudiantes/{uuidEstudiante}` | #35 PUT estudiantes/{uuidEstudiante} | Decano, SG | Decano, SG | coincide |
| POST | `/estudiantes/{uuidEstudiante}/asignaturas` | #36 POST estudiantes/{uuidEstudiante}/asignaturas | Decano, SG | Decano, SG | coincide |
| PATCH | `/estudiantes/{uuidEstudiante}/asignaturas/{uuidMatricula}/estado` | #37 PATCH estudiantes/{uuidEstudiante}/asignaturas/{uuidMatricula}/estado | Decano, SG | Decano, SG | coincide |

### ExamenSupletorioRestController (EXTENSIÓN, 12 endpoints)

| Método | Ruta | Regla de rutas | Rol por URL | @PreAuthorize | Resultado |
|---|---|---|---|---|---|
| GET | `/examenes-supletorios/formulario` | #72 GET examenes-supletorios/formulario | Estudiante | Estudiante | coincide |
| POST | `/examenes-supletorios` | #73 POST examenes-supletorios | Estudiante | Estudiante | coincide |
| GET | `/examenes-supletorios/{uuidSolicitud}` | #77 GET examenes-supletorios/{uuidSolicitud} | Decano, Estudiante, FA | Decano, Estudiante, FA | coincide |
| POST | `/examenes-supletorios/{uuidSolicitud}/funcionario/rechazar` | #74 POST examenes-supletorios/{uuidSolicitud}/funcionario/** | FA | FA | coincide |
| POST | `/examenes-supletorios/{uuidSolicitud}/funcionario/remitir` | #74 POST examenes-supletorios/{uuidSolicitud}/funcionario/** | FA | FA | coincide |
| POST | `/examenes-supletorios/{uuidSolicitud}/funcionario/responder` | #74 POST examenes-supletorios/{uuidSolicitud}/funcionario/** | FA | FA | coincide |
| POST | `/examenes-supletorios/{uuidSolicitud}/funcionario/recibo` | #74 POST examenes-supletorios/{uuidSolicitud}/funcionario/** | FA | FA | coincide |
| POST | `/examenes-supletorios/{uuidSolicitud}/funcionario/comprobante/aprobar` | #74 POST examenes-supletorios/{uuidSolicitud}/funcionario/** | FA | FA | coincide |
| POST | `/examenes-supletorios/{uuidSolicitud}/funcionario/comprobante/rechazar` | #74 POST examenes-supletorios/{uuidSolicitud}/funcionario/** | FA | FA | coincide |
| POST | `/examenes-supletorios/{uuidSolicitud}/decano/aprobar` | #75 POST examenes-supletorios/{uuidSolicitud}/decano/** | Decano | Decano | coincide |
| POST | `/examenes-supletorios/{uuidSolicitud}/decano/rechazar` | #75 POST examenes-supletorios/{uuidSolicitud}/decano/** | Decano | Decano | coincide |
| POST | `/examenes-supletorios/{uuidSolicitud}/estudiante/comprobante` | #76 POST examenes-supletorios/{uuidSolicitud}/estudiante/** | Estudiante | Estudiante | coincide |

### FuncionarioAcademicoRestController (EXTENSIÓN, 6 endpoints)

| Método | Ruta | Regla de rutas | Rol por URL | @PreAuthorize | Resultado |
|---|---|---|---|---|---|
| POST | `/funcionarios-academicos` | #43 POST funcionarios-academicos | Decano, SG | Decano, SG | coincide |
| POST | `/funcionarios-academicos/cargar/archivo` | #39 POST funcionarios-academicos/cargar/archivo | Decano, SG | Decano, SG | coincide |
| GET | `/funcionarios-academicos/paginado` | #40 GET funcionarios-academicos/paginado | Decano, FA, SG | Decano, FA, SG | coincide |
| GET | `/funcionarios-academicos/filtro` | #41 GET funcionarios-academicos/filtro | Decano, FA, SG | Decano, FA, SG | coincide |
| GET | `/funcionarios-academicos/{uuidFuncionario}` | #42 GET funcionarios-academicos/{uuidFuncionario} | Decano, FA, SG | Decano, FA, SG | coincide |
| PUT | `/funcionarios-academicos/{uuidFuncionario}` | #44 PUT funcionarios-academicos/{uuidFuncionario} | Decano, SG | Decano, SG | coincide |

### LogRestController (BASE, 3 endpoints)

| Método | Ruta | Regla de rutas | Rol por URL | @PreAuthorize | Resultado |
|---|---|---|---|---|---|
| GET | `/logs` | #10 logs/** | Decano, SG | Decano, SG | coincide |
| GET | `/logs/paginado` | #10 logs/** | Decano, SG | Decano, SG | coincide |
| GET | `/logs/filtro` | #10 logs/** | Decano, SG | Decano, SG | coincide |

### RespuestaRestController (BASE, 10 endpoints)

| Método | Ruta | Regla de rutas | Rol por URL | @PreAuthorize | Resultado |
|---|---|---|---|---|---|
| GET | `/respuestas/paginado` | #21 GET respuestas/** | cualquier autenticado | isAuthenticated() | coincide |
| GET | `/respuestas/paginado/nombre-solicitud` | #21 GET respuestas/** | cualquier autenticado | isAuthenticated() | coincide |
| GET | `/respuestas/paginado/funcionario` | #21 GET respuestas/** | cualquier autenticado | Decano, F, SG | ÚNICA BARRERA: @PreAuthorize |
| GET | `/respuestas/paginado/funcionario-nombre-solicitud` | #21 GET respuestas/** | cualquier autenticado | Decano, F, SG | ÚNICA BARRERA: @PreAuthorize |
| GET | `/respuestas/{uuidRespuesta}` | #21 GET respuestas/** | cualquier autenticado | isAuthenticated() | coincide |
| GET | `/respuestas/solicitud/{uuidSolicitud}` | #21 GET respuestas/** | cualquier autenticado | isAuthenticated() | coincide |
| POST | `/respuestas/{uuidSolicitud}` | #22 POST respuestas/** | Decano, F, SG | Decano, SG | DIVERGE |
| POST | `/respuestas/{uuidRespuesta}/archivo` | #22 POST respuestas/** | Decano, F, SG | Decano, F, SG | coincide |
| PATCH | `/respuestas/{uuidRespuesta}/archivo` | anyRequest | cualquier autenticado | Decano, F, SG | ÚNICA BARRERA: @PreAuthorize |
| GET | `/respuestas/{uuidRespuesta}/{fileName}` | #21 GET respuestas/** | cualquier autenticado | isAuthenticated() | coincide |

### RolRestController (BASE, 4 endpoints)

| Método | Ruta | Regla de rutas | Rol por URL | @PreAuthorize | Resultado |
|---|---|---|---|---|---|
| GET | `/roles` | #11 roles/** | Decano, SG | Decano, SG | coincide |
| GET | `/roles/paginado` | #11 roles/** | Decano, SG | Decano, SG | coincide |
| GET | `/roles/{uuidRol}` | #11 roles/** | Decano, SG | Decano, SG | coincide |
| PUT | `/roles/{uuidRol}` | #11 roles/** | Decano, SG | Decano, SG | coincide |

### SesionRestController (BASE, 1 endpoints)

| Método | Ruta | Regla de rutas | Rol por URL | @PreAuthorize | Resultado |
|---|---|---|---|---|---|
| POST | `/sesiones` | #1 sesiones | pública | ninguno | solo URL |

### SolicitudAcademicaRestController (EXTENSIÓN, 9 endpoints)

| Método | Ruta | Regla de rutas | Rol por URL | @PreAuthorize | Resultado |
|---|---|---|---|---|---|
| GET | `/solicitudes-academicas/estudiante` | #50 GET solicitudes-academicas/estudiante | Estudiante | Estudiante | coincide |
| GET | `/solicitudes-academicas/funcionario` | #51 GET solicitudes-academicas/funcionario | FA | FA | coincide |
| GET | `/solicitudes-academicas/decano` | #52 GET solicitudes-academicas/decano | Decano | Decano | coincide |
| GET | `/solicitudes-academicas/{uuidSolicitud}` | #53 GET solicitudes-academicas/{uuidSolicitud} | Decano, Estudiante, FA | Decano, Estudiante, FA | coincide |
| GET | `/solicitudes-academicas/{uuidSolicitud}/historial` | #54 GET solicitudes-academicas/{uuidSolicitud}/historial | Decano, Estudiante, FA | Decano, Estudiante, FA | coincide |
| POST | `/solicitudes-academicas/{uuidSolicitud}/anexos` | #57 POST solicitudes-academicas/{uuidSolicitud}/anexos | Estudiante, FA | Estudiante, FA | coincide |
| GET | `/solicitudes-academicas/{uuidSolicitud}/anexos/{uuidAnexo}` | #55 GET solicitudes-academicas/{uuidSolicitud}/anexos/{uuidAnexo} | Decano, Estudiante, FA | Decano, Estudiante, FA | coincide |
| POST | `/solicitudes-academicas/{uuidSolicitud}/resolucion` | #58 POST solicitudes-academicas/{uuidSolicitud}/resolucion | FA | FA | coincide |
| GET | `/solicitudes-academicas/{uuidSolicitud}/resolucion` | #56 GET solicitudes-academicas/{uuidSolicitud}/resolucion | Decano, Estudiante, FA | Decano, Estudiante, FA | coincide |

### SolicitudesRestController (BASE, 24 endpoints)

| Método | Ruta | Regla de rutas | Rol por URL | @PreAuthorize | Resultado |
|---|---|---|---|---|---|
| GET | `/solicitudes/orden-del-dia/paginado` | #9 solicitudes/orden-del-dia/** | Decano, SG | Decano, SG | coincide |
| GET | `/solicitudes/orden-del-dia` | #9 solicitudes/orden-del-dia/** | Decano, SG | Decano, SG | coincide |
| GET | `/solicitudes/orden-del-dia/{uuidOrdenDelDia}` | #9 solicitudes/orden-del-dia/** | Decano, SG | Decano, SG | coincide |
| POST | `/solicitudes/orden-del-dia` | #9 solicitudes/orden-del-dia/** | Decano, SG | Decano, SG | coincide |
| PUT | `/solicitudes/orden-del-dia/{uuidOrdenDelDia}` | #9 solicitudes/orden-del-dia/** | Decano, SG | Decano, SG | coincide |
| GET | `/solicitudes/paginado` | #20 solicitudes/** | cualquier autenticado | Decano, F, SG | ÚNICA BARRERA: @PreAuthorize |
| GET | `/solicitudes` | #20 solicitudes/** | cualquier autenticado | Decano, F, SG | ÚNICA BARRERA: @PreAuthorize |
| GET | `/solicitudes/{uuidSolicitud}` | #20 solicitudes/** | cualquier autenticado | Decano, F, SG | ÚNICA BARRERA: @PreAuthorize |
| POST | `/solicitudes` | #20 solicitudes/** | cualquier autenticado | isAuthenticated() | coincide |
| POST | `/solicitudes/public` | #2 POST solicitudes/public | pública | Decano, F, SG | PÚBLICO por URL |
| PUT | `/solicitudes/{uuidSolicitud}` | #20 solicitudes/** | cualquier autenticado | Decano, SG | ÚNICA BARRERA: @PreAuthorize |
| GET | `/solicitudes/funcionario/{uuidFuncionario}` | #20 solicitudes/** | cualquier autenticado | Decano, SG | ÚNICA BARRERA: @PreAuthorize |
| GET | `/solicitudes/orden-del-dia/{uuidOrdenDelDia}/solicitudes` | #9 solicitudes/orden-del-dia/** | Decano, SG | Decano, SG | coincide |
| GET | `/solicitudes/estado` | #20 solicitudes/** | cualquier autenticado | isAuthenticated() | coincide |
| GET | `/solicitudes/buscar` | #20 solicitudes/** | cualquier autenticado | Decano, F, SG | ÚNICA BARRERA: @PreAuthorize |
| GET | `/solicitudes/buscar/solicitante` | #20 solicitudes/** | cualquier autenticado | Decano, SG | ÚNICA BARRERA: @PreAuthorize |
| GET | `/solicitudes/fun/buscar/` | #20 solicitudes/** | cualquier autenticado | Decano, F, SG | ÚNICA BARRERA: @PreAuthorize |
| GET | `/solicitudes/orden-del-dia/buscar` | #9 solicitudes/orden-del-dia/** | Decano, SG | Decano, SG | coincide |
| GET | `/solicitudes/anexos/{uuidSolicitud}/{fileName}` | #20 solicitudes/** | cualquier autenticado | isAuthenticated() | coincide |
| GET | `/solicitudes/anexos/download` | #20 solicitudes/** | cualquier autenticado | Decano, SG | ÚNICA BARRERA: @PreAuthorize |
| GET | `/solicitudes/exportar` | #20 solicitudes/** | cualquier autenticado | Decano, SG | ÚNICA BARRERA: @PreAuthorize |
| GET | `/solicitudes/exportar/respuestas` | #20 solicitudes/** | cualquier autenticado | Decano, SG | ÚNICA BARRERA: @PreAuthorize |
| GET | `/solicitudes/exportar/reunion` | #20 solicitudes/** | cualquier autenticado | Decano, SG | ÚNICA BARRERA: @PreAuthorize |
| GET | `/solicitudes/orden-del-dia/estado` | #8 GET solicitudes/orden-del-dia/estado | cualquier autenticado | isAuthenticated() | coincide |

### TiposSolicitudRestController (BASE, 10 endpoints)

| Método | Ruta | Regla de rutas | Rol por URL | @PreAuthorize | Resultado |
|---|---|---|---|---|---|
| GET | `/tipos/solicitudes/paginado` | #6 GET tipos/solicitudes/{uuidTipoSolicitud} | pública | Decano, SG | PÚBLICO por URL |
| GET | `/tipos/solicitudes/filtro` | #6 GET tipos/solicitudes/{uuidTipoSolicitud} | pública | Decano, SG | PÚBLICO por URL |
| GET | `/tipos/solicitudes` | #7 tipos/solicitudes/** | Decano, SG | Decano, SG | coincide |
| GET | `/tipos/solicitudes/{uuidTipoSolicitud}` | #6 GET tipos/solicitudes/{uuidTipoSolicitud} | pública | isAuthenticated() | PÚBLICO por URL |
| POST | `/tipos/solicitudes` | #7 tipos/solicitudes/** | Decano, SG | Decano, SG | coincide |
| POST | `/tipos/solicitudes/cargar/archivo` | #7 tipos/solicitudes/** | Decano, SG | Decano, SG | coincide |
| PUT | `/tipos/solicitudes/{uuidTipoSolicitud}` | #7 tipos/solicitudes/** | Decano, SG | Decano, SG | coincide |
| GET | `/tipos/solicitudes/perfil` | #3 GET tipos/solicitudes/perfil | pública | ninguno | solo URL |
| GET | `/tipos/solicitudes/perfil/paginado` | #4 GET tipos/solicitudes/perfil/paginado | pública | isAuthenticated() | PÚBLICO por URL |
| GET | `/tipos/solicitudes/perfil/filtro` | #5 GET tipos/solicitudes/perfil/filtro | pública | isAuthenticated() | PÚBLICO por URL |

### UsuarioRestController (BASE, 10 endpoints)

| Método | Ruta | Regla de rutas | Rol por URL | @PreAuthorize | Resultado |
|---|---|---|---|---|---|
| GET | `/usuarios/tipos` | #16 GET usuarios/tipos | Decano, SG | Decano, SG | coincide |
| GET | `/usuarios/paginado` | #13 GET usuarios/paginado | Decano, SG | Decano, SG | coincide |
| GET | `/usuarios/filtro` | #14 GET usuarios/filtro | Decano, SG | Decano, SG | coincide |
| GET | `/usuarios` | #12 GET usuarios | Decano, SG | Decano, SG | coincide |
| GET | `/usuarios/funcionarios` | #15 GET usuarios/funcionarios | Decano, SG | Decano, SG | coincide |
| GET | `/usuarios/{uuidUsuario}` | #17 GET usuarios/** | cualquier autenticado | ninguno | solo URL |
| POST | `/usuarios` | #19 usuarios/** | Decano, SG | Decano, SG | coincide |
| POST | `/usuarios/cargar/archivo` | #19 usuarios/** | Decano, SG | Decano, SG | coincide |
| PUT | `/usuarios/{uuidUsuario}` | #19 usuarios/** | Decano, SG | Decano, SG | coincide |
| PATCH | `/usuarios/{uuidUsuario}` | #18 PATCH usuarios/** | cualquier autenticado | ninguno | solo URL |

## 3. Endpoints donde @PreAuthorize es la única barrera

Son 21 endpoints, todos de Julián (BASE). Hoy la URL deja pasar a cualquier usuario autenticado (o a cualquiera, en los públicos) y el rol que pide el `@PreAuthorize` no se aplica. En las tres filas de `tipos/solicitudes` cuyo `@PreAuthorize` es `isAuthenticated()` la anotación pediría token, pero la URL es pública.

| Método | Ruta | Regla que lo cubre | @PreAuthorize | Quién entra hoy |
|---|---|---|---|---|
| GET | `/respuestas/paginado/funcionario` | #21 GET respuestas/** | Decano, F, SG | cualquier token válido (Estudiante, FA, Decano, SG, F) |
| GET | `/respuestas/paginado/funcionario-nombre-solicitud` | #21 GET respuestas/** | Decano, F, SG | cualquier token válido (Estudiante, FA, Decano, SG, F) |
| PATCH | `/respuestas/{uuidRespuesta}/archivo` | anyRequest | Decano, F, SG | cualquier token válido (Estudiante, FA, Decano, SG, F) |
| GET | `/solicitudes/paginado` | #20 solicitudes/** | Decano, F, SG | cualquier token válido (Estudiante, FA, Decano, SG, F) |
| GET | `/solicitudes` | #20 solicitudes/** | Decano, F, SG | cualquier token válido (Estudiante, FA, Decano, SG, F) |
| GET | `/solicitudes/{uuidSolicitud}` | #20 solicitudes/** | Decano, F, SG | cualquier token válido (Estudiante, FA, Decano, SG, F) |
| POST | `/solicitudes/public` | #2 POST solicitudes/public | Decano, F, SG | cualquiera, sin token |
| PUT | `/solicitudes/{uuidSolicitud}` | #20 solicitudes/** | Decano, SG | cualquier token válido (Estudiante, FA, Decano, SG, F) |
| GET | `/solicitudes/funcionario/{uuidFuncionario}` | #20 solicitudes/** | Decano, SG | cualquier token válido (Estudiante, FA, Decano, SG, F) |
| GET | `/solicitudes/buscar` | #20 solicitudes/** | Decano, F, SG | cualquier token válido (Estudiante, FA, Decano, SG, F) |
| GET | `/solicitudes/buscar/solicitante` | #20 solicitudes/** | Decano, SG | cualquier token válido (Estudiante, FA, Decano, SG, F) |
| GET | `/solicitudes/fun/buscar/` | #20 solicitudes/** | Decano, F, SG | cualquier token válido (Estudiante, FA, Decano, SG, F) |
| GET | `/solicitudes/anexos/download` | #20 solicitudes/** | Decano, SG | cualquier token válido (Estudiante, FA, Decano, SG, F) |
| GET | `/solicitudes/exportar` | #20 solicitudes/** | Decano, SG | cualquier token válido (Estudiante, FA, Decano, SG, F) |
| GET | `/solicitudes/exportar/respuestas` | #20 solicitudes/** | Decano, SG | cualquier token válido (Estudiante, FA, Decano, SG, F) |
| GET | `/solicitudes/exportar/reunion` | #20 solicitudes/** | Decano, SG | cualquier token válido (Estudiante, FA, Decano, SG, F) |
| GET | `/tipos/solicitudes/paginado` | #6 GET tipos/solicitudes/{uuidTipoSolicitud} | Decano, SG | cualquiera, sin token |
| GET | `/tipos/solicitudes/filtro` | #6 GET tipos/solicitudes/{uuidTipoSolicitud} | Decano, SG | cualquiera, sin token |
| GET | `/tipos/solicitudes/{uuidTipoSolicitud}` | #6 GET tipos/solicitudes/{uuidTipoSolicitud} | isAuthenticated() | cualquiera, sin token |
| GET | `/tipos/solicitudes/perfil/paginado` | #4 GET tipos/solicitudes/perfil/paginado | isAuthenticated() | cualquiera, sin token |
| GET | `/tipos/solicitudes/perfil/filtro` | #5 GET tipos/solicitudes/perfil/filtro | isAuthenticated() | cualquiera, sin token |

Observaciones:

- `GET tipos/solicitudes/paginado` y `GET tipos/solicitudes/filtro` quedan públicos porque la regla pública #6 `GET tipos/solicitudes/{uuidTipoSolicitud}` toma `paginado` y `filtro` como valor de la variable; llegan antes que la regla #7 que los restringiría a SG y Decano.
- `PATCH respuestas/{uuidRespuesta}/archivo` no coincide con ninguna regla (la de `POST respuestas/**` es solo POST): cae en `anyRequest().authenticated()`.
- `GET solicitudes/fun/buscar/`: Spring une el prefijo `solicitudes` y el sufijo `fun/buscar/` con una barra (el script de la primera versión los concatenó sin ella y lo puso en `anyRequest`); la ruta real cae en `solicitudes/**`. El frontend la llama así (`SolicitudesService.buscarSolicitudesPorNombreYFuncionario`).
- `POST solicitudes/public` y `GET tipos/solicitudes/{uuidTipoSolicitud}`, `perfil/paginado` y `perfil/filtro` están declarados públicos a propósito en la URL; su `@PreAuthorize` es contradictorio con esa intención. Activar `@EnableMethodSecurity` sin corregirlos rompería el formulario público de envío.
- `POST respuestas/{uuidSolicitud}`: la URL admite SG, Decano y F; el `@PreAuthorize` solo SG y Decano. Hoy puede responder un usuario con rol Funcionario aunque la anotación no lo permita.

## 4. Endpoints de Estudiante, Funcionario Académico y Decano que un token de otro rol podría llamar

### 4.1 Endpoints de la extensión (66)

En estos la URL y el `@PreAuthorize` coinciden en los 66 casos, así que la protección por rol sí está activa. Los endpoints de acción con segmento de rol (`/estudiante/`, `/funcionario/`, `/decano/`) son exclusivos de un solo rol. Pasan roles de más estos:

| Método | Ruta | Roles que pasan la URL | Observación |
|---|---|---|---|
| POST | `/asignaturas/cargar/archivo` | Decano, SG | catálogo/administración |
| POST | `/asignaturas` | Decano, SG | catálogo/administración |
| GET | `/asignaturas/paginado` | Decano, FA, SG | catálogo/administración |
| GET | `/asignaturas/filtro` | Decano, FA, SG | catálogo/administración |
| GET | `/asignaturas/programa/{uuidPrograma}` | Decano, FA, SG | catálogo/administración |
| GET | `/asignaturas/{uuidAsignatura}` | Decano, FA, SG | catálogo/administración |
| PUT | `/asignaturas/{uuidAsignatura}` | Decano, SG | catálogo/administración |
| GET | `/cancelaciones-asignatura/{uuidSolicitud}` | Decano, Estudiante, FA | diseño: lectura compartida entre los actores del proceso |
| GET | `/cancelaciones-matricula/{uuidSolicitud}` | Decano, Estudiante, FA | diseño: lectura compartida entre los actores del proceso |
| PUT | `/catalogos-academicos/tipos-solicitud/{uuidTipo}/funcionario` | Decano, SG | catálogo/administración |
| POST | `/estudiantes/cargar/archivo` | Decano, SG | catálogo/administración |
| POST | `/estudiantes` | Decano, SG | catálogo/administración |
| GET | `/estudiantes/paginado` | Decano, FA, SG | catálogo/administración |
| GET | `/estudiantes/filtro` | Decano, FA, SG | catálogo/administración |
| GET | `/estudiantes/{uuidEstudiante}` | Decano, FA, SG | catálogo/administración |
| PUT | `/estudiantes/{uuidEstudiante}` | Decano, SG | catálogo/administración |
| POST | `/estudiantes/{uuidEstudiante}/asignaturas` | Decano, SG | catálogo/administración |
| PATCH | `/estudiantes/{uuidEstudiante}/asignaturas/{uuidMatricula}/estado` | Decano, SG | catálogo/administración |
| GET | `/examenes-supletorios/{uuidSolicitud}` | Decano, Estudiante, FA | diseño: lectura compartida entre los actores del proceso |
| POST | `/funcionarios-academicos` | Decano, SG | catálogo/administración |
| POST | `/funcionarios-academicos/cargar/archivo` | Decano, SG | catálogo/administración |
| GET | `/funcionarios-academicos/paginado` | Decano, FA, SG | catálogo/administración |
| GET | `/funcionarios-academicos/filtro` | Decano, FA, SG | catálogo/administración |
| GET | `/funcionarios-academicos/{uuidFuncionario}` | Decano, FA, SG | catálogo/administración |
| PUT | `/funcionarios-academicos/{uuidFuncionario}` | Decano, SG | catálogo/administración |
| GET | `/solicitudes-academicas/{uuidSolicitud}` | Decano, Estudiante, FA | diseño: lectura compartida entre los actores del proceso |
| GET | `/solicitudes-academicas/{uuidSolicitud}/historial` | Decano, Estudiante, FA | diseño: lectura compartida entre los actores del proceso |
| POST | `/solicitudes-academicas/{uuidSolicitud}/anexos` | Estudiante, FA | diseño: lectura compartida entre los actores del proceso |
| GET | `/solicitudes-academicas/{uuidSolicitud}/anexos/{uuidAnexo}` | Decano, Estudiante, FA | diseño: lectura compartida entre los actores del proceso |
| GET | `/solicitudes-academicas/{uuidSolicitud}/resolucion` | Decano, Estudiante, FA | diseño: lectura compartida entre los actores del proceso |

Lectura de la tabla:

- Un token de Estudiante no pasa ningún endpoint con `/funcionario/` ni `/decano/`; un token de Funcionario Académico no pasa los de `/estudiante/`, `/decano/`, radicación ni formulario; un token de Decano no pasa radicación, formulario, `/funcionario/` ni `/estudiante/`.
- Un token de SG o F no pasa ningún endpoint de procesos académicos (cada grupo termina en `denyAll`); SG sí administra estudiantes, funcionarios académicos, asignaturas y la asignación de funcionario por tipo.
- El Decano pasa por URL todo lo que pasa SG (administración de usuarios, estudiantes, asignaturas, logs, roles).
- Los endpoints compartidos (detalle, historial, anexos y resolución) dependen del control de dueño en el caso de uso (sección 5).

### 4.2 Endpoints de Julián abiertos a cualquier rol

Un token de Estudiante, Funcionario Académico o Decano puede llamar, por el estado actual de las reglas, los endpoints de la sección 3 y además estos solo-URL:

| Método | Ruta | Por qué |
|---|---|---|
| GET | `/usuarios/{uuidUsuario}` | solo `authenticated()`, sin `@PreAuthorize` |
| PATCH | `/usuarios/{uuidUsuario}` | solo `authenticated()`, sin `@PreAuthorize` |
| GET | `/respuestas/paginado` | `@PreAuthorize(isAuthenticated())`, coherente con la URL pero sin filtro por rol ni dueño |
| GET | `/respuestas/paginado/nombre-solicitud` | `@PreAuthorize(isAuthenticated())`, coherente con la URL pero sin filtro por rol ni dueño |
| GET | `/respuestas/{uuidRespuesta}` | `@PreAuthorize(isAuthenticated())`, coherente con la URL pero sin filtro por rol ni dueño |
| GET | `/respuestas/solicitud/{uuidSolicitud}` | `@PreAuthorize(isAuthenticated())`, coherente con la URL pero sin filtro por rol ni dueño |
| GET | `/respuestas/{uuidRespuesta}/{fileName}` | `@PreAuthorize(isAuthenticated())`, coherente con la URL pero sin filtro por rol ni dueño |
| POST | `/solicitudes` | `@PreAuthorize(isAuthenticated())`, coherente con la URL pero sin filtro por rol ni dueño |
| GET | `/solicitudes/estado` | `@PreAuthorize(isAuthenticated())`, coherente con la URL pero sin filtro por rol ni dueño |
| GET | `/solicitudes/anexos/{uuidSolicitud}/{fileName}` | `@PreAuthorize(isAuthenticated())`, coherente con la URL pero sin filtro por rol ni dueño |
| GET | `/solicitudes/orden-del-dia/estado` | `@PreAuthorize(isAuthenticated())`, coherente con la URL pero sin filtro por rol ni dueño |

Efecto práctico sobre datos: con un token de Estudiante se pueden pedir `GET solicitudes`, `GET solicitudes/paginado`, `GET solicitudes/{uuid}`, `GET solicitudes/buscar`, `GET solicitudes/buscar/solicitante`, `GET solicitudes/exportar*`, `GET solicitudes/anexos/download`, `PUT solicitudes/{uuid}` y `GET solicitudes/funcionario/{uuid}`, es decir, el listado, la búsqueda, la exportación y la edición de las solicitudes al Consejo de Julián con datos de solicitantes. NO VERIFICADO en ejecución; el análisis es de reglas.

## 5. Qué pasa cuando el usuario no es dueño del recurso

Mecanismo central en la extensión: `ValidadorActorSolicitud` (`B/dominio/servicios/ValidadorActorSolicitud.java`) con `validarAlcance`: el Estudiante debe ser el dueño de la solicitud, el Funcionario Académico debe estar asignado al tipo de la solicitud (`TIPO_SOLICITUD_ACADEMICA.funcionarioAcademico`) y el Decano no tiene restricción de alcance. Las lecturas pasan por `ConsultaSolicitudAcademicaCUImplAdaptador.acceso`/`rolEn` (`B/dominio/casosdeuso/ConsultaSolicitudAcademicaCUImplAdaptador.java:176-200`), que devuelve ENTIDAD_NO_ENCONTRADA cuando el usuario no tiene relación con la solicitud. Todas las excepciones de negocio responden HTTP 500 con `codigoError` (hallazgo ya documentado en 06).

| Endpoint | Si el usuario no es dueño o no está asignado | Fuente |
|---|---|---|
| GET solicitudes-academicas/estudiante, funcionario, decano | No aplica: la bandeja se filtra por el usuario del token. Estudiante: solo las propias; Funcionario Académico: solo los tipos que tiene asignados; Decano: todas | `ConsultaSolicitudAcademicaCUImplAdaptador.getBandejaEstudiante/Funcionario/Decano` |
| GET solicitudes-academicas/{uuid}, /historial | Estudiante no dueño o Funcionario no asignado: ENTIDAD_NO_ENCONTRADA (se comporta como si no existiera). Decano: ve cualquiera | `acceso`, `rolEn` |
| GET solicitudes-academicas/{uuid}/anexos/{uuidAnexo} | Igual que el detalle; además el anexo debe pertenecer a esa solicitud o devuelve ENTIDAD_NO_ENCONTRADA | `descargarAnexo` |
| GET solicitudes-academicas/{uuid}/resolucion | `validar`: SOLICITUD_AJENA (estudiante) o TIPO_NO_ASIGNADO_FUNCIONARIO (funcionario); el estudiante además solo si la etapa es final (RESOLUCION_NO_DISPONIBLE) | `ResolucionAcademicaCUImplAdaptador.obtenerResolucion`, líneas 105-110 |
| POST solicitudes-academicas/{uuid}/anexos | `validar` (SOLICITUD_AJENA o TIPO_NO_ASIGNADO_FUNCIONARIO) y luego quién y cuándo puede subir cada tipo de anexo | `AnexoAcademicoCUImplAdaptador`, líneas 65 y 188 |
| POST solicitudes-academicas/{uuid}/resolucion | `validar`: un Funcionario no asignado recibe TIPO_NO_ASIGNADO_FUNCIONARIO | `ResolucionAcademicaCUImplAdaptador`, línea 61 |
| GET cancelaciones-matricula/{uuid}, cancelaciones-asignatura/{uuid}, examenes-supletorios/{uuid} | Pasan por `consultaCU.getDetalle`/`resolverActor`: mismo resultado que el detalle (ENTIDAD_NO_ENCONTRADA). Al Estudiante se le oculta información de la decisión antes de la etapa final | `ConsultaCancelacion*CUImplAdaptador.getDetalle` |
| GET cancelaciones-*/formulario, examenes-supletorios/formulario y POST de radicación | No hay recurso previo: el estudiante sale del token (`getEstudianteAutenticado`); si el usuario no tiene rol Estudiante o fila en `ESTUDIANTE`, ACTOR_SIN_ROL | `ConsultaCancelacion*CUImplAdaptador.getEstudianteAutenticado` |
| POST .../funcionario/rechazar, remitir, responder, recibo, comprobante/aprobar, comprobante/rechazar | `tramite.actorDe(token, FUNCIONARIO)` y `cambiarEtapa` o `solicitudValidada` hacen `validar`: Funcionario Académico no asignado al tipo recibe TIPO_NO_ASIGNADO_FUNCIONARIO | `SolicitudAcademicaCUImplAdaptador.cambiarEtapa` (línea 127), `TramiteSolicitud.solicitudValidada` (línea 59) |
| POST .../decano/aprobar, rechazar | No hay alcance por solicitud: cualquier Decano actúa sobre cualquier solicitud, sujeto a la etapa (transición inválida en `MaquinaEtapas`) | `ValidadorActorSolicitud.validarAlcance` (sin rama para DECANO) |
| POST examenes-supletorios/{uuid}/estudiante/comprobante | `validar`: SOLICITUD_AJENA si el estudiante no es el dueño | `AnexoAcademicoCUImplAdaptador`, línea 65 |
| GET estudiantes/mis-asignaturas | Usa el usuario del token | `EstudianteRestController` |
| GET estudiantes/{uuid}, paginado, filtro y funcionarios-academicos/{uuid}, paginado, filtro | Sin control de dueño: SG, Decano y FA leen cualquier estudiante o funcionario | `ConfiguracionSeguridad` reglas de estudiantes y funcionarios-academicos |
| Endpoints de administración (POST/PUT estudiantes, funcionarios-academicos, asignaturas, usuarios, roles, catálogos) | Sin dueño; solo rol SG o Decano | reglas por URL |

Endpoints de Julián:

| Endpoint | Si el usuario no es dueño | Fuente |
|---|---|---|
| GET usuarios/{uuidUsuario} | Cualquier autenticado lee los datos de cualquier usuario (documento, teléfono, correo, nombre de usuario, tipo, roles). No devuelve la contraseña | `UsuarioRestController.getUsuario`, `UsuarioDTORespuesta` |
| PATCH usuarios/{uuidUsuario} (cambio de contraseña) | No se compara el `uuidUsuario` con el usuario del token; basta enviar la contraseña actual del objetivo. Sin límite de intentos, permite adivinar la contraseña de otro usuario | `UsuarioCUImplAdaptador.cambiarContraseña`, línea 213 |
| GET solicitudes/anexos/{uuidSolicitud}/{fileName}, GET respuestas/{uuidRespuesta}/{fileName} | Cualquier autenticado descarga el archivo si conoce el UUID y el nombre; no se verifica dueño | `SolicitudesRestController.descargarAnexo`, `RespuestaRestController.descargarRespuesta` |
| GET solicitudes, buscar, exportar, PUT solicitudes/{uuid}, y listados de respuestas | Sin control de dueño ni de rol por URL (sección 3) | reglas por URL |

## 6. Vigencia real del JWT

- Vigencia: 24 horas. Sale del código, no de configuración: `B/infraestructura/configuracion/seguridad/jwt/JwtServicio.java`, método `getToken`, línea 36: `setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))`. En `application.properties` no hay propiedad de vigencia ni de clave. La monografía de Julián dice 5 horas: divergente.
- Clave de firma: constante `SECRET_KEY` escrita en la clase `JwtServicio` (línea 23), en Base64, algoritmo HS256 (`SignatureAlgorithm.HS256`). Valor: <REDACTADO>.
- Claims: `sub` (nombre de usuario), `iat` y `exp`. No hay claim de roles.
- `JwtServicio.isTokenValid` solo compara el nombre de usuario y la expiración. `UsuarioEntidad` no sobrescribe `isEnabled`, así que un usuario deshabilitado o con un rol retirado conserva el acceso con un token ya emitido; el estado solo se verifica al iniciar sesión (`SesionCUImplAdaptador`, líneas 36 y 40). Los roles se toman de la base en cada petición, así que un cambio de rol sí aplica sin esperar la expiración.
- `JwtFiltroAutenticacion` solo captura `ExpiredJwtException` (401). El resultado con firma inválida o token mal formado (otras excepciones de jjwt) es NO VERIFICADO.
- No hay refresh ni revocación.

## 7. Riesgos ordenados por gravedad y cambio mínimo

| # | Gravedad | Riesgo | Cambio mínimo | Estado (T12.21) |
|---|---|---|---|---|
| 1 | Alta | Rutas de Julián con regla solo `authenticated()`: cualquier token (incluido Estudiante) lista, busca, exporta y edita (`PUT solicitudes/{uuid}`) las solicitudes al Consejo; `PATCH respuestas/{uuid}/archivo` cae en `anyRequest()` | Agregar reglas `hasAnyAuthority` antes de `solicitudes/**` y de `anyRequest()` según el `@PreAuthorize` de cada método, o activar `@EnableMethodSecurity` después de corregir el punto 3 | CERRADO. Reglas por URL nuevas en `ConfiguracionSeguridad` para los 21 endpoints, `PATCH respuestas/{uuid}/archivo` y `GET`/`PATCH usuarios/{uuid}` (sección 8) |
| 2 | Alta | Clave HS256 fija en el código fuente y versionada: quien la tenga puede emitir tokens de cualquier usuario | Leerla de una variable de entorno y rotarla | CERRADO. La clave sale de `jwt.secreto` = `${JWT_SECRET:...}`; el valor anterior queda solo como valor por defecto local en `application.properties` y `docker-compose.yml`. Rotarla fuera del entorno local sigue PENDIENTE |
| 3 | Alta | `@PreAuthorize` sin `@EnableMethodSecurity`: 124 anotaciones sin efecto, 22 contradicen la URL; activarlas sin corregir rompe el formulario público (`POST solicitudes/public`, `tipos/solicitudes/{uuid}` y `perfil/*` tienen `@PreAuthorize` con roles) | Primero alinear esas 5 anotaciones con su regla pública; luego agregar `@EnableMethodSecurity` a `ConfiguracionSeguridad` | NO APLICA. Por decisión de la tarea no se activa `@EnableMethodSecurity`; las reglas por URL ya siguen el uso real |
| 4 | Media | `GET tipos/solicitudes/paginado` y `filtro` públicos por orden de reglas | Mover la regla #7 (`tipos/solicitudes/**` SG y Decano) antes de la #6 solo para esas dos rutas, o declarar `paginado` y `filtro` antes de `{uuidTipoSolicitud}` | NO APLICA. Se dejan públicos a pedido; el frontend público no los usa (solo `sec-general/tipos/solicitudes`, Secretario General) |
| 5 | Media | `PATCH usuarios/{uuid}` sin comprobar que sea el propio usuario y sin límite de intentos | Comparar `uuidUsuario` con el usuario del token en `cambiarContraseña` | CERRADO en dueño (solo el propio usuario o el Secretario General). El límite de intentos sigue PENDIENTE |
| 6 | Media | `GET usuarios/{uuid}` entrega datos personales de cualquier usuario a cualquier autenticado | Restringir a SG y Decano o al propio usuario | CERRADO. Solo el propio usuario o el Secretario General; a otro rol le responde ENTIDAD_NO_ENCONTRADA |
| 7 | Media | Descarga de anexos y respuestas de Julián sin control de dueño; posible recorrido de directorios por doble decodificación del nombre (`URLDecoder.decode` sobre una variable ya decodificada por Spring), NO VERIFICADO en ejecución | Resolver la ruta con `Path.normalize()` y comprobar que quede dentro de `basePath`; validar dueño o rol | CERRADO. Dueño, funcionario asignado o rol interno (SG, Decano, Secretaria Decanatura FIET), y ruta normalizada dentro de `uploads` |
| 8 | Media | Token válido 24 h sin comprobar el estado del usuario en cada petición | Sobrescribir `isEnabled` en `UsuarioEntidad` con el estado del usuario | CERRADO. `JwtFiltroAutenticacion` responde 401 si el usuario está deshabilitado |
| 9 | Baja | Decano sin alcance por solicitud (cualquier Decano actúa sobre cualquiera) | Decisión de negocio; si hay varios decanos, agregar rama en `validarAlcance` | PENDIENTE (decisión de negocio) |
| 10 | Baja | Errores de negocio como HTTP 500 | Mapear a 4xx en `RestApiExcepcion` | PENDIENTE |

Nota de conciliación: el análisis anterior (`06-seguridad-y-errores.md`) reportaba 138 `@PreAuthorize`; el conteo por grep de esta verificación da 124 líneas en los controladores. Se toma 124. Los 22 endpoints divergentes y la regla número 78 (`examenes-supletorios/**` denyAll) fueron recalculados aquí.

## 8. Cambios aplicados en T12.21

Sin `@EnableMethodSecurity`. Los `@PreAuthorize` existentes no se cambiaron; donde el uso real del frontend incluye un rol que la anotación no tiene, la regla por URL sigue el uso real y se indica abajo. El uso real se obtuvo recorriendo, para cada método de `SolicitudesService`, `RespuestasService`, `UsuariosService` y `TipoSolicitudService`, los componentes que lo llaman y las rutas de `app.routes.ts` que los cargan, con sus roles.

### 8.1 Reglas por URL nuevas (`ConfiguracionSeguridad.securityFilterChain`)

Antes de `solicitudes/**`:

| Método | Ruta | Roles | Uso real en el frontend | Diferencia con `@PreAuthorize` |
|---|---|---|---|---|
| GET | `solicitudes` | SG, Decano, F | sin uso | ninguna |
| GET | `solicitudes/paginado` | SG, Decano, F | `sec-general/solicitudes` (SG) | ninguna |
| GET | `solicitudes/buscar` | SG, Decano, F | `sec-general/solicitudes` (SG) | ninguna |
| GET | `solicitudes/buscar/solicitante` | SG, Decano | sin uso | ninguna |
| GET | `solicitudes/fun/**` | SG, Decano, F | `funcionario/solicitudes` (F) | ninguna |
| GET | `solicitudes/funcionario/{uuidFuncionario}` | SG, Decano, F | `funcionario/solicitudes` (F) | agrega F |
| GET | `solicitudes/anexos/download` | SG, Decano | `sec-general/orden-del-dia` (SG) | ninguna |
| GET | `solicitudes/exportar/**` | SG, Decano | `sec-general/orden-del-dia` (SG) | ninguna |
| GET | `solicitudes/estado` | autenticado | `sec-general/orden-del-dia` (SG) | ninguna; regla explícita para que no la tome `{uuidSolicitud}` |
| GET | `solicitudes/{uuidSolicitud}` | SG, Decano, F (T12.23 quitó a la Secretaria) | SG y F | ninguna |
| PUT | `solicitudes/{uuidSolicitud}` | SG, Decano, F (T12.22 quitó a la Secretaria); dueño y reasignación en el caso de uso desde T12.23 | SG y F | agrega F |

Antes de `GET respuestas/**`:

| Método | Ruta | Roles | Uso real | Diferencia |
|---|---|---|---|---|
| GET | `respuestas/paginado/funcionario` | SG, Decano, F (T12.23 quitó a la Secretaria) | `funcionario/respuestas` | ninguna |
| GET | `respuestas/paginado/funcionario-nombre-solicitud` | igual | igual | ninguna |
| PATCH | `respuestas/{uuidRespuesta}/archivo` | SG, Decano, F (T12.23 quitó a la Secretaria) | SG y F | ninguna; antes caía en `anyRequest()` |

Antes de `GET usuarios/**`: `GET usuarios/{uuidUsuario}` y `PATCH usuarios/{uuidUsuario}` con `authenticated()` explícito; el control de dueño está en el caso de uso.

Los públicos (`sesiones`, `POST solicitudes/public`, `tipos/solicitudes/perfil`, `perfil/paginado`, `perfil/filtro`, `tipos/solicitudes/{uuid}`, `tipos/solicitudes/paginado` y `filtro`) no cambiaron. La vista pública (`usuario-publico/solicitudes`) usa `tipos/solicitudes/{uuid}` y los dos de `perfil`; `paginado` y `filtro` solo los usa la pantalla del Secretario General.

Corrección (T12.22): el hallazgo que decía que `sec-fiet/respuestas` sube la respuesta con `POST respuestas/{uuid}/archivo` era falso; ver 8.5.

### 8.2 Dueño del recurso

Caso de uso nuevo `AccesoRecursoConsejoCUImplAdaptador` (puerto `AccesoRecursoConsejoCUIntPuerto`, bean `crearAccesoRecursoConsejoCU`):

| Endpoint | Quién puede | Respuesta si no corresponde |
|---|---|---|
| GET `usuarios/{uuid}` | el propio usuario o el Secretario General | ENTIDAD_NO_ENCONTRADA (código 3), como las lecturas de la extensión |
| PATCH `usuarios/{uuid}` | el propio usuario o el Secretario General | REGLA_NEGOCIO_VIOLADA (código 4) con `USUARIO_AJENO` |
| GET `solicitudes/anexos/{uuid}/{archivo}` | el solicitante (mismo tipo y número de documento que `InformacionSolicitante`), el Funcionario asignado a la solicitud, o SG, Decano y Secretaria Decanatura FIET | ENTIDAD_NO_ENCONTRADA |
| GET `respuestas/{uuid}/{archivo}` | igual, sobre la solicitud de la respuesta | ENTIDAD_NO_ENCONTRADA |

### 8.3 Recorrido de directorios

`AlmacenadorArchivos.resolverArchivo` normaliza la ruta y exige que el archivo quede directamente en `uploads/<carpeta>/<uuid>/`; rechaza separadores en el uuid y en el nombre. `AlmacenadorArchivosRecorridoTest` demuestra que la construcción anterior (`new File(base + "/anexos/" + uuid + "/" + URLDecoder.decode(nombre))`) sí salía de la carpeta con `%252e%252e%252f` una vez decodificado por Spring y por el controlador, y que la nueva no sale con `../`, `%2e%2e%2f`, `%252e%252e%252f`, `..%5C` ni con un uuid malicioso. Por HTTP, el `StrictHttpFirewall` de Spring Security rechaza por defecto `%25`, `%2F` y `%2E` codificados, así que el ataque probablemente no llegaba al controlador: NO VERIFICADO en ejecución.

### 8.4 Pruebas

| Clase | Pruebas | Qué cubre |
|---|---|---|
| `infraestructura/configuracion/seguridad/configuracion/ConfiguracionSeguridadRolesTest` | 109 | `@WebMvcTest` con la cadena real (`ConfiguracionSeguridad`, `JwtFiltroAutenticacion`, `JwtServicio`) y un controlador comodín: Estudiante 403 y sin token 401 en los cerrados; FA y Docente 403; flujos legítimos de Estudiante, FA, Decano, F, Secretaria, SG y Docente 200; públicos 200; cruces de roles de la extensión 403; usuario deshabilitado 401 |
| `dominio/casosdeuso/AccesoRecursoConsejoCUImplAdaptadorTest` | 23 | dueño de usuario, cambio de contraseña, anexos y respuestas |
| `infraestructura/configuracion/lectorArchivos/almacenador/AlmacenadorArchivosRecorridoTest` | 17 | recorrido de directorios |
| `AlmacenadorArchivosTest`, `ValidadorActorSolicitudTest`, `TramiteSolicitudTest` (existentes) | 68 | regresión |

Resultado: 217 pruebas, 0 fallos (`mvnw -o test -Dtest=...`). No se corrió la suite completa ni los scripts de humo, que necesitan la base y el backend arriba.

### 8.5 Revisión de `sec-fiet/respuestas` y del PUT del Funcionario (T12.22)

Error del análisis de T12.21: la página `core/secretariaFiet/pages/secretaria-fiet-respuestas-component/secretaria-fiet-respuestas-component.ts` importa `FunRespuestasComponent` (línea 4) pero no lo usa: su `main` es `SecFietRespuestasComponent`. El recorrido estático de imports contó las llamadas de la pantalla del Funcionario como si fueran de la Secretaria.

Lo que llama de verdad `sec-fiet/respuestas` (`shared/pages/content/sec-fiet-respuestas-content-component/sec-fiet-respuestas-content-component.ts`), solo con `RespuestasService`:

| Acción de la pantalla | Endpoint |
|---|---|
| Cargar la tabla y paginar (`cargarRespuestas`) | GET `respuestas/paginado` |
| Barra "Buscar por solicitud" (`onBuscarSolicitud`) | GET `respuestas/paginado/nombre-solicitud` |
| Botón ojo de la fila (`verMasInfo`) | GET `respuestas/{uuid}` |
| Botón de descarga del diálogo (`app-descargar-respuesta-component`) | GET `respuestas/{uuid}/{archivo}` |

No llama GET ni PUT `solicitudes/{uuid}`, ni POST ni PATCH `respuestas/{uuid}/archivo`, ni `respuestas/paginado/funcionario*`. La pantalla no muestra botón para adjuntar ni quitar el archivo de respuesta, así que no hay 403 visible para ese rol.

Cambio aplicado: se quitó Secretaria Decanatura FIET de la regla `PUT solicitudes/{uuidSolicitud}` (queda SG, Decano, F) y `ConfiguracionSeguridadRolesTest` pasó ese caso de 200 a 403 (109 pruebas, 0 fallos).

Por el mismo motivo, la Secretaria tampoco usa GET `solicitudes/{uuidSolicitud}`, GET `respuestas/paginado/funcionario`, GET `respuestas/paginado/funcionario-nombre-solicitud` ni PATCH `respuestas/{uuidRespuesta}/archivo`. Se quitó de esas reglas en T12.23 (8.6).

PUT `solicitudes/{uuid}` del Funcionario: `SolicitudCUImplAdaptador.actualizarSolicitud` (`dominio/casosdeuso/SolicitudCUImplAdaptador.java:158-193`) no compara al usuario del token con el funcionario de la solicitud. Un Funcionario puede editar nombre, descripción, estado, consecutivo y orden del día de cualquier solicitud del Consejo, y reasignarla a otro funcionario con `uuidFuncionario`. Si el uuid no existe, `solicitudOriginal` es nulo y la respuesta es un 500 sin `codigoError`. La pantalla no lo necesita así: `funcionario/solicitudes` (`fun-solicitudes-content-component.ts`, botón de lápiz `abrirModalActualizarSolicitud`) solo lista las solicitudes del propio funcionario (`getSolicitudesPorFuncionario(this.usuario.uuidUsuario, ...)`), y `funcionario/respuestas` (`dialog-gestion-respuesta-component.ts`, `actualizarEstadoSolicitud`) actualiza la solicitud de una respuesta que ya es suya. Exigir que el Funcionario sea el asignado no rompería ninguna de las dos. Corregido en T12.23 (8.6).

Nota de entorno: durante la corrida, el compilador del IDE escribió en `target/classes` un `ConfiguracionSeguridad.class` roto ("Flexible Constructor Bodies ... source level 25"), y Maven lo tomó como al día. Se borró solo ese `.class` y Maven lo recompiló. Si vuelve a pasar, conviene configurar el IDE con Java 17 o compilar con Maven.

### 8.6 Secretaria sin rutas de más y PUT de solicitudes con dueño (T12.23)

Reglas: se quitó Secretaria Decanatura FIET de GET `solicitudes/{uuidSolicitud}`, GET `respuestas/paginado/funcionario`, GET `respuestas/paginado/funcionario-nombre-solicitud` y PATCH `respuestas/{uuidRespuesta}/archivo`. Sus cuatro endpoints reales de `sec-fiet/respuestas` (GET `respuestas/paginado`, `paginado/nombre-solicitud`, `respuestas/{uuid}` y la descarga `respuestas/{uuid}/{archivo}`) siguen bajo `GET respuestas/**` con `authenticated()`. `SECRETARIA_DECANATURA_ROL` ya no aparece en `ConfiguracionSeguridad`; sigue en `AccesoRecursoConsejoCUImplAdaptador` como rol interno para descargar respuestas.

PUT `solicitudes/{uuid}`: el controlador llama primero a `AccesoRecursoConsejoCUIntPuerto.verificarActualizacionSolicitud(uuidSolicitud, uuidFuncionarioNuevo, token)`:

| Caso | Resultado |
|---|---|
| La solicitud no existe (cualquier rol) | ENTIDAD_NO_ENCONTRADA, código 3. `SolicitudCUImplAdaptador.actualizarSolicitud` también lo valida antes de usar la solicitud |
| Secretario General o Decano | sin cambios, incluida la reasignación con `uuidFuncionario` |
| Funcionario asignado a la solicitud (`objFuncionario`) | puede actualizar; `uuidFuncionario` vacío o igual al actual |
| Funcionario asignado que envía otro `uuidFuncionario` | REGLA_NEGOCIO_VIOLADA (código 4) con `REASIGNACION_NO_PERMITIDA` |
| Funcionario no asignado, o solicitud sin funcionario | ENTIDAD_NO_ENCONTRADA, como en las descargas |
| Cualquier otro rol | ENTIDAD_NO_ENCONTRADA (la URL ya lo frena con 403) |

"Funcionario responsable de una respuesta asociada": `respuestas` no guarda funcionario propio. La bandeja `funcionario/respuestas` sale de `RespuestaRepositorio.obtenerRespuestasPorFuncionario`, que une `r.solicitud s JOIN s.objFuncionario`, así que el responsable de la respuesta es siempre el funcionario asignado a la solicitud. Ese caso queda cubierto por la misma regla y tiene su propia prueba.

Pantallas: `funcionario/solicitudes` (botón lápiz) solo edita solicitudes de su propia bandeja y no envía `uuidFuncionario`. `funcionario/respuestas` (`dialog-gestion-respuesta-component.actualizarEstadoSolicitud`) envía `uuidFuncionario = solicitud.objFuncionario.uuidUsuario`, que es el mismo funcionario. Ninguna de las dos se ve afectada. Excepción posible: si el Secretario General reasigna una solicitud mientras el Funcionario anterior tiene la pantalla abierta, el siguiente guardado de ese Funcionario recibe "no existe". Es el comportamiento buscado, pero la pantalla mostrará el mensaje de error genérico.

Fuera del alcance, no corregido: `SolicitudCUImplAdaptador.actualizarSolicitud` sigue haciendo `solicitudOriginal.getObjFuncionario().getUuidUsuario()` sin comprobar nulo. Si una solicitud no tiene funcionario y el Secretario General o el Decano envían `uuidFuncionario`, la respuesta es un 500. El Funcionario no llega ahí porque antes recibe "no existe".

Pruebas: `ConfiguracionSeguridadRolesTest` 112, `AccesoRecursoConsejoCUImplAdaptadorTest` 36 y `SolicitudCUImplAdaptadorTest` 3 (nueva). Total 151, 0 fallos.
