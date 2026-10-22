package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import com.unicauca.cfiet.solicitudes.dominio.modelos.EstadoNotificacionResolucion;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICACION_RESOLUCION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionResolucionEntidad implements Persistable<String> {
    @Id
    @Column(name = "SolicitudAcademica_uuid", length = 100)
    private String uuidSolicitudAcademica;
    @MapsId
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "SolicitudAcademica_uuid",
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_notifres_solacad")
    )
    private SolicitudAcademicaEntidad solicitudAcademica;
    @Column(name = "fechaProgramada", nullable = false, columnDefinition = "datetime")
    private LocalDateTime fechaProgramada;
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoNotificacionResolucion estado;
    @ColumnDefault("0")
    @Column(name = "firmada", nullable = false)
    private boolean firmada;
    @Column(name = "numeroResolucion", length = 20, nullable = false)
    private String numeroResolucion;
    @Column(name = "anioResolucion", nullable = false)
    private Integer anioResolucion;
    @Column(name = "decision", length = 60, nullable = false)
    private String decision;
    @Column(name = "mensajeInicial", length = 2000)
    private String mensajeInicial;
    @Column(name = "textoComunicacion", length = 4000)
    private String textoComunicacion;
    @Column(name = "textoDarca", length = 4000)
    private String textoDarca;
    @Column(name = "fechaEnvioEstudiante", columnDefinition = "datetime")
    private LocalDateTime fechaEnvioEstudiante;
    @Column(name = "fechaEnvioDarca", columnDefinition = "datetime")
    private LocalDateTime fechaEnvioDarca;
    @ColumnDefault("0")
    @Column(name = "intentos", nullable = false)
    private int intentos;
    @Column(name = "ultimoError", length = 1000)
    private String ultimoError;
    @Column(name = "fechaReclamo", columnDefinition = "datetime")
    private LocalDateTime fechaReclamo;
    @Column(name = "fechaEnvio", columnDefinition = "datetime")
    private LocalDateTime fechaEnvio;
    @Column(name = "fechaLiberacion", columnDefinition = "datetime")
    private LocalDateTime fechaLiberacion;
    @Column(name = "fechaCreacion", nullable = false, columnDefinition = "datetime")
    private LocalDateTime fechaCreacion;
    @Transient
    private boolean nuevo;

    @Override
    public String getId() {
        return uuidSolicitudAcademica;
    }

    @Override
    public boolean isNew() {
        return nuevo;
    }
}
