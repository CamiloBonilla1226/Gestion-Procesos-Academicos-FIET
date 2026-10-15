package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.NotificacionDocente;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.NotificacionDocenteEntidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificacionDocenteOwnMapper implements OwnMapper<NotificacionDocente, NotificacionDocenteEntidad> {
    private final DocenteOwnMapper docenteMapper;

    @Override
    public NotificacionDocente toDominio(NotificacionDocenteEntidad source) {
        if (source == null) return null;
        return NotificacionDocente.builder()
                .idNotificacion(source.getIdNotificacion())
                .uuidNotificacion(source.getUuidNotificacion())
                .uuidSolicitudAcademica(source.getSolicitudAcademica() == null ? null : source.getSolicitudAcademica().getUuidSolicitudAcademica())
                .docente(docenteMapper.toDominio(source.getDocente()))
                .titulo(source.getTitulo())
                .fechaCreacion(source.getFechaCreacion())
                .fechaLectura(source.getFechaLectura())
                .build();
    }

    @Override
    public NotificacionDocenteEntidad toEntidad(NotificacionDocente source) {
        if (source == null) return null;
        return NotificacionDocenteEntidad.builder()
                .idNotificacion(source.getIdNotificacion())
                .uuidNotificacion(source.getUuidNotificacion())
                .titulo(source.getTitulo())
                .fechaCreacion(source.getFechaCreacion())
                .fechaLectura(source.getFechaLectura())
                .build();
    }
}
