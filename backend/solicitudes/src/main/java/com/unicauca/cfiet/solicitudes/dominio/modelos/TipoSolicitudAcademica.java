package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoSolicitudAcademica {
    private String uuidTipoSolicitudAcademica;
    private String nombre;
    private String descripcion;
    private FuncionarioAcademico funcionarioAcademico;
}
