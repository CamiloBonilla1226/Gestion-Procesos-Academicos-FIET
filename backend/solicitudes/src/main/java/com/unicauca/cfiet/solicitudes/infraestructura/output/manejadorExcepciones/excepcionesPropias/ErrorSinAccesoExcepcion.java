package com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias;

import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.estructura.CodigoError;
import lombok.Getter;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
public class ErrorSinAccesoExcepcion extends  RuntimeException{
    private final String mensaje;
    private final String codigo;

    public ErrorSinAccesoExcepcion(CodigoError codigo){
        super(codigo.getCodigo());
        this.mensaje = codigo.getMensaje();
        this.codigo = codigo.getCodigo();
    }

    public ErrorSinAccesoExcepcion(final String mensaje){
        super(mensaje);
        this.mensaje = CodigoError.ERROR_SIN_ACCESO.getMensaje();
        this.codigo = CodigoError.ERROR_SIN_ACCESO.getCodigo();
    }
}
