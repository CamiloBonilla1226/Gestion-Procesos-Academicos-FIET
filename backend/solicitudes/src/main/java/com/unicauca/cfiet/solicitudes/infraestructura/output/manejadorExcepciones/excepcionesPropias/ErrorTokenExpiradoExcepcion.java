package com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias;

import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.estructura.CodigoError;
import lombok.Getter;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
public class ErrorTokenExpiradoExcepcion extends RuntimeException{
    private final String mensaje;
    private final String codigo;

    public ErrorTokenExpiradoExcepcion(CodigoError codigo){
        super(codigo.getCodigo());
        this.mensaje = codigo.getMensaje();
        this.codigo = codigo.getCodigo();
    }

    public ErrorTokenExpiradoExcepcion(final String mensaje){
        super(mensaje);
        this.mensaje = CodigoError.ERROR_TOKEN_EXPIRADO.getMensaje();
        this.codigo = CodigoError.ERROR_TOKEN_EXPIRADO.getCodigo();
    }
}
