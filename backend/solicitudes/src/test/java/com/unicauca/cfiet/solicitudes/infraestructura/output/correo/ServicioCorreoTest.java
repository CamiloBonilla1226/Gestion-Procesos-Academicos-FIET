package com.unicauca.cfiet.solicitudes.infraestructura.output.correo;

import com.unicauca.cfiet.solicitudes.aplicacion.output.AlmacenamientoAnexosIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AdjuntoCorreo;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ServicioCorreoTest {

    private static final String RUTA_RESOLUCION = "/api/anexos/sol-1/resolucion.pdf";
    private static final String RUTA_ADICIONAL = "/api/anexos/sol-1/adicional.pdf";

    private final JavaMailSender enviador = mock(JavaMailSender.class);
    private final AlmacenamientoAnexosIntPuerto almacenamiento = mock(AlmacenamientoAnexosIntPuerto.class);
    private final ServicioCorreo servicio = new ServicioCorreo(enviador, almacenamiento, "fiet@gmail.com");

    @BeforeEach
    void setUp() {
        when(enviador.createMimeMessage()).thenAnswer(invocacion -> new MimeMessage(Session.getInstance(new Properties())));
    }

    private AdjuntoCorreo adjunto(String nombre, String ruta) {
        return AdjuntoCorreo.builder().nombre(nombre).tipoContenido("application/pdf").urlArchivo(ruta).build();
    }

    private MimeMessage enviado() {
        ArgumentCaptor<MimeMessage> mensaje = ArgumentCaptor.forClass(MimeMessage.class);
        verify(enviador).send(mensaje.capture());
        return mensaje.getValue();
    }

    @Test
    void enviaElMensajeSinAdjuntosConRemitenteDestinatarioAsuntoYTexto() throws Exception {
        servicio.enviar("est@unicauca.edu.co", "Notificación de examen supletorio", "Texto", List.of());

        MimeMessage mensaje = enviado();
        assertEquals("fiet@gmail.com", mensaje.getFrom()[0].toString());
        assertEquals("est@unicauca.edu.co", mensaje.getAllRecipients()[0].toString());
        assertEquals("Notificación de examen supletorio", mensaje.getSubject());
        assertEquals("Texto", mensaje.getContent());
    }

    @Test
    void adjuntaLosArchivosLeidosDelAlmacenamiento() throws Exception {
        when(almacenamiento.leer(RUTA_RESOLUCION)).thenReturn(new byte[]{1, 2, 3});
        when(almacenamiento.leer(RUTA_ADICIONAL)).thenReturn(new byte[]{4, 5});

        servicio.enviar("est@unicauca.edu.co", "Notificación de Cancelación de matrícula", "Texto",
                List.of(adjunto("Resolucion-2026-CM-0001.pdf", RUTA_RESOLUCION), adjunto("acta.pdf", RUTA_ADICIONAL)));

        MimeMessage mensaje = enviado();
        mensaje.saveChanges();
        List<String> nombres = new ArrayList<>();
        recogerAdjuntos((Multipart) mensaje.getContent(), nombres);
        assertEquals(List.of("Resolucion-2026-CM-0001.pdf", "acta.pdf"), nombres);
    }

    private void recogerAdjuntos(Multipart partes, List<String> nombres) throws Exception {
        for (int i = 0; i < partes.getCount(); i++) {
            BodyPart parte = partes.getBodyPart(i);
            if (parte.getContent() instanceof Multipart interno)
                recogerAdjuntos(interno, nombres);
            else if (parte.getFileName() != null)
                nombres.add(parte.getFileName());
        }
    }

    @Test
    void siFaltaUnArchivoAdjuntoNoEnviaNiFalla() {
        when(almacenamiento.leer(RUTA_RESOLUCION)).thenReturn(null);

        assertDoesNotThrow(() -> servicio.enviar("est@unicauca.edu.co", "Asunto", "Texto", List.of(adjunto("r.pdf", RUTA_RESOLUCION))));

        verify(enviador, never()).send(any(MimeMessage.class));
    }

    @Test
    void sinCorreoDelDestinatarioNoEnviaNiFalla() {
        assertDoesNotThrow(() -> servicio.enviar(null, "Asunto", "Texto", List.of()));
        assertDoesNotThrow(() -> servicio.enviar("  ", "Asunto", "Texto", List.of()));

        verify(enviador, never()).send(any(MimeMessage.class));
    }

    @Test
    void unFalloDelServidorDeCorreoNoSePropaga() {
        doThrow(new MailSendException("mensaje demasiado grande")).when(enviador).send(any(MimeMessage.class));

        assertDoesNotThrow(() -> servicio.enviar("est@unicauca.edu.co", "Asunto", "Texto", List.of()));
    }

    @Test
    void dentroDeUnaTransaccionElCorreoSaleSoloAlConfirmar() {
        ServicioCorreo servicioMock = mock(ServicioCorreo.class);
        CorreoImplAdaptador adaptador = new CorreoImplAdaptador(servicioMock);
        List<AdjuntoCorreo> adjuntos = List.of(adjunto("r.pdf", RUTA_RESOLUCION));
        TransactionSynchronizationManager.initSynchronization();
        try {
            adaptador.enviar("est@unicauca.edu.co", "Asunto", "Texto", adjuntos);
            verify(servicioMock, never()).enviar(any(), any(), any(), any());

            for (TransactionSynchronization sincronizacion : TransactionSynchronizationManager.getSynchronizations())
                sincronizacion.afterCommit();
            verify(servicioMock).enviar("est@unicauca.edu.co", "Asunto", "Texto", adjuntos);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void siLaTransaccionSeRevierteNoSeEnviaElCorreo() {
        ServicioCorreo servicioMock = mock(ServicioCorreo.class);
        CorreoImplAdaptador adaptador = new CorreoImplAdaptador(servicioMock);
        TransactionSynchronizationManager.initSynchronization();
        try {
            adaptador.enviar("est@unicauca.edu.co", "Asunto", "Texto", List.of());
            for (TransactionSynchronization sincronizacion : TransactionSynchronizationManager.getSynchronizations())
                sincronizacion.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        verify(servicioMock, never()).enviar(any(), any(), any(), any());
    }

    @Test
    void unFalloAlProgramarElEnvioNoSePropaga() {
        ServicioCorreo servicioMock = mock(ServicioCorreo.class);
        doThrow(new IllegalStateException("sin hilos")).when(servicioMock).enviar(any(), any(), any(), any());

        assertDoesNotThrow(() -> new CorreoImplAdaptador(servicioMock).enviar("est@unicauca.edu.co", "Asunto", "Texto", List.of()));
    }
}
