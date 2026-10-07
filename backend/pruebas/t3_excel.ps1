. "$PSScriptRoot\comun.ps1"

$Encabezado = @("nombres", "apellidos", "tipoDocumento", "numeroDocumento", "telefono", "correoElectronico", "username", "password", "codigoEstudiantil", "programaAcademico", "semestre", "facultad", "codigoAsignatura", "nombreAsignatura", "grupo")
$Carpeta = Join-Path ([System.IO.Path]::GetTempPath()) ("t3_excel_" + (Nuevo-Sufijo))
New-Item -ItemType Directory -Path $Carpeta | Out-Null

function Datos-Estudiante {
    param([string]$Sufijo)
    $script:usernames += "xl$Sufijo"
    return @("Excel", "Prueba $Sufijo", $Global:CedulaCiudadania, "7$Sufijo", "3000000000", "xl$Sufijo@unicauca.edu.co", "xl$Sufijo", "Clave12345", "X$Sufijo", "Ingenieria de Sistemas", "4", "FIET")
}

function Nuevo-Excel {
    param([string]$Nombre, $Filas)
    $script:codigos += @($Filas | ForEach-Object { @($_)[12] })
    $ruta = Join-Path $Carpeta $Nombre
    Nuevo-ArchivoXlsx -Ruta $ruta -Filas (@(, $Encabezado) + $Filas)
    return $ruta
}

function Total-PorCodigo {
    param([string]$Codigo)
    $r = Invoke-Api -Metodo GET -Ruta "estudiantes/filtro?codigo=$Codigo&pagina=0&tamanio=5" -Token $tokenRoot
    if ($r.Status -ne 200) { return -1 }
    return ($r.Body | ConvertFrom-Json).totalElements
}

$tokenRoot = Iniciar-Sesion -Usuario "rootfiet" -Clave "rootfiet1234"
$inicio = Inicio-Corrida
$fotoInicial = Foto-Base
$usernames = @()
$codigos = @()

try {
    $estudianteRol = Crear-UsuarioPrueba -Perfil "Estudiante" -TokenAdmin $tokenRoot -Prefijo "est"
    $usernames += $estudianteRol.Username
    $funcAcad = Crear-UsuarioPrueba -Perfil "FuncionarioAcademico" -TokenAdmin $tokenRoot -Prefijo "facad"
    $usernames += $funcAcad.Username

    $s1 = Nuevo-Sufijo
    $s2 = Nuevo-Sufijo
    $e1 = Datos-Estudiante $s1
    $e2 = Datos-Estudiante $s2
    $valido = Nuevo-Excel "valido.xlsx" @(
        , ($e1 + @("XA$s1", "Materia Excel A $s1", "A"))
        , ($e1 + @("XB$s1", "Materia Excel B $s1", "B"))
        , ($e2 + @("XA$s1", "Materia Excel A $s1", "C"))
    )
    $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $valido -Token $tokenRoot
    $creados = @()
    if ($r.Status -eq 200) { $lista = $r.Body | ConvertFrom-Json; $creados = @($lista) }
    Escribir-Resultado "archivo valido (200 y 2 estudiantes, obtuvo $($r.Status) y $($creados.Count))" (($r.Status -eq 200) -and ($creados.Count -eq 2))
    if ($r.Status -ne 200) { Write-Host $r.Body }
    Escribir-Resultado "la respuesta de la carga no contiene password" ($r.Body -notmatch '"password"')

    $r = Invoke-Api -Metodo GET -Ruta "estudiantes/filtro?codigo=X$s1&pagina=0&tamanio=5" -Token $tokenRoot
    $primero = if ($r.Status -eq 200) { @(($r.Body | ConvertFrom-Json).content)[0] } else { $null }
    $materias1 = if ($primero) { @($primero.asignaturasMatriculadas).Count } else { -1 }
    Escribir-Resultado "el primer estudiante quedo con sus dos materias (obtuvo $materias1)" ($materias1 -eq 2)
    Escribir-Resultado "el segundo estudiante quedo creado (obtuvo $(Total-PorCodigo "X$s2"))" ((Total-PorCodigo "X$s2") -eq 1)

    $tokenExcel = Iniciar-Sesion -Usuario "xl$s1" -Clave "Clave12345"
    $r = Invoke-Api -Metodo GET -Ruta "estudiantes/mis-asignaturas" -Token $tokenExcel
    $n = -1
    if ($r.Status -eq 200) { $lista = $r.Body | ConvertFrom-Json; $n = @($lista).Count }
    Escribir-Resultado "el estudiante creado por Excel inicia sesion y ve sus materias (200 y 2, obtuvo $($r.Status) y $n)" (($r.Status -eq 200) -and ($n -eq 2))

    $s3 = Nuevo-Sufijo
    $s4 = Nuevo-Sufijo
    $s5 = Nuevo-Sufijo
    $malo = Datos-Estudiante $s4
    $malo[3] = ""
    $filaInvalida = Nuevo-Excel "fila_invalida.xlsx" @(
        , ((Datos-Estudiante $s3) + @("XC$s3", "Materia C", "A"))
        , ($malo + @("XC$s3", "Materia C", "B"))
        , ((Datos-Estudiante $s5) + @("XC$s3", "Materia C", "C"))
    )
    $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $filaInvalida -Token $tokenRoot
    Escribir-Resultado "fila invalida en el medio (400, obtuvo $($r.Status))" ($r.Status -eq 400)
    Escribir-Resultado "el error indica fila 3 y columna D" (($r.Body -match "Fila 3") -and ($r.Body -match "columna D"))
    Write-Host "  respuesta: $($r.Body)"
    Escribir-Resultado "no se guardo ningun estudiante del archivo con fila invalida" (((Total-PorCodigo "X$s3") -eq 0) -and ((Total-PorCodigo "X$s5") -eq 0))

    $csv = Join-Path $Carpeta "estudiantes.csv"
    Set-Content -Path $csv -Value (($Encabezado -join ",") + "`r`n" + (((Datos-Estudiante (Nuevo-Sufijo)) + @("XZ", "Z", "A")) -join ",")) -Encoding ASCII
    $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $csv -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "archivo que no es .xlsx (500 codigoError 6, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6"))

    $falso = Join-Path $Carpeta "falso.xlsx"
    Set-Content -Path $falso -Value "esto no es un excel" -Encoding ASCII
    $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $falso -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "archivo .xlsx con contenido que no es Excel (500 codigoError 6, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6"))

    $soloEncabezado = Nuevo-Excel "solo_encabezado.xlsx" @()
    $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $soloEncabezado -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "archivo sin filas de datos (500 codigoError 6, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6"))

    $s6 = Nuevo-Sufijo
    $otroEncabezado = Join-Path $Carpeta "otro_encabezado.xlsx"
    $encabezadoMalo = @($Encabezado)
    $encabezadoMalo[4] = "celular"
    Nuevo-ArchivoXlsx -Ruta $otroEncabezado -Filas @($encabezadoMalo, ((Datos-Estudiante $s6) + @("XE$s6", "Materia E", "A")))
    $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $otroEncabezado -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "encabezado distinto (500 codigoError 6 con fila 1 y columna E, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6") -and ($r.Body -match "Fila 1, columna E"))

    $s7 = Nuevo-Sufijo
    $base = Datos-Estudiante $s7
    $distinto = @($base)
    $distinto[10] = "9"
    $datosDistintos = Nuevo-Excel "datos_distintos.xlsx" @(
        , ($base + @("XF$s7", "Materia F", "A"))
        , ($distinto + @("XG$s7", "Materia G", "A"))
    )
    $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $datosDistintos -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "mismo documento con datos distintos (500 codigoError 6 con fila 3 y columna K, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6") -and ($r.Body -match "Fila 3, columna K"))
    Escribir-Resultado "no se guardo el estudiante con datos distintos" ((Total-PorCodigo "X$s7") -eq 0)

    $s8 = Nuevo-Sufijo
    $s9 = Nuevo-Sufijo
    $repetido = Datos-Estudiante $s9
    $repetido[8] = "X$s8"
    $codigoRepetido = Nuevo-Excel "codigo_repetido.xlsx" @(
        , ((Datos-Estudiante $s8) + @("XH$s8", "Materia H", "A"))
        , ($repetido + @("XH$s8", "Materia H", "B"))
    )
    $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $codigoRepetido -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "codigo estudiantil repetido en el archivo (500 codigoError 6 con fila 3 y columna I, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6") -and ($r.Body -match "Fila 3, columna I"))
    Escribir-Resultado "no se guardo ningun estudiante del archivo con codigo repetido" ((Total-PorCodigo "X$s8") -eq 0)

    $s10 = Nuevo-Sufijo
    $yaRegistrado = Datos-Estudiante $s10
    $yaRegistrado[8] = "X$s1"
    $s11 = Nuevo-Sufijo
    $codigoEnBase = Nuevo-Excel "codigo_en_base.xlsx" @(
        , ((Datos-Estudiante $s11) + @("XI$s11", "Materia I", "A"))
        , ($yaRegistrado + @("XI$s11", "Materia I", "B"))
    )
    $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $codigoEnBase -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "codigo estudiantil ya registrado en la base (500 codigoError 2, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "2"))
    Escribir-Resultado "no se guardo el otro estudiante del archivo" ((Total-PorCodigo "X$s11") -eq 0)

    $s12 = Nuevo-Sufijo
    $permisos = Nuevo-Excel "permisos.xlsx" @(, ((Datos-Estudiante $s12) + @("XJ$s12", "Materia J", "A")))
    $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $permisos -Token $estudianteRol.Token
    Escribir-Resultado "Estudiante sube archivo (403, obtuvo $($r.Status))" ($r.Status -eq 403)
    $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $permisos -Token $funcAcad.Token
    Escribir-Resultado "Funcionario Academico sube archivo (403, obtuvo $($r.Status))" ($r.Status -eq 403)
    $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $permisos
    Escribir-Resultado "Sin sesion sube archivo (401, obtuvo $($r.Status))" ($r.Status -eq 401)
    Escribir-Resultado "los intentos denegados no crearon al estudiante" ((Total-PorCodigo "X$s12") -eq 0)

    Remove-Item -Recurse -Force $Carpeta
}
finally {
    $borrados = Limpiar-DatosPrueba -Desde $inicio -Usernames $usernames -CodigosAsignatura $codigos
    $fotoFinal = Foto-Base
    Escribir-Resultado "la base quedo como al inicio salvo los logs de rootfiet (usuarios borrados $borrados; $fotoFinal)" ($fotoFinal -eq $fotoInicial)
}
