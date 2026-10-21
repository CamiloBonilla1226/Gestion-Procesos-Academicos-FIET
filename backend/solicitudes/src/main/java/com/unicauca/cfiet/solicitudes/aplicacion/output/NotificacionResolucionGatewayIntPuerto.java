package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.EstadoNotificacionResolucion;
import com.unicauca.cfiet.solicitudes.dominio.modelos.NotificacionResolucion;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ProgramacionResolucion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface NotificacionResolucionGatewayIntPuerto {

    NotificacionResolucion crear(NotificacionResolucion notificacion);

    NotificacionResolucion getPorSolicitud(String uuidSolicitudAcademica);

    boolean estaRetenida(String uuidSolicitudAcademica);

    Set<String> getRetenidas(List<String> uuidsSolicitud);

    List<String> getPendientes(LocalDateTime ahora, LocalDateTime reclamoVencido);

    boolean reclamar(String uuidSolicitudAcademica, LocalDateTime ahora, LocalDateTime reclamoVencido);

    boolean actualizarProgramacion(String uuidSolicitudAcademica, ProgramacionResolucion programacion, LocalDateTime ahora);

    boolean marcarFirmada(String uuidSolicitudAcademica, LocalDateTime ahora);

    boolean reintentar(String uuidSolicitudAcademica);

    void marcarEnvioEstudiante(String uuidSolicitudAcademica, LocalDateTime fecha);

    void marcarEnvioDarca(String uuidSolicitudAcademica, LocalDateTime fecha);

    void registrarResultado(String uuidSolicitudAcademica, EstadoNotificacionResolucion estado, int intentos, String ultimoError,
                            LocalDateTime fechaEnvio);
}
