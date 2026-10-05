package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

@Entity
@Table(
        name = "ASIGNATURA",
        uniqueConstraints = @UniqueConstraint(name = "uk_asignatura_codigo", columnNames = "codigoAsignatura")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignaturaEntidad {
    @Id
    @Column(name = "uuidAsignatura", length = 100)
    private String uuidAsignatura;
    @Column(name = "codigoAsignatura", length = 45, nullable = false)
    private String codigoAsignatura;
    @Column(name = "nombreAsignatura", length = 150, nullable = false)
    private String nombreAsignatura;
}
