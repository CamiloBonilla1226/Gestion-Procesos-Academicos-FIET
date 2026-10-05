. "$PSScriptRoot\comun.ps1"

$tokenRoot = Iniciar-Sesion -Usuario "rootfiet" -Clave "rootfiet1234"
$estudiante = Crear-UsuarioPrueba -Perfil "Estudiante" -TokenAdmin $tokenRoot -Prefijo "est"
$funcAcad = Crear-UsuarioPrueba -Perfil "FuncionarioAcademico" -TokenAdmin $tokenRoot -Prefijo "facad"
$funcionario = Crear-UsuarioPrueba -Perfil "Funcionario" -TokenAdmin $tokenRoot -Prefijo "func"

$r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/tipos-solicitud" -Token $tokenRoot
Escribir-Resultado "root lista tipos de solicitud (200, obtuvo $($r.Status))" ($r.Status -eq 200)
$tipos = @()
if ($r.Status -eq 200) { $lista = $r.Body | ConvertFrom-Json; $tipos = @($lista) }
Write-Host "  tipos de solicitud registrados: $($tipos.Count)"

$inexistente = [guid]::NewGuid().ToString()
foreach ($sub in @("etapas", "tipos-anexo")) {
    $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/tipos-solicitud/$inexistente/$sub" -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "$sub de un tipo inexistente (500 codigoError 3, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "3"))
}

if ($tipos.Count -gt 0) {
    $uuidTipo = $tipos[0].uuidTipoSolicitudAcademica
    foreach ($sub in @("etapas", "tipos-anexo")) {
        $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/tipos-solicitud/$uuidTipo/$sub" -Token $tokenRoot
        Escribir-Resultado "$sub de un tipo existente (200, obtuvo $($r.Status))" ($r.Status -eq 200)
    }
}

foreach ($rol in @("ESTUDIANTE", "funcionario", "Decano")) {
    $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/etiquetas?rol=$rol" -Token $tokenRoot
    Escribir-Resultado "etiquetas del rol $rol (200, obtuvo $($r.Status))" ($r.Status -eq 200)
}
$r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/etiquetas?rol=SECRETARIO" -Token $tokenRoot
$cod = Obtener-CodigoError $r
Escribir-Resultado "etiquetas de un rol no valido (500 codigoError 6, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6"))

$r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/situaciones" -Token $tokenRoot
Escribir-Resultado "root lista situaciones (200, obtuvo $($r.Status))" ($r.Status -eq 200)

$rutas = @("catalogos-academicos/tipos-solicitud", "catalogos-academicos/etiquetas?rol=ESTUDIANTE", "catalogos-academicos/situaciones")
$perfiles = @(
    @{ Nombre = "Estudiante"; Token = $estudiante.Token },
    @{ Nombre = "Funcionario Academico"; Token = $funcAcad.Token },
    @{ Nombre = "Funcionario"; Token = $funcionario.Token }
)
foreach ($p in $perfiles) {
    foreach ($ruta in $rutas) {
        $r = Invoke-Api -Metodo GET -Ruta $ruta -Token $p.Token
        Escribir-Resultado "$($p.Nombre) GET $ruta (200, obtuvo $($r.Status))" ($r.Status -eq 200)
    }
}
foreach ($ruta in $rutas) {
    $r = Invoke-Api -Metodo GET -Ruta $ruta
    Escribir-Resultado "Sin sesion GET $ruta (401, obtuvo $($r.Status))" ($r.Status -eq 401)
}

$r = Invoke-Api -Metodo POST -Ruta "catalogos-academicos/tipos-solicitud" -Token $tokenRoot -Cuerpo @{ nombre = "No" }
Escribir-Resultado "root POST catalogos-academicos queda denegado (403, obtuvo $($r.Status))" ($r.Status -eq 403)
$r = Invoke-Api -Metodo DELETE -Ruta "catalogos-academicos/situaciones" -Token $tokenRoot
Escribir-Resultado "root DELETE catalogos-academicos queda denegado (403, obtuvo $($r.Status))" ($r.Status -eq 403)
