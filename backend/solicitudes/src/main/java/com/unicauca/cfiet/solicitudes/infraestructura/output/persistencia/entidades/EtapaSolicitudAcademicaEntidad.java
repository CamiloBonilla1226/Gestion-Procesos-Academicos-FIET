package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ETAPA_SOLICITUD_ACADEMICA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtapaSolicitudAcademicaEntidad {
    @Id
    @Column(name = "uuidEtapa", length = 100)
    private String uuidEtapa;
    @Column(name = "codigo", length = 60, nullable = false)
    private String codigo;
    @ManyToOne
    @JoinColumn(
            name = "TipoSolicitudAcademica_uuid",
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_etapa_tiposolicitud")
    )
    private TipoSolicitudAcademicaEntidad tipoSolicitudAcademica;
}
