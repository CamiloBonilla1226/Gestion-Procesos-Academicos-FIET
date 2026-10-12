. "$PSScriptRoot\comun.ps1"

function Nuevo-CuerpoFuncionario {
    param([string]$Sufijo, [string]$Cargo)
    $script:usernames += "fa$Sufijo"
    return @{
        nombres           = "Funcionario"
        apellidos         = "Academico $Sufijo"
        tipoDocumento     = $Global:CedulaCiudadania
        numeroDocumento   = "6$Sufijo"
        telefono          = "+573000000000"
        correoElectronico = "fa$Sufijo@unicauca.edu.co"
        username          = "fa$Sufijo"
        password          = "Clave12345"
        cargo             = $Cargo
    }
}

function Puede-IniciarSesion {
    param([string]$Usuario, [string]$Clave)
    $r = Invoke-Api -Metodo POST -Ruta "sesiones" -Cuerpo @{ username = $Usuario; password = $Clave }
    return ($r.Status -eq 200)
}

$tokenRoot = Iniciar-Sesion -Usuario "rootfiet" -Clave "rootfiet1234"
$inicio = Inicio-Corrida
$fotoInicial = Foto-Base
$usernames = @()
$codigos = @()

try {
    $estudiante = Crear-UsuarioPrueba -Perfil "Estudiante" -TokenAdmin $tokenRoot -Prefijo "est"
    $usernames += $estudiante.Username
    $funcionario = Crear-UsuarioPrueba -Perfil "Funcionario" -TokenAdmin $tokenRoot -Prefijo "func"
    $usernames += $funcionario.Username

    $sufijo = Nuevo-Sufijo
    $cargo = "Dep $sufijo"
    $cuerpo = Nuevo-CuerpoFuncionario -Sufijo $sufijo -Cargo $cargo
    $r = Invoke-Api -Metodo POST -Ruta "funcionarios-academicos" -Token $tokenRoot -Cuerpo $cuerpo
    Escribir-Resultado "root crea funcionario academico (200, obtuvo $($r.Status))" ($r.Status -eq 200)
    if ($r.Status -ne 200) { Write-Host $r.Body; exit 1 }
    $creado = $r.Body | ConvertFrom-Json
    $uuid = $creado.uuidUsuario
    Escribir-Resultado "la respuesta no contiene password" ($r.Body -notmatch '"password"')
    Escribir-Resultado "la respuesta trae el cargo" ($creado.cargo -eq $cargo)

    $tokenFa = Iniciar-Sesion -Usuario $cuerpo.username -Clave $cuerpo.password
    Escribir-Resultado "el funcionario academico creado inicia sesion" ([bool]$tokenFa)

    $r = Invoke-Api -Metodo GET -Ruta "usuarios/$uuid" -Token $tokenRoot
    $usuarioFa = $r.Body | ConvertFrom-Json
    $uuidsRoles = @($usuarioFa.roles | ForEach-Object { $_.uuidRol })
    Escribir-Resultado "tipo de usuario Empleado FIET - Funcionario (obtuvo $($usuarioFa.objTipoUsuario.nombre))" ($usuarioFa.objTipoUsuario.uuidTipoUsuario -eq "79105584-1091-4a4e-ba8e-9cbdd1c85b91")
    Escribir-Resultado "rol unico Funcionario Academico (obtuvo $($uuidsRoles -join ', '))" (($uuidsRoles.Count -eq 1) -and ($uuidsRoles[0] -eq $Global:Perfiles.FuncionarioAcademico.Rol.uuidRol))

    $campos = @(
        @{ Campo = "numeroDocumento"; Descripcion = "documento" },
        @{ Campo = "correoElectronico"; Descripcion = "correo" },
        @{ Campo = "username"; Descripcion = "username" }
    )
    foreach ($c in $campos) {
        $s = Nuevo-Sufijo
        $repetido = Nuevo-CuerpoFuncionario -Sufijo $s -Cargo "No debe crearse"
        $repetido[$c.Campo] = $cuerpo[$c.Campo]
        $r = Invoke-Api -Metodo POST -Ruta "funcionarios-academicos" -Token $tokenRoot -Cuerpo $repetido
        $cod = Obtener-CodigoError $r
        Escribir-Resultado "$($c.Descripcion) repetido (500 codigoError 2, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "2"))
        if ($c.Campo -ne "username") {
            Escribir-Resultado "el username del intento con $($c.Descripcion) repetido no puede iniciar sesion" (-not (Puede-IniciarSesion $repetido.username $repetido.password))
        }
    }
    $r = Invoke-Api -Metodo GET -Ruta "funcionarios-academicos/filtro?cargo=No debe crearse&pagina=0&tamanio=5" -Token $tokenRoot
    $total = if ($r.Status -eq 200) { ($r.Body | ConvertFrom-Json).totalElements } else { -1 }
    Escribir-Resultado "los intentos repetidos no dejaron funcionarios (0, obtuvo $total)" ($total -eq 0)

    $vacio = Nuevo-CuerpoFuncionario -Sufijo (Nuevo-Sufijo) -Cargo ""
    $vacio.nombres = ""
    $vacio.numeroDocumento = ""
    $r = Invoke-Api -Metodo POST -Ruta "funcionarios-academicos" -Token $tokenRoot -Cuerpo $vacio
    Escribir-Resultado "campos vacios (400, obtuvo $($r.Status))" ($r.Status -eq 400)

    $r = Invoke-Api -Metodo GET -Ruta "funcionarios-academicos/paginado?pagina=0&tamanio=5" -Token $tokenRoot
    Escribir-Resultado "root lista paginado (200, obtuvo $($r.Status))" ($r.Status -eq 200)
    Escribir-Resultado "el listado no contiene password" ($r.Body -notmatch '"password"')

    $r = Invoke-Api -Metodo GET -Ruta "funcionarios-academicos/filtro?cargo=$cargo&pagina=0&tamanio=5" -Token $tokenRoot
    $total = if ($r.Status -eq 200) { ($r.Body | ConvertFrom-Json).totalElements } else { -1 }
    Escribir-Resultado "root filtra por cargo (200 y 1, obtuvo $($r.Status) y $total)" (($r.Status -eq 200) -and ($total -eq 1))

    $r = Invoke-Api -Metodo GET -Ruta "funcionarios-academicos/filtro?apellido=Academico $sufijo&pagina=0&tamanio=5" -Token $tokenRoot
    $total = if ($r.Status -eq 200) { ($r.Body | ConvertFrom-Json).totalElements } else { -1 }
    Escribir-Resultado "root filtra por apellido (200 y 1, obtuvo $($r.Status) y $total)" (($r.Status -eq 200) -and ($total -eq 1))

    $r = Invoke-Api -Metodo GET -Ruta "funcionarios-academicos/filtro?nombre=noexiste$sufijo&pagina=0&tamanio=5" -Token $tokenRoot
    $total = if ($r.Status -eq 200) { ($r.Body | ConvertFrom-Json).totalElements } else { -1 }
    Escribir-Resultado "root filtra sin resultados (200 y 0, obtuvo $($r.Status) y $total)" (($r.Status -eq 200) -and ($total -eq 0))

    $r = Invoke-Api -Metodo GET -Ruta "funcionarios-academicos/paginado?pagina=-1&tamanio=5" -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root paginado con pagina negativa (500 codigoError 6, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6"))

    $r = Invoke-Api -Metodo GET -Ruta "funcionarios-academicos/$uuid" -Token $tokenRoot
    Escribir-Resultado "root obtiene por uuid (200, obtuvo $($r.Status))" ($r.Status -eq 200)

    $inexistente = [guid]::NewGuid().ToString()
    $r = Invoke-Api -Metodo GET -Ruta "funcionarios-academicos/$inexistente" -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root obtiene uuid inexistente (500 codigoError 3, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "3"))

    $nuevoCargo = "Dep editada $sufijo"
    $r = Invoke-Api -Metodo PUT -Ruta "funcionarios-academicos/$uuid" -Token $tokenRoot -Cuerpo @{ cargo = $nuevoCargo }
    $dep = if ($r.Status -eq 200) { ($r.Body | ConvertFrom-Json).cargo } else { "" }
    Escribir-Resultado "root edita el cargo (200 y nuevo cargo, obtuvo $($r.Status))" (($r.Status -eq 200) -and ($dep -eq $nuevoCargo))

    $r = Invoke-Api -Metodo PUT -Ruta "funcionarios-academicos/$uuid" -Token $tokenRoot -Cuerpo @{ cargo = "" }
    $dep = if ($r.Status -eq 200) { ($r.Body | ConvertFrom-Json).cargo } else { "" }
    Escribir-Resultado "root edita con cargo vacio y el cargo no cambia (200, obtuvo $($r.Status) y $dep)" (($r.Status -eq 200) -and ($dep -eq $nuevoCargo))

    $r = Invoke-Api -Metodo PUT -Ruta "funcionarios-academicos/$uuid" -Token $tokenRoot -Cuerpo @{ nombres = "Editado"; apellidos = "Funcionario $sufijo"; telefono = "3110000000"; numeroDocumento = $cuerpo.numeroDocumento; correoElectronico = $cuerpo.correoElectronico; username = "otro$sufijo" }
    $editado = if ($r.Status -eq 200) { Leer-Json $r } else { $null }
    Escribir-Resultado "root edita datos personales con su propio documento y correo (200, obtuvo $($r.Status))" (($r.Status -eq 200) -and ($editado.nombres -eq "Editado") -and ($editado.apellidos -eq "Funcionario $sufijo") -and ($editado.telefono -eq "3110000000") -and ($editado.cargo -eq $nuevoCargo))
    Escribir-Resultado "el username no cambia al editar (obtuvo $($editado.username))" ($editado.username -eq $cuerpo.username)

    $nuevoDocumento = "7$sufijo"
    $nuevoCorreo = "editado$sufijo@unicauca.edu.co"
    $r = Invoke-Api -Metodo PUT -Ruta "funcionarios-academicos/$uuid" -Token $tokenRoot -Cuerpo @{ numeroDocumento = $nuevoDocumento; correoElectronico = $nuevoCorreo; tipoDocumento = "Tarjeta de identidad"; nombres = "" }
    $editado = if ($r.Status -eq 200) { Leer-Json $r } else { $null }
    Escribir-Resultado "root cambia documento, correo y tipo de documento; el nombre vacio no cambia (200, obtuvo $($r.Status))" (($r.Status -eq 200) -and ($editado.numeroDocumento -eq $nuevoDocumento) -and ($editado.correoElectronico -eq $nuevoCorreo) -and ($editado.tipoDocumento -eq "Tarjeta de identidad") -and ($editado.nombres -eq "Editado"))
    $enBase = Consultar-Base "select concat(l.nombres, '|', u.numeroDocumento, '|', u.correoElectronico) from usuarios u join usuariosLivianos l on l.uuidUsuario = u.uuidUsuario where u.uuidUsuario = '$uuid'"
    Escribir-Resultado "los datos personales quedaron en usuarios y usuariosLivianos (obtuvo $enBase)" ($enBase -eq "Editado|$nuevoDocumento|$nuevoCorreo")

    $documentoRoot = Consultar-Base "select numeroDocumento from usuarios where username = 'rootfiet'"
    $correoRoot = Consultar-Base "select correoElectronico from usuarios where username = 'rootfiet'"
    $r = Invoke-Api -Metodo PUT -Ruta "funcionarios-academicos/$uuid" -Token $tokenRoot -Cuerpo @{ numeroDocumento = $documentoRoot }
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root edita con el documento de otro usuario (500 codigoError 2, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "2"))
    $r = Invoke-Api -Metodo PUT -Ruta "funcionarios-academicos/$uuid" -Token $tokenRoot -Cuerpo @{ correoElectronico = $correoRoot }
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root edita con el correo de otro usuario (500 codigoError 2, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "2"))
    $r = Invoke-Api -Metodo PUT -Ruta "funcionarios-academicos/$uuid" -Token $tokenRoot -Cuerpo @{ tipoDocumento = "Pasaporte extranjero" }
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root edita con un tipo de documento invalido (500 codigoError 6, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6"))
    $r = Invoke-Api -Metodo PUT -Ruta "funcionarios-academicos/$uuid" -Token $tokenRoot -Cuerpo @{ correoElectronico = "no-es-correo" }
    Escribir-Resultado "root edita con un correo mal formado (400, obtuvo $($r.Status))" ($r.Status -eq 400)
    $enBase = Consultar-Base "select concat(u.numeroDocumento, '|', u.correoElectronico, '|', u.tipoDocumento) from usuarios u where u.uuidUsuario = '$uuid'"
    Escribir-Resultado "los intentos rechazados no cambiaron los datos (obtuvo $enBase)" ($enBase -eq "$nuevoDocumento|$nuevoCorreo|Tarjeta de identidad")
    $login = Invoke-Api -Metodo POST -Ruta "sesiones" -Cuerpo @{ username = $cuerpo.username; password = $cuerpo.password }
    Escribir-Resultado "el funcionario editado sigue iniciando sesion con su username (obtuvo $($login.Status))" ($login.Status -eq 200)

    $r = Invoke-Api -Metodo PUT -Ruta "funcionarios-academicos/$inexistente" -Token $tokenRoot -Cuerpo @{ cargo = "X" }
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root edita uuid inexistente (500 codigoError 3, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "3"))

    $r = Invoke-Api -Metodo DELETE -Ruta "funcionarios-academicos/$uuid" -Token $tokenRoot
    Escribir-Resultado "root DELETE funcionarios-academicos/{uuid} queda denegado (403, obtuvo $($r.Status))" ($r.Status -eq 403)

    $archivo = Join-Path ([System.IO.Path]::GetTempPath()) ("t4_permisos_" + (Nuevo-Sufijo) + ".xlsx")
    $sArchivo = Nuevo-Sufijo
    $usernames += "fa$sArchivo"
    Nuevo-ArchivoXlsx -Ruta $archivo -Filas @(
        @("nombres", "apellidos", "tipoDocumento", "numeroDocumento", "telefono", "correoElectronico", "username", "password", "cargo"),
        @("No", "Debe", $Global:CedulaCiudadania, "6$sArchivo", "3000000000", "fa$sArchivo@unicauca.edu.co", "fa$sArchivo", "Clave12345", "Dep permisos $sArchivo")
    )

    $lecturas = @("funcionarios-academicos/paginado?pagina=0&tamanio=5", "funcionarios-academicos/filtro?cargo=Dep&pagina=0&tamanio=5", "funcionarios-academicos/$uuid")
    $escrituras = @(
        @{ Metodo = "POST"; Ruta = "funcionarios-academicos"; Cuerpo = (Nuevo-CuerpoFuncionario -Sufijo (Nuevo-Sufijo) -Cargo "No debe crearse") },
        @{ Metodo = "PUT"; Ruta = "funcionarios-academicos/$uuid"; Cuerpo = @{ cargo = "No debe editarse" } }
    )

    function Probar-Perfil {
        param([string]$Nombre, [string]$Token, [int]$EsperadoLectura, [int]$EsperadoEscritura)
        foreach ($ruta in $lecturas) {
            $r = Invoke-Api -Metodo GET -Ruta $ruta -Token $Token
            Escribir-Resultado "$Nombre GET $ruta ($EsperadoLectura, obtuvo $($r.Status))" ($r.Status -eq $EsperadoLectura)
        }
        foreach ($e in $escrituras) {
            $r = Invoke-Api -Metodo $e.Metodo -Ruta $e.Ruta -Token $Token -Cuerpo $e.Cuerpo
            Escribir-Resultado "$Nombre $($e.Metodo) $($e.Ruta) ($EsperadoEscritura, obtuvo $($r.Status))" ($r.Status -eq $EsperadoEscritura)
        }
        $r = Subir-Archivo -Ruta "funcionarios-academicos/cargar/archivo" -Archivo $archivo -Token $Token
        Escribir-Resultado "$Nombre POST funcionarios-academicos/cargar/archivo ($EsperadoEscritura, obtuvo $($r.Status))" ($r.Status -eq $EsperadoEscritura)
    }

    Probar-Perfil -Nombre "Funcionario Academico creado" -Token $tokenFa -EsperadoLectura 200 -EsperadoEscritura 403
    Probar-Perfil -Nombre "Estudiante" -Token $estudiante.Token -EsperadoLectura 403 -EsperadoEscritura 403
    Probar-Perfil -Nombre "Funcionario" -Token $funcionario.Token -EsperadoLectura 403 -EsperadoEscritura 403
    Probar-Perfil -Nombre "Sin sesion" -Token $null -EsperadoLectura 401 -EsperadoEscritura 401
    Remove-Item $archivo -Force

    $r = Invoke-Api -Metodo GET -Ruta "funcionarios-academicos/$uuid" -Token $tokenRoot
    $dep = if ($r.Status -eq 200) { ($r.Body | ConvertFrom-Json).cargo } else { "" }
    Escribir-Resultado "las escrituras denegadas no cambiaron el cargo" ($dep -eq $nuevoCargo)
    $r = Invoke-Api -Metodo GET -Ruta "funcionarios-academicos/filtro?cargo=Dep permisos $sArchivo&pagina=0&tamanio=5" -Token $tokenRoot
    $total = if ($r.Status -eq 200) { ($r.Body | ConvertFrom-Json).totalElements } else { -1 }
    Escribir-Resultado "las cargas denegadas no crearon funcionarios (0, obtuvo $total)" ($total -eq 0)
}
finally {
    $borrados = Limpiar-DatosPrueba -Desde $inicio -Usernames $usernames -CodigosAsignatura $codigos
    $fotoFinal = Foto-Base
    Escribir-Resultado "la base quedo como al inicio salvo los logs de rootfiet (usuarios borrados $borrados; $fotoFinal)" ($fotoFinal -eq $fotoInicial)
}
