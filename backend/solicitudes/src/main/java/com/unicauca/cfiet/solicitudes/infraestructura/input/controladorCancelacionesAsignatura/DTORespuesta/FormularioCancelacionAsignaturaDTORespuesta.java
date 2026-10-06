package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTORespuesta;

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
public class FormularioCancelacionAsignaturaDTORespuesta {
    @Builder.Default
    private List<AsignaturaElegibleDTORespuesta> asignaturas = new ArrayList<>();
    @Builder.Default
    private List<SoportePermitidoDTORespuesta> soportes = new ArrayList<>();
}
