package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;

/**
 * Interfaz que actúa como fachada hacia la capa de persistencia para la gestión de sesiones.
 *
 * author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public interface SesionGatewayIntPuerto {

    /**
     * Inicia sesión en el sistema con las credenciales proporcionadas.
     *
     * @param username nombre de usuario
     * @param password contraseña del usuario
     * @return token de autenticación si el inicio de sesión es exitoso
     */
    String login(String username, String password);

    /**
     * Obtiene la información de un usuario a partir de su nombre de usuario.
     *
     * @param username nombre de usuario
     * @return usuario correspondiente
     */
    Usuario getUsuario(String username);
}