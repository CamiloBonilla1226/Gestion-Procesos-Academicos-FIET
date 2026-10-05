# Registro de cambios

La entrada más reciente va primero. Formato y reglas en `CLAUDE.md`.

## 2026-10-05 - Documentación: seguridad real, Decano, creación de actores y pruebas (Tarea 2)
- Qué se hizo: se corrigió la documentación para que diga que solo los requestMatchers protegen (no @PreAuthorize), que el Decano no tiene fila en FUNCIONARIO_ACADEMICO, que los actores se crean con UsuarioCUIntPuerto.crearUsuario y @Transactional en el controlador (D1, D6, D7), y se agregó la sección "Pruebas" (niveles A, B, C y compuerta de aceptación).
- Archivos: `backend/CLAUDE.md` (modificado), `.claude/skills/extender-backend-fiet/SKILL.md` (modificado), `docs/database/diccionario-datos-extension.md` (modificado), `docs/database/script_bd_extension.sql` (modificado, solo comentarios)
- Notas: el script tiene 18 CREATE TABLE y el diccionario 18 secciones, así que el conteo ya era correcto. La columna dependencia queda como la dependencia u oficina del Técnico Administrativo y no distingue roles. También se actualizó el paso 9 de la skill (decía que la verificación era manual) y la nota de BeanConfiguracion sobre la dependencia entre beans hacia UsuarioCUIntPuerto. Sin cambios en Java ni en SQL ejecutable.

## 2026-10-05 - Prueba de seguridad de @PreAuthorize (Tarea 1)
- Qué se hizo: script de humo que crea un usuario con rol Funcionario y comprueba si puede listar usuarios; funciones comunes de sesión y llamadas a la API en un archivo aparte.
- Archivos: `backend/pruebas/comun.ps1` (creado), `backend/pruebas/t1_preauthorize.ps1` (creado)
- Notas: resultado PREAUTHORIZE INERTE. Un Funcionario recibe 200 en GET usuarios, usuarios/paginado y usuarios/funcionarios, porque no hay @EnableMethodSecurity y ConfiguracionSeguridad deja GET usuarios/** en authenticated(). Los .ps1 se mantienen en ASCII: PowerShell 5.1 lee los archivos sin BOM como ANSI, por eso las tildes se arman con [char] en comun.ps1. No se cambió código Java.
