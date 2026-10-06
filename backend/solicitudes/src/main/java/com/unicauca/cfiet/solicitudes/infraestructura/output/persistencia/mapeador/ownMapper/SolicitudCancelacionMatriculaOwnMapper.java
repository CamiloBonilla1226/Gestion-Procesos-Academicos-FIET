package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudCancelacionMatricula;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudCancelacionMatriculaEntidad;
import org.springframework.stereotype.Service;

@Service
public class SolicitudCancelacionMatriculaOwnMapper implements OwnMapper<SolicitudCancelacionMatricula, SolicitudCancelacionMatriculaEntidad> {

    @Override
    public SolicitudCancelacionMatricula toDominio(SolicitudCancelacionMatriculaEntidad source) {
        if (source == null) return null;
        return SolicitudCancelacionMatricula.builder()
                .solicitudAcademica(source.getSolicitudAcademica() == null ? null : SolicitudAcademica.builder()
                        .uuidSolicitudAcademica(source.getSolicitudAcademica().getUuidSolicitudAcademica())
                        .radicado(source.getSolicitudAcademica().getRadicado())
                        .build())
                .motivoCancelacion(source.getMotivoCancelacion())
                .build();
    }

    @Override
    public SolicitudCancelacionMatriculaEntidad toEntidad(SolicitudCancelacionMatricula source) {
        if (source == null) return null;
        return SolicitudCancelacionMatriculaEntidad.builder()
                .uuidSolicitudAcademica(source.getSolicitudAcademica() == null ? null
                        : source.getSolicitudAcademica().getUuidSolicitudAcademica())
                .motivoCancelacion(source.getMotivoCancelacion())
                .build();
    }
}
