. "$PSScriptRoot\comun.ps1"

function Nuevo-FuncionarioAcademico {
    param([string]$Token)
    $s = Nuevo-Sufijo
    $cuerpo = @{
        nombres           = "Asignacion"
        apellidos         = "Prueba $s"
        tipoDocumento     = $Global:CedulaCiudadania
        numeroDocumento   = "7$s"
        telefono          = "+573000000000"
        correoElectronico = "asig$s@unicauca.edu.co"
        username          = "asig$s"
        password          = "Clave12345"
        dependencia       = "DepAsig $s"
    }
    $script:usernames += $cuerpo.username
    $r = Invoke-Api -Metodo POST -Ruta "funcionarios-academicos" -Token $Token -Cuerpo $cuerpo
    if ($r.Status -ne 200) { throw "No se pudo crear el funcionario academico ($($r.Status)): $($r.Body)" }
    return (Leer-Json $r)
}

function Asignar {
    param([string]$UuidTipo, $Cuerpo, [string]$Token)
    return Invoke-Api -Metodo PUT -Ruta "catalogos-academicos/tipos-solicitud/$UuidTipo/funcionario" -Token $Token -Cuerpo $Cuerpo
}

function Tipos-DeFuncionario {
    param([string]$Uuid, [string]$Token)
    $r = Invoke-Api -Metodo GET -Ruta "funcionarios-academicos/$Uuid" -Token $Token
    $f = Leer-Json $r
    return @($f.tiposSolicitud | ForEach-Object { $_.uuidTipoSolicitudAcademica })
}

function Responsables-Actuales {
    param([string]$Token)
    $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/tipos-solicitud" -Token $Token
    $lista = Leer-Json $r
    $mapa = @{}
    foreach ($t in @($lista)) { $mapa[$t.uuidTipoSolicitudAcademica] = $t.uuidFuncionarioAcademico }
    return $mapa
}

$tokenRoot = Iniciar-Sesion -Usuario "rootfiet" -Clave "rootfiet1234"
$originales = Responsables-Actuales -Token $tokenRoot
if ($originales.Count -eq 0) { Write-Host "No hay tipos de solicitud sembrados; corre docs/database/seed-procesos-academicos.sql"; exit 1 }
$uuidTipo = @($originales.Keys | Sort-Object)[0]
$inicio = Inicio-Corrida
$fotoInicial = Foto-Base
$usernames = @()

try {
    $faA = Nuevo-FuncionarioAcademico -Token $tokenRoot
    $faB = Nuevo-FuncionarioAcademico -Token $tokenRoot
    $estudiante = Crear-UsuarioPrueba -Perfil "Estudiante" -TokenAdmin $tokenRoot -Prefijo "est"
    $usernames += $estudiante.Username
    $funcAcad = Crear-UsuarioPrueba -Perfil "FuncionarioAcademico" -TokenAdmin $tokenRoot -Prefijo "facad"
    $usernames += $funcAcad.Username
    $funcionario = Crear-UsuarioPrueba -Perfil "Funcionario" -TokenAdmin $tokenRoot -Prefijo "func"
    $usernames += $funcionario.Username
    $decano = Crear-UsuarioPrueba -Perfil "Decano" -TokenAdmin $tokenRoot -Prefijo "dec"
    $usernames += $decano.Username

    Escribir-Resultado "un funcionario recien creado no atiende tipos (obtuvo $(@(Tipos-DeFuncionario $faA.uuidUsuario $tokenRoot).Count))" (@(Tipos-DeFuncionario $faA.uuidUsuario $tokenRoot).Count -eq 0)

    $r = Asignar -UuidTipo $uuidTipo -Cuerpo @{ funcionarioUuid = $faA.uuidUsuario } -Token $tokenRoot
    $resp = Leer-Json $r
    Escribir-Resultado "root asigna el tipo al funcionario A (200, obtuvo $($r.Status))" (($r.Status -eq 200) -and ($resp.uuidFuncionarioAcademico -eq $faA.uuidUsuario))
    Escribir-Resultado "A lista el tipo asignado en su consulta por uuid" ((Tipos-DeFuncionario $faA.uuidUsuario $tokenRoot) -contains $uuidTipo)

    $r = Asignar -UuidTipo $uuidTipo -Cuerpo @{ funcionarioUuid = $faB.uuidUsuario } -Token $decano.Token
    $resp = Leer-Json $r
    Escribir-Resultado "Decano reasigna el tipo al funcionario B (200, obtuvo $($r.Status))" (($r.Status -eq 200) -and ($resp.uuidFuncionarioAcademico -eq $faB.uuidUsuario))
    Escribir-Resultado "B lista el tipo reasignado" ((Tipos-DeFuncionario $faB.uuidUsuario $tokenRoot) -contains $uuidTipo)
    Escribir-Resultado "A ya no lista el tipo" (-not ((Tipos-DeFuncionario $faA.uuidUsuario $tokenRoot) -contains $uuidTipo))
    Escribir-Resultado "el catalogo muestra a B como responsable" ((Responsables-Actuales -Token $tokenRoot)[$uuidTipo] -eq $faB.uuidUsuario)

    $r = Invoke-Api -Metodo GET -Ruta "funcionarios-academicos/filtro?dependencia=$($faB.dependencia)&pagina=0&tamanio=5" -Token $tokenRoot
    $pagina = Leer-Json $r
    $enFiltro = @($pagina.content | Where-Object { $_.uuidUsuario -eq $faB.uuidUsuario })
    $tiposFiltro = @($enFiltro | ForEach-Object { $_.tiposSolicitud } | ForEach-Object { $_.uuidTipoSolicitudAcademica })
    Escribir-Resultado "el filtro de funcionarios trae los tipos de B" (($enFiltro.Count -eq 1) -and ($tiposFiltro -contains $uuidTipo))
    $conNombre = @($enFiltro | ForEach-Object { $_.tiposSolicitud } | Where-Object { $_.uuidTipoSolicitudAcademica -eq $uuidTipo -and $_.nombre })
    Escribir-Resultado "cada tipo listado trae uuid y nombre" ($conNombre.Count -eq 1)

    $r = Invoke-Api -Metodo GET -Ruta "funcionarios-academicos/paginado?pagina=0&tamanio=1000" -Token $tokenRoot
    $pagina = Leer-Json $r
    $sinLista = @($pagina.content | Where-Object { $null -eq $_.tiposSolicitud })
    $enPaginado = @($pagina.content | Where-Object { $_.uuidUsuario -eq $faB.uuidUsuario } | ForEach-Object { $_.tiposSolicitud } | ForEach-Object { $_.uuidTipoSolicitudAcademica })
    Escribir-Resultado "el paginado trae tiposSolicitud en todos los funcionarios y los de B" (($sinLista.Count -eq 0) -and ($enPaginado -contains $uuidTipo))

    $inexistente = [guid]::NewGuid().ToString()
    $r = Asignar -UuidTipo $inexistente -Cuerpo @{ funcionarioUuid = $faA.uuidUsuario } -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "tipo inexistente (500 codigoError 3, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "3"))
    $r = Asignar -UuidTipo $uuidTipo -Cuerpo @{ funcionarioUuid = $inexistente } -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "funcionario inexistente (500 codigoError 3, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "3"))
    foreach ($otro in @(@{ Nombre = "Estudiante"; Uuid = $estudiante.Uuid }, @{ Nombre = "usuario con rol Funcionario Academico sin fila"; Uuid = $funcAcad.Uuid })) {
        $r = Asignar -UuidTipo $uuidTipo -Cuerpo @{ funcionarioUuid = $otro.Uuid } -Token $tokenRoot
        $cod = Obtener-CodigoError $r
        Escribir-Resultado "uuid de $($otro.Nombre) no es funcionario academico (500 codigoError 3, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "3"))
    }
    $r = Asignar -UuidTipo $uuidTipo -Cuerpo @{ funcionarioUuid = "" } -Token $tokenRoot
    Escribir-Resultado "funcionarioUuid vacio (400, obtuvo $($r.Status))" ($r.Status -eq 400)
    Escribir-Resultado "los intentos fallidos no cambiaron el responsable" ((Responsables-Actuales -Token $tokenRoot)[$uuidTipo] -eq $faB.uuidUsuario)

    foreach ($p in @(@{ Nombre = "Estudiante"; Token = $estudiante.Token }, @{ Nombre = "Funcionario Academico"; Token = $funcAcad.Token }, @{ Nombre = "Funcionario"; Token = $funcionario.Token })) {
        $r = Asignar -UuidTipo $uuidTipo -Cuerpo @{ funcionarioUuid = $faA.uuidUsuario } -Token $p.Token
        Escribir-Resultado "$($p.Nombre) asigna (403, obtuvo $($r.Status))" ($r.Status -eq 403)
    }
    $r = Asignar -UuidTipo $uuidTipo -Cuerpo @{ funcionarioUuid = $faA.uuidUsuario }
    Escribir-Resultado "Sin sesion asigna (401, obtuvo $($r.Status))" ($r.Status -eq 401)
    Escribir-Resultado "los intentos sin permiso no cambiaron el responsable" ((Responsables-Actuales -Token $tokenRoot)[$uuidTipo] -eq $faB.uuidUsuario)
}
finally {
    foreach ($uuid in $originales.Keys) {
        $r = Asignar -UuidTipo $uuid -Cuerpo @{ funcionarioUuid = $originales[$uuid] } -Token $tokenRoot
        if ($r.Status -ne 200) { Write-Host "No se pudo restaurar el tipo ${uuid}: $($r.Status) $($r.Body)" }
    }
    $finales = Responsables-Actuales -Token $tokenRoot
    $distintos = @($originales.Keys | Where-Object { $finales[$_] -ne $originales[$_] })
    Escribir-Resultado "los tipos quedaron con su responsable original (distintos: $($distintos.Count))" ($distintos.Count -eq 0)
    $borrados = Limpiar-DatosPrueba -Desde $inicio -Usernames $usernames
    $fotoFinal = Foto-Base
    Escribir-Resultado "la base quedo como al inicio salvo los logs de rootfiet (usuarios borrados $borrados; $fotoFinal)" ($fotoFinal -eq $fotoInicial)
}
