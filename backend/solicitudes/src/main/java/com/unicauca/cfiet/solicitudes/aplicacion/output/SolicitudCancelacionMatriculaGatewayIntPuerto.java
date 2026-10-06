package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudCancelacionMatricula;

public interface SolicitudCancelacionMatriculaGatewayIntPuerto {

    SolicitudCancelacionMatricula guardar(SolicitudCancelacionMatricula cancelacion);

    SolicitudCancelacionMatricula getPorSolicitud(String uuidSolicitudAcademica);
}
