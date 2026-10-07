package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HistorialSolicitudAcademicaDTORespuesta {
    private String accion;
    private String etapaCodigo;
    private String observaciones;
    private LocalDateTime fecha;
    private String nombresUsuario;
    private String apellidosUsuario;
}
