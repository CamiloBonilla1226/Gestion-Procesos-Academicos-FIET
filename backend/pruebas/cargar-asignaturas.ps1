. "$PSScriptRoot\comun.ps1"

function Clave {
    param([string]$Texto)
    $d = $Texto.Normalize([System.Text.NormalizationForm]::FormD)
    $sb = New-Object System.Text.StringBuilder
    foreach ($c in $d.ToCharArray()) {
        if ([System.Globalization.CharUnicodeInfo]::GetUnicodeCategory($c) -ne [System.Globalization.UnicodeCategory]::NonSpacingMark) { [void]$sb.Append($c) }
    }
    return (($sb.ToString() -replace '\s+', ' ').Trim().ToLowerInvariant())
}

function Separar-Lista {
    param([string]$Lista)
    $partes = New-Object System.Collections.Generic.List[string]
    foreach ($p in ($Lista -split ',')) {
        $t = $p.Trim()
        if ($t -eq '') { continue }
        if ($t -match '^[IVX]+(\s+y\s+[IVX]+)?$' -and $partes.Count -gt 0) { $partes[$partes.Count - 1] = $partes[$partes.Count - 1] + ', ' + $t }
        else { $partes.Add($t) }
    }
    $nombres = @()
    foreach ($t in $partes) {
        $t = ($t -replace '\s*\(con laboratorios?\)', '').Trim()
        if ($t -match '^(.*?)\s+([IVX]+(?:\s*,\s*[IVX]+)*\s+y\s+[IVX]+)$') {
            $base = $Matches[1] -replace '^Electivas\b', 'Electiva'
            foreach ($n in ($Matches[2] -split '\s*,\s*|\s+y\s+')) { $nombres += "$base $n" }
        } else { $nombres += $t }
    }
    return $nombres
}

try {
    $lineas = Get-Content -Path "$PSScriptRoot\asignaturas-programas.txt" -Encoding UTF8
    $token = Iniciar-Sesion -Usuario "rootfiet" -Clave "rootfiet1234"
    $programas = Obtener-Programas -Token $token
    $plan = [ordered]@{}
    $programa = $null
    foreach ($linea in $lineas) {
        $l = $linea.Trim()
        if ($l -match '^\d+\.\s+(.+)$') {
            $nombrePrograma = $Matches[1]
            $programa = @($programas | Where-Object { (Clave $_.nombre) -eq (Clave $nombrePrograma) })[0]
            if ($null -eq $programa) { throw "no existe el programa $nombrePrograma en PROGRAMA_ACADEMICO" }
            continue
        }
        if ($l -match '^Semestres?\s+[0-9 y,]+:\s*(.+)$' -and $null -ne $programa) {
            foreach ($nombre in (Separar-Lista $Matches[1])) {
                $k = $programa.uuidProgramaAcademico + '|' + (Clave $nombre)
                if (-not $plan.Contains($k)) { $plan[$k] = [pscustomobject]@{ Programa = $programa.uuidProgramaAcademico; Nombre = $nombre } }
            }
        }
    }
    if ($plan.Count -eq 0) { throw "el archivo no trae asignaturas" }
    $existentes = @{}
    foreach ($p in $programas) {
        $r = Invoke-Api -Metodo GET -Ruta ("asignaturas/programa/" + $p.uuidProgramaAcademico) -Token $token
        if ($r.Status -ne 200) { throw "no se pudo leer las asignaturas de $($p.sigla) ($($r.Status))" }
        foreach ($a in @(Leer-Json $r)) { $existentes[$p.uuidProgramaAcademico + '|' + (Clave $a.nombreAsignatura)] = $true }
    }
    foreach ($k in $plan.Keys) {
        if ($existentes.ContainsKey($k)) { continue }
        $item = $plan[$k]
        $r = Invoke-Api -Metodo POST -Ruta "asignaturas" -Token $token -Cuerpo @{ nombreAsignatura = $item.Nombre; uuidProgramaAcademico = $item.Programa }
        if ($r.Status -ne 200) { throw "no se pudo crear $($item.Nombre) ($($r.Status))" }
        $existentes[$k] = $true
    }
    Write-Host "OK"
} catch {
    Write-Host ("FALLO " + ($_.Exception.Message -replace '\s+', ' '))
}
