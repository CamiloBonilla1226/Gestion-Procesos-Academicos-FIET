package com.unicauca.cfiet.solicitudes.infraestructura.output.correo;

import com.unicauca.cfiet.solicitudes.aplicacion.output.CorreoIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AdjuntoCorreo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CorreoImplAdaptador implements CorreoIntPuerto {
    private final ServicioCorreo servicioCorreo;

    @Override
    public void enviar(String destinatario, String asunto, String texto, List<AdjuntoCorreo> adjuntos) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            enviarSinFallar(destinatario, asunto, texto, adjuntos);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                enviarSinFallar(destinatario, asunto, texto, adjuntos);
            }
        });
    }

    private void enviarSinFallar(String destinatario, String asunto, String texto, List<AdjuntoCorreo> adjuntos) {
        try {
            servicioCorreo.enviar(destinatario, asunto, texto, adjuntos);
        } catch (RuntimeException error) {
            log.error("No se pudo programar el correo \"{}\" a {}: {}", asunto, destinatario, error.getMessage());
        }
    }
}
