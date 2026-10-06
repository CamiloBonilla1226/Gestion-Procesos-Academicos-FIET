package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTORespuesta;

import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTORespuesta.SituacionAsignaturaDTORespuesta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AsignaturaSolicitadaDTORespuesta {
    private String uuidAsignaturaSolicitud;
    private String codigoAsignatura;
    private String nombreAsignatura;
    private Integer numeroFaltas;
    private BigDecimal nota;
    private SituacionAsignaturaDTORespuesta situacionMatricula;
    private Boolean cumpleCondiciones;
    private String observacionEvaluacion;
    private Boolean aprobadaPorDecano;
    private String observacionDecision;
    private SituacionAsignaturaDTORespuesta situacionCancelar;
}
