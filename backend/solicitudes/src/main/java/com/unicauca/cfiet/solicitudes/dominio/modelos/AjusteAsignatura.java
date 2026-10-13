package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AjusteAsignatura {
    private String uuidAsignaturaSolicitud;
    private String uuidSituacionCancelar;
    private String observacionDecision;
}
