package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SituacionAcademicaAsignaturaDTORespuesta {
    private String uuidSituacionAcademica;
    private String codigo;
    private String nombre;
}
