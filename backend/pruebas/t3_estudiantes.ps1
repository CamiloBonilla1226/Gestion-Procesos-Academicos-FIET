. "$PSScriptRoot\comun.ps1"

function Nuevo-CuerpoEstudiante {
    param([string]$Sufijo, $Materias)
    $script:usernames += "alu$Sufijo"
    $script:codigos += @($Materias | ForEach-Object { $_.codigoAsignatura })
    return @{
        nombres            = "Estudiante"
        apellidos          = "Prueba $Sufijo"
        tipoDocumento      = $Global:CedulaCiudadania
        numeroDocumento    = "8$Sufijo"
        telefono           = "+573000000000"
        correoElectronico  = "est$Sufijo@unicauca.edu.co"
        username           = "alu$Sufijo"
        password           = "Clave12345"
        codigoEstudiantil  = "C$Sufijo"
        programaAcademico  = "Ingenieria de Sistemas"
        semestre           = "5"
        facultad           = "FIET"
        asignaturas        = @($Materias)
    }
}

function Contar-Lista {
    param($Respuesta)
    if ($Respuesta.Status -ne 200) { return -1 }
    $lista = $Respuesta.Body | ConvertFrom-Json
    return @($lista).Count
}

$tokenRoot = Iniciar-Sesion -Usuario "rootfiet" -Clave "rootfiet1234"
$inicio = Inicio-Corrida
$fotoInicial = Foto-Base
$usernames = @()
$codigos = @()

try {
    $funcAcad = Crear-UsuarioPrueba -Perfil "FuncionarioAcademico" -TokenAdmin $tokenRoot -Prefijo "facad"
    $usernames += $funcAcad.Username
    $funcionario = Crear-UsuarioPrueba -Perfil "Funcionario" -TokenAdmin $tokenRoot -Prefijo "func"
    $usernames += $funcionario.Username

    $sufijoA = Nuevo-Sufijo
    $codigos += @("MC$sufijoA", "MZ$sufijoA")
    $materiaA = @{ codigoAsignatura = "MA$sufijoA"; nombreAsignatura = "Materia A $sufijoA"; grupo = "A" }
    $materiaB = @{ codigoAsignatura = "MB$sufijoA"; nombreAsignatura = "Materia B $sufijoA"; grupo = "B" }
    $cuerpoA = Nuevo-CuerpoEstudiante -Sufijo $sufijoA -Materias @($materiaA, $materiaB)

    $r = Invoke-Api -Metodo POST -Ruta "estudiantes" -Token $tokenRoot -Cuerpo $cuerpoA
    Escribir-Resultado "root crea estudiante con dos materias (200, obtuvo $($r.Status))" ($r.Status -eq 200)
    if ($r.Status -ne 200) { Write-Host $r.Body; exit 1 }
    $estudianteA = $r.Body | ConvertFrom-Json
    $uuidA = $estudianteA.uuidUsuario
    $matriculas = @($estudianteA.asignaturasMatriculadas)
    Escribir-Resultado "la respuesta trae dos materias activas" (($matriculas.Count -eq 2) -and (@($matriculas | Where-Object { $_.estado -eq "activa" }).Count -eq 2))
    Escribir-Resultado "la respuesta del estudiante no contiene password" ($r.Body -notmatch '"password"')

    $tokenA = Iniciar-Sesion -Usuario $cuerpoA.username -Clave $cuerpoA.password

    $r = Invoke-Api -Metodo GET -Ruta "estudiantes/mis-asignaturas" -Token $tokenA
    $n = Contar-Lista $r
    Escribir-Resultado "estudiante ve sus dos materias en mis-asignaturas (200 y 2, obtuvo $($r.Status) y $n)" (($r.Status -eq 200) -and ($n -eq 2))

    $uuidMatricula = $matriculas[0].uuidAsignaturaMatriculada
    $r = Invoke-Api -Metodo PATCH -Ruta "estudiantes/$uuidA/asignaturas/$uuidMatricula/estado" -Token $tokenRoot -Cuerpo @{ estado = "cancelada" }
    $estado = if ($r.Status -eq 200) { ($r.Body | ConvertFrom-Json).estado } else { "" }
    Escribir-Resultado "root cancela una materia (200 y cancelada, obtuvo $($r.Status) y $estado)" (($r.Status -eq 200) -and ($estado -eq "cancelada"))

    $r = Invoke-Api -Metodo GET -Ruta "estudiantes/mis-asignaturas" -Token $tokenA
    $n = Contar-Lista $r
    Escribir-Resultado "mis-asignaturas devuelve solo la activa (200 y 1, obtuvo $($r.Status) y $n)" (($r.Status -eq 200) -and ($n -eq 1))

    $r = Invoke-Api -Metodo PATCH -Ruta "estudiantes/$uuidA/asignaturas/$uuidMatricula/estado" -Token $tokenRoot -Cuerpo @{ estado = "activa" }
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root no puede reactivar una materia cancelada (500 codigoError 6, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6"))

    $r = Invoke-Api -Metodo PATCH -Ruta "estudiantes/$uuidA/asignaturas/$($matriculas[1].uuidAsignaturaMatriculada)/estado" -Token $tokenRoot -Cuerpo @{ estado = "retirada" }
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root con estado no valido (500 codigoError 6, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6"))

    $r = Invoke-Api -Metodo PATCH -Ruta "estudiantes/$uuidA/asignaturas/$uuidMatricula/estado" -Token $tokenRoot -Cuerpo @{ estado = "" }
    Escribir-Resultado "root con estado vacio (400, obtuvo $($r.Status))" ($r.Status -eq 400)

    $sufijoB = Nuevo-Sufijo
    $cuerpoB = Nuevo-CuerpoEstudiante -Sufijo $sufijoB -Materias @(@{ codigoAsignatura = $materiaA.codigoAsignatura; nombreAsignatura = "Nombre que no debe cambiar"; grupo = "A" })
    $r = Invoke-Api -Metodo POST -Ruta "estudiantes" -Token $tokenRoot -Cuerpo $cuerpoB
    Escribir-Resultado "root crea un segundo estudiante con una materia existente (200, obtuvo $($r.Status))" ($r.Status -eq 200)
    $estudianteB = $r.Body | ConvertFrom-Json
    $uuidB = $estudianteB.uuidUsuario
    $nombreMateria = @($estudianteB.asignaturasMatriculadas)[0].nombreAsignatura
    Escribir-Resultado "la materia existente conserva su nombre del catalogo" ($nombreMateria -eq $materiaA.nombreAsignatura)

    $r = Invoke-Api -Metodo PATCH -Ruta "estudiantes/$uuidB/asignaturas/$($matriculas[1].uuidAsignaturaMatriculada)/estado" -Token $tokenRoot -Cuerpo @{ estado = "aprobada" }
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root cambia estado de materia de otro estudiante (500 codigoError 3, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "3"))

    $r = Invoke-Api -Metodo POST -Ruta "estudiantes/$uuidA/asignaturas" -Token $tokenRoot -Cuerpo @{ codigoAsignatura = "MC$sufijoA"; nombreAsignatura = "Materia C $sufijoA"; grupo = "A" }
    $estado = if ($r.Status -eq 200) { ($r.Body | ConvertFrom-Json).estado } else { "" }
    Escribir-Resultado "root agrega materia nueva al estudiante (200 y activa, obtuvo $($r.Status) y $estado)" (($r.Status -eq 200) -and ($estado -eq "activa"))

    $r = Invoke-Api -Metodo POST -Ruta "estudiantes/$uuidA/asignaturas" -Token $tokenRoot -Cuerpo $materiaB
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root agrega materia ya activa en el mismo grupo (500 codigoError 6, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6"))

    $r = Invoke-Api -Metodo PUT -Ruta "estudiantes/$uuidA" -Token $tokenRoot -Cuerpo @{ semestre = "6"; programaAcademico = "Ingenieria Electronica" }
    $semestre = if ($r.Status -eq 200) { ($r.Body | ConvertFrom-Json).semestre } else { "" }
    Escribir-Resultado "root actualiza datos academicos (200 y semestre 6, obtuvo $($r.Status) y $semestre)" (($r.Status -eq 200) -and ($semestre -eq "6"))

    $r = Invoke-Api -Metodo PUT -Ruta "estudiantes/$uuidA" -Token $tokenRoot -Cuerpo @{ codigoEstudiantil = $cuerpoB.codigoEstudiantil }
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root actualiza con codigo de otro estudiante (500 codigoError 2, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "2"))

    $sufijoX = Nuevo-Sufijo
    $repetido = Nuevo-CuerpoEstudiante -Sufijo $sufijoX -Materias @(@{ codigoAsignatura = "MX$sufijoX"; nombreAsignatura = "No debe crearse"; grupo = "A" })
    $repetido.codigoEstudiantil = $cuerpoA.codigoEstudiantil
    $r = Invoke-Api -Metodo POST -Ruta "estudiantes" -Token $tokenRoot -Cuerpo $repetido
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "codigo estudiantil repetido (500 codigoError 2, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "2"))
    $login = Invoke-Api -Metodo POST -Ruta "sesiones" -Cuerpo @{ username = $repetido.username; password = $repetido.password }
    Escribir-Resultado "el username del intento fallido no puede iniciar sesion (obtuvo $($login.Status))" ($login.Status -ne 200)
    $r = Invoke-Api -Metodo GET -Ruta "asignaturas/filtro?texto=MX$sufijoX&pagina=0&tamanio=5" -Token $tokenRoot
    $total = if ($r.Status -eq 200) { ($r.Body | ConvertFrom-Json).totalElements } else { -1 }
    Escribir-Resultado "la materia del intento fallido no quedo en el catalogo (0, obtuvo $total)" ($total -eq 0)

    $campos = @(
        @{ Campo = "numeroDocumento"; Descripcion = "documento" },
        @{ Campo = "correoElectronico"; Descripcion = "correo" },
        @{ Campo = "username"; Descripcion = "username" }
    )
    foreach ($c in $campos) {
        $s = Nuevo-Sufijo
        $cuerpo = Nuevo-CuerpoEstudiante -Sufijo $s -Materias @(@{ codigoAsignatura = "MX$s"; nombreAsignatura = "No debe crearse"; grupo = "A" })
        $cuerpo[$c.Campo] = $cuerpoA[$c.Campo]
        $r = Invoke-Api -Metodo POST -Ruta "estudiantes" -Token $tokenRoot -Cuerpo $cuerpo
        $cod = Obtener-CodigoError $r
        Escribir-Resultado "$($c.Descripcion) repetido (500 codigoError 2, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "2"))
    }

    $s = Nuevo-Sufijo
    $cuerpo = Nuevo-CuerpoEstudiante -Sufijo $s -Materias @(
        @{ codigoAsignatura = "MD$s"; nombreAsignatura = "Duplicada"; grupo = "A" },
        @{ codigoAsignatura = "MD$s"; nombreAsignatura = "Duplicada"; grupo = "A" }
    )
    $r = Invoke-Api -Metodo POST -Ruta "estudiantes" -Token $tokenRoot -Cuerpo $cuerpo
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "materia duplicada en la peticion (500 codigoError 6, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6"))

    $vacio = Nuevo-CuerpoEstudiante -Sufijo (Nuevo-Sufijo) -Materias @()
    foreach ($k in @("nombres", "numeroDocumento", "codigoEstudiantil", "programaAcademico")) { $vacio[$k] = "" }
    $r = Invoke-Api -Metodo POST -Ruta "estudiantes" -Token $tokenRoot -Cuerpo $vacio
    Escribir-Resultado "campos vacios (400, obtuvo $($r.Status))" ($r.Status -eq 400)

    $sinMaterias = Nuevo-CuerpoEstudiante -Sufijo (Nuevo-Sufijo) -Materias @()
    $r = Invoke-Api -Metodo POST -Ruta "estudiantes" -Token $tokenRoot -Cuerpo $sinMaterias
    Escribir-Resultado "estudiante sin materias (400, obtuvo $($r.Status))" ($r.Status -eq 400)

    $r = Invoke-Api -Metodo GET -Ruta "estudiantes/filtro?codigo=$($cuerpoA.codigoEstudiantil)&pagina=0&tamanio=5" -Token $tokenRoot
    $total = if ($r.Status -eq 200) { ($r.Body | ConvertFrom-Json).totalElements } else { -1 }
    Escribir-Resultado "root filtra por codigo (200 y 1, obtuvo $($r.Status) y $total)" (($r.Status -eq 200) -and ($total -eq 1))
    Escribir-Resultado "el listado no contiene password" ($r.Body -notmatch '"password"')

    $r = Invoke-Api -Metodo GET -Ruta "estudiantes/filtro?codigo=noexiste$sufijoA&pagina=0&tamanio=5" -Token $tokenRoot
    $total = if ($r.Status -eq 200) { ($r.Body | ConvertFrom-Json).totalElements } else { -1 }
    Escribir-Resultado "root filtra sin resultados (200 y 0, obtuvo $($r.Status) y $total)" (($r.Status -eq 200) -and ($total -eq 0))

    $r = Invoke-Api -Metodo GET -Ruta "estudiantes/paginado?pagina=-1&tamanio=5" -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root paginado con pagina negativa (500 codigoError 6, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6"))

    $inexistente = [guid]::NewGuid().ToString()
    $r = Invoke-Api -Metodo GET -Ruta "estudiantes/$inexistente" -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "root obtiene estudiante inexistente (500 codigoError 3, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "3"))

    $r = Invoke-Api -Metodo GET -Ruta "estudiantes/mis-asignaturas" -Token $tokenRoot
    Escribir-Resultado "root en mis-asignaturas (403, obtuvo $($r.Status))" ($r.Status -eq 403)

    $r = Invoke-Api -Metodo DELETE -Ruta "estudiantes/$uuidA" -Token $tokenRoot
    Escribir-Resultado "root DELETE estudiantes/{uuid} queda denegado (403, obtuvo $($r.Status))" ($r.Status -eq 403)

    $lecturas = @("estudiantes/paginado?pagina=0&tamanio=5", "estudiantes/filtro?nombre=Estudiante&pagina=0&tamanio=5", "estudiantes/$uuidB")
    $escrituras = @(
        @{ Metodo = "POST"; Ruta = "estudiantes"; Cuerpo = (Nuevo-CuerpoEstudiante -Sufijo (Nuevo-Sufijo) -Materias @($materiaA)) },
        @{ Metodo = "PUT"; Ruta = "estudiantes/$uuidA"; Cuerpo = @{ semestre = "9" } },
        @{ Metodo = "POST"; Ruta = "estudiantes/$uuidA/asignaturas"; Cuerpo = @{ codigoAsignatura = "MZ$sufijoA"; nombreAsignatura = "No"; grupo = "Z" } },
        @{ Metodo = "PATCH"; Ruta = "estudiantes/$uuidA/asignaturas/$($matriculas[1].uuidAsignaturaMatriculada)/estado"; Cuerpo = @{ estado = "perdida" } }
    )

    $r = Invoke-Api -Metodo POST -Ruta "estudiantes" -Token $tokenA -Cuerpo $escrituras[0].Cuerpo
    Escribir-Resultado "Estudiante POST estudiantes (403, obtuvo $($r.Status))" ($r.Status -eq 403)
    $r = Invoke-Api -Metodo GET -Ruta $lecturas[0] -Token $tokenA
    Escribir-Resultado "Estudiante GET estudiantes/paginado (403, obtuvo $($r.Status))" ($r.Status -eq 403)
    $r = Invoke-Api -Metodo GET -Ruta "estudiantes/$uuidB" -Token $tokenA
    Escribir-Resultado "Estudiante GET estudiantes/{uuid} de otro estudiante (403, obtuvo $($r.Status))" ($r.Status -eq 403)
    $r = Invoke-Api -Metodo GET -Ruta "usuarios" -Token $tokenA
    Escribir-Resultado "Estudiante GET usuarios (403, obtuvo $($r.Status))" ($r.Status -eq 403)

    foreach ($ruta in $lecturas) {
        $r = Invoke-Api -Metodo GET -Ruta $ruta -Token $funcAcad.Token
        Escribir-Resultado "Funcionario Academico GET $ruta (200, obtuvo $($r.Status))" ($r.Status -eq 200)
    }
    foreach ($e in $escrituras) {
        $r = Invoke-Api -Metodo $e.Metodo -Ruta $e.Ruta -Token $funcAcad.Token -Cuerpo $e.Cuerpo
        Escribir-Resultado "Funcionario Academico $($e.Metodo) $($e.Ruta) (403, obtuvo $($r.Status))" ($r.Status -eq 403)
    }
    $r = Invoke-Api -Metodo GET -Ruta "estudiantes/mis-asignaturas" -Token $funcAcad.Token
    Escribir-Resultado "Funcionario Academico GET estudiantes/mis-asignaturas (403, obtuvo $($r.Status))" ($r.Status -eq 403)

    $todas = @($lecturas | ForEach-Object { @{ Metodo = "GET"; Ruta = $_; Cuerpo = $null } }) + @(@{ Metodo = "GET"; Ruta = "estudiantes/mis-asignaturas"; Cuerpo = $null }) + $escrituras
    foreach ($e in $todas) {
        $r = Invoke-Api -Metodo $e.Metodo -Ruta $e.Ruta -Token $funcionario.Token -Cuerpo $e.Cuerpo
        Escribir-Resultado "Funcionario $($e.Metodo) $($e.Ruta) (403, obtuvo $($r.Status))" ($r.Status -eq 403)
    }
    foreach ($e in $todas) {
        $r = Invoke-Api -Metodo $e.Metodo -Ruta $e.Ruta -Cuerpo $e.Cuerpo
        Escribir-Resultado "Sin sesion $($e.Metodo) $($e.Ruta) (401, obtuvo $($r.Status))" ($r.Status -eq 401)
    }

    $r = Invoke-Api -Metodo GET -Ruta "estudiantes/$uuidA" -Token $tokenRoot
    $a = $r.Body | ConvertFrom-Json
    Escribir-Resultado "las escrituras denegadas no cambiaron al estudiante (semestre 6 y 3 materias)" (($a.semestre -eq "6") -and (@($a.asignaturasMatriculadas).Count -eq 3))
}
finally {
    $borrados = Limpiar-DatosPrueba -Desde $inicio -Usernames $usernames -CodigosAsignatura $codigos
    $fotoFinal = Foto-Base
    Escribir-Resultado "la base quedo como al inicio salvo los logs de rootfiet (usuarios borrados $borrados; $fotoFinal)" ($fotoFinal -eq $fotoInicial)
}
