package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudAcademica;

public interface AsignacionFuncionarioAcademicoCUIntPuerto {

    TipoSolicitudAcademica asignarFuncionarioAcademico(String uuidTipoSolicitudAcademica, String uuidFuncionarioAcademico, String token);
}
