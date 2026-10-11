package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estudiante {
    private String uuidUsuario;
    private Usuario usuario;
    private String codigoEstudiantil;
    private ProgramaAcademico programaAcademico;
    private String semestre;
    private String facultad;
    @Builder.Default
    private List<AsignaturaMatriculada> asignaturasMatriculadas = new ArrayList<>();
}
