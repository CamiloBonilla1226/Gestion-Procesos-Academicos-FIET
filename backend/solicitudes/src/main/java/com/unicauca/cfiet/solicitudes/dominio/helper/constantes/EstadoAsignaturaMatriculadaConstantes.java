package com.unicauca.cfiet.solicitudes.dominio.helper.constantes;

import java.util.List;

public final class EstadoAsignaturaMatriculadaConstantes {
    public static final String ACTIVA = "activa";
    public static final String CANCELADA = "cancelada";
    public static final String APROBADA = "aprobada";
    public static final String PERDIDA = "perdida";
    public static final List<String> ESTADOS_VALIDOS = List.of(ACTIVA, CANCELADA, APROBADA, PERDIDA);

    private EstadoAsignaturaMatriculadaConstantes() {
    }
}
