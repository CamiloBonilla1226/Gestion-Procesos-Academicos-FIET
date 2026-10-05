package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "TIPO_ANEXO_ACADEMICO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoAnexoAcademicoEntidad {
    @Id
    @Column(name = "uuidTipoAnexoAcademico", length = 100)
    private String uuidTipoAnexoAcademico;
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "TipoSolicitudAcademica_uuid",
            nullable = false,
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_tipoanexo_tiposolicitud")
    )
    private TipoSolicitudAcademicaEntidad tipoSolicitudAcademica;
    @Column(name = "nombre", length = 150, nullable = false)
    private String nombre;
    @Column(name = "formatosPermitidos", length = 60, nullable = false)
    private String formatosPermitidos;
    @ColumnDefault("1")
    @Column(name = "obligatorio", nullable = false, columnDefinition = "tinyint(1)")
    private Boolean obligatorio;
}
