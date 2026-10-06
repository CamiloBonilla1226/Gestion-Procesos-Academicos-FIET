package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.EvaluacionAsignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SituacionCancelarAsignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudCancelacionMatricula;

import java.util.List;

public interface TramiteCancelacionMatriculaCUIntPuerto {

    SolicitudCancelacionMatricula rechazarPorFuncionario(String uuidSolicitudAcademica, String observacion, String token);

    SolicitudCancelacionMatricula remitirADecano(String uuidSolicitudAcademica, List<EvaluacionAsignatura> evaluaciones,
                                                 String observacion, String token);

    SolicitudCancelacionMatricula aprobarPorDecano(String uuidSolicitudAcademica, List<SituacionCancelarAsignatura> situacionesAlCancelar,
                                                   String token);

    SolicitudCancelacionMatricula rechazarPorDecano(String uuidSolicitudAcademica, String observacion, String token);

    SolicitudCancelacionMatricula enviarRespuesta(String uuidSolicitudAcademica, String token);
}
