package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoAnexoAcademico;

import java.util.List;

public interface TipoAnexoAcademicoCUIntPuerto {

    List<TipoAnexoAcademico> getTiposAnexoPorTipo(String uuidTipoSolicitudAcademica);
}
