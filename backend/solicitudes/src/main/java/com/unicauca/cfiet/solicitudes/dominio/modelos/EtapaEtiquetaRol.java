package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtapaEtiquetaRol {
    private EtapaSolicitudAcademica etapa;
    private RolEtiquetaEtapa rol;
    private String etiqueta;
}
