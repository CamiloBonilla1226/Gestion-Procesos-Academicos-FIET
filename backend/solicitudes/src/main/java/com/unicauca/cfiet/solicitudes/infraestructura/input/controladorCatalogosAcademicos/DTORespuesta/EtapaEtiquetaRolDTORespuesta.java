package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EtapaEtiquetaRolDTORespuesta {
    private String uuidEtapa;
    private String codigoEtapa;
    private String rol;
    private String etiqueta;
}
