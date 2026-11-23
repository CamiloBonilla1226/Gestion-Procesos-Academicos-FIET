package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.CorreosPendientesCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.CorreoIntPuerto;

public class CorreosPendientesCUImplAdaptador implements CorreosPendientesCUIntPuerto {
    private final CorreoIntPuerto correo;

    public CorreosPendientesCUImplAdaptador(CorreoIntPuerto correo) {
        this.correo = correo;
    }

    @Override
    public int contarPendientes() {
        return correo.contarPendientes();
    }
}
