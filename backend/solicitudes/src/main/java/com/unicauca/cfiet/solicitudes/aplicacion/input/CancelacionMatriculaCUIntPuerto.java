package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.AnexoRadicacion;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudCancelacionMatricula;

import java.util.List;

public interface CancelacionMatriculaCUIntPuerto {

    SolicitudCancelacionMatricula radicarCancelacionMatricula(String uuidEstudiante, String motivoCancelacion,
                                                              List<AnexoRadicacion> anexos, String token);
}
