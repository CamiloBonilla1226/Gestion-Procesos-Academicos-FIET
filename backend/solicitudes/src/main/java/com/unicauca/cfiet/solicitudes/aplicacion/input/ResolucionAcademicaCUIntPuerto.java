package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.ActorSolicitud;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ArchivoAdjunto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ResolucionAcademica;

public interface ResolucionAcademicaCUIntPuerto {

    ResolucionAcademica adjuntarResolucion(String uuidSolicitudAcademica, ArchivoAdjunto archivo, ActorSolicitud actor, String token);

    ArchivoAdjunto obtenerResolucion(String uuidSolicitudAcademica, ActorSolicitud actor);
}
