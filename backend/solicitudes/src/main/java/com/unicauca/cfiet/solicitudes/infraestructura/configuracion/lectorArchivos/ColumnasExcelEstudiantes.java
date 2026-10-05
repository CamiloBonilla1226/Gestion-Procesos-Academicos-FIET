package com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos;

import java.util.List;

public final class ColumnasExcelEstudiantes {
    public static final List<String> COLUMNAS = List.of(
            "nombres", "apellidos", "tipoDocumento", "numeroDocumento", "telefono",
            "correoElectronico", "username", "password", "codigoEstudiantil", "programaAcademico",
            "semestre", "facultad", "codigoAsignatura", "nombreAsignatura", "grupo");

    private ColumnasExcelEstudiantes() {
    }

    public static String letra(int indice) {
        return String.valueOf((char) ('A' + indice));
    }

    public static String letra(String columna) {
        return letra(COLUMNAS.indexOf(columna));
    }
}
