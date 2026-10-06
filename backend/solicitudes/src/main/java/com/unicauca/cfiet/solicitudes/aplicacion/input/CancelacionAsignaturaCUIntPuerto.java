package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.AnexoRadicacion;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudCancelacionAsignatura;

import java.util.List;

public interface CancelacionAsignaturaCUIntPuerto {

    SolicitudCancelacionAsignatura radicarCancelacionAsignatura(String uuidEstudiante, String motivoCancelacion,
                                                                List<String> uuidsAsignaturaMatriculada,
                                                                List<AnexoRadicacion> anexos, String token);
}
