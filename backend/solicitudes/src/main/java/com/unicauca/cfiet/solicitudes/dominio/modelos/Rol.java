package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

/**
 * Representa un rol de usuario dentro del sistema.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {
    private String uuidRol;
    private String nombre;
    private String descripcion;
    private Boolean estado;
}
