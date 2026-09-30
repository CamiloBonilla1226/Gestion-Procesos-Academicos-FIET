package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

/**
 * Representa un usuario junto con su token de autenticación.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioTokenizado{
    private String uuidUsuario;
    private String token;
}
