package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudCancelacionAsignatura;

public interface SolicitudCancelacionAsignaturaGatewayIntPuerto {

    SolicitudCancelacionAsignatura guardar(SolicitudCancelacionAsignatura cancelacion);

    SolicitudCancelacionAsignatura getPorSolicitud(String uuidSolicitudAcademica);
}
