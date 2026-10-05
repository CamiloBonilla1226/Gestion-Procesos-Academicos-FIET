package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "ASIGNATURA_MATRICULADA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignaturaMatriculadaEntidad {
    @Id
    @Column(name = "uuidAsignaturaMatriculada", length = 100)
    private String uuidAsignaturaMatriculada;
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "Estudiante_uuid",
            nullable = false,
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_asigmat_estudiante")
    )
    private EstudianteEntidad estudiante;
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "Asignatura_uuid",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_asigmat_asignatura")
    )
    private AsignaturaEntidad asignatura;
    @Column(name = "grupo", length = 20, nullable = false)
    private String grupo;
    @ColumnDefault("'activa'")
    @Column(name = "estado", length = 20, nullable = false)
    private String estado;
}
