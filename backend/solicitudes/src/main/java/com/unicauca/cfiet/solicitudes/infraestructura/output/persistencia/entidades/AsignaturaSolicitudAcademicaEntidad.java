package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ASIGNATURA_SOLICITUD_ACADEMICA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignaturaSolicitudAcademicaEntidad {
    @Id
    @Column(name = "uuidAsignaturaSolicitud", length = 100)
    private String uuidAsignaturaSolicitud;
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "SolicitudAcademica_uuid",
            nullable = false,
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_asigsol_solacad")
    )
    private SolicitudAcademicaEntidad solicitudAcademica;
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "AsignaturaMatriculada_uuid",
            nullable = false,
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_asigsol_asigmat")
    )
    private AsignaturaMatriculadaEntidad asignaturaMatriculada;
    @Column(name = "numeroFaltas")
    private Integer numeroFaltas;
    @Column(name = "nota", columnDefinition = "decimal(3,1)")
    private BigDecimal nota;
    @ManyToOne
    @JoinColumn(
            name = "SituacionMatricula_uuid",
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_asigsol_situmatricula")
    )
    private SituacionAcademicaAsignaturaEntidad situacionMatricula;
    @ManyToOne
    @JoinColumn(
            name = "SituacionCancelar_uuid",
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_asigsol_situcancelar")
    )
    private SituacionAcademicaAsignaturaEntidad situacionCancelar;
    @Column(name = "cumpleCondiciones", columnDefinition = "tinyint(1)")
    private Boolean cumpleCondiciones;
    @Column(name = "observacionEvaluacion", length = 255)
    private String observacionEvaluacion;
    @Column(name = "aprobadaPorDecano", columnDefinition = "tinyint(1)")
    private Boolean aprobadaPorDecano;
    @Column(name = "observacionDecision", length = 255)
    private String observacionDecision;
}
