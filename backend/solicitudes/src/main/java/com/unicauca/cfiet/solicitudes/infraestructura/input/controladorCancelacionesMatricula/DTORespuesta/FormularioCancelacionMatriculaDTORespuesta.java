package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTORespuesta;

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
public class FormularioCancelacionMatriculaDTORespuesta {
    @Builder.Default
    private List<AnexoRequeridoDTORespuesta> anexosRequeridos = new ArrayList<>();
    @Builder.Default
    private List<AsignaturaFormularioDTORespuesta> asignaturas = new ArrayList<>();
}
