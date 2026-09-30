package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Entity
@Table(name = "tiposUsuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoUsuarioEntidad {
    @Id
    @Column(length = 150)
    private String uuidTipoUsuario;
    @Column(nullable = false, unique = true)
    private String nombre;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "objTipoUsuario")
    private List<UsuarioEntidad> usuarios;
}
