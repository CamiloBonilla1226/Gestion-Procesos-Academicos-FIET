package com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias;

import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.estructura.CodigoError;
import lombok.Getter;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
public class ErrorMalFormatoExcepcion extends  RuntimeException{
    private final String mensaje;
    private final String codigo;

    public ErrorMalFormatoExcepcion(CodigoError codigo){
        super(codigo.getCodigo());
        this.mensaje = codigo.getMensaje();
        this.codigo = codigo.getCodigo();
    }

    public ErrorMalFormatoExcepcion(final String mensaje){
        super(mensaje);
        this.mensaje = CodigoError.ERROR_MAL_FORMATO.getMensaje();
        this.codigo = CodigoError.ERROR_MAL_FORMATO.getCodigo();
    }
}
