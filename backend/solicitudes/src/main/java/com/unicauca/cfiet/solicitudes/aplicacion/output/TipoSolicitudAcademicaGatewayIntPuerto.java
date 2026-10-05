package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudAcademica;

import java.util.List;

public interface TipoSolicitudAcademicaGatewayIntPuerto {

    List<TipoSolicitudAcademica> getTodos();

    boolean existePorUuid(String uuidTipoSolicitudAcademica);

    TipoSolicitudAcademica getPorUuid(String uuidTipoSolicitudAcademica);

    TipoSolicitudAcademica asignarFuncionarioAcademico(String uuidTipoSolicitudAcademica, String uuidFuncionarioAcademico);
}
