package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudCancelacionMatricula;

import java.util.List;

public interface SolicitudCancelacionMatriculaGatewayIntPuerto {

    SolicitudCancelacionMatricula guardar(SolicitudCancelacionMatricula cancelacion);

    SolicitudCancelacionMatricula getPorSolicitud(String uuidSolicitudAcademica);

    List<AsignaturaSolicitudAcademica> guardarAsignaturas(String uuidSolicitudAcademica, List<AsignaturaSolicitudAcademica> asignaturas);

    List<AsignaturaSolicitudAcademica> actualizarAsignaturas(List<AsignaturaSolicitudAcademica> asignaturas);

    void cambiarEstadoAsignaturasMatriculadas(List<String> uuidsAsignaturaMatriculada, String estado);
}
