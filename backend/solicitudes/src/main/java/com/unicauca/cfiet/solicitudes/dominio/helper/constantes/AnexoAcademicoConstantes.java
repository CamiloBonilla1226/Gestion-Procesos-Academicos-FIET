package com.unicauca.cfiet.solicitudes.dominio.helper.constantes;

import java.util.Map;

public class AnexoAcademicoConstantes {
    public static final long TAMANIO_MAXIMO_BYTES = 5L * 1024 * 1024;
    public static final String FORMATOS_SOPORTE_LIBRE = "pdf,jpg,jpeg,png";
    public static final String RECIBO_PAGO = "Recibo de pago";
    public static final String COMPROBANTE_PAGO = "Comprobante de pago";
    public static final String SOPORTE_JUSTIFICACION = "Soporte de la justificación de la no presentación";
    public static final String FORMATO_DOCENTE_CRUCE = "Formato firmado por el docente de la asignatura con la que se cruza";
    public static final String FORMATO_FOR_23 = "Formato PM-FO-4-FOR-23 firmado por el docente que orienta la asignatura";
    public static final Map<String, String> TIPOS_CONTENIDO = Map.of(
            "pdf", "application/pdf",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png");

    private AnexoAcademicoConstantes() {
    }
}
