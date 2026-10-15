. "$PSScriptRoot\comun.ps1"

$Global:CarpetaBackend = (Resolve-Path "$PSScriptRoot\..").Path
$Global:CarpetaAnexos = Join-Path $Global:CarpetaBackend "uploads\anexos"
$Global:CarpetaCorreos = $env:PRUEBAS_CORREOS
$Global:LogBackend = $env:PRUEBAS_LOG_BACKEND
$Global:Adicional = "Documento adicional de la respuesta"
$o = [char]0x00F3
$Global:AsuntoCa = "Notificaci$($o)n de Cancelaci$($o)n de asignatura"
$Global:AsuntoCm = "Notificaci$($o)n de Cancelaci$($o)n de matr$([char]0x00ED)cula"
$Global:AsuntoEs = "Notificaci$($o)n de examen supletorio"
$Global:TextoResolucion = "Se adjunta la resoluci$($o)n correspondiente a su solicitud."
$Global:Privada = "OBSERVACION-PRIVADA-T9"

if (-not $Global:CarpetaCorreos -or -not (Test-Path $Global:CarpetaCorreos)) {
    Write-Host "FAIL falta PRUEBAS_CORREOS: carpeta donde smtp_prueba.py guarda los correos, con el backend apuntando a ese smtp"
    exit 1
}

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
    param([string]$Token, [int]$Materias, [string]$Correo = "")
    $s = Nuevo-Sufijo
    $asignaturas = @()
    if (-not $script:programaIsi) { $script:programaIsi = Programa-PorSigla -Token $Token -Sigla "ISI" }
    for ($i = 1; $i -le $Materias; $i++) {
        $asignatura = Crear-AsignaturaPrueba -Token $Token -UuidPrograma $script:programaIsi.uuidProgramaAcademico -Nombre "Materia T9 $i $s"
        $script:codigos += $asignatura.codigoAsignatura
        $asignaturas += @{ codigoAsignatura = $asignatura.codigoAsignatura; grupo = "A" }
    }
    $correoElectronico = if ($Correo) { "$Correo$s@unicauca.edu.co" } else { "res$s@unicauca.edu.co" }
    $cuerpo = @{
        nombres           = "Notificada"
        apellidos         = "Prueba $s"
        tipoDocumento     = $Global:CedulaCiudadania
        numeroDocumento   = "6$s"
        telefono          = "+573000000000"
        correoElectronico = $correoElectronico
        username          = "res$s"
        password          = "Clave12345"
        codigoEstudiantil = "R$s"
        uuidProgramaAcademico = $script:programaIsi.uuidProgramaAcademico
        semestre          = "5"
        asignaturas       = $asignaturas
    }
    $r = Invoke-Api -Metodo POST -Ruta "estudiantes" -Token $Token -Cuerpo $cuerpo
    if ($r.Status -ne 200) { throw "No se pudo crear el estudiante ($($r.Status)): $($r.Body)" }
    $estudiante = [pscustomobject]@{
        Uuid   = (Leer-Json $r).uuidUsuario
        Correo = $correoElectronico
        Nombre = "Notificada Prueba $s"
        Token  = Iniciar-Sesion -Usuario "res$s" -Clave "Clave12345"
    }
    $script:usuarios += $estudiante.Uuid
    $script:estudiantes += $estudiante.Uuid
    return $estudiante
}

function Nuevo-Funcionario {
    param([string]$Token)
    $s = Nuevo-Sufijo
    $cuerpo = @{
        nombres           = "Respondedor"
        apellidos         = "Prueba $s"
        tipoDocumento     = $Global:CedulaCiudadania
        numeroDocumento   = "3$s"
        telefono          = "+573000000000"
        correoElectronico = "rsp$s@unicauca.edu.co"
        username          = "rsp$s"
        password          = "Clave12345"
        cargo             = "DepT9 $s"
    }
    $r = Invoke-Api -Metodo POST -Ruta "funcionarios-academicos" -Token $Token -Cuerpo $cuerpo
    if ($r.Status -ne 200) { throw "No se pudo crear el funcionario academico ($($r.Status)): $($r.Body)" }
    $funcionario = [pscustomobject]@{
        Uuid  = (Leer-Json $r).uuidUsuario
        Token = Iniciar-Sesion -Usuario "rsp$s" -Clave "Clave12345"
    }
    $script:usuarios += $funcionario.Uuid
    $script:funcionarios += $funcionario.Uuid
    return $funcionario
}

function Nuevo-Pdf {
    param([string]$Nombre)
    $ruta = Join-Path ([System.IO.Path]::GetTempPath()) ("t9_" + (Nuevo-Sufijo) + "_$Nombre.pdf")
    $contenido = "%PDF-1.4`n1 0 obj << /Type /Catalog >> endobj`n% $Nombre`n%%EOF`n"
    [System.IO.File]::WriteAllBytes($ruta, [System.Text.Encoding]::ASCII.GetBytes($contenido))
    $script:archivos += $ruta
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

function Subir-Anexo {
    param([string]$Solicitud, [string]$Tipo, [string]$Pdf, [string]$Token)
    $argumentos = @("-F", "archivo=@$Pdf;type=application/pdf")
    if ($Tipo) { $argumentos += @("-F", "tipoAnexo=$Tipo") }
    return Ejecutar-Curl -Argumentos $argumentos -Ruta "solicitudes-academicas/$Solicitud/anexos" -Token $Token
}

function Subir-Escaneo {
    param([string]$Solicitud, [string]$Pdf, [string]$Token)
    return Ejecutar-Curl -Argumentos @("-F", "archivo=@$Pdf;type=application/pdf") -Ruta "solicitudes-academicas/$Solicitud/resolucion" -Token $Token
}

function Mensaje-De {
    param($Respuesta)
    try { return [string](Leer-Json $Respuesta).mensaje } catch { return "" }
}

function Etapa-De {
    param([string]$Solicitud)
    return Ejecutar-Sql "select e.codigo from SOLICITUD_ACADEMICA s join ETAPA_SOLICITUD_ACADEMICA e on e.uuidEtapa = s.Etapa_uuid where s.uuidSolicitudAcademica = '$Solicitud'"
}

function Tipo-Anexo {
    param([string]$Tipo, [string]$Patron)
    return Ejecutar-Sql "select uuidTipoAnexoAcademico from TIPO_ANEXO_ACADEMICO where TipoSolicitudAcademica_uuid = '$Tipo' and nombre like '$Patron'"
}

function Filas-Asignatura {
    param([string]$Solicitud)
    return @(Ejecutar-Sql "select uuidAsignaturaSolicitud from ASIGNATURA_SOLICITUD_ACADEMICA where SolicitudAcademica_uuid = '$Solicitud' order by uuidAsignaturaSolicitud")
}

function Matriculadas-Del-Formulario {
    param([string]$Ruta, [string]$Token)
    $formulario = Leer-Json (Invoke-Api -Metodo GET -Ruta "$Ruta/formulario" -Token $Token)
    return @($formulario.asignaturas | ForEach-Object { $_.uuidAsignaturaMatriculada })
}

function Paso {
    param([string]$Descripcion, $Respuesta, [string]$Solicitud, [string]$Etapa)
    $actual = Etapa-De $Solicitud
    $ok = ($Respuesta.Status -eq 200) -and ($actual -eq $Etapa)
    Escribir-Resultado "$Descripcion (200 y $Etapa, obtuvo $($Respuesta.Status) y $actual$(if ($Respuesta.Status -ne 200) { ': ' + (Mensaje-De $Respuesta) }))" $ok
    if (-not $ok) { throw "Se detiene el flujo: $Descripcion" }
}

function Correos-Para {
    param([string]$Para)
    return @(Get-ChildItem -Path $Global:CarpetaCorreos -Filter "*.json" -File | ForEach-Object {
        Get-Content -LiteralPath $_.FullName -Raw -Encoding UTF8 | ConvertFrom-Json
    } | Where-Object { @($_.para) -contains $Para })
}

function Esperar-Correo {
    param([string]$Para)
    for ($i = 0; $i -lt 60; $i++) {
        $correos = @(Correos-Para $Para)
        if ($correos.Count -gt 0) { Start-Sleep -Milliseconds 1500; return @(Correos-Para $Para) }
        Start-Sleep -Milliseconds 500
    }
    return @()
}

function Verificar-Correo {
    param([string]$Caso, $Estudiante, [string]$Asunto, [string[]]$Adjuntos, [string[]]$Textos)
    $correos = @(Esperar-Correo $Estudiante.Correo)
    Escribir-Resultado "$Caso`: sale exactamente un correo al estudiante (obtuvo $($correos.Count))" ($correos.Count -eq 1)
    if ($correos.Count -eq 0) { return }
    $correo = $correos[0]
    $nombres = @($correo.adjuntos | ForEach-Object { $_.nombre })
    $esperados = (@($Adjuntos | Sort-Object) -join "|")
    Escribir-Resultado "$Caso`: asunto correcto (obtuvo '$($correo.asunto)')" ($correo.asunto -ceq $Asunto)
    Escribir-Resultado "$Caso`: adjuntos $($Adjuntos -join ', ') (obtuvo $($nombres -join ', '))" (((@($nombres | Sort-Object)) -join "|") -eq $esperados)
    Escribir-Resultado "$Caso`: los adjuntos son PDF con contenido" (@($correo.adjuntos | Where-Object { $_.tipo -ne "application/pdf" -or $_.tamanio -le 0 }).Count -eq 0)
    $faltan = @($Textos | Where-Object { -not $correo.texto.Contains($_) })
    Escribir-Resultado "$Caso`: el texto trae fecha, nombre y mensaje (faltan: $($faltan -join ' | '))" ($faltan.Count -eq 0)
    Escribir-Resultado "$Caso`: el texto no trae la observacion ni dice aprobada o rechazada" ((-not $correo.texto.Contains($Global:Privada)) -and ($correo.texto -notmatch "(?i)aprobad|rechazad"))
}

function Textos-Carta {
    param($Estudiante, [string]$Mensaje)
    return @("Popay$([char]0x00E1)n, Cauca, ", "Sr(a) $($Estudiante.Nombre)", "Estudiante Facultad de Ingenier$([char]0x00ED)a Electr$([char]0x00F3)nica y Telecomunicaciones", $Mensaje)
}

$tokenRoot = Iniciar-Sesion -Usuario "rootfiet" -Clave "rootfiet1234"
$tipoCm = Ejecutar-Sql "select uuidTipoSolicitudAcademica from TIPO_SOLICITUD_ACADEMICA where nombre like 'Cancelaci%n de Matr%cula'"
$tipoCa = Ejecutar-Sql "select uuidTipoSolicitudAcademica from TIPO_SOLICITUD_ACADEMICA where nombre like 'Cancelaci%n de Asignatura'"
$tipoEs = Ejecutar-Sql "select uuidTipoSolicitudAcademica from TIPO_SOLICITUD_ACADEMICA where nombre like 'Examen Supletorio'"
$obligatoriosCm = @(Ejecutar-Sql "select uuidTipoAnexoAcademico from TIPO_ANEXO_ACADEMICO where TipoSolicitudAcademica_uuid = '$tipoCm' and obligatorio = 1")
$adicionalCm = Tipo-Anexo $tipoCm $Global:Adicional
$adicionalCa = Tipo-Anexo $tipoCa $Global:Adicional
$adicionalEs = Tipo-Anexo $tipoEs $Global:Adicional
$for23 = Tipo-Anexo $tipoEs "Formato firmado por el docente que orienta la asignatura"
$justificacion = Tipo-Anexo $tipoEs "Soporte de la justificaci%"
$recibo = Tipo-Anexo $tipoEs "Recibo de pago"
$comprobante = Tipo-Anexo $tipoEs "Comprobante de pago"
$situacion = Ejecutar-Sql "select uuidSituacionAcademica from SITUACION_ACADEMICA_ASIGNATURA where codigo = 'R1'"
if (-not $tipoCm -or -not $tipoCa -or -not $tipoEs -or $obligatoriosCm.Count -ne 6 -or -not $adicionalCm -or -not $adicionalCa -or -not $adicionalEs -or -not $for23 -or -not $justificacion -or -not $recibo -or -not $comprobante -or -not $situacion) {
    Write-Host "FAIL faltan catalogos sembrados (procesos, anexos o el Documento adicional de la respuesta); corre docs/database/seed-procesos-academicos.sql"
    exit 1
}
$responsables = @{}
foreach ($tipo in @($tipoCm, $tipoCa, $tipoEs)) {
    $responsables[$tipo] = Ejecutar-Sql "select coalesce(FuncionarioAcademico_uuid, '') from TIPO_SOLICITUD_ACADEMICA where uuidTipoSolicitudAcademica = '$tipo'"
}
$habiaAnexos = Test-Path $Global:CarpetaAnexos
$ahora = [System.TimeZoneInfo]::ConvertTimeBySystemTimeZoneId([DateTime]::UtcNow, "SA Pacific Standard Time")
$hoy = $ahora.ToString("yyyy-MM-dd")
$lineasLogIniciales = if ($Global:LogBackend -and (Test-Path $Global:LogBackend)) { @(Get-Content -LiteralPath $Global:LogBackend).Count } else { 0 }

$usuarios = @()
$estudiantes = @()
$funcionarios = @()
$codigos = @()
$archivos = @()
$solicitudes = @()

try {
    $fa = Nuevo-Funcionario -Token $tokenRoot
    $decano = Crear-UsuarioPrueba -Perfil "Decano" -TokenAdmin $tokenRoot -Prefijo "dect9"
    $docente = Crear-UsuarioPrueba -Perfil "Docente" -TokenAdmin $tokenRoot -Prefijo "doct9"
    $usuarios += @($decano.Uuid, $docente.Uuid)
    foreach ($tipo in @($tipoCm, $tipoCa, $tipoEs)) {
        $r = Invoke-Api -Metodo PUT -Ruta "catalogos-academicos/tipos-solicitud/$tipo/funcionario" -Token $tokenRoot -Cuerpo @{ funcionarioUuid = $fa.Uuid }
        if ($r.Status -ne 200) { throw "No se pudo asignar el proceso al funcionario de prueba ($($r.Status)): $($r.Body)" }
    }
    $pdfAnexo = Nuevo-Pdf "anexo"
    $pdfResolucion = Nuevo-Pdf "resolucion"
    $pdfAdicional = Nuevo-Pdf "acta"
    $nombreAdicional = Split-Path $pdfAdicional -Leaf
    $observacion = @{ observacion = $Global:Privada }

    function Radicar-Matricula {
        param($Estudiante)
        $argumentos = @("-F", "motivo=Calamidad domestica T9")
        foreach ($tipo in $obligatoriosCm) { $argumentos += @("-F", "$tipo=@$pdfAnexo;type=application/pdf") }
        $r = Ejecutar-Curl -Argumentos $argumentos -Ruta "cancelaciones-matricula" -Token $Estudiante.Token
        if ($r.Status -ne 200) { throw "No se pudo radicar la cancelacion de matricula ($($r.Status)): $($r.Body)" }
        $uuid = (Leer-Json $r).uuidSolicitudAcademica
        $script:solicitudes += $uuid
        return [pscustomobject]@{ Uuid = $uuid; Radicado = (Ejecutar-Sql "select radicado from SOLICITUD_ACADEMICA where uuidSolicitudAcademica = '$uuid'") }
    }

    function Radicar-Asignatura {
        param($Estudiante)
        $argumentos = @("-F", "motivo=Cruce laboral T9")
        foreach ($m in (Matriculadas-Del-Formulario "cancelaciones-asignatura" $Estudiante.Token)) { $argumentos += @("-F", "asignaturas=$m") }
        $r = Ejecutar-Curl -Argumentos $argumentos -Ruta "cancelaciones-asignatura" -Token $Estudiante.Token
        if ($r.Status -ne 200) { throw "No se pudo radicar la cancelacion de asignatura ($($r.Status)): $($r.Body)" }
        $uuid = (Leer-Json $r).uuidSolicitudAcademica
        $script:solicitudes += $uuid
        return [pscustomobject]@{ Uuid = $uuid; Radicado = (Ejecutar-Sql "select radicado from SOLICITUD_ACADEMICA where uuidSolicitudAcademica = '$uuid'") }
    }

    function Radicar-Supletorio {
        param($Estudiante)
        $materia = @(Matriculadas-Del-Formulario "examenes-supletorios" $Estudiante.Token)[0]
        $argumentos = @("-F", "asignaturaMatriculada=$materia", "-F", "fechaExamenNoPresentado=$hoy", "-F", "tipoCausa=otra",
            "-F", "$for23=@$pdfAnexo;type=application/pdf", "-F", "$justificacion=@$pdfAnexo;type=application/pdf")
        $r = Ejecutar-Curl -Argumentos $argumentos -Ruta "examenes-supletorios" -Token $Estudiante.Token
        if ($r.Status -ne 200) { throw "No se pudo radicar el examen supletorio ($($r.Status)): $($r.Body)" }
        $uuid = (Leer-Json $r).uuidSolicitudAcademica
        $script:solicitudes += $uuid
        return [pscustomobject]@{ Uuid = $uuid; Radicado = (Ejecutar-Sql "select radicado from SOLICITUD_ACADEMICA where uuidSolicitudAcademica = '$uuid'") }
    }

    function Cargar-Archivos {
        param([string]$Caso, [string]$Solicitud, [string]$Adicional, [bool]$ConResolucion, [bool]$ConAdicional)
        if ($ConResolucion) {
            $r = Subir-Escaneo -Solicitud $Solicitud -Pdf $pdfResolucion -Token $fa.Token
            Escribir-Resultado "$Caso`: el funcionario asignado sube la Resolucion escaneada (200, obtuvo $($r.Status) $(Mensaje-De $r))" ($r.Status -eq 200)
        }
        if ($ConAdicional) {
            $r = Subir-Anexo -Solicitud $Solicitud -Tipo $Adicional -Pdf $pdfAdicional -Token $fa.Token
            Escribir-Resultado "$Caso`: el funcionario asignado sube el documento adicional (200, obtuvo $($r.Status) $(Mensaje-De $r))" ($r.Status -eq 200)
        }
    }

    $estCm1 = Nuevo-Estudiante -Token $tokenRoot -Materias 2
    $sol = Radicar-Matricula $estCm1
    $r = Subir-Anexo -Solicitud $sol.Uuid -Tipo $adicionalCm -Pdf $pdfAdicional -Token $estCm1.Token
    Escribir-Resultado "matricula: el estudiante no puede subir el documento adicional (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*FUNCIONARIO*"))
    $r = Subir-Anexo -Solicitud $sol.Uuid -Tipo $obligatoriosCm[0] -Pdf $pdfAnexo -Token $fa.Token
    Escribir-Resultado "matricula: el funcionario no puede subir un anexo del estudiante (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*ESTUDIANTE*"))
    $r = Subir-Anexo -Solicitud $sol.Uuid -Tipo "" -Pdf $pdfAnexo -Token $fa.Token
    Escribir-Resultado "matricula: el funcionario no puede subir un soporte libre (obtuvo $($r.Status))" ($r.Status -ne 200)
    Cargar-Archivos "matricula rechazo del funcionario" $sol.Uuid $adicionalCm $true $true
    Paso "matricula: el funcionario rechaza" (Invoke-Api -Metodo POST -Ruta "cancelaciones-matricula/$($sol.Uuid)/funcionario/rechazar" -Token $fa.Token -Cuerpo $observacion) $sol.Uuid "RECHAZADA"
    Verificar-Correo "matricula rechazo del funcionario" $estCm1 $Global:AsuntoCm @("Resolucion-$($sol.Radicado).pdf", $nombreAdicional) (Textos-Carta $estCm1 $Global:TextoResolucion)

    $estCm2 = Nuevo-Estudiante -Token $tokenRoot -Materias 2
    $sol = Radicar-Matricula $estCm2
    $evaluaciones = @(Filas-Asignatura $sol.Uuid | ForEach-Object { @{ asignaturaSolicitudUuid = $_ } })
    Paso "matricula: remite al Decano" (Invoke-Api -Metodo POST -Ruta "cancelaciones-matricula/$($sol.Uuid)/funcionario/remitir" -Token $fa.Token -Cuerpo @{ evaluaciones = $evaluaciones }) $sol.Uuid "EN_REVISION_DECANO"
    Paso "matricula: el Decano aprueba" (Invoke-Api -Metodo POST -Ruta "cancelaciones-matricula/$($sol.Uuid)/decano/aprobar" -Token $decano.Token -Cuerpo @{ observacion = "Visto" }) $sol.Uuid "APROBADA_POR_DECANO"
    Cargar-Archivos "matricula respuesta aprobada" $sol.Uuid $adicionalCm $true $true
    Paso "matricula: el funcionario envia la respuesta aprobada" (Invoke-Api -Metodo POST -Ruta "cancelaciones-matricula/$($sol.Uuid)/funcionario/responder" -Token $fa.Token -Cuerpo $observacion) $sol.Uuid "APROBADA"
    Verificar-Correo "matricula respuesta aprobada" $estCm2 $Global:AsuntoCm @("Resolucion-$($sol.Radicado).pdf", $nombreAdicional) (Textos-Carta $estCm2 $Global:TextoResolucion)

    $estCm3 = Nuevo-Estudiante -Token $tokenRoot -Materias 1
    $sol = Radicar-Matricula $estCm3
    $evaluaciones = @(Filas-Asignatura $sol.Uuid | ForEach-Object { @{ asignaturaSolicitudUuid = $_ } })
    Paso "matricula: remite al Decano" (Invoke-Api -Metodo POST -Ruta "cancelaciones-matricula/$($sol.Uuid)/funcionario/remitir" -Token $fa.Token -Cuerpo @{ evaluaciones = $evaluaciones }) $sol.Uuid "EN_REVISION_DECANO"
    Paso "matricula: el Decano rechaza" (Invoke-Api -Metodo POST -Ruta "cancelaciones-matricula/$($sol.Uuid)/decano/rechazar" -Token $decano.Token -Cuerpo @{ observacion = "No procede" }) $sol.Uuid "RECHAZADA_POR_DECANO"
    Cargar-Archivos "matricula respuesta rechazada sin documento adicional" $sol.Uuid $adicionalCm $true $false
    Paso "matricula: el funcionario envia la respuesta rechazada" (Invoke-Api -Metodo POST -Ruta "cancelaciones-matricula/$($sol.Uuid)/funcionario/responder" -Token $fa.Token -Cuerpo $observacion) $sol.Uuid "RECHAZADA"
    Verificar-Correo "matricula respuesta rechazada sin documento adicional" $estCm3 $Global:AsuntoCm @("Resolucion-$($sol.Radicado).pdf") (Textos-Carta $estCm3 $Global:TextoResolucion)

    $estCa1 = Nuevo-Estudiante -Token $tokenRoot -Materias 2
    $sol = Radicar-Asignatura $estCa1
    $r = Subir-Anexo -Solicitud $sol.Uuid -Tipo $adicionalCa -Pdf $pdfAdicional -Token $estCa1.Token
    Escribir-Resultado "asignatura: el estudiante no puede subir el documento adicional (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*FUNCIONARIO*"))
    $r = Subir-Anexo -Solicitud $sol.Uuid -Tipo "" -Pdf $pdfAnexo -Token $fa.Token
    Escribir-Resultado "asignatura: el funcionario no puede subir un soporte libre (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*ESTUDIANTE*"))
    Cargar-Archivos "asignatura rechazo del funcionario" $sol.Uuid $adicionalCa $true $true
    Paso "asignatura: el funcionario rechaza" (Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/rechazar" -Token $fa.Token -Cuerpo $observacion) $sol.Uuid "RECHAZADA"
    Verificar-Correo "asignatura rechazo del funcionario" $estCa1 $Global:AsuntoCa @("Resolucion-$($sol.Radicado).pdf", $nombreAdicional) (Textos-Carta $estCa1 $Global:TextoResolucion)

    $estCa2 = Nuevo-Estudiante -Token $tokenRoot -Materias 2
    $sol = Radicar-Asignatura $estCa2
    $evaluaciones = @(Filas-Asignatura $sol.Uuid | ForEach-Object { @{ asignaturaSolicitudUuid = $_; cumpleCondiciones = $true; situacionCancelarUuid = $situacion } })
    Paso "asignatura: remite al Decano" (Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/remitir" -Token $fa.Token -Cuerpo @{ evaluaciones = $evaluaciones }) $sol.Uuid "EN_REVISION_DECANO"
    Paso "asignatura: el Decano acepta la evaluacion" (Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$($sol.Uuid)/decano/aprobar" -Token $decano.Token -Cuerpo @{ aceptarEvaluacion = $true }) $sol.Uuid "APROBADA_POR_DECANO"
    Cargar-Archivos "asignatura respuesta aprobada" $sol.Uuid $adicionalCa $true $true
    Paso "asignatura: el funcionario envia la respuesta aprobada" (Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/responder" -Token $fa.Token -Cuerpo $observacion) $sol.Uuid "APROBADA"
    Verificar-Correo "asignatura respuesta aprobada" $estCa2 $Global:AsuntoCa @("Resolucion-$($sol.Radicado).pdf", $nombreAdicional) (Textos-Carta $estCa2 $Global:TextoResolucion)

    $estCa3 = Nuevo-Estudiante -Token $tokenRoot -Materias 1
    $sol = Radicar-Asignatura $estCa3
    $evaluaciones = @(Filas-Asignatura $sol.Uuid | ForEach-Object { @{ asignaturaSolicitudUuid = $_; cumpleCondiciones = $true; situacionCancelarUuid = $situacion } })
    Paso "asignatura: remite al Decano" (Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/remitir" -Token $fa.Token -Cuerpo @{ evaluaciones = $evaluaciones }) $sol.Uuid "EN_REVISION_DECANO"
    Paso "asignatura: el Decano rechaza" (Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$($sol.Uuid)/decano/rechazar" -Token $decano.Token -Cuerpo @{ observacion = "No procede" }) $sol.Uuid "RECHAZADA_POR_DECANO"
    Cargar-Archivos "asignatura respuesta rechazada sin documento adicional" $sol.Uuid $adicionalCa $true $false
    Paso "asignatura: el funcionario envia la respuesta rechazada" (Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/responder" -Token $fa.Token -Cuerpo $observacion) $sol.Uuid "RECHAZADA"
    Verificar-Correo "asignatura respuesta rechazada sin documento adicional" $estCa3 $Global:AsuntoCa @("Resolucion-$($sol.Radicado).pdf") (Textos-Carta $estCa3 $Global:TextoResolucion)

    $mensajeEs = "ya tiene respuesta. Ingrese a la aplicaci$($o)n para consultarla."

    $estEs1 = Nuevo-Estudiante -Token $tokenRoot -Materias 1
    $sol = Radicar-Supletorio $estEs1
    $r = Subir-Anexo -Solicitud $sol.Uuid -Tipo $adicionalEs -Pdf $pdfAdicional -Token $estEs1.Token
    Escribir-Resultado "supletorio: el estudiante no puede subir el documento adicional (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*FUNCIONARIO*"))
    $r = Subir-Anexo -Solicitud $sol.Uuid -Tipo $for23 -Pdf $pdfAnexo -Token $fa.Token
    Escribir-Resultado "supletorio: el funcionario no puede subir un anexo del estudiante (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*ESTUDIANTE*"))
    $r = Subir-Escaneo -Solicitud $sol.Uuid -Pdf $pdfResolucion -Token $fa.Token
    Escribir-Resultado "supletorio: no admite Resolucion escaneada (obtuvo $($r.Status))" ($r.Status -ne 200)
    Cargar-Archivos "supletorio rechazo del funcionario" $sol.Uuid $adicionalEs $false $true
    Paso "supletorio: el funcionario rechaza" (Invoke-Api -Metodo POST -Ruta "examenes-supletorios/$($sol.Uuid)/funcionario/rechazar" -Token $fa.Token -Cuerpo $observacion) $sol.Uuid "RECHAZADA"
    Verificar-Correo "supletorio rechazo del funcionario" $estEs1 $Global:AsuntoEs @($nombreAdicional) (Textos-Carta $estEs1 "con radicado $($sol.Radicado) $mensajeEs")

    $estEs2 = Nuevo-Estudiante -Token $tokenRoot -Materias 1
    $sol = Radicar-Supletorio $estEs2
    Paso "supletorio: remite al Decano" (Invoke-Api -Metodo POST -Ruta "examenes-supletorios/$($sol.Uuid)/funcionario/remitir" -Token $fa.Token -Cuerpo @{ observacion = "Para el Decano" }) $sol.Uuid "EN_REVISION_DECANO"
    Paso "supletorio: el Decano rechaza" (Invoke-Api -Metodo POST -Ruta "examenes-supletorios/$($sol.Uuid)/decano/rechazar" -Token $decano.Token -Cuerpo @{ observacion = "No procede" }) $sol.Uuid "RECHAZADA_POR_DECANO"
    Cargar-Archivos "supletorio respuesta rechazada" $sol.Uuid $adicionalEs $false $true
    Paso "supletorio: el funcionario envia la respuesta rechazada" (Invoke-Api -Metodo POST -Ruta "examenes-supletorios/$($sol.Uuid)/funcionario/responder" -Token $fa.Token -Cuerpo $observacion) $sol.Uuid "RECHAZADA"
    Verificar-Correo "supletorio respuesta rechazada" $estEs2 $Global:AsuntoEs @($nombreAdicional) (Textos-Carta $estEs2 "con radicado $($sol.Radicado) $mensajeEs")

    function Llevar-A-Verificacion {
        param($Estudiante, $Solicitud)
        Paso "supletorio: remite al Decano" (Invoke-Api -Metodo POST -Ruta "examenes-supletorios/$($Solicitud.Uuid)/funcionario/remitir" -Token $fa.Token -Cuerpo @{ observacion = "Para el Decano" }) $Solicitud.Uuid "EN_REVISION_DECANO"
        Paso "supletorio: el Decano aprueba" (Invoke-Api -Metodo POST -Ruta "examenes-supletorios/$($Solicitud.Uuid)/decano/aprobar" -Token $decano.Token -Cuerpo @{}) $Solicitud.Uuid "APROBADA_POR_DECANO"
        $r = Subir-Anexo -Solicitud $Solicitud.Uuid -Tipo $recibo -Pdf $pdfAnexo -Token $fa.Token
        Escribir-Resultado "supletorio: el funcionario sigue subiendo el recibo de pago (200, obtuvo $($r.Status))" ($r.Status -eq 200)
        Paso "supletorio: el funcionario envia el recibo" (Invoke-Api -Metodo POST -Ruta "examenes-supletorios/$($Solicitud.Uuid)/funcionario/recibo" -Token $fa.Token) $Solicitud.Uuid "PENDIENTE_PAGO"
        $r = Subir-Anexo -Solicitud $Solicitud.Uuid -Tipo $comprobante -Pdf $pdfAnexo -Token $Estudiante.Token
        Escribir-Resultado "supletorio: el estudiante sigue subiendo el comprobante (200, obtuvo $($r.Status))" ($r.Status -eq 200)
        Paso "supletorio: el estudiante confirma el comprobante" (Invoke-Api -Metodo POST -Ruta "examenes-supletorios/$($Solicitud.Uuid)/estudiante/comprobante" -Token $Estudiante.Token) $Solicitud.Uuid "EN_VERIFICACION_PAGO"
        $correos = @(Correos-Para $Estudiante.Correo)
        Escribir-Resultado "supletorio: las etapas intermedias no envian correo (obtuvo $($correos.Count))" ($correos.Count -eq 0)
    }

    $estEs3 = Nuevo-Estudiante -Token $tokenRoot -Materias 1
    $sol = Radicar-Supletorio $estEs3
    Llevar-A-Verificacion $estEs3 $sol
    Cargar-Archivos "supletorio aprobar comprobante" $sol.Uuid $adicionalEs $false $true
    Paso "supletorio: el funcionario aprueba el comprobante" (Invoke-Api -Metodo POST -Ruta "examenes-supletorios/$($sol.Uuid)/funcionario/comprobante/aprobar" -Token $fa.Token -Cuerpo @{ docenteUuid = $docente.Uuid; observacion = $Global:Privada }) $sol.Uuid "APROBADA"
    Verificar-Correo "supletorio aprobar comprobante" $estEs3 $Global:AsuntoEs @($nombreAdicional) (Textos-Carta $estEs3 "con radicado $($sol.Radicado) $mensajeEs")

    $estEs4 = Nuevo-Estudiante -Token $tokenRoot -Materias 1
    $sol = Radicar-Supletorio $estEs4
    Llevar-A-Verificacion $estEs4 $sol
    Paso "supletorio: el funcionario rechaza el comprobante sin documento adicional" (Invoke-Api -Metodo POST -Ruta "examenes-supletorios/$($sol.Uuid)/funcionario/comprobante/rechazar" -Token $fa.Token -Cuerpo $observacion) $sol.Uuid "RECHAZADA"
    Verificar-Correo "supletorio rechazar comprobante sin documento adicional" $estEs4 $Global:AsuntoEs @() (Textos-Carta $estEs4 "con radicado $($sol.Radicado) $mensajeEs")

    $estFalla = Nuevo-Estudiante -Token $tokenRoot -Materias 1 -Correo "smtp-rechaza"
    $sol = Radicar-Matricula $estFalla
    Cargar-Archivos "matricula con el correo rechazado por el servidor" $sol.Uuid $adicionalCm $true $true
    Paso "matricula: si el servidor de correo rechaza el envio la respuesta igual queda guardada" (Invoke-Api -Metodo POST -Ruta "cancelaciones-matricula/$($sol.Uuid)/funcionario/rechazar" -Token $fa.Token -Cuerpo $observacion) $sol.Uuid "RECHAZADA"
    Start-Sleep -Seconds 5
    Escribir-Resultado "matricula: el correo rechazado no queda entregado" (@(Correos-Para $estFalla.Correo).Count -eq 0)
    $historial = Ejecutar-Sql "select count(*) from HISTORIAL_SOLICITUD_ACADEMICA where SolicitudAcademica_uuid = '$($sol.Uuid)' and accion = 'RECHAZAR_FUNCIONARIO'"
    Escribir-Resultado "matricula: el rechazo queda en el historial aunque el correo falle (obtuvo $historial)" ($historial -eq "1")
    if ($Global:LogBackend -and (Test-Path $Global:LogBackend)) {
        $nuevas = @(Get-Content -LiteralPath $Global:LogBackend | Select-Object -Skip $lineasLogIniciales)
        $registro = @($nuevas | Where-Object { $_ -like "*No se pudo enviar el correo*" -and $_ -like "*$($estFalla.Correo)*" })
        Escribir-Resultado "matricula: el fallo del correo queda en el log del backend (obtuvo $($registro.Count) lineas)" ($registro.Count -ge 1)
    } else {
        Write-Host "  sin PRUEBAS_LOG_BACKEND no se revisa el log del backend"
    }
}
finally {
    foreach ($tipo in @($tipoCm, $tipoCa, $tipoEs)) {
        if ($responsables[$tipo]) {
            $r = Invoke-Api -Metodo PUT -Ruta "catalogos-academicos/tipos-solicitud/$tipo/funcionario" -Token $tokenRoot -Cuerpo @{ funcionarioUuid = $responsables[$tipo] }
            if ($r.Status -ne 200) { Write-Host "No se pudo restaurar el responsable: $($r.Status) $($r.Body)" }
        }
    }
    $restaurados = @(@($tipoCm, $tipoCa, $tipoEs) | Where-Object { (Ejecutar-Sql "select coalesce(FuncionarioAcademico_uuid, '') from TIPO_SOLICITUD_ACADEMICA where uuidTipoSolicitudAcademica = '$_'") -ne $responsables[$_] })
    Escribir-Resultado "los tres procesos quedaron con su responsable original" ($restaurados.Count -eq 0)

    $est = Lista-Sql $estudiantes
    if ($est) { $solicitudes = @($solicitudes + @(Ejecutar-Sql "select uuidSolicitudAcademica from SOLICITUD_ACADEMICA where Estudiante_uuid in ($est)")) | Where-Object { $_ } | Select-Object -Unique }
    $sol = Lista-Sql $solicitudes
    $usu = Lista-Sql $usuarios
    $fun = Lista-Sql $funcionarios
    $cod = Lista-Sql $codigos
    $sentencias = @()
    if ($sol) {
        $sentencias += "delete from NOTIFICACION_DOCENTE where SolicitudAcademica_uuid in ($sol)"
        $sentencias += "delete from ANEXO_ACADEMICO where SolicitudAcademica_uuid in ($sol)"
        $sentencias += "delete from RESOLUCION_ACADEMICA where SolicitudAcademica_uuid in ($sol)"
        $sentencias += "delete from ASIGNATURA_SOLICITUD_ACADEMICA where SolicitudAcademica_uuid in ($sol)"
        $sentencias += "delete from SOLICITUD_CANCELACION_MATRICULA where SolicitudAcademica_uuid in ($sol)"
        $sentencias += "delete from SOLICITUD_CANCELACION_ASIGNATURA where SolicitudAcademica_uuid in ($sol)"
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
