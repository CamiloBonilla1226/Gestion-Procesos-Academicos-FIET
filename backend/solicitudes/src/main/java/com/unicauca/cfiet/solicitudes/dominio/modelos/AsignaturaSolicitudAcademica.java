package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignaturaSolicitudAcademica {
    private String uuidAsignaturaSolicitud;
    private AsignaturaMatriculada asignaturaMatriculada;
    private Integer numeroFaltas;
    private BigDecimal nota;
    private SituacionAcademicaAsignatura situacionMatricula;
    private SituacionAcademicaAsignatura situacionCancelar;
    private Boolean cumpleCondiciones;
    private String observacionEvaluacion;
    private Boolean aprobadaPorDecano;
    private String observacionDecision;
}
