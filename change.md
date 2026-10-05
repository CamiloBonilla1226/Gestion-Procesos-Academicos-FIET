# Registro de cambios

La entrada más reciente va primero. Formato y reglas en `CLAUDE.md`.

## 2026-10-05 - Roles Estudiante y Funcionario Académico (Tarea 3)
- Qué se hizo: constantes ESTUDIANTE_ROL y FUNCIONARIO_ACADEMICO_ROL; roles y tipos de usuario "Estudiante" y "Funcionario Académico" en data.sql y en un seed idempotente con INSERT IGNORE, aplicado en la base local (11 roles, 9 tipos de usuario); .claude/settings.local.json ignorado por git.
- Archivos: `backend/solicitudes/src/main/java/com/unicauca/cfiet/solicitudes/dominio/helper/constantes/ApplicationConstantes.java` (modificado), `backend/solicitudes/src/main/resources/data.sql` (modificado), `docs/database/seed-roles-extension.sql` (creado), `.gitignore` (modificado)
- Notas: UUID fijos: rol Estudiante b77e970f-9c82-4c4b-9570-fe79840245a7, rol Funcionario Académico 996f4a2d-e051-4367-a702-a3e6a0a91873, tipo Estudiante 5725e5e5-aa04-494c-b347-be14f619fd97, tipo Funcionario Académico cf25e8ad-8c17-494e-8f3b-70ac00801d3e. Se agregó el ";" que le faltaba a la última sentencia de data.sql (INSERT INTO Usuario_has_Roles), como indica backend/CLAUDE.md. El Decano reutiliza el rol Decano y el tipo "Maxima autoridad FIET - Decano"; no se crearon otros.

## 2026-10-05 - Documentación: seguridad real, Decano, creación de actores y pruebas (Tarea 2)
- Qué se hizo: se corrigió la documentación para que diga que solo los requestMatchers protegen (no @PreAuthorize), que el Decano no tiene fila en FUNCIONARIO_ACADEMICO, que los actores se crean con UsuarioCUIntPuerto.crearUsuario y @Transactional en el controlador (D1, D6, D7), y se agregó la sección "Pruebas" (niveles A, B, C y compuerta de aceptación).
- Archivos: `backend/CLAUDE.md` (modificado), `.claude/skills/extender-backend-fiet/SKILL.md` (modificado), `docs/database/diccionario-datos-extension.md` (modificado), `docs/database/script_bd_extension.sql` (modificado, solo comentarios)
- Notas: el script tiene 18 CREATE TABLE y el diccionario 18 secciones, así que el conteo ya era correcto. La columna dependencia queda como la dependencia u oficina del Técnico Administrativo y no distingue roles. También se actualizó el paso 9 de la skill (decía que la verificación era manual) y la nota de BeanConfiguracion sobre la dependencia entre beans hacia UsuarioCUIntPuerto. Sin cambios en Java ni en SQL ejecutable.

## 2026-10-05 - Prueba de seguridad de @PreAuthorize (Tarea 1)
- Qué se hizo: script de humo que crea un usuario con rol Funcionario y comprueba si puede listar usuarios; funciones comunes de sesión y llamadas a la API en un archivo aparte.
- Archivos: `backend/pruebas/comun.ps1` (creado), `backend/pruebas/t1_preauthorize.ps1` (creado)
- Notas: resultado PREAUTHORIZE INERTE. Un Funcionario recibe 200 en GET usuarios, usuarios/paginado y usuarios/funcionarios, porque no hay @EnableMethodSecurity y ConfiguracionSeguridad deja GET usuarios/** en authenticated(). Los .ps1 se mantienen en ASCII: PowerShell 5.1 lee los archivos sin BOM como ANSI, por eso las tildes se arman con [char] en comun.ps1. No se cambió código Java.
