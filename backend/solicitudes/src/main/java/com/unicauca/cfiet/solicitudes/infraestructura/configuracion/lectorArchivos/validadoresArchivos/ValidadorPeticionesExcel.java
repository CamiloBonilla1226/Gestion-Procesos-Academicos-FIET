package com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.validadoresArchivos;

import java.util.Map;

/**
 * Interfaz para validación de peticiones recibidas por archivos excel.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public interface ValidadorPeticionesExcel <T>{
    Map<String, String> validar(T peticion);
}
