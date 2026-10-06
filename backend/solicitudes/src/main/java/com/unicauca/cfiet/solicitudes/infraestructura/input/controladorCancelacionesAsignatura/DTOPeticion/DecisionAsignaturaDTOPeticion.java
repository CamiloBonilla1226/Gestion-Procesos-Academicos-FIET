package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTOPeticion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DecisionAsignaturaDTOPeticion {
    private String asignaturaSolicitudUuid;
    private Boolean aprobada;
    private String situacionCancelarUuid;
    private String observacionDecision;
}
