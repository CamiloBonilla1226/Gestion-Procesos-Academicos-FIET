. "$PSScriptRoot\comun.ps1"

$Global:CarpetaBackend = (Resolve-Path "$PSScriptRoot\..").Path
$Global:CarpetaAnexos = Join-Path $Global:CarpetaBackend "uploads\anexos"
$Global:Ruta = "cancelaciones-asignatura"
$Global:Trigger = "t7_falla_mitad"
$Global:ArchivoFalla = "t7_falla_mitad.pdf"

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
    for ($i = 1; $i -le $Materias; $i++) { $asignaturas += @{ codigoAsignatura = "T7$s$i"; nombreAsignatura = "Materia T7 $i $s"; grupo = "A" } }
    $cuerpo = @{
        nombres           = "Cancelante"
        apellidos         = "Asignatura $s"
        tipoDocumento     = $Global:CedulaCiudadania
        numeroDocumento   = "6$s"
        telefono          = "+573000000000"
        correoElectronico = "cas$s@unicauca.edu.co"
        username          = "cas$s"
        password          = "Clave12345"
        codigoEstudiantil = "A$s"
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
        Token   = Iniciar-Sesion -Usuario "cas$s" -Clave "Clave12345"
    }
}

function Nuevo-Funcionario {
    param([string]$Token)
    $s = Nuevo-Sufijo
    $cuerpo = @{
        nombres           = "Tramitador"
        apellidos         = "Asignatura $s"
        tipoDocumento     = $Global:CedulaCiudadania
        numeroDocumento   = "3$s"
        telefono          = "+573000000000"
        correoElectronico = "tca$s@unicauca.edu.co"
        username          = "tca$s"
        password          = "Clave12345"
        dependencia       = "DepT7 $s"
    }
    $r = Invoke-Api -Metodo POST -Ruta "funcionarios-academicos" -Token $Token -Cuerpo $cuerpo
    if ($r.Status -ne 200) { throw "No se pudo crear el funcionario academico ($($r.Status)): $($r.Body)" }
    return [pscustomobject]@{
        Uuid  = (Leer-Json $r).uuidUsuario
        Token = Iniciar-Sesion -Usuario "tca$s" -Clave "Clave12345"
    }
}

function Nuevo-Pdf {
    param([string]$Texto)
    $ruta = Join-Path ([System.IO.Path]::GetTempPath()) ("t7_" + (Nuevo-Sufijo) + ".pdf")
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
    param([string]$Token, [string[]]$Asignaturas, [string]$Pdf, [bool]$ConSoporte, [string]$Motivo = "Cruce con horario laboral de prueba", [string[]]$Extra = @())
    $argumentos = @()
    if ($Motivo) { $argumentos += @("-F", "motivo=$Motivo") }
    foreach ($asignatura in $Asignaturas) { $argumentos += @("-F", "asignaturas=$asignatura") }
    if ($ConSoporte) { $argumentos += @("-F", "soporte=@$Pdf;type=application/pdf") }
    $argumentos += $Extra
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

function Mensaje-De {
    param($Respuesta)
    try { return [string]((Leer-Json $Respuesta).mensaje) } catch { return "" }
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
            " ca=" + (Ejecutar-Sql "select count(*) from SOLICITUD_CANCELACION_ASIGNATURA") +
            " asig=" + (Ejecutar-Sql "select count(*) from ASIGNATURA_SOLICITUD_ACADEMICA") +
            " anexos=" + (Ejecutar-Sql "select count(*) from ANEXO_ACADEMICO") +
            " archivos=" + (Archivos-Subidos)
}

function Estado-Filas {
    param([string]$Solicitud)
    return (@(Ejecutar-Sql ("select concat(coalesce(numeroFaltas, '-'), '|', coalesce(nota, '-'), '|', coalesce(cumpleCondiciones, '-'), '|', coalesce(observacionEvaluacion, '-'), '|', " +
            "coalesce(aprobadaPorDecano, '-'), '|', coalesce(observacionDecision, '-'), '|', coalesce(SituacionCancelar_uuid, '-')) from ASIGNATURA_SOLICITUD_ACADEMICA " +
            "where SolicitudAcademica_uuid = '$Solicitud' order by uuidAsignaturaSolicitud")) -join ";")
}

function Fila-De {
    param([string]$Solicitud, [string]$Matriculada)
    return Ejecutar-Sql "select uuidAsignaturaSolicitud from ASIGNATURA_SOLICITUD_ACADEMICA where SolicitudAcademica_uuid = '$Solicitud' and AsignaturaMatriculada_uuid = '$Matriculada'"
}

function Estado-De {
    param([string]$Matriculada)
    return Ejecutar-Sql "select estado from ASIGNATURA_MATRICULADA where uuidAsignaturaMatriculada = '$Matriculada'"
}

function Estados {
    param([string[]]$Matriculadas)
    return (@($Matriculadas | ForEach-Object { Estado-De $_ }) -join ",")
}

function Activas {
    param($Estudiante)
    $formulario = Leer-Json (Invoke-Api -Metodo GET -Ruta "$Global:Ruta/formulario" -Token $Estudiante.Token)
    return @($formulario.asignaturas | Sort-Object codigoAsignatura | ForEach-Object { $_.uuidAsignaturaMatriculada })
}

function Evaluacion {
    param([string]$Fila, [int]$Faltas, [double]$Nota, [string]$Situacion, $Cumple, [string]$Observacion)
    $evaluacion = @{ asignaturaSolicitudUuid = $Fila; numeroFaltas = $Faltas; nota = $Nota; situacionMatriculaUuid = $Situacion; cumpleCondiciones = $Cumple }
    if ($Observacion) { $evaluacion.observacionEvaluacion = $Observacion }
    return $evaluacion
}

function Decision {
    param([string]$Fila, [bool]$Aprobada, [string]$Situacion, [string]$Observacion)
    $decision = @{ asignaturaSolicitudUuid = $Fila; aprobada = $Aprobada }
    if ($Situacion) { $decision.situacionCancelarUuid = $Situacion }
    if ($Observacion) { $decision.observacionDecision = $Observacion }
    return $decision
}

function Asignatura-De {
    param($Detalle, [string]$Fila)
    return @($Detalle.asignaturas | Where-Object { $_.uuidAsignaturaSolicitud -eq $Fila })[0]
}

function Sin-Resultado {
    param($Detalle)
    return (@($Detalle.asignaturas | Where-Object { ($null -ne $_.aprobadaPorDecano) -or $_.observacionDecision -or ($null -ne $_.cumpleCondiciones) -or $_.observacionEvaluacion -or ($null -ne $_.nota) -or ($null -ne $_.numeroFaltas) -or $_.situacionMatricula -or $_.situacionCancelar }).Count -eq 0)
}

function Quitar-Trigger {
    Ejecutar-Sql "drop trigger if exists $Global:Trigger" | Out-Null
}

$tokenRoot = Iniciar-Sesion -Usuario "rootfiet" -Clave "rootfiet1234"
$tipoCa = Ejecutar-Sql "select uuidTipoSolicitudAcademica from TIPO_SOLICITUD_ACADEMICA where nombre like 'Cancelaci%n de Asignatura'"
$tipoCm = Ejecutar-Sql "select uuidTipoSolicitudAcademica from TIPO_SOLICITUD_ACADEMICA where nombre like 'Cancelaci%n de Matr%cula'"
$situaciones = @(Ejecutar-Sql "select uuidSituacionAcademica from SITUACION_ACADEMICA_ASIGNATURA order by codigo")
$responsableCa = Ejecutar-Sql "select coalesce(FuncionarioAcademico_uuid, '') from TIPO_SOLICITUD_ACADEMICA where uuidTipoSolicitudAcademica = '$tipoCa'"
if (-not $tipoCa -or -not $tipoCm -or $situaciones.Count -lt 2) { Write-Host "FAIL faltan los catalogos sembrados de procesos academicos"; exit 1 }
$habiaAnexos = Test-Path $Global:CarpetaAnexos
$anio = (Get-Date).Year

$usuarios = @()
$estudiantes = @()
$funcionarios = @()
$codigos = @()
$archivos = @()

try {
    $est1 = Nuevo-Estudiante -Token $tokenRoot -Materias 5
    $est2 = Nuevo-Estudiante -Token $tokenRoot -Materias 2
    $est3 = Nuevo-Estudiante -Token $tokenRoot -Materias 2
    $est4 = Nuevo-Estudiante -Token $tokenRoot -Materias 2
    $fa1 = Nuevo-Funcionario -Token $tokenRoot
    $fa2 = Nuevo-Funcionario -Token $tokenRoot
    $decano = Crear-UsuarioPrueba -Perfil "Decano" -TokenAdmin $tokenRoot -Prefijo "dect7"
    $estudiantes = @($est1.Uuid, $est2.Uuid, $est3.Uuid, $est4.Uuid)
    $funcionarios = @($fa1.Uuid, $fa2.Uuid)
    $codigos = @($est1.Codigos + $est2.Codigos + $est3.Codigos + $est4.Codigos)
    $usuarios = @($estudiantes + $funcionarios + $decano.Uuid)

    $r = Invoke-Api -Metodo PUT -Ruta "catalogos-academicos/tipos-solicitud/$tipoCa/funcionario" -Token $tokenRoot -Cuerpo @{ funcionarioUuid = $fa1.Uuid }
    if ($r.Status -ne 200) { throw "No se pudo asignar Cancelacion de Asignatura al funcionario de prueba ($($r.Status)): $($r.Body)" }

    $pdf = Nuevo-Pdf "soporte"; $archivos += $pdf
    $pdfResolucion = Nuevo-Pdf "resolucion"; $archivos += $pdfResolucion
    $falsa = [guid]::NewGuid().ToString()
    $observacion = @{ observacion = "Observacion de prueba" }
    $remision = @{ evaluaciones = @() }
    $aprobacion = @{ decisiones = @() }

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
        @{ Nombre = "POST /{uuid}/otra (sin endpoint)"; Metodo = "POST"; Ruta = "$Global:Ruta/$falsa/otra"; Cuerpo = $null; Prohibidos = @{ "Funcionario Academico" = $fa1.Token; "Decano" = $decano.Token } }
    )
    $antesPermisos = Conteos $est1.Uuid
    foreach ($caso in $casos) {
        if ($caso.Metodo -eq "MULTIPART") { $r = Radicar -Token $null -Asignaturas @($falsa) -Pdf $pdf -ConSoporte $true }
        else { $r = Invoke-Api -Metodo $caso.Metodo -Ruta $caso.Ruta -Cuerpo $caso.Cuerpo }
        if ($caso.Nombre -notlike "*sin endpoint*") { Escribir-Resultado "sin token en $($caso.Nombre) (401, obtuvo $($r.Status))" ($r.Status -eq 401) }
        foreach ($rol in $caso.Prohibidos.Keys) {
            if ($caso.Metodo -eq "MULTIPART") { $r = Radicar -Token $caso.Prohibidos[$rol] -Asignaturas @($falsa) -Pdf $pdf -ConSoporte $true }
            else { $r = Invoke-Api -Metodo $caso.Metodo -Ruta $caso.Ruta -Token $caso.Prohibidos[$rol] -Cuerpo $caso.Cuerpo }
            Escribir-Resultado "$rol en $($caso.Nombre) (403, obtuvo $($r.Status))" ($r.Status -eq 403)
        }
    }
    Escribir-Resultado "los intentos sin permiso no dejaron nada ($antesPermisos)" ((Conteos $est1.Uuid) -eq $antesPermisos)
    $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/situaciones" -Token $decano.Token
    Escribir-Resultado "el catalogo de situaciones de T5.1 responde para el desplegable (200, obtuvo $($r.Status) con $(@(Leer-Json $r).Count))" (($r.Status -eq 200) -and (@(Leer-Json $r).Count -ge 2))

    $todas = Activas $est1
    Escribir-Resultado "el formulario trae al inicio las cinco asignaturas activas (obtuvo $($todas.Count))" ($todas.Count -eq 5)
    $noActiva = $todas[4]
    Ejecutar-Sql "update ASIGNATURA_MATRICULADA set estado = 'cancelada' where uuidAsignaturaMatriculada = '$noActiva'" | Out-Null
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/formulario" -Token $est1.Token
    $formulario = Leer-Json $r
    $materias = @($formulario.asignaturas)
    Escribir-Resultado "el formulario trae solo las cuatro activas con uuid, codigo y nombre (obtuvo $($r.Status) con $($materias.Count))" (($r.Status -eq 200) -and ($materias.Count -eq 4) -and (@($materias | Where-Object { $_.uuidAsignaturaMatriculada -and ($est1.Codigos -contains $_.codigoAsignatura) -and $_.nombreAsignatura }).Count -eq 4) -and (@($materias | Where-Object { $_.uuidAsignaturaMatriculada -eq $noActiva }).Count -eq 0))
    $soportes = @($formulario.soportes)
    Escribir-Resultado "el formulario trae el soporte libre opcional con formatos y tamano (obtuvo $($soportes.Count): $($soportes[0].formatosPermitidos) $($soportes[0].tamanioMaximoBytes))" (($soportes.Count -eq 1) -and ($null -eq $soportes[0].uuidTipoAnexoAcademico) -and ($soportes[0].nombre -eq "Soporte libre") -and ($soportes[0].formatosPermitidos -eq "pdf,jpg,jpeg,png") -and ($soportes[0].tamanioMaximoBytes -eq 5242880) -and ($soportes[0].obligatorio -eq $false))
    Escribir-Resultado "el formulario no expone rol ni tipo de usuario" (($r.Body -notmatch '"rol') -and ($r.Body -notmatch 'tipoUsuario'))
    $activas1 = Activas $est1
    $a1 = $activas1[0]; $a2 = $activas1[1]; $a3 = $activas1[2]; $a4 = $activas1[3]
    $ajena = (Activas $est2)[0]

    $antes = Conteos $est1.Uuid
    $r = Radicar -Token $est1.Token -Asignaturas @($a1, $ajena) -Pdf $pdf -ConSoporte $true
    Escribir-Resultado "radicar con una asignatura ajena falla (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*no es una asignatura matriculada del estudiante*"))
    Escribir-Resultado "la asignatura ajena no dejo solicitud, historial, asignaturas, anexos ni archivos ($(Conteos $est1.Uuid))" ((Conteos $est1.Uuid) -eq $antes)
    $r = Radicar -Token $est1.Token -Asignaturas @($a1, $noActiva) -Pdf $pdf -ConSoporte $true
    Escribir-Resultado "radicar con una asignatura no activa falla (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*solo se pueden cancelar asignaturas activas*"))
    Escribir-Resultado "la asignatura no activa no dejo nada ($(Conteos $est1.Uuid))" ((Conteos $est1.Uuid) -eq $antes)
    $r = Radicar -Token $est1.Token -Asignaturas @() -Pdf $pdf -ConSoporte $true
    Escribir-Resultado "radicar sin asignaturas falla y no deja nada (obtuvo $($r.Status))" (($r.Status -ne 200) -and ((Conteos $est1.Uuid) -eq $antes))
    $r = Radicar -Token $est1.Token -Asignaturas @($a1) -Pdf $pdf -ConSoporte $false -Extra @("-F", "$falsa=@$pdf;type=application/pdf")
    Escribir-Resultado "radicar con un anexo con tipo falla porque solo se admiten soportes libres (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*solo admite soportes libres*") -and ((Conteos $est1.Uuid) -eq $antes))

    $ultimo = Ejecutar-Sql "select coalesce(max(radicado), '') from SOLICITUD_ACADEMICA where radicado like '$anio-CA-%'"
    $consecutivo = if ($ultimo) { [int]$ultimo.Substring(8) + 1 } else { 1 }
    $esperado = "$anio-CA-" + $consecutivo.ToString("0000")
    $r = Radicar -Token $est1.Token -Asignaturas @($a1, $a2, $a3) -Pdf $pdf -ConSoporte $true
    $radicada = if ($r.Status -eq 200) { Leer-Json $r } else { $null }
    $sol1 = $radicada.uuidSolicitudAcademica
    Escribir-Resultado "radicar tres asignaturas con un soporte responde uuid y radicado $esperado (obtuvo $($r.Status) $($radicada.radicado))" (($r.Status -eq 200) -and $sol1 -and ($radicada.radicado -eq $esperado))
    Escribir-Resultado "la solicitud queda RADICADA (obtuvo $(Etapa-De $sol1))" ((Etapa-De $sol1) -eq "RADICADA")
    Verificar-EtapaHttp -Solicitud $sol1 -Esperada "RADICADA" -Estudiante $est1.Token -Funcionario $fa1.Token -Decano $decano.Token
    $anexos = Ejecutar-Sql "select count(*), sum(TipoAnexoAcademico_uuid is null) from ANEXO_ACADEMICO where SolicitudAcademica_uuid = '$sol1'"
    Escribir-Resultado "quedo el soporte libre y su archivo (obtuvo $anexos y $(@(Get-ChildItem (Join-Path $Global:CarpetaAnexos $sol1) -File).Count))" (($anexos -eq "1`t1") -and (@(Get-ChildItem (Join-Path $Global:CarpetaAnexos $sol1) -File).Count -eq 1))
    $f1 = Fila-De $sol1 $a1; $f2 = Fila-De $sol1 $a2; $f3 = Fila-De $sol1 $a3
    $total = Ejecutar-Sql "select count(*) from ASIGNATURA_SOLICITUD_ACADEMICA where SolicitudAcademica_uuid = '$sol1'"
    Escribir-Resultado "la solicitud incluye solo las tres asignaturas elegidas (obtuvo $total)" (($total -eq "3") -and $f1 -and $f2 -and $f3 -and -not (Fila-De $sol1 $a4))
    $r = Radicar -Token $est1.Token -Asignaturas @($a4) -Pdf $pdf -ConSoporte $false
    Escribir-Resultado "una segunda radicacion en curso se rechaza (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*en curso*") -and ((Ejecutar-Sql "select count(*) from SOLICITUD_ACADEMICA where Estudiante_uuid = '$($est1.Uuid)'") -eq "1"))

    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol1" -Token $est1.Token
    $detalle = Leer-Json $r
    Escribir-Resultado "el estudiante ve su detalle con motivo, etiqueta y tres asignaturas (obtuvo $($r.Status))" (($r.Status -eq 200) -and ($detalle.motivoCancelacion -eq "Cruce con horario laboral de prueba") -and ($detalle.solicitud.etiqueta -eq "En tr$([char]0x00E1)mite") -and (@($detalle.asignaturas).Count -eq 3))
    Escribir-Resultado "cada asignatura del detalle trae el uuid de su fila, codigo y nombre" (@($detalle.asignaturas | Where-Object { (@($f1, $f2, $f3) -contains $_.uuidAsignaturaSolicitud) -and $_.codigoAsignatura -and $_.nombreAsignatura }).Count -eq 3)
    $inexistente = [guid]::NewGuid().ToString()
    $firmaNoExiste = Firma-Error (Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$inexistente" -Token $est2.Token) $inexistente
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol1" -Token $est2.Token
    Escribir-Resultado "un estudiante ajeno no ve el detalle: responde igual que uno inexistente ($firmaNoExiste)" (($r.Status -ne 200) -and ((Firma-Error $r $sol1) -eq $firmaNoExiste))
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol1" -Token $decano.Token
    Escribir-Resultado "el Decano no ve una cancelacion RADICADA" ((Firma-Error $r $sol1) -eq $firmaNoExiste)
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol1" -Token $fa2.Token
    Escribir-Resultado "un funcionario no asignado no ve el detalle" ((Firma-Error $r $sol1) -eq $firmaNoExiste)
    $solCm = [guid]::NewGuid().ToString()
    Ejecutar-Sql ("insert into SOLICITUD_ACADEMICA (uuidSolicitudAcademica, radicado, Estudiante_uuid, TipoSolicitudAcademica_uuid, Etapa_uuid, fechaCreacion) " +
            "values ('$solCm', '1999-CM-$(Get-Random -Minimum 1000 -Maximum 9999)', '$($est2.Uuid)', '$tipoCm', (select uuidEtapa from ETAPA_SOLICITUD_ACADEMICA where codigo = 'RADICADA'), now())") | Out-Null
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$solCm" -Token $est2.Token
    Escribir-Resultado "una solicitud propia de otro tipo responde igual que una inexistente" ((Firma-Error $r $solCm) -eq $firmaNoExiste)
    Ejecutar-Sql "delete from SOLICITUD_ACADEMICA where uuidSolicitudAcademica = '$solCm'" | Out-Null

    $secreto = "Revision interna T7 del funcionario"
    $buenas = @((Evaluacion $f1 0 4.5 $situaciones[0] $true $secreto), (Evaluacion $f2 1 3.5 $situaciones[0] $true ""), (Evaluacion $f3 2 3.0 $situaciones[1] $true ""))
    $filasAntes = Estado-Filas $sol1
    $historialAntes = Ejecutar-Sql "select count(*) from HISTORIAL_SOLICITUD_ACADEMICA where SolicitudAcademica_uuid = '$sol1'"
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/funcionario/remitir" -Token $fa2.Token -Cuerpo @{ evaluaciones = $buenas }
    Escribir-Resultado "un funcionario no asignado no puede remitir (obtuvo $($r.Status), etapa $(Etapa-De $sol1))" (($r.Status -ne 200) -and ((Etapa-De $sol1) -eq "RADICADA") -and ((Estado-Filas $sol1) -eq $filasAntes))
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/funcionario/rechazar" -Token $fa2.Token -Cuerpo @{ observacion = "No me corresponde" }
    Escribir-Resultado "un funcionario no asignado no puede rechazar (obtuvo $($r.Status), etapa $(Etapa-De $sol1))" (($r.Status -ne 200) -and ((Etapa-De $sol1) -eq "RADICADA"))

    $malas = @((Evaluacion $f1 0 4.5 $situaciones[0] $true ""), (Evaluacion $f2 1 3.5 $situaciones[0] $true ""), (Evaluacion $f3 2 2.5 $situaciones[0] $true ""))
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/funcionario/remitir" -Token $fa1.Token -Cuerpo @{ evaluaciones = $malas }
    Escribir-Resultado "remitir con nota menor a 3.0 marcada como cumple falla (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*menor a 3.0*"))
    $historialDespues = Ejecutar-Sql "select count(*) from HISTORIAL_SOLICITUD_ACADEMICA where SolicitudAcademica_uuid = '$sol1'"
    Escribir-Resultado "esa remision no dejo evaluaciones, historial ni cambio de etapa (etapa $(Etapa-De $sol1), historial $historialDespues)" (((Etapa-De $sol1) -eq "RADICADA") -and ((Estado-Filas $sol1) -eq $filasAntes) -and ($historialDespues -eq $historialAntes))

    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/funcionario/remitir" -Token $fa1.Token -Cuerpo @{ observacion = "Condiciones verificadas"; evaluaciones = $buenas }
    $detalle = if ($r.Status -eq 200) { Leer-Json $r } else { $null }
    Escribir-Resultado "el funcionario asignado remite y pasa a EN_REVISION_DECANO (obtuvo $($r.Status), etapa $(Etapa-De $sol1))" (($r.Status -eq 200) -and ((Etapa-De $sol1) -eq "EN_REVISION_DECANO"))
    Verificar-EtapaHttp -Solicitud $sol1 -Esperada "EN_REVISION_DECANO" -Estudiante $est1.Token -Funcionario $fa1.Token -Decano $decano.Token
    $cumplen = Ejecutar-Sql "select count(*) from ASIGNATURA_SOLICITUD_ACADEMICA where SolicitudAcademica_uuid = '$sol1' and cumpleCondiciones = 1 and nota is not null and numeroFaltas is not null and aprobadaPorDecano is null"
    Escribir-Resultado "las tres evaluaciones quedaron en MySQL con cumpleCondiciones (obtuvo $cumplen)" ($cumplen -eq "3")
    Escribir-Resultado "la respuesta del funcionario trae la evaluacion y su observacion" (((Asignatura-De $detalle $f1).observacionEvaluacion -eq $secreto) -and ((Asignatura-De $detalle $f3).nota -eq 3.0) -and ((Asignatura-De $detalle $f2).cumpleCondiciones -eq $true))

    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol1" -Token $est1.Token
    Escribir-Resultado "en EN_REVISION_DECANO el estudiante solo ve las asignaturas, sin evaluacion ni observacion (obtuvo $($r.Status))" (($r.Status -eq 200) -and (Sin-Resultado (Leer-Json $r)) -and ($r.Body -notmatch $secreto) -and (@((Leer-Json $r).asignaturas).Count -eq 3))
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol1" -Token $decano.Token
    Escribir-Resultado "el Decano ve la evaluacion completa (obtuvo $($r.Status))" (($r.Status -eq 200) -and ((Asignatura-De (Leer-Json $r) $f1).observacionEvaluacion -eq $secreto) -and ((Asignatura-De (Leer-Json $r) $f1).situacionMatricula.uuidSituacionAcademica -eq $situaciones[0]))

    $motivoRechazo = "No procede para T7: la asignatura es prerrequisito"
    $filasAntes = Estado-Filas $sol1
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/decano/aprobar" -Token $decano.Token -Cuerpo @{ decisiones = @((Decision $f1 $true $situaciones[1] ""), (Decision $f2 $true $situaciones[1] ""), (Decision $f3 $false "" "")) }
    Escribir-Resultado "rechazar una asignatura sin motivo falla y no guarda nada (obtuvo $($r.Status), etapa $(Etapa-De $sol1))" (($r.Status -ne 200) -and ((Etapa-De $sol1) -eq "EN_REVISION_DECANO") -and ((Estado-Filas $sol1) -eq $filasAntes))
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/decano/aprobar" -Token $decano.Token -Cuerpo @{ decisiones = @((Decision $f1 $true $situaciones[1] ""), (Decision $f2 $true $situaciones[1] ""), (Decision $f3 $false $situaciones[1] $motivoRechazo)) }
    Escribir-Resultado "el Decano aprueba dos y rechaza una: pasa a APROBADA_POR_DECANO (obtuvo $($r.Status), etapa $(Etapa-De $sol1))" (($r.Status -eq 200) -and ((Etapa-De $sol1) -eq "APROBADA_POR_DECANO"))
    Verificar-EtapaHttp -Solicitud $sol1 -Esperada "APROBADA_POR_DECANO" -Estudiante $est1.Token -Funcionario $fa1.Token -Decano $decano.Token
    $aprobadas = Ejecutar-Sql "select count(*) from ASIGNATURA_SOLICITUD_ACADEMICA where SolicitudAcademica_uuid = '$sol1' and aprobadaPorDecano = 1 and SituacionCancelar_uuid = '$($situaciones[1])'"
    $rechazada = Ejecutar-Sql "select concat(aprobadaPorDecano, '|', coalesce(SituacionCancelar_uuid, '-'), '|', observacionDecision) from ASIGNATURA_SOLICITUD_ACADEMICA where uuidAsignaturaSolicitud = '$f3'"
    Escribir-Resultado "quedaron dos aprobadas con situacion y una rechazada con motivo y sin situacion (obtuvo $aprobadas y $rechazada)" (($aprobadas -eq "2") -and ($rechazada -eq "0|-|$motivoRechazo"))
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol1" -Token $est1.Token
    Escribir-Resultado "en APROBADA_POR_DECANO el estudiante aun no ve el resultado por asignatura" (($r.Status -eq 200) -and (Sin-Resultado (Leer-Json $r)) -and ($r.Body -notmatch "No procede para T7") -and ($r.Body -notmatch $secreto))
    $detalle = Leer-Json (Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol1" -Token $fa1.Token)
    Escribir-Resultado "el funcionario ve la decision por asignatura y la situacion al cancelar" (((Asignatura-De $detalle $f1).aprobadaPorDecano -eq $true) -and ((Asignatura-De $detalle $f1).situacionCancelar.uuidSituacionAcademica -eq $situaciones[1]) -and ((Asignatura-De $detalle $f3).aprobadaPorDecano -eq $false) -and ((Asignatura-De $detalle $f3).observacionDecision -eq $motivoRechazo) -and ($null -eq (Asignatura-De $detalle $f3).situacionCancelar))
    Escribir-Resultado "las asignaturas siguen activas tras la decision (obtuvo $(Estados @($a1, $a2, $a3, $a4)))" ((Estados @($a1, $a2, $a3, $a4)) -eq "activa,activa,activa,activa")

    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/funcionario/responder" -Token $fa1.Token
    Escribir-Resultado "enviar la respuesta sin escaneo falla (obtuvo $($r.Status), etapa $(Etapa-De $sol1))" (($r.Status -ne 200) -and ((Etapa-De $sol1) -eq "APROBADA_POR_DECANO") -and ((Estados @($a1, $a2, $a3, $a4)) -eq "activa,activa,activa,activa"))
    $r = Subir-Escaneo -Solicitud $sol1 -Pdf $pdfResolucion -Token $fa1.Token
    Escribir-Resultado "el funcionario sube el escaneo de la Resolucion (200, obtuvo $($r.Status))" ($r.Status -eq 200)
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/funcionario/responder" -Token $fa1.Token
    Escribir-Resultado "enviar la respuesta deja la solicitud APROBADA (obtuvo $($r.Status), etapa $(Etapa-De $sol1))" (($r.Status -eq 200) -and ((Etapa-De $sol1) -eq "APROBADA"))
    Verificar-EtapaHttp -Solicitud $sol1 -Esperada "APROBADA" -Estudiante $est1.Token -Funcionario $fa1.Token -Decano $decano.Token
    $estados = Estados @($a1, $a2, $a3, $a4, $noActiva)
    Escribir-Resultado "quedan canceladas las dos aprobadas; la rechazada y la cuarta siguen activas (obtuvo $estados)" ($estados -eq "cancelada,cancelada,activa,activa,cancelada")
    $d = Descargar -Ruta "solicitudes-academicas/$sol1/resolucion" -Token $est1.Token
    Escribir-Resultado "el estudiante descarga la Resolucion identica (200, obtuvo $($d.Status))" (($d.Status -eq 200) -and (Mismos-Bytes $pdfResolucion $d.Bytes))
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol1" -Token $est1.Token
    $detalle = Leer-Json $r
    Escribir-Resultado "en APROBADA el estudiante ve que asignaturas se aprobaron y el motivo del rechazo" (($detalle.solicitud.etiqueta -eq "Aprobada") -and ((Asignatura-De $detalle $f1).aprobadaPorDecano -eq $true) -and ((Asignatura-De $detalle $f2).aprobadaPorDecano -eq $true) -and ((Asignatura-De $detalle $f3).aprobadaPorDecano -eq $false) -and ((Asignatura-De $detalle $f3).observacionDecision -eq $motivoRechazo))
    Escribir-Resultado "el estudiante nunca ve la observacion de la evaluacion ni los datos del funcionario" (($r.Body -notmatch $secreto) -and (@($detalle.asignaturas | Where-Object { $_.observacionEvaluacion -or ($null -ne $_.cumpleCondiciones) -or ($null -ne $_.nota) }).Count -eq 0))
    $historial = @(Leer-Json (Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$sol1/historial" -Token $est1.Token)) | ForEach-Object { $_.accion }
    Escribir-Resultado "el historial trae RADICAR, REMITIR_DECANO, APROBAR_DECANO y ENVIAR_RESPUESTA (obtuvo $($historial -join ','))" (($historial -join ',') -eq "RADICAR,REMITIR_DECANO,APROBAR_DECANO,ENVIAR_RESPUESTA")
    $etapasHistorial = (@(Leer-Json (Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$sol1/historial" -Token $fa1.Token)) | ForEach-Object { "$($_.accion):$($_.etapaCodigo)" } | Sort-Object) -join ","; Escribir-Resultado "cada accion del historial trae la etapa a la que llevo (obtuvo $etapasHistorial)" ($etapasHistorial -eq "APROBAR_DECANO:APROBADA_POR_DECANO,ENVIAR_RESPUESTA:APROBADA,RADICAR:RADICADA,REMITIR_DECANO:EN_REVISION_DECANO")

    $activas2 = Activas $est2
    $r = Radicar -Token $est2.Token -Asignaturas $activas2 -Pdf $pdf -ConSoporte $false
    $sol2 = (Leer-Json $r).uuidSolicitudAcademica
    Escribir-Resultado "el estudiante 2 radica sin soporte (200, obtuvo $($r.Status))" (($r.Status -eq 200) -and $sol2)
    Verificar-EtapaHttp -Solicitud $sol2 -Esperada "RADICADA" -Estudiante $est2.Token -Funcionario $fa1.Token -Decano $decano.Token
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol2/funcionario/rechazar" -Token $fa1.Token -Cuerpo @{ observacion = "No cumple las condiciones" }
    Escribir-Resultado "rechazar sin escaneo falla y sigue RADICADA (obtuvo $($r.Status))" (($r.Status -ne 200) -and ((Etapa-De $sol2) -eq "RADICADA"))
    $r = Subir-Escaneo -Solicitud $sol2 -Pdf $pdfResolucion -Token $fa1.Token
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol2/funcionario/rechazar" -Token $fa1.Token -Cuerpo @{ observacion = "No cumple las condiciones" }
    Escribir-Resultado "el rechazo del funcionario con escaneo deja la solicitud RECHAZADA (obtuvo $($r.Status), etapa $(Etapa-De $sol2))" (($r.Status -eq 200) -and ((Etapa-De $sol2) -eq "RECHAZADA"))
    Verificar-EtapaHttp -Solicitud $sol2 -Esperada "RECHAZADA" -Estudiante $est2.Token -Funcionario $fa1.Token -Decano $decano.Token
    Escribir-Resultado "tras el rechazo del funcionario las asignaturas siguen activas (obtuvo $(Estados $activas2))" ((Estados $activas2) -eq "activa,activa")
    $d = Descargar -Ruta "solicitudes-academicas/$sol2/resolucion" -Token $est2.Token
    Escribir-Resultado "el estudiante 2 descarga la Resolucion del rechazo (200, obtuvo $($d.Status))" (($d.Status -eq 200) -and (Mismos-Bytes $pdfResolucion $d.Bytes))
    $detalle = Leer-Json (Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol2" -Token $est2.Token)
    Escribir-Resultado "el estudiante 2 ve Rechazada sin decision por asignatura" (($detalle.solicitud.etiqueta -eq "Rechazada") -and (Sin-Resultado $detalle))

    $activas3 = Activas $est3
    $r = Radicar -Token $est3.Token -Asignaturas $activas3 -Pdf $pdf -ConSoporte $true
    $sol3 = (Leer-Json $r).uuidSolicitudAcademica
    $g1 = Fila-De $sol3 $activas3[0]; $g2 = Fila-De $sol3 $activas3[1]
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol3/funcionario/remitir" -Token $fa1.Token -Cuerpo @{ evaluaciones = @((Evaluacion $g1 1 4.0 $situaciones[0] $true ""), (Evaluacion $g2 9 2.0 $situaciones[0] $false "Nota insuficiente")) }
    Escribir-Resultado "el estudiante 3 radica y el funcionario remite con una que no cumple (obtuvo $($r.Status), etapa $(Etapa-De $sol3))" (($r.Status -eq 200) -and ((Etapa-De $sol3) -eq "EN_REVISION_DECANO"))
    Verificar-EtapaHttp -Solicitud $sol3 -Esperada "EN_REVISION_DECANO" -Estudiante $est3.Token -Funcionario $fa1.Token -Decano $decano.Token
    $filasAntes = Estado-Filas $sol3
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol3/decano/aprobar" -Token $decano.Token -Cuerpo @{ decisiones = @((Decision $g1 $true $situaciones[1] ""), (Decision $g2 $true $situaciones[1] "")) }
    Escribir-Resultado "el Decano no puede aprobar una asignatura que no cumple (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*no se puede aprobar*"))
    Escribir-Resultado "ese intento no guardo decisiones ni cambio la etapa (etapa $(Etapa-De $sol3))" (((Etapa-De $sol3) -eq "EN_REVISION_DECANO") -and ((Estado-Filas $sol3) -eq $filasAntes))
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol3/decano/rechazar" -Token $decano.Token -Cuerpo @{ observacion = "" }
    Escribir-Resultado "el Decano no rechaza sin observacion (obtuvo $($r.Status))" (($r.Status -ne 200) -and ((Etapa-De $sol3) -eq "EN_REVISION_DECANO"))
    $motivoDecano = "No procede: la causa no esta soportada"
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol3/decano/rechazar" -Token $decano.Token -Cuerpo @{ observacion = $motivoDecano }
    $sinDecision = Ejecutar-Sql "select count(*) from ASIGNATURA_SOLICITUD_ACADEMICA where SolicitudAcademica_uuid = '$sol3' and aprobadaPorDecano is null and SituacionCancelar_uuid is null"
    Escribir-Resultado "el rechazo total del Decano pasa a RECHAZADA_POR_DECANO sin decisiones por asignatura (obtuvo $($r.Status), etapa $(Etapa-De $sol3), filas $sinDecision)" (($r.Status -eq 200) -and ((Etapa-De $sol3) -eq "RECHAZADA_POR_DECANO") -and ($sinDecision -eq "2"))
    Verificar-EtapaHttp -Solicitud $sol3 -Esperada "RECHAZADA_POR_DECANO" -Estudiante $est3.Token -Funcionario $fa1.Token -Decano $decano.Token
    $r = Subir-Escaneo -Solicitud $sol3 -Pdf $pdfResolucion -Token $fa1.Token
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol3/funcionario/responder" -Token $fa1.Token
    Escribir-Resultado "la respuesta final deja la solicitud RECHAZADA (obtuvo $($r.Status), etapa $(Etapa-De $sol3))" (($r.Status -eq 200) -and ((Etapa-De $sol3) -eq "RECHAZADA"))
    Verificar-EtapaHttp -Solicitud $sol3 -Esperada "RECHAZADA" -Estudiante $est3.Token -Funcionario $fa1.Token -Decano $decano.Token
    Escribir-Resultado "tras el rechazo del Decano las asignaturas siguen activas (obtuvo $(Estados $activas3))" ((Estados $activas3) -eq "activa,activa")
    $filaDecano = @(Leer-Json (Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$sol3/historial" -Token $est3.Token)) | Where-Object { $_.accion -eq "RECHAZAR_DECANO" }
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol3" -Token $est3.Token
    Escribir-Resultado "el estudiante 3 ve el motivo del Decano en el historial y no la observacion de la evaluacion" (($filaDecano.observaciones -eq $motivoDecano) -and ($r.Body -notmatch "Nota insuficiente") -and (Sin-Resultado (Leer-Json $r)))
    $etapasHistorial = (@(Leer-Json (Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$sol3/historial" -Token $fa1.Token)) | ForEach-Object { "$($_.accion):$($_.etapaCodigo)" } | Sort-Object) -join ","; Escribir-Resultado "cada accion del historial trae la etapa a la que llevo (obtuvo $etapasHistorial)" ($etapasHistorial -eq "ENVIAR_RESPUESTA:RECHAZADA,RADICAR:RADICADA,RECHAZAR_DECANO:RECHAZADA_POR_DECANO,REMITIR_DECANO:EN_REVISION_DECANO")

    $activas4 = Activas $est4
    Ejecutar-Sql "create trigger $Global:Trigger before insert on ANEXO_ACADEMICO for each row set NEW.tipoArchivo = if(NEW.nombreArchivo = '$Global:ArchivoFalla', null, NEW.tipoArchivo)" | Out-Null
    $hayTrigger = Ejecutar-Sql "select count(*) from information_schema.TRIGGERS where TRIGGER_SCHEMA = 'cfiet' and TRIGGER_NAME = '$Global:Trigger'"
    Escribir-Resultado "se preparo el trigger temporal que hace fallar el segundo soporte (obtuvo $hayTrigger)" ($hayTrigger -eq "1")
    $antes = Conteos $est4.Uuid
    $r = Radicar -Token $est4.Token -Asignaturas $activas4 -Pdf $pdf -ConSoporte $true -Extra @("-F", "soporte=@$pdf;filename=$Global:ArchivoFalla;type=application/pdf")
    Escribir-Resultado "una falla al guardar el segundo soporte hace fallar la radicacion (obtuvo $($r.Status))" ($r.Status -ne 200)
    $despues = Conteos $est4.Uuid
    Escribir-Resultado "la falla a mitad de camino no dejo solicitud, historial, asignaturas, anexos ni archivos ($despues)" ($despues -eq $antes)
    Quitar-Trigger
    $r = Radicar -Token $est4.Token -Asignaturas $activas4 -Pdf $pdf -ConSoporte $true
    Escribir-Resultado "sin el trigger el estudiante 4 radica normalmente: no quedo una solicitud en curso (obtuvo $($r.Status))" ($r.Status -eq 200)
}
finally {
    Quitar-Trigger
    $quedaTrigger = Ejecutar-Sql "select count(*) from information_schema.TRIGGERS where TRIGGER_SCHEMA = 'cfiet' and TRIGGER_NAME = '$Global:Trigger'"
    Escribir-Resultado "no quedo el trigger temporal (obtuvo $quedaTrigger)" ($quedaTrigger -eq "0")

    if ($responsableCa) {
        $r = Invoke-Api -Metodo PUT -Ruta "catalogos-academicos/tipos-solicitud/$tipoCa/funcionario" -Token $tokenRoot -Cuerpo @{ funcionarioUuid = $responsableCa }
        if ($r.Status -ne 200) { Write-Host "No se pudo restaurar el responsable: $($r.Status) $($r.Body)" }
    } else {
        Ejecutar-Sql "update TIPO_SOLICITUD_ACADEMICA set FuncionarioAcademico_uuid = null where uuidTipoSolicitudAcademica = '$tipoCa'" | Out-Null
    }
    $actual = Ejecutar-Sql "select coalesce(FuncionarioAcademico_uuid, '') from TIPO_SOLICITUD_ACADEMICA where uuidTipoSolicitudAcademica = '$tipoCa'"
    Escribir-Resultado "Cancelacion de Asignatura quedo con su responsable original" ($actual -eq $responsableCa)

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
        $sentencias += "delete from SOLICITUD_CANCELACION_ASIGNATURA where SolicitudAcademica_uuid in ($sol)"
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
