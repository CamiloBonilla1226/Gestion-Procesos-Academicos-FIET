package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Entity
@Table(name = "solicitudes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudEntidad {
    @Id
    @Column(length = 150)
    private String uuidSolicitud;
    @Column(length = 1000)
    private String consecutivo;
    @Column(length = 1000)
    private String nombre;
    @Column(length = 1500)
    private String descripcion;
    @Column(nullable = false, length = 1000)
    private String estado;
    @ManyToOne
    @JoinColumn(name = "uuidTipoSolicitud", nullable = false)
    private TipoSolicitudEntidad objTipoSolicitud;
    @OneToMany(
            fetch = FetchType.LAZY,
            mappedBy = "objSolicitud",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<AnexoEntidad> anexos;
    @ManyToOne
    @JoinColumn(name = "uuidOrdenDelDia")
    private OrdenDelDiaEntidad objOrdenDelDia;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "uuidInformacionSolicitante", referencedColumnName = "uuidInformacionSolicitante")
    private InformacionSolicitanteEntidad informacionSolicitante;
    @ManyToOne
    @JoinColumn(name = "uuidUsuario")
    private FuncionarioEntidad objFuncionario;
    @OneToOne(mappedBy = "solicitud", fetch = FetchType.LAZY)
    private RespuestaEntidad respuesta;
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
