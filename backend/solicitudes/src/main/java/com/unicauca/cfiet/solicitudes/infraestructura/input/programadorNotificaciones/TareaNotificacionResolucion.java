package com.unicauca.cfiet.solicitudes.infraestructura.input.programadorNotificaciones;

import com.unicauca.cfiet.solicitudes.aplicacion.input.NotificacionResolucionCUIntPuerto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TareaNotificacionResolucion {
    private final NotificacionResolucionCUIntPuerto notificacionCU;

    @Scheduled(fixedDelayString = "${correo.resolucion.intervalo-ms:60000}",
            initialDelayString = "${correo.resolucion.retraso-inicial-ms:15000}")
    public void procesar() {
        try {
            int procesadas = notificacionCU.procesarPendientes();
            if (procesadas > 0)
                log.info("Comunicaciones de la Resolución procesadas: {}", procesadas);
        } catch (RuntimeException error) {
            log.error("No se pudieron procesar las comunicaciones programadas de la Resolución: {}", error.getMessage());
        }
    }
}
