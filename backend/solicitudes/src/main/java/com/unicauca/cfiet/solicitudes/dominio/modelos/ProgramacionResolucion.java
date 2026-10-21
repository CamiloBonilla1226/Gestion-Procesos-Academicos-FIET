package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramacionResolucion {
    private LocalDateTime fechaProgramada;
    private String numeroResolucion;
    private Integer anioResolucion;
    private String decision;
}
