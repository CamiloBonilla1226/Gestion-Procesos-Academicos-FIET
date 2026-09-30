package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Entity
@Table(name = "respuestas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespuestaEntidad {
    @Id
    @Column(length = 150)
    private String uuidRespuesta;
    @Column(nullable = false, length = 1000)
    private String tipoRespuesta;
    @Column(nullable = false, length = 1000)
    private String consecutivoFiet;
    @Column(nullable = false, length = 5000)
    private String respuestaConsejo;
    @Column(length = 5000)
    private String indicaciones;
    @OneToOne
    @JoinColumn(name = "uuidSolicitud", nullable = false, unique = true)
    private SolicitudEntidad solicitud;
    @Column(length = 3000)
    private String urlRespuesta;
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
