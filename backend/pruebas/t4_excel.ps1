. "$PSScriptRoot\comun.ps1"

$Encabezado = @("nombres", "apellidos", "tipoDocumento", "numeroDocumento", "telefono", "correoElectronico", "username", "password", "dependencia")
$Carpeta = Join-Path ([System.IO.Path]::GetTempPath()) ("t4_excel_" + (Nuevo-Sufijo))
New-Item -ItemType Directory -Path $Carpeta | Out-Null
$Ruta = "funcionarios-academicos/cargar/archivo"

function Fila-Funcionario {
    param([string]$Sufijo, [string]$Dependencia)
    return @("Excel", "Funcionario $Sufijo", $Global:CedulaCiudadania, "5$Sufijo", "3000000000", "xf$Sufijo@unicauca.edu.co", "xf$Sufijo", "Clave12345", $Dependencia)
}

function Nuevo-Excel {
    param([string]$Nombre, $Filas)
    $ruta = Join-Path $Carpeta $Nombre
    Nuevo-ArchivoXlsx -Ruta $ruta -Filas (@(, $Encabezado) + $Filas)
    return $ruta
}

function Total-PorDependencia {
    param([string]$Dependencia)
    $r = Invoke-Api -Metodo GET -Ruta "funcionarios-academicos/filtro?dependencia=$Dependencia&pagina=0&tamanio=10" -Token $tokenRoot
    if ($r.Status -ne 200) { return -1 }
    return ($r.Body | ConvertFrom-Json).totalElements
}

$tokenRoot = Iniciar-Sesion -Usuario "rootfiet" -Clave "rootfiet1234"
$estudiante = Crear-UsuarioPrueba -Perfil "Estudiante" -TokenAdmin $tokenRoot -Prefijo "est"
$funcAcad = Crear-UsuarioPrueba -Perfil "FuncionarioAcademico" -TokenAdmin $tokenRoot -Prefijo "facad"

$lote = Nuevo-Sufijo
$depValida = "DepXL $lote"
$s1 = Nuevo-Sufijo
$s2 = Nuevo-Sufijo
$valido = Nuevo-Excel "valido.xlsx" @(
    , (Fila-Funcionario $s1 $depValida)
    , (Fila-Funcionario $s2 $depValida)
)
$r = Subir-Archivo -Ruta $Ruta -Archivo $valido -Token $tokenRoot
$creados = @()
if ($r.Status -eq 200) { $lista = $r.Body | ConvertFrom-Json; $creados = @($lista) }
Escribir-Resultado "archivo valido (200 y 2 funcionarios, obtuvo $($r.Status) y $($creados.Count))" (($r.Status -eq 200) -and ($creados.Count -eq 2))
if ($r.Status -ne 200) { Write-Host $r.Body }
Escribir-Resultado "la respuesta de la carga no contiene password" ($r.Body -notmatch '"password"')
Escribir-Resultado "los dos funcionarios quedaron en la base (obtuvo $(Total-PorDependencia $depValida))" ((Total-PorDependencia $depValida) -eq 2)
foreach ($s in @($s1, $s2)) {
    $login = Invoke-Api -Metodo POST -Ruta "sesiones" -Cuerpo @{ username = "xf$s"; password = "Clave12345" }
    Escribir-Resultado "xf$s creado por Excel inicia sesion (200, obtuvo $($login.Status))" ($login.Status -eq 200)
}

$depInvalida = "DepXL inv $lote"
$malo = Fila-Funcionario (Nuevo-Sufijo) $depInvalida
$malo[5] = "no-es-un-correo"
$filaInvalida = Nuevo-Excel "fila_invalida.xlsx" @(
    , (Fila-Funcionario (Nuevo-Sufijo) $depInvalida)
    , $malo
    , (Fila-Funcionario (Nuevo-Sufijo) $depInvalida)
)
$r = Subir-Archivo -Ruta $Ruta -Archivo $filaInvalida -Token $tokenRoot
Escribir-Resultado "fila invalida en el medio (400, obtuvo $($r.Status))" ($r.Status -eq 400)
Escribir-Resultado "el error indica fila 3 y columna F" (($r.Body -match "Fila 3") -and ($r.Body -match "columna F"))
Write-Host "  respuesta: $($r.Body)"
Escribir-Resultado "no se guardo ningun funcionario del archivo con fila invalida" ((Total-PorDependencia $depInvalida) -eq 0)

$csv = Join-Path $Carpeta "funcionarios.csv"
Set-Content -Path $csv -Value (($Encabezado -join ",") + "`r`n" + ((Fila-Funcionario (Nuevo-Sufijo) "Dep") -join ",")) -Encoding ASCII
$r = Subir-Archivo -Ruta $Ruta -Archivo $csv -Token $tokenRoot
$cod = Obtener-CodigoError $r
Escribir-Resultado "archivo que no es .xlsx (500 codigoError 6, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6"))

$soloEncabezado = Nuevo-Excel "solo_encabezado.xlsx" @()
$r = Subir-Archivo -Ruta $Ruta -Archivo $soloEncabezado -Token $tokenRoot
$cod = Obtener-CodigoError $r
Escribir-Resultado "archivo sin filas de datos (500 codigoError 6, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6"))

$otroEncabezado = Join-Path $Carpeta "otro_encabezado.xlsx"
$encabezadoMalo = @($Encabezado)
$encabezadoMalo[8] = "oficina"
Nuevo-ArchivoXlsx -Ruta $otroEncabezado -Filas @($encabezadoMalo, (Fila-Funcionario (Nuevo-Sufijo) "Dep"))
$r = Subir-Archivo -Ruta $Ruta -Archivo $otroEncabezado -Token $tokenRoot
$cod = Obtener-CodigoError $r
Escribir-Resultado "encabezado distinto (500 codigoError 6 con fila 1 y columna I, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6") -and ($r.Body -match "Fila 1, columna I"))

$casos = @(
    @{ Indice = 3; Columna = "D"; Descripcion = "documento" },
    @{ Indice = 5; Columna = "F"; Descripcion = "correo" },
    @{ Indice = 6; Columna = "G"; Descripcion = "username" }
)
foreach ($c in $casos) {
    $dep = "DepXL dup $($c.Descripcion) $lote"
    $primera = Fila-Funcionario (Nuevo-Sufijo) $dep
    $segunda = Fila-Funcionario (Nuevo-Sufijo) $dep
    $segunda[$c.Indice] = $primera[$c.Indice].ToUpper()
    $archivo = Nuevo-Excel "dup_$($c.Descripcion).xlsx" @($primera, $segunda)
    $r = Subir-Archivo -Ruta $Ruta -Archivo $archivo -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    $ok = ($r.Status -eq 500) -and ($cod -eq "6") -and ($r.Body -match "Fila 3, columna $($c.Columna)") -and ($r.Body -match "fila 2")
    Escribir-Resultado "$($c.Descripcion) repetido dentro del archivo (500 codigoError 6 con filas 3 y 2, obtuvo $($r.Status) codigoError $cod)" $ok
    Escribir-Resultado "no se guardo ningun funcionario del archivo con $($c.Descripcion) repetido" ((Total-PorDependencia $dep) -eq 0)
}

$depBase = "DepXL base $lote"
$yaRegistrado = Fila-Funcionario (Nuevo-Sufijo) $depBase
$yaRegistrado[6] = "xf$s1"
$enBase = Nuevo-Excel "username_en_base.xlsx" @(
    , (Fila-Funcionario (Nuevo-Sufijo) $depBase)
    , $yaRegistrado
)
$r = Subir-Archivo -Ruta $Ruta -Archivo $enBase -Token $tokenRoot
$cod = Obtener-CodigoError $r
Escribir-Resultado "username ya registrado en la base (500 codigoError 2, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "2"))
Escribir-Resultado "no se guardo el otro funcionario del archivo" ((Total-PorDependencia $depBase) -eq 0)

$depPermisos = "DepXL permisos $lote"
$permisos = Nuevo-Excel "permisos.xlsx" @(, (Fila-Funcionario (Nuevo-Sufijo) $depPermisos))
$r = Subir-Archivo -Ruta $Ruta -Archivo $permisos -Token $estudiante.Token
Escribir-Resultado "Estudiante sube archivo (403, obtuvo $($r.Status))" ($r.Status -eq 403)
$r = Subir-Archivo -Ruta $Ruta -Archivo $permisos -Token $funcAcad.Token
Escribir-Resultado "Funcionario Academico sube archivo (403, obtuvo $($r.Status))" ($r.Status -eq 403)
$r = Subir-Archivo -Ruta $Ruta -Archivo $permisos
Escribir-Resultado "Sin sesion sube archivo (401, obtuvo $($r.Status))" ($r.Status -eq 401)
Escribir-Resultado "los intentos denegados no crearon funcionarios" ((Total-PorDependencia $depPermisos) -eq 0)

Remove-Item -Recurse -Force $Carpeta
