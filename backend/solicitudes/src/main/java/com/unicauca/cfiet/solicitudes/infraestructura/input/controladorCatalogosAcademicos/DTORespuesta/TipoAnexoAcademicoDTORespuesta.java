package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TipoAnexoAcademicoDTORespuesta {
    private String uuidTipoAnexoAcademico;
    private String uuidTipoSolicitudAcademica;
    private String nombre;
    private String formatosPermitidos;
    private Boolean obligatorio;
}
