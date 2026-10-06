package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.AccionEtapa;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ActorSolicitud;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudAcademica;

public interface SolicitudAcademicaCUIntPuerto {

    SolicitudAcademica crearSolicitud(String uuidEstudiante, String uuidTipoSolicitudAcademica, String token);

    void verificarSinSolicitudEnCurso(String uuidEstudiante, TipoSolicitudAcademica tipo);

    SolicitudAcademica cambiarEtapa(String uuidSolicitudAcademica, AccionEtapa accion, ActorSolicitud actor, String observacion, String token);
}
