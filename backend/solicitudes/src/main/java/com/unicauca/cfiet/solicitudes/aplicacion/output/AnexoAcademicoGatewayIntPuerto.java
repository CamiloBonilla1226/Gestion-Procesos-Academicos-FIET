package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.AnexoAcademico;

import java.util.List;

public interface AnexoAcademicoGatewayIntPuerto {

    AnexoAcademico guardar(AnexoAcademico anexo);

    AnexoAcademico getPorUuid(String uuidAnexoAcademico);

    List<AnexoAcademico> getPorSolicitud(String uuidSolicitudAcademica);
}
