package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.ProgramaAcademico;

import java.util.List;

public interface ProgramaAcademicoGatewayIntPuerto {

    List<ProgramaAcademico> getProgramas();

    ProgramaAcademico getPorUuid(String uuidProgramaAcademico);

    ProgramaAcademico bloquear(String uuidProgramaAcademico);

    void guardarUltimoConsecutivo(String uuidProgramaAcademico, int ultimoConsecutivo);
}
