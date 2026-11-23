package com.unicauca.cfiet.solicitudes.infraestructura.output.ejecucion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EjecutorAsincrono {

    @Async
    public void ejecutar(String descripcion, Runnable tarea) {
        try {
            tarea.run();
        } catch (RuntimeException error) {
            log.error("No se pudo completar {}: {}", descripcion, error.getMessage());
        }
    }
}
