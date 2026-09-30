package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorTiposSolicitud.DTORespuesta;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
@NoArgsConstructor
public class TipoSolicitudDTORespuesta {
    private String uuidTipoSolicitud;
    private String nombre;
    private String descripcion;
    private String seccion;
    private String perfilSolicitante;
    private List<TipoAnexoDTORespuesta> anexos;
    private FuncionarioTipoSolicitudDTORespuesta objFuncionarioEncargado;
}
