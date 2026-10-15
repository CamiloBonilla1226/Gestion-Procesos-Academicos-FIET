package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.ArchivoAdjunto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.DetalleNotificacionDocente;

import java.util.List;

public interface NotificacionDocenteCUIntPuerto {

    List<DetalleNotificacionDocente> getMisNotificaciones(String token);

    DetalleNotificacionDocente getNotificacion(String uuidNotificacion, String token);

    DetalleNotificacionDocente marcarLeida(String uuidNotificacion, String token);

    ArchivoAdjunto descargarAnexo(String uuidNotificacion, String uuidAnexoAcademico, String token);
}
