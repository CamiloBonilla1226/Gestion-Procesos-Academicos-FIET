package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Entity
@Table(name = "logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogEntidad {
    @Id
    @Column(length = 150)
    private String uuidLog;
    @Column(nullable = false, length = 1000)
    private String accion;
    @Column(nullable = false, length = 1000)
    private String fecha;
    @Column(nullable = false, length = 1000)
    private String resultado;
    @ManyToOne
    @JoinColumn(name = "uuidUsuario", nullable = false)
    private UsuarioEntidad objUsuarioLog;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
