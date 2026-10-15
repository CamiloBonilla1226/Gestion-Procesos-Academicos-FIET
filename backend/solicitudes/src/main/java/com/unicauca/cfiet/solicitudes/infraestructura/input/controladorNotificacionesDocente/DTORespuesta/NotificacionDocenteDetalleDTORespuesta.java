package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorNotificacionesDocente.DTORespuesta;

import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.DTORespuesta.AsignaturaSupletorioDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.DTORespuesta.CruceSupletorioDTORespuesta;
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
public class NotificacionDocenteDetalleDTORespuesta {
    private NotificacionDocenteResumenDTORespuesta notificacion;
    private String tipoSolicitud;
    private LocalDateTime fechaRadicacion;
    private String nombresEstudiante;
    private String apellidosEstudiante;
    private String codigoEstudiantil;
    private String programaAcademico;
    private AsignaturaSupletorioDTORespuesta asignatura;
    private String tipoCausa;
    private String fechaExamenNoPresentado;
    private CruceSupletorioDTORespuesta cruce;
    @Builder.Default
    private List<AnexoNotificacionDTORespuesta> anexos = new ArrayList<>();
}
