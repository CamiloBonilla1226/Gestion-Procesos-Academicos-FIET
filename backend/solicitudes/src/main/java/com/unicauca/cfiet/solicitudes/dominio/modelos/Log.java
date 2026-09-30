package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

/**
 * Representa un registro de actividad o evento en el sistema.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Log {
    private String uuidLog;
    private String accion;
    private String fecha;
    private String resultado;
    private Usuario objUsuarioLog;
}
