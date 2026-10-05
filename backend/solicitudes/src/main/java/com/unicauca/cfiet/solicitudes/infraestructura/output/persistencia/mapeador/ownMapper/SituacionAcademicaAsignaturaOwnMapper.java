package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.SituacionAcademicaAsignatura;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SituacionAcademicaAsignaturaEntidad;
import org.springframework.stereotype.Service;

@Service
public class SituacionAcademicaAsignaturaOwnMapper implements OwnMapper<SituacionAcademicaAsignatura, SituacionAcademicaAsignaturaEntidad> {

    @Override
    public SituacionAcademicaAsignatura toDominio(SituacionAcademicaAsignaturaEntidad source) {
        return SituacionAcademicaAsignatura.builder()
                .uuidSituacionAcademica(source.getUuidSituacionAcademica())
                .codigo(source.getCodigo())
                .nombre(source.getNombre())
                .build();
    }

    @Override
    public SituacionAcademicaAsignaturaEntidad toEntidad(SituacionAcademicaAsignatura source) {
        return SituacionAcademicaAsignaturaEntidad.builder()
                .uuidSituacionAcademica(source.getUuidSituacionAcademica())
                .codigo(source.getCodigo())
                .nombre(source.getNombre())
                .build();
    }
}
