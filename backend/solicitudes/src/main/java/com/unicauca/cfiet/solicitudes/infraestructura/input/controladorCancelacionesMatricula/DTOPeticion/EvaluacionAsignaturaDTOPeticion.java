package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTOPeticion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EvaluacionAsignaturaDTOPeticion {
    private String asignaturaSolicitudUuid;
    private String numeroFaltas;
    private String nota;
    private String situacionMatriculaUuid;
    private String situacionCancelarUuid;
}
