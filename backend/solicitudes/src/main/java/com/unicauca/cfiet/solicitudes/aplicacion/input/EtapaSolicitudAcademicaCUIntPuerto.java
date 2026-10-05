package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.EtapaEtiquetaRol;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EtapaSolicitudAcademica;

import java.util.List;

public interface EtapaSolicitudAcademicaCUIntPuerto {

    List<EtapaSolicitudAcademica> getEtapasPorTipo(String uuidTipoSolicitudAcademica);

    List<EtapaEtiquetaRol> getEtiquetasPorRol(String rol);
}
