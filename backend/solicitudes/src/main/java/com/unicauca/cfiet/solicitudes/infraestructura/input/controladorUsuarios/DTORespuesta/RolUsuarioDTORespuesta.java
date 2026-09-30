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
public class RolUsuarioDTORespuesta {
    private String uuidRol;
    private String nombre;
    private String descripcion;
    private Boolean estado;
}
