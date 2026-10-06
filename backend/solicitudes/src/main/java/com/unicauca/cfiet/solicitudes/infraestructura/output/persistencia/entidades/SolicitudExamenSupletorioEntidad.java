package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

@Entity
@Table(name = "SOLICITUD_EXAMEN_SUPLETORIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudExamenSupletorioEntidad implements Persistable<String> {
    @Id
    @Column(name = "SolicitudAcademica_uuid", length = 100)
    private String uuidSolicitudAcademica;
    @MapsId
    @OneToOne(optional = false)
    @JoinColumn(
            name = "SolicitudAcademica_uuid",
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_solsup_solacad")
    )
    private SolicitudAcademicaEntidad solicitudAcademica;
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "AsignaturaMatriculada_uuid",
            nullable = false,
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_solsup_asigmat")
    )
    private AsignaturaMatriculadaEntidad asignaturaMatriculada;
    @Column(name = "fechaExamenNoPresentado", nullable = false, columnDefinition = "datetime")
    private LocalDateTime fechaExamenNoPresentado;
    @Column(name = "fechaAcordadaExamen", columnDefinition = "datetime")
    private LocalDateTime fechaAcordadaExamen;
    @Column(name = "tipoCausa", nullable = false, columnDefinition = "enum('cruce','otra')")
    private String tipoCausa;
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
