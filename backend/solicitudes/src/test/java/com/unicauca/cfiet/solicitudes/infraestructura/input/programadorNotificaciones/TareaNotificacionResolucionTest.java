package com.unicauca.cfiet.solicitudes.infraestructura.input.programadorNotificaciones;

import com.unicauca.cfiet.solicitudes.aplicacion.input.NotificacionResolucionCUIntPuerto;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TareaNotificacionResolucionTest {

    private final NotificacionResolucionCUIntPuerto notificacionCU = mock(NotificacionResolucionCUIntPuerto.class);
    private final TareaNotificacionResolucion tarea = new TareaNotificacionResolucion(notificacionCU);

    @Test
    void cadaEjecucionProcesaLasPendientes() {
        tarea.procesar();
        tarea.procesar();

        verify(notificacionCU, times(2)).procesarPendientes();
    }

    @Test
    void unaFallaNoDetieneLaTarea() {
        when(notificacionCU.procesarPendientes()).thenThrow(new IllegalStateException("base caída")).thenReturn(1);

        assertDoesNotThrow(tarea::procesar);
        assertDoesNotThrow(tarea::procesar);
        verify(notificacionCU, times(2)).procesarPendientes();
    }

    @Test
    void elIntervaloYElPrimerArranqueSonConfigurables() throws Exception {
        Scheduled programacion = TareaNotificacionResolucion.class.getMethod("procesar").getAnnotation(Scheduled.class);

        assertEquals("${correo.resolucion.intervalo-ms:60000}", programacion.fixedDelayString());
        assertEquals("${correo.resolucion.retraso-inicial-ms:15000}", programacion.initialDelayString());
    }
}
