package com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones;

/**
 *  Definición de mensajes de error
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public class MensajesError {
    public static final String PAGINACION_ERROR = "Error en la paginación o tamaño de la pagina...";
    public static final String SIN_REGISTROS = "No existen registrados %s en el sistema...";
    public static final String ARCHIVO_EXCEL_VACIO = "No se pudo procesar %s de la petición";
    public static final String ENTIDAD_NO_ENCONTRADA = "%s con id %s no fue encontrado en el sistema...";
    public static final String ATRIBUTO_UNICO_YA_EXISTE = "%s con %s: %s existe en el sistema...";
    public static final String ENTIDAD_NO_ENCONTRADA_FILTRO = "%s con %s: %s no fue encontrado en el sistema...";
    public static final String INSTANCIA_NO_VALIDA = "Se intentó crear una instancia de %s no permitida...";
    public static final String TIPO_DE_USUARIO_NO_VALIDO = "Tipo de usuario no valido..";
    public static final String ROLES_DUPLICADOS_USUARIO = "El usuario tiene roles duplicados...";
    public static final String ROLES_NO_VALIDOS = "Los roles ingresados no son validos...";
    public static final String CONTRASEÑA_INCORRECTA = "Contraseña incorrecta...";
    public static final String CREDENCIALES_ERRONEAS = "Credenciales erroneas, revise su username o contraseña...";
    public static final String USERNAME_TOKEN =  "No se pudo extraer el username del token...";
    public static final String MAL_FORMATO_ANEXO = "El formato de anexo ingresado no esta soportado...";
    public static final String MAL_ASIGNACION = "Solo los usuarios creados inicialmente como funcionarios pueden tener tipos de solicitudes asignados...";
    public static final String SECCION_NO_EXISTENTE = "Sección universitaria no soportada...";
    public static final String PERFIL_SOLICITANTE_NO_VALIDO = "Perfil solicitante no es valido...";
    public static final String NO_ACCESO = "Usuario sin acceso para acceder a la aplicación...";
    public static final String ROL_NO_HABILITADO = "Usuario con rol no habilidatado para acceder al sistema...";
    public static final String TIPO_DOCUMENTO_ERRONEO = "Tipo de documento invalido...";
    public static final String TIPO_RESPUESTA_NO_VALIDO = "Tipo de respuesta %s no es valido";
    public static final String FORMATO_RESPUESTA_NO_VALIDO = "Tipo de archivo de respuesta no es valido";
    public static final String VALOR_REPETIDO_PETICION = "La petición trae %s %s más de una vez...";
    public static final String ASIGNATURA_YA_MATRICULADA = "El estudiante ya tiene activa la asignatura %s en el grupo %s...";
    public static final String ESTADO_ASIGNATURA_NO_VALIDO = "El estado %s no es valido, los estados permitidos son %s...";
    public static final String CAMBIO_ESTADO_NO_PERMITIDO = "No se puede cambiar la asignatura matriculada de %s a %s...";
    public static final String EXCEL_NO_VALIDO = "El archivo debe ser un Excel .xlsx valido con una hoja...";
    public static final String EXCEL_SIN_FILAS = "El archivo no tiene filas de estudiantes despues del encabezado...";
    public static final String EXCEL_ENCABEZADO_INVALIDO = "Fila 1, columna %s: se esperaba el encabezado %s y se encontró '%s'...";
    public static final String EXCEL_FILA_ERROR = "Fila %d, columna %s (%s): %s";
    public static final String EXCEL_FILA_DATO_DISTINTO = "Fila %d, columna %s (%s): el estudiante con documento %s tiene un valor distinto al de la fila %d...";
    public static final String EXCEL_FILA_MATERIA_REPETIDA = "Fila %d, columna %s (%s): la asignatura %s en el grupo %s ya aparece en la fila %d para el mismo estudiante...";
    public static final String EXCEL_FILA_VALOR_REPETIDO = "Fila %d, columna %s (%s): el valor %s ya lo usa otro estudiante en la fila %d...";
    public static final String EXCEL_SIN_DATOS = "El archivo no tiene filas de datos despues del encabezado...";
    public static final String EXCEL_FILA_VALOR_DUPLICADO = "Fila %d, columna %s (%s): el valor %s ya aparece en la fila %d del archivo...";
    public static final String ROL_ETIQUETA_NO_VALIDO = "El rol %s no es valido, los roles permitidos son %s...";
    public static final String DATOS_TRANSICION_INCOMPLETOS = "Para mover la solicitud se requieren el tipo de proceso, la acción y el rol del actor...";
    public static final String ETAPA_NO_VALIDA = "La etapa %s no es valida...";
    public static final String ETAPA_FINAL_SIN_ACCIONES = "La solicitud está en la etapa final %s y no admite más acciones...";
    public static final String TRANSICION_NO_PERMITIDA = "La acción %s no está permitida desde la etapa %s en %s...";
    public static final String ROL_TRANSICION_NO_PERMITIDO = "El rol %s no puede ejecutar la acción %s desde la etapa %s, le corresponde a %s...";
    public static final String REQUISITO_TRANSICION_FALTANTE = "La acción %s exige %s...";
    public static final String TIPO_SOLICITUD_SIN_PROCESO = "El tipo de solicitud %s no corresponde a ningún proceso académico...";
    public static final String ACTOR_SIN_ROL = "El usuario %s no tiene el rol %s...";
    public static final String SOLICITUD_AJENA = "La solicitud %s no pertenece al estudiante %s...";
    public static final String TIPO_NO_ASIGNADO_FUNCIONARIO = "El tipo de solicitud %s no está asignado al funcionario académico %s...";
    public static final String OBSERVACION_MUY_LARGA = "La observación supera los %d caracteres permitidos...";
    public static final String ARCHIVO_VACIO = "El archivo adjunto está vacío...";
    public static final String ARCHIVO_MUY_GRANDE = "El archivo pesa %d bytes y el máximo permitido es %d bytes (5 MB)...";
    public static final String FORMATO_ANEXO_NO_PERMITIDO = "El formato %s no está permitido para %s, se aceptan %s...";
    public static final String CONTENIDO_ANEXO_NO_COINCIDE = "El contenido del archivo no corresponde a un archivo %s...";
    public static final String ANEXO_DE_OTRO_TIPO = "El tipo de anexo %s no pertenece al tipo de solicitud %s...";
    public static final String SOPORTE_LIBRE_NO_PERMITIDO = "El tipo de solicitud %s no admite soportes libres...";
    public static final String ANEXO_ACTOR_NO_PERMITIDO = "El anexo %s solo lo puede subir el rol %s...";
    public static final String ANEXO_ETAPA_NO_PERMITIDA = "El anexo %s solo se puede subir en la etapa %s y la solicitud está en %s...";
    public static final String CAUSA_SUPLETORIO_REQUERIDA = "Para saber qué anexos exige el examen supletorio se requiere la causa (CRUCE u OTRA)...";
    public static final String ERROR_GUARDANDO_ARCHIVO = "No se pudo guardar el archivo del anexo...";
    public static final String RESOLUCION_SOLO_CANCELACIONES = "El tipo de solicitud %s no produce Resolución, solo la producen las cancelaciones de matrícula y de asignatura...";
    public static final String RESOLUCION_SOLO_FUNCIONARIO = "El escaneo de la Resolución solo lo puede subir el funcionario académico asignado...";
    public static final String RESOLUCION_ETAPA_FINAL = "La solicitud %s está en la etapa final %s y su Resolución ya no se puede subir ni reemplazar...";
    public static final String RESOLUCION_ETAPA_NO_PERMITIDA = "El escaneo de la Resolución solo se sube en las etapas %s y la solicitud está en %s...";
    public static final String RESOLUCION_NO_DISPONIBLE = "La Resolución de la solicitud %s estará disponible cuando la solicitud termine...";
    public static final String SOLICITUD_EN_CURSO = "Ya tienes una solicitud de %s en curso (%s), debe terminar antes de radicar otra...";
    public static final String RADICADO_NO_GENERADO = "No se pudo generar el radicado, intente de nuevo...";
    public static final String MOTIVO_CANCELACION_REQUERIDO = "El motivo de la cancelación es obligatorio...";
    public static final String MOTIVO_CANCELACION_MUY_LARGO = "El motivo de la cancelación supera los %d caracteres permitidos...";
    public static final String SIN_ASIGNATURAS_ACTIVAS = "El estudiante %s no tiene asignaturas activas para cancelar...";
    public static final String ANEXOS_OBLIGATORIOS_FALTANTES = "Falta el anexo obligatorio: %s...";
    public static final String ASIGNATURAS_SIN_EVALUAR = "Faltan datos de las asignaturas de la solicitud: %s...";
    public static final String ASIGNATURA_REPETIDA = "La asignatura %s viene más de una vez...";
    public static final String ASIGNATURA_AJENA = "La asignatura %s no pertenece a la solicitud %s...";
    public static final String DATO_ASIGNATURA_FALTANTE = "Falta %s de la asignatura %s...";
    public static final String NUMERO_FALTAS_NO_VALIDO = "El número de faltas de la asignatura %s debe ser un entero mayor o igual a 0...";
    public static final String NOTA_NO_VALIDA = "La nota de la asignatura %s debe estar entre 0.0 y 5.0 con un decimal...";
}
