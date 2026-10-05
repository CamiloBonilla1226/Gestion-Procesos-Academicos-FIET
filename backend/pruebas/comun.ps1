$Global:BaseUrl = "http://localhost:8080/api/unicauca/fiet/consejo/"
$Global:CedulaCiudadania = "C$([char]0x00E9)dula de ciudadan$([char]0x00ED)a"

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

function Escribir-Resultado {
    param([string]$Descripcion, [bool]$Ok)
    if ($Ok) { Write-Host "PASS $Descripcion" } else { Write-Host "FAIL $Descripcion" }
}
