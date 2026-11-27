package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilaResolucionDTORespuesta {
    private String asignatura;
    private String faltas;
    private String nota;
    private String estadoAlSolicitar;
    private String estadoCancelacion;
    private String estadoNuevaMatricula;
}
