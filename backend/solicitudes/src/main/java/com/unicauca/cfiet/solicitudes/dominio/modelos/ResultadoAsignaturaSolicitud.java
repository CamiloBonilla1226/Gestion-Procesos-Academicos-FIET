package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoAsignaturaSolicitud {
    private String asignaturaSolicitudUuid;
    private boolean aprobada;
}
