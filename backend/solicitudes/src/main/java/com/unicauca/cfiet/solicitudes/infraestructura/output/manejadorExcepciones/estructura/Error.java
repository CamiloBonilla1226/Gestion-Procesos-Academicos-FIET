package com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.estructura;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Error {
    private String codigoError;
    private String mensaje;
    private Integer httpCodigo;
    @Accessors(chain = true)
    private String url;
    @Accessors(chain = true)
    private String metodo;
}