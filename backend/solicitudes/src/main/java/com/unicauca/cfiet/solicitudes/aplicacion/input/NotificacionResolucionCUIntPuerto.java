package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.ArchivoAdjunto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ComunicacionRespuesta;
import com.unicauca.cfiet.solicitudes.dominio.modelos.NotificacionResolucion;
import com.unicauca.cfiet.solicitudes.dominio.modelos.PlantillaNotificacionResolucion;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ProgramacionResolucion;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudAcademica;

public interface NotificacionResolucionCUIntPuerto {

    void validarComunicacion(ComunicacionRespuesta comunicacion);

    NotificacionResolucion programar(SolicitudAcademica solicitud, ComunicacionRespuesta comunicacion, String token);

    PlantillaNotificacionResolucion getPlantilla(String uuidSolicitudAcademica, String token);

    NotificacionResolucion getNotificacion(String uuidSolicitudAcademica, String token);

    NotificacionResolucion reprogramar(String uuidSolicitudAcademica, ProgramacionResolucion programacion, String token);

    NotificacionResolucion registrarFirma(String uuidSolicitudAcademica, ArchivoAdjunto archivo, String textoDarca, String token);

    NotificacionResolucion reintentar(String uuidSolicitudAcademica, String token);

    int procesarPendientes();
}
