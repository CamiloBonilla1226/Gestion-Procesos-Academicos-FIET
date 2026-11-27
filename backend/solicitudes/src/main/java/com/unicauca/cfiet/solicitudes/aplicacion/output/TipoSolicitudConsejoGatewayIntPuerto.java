package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudConsejo;

public interface TipoSolicitudConsejoGatewayIntPuerto {

    TipoSolicitudConsejo getPorTipoSolicitud(String uuidTipoSolicitud);

    TipoSolicitudConsejo getPorTipoAcademico(String uuidTipoSolicitudAcademica);

    TipoSolicitudConsejo guardar(TipoSolicitudConsejo enlace);

    void eliminarPorTipoSolicitud(String uuidTipoSolicitud);
}
