package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FirmaCorreo {
    private String nombre;
    private String cargo;
    private String dependencia;
    private String facultad;
}
