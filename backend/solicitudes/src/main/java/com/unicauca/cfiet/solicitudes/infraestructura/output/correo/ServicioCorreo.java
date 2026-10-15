package com.unicauca.cfiet.solicitudes.infraestructura.output.correo;

import com.unicauca.cfiet.solicitudes.aplicacion.output.AlmacenamientoAnexosIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AdjuntoCorreo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ServicioCorreo {
    private final JavaMailSender enviador;
    private final AlmacenamientoAnexosIntPuerto almacenamiento;
    private final String remitente;

    public ServicioCorreo(JavaMailSender enviador, AlmacenamientoAnexosIntPuerto almacenamiento,
                          @Value("${spring.mail.username:}") String remitente) {
        this.enviador = enviador;
        this.almacenamiento = almacenamiento;
        this.remitente = remitente;
    }

    @Async
    public void enviar(String destinatario, String asunto, String texto, List<AdjuntoCorreo> adjuntos) {
        if (destinatario == null || destinatario.isBlank()) {
            log.warn("No se envió el correo \"{}\": el destinatario no tiene correo registrado", asunto);
            return;
        }
        try {
            List<AdjuntoCorreo> archivos = adjuntos == null ? List.of() : adjuntos;
            MimeMessage mensaje = enviador.createMimeMessage();
            MimeMessageHelper ayudante = new MimeMessageHelper(mensaje, !archivos.isEmpty(), "UTF-8");
            ayudante.setFrom(remitente);
            ayudante.setTo(destinatario);
            ayudante.setSubject(asunto);
            ayudante.setText(texto);
            for (AdjuntoCorreo adjunto : archivos)
                ayudante.addAttachment(adjunto.getNombre(), new ByteArrayResource(contenidoDe(adjunto)), adjunto.getTipoContenido());
            enviador.send(mensaje);
        } catch (MessagingException | RuntimeException error) {
            log.error("No se pudo enviar el correo \"{}\" a {}: {}", asunto, destinatario, error.getMessage());
        }
    }

    private byte[] contenidoDe(AdjuntoCorreo adjunto) {
        if (adjunto.getContenido() != null)
            return adjunto.getContenido();
        byte[] contenido = almacenamiento.leer(adjunto.getUrlArchivo());
        if (contenido == null)
            throw new IllegalStateException("No se encontró el archivo adjunto " + adjunto.getNombre());
        return contenido;
    }
}
