package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Entity
@Table(name = "anexos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnexoEntidad {
    @Id
    @Column(length = 150)
    private String uuidAnexo;
    @Column(nullable = false, length = 1000)
    private String nombre;
    @Column(nullable = false, length = 3000)
    private String urlAnexo;
    @ManyToOne
    @JoinColumn(name = "uuidSolicitud", nullable = false)
    private SolicitudEntidad objSolicitud;
}
