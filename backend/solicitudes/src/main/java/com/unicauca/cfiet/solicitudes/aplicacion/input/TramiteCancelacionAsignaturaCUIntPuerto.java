package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.DecisionAsignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EvaluacionAsignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudCancelacionAsignatura;

import java.util.List;

public interface TramiteCancelacionAsignaturaCUIntPuerto {

    SolicitudCancelacionAsignatura rechazarPorFuncionario(String uuidSolicitudAcademica, String observacion, String token);

    SolicitudCancelacionAsignatura remitirADecano(String uuidSolicitudAcademica, List<EvaluacionAsignatura> evaluaciones,
                                                  String observacion, String token);

    SolicitudCancelacionAsignatura aprobarPorDecano(String uuidSolicitudAcademica, List<DecisionAsignatura> decisiones, String token);

    SolicitudCancelacionAsignatura rechazarPorDecano(String uuidSolicitudAcademica, String observacion, String token);

    SolicitudCancelacionAsignatura enviarRespuesta(String uuidSolicitudAcademica, String token);
}
