package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionAsignatura {
    private String uuidAsignaturaSolicitud;
    private Integer numeroFaltas;
    private BigDecimal nota;
    private String uuidSituacionMatricula;
}
