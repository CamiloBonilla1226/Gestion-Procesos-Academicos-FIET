package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.EncargadoTipoSolicitud;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudConsejo;

import java.util.List;

public interface TipoSolicitudConsejoCUIntPuerto {

    TipoSolicitudConsejo prepararEnlace(String uuidTipoSolicitud, String uuidProcesoAcademico, String uuidEncargado);

    TipoSolicitudConsejo guardarEnlace(String uuidTipoSolicitud, TipoSolicitudConsejo enlace, String token);

    TipoSolicitudConsejo getEnlace(String uuidTipoSolicitud);

    List<EncargadoTipoSolicitud> getEncargados(String uuidProcesoAcademico);
}
