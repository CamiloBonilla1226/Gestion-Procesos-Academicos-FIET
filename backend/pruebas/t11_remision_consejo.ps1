. "$PSScriptRoot\comun.ps1"

$Global:CarpetaBackend = (Resolve-Path "$PSScriptRoot\..").Path
$Global:CarpetaAnexos = Join-Path $Global:CarpetaBackend "uploads\anexos"
$Global:PendienteOrden = "PENDIENTE AL ORDEN DEL D$([char]0x00CD)A"

function Ejecutar-Sql {
    param([string]$Sql)
    Push-Location $Global:CarpetaBackend
    try {
        $salida = & docker compose exec -T cfiet_database mysql -u root -pmysql --default-character-set=utf8mb4 cfiet -N -B -e $Sql 2>$null
    } finally {
        Pop-Location
    }
    return $salida
}

function Lista-Sql {
    param([string[]]$Valores)
    return (@($Valores | Where-Object { $_ } | ForEach-Object { "'$_'" }) -join ",")
}

function Nuevo-Pdf {
    param([string]$Texto)
    $ruta = Join-Path ([System.IO.Path]::GetTempPath()) ("t11_" + (Nuevo-Sufijo) + ".pdf")
    $contenido = "%PDF-1.4`n1 0 obj << /Type /Catalog >> endobj`n% $Texto`n%%EOF`n"
    [System.IO.File]::WriteAllBytes($ruta, [System.Text.Encoding]::ASCII.GetBytes($contenido))
    return $ruta
}

function Ejecutar-Curl {
    param([string[]]$Argumentos, [string]$Ruta, [string]$Token)
    $salida = [System.IO.Path]::GetTempFileName()
    $todos = @("-s", "-o", $salida, "-w", "%{http_code}", "-X", "POST") + $Argumentos
    if ($Token) { $todos += @("-H", "Authorization: Bearer $Token") }
    $todos += ($Global:BaseUrl + $Ruta)
    $codigo = & curl.exe @todos
    $cuerpo = [System.IO.File]::ReadAllText($salida, [System.Text.Encoding]::UTF8)
    Remove-Item $salida -Force
    return [pscustomobject]@{ Status = [int]$codigo; Body = $cuerpo }
}

function Descargar {
    param([string]$Ruta, [string]$Token, [string]$Destino)
    $argumentos = @("-s", "-o", $Destino, "-w", "%{http_code}", "-X", "GET", "-H", "Authorization: Bearer $Token", ($Global:BaseUrl + $Ruta))
    return [int](& curl.exe @argumentos)
}

function Codigo-Error {
    param($Respuesta)
    try { return [string]((Leer-Json $Respuesta).codigoError) } catch { return "" }
}

function Etapa-De {
    param([string]$Solicitud)
    return Ejecutar-Sql "select e.codigo from SOLICITUD_ACADEMICA s join ETAPA_SOLICITUD_ACADEMICA e on e.uuidEtapa = s.Etapa_uuid where s.uuidSolicitudAcademica = '$Solicitud'"
}

function Cuerpo-Tipo {
    param([string]$Nombre, [string]$Encargado, $Proceso)
    $cuerpo = @{
        nombre            = $Nombre
        descripcion       = "Tipo de prueba de la remision al Consejo"
        seccion           = "asuntos pregrado"
        perfilSolicitante = "Estudiante"
        anexos            = @(@{ nombre = "Formato de la solicitud"; descripcion = "FOR-84"; formato = "pdf"; obligatoriedad = $true })
        uuidFuncionario   = $Encargado
    }
    if ($null -ne $Proceso) { $cuerpo.procesoAcademicoUuid = $Proceso }
    return $cuerpo
}

$tokenRoot = Iniciar-Sesion -Usuario "rootfiet" -Clave "rootfiet1234"
$inicio = Consultar-Base "select now()"
$tipoCa = Ejecutar-Sql "select uuidTipoSolicitudAcademica from TIPO_SOLICITUD_ACADEMICA where nombre like 'Cancelaci%n de Asignatura'"
$tipoCm = Ejecutar-Sql "select uuidTipoSolicitudAcademica from TIPO_SOLICITUD_ACADEMICA where nombre like 'Cancelaci%n de Matr%cula'"
$situaciones = @(Ejecutar-Sql "select uuidSituacionAcademica from SITUACION_ACADEMICA_ASIGNATURA where ambito = 'MATRICULA' order by codigo")
$responsableCa = Ejecutar-Sql "select coalesce(FuncionarioAcademico_uuid, '') from TIPO_SOLICITUD_ACADEMICA where uuidTipoSolicitudAcademica = '$tipoCa'"
$enlacesPrevios = [int](Ejecutar-Sql "select count(*) from TIPO_SOLICITUD_CONSEJO")
$etapaConsejo = Ejecutar-Sql "select count(*) from ETAPA_SOLICITUD_ACADEMICA where codigo = 'EN_REVISION_CONSEJO'"
if (-not $tipoCa -or -not $tipoCm -or $situaciones.Count -lt 1 -or $etapaConsejo -ne "1") { Write-Host "FAIL faltan los catalogos sembrados o la migracion de la remision al Consejo"; exit 1 }
$habiaAnexos = Test-Path $Global:CarpetaAnexos
$Global:CarpetaRespuestas = Join-Path $Global:CarpetaBackend "uploads\respuestas"
$habiaRespuestas = Test-Path $Global:CarpetaRespuestas
$orden = $null
$uuidRespuesta = $null
$sufijo = Nuevo-Sufijo

$usernames = @()
$codigos = @()
$archivos = @()
$tiposCreados = @()
$solicitudes = @()

try {
    $programa = Programa-PorSigla -Token $tokenRoot -Sigla "PIS"
    $asignatura = Crear-AsignaturaPrueba -Token $tokenRoot -UuidPrograma $programa.uuidProgramaAcademico -Nombre "Materia T11 $sufijo"
    $codigos += $asignatura.codigoAsignatura

    $cuerpoEstudiante = @{
        nombres = "Remitida"; apellidos = "Consejo $sufijo"; tipoDocumento = $Global:CedulaCiudadania; numeroDocumento = "5$sufijo"
        telefono = "+573000000000"; correoElectronico = "rce$sufijo@unicauca.edu.co"; username = "rce$sufijo"; password = "Clave12345"
        codigoEstudiantil = "C$sufijo"; uuidProgramaAcademico = $programa.uuidProgramaAcademico; semestre = "5"
        asignaturas = @(@{ codigoAsignatura = $asignatura.codigoAsignatura; grupo = "A" })
    }
    $usernames += "rce$sufijo"
    $r = Invoke-Api -Metodo POST -Ruta "estudiantes" -Token $tokenRoot -Cuerpo $cuerpoEstudiante
    if ($r.Status -ne 200) { throw "No se pudo crear el estudiante ($($r.Status)): $($r.Body)" }
    $estudiante = (Leer-Json $r).uuidUsuario
    $tokenEstudiante = Iniciar-Sesion -Usuario "rce$sufijo" -Clave "Clave12345"

    $cuerpoAcademico = @{
        nombres = "Encargada"; apellidos = "Consejo $sufijo"; tipoDocumento = $Global:CedulaCiudadania; numeroDocumento = "4$sufijo"
        telefono = "+573000000000"; correoElectronico = "eca$sufijo@unicauca.edu.co"; username = "eca$sufijo"; password = "Clave12345"; cargo = "DepT11 $sufijo"
    }
    $usernames += "eca$sufijo"
    $r = Invoke-Api -Metodo POST -Ruta "funcionarios-academicos" -Token $tokenRoot -Cuerpo $cuerpoAcademico
    if ($r.Status -ne 200) { throw "No se pudo crear el funcionario academico ($($r.Status)): $($r.Body)" }
    $academico = (Leer-Json $r).uuidUsuario
    $tokenAcademico = Iniciar-Sesion -Usuario "eca$sufijo" -Clave "Clave12345"

    $funcionarioJulian = Crear-UsuarioPrueba -Perfil "Funcionario" -TokenAdmin $tokenRoot -Prefijo "fjt11"
    $usernames += $funcionarioJulian.Username
    $decano = Crear-UsuarioPrueba -Perfil "Decano" -TokenAdmin $tokenRoot -Prefijo "dect11"
    $usernames += $decano.Username

    $r = Invoke-Api -Metodo PUT -Ruta "catalogos-academicos/tipos-solicitud/$tipoCa/funcionario" -Token $tokenRoot -Cuerpo @{ funcionarioUuid = $academico }
    if ($r.Status -ne 200) { throw "No se pudo asignar Cancelacion de Asignatura ($($r.Status)): $($r.Body)" }

    $soporte = Nuevo-Pdf "soporte del motivo"; $archivos += $soporte
    $r = Ejecutar-Curl -Argumentos @("-F", "motivo=Motivo de prueba para el Consejo", "-F", "asignaturas=$((Ejecutar-Sql "select m.uuidAsignaturaMatriculada from ASIGNATURA_MATRICULADA m where m.Estudiante_uuid = '$estudiante'"))", "-F", "soporte=@$soporte;type=application/pdf") -Ruta "cancelaciones-asignatura" -Token $tokenEstudiante
    $radicada = if ($r.Status -eq 200) { Leer-Json $r } else { $null }
    $sol = $radicada.uuidSolicitudAcademica
    $solicitudes += $sol
    Escribir-Resultado "el estudiante genera la solicitud (obtuvo $($r.Status))" (($r.Status -eq 200) -and $sol)
    $formato = Join-Path ([System.IO.Path]::GetTempPath()) ("t11_formato_" + $sufijo + ".pdf"); $archivos += $formato
    $codigo = Descargar -Ruta "solicitudes-academicas/$sol/formato" -Token $tokenEstudiante -Destino $formato
    $r = Ejecutar-Curl -Argumentos @("-F", "archivo=@$formato;type=application/pdf") -Ruta "solicitudes-academicas/$sol/estudiante/radicar" -Token $tokenEstudiante
    Escribir-Resultado "el estudiante sube el formato firmado y la solicitud queda RADICADA (formato $codigo, radicar $($r.Status), etapa $(Etapa-De $sol))" (($codigo -eq 200) -and ($r.Status -eq 200) -and ((Etapa-De $sol) -eq "RADICADA"))

    $fila = Ejecutar-Sql "select uuidAsignaturaSolicitud from ASIGNATURA_SOLICITUD_ACADEMICA where SolicitudAcademica_uuid = '$sol'"
    $remision = @{ observacion = "Caso especial para el Consejo"; evaluaciones = @(@{ asignaturaSolicitudUuid = $fila; numeroFaltas = "2"; nota = "3.5"; situacionMatriculaUuid = $situaciones[0] }) }

    if ($enlacesPrevios -eq 0) {
        $r = Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$sol/funcionario/remitir-consejo" -Token $tokenAcademico -Cuerpo $remision
        $espejos = Ejecutar-Sql "select count(*) from REMISION_CONSEJO where SolicitudAcademica_uuid = '$sol'"
        Escribir-Resultado "sin tipo enlazado no se remite al Consejo: codigo 4 y sigue RADICADA (obtuvo $($r.Status) codigo $(Codigo-Error $r), etapa $(Etapa-De $sol), remisiones $espejos)" (($r.Status -ne 200) -and ((Codigo-Error $r) -eq "4") -and ((Etapa-De $sol) -eq "RADICADA") -and ($espejos -eq "0"))
    } else {
        Write-Host "Aviso: la base ya tiene un tipo enlazado al Consejo; no se prueba la remision sin enlace"
    }

    $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/tipos-solicitud-consejo/encargados" -Token $tokenRoot
    $sinProceso = @((Leer-Json $r) | ForEach-Object { $_.uuidUsuario })
    Escribir-Resultado "sin proceso los candidatos son los Funcionarios de Julian (obtuvo $($r.Status), funcionario $($sinProceso -contains $funcionarioJulian.Uuid), academico $($sinProceso -contains $academico))" (($r.Status -eq 200) -and ($sinProceso -contains $funcionarioJulian.Uuid) -and ($sinProceso -notcontains $academico))
    $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/tipos-solicitud-consejo/encargados?procesoAcademico=$tipoCa" -Token $decano.Token
    $conProceso = @(Leer-Json $r)
    $filaAcademico = @($conProceso | Where-Object { $_.uuidUsuario -eq $academico })
    Escribir-Resultado "con Cancelacion de Asignatura se suman los Funcionarios Academicos (obtuvo $($r.Status), tipo $($filaAcademico.tipo))" (($r.Status -eq 200) -and (@($conProceso | Where-Object { $_.uuidUsuario -eq $funcionarioJulian.Uuid }).Count -eq 1) -and ($filaAcademico.Count -eq 1) -and ($filaAcademico[0].tipo -eq "FUNCIONARIO_ACADEMICO"))
    $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/tipos-solicitud-consejo/encargados?procesoAcademico=$tipoCm" -Token $tokenRoot
    Escribir-Resultado "otro proceso no tiene candidatos del Consejo: codigo 4 (obtuvo $($r.Status) codigo $(Codigo-Error $r))" (($r.Status -ne 200) -and ((Codigo-Error $r) -eq "4"))
    $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/tipos-solicitud-consejo/encargados" -Token $tokenAcademico
    Escribir-Resultado "el Funcionario Academico no lista candidatos (403, obtuvo $($r.Status))" ($r.Status -eq 403)

    $nombreTipo = "Cancelacion de asignatura T11 $sufijo"
    $r = Invoke-Api -Metodo POST -Ruta "tipos/solicitudes" -Token $tokenRoot -Cuerpo (Cuerpo-Tipo "$nombreTipo otro" $academico $null)
    $sinEnlace = [int](Ejecutar-Sql "select count(*) from tiposSolicitudes where nombre = '$nombreTipo otro'")
    Escribir-Resultado "un Funcionario Academico sin proceso no puede ser encargado y no se crea el tipo (obtuvo $($r.Status), tipos $sinEnlace)" (($r.Status -ne 200) -and ($sinEnlace -eq 0))
    $r = Invoke-Api -Metodo POST -Ruta "tipos/solicitudes" -Token $tokenRoot -Cuerpo (Cuerpo-Tipo "$nombreTipo matricula" $academico $tipoCm)
    $conMatricula = [int](Ejecutar-Sql "select count(*) from tiposSolicitudes where nombre = '$nombreTipo matricula'")
    Escribir-Resultado "Cancelacion de Matricula no se puede enlazar: codigo 4 y no se crea el tipo (obtuvo $($r.Status) codigo $(Codigo-Error $r), tipos $conMatricula)" (($r.Status -ne 200) -and ((Codigo-Error $r) -eq "4") -and ($conMatricula -eq 0))

    if ($enlacesPrevios -eq 0) {
        $r = Invoke-Api -Metodo POST -Ruta "tipos/solicitudes" -Token $tokenRoot -Cuerpo (Cuerpo-Tipo $nombreTipo $academico $tipoCa)
        $tipo = if ($r.Status -eq 200) { (Leer-Json $r).uuidTipoSolicitud } else { $null }
        if ($tipo) { $tiposCreados += $tipo }
        $enlace = Ejecutar-Sql "select concat(TipoSolicitudAcademica_uuid, '|', coalesce(EncargadoAcademico_uuid, '')) from TIPO_SOLICITUD_CONSEJO where TipoSolicitud_uuid = '$tipo'"
        $encargadoJulian = Ejecutar-Sql "select coalesce(uuidUsuario, 'nulo') from tiposSolicitudes where uuidTipoSolicitud = '$tipo'"
        Escribir-Resultado "el tipo se crea enlazado con la encargada academica y sin encargado de Julian (obtuvo $($r.Status), enlace $enlace, uuidUsuario $encargadoJulian)" (($r.Status -eq 200) -and ($enlace -eq "$tipoCa|$academico") -and ($encargadoJulian -eq "nulo"))

        $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/tipos-solicitud-consejo/$tipo" -Token $tokenRoot
        $leido = if ($r.Status -eq 200) { Leer-Json $r } else { $null }
        Escribir-Resultado "el enlace se consulta con su proceso y su encargada (obtuvo $($r.Status), $($leido.procesoAcademicoUuid), $($leido.encargadoAcademico.uuidUsuario))" (($r.Status -eq 200) -and ($leido.procesoAcademicoUuid -eq $tipoCa) -and ($leido.encargadoAcademico.uuidUsuario -eq $academico))
        $r = Invoke-Api -Metodo GET -Ruta "catalogos-academicos/tipos-solicitud-consejo/$([guid]::NewGuid().ToString())" -Token $tokenRoot
        Escribir-Resultado "un tipo sin enlace responde 204 (obtuvo $($r.Status))" ($r.Status -eq 204)

        $r = Invoke-Api -Metodo POST -Ruta "tipos/solicitudes" -Token $tokenRoot -Cuerpo (Cuerpo-Tipo "$nombreTipo segundo" $funcionarioJulian.Uuid $tipoCa)
        $segundo = [int](Ejecutar-Sql "select count(*) from tiposSolicitudes where nombre = '$nombreTipo segundo'")
        Escribir-Resultado "solo un tipo puede quedar enlazado: codigo 4 y no se crea el segundo (obtuvo $($r.Status) codigo $(Codigo-Error $r), tipos $segundo)" (($r.Status -ne 200) -and ((Codigo-Error $r) -eq "4") -and ($segundo -eq 0))

        $r = Invoke-Api -Metodo PUT -Ruta "tipos/solicitudes/$tipo" -Token $tokenRoot -Cuerpo (Cuerpo-Tipo $nombreTipo $funcionarioJulian.Uuid $null)
        $enlace = Ejecutar-Sql "select concat(TipoSolicitudAcademica_uuid, '|', coalesce(EncargadoAcademico_uuid, '')) from TIPO_SOLICITUD_CONSEJO where TipoSolicitud_uuid = '$tipo'"
        $encargadoJulian = Ejecutar-Sql "select coalesce(uuidUsuario, 'nulo') from tiposSolicitudes where uuidTipoSolicitud = '$tipo'"
        Escribir-Resultado "editar sin el campo conserva el enlace y con un Funcionario de Julian lo guarda en su columna (obtuvo $($r.Status), enlace $enlace, uuidUsuario $encargadoJulian)" (($r.Status -eq 200) -and ($enlace -eq "$tipoCa|") -and ($encargadoJulian -eq $funcionarioJulian.Uuid))
        $r = Invoke-Api -Metodo PUT -Ruta "tipos/solicitudes/$tipo" -Token $tokenRoot -Cuerpo (Cuerpo-Tipo $nombreTipo $academico $tipoCa)
        $enlace = Ejecutar-Sql "select concat(TipoSolicitudAcademica_uuid, '|', coalesce(EncargadoAcademico_uuid, '')) from TIPO_SOLICITUD_CONSEJO where TipoSolicitud_uuid = '$tipo'"
        $encargadoJulian = Ejecutar-Sql "select coalesce(uuidUsuario, 'nulo') from tiposSolicitudes where uuidTipoSolicitud = '$tipo'"
        Escribir-Resultado "volver a la encargada academica deja nulo el encargado de Julian (obtuvo $($r.Status), enlace $enlace, uuidUsuario $encargadoJulian)" (($r.Status -eq 200) -and ($enlace -eq "$tipoCa|$academico") -and ($encargadoJulian -eq "nulo"))

        $r = Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$sol/funcionario/remitir-consejo" -Token $tokenRoot -Cuerpo $remision
        Escribir-Resultado "el Secretario General no remite al Consejo (403, obtuvo $($r.Status))" ($r.Status -eq 403)
        $r = Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$sol/funcionario/remitir-consejo" -Token $tokenAcademico -Cuerpo $remision
        $detalle = if ($r.Status -eq 200) { Leer-Json $r } else { $null }
        Escribir-Resultado "el Funcionario Academico remite al Consejo y la solicitud queda EN_REVISION_CONSEJO (obtuvo $($r.Status), detalle $($detalle.solicitud.etapaCodigo), etapa $(Etapa-De $sol))" (($r.Status -eq 200) -and ($detalle.solicitud.etapaCodigo -eq "EN_REVISION_CONSEJO") -and ((Etapa-De $sol) -eq "EN_REVISION_CONSEJO"))
        $evaluada = Ejecutar-Sql "select concat(numeroFaltas, '|', nota) from ASIGNATURA_SOLICITUD_ACADEMICA where uuidAsignaturaSolicitud = '$fila'"
        $historial = Ejecutar-Sql "select accion from HISTORIAL_SOLICITUD_ACADEMICA where SolicitudAcademica_uuid = '$sol' and accion = 'REMITIR_CONSEJO'"
        Escribir-Resultado "la evaluacion queda guardada y el historial trae REMITIR_CONSEJO (obtuvo $evaluada, $historial)" (($evaluada -eq "2|3.5") -and ($historial -eq "REMITIR_CONSEJO"))

        $espejo = Ejecutar-Sql "select Solicitud_uuid from REMISION_CONSEJO where SolicitudAcademica_uuid = '$sol'"
        $datos = Ejecutar-Sql "select concat(s.estado, '|', s.uuidTipoSolicitud, '|', coalesce(s.uuidUsuario, 'nulo'), '|', s.consecutivo, '|', i.numeroDocumento, '|', i.correoElectronico, '|', coalesce(s.uuidOrdenDelDia, 'nulo')) from solicitudes s join informacionSolicitantes i on i.uuidInformacionSolicitante = s.uuidInformacionSolicitante where s.uuidSolicitud = '$espejo'"
        $esperado = "$Global:PendienteOrden|$tipo|nulo|$($radicada.radicado)|5$sufijo|rce$sufijo@unicauca.edu.co|nulo"
        Escribir-Resultado "la espejo queda pendiente al orden del dia con el tipo enlazado y los datos del estudiante (obtuvo $datos)" ($espejo -and ($datos -eq $esperado))
        $anexos = @(Ejecutar-Sql "select urlAnexo from anexos where uuidSolicitud = '$espejo' order by nombre")
        $carpetaEspejo = Join-Path $Global:CarpetaAnexos $espejo
        $enDisco = @($anexos | Where-Object { Test-Path (Join-Path $carpetaEspejo ($_.Substring($_.LastIndexOf("/") + 1))) }).Count
        Escribir-Resultado "la espejo tiene el formato firmado y el soporte copiados en su carpeta (obtuvo $($anexos.Count) anexos, $enDisco en disco)" (($anexos.Count -eq 2) -and ($enDisco -eq 2))
        $copiaFormato = @(Get-ChildItem $carpetaEspejo -File | Where-Object { $_.Length -eq (Get-Item $formato).Length }).Count
        Escribir-Resultado "el formato copiado es el que subio el estudiante (obtuvo $copiaFormato)" ($copiaFormato -eq 1)

        $r = Invoke-Api -Metodo GET -Ruta "solicitudes/estado?estado=$([uri]::EscapeDataString($Global:PendienteOrden))" -Token $tokenRoot
        $pendientes = if ($r.Status -eq 200) { @((Leer-Json $r) | ForEach-Object { $_.uuidSolicitud }) } else { @() }
        Escribir-Resultado "la espejo aparece en las pendientes del orden del dia de Julian (obtuvo $($r.Status), $($pendientes -contains $espejo))" (($r.Status -eq 200) -and ($pendientes -contains $espejo))

        $r = Invoke-Api -Metodo GET -Ruta "cancelaciones-asignatura/$sol" -Token $tokenEstudiante
        $vista = if ($r.Status -eq 200) { Leer-Json $r } else { $null }
        Escribir-Resultado "el estudiante ve EN_REVISION_CONSEJO (obtuvo $($r.Status), $($vista.solicitud.etapaCodigo))" (($r.Status -eq 200) -and ($vista.solicitud.etapaCodigo -eq "EN_REVISION_CONSEJO"))

        $r = Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$sol/funcionario/remitir-consejo" -Token $tokenAcademico -Cuerpo $remision
        $remisiones = Ejecutar-Sql "select count(*) from REMISION_CONSEJO where SolicitudAcademica_uuid = '$sol'"
        Escribir-Resultado "no se remite dos veces: codigo 4 y una sola espejo (obtuvo $($r.Status) codigo $(Codigo-Error $r), remisiones $remisiones)" (($r.Status -ne 200) -and ((Codigo-Error $r) -eq "4") -and ($remisiones -eq "1"))
        $r = Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$sol/funcionario/remitir" -Token $tokenAcademico -Cuerpo $remision
        Escribir-Resultado "ya en el Consejo no se puede remitir al Decano: codigo 4 (obtuvo $($r.Status) codigo $(Codigo-Error $r))" (($r.Status -ne 200) -and ((Codigo-Error $r) -eq "4"))

        $r = Invoke-Api -Metodo GET -Ruta "cancelaciones-asignatura/consejo/$([guid]::NewGuid().ToString())" -Token $tokenRoot
        Escribir-Resultado "una solicitud que no vino del Consejo no tiene detalle del Consejo (204, obtuvo $($r.Status))" ($r.Status -eq 204)
        $r = Invoke-Api -Metodo GET -Ruta "cancelaciones-asignatura/consejo/$espejo" -Token $tokenRoot
        $consejo = if ($r.Status -eq 200) { Leer-Json $r } else { $null }
        Escribir-Resultado "el Secretario General ve la espejo con su radicado, sus asignaturas y sin orden todavia (obtuvo $($r.Status), $($consejo.radicado), $(@($consejo.asignaturas).Count) asignaturas, orden $($consejo.ordenDelDia))" (($r.Status -eq 200) -and ($consejo.radicado -eq $radicada.radicado) -and (@($consejo.asignaturas).Count -eq 1) -and ($consejo.asignaturas[0].asignaturaSolicitudUuid -eq $fila) -and ($null -eq $consejo.ordenDelDia))
        $r = Invoke-Api -Metodo GET -Ruta "cancelaciones-asignatura/consejo/$espejo" -Token $tokenAcademico
        Escribir-Resultado "el Funcionario Academico no ve el detalle del Consejo (403, obtuvo $($r.Status))" ($r.Status -eq 403)

        $decision = @{ decisiones = @(@{ asignaturaSolicitudUuid = $fila; aprobada = $true }); consecutivoFiet = "T11-$sufijo" }
        $r = Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/consejo/$espejo/decision" -Token $decano.Token -Cuerpo $decision
        Escribir-Resultado "sin orden del dia no se registra la decision: codigo 4 y sigue EN_REVISION_CONSEJO (obtuvo $($r.Status) codigo $(Codigo-Error $r), etapa $(Etapa-De $sol))" (($r.Status -ne 200) -and ((Codigo-Error $r) -eq "4") -and ((Etapa-De $sol) -eq "EN_REVISION_CONSEJO"))

        $orden = [guid]::NewGuid().ToString()
        Ejecutar-Sql ("insert into ordenesDelDia (uuidOrdenDelDia, ciudad, descripcion, estado, fecha, fechaCreacion, horaFin, horaInicio, lugarReunion, nombre, numeroActa) " +
                "values ('$orden', 'Popayan', 'Orden T11', 1, '2026-11-20', now(), '10:00', '08:00', 'Sala T11', 'Orden T11 $sufijo', '045'); " +
                "update solicitudes set uuidOrdenDelDia = '$orden', estado = 'AGREGADO EN EL ORDEN DEL D$([char]0x00CD)A' where uuidSolicitud = '$espejo';") | Out-Null
        $r = Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/consejo/$espejo/decision" -Token $tokenAcademico -Cuerpo $decision
        Escribir-Resultado "el Funcionario Academico no registra la decision del Consejo (403, obtuvo $($r.Status))" ($r.Status -eq 403)
        $r = Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/consejo/$espejo/decision" -Token $decano.Token -Cuerpo @{ decisiones = @() }
        Escribir-Resultado "sin decisiones por asignatura no se registra nada: codigo 4 (obtuvo $($r.Status) codigo $(Codigo-Error $r))" (($r.Status -ne 200) -and ((Codigo-Error $r) -eq "4") -and ((Etapa-De $sol) -eq "EN_REVISION_CONSEJO"))
        $r = Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/consejo/$espejo/decision" -Token $decano.Token -Cuerpo $decision
        $decidida = if ($r.Status -eq 200) { Leer-Json $r } else { $null }
        Escribir-Resultado "el Decano registra la decision del Consejo y la solicitud queda APROBADA_POR_CONSEJO (obtuvo $($r.Status), $($decidida.etapaCodigo), $(Etapa-De $sol), aprobada $($decidida.asignaturas[0].aprobada))" (($r.Status -eq 200) -and ($decidida.etapaCodigo -eq "APROBADA_POR_CONSEJO") -and ((Etapa-De $sol) -eq "APROBADA_POR_CONSEJO") -and ($decidida.asignaturas[0].aprobada -eq $true))
        $respuestaJulian = Ejecutar-Sql "select concat(uuidRespuesta, '|', consecutivoFiet, '|', (tipoRespuesta like 'Resoluci%'), '|', (respuestaConsejo like 'El Consejo de Facultad en la sesi%n del 20 de noviembre de 2026, acta N% 045, aprueba la cancelaci%$($radicada.radicado)%')) from respuestas where uuidSolicitud = '$espejo'"
        $uuidRespuesta = ($respuestaJulian -split "\|")[0]
        Escribir-Resultado "la respuesta de Julian queda con el texto del Consejo, la sesion, el acta y el consecutivo (obtuvo $respuestaJulian)" ($respuestaJulian -eq "$uuidRespuesta|T11-$sufijo|1|1")
        $r = Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/consejo/$espejo/decision" -Token $tokenRoot -Cuerpo $decision
        Escribir-Resultado "la decision no se registra dos veces: codigo 4 (obtuvo $($r.Status) codigo $(Codigo-Error $r))" (($r.Status -ne 200) -and ((Codigo-Error $r) -eq "4"))
        $r = Invoke-Api -Metodo GET -Ruta "cancelaciones-asignatura/$sol" -Token $tokenEstudiante
        $vista = if ($r.Status -eq 200) { Leer-Json $r } else { $null }
        Escribir-Resultado "el estudiante sigue viendo EN_REVISION_CONSEJO tras la decision (obtuvo $($vista.solicitud.etapaCodigo))" ($vista.solicitud.etapaCodigo -eq "EN_REVISION_CONSEJO")

        $r = Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$sol/funcionario/resoluciones/borradores" -Token $tokenAcademico -Cuerpo @{}
        $borrador = if ($r.Status -eq 200) { @(Leer-Json $r)[0] } else { $null }
        Escribir-Resultado "el borrador de la Resolucion trae el texto del Consejo, la sesion, el acta y el consecutivo como numero (obtuvo $($r.Status), numero $($borrador.numero))" (($r.Status -eq 200) -and ($borrador.numero -eq "T11-$sufijo") -and ($borrador.preambulo -like "El Consejo de la Facultad*") -and ($borrador.considerandos -like "*el Consejo de la Facultad*20 de noviembre de 2026*Acta N*045*decide Aceptar*") -and ($borrador.articulosSiguientes -like "*reposici*ante el Consejo de la Facultad*"))

        $resolucionPdf = Nuevo-Pdf "resolucion del Consejo"; $archivos += $resolucionPdf
        $tipoFormatoDecano = Ejecutar-Sql "select t.uuidTipoAnexoAcademico from TIPO_ANEXO_ACADEMICO t where t.TipoSolicitudAcademica_uuid = '$tipoCa' and t.nombre = 'Formato firmado por el Decano'"
        $r1 = Ejecutar-Curl -Argumentos @("-F", "archivo=@$resolucionPdf;type=application/pdf") -Ruta "solicitudes-academicas/$sol/resolucion" -Token $tokenAcademico
        $r2 = Ejecutar-Curl -Argumentos @("-F", "archivo=@$formato;type=application/pdf", "-F", "tipoAnexo=$tipoFormatoDecano") -Ruta "solicitudes-academicas/$sol/anexos" -Token $tokenAcademico
        Escribir-Resultado "el Funcionario sube la Resolucion y el formato firmado por el Decano (obtuvo $($r1.Status) y $($r2.Status))" (($r1.Status -eq 200) -and ($r2.Status -eq 200))
        $situacionesCancelar = @(Ejecutar-Sql "select uuidSituacionAcademica from SITUACION_ACADEMICA_ASIGNATURA where ambito = 'CANCELACION' order by codigo")
        $ahora = [System.TimeZoneInfo]::ConvertTimeBySystemTimeZoneId([DateTime]::UtcNow, "SA Pacific Standard Time")
        $respuesta = @{
            mensaje = "Acerquese a la Decanatura T11"; enviarMensaje = $true
            ajustes = @(@{ asignaturaSolicitudUuid = $fila; situacionMatriculaUuid = $situaciones[0]; situacionCancelarUuid = $situacionesCancelar[0]; situacionNuevaMatriculaUuid = $situaciones[0] })
            programacion = @{ fechaProgramada = $ahora.AddHours(2).ToString("yyyy-MM-ddTHH:mm:ss"); numeroResolucion = "T11-1"; anioResolucion = $ahora.Year; decision = "aprueba la" }
        }
        $r = Invoke-Api -Metodo POST -Ruta "cancelaciones-asignatura/$sol/funcionario/responder" -Token $tokenAcademico -Cuerpo $respuesta
        Escribir-Resultado "el Funcionario envia la respuesta como despues del Decano y la solicitud queda APROBADA (obtuvo $($r.Status), etapa $(Etapa-De $sol))" (($r.Status -eq 200) -and ((Etapa-De $sol) -eq "APROBADA"))
        $cierre = Ejecutar-Sql "select concat(s.estado, '|', coalesce(r.urlRespuesta, 'nula')) from solicitudes s join respuestas r on r.uuidSolicitud = s.uuidSolicitud where s.uuidSolicitud = '$espejo'"
        $partes = $cierre -split "\|"
        $archivoRespuesta = if ($partes.Count -eq 2 -and $partes[1] -ne "nula") { Join-Path $Global:CarpetaBackend ("uploads\respuestas\$uuidRespuesta\" + $partes[1].Substring($partes[1].LastIndexOf("/") + 1)) } else { "" }
        $mismaResolucion = $archivoRespuesta -and (Test-Path $archivoRespuesta) -and ((Get-Item $archivoRespuesta).Length -eq (Get-Item $resolucionPdf).Length)
        Escribir-Resultado "la espejo queda RESPONDIDA con la Resolucion adjunta a su respuesta (obtuvo $($partes[0]), $($partes[1]), archivo $mismaResolucion)" (($partes[0] -eq "RESPONDIDA") -and ($partes[1] -like "*respuestas/$uuidRespuesta/*") -and $mismaResolucion)
        $r = Invoke-Api -Metodo GET -Ruta "respuestas/solicitud/$espejo" -Token $tokenRoot
        Escribir-Resultado "Julian ve la respuesta de la espejo con su archivo (obtuvo $($r.Status))" (($r.Status -eq 200) -and ((Leer-Json $r).urlRespuesta -like "*respuestas/$uuidRespuesta/*"))
    } else {
        Write-Host "Aviso: la base ya tiene un tipo enlazado al Consejo; no se crea otro ni se remite"
    }
}
finally {
    if ($responsableCa) {
        $r = Invoke-Api -Metodo PUT -Ruta "catalogos-academicos/tipos-solicitud/$tipoCa/funcionario" -Token $tokenRoot -Cuerpo @{ funcionarioUuid = $responsableCa }
        if ($r.Status -ne 200) { Write-Host "No se pudo restaurar el responsable: $($r.Status) $($r.Body)" }
    } else {
        Ejecutar-Sql "update TIPO_SOLICITUD_ACADEMICA set FuncionarioAcademico_uuid = null where uuidTipoSolicitudAcademica = '$tipoCa'" | Out-Null
    }
    $actual = Ejecutar-Sql "select coalesce(FuncionarioAcademico_uuid, '') from TIPO_SOLICITUD_ACADEMICA where uuidTipoSolicitudAcademica = '$tipoCa'"
    Escribir-Resultado "Cancelacion de Asignatura quedo con su responsable original" ($actual -eq $responsableCa)

    $sol = Lista-Sql $solicitudes
    $espejos = @()
    if ($sol) { $espejos = @(Ejecutar-Sql "select Solicitud_uuid from REMISION_CONSEJO where SolicitudAcademica_uuid in ($sol)") }
    $esp = Lista-Sql $espejos
    $tip = Lista-Sql $tiposCreados
    $informaciones = @()
    if ($esp) { $informaciones = @(Ejecutar-Sql "select uuidInformacionSolicitante from solicitudes where uuidSolicitud in ($esp)") }
    $inf = Lista-Sql $informaciones
    $sentencias = @()
    if ($sol) {
        $sentencias += "delete from REMISION_CONSEJO where SolicitudAcademica_uuid in ($sol)"
        $sentencias += "delete from NOTIFICACION_RESOLUCION where SolicitudAcademica_uuid in ($sol)"
        $sentencias += "delete from RESOLUCION_ACADEMICA where SolicitudAcademica_uuid in ($sol)"
        $sentencias += "delete from ANEXO_ACADEMICO where SolicitudAcademica_uuid in ($sol)"
        $sentencias += "delete from ASIGNATURA_SOLICITUD_ACADEMICA where SolicitudAcademica_uuid in ($sol)"
        $sentencias += "delete from SOLICITUD_CANCELACION_ASIGNATURA where SolicitudAcademica_uuid in ($sol)"
        $sentencias += "delete from HISTORIAL_SOLICITUD_ACADEMICA where SolicitudAcademica_uuid in ($sol)"
        $sentencias += "delete from SOLICITUD_ACADEMICA where uuidSolicitudAcademica in ($sol)"
    }
    if ($esp) {
        $sentencias += "delete from anexos where uuidSolicitud in ($esp)"
        $sentencias += "delete from respuestas where uuidSolicitud in ($esp)"
        $sentencias += "delete from solicitudes where uuidSolicitud in ($esp)"
    }
    if ($orden) { $sentencias += "delete from ordenesDelDia where uuidOrdenDelDia = '$orden'" }
    if ($inf) { $sentencias += "delete from informacionSolicitantes where uuidInformacionSolicitante in ($inf)" }
    if ($tip) {
        $sentencias += "delete from TIPO_SOLICITUD_CONSEJO where TipoSolicitud_uuid in ($tip)"
        $sentencias += "delete from tiposAnexos where uuidTipoSolicitud in ($tip)"
        $sentencias += "delete from tiposSolicitudes where uuidTipoSolicitud in ($tip)"
    }
    $nombresSql = Lista-Sql $usernames
    $usu = if ($nombresSql) { Lista-Sql @(Ejecutar-Sql "select uuidUsuario from usuarios where username in ($nombresSql)") } else { "" }
    $correosPrueba = Sentencia-CorreosPrueba $inicio $sol $usu
    if ($correosPrueba) { $sentencias = @($correosPrueba) + $sentencias }
    if ($sentencias.Count -gt 0) { Ejecutar-Sql ("start transaction; " + ($sentencias -join "; ") + "; commit;") | Out-Null }
    $usuariosBorrados = Limpiar-DatosPrueba -Desde $inicio -Usernames $usernames -CodigosAsignatura $codigos

    foreach ($uuid in @($solicitudes + $espejos)) {
        if (-not $uuid) { continue }
        $carpeta = Join-Path $Global:CarpetaAnexos $uuid
        if (Test-Path $carpeta) { Remove-Item -LiteralPath $carpeta -Recurse -Force }
    }
    if ($uuidRespuesta) {
        $carpetaRespuesta = Join-Path $Global:CarpetaRespuestas $uuidRespuesta
        if (Test-Path $carpetaRespuesta) { Remove-Item -LiteralPath $carpetaRespuesta -Recurse -Force }
    }
    if (-not $habiaRespuestas -and (Test-Path $Global:CarpetaRespuestas) -and (@(Get-ChildItem $Global:CarpetaRespuestas -Force).Count -eq 0)) {
        Remove-Item -LiteralPath $Global:CarpetaRespuestas -Force
    }
    if (-not $habiaAnexos -and (Test-Path $Global:CarpetaAnexos) -and (@(Get-ChildItem $Global:CarpetaAnexos -Force).Count -eq 0)) {
        Remove-Item -LiteralPath $Global:CarpetaAnexos -Force
    }
    foreach ($archivo in $archivos) { if (Test-Path $archivo) { Remove-Item -LiteralPath $archivo -Force } }

    $restos = 0
    if ($sol) { $restos += [int](Ejecutar-Sql "select count(*) from SOLICITUD_ACADEMICA where uuidSolicitudAcademica in ($sol)") }
    if ($esp) { $restos += [int](Ejecutar-Sql "select count(*) from solicitudes where uuidSolicitud in ($esp)") }
    if ($tip) { $restos += [int](Ejecutar-Sql "select count(*) from tiposSolicitudes where uuidTipoSolicitud in ($tip)") }
    if ($orden) { $restos += [int](Ejecutar-Sql "select count(*) from ordenesDelDia where uuidOrdenDelDia = '$orden'") }
    if ($esp) { $restos += [int](Ejecutar-Sql "select count(*) from respuestas where uuidSolicitud in ($esp)") }
    $restos += [int](Ejecutar-Sql "select count(*) from TIPO_SOLICITUD_CONSEJO") - $enlacesPrevios
    $carpetas = @(@($solicitudes + $espejos) | Where-Object { $_ -and (Test-Path (Join-Path $Global:CarpetaAnexos $_)) }).Count
    Escribir-Resultado "no quedaron solicitudes, espejos, tipos, enlaces ni carpetas de prueba (filas $restos, carpetas $carpetas, usuarios borrados $usuariosBorrados)" (($restos -eq 0) -and ($carpetas -eq 0) -and ($usuariosBorrados -eq $usernames.Count))
}
