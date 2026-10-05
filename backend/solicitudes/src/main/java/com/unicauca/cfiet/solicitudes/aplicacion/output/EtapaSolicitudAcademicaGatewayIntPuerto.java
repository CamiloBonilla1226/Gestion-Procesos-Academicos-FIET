package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.EtapaSolicitudAcademica;

import java.util.List;

public interface EtapaSolicitudAcademicaGatewayIntPuerto {

    List<EtapaSolicitudAcademica> getPorTipoIncluyendoUniversales(String uuidTipoSolicitudAcademica);
}
