package com.unicauca.cfiet.solicitudes.dominio.helper.constantes;

import java.util.List;

public class EtapaSolicitudAcademicaConstantes {
    public static final String RADICADA = "RADICADA";
    public static final String EN_REVISION_DECANO = "EN_REVISION_DECANO";
    public static final String APROBADA_POR_DECANO = "APROBADA_POR_DECANO";
    public static final String RECHAZADA_POR_DECANO = "RECHAZADA_POR_DECANO";
    public static final String PENDIENTE_PAGO = "PENDIENTE_PAGO";
    public static final String EN_VERIFICACION_PAGO = "EN_VERIFICACION_PAGO";
    public static final String APROBADA = "APROBADA";
    public static final String RECHAZADA = "RECHAZADA";
    public static final List<String> ETAPAS = List.of(RADICADA, EN_REVISION_DECANO, APROBADA_POR_DECANO,
            RECHAZADA_POR_DECANO, PENDIENTE_PAGO, EN_VERIFICACION_PAGO, APROBADA, RECHAZADA);

    private EtapaSolicitudAcademicaConstantes() {
    }
}
