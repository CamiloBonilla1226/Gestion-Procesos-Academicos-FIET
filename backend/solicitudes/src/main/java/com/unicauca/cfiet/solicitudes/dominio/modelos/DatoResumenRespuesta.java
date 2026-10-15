package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatoResumenRespuesta {
    private String etiqueta;
    private String valor;
}
