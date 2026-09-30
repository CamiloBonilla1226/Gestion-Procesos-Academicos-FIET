package com.unicauca.cfiet.solicitudes.aplicacion.output;

/**
 * Interfaz para interacción con la seguridad de Spring Boot.
 * Permite obtener información del usuario a partir de un token JWT.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public interface IJwtServicio {

    /**
     * Obtiene el nombre de usuario a partir de un token JWT.
     *
     * @param token JWT del usuario
     * @return nombre de usuario contenido en el token
     */
    String getUsername(String token);
}
