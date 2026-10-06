package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.ResolucionAcademica;

public interface ResolucionAcademicaGatewayIntPuerto {

    ResolucionAcademica getPorSolicitud(String uuidSolicitudAcademica);

    boolean existePorSolicitud(String uuidSolicitudAcademica);

    ResolucionAcademica guardar(ResolucionAcademica resolucion);
}
