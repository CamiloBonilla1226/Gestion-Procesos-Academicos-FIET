package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Solicitud;
import java.util.List;

/**
 * Interfaz que actúa como fachada hacia la capa de persistencia para la gestión de solicitudes.
 *
 * author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public interface SolicitudGatewayIntPuerto {

    /**
     * Obtiene todas las solicitudes almacenadas.
     *
     * @return lista de solicitudes
     */
    List<Solicitud> getSolicitudes();

    /**
     * Obtiene solicitudes paginadas.
     *
     * @param pagina número de página
     * @param tamanio tamaño de cada página
     * @return paginación de solicitudes
     */
    PaginacionRespuestaDTO<Solicitud> getSolicitudes(int pagina, int tamanio);

    /**
     * Obtener solicitudes por filtro.
     *
     * @param nombreSolicitud nombre de la solicitud
     * @param solicitante nombre del solicitante (nombres apellidos)
     * @param pagina número de página
     * @param tamanio tamaño de cada página
     * @return paginación de solicitudes
     */
    PaginacionRespuestaDTO<Solicitud> buscarSolicitudesPorSolicitante(String nombreSolicitud, String solicitante, String estado, int pagina, int tamanio);

    /**
     * Obtiene una solicitud por su identificador único.
     *
     * @param uuidSolicitud identificador de la solicitud
     * @return solicitud correspondiente
     */
    Solicitud getSolicitud(String uuidSolicitud);

    /**
     * Guarda una solicitud nueva o actualiza una existente.
     *
     * @param solicitud solicitud a guardar
     * @return solicitud guardada
     */
    Solicitud guardarSolicitud(Solicitud solicitud);

    /**
     * Obtiene solicitudes de un funcionario específico de forma paginada.
     *
     * @param uuidFuncionario identificador del funcionario
     * @param pagina número de página
     * @param tamanio tamaño de cada página
     * @return paginación de solicitudes del funcionario
     */
    PaginacionRespuestaDTO<Solicitud> getSolicitudesPorFuncionario(String uuidFuncionario, int pagina, int tamanio);

    /**
     * Obtiene todas las solicitudes pertenecientes a un Orden del Día.
     *
     * @param uuidOrdenDelDia identificador del Orden del Día
     * @return lista de solicitudes correspondientes
     */
    List<Solicitud> getSolicitudesPorOrdenDelDia(String uuidOrdenDelDia);

    /**
     * Obtiene todas las solicitudes con un estado específico.
     *
     * @param estado estado de las solicitudes
     * @return lista de solicitudes con el estado indicado
     */
    List<Solicitud> getSolicitudesPorEstado(String estado);

    /**
     * Busca solicitudes por nombre de forma paginada.
     *
     * @param filtro texto de búsqueda
     * @param pagina número de página
     * @param tamanio tamaño de cada página
     * @return paginación de solicitudes que coinciden con el filtro
     */
    PaginacionRespuestaDTO<Solicitud> buscarSolicitudesPorNombre(String filtro, int pagina, int tamanio);

    /**
     * Busca solicitudes por nombre y funcionario de forma paginada.
     *
     * @param uuidFuncionario identificador del funcionario
     * @param filtro texto de búsqueda
     * @param pagina número de página
     * @param tamanio tamaño de cada página
     * @return paginación de solicitudes que coinciden con el filtro y funcionario
     */
    PaginacionRespuestaDTO<Solicitud> buscarSolicitudesPorNombreYFuncionario(String uuidFuncionario, String filtro, int pagina, int tamanio);
}