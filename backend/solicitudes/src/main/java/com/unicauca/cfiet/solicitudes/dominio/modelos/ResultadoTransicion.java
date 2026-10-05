package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ResultadoTransicion {
    private final String etapaSiguiente;
    private final boolean observacionObligatoria;
    private final boolean resolucionObligatoria;
    private final boolean reciboObligatorio;
    private final boolean comprobanteObligatorio;
}
