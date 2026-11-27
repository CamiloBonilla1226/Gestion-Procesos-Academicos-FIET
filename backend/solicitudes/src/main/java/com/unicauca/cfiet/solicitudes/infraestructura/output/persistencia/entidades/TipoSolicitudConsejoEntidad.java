package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "TIPO_SOLICITUD_CONSEJO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoSolicitudConsejoEntidad implements Persistable<String> {
    @Id
    @Column(name = "TipoSolicitud_uuid", length = 150)
    private String uuidTipoSolicitud;
    @MapsId
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "TipoSolicitud_uuid",
            columnDefinition = "varchar(150)",
            foreignKey = @ForeignKey(name = "fk_tsconsejo_tiposolicitud")
    )
    private TipoSolicitudEntidad tipoSolicitud;
    @OneToOne(optional = false)
    @JoinColumn(
            name = "TipoSolicitudAcademica_uuid",
            nullable = false,
            unique = true,
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_tsconsejo_tipoacademico")
    )
    private TipoSolicitudAcademicaEntidad tipoSolicitudAcademica;
    @ManyToOne
    @JoinColumn(
            name = "EncargadoAcademico_uuid",
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_tsconsejo_encargado")
    )
    private FuncionarioAcademicoEntidad encargadoAcademico;
    @Transient
    private boolean nuevo;

    @Override
    public String getId() {
        return uuidTipoSolicitud;
    }

    @Override
    public boolean isNew() {
        return nuevo;
    }
}
