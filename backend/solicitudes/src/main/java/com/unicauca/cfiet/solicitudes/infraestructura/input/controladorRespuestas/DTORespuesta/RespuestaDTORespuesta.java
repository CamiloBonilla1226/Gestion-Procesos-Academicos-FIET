package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorRespuestas.DTORespuesta;

import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudes.DTORespuesta.SolicitudDTORespuesta;
import lombok.*;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespuestaDTORespuesta {
    private String uuidRespuesta;
    private String tipoRespuesta;
    private String consecutivoFiet;
    private String respuestaConsejo;
    private String indicaciones;
    private SolicitudDTORespuesta solicitud;
    private String urlRespuesta;
}
