. "$PSScriptRoot\comun.ps1"

$filas = @(
    @("nombres", "apellidos", "tipoDocumento", "numeroDocumento", "telefono", "correoElectronico", "username", "password", "cargo"),
    @("Marta Lucia", "Rojas Diaz", $Global:CedulaCiudadania, "1061900001", "3011234567", "mrojas@unicauca.edu.co", "mrojas", "Clave12345", "Tecnico Administrativo de Procesos Academicos"),
    @("Jorge Ivan", "Munoz Paz", $Global:CedulaCiudadania, "1061900002", "3017654321", "jmunoz@unicauca.edu.co", "jmunoz", "Clave12345", "Profesional de Apoyo Academico")
)

$ruta = Join-Path $PSScriptRoot "plantilla-funcionarios-academicos.xlsx"
Nuevo-ArchivoXlsx -Ruta $ruta -Filas $filas
Write-Host "Plantilla generada en $ruta"
