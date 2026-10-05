package com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos;

import java.util.List;

public final class ColumnasExcelFuncionariosAcademicos {
    public static final List<String> COLUMNAS = List.of(
            "nombres", "apellidos", "tipoDocumento", "numeroDocumento", "telefono",
            "correoElectronico", "username", "password", "dependencia");

    private ColumnasExcelFuncionariosAcademicos() {
    }

    public static String letra(int indice) {
        return String.valueOf((char) ('A' + indice));
    }

    public static String letra(String columna) {
        return letra(COLUMNAS.indexOf(columna));
    }
}
