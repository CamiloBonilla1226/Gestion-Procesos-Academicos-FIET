package com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos;

import java.util.List;

public final class ColumnasExcelAsignaturas {
    public static final List<String> COLUMNAS = List.of("nombreAsignatura", "programaAcademico");

    private ColumnasExcelAsignaturas() {
    }

    public static String letra(int indice) {
        return String.valueOf((char) ('A' + indice));
    }

    public static String letra(String columna) {
        return letra(COLUMNAS.indexOf(columna));
    }
}
