package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AsignaturaCancelacionDTORespuesta {
    private String uuidAsignaturaSolicitud;
    private String codigoAsignatura;
    private String nombreAsignatura;
    private Integer numeroFaltas;
    private BigDecimal nota;
    private SituacionAsignaturaDTORespuesta situacionMatricula;
    private SituacionAsignaturaDTORespuesta situacionCancelar;
}
