package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTOPeticion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EvaluacionCancelacionAsignaturaDTOPeticion {
    private String asignaturaSolicitudUuid;
    private Integer numeroFaltas;
    private BigDecimal nota;
    private String situacionMatriculaUuid;
    private Boolean cumpleCondiciones;
    private String observacionEvaluacion;
}
