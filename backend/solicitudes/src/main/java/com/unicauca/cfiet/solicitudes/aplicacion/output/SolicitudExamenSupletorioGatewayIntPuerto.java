package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudExamenSupletorio;

public interface SolicitudExamenSupletorioGatewayIntPuerto {

    SolicitudExamenSupletorio guardar(SolicitudExamenSupletorio supletorio);

    SolicitudExamenSupletorio getPorSolicitud(String uuidSolicitudAcademica);
}
