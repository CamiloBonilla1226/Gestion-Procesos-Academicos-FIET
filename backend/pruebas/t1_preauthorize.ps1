. "$PSScriptRoot\comun.ps1"

$tokenRoot = Iniciar-Sesion -Usuario "rootfiet" -Clave "rootfiet1234"
Write-Host "Sesion root iniciada"
$inicio = Inicio-Corrida
$fotoInicial = Foto-Base
$usernames = @()

try {
    $sufijo = Get-Date -Format "yyyyMMddHHmmssfff"
    $username = "func$sufijo"
    $clave = "Clave12345"

    $nuevo = @{
        nombres           = "Prueba"
        apellidos         = "Funcionario $sufijo"
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

    $usernames += $username
    $creacion = Invoke-Api -Metodo POST -Ruta "usuarios?tipoUsuario=FUNCIONARIO" -Token $tokenRoot -Cuerpo $nuevo
    Write-Host "POST usuarios -> $($creacion.Status)"
    if ($creacion.Status -ne 200) {
        Write-Host $creacion.Body
        throw "No se pudo crear el usuario Funcionario"
    }
    Write-Host "Usuario creado: $username"

    $tokenFuncionario = Iniciar-Sesion -Usuario $username -Clave $clave
    Write-Host "Sesion del Funcionario iniciada"

    $rutas = @("usuarios", "usuarios/paginado?pagina=0&tamanio=5", "usuarios/funcionarios")
    $codigos = @()
    foreach ($ruta in $rutas) {
        $r = Invoke-Api -Metodo GET -Ruta $ruta -Token $tokenFuncionario
        Write-Host "GET $ruta -> $($r.Status)"
        $codigos += $r.Status
    }

    if ($codigos -contains 200) {
        Write-Host "PREAUTHORIZE INERTE"
    } elseif (($codigos | Where-Object { $_ -ne 403 }).Count -eq 0) {
        Write-Host "Listados cerrados por las reglas de ConfiguracionSeguridad"
    } else {
        Write-Host "RESULTADO NO CONCLUYENTE: $($codigos -join ', ')"
    }
}
finally {
    $borrados = Limpiar-DatosPrueba -Desde $inicio -Usernames $usernames
    $fotoFinal = Foto-Base
    Escribir-Resultado "la base quedo como al inicio salvo los logs de rootfiet (usuarios borrados $borrados; $fotoFinal)" ($fotoFinal -eq $fotoInicial)
}
