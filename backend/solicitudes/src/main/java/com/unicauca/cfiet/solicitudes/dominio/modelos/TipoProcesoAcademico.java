package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.Getter;

@Getter
public enum TipoProcesoAcademico {
    CANCELACION_MATRICULA("Cancelación de Matrícula"),
    CANCELACION_ASIGNATURA("Cancelación de Asignatura"),
    EXAMEN_SUPLETORIO("Examen Supletorio");

    private final String nombre;

    TipoProcesoAcademico(String nombre) {
        this.nombre = nombre;
    }
}
