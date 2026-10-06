package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

@Entity
@Table(name = "SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudSupletorioCruceAsignaturaEntidad implements Persistable<String> {
    @Id
    @Column(name = "SolicitudAcademica_uuid", length = 100)
    private String uuidSolicitudAcademica;
    @MapsId
    @OneToOne(optional = false)
    @JoinColumn(
            name = "SolicitudAcademica_uuid",
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_solcruce_solsup")
    )
    private SolicitudExamenSupletorioEntidad solicitudExamenSupletorio;
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "AsignaturaMatriculadaCruzada_uuid",
            nullable = false,
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_solcruce_asigmat")
    )
    private AsignaturaMatriculadaEntidad asignaturaMatriculadaCruzada;
    @Column(name = "fechaExamenCruzada", nullable = false, columnDefinition = "datetime")
    private LocalDateTime fechaExamenCruzada;
    @Column(name = "horaExamenCruzada", length = 10, nullable = false)
    private String horaExamenCruzada;
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
