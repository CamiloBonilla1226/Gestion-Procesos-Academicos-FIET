package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.AjusteAsignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ComunicacionRespuesta;
import com.unicauca.cfiet.solicitudes.dominio.modelos.DecisionAsignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EvaluacionAsignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudCancelacionAsignatura;

import java.util.List;

public interface TramiteCancelacionAsignaturaCUIntPuerto {

    SolicitudCancelacionAsignatura rechazarPorFuncionario(String uuidSolicitudAcademica, String observacion, String token);

    SolicitudCancelacionAsignatura remitirADecano(String uuidSolicitudAcademica, List<EvaluacionAsignatura> evaluaciones,
                                                  String observacion, String token);

    SolicitudCancelacionAsignatura aprobarPorDecano(String uuidSolicitudAcademica, Boolean aceptarEvaluacion, List<DecisionAsignatura> decisiones,
                                                    String observacion, String token);

    SolicitudCancelacionAsignatura rechazarPorDecano(String uuidSolicitudAcademica, String observacion, String token);

    SolicitudCancelacionAsignatura enviarRespuesta(String uuidSolicitudAcademica, String observacion, List<AjusteAsignatura> ajustes,
                                                   ComunicacionRespuesta comunicacion, String token);
}
