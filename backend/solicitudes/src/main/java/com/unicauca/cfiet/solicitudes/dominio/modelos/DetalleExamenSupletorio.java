package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleExamenSupletorio {
    private DetalleSolicitudAcademica detalle;
    private SolicitudExamenSupletorio supletorio;
}
