package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramaAcademico {
    private String uuidProgramaAcademico;
    private String nombre;
    private String sigla;
    private Integer ultimoConsecutivoAsignatura;
}
