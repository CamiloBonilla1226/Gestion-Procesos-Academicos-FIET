package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.EstadoNotificacionResolucion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface EliminacionRespuestasGatewayIntPuerto {

    List<String> getRespuestasEliminables(String uuidFuncionario, Set<String> etapasFinales, Set<EstadoNotificacionResolucion> estadosBloqueantes,
                                          LocalDateTime desde, LocalDateTime hasta);

    List<String> eliminar(List<String> uuidsSolicitud);
}
