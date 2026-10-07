. "$PSScriptRoot\comun.ps1"

$tokenRoot = Iniciar-Sesion -Usuario "rootfiet" -Clave "rootfiet1234"
$inicio = Inicio-Corrida
$fotoInicial = Foto-Base
$usernames = @()
$codigos = @()

try {
    $estudiante = Crear-UsuarioPrueba -Perfil "Estudiante" -TokenAdmin $tokenRoot -Prefijo "est"
    $usernames += $estudiante.Username
    $funcAcad = Crear-UsuarioPrueba -Perfil "FuncionarioAcademico" -TokenAdmin $tokenRoot -Prefijo "facad"
    $usernames += $funcAcad.Username

    $sufijo = Nuevo-Sufijo
    $codigo = "T$sufijo"
    $codigos += @($codigo, "X$sufijo")
    $nueva = @{ codigoAsignatura = $codigo; nombreAsignatura = "Asignatura de prueba $sufijo" }

    $r = Invoke-Api -Metodo POST -Ruta "asignaturas" -Token $tokenRoot -Cuerpo $nueva
    Escribir-Resultado "root crea asignatura (200, obtuvo $($r.Status))" ($r.Status -eq 200)
    if ($r.Status -ne 200) { Write-Host $r.Body; exit 1 }
    $uuid = ($r.Body | ConvertFrom-Json).uuidAsignatura

    $r = Invoke-Api -Metodo POST -Ruta "asignaturas" -Token $tokenRoot -Cuerpo @{ codigoAsignatura = $codigo; nombreAsignatura = "Repetida" }
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root crea con codigo repetido (500 codigoError 2, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "2"))

    $r = Invoke-Api -Metodo POST -Ruta "asignaturas" -Token $tokenRoot -Cuerpo @{ codigoAsignatura = ""; nombreAsignatura = "" }
    Escribir-Resultado "root crea con campos vacios (400, obtuvo $($r.Status))" ($r.Status -eq 400)

    $r = Invoke-Api -Metodo GET -Ruta "asignaturas/paginado?pagina=0&tamanio=5" -Token $tokenRoot
    Escribir-Resultado "root lista paginado (200, obtuvo $($r.Status))" ($r.Status -eq 200)

    $r = Invoke-Api -Metodo GET -Ruta "asignaturas/filtro?texto=$codigo&pagina=0&tamanio=5" -Token $tokenRoot
    $total = if ($r.Status -eq 200) { ($r.Body | ConvertFrom-Json).totalElements } else { -1 }
    Escribir-Resultado "root filtra por codigo (200 y 1 resultado, obtuvo $($r.Status) y $total)" (($r.Status -eq 200) -and ($total -eq 1))

    $r = Invoke-Api -Metodo GET -Ruta "asignaturas/filtro?texto=noexiste$sufijo&pagina=0&tamanio=5" -Token $tokenRoot
    $total = if ($r.Status -eq 200) { ($r.Body | ConvertFrom-Json).totalElements } else { -1 }
    Escribir-Resultado "root filtra sin resultados (200 y pagina vacia, obtuvo $($r.Status) y $total)" (($r.Status -eq 200) -and ($total -eq 0))

    $r = Invoke-Api -Metodo GET -Ruta "asignaturas/paginado?pagina=-1&tamanio=5" -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root paginado con pagina negativa (500 codigoError 6, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6"))

    $r = Invoke-Api -Metodo GET -Ruta "asignaturas/$uuid" -Token $tokenRoot
    Escribir-Resultado "root obtiene por uuid (200, obtuvo $($r.Status))" ($r.Status -eq 200)

    $inexistente = [guid]::NewGuid().ToString()
    $r = Invoke-Api -Metodo GET -Ruta "asignaturas/$inexistente" -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root obtiene uuid inexistente (500 codigoError 3, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "3"))

    $editada = @{ codigoAsignatura = $codigo; nombreAsignatura = "Asignatura editada $sufijo" }
    $r = Invoke-Api -Metodo PUT -Ruta "asignaturas/$uuid" -Token $tokenRoot -Cuerpo $editada
    $nombre = if ($r.Status -eq 200) { ($r.Body | ConvertFrom-Json).nombreAsignatura } else { "" }
    Escribir-Resultado "root edita (200 y nombre nuevo, obtuvo $($r.Status))" (($r.Status -eq 200) -and ($nombre -eq $editada.nombreAsignatura))

    $r = Invoke-Api -Metodo PUT -Ruta "asignaturas/$inexistente" -Token $tokenRoot -Cuerpo $editada
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root edita uuid inexistente (500 codigoError 3, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "3"))

    $lecturas = @("asignaturas/paginado?pagina=0&tamanio=5", "asignaturas/filtro?texto=$codigo&pagina=0&tamanio=5", "asignaturas/$uuid")
    $escrituras = @(
        @{ Metodo = "POST"; Ruta = "asignaturas"; Cuerpo = @{ codigoAsignatura = "X$sufijo"; nombreAsignatura = "No debe crearse" } },
        @{ Metodo = "PUT"; Ruta = "asignaturas/$uuid"; Cuerpo = @{ codigoAsignatura = $codigo; nombreAsignatura = "No debe editarse" } }
    )

    foreach ($ruta in $lecturas) {
        $r = Invoke-Api -Metodo GET -Ruta $ruta -Token $funcAcad.Token
        Escribir-Resultado "Funcionario Academico GET $ruta (200, obtuvo $($r.Status))" ($r.Status -eq 200)
    }
    foreach ($e in $escrituras) {
        $r = Invoke-Api -Metodo $e.Metodo -Ruta $e.Ruta -Token $funcAcad.Token -Cuerpo $e.Cuerpo
        Escribir-Resultado "Funcionario Academico $($e.Metodo) $($e.Ruta) (403, obtuvo $($r.Status))" ($r.Status -eq 403)
    }

    foreach ($ruta in $lecturas) {
        $r = Invoke-Api -Metodo GET -Ruta $ruta -Token $estudiante.Token
        Escribir-Resultado "Estudiante GET $ruta (403, obtuvo $($r.Status))" ($r.Status -eq 403)
    }
    foreach ($e in $escrituras) {
        $r = Invoke-Api -Metodo $e.Metodo -Ruta $e.Ruta -Token $estudiante.Token -Cuerpo $e.Cuerpo
        Escribir-Resultado "Estudiante $($e.Metodo) $($e.Ruta) (403, obtuvo $($r.Status))" ($r.Status -eq 403)
    }

    foreach ($ruta in $lecturas) {
        $r = Invoke-Api -Metodo GET -Ruta $ruta
        Escribir-Resultado "Sin sesion GET $ruta (401, obtuvo $($r.Status))" ($r.Status -eq 401)
    }
    foreach ($e in $escrituras) {
        $r = Invoke-Api -Metodo $e.Metodo -Ruta $e.Ruta -Cuerpo $e.Cuerpo
        Escribir-Resultado "Sin sesion $($e.Metodo) $($e.Ruta) (401, obtuvo $($r.Status))" ($r.Status -eq 401)
    }

    $r = Invoke-Api -Metodo DELETE -Ruta "asignaturas/$uuid" -Token $tokenRoot
    Escribir-Resultado "root DELETE asignaturas/{uuid} queda denegado (403, obtuvo $($r.Status))" ($r.Status -eq 403)

    $r = Invoke-Api -Metodo GET -Ruta "asignaturas/$uuid" -Token $tokenRoot
    $nombre = if ($r.Status -eq 200) { ($r.Body | ConvertFrom-Json).nombreAsignatura } else { "" }
    Escribir-Resultado "las escrituras denegadas no cambiaron la asignatura" ($nombre -eq $editada.nombreAsignatura)
}
finally {
    $borrados = Limpiar-DatosPrueba -Desde $inicio -Usernames $usernames -CodigosAsignatura $codigos
    $fotoFinal = Foto-Base
    Escribir-Resultado "la base quedo como al inicio salvo los logs de rootfiet (usuarios borrados $borrados; $fotoFinal)" ($fotoFinal -eq $fotoInicial)
}
