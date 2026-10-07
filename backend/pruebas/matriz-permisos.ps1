. "$PSScriptRoot\comun.ps1"

$Global:CarpetaBackend = (Resolve-Path "$PSScriptRoot\..").Path
$Global:CarpetaUploads = Join-Path $Global:CarpetaBackend "uploads"
$Global:Fuentes = Join-Path $Global:CarpetaBackend "solicitudes\src\main\java\com\unicauca\cfiet\solicitudes"
$Global:Seguridad = Join-Path $Global:Fuentes "infraestructura\configuracion\seguridad\configuracion\ConfiguracionSeguridad.java"
$Global:Pasan = 0
$Global:Fallan = 0

$Global:PrefijosJulian = @("usuarios", "respuestas", "logs", "tipos/solicitudes", "roles", "solicitudes", "sesiones")
$Global:PermitAllJulian = @(
    'baseUrl + "sesiones"',
    'HttpMethod.POST, baseUrl + "solicitudes/public"',
    'HttpMethod.GET, baseUrl + "tipos/solicitudes/perfil"',
    'HttpMethod.GET, baseUrl + "tipos/solicitudes/perfil/paginado"',
    'HttpMethod.GET, baseUrl + "tipos/solicitudes/perfil/filtro"',
    'HttpMethod.GET, baseUrl + "tipos/solicitudes/{uuidTipoSolicitud}"'
)

$Global:Perfiles["SecretarioGeneral"] = @{
    TipoUsuario = "SECRETARIO"
    Rol         = @{ uuidRol = "0f76e194-eda2-49c3-adb0-c16abcfbfa4d"; nombre = "Secretario General"; descripcion = "Administrador del sistema"; estado = $true }
    Tipo        = @{ uuidTipoUsuario = "cf4858d4-77b4-4e70-ac47-bc650a34584b"; nombre = "Empleado FIET - Secretario General" }
}

$Global:MatrizTexto = @'
METODO RUTA                                                                     CUERPO    SIN_TOKEN SECRETARIO DECANO    FUNC_ACADEMICO ESTUDIANTE
POST   asignaturas                                                              json      denegado  permitido  permitido denegado       denegado
GET    asignaturas/paginado                                                     ninguno   denegado  permitido  permitido permitido      denegado
GET    asignaturas/filtro                                                       ninguno   denegado  permitido  permitido permitido      denegado
GET    asignaturas/{uuidAsignatura}                                             ninguno   denegado  permitido  permitido permitido      denegado
PUT    asignaturas/{uuidAsignatura}                                             json      denegado  permitido  permitido denegado       denegado
POST   estudiantes/cargar/archivo                                               multipart denegado  permitido  permitido denegado       denegado
POST   estudiantes                                                              json      denegado  permitido  permitido denegado       denegado
GET    estudiantes/paginado                                                     ninguno   denegado  permitido  permitido permitido      denegado
GET    estudiantes/filtro                                                       ninguno   denegado  permitido  permitido permitido      denegado
GET    estudiantes/mis-asignaturas                                              ninguno   denegado  denegado   denegado  denegado       permitido
GET    estudiantes/{uuidEstudiante}                                             ninguno   denegado  permitido  permitido permitido      denegado
PUT    estudiantes/{uuidEstudiante}                                             json      denegado  permitido  permitido denegado       denegado
POST   estudiantes/{uuidEstudiante}/asignaturas                                 json      denegado  permitido  permitido denegado       denegado
PATCH  estudiantes/{uuidEstudiante}/asignaturas/{uuidMatricula}/estado          json      denegado  permitido  permitido denegado       denegado
POST   funcionarios-academicos                                                  json      denegado  permitido  permitido denegado       denegado
POST   funcionarios-academicos/cargar/archivo                                   multipart denegado  permitido  permitido denegado       denegado
GET    funcionarios-academicos/paginado                                         ninguno   denegado  permitido  permitido permitido      denegado
GET    funcionarios-academicos/filtro                                           ninguno   denegado  permitido  permitido permitido      denegado
GET    funcionarios-academicos/{uuidFuncionario}                                ninguno   denegado  permitido  permitido permitido      denegado
PUT    funcionarios-academicos/{uuidFuncionario}                                json      denegado  permitido  permitido denegado       denegado
GET    catalogos-academicos/tipos-solicitud                                     ninguno   denegado  permitido  permitido permitido      permitido
GET    catalogos-academicos/tipos-solicitud/{uuidTipo}/etapas                   ninguno   denegado  permitido  permitido permitido      permitido
GET    catalogos-academicos/tipos-solicitud/{uuidTipo}/tipos-anexo              ninguno   denegado  permitido  permitido permitido      permitido
GET    catalogos-academicos/etiquetas                                           ninguno   denegado  permitido  permitido permitido      permitido
GET    catalogos-academicos/situaciones                                         ninguno   denegado  permitido  permitido permitido      permitido
PUT    catalogos-academicos/tipos-solicitud/{uuidTipo}/funcionario              json      denegado  permitido  permitido denegado       denegado
GET    solicitudes-academicas/estudiante                                        ninguno   denegado  denegado   denegado  denegado       permitido
GET    solicitudes-academicas/funcionario                                       ninguno   denegado  denegado   denegado  permitido      denegado
GET    solicitudes-academicas/decano                                            ninguno   denegado  denegado   permitido denegado       denegado
GET    solicitudes-academicas/{uuidSolicitud}                                   ninguno   denegado  denegado   permitido permitido      permitido
GET    solicitudes-academicas/{uuidSolicitud}/historial                         ninguno   denegado  denegado   permitido permitido      permitido
POST   solicitudes-academicas/{uuidSolicitud}/anexos                            multipart denegado  denegado   denegado  permitido      permitido
GET    solicitudes-academicas/{uuidSolicitud}/anexos/{uuidAnexo}                ninguno   denegado  denegado   permitido permitido      permitido
POST   solicitudes-academicas/{uuidSolicitud}/resolucion                        multipart denegado  denegado   denegado  permitido      denegado
GET    solicitudes-academicas/{uuidSolicitud}/resolucion                        ninguno   denegado  denegado   permitido permitido      permitido
GET    cancelaciones-matricula/formulario                                       ninguno   denegado  denegado   denegado  denegado       permitido
POST   cancelaciones-matricula                                                  multipart denegado  denegado   denegado  denegado       permitido
GET    cancelaciones-matricula/{uuidSolicitud}                                  ninguno   denegado  denegado   permitido permitido      permitido
POST   cancelaciones-matricula/{uuidSolicitud}/funcionario/rechazar             json      denegado  denegado   denegado  permitido      denegado
POST   cancelaciones-matricula/{uuidSolicitud}/funcionario/remitir              json      denegado  denegado   denegado  permitido      denegado
POST   cancelaciones-matricula/{uuidSolicitud}/funcionario/responder            ninguno   denegado  denegado   denegado  permitido      denegado
POST   cancelaciones-matricula/{uuidSolicitud}/decano/aprobar                   json      denegado  denegado   permitido denegado       denegado
POST   cancelaciones-matricula/{uuidSolicitud}/decano/rechazar                  json      denegado  denegado   permitido denegado       denegado
GET    cancelaciones-asignatura/formulario                                      ninguno   denegado  denegado   denegado  denegado       permitido
POST   cancelaciones-asignatura                                                 multipart denegado  denegado   denegado  denegado       permitido
GET    cancelaciones-asignatura/{uuidSolicitud}                                 ninguno   denegado  denegado   permitido permitido      permitido
POST   cancelaciones-asignatura/{uuidSolicitud}/funcionario/rechazar            json      denegado  denegado   denegado  permitido      denegado
POST   cancelaciones-asignatura/{uuidSolicitud}/funcionario/remitir             json      denegado  denegado   denegado  permitido      denegado
POST   cancelaciones-asignatura/{uuidSolicitud}/funcionario/responder           ninguno   denegado  denegado   denegado  permitido      denegado
POST   cancelaciones-asignatura/{uuidSolicitud}/decano/aprobar                  json      denegado  denegado   permitido denegado       denegado
POST   cancelaciones-asignatura/{uuidSolicitud}/decano/rechazar                 json      denegado  denegado   permitido denegado       denegado
GET    examenes-supletorios/formulario                                          ninguno   denegado  denegado   denegado  denegado       permitido
POST   examenes-supletorios                                                     multipart denegado  denegado   denegado  denegado       permitido
GET    examenes-supletorios/{uuidSolicitud}                                     ninguno   denegado  denegado   permitido permitido      permitido
POST   examenes-supletorios/{uuidSolicitud}/funcionario/rechazar                json      denegado  denegado   denegado  permitido      denegado
POST   examenes-supletorios/{uuidSolicitud}/funcionario/remitir                 json      denegado  denegado   denegado  permitido      denegado
POST   examenes-supletorios/{uuidSolicitud}/funcionario/responder               ninguno   denegado  denegado   denegado  permitido      denegado
POST   examenes-supletorios/{uuidSolicitud}/funcionario/recibo                  ninguno   denegado  denegado   denegado  permitido      denegado
POST   examenes-supletorios/{uuidSolicitud}/funcionario/comprobante/aprobar     json      denegado  denegado   denegado  permitido      denegado
POST   examenes-supletorios/{uuidSolicitud}/funcionario/comprobante/rechazar    json      denegado  denegado   denegado  permitido      denegado
POST   examenes-supletorios/{uuidSolicitud}/decano/aprobar                      json      denegado  denegado   permitido denegado       denegado
POST   examenes-supletorios/{uuidSolicitud}/decano/rechazar                     json      denegado  denegado   permitido denegado       denegado
POST   examenes-supletorios/{uuidSolicitud}/estudiante/comprobante              ninguno   denegado  denegado   denegado  denegado       permitido
'@

$Global:Columnas = @("SIN_TOKEN", "SECRETARIO", "DECANO", "FUNC_ACADEMICO", "ESTUDIANTE")
$Global:NombresColumna = @{ SIN_TOKEN = "sin token"; SECRETARIO = "Secretario General"; DECANO = "Decano"; FUNC_ACADEMICO = "Funcionario Academico"; ESTUDIANTE = "Estudiante" }

function Verificar {
    param([string]$Descripcion, [bool]$Ok)
    Escribir-Resultado $Descripcion $Ok
    if ($Ok) { $Global:Pasan++ } else { $Global:Fallan++ }
}

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

function Leer-Matriz {
    $filas = @()
    $lineas = @($Global:MatrizTexto -split "`r?`n" | Where-Object { $_.Trim() })
    foreach ($linea in ($lineas | Select-Object -Skip 1)) {
        $partes = @($linea.Trim() -split "\s+")
        $permisos = @{}
        for ($i = 0; $i -lt $Global:Columnas.Count; $i++) { $permisos[$Global:Columnas[$i]] = $partes[3 + $i] }
        $filas += [pscustomobject]@{ Metodo = $partes[0]; Ruta = $partes[1]; Cuerpo = $partes[2]; Permisos = $permisos; Linea = $linea }
    }
    return $filas
}

function Normalizar-Ruta {
    param([string]$Ruta)
    return ([regex]::Replace($Ruta, "\{\w+(:[^/]*)?\}", "{}")).TrimEnd("/")
}

function Rutas-Del-Codigo {
    $rutas = @()
    $errores = @()
    $controladores = Get-ChildItem (Join-Path $Global:Fuentes "infraestructura\input") -Recurse -Filter "*RestController.java"
    foreach ($archivo in $controladores) {
        $texto = [System.IO.File]::ReadAllText($archivo.FullName)
        $clase = [regex]::Match($texto, '@RequestMapping\("\$\{url\.application\}([^"]*)"\)')
        if (-not $clase.Success) { $errores += "$($archivo.Name) sin @RequestMapping de clase"; continue }
        $prefijo = $clase.Groups[1].Value
        if ($Global:PrefijosJulian -contains $prefijo) { continue }
        if ([regex]::Matches($texto, '@RequestMapping\(').Count -ne 1) { $errores += "$($archivo.Name) tiene @RequestMapping en metodos" }
        $constantes = @{}
        foreach ($c in [regex]::Matches($texto, 'private static final String (\w+)\s*=\s*"([^"]*)";')) { $constantes[$c.Groups[1].Value] = $c.Groups[2].Value }
        foreach ($m in [regex]::Matches($texto, '@(Get|Post|Put|Patch|Delete)Mapping(\(([^)]*)\))?')) {
            $argumento = $m.Groups[3].Value
            $argumento = [regex]::Replace($argumento, ',?\s*(consumes|produces)\s*=\s*[\w.]+', "")
            $argumento = [regex]::Replace($argumento.Trim(), '^(value|path)\s*=\s*', "").Trim().TrimEnd(",").Trim()
            $ruta = ""
            if ($argumento) {
                foreach ($parte in ($argumento -split "\+")) {
                    $parte = $parte.Trim()
                    if ($parte.StartsWith('"')) { $ruta += $parte.Trim('"') }
                    elseif ($constantes.ContainsKey($parte)) { $ruta += $constantes[$parte] }
                    else { $errores += "$($archivo.Name): no se pudo resolver '$parte'" }
                }
            }
            $rutas += [pscustomobject]@{ Prefijo = $prefijo; Clave = $m.Groups[1].Value.ToUpper() + " " + (Normalizar-Ruta ($prefijo + $ruta)) }
        }
    }
    return [pscustomobject]@{ Rutas = $rutas; Errores = $errores }
}

function Llamar {
    param([string]$Metodo, [string]$Ruta, [string]$Cuerpo, [string]$Token)
    $salida = [System.IO.Path]::GetTempFileName()
    $argumentos = @("-s", "-o", $salida, "-w", "%{http_code}", "-X", $Metodo)
    if ($Token) { $argumentos += @("-H", "Authorization: Bearer $Token") }
    if ($Cuerpo -eq "json") { $argumentos += @("-H", "Content-Type: application/json", "--data-binary", "{") }
    if ($Cuerpo -eq "multipart") { $argumentos += @("-F", "campo=sin-archivo") }
    $argumentos += ($Global:BaseUrl + $Ruta)
    $codigo = & curl.exe @argumentos
    $cuerpo = [System.IO.File]::ReadAllText($salida, [System.Text.Encoding]::UTF8)
    Remove-Item $salida -Force
    return [pscustomobject]@{ Status = [int]$codigo; Body = $cuerpo }
}

function Resumen-Error {
    param($Respuesta)
    if (-not $Respuesta.Body) { return "sin cuerpo" }
    try {
        $e = $Respuesta.Body | ConvertFrom-Json
        if ($e -isnot [array] -and $null -ne $e.codigoError) {
            $mensaje = [string]$e.mensaje
            return "$($e.codigoError) " + $mensaje.Substring(0, [Math]::Min(90, $mensaje.Length))
        }
        return "respuesta valida"
    } catch {
        return "respuesta valida"
    }
}

function Ruta-Concreta {
    param([string]$Ruta)
    return [regex]::Replace($Ruta, "\{\w+\}", { param($x) [guid]::NewGuid().ToString() })
}

function Archivos-Subidos {
    if (-not (Test-Path $Global:CarpetaUploads)) { return 0 }
    return @(Get-ChildItem $Global:CarpetaUploads -Recurse -File -Force).Count
}

function Conteos-Tramite {
    $sql = "select concat_ws(' ', (select count(*) from SOLICITUD_ACADEMICA), (select count(*) from ANEXO_ACADEMICO), (select count(*) from RESOLUCION_ACADEMICA), (select count(*) from HISTORIAL_SOLICITUD_ACADEMICA))"
    return "solicitudes/anexos/resoluciones/historial=" + (Ejecutar-Sql $sql) + " archivos=" + (Archivos-Subidos)
}

function Conteos-Datos {
    $sql = "select concat_ws(' ', (select count(*) from usuarios), (select count(*) from ASIGNATURA), (select count(*) from ASIGNATURA_MATRICULADA), (select count(*) from ESTUDIANTE), (select count(*) from FUNCIONARIO_ACADEMICO), " +
            "(select coalesce(group_concat(coalesce(FuncionarioAcademico_uuid, '-') order by uuidTipoSolicitudAcademica), '') from TIPO_SOLICITUD_ACADEMICA))"
    return "usuarios/asignaturas/matriculadas/estudiantes/funcionarios/responsables=" + (Ejecutar-Sql $sql)
}

function Nuevo-Estudiante {
    param([string]$Token)
    $s = Nuevo-Sufijo
    $cuerpo = @{
        nombres           = "Matriz"
        apellidos         = "Permisos $s"
        tipoDocumento     = $Global:CedulaCiudadania
        numeroDocumento   = "6$s"
        telefono          = "+573000000000"
        correoElectronico = "mpe$s@unicauca.edu.co"
        username          = "mpe$s"
        password          = "Clave12345"
        codigoEstudiantil = "M$s"
        programaAcademico = "Ingenieria de Sistemas"
        semestre          = "5"
        facultad          = "FIET"
        asignaturas       = @(@{ codigoAsignatura = "MP$s"; nombreAsignatura = "Materia matriz $s"; grupo = "A" })
    }
    $r = Invoke-Api -Metodo POST -Ruta "estudiantes" -Token $Token -Cuerpo $cuerpo
    if ($r.Status -ne 200) { throw "No se pudo crear el estudiante ($($r.Status)): $($r.Body)" }
    return [pscustomobject]@{
        Uuid   = (Leer-Json $r).uuidUsuario
        Codigo = "MP$s"
        Token  = Iniciar-Sesion -Usuario "mpe$s" -Clave "Clave12345"
    }
}

function Nuevo-Funcionario {
    param([string]$Token)
    $s = Nuevo-Sufijo
    $cuerpo = @{
        nombres           = "Matriz"
        apellidos         = "Funcionario $s"
        tipoDocumento     = $Global:CedulaCiudadania
        numeroDocumento   = "3$s"
        telefono          = "+573000000000"
        correoElectronico = "mpf$s@unicauca.edu.co"
        username          = "mpf$s"
        password          = "Clave12345"
        dependencia       = "DepMatriz $s"
    }
    $r = Invoke-Api -Metodo POST -Ruta "funcionarios-academicos" -Token $Token -Cuerpo $cuerpo
    if ($r.Status -ne 200) { throw "No se pudo crear el funcionario academico ($($r.Status)): $($r.Body)" }
    return [pscustomobject]@{
        Uuid  = (Leer-Json $r).uuidUsuario
        Token = Iniciar-Sesion -Usuario "mpf$s" -Clave "Clave12345"
    }
}

$matriz = Leer-Matriz
Write-Host "Matriz esperada de permisos (permitido / denegado):"
foreach ($linea in @($Global:MatrizTexto -split "`r?`n" | Where-Object { $_.Trim() })) { Write-Host "  $linea" }
Write-Host ""

$codigo = Rutas-Del-Codigo
foreach ($e in $codigo.Errores) { Verificar "lectura de controladores: $e" $false }
$clavesCodigo = @($codigo.Rutas | ForEach-Object { $_.Clave } | Sort-Object -Unique)
$clavesMatriz = @($matriz | ForEach-Object { $_.Metodo + " " + (Normalizar-Ruta $_.Ruta) } | Sort-Object -Unique)
$prefijosNuevos = @($codigo.Rutas | ForEach-Object { $_.Prefijo } | Sort-Object -Unique)
Verificar "la matriz no repite filas ($($matriz.Count) filas, $($clavesMatriz.Count) distintas)" ($matriz.Count -eq $clavesMatriz.Count)
Verificar "los controladores nuevos declaran $($codigo.Rutas.Count) rutas en $($prefijosNuevos.Count) prefijos ($($prefijosNuevos -join ', '))" ($codigo.Rutas.Count -eq $clavesCodigo.Count -and $clavesCodigo.Count -gt 0)
foreach ($clave in $clavesCodigo) { if ($clavesMatriz -notcontains $clave) { Verificar "la ruta del codigo $clave esta en la matriz" $false } }
foreach ($clave in $clavesMatriz) { if ($clavesCodigo -notcontains $clave) { Verificar "la ruta de la matriz $clave existe en el codigo" $false } }
$faltanEnMatriz = @($clavesCodigo | Where-Object { $clavesMatriz -notcontains $_ }).Count
$sobranEnMatriz = @($clavesMatriz | Where-Object { $clavesCodigo -notcontains $_ }).Count
Verificar "las rutas de la matriz coinciden con las del codigo (faltan $faltanEnMatriz, sobran $sobranEnMatriz)" (($faltanEnMatriz -eq 0) -and ($sobranEnMatriz -eq 0))

$seguridad = [System.IO.File]::ReadAllText($Global:Seguridad)
$permitAll = @([regex]::Matches($seguridad, '\.requestMatchers\(([^)]*)\)\s*\.permitAll\(\)') | ForEach-Object { ($_.Groups[1].Value -replace "\s+", " ").Trim() })
foreach ($regla in $permitAll) { if ($Global:PermitAllJulian -notcontains $regla) { Verificar "permitAll nuevo fuera de los de Julian: $regla" $false } }
$totalPermitAll = 0
foreach ($fuente in (Get-ChildItem $Global:Fuentes -Recurse -Filter "*.java")) { $totalPermitAll += [regex]::Matches([System.IO.File]::ReadAllText($fuente.FullName), 'permitAll').Count }
Verificar "solo existen los $($Global:PermitAllJulian.Count) permitAll de Julian (reglas permitAll $($permitAll.Count), apariciones en el codigo $totalPermitAll)" (($permitAll.Count -eq $Global:PermitAllJulian.Count) -and ($totalPermitAll -eq $Global:PermitAllJulian.Count) -and (@($permitAll | Where-Object { $Global:PermitAllJulian -notcontains $_ }).Count -eq 0))
foreach ($prefijo in $prefijosNuevos) {
    $tieneDeny = $seguridad.Contains(".requestMatchers(baseUrl + `"$prefijo/**`").denyAll()")
    Verificar "ConfiguracionSeguridad cierra $prefijo/** con denyAll" $tieneDeny
}

$tokenRoot = Iniciar-Sesion -Usuario "rootfiet" -Clave "rootfiet1234"
$tramiteAntes = Conteos-Tramite
$datosAntes = Conteos-Datos
$habiaAnexos = Test-Path (Join-Path $Global:CarpetaUploads "anexos")

$usuarios = @()
$estudiantes = @()
$funcionarios = @()
$codigos = @()

try {
    $secretario = Crear-UsuarioPrueba -Perfil "SecretarioGeneral" -TokenAdmin $tokenRoot -Prefijo "sgmp"
    $usuarios += $secretario.Uuid
    $decano = Crear-UsuarioPrueba -Perfil "Decano" -TokenAdmin $tokenRoot -Prefijo "decmp"
    $usuarios += $decano.Uuid
    $funcionario = Nuevo-Funcionario -Token $tokenRoot
    $usuarios += $funcionario.Uuid; $funcionarios += $funcionario.Uuid
    $estudiante = Nuevo-Estudiante -Token $tokenRoot
    $usuarios += $estudiante.Uuid; $estudiantes += $estudiante.Uuid; $codigos += $estudiante.Codigo

    $tokens = @{ SIN_TOKEN = ""; SECRETARIO = $secretario.Token; DECANO = $decano.Token; FUNC_ACADEMICO = $funcionario.Token; ESTUDIANTE = $estudiante.Token }

    foreach ($fila in $matriz) {
        $ruta = Ruta-Concreta $fila.Ruta
        foreach ($columna in $Global:Columnas) {
            $nombre = $Global:NombresColumna[$columna]
            $esperado = $fila.Permisos[$columna]
            $r = Llamar -Metodo $fila.Metodo -Ruta $ruta -Cuerpo $fila.Cuerpo -Token $tokens[$columna]
            $detalle = ""
            if ($esperado -eq "permitido") {
                $sinRuta = ($r.Status -eq 404) -or ($r.Body -match "No static resource|NoResourceFound|No endpoint")
                $ok = ($r.Status -ne 401) -and ($r.Status -ne 403) -and ($r.Status -ne 0) -and -not $sinRuta
                $detalle = " " + (Resumen-Error $r)
            } elseif ($columna -eq "SIN_TOKEN") {
                $ok = $r.Status -eq 401
            } else {
                $ok = $r.Status -eq 403
            }
            if (-not $ok -and $esperado -ne "permitido" -and $r.Body) { $detalle = " " + $r.Body.Substring(0, [Math]::Min(160, $r.Body.Length)) }
            Verificar "$($fila.Metodo) $($fila.Ruta) [$nombre] $esperado (obtuvo $($r.Status))$detalle" $ok
        }
    }

    foreach ($prefijo in $prefijosNuevos) {
        foreach ($prueba in @(@{ Metodo = "DELETE"; Ruta = "$prefijo/ruta-no-declarada" }, @{ Metodo = "POST"; Ruta = "$prefijo/ruta-no-declarada/x" })) {
            foreach ($columna in $Global:Columnas) {
                $esperado = if ($columna -eq "SIN_TOKEN") { 401 } else { 403 }
                $r = Llamar -Metodo $prueba.Metodo -Ruta $prueba.Ruta -Cuerpo "ninguno" -Token $tokens[$columna]
                Verificar "ruta no declarada $($prueba.Metodo) $($prueba.Ruta) [$($Global:NombresColumna[$columna])] responde $esperado (obtuvo $($r.Status))" ($r.Status -eq $esperado)
            }
        }
    }

    $tramiteDespues = Conteos-Tramite
    Verificar "no aparecieron solicitudes, anexos, resoluciones, historial ni archivos (antes $tramiteAntes, despues $tramiteDespues)" ($tramiteDespues -eq $tramiteAntes)
}
finally {
    $est = Lista-Sql $estudiantes
    $solicitudes = @()
    if ($est) { $solicitudes = @(Ejecutar-Sql "select uuidSolicitudAcademica from SOLICITUD_ACADEMICA where Estudiante_uuid in ($est)") }
    $sol = Lista-Sql $solicitudes
    $usu = Lista-Sql $usuarios
    $fun = Lista-Sql $funcionarios
    $cod = Lista-Sql $codigos
    $sentencias = @()
    if ($sol) {
        $sentencias += "delete from ANEXO_ACADEMICO where SolicitudAcademica_uuid in ($sol)"
        $sentencias += "delete from RESOLUCION_ACADEMICA where SolicitudAcademica_uuid in ($sol)"
        $sentencias += "delete from HISTORIAL_SOLICITUD_ACADEMICA where SolicitudAcademica_uuid in ($sol)"
        $sentencias += "delete from SOLICITUD_ACADEMICA where uuidSolicitudAcademica in ($sol)"
    }
    if ($usu) {
        $sentencias += "delete from logs where uuidUsuario in ($usu)"
        if ($est) { $sentencias += "delete from ASIGNATURA_MATRICULADA where Estudiante_uuid in ($est)"; $sentencias += "delete from ESTUDIANTE where Usuario_uuid in ($est)" }
        if ($cod) { $sentencias += "delete from ASIGNATURA where codigoAsignatura in ($cod)" }
        if ($fun) { $sentencias += "delete from FUNCIONARIO_ACADEMICO where Usuario_uuid in ($fun)" }
        $sentencias += "delete from Usuario_has_Roles where uuidUsuario in ($usu)"
        $sentencias += "delete from usuarios where uuidUsuario in ($usu)"
        $sentencias += "delete from usuariosLivianos where uuidUsuario in ($usu)"
    }
    if ($sentencias.Count -gt 0) { Ejecutar-Sql (($sentencias -join "; ") + ";") | Out-Null }
    $carpetaAnexos = Join-Path $Global:CarpetaUploads "anexos"
    foreach ($uuid in $solicitudes) {
        $carpeta = Join-Path $carpetaAnexos $uuid
        if (Test-Path $carpeta) { Remove-Item -LiteralPath $carpeta -Recurse -Force }
    }
    if (-not $habiaAnexos -and (Test-Path $carpetaAnexos) -and (@(Get-ChildItem $carpetaAnexos -Force).Count -eq 0)) {
        Remove-Item -LiteralPath $carpetaAnexos -Force
    }

    $datosDespues = Conteos-Datos
    Verificar "los usuarios, asignaturas, estudiantes, funcionarios y responsables quedaron como al inicio (antes $datosAntes, despues $datosDespues)" ($datosDespues -eq $datosAntes)
    $tramiteFinal = Conteos-Tramite
    Verificar "tras la limpieza no quedaron solicitudes, anexos ni archivos nuevos ($tramiteFinal)" ($tramiteFinal -eq $tramiteAntes)
}

Write-Host ""
Write-Host "PASS: $Global:Pasan  FAIL: $Global:Fallan"
if ($Global:Fallan -gt 0) { exit 1 }
exit 0
