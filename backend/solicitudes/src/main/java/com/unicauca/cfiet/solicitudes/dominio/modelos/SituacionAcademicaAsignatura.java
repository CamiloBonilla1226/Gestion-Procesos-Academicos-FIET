package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SituacionAcademicaAsignatura {
    private String uuidSituacionAcademica;
    private String codigo;
    private String nombre;
}
