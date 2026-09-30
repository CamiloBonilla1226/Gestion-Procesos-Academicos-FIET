package com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * Interfaz para el procesamiento de archivos excel.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public interface ProcesadorArchivos <T>{
    public List<T> procesarArchivo(MultipartFile file);
}
