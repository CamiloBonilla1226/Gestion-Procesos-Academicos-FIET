package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Log;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import java.util.List;

/**
 * Interfaz que actúa como fachada hacia la capa de persistencia para la gestión de logs.
 * Permite crear, consultar y contar logs, así como obtener usuarios por nombre.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public interface LogGatewayIntPuerto {

    /**
     * Crea un log en el sistema.
     *
     * @param log objeto Log a crear
     * @return log creado
     */
    Log crearLog(Log log);

    /**
     * Obtiene logs paginados.
     *
     * @param pagina número de página
     * @param tamanio tamaño de cada página
     * @return paginación de logs
     */
    PaginacionRespuestaDTO<Log> getLogs(int pagina, int tamanio);

    /**
     * Obtiene logs filtrados por responsable y fecha.
     *
     * @param responsable responsable de la acción
     * @param fecha fecha de creación
     * @param pagina número de página
     * @param tamanio tamaño de cada página
     * @return paginación de logs que coinciden con los filtros
     */
    PaginacionRespuestaDTO<Log> getLogs(String responsable, String fecha, int pagina, int tamanio);

    /**
     * Obtiene todos los logs del sistema.
     *
     * @return lista completa de logs
     */
    List<Log> getLogs();

    /**
     * Obtiene un usuario por su nombre de usuario.
     *
     * @param username nombre de usuario
     * @return usuario correspondiente
     */
    Usuario getUsuarioUsername(String username);
}
