package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Respuesta;
import org.springframework.web.multipart.MultipartFile;

/**
 * Interfaz de caso de uso para la gestión de Respuestas.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public interface RespuestaCUIntPuerto {

    /**
     * Obtiene todas las respuestas paginadas.
     *
     * @param pagina número de página
     * @param tamanio tamaño de la página
     * @return respuestas paginadas
     */
    PaginacionRespuestaDTO<Respuesta> getRespuestas(int pagina, int tamanio);

    /**
     * Obtiene respuestas filtradas por nombre de solicitud.
     *
     * @param nombreSolicitud nombre de la solicitud
     * @param pagina número de página
     * @param tamanio tamaño de la página
     * @return respuestas filtradas y paginadas
     */
    PaginacionRespuestaDTO<Respuesta> getRespuestasPorNombreSolicitud(String nombreSolicitud, int pagina, int tamanio);

    /**
     * Obtiene respuestas asociadas a un funcionario.
     *
     * @param uuidFuncionario identificador del funcionario
     * @param pagina número de página
     * @param tamanio tamaño de la página
     * @return respuestas filtradas y paginadas
     */
    PaginacionRespuestaDTO<Respuesta> getRespuestasPorFuncionario(String uuidFuncionario, int pagina, int tamanio);

    /**
     * Obtiene respuestas filtradas por funcionario y nombre de solicitud.
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
     * Obtiene la respuesta asociada a una solicitud.
     *
     * @param uuidSolicitud identificador de la solicitud
     * @return respuesta asociada
     */
    Respuesta getRespuestaPorSolicitud(String uuidSolicitud);

    /**
     * Registra una nueva respuesta para una solicitud.
     *
     * @param uuidSolicitud identificador de la solicitud
     * @param respuesta información de la respuesta
     * @param token token de autorización
     * @return respuesta registrada
     */
    Respuesta registrarRespuesta(String uuidSolicitud, Respuesta respuesta, String token);

    /**
     * Adjunta un archivo de respuesta a una respuesta existente.
     *
     * @param uuidRespuesta identificador de la respuesta
     * @param respuesta archivo de la respuesta
     * @param token token de autorización
     * @return respuesta actualizada
     */
    Respuesta responderSolicitud(String uuidRespuesta, MultipartFile respuesta, String token);

    /**
     * Elimina el archivo asociado a la respuesta si existe.
     *
     * @param uuidRespuesta identificador de la respuesta
     * @param token token de autorización
     * @return respuesta
     */
    Respuesta eliminarArchivoRespuesta(String uuidRespuesta, String token);
}
