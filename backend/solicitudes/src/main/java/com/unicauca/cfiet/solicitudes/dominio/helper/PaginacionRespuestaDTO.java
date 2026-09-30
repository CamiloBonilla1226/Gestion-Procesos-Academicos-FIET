package com.unicauca.cfiet.solicitudes.dominio.helper;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * Clase que representa una respuesta paginada.
 * Contiene una lista de elementos de la página actual y el total de elementos disponibles.
 *
 * @param <T> el tipo de elementos que contiene la lista
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
public class PaginacionRespuestaDTO <T>{
    // Entidad a responder
    private List<T> content;
    // Numero de elementos
    private long totalElements;

    public PaginacionRespuestaDTO(List<T> content, long totalElements) {
        this.content = content;
        this.totalElements = totalElements;
    }
}
