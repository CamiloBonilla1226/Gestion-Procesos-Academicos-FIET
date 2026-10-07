$Global:BaseUrl = "http://localhost:8080/api/unicauca/fiet/consejo/"
$Global:CedulaCiudadania = "C$([char]0x00E9)dula de ciudadan$([char]0x00ED)a"
$Global:FuncionarioAcademico = "Funcionario Acad$([char]0x00E9)mico"

$Global:Perfiles = @{
    Funcionario          = @{
        TipoUsuario = "FUNCIONARIO"
        Rol         = @{ uuidRol = "48e3a5ed-709a-4dcb-9434-3bcb43ef9413"; nombre = "Funcionario"; descripcion = "Empleado de la FIET"; estado = $true }
        Tipo        = @{ uuidTipoUsuario = "79105584-1091-4a4e-ba8e-9cbdd1c85b91"; nombre = "Empleado FIET - Funcionario" }
    }
    Estudiante           = @{
        TipoUsuario = "ESTUDIANTE"
        Rol         = @{ uuidRol = "b77e970f-9c82-4c4b-9570-fe79840245a7"; nombre = "Estudiante"; descripcion = "Estudiante de la FIET"; estado = $true }
        Tipo        = @{ uuidTipoUsuario = "5725e5e5-aa04-494c-b347-be14f619fd97"; nombre = "Estudiante" }
    }
    FuncionarioAcademico = @{
        TipoUsuario = "FUNCIONARIOACADEMICO"
        Rol         = @{ uuidRol = "996f4a2d-e051-4367-a702-a3e6a0a91873"; nombre = $Global:FuncionarioAcademico; descripcion = "Tecnico Administrativo de Procesos Academicos de la FIET"; estado = $true }
        Tipo        = @{ uuidTipoUsuario = "79105584-1091-4a4e-ba8e-9cbdd1c85b91"; nombre = "Empleado FIET - Funcionario" }
    }
    Decano               = @{
        TipoUsuario = "DECANO"
        Rol         = @{ uuidRol = "1b08e82e-4617-4cf5-96b6-7ace7ed1bfbd"; nombre = "Decano"; descripcion = "Autoridad maxima de la decanatura de la FIET"; estado = $true }
        Tipo        = @{ uuidTipoUsuario = "9d4ddd15-925e-444e-a60e-4b0f0bdba16a"; nombre = "Maxima autoridad FIET - Decano" }
    }
}

function Nuevo-Sufijo {
    return (Get-Date -Format "yyyyMMddHHmmssfff") + (Get-Random -Minimum 100 -Maximum 999)
}

function Invoke-Api {
    param(
        [string]$Metodo,
        [string]$Ruta,
        [string]$Token,
        $Cuerpo
    )
    $cabeceras = @{}
    if ($Token) { $cabeceras["Authorization"] = "Bearer $Token" }
    $parametros = @{
        Uri             = $Global:BaseUrl + $Ruta
        Method          = $Metodo
        Headers         = $cabeceras
        UseBasicParsing = $true
    }
    if ($null -ne $Cuerpo) {
        $json = $Cuerpo | ConvertTo-Json -Depth 10
        $parametros["Body"] = [System.Text.Encoding]::UTF8.GetBytes($json)
        $parametros["ContentType"] = "application/json; charset=utf-8"
    }
    try {
        $respuesta = Invoke-WebRequest @parametros
        return [pscustomobject]@{ Status = [int]$respuesta.StatusCode; Body = $respuesta.Content }
    } catch {
        $respuestaError = $_.Exception.Response
        if ($null -eq $respuestaError) { throw }
        $contenido = ""
        try {
            $lector = New-Object System.IO.StreamReader($respuestaError.GetResponseStream())
            $contenido = $lector.ReadToEnd()
        } catch {}
        return [pscustomobject]@{ Status = [int]$respuestaError.StatusCode; Body = $contenido }
    }
}

function Iniciar-Sesion {
    param([string]$Usuario, [string]$Clave)
    $r = Invoke-Api -Metodo POST -Ruta "sesiones" -Cuerpo @{ username = $Usuario; password = $Clave }
    if ($r.Status -ne 200) { throw "Inicio de sesion fallido para $Usuario ($($r.Status)): $($r.Body)" }
    return ($r.Body | ConvertFrom-Json).token
}

function Crear-UsuarioPrueba {
    param([string]$Perfil, [string]$TokenAdmin, [string]$Prefijo = "prueba")
    $datos = $Global:Perfiles[$Perfil]
    $sufijo = Nuevo-Sufijo
    $username = "$Prefijo$sufijo"
    $clave = "Clave12345"
    $cuerpo = @{
        nombres           = "Prueba"
        apellidos         = "$Perfil $sufijo"
        estado            = $true
        tipoDocumento     = $Global:CedulaCiudadania
        numeroDocumento   = "9$sufijo"
        telefono          = "+573000000000"
        correoElectronico = "$username@unicauca.edu.co"
        username          = $username
        password          = $clave
        objTipoUsuario    = $datos.Tipo
        roles             = @($datos.Rol)
    }
    $r = Invoke-Api -Metodo POST -Ruta "usuarios?tipoUsuario=$($datos.TipoUsuario)" -Token $TokenAdmin -Cuerpo $cuerpo
    if ($r.Status -ne 200) { throw "No se pudo crear el usuario $Perfil ($($r.Status)): $($r.Body)" }
    return [pscustomobject]@{
        Username = $username
        Clave    = $clave
        Uuid     = ($r.Body | ConvertFrom-Json).uuidUsuario
        Token    = Iniciar-Sesion -Usuario $username -Clave $clave
    }
}

function Leer-Json {
    param($Respuesta)
    $texto = [System.Text.Encoding]::UTF8.GetString([System.Text.Encoding]::GetEncoding(28591).GetBytes($Respuesta.Body))
    return ($texto | ConvertFrom-Json)
}

function Texto {
    param([string]$Escapado)
    return [regex]::Unescape($Escapado)
}

function Obtener-CodigoError {
    param($Respuesta)
    try { return ($Respuesta.Body | ConvertFrom-Json).codigoError } catch { return $null }
}

function Nuevo-ArchivoXlsx {
    param([string]$Ruta, $Filas)
    Add-Type -AssemblyName System.IO.Compression
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $xmlFilas = New-Object System.Text.StringBuilder
    for ($i = 0; $i -lt $Filas.Count; $i++) {
        $n = $i + 1
        [void]$xmlFilas.Append("<row r=`"$n`">")
        $celdas = @($Filas[$i])
        for ($j = 0; $j -lt $celdas.Count; $j++) {
            $referencia = [string][char](65 + $j) + $n
            $valor = [System.Security.SecurityElement]::Escape([string]$celdas[$j])
            [void]$xmlFilas.Append("<c r=`"$referencia`" t=`"inlineStr`"><is><t xml:space=`"preserve`">$valor</t></is></c>")
        }
        [void]$xmlFilas.Append("</row>")
    }
    $cabecera = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
    $partes = [ordered]@{
        "[Content_Types].xml"        = $cabecera + '<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>'
        "_rels/.rels"                = $cabecera + '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>'
        "xl/workbook.xml"            = $cabecera + '<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="Estudiantes" sheetId="1" r:id="rId1"/></sheets></workbook>'
        "xl/_rels/workbook.xml.rels" = $cabecera + '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/></Relationships>'
        "xl/worksheets/sheet1.xml"   = $cabecera + "<worksheet xmlns=`"http://schemas.openxmlformats.org/spreadsheetml/2006/main`"><sheetData>$xmlFilas</sheetData></worksheet>"
    }
    if (Test-Path $Ruta) { Remove-Item $Ruta -Force }
    $utf8 = New-Object System.Text.UTF8Encoding($false)
    $zip = [System.IO.Compression.ZipFile]::Open($Ruta, [System.IO.Compression.ZipArchiveMode]::Create)
    try {
        foreach ($nombre in $partes.Keys) {
            $flujo = $zip.CreateEntry($nombre).Open()
            $bytes = $utf8.GetBytes($partes[$nombre])
            $flujo.Write($bytes, 0, $bytes.Length)
            $flujo.Close()
        }
    } finally {
        $zip.Dispose()
    }
}

function Subir-Archivo {
    param([string]$Ruta, [string]$Archivo, [string]$Token)
    $salida = [System.IO.Path]::GetTempFileName()
    $argumentos = @("-s", "-o", $salida, "-w", "%{http_code}", "-X", "POST", "-F", "file=@$Archivo")
    if ($Token) { $argumentos += @("-H", "Authorization: Bearer $Token") }
    $argumentos += ($Global:BaseUrl + $Ruta)
    $codigo = & curl.exe @argumentos
    $cuerpo = [System.IO.File]::ReadAllText($salida, [System.Text.Encoding]::UTF8)
    Remove-Item $salida -Force
    return [pscustomobject]@{ Status = [int]$codigo; Body = $cuerpo }
}

function Escribir-Resultado {
    param([string]$Descripcion, [bool]$Ok)
    if ($Ok) { Write-Host "PASS $Descripcion" } else { Write-Host "FAIL $Descripcion" }
}

function Consultar-Base {
    param([string]$Sql)
    Push-Location (Resolve-Path "$PSScriptRoot\..").Path
    try {
        $salida = & docker compose exec -T cfiet_database mysql -u root -pmysql --default-character-set=utf8mb4 cfiet -N -B -e $Sql 2>$null
    } finally {
        Pop-Location
    }
    return $salida
}

function Valores-Sql {
    param([string[]]$Valores)
    return (@($Valores | Where-Object { $_ } | Select-Object -Unique | ForEach-Object { "'" + $_.Replace("'", "''") + "'" }) -join ",")
}

function Foto-Base {
    $tablas = @("usuariosLivianos", "usuarios", "Usuario_has_Roles", "funcionarios", "ESTUDIANTE", "FUNCIONARIO_ACADEMICO", "ASIGNATURA", "ASIGNATURA_MATRICULADA", "SOLICITUD_ACADEMICA", "ANEXO_ACADEMICO")
    $conteos = @($tablas | ForEach-Object { "(select count(*) from $_)" }) + @(
        "(select count(*) from logs where uuidUsuario <> 'uuid-root-0001')",
        "(select md5(group_concat(concat(uuidTipoSolicitudAcademica, coalesce(FuncionarioAcademico_uuid, '-')) order by uuidTipoSolicitudAcademica)) from TIPO_SOLICITUD_ACADEMICA)")
    $valores = @((Consultar-Base ("select concat_ws(' ', " + ($conteos -join ", ") + ")")) -split " ")
    $nombres = $tablas + @("logs sin root", "responsables")
    return (@(for ($i = 0; $i -lt $nombres.Count; $i++) { "$($nombres[$i])=$($valores[$i])" }) -join " ")
}

function Inicio-Corrida {
    return [string](Consultar-Base "select now()")
}

function Limpiar-DatosPrueba {
    param([string]$Desde, [string[]]$Usernames, [string[]]$CodigosAsignatura)
    $nombres = Valores-Sql $Usernames
    $uuids = @()
    if ($nombres) {
        $uuids = @(Consultar-Base ("select u.uuidUsuario from usuarios u join usuariosLivianos l on l.uuidUsuario = u.uuidUsuario " +
                "where u.username in ($nombres) and l.fechaCreacion >= '$Desde'"))
    }
    $usu = Valores-Sql $uuids
    $cod = Valores-Sql $CodigosAsignatura
    $sentencias = @()
    if ($usu) {
        $sentencias += "delete from logs where uuidUsuario in ($usu)"
        $sentencias += "delete from ASIGNATURA_MATRICULADA where Estudiante_uuid in ($usu)"
        $sentencias += "delete from ESTUDIANTE where Usuario_uuid in ($usu)"
        $sentencias += "delete from FUNCIONARIO_ACADEMICO where Usuario_uuid in ($usu)"
        $sentencias += "delete from funcionarios where uuidUsuario in ($usu)"
        $sentencias += "delete from Usuario_has_Roles where uuidUsuario in ($usu)"
        $sentencias += "delete from usuarios where uuidUsuario in ($usu)"
        $sentencias += "delete from usuariosLivianos where uuidUsuario in ($usu)"
    }
    if ($cod) {
        $sentencias += "delete from ASIGNATURA where codigoAsignatura in ($cod) and uuidAsignatura not in (select Asignatura_uuid from ASIGNATURA_MATRICULADA)"
    }
    if ($sentencias.Count -gt 0) { Consultar-Base ("start transaction; " + ($sentencias -join "; ") + "; commit;") | Out-Null }
    return $uuids.Count
}
