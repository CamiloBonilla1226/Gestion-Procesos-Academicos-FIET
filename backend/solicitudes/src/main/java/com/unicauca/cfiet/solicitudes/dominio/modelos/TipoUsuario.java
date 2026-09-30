package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;
import java.util.List;

/**
 * Representa un tipo de usuario en el sistema, utilizado para clasificar a los usuarios.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoUsuario {
    private String uuidTipoUsuario;
    private String nombre;
    private List<Usuario> usuarios;
}
