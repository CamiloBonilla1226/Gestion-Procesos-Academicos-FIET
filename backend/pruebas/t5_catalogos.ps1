. "$PSScriptRoot\comun.ps1"

$tokenRoot = Iniciar-Sesion -Usuario "rootfiet" -Clave "rootfiet1234"
$inicio = Inicio-Corrida
$fotoInicial = Foto-Base
$usernames = @()

try {
    $estudiante = Crear-UsuarioPrueba -Perfil "Estudiante" -TokenAdmin $tokenRoot -Prefijo "est"
    $usernames += $estudiante.Username
    $funcAcad = Crear-UsuarioPrueba -Perfil "FuncionarioAcademico" -TokenAdmin $tokenRoot -Prefijo "facad"
    $usernames += $funcAcad.Username
    $funcionario = Crear-UsuarioPrueba -Perfil "Funcionario" -TokenAdmin $tokenRoot -Prefijo "func"
    $usernames += $funcionario.Username

    $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/tipos-solicitud" -Token $tokenRoot
    Escribir-Resultado "root lista tipos de solicitud (200, obtuvo $($r.Status))" ($r.Status -eq 200)
    $tipos = @()
    if ($r.Status -eq 200) { $lista = $r.Body | ConvertFrom-Json; $tipos = @($lista) }
    Write-Host "  tipos de solicitud registrados: $($tipos.Count)"

    $inexistente = [guid]::NewGuid().ToString()
    foreach ($sub in @("etapas", "tipos-anexo")) {
        $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/tipos-solicitud/$inexistente/$sub" -Token $tokenRoot
        $cod = Obtener-CodigoError $r
        Escribir-Resultado "$sub de un tipo inexistente (500 codigoError 3, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "3"))
    }

    if ($tipos.Count -gt 0) {
        $uuidTipo = $tipos[0].uuidTipoSolicitudAcademica
        foreach ($sub in @("etapas", "tipos-anexo")) {
            $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/tipos-solicitud/$uuidTipo/$sub" -Token $tokenRoot
            Escribir-Resultado "$sub de un tipo existente (200, obtuvo $($r.Status))" ($r.Status -eq 200)
        }
    }

    foreach ($rol in @("ESTUDIANTE", "funcionario", "Decano")) {
        $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/etiquetas?rol=$rol" -Token $tokenRoot
        Escribir-Resultado "etiquetas del rol $rol (200, obtuvo $($r.Status))" ($r.Status -eq 200)
    }
    $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/etiquetas?rol=SECRETARIO" -Token $tokenRoot
    $cod = Obtener-CodigoError $r
    Escribir-Resultado "etiquetas de un rol no valido (500 codigoError 6, obtuvo $($r.Status) codigoError $cod)" (($r.Status -eq 500) -and ($cod -eq "6"))

    $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/situaciones" -Token $tokenRoot
    Escribir-Resultado "root lista situaciones (200, obtuvo $($r.Status))" ($r.Status -eq 200)

    $rutas = @("catalogos-academicos/tipos-solicitud", "catalogos-academicos/etiquetas?rol=ESTUDIANTE", "catalogos-academicos/situaciones")
    $perfiles = @(
        @{ Nombre = "Estudiante"; Token = $estudiante.Token },
        @{ Nombre = "Funcionario Academico"; Token = $funcAcad.Token },
        @{ Nombre = "Funcionario"; Token = $funcionario.Token }
    )
    foreach ($p in $perfiles) {
        foreach ($ruta in $rutas) {
            $r = Invoke-Api -Metodo GET -Ruta $ruta -Token $p.Token
            Escribir-Resultado "$($p.Nombre) GET $ruta (200, obtuvo $($r.Status))" ($r.Status -eq 200)
        }
    }
    foreach ($ruta in $rutas) {
        $r = Invoke-Api -Metodo GET -Ruta $ruta
        Escribir-Resultado "Sin sesion GET $ruta (401, obtuvo $($r.Status))" ($r.Status -eq 401)
    }

    $r = Invoke-Api -Metodo POST -Ruta "catalogos-academicos/tipos-solicitud" -Token $tokenRoot -Cuerpo @{ nombre = "No" }
    Escribir-Resultado "root POST catalogos-academicos queda denegado (403, obtuvo $($r.Status))" ($r.Status -eq 403)
    $r = Invoke-Api -Metodo DELETE -Ruta "catalogos-academicos/situaciones" -Token $tokenRoot
    Escribir-Resultado "root DELETE catalogos-academicos queda denegado (403, obtuvo $($r.Status))" ($r.Status -eq 403)

    $TipoMatricula = "3b6dccc9-ff78-48ef-8e66-05fb9932e302"
    $TipoAsignatura = "7b3d4347-3525-4ab7-a2b4-723b1ced80c8"
    $TipoSupletorio = "459cde47-f3a3-4a74-9b44-1063a516326c"
    $TiposSembrados = [ordered]@{
        $TipoMatricula  = Texto "Cancelaci\u00f3n de Matr\u00edcula"
        $TipoAsignatura = Texto "Cancelaci\u00f3n de Asignatura"
        $TipoSupletorio = "Examen Supletorio"
    }
    $EtapasUniversales = @("APROBADA", "APROBADA_POR_DECANO", "EN_REVISION_DECANO", "RADICADA", "RECHAZADA", "RECHAZADA_POR_DECANO")
    $EtapasSupletorio = @($EtapasUniversales + @("EN_VERIFICACION_PAGO", "PENDIENTE_PAGO") | Sort-Object)
    $Etiquetas = @{
        ESTUDIANTE  = @{
            RADICADA = Texto "En tr\u00e1mite"; EN_REVISION_DECANO = Texto "En tr\u00e1mite"; APROBADA_POR_DECANO = Texto "En tr\u00e1mite"; RECHAZADA_POR_DECANO = Texto "En tr\u00e1mite"
            PENDIENTE_PAGO = "Pendiente de pago"; EN_VERIFICACION_PAGO = Texto "En verificaci\u00f3n de pago"; APROBADA = "Aprobada"; RECHAZADA = "Rechazada"
        }
        FUNCIONARIO = @{
            RADICADA = "Pendiente"; EN_REVISION_DECANO = Texto "En Gesti\u00f3n"; APROBADA_POR_DECANO = "Pendiente de Respuesta"; RECHAZADA_POR_DECANO = "Pendiente de Respuesta"
            PENDIENTE_PAGO = Texto "En Gesti\u00f3n"; EN_VERIFICACION_PAGO = Texto "Pendiente de Verificaci\u00f3n"; APROBADA = "Respondida"; RECHAZADA = "Respondida"
        }
        DECANO      = @{
            EN_REVISION_DECANO = "Pendiente"; APROBADA_POR_DECANO = "Respondida"; RECHAZADA_POR_DECANO = "Respondida"
            PENDIENTE_PAGO = "Respondida"; EN_VERIFICACION_PAGO = "Respondida"; APROBADA = "Respondida"; RECHAZADA = "Respondida"
        }
    }
    $AnexosMatricula = @{
        (Texto "Paz y salvo - Divisi\u00f3n de Bibliotecas")                                          = "pdf"
        (Texto "Paz y salvo - Divisi\u00f3n de Deportes y Recreaci\u00f3n")                           = "pdf"
        (Texto "Paz y salvo - Divisi\u00f3n de Salud Integral")                                       = "pdf"
        (Texto "Cup\u00f3n de Confirmaci\u00f3n de la Intervenci\u00f3n Psicosocial - Divisi\u00f3n de Salud Integral") = "pdf"
        (Texto "Paz y salvo - Divisi\u00f3n Financiera")                                              = "pdf"
        (Texto "Carn\u00e9 estudiantil o constancia de no tr\u00e1mite - DARCA")                      = "pdf,jpg,jpeg,png"
    }
    $AnexosSupletorio = @{
        "Formato PM-FO-4-FOR-23 firmado por el docente que orienta la asignatura"       = @{ Formatos = "pdf,jpg,png"; Obligatorio = $true }
        (Texto "Soporte de la justificaci\u00f3n de la no presentaci\u00f3n")           = @{ Formatos = "pdf,jpg,png"; Obligatorio = $false }
        "Formato firmado por el docente de la asignatura con la que se cruza"           = @{ Formatos = "pdf,jpg,png"; Obligatorio = $false }
        "Recibo de pago"                                                                = @{ Formatos = "pdf"; Obligatorio = $false }
        "Comprobante de pago"                                                           = @{ Formatos = "pdf,jpg,png"; Obligatorio = $false }
    }
    $Situaciones = [ordered]@{ R0 = "Cursada por primera vez"; R1 = "Cursada por segunda vez"; R2 = "Cursada por tercera vez"; R3 = "Cursada por cuarta vez" }

    $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/tipos-solicitud" -Token $tokenRoot
    $lista = Leer-Json $r
    $tipos = @($lista)
    foreach ($uuidTipo in $TiposSembrados.Keys) {
        $t = @($tipos | Where-Object { $_.uuidTipoSolicitudAcademica -eq $uuidTipo })
        $ok = ($t.Count -eq 1) -and ($t[0].nombre -ceq $TiposSembrados[$uuidTipo]) -and [bool]$t[0].uuidFuncionarioAcademico
        Escribir-Resultado "tipo sembrado $uuidTipo existe una vez con su nombre y funcionario (obtuvo $($t.Count))" $ok
    }
    $funcionarios = @($tipos | Where-Object { $TiposSembrados.Contains($_.uuidTipoSolicitudAcademica) } | ForEach-Object { $_.uuidFuncionarioAcademico } | Sort-Object -Unique)
    Escribir-Resultado "los tres tipos sembrados tienen el mismo funcionario academico (obtuvo $($funcionarios.Count))" ($funcionarios.Count -eq 1)

    foreach ($caso in @(@{ Uuid = $TipoMatricula; Esperadas = $EtapasUniversales; Nombre = "matricula" }, @{ Uuid = $TipoAsignatura; Esperadas = $EtapasUniversales; Nombre = "asignatura" }, @{ Uuid = $TipoSupletorio; Esperadas = $EtapasSupletorio; Nombre = "supletorio" })) {
        $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/tipos-solicitud/$($caso.Uuid)/etapas" -Token $tokenRoot
        $lista = Leer-Json $r
        $codigos = @(@($lista) | ForEach-Object { $_.codigo } | Sort-Object)
        $ok = ($r.Status -eq 200) -and (($codigos -join ",") -ceq (@($caso.Esperadas | Sort-Object) -join ","))
        Escribir-Resultado "etapas de $($caso.Nombre) ($($caso.Esperadas.Count) esperadas, obtuvo $($codigos -join ','))" $ok
    }

    foreach ($rol in @("ESTUDIANTE", "FUNCIONARIO", "DECANO")) {
        $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/etiquetas?rol=$rol" -Token $tokenRoot
        $lista = Leer-Json $r
        $filas = @($lista)
        $esperadas = $Etiquetas[$rol]
        $malas = @($esperadas.Keys | Where-Object { $codigo = $_; $f = @($filas | Where-Object { $_.codigoEtapa -eq $codigo }); ($f.Count -ne 1) -or ($f[0].etiqueta -cne $esperadas[$codigo]) })
        $ok = ($filas.Count -eq $esperadas.Count) -and ($malas.Count -eq 0)
        Escribir-Resultado "etiquetas de $rol ($($esperadas.Count) esperadas, obtuvo $($filas.Count), distintas: $($malas -join ','))" $ok
    }
    $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/etiquetas?rol=DECANO" -Token $tokenRoot
    $lista = Leer-Json $r
    Escribir-Resultado "el Decano no tiene etiqueta en RADICADA" (@(@($lista) | Where-Object { $_.codigoEtapa -eq "RADICADA" }).Count -eq 0)

    $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/tipos-solicitud/$TipoMatricula/tipos-anexo" -Token $tokenRoot
    $lista = Leer-Json $r
    $anexos = @($lista)
    $malos = @($AnexosMatricula.Keys | Where-Object { $nombre = $_; $a = @($anexos | Where-Object { $_.nombre -ceq $nombre }); ($a.Count -ne 1) -or ($a[0].formatosPermitidos -cne $AnexosMatricula[$nombre]) -or ($a[0].obligatorio -ne $true) })
    Escribir-Resultado "matricula tiene los seis anexos obligatorios con sus formatos (obtuvo $($anexos.Count), distintos: $($malos.Count))" (($anexos.Count -eq 6) -and ($malos.Count -eq 0))

    $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/tipos-solicitud/$TipoAsignatura/tipos-anexo" -Token $tokenRoot
    $lista = Leer-Json $r
    Escribir-Resultado "asignatura no tiene tipos de anexo (obtuvo $(@($lista).Count))" (($r.Status -eq 200) -and (@($lista).Count -eq 0))

    $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/tipos-solicitud/$TipoSupletorio/tipos-anexo" -Token $tokenRoot
    $lista = Leer-Json $r
    $anexos = @($lista)
    $malos = @($AnexosSupletorio.Keys | Where-Object { $nombre = $_; $e = $AnexosSupletorio[$nombre]; $a = @($anexos | Where-Object { $_.nombre -ceq $nombre }); ($a.Count -ne 1) -or ($a[0].formatosPermitidos -cne $e.Formatos) -or ($a[0].obligatorio -ne $e.Obligatorio) })
    Escribir-Resultado "supletorio tiene sus cinco anexos y solo el FOR-23 es obligatorio (obtuvo $($anexos.Count), distintos: $($malos.Count))" (($anexos.Count -eq 5) -and ($malos.Count -eq 0))

    $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/situaciones" -Token $tokenRoot
    $lista = Leer-Json $r
    $situacionesApi = @($lista)
    $malas = @($Situaciones.Keys | Where-Object { $codigo = $_; $s = @($situacionesApi | Where-Object { $_.codigo -eq $codigo }); ($s.Count -ne 1) -or ($s[0].nombre -cne $Situaciones[$codigo]) })
    Escribir-Resultado "situaciones R0 a R3 con sus nombres (obtuvo $($situacionesApi.Count), distintas: $($malas -join ','))" (($situacionesApi.Count -eq 4) -and ($malas.Count -eq 0))
}
finally {
    $borrados = Limpiar-DatosPrueba -Desde $inicio -Usernames $usernames
    $fotoFinal = Foto-Base
    Escribir-Resultado "la base quedo como al inicio salvo los logs de rootfiet (usuarios borrados $borrados; $fotoFinal)" ($fotoFinal -eq $fotoInicial)
}
