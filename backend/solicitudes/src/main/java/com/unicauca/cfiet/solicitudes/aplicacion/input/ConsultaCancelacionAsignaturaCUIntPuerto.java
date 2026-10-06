package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.DetalleCancelacionAsignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FormularioCancelacionAsignatura;

public interface ConsultaCancelacionAsignaturaCUIntPuerto {

    String getEstudianteAutenticado(String token);

    FormularioCancelacionAsignatura getFormulario(String token);

    DetalleCancelacionAsignatura getDetalle(String uuidSolicitudAcademica, String token);
}
