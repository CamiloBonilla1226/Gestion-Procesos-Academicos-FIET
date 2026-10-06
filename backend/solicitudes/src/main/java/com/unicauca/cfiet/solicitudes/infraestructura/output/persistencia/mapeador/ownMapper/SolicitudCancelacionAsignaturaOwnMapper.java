package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudCancelacionAsignatura;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudCancelacionAsignaturaEntidad;
import org.springframework.stereotype.Service;

@Service
public class SolicitudCancelacionAsignaturaOwnMapper implements OwnMapper<SolicitudCancelacionAsignatura, SolicitudCancelacionAsignaturaEntidad> {

    @Override
    public SolicitudCancelacionAsignatura toDominio(SolicitudCancelacionAsignaturaEntidad source) {
        if (source == null) return null;
        return SolicitudCancelacionAsignatura.builder()
                .solicitudAcademica(source.getSolicitudAcademica() == null ? null : SolicitudAcademica.builder()
                        .uuidSolicitudAcademica(source.getSolicitudAcademica().getUuidSolicitudAcademica())
                        .radicado(source.getSolicitudAcademica().getRadicado())
                        .build())
                .motivoCancelacion(source.getMotivoCancelacion())
                .build();
    }

    @Override
    public SolicitudCancelacionAsignaturaEntidad toEntidad(SolicitudCancelacionAsignatura source) {
        if (source == null) return null;
        return SolicitudCancelacionAsignaturaEntidad.builder()
                .uuidSolicitudAcademica(source.getSolicitudAcademica() == null ? null
                        : source.getSolicitudAcademica().getUuidSolicitudAcademica())
                .motivoCancelacion(source.getMotivoCancelacion())
                .build();
    }
}
