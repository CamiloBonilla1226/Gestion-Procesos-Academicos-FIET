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
public class SolicitudAcademicaResumenDTORespuesta {
    private String uuidSolicitudAcademica;
    private String radicado;
    private String uuidTipoSolicitudAcademica;
    private String tipoSolicitud;
    private LocalDateTime fechaCreacion;
    private String etiqueta;
    private String nombreEstudiante;
    private String codigoEstudiantil;
}
