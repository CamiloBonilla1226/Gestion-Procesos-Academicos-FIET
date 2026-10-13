package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionAsignatura {
    private String uuidAsignaturaSolicitud;
    private String numeroFaltas;
    private String nota;
    private String uuidSituacionMatricula;
    private String uuidSituacionCancelar;
    private Boolean cumpleCondiciones;
    private String observacionEvaluacion;
}
