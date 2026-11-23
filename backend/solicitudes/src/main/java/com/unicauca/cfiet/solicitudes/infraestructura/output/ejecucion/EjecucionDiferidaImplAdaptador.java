package com.unicauca.cfiet.solicitudes.infraestructura.output.ejecucion;

import com.unicauca.cfiet.solicitudes.aplicacion.output.EjecucionDiferidaIntPuerto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class EjecucionDiferidaImplAdaptador implements EjecucionDiferidaIntPuerto {
    private final EjecutorAsincrono ejecutor;

    @Override
    public void despuesDeConfirmar(String descripcion, Runnable tarea) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            ejecutor.ejecutar(descripcion, tarea);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                ejecutor.ejecutar(descripcion, tarea);
            }
        });
    }
}
