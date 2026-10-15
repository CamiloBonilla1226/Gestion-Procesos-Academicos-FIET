package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.NotificacionDocente;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificacionDocenteGatewayIntPuerto {

    NotificacionDocente crear(NotificacionDocente notificacion);

    List<NotificacionDocente> getPorDocente(String uuidDocente);

    NotificacionDocente getPorUuid(String uuidNotificacion);

    NotificacionDocente marcarLeida(String uuidNotificacion, LocalDateTime fechaLectura);
}
