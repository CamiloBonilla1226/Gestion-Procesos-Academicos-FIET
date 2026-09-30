package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Respuesta;

/**
 * Interfaz que actúa como fachada hacia la capa de persistencia para la gestión de Respuestas.
 *
 * author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public interface RespuestaGatewayIntPuerto {

    /**
     * Verifica si una solicitud ya tiene una respuesta registrada.
     *
     * @param uuidSolicitud identificador de la solicitud
     * @return true si existe respuesta, false en caso contrario
     */
    boolean solicitudTieneRespuesta(String uuidSolicitud);

    /**
     * Obtiene la respuesta asociada a una solicitud.
     *
     * @param uuidSolicitud identificador de la solicitud
     * @return respuesta asociada
     */
    Respuesta getRespuestaPorSolicitud(String uuidSolicitud);

    /**
     * Obtiene todas las respuestas paginadas.
     *
     * @param pagina número de página
     * @param tamanio tamaño de la página
     * @return respuestas paginadas
     */
    PaginacionRespuestaDTO<Respuesta> getRespuestas(int pagina, int tamanio);

    /**
     * Obtiene respuestas filtradas por nombre de solicitud y paginadas.
     *
     * @param nombreSolicitud nombre de la solicitud
     * @param pagina número de página
     * @param tamanio tamaño de la página
     * @return respuestas filtradas y paginadas
     */
    PaginacionRespuestaDTO<Respuesta> getRespuestasPorNombreSolicitud(String nombreSolicitud, int pagina, int tamanio);

    /**
     * Obtiene respuestas asociadas a un funcionario, con paginación.
     *
     * @param uuidFuncionario identificador del funcionario
     * @param pagina número de página
     * @param tamanio tamaño de la página
     * @return respuestas filtradas y paginadas
     */
    PaginacionRespuestaDTO<Respuesta> getRespuestasPorFuncionario(String uuidFuncionario, int pagina, int tamanio);

    /**
     * Obtiene respuestas filtradas por funcionario y nombre de solicitud, con paginación.
     *
     * @param uuidFuncionario identificador del funcionario
     * @param nombreSolicitud nombre de la solicitud
     * @param pagina número de página
     * @param tamanio tamaño de la página
     * @return respuestas filtradas y paginadas
     */
    PaginacionRespuestaDTO<Respuesta> getRespuestasPorFuncionarioNombreSolicitud(String uuidFuncionario, String nombreSolicitud, int pagina, int tamanio);

    /**
     * Obtiene una respuesta por su identificador.
     *
     * @param uuidRespuesta identificador de la respuesta
     * @return respuesta encontrada
     */
    Respuesta getRespuesta(String uuidRespuesta);

    /**
     * Guarda o actualiza una respuesta en la base de datos.
     *
     * @param respuesta respuesta a guardar o actualizar
     * @return respuesta guardada
     */
    Respuesta guardarRespuesta(Respuesta respuesta);
}
