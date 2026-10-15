package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenRespuesta {
    private String radicado;
    private String tipoSolicitud;
    private LocalDate fechaRespuesta;
    private String estudiante;
    private String codigoEstudiantil;
    private String programa;
    private String estadoFinal;
    @Builder.Default
    private List<DatoResumenRespuesta> detalles = new ArrayList<>();
    @Builder.Default
    private List<AsignaturaResumenRespuesta> asignaturas = new ArrayList<>();
}
