package com.unicauca.cfiet.solicitudes.infraestructura.output.correo;

import com.unicauca.cfiet.solicitudes.aplicacion.output.CorreoIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AdjuntoCorreo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.CorreoPreparado;
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
    private final ColaCorreos colaCorreos;

    @Override
    public void enviar(String destinatario, String asunto, String texto, List<AdjuntoCorreo> adjuntos) {
        if (destinatario == null || destinatario.isBlank()) {
            log.warn("No se envió el correo \"{}\": el destinatario no tiene correo registrado", asunto);
            return;
        }
        Long id = guardar(destinatario, asunto, texto, adjuntos);
        if (id == null)
            return;
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            enviarSinFallar(id, asunto, destinatario);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                enviarSinFallar(id, asunto, destinatario);
            }
        });
    }

    @Override
    public void enviarAhora(CorreoPreparado correo) {
        servicioCorreo.enviarAhora(correo);
    }

    @Override
    public int contarPendientes() {
        return colaCorreos.contarPendientes();
    }

    private Long guardar(String destinatario, String asunto, String texto, List<AdjuntoCorreo> adjuntos) {
        try {
            return colaCorreos.guardar(destinatario, asunto, texto, adjuntos);
        } catch (RuntimeException error) {
            log.error("No se pudo guardar el correo \"{}\" a {}: {}", asunto, destinatario, error.getMessage());
            return null;
        }
    }

    private void enviarSinFallar(Long id, String asunto, String destinatario) {
        try {
            colaCorreos.enviarEnSegundoPlano(id);
        } catch (RuntimeException error) {
            log.error("No se pudo programar el correo \"{}\" a {}: {}", asunto, destinatario, error.getMessage());
        }
    }
}
