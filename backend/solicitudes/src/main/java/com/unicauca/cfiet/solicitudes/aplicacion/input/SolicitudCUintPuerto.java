package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Solicitud;
import java.util.List;

/**
 * Interfaz de caso de uso para la gestión de Solicitudes.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public interface SolicitudCUintPuerto {

    /**
     * Obtiene todas las solicitudes.
     *
     * @return lista completa de solicitudes
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
     * Obtiene una solicitud por su identificador.
     *
     * @param uuidSolicitud identificador único de la solicitud
     * @return solicitud correspondiente
     */
    Solicitud getSolicitud(String uuidSolicitud);

    /**
     * Crea una nueva solicitud.
     *
     * @param solicitud solicitud a crear
     * @param token token de autenticación
     * @return solicitud creada
     */
    Solicitud crearSolicitud(Solicitud solicitud, String token);

    /**
     * Crea una nueva solicitud para perfil público.
     *
     * @param solicitud solicitud a crear
     * @param token token de autenticación
     * @return solicitud creada
     */
    Solicitud crearSolicitudPublica(Solicitud solicitud, String token);

    /**
     * Actualiza una solicitud existente.
     *
     * @param uuidSolicitud identificador único de la solicitud
     * @param solicitud información actualizada
     * @param token token de autorización
     * @return solicitud actualizada
     */
    Solicitud actualizarSolicitud(String uuidSolicitud, Solicitud solicitud, String token);

    /**
     * Obtiene solicitudes de un funcionario específico.
     *
     * @param uuidFuncionario identificador del funcionario
     * @param pagina número de página
     * @param tamanio tamaño de cada página
     * @return paginación de solicitudes del funcionario
     */
    PaginacionRespuestaDTO<Solicitud> getSolicitudesPorFuncionario(String uuidFuncionario, int pagina, int tamanio);

    /**
     * Obtiene todas las solicitudes asociadas a un Orden del Día.
     *
     * @param uuidOrdenDelDia identificador del Orden del Día
     * @return lista de solicitudes asociadas
     */
    List<Solicitud> getSolicitudesPorOrdenDelDia(String uuidOrdenDelDia);

    /**
     * Obtiene solicitudes por estado específico.
     *
     * @param estado estado de las solicitudes
     * @return lista de solicitudes con el estado indicado
     */
    List<Solicitud> getSolicitudesPorEstado(String estado);

    /**
     * Busca solicitudes por coincidencia en el nombre.
     *
     * @param filtro texto a buscar en el nombre
     * @param pagina número de página
     * @param tamanio tamaño de cada página
     * @return paginación de solicitudes que coinciden con el filtro
     */
    PaginacionRespuestaDTO<Solicitud> buscarSolicitudesPorNombre(String filtro, int pagina, int tamanio);

    /**
     * Busca solicitudes por coincidencia en el nombre.
     *
     * @param nombreSolicitud nombre de la solicitud
     * @param solicitante nombre del solicitante
     * @param pagina número de página
     * @param tamanio tamaño de cada página
     * @return paginación de solicitudes que coinciden con el filtro
     */
    PaginacionRespuestaDTO<Solicitud> buscarSolicitudesPorSolicitante(String nombreSolicitud,String solicitante, String estado, int pagina, int tamanio);

    /**
     * Busca solicitudes por nombre y funcionario.
     *
     * @param uuidFuncionario identificador del funcionario
     * @param filtro texto a buscar en el nombre
     * @param pagina número de página
     * @param tamanio tamaño de cada página
     * @return paginación de solicitudes que coinciden con el filtro
     */
    PaginacionRespuestaDTO<Solicitud> buscarSolicitudesPorNombreYFuncionario(String uuidFuncionario, String filtro, int pagina, int tamanio);

    /**
     * Genera un archivo ZIP con los anexos de un Orden del Día.
     *
     * @param uuidOrden identificador del Orden del Día
     * @param basePath ruta base para generar el ZIP
     * @return contenido del ZIP en bytes
     */
    byte[] generarZipAnexosPorOrdenDelDia(String uuidOrden, String basePath);
}