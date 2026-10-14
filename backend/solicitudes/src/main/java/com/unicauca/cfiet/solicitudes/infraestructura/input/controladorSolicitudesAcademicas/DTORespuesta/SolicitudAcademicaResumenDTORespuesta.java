package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
    private String etapaCodigo;
    private String nombreEstudiante;
    private String codigoEstudiantil;
    private String numeroDocumento;
    private String decisionDecano;
    private List<ResultadoAsignaturaDTORespuesta> resultadosAsignaturas;
}
