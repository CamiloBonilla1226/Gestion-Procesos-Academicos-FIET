package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoAnexoAcademico {
    private String uuidTipoAnexoAcademico;
    private TipoSolicitudAcademica tipoSolicitudAcademica;
    private String nombre;
    private String formatosPermitidos;
    private Boolean obligatorio;
}
