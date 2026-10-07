package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.DetalleExamenSupletorio;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FormularioExamenSupletorio;

public interface ConsultaExamenSupletorioCUIntPuerto {

    String getEstudianteAutenticado(String token);

    FormularioExamenSupletorio getFormulario(String token);

    DetalleExamenSupletorio getDetalle(String uuidSolicitudAcademica, String token);
}
