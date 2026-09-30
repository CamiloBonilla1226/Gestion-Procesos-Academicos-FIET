package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Funcionario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoUsuario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.UsuarioLiviano;
import java.util.List;

/**
 * Interfaz de caso de uso para la gestión de Usuarios.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public interface UsuarioCUIntPuerto {

    /**
     * Obtiene todos los usuarios en formato liviano.
     *
     * @return lista de usuarios
     */
    List<UsuarioLiviano> getUsuarios();

    /**
     * Obtiene todos los funcionarios.
     *
     * @return lista de funcionarios
     */
    List<Funcionario> getFuncionarios();

    /**
     * Obtiene usuarios paginados en formato liviano.
     *
     * @param pagina número de página
     * @param tamanio tamaño de cada página
     * @return paginación de usuarios
     */
    PaginacionRespuestaDTO<UsuarioLiviano> getUsuarios(int pagina, int tamanio);

    /**
     * Obtiene usuarios filtrados por nombre completo de forma paginada.
     *
     * @param nombreCompleto nombre completo del usuario
     * @param pagina número de página
     * @param tamanio tamaño de cada página
     * @return paginación de usuarios que coinciden con el filtro
     */
    PaginacionRespuestaDTO<UsuarioLiviano> getUsuariosByNombreCompleto(String nombreCompleto, int pagina, int tamanio);

    /**
     * Obtiene un usuario por su identificador.
     *
     * @param uuidUsuario identificador único
     * @return usuario correspondiente
     */
    Usuario getUsuario(String uuidUsuario);

    /**
     * Crea un nuevo usuario.
     *
     * @param usuario información del usuario a crear
     * @param tipoUsuario tipo de usuario (Decano, Secretario General, Funcionario, Secretaria Decanatura Fiet)
     * @param token token de autorización
     * @return usuario creado
     */
    Usuario crearUsuario(Usuario usuario, String tipoUsuario, String token);

    /**
     * Crea múltiples usuarios de una sola vez.
     *
     * @param usuarios lista de usuarios a crear (ej. desde archivo Excel)
     * @param token token de autorización
     * @return lista de usuarios creados
     */
    List<Usuario> crearUsuarios(List<Usuario> usuarios, String token);

    /**
     * Actualiza un usuario existente.
     *
     * @param uuidUsuario identificador único del usuario
     * @param usuario información actualizada del usuario
     * @param token token de autorización
     * @return usuario actualizado
     */
    Usuario actualizarUsuario(String uuidUsuario, Usuario usuario, String token);

    /**
     * Cambia la contraseña de un usuario.
     *
     * @param uuidUsuario identificador único del usuario
     * @param contraseña contraseña actual
     * @param nuevaContraseña nueva contraseña a establecer
     * @param token token de autorización
     * @return usuario con la contraseña actualizada
     */
    Usuario cambiarContraseña(String uuidUsuario, String contraseña, String nuevaContraseña, String token);

    /**
     * Obtiene los tipos de usuario soportados en el sistema.
     *
     * @return lista de tipos de usuario
     */
    List<TipoUsuario> getTiposUsuario();
}