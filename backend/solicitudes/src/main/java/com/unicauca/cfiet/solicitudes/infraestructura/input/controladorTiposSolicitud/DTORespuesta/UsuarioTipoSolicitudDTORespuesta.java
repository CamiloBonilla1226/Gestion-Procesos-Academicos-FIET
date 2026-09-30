package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorTiposSolicitud.DTORespuesta;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
public class UsuarioTipoSolicitudDTORespuesta extends  UsuarioLivianoTipoSolicitudDTORespuesta{
    private String correoElectronico;
}
