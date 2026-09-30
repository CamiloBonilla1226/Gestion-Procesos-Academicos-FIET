package com.unicauca.cfiet.solicitudes.dominio.modelos;

import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import lombok.*;

/**
 * Representa la respuesta de una solicitud.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Respuesta {
    private String uuidRespuesta;
    private String tipoRespuesta;
    private String consecutivoFiet;
    private String respuestaConsejo;
    private String indicaciones;
    private Solicitud solicitud;
    private String urlRespuesta;

    public boolean esValidoTipoRespuesta(){
        if (tipoRespuesta == null)
            return false;
        return ApplicationConstantes.OFICIO.equals(tipoRespuesta) || ApplicationConstantes.RESOLUCION.equals(tipoRespuesta);
    }
}
