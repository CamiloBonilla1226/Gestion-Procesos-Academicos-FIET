# Registro de cambios

La entrada más reciente va primero. Formato y reglas en `CLAUDE.md`.

## 2026-10-05 - Prueba de seguridad de @PreAuthorize (Tarea 1)
- Qué se hizo: script de humo que crea un usuario con rol Funcionario y comprueba si puede listar usuarios; funciones comunes de sesión y llamadas a la API en un archivo aparte.
- Archivos: `backend/pruebas/comun.ps1` (creado), `backend/pruebas/t1_preauthorize.ps1` (creado)
- Notas: resultado PREAUTHORIZE INERTE. Un Funcionario recibe 200 en GET usuarios, usuarios/paginado y usuarios/funcionarios, porque no hay @EnableMethodSecurity y ConfiguracionSeguridad deja GET usuarios/** en authenticated(). Los .ps1 se mantienen en ASCII: PowerShell 5.1 lee los archivos sin BOM como ANSI, por eso las tildes se arman con [char] en comun.ps1. No se cambió código Java.
