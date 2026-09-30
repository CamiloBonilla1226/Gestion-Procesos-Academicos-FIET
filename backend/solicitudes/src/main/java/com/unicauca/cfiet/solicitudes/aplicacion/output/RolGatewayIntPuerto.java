package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.Rol;
import java.util.List;

/**
 * Interfaz que actúa como fachada hacia la capa de persistencia para la gestión de roles.
 * Permite consultar y guardar roles en el sistema.
 *
 * author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public interface RolGatewayIntPuerto {

    /**
     * Obtiene todos los roles del sistema.
     *
     * @return lista de roles
     */
    List<Rol> getRoles();

    /**
     * Obtiene roles del sistema de forma paginada.
     *
     * @param pagina número de página
     * @param tamanio tamaño de cada página
     * @return lista de roles correspondientes a la página
     */
    List<Rol> getRoles(int pagina, int tamanio);

    /**
     * Obtiene un rol por su identificador.
     *
     * @param uuidRol identificador único del rol
     * @return rol correspondiente
     */
    Rol getRol(String uuidRol);

    /**
     * Guarda un rol en el sistema.
     *
     * @param rol rol a guardar
     * @return rol guardado
     */
    Rol guardarRol(Rol rol);
}
