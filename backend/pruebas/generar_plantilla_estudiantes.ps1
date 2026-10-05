. "$PSScriptRoot\comun.ps1"

$encabezado = @("nombres", "apellidos", "tipoDocumento", "numeroDocumento", "telefono", "correoElectronico", "username", "password", "codigoEstudiantil", "programaAcademico", "semestre", "facultad", "codigoAsignatura", "nombreAsignatura", "grupo")
$ana = @("Ana Maria", "Gomez Ruiz", $Global:CedulaCiudadania, "1061000001", "3001234567", "anagomez@unicauca.edu.co", "anagomez", "Clave12345", "104619010001", "Ingenieria de Sistemas", "5", "FIET")
$luis = @("Luis Carlos", "Perez Mora", $Global:CedulaCiudadania, "1061000002", "3007654321", "luisperez@unicauca.edu.co", "luisperez", "Clave12345", "104619010002", "Ingenieria Electronica", "3", "FIET")

$filas = @(
    $encabezado,
    ($ana + @("SIS101", "Introduccion a la Informatica", "A")),
    ($ana + @("SIS102", "Programacion Orientada a Objetos", "B")),
    ($luis + @("ELE201", "Circuitos Electricos", "A"))
)

$ruta = Join-Path $PSScriptRoot "plantilla-estudiantes.xlsx"
Nuevo-ArchivoXlsx -Ruta $ruta -Filas $filas
Write-Host "Plantilla generada en $ruta"
