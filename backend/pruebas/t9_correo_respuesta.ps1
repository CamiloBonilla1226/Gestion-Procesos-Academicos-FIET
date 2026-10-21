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
$Global:TextoRechazoDirecto = "fue respondida. Ingrese a la aplicaci$($o)n para ver la respuesta."
$Global:PazYSalvos = "Paz y salvos (un solo PDF con todos los paz y salvos)"
$Global:Remitente = "Decanatura FIET - Procesos acad$([char]0x00E9)micos"
$Global:Privada = "OBSERVACION-PRIVADA-T9"
$Global:CorreoDarca = $env:PRUEBAS_CORREO_DARCA
$Global:DarcaCa = "DARCA - Notificaci$($o)n de Cancelaci$($o)n de asignatura"
$Global:DarcaCm = "DARCA - Notificaci$($o)n de Cancelaci$($o)n de matr$([char]0x00ED)cula"
$Global:DarcaEs = "DARCA - Notificaci$($o)n de examen supletorio"
$Global:Matricula = "matr$([char]0x00ED)cula"
$Global:AntesDelPago = "No aplica, la solicitud se cerr$($o) antes del pago"
$Global:AsuntoMensajeCa = "Respuesta a su solicitud de cancelaci$($o)n de asignatura - Radicado "
$Global:TituloComunicacion = "COMUNICACI$([char]0x00D3)N ELECTR$([char]0x00D3)NICA - RESOLUCI$([char]0x00D3)N No. "
$Global:EnTramite = "En tr$([char]0x00E1)mite"
$Global:Firma = "CRISTIAN ESTIBEN ILLERA OSPITIA`nApoyo Administrativo`nDecanatura`nFacultad de Ingenier$([char]0x00ED)a Electr$($o)nica y Telecomunicaciones"
$Global:MensajeEditado = "Buen dia, estudiante`n<b>T9</b> acerquese a la Decanatura.`n`nCordialmente,`nApoyo T9"
$Global:Espera = if ($env:PRUEBAS_ESPERA_SEGUNDOS) { [int]$env:PRUEBAS_ESPERA_SEGUNDOS } else { 45 }

if (-not $Global:CarpetaCorreos -or -not (Test-Path $Global:CarpetaCorreos)) {
    Write-Host "FAIL falta PRUEBAS_CORREOS: carpeta donde smtp_prueba.py guarda los correos, con el backend apuntando a ese smtp"
    exit 1
}
if (-not $Global:CorreoDarca) {
    Write-Host "FAIL falta PRUEBAS_CORREO_DARCA: el mismo valor de CORREO_DARCA con el que se levanto el backend"
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
    $creadas = @()
    if (-not $script:programaIsi) { $script:programaIsi = Programa-PorSigla -Token $Token -Sigla "ISI" }
    for ($i = 1; $i -le $Materias; $i++) {
        $asignatura = Crear-AsignaturaPrueba -Token $Token -UuidPrograma $script:programaIsi.uuidProgramaAcademico -Nombre "Materia T9 $i $s"
        $script:codigos += $asignatura.codigoAsignatura
        $asignaturas += @{ codigoAsignatura = $asignatura.codigoAsignatura; grupo = "A" }
        $creadas += [pscustomobject]@{ Codigo = $asignatura.codigoAsignatura; Nombre = "Materia T9 $i $s" }
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
        Codigo = "R$s"
        Materias = $creadas
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

function Verificar-Formato {
    param([string]$Caso, $Correo)
    $html = [string]$Correo.html
    Escribir-Resultado "$Caso`: el remitente se muestra como '$($Global:Remitente)' (obtuvo '$($Correo.de)')" (([string]$Correo.de).Contains($Global:Remitente))
    $primera = (([string]$Correo.texto) -split "`r?`n" | Where-Object { $_.Trim() } | Select-Object -First 1)
    Escribir-Resultado "$Caso`: trae texto plano y una version HTML simple con el mismo contenido" (($html -match "(?i)<html") -and $primera -and $html.Contains($primera.Trim()))
    Escribir-Resultado "$Caso`: el HTML no lleva imagenes ni enlaces" ($html -notmatch "(?i)<img|<a |https?://")
}

function Verificar-Correo {
    param([string]$Caso, $Estudiante, [string]$Asunto, [string[]]$Adjuntos, [string[]]$Textos)
    $correos = @(Esperar-Correo $Estudiante.Correo)
    Escribir-Resultado "$Caso`: sale exactamente un correo al estudiante (obtuvo $($correos.Count))" ($correos.Count -eq 1)
    if ($correos.Count -eq 0) { return }
    $correo = $correos[0]
    Verificar-Formato "$Caso (estudiante)" $correo
    $nombres = @($correo.adjuntos | ForEach-Object { $_.nombre })
    $esperados = (@($Adjuntos | Sort-Object) -join "|")
    Escribir-Resultado "$Caso`: asunto correcto (obtuvo '$($correo.asunto)')" ($correo.asunto -ceq $Asunto)
    Escribir-Resultado "$Caso`: adjuntos $($Adjuntos -join ', ') (obtuvo $($nombres -join ', '))" (((@($nombres | Sort-Object)) -join "|") -eq $esperados)
    Escribir-Resultado "$Caso`: los adjuntos son PDF con contenido" (@($correo.adjuntos | Where-Object { $_.tipo -ne "application/pdf" -or $_.tamanio -le 0 }).Count -eq 0)
    $faltan = @($Textos | Where-Object { -not $correo.texto.Contains($_) })
    Escribir-Resultado "$Caso`: el texto trae fecha, nombre y mensaje (faltan: $($faltan -join ' | '))" ($faltan.Count -eq 0)
    Escribir-Resultado "$Caso`: el texto no trae la observacion ni dice aprobada o rechazada" ((-not $correo.texto.Contains($Global:Privada)) -and ($correo.texto -notmatch "(?i)aprobad|rechazad"))
}

function Correos-Con-Asunto {
    param([string]$Para, [string]$Asunto)
    return @(Correos-Para $Para | Where-Object { $_.asunto -ceq $Asunto })
}

function Esperar-Asunto {
    param([string]$Para, [string]$Asunto)
    for ($i = 0; $i -lt 60; $i++) {
        $correos = @(Correos-Con-Asunto $Para $Asunto)
        if ($correos.Count -gt 0) { Start-Sleep -Milliseconds 1500; return @(Correos-Con-Asunto $Para $Asunto) }
        Start-Sleep -Milliseconds 500
    }
    return @()
}

function Normalizar {
    param([string]$Texto)
    return (($Texto -replace "\s+", " ").Trim())
}

function Verificar-Adjuntos-Pdf {
    param([string]$Caso, $Correo, [string]$Pdf, [string[]]$Adjuntos, [string[]]$TextosPdf)
    $nombres = @($Correo.adjuntos | ForEach-Object { $_.nombre })
    $esperados = @(@($Pdf) + @($Adjuntos | Where-Object { $_ }))
    Escribir-Resultado "$Caso`: adjuntos $($esperados -join ', ') (obtuvo $($nombres -join ', '))" (((@($nombres | Sort-Object)) -join "|") -eq ((@($esperados | Sort-Object)) -join "|"))
    Escribir-Resultado "$Caso`: los adjuntos son PDF con contenido" (@($Correo.adjuntos | Where-Object { $_.tipo -ne "application/pdf" -or $_.tamanio -le 0 }).Count -eq 0)
    $generado = @($Correo.adjuntos | Where-Object { $_.nombre -eq $Pdf })[0]
    if ($null -eq $generado) { return }
    Escribir-Resultado "$Caso`: $Pdf es un PDF valido" ([bool]$generado.esPdf)
    $textoPdf = Normalizar $generado.textoPdf
    $faltan = @($TextosPdf | Where-Object { -not $textoPdf.Contains((Normalizar $_)) })
    Escribir-Resultado "$Caso`: el PDF trae los datos de la respuesta (faltan: $($faltan -join ' | '))" ($faltan.Count -eq 0)
    Escribir-Resultado "$Caso`: el PDF no trae observaciones ni historial" ((-not $textoPdf.Contains($Global:Privada)) -and ($textoPdf -notmatch "(?i)observaci|historial"))
}

function Verificar-Darca {
    param([string]$Caso, $Estudiante, $Solicitud, [string]$Prefijo, [string]$Tipo, [string]$Estado, [string[]]$Adjuntos, [string]$Frase, [string[]]$TextosPdf)
    $asunto = "$Prefijo - Radicado $($Solicitud.Radicado)"
    $correos = @(Esperar-Asunto $Global:CorreoDarca $asunto)
    Escribir-Resultado "$Caso`: sale exactamente un correo a DARCA con el asunto '$asunto' (obtuvo $($correos.Count))" ($correos.Count -eq 1)
    if ($correos.Count -eq 0) { return }
    $correo = $correos[0]
    Verificar-Formato "$Caso (DARCA)" $correo
    $comunes = @($Solicitud.Radicado, $Tipo, $Estudiante.Nombre, $Estudiante.Codigo, $script:programaIsi.nombre, $Estado)
    Verificar-Adjuntos-Pdf "$Caso (DARCA)" $correo "Radicado-$($Solicitud.Radicado).pdf" $Adjuntos (@($comunes) + @($TextosPdf))
    $texto = Normalizar $correo.texto
    $esperados = @("Popay$([char]0x00E1)n, Cauca, ", "El estudiante $($Estudiante.Nombre), con c$($o)digo $($Estudiante.Codigo), $Frase", "PDF adjunto")
    $faltan = @($esperados | Where-Object { -not $texto.Contains($_) })
    Escribir-Resultado "$Caso (DARCA): el texto trae la fecha, la frase y el aviso del PDF (faltan: $($faltan -join ' | '))" ($faltan.Count -eq 0)
    Escribir-Resultado "$Caso (DARCA): el texto no trae la observacion" (-not $texto.Contains($Global:Privada))
}

function Verificar-Sin-Profesor {
    param([string]$Caso, $Solicitud)
    $asunto = "Notificaci$($o)n de examen supletorio - Radicado $($Solicitud.Radicado)"
    $correos = @(Get-ChildItem -Path $Global:CarpetaCorreos -Filter "*.json" -File | ForEach-Object {
        Get-Content -LiteralPath $_.FullName -Raw -Encoding UTF8 | ConvertFrom-Json
    } | Where-Object { $_.asunto -ceq $asunto })
    Escribir-Resultado "$Caso`: no sale correo a ningun profesor (obtuvo $($correos.Count))" ($correos.Count -eq 0)
}

function Verificar-Profesor {
    param([string]$Caso, $Estudiante, $Solicitud, $Profesor)
    $asunto = "Notificaci$($o)n de examen supletorio - Radicado $($Solicitud.Radicado)"
    $correos = @(Esperar-Asunto $Profesor.Correo $asunto)
    Escribir-Resultado "$Caso`: sale exactamente un correo al profesor con el asunto '$asunto' (obtuvo $($correos.Count))" ($correos.Count -eq 1)
    if ($correos.Count -eq 0) { return }
    $correo = $correos[0]
    Verificar-Formato "$Caso (profesor)" $correo
    $materia = $Estudiante.Materias[0]
    Verificar-Adjuntos-Pdf "$Caso (profesor)" $correo "Radicado-$($Solicitud.Radicado).pdf" @() @($Solicitud.Radicado, $Estudiante.Nombre, $Estudiante.Codigo, "$($materia.Codigo) - $($materia.Nombre)", $Profesor.Nombre, "Aprobado")
    $texto = Normalizar $correo.texto
    $esperados = @("Sr(a) $($Profesor.Nombre)", "Tiene una solicitud de examen supletorio aprobada del estudiante $($Estudiante.Nombre), con c$($o)digo $($Estudiante.Codigo), de la asignatura $($materia.Codigo) - $($materia.Nombre), radicado $($Solicitud.Radicado).", "Ingrese a la aplicaci$($o)n para consultarla.")
    $faltan = @($esperados | Where-Object { -not $texto.Contains($_) })
    Escribir-Resultado "$Caso (profesor): el texto trae nombre, estudiante, codigo, asignatura, radicado y la invitacion (faltan: $($faltan -join ' | '))" ($faltan.Count -eq 0)
    Escribir-Resultado "$Caso (profesor): el texto no trae la observacion" (-not $texto.Contains($Global:Privada))
}

function Textos-Carta {
    param($Estudiante, [string]$Mensaje)
    return @("Popay$([char]0x00E1)n, Cauca, ", "Sr(a) $($Estudiante.Nombre)", "Estudiante Facultad de Ingenier$([char]0x00ED)a Electr$([char]0x00F3)nica y Telecomunicaciones", $Mensaje)
}

function Ahora-Bogota {
    return [System.TimeZoneInfo]::ConvertTimeBySystemTimeZoneId([DateTime]::UtcNow, "SA Pacific Standard Time")
}

function Programacion {
    param([int]$Segundos, [string]$Numero, [string]$Decision)
    $momento = Ahora-Bogota
    return @{ fechaProgramada = $momento.AddSeconds($Segundos).ToString("yyyy-MM-ddTHH:mm:ss"); numeroResolucion = $Numero; anioResolucion = $momento.Year; decision = $Decision }
}

function Esperar-Notificacion {
    param([string]$Solicitud, [string[]]$Estados, [int]$Segundos)
    $limite = (Get-Date).AddSeconds($Segundos)
    do {
        $estado = Ejecutar-Sql "select estado from NOTIFICACION_RESOLUCION where SolicitudAcademica_uuid = '$Solicitud'"
        if ($Estados -contains $estado) { return $estado }
        Start-Sleep -Seconds 1
    } while ((Get-Date) -lt $limite)
    return $estado
}

function Vista-Estudiante {
    param($Estudiante, $Solicitud)
    $fila = @(Leer-Json (Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/estudiante" -Token $Estudiante.Token)) | Where-Object { $_.uuidSolicitudAcademica -eq $Solicitud.Uuid }
    $detalle = Leer-Json (Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$($Solicitud.Uuid)" -Token $Estudiante.Token)
    $historial = @(Leer-Json (Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$($Solicitud.Uuid)/historial" -Token $Estudiante.Token))
    $proceso = Invoke-Api -Metodo GET -Ruta "cancelaciones-asignatura/$($Solicitud.Uuid)" -Token $Estudiante.Token
    $decididas = if ($proceso.Status -eq 200) { @((Leer-Json $proceso).asignaturas | Where-Object { $null -ne $_.aprobadaPorDecano }).Count } else { -1 }
    return [pscustomobject]@{
        Etiqueta        = $fila.etiqueta
        EtapaBandeja    = $fila.etapaCodigo
        EtiquetaDetalle = $detalle.etiqueta
        EtapaDetalle    = $detalle.etapaCodigo
        TieneResolucion = [bool]$detalle.tieneResolucion
        Descargable     = [bool]$detalle.puedeDescargarResolucion
        Anexos          = @($detalle.anexos | ForEach-Object { $_.nombreArchivo })
        Decididas       = $decididas
        Acciones        = @($historial | ForEach-Object { $_.accion })
        Descarga        = (Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$($Solicitud.Uuid)/resolucion" -Token $Estudiante.Token).Status
    }
}

function Verificar-Retenida {
    param([string]$Caso, $Estudiante, $Solicitud)
    $v = Vista-Estudiante $Estudiante $Solicitud
    Escribir-Resultado "$Caso`: hasta la fecha programada el estudiante ve la solicitud '$($Global:EnTramite)' como con el Decano en bandeja y detalle (obtuvo '$($v.Etiqueta)' $($v.EtapaBandeja), '$($v.EtiquetaDetalle)' $($v.EtapaDetalle))" (($v.Etiqueta -eq $Global:EnTramite) -and ($v.EtapaBandeja -eq "EN_REVISION_DECANO") -and ($v.EtiquetaDetalle -eq $Global:EnTramite) -and ($v.EtapaDetalle -eq "EN_REVISION_DECANO"))
    Escribir-Resultado "$Caso`: el estudiante no recibe Resolucion, resultado por asignatura, respuesta del historial ni documento adicional (resolucion $($v.TieneResolucion), decididas $($v.Decididas), historial $($v.Acciones -join ','))" ((-not $v.TieneResolucion) -and (-not $v.Descargable) -and ($v.Decididas -eq 0) -and ($v.Acciones -notcontains "ENVIAR_RESPUESTA") -and ($v.Acciones -notcontains "APROBAR_DECANO") -and ($v.Anexos -notcontains $nombreAdicional))
    Escribir-Resultado "$Caso`: la descarga de la Resolucion se rechaza para el estudiante (obtuvo $($v.Descarga))" ($v.Descarga -ne 200)
    $deFuncionario = Leer-Json (Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$($Solicitud.Uuid)" -Token $fa.Token)
    $deDecano = Leer-Json (Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$($Solicitud.Uuid)" -Token $decano.Token)
    Escribir-Resultado "$Caso`: el funcionario y el Decano siguen viendo la etapa final y la Resolucion (obtuvo $($deFuncionario.etapaCodigo) y $($deDecano.etapaCodigo))" ((@("APROBADA", "RECHAZADA") -contains $deFuncionario.etapaCodigo) -and $deFuncionario.puedeDescargarResolucion -and (@("APROBADA", "RECHAZADA") -contains $deDecano.etapaCodigo) -and $deDecano.puedeDescargarResolucion)
}

function Verificar-Liberada {
    param([string]$Caso, $Estudiante, $Solicitud, [string]$Etiqueta, [bool]$ConAdicional)
    $v = Vista-Estudiante $Estudiante $Solicitud
    $liberacion = Ejecutar-Sql "select coalesce(fechaLiberacion, '') from NOTIFICACION_RESOLUCION where SolicitudAcademica_uuid = '$($Solicitud.Uuid)'"
    Escribir-Resultado "$Caso`: desde que la tarea toma la comunicacion el estudiante ve '$Etiqueta' con su Resolucion e historial (obtuvo '$($v.Etiqueta)', descarga $($v.Descarga), liberada '$liberacion')" (($v.Etiqueta -eq $Etiqueta) -and ($v.EtiquetaDetalle -eq $Etiqueta) -and $v.TieneResolucion -and $v.Descargable -and ($v.Descarga -eq 200) -and ($v.Acciones -contains "ENVIAR_RESPUESTA") -and $liberacion)
    if ($ConAdicional) { Escribir-Resultado "$Caso`: tras la liberacion el estudiante ve el documento adicional" ($v.Anexos -contains $nombreAdicional) }
}

function Verificar-Mensaje-Inicial {
    param([string]$Caso, $Estudiante, $Solicitud, [string]$Texto)
    $asunto = "$($Global:AsuntoMensajeCa)$($Solicitud.Radicado)"
    $correos = @(Esperar-Asunto $Estudiante.Correo $asunto)
    Escribir-Resultado "$Caso`: sale de inmediato un solo mensaje al estudiante con el asunto '$asunto' (obtuvo $($correos.Count))" ($correos.Count -eq 1)
    if ($correos.Count -eq 0) { return }
    $correo = $correos[0]
    Verificar-Formato "$Caso (mensaje inicial)" $correo
    Escribir-Resultado "$Caso`: el mensaje inicial no lleva adjuntos (obtuvo $(@($correo.adjuntos).Count))" (@($correo.adjuntos).Count -eq 0)
    Escribir-Resultado "$Caso`: el mensaje inicial trae el texto enviado y no dice el resultado" ((Normalizar $correo.texto).Contains((Normalizar $Texto)) -and ($correo.texto -notmatch "(?i)aprobad|rechazad") -and (-not ([string]$correo.texto).Contains($Global:Privada)))
    if (([string]$correo.texto).Contains("<b>T9</b>")) {
        $html = [string]$correo.html
        Escribir-Resultado "$Caso`: el HTML escapa el texto editado y conserva los saltos de linea" ($html.Contains("&lt;b&gt;T9&lt;/b&gt;") -and (-not $html.Contains("<b>T9")) -and $html.Contains("Buen dia, estudiante<br>"))
    }
}

function Verificar-Sin-Comunicacion {
    param([string]$Caso, $Estudiante, $Solicitud)
    $alEstudiante = @(Correos-Con-Asunto $Estudiante.Correo $Global:AsuntoCa)
    $aDarca = @(Correos-Con-Asunto $Global:CorreoDarca "$($Global:DarcaCa) - Radicado $($Solicitud.Radicado)")
    Escribir-Resultado "$Caso`: no salen de inmediato la comunicacion al estudiante ni el correo a DARCA (estudiante $($alEstudiante.Count), DARCA $($aDarca.Count))" (($alEstudiante.Count -eq 0) -and ($aDarca.Count -eq 0))
}

function Verificar-Comunicacion {
    param([string]$Caso, $Estudiante, $Solicitud, [string]$Numero, [string]$Decision, [string[]]$Adjuntos)
    $correos = @(Esperar-Asunto $Estudiante.Correo $Global:AsuntoCa)
    Escribir-Resultado "$Caso`: en la fecha programada sale una sola comunicacion al estudiante con el asunto '$($Global:AsuntoCa)' (obtuvo $($correos.Count))" ($correos.Count -eq 1)
    if ($correos.Count -eq 0) { return }
    $correo = $correos[0]
    Verificar-Formato "$Caso (comunicacion)" $correo
    $anio = (Ahora-Bogota).Year
    $texto = Normalizar $correo.texto
    $esperados = @("Estimado (a) estudiante reciba un cordial saludo, de conformidad con la solicitud", "$($Global:TituloComunicacion)$Numero/178 de $anio",
        "Se comunica al estudiante, $($Estudiante.Nombre), del texto de la resoluci$($o)n No. $Numero/178 de $anio", "la misma por la cual se $Decision cancelaci$($o)n de asignatura",
        "Ley 1437 de 2011", (Normalizar $Global:Firma))
    $faltan = @($esperados | Where-Object { -not $texto.Contains($_) })
    Escribir-Resultado "$Caso`: el texto plano es el del cliente con numero, sufijo, anio, nombre, decision y firma (faltan: $($faltan -join ' | '))" ($faltan.Count -eq 0)
    $html = [string]$correo.html
    Escribir-Resultado "$Caso`: en el HTML el numero, el anio, el nombre y la decision van en negrilla" ($html.Contains("<strong>$Numero</strong>/178 de <strong>$anio</strong>") -and $html.Contains("<strong>$($Estudiante.Nombre)</strong>") -and $html.Contains("<strong>$Decision</strong>"))
    Verificar-Adjuntos-Pdf "$Caso (comunicacion)" $correo "Radicado-$($Solicitud.Radicado).pdf" $Adjuntos @($Solicitud.Radicado, $Estudiante.Nombre)
}

function Verificar-Darca-Programada {
    param([string]$Caso, $Estudiante, $Solicitud, [string[]]$Adjuntos, [string]$Frase, [string]$Numero, [bool]$ConConstancia)
    $asunto = "$($Global:DarcaCa) - Radicado $($Solicitud.Radicado)"
    $correos = @(Esperar-Asunto $Global:CorreoDarca $asunto)
    Escribir-Resultado "$Caso`: en la fecha programada sale un solo correo a DARCA con el asunto '$asunto' (obtuvo $($correos.Count))" ($correos.Count -eq 1)
    if ($correos.Count -eq 0) { return }
    $correo = $correos[0]
    Verificar-Formato "$Caso (DARCA)" $correo
    Verificar-Adjuntos-Pdf "$Caso (DARCA)" $correo "Radicado-$($Solicitud.Radicado).pdf" $Adjuntos @($Solicitud.Radicado, $Estudiante.Nombre, $Estudiante.Codigo)
    $texto = Normalizar $correo.texto
    $esperados = @("Popay$([char]0x00E1)n, Cauca, ", "El estudiante $($Estudiante.Nombre), con c$($o)digo $($Estudiante.Codigo), $Frase")
    $faltan = @($esperados | Where-Object { -not $texto.Contains($_) })
    Escribir-Resultado "$Caso (DARCA): el texto trae la fecha y la frase de resumen (faltan: $($faltan -join ' | '))" ($faltan.Count -eq 0)
    Escribir-Resultado "$Caso (DARCA): el texto no trae la observacion" (-not $texto.Contains($Global:Privada))
    if ($ConConstancia) {
        $constancia = @($correo.adjuntos | Where-Object { $_.nombre -eq "Comunicacion-Estudiante-$($Solicitud.Radicado).pdf" })[0]
        $textoPdf = if ($constancia) { Normalizar $constancia.textoPdf } else { "" }
        $esperadosPdf = @($Estudiante.Correo, "Fecha y hora de env", "hora de Colombia", $Numero, "/178 de", $Estudiante.Nombre, "Ley 1437 de 2011", "CRISTIAN ESTIBEN ILLERA OSPITIA")
        $faltan = @($esperadosPdf | Where-Object { -not $textoPdf.Contains($_) })
        Escribir-Resultado "$Caso (DARCA): la constancia trae el texto del correo, la fecha y hora de envio y el destinatario (faltan: $($faltan -join ' | '))" ($constancia -and [bool]$constancia.esPdf -and ($faltan.Count -eq 0))
        Escribir-Resultado "$Caso (DARCA): la constancia no trae observaciones" (-not $textoPdf.Contains($Global:Privada))
    } else {
        Escribir-Resultado "$Caso (DARCA): con la firma del estudiante no va la constancia de la comunicacion" (@($correo.adjuntos | Where-Object { $_.nombre -like "Comunicacion-Estudiante-*" }).Count -eq 0)
        Escribir-Resultado "$Caso (DARCA): el texto dice que el estudiante firmo la Resolucion" ($texto.Contains("firm$($o) la Resoluci$($o)n"))
    }
}

$tokenRoot = Iniciar-Sesion -Usuario "rootfiet" -Clave "rootfiet1234"
$tipoCm = Ejecutar-Sql "select uuidTipoSolicitudAcademica from TIPO_SOLICITUD_ACADEMICA where nombre like 'Cancelaci%n de Matr%cula'"
$tipoCa = Ejecutar-Sql "select uuidTipoSolicitudAcademica from TIPO_SOLICITUD_ACADEMICA where nombre like 'Cancelaci%n de Asignatura'"
$tipoEs = Ejecutar-Sql "select uuidTipoSolicitudAcademica from TIPO_SOLICITUD_ACADEMICA where nombre like 'Examen Supletorio'"
$obligatoriosCm = @(Ejecutar-Sql "select uuidTipoAnexoAcademico from TIPO_ANEXO_ACADEMICO where TipoSolicitudAcademica_uuid = '$tipoCm' and obligatorio = 1 and vigente = 1")
$nombreObligatorioCm = Ejecutar-Sql "select nombre from TIPO_ANEXO_ACADEMICO where TipoSolicitudAcademica_uuid = '$tipoCm' and obligatorio = 1 and vigente = 1"
$retiradosCm = @(Ejecutar-Sql "select uuidTipoAnexoAcademico from TIPO_ANEXO_ACADEMICO where TipoSolicitudAcademica_uuid = '$tipoCm' and vigente = 0")
$adicionalCm = Tipo-Anexo $tipoCm $Global:Adicional
$adicionalCa = Tipo-Anexo $tipoCa $Global:Adicional
$adicionalEs = Tipo-Anexo $tipoEs $Global:Adicional
$for23 = Tipo-Anexo $tipoEs "Formato firmado por el docente que orienta la asignatura"
$justificacion = Tipo-Anexo $tipoEs "Soporte de la justificaci%"
$recibo = Tipo-Anexo $tipoEs "Recibo de pago"
$comprobante = Tipo-Anexo $tipoEs "Comprobante de pago"
$situacion = Ejecutar-Sql "select uuidSituacionAcademica from SITUACION_ACADEMICA_ASIGNATURA where codigo = 'R1'"
$nombreSituacion = Ejecutar-Sql "select nombre from SITUACION_ACADEMICA_ASIGNATURA where codigo = 'R1'"
if (-not $tipoCm -or -not $tipoCa -or -not $tipoEs -or $obligatoriosCm.Count -ne 1 -or $retiradosCm.Count -ne 6 -or -not $adicionalCm -or -not $adicionalCa -or -not $adicionalEs -or -not $for23 -or -not $justificacion -or -not $recibo -or -not $comprobante -or -not $situacion) {
    Write-Host "FAIL faltan catalogos sembrados (procesos, anexos, el Documento adicional de la respuesta o el PDF unico de paz y salvos); corre docs/database/seed-procesos-academicos.sql"
    exit 1
}
Escribir-Resultado "matricula: el unico anexo obligatorio vigente es '$($Global:PazYSalvos)' (obtuvo '$nombreObligatorioCm') y los seis anteriores quedan retirados" ($nombreObligatorioCm -ceq $Global:PazYSalvos)
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
    $docente2 = Crear-UsuarioPrueba -Perfil "Docente" -TokenAdmin $tokenRoot -Prefijo "doct9"
    $usuarios += @($decano.Uuid, $docente.Uuid, $docente2.Uuid)
    $profesor = [pscustomobject]@{ Uuid = $docente.Uuid; Correo = "$($docente.Username)@unicauca.edu.co"; Nombre = "Prueba Docente $($docente.Username.Substring(5))" }
    $correoRechazado = "smtp-rechaza-$($docente2.Username)@unicauca.edu.co"
    Ejecutar-Sql "update usuarios set correoElectronico = '$correoRechazado' where uuidUsuario = '$($docente2.Uuid)'" | Out-Null
    $profesorRechazado = [pscustomobject]@{ Uuid = $docente2.Uuid; Correo = $correoRechazado; Nombre = "Prueba Docente $($docente2.Username.Substring(5))" }
    foreach ($tipo in @($tipoCm, $tipoCa, $tipoEs)) {
        $r = Invoke-Api -Metodo PUT -Ruta "catalogos-academicos/tipos-solicitud/$tipo/funcionario" -Token $tokenRoot -Cuerpo @{ funcionarioUuid = $fa.Uuid }
        if ($r.Status -ne 200) { throw "No se pudo asignar el proceso al funcionario de prueba ($($r.Status)): $($r.Body)" }
    }
    $pdfAnexo = Nuevo-Pdf "anexo"
    $pdfResolucion = Nuevo-Pdf "resolucion"
    $pdfAdicional = Nuevo-Pdf "acta"
    $nombreAdicional = Split-Path $pdfAdicional -Leaf
    $pdfFirmada = Nuevo-Pdf "firmada"
    $nombreFirmada = Split-Path $pdfFirmada -Leaf
    $observacion = @{ observacion = $Global:Privada }

    function Radicar-Matricula {
        param($Estudiante, [string]$Pdf = $pdfAnexo)
        $argumentos = @("-F", "motivo=Calamidad domestica T9")
        foreach ($tipo in $obligatoriosCm) { $argumentos += @("-F", "$tipo=@$Pdf;type=application/pdf") }
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

    $pdfPazGrande = Nuevo-Pdf "paz_y_salvos"
    $relleno = New-Object byte[] (7 * 1024 * 1024)
    $flujo = [System.IO.File]::Open($pdfPazGrande, [System.IO.FileMode]::Append)
    try { $flujo.Write($relleno, 0, $relleno.Length) } finally { $flujo.Close() }

    $estViejo = Nuevo-Estudiante -Token $tokenRoot -Materias 1
    $argumentos = @("-F", "motivo=Calamidad domestica T9", "-F", "$($obligatoriosCm[0])=@$pdfAnexo;type=application/pdf", "-F", "$($retiradosCm[0])=@$pdfAnexo;type=application/pdf")
    $r = Ejecutar-Curl -Argumentos $argumentos -Ruta "cancelaciones-matricula" -Token $estViejo.Token
    Escribir-Resultado "matricula: un anexo de los seis anteriores ya no se recibe (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*ya no se recibe*"))
    $r = Ejecutar-Curl -Argumentos @("-F", "motivo=Calamidad domestica T9") -Ruta "cancelaciones-matricula" -Token $estViejo.Token
    Escribir-Resultado "matricula: sin el PDF unico de paz y salvos no se radica (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*Paz y salvos (un solo PDF*"))
    $enCurso = Ejecutar-Sql "select count(*) from SOLICITUD_ACADEMICA where Estudiante_uuid = '$($estViejo.Uuid)'"
    Escribir-Resultado "matricula: los intentos fallidos no dejan solicitud (obtuvo $enCurso)" ($enCurso -eq "0")

    $estCm1 = Nuevo-Estudiante -Token $tokenRoot -Materias 2
    $sol = Radicar-Matricula $estCm1 $pdfPazGrande
    $tamanio = Ejecutar-Sql "select tamanioBytes from ANEXO_ACADEMICO where SolicitudAcademica_uuid = '$($sol.Uuid)' and TipoAnexoAcademico_uuid = '$($obligatoriosCm[0])'"
    Escribir-Resultado "matricula: se radica con un solo PDF de paz y salvos de mas de 5 MB (obtuvo $tamanio bytes)" ([long]$tamanio -gt 5242880)
    $r = Subir-Anexo -Solicitud $sol.Uuid -Tipo $adicionalCm -Pdf $pdfAdicional -Token $estCm1.Token
    Escribir-Resultado "matricula: el estudiante no puede subir el documento adicional (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*FUNCIONARIO*"))
    $r = Subir-Anexo -Solicitud $sol.Uuid -Tipo $obligatoriosCm[0] -Pdf $pdfAnexo -Token $fa.Token
    Escribir-Resultado "matricula: el funcionario no puede subir un anexo del estudiante (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*ESTUDIANTE*"))
    $r = Subir-Anexo -Solicitud $sol.Uuid -Tipo "" -Pdf $pdfAnexo -Token $fa.Token
    Escribir-Resultado "matricula: el funcionario no puede subir un soporte libre (obtuvo $($r.Status))" ($r.Status -ne 200)
    Cargar-Archivos "matricula rechazo directo sin Resolucion" $sol.Uuid $adicionalCm $false $true
    Paso "matricula: el funcionario rechaza sin subir la Resolucion" (Invoke-Api -Metodo POST -Ruta "cancelaciones-matricula/$($sol.Uuid)/funcionario/rechazar" -Token $fa.Token -Cuerpo $observacion) $sol.Uuid "RECHAZADA"
    $filas = Ejecutar-Sql "select count(*) from RESOLUCION_ACADEMICA where SolicitudAcademica_uuid = '$($sol.Uuid)'"
    Escribir-Resultado "matricula rechazo directo: la solicitud queda sin fila en RESOLUCION_ACADEMICA (obtuvo $filas)" ($filas -eq "0")
    Verificar-Correo "matricula rechazo directo sin Resolucion" $estCm1 $Global:AsuntoCm @($nombreAdicional) (Textos-Carta $estCm1 "Su solicitud con radicado $($sol.Radicado) $($Global:TextoRechazoDirecto)")
    $correoEstudiante = @(Correos-Para $estCm1.Correo)[0]
    Escribir-Resultado "matricula rechazo directo: el correo no dice que se adjunta la resolucion" (-not ([string]$correoEstudiante.texto).ToLower().Contains("resoluci"))
    Verificar-Darca "matricula rechazo directo sin Resolucion" $estCm1 $sol $Global:DarcaCm "Cancelaci$($o)n de Matr$([char]0x00ED)cula" "Rechazada" @($nombreAdicional) "solicita cancelar su $($Global:Matricula) (rechazada)." @("Decisi$($o)n de la solicitud")
    $r = Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$($sol.Uuid)" -Token $estCm1.Token
    $detalleEstudiante = Leer-Json $r
    Escribir-Resultado "matricula rechazo directo: el detalle del estudiante responde 200 sin Resolucion ni descarga (obtuvo $($r.Status))" (($r.Status -eq 200) -and (-not $detalleEstudiante.tieneResolucion) -and (-not $detalleEstudiante.puedeDescargarResolucion))
    $r = Invoke-Api -Metodo GET -Ruta "cancelaciones-matricula/$($sol.Uuid)" -Token $fa.Token
    Escribir-Resultado "matricula rechazo directo: el detalle del funcionario responde 200 sin Resolucion (obtuvo $($r.Status))" ($r.Status -eq 200)
    $r = Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$($sol.Uuid)/resolucion" -Token $estCm1.Token
    Escribir-Resultado "matricula rechazo directo: pedir la Resolucion inexistente no rompe el backend (obtuvo $($r.Status))" ($r.Status -ne 200 -and $r.Status -lt 600)

    $estCm2 = Nuevo-Estudiante -Token $tokenRoot -Materias 2
    $sol = Radicar-Matricula $estCm2
    $evaluaciones = @(Filas-Asignatura $sol.Uuid | ForEach-Object { @{ asignaturaSolicitudUuid = $_ } })
    Paso "matricula: remite al Decano" (Invoke-Api -Metodo POST -Ruta "cancelaciones-matricula/$($sol.Uuid)/funcionario/remitir" -Token $fa.Token -Cuerpo @{ evaluaciones = $evaluaciones }) $sol.Uuid "EN_REVISION_DECANO"
    Paso "matricula: el Decano aprueba" (Invoke-Api -Metodo POST -Ruta "cancelaciones-matricula/$($sol.Uuid)/decano/aprobar" -Token $decano.Token -Cuerpo @{ observacion = "Visto" }) $sol.Uuid "APROBADA_POR_DECANO"
    Cargar-Archivos "matricula respuesta aprobada" $sol.Uuid $adicionalCm $true $true
    Paso "matricula: el funcionario envia la respuesta aprobada" (Invoke-Api -Metodo POST -Ruta "cancelaciones-matricula/$($sol.Uuid)/funcionario/responder" -Token $fa.Token -Cuerpo $observacion) $sol.Uuid "APROBADA"
    Verificar-Correo "matricula respuesta aprobada" $estCm2 $Global:AsuntoCm @("Resolucion-$($sol.Radicado).pdf", $nombreAdicional) (Textos-Carta $estCm2 $Global:TextoResolucion)
    Verificar-Darca "matricula respuesta aprobada" $estCm2 $sol $Global:DarcaCm "Cancelaci$($o)n de Matr$([char]0x00ED)cula" "Aprobada" @("Resolucion-$($sol.Radicado).pdf", $nombreAdicional) "cancela su $($Global:Matricula) (aprobada)." @("Decisi$($o)n de la solicitud")
    $fila = @(Leer-Json (Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/estudiante" -Token $estCm2.Token)) | Where-Object { $_.uuidSolicitudAcademica -eq $sol.Uuid }
    Escribir-Resultado "matricula respuesta aprobada: el estudiante ve la respuesta de inmediato, como hoy (obtuvo '$($fila.etiqueta)')" ($fila.etiqueta -eq "Aprobada")
    $r = Invoke-Api -Metodo GET -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/notificacion" -Token $fa.Token
    Escribir-Resultado "matricula: el panel de la comunicacion programada no aplica (obtuvo $($r.Status))" ($r.Status -ne 200)

    $estCm3 = Nuevo-Estudiante -Token $tokenRoot -Materias 1
    $sol = Radicar-Matricula $estCm3
    $evaluaciones = @(Filas-Asignatura $sol.Uuid | ForEach-Object { @{ asignaturaSolicitudUuid = $_ } })
    Paso "matricula: remite al Decano" (Invoke-Api -Metodo POST -Ruta "cancelaciones-matricula/$($sol.Uuid)/funcionario/remitir" -Token $fa.Token -Cuerpo @{ evaluaciones = $evaluaciones }) $sol.Uuid "EN_REVISION_DECANO"
    Paso "matricula: el Decano rechaza" (Invoke-Api -Metodo POST -Ruta "cancelaciones-matricula/$($sol.Uuid)/decano/rechazar" -Token $decano.Token -Cuerpo @{ observacion = "No procede" }) $sol.Uuid "RECHAZADA_POR_DECANO"
    $r = Invoke-Api -Metodo POST -Ruta "cancelaciones-matricula/$($sol.Uuid)/funcionario/responder" -Token $fa.Token -Cuerpo $observacion
    $etapa = Etapa-De $sol.Uuid
    Escribir-Resultado "matricula: tras el Decano la respuesta final sin Resolucion se rechaza (obtuvo $($r.Status) y ${etapa}: $(Mensaje-De $r))" (($r.Status -ne 200) -and ($etapa -eq "RECHAZADA_POR_DECANO") -and ((Mensaje-De $r) -like "*Resoluci*"))
    Cargar-Archivos "matricula respuesta rechazada sin documento adicional" $sol.Uuid $adicionalCm $true $false
    Paso "matricula: el funcionario envia la respuesta rechazada" (Invoke-Api -Metodo POST -Ruta "cancelaciones-matricula/$($sol.Uuid)/funcionario/responder" -Token $fa.Token -Cuerpo $observacion) $sol.Uuid "RECHAZADA"
    Verificar-Correo "matricula respuesta rechazada sin documento adicional" $estCm3 $Global:AsuntoCm @("Resolucion-$($sol.Radicado).pdf") (Textos-Carta $estCm3 $Global:TextoResolucion)
    Verificar-Darca "matricula respuesta rechazada sin documento adicional" $estCm3 $sol $Global:DarcaCm "Cancelaci$($o)n de Matr$([char]0x00ED)cula" "Rechazada" @("Resolucion-$($sol.Radicado).pdf") "solicita cancelar su $($Global:Matricula) (rechazada)." @("Decisi$($o)n de la solicitud")

    $estCa1 = Nuevo-Estudiante -Token $tokenRoot -Materias 2
    $sol = Radicar-Asignatura $estCa1
    $r = Subir-Anexo -Solicitud $sol.Uuid -Tipo $adicionalCa -Pdf $pdfAdicional -Token $estCa1.Token
    Escribir-Resultado "asignatura: el estudiante no puede subir el documento adicional (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*FUNCIONARIO*"))
    $r = Subir-Anexo -Solicitud $sol.Uuid -Tipo "" -Pdf $pdfAnexo -Token $fa.Token
    Escribir-Resultado "asignatura: el funcionario no puede subir un soporte libre (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*ESTUDIANTE*"))
    Cargar-Archivos "asignatura rechazo directo con una Resolucion cargada antes" $sol.Uuid $adicionalCa $true $true
    Paso "asignatura: el funcionario rechaza directo" (Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/rechazar" -Token $fa.Token -Cuerpo $observacion) $sol.Uuid "RECHAZADA"
    $filas = Ejecutar-Sql "select count(*) from RESOLUCION_ACADEMICA where SolicitudAcademica_uuid = '$($sol.Uuid)'"
    Escribir-Resultado "asignatura rechazo directo: la Resolucion que ya estaba cargada se conserva como hoy (obtuvo $filas filas)" ($filas -eq "1")
    Verificar-Correo "asignatura rechazo directo con una Resolucion cargada antes" $estCa1 $Global:AsuntoCa @($nombreAdicional) (Textos-Carta $estCa1 "Su solicitud con radicado $($sol.Radicado) $($Global:TextoRechazoDirecto)")
    Verificar-Darca "asignatura rechazo directo con una Resolucion cargada antes" $estCa1 $sol $Global:DarcaCa "Cancelaci$($o)n de Asignatura" "Rechazada" @($nombreAdicional) "solicita cancelar 2 asignaturas: 0 aprobadas y 2 rechazadas." @($estCa1.Materias[0].Codigo, $estCa1.Materias[0].Nombre, $estCa1.Materias[1].Codigo, $estCa1.Materias[1].Nombre, "Situaci$($o)n acad$([char]0x00E9)mica al cancelar", "No aplica")
    $r = Invoke-Api -Metodo GET -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/notificacion" -Token $fa.Token
    Escribir-Resultado "asignatura rechazo directo: no hay comunicacion programada (204, obtuvo $($r.Status))" ($r.Status -eq 204)
    $v = Vista-Estudiante $estCa1 $sol
    Escribir-Resultado "asignatura rechazo directo: el estudiante ve la respuesta de inmediato, como hoy (obtuvo '$($v.Etiqueta)', historial $($v.Acciones -join ','))" (($v.Etiqueta -eq "Rechazada") -and ($v.Acciones -contains "RECHAZAR_FUNCIONARIO"))

    $plantilla = $null
    $estCa2 = Nuevo-Estudiante -Token $tokenRoot -Materias 2
    $sol = Radicar-Asignatura $estCa2
    $filas = @(Filas-Asignatura $sol.Uuid)
    $evaluaciones = @($filas | ForEach-Object { @{ asignaturaSolicitudUuid = $_; cumpleCondiciones = $true; situacionCancelarUuid = $situacion } })
    Paso "asignatura: remite al Decano" (Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/remitir" -Token $fa.Token -Cuerpo @{ evaluaciones = $evaluaciones }) $sol.Uuid "EN_REVISION_DECANO"
    $decisiones = @(@{ asignaturaSolicitudUuid = $filas[0]; aprobada = $true }, @{ asignaturaSolicitudUuid = $filas[1]; aprobada = $false })
    Paso "asignatura: el Decano aprueba una asignatura y rechaza la otra" (Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$($sol.Uuid)/decano/aprobar" -Token $decano.Token -Cuerpo @{ decisiones = $decisiones }) $sol.Uuid "APROBADA_POR_DECANO"
    Cargar-Archivos "asignatura respuesta aprobada parcial" $sol.Uuid $adicionalCa $true $true
    $r = Invoke-Api -Metodo GET -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/notificacion/plantilla" -Token $fa.Token
    $plantilla = Leer-Json $r
    Escribir-Resultado "asignatura: la plantilla trae el mensaje con la firma de cuatro lineas, la decision propuesta, el anio y el sufijo (obtuvo $($r.Status), '$($plantilla.decisionPropuesta)', $($plantilla.anioResolucion), '$($plantilla.sufijoResolucion)')" (($r.Status -eq 200) -and ([string]$plantilla.mensajeInicial).Contains($Global:Firma) -and (-not ([string]$plantilla.mensajeInicial).Contains("[FIRMA]")) -and ($plantilla.decisionPropuesta -eq "aprueba parcialmente la") -and ($plantilla.anioResolucion -eq (Ahora-Bogota).Year) -and ($plantilla.sufijoResolucion -eq "/178"))
    foreach ($ajeno in @(@{ Nombre = "estudiante"; Token = $estCa2.Token }, @{ Nombre = "Decano"; Token = $decano.Token })) {
        $r = Invoke-Api -Metodo GET -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/notificacion/plantilla" -Token $ajeno.Token
        Escribir-Resultado "asignatura: el $($ajeno.Nombre) no pide la plantilla (403, obtuvo $($r.Status))" ($r.Status -eq 403)
    }
    $r = Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/responder" -Token $fa.Token -Cuerpo $observacion
    Escribir-Resultado "asignatura: sin programar la comunicacion no se envia la respuesta (obtuvo $($r.Status), etapa $(Etapa-De $sol.Uuid): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Etapa-De $sol.Uuid) -eq "APROBADA_POR_DECANO") -and ((Mensaje-De $r) -like "*programar la comunicaci*"))
    $r = Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/responder" -Token $fa.Token -Cuerpo @{ observacion = $Global:Privada; programacion = (Programacion -Segundos -120 -Numero "100-T9" -Decision "aprueba la") }
    Escribir-Resultado "asignatura: con una fecha pasada no se envia la respuesta (obtuvo $($r.Status), etapa $(Etapa-De $sol.Uuid): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Etapa-De $sol.Uuid) -eq "APROBADA_POR_DECANO") -and ((Mensaje-De $r) -like "*posteriores*"))
    $cuerpo = @{ observacion = $Global:Privada; enviarMensaje = $true; mensaje = $Global:MensajeEditado; programacion = (Programacion -Segundos $Global:Espera -Numero "101-T9" -Decision "aprueba parcialmente la") }
    Paso "asignatura: el funcionario envia la respuesta aprobada parcial con mensaje y comunicacion programada" (Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/responder" -Token $fa.Token -Cuerpo $cuerpo) $sol.Uuid "APROBADA"
    $solParcial = $sol
    Verificar-Mensaje-Inicial "asignatura parcial" $estCa2 $sol "acerquese a la Decanatura"
    Verificar-Sin-Comunicacion "asignatura parcial (antes de la fecha)" $estCa2 $sol
    Verificar-Retenida "asignatura parcial" $estCa2 $sol
    $panel = Leer-Json (Invoke-Api -Metodo GET -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/notificacion" -Token $fa.Token)
    Escribir-Resultado "asignatura parcial: el funcionario ve la programacion editable con el mensaje enviado (estado $($panel.estado), editable $($panel.editable))" (($panel.estado -eq "PROGRAMADA") -and $panel.editable -and ([string]$panel.mensajeInicial).Contains("acerquese a la Decanatura") -and (-not $panel.firmada))
    foreach ($ajeno in @(@{ Nombre = "estudiante"; Token = $estCa2.Token }, @{ Nombre = "Decano"; Token = $decano.Token })) {
        $r = Invoke-Api -Metodo GET -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/notificacion" -Token $ajeno.Token
        Escribir-Resultado "asignatura parcial: el $($ajeno.Nombre) no ve el panel (403, obtuvo $($r.Status))" ($r.Status -eq 403)
        $r = Invoke-Api -Metodo PUT -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/notificacion" -Token $ajeno.Token -Cuerpo (Programacion -Segundos 600 -Numero "X" -Decision "aprueba la")
        Escribir-Resultado "asignatura parcial: el $($ajeno.Nombre) no reprograma (403, obtuvo $($r.Status))" ($r.Status -eq 403)
    }
    $historialFa = (Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/$($sol.Uuid)/historial" -Token $fa.Token).Body
    Escribir-Resultado "asignatura parcial: el mensaje inicial no queda en el historial" ($historialFa -notmatch "acerquese a la Decanatura")
    $r = Invoke-Api -Metodo PUT -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/notificacion" -Token $fa.Token -Cuerpo (Programacion -Segundos -60 -Numero "102-T9" -Decision "aprueba parcialmente la")
    Escribir-Resultado "asignatura parcial: reprogramar a una fecha pasada se rechaza (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*posteriores*"))
    $r = Invoke-Api -Metodo PUT -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/notificacion" -Token $fa.Token -Cuerpo (Programacion -Segundos $Global:Espera -Numero "102-T9" -Decision "aprueba parcialmente la")
    $numero = Ejecutar-Sql "select numeroResolucion from NOTIFICACION_RESOLUCION where SolicitudAcademica_uuid = '$($sol.Uuid)'"
    Escribir-Resultado "asignatura parcial: el funcionario reprograma y corrige el numero antes de la fecha (obtuvo $($r.Status), numero $numero)" (($r.Status -eq 200) -and ($numero -eq "102-T9"))

    $estCa4 = Nuevo-Estudiante -Token $tokenRoot -Materias 1
    $sol = Radicar-Asignatura $estCa4
    $evaluaciones = @(Filas-Asignatura $sol.Uuid | ForEach-Object { @{ asignaturaSolicitudUuid = $_; cumpleCondiciones = $true; situacionCancelarUuid = $situacion } })
    Paso "asignatura: remite al Decano" (Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/remitir" -Token $fa.Token -Cuerpo @{ evaluaciones = $evaluaciones }) $sol.Uuid "EN_REVISION_DECANO"
    Paso "asignatura: el Decano acepta la evaluacion" (Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$($sol.Uuid)/decano/aprobar" -Token $decano.Token -Cuerpo @{ aceptarEvaluacion = $true }) $sol.Uuid "APROBADA_POR_DECANO"
    Cargar-Archivos "asignatura con firma del estudiante" $sol.Uuid $adicionalCa $true $false
    Paso "asignatura: el funcionario responde sin mensaje inicial" (Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/responder" -Token $fa.Token -Cuerpo @{ enviarMensaje = $false; mensaje = "no se envia"; programacion = (Programacion -Segundos $Global:Espera -Numero "201-T9" -Decision "aprueba la") }) $sol.Uuid "APROBADA"
    $solFirma = $sol
    $r = Ejecutar-Curl -Argumentos @("-F", "archivo=@$pdfFirmada;type=application/pdf") -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/notificacion/firma" -Token $decano.Token
    Escribir-Resultado "asignatura con firma: el Decano no registra la firma (403, obtuvo $($r.Status))" ($r.Status -eq 403)
    $r = Ejecutar-Curl -Argumentos @("-F", "archivo=@$pdfFirmada;type=application/pdf") -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/notificacion/firma" -Token $fa.Token
    $resolucion = Ejecutar-Sql "select nombreArchivo from RESOLUCION_ACADEMICA where SolicitudAcademica_uuid = '$($sol.Uuid)'"
    $firmada = Ejecutar-Sql "select firmada + 0 from NOTIFICACION_RESOLUCION where SolicitudAcademica_uuid = '$($sol.Uuid)'"
    Escribir-Resultado "asignatura con firma: el funcionario sube la Resolucion firmada, reemplaza la escaneada y queda marcada (obtuvo $($r.Status), archivo $resolucion, firmada $firmada)" (($r.Status -eq 200) -and ($resolucion -eq $nombreFirmada) -and ($firmada -eq "1"))
    Verificar-Retenida "asignatura con firma" $estCa4 $sol

    $estCa3 = Nuevo-Estudiante -Token $tokenRoot -Materias 1
    $sol = Radicar-Asignatura $estCa3
    $evaluaciones = @(Filas-Asignatura $sol.Uuid | ForEach-Object { @{ asignaturaSolicitudUuid = $_; cumpleCondiciones = $true; situacionCancelarUuid = $situacion } })
    Paso "asignatura: remite al Decano" (Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/remitir" -Token $fa.Token -Cuerpo @{ evaluaciones = $evaluaciones }) $sol.Uuid "EN_REVISION_DECANO"
    Paso "asignatura: el Decano rechaza" (Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$($sol.Uuid)/decano/rechazar" -Token $decano.Token -Cuerpo @{ observacion = "No procede" }) $sol.Uuid "RECHAZADA_POR_DECANO"
    Cargar-Archivos "asignatura respuesta rechazada sin documento adicional" $sol.Uuid $adicionalCa $true $false
    $propuesta = (Leer-Json (Invoke-Api -Metodo GET -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/notificacion/plantilla" -Token $fa.Token)).decisionPropuesta
    Escribir-Resultado "asignatura rechazada: la decision propuesta es 'rechaza la' (obtuvo '$propuesta')" ($propuesta -eq "rechaza la")
    Paso "asignatura: el funcionario envia la respuesta rechazada con el mensaje de la plantilla" (Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/responder" -Token $fa.Token -Cuerpo @{ observacion = $Global:Privada; enviarMensaje = $true; mensaje = $plantilla.mensajeInicial; programacion = (Programacion -Segundos $Global:Espera -Numero "301-T9" -Decision $propuesta) }) $sol.Uuid "RECHAZADA"
    $solRechazada = $sol
    Verificar-Mensaje-Inicial "asignatura rechazada" $estCa3 $sol $Global:Firma

    $estCaFalla = Nuevo-Estudiante -Token $tokenRoot -Materias 1 -Correo "smtp-rechaza"
    $sol = Radicar-Asignatura $estCaFalla
    $evaluaciones = @(Filas-Asignatura $sol.Uuid | ForEach-Object { @{ asignaturaSolicitudUuid = $_; cumpleCondiciones = $true; situacionCancelarUuid = $situacion } })
    Paso "asignatura: remite al Decano" (Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/remitir" -Token $fa.Token -Cuerpo @{ evaluaciones = $evaluaciones }) $sol.Uuid "EN_REVISION_DECANO"
    Paso "asignatura: el Decano acepta la evaluacion" (Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$($sol.Uuid)/decano/aprobar" -Token $decano.Token -Cuerpo @{ aceptarEvaluacion = $true }) $sol.Uuid "APROBADA_POR_DECANO"
    Cargar-Archivos "asignatura con el correo del estudiante rechazado" $sol.Uuid $adicionalCa $true $false
    Paso "asignatura: el funcionario responde a un estudiante cuyo correo rechaza el servidor" (Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$($sol.Uuid)/funcionario/responder" -Token $fa.Token -Cuerpo @{ enviarMensaje = $false; programacion = (Programacion -Segundos $Global:Espera -Numero "401-T9" -Decision "aprueba la") }) $sol.Uuid "APROBADA"
    $solFalla = $sol

    foreach ($caso in @(@{ Sol = $solParcial; Fin = "ENVIADA" }, @{ Sol = $solFirma; Fin = "ENVIADA" }, @{ Sol = $solRechazada; Fin = "ENVIADA" }, @{ Sol = $solFalla; Fin = "FALLIDA" })) {
        $estado = Esperar-Notificacion $caso.Sol.Uuid @($caso.Fin) 240
        Escribir-Resultado "asignatura $($caso.Sol.Radicado): la tarea programada procesa la comunicacion vencida y queda $($caso.Fin) (obtuvo $estado)" ($estado -eq $caso.Fin)
    }

    Verificar-Comunicacion "asignatura parcial" $estCa2 $solParcial "102-T9" "aprueba parcialmente la" @("Resolucion-$($solParcial.Radicado).pdf", $nombreAdicional)
    Verificar-Darca-Programada "asignatura parcial" $estCa2 $solParcial @("Resolucion-$($solParcial.Radicado).pdf", "Comunicacion-Estudiante-$($solParcial.Radicado).pdf", $nombreAdicional) "solicita cancelar 2 asignaturas: 1 aprobada y 1 rechazada." "102-T9" $true
    Verificar-Liberada "asignatura parcial" $estCa2 $solParcial "Aprobada" $true
    $r = Ejecutar-Curl -Argumentos @("-F", "archivo=@$pdfFirmada;type=application/pdf") -Ruta "cancelaciones-asignatura/$($solParcial.Uuid)/funcionario/notificacion/firma" -Token $fa.Token
    Escribir-Resultado "asignatura parcial: pasada la fecha no se registra la firma (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*ya no se puede modificar*"))
    $r = Invoke-Api -Metodo PUT -Ruta "cancelaciones-asignatura/$($solParcial.Uuid)/funcionario/notificacion" -Token $fa.Token -Cuerpo (Programacion -Segundos 600 -Numero "103-T9" -Decision "aprueba la")
    Escribir-Resultado "asignatura parcial: pasada la fecha no se edita la programacion (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*ya no se puede modificar*"))

    $correosFirma = @(Correos-Para $estCa4.Correo)
    Escribir-Resultado "asignatura con firma: el estudiante no recibe ningun correo, ni mensaje inicial ni comunicacion (obtuvo $($correosFirma.Count))" ($correosFirma.Count -eq 0)
    Verificar-Darca-Programada "asignatura con firma" $estCa4 $solFirma @("Resolucion-$($solFirma.Radicado).pdf") "cancela la asignatura $($estCa4.Materias[0].Nombre) (aprobada)." "" $false
    Verificar-Liberada "asignatura con firma" $estCa4 $solFirma "Aprobada" $false

    Verificar-Comunicacion "asignatura rechazada" $estCa3 $solRechazada "301-T9" "rechaza la" @("Resolucion-$($solRechazada.Radicado).pdf")
    Verificar-Darca-Programada "asignatura rechazada" $estCa3 $solRechazada @("Resolucion-$($solRechazada.Radicado).pdf", "Comunicacion-Estudiante-$($solRechazada.Radicado).pdf") "solicita cancelar la asignatura $($estCa3.Materias[0].Nombre) (rechazada)." "301-T9" $true
    Verificar-Liberada "asignatura rechazada" $estCa3 $solRechazada "Rechazada" $false

    $panel = Leer-Json (Invoke-Api -Metodo GET -Ruta "cancelaciones-asignatura/$($solFalla.Uuid)/funcionario/notificacion" -Token $fa.Token)
    Escribir-Resultado "asignatura con correo rechazado: tras tres intentos queda FALLIDA con el error visible al funcionario (estado $($panel.estado), intentos $($panel.intentos), error '$($panel.ultimoError)')" (($panel.estado -eq "FALLIDA") -and ($panel.intentos -eq 3) -and ([string]$panel.ultimoError -like "*Correo al estudiante*") -and (-not $panel.fechaEnvioEstudiante) -and $panel.fechaEnvioDarca)
    $darcaFalla = @(Correos-Con-Asunto $Global:CorreoDarca "$($Global:DarcaCa) - Radicado $($solFalla.Radicado)")
    Escribir-Resultado "asignatura con correo rechazado: DARCA recibe su correo una sola vez aunque el del estudiante falle (obtuvo $($darcaFalla.Count))" ($darcaFalla.Count -eq 1)
    Verificar-Liberada "asignatura con correo rechazado" $estCaFalla $solFalla "Aprobada" $false
    $r = Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$($solParcial.Uuid)/funcionario/notificacion/reintentar" -Token $fa.Token
    Escribir-Resultado "asignatura: una comunicacion ENVIADA no se reintenta (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like "*no est*fallida*"))
    $r = Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$($solFalla.Uuid)/funcionario/notificacion/reintentar" -Token $fa.Token
    Escribir-Resultado "asignatura con correo rechazado: el funcionario reintenta (200, obtuvo $($r.Status))" ($r.Status -eq 200)
    Start-Sleep -Seconds 3
    $estado = Esperar-Notificacion $solFalla.Uuid @("FALLIDA") 120
    $darcaFalla = @(Correos-Con-Asunto $Global:CorreoDarca "$($Global:DarcaCa) - Radicado $($solFalla.Radicado)")
    Escribir-Resultado "asignatura con correo rechazado: el reintento repite solo el correo que fallo, DARCA no lo recibe otra vez (estado $estado, DARCA $($darcaFalla.Count))" (($estado -eq "FALLIDA") -and ($darcaFalla.Count -eq 1))

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
    Verificar-Darca "supletorio rechazo del funcionario" $estEs1 $sol $Global:DarcaEs "Examen Supletorio" "Rechazada" @($nombreAdicional) "solicita examen supletorio de la asignatura $($estEs1.Materias[0].Codigo) - $($estEs1.Materias[0].Nombre) (rechazada)." @("$($estEs1.Materias[0].Codigo) - $($estEs1.Materias[0].Nombre)", $Global:AntesDelPago, "Sin asignar")
    Verificar-Sin-Profesor "supletorio rechazo del funcionario" $sol

    $estEs2 = Nuevo-Estudiante -Token $tokenRoot -Materias 1
    $sol = Radicar-Supletorio $estEs2
    Paso "supletorio: remite al Decano" (Invoke-Api -Metodo POST -Ruta "examenes-supletorios/$($sol.Uuid)/funcionario/remitir" -Token $fa.Token -Cuerpo @{ observacion = "Para el Decano" }) $sol.Uuid "EN_REVISION_DECANO"
    Paso "supletorio: el Decano rechaza" (Invoke-Api -Metodo POST -Ruta "examenes-supletorios/$($sol.Uuid)/decano/rechazar" -Token $decano.Token -Cuerpo @{ observacion = "No procede" }) $sol.Uuid "RECHAZADA_POR_DECANO"
    Cargar-Archivos "supletorio respuesta rechazada" $sol.Uuid $adicionalEs $false $true
    Paso "supletorio: el funcionario envia la respuesta rechazada" (Invoke-Api -Metodo POST -Ruta "examenes-supletorios/$($sol.Uuid)/funcionario/responder" -Token $fa.Token -Cuerpo $observacion) $sol.Uuid "RECHAZADA"
    Verificar-Correo "supletorio respuesta rechazada" $estEs2 $Global:AsuntoEs @($nombreAdicional) (Textos-Carta $estEs2 "con radicado $($sol.Radicado) $mensajeEs")
    Verificar-Darca "supletorio respuesta rechazada" $estEs2 $sol $Global:DarcaEs "Examen Supletorio" "Rechazada" @($nombreAdicional) "solicita examen supletorio de la asignatura $($estEs2.Materias[0].Codigo) - $($estEs2.Materias[0].Nombre) (rechazada)." @($Global:AntesDelPago, "Sin asignar")
    Verificar-Sin-Profesor "supletorio respuesta rechazada" $sol

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
        $correos = @(Correos-Con-Asunto $Global:CorreoDarca "$($Global:DarcaEs) - Radicado $($Solicitud.Radicado)")
        Escribir-Resultado "supletorio: las etapas intermedias no avisan a DARCA (obtuvo $($correos.Count))" ($correos.Count -eq 0)
    }

    $estEs3 = Nuevo-Estudiante -Token $tokenRoot -Materias 1
    $sol = Radicar-Supletorio $estEs3
    Llevar-A-Verificacion $estEs3 $sol
    Cargar-Archivos "supletorio aprobar comprobante" $sol.Uuid $adicionalEs $false $true
    Paso "supletorio: el funcionario aprueba el comprobante" (Invoke-Api -Metodo POST -Ruta "examenes-supletorios/$($sol.Uuid)/funcionario/comprobante/aprobar" -Token $fa.Token -Cuerpo @{ docenteUuid = $docente.Uuid; observacion = $Global:Privada }) $sol.Uuid "APROBADA"
    Verificar-Correo "supletorio aprobar comprobante" $estEs3 $Global:AsuntoEs @($nombreAdicional) (Textos-Carta $estEs3 "con radicado $($sol.Radicado) $mensajeEs")
    Verificar-Darca "supletorio aprobar comprobante" $estEs3 $sol $Global:DarcaEs "Examen Supletorio" "Aprobada" @($nombreAdicional) "solicita examen supletorio de la asignatura $($estEs3.Materias[0].Codigo) - $($estEs3.Materias[0].Nombre) (aprobada)." @("Decisi$($o)n sobre el comprobante", "Aprobado", "Profesor asignado", $profesor.Nombre)
    Verificar-Profesor "supletorio aprobar comprobante" $estEs3 $sol $profesor
    $fila = @(Leer-Json (Invoke-Api -Metodo GET -Ruta "solicitudes-academicas/estudiante" -Token $estEs3.Token)) | Where-Object { $_.uuidSolicitudAcademica -eq $sol.Uuid }
    Escribir-Resultado "supletorio aprobar comprobante: el estudiante ve la respuesta de inmediato, como hoy (obtuvo '$($fila.etiqueta)')" ($fila.etiqueta -eq "Aprobada")
    $notificaciones = Ejecutar-Sql "select count(*) from NOTIFICACION_DOCENTE where SolicitudAcademica_uuid = '$($sol.Uuid)'"
    Escribir-Resultado "supletorio aprobar comprobante: el profesor tiene su notificacion en el perfil (obtuvo $notificaciones)" ($notificaciones -eq "1")

    $estEs4 = Nuevo-Estudiante -Token $tokenRoot -Materias 1
    $sol = Radicar-Supletorio $estEs4
    Llevar-A-Verificacion $estEs4 $sol
    Paso "supletorio: el funcionario rechaza el comprobante sin documento adicional" (Invoke-Api -Metodo POST -Ruta "examenes-supletorios/$($sol.Uuid)/funcionario/comprobante/rechazar" -Token $fa.Token -Cuerpo $observacion) $sol.Uuid "RECHAZADA"
    Verificar-Correo "supletorio rechazar comprobante sin documento adicional" $estEs4 $Global:AsuntoEs @() (Textos-Carta $estEs4 "con radicado $($sol.Radicado) $mensajeEs")
    Verificar-Darca "supletorio rechazar comprobante sin documento adicional" $estEs4 $sol $Global:DarcaEs "Examen Supletorio" "Rechazada" @() "solicita examen supletorio de la asignatura $($estEs4.Materias[0].Codigo) - $($estEs4.Materias[0].Nombre) (rechazada)." @("Rechazado", "Sin asignar")
    Verificar-Sin-Profesor "supletorio rechazar comprobante" $sol

    $estEs5 = Nuevo-Estudiante -Token $tokenRoot -Materias 1
    $sol = Radicar-Supletorio $estEs5
    Llevar-A-Verificacion $estEs5 $sol
    Paso "supletorio: si el servidor rechaza el correo del profesor la aprobacion igual queda guardada" (Invoke-Api -Metodo POST -Ruta "examenes-supletorios/$($sol.Uuid)/funcionario/comprobante/aprobar" -Token $fa.Token -Cuerpo @{ docenteUuid = $profesorRechazado.Uuid }) $sol.Uuid "APROBADA"
    Verificar-Correo "supletorio con el correo del profesor rechazado" $estEs5 $Global:AsuntoEs @() (Textos-Carta $estEs5 "con radicado $($sol.Radicado) $mensajeEs")
    Verificar-Darca "supletorio con el correo del profesor rechazado" $estEs5 $sol $Global:DarcaEs "Examen Supletorio" "Aprobada" @() "solicita examen supletorio de la asignatura $($estEs5.Materias[0].Codigo) - $($estEs5.Materias[0].Nombre) (aprobada)." @("Aprobado", $profesorRechazado.Nombre)
    Escribir-Resultado "supletorio con el correo del profesor rechazado: el correo del profesor no queda entregado" (@(Correos-Para $profesorRechazado.Correo).Count -eq 0)
    $notificaciones = Ejecutar-Sql "select count(*) from NOTIFICACION_DOCENTE where SolicitudAcademica_uuid = '$($sol.Uuid)'"
    Escribir-Resultado "supletorio con el correo del profesor rechazado: la notificacion en el perfil queda creada (obtuvo $notificaciones)" ($notificaciones -eq "1")
    Escribir-Resultado "el profesor recibio solo el correo de su aprobacion (obtuvo $(@(Correos-Para $profesor.Correo).Count))" (@(Correos-Para $profesor.Correo).Count -eq 1)

    $estFalla = Nuevo-Estudiante -Token $tokenRoot -Materias 1 -Correo "smtp-rechaza"
    $sol = Radicar-Matricula $estFalla
    Cargar-Archivos "matricula con el correo rechazado por el servidor" $sol.Uuid $adicionalCm $false $true
    Paso "matricula: si el servidor de correo rechaza el envio la respuesta igual queda guardada" (Invoke-Api -Metodo POST -Ruta "cancelaciones-matricula/$($sol.Uuid)/funcionario/rechazar" -Token $fa.Token -Cuerpo $observacion) $sol.Uuid "RECHAZADA"
    Start-Sleep -Seconds 5
    Escribir-Resultado "matricula: el correo rechazado no queda entregado" (@(Correos-Para $estFalla.Correo).Count -eq 0)
    $historial = Ejecutar-Sql "select count(*) from HISTORIAL_SOLICITUD_ACADEMICA where SolicitudAcademica_uuid = '$($sol.Uuid)' and accion = 'RECHAZAR_FUNCIONARIO'"
    Escribir-Resultado "matricula: el rechazo queda en el historial aunque el correo falle (obtuvo $historial)" ($historial -eq "1")
    Verificar-Darca "matricula con el correo del estudiante rechazado" $estFalla $sol $Global:DarcaCm "Cancelaci$($o)n de Matr$([char]0x00ED)cula" "Rechazada" @($nombreAdicional) "solicita cancelar su $($Global:Matricula) (rechazada)." @("Decisi$($o)n de la solicitud")
    if ($Global:LogBackend -and (Test-Path $Global:LogBackend)) {
        $nuevas = @(Get-Content -LiteralPath $Global:LogBackend | Select-Object -Skip $lineasLogIniciales)
        $registro = @($nuevas | Where-Object { $_ -like "*No se pudo enviar el correo*" -and $_ -like "*$($estFalla.Correo)*" })
        Escribir-Resultado "matricula: el fallo del correo queda en el log del backend (obtuvo $($registro.Count) lineas)" ($registro.Count -ge 1)
        $registro = @($nuevas | Where-Object { $_ -like "*No se pudo enviar el correo*" -and $_ -like "*$($profesorRechazado.Correo)*" -and $_ -like "*Radicado*" })
        Escribir-Resultado "supletorio: el fallo del correo del profesor queda en el log del backend con su asunto (obtuvo $($registro.Count) lineas)" ($registro.Count -ge 1)
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
        $sentencias += "delete from NOTIFICACION_RESOLUCION where SolicitudAcademica_uuid in ($sol)"
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
