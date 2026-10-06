package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleSolicitudAcademica {
    private SolicitudAcademica solicitudAcademica;
    private String etiqueta;
    @Builder.Default
    private List<AnexoAcademico> anexos = new ArrayList<>();
    private boolean tieneResolucion;
    private boolean puedeDescargarResolucion;
    @Builder.Default
    private List<AccionEtapa> accionesDisponibles = new ArrayList<>();
}
