. "$PSScriptRoot\comun.ps1"

$rutas = @(
    "usuarios",
    "usuarios/paginado?pagina=0&tamanio=5",
    "usuarios/filtro?nombreCompleto=root&pagina=0&tamanio=5",
    "usuarios/funcionarios",
    "usuarios/tipos"
)

$tokenRoot = Iniciar-Sesion -Usuario "rootfiet" -Clave "rootfiet1234"

$sufijo = Get-Date -Format "yyyyMMddHHmmssfff"
$username = "func$sufijo"
$clave = "Clave12345"
$nuevo = @{
    nombres           = "Prueba"
    apellidos         = "Lectura $sufijo"
    estado            = $true
    tipoDocumento     = $Global:CedulaCiudadania
    numeroDocumento   = "9$sufijo"
    telefono          = "+573000000000"
    correoElectronico = "$username@unicauca.edu.co"
    username          = $username
    password          = $clave
    objTipoUsuario    = @{
        uuidTipoUsuario = "79105584-1091-4a4e-ba8e-9cbdd1c85b91"
        nombre          = "Empleado FIET - Funcionario"
    }
    roles             = @(
        @{
            uuidRol     = "48e3a5ed-709a-4dcb-9434-3bcb43ef9413"
            nombre      = "Funcionario"
            descripcion = "Empleado de la FIET"
            estado      = $true
        }
    )
}
$creacion = Invoke-Api -Metodo POST -Ruta "usuarios?tipoUsuario=FUNCIONARIO" -Token $tokenRoot -Cuerpo $nuevo
Escribir-Resultado "root crea el Funcionario de prueba (200, obtuvo $($creacion.Status))" ($creacion.Status -eq 200)
if ($creacion.Status -ne 200) { Write-Host $creacion.Body; exit 1 }
$uuidFuncionario = ($creacion.Body | ConvertFrom-Json).uuidUsuario
$tokenFuncionario = Iniciar-Sesion -Usuario $username -Clave $clave

foreach ($ruta in $rutas) {
    $r = Invoke-Api -Metodo GET -Ruta $ruta -Token $tokenRoot
    Escribir-Resultado "root GET $ruta (200, obtuvo $($r.Status))" ($r.Status -eq 200)
}

foreach ($ruta in $rutas) {
    $r = Invoke-Api -Metodo GET -Ruta $ruta -Token $tokenFuncionario
    Escribir-Resultado "Funcionario GET $ruta (403, obtuvo $($r.Status))" ($r.Status -eq 403)
}

$r = Invoke-Api -Metodo GET -Ruta "usuarios/$uuidFuncionario" -Token $tokenFuncionario
Escribir-Resultado "Funcionario GET usuarios/{su uuid} (200, obtuvo $($r.Status))" ($r.Status -eq 200)

$r = Invoke-Api -Metodo GET -Ruta "usuarios/funcionarios/" -Token $tokenFuncionario
Escribir-Resultado "Funcionario GET usuarios/funcionarios/ con barra final no da 200 (obtuvo $($r.Status))" ($r.Status -ne 200)

foreach ($ruta in $rutas) {
    $r = Invoke-Api -Metodo GET -Ruta $ruta
    Escribir-Resultado "Sin sesion GET $ruta (401, obtuvo $($r.Status))" ($r.Status -eq 401)
}

Write-Host "Funcionario de prueba: usuario $username, contrasena $clave"
