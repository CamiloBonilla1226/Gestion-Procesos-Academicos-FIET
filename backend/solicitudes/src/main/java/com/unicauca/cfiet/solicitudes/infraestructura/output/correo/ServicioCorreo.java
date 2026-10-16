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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ServicioCorreo {
    public static final String NOMBRE_REMITENTE = "Decanatura FIET - Procesos académicos";
    private static final String INICIO_HTML = "<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\"></head>"
            + "<body style=\"margin:0;padding:16px;font-family:Arial,Helvetica,sans-serif;font-size:14px;line-height:1.5;color:#222222\">";
    private static final String FIN_HTML = "</body></html>";
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
            MimeMessageHelper ayudante = new MimeMessageHelper(mensaje, true, "UTF-8");
            ayudante.setFrom(remitente, NOMBRE_REMITENTE);
            ayudante.setTo(destinatario);
            ayudante.setSubject(asunto);
            ayudante.setText(texto, html(texto));
            for (AdjuntoCorreo adjunto : archivos)
                ayudante.addAttachment(adjunto.getNombre(), new ByteArrayResource(contenidoDe(adjunto)), adjunto.getTipoContenido());
            enviador.send(mensaje);
        } catch (MessagingException | UnsupportedEncodingException | RuntimeException error) {
            log.error("No se pudo enviar el correo \"{}\" a {}: {}", asunto, destinatario, error.getMessage());
        }
    }

    static String html(String texto) {
        String limpio = texto == null ? "" : texto.replace("\r\n", "\n").replace('\r', '\n').strip();
        String parrafos = Arrays.stream(limpio.split("\n\\s*\n"))
                .filter(parrafo -> !parrafo.isBlank())
                .map(parrafo -> "<p style=\"margin:0 0 12px 0\">" + escapar(parrafo.strip()).replace("\n", "<br>") + "</p>")
                .collect(Collectors.joining());
        return INICIO_HTML + parrafos + FIN_HTML;
    }

    private static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
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
