package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorNotificacionesDocente.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionDocenteResumenDTORespuesta {
    private String uuidNotificacion;
    private String titulo;
    private String uuidSolicitudAcademica;
    private String radicado;
    private String estudiante;
    private String asignatura;
    private String resultado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaLectura;
    private boolean leida;
}
