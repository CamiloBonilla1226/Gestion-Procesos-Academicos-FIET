package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudAcademicaDetalleDTORespuesta {
    private String uuidSolicitudAcademica;
    private String radicado;
    private String uuidTipoSolicitudAcademica;
    private String tipoSolicitud;
    private LocalDateTime fechaCreacion;
    private String etiqueta;
    private EstudianteSolicitudDTORespuesta estudiante;
    @Builder.Default
    private List<AnexoAcademicoDTORespuesta> anexos = new ArrayList<>();
    private boolean tieneResolucion;
    private boolean puedeDescargarResolucion;
    @Builder.Default
    private List<String> accionesDisponibles = new ArrayList<>();
}
