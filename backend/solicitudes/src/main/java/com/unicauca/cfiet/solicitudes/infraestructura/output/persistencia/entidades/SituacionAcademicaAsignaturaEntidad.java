package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "SITUACION_ACADEMICA_ASIGNATURA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SituacionAcademicaAsignaturaEntidad {
    @Id
    @Column(name = "uuidSituacionAcademica", length = 100)
    private String uuidSituacionAcademica;
    @Column(name = "codigo", length = 10, nullable = false)
    private String codigo;
    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;
}
