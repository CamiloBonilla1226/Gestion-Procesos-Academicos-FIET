package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TipoSolicitudAcademicaDTORespuesta {
    private String uuidTipoSolicitudAcademica;
    private String nombre;
    private String descripcion;
    private String uuidFuncionarioAcademico;
    private String nombreFuncionarioAcademico;
}
