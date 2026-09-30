package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Representa un usuario con información mínima.
 * Puede ser utilizado como base para otras clases de usuario.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UsuarioLiviano {
    private String uuidUsuario;
    private String nombres;
    private String apellidos;
    private Boolean estado;
}
