package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudes.DTORespuesta;

import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorTiposSolicitud.DTORespuesta.TipoSolicitudDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.DTORespuesta.UsuarioDTORespuesta;
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
public class SolicitudDTORespuesta {
    private String uuidSolicitud;
    private String consecutivo;
    private String nombre;
    private String descripcion;
    private String estado;
    private TipoSolicitudDTORespuesta tipoSolicitud;
    private List<AnexoDTORespuesta> anexos;
    private OrdenDelDiaDTORespuesta ordenDelDia;
    private InformacionSolicitanteDTORespuesta informacionSolicitante;
    private FuncionarioDTORespuesta objFuncionario;
}
