package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitud;
import java.util.List;

/**
 * Interfaz de caso de uso para la gestión de Tipos de Solicitud.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public interface TipoSolicitudCUIntPuerto {

    /**
     * Obtiene todos los tipos de solicitud.
     *
     * @return lista completa de tipos de solicitud
     */
    List<TipoSolicitud> getTiposSolicitud();

    /**
     * Obtiene tipos de solicitud paginados.
     *
     * @param pagina número de página
     * @param tamanio cantidad de elementos por página
     * @return paginación de tipos de solicitud
     */
    PaginacionRespuestaDTO<TipoSolicitud> getTiposSolicitud(int pagina, int tamanio);

    /**
     * Obtiene tipos de solicitud filtrados por nombre y funcionario de forma paginada.
     *
     * @param nombreSolicitud nombre del tipo de solicitud
     * @param funcionario nombre del funcionario
     * @param pagina número de página
     * @param tamanio cantidad de elementos por página
     * @return paginación de tipos de solicitud que coinciden con los filtros
     */
    PaginacionRespuestaDTO<TipoSolicitud> getTiposSolicitud(String nombreSolicitud, String funcionario, int pagina, int tamanio);

    /**
     * Obtiene un tipo de solicitud por su identificador.
     *
     * @param uuidTipoSolicitud identificador único
     * @return tipo de solicitud correspondiente
     */
    TipoSolicitud getTipoSolicitud(String uuidTipoSolicitud);

    /**
     * Crea un nuevo tipo de solicitud.
     *
     * @param tipoSolicitud objeto con la información del tipo de solicitud
     * @param token token de usuario que realiza la acción
     * @return tipo de solicitud creado
     */
    TipoSolicitud crearTipoSolicitud(TipoSolicitud tipoSolicitud, String token);

    /**
     * Actualiza un tipo de solicitud existente.
     *
     * @param uuidTipoSolicitud identificador del tipo de solicitud
     * @param tipoSolicitud nueva información del tipo de solicitud
     * @param token token de usuario que realiza la acción
     * @return tipo de solicitud actualizado
     */
    TipoSolicitud actualizarTipoSolicitud(String uuidTipoSolicitud, TipoSolicitud tipoSolicitud, String token);

    /**
     * Crea varios tipos de solicitud en una sola operación.
     *
     * @param tiposSolicitud lista de tipos de solicitud a crear
     * @param token token de usuario que realiza la acción
     * @return lista de tipos de solicitud creados
     */
    List<TipoSolicitud> crearTiposSolicitud(List<TipoSolicitud> tiposSolicitud, String token);

    /**
     * Obtiene tipos de solicitud por perfil.
     *
     * @param perfil perfil del solicitante
     * @return lista de tipos de solicitud correspondientes al perfil
     */
    List<TipoSolicitud> getTiposSolicitudesPorPerfil(String perfil);

    /**
     * Obtiene tipos de solicitud por perfil de solicitante de forma paginada.
     *
     * @param perfil perfil del solicitante
     * @param pagina número de página
     * @param tamanio tamaño de cada página
     * @return paginación de tipos de solicitud
     */
    PaginacionRespuestaDTO<TipoSolicitud> getTiposSolicitudesPorPerfilSolicitante(String perfil, int pagina, int tamanio);

    /**
     * Obtiene tipos de solicitud por nombre y perfil de solicitante de forma paginada.
     *
     * @param nombre nombre del tipo de solicitud
     * @param perfil perfil del solicitante
     * @param pagina número de página
     * @param tamanio tamaño de cada página
     * @return paginación de tipos de solicitud que coinciden con los filtros
     */
    PaginacionRespuestaDTO<TipoSolicitud> getTiposSolicitudesPorNombreYPerfilSolicitante(String nombre, String perfil, int pagina, int tamanio);
}