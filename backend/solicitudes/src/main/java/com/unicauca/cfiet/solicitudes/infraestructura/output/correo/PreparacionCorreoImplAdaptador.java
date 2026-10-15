package com.unicauca.cfiet.solicitudes.infraestructura.output.correo;

import com.unicauca.cfiet.solicitudes.aplicacion.output.PreparacionCorreoIntPuerto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
public class PreparacionCorreoImplAdaptador implements PreparacionCorreoIntPuerto {

    @Override
    public void antesDeConfirmar(String descripcion, Runnable preparacion) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            prepararSinFallar(descripcion, preparacion);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void beforeCommit(boolean readOnly) {
                prepararSinFallar(descripcion, preparacion);
            }
        });
    }

    private void prepararSinFallar(String descripcion, Runnable preparacion) {
        try {
            preparacion.run();
        } catch (RuntimeException error) {
            log.error("No se pudo preparar {}: {}", descripcion, error.getMessage());
        }
    }
}
