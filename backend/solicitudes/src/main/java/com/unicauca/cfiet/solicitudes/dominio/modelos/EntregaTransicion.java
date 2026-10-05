package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntregaTransicion {
    private String observacion;
    private boolean resolucion;
    private boolean recibo;
    private boolean comprobante;
}
