package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenSolicitudAcademica {
    private SolicitudAcademica solicitudAcademica;
    private String etiqueta;
}
