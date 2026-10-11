package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.ProgramaAcademico;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.ProgramaAcademicoEntidad;
import org.springframework.stereotype.Service;

@Service
public class ProgramaAcademicoOwnMapper implements OwnMapper<ProgramaAcademico, ProgramaAcademicoEntidad> {
    @Override
    public ProgramaAcademico toDominio(ProgramaAcademicoEntidad source) {
        return ProgramaAcademico.builder()
                .uuidProgramaAcademico(source.getUuidProgramaAcademico())
                .nombre(source.getNombre())
                .sigla(source.getSigla())
                .ultimoConsecutivoAsignatura(source.getUltimoConsecutivoAsignatura())
                .build();
    }

    @Override
    public ProgramaAcademicoEntidad toEntidad(ProgramaAcademico source) {
        return ProgramaAcademicoEntidad.builder()
                .uuidProgramaAcademico(source.getUuidProgramaAcademico())
                .nombre(source.getNombre())
                .sigla(source.getSigla())
                .ultimoConsecutivoAsignatura(source.getUltimoConsecutivoAsignatura())
                .build();
    }
}
