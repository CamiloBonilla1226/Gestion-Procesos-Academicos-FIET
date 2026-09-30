package com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias;

import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.estructura.CodigoError;
import lombok.Getter;
import org.springframework.security.authentication.BadCredentialsException;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
public class ErrorCredencialesIncorrectasExcepcion extends BadCredentialsException {
    private final String mensaje;
    private final String codigo;

    public ErrorCredencialesIncorrectasExcepcion(CodigoError codigo){
        super(codigo.getCodigo());
        this.mensaje = codigo.getMensaje();
        this.codigo = codigo.getCodigo();
    }

    public ErrorCredencialesIncorrectasExcepcion(final String mensaje){
        super(mensaje);
        this.mensaje = CodigoError.ERROR_CREDENCIALES.getMensaje();
        this.codigo = CodigoError.ERROR_CREDENCIALES.getCodigo();
    }
}
