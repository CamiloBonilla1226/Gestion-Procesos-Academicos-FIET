package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTORespuesta;

import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta.SolicitudAcademicaDetalleDTORespuesta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CancelacionMatriculaDetalleDTORespuesta {
    private SolicitudAcademicaDetalleDTORespuesta solicitud;
    private String motivoCancelacion;
    @Builder.Default
    private List<AsignaturaCancelacionDTORespuesta> asignaturas = new ArrayList<>();
}
