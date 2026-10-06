package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AsignaturaElegibleDTORespuesta {
    private String uuidAsignaturaMatriculada;
    private String codigoAsignatura;
    private String nombreAsignatura;
    private String grupo;
}
