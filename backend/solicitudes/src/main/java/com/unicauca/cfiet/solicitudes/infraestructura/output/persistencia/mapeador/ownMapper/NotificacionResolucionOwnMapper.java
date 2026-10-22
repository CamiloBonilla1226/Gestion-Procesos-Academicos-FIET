package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.NotificacionResolucion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.NotificacionResolucionEntidad;
import org.springframework.stereotype.Service;

@Service
public class NotificacionResolucionOwnMapper implements OwnMapper<NotificacionResolucion, NotificacionResolucionEntidad> {

    @Override
    public NotificacionResolucion toDominio(NotificacionResolucionEntidad source) {
        if (source == null) return null;
        return NotificacionResolucion.builder()
                .uuidSolicitudAcademica(source.getUuidSolicitudAcademica())
                .fechaProgramada(source.getFechaProgramada())
                .estado(source.getEstado())
                .firmada(source.isFirmada())
                .numeroResolucion(source.getNumeroResolucion())
                .anioResolucion(source.getAnioResolucion())
                .decision(source.getDecision())
                .mensajeInicial(source.getMensajeInicial())
                .textoComunicacion(source.getTextoComunicacion())
                .textoDarca(source.getTextoDarca())
                .fechaEnvioEstudiante(source.getFechaEnvioEstudiante())
                .fechaEnvioDarca(source.getFechaEnvioDarca())
                .intentos(source.getIntentos())
                .ultimoError(source.getUltimoError())
                .fechaReclamo(source.getFechaReclamo())
                .fechaEnvio(source.getFechaEnvio())
                .fechaLiberacion(source.getFechaLiberacion())
                .fechaCreacion(source.getFechaCreacion())
                .build();
    }

    @Override
    public NotificacionResolucionEntidad toEntidad(NotificacionResolucion source) {
        if (source == null) return null;
        return NotificacionResolucionEntidad.builder()
                .uuidSolicitudAcademica(source.getUuidSolicitudAcademica())
                .fechaProgramada(source.getFechaProgramada())
                .estado(source.getEstado())
                .firmada(source.isFirmada())
                .numeroResolucion(source.getNumeroResolucion())
                .anioResolucion(source.getAnioResolucion())
                .decision(source.getDecision())
                .mensajeInicial(source.getMensajeInicial())
                .textoComunicacion(source.getTextoComunicacion())
                .textoDarca(source.getTextoDarca())
                .fechaEnvioEstudiante(source.getFechaEnvioEstudiante())
                .fechaEnvioDarca(source.getFechaEnvioDarca())
                .intentos(source.getIntentos())
                .ultimoError(source.getUltimoError())
                .fechaReclamo(source.getFechaReclamo())
                .fechaEnvio(source.getFechaEnvio())
                .fechaLiberacion(source.getFechaLiberacion())
                .fechaCreacion(source.getFechaCreacion())
                .build();
    }
}
