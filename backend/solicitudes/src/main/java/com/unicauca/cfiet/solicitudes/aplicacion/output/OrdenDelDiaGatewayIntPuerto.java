package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.OrdenDelDia;
import java.util.List;

/**
 * Interfaz que actúa como fachada hacia la capa de persistencia para la gestión del Orden del Día.
 *
 * author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public interface OrdenDelDiaGatewayIntPuerto {

    /**
     * Obtiene todos los órdenes del día almacenados.
     *
     * @return lista de órdenes del día
     */
    List<OrdenDelDia> getOrdenesDelDia();

    /**
     * Obtiene los órdenes del día filtrados por estado.
     *
     * @param estado estado de los órdenes
     * @return lista de órdenes del día correspondientes al estado
     */
    List<OrdenDelDia> getOrdenesDelDiaPorEstado(boolean estado);

    /**
     * Obtiene órdenes del día paginados.
     *
     * @param pagina número de página
     * @param tamanio tamaño de cada página
     * @return paginación de órdenes del día
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
     * Guarda un nuevo orden del día o actualiza uno existente.
     *
     * @param ordenDelDia orden del día a guardar
     * @return orden del día guardado
     */
    OrdenDelDia guardarOrdenDelDia(OrdenDelDia ordenDelDia);

    /**
     * Obtiene órdenes del día filtrados por un texto y de forma paginada.
     *
     * @param filtro texto de búsqueda
     * @param pagina número de página
     * @param tamanio tamaño de cada página
     * @return paginación de órdenes del día que coinciden con el filtro
     */
    PaginacionRespuestaDTO<OrdenDelDia> getOrdenesDelDia(String filtro, int pagina, int tamanio);
}
