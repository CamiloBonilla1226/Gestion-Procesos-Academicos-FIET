package com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias;

import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.estructura.CodigoError;
import lombok.Getter;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
public class ErrorReglaNegocioVioladaExcepcion extends  RuntimeException{
    private final String mensaje;
    private final String codigo;

    public ErrorReglaNegocioVioladaExcepcion(CodigoError codigo){
        super(codigo.getCodigo());
        this.mensaje = codigo.getMensaje();
        this.codigo = codigo.getCodigo();
    }

    public ErrorReglaNegocioVioladaExcepcion(final String mensaje){
        super(mensaje);
        this.mensaje = CodigoError.ERROR_REGLA_DE_NEGOCIO_VIOLADA.getMensaje();
        this.codigo = CodigoError.ERROR_REGLA_DE_NEGOCIO_VIOLADA.getCodigo();
    }
}
