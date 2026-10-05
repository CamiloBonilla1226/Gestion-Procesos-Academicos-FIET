package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.SituacionAcademicaAsignaturaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SituacionAcademicaAsignaturaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SituacionAcademicaAsignatura;

import java.util.List;

public class SituacionAcademicaAsignaturaCUImplAdaptador implements SituacionAcademicaAsignaturaCUIntPuerto {
    private final SituacionAcademicaAsignaturaGatewayIntPuerto gateway;

    public SituacionAcademicaAsignaturaCUImplAdaptador(SituacionAcademicaAsignaturaGatewayIntPuerto gateway) {
        this.gateway = gateway;
    }

    @Override
    public List<SituacionAcademicaAsignatura> getSituaciones() {
        return gateway.getTodas();
    }
}
