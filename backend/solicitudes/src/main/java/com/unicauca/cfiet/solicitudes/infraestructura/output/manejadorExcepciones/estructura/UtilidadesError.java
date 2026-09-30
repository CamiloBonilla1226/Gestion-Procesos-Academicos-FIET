package com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.estructura;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public class UtilidadesError {
    public UtilidadesError(){}

    public static Error crearError(final String codigoError, final String mensaje, final Integer httpCodigo) {
        final Error error = new Error();
        error.setCodigoError(codigoError);
        error.setMensaje(mensaje);
        error.setHttpCodigo(httpCodigo);
        return error;
    }
}
