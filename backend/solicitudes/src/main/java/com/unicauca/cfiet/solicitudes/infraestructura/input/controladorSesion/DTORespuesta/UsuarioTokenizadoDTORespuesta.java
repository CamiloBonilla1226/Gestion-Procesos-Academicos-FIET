package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSesion.DTORespuesta;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioTokenizadoDTORespuesta{
    private String uuidUsuario;
    private String token;
}
