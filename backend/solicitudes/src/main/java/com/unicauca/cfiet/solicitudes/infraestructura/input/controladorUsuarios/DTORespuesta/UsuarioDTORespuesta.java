package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.DTORespuesta;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
public class UsuarioDTORespuesta extends UsuarioLivianoDTORespuesta {
    private String tipoDocumento;
    private String numeroDocumento;
    private String telefono;
    private String correoElectronico;
    private String username;
    private TipoUsuarioDTORespuesta objTipoUsuario;
    private List<RolUsuarioDTORespuesta> roles;

    public UsuarioDTORespuesta(){
        super();
        this.roles = new ArrayList<>();
    }
}
