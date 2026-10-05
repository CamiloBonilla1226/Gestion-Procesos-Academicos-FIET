package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AsignaturaDTORespuesta {
    private String uuidAsignatura;
    private String codigoAsignatura;
    private String nombreAsignatura;
}
