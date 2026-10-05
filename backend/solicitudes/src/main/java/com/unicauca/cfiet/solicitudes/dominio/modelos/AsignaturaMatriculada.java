package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignaturaMatriculada {
    private String uuidAsignaturaMatriculada;
    private Asignatura asignatura;
    private String grupo;
    private String estado;
}
