package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(
        name = "PROGRAMA_ACADEMICO",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_programa_nombre", columnNames = "nombre"),
                @UniqueConstraint(name = "uk_programa_sigla", columnNames = "sigla")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramaAcademicoEntidad {
    @Id
    @Column(name = "uuidProgramaAcademico", length = 100)
    private String uuidProgramaAcademico;
    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;
    @Column(name = "sigla", length = 10, nullable = false)
    private String sigla;
    @ColumnDefault("0")
    @Column(name = "ultimoConsecutivoAsignatura", nullable = false)
    private Integer ultimoConsecutivoAsignatura;
}
