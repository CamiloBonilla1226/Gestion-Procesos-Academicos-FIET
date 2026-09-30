package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorLog.DTORespuesta;

import lombok.*;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
@NoArgsConstructor
public class LogDTORespuesta {
    private String uuidLog;
    private String accion;
    private String fecha;
    private String resultado;
    private UsuarioLogDTORespuesta objUsuarioLog;
}