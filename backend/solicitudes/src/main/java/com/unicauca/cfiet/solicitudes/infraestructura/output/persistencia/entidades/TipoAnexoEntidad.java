package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Entity
@Table(name = "tiposAnexos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoAnexoEntidad {
    @Id
    @Column(length = 150)
    private String uuidTipoAnexo;
    @Column(nullable = false, length = 1000)
    private String nombre;
    @Column(length = 1500)
    private String descripcion;
    @Column(nullable = false, length = 1000)
    private String formato;
    @Column(nullable = false)
    private Boolean obligatoriedad;
    @ManyToOne
    @JoinColumn(name = "uuidTipoSolicitud", nullable = false)
    private TipoSolicitudEntidad objTipoSolicitud;
}
