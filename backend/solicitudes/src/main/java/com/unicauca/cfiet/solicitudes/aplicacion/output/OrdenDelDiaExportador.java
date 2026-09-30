package com.unicauca.cfiet.solicitudes.aplicacion.output;

import java.util.HashMap;

/**
 * Interfaz para exportar información de un Orden del Día.
 * Permite generar la representación en bytes del orden del día.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public interface OrdenDelDiaExportador {

    /**
     * Exporta un Orden del Día a un arreglo de bytes.
     *
     * @param data información del orden del día en formato clave-valor
     * @return contenido del orden del día en bytes
     */
    byte[] exportarOrdenDelDia(HashMap<String, String> data, String plantilla);
}
