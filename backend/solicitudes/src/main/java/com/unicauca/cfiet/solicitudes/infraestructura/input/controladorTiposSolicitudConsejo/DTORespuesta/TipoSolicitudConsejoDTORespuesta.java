package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorTiposSolicitudConsejo.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoSolicitudConsejoDTORespuesta {
    private String uuidTipoSolicitud;
    private String procesoAcademicoUuid;
    private String procesoAcademicoNombre;
    private EncargadoTipoSolicitudDTORespuesta encargadoAcademico;
}
