package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.AccionEtapa;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ActorSolicitud;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EntregaTransicion;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudAcademica;

public interface SolicitudAcademicaCUIntPuerto {

    SolicitudAcademica crearSolicitud(String uuidEstudiante, String uuidTipoSolicitudAcademica);

    SolicitudAcademica cambiarEtapa(String uuidSolicitudAcademica, AccionEtapa accion, ActorSolicitud actor, EntregaTransicion entrega);
}
