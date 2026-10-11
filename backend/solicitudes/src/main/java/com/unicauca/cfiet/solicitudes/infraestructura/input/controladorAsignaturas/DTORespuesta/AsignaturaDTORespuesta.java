package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AsignaturaDTORespuesta {
    private String uuidAsignatura;
    private String codigoAsignatura;
    private String nombreAsignatura;
    private String uuidProgramaAcademico;
    private String programaAcademico;
}
