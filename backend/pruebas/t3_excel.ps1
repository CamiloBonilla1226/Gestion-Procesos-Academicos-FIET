. "$PSScriptRoot\comun.ps1"

$Encabezado = @("nombres", "apellidos", "tipoDocumento", "numeroDocumento", "telefono", "correoElectronico", "username", "password", "codigoEstudiantil", "programaAcademico", "semestre", "codigoAsignatura", "grupo")
$Carpeta = Join-Path ([System.IO.Path]::GetTempPath()) ("t3_excel_" + (Nuevo-Sufijo))
New-Item -ItemType Directory -Path $Carpeta | Out-Null

function Datos-Estudiante {
    param([string]$Sufijo, [string]$Programa = $script:isi.nombre)
    $script:usernames += "xl$Sufijo"
    return @("Excel", "Prueba $Sufijo", $Global:CedulaCiudadania, "7$Sufijo", "3000000000", "xl$Sufijo@unicauca.edu.co", "xl$Sufijo", "Clave12345", "X$Sufijo", $Programa, "4")
}

function Nuevo-Excel {
    param([string]$Nombre, $Filas)
    $ruta = Join-Path $Carpeta $Nombre
    Nuevo-ArchivoXlsx -Ruta $ruta -Filas (@(, $Encabezado) + $Filas)
    return $ruta
}

function Nueva-Asignatura {
    param($Programa, [string]$Nombre)
    $asignatura = Crear-AsignaturaPrueba -Token $script:tokenRoot -UuidPrograma $Programa.uuidProgramaAcademico -Nombre $Nombre
    $script:codigos += $asignatura.codigoAsignatura
    return $asignatura.codigoAsignatura
}

function Total-PorCodigo {
    param([string]$Codigo)
    $r = Invoke-Api -Metodo GET -Ruta "estudiantes/filtro?codigo=$Codigo&pagina=0&tamanio=5" -Token $tokenRoot
    if ($r.Status -ne 200) { return -1 }
    return ($r.Body | ConvertFrom-Json).totalElements
}

function Total-Asignaturas {
    return [int](Consultar-Base "select count(*) from ASIGNATURA")
}

$tokenRoot = Iniciar-Sesion -Usuario "rootfiet" -Clave "rootfiet1234"
$inicio = Inicio-Corrida
$fotoInicial = Foto-Base
$usernames = @()
$codigos = @()

try {
    $isi = Programa-PorSigla -Token $tokenRoot -Sigla "PIS"
    $iet = Programa-PorSigla -Token $tokenRoot -Sigla "PIET"
    $estudianteRol = Crear-UsuarioPrueba -Perfil "Estudiante" -TokenAdmin $tokenRoot -Prefijo "est"
    $usernames += $estudianteRol.Username
    $funcAcad = Crear-UsuarioPrueba -Perfil "FuncionarioAcademico" -TokenAdmin $tokenRoot -Prefijo "facad"
    $usernames += $funcAcad.Username

    $s0 = Nuevo-Sufijo
    $xa = Nueva-Asignatura -Programa $isi -Nombre "Materia Excel A $s0"
    $xb = Nueva-Asignatura -Programa $isi -Nombre "Materia Excel B $s0"
    $xo = Nueva-Asignatura -Programa $iet -Nombre "Materia Excel IET $s0"
    $asignaturasAntes = Total-Asignaturas

    $s1 = Nuevo-Sufijo
    $s2 = Nuevo-Sufijo
    $e1 = Datos-Estudiante $s1
    $e2 = Datos-Estudiante $s2 $isi.nombre.ToUpper()
    $valido = Nuevo-Excel "valido.xlsx" @(
        , ($e1 + @($xa, "A"))
        , ($e1 + @($xb.ToLower(), "B"))
        , ($e2 + @($xa, "C"))
    )
    $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $valido -Token $tokenRoot
    $creados = @()
    if ($r.Status -eq 200) { $lista = $r.Body | ConvertFrom-Json; $creados = @($lista) }
    Escribir-Resultado "archivo valido (200 y 2 estudiantes, obtuvo $($r.Status) y $($creados.Count))" (($r.Status -eq 200) -and ($creados.Count -eq 2))
    if ($r.Status -ne 200) { Write-Host $r.Body }
    Escribir-Resultado "la respuesta de la carga no contiene password" ($r.Body -notmatch '"password"')
    Escribir-Resultado "los estudiantes quedan en el programa del archivo, escrito sin importar mayusculas" (@($creados | Where-Object { $_.uuidProgramaAcademico -eq $isi.uuidProgramaAcademico -and $_.programaAcademico -eq $isi.nombre }).Count -eq 2)
    Escribir-Resultado "la carga de estudiantes no creo asignaturas" ((Total-Asignaturas) -eq $asignaturasAntes)

    $r = Invoke-Api -Metodo GET -Ruta "estudiantes/filtro?codigo=X$s1&pagina=0&tamanio=5" -Token $tokenRoot
    $primero = if ($r.Status -eq 200) { @((Leer-Json $r).content)[0] } else { $null }
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
        , ((Datos-Estudiante $s3) + @($xa, "A"))
        , ($malo + @($xa, "B"))
        , ((Datos-Estudiante $s5) + @($xa, "C"))
    )
    $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $filaInvalida -Token $tokenRoot
    Escribir-Resultado "fila invalida en el medio (400, obtuvo $($r.Status))" ($r.Status -eq 400)
    Escribir-Resultado "el error indica fila 3 y columna D" (($r.Body -match "Fila 3") -and ($r.Body -match "columna D"))
    Escribir-Resultado "no se guardo ningun estudiante del archivo con fila invalida" (((Total-PorCodigo "X$s3") -eq 0) -and ((Total-PorCodigo "X$s5") -eq 0))

    $casos = @(
        @{ Nombre = "programa desconocido"; Filas = @(, ((Datos-Estudiante ($t = Nuevo-Sufijo)) + @($xa, "A"))) + @(, ((Datos-Estudiante (Nuevo-Sufijo) "Medicina") + @($xa, "A"))); Status = 500; Codigo = "6"; Texto = "Fila 3, columna J (programaAcademico)"; Sufijo = $t },
        @{ Nombre = "programa vacio"; Filas = @(, ((Datos-Estudiante ($t = Nuevo-Sufijo) "") + @($xa, "A"))); Status = 400; Codigo = $null; Texto = "Fila 2, columna J (programaAcademico)"; Sufijo = $t },
        @{ Nombre = "asignatura inexistente"; Filas = @(, ((Datos-Estudiante ($t = Nuevo-Sufijo)) + @($xa, "A"))) + @(, ((Datos-Estudiante (Nuevo-Sufijo)) + @("NOEXISTE-$s0", "A"))); Status = 500; Codigo = "6"; Texto = "Fila 3, columna L (codigoAsignatura)"; Sufijo = $t },
        @{ Nombre = "asignatura de otro programa"; Filas = @(, ((Datos-Estudiante ($t = Nuevo-Sufijo)) + @($xo, "A"))); Status = 500; Codigo = "6"; Texto = "Fila 2, columna L (codigoAsignatura)"; Sufijo = $t },
        @{ Nombre = "asignatura vacia"; Filas = @(, ((Datos-Estudiante ($t = Nuevo-Sufijo)) + @("", "A"))); Status = 400; Codigo = $null; Texto = "columna L (codigoAsignatura)"; Sufijo = $t }
    )
    foreach ($caso in $casos) {
        $ruta = Nuevo-Excel "caso.xlsx" $caso.Filas
        $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $ruta -Token $tokenRoot
        $cod = Obtener-CodigoError $r
        $ok = ($r.Status -eq $caso.Status) -and ($cod -eq $caso.Codigo) -and ($r.Body.Contains($caso.Texto))
        Escribir-Resultado "Excel con $($caso.Nombre) ($($caso.Status) $($caso.Codigo) y '$($caso.Texto)', obtuvo $($r.Status) $cod)" $ok
        Escribir-Resultado "no se guardo ningun estudiante del archivo con $($caso.Nombre)" ((Total-PorCodigo "X$($caso.Sufijo)") -eq 0)
    }

    $s6 = Nuevo-Sufijo
    $otroPrograma = Datos-Estudiante $s6
    $otroPrograma[9] = $iet.nombre
    $programaDistinto = Nuevo-Excel "programa_distinto.xlsx" @(
        , ((Datos-Estudiante $s6) + @($xa, "A"))
        , ($otroPrograma + @($xo, "A"))
    )
    $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $programaDistinto -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "filas del mismo estudiante con otro programa (500 codigoError 6 con fila 3 y columna J, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6") -and ($r.Body -match "Fila 3, columna J"))
    Escribir-Resultado "no se guardo el estudiante con programas distintos" ((Total-PorCodigo "X$s6") -eq 0)

    $csv = Join-Path $Carpeta "estudiantes.csv"
    Set-Content -Path $csv -Value (($Encabezado -join ",") + "`r`n" + (((Datos-Estudiante (Nuevo-Sufijo)) + @($xa, "A")) -join ",")) -Encoding ASCII
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

    $s7 = Nuevo-Sufijo
    $otroEncabezado = Join-Path $Carpeta "otro_encabezado.xlsx"
    $encabezadoMalo = @($Encabezado)
    $encabezadoMalo[4] = "celular"
    Nuevo-ArchivoXlsx -Ruta $otroEncabezado -Filas @($encabezadoMalo, ((Datos-Estudiante $s7) + @($xa, "A")))
    $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $otroEncabezado -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "encabezado distinto (500 codigoError 6 con fila 1 y columna E, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6") -and ($r.Body -match "Fila 1, columna E"))

    $encabezadoViejo = Join-Path $Carpeta "encabezado_viejo.xlsx"
    $viejo = @($Encabezado[0..11]) + @("nombreAsignatura", "grupo")
    Nuevo-ArchivoXlsx -Ruta $encabezadoViejo -Filas @($viejo, ((Datos-Estudiante (Nuevo-Sufijo)) + @($xa, "Materia", "A")))
    $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $encabezadoViejo -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "plantilla anterior con nombreAsignatura (500 codigoError 6 con fila 1 y columna M, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6") -and ($r.Body -match "Fila 1, columna M"))

    $encabezadoFacultad = Join-Path $Carpeta "encabezado_facultad.xlsx"
    $conFacultad = @($Encabezado[0..10]) + @("facultad", "codigoAsignatura", "grupo")
    Nuevo-ArchivoXlsx -Ruta $encabezadoFacultad -Filas @($conFacultad, ((Datos-Estudiante (Nuevo-Sufijo)) + @("FIET", $xa, "A")))
    $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $encabezadoFacultad -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "plantilla anterior con facultad (500 codigoError 6 con fila 1 y columna L, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6") -and ($r.Body -match "Fila 1, columna L"))

    $s8 = Nuevo-Sufijo
    $base = Datos-Estudiante $s8
    $distinto = @($base)
    $distinto[10] = "9"
    $datosDistintos = Nuevo-Excel "datos_distintos.xlsx" @(
        , ($base + @($xa, "A"))
        , ($distinto + @($xb, "A"))
    )
    $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $datosDistintos -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "mismo documento con datos distintos (500 codigoError 6 con fila 3 y columna K, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6") -and ($r.Body -match "Fila 3, columna K"))
    Escribir-Resultado "no se guardo el estudiante con datos distintos" ((Total-PorCodigo "X$s8") -eq 0)

    $s9 = Nuevo-Sufijo
    $s10 = Nuevo-Sufijo
    $repetido = Datos-Estudiante $s10
    $repetido[8] = "X$s9"
    $codigoRepetido = Nuevo-Excel "codigo_repetido.xlsx" @(
        , ((Datos-Estudiante $s9) + @($xa, "A"))
        , ($repetido + @($xa, "B"))
    )
    $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $codigoRepetido -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "codigo estudiantil repetido en el archivo (500 codigoError 6 con fila 3 y columna I, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6") -and ($r.Body -match "Fila 3, columna I"))
    Escribir-Resultado "no se guardo ningun estudiante del archivo con codigo repetido" ((Total-PorCodigo "X$s9") -eq 0)

    $s11 = Nuevo-Sufijo
    $yaRegistrado = Datos-Estudiante $s11
    $yaRegistrado[8] = "X$s1"
    $s12 = Nuevo-Sufijo
    $codigoEnBase = Nuevo-Excel "codigo_en_base.xlsx" @(
        , ((Datos-Estudiante $s12) + @($xa, "A"))
        , ($yaRegistrado + @($xa, "B"))
    )
    $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $codigoEnBase -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "codigo estudiantil ya registrado en la base (500 codigoError 2, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "2"))
    Escribir-Resultado "no se guardo el otro estudiante del archivo" ((Total-PorCodigo "X$s12") -eq 0)
    Escribir-Resultado "ninguna carga creo asignaturas" ((Total-Asignaturas) -eq $asignaturasAntes)

    $s13 = Nuevo-Sufijo
    $permisos = Nuevo-Excel "permisos.xlsx" @(, ((Datos-Estudiante $s13) + @($xa, "A")))
    $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $permisos -Token $estudianteRol.Token
    Escribir-Resultado "Estudiante sube archivo (403, obtuvo $($r.Status))" ($r.Status -eq 403)
    $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $permisos -Token $funcAcad.Token
    Escribir-Resultado "Funcionario Academico sube archivo (403, obtuvo $($r.Status))" ($r.Status -eq 403)
    $r = Subir-Archivo -Ruta "estudiantes/cargar/archivo" -Archivo $permisos
    Escribir-Resultado "Sin sesion sube archivo (401, obtuvo $($r.Status))" ($r.Status -eq 401)
    Escribir-Resultado "los intentos denegados no crearon al estudiante" ((Total-PorCodigo "X$s13") -eq 0)
}
finally {
    if (Test-Path $Carpeta) { Remove-Item -Recurse -Force $Carpeta }
    $borrados = Limpiar-DatosPrueba -Desde $inicio -Usernames $usernames -CodigosAsignatura $codigos
    $fotoFinal = Foto-Base
    Escribir-Resultado "la base quedo como al inicio salvo los logs de rootfiet y los contadores de programa (usuarios borrados $borrados; $fotoFinal)" ($fotoFinal -eq $fotoInicial)
}
