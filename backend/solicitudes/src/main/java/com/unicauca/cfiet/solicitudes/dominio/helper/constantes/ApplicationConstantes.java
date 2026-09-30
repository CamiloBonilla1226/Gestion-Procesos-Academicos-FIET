package com.unicauca.cfiet.solicitudes.dominio.helper.constantes;

/**
 * Clase que tiene las variables de configuración del sistema.
 * Se usa esta clase para evitar hardcodear Strings en metodos de la aplicación.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public final class ApplicationConstantes {
    /*Roles del Sistema*/
    public static final String SECRETARIO_GENERAL = "Secretario General";
    public static final String DECANO = "Decano";
    public static final String FUNCIONARIO_ROL = "Funcionario";
    public static final String FUNCIONARIO = "FUNCIONARIO";
    /*Secciones del Orden del Día*/
    public static final String ASUNTOS_DECANO = "asuntos decano";
    public static final String ASUNTOS_PREGRADO = "asuntos pregrado";
    public static final String ASUNTOS_POSGRADOS = "asuntos posgrados";
    public static final String ASUNTOS_DELEGADOS_EN_DECANO = "asuntos delegados en decano";
    public static final String SOLICITUD_COMISION_ACADEMICA_INTERIOR_PAIS = "solicitud comisión académica al interior del país";
    public static final String SOLICITUD_COMISION_ACADEMICA_EXTERIOR_PAIS = "solicitud comisión académica al exterior del país";
    public static final String INFORME_COMISION_ACADEMICA = "informe de comisión académica";
    public static final String ASUNTOS_VARIOS = "asuntos varios";
    /*Estados de solicitudes*/
    public static final String AGREGADO_EN_EL_ORDEN_DEL_DIA = "AGREGADO EN EL ORDEN DEL DÍA";
    public static final String SIN_RESPONDER = "SIN RESPONDER";
    /*Formatos de Anexos*/
    public static final String FORMATO_PDF = "PDF";
    public static final String FORMATO_DOCX = "DOCX";
    public static final String FORMATO_XLSX = "XLSX";
    /*Tipos de documentos*/
    public static final String CEDULA_CIUDADANIA = "Cédula de ciudadanía";
    public static final String TARJETA_IDENTIDAD = "Tarjeta de identidad";
    public static final String CEDULA_EXTRANJERIA = "Cédula de extranjería";
    public static final String CEDULA_CIUDADANIA_LOWER = "cédula de ciudadanía";
    public static final String TARJETA_IDENTIDAD_LOWER = "tarjeta de identidad";
    public static final String CEDULA_EXTRANJERIA_LOWER = "cédula de extranjería";
    /*Perfil de Seguridad*/
    public static final String SECRETARIO_DECANO_ACCESO =  "hasAnyAuthority('" + SECRETARIO_GENERAL + "', '" + DECANO + "')";
    public static final String AUTHENTICATED = "isAuthenticated()";
    public static final String SECRETARIO_DECANO_FUNCIONARIO_ACCESO = "hasAnyAuthority('" + SECRETARIO_GENERAL + "', '" + FUNCIONARIO_ROL + "', '" + DECANO + "')";
    /*Tipos de Respuesta*/
    public static final String OFICIO = "Oficio";
    public static final String RESOLUCION = "Resolución";
}
