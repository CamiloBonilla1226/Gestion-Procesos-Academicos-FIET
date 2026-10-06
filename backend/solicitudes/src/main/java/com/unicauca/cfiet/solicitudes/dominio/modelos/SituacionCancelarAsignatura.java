package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SituacionCancelarAsignatura {
    private String uuidAsignaturaSolicitud;
    private String uuidSituacionCancelar;
}
