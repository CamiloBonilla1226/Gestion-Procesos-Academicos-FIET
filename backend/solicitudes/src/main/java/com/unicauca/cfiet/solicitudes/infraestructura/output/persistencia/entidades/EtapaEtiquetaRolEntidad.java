package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ETAPA_ETIQUETA_ROL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtapaEtiquetaRolEntidad {
    @EmbeddedId
    private EtapaEtiquetaRolIdEntidad id;
    @MapsId("etapaUuid")
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "Etapa_uuid",
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_etiqueta_etapa")
    )
    private EtapaSolicitudAcademicaEntidad etapa;
    @Column(name = "etiqueta", length = 60, nullable = false)
    private String etiqueta;
}
