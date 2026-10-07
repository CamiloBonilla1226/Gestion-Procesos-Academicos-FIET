. "$PSScriptRoot\comun.ps1"

$Global:CarpetaBackend = (Resolve-Path "$PSScriptRoot\..").Path
$Global:CarpetaAnexos = Join-Path $Global:CarpetaBackend "uploads\anexos"
$Global:Ruta = "cancelaciones-matricula"

function Ejecutar-Sql {
    param([string]$Sql)
    Push-Location $Global:CarpetaBackend
    try {
        $salida = & docker compose exec -T cfiet_database mysql -u root -pmysql --default-character-set=utf8mb4 cfiet -N -B -e $Sql 2>$null
    } finally {
        Pop-Location
    }
    return $salida
}

function Lista-Sql {
    param([string[]]$Valores)
    return (@($Valores | Where-Object { $_ } | ForEach-Object { "'$_'" }) -join ",")
}

function Nuevo-Estudiante {
    param([string]$Token, [int]$Materias)
    $s = Nuevo-Sufijo
    $asignaturas = @()
    for ($i = 1; $i -le $Materias; $i++) { $asignaturas += @{ codigoAsignatura = "T6$s$i"; nombreAsignatura = "Materia T6 $i $s"; grupo = "A" } }
    $cuerpo = @{
        nombres           = "Cancelante"
        apellidos         = "Prueba $s"
        tipoDocumento     = $Global:CedulaCiudadania
        numeroDocumento   = "7$s"
        telefono          = "+573000000000"
        correoElectronico = "can$s@unicauca.edu.co"
        username          = "can$s"
        password          = "Clave12345"
        codigoEstudiantil = "M$s"
        programaAcademico = "Ingenieria de Sistemas"
        semestre          = "5"
        facultad          = "FIET"
        asignaturas       = $asignaturas
    }
    $r = Invoke-Api -Metodo POST -Ruta "estudiantes" -Token $Token -Cuerpo $cuerpo
    if ($r.Status -ne 200) { throw "No se pudo crear el estudiante ($($r.Status)): $($r.Body)" }
    return [pscustomobject]@{
        Uuid    = (Leer-Json $r).uuidUsuario
        Codigos = @($asignaturas | ForEach-Object { $_.codigoAsignatura })
        Token   = Iniciar-Sesion -Usuario "can$s" -Clave "Clave12345"
    }
}

function Nuevo-Funcionario {
    param([string]$Token)
    $s = Nuevo-Sufijo
    $cuerpo = @{
        nombres           = "Tramitador"
        apellidos         = "Prueba $s"
        tipoDocumento     = $Global:CedulaCiudadania
        numeroDocumento   = "4$s"
        telefono          = "+573000000000"
        correoElectronico = "tra$s@unicauca.edu.co"
        username          = "tra$s"
        password          = "Clave12345"
        dependencia       = "DepT6 $s"
    }
    $r = Invoke-Api -Metodo POST -Ruta "funcionarios-academicos" -Token $Token -Cuerpo $cuerpo
    if ($r.Status -ne 200) { throw "No se pudo crear el funcionario academico ($($r.Status)): $($r.Body)" }
    return [pscustomobject]@{
        Uuid  = (Leer-Json $r).uuidUsuario
        Token = Iniciar-Sesion -Usuario "tra$s" -Clave "Clave12345"
    }
}

function Nuevo-Pdf {
    param([string]$Texto)
    $ruta = Join-Path ([System.IO.Path]::GetTempPath()) ("t6_" + (Nuevo-Sufijo) + ".pdf")
    $contenido = "%PDF-1.4`n1 0 obj << /Type /Catalog >> endobj`n% $Texto`n%%EOF`n"
    [System.IO.File]::WriteAllBytes($ruta, [System.Text.Encoding]::ASCII.GetBytes($contenido))
    return $ruta
}

function Ejecutar-Curl {
    param([string[]]$Argumentos, [string]$Ruta, [string]$Token)
    $salida = [System.IO.Path]::GetTempFileName()
    $todos = @("-s", "-o", $salida, "-w", "%{http_code}", "-X", "POST") + $Argumentos
    if ($Token) { $todos += @("-H", "Authorization: Bearer $Token") }
    $todos += ($Global:BaseUrl + $Ruta)
    $codigo = & curl.exe @todos
    $cuerpo = [System.IO.File]::ReadAllText($salida, [System.Text.Encoding]::UTF8)
    Remove-Item $salida -Force
    return [pscustomobject]@{ Status = [int]$codigo; Body = $cuerpo }
}

function Radicar {
    param([string]$Token, [string[]]$Tipos, [string]$Pdf, [bool]$ConSoporte, [string]$Motivo = "Calamidad domestica de prueba")
    $argumentos = @()
    if ($Motivo) { $argumentos += @("-F", "motivo=$Motivo") }
    foreach ($tipo in $Tipos) { $argumentos += @("-F", "$tipo=@$Pdf;type=application/pdf") }
    if ($ConSoporte) { $argumentos += @("-F", "soporte=@$Pdf;type=application/pdf") }
    return Ejecutar-Curl -Argumentos $argumentos -Ruta $Global:Ruta -Token $Token
}

function Subir-Escaneo {
    param([string]$Solicitud, [string]$Pdf, [string]$Token)
    return Ejecutar-Curl -Argumentos @("-F", "archivo=@$Pdf;type=application/pdf") -Ruta "solicitudes-academicas/$Solicitud/resolucion" -Token $Token
}

function Descargar {
    param([string]$Ruta, [string]$Token)
    $salida = [System.IO.Path]::GetTempFileName()
    $argumentos = @("-s", "-o", $salida, "-w", "%{http_code}", "-X", "GET")
    if ($Token) { $argumentos += @("-H", "Authorization: Bearer $Token") }
    $argumentos += ($Global:BaseUrl + $Ruta)
    $codigo = [int](& curl.exe @argumentos)
    $bytes = [System.IO.File]::ReadAllBytes($salida)
    Remove-Item $salida -Force
    return [pscustomobject]@{ Status = $codigo; Bytes = $bytes }
}

function Mismos-Bytes {
    param([string]$Archivo, [byte[]]$Bytes)
    $original = [System.IO.File]::ReadAllBytes($Archivo)
    if ($null -eq $Bytes -or $original.Length -ne $Bytes.Length) { return $false }
    for ($i = 0; $i -lt $original.Length; $i++) { if ($original[$i] -ne $Bytes[$i]) { return $false } }
    return $true
}

function Firma-Error {
    param($Respuesta, [string]$Uuid)
    try { $e = $Respuesta.Body | ConvertFrom-Json } catch { return "$($Respuesta.Status)|sin cuerpo" }
    return "$($Respuesta.Status)|$($e.codigoError)|$(([string]$e.mensaje).Replace($Uuid, 'X'))"
}

function Etapa-De {
    param([string]$Solicitud)
    return Ejecutar-Sql "select e.codigo from SOLICITUD_ACADEMICA s join ETAPA_SOLICITUD_ACADEMICA e on e.uuidEtapa = s.Etapa_uuid where s.uuidSolicitudAcademica = '$Solicitud'"
}

function Verificar-EtapaHttp {
    param([string]$Solicitud, [string]$Esperada, [string]$Estudiante, [string]$Funcionario, [string]$Decano)
    $lectores = [ordered]@{ estudiante = $Estudiante; funcionario = $Funcionario }
    if ($Esperada -ne "RADICADA") { $lectores["decano"] = $Decano }
    $fallos = @()
    foreach ($rol in $lectores.Keys) {
        $token = $lectores[$rol]
        $proceso = (Leer-Json (Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$Solicitud" -Token $token)).solicitud.etapaCodigo
        $comun = (Leer-Json (Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$Solicitud" -Token $token)).etapaCodigo
        $bandeja = (@(Leer-Json (Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$rol" -Token $token)) | Where-Object { $_.uuidSolicitudAcademica -eq $Solicitud }).etapaCodigo
        $historial = @(Leer-Json (Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$Solicitud/historial" -Token $token))
        $codigos = @($historial | ForEach-Object { $_.etapaCodigo })
        $sinCodigo = @($historial | Where-Object { -not $_.etapaCodigo }).Count
        if (($proceso -ne $Esperada) -or ($comun -ne $Esperada) -or ($bandeja -ne $Esperada) -or ($sinCodigo -gt 0) -or ($codigos -notcontains $Esperada)) {
            $fallos += "$rol=$proceso/$comun/$bandeja/historial $($codigos -join ' ')"
        }
    }
    if ($Esperada -eq "RADICADA") {
        $r = Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$Solicitud" -Token $Decano
        $b = Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/decano" -Token $Decano
        if (($r.Status -eq 200) -or ($r.Body -match "etapaCodigo") -or ($b.Body -match $Solicitud)) { $fallos += "el Decano ve la solicitud RADICADA" }
    }
    $quienes = @($lectores.Keys) -join ", "
    $detalle = if ($fallos.Count -gt 0) { " (" + ($fallos -join "; ") + ")" } else { "" }
    Escribir-Resultado "etapaCodigo $Esperada en detalle, detalle comun, bandeja e historial para $quienes$detalle" ($fallos.Count -eq 0)
}

function Archivos-Subidos {
    if (-not (Test-Path $Global:CarpetaAnexos)) { return 0 }
    return @(Get-ChildItem $Global:CarpetaAnexos -Recurse -File).Count
}

function Conteos {
    param([string]$Estudiante)
    return "sol=" + (Ejecutar-Sql "select count(*) from SOLICITUD_ACADEMICA where Estudiante_uuid = '$Estudiante'") +
            " hist=" + (Ejecutar-Sql "select count(*) from HISTORIAL_SOLICITUD_ACADEMICA") +
            " cm=" + (Ejecutar-Sql "select count(*) from SOLICITUD_CANCELACION_MATRICULA") +
            " asig=" + (Ejecutar-Sql "select count(*) from ASIGNATURA_SOLICITUD_ACADEMICA") +
            " anexos=" + (Ejecutar-Sql "select count(*) from ANEXO_ACADEMICO") +
            " archivos=" + (Archivos-Subidos)
}

function Filas-Asignatura {
    param([string]$Solicitud)
    return @(Ejecutar-Sql "select uuidAsignaturaSolicitud from ASIGNATURA_SOLICITUD_ACADEMICA where SolicitudAcademica_uuid = '$Solicitud' order by uuidAsignaturaSolicitud")
}

function Evaluaciones {
    param([string[]]$Filas, [string]$Situacion)
    $lista = @()
    $i = 0
    foreach ($fila in $Filas) {
        $lista += @{ asignaturaSolicitudUuid = $fila; numeroFaltas = $i; nota = (4.5 - $i); situacionMatriculaUuid = $Situacion }
        $i++
    }
    return , $lista
}

function Estados-Matricula {
    param([string]$Estudiante)
    return (@(Ejecutar-Sql "select estado from ASIGNATURA_MATRICULADA where Estudiante_uuid = '$Estudiante' order by estado") -join ",")
}

$tokenRoot = Iniciar-Sesion -Usuario "rootfiet" -Clave "rootfiet1234"
$tipoCm = Ejecutar-Sql "select uuidTipoSolicitudAcademica from TIPO_SOLICITUD_ACADEMICA where nombre like 'Cancelaci%n de Matr%cula'"
$tipoCa = Ejecutar-Sql "select uuidTipoSolicitudAcademica from TIPO_SOLICITUD_ACADEMICA where nombre like 'Cancelaci%n de Asignatura'"
$situaciones = @(Ejecutar-Sql "select uuidSituacionAcademica from SITUACION_ACADEMICA_ASIGNATURA order by codigo")
$responsableCm = Ejecutar-Sql "select coalesce(FuncionarioAcademico_uuid, '') from TIPO_SOLICITUD_ACADEMICA where uuidTipoSolicitudAcademica = '$tipoCm'"
if (-not $tipoCm -or -not $tipoCa -or $situaciones.Count -lt 2) { Write-Host "FAIL faltan los catalogos sembrados de procesos academicos"; exit 1 }
$habiaAnexos = Test-Path $Global:CarpetaAnexos
$anio = (Get-Date).Year

$usuarios = @()
$estudiantes = @()
$funcionarios = @()
$codigos = @()
$archivos = @()

try {
    $est1 = Nuevo-Estudiante -Token $tokenRoot -Materias 3
    $est2 = Nuevo-Estudiante -Token $tokenRoot -Materias 2
    $est3 = Nuevo-Estudiante -Token $tokenRoot -Materias 2
    $fa1 = Nuevo-Funcionario -Token $tokenRoot
    $decano = Crear-UsuarioPrueba -Perfil "Decano" -TokenAdmin $tokenRoot -Prefijo "dect6"
    $estudiantes = @($est1.Uuid, $est2.Uuid, $est3.Uuid)
    $funcionarios = @($fa1.Uuid)
    $codigos = @($est1.Codigos + $est2.Codigos + $est3.Codigos)
    $usuarios = @($est1.Uuid, $est2.Uuid, $est3.Uuid, $fa1.Uuid, $decano.Uuid)

    $r = Invoke-Api -Metodo PUT -Ruta "catalogos-academicos/tipos-solicitud/$tipoCm/funcionario" -Token $tokenRoot -Cuerpo @{ funcionarioUuid = $fa1.Uuid }
    if ($r.Status -ne 200) { throw "No se pudo asignar Cancelacion de Matricula al funcionario de prueba ($($r.Status)): $($r.Body)" }

    $pdf = Nuevo-Pdf "anexo"; $archivos += $pdf
    $pdfResolucion = Nuevo-Pdf "resolucion"; $archivos += $pdfResolucion
    $falsa = [guid]::NewGuid().ToString()
    $observacion = @{ observacion = "Observacion de prueba" }
    $remision = @{ evaluaciones = @() }
    $aprobacion = @{ situaciones = @() }

    $casos = @(
        @{ Nombre = "GET /formulario"; Metodo = "GET"; Ruta = "$Global:Ruta/formulario"; Cuerpo = $null; Prohibidos = @{ "Funcionario Academico" = $fa1.Token; "Decano" = $decano.Token; "Secretario General" = $tokenRoot } },
        @{ Nombre = "POST / (radicar)"; Metodo = "MULTIPART"; Ruta = $Global:Ruta; Cuerpo = $null; Prohibidos = @{ "Funcionario Academico" = $fa1.Token; "Decano" = $decano.Token; "Secretario General" = $tokenRoot } },
        @{ Nombre = "GET /{uuid}"; Metodo = "GET"; Ruta = "$Global:Ruta/$falsa"; Cuerpo = $null; Prohibidos = @{ "Secretario General" = $tokenRoot } },
        @{ Nombre = "POST funcionario/rechazar"; Metodo = "POST"; Ruta = "$Global:Ruta/$falsa/funcionario/rechazar"; Cuerpo = $observacion; Prohibidos = @{ "Estudiante" = $est1.Token; "Decano" = $decano.Token; "Secretario General" = $tokenRoot } },
        @{ Nombre = "POST funcionario/remitir"; Metodo = "POST"; Ruta = "$Global:Ruta/$falsa/funcionario/remitir"; Cuerpo = $remision; Prohibidos = @{ "Estudiante" = $est1.Token; "Decano" = $decano.Token; "Secretario General" = $tokenRoot } },
        @{ Nombre = "POST funcionario/responder"; Metodo = "POST"; Ruta = "$Global:Ruta/$falsa/funcionario/responder"; Cuerpo = $null; Prohibidos = @{ "Estudiante" = $est1.Token; "Decano" = $decano.Token; "Secretario General" = $tokenRoot } },
        @{ Nombre = "POST decano/aprobar"; Metodo = "POST"; Ruta = "$Global:Ruta/$falsa/decano/aprobar"; Cuerpo = $aprobacion; Prohibidos = @{ "Estudiante" = $est1.Token; "Funcionario Academico" = $fa1.Token; "Secretario General" = $tokenRoot } },
        @{ Nombre = "POST decano/rechazar"; Metodo = "POST"; Ruta = "$Global:Ruta/$falsa/decano/rechazar"; Cuerpo = $observacion; Prohibidos = @{ "Estudiante" = $est1.Token; "Funcionario Academico" = $fa1.Token; "Secretario General" = $tokenRoot } },
        @{ Nombre = "GET / (sin endpoint)"; Metodo = "GET"; Ruta = $Global:Ruta; Cuerpo = $null; Prohibidos = @{ "Estudiante" = $est1.Token; "Funcionario Academico" = $fa1.Token; "Decano" = $decano.Token } },
        @{ Nombre = "POST /{uuid}/funcionario (sin endpoint)"; Metodo = "POST"; Ruta = "$Global:Ruta/$falsa/otra"; Cuerpo = $null; Prohibidos = @{ "Funcionario Academico" = $fa1.Token; "Decano" = $decano.Token } }
    )
    $antesPermisos = Conteos $est1.Uuid
    foreach ($caso in $casos) {
        if ($caso.Metodo -eq "MULTIPART") { $r = Radicar -Token $null -Tipos @() -Pdf $pdf -ConSoporte $true }
        else { $r = Invoke-Api -Metodo $caso.Metodo -Ruta $caso.Ruta -Cuerpo $caso.Cuerpo }
        if ($caso.Nombre -notlike "*sin endpoint*") { Escribir-Resultado "sin token en $($caso.Nombre) (401, obtuvo $($r.Status))" ($r.Status -eq 401) }
        foreach ($rol in $caso.Prohibidos.Keys) {
            if ($caso.Metodo -eq "MULTIPART") { $r = Radicar -Token $caso.Prohibidos[$rol] -Tipos @() -Pdf $pdf -ConSoporte $true }
            else { $r = Invoke-Api -Metodo $caso.Metodo -Ruta $caso.Ruta -Token $caso.Prohibidos[$rol] -Cuerpo $caso.Cuerpo }
            Escribir-Resultado "$rol en $($caso.Nombre) (403, obtuvo $($r.Status))" ($r.Status -eq 403)
        }
    }
    Escribir-Resultado "los intentos sin permiso no dejaron nada ($antesPermisos)" ((Conteos $est1.Uuid) -eq $antesPermisos)
    foreach ($token in @($fa1.Token, $decano.Token)) {
        $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/situaciones" -Token $token
        Escribir-Resultado "el catalogo de situaciones de T5.1 responde para el desplegable (200, obtuvo $($r.Status) con $(@(Leer-Json $r).Count))" (($r.Status -eq 200) -and (@(Leer-Json $r).Count -ge 2))
    }

    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/formulario" -Token $est1.Token
    $formulario = Leer-Json $r
    $requeridos = @($formulario.anexosRequeridos)
    $obligatorios = @($requeridos | Where-Object { $_.obligatorio -eq $true })
    Escribir-Resultado "el formulario trae los seis anexos requeridos con uuid, nombre y formatos (obtuvo $($requeridos.Count))" (($r.Status -eq 200) -and ($requeridos.Count -eq 6) -and ($obligatorios.Count -eq 6) -and (@($requeridos | Where-Object { $_.uuidTipoAnexoAcademico -and $_.nombre -and $_.formatosPermitidos }).Count -eq 6))
    $materias = @($formulario.asignaturas)
    Escribir-Resultado "el formulario trae las tres asignaturas activas con nombre y codigo (obtuvo $($materias.Count))" (($materias.Count -eq 3) -and (@($materias | Where-Object { $est1.Codigos -contains $_.codigoAsignatura -and $_.nombreAsignatura }).Count -eq 3))
    Escribir-Resultado "el formulario no expone rol ni tipo de usuario" (($r.Body -notmatch '"rol') -and ($r.Body -notmatch 'tipoUsuario'))
    $tipos = @($requeridos | ForEach-Object { $_.uuidTipoAnexoAcademico })

    $antes = Conteos $est1.Uuid
    $r = Radicar -Token $est1.Token -Tipos @($tipos | Select-Object -Skip 1) -Pdf $pdf -ConSoporte $true
    Escribir-Resultado "radicar sin un anexo obligatorio falla (obtuvo $($r.Status): $(([string]((Leer-Json $r).mensaje))))" (($r.Status -ne 200) -and ((Leer-Json $r).mensaje -like "Falta el anexo obligatorio*"))
    $despues = Conteos $est1.Uuid
    Escribir-Resultado "el intento fallido no dejo solicitud, historial, asignaturas, anexos ni archivos ($despues)" ($despues -eq $antes)
    $r = Radicar -Token $est1.Token -Tipos $tipos -Pdf $pdf -ConSoporte $true -Motivo ""
    Escribir-Resultado "radicar sin motivo falla y no deja nada (obtuvo $($r.Status))" (($r.Status -ne 200) -and ((Conteos $est1.Uuid) -eq $antes))

    $ultimo = Ejecutar-Sql "select coalesce(max(radicado), '') from SOLICITUD_ACADEMICA where radicado like '$anio-CM-%'"
    $consecutivo = if ($ultimo) { [int]$ultimo.Substring(8) + 1 } else { 1 }
    $esperado = "$anio-CM-" + $consecutivo.ToString("0000")
    $r = Radicar -Token $est1.Token -Tipos $tipos -Pdf $pdf -ConSoporte $true
    $radicada = if ($r.Status -eq 200) { Leer-Json $r } else { $null }
    $sol1 = $radicada.uuidSolicitudAcademica
    Escribir-Resultado "radicar con los seis anexos y un soporte responde uuid y radicado $esperado (obtuvo $($r.Status) $($radicada.radicado))" (($r.Status -eq 200) -and $sol1 -and ($radicada.radicado -eq $esperado))
    Escribir-Resultado "la solicitud queda RADICADA (obtuvo $(Etapa-De $sol1))" ((Etapa-De $sol1) -eq "RADICADA")
    Verificar-EtapaHttp -Solicitud $sol1 -Esperada "RADICADA" -Estudiante $est1.Token -Funcionario $fa1.Token -Decano $decano.Token
    $anexos = Ejecutar-Sql "select count(*), sum(TipoAnexoAcademico_uuid is null) from ANEXO_ACADEMICO where SolicitudAcademica_uuid = '$sol1'"
    Escribir-Resultado "quedaron 7 anexos, uno de ellos soporte libre, y sus 7 archivos (obtuvo $anexos y $(@(Get-ChildItem (Join-Path $Global:CarpetaAnexos $sol1) -File).Count))" (($anexos -eq "7`t1") -and (@(Get-ChildItem (Join-Path $Global:CarpetaAnexos $sol1) -File).Count -eq 7))
    $filas1 = Filas-Asignatura $sol1
    Escribir-Resultado "la solicitud incluye las tres asignaturas activas (obtuvo $($filas1.Count))" ($filas1.Count -eq 3)
    $r = Radicar -Token $est1.Token -Tipos $tipos -Pdf $pdf -ConSoporte $false
    Escribir-Resultado "una segunda radicacion en curso se rechaza (obtuvo $($r.Status): $(([string]((Leer-Json $r).mensaje))))" (($r.Status -ne 200) -and ((Leer-Json $r).mensaje -like "*en curso*") -and ((Ejecutar-Sql "select count(*) from SOLICITUD_ACADEMICA where Estudiante_uuid = '$($est1.Uuid)'") -eq "1"))

    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol1" -Token $est1.Token
    $detalle = Leer-Json $r
    Escribir-Resultado "el estudiante ve su detalle con motivo, etiqueta y tres asignaturas (obtuvo $($r.Status))" (($r.Status -eq 200) -and ($detalle.motivoCancelacion -eq "Calamidad domestica de prueba") -and ($detalle.solicitud.etiqueta -eq "En tr$([char]0x00E1)mite") -and (@($detalle.asignaturas).Count -eq 3))
    Escribir-Resultado "cada asignatura del detalle trae el uuid de su fila, codigo y nombre" (@($detalle.asignaturas | Where-Object { ($filas1 -contains $_.uuidAsignaturaSolicitud) -and $_.codigoAsignatura -and $_.nombreAsignatura }).Count -eq 3)
    $inexistente = [guid]::NewGuid().ToString()
    $firmaNoExiste = Firma-Error (Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$inexistente" -Token $est2.Token) $inexistente
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol1" -Token $est2.Token
    Escribir-Resultado "el detalle ajeno responde igual que uno inexistente ($firmaNoExiste)" (($r.Status -ne 200) -and ((Firma-Error $r $sol1) -eq $firmaNoExiste))
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol1" -Token $decano.Token
    Escribir-Resultado "el Decano no ve una cancelacion RADICADA" ((Firma-Error $r $sol1) -eq $firmaNoExiste)
    $solCa = [guid]::NewGuid().ToString()
    Ejecutar-Sql ("insert into SOLICITUD_ACADEMICA (uuidSolicitudAcademica, radicado, Estudiante_uuid, TipoSolicitudAcademica_uuid, Etapa_uuid, fechaCreacion) " +
            "values ('$solCa', '1999-CA-$(Get-Random -Minimum 1000 -Maximum 9999)', '$($est2.Uuid)', '$tipoCa', (select uuidEtapa from ETAPA_SOLICITUD_ACADEMICA where codigo = 'RADICADA'), now())") | Out-Null
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$solCa" -Token $est2.Token
    Escribir-Resultado "una solicitud propia de otro tipo responde igual que una inexistente" ((Firma-Error $r $solCa) -eq $firmaNoExiste)
    $r = Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$solCa" -Token $est2.Token
    Escribir-Resultado "esa misma solicitud si se ve por solicitudes-academicas (200, obtuvo $($r.Status))" ($r.Status -eq 200)
    Ejecutar-Sql "delete from SOLICITUD_ACADEMICA where uuidSolicitudAcademica = '$solCa'" | Out-Null

    Ejecutar-Sql "update ASIGNATURA_MATRICULADA set estado = 'perdida' where Estudiante_uuid = '$($est1.Uuid)' and uuidAsignaturaMatriculada = (select AsignaturaMatriculada_uuid from ASIGNATURA_SOLICITUD_ACADEMICA where uuidAsignaturaSolicitud = '$($filas1[2])')" | Out-Null

    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/funcionario/remitir" -Token $fa1.Token -Cuerpo @{ evaluaciones = (Evaluaciones @($filas1[0], $filas1[1]) $situaciones[0]) }
    $evaluadas = Ejecutar-Sql "select count(*) from ASIGNATURA_SOLICITUD_ACADEMICA where SolicitudAcademica_uuid = '$sol1' and numeroFaltas is not null"
    Escribir-Resultado "remitir con una asignatura sin evaluar falla y no guarda nada (obtuvo $($r.Status), etapa $(Etapa-De $sol1), evaluadas $evaluadas)" (($r.Status -ne 200) -and ((Etapa-De $sol1) -eq "RADICADA") -and ($evaluadas -eq "0") -and ((Leer-Json $r).mensaje -like "Faltan datos*"))
    $malas = Evaluaciones $filas1 $situaciones[0]
    $malas[1].nota = 5.5
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/funcionario/remitir" -Token $fa1.Token -Cuerpo @{ evaluaciones = $malas }
    Escribir-Resultado "remitir con una nota fuera de rango falla y sigue RADICADA (obtuvo $($r.Status))" (($r.Status -ne 200) -and ((Etapa-De $sol1) -eq "RADICADA"))
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/funcionario/remitir" -Token $fa1.Token -Cuerpo @{ observacion = "Requisitos verificados"; evaluaciones = (Evaluaciones $filas1 $situaciones[0]) }
    $detalle = if ($r.Status -eq 200) { Leer-Json $r } else { $null }
    Escribir-Resultado "remitir correcto pasa a EN_REVISION_DECANO (obtuvo $($r.Status), etapa $(Etapa-De $sol1))" (($r.Status -eq 200) -and ((Etapa-De $sol1) -eq "EN_REVISION_DECANO"))
    Verificar-EtapaHttp -Solicitud $sol1 -Esperada "EN_REVISION_DECANO" -Estudiante $est1.Token -Funcionario $fa1.Token -Decano $decano.Token
    $guardadas = Ejecutar-Sql "select count(*) from ASIGNATURA_SOLICITUD_ACADEMICA where SolicitudAcademica_uuid = '$sol1' and numeroFaltas is not null and nota is not null and SituacionMatricula_uuid = '$($situaciones[0])' and SituacionCancelar_uuid is null"
    Escribir-Resultado "las tres evaluaciones quedaron en MySQL (obtuvo $guardadas)" ($guardadas -eq "3")
    $notaFila = Ejecutar-Sql "select concat(numeroFaltas, '|', nota) from ASIGNATURA_SOLICITUD_ACADEMICA where uuidAsignaturaSolicitud = '$($filas1[1])'"
    Escribir-Resultado "la respuesta trae el detalle con la evaluacion (obtuvo $notaFila)" (($notaFila -eq "1|3.5") -and (@($detalle.asignaturas | Where-Object { $_.uuidAsignaturaSolicitud -eq $filas1[1] -and $_.numeroFaltas -eq 1 -and $_.nota -eq 3.5 -and $_.situacionMatricula.uuidSituacionAcademica -eq $situaciones[0] }).Count -eq 1))

    $alCancelar = @($filas1 | ForEach-Object { @{ asignaturaSolicitudUuid = $_; situacionCancelarUuid = $situaciones[1] } })
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/decano/aprobar" -Token $decano.Token -Cuerpo @{ situaciones = @($alCancelar[0]) }
    Escribir-Resultado "aprobar sin la situacion de todas las asignaturas falla (obtuvo $($r.Status), etapa $(Etapa-De $sol1))" (($r.Status -ne 200) -and ((Etapa-De $sol1) -eq "EN_REVISION_DECANO"))
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/decano/aprobar" -Token $decano.Token -Cuerpo @{ situaciones = $alCancelar }
    $conSituacion = Ejecutar-Sql "select count(*) from ASIGNATURA_SOLICITUD_ACADEMICA where SolicitudAcademica_uuid = '$sol1' and SituacionCancelar_uuid = '$($situaciones[1])' and numeroFaltas is not null"
    Escribir-Resultado "aprobar con situaciones pasa a APROBADA_POR_DECANO y las guarda (obtuvo $($r.Status), etapa $(Etapa-De $sol1), filas $conSituacion)" (($r.Status -eq 200) -and ((Etapa-De $sol1) -eq "APROBADA_POR_DECANO") -and ($conSituacion -eq "3"))
    Verificar-EtapaHttp -Solicitud $sol1 -Esperada "APROBADA_POR_DECANO" -Estudiante $est1.Token -Funcionario $fa1.Token -Decano $decano.Token
    $detalle = Leer-Json (Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol1" -Token $est1.Token)
    Escribir-Resultado "el estudiante aun no ve la situacion al cancelar" ((@($detalle.asignaturas | Where-Object { $_.situacionCancelar }).Count -eq 0) -and (@($detalle.asignaturas | Where-Object { $_.situacionMatricula }).Count -eq 3))
    $detalle = Leer-Json (Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol1" -Token $fa1.Token)
    Escribir-Resultado "el funcionario si ve la situacion al cancelar" (@($detalle.asignaturas | Where-Object { $_.situacionCancelar.uuidSituacionAcademica -eq $situaciones[1] }).Count -eq 3)
    Escribir-Resultado "las asignaturas siguen sin cancelar tras aprobar (obtuvo $(Estados-Matricula $est1.Uuid))" ((Estados-Matricula $est1.Uuid) -eq "activa,activa,perdida")

    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/funcionario/responder" -Token $fa1.Token
    Escribir-Resultado "enviar respuesta sin escaneo falla (obtuvo $($r.Status), etapa $(Etapa-De $sol1))" (($r.Status -ne 200) -and ((Etapa-De $sol1) -eq "APROBADA_POR_DECANO") -and ((Estados-Matricula $est1.Uuid) -eq "activa,activa,perdida"))
    $r = Subir-Escaneo -Solicitud $sol1 -Pdf $pdfResolucion -Token $fa1.Token
    Escribir-Resultado "el funcionario sube el escaneo de la Resolucion (200, obtuvo $($r.Status))" ($r.Status -eq 200)
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/funcionario/responder" -Token $fa1.Token
    Escribir-Resultado "enviar respuesta deja la solicitud APROBADA (obtuvo $($r.Status), etapa $(Etapa-De $sol1))" (($r.Status -eq 200) -and ((Etapa-De $sol1) -eq "APROBADA"))
    Verificar-EtapaHttp -Solicitud $sol1 -Esperada "APROBADA" -Estudiante $est1.Token -Funcionario $fa1.Token -Decano $decano.Token
    Escribir-Resultado "las asignaturas activas quedaron canceladas y la perdida no se toco (obtuvo $(Estados-Matricula $est1.Uuid))" ((Estados-Matricula $est1.Uuid) -eq "cancelada,cancelada,perdida")
    $d = Descargar -Ruta "solicitudes-academicas/$sol1/resolucion" -Token $est1.Token
    Escribir-Resultado "el estudiante descarga la Resolucion identica (200, obtuvo $($d.Status))" (($d.Status -eq 200) -and (Mismos-Bytes $pdfResolucion $d.Bytes))
    $detalle = Leer-Json (Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol1" -Token $est1.Token)
    Escribir-Resultado "en APROBADA el estudiante ve la situacion al cancelar y la etiqueta Aprobada" (($detalle.solicitud.etiqueta -eq "Aprobada") -and (@($detalle.asignaturas | Where-Object { $_.situacionCancelar }).Count -eq 3))
    $historial = @(Leer-Json (Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$sol1/historial" -Token $est1.Token)) | ForEach-Object { $_.accion }
    Escribir-Resultado "el historial trae RADICAR, REMITIR_DECANO, APROBAR_DECANO y ENVIAR_RESPUESTA (obtuvo $($historial -join ','))" (($historial -join ',') -eq "RADICAR,REMITIR_DECANO,APROBAR_DECANO,ENVIAR_RESPUESTA")
    $etapasHistorial = (@(Leer-Json (Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$sol1/historial" -Token $fa1.Token)) | ForEach-Object { "$($_.accion):$($_.etapaCodigo)" }) -join ","; Escribir-Resultado "cada accion del historial trae la etapa a la que llevo (obtuvo $etapasHistorial)" ($etapasHistorial -eq "RADICAR:RADICADA,REMITIR_DECANO:EN_REVISION_DECANO,APROBAR_DECANO:APROBADA_POR_DECANO,ENVIAR_RESPUESTA:APROBADA")

    $r = Radicar -Token $est2.Token -Tipos $tipos -Pdf $pdf -ConSoporte $false
    $sol2 = (Leer-Json $r).uuidSolicitudAcademica
    Escribir-Resultado "el estudiante 2 radica (200, obtuvo $($r.Status))" (($r.Status -eq 200) -and $sol2)
    Verificar-EtapaHttp -Solicitud $sol2 -Esperada "RADICADA" -Estudiante $est2.Token -Funcionario $fa1.Token -Decano $decano.Token
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol2/funcionario/rechazar" -Token $fa1.Token -Cuerpo @{ observacion = "No cumple requisitos" }
    Escribir-Resultado "rechazar sin escaneo falla y sigue RADICADA (obtuvo $($r.Status))" (($r.Status -ne 200) -and ((Etapa-De $sol2) -eq "RADICADA"))
    $r = Subir-Escaneo -Solicitud $sol2 -Pdf $pdfResolucion -Token $fa1.Token
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol2/funcionario/rechazar" -Token $fa1.Token -Cuerpo @{ observacion = " " }
    Escribir-Resultado "rechazar sin observacion falla aun con escaneo (obtuvo $($r.Status))" (($r.Status -ne 200) -and ((Etapa-De $sol2) -eq "RADICADA"))
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol2/funcionario/rechazar" -Token $fa1.Token -Cuerpo @{ observacion = "No cumple requisitos" }
    Escribir-Resultado "rechazo del funcionario con escaneo deja la solicitud RECHAZADA (obtuvo $($r.Status), etapa $(Etapa-De $sol2))" (($r.Status -eq 200) -and ((Etapa-De $sol2) -eq "RECHAZADA"))
    Verificar-EtapaHttp -Solicitud $sol2 -Esperada "RECHAZADA" -Estudiante $est2.Token -Funcionario $fa1.Token -Decano $decano.Token
    Escribir-Resultado "tras el rechazo del funcionario las asignaturas siguen activas (obtuvo $(Estados-Matricula $est2.Uuid))" ((Estados-Matricula $est2.Uuid) -eq "activa,activa")
    $d = Descargar -Ruta "solicitudes-academicas/$sol2/resolucion" -Token $est2.Token
    Escribir-Resultado "el estudiante 2 descarga la Resolucion del rechazo (200, obtuvo $($d.Status))" (($d.Status -eq 200) -and (Mismos-Bytes $pdfResolucion $d.Bytes))

    $r = Radicar -Token $est3.Token -Tipos $tipos -Pdf $pdf -ConSoporte $true
    $sol3 = (Leer-Json $r).uuidSolicitudAcademica
    $filas3 = Filas-Asignatura $sol3
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol3/funcionario/remitir" -Token $fa1.Token -Cuerpo @{ evaluaciones = (Evaluaciones $filas3 $situaciones[1]) }
    Escribir-Resultado "el estudiante 3 radica y el funcionario remite sin observacion (obtuvo $($r.Status), etapa $(Etapa-De $sol3))" (($r.Status -eq 200) -and ((Etapa-De $sol3) -eq "EN_REVISION_DECANO"))
    Verificar-EtapaHttp -Solicitud $sol3 -Esperada "EN_REVISION_DECANO" -Estudiante $est3.Token -Funcionario $fa1.Token -Decano $decano.Token
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol3/decano/rechazar" -Token $decano.Token -Cuerpo @{ observacion = "" }
    Escribir-Resultado "el Decano no rechaza sin observacion (obtuvo $($r.Status))" (($r.Status -ne 200) -and ((Etapa-De $sol3) -eq "EN_REVISION_DECANO"))
    $motivoDecano = "No procede: la causa no esta soportada"
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol3/decano/rechazar" -Token $decano.Token -Cuerpo @{ observacion = $motivoDecano }
    $sinSituacion = Ejecutar-Sql "select count(*) from ASIGNATURA_SOLICITUD_ACADEMICA where SolicitudAcademica_uuid = '$sol3' and SituacionCancelar_uuid is null"
    Escribir-Resultado "el rechazo del Decano pasa a RECHAZADA_POR_DECANO sin situaciones al cancelar (obtuvo $($r.Status), etapa $(Etapa-De $sol3))" (($r.Status -eq 200) -and ((Etapa-De $sol3) -eq "RECHAZADA_POR_DECANO") -and ($sinSituacion -eq "2"))
    Verificar-EtapaHttp -Solicitud $sol3 -Esperada "RECHAZADA_POR_DECANO" -Estudiante $est3.Token -Funcionario $fa1.Token -Decano $decano.Token
    $historial = @(Leer-Json (Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$sol3/historial" -Token $est3.Token))
    $filaDecano = $historial | Where-Object { $_.accion -eq "RECHAZAR_DECANO" }
    Escribir-Resultado "el estudiante ve la observacion del Decano en el historial (obtuvo $($filaDecano.observaciones))" ($filaDecano.observaciones -eq $motivoDecano)
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol3/funcionario/responder" -Token $fa1.Token
    Escribir-Resultado "responder sin escaneo falla (obtuvo $($r.Status))" (($r.Status -ne 200) -and ((Etapa-De $sol3) -eq "RECHAZADA_POR_DECANO"))
    $r = Subir-Escaneo -Solicitud $sol3 -Pdf $pdfResolucion -Token $fa1.Token
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol3/funcionario/responder" -Token $fa1.Token
    Escribir-Resultado "la respuesta final deja la solicitud RECHAZADA (obtuvo $($r.Status), etapa $(Etapa-De $sol3))" (($r.Status -eq 200) -and ((Etapa-De $sol3) -eq "RECHAZADA"))
    Verificar-EtapaHttp -Solicitud $sol3 -Esperada "RECHAZADA" -Estudiante $est3.Token -Funcionario $fa1.Token -Decano $decano.Token
    Escribir-Resultado "tras el rechazo del Decano las asignaturas siguen activas (obtuvo $(Estados-Matricula $est3.Uuid))" ((Estados-Matricula $est3.Uuid) -eq "activa,activa")
    $detalle = Leer-Json (Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol3" -Token $est3.Token)
    Escribir-Resultado "el estudiante 3 ve su solicitud Rechazada con la Resolucion descargable" (($detalle.solicitud.etiqueta -eq "Rechazada") -and $detalle.solicitud.puedeDescargarResolucion)
    $etapasHistorial = (@(Leer-Json (Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$sol3/historial" -Token $fa1.Token)) | ForEach-Object { "$($_.accion):$($_.etapaCodigo)" }) -join ","; Escribir-Resultado "cada accion del historial trae la etapa a la que llevo (obtuvo $etapasHistorial)" ($etapasHistorial -eq "RADICAR:RADICADA,REMITIR_DECANO:EN_REVISION_DECANO,RECHAZAR_DECANO:RECHAZADA_POR_DECANO,ENVIAR_RESPUESTA:RECHAZADA")
}
finally {
    if ($responsableCm) {
        $r = Invoke-Api -Metodo PUT -Ruta "catalogos-academicos/tipos-solicitud/$tipoCm/funcionario" -Token $tokenRoot -Cuerpo @{ funcionarioUuid = $responsableCm }
        if ($r.Status -ne 200) { Write-Host "No se pudo restaurar el responsable: $($r.Status) $($r.Body)" }
    } else {
        Ejecutar-Sql "update TIPO_SOLICITUD_ACADEMICA set FuncionarioAcademico_uuid = null where uuidTipoSolicitudAcademica = '$tipoCm'" | Out-Null
    }
    $actual = Ejecutar-Sql "select coalesce(FuncionarioAcademico_uuid, '') from TIPO_SOLICITUD_ACADEMICA where uuidTipoSolicitudAcademica = '$tipoCm'"
    Escribir-Resultado "Cancelacion de Matricula quedo con su responsable original" ($actual -eq $responsableCm)

    $est = Lista-Sql $estudiantes
    $solicitudes = @()
    if ($est) { $solicitudes = @(Ejecutar-Sql "select uuidSolicitudAcademica from SOLICITUD_ACADEMICA where Estudiante_uuid in ($est)") }
    $sol = Lista-Sql $solicitudes
    $usu = Lista-Sql $usuarios
    $fun = Lista-Sql $funcionarios
    $cod = Lista-Sql $codigos
    $sentencias = @()
    if ($sol) {
        $sentencias += "delete from ANEXO_ACADEMICO where SolicitudAcademica_uuid in ($sol)"
        $sentencias += "delete from RESOLUCION_ACADEMICA where SolicitudAcademica_uuid in ($sol)"
        $sentencias += "delete from ASIGNATURA_SOLICITUD_ACADEMICA where SolicitudAcademica_uuid in ($sol)"
        $sentencias += "delete from SOLICITUD_CANCELACION_MATRICULA where SolicitudAcademica_uuid in ($sol)"
        $sentencias += "delete from HISTORIAL_SOLICITUD_ACADEMICA where SolicitudAcademica_uuid in ($sol)"
        $sentencias += "delete from SOLICITUD_ACADEMICA where uuidSolicitudAcademica in ($sol)"
    }
    if ($usu) {
        $sentencias += "delete from logs where uuidUsuario in ($usu)"
        if ($est) { $sentencias += "delete from ASIGNATURA_MATRICULADA where Estudiante_uuid in ($est)"; $sentencias += "delete from ESTUDIANTE where Usuario_uuid in ($est)" }
        if ($cod) { $sentencias += "delete from ASIGNATURA where codigoAsignatura in ($cod)" }
        if ($fun) { $sentencias += "delete from FUNCIONARIO_ACADEMICO where Usuario_uuid in ($fun)" }
        $sentencias += "delete from Usuario_has_Roles where uuidUsuario in ($usu)"
        $sentencias += "delete from usuarios where uuidUsuario in ($usu)"
        $sentencias += "delete from usuariosLivianos where uuidUsuario in ($usu)"
    }
    if ($sentencias.Count -gt 0) { Ejecutar-Sql (($sentencias -join "; ") + ";") | Out-Null }

    foreach ($uuid in $solicitudes) {
        $carpeta = Join-Path $Global:CarpetaAnexos $uuid
        if (Test-Path $carpeta) { Remove-Item -LiteralPath $carpeta -Recurse -Force }
    }
    if (-not $habiaAnexos -and (Test-Path $Global:CarpetaAnexos) -and (@(Get-ChildItem $Global:CarpetaAnexos -Force).Count -eq 0)) {
        Remove-Item -LiteralPath $Global:CarpetaAnexos -Force
    }
    foreach ($archivo in $archivos) { if (Test-Path $archivo) { Remove-Item -LiteralPath $archivo -Force } }

    $restos = 0
    if ($sol) { $restos += [int](Ejecutar-Sql "select count(*) from SOLICITUD_ACADEMICA where uuidSolicitudAcademica in ($sol)") }
    if ($usu) { $restos += [int](Ejecutar-Sql "select count(*) from usuariosLivianos where uuidUsuario in ($usu)") }
    if ($cod) { $restos += [int](Ejecutar-Sql "select count(*) from ASIGNATURA where codigoAsignatura in ($cod)") }
    $carpetas = @($solicitudes | Where-Object { Test-Path (Join-Path $Global:CarpetaAnexos $_) }).Count
    Escribir-Resultado "no quedaron filas, usuarios, asignaturas ni carpetas de prueba (filas $restos, carpetas $carpetas)" (($restos -eq 0) -and ($carpetas -eq 0))
}
