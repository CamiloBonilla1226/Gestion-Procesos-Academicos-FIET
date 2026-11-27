package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

@Entity
@Table(name = "REMISION_CONSEJO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemisionConsejoEntidad implements Persistable<String> {
    @Id
    @Column(name = "SolicitudAcademica_uuid", length = 100)
    private String uuidSolicitudAcademica;
    @MapsId
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "SolicitudAcademica_uuid",
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_remconsejo_solacad")
    )
    private SolicitudAcademicaEntidad solicitudAcademica;
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "Solicitud_uuid",
            nullable = false,
            unique = true,
            columnDefinition = "varchar(150)",
            foreignKey = @ForeignKey(name = "fk_remconsejo_solicitud")
    )
    private SolicitudEntidad solicitud;
    @Column(name = "fechaRemision", nullable = false, columnDefinition = "datetime")
    private LocalDateTime fechaRemision;
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
