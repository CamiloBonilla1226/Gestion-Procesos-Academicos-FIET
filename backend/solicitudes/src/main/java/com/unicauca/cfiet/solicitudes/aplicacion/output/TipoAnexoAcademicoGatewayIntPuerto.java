package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoAnexoAcademico;

import java.util.List;

public interface TipoAnexoAcademicoGatewayIntPuerto {

    List<TipoAnexoAcademico> getPorTipo(String uuidTipoSolicitudAcademica);
}
