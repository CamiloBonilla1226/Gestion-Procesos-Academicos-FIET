. "$PSScriptRoot\comun.ps1"

$filas = @(
    @("nombres", "apellidos", "tipoDocumento", "numeroDocumento", "telefono", "correoElectronico", "username", "password", "dependencia"),
    @("Marta Lucia", "Rojas Diaz", $Global:CedulaCiudadania, "1061900001", "3011234567", "mrojas@unicauca.edu.co", "mrojas", "Clave12345", "Division de Admisiones, Registro y Control Academico"),
    @("Jorge Ivan", "Munoz Paz", $Global:CedulaCiudadania, "1061900002", "3017654321", "jmunoz@unicauca.edu.co", "jmunoz", "Clave12345", "Secretaria General FIET")
)

$ruta = Join-Path $PSScriptRoot "plantilla-funcionarios-academicos.xlsx"
Nuevo-ArchivoXlsx -Ruta $ruta -Filas $filas
Write-Host "Plantilla generada en $ruta"
