package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.DecisionAsignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.DetalleRemisionConsejo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EvaluacionAsignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SesionConsejo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudCancelacionAsignatura;

import java.util.List;

public interface RemisionConsejoCUIntPuerto {

    SolicitudCancelacionAsignatura remitir(String uuidSolicitudAcademica, List<EvaluacionAsignatura> evaluaciones,
                                           String observacion, String token);

    DetalleRemisionConsejo getDetalle(String uuidSolicitud);

    DetalleRemisionConsejo registrarDecision(String uuidSolicitud, List<DecisionAsignatura> decisiones, String tipoRespuesta,
                                             String consecutivoFiet, String indicaciones, String token);

    SesionConsejo getSesion(String uuidSolicitudAcademica);

    void cerrar(String uuidSolicitudAcademica, String token);
}
