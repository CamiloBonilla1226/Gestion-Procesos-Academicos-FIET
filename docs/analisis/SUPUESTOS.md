# Supuestos y decisiones

1. El prompt prohíbe modificar archivos fuera de docs/analisis/; la regla de CLAUDE.md de registrar en change.md choca con eso. Se priorizó el prompt explícito: change.md NO se modificó. Se debe agregar manualmente una entrada si se desea.
2. Docker no está corriendo (no hay API de Docker disponible): el esquema se deduce de entidades JPA, docs/database y scripts SQL.
3. Solo existe la rama main (local y origin); las ramas Prod/Dev de la monografía no se encuentran en el repo.
4. La regla de no comentarios aplica a código; los .md del análisis sí llevan texto explicativo.
5. Actualización T12.37 (2026-10-21): esta vez sí se registra en `change.md`, porque la tarea lo permite y `CLAUDE.md` lo exige. Las cifras de ejecución de pruebas se toman de `change.md`; no se ejecutó nada.
6. Los inventarios muy detallados (tabla por módulo de 02, anexos B y C de 03, columnas de tablas y códigos de error de los endpoints nuevos) no se recalcularon; cada documento dice qué parte conserva el corte del 2026-10-14.
7. No se regeneró el paquete `paquete-para-claude/`: copia el estado del 2026-10-14 y sus archivos 08 a 12 son copias de código y configuración con secretos redactados; regenerarlo exige repetir esa redacción.
