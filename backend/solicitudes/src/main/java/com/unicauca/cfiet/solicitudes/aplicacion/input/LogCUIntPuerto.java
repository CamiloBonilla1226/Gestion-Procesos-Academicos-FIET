package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Log;
import java.util.List;

/**
 * Interfaz de caso de uso para la gestión de Logs en el sistema.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public interface LogCUIntPuerto {

    /**
     * Crea un log en el sistema.
     *
     * @param accion la acción realizada
     * @param resultado el resultado de la acción
     * @param token JWT del usuario que realiza la acción
     */
    void crearLog(String accion, String resultado, String token);

    /**
     * Crea un log de inicio de sesión para un usuario.
     *
     * @param accion la acción de la sesión
     * @param resultado el resultado de la acción
     * @param username nombre de usuario
     */
    void crearLogSesion(String accion, String resultado, String username);

    /**
     * Obtiene logs paginados.
     *
     * @param pagina número de página
     * @param tamanio tamaño de cada página
     * @return lista de logs correspondiente a la página
     */
    PaginacionRespuestaDTO<Log> getLogs(int pagina, int tamanio);

    /**
     * Obtiene logs filtrados por responsable y fecha.
     *
     * @param responsable responsable de la acción
     * @param fecha fecha de creación
     * @param pagina número de página
     * @param tamanio tamaño de cada página
     * @return lista de logs correspondiente a la página
     */
    PaginacionRespuestaDTO<Log> getLogs(String responsable, String fecha, int pagina, int tamanio);

    /**
     * Obtiene todos los logs del sistema.
     *
     * @return lista completa de logs
     */
    List<Log> getLogs();
}
