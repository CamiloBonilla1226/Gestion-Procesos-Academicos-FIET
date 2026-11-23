. "$PSScriptRoot\comun.ps1"

$Global:CarpetaBackend = (Resolve-Path "$PSScriptRoot\..").Path
$Global:CarpetaAnexos = Join-Path $Global:CarpetaBackend "uploads\anexos"
$Global:Conteo = "solicitudes-academicas/funcionario/respuestas/conteo"
$Global:Borrar = "solicitudes-academicas/funcionario/respuestas"
$Global:Desde = "2001-03-01"
$Global:Hasta = "2001-03-31"

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

function Rango {
    param([string]$Desde = $Global:Desde, [string]$Hasta = $Global:Hasta)
    return "?desde=$Desde&hasta=$Hasta"
}

function Mensaje-De {
    param($Respuesta)
    try { return [string](Leer-Json $Respuesta).mensaje } catch { return "" }
}

function Cantidad-De {
    param($Respuesta)
    try { return [int](Leer-Json $Respuesta).cantidad } catch { return -1 }
}

function Nuevo-Estudiante {
    param([string]$Token)
    $s = Nuevo-Sufijo
    if (-not $script:programa) { $script:programa = Programa-PorSigla -Token $Token -Sigla "PIS" }
    $asignatura = Crear-AsignaturaPrueba -Token $Token -UuidPrograma $script:programa.uuidProgramaAcademico -Nombre "Materia T10 $s"
    $script:codigos += $asignatura.codigoAsignatura
    $cuerpo = @{
        nombres = "Borrable"; apellidos = "Prueba $s"; tipoDocumento = $Global:CedulaCiudadania; numeroDocumento = "6$s"
        telefono = "+573000000000"; correoElectronico = "t10$s@unicauca.edu.co"; username = "t10e$s"; password = "Clave12345"
        codigoEstudiantil = "B$s"; uuidProgramaAcademico = $script:programa.uuidProgramaAcademico; semestre = "5"
        asignaturas = @(@{ codigoAsignatura = $asignatura.codigoAsignatura; grupo = "A" })
    }
    $r = Invoke-Api -Metodo POST -Ruta "estudiantes" -Token $Token -Cuerpo $cuerpo
    if ($r.Status -ne 200) { throw "No se pudo crear el estudiante ($($r.Status)): $($r.Body)" }
    $uuid = (Leer-Json $r).uuidUsuario
    $script:usuarios += $uuid
    $script:estudiantes += $uuid
    return [pscustomobject]@{ Uuid = $uuid; Token = Iniciar-Sesion -Usuario "t10e$s" -Clave "Clave12345" }
}

function Nuevo-Funcionario {
    param([string]$Token)
    $s = Nuevo-Sufijo
    $cuerpo = @{
        nombres = "Depurador"; apellidos = "Prueba $s"; tipoDocumento = $Global:CedulaCiudadania; numeroDocumento = "3$s"
        telefono = "+573000000000"; correoElectronico = "t10f$s@unicauca.edu.co"; username = "t10f$s"; password = "Clave12345"; cargo = "T10 $s"
    }
    $r = Invoke-Api -Metodo POST -Ruta "funcionarios-academicos" -Token $Token -Cuerpo $cuerpo
    if ($r.Status -ne 200) { throw "No se pudo crear el funcionario academico ($($r.Status)): $($r.Body)" }
    $uuid = (Leer-Json $r).uuidUsuario
    $script:usuarios += $uuid
    $script:funcionarios += $uuid
    return [pscustomobject]@{ Uuid = $uuid; Token = Iniciar-Sesion -Usuario "t10f$s" -Clave "Clave12345" }
}

function Radicar {
    param($Estudiante)
    $form = Leer-Json (Invoke-Api -Metodo GET -Ruta "cancelaciones-asignatura/formulario" -Token $Estudiante.Token)
    $materia = @($form.asignaturas)[0].uuidAsignaturaMatriculada
    $salida = [System.IO.Path]::GetTempFileName()
    $codigo = & curl.exe -s -o $salida -w "%{http_code}" -X POST -F "motivo=Prueba T10" -F "asignaturas=$materia" -F "soporte=@$script:pdf;type=application/pdf" `
        -H "Authorization: Bearer $($Estudiante.Token)" ($Global:BaseUrl + "cancelaciones-asignatura")
    $cuerpo = [System.IO.File]::ReadAllText($salida)
    Remove-Item $salida -Force
    if ($codigo -ne "200") { throw "No se pudo radicar ($codigo): $cuerpo" }
    $uuid = ($cuerpo | ConvertFrom-Json).uuidSolicitudAcademica
    $script:solicitudes += $uuid
    return $uuid
}

function Cerrar {
    param([string]$Solicitud, [string]$Etapa, [string]$Fecha)
    Ejecutar-Sql ("update SOLICITUD_ACADEMICA set Etapa_uuid = (select uuidEtapa from ETAPA_SOLICITUD_ACADEMICA where codigo = '$Etapa' limit 1) " +
        "where uuidSolicitudAcademica = '$Solicitud'; update HISTORIAL_SOLICITUD_ACADEMICA set fecha = '$Fecha' where SolicitudAcademica_uuid = '$Solicitud'") | Out-Null
}

function Filas-De {
    param([string]$Solicitud)
    $tablas = @("ANEXO_ACADEMICO", "ASIGNATURA_SOLICITUD_ACADEMICA", "SOLICITUD_CANCELACION_ASIGNATURA", "HISTORIAL_SOLICITUD_ACADEMICA", "NOTIFICACION_RESOLUCION", "RESOLUCION_ACADEMICA")
    $conteos = @($tablas | ForEach-Object { "(select count(*) from $_ where SolicitudAcademica_uuid = '$Solicitud')" }) + @("(select count(*) from SOLICITUD_ACADEMICA where uuidSolicitudAcademica = '$Solicitud')")
    return [int](Ejecutar-Sql ("select " + ($conteos -join " + ")))
}

function Catalogo {
    return Ejecutar-Sql ("select concat_ws(' ', (select count(*) from TIPO_SOLICITUD_ACADEMICA), (select count(*) from ETAPA_SOLICITUD_ACADEMICA), " +
        "(select count(*) from ETAPA_ETIQUETA_ROL), (select count(*) from TIPO_ANEXO_ACADEMICO), (select count(*) from PROGRAMA_ACADEMICO), " +
        "(select count(*) from SITUACION_ACADEMICA_ASIGNATURA))")
}

$tokenRoot = Iniciar-Sesion -Usuario "rootfiet" -Clave "rootfiet1234"
$desdeCorrida = Inicio-Corrida
$fotoInicial = Foto-Base
$catalogoInicial = Catalogo
$tipos = @(Ejecutar-Sql "select uuidTipoSolicitudAcademica from TIPO_SOLICITUD_ACADEMICA")
$responsables = @{}
foreach ($tipo in $tipos) { $responsables[$tipo] = Ejecutar-Sql "select FuncionarioAcademico_uuid from TIPO_SOLICITUD_ACADEMICA where uuidTipoSolicitudAcademica = '$tipo'" }
$habiaAnexos = Test-Path $Global:CarpetaAnexos
$usuarios = @(); $estudiantes = @(); $funcionarios = @(); $codigos = @(); $solicitudes = @()
$pdf = Join-Path ([System.IO.Path]::GetTempPath()) ("t10_" + (Nuevo-Sufijo) + ".pdf")
[System.IO.File]::WriteAllBytes($pdf, [System.Text.Encoding]::ASCII.GetBytes("%PDF-1.4`n1 0 obj << /Type /Catalog >> endobj`n%%EOF`n"))

try {
    $ajenas = [int](Ejecutar-Sql ("select count(*) from SOLICITUD_ACADEMICA s join HISTORIAL_SOLICITUD_ACADEMICA h on h.SolicitudAcademica_uuid = s.uuidSolicitudAcademica " +
        "where h.fecha >= '2001-02-01' and h.fecha < '2001-05-01'"))
    if ($ajenas -ne 0) { Write-Host "FAIL hay solicitudes con historial en 2001; el script no corre para no borrar datos ajenos"; exit 1 }

    $fa = Nuevo-Funcionario -Token $tokenRoot
    $otro = Nuevo-Funcionario -Token $tokenRoot
    $decano = Crear-UsuarioPrueba -Perfil "Decano" -TokenAdmin $tokenRoot -Prefijo "dect10"
    $usuarios += $decano.Uuid
    foreach ($tipo in $tipos) {
        $r = Invoke-Api -Metodo PUT -Ruta "catalogos-academicos/tipos-solicitud/$tipo/funcionario" -Token $tokenRoot -Cuerpo @{ funcionarioUuid = $fa.Uuid }
        if ($r.Status -ne 200) { throw "No se pudo asignar el proceso ($($r.Status)): $($r.Body)" }
    }
    $est1 = Nuevo-Estudiante -Token $tokenRoot
    $est2 = Nuevo-Estudiante -Token $tokenRoot
    $est3 = Nuevo-Estudiante -Token $tokenRoot
    $cerrada = Radicar $est1
    $conComunicacion = Radicar $est2
    $enTramite = Radicar $est3
    Cerrar $cerrada "RECHAZADA" "2001-03-15 12:00:00"
    Cerrar $conComunicacion "APROBADA" "2001-03-10 12:00:00"
    Ejecutar-Sql "update HISTORIAL_SOLICITUD_ACADEMICA set fecha = '2001-03-12 12:00:00' where SolicitudAcademica_uuid = '$enTramite'" | Out-Null
    Ejecutar-Sql ("insert into NOTIFICACION_RESOLUCION (SolicitudAcademica_uuid, fechaProgramada, estado, firmada, numeroResolucion, anioResolucion, decision, intentos, fechaCreacion) " +
        "values ('$conComunicacion', '2099-01-01 08:00:00', 'PROGRAMADA', 0, 'T10', 2001, 'aprueba la', 0, now())") | Out-Null
    $carpeta = Join-Path $Global:CarpetaAnexos $cerrada
    Escribir-Resultado "la solicitud cerrada tiene su soporte en disco antes de borrar" (Test-Path $carpeta)

    foreach ($ruta in @($Global:Conteo, $Global:Borrar)) {
        $metodo = if ($ruta -eq $Global:Borrar) { "DELETE" } else { "GET" }
        Escribir-Resultado "$metodo $ruta sin token responde 401 (obtuvo $((Invoke-Api -Metodo $metodo -Ruta ($ruta + (Rango))).Status))" ((Invoke-Api -Metodo $metodo -Ruta ($ruta + (Rango))).Status -eq 401)
        foreach ($ajeno in @(@{ Nombre = "estudiante"; Token = $est1.Token }, @{ Nombre = "Decano"; Token = $decano.Token }, @{ Nombre = "Secretario General"; Token = $tokenRoot })) {
            $estado = (Invoke-Api -Metodo $metodo -Ruta ($ruta + (Rango)) -Token $ajeno.Token).Status
            Escribir-Resultado "$metodo $ruta del $($ajeno.Nombre) responde 403 (obtuvo $estado)" ($estado -eq 403)
        }
    }
    Escribir-Resultado "las solicitudes siguen completas tras los intentos sin permiso" ((Filas-De $cerrada) -gt 0)

    $r = Invoke-Api -Metodo GET -Ruta ($Global:Conteo + (Rango)) -Token $fa.Token
    Escribir-Resultado "el conteo cuenta solo la respuesta cerrada sin comunicacion pendiente, no la que esta en tramite ni la PROGRAMADA (obtuvo $($r.Status), $(Cantidad-De $r))" (($r.Status -eq 200) -and ((Cantidad-De $r) -eq 1))
    $r = Invoke-Api -Metodo GET -Ruta ($Global:Conteo + (Rango -Desde "2001-03-15" -Hasta "2001-03-15")) -Token $fa.Token
    Escribir-Resultado "el rango de un solo dia incluye la respuesta de ese dia (obtuvo $(Cantidad-De $r))" ((Cantidad-De $r) -eq 1)
    $r = Invoke-Api -Metodo GET -Ruta ($Global:Conteo + (Rango -Desde "2001-03-16" -Hasta "2001-03-31")) -Token $fa.Token
    Escribir-Resultado "fuera del rango no cuenta (obtuvo $(Cantidad-De $r))" ((Cantidad-De $r) -eq 0)
    $r = Invoke-Api -Metodo GET -Ruta ($Global:Conteo + (Rango)) -Token $otro.Token
    Escribir-Resultado "otro funcionario academico sin esos procesos no cuenta ni borra las respuestas ajenas (obtuvo $(Cantidad-De $r))" ((Cantidad-De $r) -eq 0)
    $r = Invoke-Api -Metodo DELETE -Ruta ($Global:Borrar + (Rango)) -Token $otro.Token
    Escribir-Resultado "el borrado de otro funcionario no elimina nada (obtuvo $($r.Status), $(Cantidad-De $r), filas $(Filas-De $cerrada))" (($r.Status -eq 200) -and ((Cantidad-De $r) -eq 0) -and ((Filas-De $cerrada) -gt 0))

    $manana = (([System.TimeZoneInfo]::ConvertTimeBySystemTimeZoneId([DateTime]::UtcNow, "SA Pacific Standard Time")).AddDays(1)).ToString("yyyy-MM-dd")
    foreach ($caso in @(
            @{ Nombre = "la fecha inicial posterior a la final"; Ruta = (Rango -Desde "2001-03-31" -Hasta "2001-03-01"); Texto = "*no puede ser posterior a la fecha final*" },
            @{ Nombre = "una fecha final futura"; Ruta = (Rango -Desde "2001-03-01" -Hasta $manana); Texto = "*posterior a hoy*" },
            @{ Nombre = "una fecha inicial futura"; Ruta = (Rango -Desde $manana -Hasta $manana); Texto = "*posterior a hoy*" },
            @{ Nombre = "sin fechas"; Ruta = ""; Texto = "*fecha inicial y la fecha final*" },
            @{ Nombre = "una fecha mal escrita"; Ruta = (Rango -Desde "01/03/2001"); Texto = "*AAAA-MM-DD*" })) {
        foreach ($metodo in @("GET", "DELETE")) {
            $ruta = if ($metodo -eq "GET") { $Global:Conteo } else { $Global:Borrar }
            $r = Invoke-Api -Metodo $metodo -Ruta ($ruta + $caso.Ruta) -Token $fa.Token
            Escribir-Resultado "$metodo con $($caso.Nombre) se rechaza en el servidor (obtuvo $($r.Status): $(Mensaje-De $r))" (($r.Status -ne 200) -and ((Mensaje-De $r) -like $caso.Texto))
        }
    }
    Escribir-Resultado "los rangos invalidos no borraron nada (filas $(Filas-De $cerrada))" ((Filas-De $cerrada) -gt 0)

    $r = Invoke-Api -Metodo DELETE -Ruta ($Global:Borrar + (Rango)) -Token $fa.Token
    Escribir-Resultado "el funcionario elimina la respuesta del rango y el servidor recalcula la cantidad (obtuvo $($r.Status), $(Cantidad-De $r))" (($r.Status -eq 200) -and ((Cantidad-De $r) -eq 1))
    Start-Sleep -Seconds 1
    Escribir-Resultado "de la solicitud eliminada no queda ninguna fila (obtuvo $(Filas-De $cerrada))" ((Filas-De $cerrada) -eq 0)
    Escribir-Resultado "su soporte se borro del disco" (-not (Test-Path $carpeta))
    Escribir-Resultado "la que esta en tramite y la de comunicacion PROGRAMADA siguen intactas" (((Filas-De $enTramite) -gt 0) -and ((Filas-De $conComunicacion) -gt 0))
    $matricula = Ejecutar-Sql "select count(*) from ASIGNATURA_MATRICULADA where Estudiante_uuid = '$($est1.Uuid)'"
    Escribir-Resultado "la asignatura matriculada del estudiante no se toca (obtuvo $matricula)" ($matricula -eq "1")
    $log = Ejecutar-Sql "select count(*) from logs where uuidUsuario = '$($fa.Uuid)' and accion = 'Eliminar respuestas de solicitudes acad$([char]0x00E9)micas'"
    Escribir-Resultado "el borrado queda en el log (obtuvo $log)" ($log -eq "1")
    $r = Invoke-Api -Metodo DELETE -Ruta ($Global:Borrar + (Rango)) -Token $fa.Token
    Escribir-Resultado "repetir el borrado no elimina nada mas (obtuvo $(Cantidad-De $r))" ((Cantidad-De $r) -eq 0)

    Ejecutar-Sql "update NOTIFICACION_RESOLUCION set estado = 'EN_PROCESO' where SolicitudAcademica_uuid = '$conComunicacion'" | Out-Null
    Escribir-Resultado "con la comunicacion EN_PROCESO no se cuenta (obtuvo $(Cantidad-De (Invoke-Api -Metodo GET -Ruta ($Global:Conteo + (Rango)) -Token $fa.Token)))" ((Cantidad-De (Invoke-Api -Metodo GET -Ruta ($Global:Conteo + (Rango)) -Token $fa.Token)) -eq 0)
    Ejecutar-Sql "update NOTIFICACION_RESOLUCION set estado = 'FALLIDA' where SolicitudAcademica_uuid = '$conComunicacion'" | Out-Null
    Escribir-Resultado "con la comunicacion FALLIDA tampoco (obtuvo $(Cantidad-De (Invoke-Api -Metodo GET -Ruta ($Global:Conteo + (Rango)) -Token $fa.Token)))" ((Cantidad-De (Invoke-Api -Metodo GET -Ruta ($Global:Conteo + (Rango)) -Token $fa.Token)) -eq 0)
    Ejecutar-Sql "update NOTIFICACION_RESOLUCION set estado = 'ENVIADA' where SolicitudAcademica_uuid = '$conComunicacion'" | Out-Null
    $r = Invoke-Api -Metodo DELETE -Ruta ($Global:Borrar + (Rango)) -Token $fa.Token
    Escribir-Resultado "con la comunicacion ENVIADA se elimina con su notificacion (obtuvo $(Cantidad-De $r), filas $(Filas-De $conComunicacion))" (((Cantidad-De $r) -eq 1) -and ((Filas-De $conComunicacion) -eq 0))
    Escribir-Resultado "la que esta en tramite sigue intacta al final" ((Filas-De $enTramite) -gt 0)
    Escribir-Resultado "el catalogo de procesos, etapas, etiquetas, tipos de anexo, programas y situaciones no cambia (antes $catalogoInicial, despues $(Catalogo))" ((Catalogo) -eq $catalogoInicial)
}
finally {
    foreach ($tipo in $tipos) {
        Invoke-Api -Metodo PUT -Ruta "catalogos-academicos/tipos-solicitud/$tipo/funcionario" -Token $tokenRoot -Cuerpo @{ funcionarioUuid = $responsables[$tipo] } | Out-Null
    }
    $restaurados = @($tipos | Where-Object { (Ejecutar-Sql "select FuncionarioAcademico_uuid from TIPO_SOLICITUD_ACADEMICA where uuidTipoSolicitudAcademica = '$_'") -ne $responsables[$_] })
    Escribir-Resultado "los procesos quedaron con su responsable original" ($restaurados.Count -eq 0)
    $sol = Lista-Sql $solicitudes
    $sentencias = @()
    if ($sol) {
        foreach ($tabla in @("NOTIFICACION_RESOLUCION", "ANEXO_ACADEMICO", "RESOLUCION_ACADEMICA", "ASIGNATURA_SOLICITUD_ACADEMICA", "SOLICITUD_CANCELACION_ASIGNATURA", "HISTORIAL_SOLICITUD_ACADEMICA")) {
            $sentencias += "delete from $tabla where SolicitudAcademica_uuid in ($sol)"
        }
        $sentencias += "delete from SOLICITUD_ACADEMICA where uuidSolicitudAcademica in ($sol)"
    }
    if ($sentencias.Count -gt 0) { Ejecutar-Sql ("start transaction; " + ($sentencias -join "; ") + "; commit;") | Out-Null }
    foreach ($uuid in $solicitudes) {
        $c = Join-Path $Global:CarpetaAnexos $uuid
        if (Test-Path $c) { Remove-Item -LiteralPath $c -Recurse -Force }
    }
    if (-not $habiaAnexos -and (Test-Path $Global:CarpetaAnexos) -and (@(Get-ChildItem $Global:CarpetaAnexos -Force).Count -eq 0)) { Remove-Item -LiteralPath $Global:CarpetaAnexos -Force }
    $usernames = @(Ejecutar-Sql ("select username from usuarios where uuidUsuario in (" + (Lista-Sql $usuarios) + ")"))
    Limpiar-DatosPrueba -Desde $desdeCorrida -Usernames $usernames -CodigosAsignatura $codigos | Out-Null
    if (Test-Path $pdf) { Remove-Item -LiteralPath $pdf -Force }
    $fotoFinal = Foto-Base
    Escribir-Resultado "la base quedo como estaba (antes: $fotoInicial; despues: $fotoFinal)" ($fotoFinal -eq $fotoInicial)
}
