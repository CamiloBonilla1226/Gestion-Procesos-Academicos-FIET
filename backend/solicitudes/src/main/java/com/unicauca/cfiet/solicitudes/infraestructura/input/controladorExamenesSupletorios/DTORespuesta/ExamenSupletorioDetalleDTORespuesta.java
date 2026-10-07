package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.DTORespuesta;

import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta.SolicitudAcademicaDetalleDTORespuesta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExamenSupletorioDetalleDTORespuesta {
    private SolicitudAcademicaDetalleDTORespuesta solicitud;
    private AsignaturaSupletorioDTORespuesta asignatura;
    private String tipoCausa;
    private String fechaExamenNoPresentado;
    private CruceSupletorioDTORespuesta cruce;
    private String fechaAcordadaExamen;
}
