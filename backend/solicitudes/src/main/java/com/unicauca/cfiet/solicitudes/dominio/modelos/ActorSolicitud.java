package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActorSolicitud {
    private String uuidUsuario;
    private RolEtiquetaEtapa rol;
}
