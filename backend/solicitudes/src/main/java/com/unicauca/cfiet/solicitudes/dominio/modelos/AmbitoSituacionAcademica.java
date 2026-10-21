package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.Getter;

@Getter
public enum AmbitoSituacionAcademica {
    MATRICULA("La situación de la asignatura"),
    CANCELACION("La situación al cancelar");

    private final String campo;

    AmbitoSituacionAcademica(String campo) {
        this.campo = campo;
    }
}
