package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SesionConsejo {
    private String fecha;
    private String numeroActa;
    private String consecutivo;
}
