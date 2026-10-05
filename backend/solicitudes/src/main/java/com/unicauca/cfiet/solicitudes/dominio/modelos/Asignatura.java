package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asignatura {
    private String uuidAsignatura;
    private String codigoAsignatura;
    private String nombreAsignatura;
}
