package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.UsuarioTokenizado;

/**
 * Interfaz de caso de uso para la gestión de Sesiones.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public interface SesionCUIntPuerto {

    /**
     * Inicia sesión en el sistema.
     *
     * @param username nombre de usuario
     * @param contraseña contraseña del usuario
     * @return token de usuario generado si las credenciales son correctas
     */
    UsuarioTokenizado login(String username, String contraseña);
}
