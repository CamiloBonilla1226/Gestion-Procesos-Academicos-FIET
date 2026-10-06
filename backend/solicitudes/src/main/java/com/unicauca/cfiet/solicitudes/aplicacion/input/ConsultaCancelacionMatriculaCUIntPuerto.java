package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.DetalleCancelacionMatricula;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FormularioCancelacionMatricula;

public interface ConsultaCancelacionMatriculaCUIntPuerto {

    String getEstudianteAutenticado(String token);

    FormularioCancelacionMatricula getFormulario(String token);

    DetalleCancelacionMatricula getDetalle(String uuidSolicitudAcademica, String token);
}
