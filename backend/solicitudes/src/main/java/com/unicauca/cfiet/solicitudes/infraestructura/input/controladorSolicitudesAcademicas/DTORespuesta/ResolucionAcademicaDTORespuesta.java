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
public class ResolucionAcademicaDTORespuesta {
    private String uuidSolicitudAcademica;
    private String nombreArchivo;
    private LocalDateTime fechaSubida;
}
