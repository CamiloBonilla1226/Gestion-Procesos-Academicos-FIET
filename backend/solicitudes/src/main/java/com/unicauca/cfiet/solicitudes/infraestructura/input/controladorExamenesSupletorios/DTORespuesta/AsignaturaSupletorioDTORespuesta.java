package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AsignaturaSupletorioDTORespuesta {
    private String uuidAsignaturaMatriculada;
    private String codigoAsignatura;
    private String nombreAsignatura;
    private String grupo;
}
