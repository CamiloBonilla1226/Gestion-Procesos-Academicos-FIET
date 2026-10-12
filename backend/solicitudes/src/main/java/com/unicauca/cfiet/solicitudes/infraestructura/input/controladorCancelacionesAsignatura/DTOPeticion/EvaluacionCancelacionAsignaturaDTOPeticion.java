package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTOPeticion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EvaluacionCancelacionAsignaturaDTOPeticion {
    private String asignaturaSolicitudUuid;
    private String numeroFaltas;
    private String nota;
    private String situacionMatriculaUuid;
    private Boolean cumpleCondiciones;
    private String observacionEvaluacion;
}
