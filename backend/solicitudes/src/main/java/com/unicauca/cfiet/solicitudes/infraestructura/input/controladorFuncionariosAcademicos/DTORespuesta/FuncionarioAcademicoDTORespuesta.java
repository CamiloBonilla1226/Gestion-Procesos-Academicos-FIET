package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorFuncionariosAcademicos.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FuncionarioAcademicoDTORespuesta {
    private String uuidUsuario;
    private String nombres;
    private String apellidos;
    private Boolean estado;
    private String tipoDocumento;
    private String numeroDocumento;
    private String telefono;
    private String correoElectronico;
    private String username;
    private String dependencia;
    private List<TipoSolicitudAtendidaDTORespuesta> tiposSolicitud;
}
