package com.unicauca.cfiet.solicitudes.dominio.helper.constantes;

import java.util.Map;

public class AnexoAcademicoConstantes {
    public static final long TAMANIO_MAXIMO_BYTES = 5L * 1024 * 1024;
    public static final long TAMANIO_MAXIMO_PAZ_Y_SALVOS_BYTES = 10L * 1024 * 1024;
    public static final String FORMATOS_SOPORTE_LIBRE = "pdf,jpg,jpeg,png";
    public static final String RECIBO_PAGO = "Recibo de pago";
    public static final String COMPROBANTE_PAGO = "Comprobante de pago";
    public static final String SOPORTE_JUSTIFICACION = "Soporte de la justificación de la no presentación";
    public static final String FORMATO_DOCENTE_CRUCE = "Formato firmado por el docente de la asignatura con la que se cruza";
    public static final String FORMATO_FOR_23 = "Formato firmado por el docente que orienta la asignatura";
    public static final String DOCUMENTO_ADICIONAL = "Documento adicional de la respuesta";
    public static final String PAZ_Y_SALVOS = "Paz y salvos (un solo PDF con todos los paz y salvos)";
    public static final Map<String, String> TIPOS_CONTENIDO = Map.of(
            "pdf", "application/pdf",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png");

    public static long tamanioMaximoDe(String nombreTipoAnexo) {
        return PAZ_Y_SALVOS.equals(nombreTipoAnexo) ? TAMANIO_MAXIMO_PAZ_Y_SALVOS_BYTES : TAMANIO_MAXIMO_BYTES;
    }

    private AnexoAcademicoConstantes() {
    }
}
