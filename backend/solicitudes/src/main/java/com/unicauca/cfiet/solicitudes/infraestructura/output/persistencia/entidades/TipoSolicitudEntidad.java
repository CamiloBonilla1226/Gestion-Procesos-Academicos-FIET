package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Entity
@Table(name = "tiposSolicitudes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoSolicitudEntidad {
    @Id
    @Column(length = 150)
    private String uuidTipoSolicitud;
    @Column(nullable = false, length = 1000)
    private String nombre;
    @Column(length = 1500)
    private String descripcion;
    @Column(nullable = false, length = 1000)
    private String seccion;
    @Column(nullable = false, length = 1000)
    private String perfilSolicitante;
    @OneToMany(
            fetch = FetchType.LAZY,
            mappedBy = "objTipoSolicitud",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<TipoAnexoEntidad> anexos;
    @ManyToOne
    @JoinColumn(name = "uuidUsuario")
    private FuncionarioEntidad objFuncionarioEncargado;
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }

}
