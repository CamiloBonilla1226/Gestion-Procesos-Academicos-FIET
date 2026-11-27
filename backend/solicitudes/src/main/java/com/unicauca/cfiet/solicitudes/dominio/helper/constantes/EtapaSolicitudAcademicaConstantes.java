package com.unicauca.cfiet.solicitudes.dominio.helper.constantes;

import java.util.List;
import java.util.Set;

public class EtapaSolicitudAcademicaConstantes {
    public static final String RADICADA = "RADICADA";
    public static final String EN_REVISION_DECANO = "EN_REVISION_DECANO";
    public static final String APROBADA_POR_DECANO = "APROBADA_POR_DECANO";
    public static final String RECHAZADA_POR_DECANO = "RECHAZADA_POR_DECANO";
    public static final String PENDIENTE_PAGO = "PENDIENTE_PAGO";
    public static final String EN_VERIFICACION_PAGO = "EN_VERIFICACION_PAGO";
    public static final String APROBADA = "APROBADA";
    public static final String RECHAZADA = "RECHAZADA";
    public static final String PENDIENTE_FIRMA_ESTUDIANTE = "PENDIENTE_FIRMA_ESTUDIANTE";
    public static final String EN_REVISION_CONSEJO = "EN_REVISION_CONSEJO";
    public static final String APROBADA_POR_CONSEJO = "APROBADA_POR_CONSEJO";
    public static final String NO_APROBADA_POR_CONSEJO = "NO_APROBADA_POR_CONSEJO";
    public static final List<String> ETAPAS = List.of(PENDIENTE_FIRMA_ESTUDIANTE, RADICADA, EN_REVISION_DECANO, APROBADA_POR_DECANO,
            RECHAZADA_POR_DECANO, PENDIENTE_PAGO, EN_VERIFICACION_PAGO, APROBADA, RECHAZADA, EN_REVISION_CONSEJO, APROBADA_POR_CONSEJO,
            NO_APROBADA_POR_CONSEJO);
    public static final Set<String> APROBADAS_CON_DECISION = Set.of(APROBADA_POR_DECANO, APROBADA_POR_CONSEJO);
    public static final Set<String> NO_APROBADAS_CON_DECISION = Set.of(RECHAZADA_POR_DECANO, NO_APROBADA_POR_CONSEJO);
    public static final Set<String> CON_DECISION = Set.of(APROBADA_POR_DECANO, RECHAZADA_POR_DECANO, APROBADA_POR_CONSEJO,
            NO_APROBADA_POR_CONSEJO);
    public static final Set<String> EN_REVISION = Set.of(EN_REVISION_DECANO, EN_REVISION_CONSEJO);
    public static final String ETIQUETA_FUNCIONARIO_SUPLETORIO_APROBADO = "Enviar Recibo";
    public static final String DECISION_APROBADA = "APROBADA";
    public static final String DECISION_RECHAZADA = "RECHAZADA";
    public static final String DECISION_APROBADA_PARCIAL = "APROBADA_PARCIAL";

    private EtapaSolicitudAcademicaConstantes() {
    }
}
