# 16 - Archivos para subir a la asistente

La asistente (otra instancia de Claude en un proyecto de claude.ai) no tiene acceso al repositorio. Rutas relativas a la raíz del repositorio. Tamaños en KB aproximados.

## 1. Lista priorizada

### Imprescindibles

| Ruta | KB | Contiene | Capítulo | Por qué |
|---|---|---|---|---|
| docs/analisis/00-RESUMEN-EJECUTIVO.md | 5 | Cifras y 10 hallazgos | Introducción, conclusiones | Da el panorama en tres páginas |
| docs/analisis/02-backend-arquitectura.md | 67 | Capas, inventario, inyección de dependencias | Arquitectura | Base del capítulo de diseño |
| docs/analisis/05-base-de-datos.md | 51 | Tablas, E-R, mapeo JPA | Base de datos | Sustenta el modelo de datos |
| docs/analisis/04-backend-casos-de-uso.md | 60 | Reglas por caso de uso y proceso | Implementación | Sustenta la lógica de negocio |
| docs/analisis/10-contraste-monografia.md | 11 | Afirmaciones de Julián contra el código | Patrones, antecedentes | Evita repetir afirmaciones falsas |
| docs/analisis/13-diagramas-fuente.md | 11 | 41 diagramas Mermaid | Diagramas | Listos para pegar |
| docs/analisis/09-flujos-y-estados.md | 73 | Flujos y máquinas de estado | Implementación | Describe los tres procesos |

### Recomendados

| Ruta | KB | Contiene | Capítulo | Por qué |
|---|---|---|---|---|
| docs/analisis/03-backend-endpoints.md | 114 | 128 endpoints | Implementación | Catálogo de la API |
| docs/analisis/06-seguridad-y-errores.md | 32 | Seguridad, JWT, errores, logs | Seguridad | Sustenta el capítulo de seguridad |
| docs/analisis/07-frontend-arquitectura.md y 08-frontend-pantallas.md | 44 y 41 | Rutas, servicios, pantallas | Frontend | Describe la interfaz |
| docs/analisis/12-insumos-diagramas.md | — | Listas de elementos de diagramas | Diagramas | Complementa el 13 |
| docs/analisis/11-pruebas-despliegue-configuracion.md | 21 | Tests, Docker, configuración | Pruebas, despliegue | Corrige la afirmación "solo Postman" |
| docs/analisis/14-hallazgos-deuda-tecnica.md | 7 | Hallazgos | Discusión | Trabajo futuro |
| docs/database/etapas-por-proceso.md | 37 | Decisiones por proceso | Requisitos | Fuente de reglas de negocio |

### Opcionales

| Ruta | KB | Contiene | Capítulo | Por qué |
|---|---|---|---|---|
| docs/analisis/01, 15, SUPUESTOS, VERIFICACION | pocos | Stack, glosario, control | Anexos | Contexto |
| docs/api/contrato-api-procesos-academicos.md | 61 | Contrato de 66 endpoints | Anexos | Detalle de la API nueva |
| docs/database/script_bd_extension.sql y diccionario-datos-extension.md | 18 y 21 | DDL de referencia y diccionario | Base de datos | Detalle de columnas |
| backend/CLAUDE.md y frontend/CLAUDE.md | 85 y 54 | Arquitectura documentada | Arquitectura | Redundante con el análisis |

## 2. Paquete reducido (docs/analisis/paquete-para-claude/)

Doce archivos, todos de 150 KB o menos; secretos reemplazados por `<REDACTADO>` (clave JWT, contraseñas de MySQL en compose y properties).

| Archivo | KB | Contenido |
|---|---|---|
| 01-resumen-metodo-hallazgos.md | 38 | Resumen, método, stack, contraste, hallazgos, glosario, supuestos, verificación |
| 02-arquitectura-backend-y-seguridad.md | 98 | Documentos 02 y 06 |
| 03-endpoints.md | 113 | Documento 03 |
| 04-casos-de-uso-y-base-de-datos.md | 110 | Documentos 04 y 05 |
| 05-frontend.md | 84 | Documentos 07 y 08 |
| 06-flujos-pruebas-despliegue.md | 93 | Documentos 09 y 11 |
| 07-diagramas.md | 104 | Documentos 12 y 13 |
| 08-codigo-backend-clave.md | 142 | Código fuente: seguridad, JWT, MaquinaEtapas, BeanConfiguracion, entidades, controlador y casos de uso de matrícula y supletorio, usuarios |
| 09-configuracion-y-despliegue.md | 10 | pom.xml, package.json, angular.json, docker-compose, Dockerfiles, application.properties |
| 10-sql-y-semillas.md | 36 | DDL de extensión, seeds, migraciones, data.sql |
| 11-documentacion-del-repo.md | 133 | CLAUDE.md raíz, README, diccionario, etapas por proceso, contrato API |
| 12-claude-md-backend-y-frontend.md | 137 | CLAUDE.md de backend y frontend |

Cada archivo concatena sus fuentes bajo separadores `===== ... =====`. Los archivos 01 a 07 se generaron de los documentos de este análisis; los 08 a 12 son copias de archivos del repositorio. Estos paquetes se copiaron al momento del análisis: si el repositorio cambia, hay que regenerarlos.

## 3. Qué NO subir

- Binarios y compilados: `backend/solicitudes/target/`, `frontend/dist/`, `.jar`.
- Dependencias: `frontend/node_modules/`.
- Archivos subidos por usuarios: `backend/uploads/`.
- Dumps o bases con datos reales, `limpieza_local.sql` si contiene datos reales (NO VERIFICADO su contenido).
- Archivos `.env` y cualquier credencial; `package-lock.json` (grande y sin valor para la monografía).
- Carpeta `.git`.

## 4. Orden recomendado de subida

1. 01-resumen-metodo-hallazgos.md
2. 02-arquitectura-backend-y-seguridad.md
3. 04-casos-de-uso-y-base-de-datos.md
4. 10-sql-y-semillas.md
5. 06-flujos-pruebas-despliegue.md
6. 07-diagramas.md
7. 05-frontend.md
8. 03-endpoints.md
9. 09-configuracion-y-despliegue.md
10. 08-codigo-backend-clave.md
11. 11-documentacion-del-repo.md
12. 12-claude-md-backend-y-frontend.md
