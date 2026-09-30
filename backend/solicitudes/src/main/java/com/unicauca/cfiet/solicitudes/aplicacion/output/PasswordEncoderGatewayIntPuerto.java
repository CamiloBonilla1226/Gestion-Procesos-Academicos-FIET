package com.unicauca.cfiet.solicitudes.aplicacion.output;

/**
 * Interfaz que encripta contraseñas y verifica coincidencias.
 * Permite manejar de forma segura las contraseñas de los usuarios.
 *
 * author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public interface PasswordEncoderGatewayIntPuerto {

    /**
     * Encripta una contraseña utilizando un algoritmo seguro de hashing.
     *
     * @param contraseña contraseña en texto plano
     * @return contraseña encriptada
     */
    String encriptarContraseña(String contraseña);

    /**
     * Verifica si una contraseña en texto plano coincide con su versión encriptada.
     *
     * @param contraseñaOriginal contraseña encriptada almacenada
     * @param contraseña contraseña en texto plano
     * @return true si coinciden, false en caso contrario
     */
    boolean contraseñaCoincide(String contraseñaOriginal, String contraseña);
}