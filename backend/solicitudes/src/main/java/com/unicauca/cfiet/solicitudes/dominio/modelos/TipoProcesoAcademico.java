package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.Getter;

@Getter
public enum TipoProcesoAcademico {
    CANCELACION_MATRICULA("Cancelación de Matrícula", "CM"),
    CANCELACION_ASIGNATURA("Cancelación de Asignatura", "CA"),
    EXAMEN_SUPLETORIO("Examen Supletorio", "ES");

    private final String nombre;
    private final String codigo;

    TipoProcesoAcademico(String nombre, String codigo) {
        this.nombre = nombre;
        this.codigo = codigo;
    }

    public static TipoProcesoAcademico porNombre(String nombre) {
        if (nombre == null) return null;
        for (TipoProcesoAcademico tipo : values())
            if (tipo.nombre.equalsIgnoreCase(nombre.trim()))
                return tipo;
        return null;
    }
}
