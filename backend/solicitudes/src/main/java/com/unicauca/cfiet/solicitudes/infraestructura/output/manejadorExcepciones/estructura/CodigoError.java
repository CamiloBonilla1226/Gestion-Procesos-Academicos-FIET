package com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.estructura;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@RequiredArgsConstructor
@Getter
public enum CodigoError {
    ERROR_GENERICO("1", "Error generico"),
    ERROR_ENTIDAD_EXISTE("2", "Error, entidad ya existe"),
    ERROR_ENTIDAD_NO_EXISTE("3", "Error, entidad no existe"),
    ERROR_REGLA_DE_NEGOCIO_VIOLADA("4", "Error, regla de negocio violada"),
    ERROR_CREDENCIALES("5", "Error, credenciales incorrectas"),
    ERROR_MAL_FORMATO("6", "Error, mal formato"),
    ERROR_NO_INFORMACION("7", "Error, no se obtuvo información"),
    ERROR_SIN_ACCESO("8", "Error, sin acceso"),
    ERROR_TOKEN_EXPIRADO("9", "Error, token expirado");

    private final String codigo;
    private final String mensaje;
}
