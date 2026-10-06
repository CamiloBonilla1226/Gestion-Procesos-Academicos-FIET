package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.ActorSolicitud;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AnexoAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ArchivoAdjunto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.CausaSupletorio;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoAnexoAcademico;

import java.util.List;

public interface AnexoAcademicoCUIntPuerto {

    AnexoAcademico adjuntarAnexo(String uuidSolicitudAcademica, String uuidTipoAnexoAcademico, ArchivoAdjunto archivo, ActorSolicitud actor, String token);

    ArchivoAdjunto obtenerAnexo(String uuidAnexoAcademico, ActorSolicitud actor);

    List<TipoAnexoAcademico> getAnexosObligatorios(String uuidTipoSolicitudAcademica, CausaSupletorio causa);

    List<TipoAnexoAcademico> verificarAnexosObligatorios(String uuidSolicitudAcademica, CausaSupletorio causa);
}
