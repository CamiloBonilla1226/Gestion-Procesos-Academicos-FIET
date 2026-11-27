package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.RemisionConsejo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudAcademica;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.RemisionConsejoEntidad;
import org.springframework.stereotype.Service;

@Service
public class RemisionConsejoOwnMapper implements OwnMapper<RemisionConsejo, RemisionConsejoEntidad> {

    @Override
    public RemisionConsejo toDominio(RemisionConsejoEntidad source) {
        if (source == null) return null;
        return RemisionConsejo.builder()
                .solicitudAcademica(SolicitudAcademica.builder().uuidSolicitudAcademica(source.getUuidSolicitudAcademica()).build())
                .uuidSolicitud(source.getSolicitud() == null ? null : source.getSolicitud().getUuidSolicitud())
                .fechaRemision(source.getFechaRemision())
                .build();
    }

    @Override
    public RemisionConsejoEntidad toEntidad(RemisionConsejo source) {
        if (source == null) return null;
        return RemisionConsejoEntidad.builder()
                .uuidSolicitudAcademica(source.getSolicitudAcademica() == null ? null : source.getSolicitudAcademica().getUuidSolicitudAcademica())
                .fechaRemision(source.getFechaRemision())
                .build();
    }
}
