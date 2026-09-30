package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudes.DTORespuesta;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
@NoArgsConstructor
public class InformacionSolicitanteDTORespuesta {
    private String uuidInformacionSolicitante;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private String telefono;
    private String correoElectronico;
}
