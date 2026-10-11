package com.unicauca.cfiet.solicitudes.dominio.servicios;

import java.util.Locale;

public final class ReglasAsignatura {
    public static final int LONGITUD_MAXIMA_NOMBRE = 150;

    private ReglasAsignatura() {
    }

    public static String normalizarNombre(String nombre) {
        return nombre == null ? "" : nombre.trim().replaceAll("\\s+", " ");
    }

    public static String claveNombre(String nombre) {
        return normalizarNombre(nombre).toLowerCase(Locale.ROOT);
    }

    public static String normalizarCodigo(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase(Locale.ROOT);
    }

    public static String generarCodigo(String sigla, int consecutivo) {
        return String.format("%s-%03d", sigla, consecutivo);
    }
}
