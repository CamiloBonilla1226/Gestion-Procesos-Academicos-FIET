package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoSolicitudConsejo {
    private String uuidTipoSolicitud;
    private TipoSolicitudAcademica tipoSolicitudAcademica;
    private FuncionarioAcademico encargadoAcademico;
}
