package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "HISTORIAL_SOLICITUD_ACADEMICA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialSolicitudAcademicaEntidad {
    @Id
    @Column(name = "uuidHistorial", length = 100)
    private String uuidHistorial;
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "SolicitudAcademica_uuid",
            nullable = false,
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_historial_solacad")
    )
    private SolicitudAcademicaEntidad solicitudAcademica;
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "Usuario_uuid",
            nullable = false,
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_historial_usuario")
    )
    private UsuarioEntidad usuario;
    @Column(name = "accion", length = 150, nullable = false)
    private String accion;
    @Column(name = "observaciones", length = 500)
    private String observaciones;
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "fecha", nullable = false, columnDefinition = "datetime")
    private LocalDateTime fecha;
}
