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
}
