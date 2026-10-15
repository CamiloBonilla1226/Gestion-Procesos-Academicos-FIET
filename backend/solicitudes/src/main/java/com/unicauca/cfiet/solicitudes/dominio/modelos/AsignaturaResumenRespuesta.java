package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignaturaResumenRespuesta {
    private String codigo;
    private String nombre;
    private String decision;
    private String situacionAlCancelar;
}
