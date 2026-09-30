package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import java.util.List;

/**
 * Interfaz que actúa como fachada hacia la capa de persistencia para la gestión de usuarios.
 *
 * author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public interface UsuarioGatewayIntPuerto {

    /**
     * Obtiene la lista de todos los usuarios en formato liviano.
     *
     * @return lista de usuarios livianos
     */
    List<UsuarioLiviano> getUsuarios();

    /**
     * Obtiene la lista de todos los funcionarios.
     *
     * @return lista de funcionarios
     */
    List<Funcionario> getFuncionarios();

    /**
     * Obtiene usuarios de forma paginada.
     *
     * @param pagina número de página (empezando desde 0)
     * @param tamanio cantidad de elementos por página
     * @return paginación de usuarios livianos
     */
    PaginacionRespuestaDTO<UsuarioLiviano> getUsuarios(int pagina, int tamanio);

    /**
     * Obtiene un usuario por su identificador único.
     *
     * @param uuidUsuario identificador del usuario
     * @return usuario correspondiente
     */
    Usuario getUsuario(String uuidUsuario);

    /**
     * Guarda un usuario en el sistema.
     *
     * @param usuario usuario a guardar
     * @return usuario guardado
     */
    Usuario guardarUsuario(Usuario usuario);

    /**
     * Guarda varios usuarios en el sistema.
     *
     * @param usuarios lista de usuarios a guardar
     * @return lista de usuarios guardados
     */
    List<Usuario> guardarUsuarios(List<Usuario> usuarios);

    /**
     * Obtiene usuarios filtrados por nombre completo de forma paginada.
     *
     * @param nombreCompleto nombre completo del usuario
     * @param pagina número de página
     * @param tamanio tamaño de página
     * @return paginación de usuarios que coinciden con el nombre
     */
    PaginacionRespuestaDTO<UsuarioLiviano> getUsuariosByNombreCompleto(String nombreCompleto, int pagina, int tamanio);

    /**
     * Verifica si existe un usuario con un número de documento dado.
     *
     * @param numeroDocumento número de documento
     * @return true si existe, false en caso contrario
     */
    boolean existeUsuarioNumeroDocumento(String numeroDocumento);

    /**
     * Verifica si existe un usuario con un correo dado.
     *
     * @param correo correo electrónico
     * @return true si existe, false en caso contrario
     */
    boolean existeUsuarioCorreo(String correo);

    /**
     * Verifica si existe un usuario con un nombre de usuario dado.
     *
     * @param username nombre de usuario
     * @return true si existe, false en caso contrario
     */
    boolean existeUsuarioUsername(String username);

    /**
     * Obtiene la lista de tipos de usuario disponibles.
     *
     * @return lista de tipos de usuario
     */
    List<TipoUsuario> getTiposUsuario();

    /**
     * Obtiene un tipo de usuario por su nombre.
     *
     * @param nombre nombre del tipo de usuario
     * @return tipo de usuario correspondiente
     */
    TipoUsuario getTipoUsuarioPorNombre(String nombre);

    /**
     * Obtiene la lista de roles disponibles.
     *
     * @return lista de roles
     */
    List<Rol> getRoles();
}