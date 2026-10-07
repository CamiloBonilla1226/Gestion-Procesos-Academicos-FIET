. "$PSScriptRoot\comun.ps1"

$Global:Scripts = @(
    "t1_lectura_usuarios",
    "t1_preauthorize",
    "t2_asignaturas",
    "t3_estudiantes",
    "t3_excel",
    "t4_funcionarios_academicos",
    "t4_excel",
    "t5_catalogos",
    "t5_asignacion",
    "t5_solicitudes",
    "t6_cancelacion_matricula",
    "t7_cancelacion_asignatura",
    "t8_examen_supletorio",
    "matriz-permisos"
)

function Probar-Contenedor {
    try {
        $r = Invoke-Api -Metodo POST -Ruta "sesiones" -Cuerpo @{ username = "rootfiet"; password = "rootfiet1234" }
    } catch {
        return "el backend no responde en $($Global:BaseUrl) ($($_.Exception.Message))"
    }
    if ($r.Status -ne 200) { return "el backend responde $($r.Status) al iniciar sesion con rootfiet" }
    if ((Consultar-Base "select 1") -ne "1") { return "la base cfiet_database no responde por docker compose exec" }
    return $null
}

function Foto-Completa {
    $foto = [ordered]@{}
    $tablas = @(Consultar-Base "select table_name from information_schema.tables where table_schema = 'cfiet' and table_type = 'BASE TABLE' and table_name <> 'logs' order by table_name")
    if ($tablas.Count -eq 0) { return $null }
    $sql = @($tablas | ForEach-Object { "select '$_', count(*) from ``$_``" }) -join " union all "
    $filas = @(Consultar-Base $sql)
    if ($filas.Count -ne $tablas.Count) { return $null }
    foreach ($fila in $filas) {
        $partes = $fila -split "`t"
        $foto["tabla $($partes[0])"] = $partes[1]
    }
    foreach ($fila in @(Consultar-Base "select uuidTipoSolicitudAcademica, coalesce(FuncionarioAcademico_uuid, 'ninguno') from TIPO_SOLICITUD_ACADEMICA")) {
        $partes = $fila -split "`t"
        $foto["responsable del tipo $($partes[0])"] = $partes[1]
    }
    $uploads = Join-Path (Resolve-Path "$PSScriptRoot\..").Path "uploads"
    $foto["uploads"] = (@(Get-ChildItem $uploads -Recurse -Force | ForEach-Object { $_.FullName.Substring($uploads.Length + 1) } | Sort-Object) -join ", ")
    return $foto
}

function Diferencias-Foto {
    param($Antes, $Despues)
    if ($null -eq $Antes -or $null -eq $Despues) { return @("no se pudo tomar la foto de la base") }
    $claves = @(@($Antes.Keys) + @($Despues.Keys) | Select-Object -Unique)
    return @($claves | Where-Object { $Antes[$_] -cne $Despues[$_] } | ForEach-Object { "$_ antes [$($Antes[$_])] despues [$($Despues[$_])]" })
}

function Ejecutar-Script {
    param([string]$Nombre)
    $ruta = Join-Path $PSScriptRoot "$Nombre.ps1"
    if (-not (Test-Path $ruta)) {
        return [pscustomobject]@{ Script = $Nombre; Pass = 0; Fail = 0; Difs = 0; Estado = "ERROR"; Segundos = 0; Detalle = @("no existe $ruta") }
    }
    $antes = Foto-Completa
    $reloj = [System.Diagnostics.Stopwatch]::StartNew()
    $salida = @(& powershell -NoProfile -ExecutionPolicy Bypass -File $ruta 2>&1 | ForEach-Object { "$_" })
    $codigo = $LASTEXITCODE
    $reloj.Stop()
    $diferencias = @(Diferencias-Foto $antes (Foto-Completa))
    $pasan = @($salida | Where-Object { $_ -like "PASS *" }).Count
    $fallas = @($salida | Where-Object { $_ -like "FAIL *" })
    $estado = "OK"
    $detalle = @($fallas) + @($diferencias | ForEach-Object { "FAIL limpieza: $_" })
    if (($fallas.Count + $diferencias.Count) -gt 0) { $estado = "FAIL" }
    if (($pasan + $fallas.Count) -eq 0 -or ($codigo -ne 0 -and $fallas.Count -eq 0)) {
        $estado = "ERROR"
        $detalle += "codigo de salida $codigo; ultimas lineas:"
        $detalle += @($salida | Select-Object -Last 5)
    }
    return [pscustomobject]@{ Script = $Nombre; Pass = $pasan; Fail = $fallas.Count + $diferencias.Count; Difs = $diferencias.Count; Estado = $estado; Segundos = [int]$reloj.Elapsed.TotalSeconds; Detalle = $detalle }
}

$problema = Probar-Contenedor
if ($problema) {
    Write-Host "No se ejecuta ninguna prueba: $problema."
    Write-Host "Levanta el contenedor desde backend con docker compose up --build -d y vuelve a intentar."
    exit 2
}

$sueltos = @(Get-ChildItem $PSScriptRoot -Filter "t*_*.ps1" | ForEach-Object { $_.BaseName } | Where-Object { $Global:Scripts -notcontains $_ })
$resultados = @()
foreach ($nombre in $Global:Scripts) {
    Write-Host "Ejecutando $nombre ..."
    $resultado = Ejecutar-Script $nombre
    $resultados += $resultado
    Write-Host ("  {0}: PASS {1}, FAIL {2}, diferencias {3}, {4} s" -f $resultado.Estado, $resultado.Pass, $resultado.Fail, $resultado.Difs, $resultado.Segundos)
    foreach ($linea in $resultado.Detalle) { Write-Host "    $linea" }
}

Write-Host ""
Write-Host ("{0,-30} {1,6} {2,6} {3,6} {4,-6} {5,9}" -f "Script", "PASS", "FAIL", "Difs", "Estado", "Segundos")
Write-Host ("-" * 68)
foreach ($r in $resultados) { Write-Host ("{0,-30} {1,6} {2,6} {3,6} {4,-6} {5,9}" -f $r.Script, $r.Pass, $r.Fail, $r.Difs, $r.Estado, $r.Segundos) }
Write-Host ("-" * 68)
$totalPass = ($resultados | Measure-Object -Property Pass -Sum).Sum
$totalFail = ($resultados | Measure-Object -Property Fail -Sum).Sum
$totalDifs = ($resultados | Measure-Object -Property Difs -Sum).Sum
$errores = @($resultados | Where-Object { $_.Estado -eq "ERROR" }).Count
$totalSegundos = ($resultados | Measure-Object -Property Segundos -Sum).Sum
Write-Host ("{0,-30} {1,6} {2,6} {3,6} {4,-6} {5,9}" -f "TOTAL", $totalPass, $totalFail, $totalDifs, "", $totalSegundos)
foreach ($s in $sueltos) { Write-Host "Aviso: $s.ps1 no esta en la lista de todo.ps1 y no se ejecuto" }

if ($totalFail -gt 0 -or $errores -gt 0) {
    Write-Host "Resultado: $totalFail FAIL ($totalDifs de limpieza) y $errores scripts que no pudieron ejecutarse"
    exit 1
}
Write-Host "Resultado: todo en PASS"
exit 0
