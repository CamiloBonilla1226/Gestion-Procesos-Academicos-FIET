package com.unicauca.cfiet.solicitudes.aplicacion.output;

public class FallaTransitoriaCorreoException extends RuntimeException {
    public FallaTransitoriaCorreoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
