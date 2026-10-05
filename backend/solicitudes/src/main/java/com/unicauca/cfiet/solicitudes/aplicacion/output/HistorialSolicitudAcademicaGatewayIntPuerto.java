package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.HistorialSolicitudAcademica;

import java.util.List;

public interface HistorialSolicitudAcademicaGatewayIntPuerto {

    List<HistorialSolicitudAcademica> getPorSolicitud(String uuidSolicitudAcademica);
}
