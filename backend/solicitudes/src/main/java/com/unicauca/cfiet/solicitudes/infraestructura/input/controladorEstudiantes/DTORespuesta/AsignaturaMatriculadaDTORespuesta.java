package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AsignaturaMatriculadaDTORespuesta {
    private String uuidAsignaturaMatriculada;
    private String uuidAsignatura;
    private String codigoAsignatura;
    private String nombreAsignatura;
    private String grupo;
    private String estado;
}
