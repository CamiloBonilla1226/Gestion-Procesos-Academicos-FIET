package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitud;
import java.util.List;

/**
 * Interfaz que actúa como fachada hacia la capa de persistencia para la gestión de Tipos de Solicitudes.
 *
 * author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public interface TipoSolicitudGatewayIntPuerto {

    /**
     * Obtiene todos los tipos de solicitud almacenados.
     *
     * @return lista de tipos de solicitud
     */
    List<TipoSolicitud> getTiposSolicitudes();

    /**
     * Obtiene tipos de solicitud paginados.
     *
     * @param pagina número de página
     * @param tamanio tamaño de cada página
     * @return paginación de tipos de solicitud
     */
    PaginacionRespuestaDTO<TipoSolicitud> getTiposSolicitudes(int pagina, int tamanio);

    /**
     * Obtiene tipos de solicitud filtrados por nombre y funcionario de forma paginada.
     *
     * @param nombreSolicitud nombre del tipo de solicitud
     * @param funcionario nombre del funcionario (nombres o apellidos)
     * @param pagina número de página
     * @param tamanio tamaño de cada página
     * @return paginación de tipos de solicitud que coinciden con los filtros
     */
    PaginacionRespuestaDTO<TipoSolicitud> getTiposSolicitudes(String nombreSolicitud, String funcionario, int pagina, int tamanio);

    /**
     * Obtiene un tipo de solicitud por su identificador único.
     *
     * @param uuidTipoSolicitud identificador del tipo de solicitud
     * @return tipo de solicitud correspondiente
     */
    TipoSolicitud getTipoSolicitud(String uuidTipoSolicitud);

    /**
     * Guarda un nuevo tipo de solicitud o actualiza uno existente.
     *
     * @param tipoSolicitud tipo de solicitud a guardar
     * @return tipo de solicitud guardado
     */
    TipoSolicitud guardarTipoSolicitud(TipoSolicitud tipoSolicitud);

    /**
     * Guarda varios tipos de solicitud en una sola operación.
     *
     * @param tiposSolicitud lista de tipos de solicitud a guardar
     * @return lista de tipos de solicitud guardados
     */
    List<TipoSolicitud> guardarTiposSolicitud(List<TipoSolicitud> tiposSolicitud);

    /**
     * Obtiene tipos de solicitud asociados a un perfil específico.
     *
     * @param perfil nombre del perfil
     * @return lista de tipos de solicitud del perfil
     */
    List<TipoSolicitud> getTiposSolicitudesPorPerfil(String perfil);

    /**
     * Obtiene tipos de solicitud paginados asociados a un perfil de solicitante.
     *
     * @param perfil nombre del perfil
     * @param pagina número de página
     * @param tamanio tamaño de cada página
     * @return paginación de tipos de solicitud del perfil
     */
    PaginacionRespuestaDTO<TipoSolicitud> getTiposSolicitudesPorPerfilSolicitante(String perfil, int pagina, int tamanio);

    /**
     * Obtiene tipos de solicitud filtrados por nombre y perfil de solicitante de forma paginada.
     *
     * @param nombre nombre del tipo de solicitud
     * @param perfil perfil del solicitante
     * @param pagina número de página
     * @param tamanio tamaño de cada página
     * @return paginación de tipos de solicitud que coinciden con los filtros
     */
    PaginacionRespuestaDTO<TipoSolicitud> getTiposSolicitudesPorNombreYPerfilSolicitante(String nombre, String perfil, int pagina, int tamanio);
}
