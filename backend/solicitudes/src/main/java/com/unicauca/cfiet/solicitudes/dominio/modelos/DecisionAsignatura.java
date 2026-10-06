package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionAsignatura {
    private String uuidAsignaturaSolicitud;
    private Boolean aprobada;
    private String uuidSituacionCancelar;
    private String observacionDecision;
}
