package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudExamenSupletorio;

import java.time.LocalDateTime;

public interface SolicitudExamenSupletorioGatewayIntPuerto {

    SolicitudExamenSupletorio guardar(SolicitudExamenSupletorio supletorio);

    SolicitudExamenSupletorio getPorSolicitud(String uuidSolicitudAcademica);

    void actualizarFechaAcordada(String uuidSolicitudAcademica, LocalDateTime fechaAcordadaExamen);
}
