package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.AsignaturaSolicitudAcademicaEntidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsignaturaSolicitudAcademicaOwnMapper implements OwnMapper<AsignaturaSolicitudAcademica, AsignaturaSolicitudAcademicaEntidad> {
    private final AsignaturaMatriculadaOwnMapper asignaturaMatriculadaMapper;
    private final SituacionAcademicaAsignaturaOwnMapper situacionMapper;

    @Override
    public AsignaturaSolicitudAcademica toDominio(AsignaturaSolicitudAcademicaEntidad source) {
        if (source == null) return null;
        return AsignaturaSolicitudAcademica.builder()
                .uuidAsignaturaSolicitud(source.getUuidAsignaturaSolicitud())
                .asignaturaMatriculada(source.getAsignaturaMatriculada() == null ? null
                        : asignaturaMatriculadaMapper.toDominio(source.getAsignaturaMatriculada()))
                .numeroFaltas(source.getNumeroFaltas())
                .nota(source.getNota())
                .situacionMatricula(source.getSituacionMatricula() == null ? null : situacionMapper.toDominio(source.getSituacionMatricula()))
                .situacionCancelar(source.getSituacionCancelar() == null ? null : situacionMapper.toDominio(source.getSituacionCancelar()))
                .build();
    }

    @Override
    public AsignaturaSolicitudAcademicaEntidad toEntidad(AsignaturaSolicitudAcademica source) {
        if (source == null) return null;
        return AsignaturaSolicitudAcademicaEntidad.builder()
                .uuidAsignaturaSolicitud(source.getUuidAsignaturaSolicitud())
                .numeroFaltas(source.getNumeroFaltas())
                .nota(source.getNota())
                .build();
    }
}
