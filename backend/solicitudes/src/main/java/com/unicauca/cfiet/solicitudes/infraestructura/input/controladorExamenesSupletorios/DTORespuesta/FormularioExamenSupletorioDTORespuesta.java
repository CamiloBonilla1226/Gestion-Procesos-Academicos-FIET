package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.DTORespuesta;

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
public class FormularioExamenSupletorioDTORespuesta {
    @Builder.Default
    private List<AsignaturaSupletorioDTORespuesta> asignaturas = new ArrayList<>();
    @Builder.Default
    private List<String> causas = new ArrayList<>();
    @Builder.Default
    private List<AnexoSupletorioDTORespuesta> anexos = new ArrayList<>();
    private Integer plazoDiasHabiles;
}
