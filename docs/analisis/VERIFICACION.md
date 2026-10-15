# Verificación cruzada

| Comprobación | Resultado | Método |
|---|---|---|
| Total de endpoints del catálogo (03) contra anotaciones de mapeo | CUADRA: 128 en el catálogo y 128 por grep de `@Get/Post/Put/Patch/DeleteMapping` en main (75 GET, 41 POST, 9 PUT, 3 PATCH, 0 DELETE) | grep de líneas con la anotación, ejecutado por el subagente y repetido en el cierre |
| Endpoints de la extensión contra `docs/api/contrato-api-procesos-academicos.md` | CUADRA: 66 | subagente de fase 3 |
| Total de tablas contra entidades y SQL | CUADRA: 32 `@Entity` (grep en el cierre), 19 `CREATE TABLE` en `docs/database/script_bd_extension.sql` (grep en el cierre) y 19 entidades de extensión; las tablas físicas son 33 por `Usuario_has_Roles`, que sale de un `@JoinTable` | grep y lectura (05) |
| 18 contra 19 tablas nuevas | RESUELTO: son 19; el 18 es un conteo histórico en `change.md` y `docs/informe-discrepancias-documentacion.md` | 05 |
| Pantallas del catálogo (08) contra el routing | CUADRA: las 35 rutas `path` de `frontend/src/app/app.routes.ts` aparecen todas en 08 | extracción de `path:` y búsqueda de cada una en el documento |
| Tests: 38 archivos, 615 `@Test` | CUADRA con 11; no se ejecutaron | grep |
| Specs frontend: 136 | CUADRA con 07/08/11 | `find *.spec.ts` |
| Componentes Angular: 145 | CUADRA con 07 | grep de `@Component(` sin specs |
| Diagramas Mermaid de 13 | 41 bloques; sintaxis no renderizada (NO VERIFICADO) | conteo de bloques |

## Discrepancias detectadas y corregidas

- `00-indice-y-metodo.md` y `01-vision-general-y-stack.md` decían que la separación BASE/EXTENSIÓN era heurística; el subagente de fase 2 demostró que `git ls-tree d3a48cc` la separa exactamente en el árbol Java (175 y 284). Se corrigieron ambos.
- `10-contraste-monografia.md` afirmaba un límite de memoria ambiguo en compose; se corrigió a "solo backend".

## Discrepancias abiertas

- Conteo de `@PreAuthorize`: conciliado en `VERIFICACION-SEGURIDAD.md`: 124 en los controladores (el 138 reportado por el subagente era erróneo).
- Líneas de código del frontend subestimadas por directorios con espacios (01).
- Tests ejecutados (1233 según `backend/CLAUDE.md`) contra 696 métodos contados: NO VERIFICADO.
- No se ejecutó nada: ni builds, ni tests, ni diagramas renderizados, ni consultas a la base (Docker apagado).
- Las clasificaciones [BASE] de archivos de tests y del frontend parten del commit inicial; no hay historial anterior de Julián.
