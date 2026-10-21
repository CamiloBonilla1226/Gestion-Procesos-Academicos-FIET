package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantillaNotificacionResolucion {
    private String mensajeInicial;
    private String decisionPropuesta;
    private Integer anioResolucion;
    private String sufijoResolucion;
}
