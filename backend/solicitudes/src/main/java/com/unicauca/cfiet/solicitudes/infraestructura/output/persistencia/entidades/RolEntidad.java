package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolEntidad {
    @Id
    @Column(length = 150)
    private String uuidRol;
    @Column(nullable = false, unique = true)
    private String nombre;
    @Column(length = 1500)
    private String descripcion;
    @Column(nullable = false)
    private Boolean estado;
}
