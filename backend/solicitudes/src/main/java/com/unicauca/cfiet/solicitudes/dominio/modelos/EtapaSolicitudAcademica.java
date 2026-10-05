package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtapaSolicitudAcademica {
    private String uuidEtapa;
    private String codigo;
    private TipoSolicitudAcademica tipoSolicitudAcademica;
}
