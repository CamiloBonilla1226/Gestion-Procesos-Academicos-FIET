package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "ESTUDIANTE",
        uniqueConstraints = @UniqueConstraint(name = "uk_estudiante_codigo", columnNames = "codigoEstudiantil")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteEntidad {
    @Id
    @Column(name = "Usuario_uuid", length = 100)
    private String uuidUsuario;
    @MapsId
    @OneToOne(optional = false)
    @JoinColumn(
            name = "Usuario_uuid",
            columnDefinition = "varchar(100)",
            foreignKey = @ForeignKey(name = "fk_estudiante_usuario")
    )
    private UsuarioEntidad usuario;
    @Column(name = "codigoEstudiantil", length = 45, nullable = false)
    private String codigoEstudiantil;
    @Column(name = "programaAcademico", length = 100, nullable = false)
    private String programaAcademico;
    @Column(name = "semestre", length = 10, nullable = false)
    private String semestre;
    @Column(name = "facultad", length = 100, nullable = false)
    private String facultad;
    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Builder.Default
    private List<AsignaturaMatriculadaEntidad> asignaturasMatriculadas = new ArrayList<>();
}
