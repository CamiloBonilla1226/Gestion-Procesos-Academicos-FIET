package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TIPO_SOLICITUD_ACADEMICA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoSolicitudAcademicaEntidad {
    @Id
    @Column(name = "uuidTipoSolicitudAcademica", length = 100)
    private String uuidTipoSolicitudAcademica;
    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;
    @Column(name = "descripcion", length = 255)
    private String descripcion;
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "FuncionarioAcademico_uuid",
            nullable = false,
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_tiposolicitud_funcionario")
    )
    private FuncionarioAcademicoEntidad funcionarioAcademico;
}
