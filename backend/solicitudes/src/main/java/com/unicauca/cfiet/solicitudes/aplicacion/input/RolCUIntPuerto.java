package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.Rol;
import java.util.List;

/**
 * Interfaz de caso de uso para la gestión de Roles.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public interface RolCUIntPuerto {

    /**
     * Obtiene todos los roles del sistema.
     *
     * @return lista de roles
     */
    List<Rol> getRoles();

    /**
     * Obtiene roles del sistema con paginación.
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
     * Actualiza un rol existente.
     *
     * @param uuidRol identificador del rol a actualizar
     * @param rol información actualizada del rol
     * @param token token de autorización
     * @return rol actualizado
     */
    Rol actualizarRol(String uuidRol, Rol rol, String token);
}