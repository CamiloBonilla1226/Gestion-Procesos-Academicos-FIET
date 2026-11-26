package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTOPeticion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormatoCompuestoDTOPeticion {
    private List<ValorProvisionalDTOPeticion> valoresProvisionales = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ValorProvisionalDTOPeticion {
        private String asignaturaSolicitudUuid;
        private String situacionMatriculaUuid;
        private String situacionCancelarUuid;
        private String numeroFaltas;
        private String nota;
        private Boolean aprobada;
    }
}
