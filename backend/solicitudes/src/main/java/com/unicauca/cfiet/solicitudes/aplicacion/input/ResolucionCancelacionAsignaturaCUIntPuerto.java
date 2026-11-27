package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.AjusteAsignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ArchivoAdjunto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.BorradorResolucion;

import java.util.List;

public interface ResolucionCancelacionAsignaturaCUIntPuerto {

    List<BorradorResolucion> getBorradores(String uuidSolicitudAcademica, List<AjusteAsignatura> ajustes, String token);

    ArchivoAdjunto generarPdf(String uuidSolicitudAcademica, BorradorResolucion borrador, String token);
}
