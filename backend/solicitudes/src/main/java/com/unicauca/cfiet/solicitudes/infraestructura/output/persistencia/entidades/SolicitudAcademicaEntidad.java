package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "SOLICITUD_ACADEMICA",
        uniqueConstraints = @UniqueConstraint(name = "uk_solacad_radicado", columnNames = "radicado")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudAcademicaEntidad {
    @Id
    @Column(name = "uuidSolicitudAcademica", length = 100)
    private String uuidSolicitudAcademica;
    @Column(name = "radicado", length = 20, nullable = false)
    private String radicado;
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "Estudiante_uuid",
            nullable = false,
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_solacad_estudiante")
    )
    private EstudianteEntidad estudiante;
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "TipoSolicitudAcademica_uuid",
            nullable = false,
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_solacad_tiposolicitud")
    )
    private TipoSolicitudAcademicaEntidad tipoSolicitudAcademica;
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "fechaCreacion", nullable = false, columnDefinition = "datetime")
    private LocalDateTime fechaCreacion;
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "Etapa_uuid",
            nullable = false,
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_solacad_etapa")
    )
    private EtapaSolicitudAcademicaEntidad etapa;
}
