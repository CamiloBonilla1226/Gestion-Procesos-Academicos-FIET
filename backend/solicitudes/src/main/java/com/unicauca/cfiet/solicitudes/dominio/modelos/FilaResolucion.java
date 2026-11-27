package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilaResolucion {
    private String asignatura;
    private String faltas;
    private String nota;
    private String estadoAlSolicitar;
    private String estadoCancelacion;
    private String estadoNuevaMatricula;
}
