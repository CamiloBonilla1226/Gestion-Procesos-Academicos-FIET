package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.OrdenDelDia;
import java.util.List;

/**
 * Interfaz de caso de uso para la gestión de Ordenes del Día.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public interface OrdenDelDiaCUIntPuerto {

    /**
     * Obtiene todas las órdenes del día.
     *
     * @return lista completa de órdenes del día
     */
    List<OrdenDelDia> getOrdenesDelDia();

    /**
     * Obtiene las órdenes del día filtradas por estado.
     *
     * @param estado estado de las órdenes
     * @return lista de órdenes del día filtradas
     */
    List<OrdenDelDia> getOrdenesDelDiaPorEstado(boolean estado);

    /**
     * Obtiene órdenes del día paginadas.
     *
     * @param pagina número de página
     * @param tamanio tamaño de la página
     * @return lista de órdenes del día correspondientes a la página
     */
    PaginacionRespuestaDTO<OrdenDelDia> getOrdenesDelDia(int pagina, int tamanio);

    /**
     * Obtiene un orden del día por su identificador.
     *
     * @param uuidOrdenDelDia identificador único del orden del día
     * @return orden del día correspondiente
     */
    OrdenDelDia getOrdenDelDia(String uuidOrdenDelDia);

    /**
     * Crea un nuevo orden del día.
     *
     * @param ordenDelDia orden del día a crear
     * @param token token de autorización
     * @return orden del día creado
     */
    OrdenDelDia crearOrdenDelDia(OrdenDelDia ordenDelDia, String token);

    /**
     * Actualiza un orden del día existente.
     *
     * @param uuidOrdenDelDia identificador único del orden del día
     * @param ordenDelDia información actualizada del orden del día
     * @param token token de autorización
     * @return orden del día actualizado
     */
    OrdenDelDia actualizarOrdenDelDia(String uuidOrdenDelDia, OrdenDelDia ordenDelDia, String token);

    /**
     * Busca órdenes del día por número de acta con paginación.
     *
     * @param nombre número de acta o nombre asociado
     * @param pagina número de página
     * @param tamanio tamaño de la página
     * @return lista de órdenes del día correspondientes a la búsqueda
     */
    PaginacionRespuestaDTO<OrdenDelDia> buscarOrdenDelDiaPorNumeroActa(String nombre, int pagina, int tamanio);

    /**
     * Genera el documento de un orden del día.
     *
     * @param uuidOrden identificador del orden del día
     * @return contenido del documento en bytes
     */
    byte[] generarOrdenDelDia(String uuidOrden);

    /**
     * Genera el documento de un orden del día con respuestas.
     *
     * @param uuidOrden identificador del orden del día
     * @return contenido del documento en bytes
     */
    byte[] generarOrdenDelDiaConRespuestas(String uuidOrden);

    /**
     * Genera el documento de un orden del día con respuestas en la sección de desarrollo de la reunión.
     *
     * @param uuidOrden identificador del orden del día
     * @return contenido del documento en bytes
     */
    byte[] generarOrdenDelDiaMerge(String uuidOrden);
}
