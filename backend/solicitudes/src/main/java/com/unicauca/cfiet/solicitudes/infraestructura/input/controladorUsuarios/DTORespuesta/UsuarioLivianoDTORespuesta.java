package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.DTORespuesta;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
@NoArgsConstructor
public class UsuarioLivianoDTORespuesta {
    private  String uuidUsuario;
    private String nombres;
    private String apellidos;
    private Boolean estado;
}