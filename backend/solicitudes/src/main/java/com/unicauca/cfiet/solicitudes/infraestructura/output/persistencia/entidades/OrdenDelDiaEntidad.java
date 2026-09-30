package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Entity
@Table(name = "ordenesDelDia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenDelDiaEntidad {
    @Id
    @Column(length = 150)
    private String uuidOrdenDelDia;
    @Column(nullable = false, length = 1000)
    private String nombre;
    @Column(length = 1500)
    private String descripcion;
    @Column(length = 1000)
    private String ciudad;
    @Column(length = 1000)
    private String fecha;
    @Column(length = 1000)
    private String horaInicio;
    @Column(length = 1000)
    private String horaFin;
    @Column(length = 1000)
    private String lugarReunion;
    @Column(nullable = false, length = 1000)
    private String numeroActa;
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    @Column(nullable = false)
    private boolean estado;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
