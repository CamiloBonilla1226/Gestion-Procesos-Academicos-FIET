package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.ProgramaAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ProgramaAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ProgramaAcademico;

import java.util.List;

public class ProgramaAcademicoCUImplAdaptador implements ProgramaAcademicoCUIntPuerto {
    private final ProgramaAcademicoGatewayIntPuerto gateway;

    public ProgramaAcademicoCUImplAdaptador(ProgramaAcademicoGatewayIntPuerto gateway) {
        this.gateway = gateway;
    }

    @Override
    public List<ProgramaAcademico> getProgramasAcademicos() {
        return gateway.getProgramas();
    }
}
