. "$PSScriptRoot\comun.ps1"

$Global:CarpetaBackend = (Resolve-Path "$PSScriptRoot\..").Path
$Global:CarpetaAnexos = Join-Path $Global:CarpetaBackend "uploads\anexos"
$Global:Ruta = "examenes-supletorios"
$Global:Trigger = "t8_falla_mitad"
$Global:ArchivoFalla = "t8_falla_mitad.pdf"

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
    for ($i = 1; $i -le $Materias; $i++) { $asignaturas += @{ codigoAsignatura = "T8$s$i"; nombreAsignatura = "Materia T8 $i $s"; grupo = "A" } }
    $cuerpo = @{
        nombres           = "Supletorio"
        apellidos         = "Prueba $s"
        tipoDocumento     = $Global:CedulaCiudadania
        numeroDocumento   = "5$s"
        telefono          = "+573000000000"
        correoElectronico = "sup$s@unicauca.edu.co"
        username          = "sup$s"
        password          = "Clave12345"
        codigoEstudiantil = "S$s"
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
        Token   = Iniciar-Sesion -Usuario "sup$s" -Clave "Clave12345"
    }
}

function Nuevo-Funcionario {
    param([string]$Token)
    $s = Nuevo-Sufijo
    $cuerpo = @{
        nombres           = "Tramitador"
        apellidos         = "Supletorio $s"
        tipoDocumento     = $Global:CedulaCiudadania
        numeroDocumento   = "2$s"
        telefono          = "+573000000000"
        correoElectronico = "tsu$s@unicauca.edu.co"
        username          = "tsu$s"
        password          = "Clave12345"
        dependencia       = "DepT8 $s"
    }
    $r = Invoke-Api -Metodo POST -Ruta "funcionarios-academicos" -Token $Token -Cuerpo $cuerpo
    if ($r.Status -ne 200) { throw "No se pudo crear el funcionario academico ($($r.Status)): $($r.Body)" }
    return [pscustomobject]@{
        Uuid  = (Leer-Json $r).uuidUsuario
        Token = Iniciar-Sesion -Usuario "tsu$s" -Clave "Clave12345"
    }
}

function Nuevo-Pdf {
    param([string]$Texto)
    $ruta = Join-Path ([System.IO.Path]::GetTempPath()) ("t8_" + (Nuevo-Sufijo) + ".pdf")
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
    param([string]$Token, [string]$Asignatura, [string]$Fecha, [string]$Causa, [string[]]$Tipos, [string]$Pdf,
          [string]$Cruzada = "", [string]$FechaCruzada = "", [string]$Hora = "", [string[]]$Extra = @())
    $argumentos = @()
    if ($Asignatura) { $argumentos += @("-F", "asignaturaMatriculada=$Asignatura") }
    if ($Fecha) { $argumentos += @("-F", "fechaExamenNoPresentado=$Fecha") }
    if ($Causa) { $argumentos += @("-F", "tipoCausa=$Causa") }
    if ($Cruzada) { $argumentos += @("-F", "asignaturaCruzada=$Cruzada") }
    if ($FechaCruzada) { $argumentos += @("-F", "fechaExamenCruzada=$FechaCruzada") }
    if ($Hora) { $argumentos += @("-F", "horaExamenCruzada=$Hora") }
    foreach ($tipo in $Tipos) { $argumentos += @("-F", "$tipo=@$Pdf;type=application/pdf") }
    $argumentos += $Extra
    return Ejecutar-Curl -Argumentos $argumentos -Ruta $Global:Ruta -Token $Token
}

function Subir-Anexo {
    param([string]$Solicitud, [string]$Tipo, [string]$Pdf, [string]$Token)
    return Ejecutar-Curl -Argumentos @("-F", "archivo=@$Pdf;type=application/pdf", "-F", "tipoAnexo=$Tipo") -Ruta "solicitudes-academicas/$Solicitud/anexos" -Token $Token
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

function Historial-De {
    param([string]$Solicitud, [string]$Token)
    $acciones = @(Leer-Json (Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$Solicitud/historial" -Token $Token)) | ForEach-Object { $_.accion }
    return ((@($acciones) | Sort-Object) -join ",")
}

function Archivos-Subidos {
    if (-not (Test-Path $Global:CarpetaAnexos)) { return 0 }
    return @(Get-ChildItem $Global:CarpetaAnexos -Recurse -File).Count
}

function Conteos {
    param([string]$Estudiante)
    return "sol=" + (Ejecutar-Sql "select count(*) from SOLICITUD_ACADEMICA where Estudiante_uuid = '$Estudiante'") +
            " hist=" + (Ejecutar-Sql "select count(*) from HISTORIAL_SOLICITUD_ACADEMICA") +
            " sup=" + (Ejecutar-Sql "select count(*) from SOLICITUD_EXAMEN_SUPLETORIO") +
            " cruce=" + (Ejecutar-Sql "select count(*) from SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA") +
            " anexos=" + (Ejecutar-Sql "select count(*) from ANEXO_ACADEMICO") +
            " archivos=" + (Archivos-Subidos)
}

function Activas {
    param($Estudiante)
    $formulario = Leer-Json (Invoke-Api -Metodo GET -Ruta "$Global:Ruta/formulario" -Token $Estudiante.Token)
    return @($formulario.asignaturas | Sort-Object codigoAsignatura | ForEach-Object { $_.uuidAsignaturaMatriculada })
}

function Estados {
    param([string[]]$Estudiantes)
    return (@(Ejecutar-Sql "select estado from ASIGNATURA_MATRICULADA where Estudiante_uuid in ($(Lista-Sql $Estudiantes)) order by uuidAsignaturaMatriculada") -join ",")
}

function Anexo-De {
    param($Detalle, [string]$Tipo)
    return @($Detalle.solicitud.anexos | Where-Object { $_.uuidTipoAnexoAcademico -eq $Tipo })[0]
}

function Tipo-Anexo {
    param([string]$Patron)
    return Ejecutar-Sql ("select a.uuidTipoAnexoAcademico from TIPO_ANEXO_ACADEMICO a join TIPO_SOLICITUD_ACADEMICA t on t.uuidTipoSolicitudAcademica = a.TipoSolicitudAcademica_uuid " +
            "where t.nombre like 'Examen%' and a.nombre like '$Patron'")
}

function Quitar-Trigger {
    Ejecutar-Sql "drop trigger if exists $Global:Trigger" | Out-Null
}

$tokenRoot = Iniciar-Sesion -Usuario "rootfiet" -Clave "rootfiet1234"
$tipoEs = Ejecutar-Sql "select uuidTipoSolicitudAcademica from TIPO_SOLICITUD_ACADEMICA where nombre like 'Examen Supletorio'"
$tipoCm = Ejecutar-Sql "select uuidTipoSolicitudAcademica from TIPO_SOLICITUD_ACADEMICA where nombre like 'Cancelaci%n de Matr%cula'"
$for23 = Tipo-Anexo "Formato PM-FO-4-FOR-23%"
$justificacion = Tipo-Anexo "Soporte de la justificaci%"
$docente = Tipo-Anexo "Formato firmado por el docente%"
$recibo = Tipo-Anexo "Recibo de pago"
$comprobante = Tipo-Anexo "Comprobante de pago"
$responsableEs = Ejecutar-Sql "select coalesce(FuncionarioAcademico_uuid, '') from TIPO_SOLICITUD_ACADEMICA where uuidTipoSolicitudAcademica = '$tipoEs'"
if (-not $tipoEs -or -not $tipoCm -or -not $for23 -or -not $justificacion -or -not $docente -or -not $recibo -or -not $comprobante) { Write-Host "FAIL faltan los catalogos sembrados de Examen Supletorio"; exit 1 }
$habiaAnexos = Test-Path $Global:CarpetaAnexos
$ahora = [System.TimeZoneInfo]::ConvertTimeBySystemTimeZoneId([DateTime]::UtcNow, "SA Pacific Standard Time")
$hoy = $ahora.ToString("yyyy-MM-dd")
$vencida = $ahora.AddDays(-10).ToString("yyyy-MM-dd")
$futura = $ahora.AddDays(1).ToString("yyyy-MM-dd")
$acordada = $ahora.AddDays(7).ToString("yyyy-MM-dd")
$anteriorAlExamen = $ahora.AddDays(-1).ToString("yyyy-MM-dd")
$anio = $ahora.Year

$usuarios = @()
$estudiantes = @()
$funcionarios = @()
$codigos = @()
$archivos = @()

try {
    $est1 = Nuevo-Estudiante -Token $tokenRoot -Materias 3
    $est2 = Nuevo-Estudiante -Token $tokenRoot -Materias 1
    $est3 = Nuevo-Estudiante -Token $tokenRoot -Materias 1
    $est4 = Nuevo-Estudiante -Token $tokenRoot -Materias 1
    $est5 = Nuevo-Estudiante -Token $tokenRoot -Materias 1
    $fa1 = Nuevo-Funcionario -Token $tokenRoot
    $fa2 = Nuevo-Funcionario -Token $tokenRoot
    $decano = Crear-UsuarioPrueba -Perfil "Decano" -TokenAdmin $tokenRoot -Prefijo "dect8"
    $estudiantes = @($est1.Uuid, $est2.Uuid, $est3.Uuid, $est4.Uuid, $est5.Uuid)
    $funcionarios = @($fa1.Uuid, $fa2.Uuid)
    $codigos = @($est1.Codigos + $est2.Codigos + $est3.Codigos + $est4.Codigos + $est5.Codigos)
    $usuarios = @($estudiantes + $funcionarios + $decano.Uuid)

    $r = Invoke-Api -Metodo PUT -Ruta "catalogos-academicos/tipos-solicitud/$tipoEs/funcionario" -Token $tokenRoot -Cuerpo @{ funcionarioUuid = $fa1.Uuid }
    if ($r.Status -ne 200) { throw "No se pudo asignar Examen Supletorio al funcionario de prueba ($($r.Status)): $($r.Body)" }

    $pdf = Nuevo-Pdf "anexo"; $archivos += $pdf
    $pdfRecibo = Nuevo-Pdf "recibo"; $archivos += $pdfRecibo
    $pdfComprobante = Nuevo-Pdf "comprobante"; $archivos += $pdfComprobante
    $falsa = [guid]::NewGuid().ToString()
    $observacion = @{ observacion = "Observacion de prueba" }

    $casos = @(
        @{ Nombre = "GET /formulario"; Metodo = "GET"; Ruta = "$Global:Ruta/formulario"; Cuerpo = $null; Prohibidos = @{ "Funcionario Academico" = $fa1.Token; "Decano" = $decano.Token; "Secretario General" = $tokenRoot } },
        @{ Nombre = "POST / (radicar)"; Metodo = "MULTIPART"; Ruta = $Global:Ruta; Cuerpo = $null; Prohibidos = @{ "Funcionario Academico" = $fa1.Token; "Decano" = $decano.Token; "Secretario General" = $tokenRoot } },
        @{ Nombre = "GET /{uuid}"; Metodo = "GET"; Ruta = "$Global:Ruta/$falsa"; Cuerpo = $null; Prohibidos = @{ "Secretario General" = $tokenRoot } },
        @{ Nombre = "POST funcionario/rechazar"; Metodo = "POST"; Ruta = "$Global:Ruta/$falsa/funcionario/rechazar"; Cuerpo = $observacion; Prohibidos = @{ "Estudiante" = $est1.Token; "Decano" = $decano.Token; "Secretario General" = $tokenRoot } },
        @{ Nombre = "POST funcionario/remitir"; Metodo = "POST"; Ruta = "$Global:Ruta/$falsa/funcionario/remitir"; Cuerpo = @{ requisitosVerificados = $true }; Prohibidos = @{ "Estudiante" = $est1.Token; "Decano" = $decano.Token; "Secretario General" = $tokenRoot } },
        @{ Nombre = "POST funcionario/responder"; Metodo = "POST"; Ruta = "$Global:Ruta/$falsa/funcionario/responder"; Cuerpo = $null; Prohibidos = @{ "Estudiante" = $est1.Token; "Decano" = $decano.Token; "Secretario General" = $tokenRoot } },
        @{ Nombre = "POST funcionario/recibo"; Metodo = "POST"; Ruta = "$Global:Ruta/$falsa/funcionario/recibo"; Cuerpo = $null; Prohibidos = @{ "Estudiante" = $est1.Token; "Decano" = $decano.Token; "Secretario General" = $tokenRoot } },
        @{ Nombre = "POST funcionario/comprobante/aprobar"; Metodo = "POST"; Ruta = "$Global:Ruta/$falsa/funcionario/comprobante/aprobar"; Cuerpo = @{ fechaAcordadaExamen = $acordada }; Prohibidos = @{ "Estudiante" = $est1.Token; "Decano" = $decano.Token; "Secretario General" = $tokenRoot } },
        @{ Nombre = "POST funcionario/comprobante/rechazar"; Metodo = "POST"; Ruta = "$Global:Ruta/$falsa/funcionario/comprobante/rechazar"; Cuerpo = $observacion; Prohibidos = @{ "Estudiante" = $est1.Token; "Decano" = $decano.Token; "Secretario General" = $tokenRoot } },
        @{ Nombre = "POST decano/aprobar"; Metodo = "POST"; Ruta = "$Global:Ruta/$falsa/decano/aprobar"; Cuerpo = @{}; Prohibidos = @{ "Estudiante" = $est1.Token; "Funcionario Academico" = $fa1.Token; "Secretario General" = $tokenRoot } },
        @{ Nombre = "POST decano/rechazar"; Metodo = "POST"; Ruta = "$Global:Ruta/$falsa/decano/rechazar"; Cuerpo = $observacion; Prohibidos = @{ "Estudiante" = $est1.Token; "Funcionario Academico" = $fa1.Token; "Secretario General" = $tokenRoot } },
        @{ Nombre = "POST estudiante/comprobante"; Metodo = "POST"; Ruta = "$Global:Ruta/$falsa/estudiante/comprobante"; Cuerpo = $null; Prohibidos = @{ "Funcionario Academico" = $fa1.Token; "Decano" = $decano.Token; "Secretario General" = $tokenRoot } },
        @{ Nombre = "GET / (sin endpoint)"; Metodo = "GET"; Ruta = $Global:Ruta; Cuerpo = $null; Prohibidos = @{ "Estudiante" = $est1.Token; "Funcionario Academico" = $fa1.Token; "Decano" = $decano.Token } },
        @{ Nombre = "POST /{uuid}/otra (sin endpoint)"; Metodo = "POST"; Ruta = "$Global:Ruta/$falsa/otra"; Cuerpo = $null; Prohibidos = @{ "Estudiante" = $est1.Token; "Funcionario Academico" = $fa1.Token; "Decano" = $decano.Token } }
    )
    $antesPermisos = Conteos $est1.Uuid
    foreach ($caso in $casos) {
        if ($caso.Metodo -eq "MULTIPART") { $r = Radicar -Token $null -Asignatura $falsa -Fecha $hoy -Causa "otra" -Tipos @($for23) -Pdf $pdf }
        else { $r = Invoke-Api -Metodo $caso.Metodo -Ruta $caso.Ruta -Cuerpo $caso.Cuerpo }
        if ($caso.Nombre -notlike "*sin endpoint*") { Escribir-Resultado "sin token en $($caso.Nombre) (401, obtuvo $($r.Status))" ($r.Status -eq 401) }
        foreach ($rol in $caso.Prohibidos.Keys) {
            if ($caso.Metodo -eq "MULTIPART") { $r = Radicar -Token $caso.Prohibidos[$rol] -Asignatura $falsa -Fecha $hoy -Causa "otra" -Tipos @($for23) -Pdf $pdf }
            else { $r = Invoke-Api -Metodo $caso.Metodo -Ruta $caso.Ruta -Token $caso.Prohibidos[$rol] -Cuerpo $caso.Cuerpo }
            Escribir-Resultado "$rol en $($caso.Nombre) (403, obtuvo $($r.Status))" ($r.Status -eq 403)
        }
    }
    Escribir-Resultado "los intentos sin permiso no dejaron nada ($antesPermisos)" ((Conteos $est1.Uuid) -eq $antesPermisos)

    $todas = Activas $est1
    Escribir-Resultado "el formulario trae al inicio las tres asignaturas activas (obtuvo $($todas.Count))" ($todas.Count -eq 3)
    $noActiva = $todas[2]
    Ejecutar-Sql "update ASIGNATURA_MATRICULADA set estado = 'cancelada' where uuidAsignaturaMatriculada = '$noActiva'" | Out-Null
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/formulario" -Token $est1.Token
    $formulario = Leer-Json $r
    $materias = @($formulario.asignaturas)
    Escribir-Resultado "el formulario trae solo las dos activas con uuid, codigo y nombre (obtuvo $($r.Status) con $($materias.Count))" (($r.Status -eq 200) -and ($materias.Count -eq 2) -and (@($materias | Where-Object { $_.uuidAsignaturaMatriculada -and ($est1.Codigos -contains $_.codigoAsignatura) -and $_.nombreAsignatura }).Count -eq 2) -and (@($materias | Where-Object { $_.uuidAsignaturaMatriculada -eq $noActiva }).Count -eq 0))
    Escribir-Resultado "el formulario trae las causas cruce y otra y el plazo de 3 dias habiles (obtuvo $(@($formulario.causas) -join ',') y $($formulario.plazoDiasHabiles))" (((@($formulario.causas) -join ',') -eq "cruce,otra") -and ($formulario.plazoDiasHabiles -eq 3))
    $anexosForm = @($formulario.anexos)
    $porTipo = @{}
    foreach ($anexo in $anexosForm) { $porTipo[$anexo.uuidTipoAnexoAcademico] = $anexo }
    Escribir-Resultado "el formulario trae los tres anexos de radicacion con formatos y 5 MB (obtuvo $($anexosForm.Count))" (($anexosForm.Count -eq 3) -and $porTipo[$for23] -and $porTipo[$justificacion] -and $porTipo[$docente] -and (@($anexosForm | Where-Object { $_.formatosPermitidos -and $_.tamanioMaximoBytes -eq 5242880 -and $_.nombre }).Count -eq 3))
    Escribir-Resultado "cada anexo dice con que causa se exige (FOR-23 ambas, justificacion otra, docente cruce)" (((@($porTipo[$for23].causas) -join ',') -eq "cruce,otra") -and ((@($porTipo[$justificacion].causas) -join ',') -eq "otra") -and ((@($porTipo[$docente].causas) -join ',') -eq "cruce"))
    Escribir-Resultado "el formulario no trae recibo ni comprobante, ni rol ni tipo de usuario" (($r.Body -notmatch $recibo) -and ($r.Body -notmatch $comprobante) -and ($r.Body -notmatch '"rol') -and ($r.Body -notmatch 'tipoUsuario'))
    $a1 = $materias[0].uuidAsignaturaMatriculada; $a2 = $materias[1].uuidAsignaturaMatriculada
    $ajena = @(Activas $est2)[0]

    $antes = Conteos $est1.Uuid
    $inexistente = [guid]::NewGuid().ToString()
    $r = Radicar -Token $est1.Token -Asignatura $ajena -Fecha $hoy -Causa "otra" -Tipos @($for23, $justificacion) -Pdf $pdf
    $rInexistente = Radicar -Token $est1.Token -Asignatura $inexistente -Fecha $hoy -Causa "otra" -Tipos @($for23, $justificacion) -Pdf $pdf
    Escribir-Resultado "una asignatura ajena se rechaza (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*no es una asignatura matriculada del estudiante*"))
    Escribir-Resultado "ajena e inexistente dan el mismo mensaje" ((Firma-Error $r $ajena) -eq (Firma-Error $rInexistente $inexistente))
    $r = Radicar -Token $est1.Token -Asignatura $noActiva -Fecha $hoy -Causa "otra" -Tipos @($for23, $justificacion) -Pdf $pdf
    Escribir-Resultado "una asignatura no activa se rechaza nombrando su estado (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*cancelada y solo se aceptan asignaturas activas*"))
    $r = Radicar -Token $est1.Token -Asignatura $a1 -Fecha $vencida -Causa "otra" -Tipos @($for23, $justificacion) -Pdf $pdf
    $fechaVencida = $ahora.AddDays(-10).ToString("dd/MM/yyyy")
    Escribir-Resultado "un examen de hace 10 dias esta vencido y el mensaje dice su fecha y el ultimo dia (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "El plazo para pedir el supletorio del examen del $fechaVencida*ltimo d*a permitido era el *"))
    $r = Radicar -Token $est1.Token -Asignatura $a1 -Fecha $futura -Causa "otra" -Tipos @($for23, $justificacion) -Pdf $pdf
    Escribir-Resultado "una fecha de examen futura se rechaza (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*no puede ser posterior a hoy*"))
    $r = Radicar -Token $est1.Token -Asignatura $a1 -Fecha $ahora.ToString("dd/MM/yyyy") -Causa "otra" -Tipos @($for23, $justificacion) -Pdf $pdf
    Escribir-Resultado "una fecha con otro formato se rechaza (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*AAAA-MM-DD*"))
    $r = Radicar -Token $est1.Token -Asignatura $a1 -Fecha $hoy -Causa "enfermedad" -Tipos @($for23, $justificacion) -Pdf $pdf
    Escribir-Resultado "una causa distinta de cruce u otra se rechaza (obtuvo $($r.Status))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*debe ser cruce u otra*"))
    $r = Radicar -Token $est1.Token -Asignatura $a1 -Fecha $hoy -Causa "otra" -Tipos @($for23) -Pdf $pdf
    Escribir-Resultado "con causa otra falta el soporte de justificacion (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "Falta el anexo obligatorio: Soporte de la justificaci*"))
    $r = Radicar -Token $est1.Token -Asignatura $a1 -Fecha $hoy -Causa "cruce" -Cruzada $a2 -FechaCruzada $hoy -Hora "10:30" -Tipos @($docente) -Pdf $pdf
    Escribir-Resultado "con causa cruce falta el FOR-23 (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "Falta el anexo obligatorio: Formato PM-FO-4-FOR-23*"))
    $r = Radicar -Token $est1.Token -Asignatura $a1 -Fecha $hoy -Causa "otra" -Tipos @($for23, $justificacion, $docente) -Pdf $pdf
    Escribir-Resultado "con causa otra el formato del docente cruzado se rechaza (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*no corresponde a la causa otra*"))
    $r = Radicar -Token $est1.Token -Asignatura $a1 -Fecha $hoy -Causa "cruce" -Cruzada $a2 -FechaCruzada $hoy -Hora "10:30" -Tipos @($for23, $docente, $justificacion) -Pdf $pdf
    Escribir-Resultado "con causa cruce el soporte de justificacion se rechaza (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*no corresponde a la causa cruce*"))
    $r = Radicar -Token $est1.Token -Asignatura $a1 -Fecha $hoy -Causa "otra" -Tipos @($for23, $justificacion, $recibo) -Pdf $pdf
    Escribir-Resultado "el recibo de pago no se acepta al radicar (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*no se entrega al radicar*"))
    $r = Radicar -Token $est1.Token -Asignatura $a1 -Fecha $hoy -Causa "cruce" -Cruzada $a1 -FechaCruzada $hoy -Hora "10:30" -Tipos @($for23, $docente) -Pdf $pdf
    Escribir-Resultado "la asignatura cruzada no puede ser la misma del examen (obtuvo $($r.Status))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*debe ser distinta*"))
    $r = Radicar -Token $est1.Token -Asignatura $a1 -Fecha $hoy -Causa "cruce" -Cruzada $a2 -FechaCruzada $hoy -Hora "7:30" -Tipos @($for23, $docente) -Pdf $pdf
    Escribir-Resultado "una hora del cruce sin formato HH:mm se rechaza (obtuvo $($r.Status))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*HH:mm*"))
    Escribir-Resultado "ningun intento fallido dejo solicitud, historial, especializacion, cruce, anexos ni archivos ($(Conteos $est1.Uuid))" ((Conteos $est1.Uuid) -eq $antes)

    $ultimo = Ejecutar-Sql "select coalesce(max(radicado), '') from SOLICITUD_ACADEMICA where radicado like '$anio-ES-%'"
    $consecutivo = if ($ultimo) { [int]$ultimo.Substring(8) + 1 } else { 1 }
    $esperado = "$anio-ES-" + $consecutivo.ToString("0000")
    $r = Radicar -Token $est1.Token -Asignatura $a1 -Fecha $hoy -Causa "cruce" -Cruzada $a2 -FechaCruzada $hoy -Hora "10:30" -Tipos @($for23, $docente) -Pdf $pdf
    $radicada = if ($r.Status -eq 200) { Leer-Json $r } else { $null }
    $sol1 = $radicada.uuidSolicitudAcademica
    Escribir-Resultado "la radicacion por cruce responde uuid y radicado $esperado (obtuvo $($r.Status) $($radicada.radicado) $(Mensaje-De $r))" (($r.Status -eq 200) -and $sol1 -and ($radicada.radicado -eq $esperado))
    Escribir-Resultado "la solicitud queda RADICADA con su cruce en MySQL (obtuvo $(Etapa-De $sol1))" (((Etapa-De $sol1) -eq "RADICADA") -and ((Ejecutar-Sql "select concat(c.AsignaturaMatriculadaCruzada_uuid, '|', c.horaExamenCruzada, '|', s.tipoCausa, '|', coalesce(s.fechaAcordadaExamen, '-')) from SOLICITUD_EXAMEN_SUPLETORIO s join SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA c on c.SolicitudAcademica_uuid = s.SolicitudAcademica_uuid where s.SolicitudAcademica_uuid = '$sol1'") -eq "$a2|10:30|cruce|-"))
    Verificar-EtapaHttp -Solicitud $sol1 -Esperada "RADICADA" -Estudiante $est1.Token -Funcionario $fa1.Token -Decano $decano.Token
    Escribir-Resultado "quedaron los dos anexos y sus archivos (obtuvo $(@(Get-ChildItem (Join-Path $Global:CarpetaAnexos $sol1) -File).Count))" (((Ejecutar-Sql "select count(*) from ANEXO_ACADEMICO where SolicitudAcademica_uuid = '$sol1'") -eq "2") -and (@(Get-ChildItem (Join-Path $Global:CarpetaAnexos $sol1) -File).Count -eq 2))
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol1" -Token $est1.Token
    $detalle = Leer-Json $r
    Escribir-Resultado "el detalle del estudiante trae radicado, etiqueta, asignatura, causa, fecha y cruce (obtuvo $($r.Status))" (($r.Status -eq 200) -and ($detalle.solicitud.radicado -eq $esperado) -and ($detalle.solicitud.etiqueta -eq "En tr$([char]0x00E1)mite") -and ($detalle.asignatura.uuidAsignaturaMatriculada -eq $a1) -and ($detalle.tipoCausa -eq "cruce") -and ($detalle.fechaExamenNoPresentado -eq $hoy) -and ($detalle.cruce.asignatura.uuidAsignaturaMatriculada -eq $a2) -and ($detalle.cruce.fechaExamenCruzada -eq $hoy) -and ($detalle.cruce.horaExamenCruzada -eq "10:30") -and ($null -eq $detalle.fechaAcordadaExamen))
    Escribir-Resultado "el detalle lista los anexos con su tipo y sin contenido ni ruta" ((@($detalle.solicitud.anexos).Count -eq 2) -and (Anexo-De $detalle $for23).tipoAnexo -and (Anexo-De $detalle $docente).tipoAnexo -and ($r.Body -notmatch 'urlArchivo') -and ($r.Body -notmatch 'contenido'))
    $r = Radicar -Token $est1.Token -Asignatura $a2 -Fecha $hoy -Causa "otra" -Tipos @($for23, $justificacion) -Pdf $pdf
    Escribir-Resultado "una segunda solicitud en curso se rechaza (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*en curso*") -and ((Ejecutar-Sql "select count(*) from SOLICITUD_ACADEMICA where Estudiante_uuid = '$($est1.Uuid)'") -eq "1"))

    $b1 = @(Activas $est2)[0]
    $r = Radicar -Token $est2.Token -Asignatura $b1 -Fecha $hoy -Causa "otra" -Tipos @($for23, $justificacion) -Pdf $pdf
    $sol2 = (Leer-Json $r).uuidSolicitudAcademica
    $detalle2 = Leer-Json (Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol2" -Token $est2.Token)
    Escribir-Resultado "la radicacion por otra causa no guarda cruce (obtuvo $($r.Status))" (($r.Status -eq 200) -and ($detalle2.tipoCausa -eq "otra") -and ($null -eq $detalle2.cruce) -and ((Ejecutar-Sql "select count(*) from SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA where SolicitudAcademica_uuid = '$sol2'") -eq "0"))
    Verificar-EtapaHttp -Solicitud $sol2 -Esperada "RADICADA" -Estudiante $est2.Token -Funcionario $fa1.Token -Decano $decano.Token

    $firmaNoExiste = Firma-Error (Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$inexistente" -Token $est2.Token) $inexistente
    Escribir-Resultado "un estudiante ajeno no ve el detalle ($firmaNoExiste)" ((Firma-Error (Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol1" -Token $est2.Token) $sol1) -eq $firmaNoExiste)
    Escribir-Resultado "el Decano no ve un supletorio RADICADO" ((Firma-Error (Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol1" -Token $decano.Token) $sol1) -eq $firmaNoExiste)
    Escribir-Resultado "un funcionario no asignado no ve el detalle" ((Firma-Error (Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol1" -Token $fa2.Token) $sol1) -eq $firmaNoExiste)
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol1" -Token $fa1.Token
    Escribir-Resultado "el funcionario asignado ve el detalle con sus acciones (obtuvo $($r.Status))" (($r.Status -eq 200) -and ((@((Leer-Json $r).solicitud.accionesDisponibles) -join ',') -eq "RECHAZAR_FUNCIONARIO,REMITIR_DECANO"))
    $solCm = [guid]::NewGuid().ToString()
    Ejecutar-Sql ("insert into SOLICITUD_ACADEMICA (uuidSolicitudAcademica, radicado, Estudiante_uuid, TipoSolicitudAcademica_uuid, Etapa_uuid, fechaCreacion) " +
            "values ('$solCm', '1999-CM-$(Get-Random -Minimum 1000 -Maximum 9999)', '$($est3.Uuid)', '$tipoCm', (select uuidEtapa from ETAPA_SOLICITUD_ACADEMICA where codigo = 'RADICADA'), now())") | Out-Null
    Escribir-Resultado "una solicitud propia de otro tipo responde igual que una inexistente" ((Firma-Error (Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$solCm" -Token $est3.Token) $solCm) -eq $firmaNoExiste)
    Ejecutar-Sql "delete from SOLICITUD_ACADEMICA where uuidSolicitudAcademica = '$solCm'" | Out-Null

    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/funcionario/remitir" -Token $fa1.Token -Cuerpo @{ requisitosVerificados = $false }
    Escribir-Resultado "remitir sin confirmar los requisitos se rechaza pidiendo rechazar (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*rechazar la solicitud en lugar de remitirla*") -and ((Etapa-De $sol1) -eq "RADICADA"))
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/funcionario/remitir" -Token $fa1.Token -Cuerpo @{ observacion = "Sin confirmar" }
    Escribir-Resultado "remitir sin el campo de confirmacion tambien se rechaza (obtuvo $($r.Status))" (($r.Status -ne 200) -and ((Etapa-De $sol1) -eq "RADICADA"))
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/funcionario/remitir" -Token $fa2.Token -Cuerpo @{ requisitosVerificados = $true }
    Escribir-Resultado "un funcionario no asignado no puede remitir (obtuvo $($r.Status))" (($r.Status -ne 200) -and ((Etapa-De $sol1) -eq "RADICADA"))
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/funcionario/remitir" -Token $fa1.Token -Cuerpo @{ requisitosVerificados = $true; observacion = "Requisitos verificados" }
    Escribir-Resultado "el funcionario asignado remite y pasa a EN_REVISION_DECANO (obtuvo $($r.Status), etapa $(Etapa-De $sol1))" (($r.Status -eq 200) -and ((Etapa-De $sol1) -eq "EN_REVISION_DECANO"))
    Verificar-EtapaHttp -Solicitud $sol1 -Esperada "EN_REVISION_DECANO" -Estudiante $est1.Token -Funcionario $fa1.Token -Decano $decano.Token
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/decano/aprobar" -Token $decano.Token -Cuerpo @{}
    Escribir-Resultado "el Decano aprueba sin observacion y pasa a APROBADA_POR_DECANO (obtuvo $($r.Status), etapa $(Etapa-De $sol1))" (($r.Status -eq 200) -and ((Etapa-De $sol1) -eq "APROBADA_POR_DECANO"))
    Verificar-EtapaHttp -Solicitud $sol1 -Esperada "APROBADA_POR_DECANO" -Estudiante $est1.Token -Funcionario $fa1.Token -Decano $decano.Token
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/funcionario/recibo" -Token $fa1.Token
    Escribir-Resultado "enviar el recibo sin haberlo cargado falla y no cambia nada (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*recibo de pago*") -and ((Etapa-De $sol1) -eq "APROBADA_POR_DECANO"))
    $r = Subir-Anexo -Solicitud $sol1 -Tipo $recibo -Pdf $pdfRecibo -Token $est1.Token
    Escribir-Resultado "el estudiante no puede cargar el recibo (obtuvo $($r.Status))" ($r.Status -ne 200)
    $r = Subir-Anexo -Solicitud $sol1 -Tipo $recibo -Pdf $pdfRecibo -Token $fa1.Token
    Escribir-Resultado "el funcionario carga el recibo por el endpoint de anexos (200, obtuvo $($r.Status))" ($r.Status -eq 200)
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/funcionario/recibo" -Token $fa1.Token
    $detalle = if ($r.Status -eq 200) { Leer-Json $r } else { $null }
    Escribir-Resultado "enviar el recibo pasa a PENDIENTE_PAGO (obtuvo $($r.Status), etapa $(Etapa-De $sol1))" (($r.Status -eq 200) -and ((Etapa-De $sol1) -eq "PENDIENTE_PAGO"))
    Verificar-EtapaHttp -Solicitud $sol1 -Esperada "PENDIENTE_PAGO" -Estudiante $est1.Token -Funcionario $fa1.Token -Decano $decano.Token
    $anexoRecibo = (Anexo-De $detalle $recibo).uuidAnexoAcademico
    $d = Descargar -Ruta "solicitudes-academicas/$sol1/anexos/$anexoRecibo" -Token $est1.Token
    Escribir-Resultado "el estudiante dueno descarga el recibo identico en PENDIENTE_PAGO (200, obtuvo $($d.Status))" (($d.Status -eq 200) -and (Mismos-Bytes $pdfRecibo $d.Bytes))
    $d = Descargar -Ruta "solicitudes-academicas/$sol1/anexos/$anexoRecibo" -Token $est2.Token
    Escribir-Resultado "otro estudiante no puede descargar el recibo (obtuvo $($d.Status))" ($d.Status -ne 200)
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol1" -Token $est1.Token
    Escribir-Resultado "el estudiante ve la etiqueta Pendiente de pago y la accion de subir comprobante" (((Leer-Json $r).solicitud.etiqueta -eq "Pendiente de pago") -and ((@((Leer-Json $r).solicitud.accionesDisponibles) -join ',') -eq "SUBIR_COMPROBANTE"))
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/estudiante/comprobante" -Token $est1.Token
    Escribir-Resultado "confirmar el comprobante sin haberlo cargado falla (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*comprobante de pago*") -and ((Etapa-De $sol1) -eq "PENDIENTE_PAGO"))
    $r = Subir-Anexo -Solicitud $sol1 -Tipo $comprobante -Pdf $pdfComprobante -Token $est2.Token
    Escribir-Resultado "un estudiante ajeno no puede cargar el comprobante (obtuvo $($r.Status))" ($r.Status -ne 200)
    $r = Subir-Anexo -Solicitud $sol1 -Tipo $comprobante -Pdf $pdfComprobante -Token $est1.Token
    Escribir-Resultado "el estudiante dueno carga el comprobante (200, obtuvo $($r.Status))" ($r.Status -eq 200)
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/estudiante/comprobante" -Token $est2.Token
    Escribir-Resultado "un estudiante ajeno no puede confirmar el comprobante (obtuvo $($r.Status))" (($r.Status -ne 200) -and ((Etapa-De $sol1) -eq "PENDIENTE_PAGO"))
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/estudiante/comprobante" -Token $est1.Token
    $detalle = if ($r.Status -eq 200) { Leer-Json $r } else { $null }
    Escribir-Resultado "el estudiante confirma el comprobante y pasa a EN_VERIFICACION_PAGO (obtuvo $($r.Status), etapa $(Etapa-De $sol1))" (($r.Status -eq 200) -and ((Etapa-De $sol1) -eq "EN_VERIFICACION_PAGO"))
    Verificar-EtapaHttp -Solicitud $sol1 -Esperada "EN_VERIFICACION_PAGO" -Estudiante $est1.Token -Funcionario $fa1.Token -Decano $decano.Token
    $anexoComprobante = (Anexo-De $detalle $comprobante).uuidAnexoAcademico
    $d = Descargar -Ruta "solicitudes-academicas/$sol1/anexos/$anexoComprobante" -Token $fa1.Token
    Escribir-Resultado "el funcionario asignado descarga el comprobante identico (200, obtuvo $($d.Status))" (($d.Status -eq 200) -and (Mismos-Bytes $pdfComprobante $d.Bytes))
    $d = Descargar -Ruta "solicitudes-academicas/$sol1/anexos/$anexoComprobante" -Token $fa2.Token
    $d2 = Descargar -Ruta "solicitudes-academicas/$sol1/anexos/$anexoComprobante" -Token $est2.Token
    Escribir-Resultado "un funcionario no asignado y otro estudiante no descargan el comprobante (obtuvo $($d.Status) y $($d2.Status))" (($d.Status -ne 200) -and ($d2.Status -ne 200))
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/funcionario/comprobante/aprobar" -Token $fa1.Token -Cuerpo @{ fechaAcordadaExamen = $anteriorAlExamen }
    Escribir-Resultado "una fecha acordada anterior al examen se rechaza (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*no puede ser anterior a la fecha del examen*") -and ((Etapa-De $sol1) -eq "EN_VERIFICACION_PAGO"))
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol1/funcionario/comprobante/aprobar" -Token $fa1.Token -Cuerpo @{ fechaAcordadaExamen = $acordada }
    $detalle = if ($r.Status -eq 200) { Leer-Json $r } else { $null }
    Escribir-Resultado "aprobar el comprobante con fecha acordada deja la solicitud APROBADA (obtuvo $($r.Status), etapa $(Etapa-De $sol1))" (($r.Status -eq 200) -and ((Etapa-De $sol1) -eq "APROBADA"))
    Verificar-EtapaHttp -Solicitud $sol1 -Esperada "APROBADA" -Estudiante $est1.Token -Funcionario $fa1.Token -Decano $decano.Token
    $fechaBase = Ejecutar-Sql "select date_format(fechaAcordadaExamen, '%Y-%m-%d') from SOLICITUD_EXAMEN_SUPLETORIO where SolicitudAcademica_uuid = '$sol1'"
    Escribir-Resultado "la fecha acordada $acordada quedo en MySQL y en el detalle (obtuvo $fechaBase y $($detalle.fechaAcordadaExamen))" (($fechaBase -eq $acordada) -and ($detalle.fechaAcordadaExamen -eq $acordada))
    $detalle = Leer-Json (Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol1" -Token $est1.Token)
    Escribir-Resultado "el estudiante ve Aprobada con la fecha acordada y cuatro anexos" (($detalle.solicitud.etiqueta -eq "Aprobada") -and ($detalle.fechaAcordadaExamen -eq $acordada) -and (@($detalle.solicitud.anexos).Count -eq 4))
    $d = Descargar -Ruta "solicitudes-academicas/$sol1/anexos/$anexoRecibo" -Token $est1.Token
    Escribir-Resultado "en APROBADA el estudiante sigue descargando el recibo (200, obtuvo $($d.Status))" (($d.Status -eq 200) -and (Mismos-Bytes $pdfRecibo $d.Bytes))
    $historial = Historial-De $sol1 $est1.Token
    Escribir-Resultado "el historial trae las seis acciones del recorrido (obtuvo $historial)" ($historial -eq "APROBAR_COMPROBANTE,APROBAR_DECANO,ENVIAR_RECIBO,RADICAR,REMITIR_DECANO,SUBIR_COMPROBANTE")
    $etapasHistorial = (@(Leer-Json (Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$sol1/historial" -Token $fa1.Token)) | ForEach-Object { "$($_.accion):$($_.etapaCodigo)" } | Sort-Object) -join ","; Escribir-Resultado "cada accion del historial trae la etapa a la que llevo (obtuvo $etapasHistorial)" ($etapasHistorial -eq "APROBAR_COMPROBANTE:APROBADA,APROBAR_DECANO:APROBADA_POR_DECANO,ENVIAR_RECIBO:PENDIENTE_PAGO,RADICAR:RADICADA,REMITIR_DECANO:EN_REVISION_DECANO,SUBIR_COMPROBANTE:EN_VERIFICACION_PAGO")
    $estadosEst1 = "$(Ejecutar-Sql "select estado from ASIGNATURA_MATRICULADA where uuidAsignaturaMatriculada = '$a1'"),$(Ejecutar-Sql "select estado from ASIGNATURA_MATRICULADA where uuidAsignaturaMatriculada = '$a2'"),$(Ejecutar-Sql "select estado from ASIGNATURA_MATRICULADA where uuidAsignaturaMatriculada = '$noActiva'")"
    Escribir-Resultado "la aprobacion no cambio el estado de ninguna asignatura (obtuvo $estadosEst1)" ($estadosEst1 -eq "activa,activa,cancelada")
    Escribir-Resultado "no se genero Resolucion para el supletorio" ((Ejecutar-Sql "select count(*) from RESOLUCION_ACADEMICA where SolicitudAcademica_uuid = '$sol1'") -eq "0")

    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol2/funcionario/rechazar" -Token $fa1.Token -Cuerpo @{ observacion = " " }
    Escribir-Resultado "el funcionario no rechaza sin observacion (obtuvo $($r.Status))" (($r.Status -ne 200) -and ((Etapa-De $sol2) -eq "RADICADA"))
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol2/funcionario/rechazar" -Token $fa1.Token -Cuerpo @{ observacion = "No cumple los requisitos" }
    Escribir-Resultado "el rechazo del funcionario sin escaneo deja la solicitud RECHAZADA (obtuvo $($r.Status), etapa $(Etapa-De $sol2))" (($r.Status -eq 200) -and ((Etapa-De $sol2) -eq "RECHAZADA"))
    Verificar-EtapaHttp -Solicitud $sol2 -Esperada "RECHAZADA" -Estudiante $est2.Token -Funcionario $fa1.Token -Decano $decano.Token
    $detalle = Leer-Json (Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$sol2" -Token $est2.Token)
    Escribir-Resultado "el estudiante 2 ve Rechazada y su asignatura sigue activa" (($detalle.solicitud.etiqueta -eq "Rechazada") -and ((Estados @($est2.Uuid)) -eq "activa"))

    $c1 = @(Activas $est3)[0]
    $r = Radicar -Token $est3.Token -Asignatura $c1 -Fecha $hoy -Causa "otra" -Tipos @($for23, $justificacion) -Pdf $pdf
    $sol3 = (Leer-Json $r).uuidSolicitudAcademica
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol3/funcionario/remitir" -Token $fa1.Token -Cuerpo @{ requisitosVerificados = $true }
    Escribir-Resultado "el estudiante 3 radica y el funcionario remite (obtuvo $($r.Status), etapa $(Etapa-De $sol3))" (($r.Status -eq 200) -and ((Etapa-De $sol3) -eq "EN_REVISION_DECANO"))
    Verificar-EtapaHttp -Solicitud $sol3 -Esperada "EN_REVISION_DECANO" -Estudiante $est3.Token -Funcionario $fa1.Token -Decano $decano.Token
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol3/decano/rechazar" -Token $decano.Token -Cuerpo @{ observacion = "" }
    Escribir-Resultado "el Decano no rechaza sin observacion (obtuvo $($r.Status))" (($r.Status -ne 200) -and ((Etapa-De $sol3) -eq "EN_REVISION_DECANO"))
    $motivoDecano = "No procede: la causa no esta soportada"
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol3/decano/rechazar" -Token $decano.Token -Cuerpo @{ observacion = $motivoDecano }
    Escribir-Resultado "el rechazo del Decano pasa a RECHAZADA_POR_DECANO (obtuvo $($r.Status), etapa $(Etapa-De $sol3))" (($r.Status -eq 200) -and ((Etapa-De $sol3) -eq "RECHAZADA_POR_DECANO"))
    Verificar-EtapaHttp -Solicitud $sol3 -Esperada "RECHAZADA_POR_DECANO" -Estudiante $est3.Token -Funcionario $fa1.Token -Decano $decano.Token
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol3/funcionario/responder" -Token $fa1.Token
    Escribir-Resultado "enviar la respuesta sin escaneo deja la solicitud RECHAZADA (obtuvo $($r.Status), etapa $(Etapa-De $sol3))" (($r.Status -eq 200) -and ((Etapa-De $sol3) -eq "RECHAZADA"))
    Verificar-EtapaHttp -Solicitud $sol3 -Esperada "RECHAZADA" -Estudiante $est3.Token -Funcionario $fa1.Token -Decano $decano.Token
    $filaDecano = @(Leer-Json (Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$sol3/historial" -Token $est3.Token)) | Where-Object { $_.accion -eq "RECHAZAR_DECANO" }
    Escribir-Resultado "el estudiante 3 ve el motivo del Decano y su asignatura sigue activa (obtuvo $($filaDecano.observaciones))" (($filaDecano.observaciones -eq $motivoDecano) -and ((Estados @($est3.Uuid)) -eq "activa"))

    $e1 = @(Activas $est4)[0]
    $r = Radicar -Token $est4.Token -Asignatura $e1 -Fecha $hoy -Causa "otra" -Tipos @($for23, $justificacion) -Pdf $pdf
    $sol4 = (Leer-Json $r).uuidSolicitudAcademica
    $r1 = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol4/funcionario/remitir" -Token $fa1.Token -Cuerpo @{ requisitosVerificados = $true }
    $r2 = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol4/decano/aprobar" -Token $decano.Token -Cuerpo @{ observacion = "Procede" }
    $r3 = Subir-Anexo -Solicitud $sol4 -Tipo $recibo -Pdf $pdfRecibo -Token $fa1.Token
    $r4 = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol4/funcionario/recibo" -Token $fa1.Token
    $r5 = Subir-Anexo -Solicitud $sol4 -Tipo $comprobante -Pdf $pdfComprobante -Token $est4.Token
    $r6 = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol4/estudiante/comprobante" -Token $est4.Token
    Escribir-Resultado "el estudiante 4 llega a EN_VERIFICACION_PAGO (obtuvo $($r1.Status),$($r2.Status),$($r3.Status),$($r4.Status),$($r5.Status),$($r6.Status), etapa $(Etapa-De $sol4))" ((@($r1, $r2, $r3, $r4, $r5, $r6) | Where-Object { $_.Status -ne 200 }).Count -eq 0 -and ((Etapa-De $sol4) -eq "EN_VERIFICACION_PAGO"))
    Verificar-EtapaHttp -Solicitud $sol4 -Esperada "EN_VERIFICACION_PAGO" -Estudiante $est4.Token -Funcionario $fa1.Token -Decano $decano.Token
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol4/funcionario/comprobante/rechazar" -Token $fa1.Token -Cuerpo @{ observacion = "" }
    Escribir-Resultado "rechazar el comprobante sin observacion falla (obtuvo $($r.Status))" (($r.Status -ne 200) -and ((Etapa-De $sol4) -eq "EN_VERIFICACION_PAGO"))
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol4/funcionario/comprobante/rechazar" -Token $fa2.Token -Cuerpo @{ observacion = "El valor no coincide" }
    Escribir-Resultado "un funcionario no asignado no rechaza el comprobante (obtuvo $($r.Status))" (($r.Status -ne 200) -and ((Etapa-De $sol4) -eq "EN_VERIFICACION_PAGO"))
    $r = Invoke-Api -Metodo POST -Ruta "$Global:Ruta/$sol4/funcionario/comprobante/rechazar" -Token $fa1.Token -Cuerpo @{ observacion = "El valor no coincide" }
    Escribir-Resultado "el comprobante rechazado deja la solicitud RECHAZADA sin fecha acordada (obtuvo $($r.Status), etapa $(Etapa-De $sol4))" (($r.Status -eq 200) -and ((Etapa-De $sol4) -eq "RECHAZADA") -and ($null -eq (Leer-Json $r).fechaAcordadaExamen))
    Verificar-EtapaHttp -Solicitud $sol4 -Esperada "RECHAZADA" -Estudiante $est4.Token -Funcionario $fa1.Token -Decano $decano.Token
    $historial = Historial-De $sol4 $est4.Token
    Escribir-Resultado "el historial del comprobante rechazado esta completo (obtuvo $historial)" ($historial -eq "APROBAR_DECANO,ENVIAR_RECIBO,RADICAR,RECHAZAR_COMPROBANTE,REMITIR_DECANO,SUBIR_COMPROBANTE")
    $etapasHistorial = (@(Leer-Json (Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$sol4/historial" -Token $fa1.Token)) | ForEach-Object { "$($_.accion):$($_.etapaCodigo)" } | Sort-Object) -join ","; Escribir-Resultado "cada accion del historial trae la etapa a la que llevo (obtuvo $etapasHistorial)" ($etapasHistorial -eq "APROBAR_DECANO:APROBADA_POR_DECANO,ENVIAR_RECIBO:PENDIENTE_PAGO,RADICAR:RADICADA,RECHAZAR_COMPROBANTE:RECHAZADA,REMITIR_DECANO:EN_REVISION_DECANO,SUBIR_COMPROBANTE:EN_VERIFICACION_PAGO")

    $f1 = @(Activas $est5)[0]
    Ejecutar-Sql "create trigger $Global:Trigger before insert on ANEXO_ACADEMICO for each row set NEW.tipoArchivo = if(NEW.nombreArchivo = '$Global:ArchivoFalla', null, NEW.tipoArchivo)" | Out-Null
    $hayTrigger = Ejecutar-Sql "select count(*) from information_schema.TRIGGERS where TRIGGER_SCHEMA = 'cfiet' and TRIGGER_NAME = '$Global:Trigger'"
    Escribir-Resultado "se preparo el trigger temporal que hace fallar el segundo anexo (obtuvo $hayTrigger)" ($hayTrigger -eq "1")
    $antes = Conteos $est5.Uuid
    $r = Radicar -Token $est5.Token -Asignatura $f1 -Fecha $hoy -Causa "otra" -Tipos @($for23) -Pdf $pdf -Extra @("-F", "$justificacion=@$pdf;filename=$Global:ArchivoFalla;type=application/pdf")
    Escribir-Resultado "una falla al guardar el segundo anexo hace fallar la radicacion (obtuvo $($r.Status))" ($r.Status -ne 200)
    $despues = Conteos $est5.Uuid
    Escribir-Resultado "la falla a mitad de camino no dejo solicitud, historial, especializacion, anexos ni archivos ($despues)" ($despues -eq $antes)
    Quitar-Trigger
    $r = Radicar -Token $est5.Token -Asignatura $f1 -Fecha $hoy -Causa "otra" -Tipos @($for23, $justificacion) -Pdf $pdf
    Escribir-Resultado "sin el trigger el estudiante 5 radica normalmente (obtuvo $($r.Status))" ($r.Status -eq 200)
}
finally {
    Quitar-Trigger
    $quedaTrigger = Ejecutar-Sql "select count(*) from information_schema.TRIGGERS where TRIGGER_SCHEMA = 'cfiet' and TRIGGER_NAME = '$Global:Trigger'"
    Escribir-Resultado "no quedo el trigger temporal (obtuvo $quedaTrigger)" ($quedaTrigger -eq "0")

    if ($responsableEs) {
        $r = Invoke-Api -Metodo PUT -Ruta "catalogos-academicos/tipos-solicitud/$tipoEs/funcionario" -Token $tokenRoot -Cuerpo @{ funcionarioUuid = $responsableEs }
        if ($r.Status -ne 200) { Write-Host "No se pudo restaurar el responsable: $($r.Status) $($r.Body)" }
    } else {
        Ejecutar-Sql "update TIPO_SOLICITUD_ACADEMICA set FuncionarioAcademico_uuid = null where uuidTipoSolicitudAcademica = '$tipoEs'" | Out-Null
    }
    $actual = Ejecutar-Sql "select coalesce(FuncionarioAcademico_uuid, '') from TIPO_SOLICITUD_ACADEMICA where uuidTipoSolicitudAcademica = '$tipoEs'"
    Escribir-Resultado "Examen Supletorio quedo con su responsable original" ($actual -eq $responsableEs)

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
        $sentencias += "delete from SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA where SolicitudAcademica_uuid in ($sol)"
        $sentencias += "delete from SOLICITUD_EXAMEN_SUPLETORIO where SolicitudAcademica_uuid in ($sol)"
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
