package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignaturaSolicitudAcademica {
    private String uuidAsignaturaSolicitud;
    private AsignaturaMatriculada asignaturaMatriculada;
    private String numeroFaltas;
    private String nota;
    private SituacionAcademicaAsignatura situacionMatricula;
    private SituacionAcademicaAsignatura situacionCancelar;
    private Boolean cumpleCondiciones;
    private String observacionEvaluacion;
    private Boolean aprobadaPorDecano;
    private String observacionDecision;

    public Boolean decisionCambiadaPorDecano() {
        if (aprobadaPorDecano == null || cumpleCondiciones == null) return null;
        return !aprobadaPorDecano.equals(cumpleCondiciones);
    }
}
