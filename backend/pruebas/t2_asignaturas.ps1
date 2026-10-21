. "$PSScriptRoot\comun.ps1"

$tokenRoot = Iniciar-Sesion -Usuario "rootfiet" -Clave "rootfiet1234"
$inicio = Inicio-Corrida
$fotoInicial = Foto-Base
$usernames = @()
$codigos = @()
$excel = Join-Path ([System.IO.Path]::GetTempPath()) ("t2_asignaturas_" + (Nuevo-Sufijo) + ".xlsx")

function Codigos-DeRespuesta {
    param($Respuesta)
    if ($Respuesta.Status -ne 200) { return @() }
    $lista = $Respuesta.Body | ConvertFrom-Json
    return @(@($lista) | ForEach-Object { $_.codigoAsignatura })
}

function Consecutivo {
    param([string]$Codigo)
    return [int]($Codigo.Substring($Codigo.IndexOf("-") + 1))
}

function Total-Asignaturas {
    return [int](Consultar-Base "select count(*) from ASIGNATURA")
}

try {
    $estudiante = Crear-UsuarioPrueba -Perfil "Estudiante" -TokenAdmin $tokenRoot -Prefijo "est"
    $usernames += $estudiante.Username
    $funcAcad = Crear-UsuarioPrueba -Perfil "FuncionarioAcademico" -TokenAdmin $tokenRoot -Prefijo "facad"
    $usernames += $funcAcad.Username

    $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/programas" -Token $tokenRoot
    $programas = if ($r.Status -eq 200) { @(Leer-Json $r) } else { @() }
    $siglas = (@($programas | ForEach-Object { $_.sigla }) | Sort-Object) -join ","
    Escribir-Resultado "el catalogo trae los cinco programas con su sigla (obtuvo $($r.Status) y $siglas)" (($r.Status -eq 200) -and ($siglas -eq "PIAC,PIAI,PIET,PIS,TTM"))
    Escribir-Resultado "cada programa trae uuid, nombre y sigla" (@($programas | Where-Object { $_.uuidProgramaAcademico -and $_.nombre -and $_.sigla }).Count -eq 5)
    foreach ($actor in @(@{ Nombre = "Estudiante"; Token = $estudiante.Token }, @{ Nombre = "Funcionario Academico"; Token = $funcAcad.Token })) {
        $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/programas" -Token $actor.Token
        Escribir-Resultado "$($actor.Nombre) GET catalogos-academicos/programas (200, obtuvo $($r.Status))" ($r.Status -eq 200)
    }
    $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/programas"
    Escribir-Resultado "Sin sesion GET catalogos-academicos/programas (401, obtuvo $($r.Status))" ($r.Status -eq 401)

    $isi = @($programas | Where-Object { $_.sigla -eq "PIS" })[0]
    $tte = @($programas | Where-Object { $_.sigla -eq "TTM" })[0]
    $sufijo = Nuevo-Sufijo

    $r = Invoke-Api -Metodo POST -Ruta "asignaturas" -Token $tokenRoot -Cuerpo @{ nombreAsignatura = "  Asignatura   de prueba $sufijo "; uuidProgramaAcademico = $isi.uuidProgramaAcademico }
    Escribir-Resultado "root crea asignatura (200, obtuvo $($r.Status))" ($r.Status -eq 200)
    if ($r.Status -ne 200) { Write-Host $r.Body; exit 1 }
    $primera = Leer-Json $r
    $codigos += $primera.codigoAsignatura
    $uuid = $primera.uuidAsignatura
    Escribir-Resultado "el codigo se genera con la sigla y tres digitos minimo (obtuvo $($primera.codigoAsignatura))" ($primera.codigoAsignatura -match '^PIS-\d{3,}$')
    Escribir-Resultado "la respuesta trae el programa (uuid y nombre)" (($primera.uuidProgramaAcademico -eq $isi.uuidProgramaAcademico) -and ($primera.programaAcademico -eq $isi.nombre))
    Escribir-Resultado "el nombre se guarda sin espacios sobrantes (obtuvo '$($primera.nombreAsignatura)')" ($primera.nombreAsignatura -eq "Asignatura de prueba $sufijo")

    $segunda = Crear-AsignaturaPrueba -Token $tokenRoot -UuidPrograma $isi.uuidProgramaAcademico -Nombre "Segunda de prueba $sufijo"
    $codigos += $segunda.codigoAsignatura
    Escribir-Resultado "la siguiente asignatura del programa toma el consecutivo siguiente ($($primera.codigoAsignatura) -> $($segunda.codigoAsignatura))" ((Consecutivo $segunda.codigoAsignatura) -eq ((Consecutivo $primera.codigoAsignatura) + 1))

    $r = Invoke-Api -Metodo POST -Ruta "asignaturas" -Token $tokenRoot -Cuerpo @{ nombreAsignatura = "Asignatura de prueba $sufijo"; uuidProgramaAcademico = $tte.uuidProgramaAcademico }
    $otraTte = if ($r.Status -eq 200) { Leer-Json $r } else { $null }
    if ($otraTte) { $codigos += $otraTte.codigoAsignatura }
    Escribir-Resultado "el mismo nombre en otro programa se permite y usa su sigla (obtuvo $($r.Status) $($otraTte.codigoAsignatura))" (($r.Status -eq 200) -and ($otraTte.codigoAsignatura -match '^TTM-\d{3,}$'))

    $r = Invoke-Api -Metodo POST -Ruta "asignaturas" -Token $tokenRoot -Cuerpo @{ nombreAsignatura = " ASIGNATURA DE   PRUEBA $sufijo"; uuidProgramaAcademico = $isi.uuidProgramaAcademico }
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root crea con nombre repetido en el programa sin importar mayusculas ni espacios (500 codigoError 2, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "2"))

    $r = Invoke-Api -Metodo POST -Ruta "asignaturas" -Token $tokenRoot -Cuerpo @{ nombreAsignatura = ""; uuidProgramaAcademico = "" }
    Escribir-Resultado "root crea con campos vacios (400, obtuvo $($r.Status))" ($r.Status -eq 400)

    $r = Invoke-Api -Metodo POST -Ruta "asignaturas" -Token $tokenRoot -Cuerpo @{ nombreAsignatura = ("x" * 151); uuidProgramaAcademico = $isi.uuidProgramaAcademico }
    Escribir-Resultado "root crea con nombre de 151 caracteres (400, obtuvo $($r.Status))" ($r.Status -eq 400)

    $r = Invoke-Api -Metodo POST -Ruta "asignaturas" -Token $tokenRoot -Cuerpo @{ nombreAsignatura = "Sin programa $sufijo"; codigoAsignatura = "X$sufijo" }
    Escribir-Resultado "root crea sin programa (400, obtuvo $($r.Status))" ($r.Status -eq 400)

    $inexistente = [guid]::NewGuid().ToString()
    $r = Invoke-Api -Metodo POST -Ruta "asignaturas" -Token $tokenRoot -Cuerpo @{ nombreAsignatura = "Programa falso $sufijo"; uuidProgramaAcademico = $inexistente }
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root crea con programa inexistente (500 codigoError 3, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "3"))

    $r = Invoke-Api -Metodo GET -Ruta "asignaturas/paginado?pagina=0&tamanio=5" -Token $tokenRoot
    Escribir-Resultado "root lista paginado (200, obtuvo $($r.Status))" ($r.Status -eq 200)

    $r = Invoke-Api -Metodo GET -Ruta "asignaturas/filtro?texto=$($primera.codigoAsignatura)&pagina=0&tamanio=5" -Token $tokenRoot
    $total = if ($r.Status -eq 200) { (Leer-Json $r).totalElements } else { -1 }
    Escribir-Resultado "root filtra por codigo (200 y 1 resultado, obtuvo $($r.Status) y $total)" (($r.Status -eq 200) -and ($total -eq 1))

    $r = Invoke-Api -Metodo GET -Ruta "asignaturas/filtro?texto=$sufijo&pagina=0&tamanio=10" -Token $tokenRoot
    $total = if ($r.Status -eq 200) { (Leer-Json $r).totalElements } else { -1 }
    Escribir-Resultado "root filtra por texto en los dos programas (200 y 3 resultados, obtuvo $($r.Status) y $total)" (($r.Status -eq 200) -and ($total -eq 3))

    $r = Invoke-Api -Metodo GET -Ruta "asignaturas/filtro?texto=$sufijo&uuidProgramaAcademico=$($tte.uuidProgramaAcademico)&pagina=0&tamanio=10" -Token $tokenRoot
    $pagina = if ($r.Status -eq 200) { Leer-Json $r } else { $null }
    Escribir-Resultado "root filtra por texto y programa (200 y solo la de TTM, obtuvo $($r.Status) y $($pagina.totalElements))" (($r.Status -eq 200) -and ($pagina.totalElements -eq 1) -and (@($pagina.content)[0].codigoAsignatura -eq $otraTte.codigoAsignatura))

    $r = Invoke-Api -Metodo GET -Ruta "asignaturas/filtro?uuidProgramaAcademico=$inexistente&pagina=0&tamanio=5" -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root filtra por programa inexistente (500 codigoError 3, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "3"))

    $r = Invoke-Api -Metodo GET -Ruta "asignaturas/filtro?texto=noexiste$sufijo&pagina=0&tamanio=5" -Token $tokenRoot
    $total = if ($r.Status -eq 200) { (Leer-Json $r).totalElements } else { -1 }
    Escribir-Resultado "root filtra sin resultados (200 y pagina vacia, obtuvo $($r.Status) y $total)" (($r.Status -eq 200) -and ($total -eq 0))

    $r = Invoke-Api -Metodo GET -Ruta "asignaturas/paginado?pagina=-1&tamanio=5" -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root paginado con pagina negativa (500 codigoError 6, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6"))

    $r = Invoke-Api -Metodo GET -Ruta "asignaturas/programa/$($isi.uuidProgramaAcademico)" -Token $tokenRoot
    $delPrograma = if ($r.Status -eq 200) { @(Leer-Json $r) } else { @() }
    $numeros = @($delPrograma | ForEach-Object { Consecutivo $_.codigoAsignatura })
    $ordenados = ($numeros | Sort-Object) -join ","
    Escribir-Resultado "root lista las asignaturas del programa sin paginar (200 e incluye las dos nuevas, obtuvo $($r.Status))" (($r.Status -eq 200) -and (@($delPrograma | Where-Object { @($primera.codigoAsignatura, $segunda.codigoAsignatura) -contains $_.codigoAsignatura }).Count -eq 2))
    Escribir-Resultado "la lista del programa viene solo de ese programa y ordenada por consecutivo" ((@($delPrograma | Where-Object { $_.uuidProgramaAcademico -ne $isi.uuidProgramaAcademico }).Count -eq 0) -and (($numeros -join ",") -eq $ordenados))

    $r = Invoke-Api -Metodo GET -Ruta "asignaturas/programa/$inexistente" -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root lista un programa inexistente (500 codigoError 3, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "3"))

    $r = Invoke-Api -Metodo GET -Ruta "asignaturas/$uuid" -Token $tokenRoot
    Escribir-Resultado "root obtiene por uuid (200, obtuvo $($r.Status))" ($r.Status -eq 200)

    $r = Invoke-Api -Metodo GET -Ruta "asignaturas/$inexistente" -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root obtiene uuid inexistente (500 codigoError 3, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "3"))

    $editada = @{ nombreAsignatura = "Asignatura editada $sufijo"; codigoAsignatura = "X$sufijo"; uuidProgramaAcademico = $tte.uuidProgramaAcademico }
    $r = Invoke-Api -Metodo PUT -Ruta "asignaturas/$uuid" -Token $tokenRoot -Cuerpo $editada
    $cambiada = if ($r.Status -eq 200) { Leer-Json $r } else { $null }
    Escribir-Resultado "root edita el nombre (200 y nombre nuevo, obtuvo $($r.Status))" (($r.Status -eq 200) -and ($cambiada.nombreAsignatura -eq $editada.nombreAsignatura))
    Escribir-Resultado "editar no cambia el codigo ni el programa" (($cambiada.codigoAsignatura -eq $primera.codigoAsignatura) -and ($cambiada.uuidProgramaAcademico -eq $isi.uuidProgramaAcademico))

    $r = Invoke-Api -Metodo PUT -Ruta "asignaturas/$uuid" -Token $tokenRoot -Cuerpo @{ nombreAsignatura = "segunda DE prueba $sufijo" }
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root edita con el nombre de otra asignatura del programa (500 codigoError 2, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "2"))

    $r = Invoke-Api -Metodo PUT -Ruta "asignaturas/$uuid" -Token $tokenRoot -Cuerpo @{ nombreAsignatura = "" }
    Escribir-Resultado "root edita con nombre vacio (400, obtuvo $($r.Status))" ($r.Status -eq 400)

    $r = Invoke-Api -Metodo PUT -Ruta "asignaturas/$inexistente" -Token $tokenRoot -Cuerpo $editada
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root edita uuid inexistente (500 codigoError 3, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "3"))

    $encabezado = @("nombreAsignatura", "programaAcademico")
    Nuevo-ArchivoXlsx -Ruta $excel -Filas @($encabezado, @("Excel uno $sufijo", $isi.nombre.ToUpper()), @("Excel dos $sufijo", "  $($tte.nombre) "), @("Excel tres $sufijo", $isi.nombre))
    $r = Subir-Archivo -Ruta "asignaturas/cargar/archivo" -Archivo $excel -Token $tokenRoot
    $creadas = Codigos-DeRespuesta $r
    $codigos += $creadas
    Escribir-Resultado "root carga asignaturas por Excel (200 y 3 creadas, obtuvo $($r.Status) y $($creadas.Count))" (($r.Status -eq 200) -and ($creadas.Count -eq 3))
    Escribir-Resultado "el Excel genera los codigos en el orden del archivo ($($creadas -join ' '))" (($creadas.Count -eq 3) -and ($creadas[0] -match '^PIS-') -and ($creadas[1] -match '^TTM-') -and ((Consecutivo $creadas[2]) -eq ((Consecutivo $creadas[0]) + 1)))

    $antes = Total-Asignaturas
    $casos = @(
        @{ Nombre = "encabezado distinto"; Filas = @(@("nombre", "programaAcademico"), @("Excel x $sufijo", $isi.nombre)); Status = 500; Codigo = "6"; Texto = "Fila 1, columna A" },
        @{ Nombre = "programa desconocido"; Filas = @($encabezado, @("Excel x $sufijo", $isi.nombre), @("Excel y $sufijo", "Medicina")); Status = 500; Codigo = "6"; Texto = "Fila 3, columna B (programaAcademico)" },
        @{ Nombre = "nombre vacio"; Filas = @($encabezado, @("Excel x $sufijo", $isi.nombre), @("", $isi.nombre), @("Excel z $sufijo", $isi.nombre)); Status = 400; Codigo = $null; Texto = "Fila 3, columna A (nombreAsignatura)" },
        @{ Nombre = "programa vacio"; Filas = @($encabezado, @("Excel x $sufijo", "")); Status = 400; Codigo = $null; Texto = "Fila 2, columna B (programaAcademico)" },
        @{ Nombre = "nombre de 151 caracteres"; Filas = @($encabezado, @(("x" * 151), $isi.nombre)); Status = 400; Codigo = $null; Texto = "Fila 2, columna A (nombreAsignatura)" },
        @{ Nombre = "nombre repetido en el archivo"; Filas = @($encabezado, @("Excel x $sufijo", $isi.nombre), @("EXCEL  X $sufijo", $isi.nombre)); Status = 500; Codigo = "6"; Texto = "Fila 3, columna A (nombreAsignatura)" },
        @{ Nombre = "nombre que ya existe en el programa"; Filas = @($encabezado, @("Excel x $sufijo", $isi.nombre), @("excel uno $sufijo", $isi.nombre)); Status = 500; Codigo = "2"; Texto = "Fila 3, columna A (nombreAsignatura)" }
    )
    foreach ($caso in $casos) {
        Nuevo-ArchivoXlsx -Ruta $excel -Filas $caso.Filas
        $r = Subir-Archivo -Ruta "asignaturas/cargar/archivo" -Archivo $excel -Token $tokenRoot
        $codigos += Codigos-DeRespuesta $r
        $cod = Obtener-CodigoError $r
        $ok = ($r.Status -eq $caso.Status) -and ($cod -eq $caso.Codigo) -and ($r.Body.Contains($caso.Texto))
        Escribir-Resultado "Excel con $($caso.Nombre) ($($caso.Status) $($caso.Codigo) y '$($caso.Texto)', obtuvo $($r.Status) $cod)" $ok
    }
    Escribir-Resultado "las cargas con error no crearon ninguna asignatura (todo o nada)" ((Total-Asignaturas) -eq $antes)

    $lecturas = @("asignaturas/paginado?pagina=0&tamanio=5", "asignaturas/filtro?texto=$sufijo&pagina=0&tamanio=5", "asignaturas/programa/$($isi.uuidProgramaAcademico)", "asignaturas/$uuid")
    $escrituras = @(
        @{ Metodo = "POST"; Ruta = "asignaturas"; Cuerpo = @{ nombreAsignatura = "No debe crearse $sufijo"; uuidProgramaAcademico = $isi.uuidProgramaAcademico } },
        @{ Metodo = "PUT"; Ruta = "asignaturas/$uuid"; Cuerpo = @{ nombreAsignatura = "No debe editarse" } }
    )
    Nuevo-ArchivoXlsx -Ruta $excel -Filas @($encabezado, @("No debe crearse excel $sufijo", $isi.nombre))

    foreach ($ruta in $lecturas) {
        $r = Invoke-Api -Metodo GET -Ruta $ruta -Token $funcAcad.Token
        Escribir-Resultado "Funcionario Academico GET $ruta (200, obtuvo $($r.Status))" ($r.Status -eq 200)
    }
    foreach ($e in $escrituras) {
        $r = Invoke-Api -Metodo $e.Metodo -Ruta $e.Ruta -Token $funcAcad.Token -Cuerpo $e.Cuerpo
        Escribir-Resultado "Funcionario Academico $($e.Metodo) $($e.Ruta) (403, obtuvo $($r.Status))" ($r.Status -eq 403)
    }
    $r = Subir-Archivo -Ruta "asignaturas/cargar/archivo" -Archivo $excel -Token $funcAcad.Token
    Escribir-Resultado "Funcionario Academico POST asignaturas/cargar/archivo (403, obtuvo $($r.Status))" ($r.Status -eq 403)

    foreach ($ruta in $lecturas) {
        $r = Invoke-Api -Metodo GET -Ruta $ruta -Token $estudiante.Token
        Escribir-Resultado "Estudiante GET $ruta (403, obtuvo $($r.Status))" ($r.Status -eq 403)
    }
    foreach ($e in $escrituras) {
        $r = Invoke-Api -Metodo $e.Metodo -Ruta $e.Ruta -Token $estudiante.Token -Cuerpo $e.Cuerpo
        Escribir-Resultado "Estudiante $($e.Metodo) $($e.Ruta) (403, obtuvo $($r.Status))" ($r.Status -eq 403)
    }
    $r = Subir-Archivo -Ruta "asignaturas/cargar/archivo" -Archivo $excel -Token $estudiante.Token
    Escribir-Resultado "Estudiante POST asignaturas/cargar/archivo (403, obtuvo $($r.Status))" ($r.Status -eq 403)

    foreach ($ruta in $lecturas) {
        $r = Invoke-Api -Metodo GET -Ruta $ruta
        Escribir-Resultado "Sin sesion GET $ruta (401, obtuvo $($r.Status))" ($r.Status -eq 401)
    }
    foreach ($e in $escrituras) {
        $r = Invoke-Api -Metodo $e.Metodo -Ruta $e.Ruta -Cuerpo $e.Cuerpo
        Escribir-Resultado "Sin sesion $($e.Metodo) $($e.Ruta) (401, obtuvo $($r.Status))" ($r.Status -eq 401)
    }
    $r = Subir-Archivo -Ruta "asignaturas/cargar/archivo" -Archivo $excel
    Escribir-Resultado "Sin sesion POST asignaturas/cargar/archivo (401, obtuvo $($r.Status))" ($r.Status -eq 401)

    $r = Invoke-Api -Metodo DELETE -Ruta "asignaturas/$uuid" -Token $tokenRoot
    Escribir-Resultado "root DELETE asignaturas/{uuid} queda denegado (403, obtuvo $($r.Status))" ($r.Status -eq 403)

    $r = Invoke-Api -Metodo GET -Ruta "asignaturas/$uuid" -Token $tokenRoot
    $nombre = if ($r.Status -eq 200) { (Leer-Json $r).nombreAsignatura } else { "" }
    Escribir-Resultado "las escrituras denegadas no cambiaron la asignatura" ($nombre -eq $editada.nombreAsignatura)
}
finally {
    if (Test-Path $excel) { Remove-Item $excel -Force }
    $borrados = Limpiar-DatosPrueba -Desde $inicio -Usernames $usernames -CodigosAsignatura $codigos
    $fotoFinal = Foto-Base
    Escribir-Resultado "la base quedo como al inicio salvo los logs de rootfiet y los contadores de programa (usuarios borrados $borrados; $fotoFinal)" ($fotoFinal -eq $fotoInicial)
}
