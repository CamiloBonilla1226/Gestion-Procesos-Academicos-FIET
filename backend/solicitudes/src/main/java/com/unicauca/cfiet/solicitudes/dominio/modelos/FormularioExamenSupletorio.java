package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormularioExamenSupletorio {
    @Builder.Default
    private List<AsignaturaMatriculada> asignaturas = new ArrayList<>();
    @Builder.Default
    private List<CausaSupletorio> causas = new ArrayList<>();
    @Builder.Default
    private List<AnexoSupletorio> anexos = new ArrayList<>();
    private Integer plazoDiasHabiles;
}
