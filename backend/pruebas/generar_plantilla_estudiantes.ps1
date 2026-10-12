. "$PSScriptRoot\comun.ps1"

$sistemas = "Ingenier$([char]0x00ED)a de Sistemas"
$electronica = "Ingenier$([char]0x00ED)a Electr$([char]0x00F3)nica y Telecomunicaciones"
$encabezado = @("nombres", "apellidos", "tipoDocumento", "numeroDocumento", "telefono", "correoElectronico", "username", "password", "codigoEstudiantil", "programaAcademico", "semestre", "codigoAsignatura", "grupo")
$ana = @("Ana Maria", "Gomez Ruiz", $Global:CedulaCiudadania, "1061000001", "3001234567", "anagomez@unicauca.edu.co", "anagomez", "Clave12345", "104619010001", $sistemas, "5")
$luis = @("Luis Carlos", "Perez Mora", $Global:CedulaCiudadania, "1061000002", "3007654321", "luisperez@unicauca.edu.co", "luisperez", "Clave12345", "104619010002", $electronica, "3")

$filas = @(
    $encabezado,
    ($ana + @("ISI-001", "A")),
    ($ana + @("ISI-002", "B")),
    ($luis + @("IET-001", "A"))
)

$ruta = Join-Path $PSScriptRoot "plantilla-estudiantes.xlsx"
Nuevo-ArchivoXlsx -Ruta $ruta -Filas $filas
Write-Host "Plantilla generada en $ruta"
