# 00 - Índice y método

## Índice de documentos

| Archivo | Contenido |
|---|---|
| 00-RESUMEN-EJECUTIVO.md | Resumen de tres páginas |
| 01-vision-general-y-stack | Versiones, dependencias, estructura, líneas de código, mapa BASE/EXTENSIÓN |
| 02-backend-arquitectura | Paquetes, capas, inventario, inyección de dependencias |
| 03-backend-endpoints | Catálogo de endpoints |
| 04-backend-casos-de-uso | Reglas de negocio por caso de uso y por proceso |
| 05-base-de-datos | Tablas, relaciones, E-R |
| 06-seguridad-y-errores | Seguridad, JWT, errores, historial |
| 07-frontend-arquitectura | Rutas, guards, servicios, componentes compartidos |
| 08-frontend-pantallas | Catálogo de pantallas por rol |
| 09-flujos-y-estados | Flujos extremo a extremo y máquinas de estado |
| 10-contraste-monografia | Afirmaciones de la monografía de Julián contra el código |
| 11-pruebas-despliegue-configuracion | Tests, Docker, configuración |
| 12-insumos-diagramas / 13-diagramas-fuente | Insumos y código Mermaid de los diagramas |
| 14-hallazgos-deuda-tecnica / 15-glosario | Hallazgos y glosario |
| 16-archivos-para-subir-a-claude | Paquete para la asistente de redacción |
| correo-electronico.md | Correo electrónico: destinatarios por proceso, implementación, envío programado de la Resolución, configuración, pruebas y puntos abiertos (agregado el 2026-10-21) |
| SUPUESTOS.md, PROGRESO.md, VERIFICACION.md, VERIFICACION-SEGURIDAD.md | Control del análisis y revisión de seguridad |

## Método

Actualización del 2026-10-21 (T12.37): revisión sencilla de los documentos contra el código, `change.md`, el contrato de la API, el diccionario de datos, `etapas-por-proceso.md` y los `CLAUDE.md`, sin cambiar su estructura. Las cifras se recontaron con grep y scripts de conteo; los inventarios muy detallados (tablas por módulo, anexos de endpoints) conservan el corte del 2026-10-14 y lo dicen. Tampoco se ejecutó nada en esta actualización.

Método original (2026-10-14):

- Lectura estática del repositorio con Glob, Grep y lectura de archivos. No se ejecutó ningún build, test ni instalación, ni se usó internet.
- Git solo en modo lectura (log, blame, show, status).
- Docker Desktop no estaba corriendo, por lo que el esquema se dedujo de entidades JPA, `docs/database/` y scripts SQL.
- Cada afirmación lleva ruta de archivo y clase o método. Lo no comprobado se marca NO VERIFICADO.
- Se trabajó por fases; las fases 2 a 8 y 11 se repartieron entre subagentes y se consolidaron.

## Convención de etiquetas

- [BASE]: código, tablas o datos del prototipo de Julián David Camacho Erazo.
- [EXTENSIÓN]: lo agregado en este trabajo (Cancelación de Matrícula, Cancelación de Asignatura, Examen Supletorio, Estudiante, Funcionario Académico, Decano, programas, asignaturas, sesiones).

## Cómo se distinguió BASE de EXTENSIÓN

El historial de git sirve solo en parte: el primer commit (`d3a48cc`, 2026-09-30, "Inicializa el repositorio del trabajo de grado") importó 647 archivos y 38 051 líneas, y los 86 commits son de un solo autor. Para el árbol Java de main, el contenido de `git ls-tree d3a48cc` separa exactamente: 175 archivos [BASE] y 284 [EXTENSIÓN] (ver 02). Para el resto se usaron tres criterios, en este orden:

1. Lo que `CLAUDE.md` raíz, `backend/CLAUDE.md` y `frontend/CLAUDE.md` declaran como de Julián o como nuevo.
2. Nombres de clases, paquetes y tablas con vocabulario de la extensión (Academic, Cancelacion, Supletorio, Estudiante, Asignatura, Programa, Resolucion, Sesion, Situacion, Etapa, Anexo).
3. Archivos de la lista de Julián (Usuario, Rol, Solicitud, TipoSolicitud, OrdenDelDia, Respuesta, Log, Funcionario, seguridad JWT, carga masiva Excel, exportación .docx).

Límite: archivos de Julián tocados por la extensión (por ejemplo `ConfiguracionSeguridad`, `MensajesError`, `BeanConfiguracion`, `data.sql`) figuran como [BASE] con adiciones [EXTENSIÓN]; la clasificación por nombre es aproximada.

## Límites del análisis

- Sin ejecución: comportamiento en tiempo de ejecución y cobertura real de tests no medidos.
- No se tiene acceso a la monografía de Julián; se contrastan solo las afirmaciones listadas en `docs/PROMPT_ANALISIS.md`.
- Ramas Prod y Dev: solo existe `main` (ver SUPUESTOS.md).
