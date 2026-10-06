package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "SOLICITUD_CANCELACION_MATRICULA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudCancelacionMatriculaEntidad implements Persistable<String> {
    @Id
    @Column(name = "SolicitudAcademica_uuid", length = 100)
    private String uuidSolicitudAcademica;
    @MapsId
    @OneToOne(optional = false)
    @JoinColumn(
            name = "SolicitudAcademica_uuid",
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_solcm_solacad")
    )
    private SolicitudAcademicaEntidad solicitudAcademica;
    @Column(name = "motivoCancelacion", length = 255, nullable = false)
    private String motivoCancelacion;
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
