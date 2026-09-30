package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorTiposSolicitud.DTORespuesta;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
@NoArgsConstructor
public class TipoAnexoDTORespuesta {
    private String uuidTipoAnexo;
    private String nombre;
    private String descripcion;
    private String formato;
    private Boolean obligatoriedad;
}
