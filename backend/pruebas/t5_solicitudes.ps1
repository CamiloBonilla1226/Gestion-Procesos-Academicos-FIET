. "$PSScriptRoot\comun.ps1"

$Global:CarpetaBackend = (Resolve-Path "$PSScriptRoot\..").Path
$Global:CarpetaAnexos = Join-Path $Global:CarpetaBackend "uploads\anexos"
$Global:Ruta = "solicitudes-academicas"

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
    param([string]$Token)
    $s = Nuevo-Sufijo
    $cuerpo = @{
        nombres           = "Solicitante"
        apellidos         = "Prueba $s"
        tipoDocumento     = $Global:CedulaCiudadania
        numeroDocumento   = "6$s"
        telefono          = "+573000000000"
        correoElectronico = "sol$s@unicauca.edu.co"
        username          = "sol$s"
        password          = "Clave12345"
        codigoEstudiantil = "S$s"
        programaAcademico = "Ingenieria de Sistemas"
        semestre          = "5"
        facultad          = "FIET"
        asignaturas       = @(@{ codigoAsignatura = "T5$s"; nombreAsignatura = "Materia T5 $s"; grupo = "A" })
    }
    $r = Invoke-Api -Metodo POST -Ruta "estudiantes" -Token $Token -Cuerpo $cuerpo
    if ($r.Status -ne 200) { throw "No se pudo crear el estudiante ($($r.Status)): $($r.Body)" }
    $creado = Leer-Json $r
    return [pscustomobject]@{
        Uuid    = $creado.uuidUsuario
        Codigo  = "T5$s"
        Token   = Iniciar-Sesion -Usuario "sol$s" -Clave "Clave12345"
    }
}

function Nuevo-Funcionario {
    param([string]$Token)
    $s = Nuevo-Sufijo
    $cuerpo = @{
        nombres           = "Revisor"
        apellidos         = "Prueba $s"
        tipoDocumento     = $Global:CedulaCiudadania
        numeroDocumento   = "5$s"
        telefono          = "+573000000000"
        correoElectronico = "rev$s@unicauca.edu.co"
        username          = "rev$s"
        password          = "Clave12345"
        dependencia       = "DepT5 $s"
    }
    $r = Invoke-Api -Metodo POST -Ruta "funcionarios-academicos" -Token $Token -Cuerpo $cuerpo
    if ($r.Status -ne 200) { throw "No se pudo crear el funcionario academico ($($r.Status)): $($r.Body)" }
    return [pscustomobject]@{
        Uuid  = (Leer-Json $r).uuidUsuario
        Token = Iniciar-Sesion -Usuario "rev$s" -Clave "Clave12345"
    }
}

function Nuevo-Pdf {
    param([string]$Texto)
    $ruta = Join-Path ([System.IO.Path]::GetTempPath()) ("t5_" + (Nuevo-Sufijo) + ".pdf")
    $contenido = "%PDF-1.4`n1 0 obj << /Type /Catalog >> endobj`n% $Texto`n%%EOF`n"
    [System.IO.File]::WriteAllBytes($ruta, [System.Text.Encoding]::ASCII.GetBytes($contenido))
    return $ruta
}

function Subir-Multipart {
    param([string]$Ruta, [string]$Archivo, [string]$Token, [string]$TipoAnexo)
    $salida = [System.IO.Path]::GetTempFileName()
    $argumentos = @("-s", "-o", $salida, "-w", "%{http_code}", "-X", "POST", "-F", "archivo=@$Archivo;type=application/pdf")
    if ($TipoAnexo) { $argumentos += @("-F", "tipoAnexo=$TipoAnexo") }
    if ($Token) { $argumentos += @("-H", "Authorization: Bearer $Token") }
    $argumentos += ($Global:BaseUrl + $Ruta)
    $codigo = & curl.exe @argumentos
    $cuerpo = [System.IO.File]::ReadAllText($salida, [System.Text.Encoding]::UTF8)
    Remove-Item $salida -Force
    return [pscustomobject]@{ Status = [int]$codigo; Body = $cuerpo }
}

function Descargar {
    param([string]$Ruta, [string]$Token)
    $salida = [System.IO.Path]::GetTempFileName()
    $argumentos = @("-s", "-o", $salida, "-w", "%{http_code}", "-D", "-", "-X", "GET")
    if ($Token) { $argumentos += @("-H", "Authorization: Bearer $Token") }
    $argumentos += ($Global:BaseUrl + $Ruta)
    $texto = (& curl.exe @argumentos) -join "`n"
    $codigo = [int]($texto.Substring($texto.Length - 3))
    $bytes = [System.IO.File]::ReadAllBytes($salida)
    Remove-Item $salida -Force
    return [pscustomobject]@{ Status = $codigo; Bytes = $bytes; Cabeceras = $texto }
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

function Uuids-DeLista {
    param($Respuesta)
    if ($Respuesta.Status -ne 200) { return @() }
    return @((Leer-Json $Respuesta) | ForEach-Object { $_.uuidSolicitudAcademica })
}

function Insertar-Solicitud {
    param([string]$Uuid, [string]$Radicado, [string]$Estudiante, [string]$Tipo, [string]$Etapa, [string]$Fecha)
    Ejecutar-Sql ("insert into SOLICITUD_ACADEMICA (uuidSolicitudAcademica, radicado, Estudiante_uuid, TipoSolicitudAcademica_uuid, Etapa_uuid, fechaCreacion) " +
            "values ('$Uuid', '$Radicado', '$Estudiante', '$Tipo', (select uuidEtapa from ETAPA_SOLICITUD_ACADEMICA where codigo = '$Etapa'), '$Fecha')") | Out-Null
}

function Insertar-Historial {
    param([string]$Solicitud, [string]$Usuario, [string]$Accion, [string]$Fecha)
    Ejecutar-Sql ("insert into HISTORIAL_SOLICITUD_ACADEMICA (uuidHistorial, accion, fecha, SolicitudAcademica_uuid, Usuario_uuid) " +
            "values ('$([guid]::NewGuid())', '$Accion', '$Fecha', '$Solicitud', '$Usuario')") | Out-Null
}

$tokenRoot = Iniciar-Sesion -Usuario "rootfiet" -Clave "rootfiet1234"
$tipoCm = Ejecutar-Sql "select uuidTipoSolicitudAcademica from TIPO_SOLICITUD_ACADEMICA where nombre like 'Cancelaci%n de Matr%cula'"
$tipoCa = Ejecutar-Sql "select uuidTipoSolicitudAcademica from TIPO_SOLICITUD_ACADEMICA where nombre like 'Cancelaci%n de Asignatura'"
$tipoPaz = Ejecutar-Sql "select uuidTipoAnexoAcademico from TIPO_ANEXO_ACADEMICO where TipoSolicitudAcademica_uuid = '$tipoCm' and nombre like 'Paz y salvo - Divisi%n de Bibliotecas'"
$responsableCm = Ejecutar-Sql "select coalesce(FuncionarioAcademico_uuid, '') from TIPO_SOLICITUD_ACADEMICA where uuidTipoSolicitudAcademica = '$tipoCm'"
if (-not $tipoCm -or -not $tipoCa -or -not $tipoPaz) { Write-Host "FAIL faltan los catalogos sembrados de procesos academicos"; exit 1 }
$habiaAnexos = Test-Path $Global:CarpetaAnexos

$usuarios = @()
$estudiantes = @()
$funcionarios = @()
$codigos = @()
$solicitudes = @()
$archivos = @()

try {
    $est1 = Nuevo-Estudiante -Token $tokenRoot
    $est2 = Nuevo-Estudiante -Token $tokenRoot
    $fa1 = Nuevo-Funcionario -Token $tokenRoot
    $fa2 = Nuevo-Funcionario -Token $tokenRoot
    $decano = Crear-UsuarioPrueba -Perfil "Decano" -TokenAdmin $tokenRoot -Prefijo "dect5"
    $estudiantes = @($est1.Uuid, $est2.Uuid)
    $funcionarios = @($fa1.Uuid, $fa2.Uuid)
    $codigos = @($est1.Codigo, $est2.Codigo)
    $usuarios = @($est1.Uuid, $est2.Uuid, $fa1.Uuid, $fa2.Uuid, $decano.Uuid)

    $r = Invoke-Api -Metodo PUT -Ruta "catalogos-academicos/tipos-solicitud/$tipoCm/funcionario" -Token $tokenRoot -Cuerpo @{ funcionarioUuid = $fa1.Uuid }
    if ($r.Status -ne 200) { throw "No se pudo asignar Cancelacion de Matricula al funcionario de prueba ($($r.Status)): $($r.Body)" }

    $n = Get-Random -Minimum 1000 -Maximum 9999
    $s1 = [guid]::NewGuid().ToString(); $s3 = [guid]::NewGuid().ToString(); $s4 = [guid]::NewGuid().ToString()
    $solicitudes = @($s1, $s3, $s4)
    Insertar-Solicitud -Uuid $s1 -Radicado "1999-CM-$n" -Estudiante $est1.Uuid -Tipo $tipoCm -Etapa "RADICADA" -Fecha "2026-01-01 10:00:00"
    Insertar-Solicitud -Uuid $s3 -Radicado "1999-CM-$($n + 1)" -Estudiante $est2.Uuid -Tipo $tipoCm -Etapa "EN_REVISION_DECANO" -Fecha "2026-01-02 10:00:00"
    Insertar-Solicitud -Uuid $s4 -Radicado "1999-CA-$n" -Estudiante $est1.Uuid -Tipo $tipoCa -Etapa "RADICADA" -Fecha "2026-01-03 10:00:00"
    Insertar-Historial -Solicitud $s1 -Usuario $est1.Uuid -Accion "RADICAR" -Fecha "2026-01-01 10:00:00"
    Insertar-Historial -Solicitud $s3 -Usuario $est2.Uuid -Accion "RADICAR" -Fecha "2026-01-02 10:00:00"
    Insertar-Historial -Solicitud $s3 -Usuario $fa1.Uuid -Accion "REMITIR_DECANO" -Fecha "2026-01-02 11:00:00"
    $insertadas = Ejecutar-Sql "select count(*) from SOLICITUD_ACADEMICA where uuidSolicitudAcademica in ($(Lista-Sql $solicitudes))"
    Escribir-Resultado "se insertaron las tres solicitudes de prueba en MySQL (obtuvo $insertadas)" ($insertadas -eq "3")

    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/estudiante"
    Escribir-Resultado "bandeja sin token (401, obtuvo $($r.Status))" ($r.Status -eq 401)
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$s1"
    Escribir-Resultado "detalle sin token (401, obtuvo $($r.Status))" ($r.Status -eq 401)
    $pdfAnonimo = Nuevo-Pdf "anonimo"; $archivos += $pdfAnonimo
    $r = Subir-Multipart -Ruta "$Global:Ruta/$s1/anexos" -Archivo $pdfAnonimo
    Escribir-Resultado "subir anexo sin token (401, obtuvo $($r.Status))" ($r.Status -eq 401)

    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/estudiante" -Token $est1.Token
    $lista = @(Uuids-DeLista $r)
    Escribir-Resultado "el estudiante 1 ve sus dos solicitudes de la mas reciente a la mas antigua (obtuvo $($lista -join ','))" (($r.Status -eq 200) -and ($lista.Count -eq 2) -and ($lista[0] -eq $s4) -and ($lista[1] -eq $s1))
    $item = @(Leer-Json $r) | Where-Object { $_.uuidSolicitudAcademica -eq $s1 }
    $enTramite = "En tr$([char]0x00E1)mite"
    Escribir-Resultado "cada item trae la etiqueta del estudiante y no el codigo de etapa (obtuvo $($item.etiqueta))" (($item.etiqueta -eq $enTramite) -and ($r.Body -notmatch 'RADICADA') -and ($r.Body -notmatch '"codigo"'))
    Escribir-Resultado "el item trae radicado y tipo" (($item.radicado -eq "1999-CM-$n") -and ($item.uuidTipoSolicitudAcademica -eq $tipoCm))
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/estudiante" -Token $est2.Token
    $lista = @(Uuids-DeLista $r)
    Escribir-Resultado "el estudiante 2 solo ve la suya (obtuvo $($lista -join ','))" (($lista.Count -eq 1) -and ($lista[0] -eq $s3))

    $inexistente = [guid]::NewGuid().ToString()
    $ajena = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$s1" -Token $est2.Token
    $noExiste = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$inexistente" -Token $est2.Token
    $firmaAjena = Firma-Error $ajena $s1
    $firmaNoExiste = Firma-Error $noExiste $inexistente
    Escribir-Resultado "detalle ajeno responde igual que uno inexistente ($firmaAjena)" (($ajena.Status -ne 200) -and ($firmaAjena -eq $firmaNoExiste))
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$s1/historial" -Token $est2.Token
    Escribir-Resultado "historial ajeno responde igual que uno inexistente" ((Firma-Error $r $s1) -eq $firmaNoExiste)

    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/funcionario" -Token $fa1.Token
    $lista = @(Uuids-DeLista $r | Where-Object { $solicitudes -contains $_ })
    Escribir-Resultado "el funcionario asignado ve las de su tipo en orden (obtuvo $($lista -join ','))" (($r.Status -eq 200) -and ($lista.Count -eq 2) -and ($lista[0] -eq $s3) -and ($lista[1] -eq $s1))
    $item = @(Leer-Json $r) | Where-Object { $_.uuidSolicitudAcademica -eq $s1 }
    Escribir-Resultado "el funcionario ve su etiqueta Pendiente (obtuvo $($item.etiqueta))" ($item.etiqueta -eq "Pendiente")
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/funcionario" -Token $fa2.Token
    $lista = @(Uuids-DeLista $r | Where-Object { $solicitudes -contains $_ })
    Escribir-Resultado "el funcionario no asignado no ve ninguna de prueba (200, obtuvo $($r.Status) con $($lista.Count))" (($r.Status -eq 200) -and ($lista.Count -eq 0))
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$s1" -Token $fa2.Token
    Escribir-Resultado "detalle para el funcionario no asignado responde igual que uno inexistente" ((Firma-Error $r $s1) -eq $firmaNoExiste)

    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/decano" -Token $decano.Token
    $lista = @(Uuids-DeLista $r | Where-Object { $solicitudes -contains $_ })
    Escribir-Resultado "el Decano ve la que esta en revision y no las radicadas (obtuvo $($lista -join ','))" (($r.Status -eq 200) -and ($lista.Count -eq 1) -and ($lista[0] -eq $s3))
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$s1" -Token $decano.Token
    Escribir-Resultado "el Decano no ve el detalle de una radicada" ((Firma-Error $r $s1) -eq $firmaNoExiste)
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$s3" -Token $decano.Token
    $detalle = Leer-Json $r
    Escribir-Resultado "el Decano ve el detalle en revision con sus acciones (obtuvo $($detalle.accionesDisponibles -join ','))" (($r.Status -eq 200) -and ($detalle.etiqueta -eq "Pendiente") -and (($detalle.accionesDisponibles -join ',') -eq "APROBAR_DECANO,RECHAZAR_DECANO"))

    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$s1" -Token $est1.Token
    $detalle = Leer-Json $r
    $basicos = ($detalle.radicado -eq "1999-CM-$n") -and ($detalle.uuidTipoSolicitudAcademica -eq $tipoCm) -and ($detalle.fechaCreacion -like "2026-01-01T10:00*")
    $deEstudiante = ($detalle.estudiante.uuidUsuario -eq $est1.Uuid) -and ($detalle.estudiante.nombres -eq "Solicitante") -and ($detalle.estudiante.codigoEstudiantil)
    Escribir-Resultado "el detalle del duenio trae radicado, tipo, fecha y datos del estudiante" ($basicos -and $deEstudiante)
    Escribir-Resultado "el duenio no tiene acciones ni resolucion en RADICADA" ((@($detalle.accionesDisponibles).Count -eq 0) -and (-not $detalle.tieneResolucion) -and (-not $detalle.puedeDescargarResolucion) -and ($detalle.etiqueta -eq $enTramite))
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$s1" -Token $fa1.Token
    $detalle = Leer-Json $r
    Escribir-Resultado "el funcionario asignado ve sus acciones en RADICADA (obtuvo $($detalle.accionesDisponibles -join ','))" (($detalle.accionesDisponibles -join ',') -eq "RECHAZAR_FUNCIONARIO,REMITIR_DECANO")

    $pdfAnexo = Nuevo-Pdf "anexo"; $archivos += $pdfAnexo
    $r = Subir-Multipart -Ruta "$Global:Ruta/$s1/anexos" -Archivo $pdfAnexo -Token $est1.Token -TipoAnexo $tipoPaz
    $anexo = if ($r.Status -eq 200) { $r.Body | ConvertFrom-Json } else { $null }
    Escribir-Resultado "el duenio sube un paz y salvo en PDF (200, obtuvo $($r.Status))" (($r.Status -eq 200) -and ($anexo.uuidAnexoAcademico) -and ($anexo.uuidTipoAnexoAcademico -eq $tipoPaz))
    Escribir-Resultado "la respuesta del anexo no trae rutas de disco" (($r.Body -notmatch 'urlArchivo') -and ($r.Body -notmatch '/anexos/'))
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$s1" -Token $est1.Token
    $detalle = Leer-Json $r
    $fila = @($detalle.anexos) | Where-Object { $_.uuidAnexoAcademico -eq $anexo.uuidAnexoAcademico }
    $tamanio = (Get-Item $pdfAnexo).Length
    Escribir-Resultado "el detalle lista el anexo con nombre, tipo, tipo de archivo, tamano y fecha" (($fila.nombreArchivo -eq (Split-Path $pdfAnexo -Leaf)) -and ($fila.tipoAnexo -like "Paz y salvo*") -and ($fila.tipoArchivo -eq "application/pdf") -and ($fila.tamanioBytes -eq $tamanio) -and ($fila.fechaSubida))
    Escribir-Resultado "el detalle no trae rutas de disco" (($r.Body -notmatch 'urlArchivo') -and ($r.Body -notmatch '/anexos/') -and ($r.Body -notmatch 'uploads'))
    $d = Descargar -Ruta "$Global:Ruta/$s1/anexos/$($anexo.uuidAnexoAcademico)" -Token $est1.Token
    Escribir-Resultado "el duenio baja el anexo identico (200, obtuvo $($d.Status))" (($d.Status -eq 200) -and (Mismos-Bytes $pdfAnexo $d.Bytes) -and ($d.Cabeceras -match 'attachment'))
    $d = Descargar -Ruta "$Global:Ruta/$s1/anexos/$($anexo.uuidAnexoAcademico)" -Token $fa1.Token
    Escribir-Resultado "el funcionario asignado baja el anexo identico (200, obtuvo $($d.Status))" (($d.Status -eq 200) -and (Mismos-Bytes $pdfAnexo $d.Bytes))
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$s3/anexos/$($anexo.uuidAnexoAcademico)" -Token $fa1.Token
    Escribir-Resultado "un anexo pedido por otra solicitud no se entrega (obtuvo $($r.Status))" ($r.Status -ne 200)
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$s1/anexos/$($anexo.uuidAnexoAcademico)" -Token $est2.Token
    Escribir-Resultado "otro estudiante no baja el anexo y la respuesta es la de inexistente" ((Firma-Error $r $s1) -eq $firmaNoExiste)

    $pdfResolucion = Nuevo-Pdf "resolucion"; $archivos += $pdfResolucion
    $r = Subir-Multipart -Ruta "$Global:Ruta/$s1/resolucion" -Archivo $pdfResolucion -Token $fa1.Token
    $resolucion = if ($r.Status -eq 200) { $r.Body | ConvertFrom-Json } else { $null }
    Escribir-Resultado "el funcionario asignado sube la Resolucion en PDF (200, obtuvo $($r.Status))" (($r.Status -eq 200) -and ($resolucion.nombreArchivo -eq (Split-Path $pdfResolucion -Leaf)) -and ($r.Body -notmatch 'urlArchivo'))
    $r = Subir-Multipart -Ruta "$Global:Ruta/$s1/resolucion" -Archivo $pdfResolucion -Token $fa2.Token
    Escribir-Resultado "el funcionario no asignado no sube la Resolucion y la respuesta es la de inexistente" ((Firma-Error $r $s1) -eq $firmaNoExiste)
    $d = Descargar -Ruta "$Global:Ruta/$s1/resolucion" -Token $fa1.Token
    Escribir-Resultado "el funcionario baja la Resolucion identica (200, obtuvo $($d.Status))" (($d.Status -eq 200) -and (Mismos-Bytes $pdfResolucion $d.Bytes))
    $d = Descargar -Ruta "$Global:Ruta/$s1/resolucion" -Token $est1.Token
    Escribir-Resultado "el duenio no baja la Resolucion antes de la etapa final (obtuvo $($d.Status))" ($d.Status -ne 200)
    $detalle = Leer-Json (Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$s1" -Token $est1.Token)
    Escribir-Resultado "el detalle del duenio indica Resolucion sin descarga" ($detalle.tieneResolucion -and (-not $detalle.puedeDescargarResolucion))

    Ejecutar-Sql "update SOLICITUD_ACADEMICA set Etapa_uuid = (select uuidEtapa from ETAPA_SOLICITUD_ACADEMICA where codigo = 'RECHAZADA') where uuidSolicitudAcademica = '$s1'" | Out-Null
    $d = Descargar -Ruta "$Global:Ruta/$s1/resolucion" -Token $est1.Token
    Escribir-Resultado "en RECHAZADA el duenio baja la Resolucion identica (200, obtuvo $($d.Status))" (($d.Status -eq 200) -and (Mismos-Bytes $pdfResolucion $d.Bytes))
    $detalle = Leer-Json (Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$s1" -Token $est1.Token)
    Escribir-Resultado "en RECHAZADA el detalle del duenio permite la descarga (etiqueta $($detalle.etiqueta))" ($detalle.puedeDescargarResolucion -and ($detalle.etiqueta -eq "Rechazada"))

    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$s3/historial" -Token $est2.Token
    $historial = @(Leer-Json $r)
    Escribir-Resultado "el historial trae RADICAR y REMITIR_DECANO en orden (obtuvo $(($historial | ForEach-Object { $_.accion }) -join ','))" (($r.Status -eq 200) -and ($historial.Count -eq 2) -and ($historial[0].accion -eq "RADICAR") -and ($historial[1].accion -eq "REMITIR_DECANO"))
    Escribir-Resultado "cada fila del historial trae fecha y quien actuo" (($historial[0].nombresUsuario -eq "Solicitante") -and ($historial[1].nombresUsuario -eq "Revisor") -and ($historial[1].fecha -like "2026-01-02T11:00*"))
    $r = Invoke-Api -Metodo GET -Ruta "$Global:Ruta/$s3/historial" -Token $decano.Token
    Escribir-Resultado "el Decano ve el historial en revision (200, obtuvo $($r.Status))" ($r.Status -eq 200)

    $casos = @(
        @{ Metodo = "GET"; Ruta = "estudiante"; Tokens = @{ "Funcionario Academico" = $fa1.Token; "Decano" = $decano.Token; "Secretario General" = $tokenRoot } },
        @{ Metodo = "GET"; Ruta = "funcionario"; Tokens = @{ "Estudiante" = $est1.Token; "Decano" = $decano.Token; "Secretario General" = $tokenRoot } },
        @{ Metodo = "GET"; Ruta = "decano"; Tokens = @{ "Estudiante" = $est1.Token; "Funcionario Academico" = $fa1.Token; "Secretario General" = $tokenRoot } },
        @{ Metodo = "GET"; Ruta = "$s3"; Tokens = @{ "Secretario General" = $tokenRoot } },
        @{ Metodo = "GET"; Ruta = "$s3/historial"; Tokens = @{ "Secretario General" = $tokenRoot } },
        @{ Metodo = "GET"; Ruta = "$s1/anexos/$($anexo.uuidAnexoAcademico)"; Tokens = @{ "Secretario General" = $tokenRoot } },
        @{ Metodo = "GET"; Ruta = "$s1/resolucion"; Tokens = @{ "Secretario General" = $tokenRoot } },
        @{ Metodo = "POST"; Ruta = "$s1/anexos"; Tokens = @{ "Decano" = $decano.Token; "Secretario General" = $tokenRoot } },
        @{ Metodo = "POST"; Ruta = "$s1/resolucion"; Tokens = @{ "Estudiante" = $est1.Token; "Decano" = $decano.Token; "Secretario General" = $tokenRoot } },
        @{ Metodo = "GET"; Ruta = ""; Tokens = @{ "Estudiante" = $est1.Token } }
    )
    foreach ($caso in $casos) {
        foreach ($rol in $caso.Tokens.Keys) {
            $ruta = if ($caso.Ruta) { "$Global:Ruta/$($caso.Ruta)" } else { $Global:Ruta }
            if ($caso.Metodo -eq "POST") { $r = Subir-Multipart -Ruta $ruta -Archivo $pdfAnexo -Token $caso.Tokens[$rol] }
            else { $r = Invoke-Api -Metodo GET -Ruta $ruta -Token $caso.Tokens[$rol] }
            Escribir-Resultado "$rol en $($caso.Metodo) /$($caso.Ruta) (403, obtuvo $($r.Status))" ($r.Status -eq 403)
        }
    }
    $anexosFinales = Ejecutar-Sql "select count(*) from ANEXO_ACADEMICO where SolicitudAcademica_uuid in ($(Lista-Sql $solicitudes))"
    Escribir-Resultado "los intentos rechazados no dejaron anexos de mas (obtuvo $anexosFinales)" ($anexosFinales -eq "1")
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

    $sol = Lista-Sql $solicitudes
    $usu = Lista-Sql $usuarios
    $est = Lista-Sql $estudiantes
    $fun = Lista-Sql $funcionarios
    $cod = Lista-Sql $codigos
    $sentencias = @()
    if ($sol) {
        $sentencias += "delete from ANEXO_ACADEMICO where SolicitudAcademica_uuid in ($sol)"
        $sentencias += "delete from RESOLUCION_ACADEMICA where SolicitudAcademica_uuid in ($sol)"
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
    $carpetas = @($solicitudes | Where-Object { Test-Path (Join-Path $Global:CarpetaAnexos $_) }).Count
    Escribir-Resultado "no quedaron filas, usuarios ni carpetas de prueba (filas $restos, carpetas $carpetas)" (($restos -eq 0) -and ($carpetas -eq 0))
}
