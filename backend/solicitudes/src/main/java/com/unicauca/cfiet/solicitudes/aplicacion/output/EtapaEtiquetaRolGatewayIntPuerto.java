package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.EtapaEtiquetaRol;
import com.unicauca.cfiet.solicitudes.dominio.modelos.RolEtiquetaEtapa;

import java.util.List;

public interface EtapaEtiquetaRolGatewayIntPuerto {

    List<EtapaEtiquetaRol> getPorRol(RolEtiquetaEtapa rol);
}
