package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.RemisionConsejo;

public interface RemisionConsejoGatewayIntPuerto {

    RemisionConsejo getPorSolicitudAcademica(String uuidSolicitudAcademica);

    RemisionConsejo getPorSolicitud(String uuidSolicitud);

    RemisionConsejo guardar(RemisionConsejo remision);
}
