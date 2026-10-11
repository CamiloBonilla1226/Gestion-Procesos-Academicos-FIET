. "$PSScriptRoot\comun.ps1"

$sistemas = "Ingenier$([char]0x00ED)a de Sistemas"
$electronica = "Ingenier$([char]0x00ED)a Electr$([char]0x00F3)nica y Telecomunicaciones"
$telematica = "Tecnolog$([char]0x00ED)a en Telem$([char]0x00E1)tica"

$filas = @(
    @("nombreAsignatura", "programaAcademico"),
    @("Introduccion a la Informatica", $sistemas),
    @("Programacion Orientada a Objetos", $sistemas),
    @("Circuitos Electricos", $electronica),
    @("Redes de Datos I", $telematica)
)

$ruta = Join-Path $PSScriptRoot "plantilla-asignaturas.xlsx"
Nuevo-ArchivoXlsx -Ruta $ruta -Filas $filas
Write-Host "Plantilla generada en $ruta"
