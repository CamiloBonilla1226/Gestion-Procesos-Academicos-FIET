package com.unicauca.cfiet.solicitudes.infraestructura.output.correo;

import com.unicauca.cfiet.solicitudes.aplicacion.output.AlmacenamientoAnexosIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AdjuntoCorreo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.CorreoPreparado;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

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
        mensaje.saveChanges();
        InternetAddress remitente = (InternetAddress) mensaje.getFrom()[0];
        assertEquals("fiet@gmail.com", remitente.getAddress());
        assertEquals("Decanatura FIET - Procesos académicos", remitente.getPersonal());
        assertEquals("est@unicauca.edu.co", mensaje.getAllRecipients()[0].toString());
        assertEquals("Notificación de examen supletorio", mensaje.getSubject());
        assertEquals("Texto", parteDeTipo((Multipart) mensaje.getContent(), "text/plain"));
    }

    @Test
    void cadaCorreoLlevaTextoPlanoYUnHtmlSimpleSinImagenesNiEnlaces() throws Exception {
        String texto = "Popayán, Cauca, 15 de octubre de 2026" + System.lineSeparator() + System.lineSeparator()
                + "Sr(a) Ana <Pérez> & Cía" + System.lineSeparator() + "Estudiante" + System.lineSeparator() + System.lineSeparator()
                + "Ingrese a la aplicación.";
        when(almacenamiento.leer(RUTA_RESOLUCION)).thenReturn(new byte[]{1, 2, 3});

        servicio.enviar("est@unicauca.edu.co", "Asunto", texto, List.of(adjunto("Resolucion.pdf", RUTA_RESOLUCION)));

        MimeMessage mensaje = enviado();
        mensaje.saveChanges();
        Multipart partes = (Multipart) mensaje.getContent();
        assertEquals(texto, parteDeTipo(partes, "text/plain"));
        String html = parteDeTipo(partes, "text/html");
        assertTrue(html.contains("<p style=\"margin:0 0 12px 0\">Popayán, Cauca, 15 de octubre de 2026</p>"));
        assertTrue(html.contains("Sr(a) Ana &lt;Pérez&gt; &amp; Cía<br>Estudiante"));
        assertTrue(html.contains("Ingrese a la aplicación."));
        assertFalse(html.contains("<img"));
        assertFalse(html.contains("<a "));
        assertFalse(html.contains("http"));
        List<String> nombres = new ArrayList<>();
        recogerAdjuntos(partes, nombres);
        assertEquals(List.of("Resolucion.pdf"), nombres);
    }

    private String parteDeTipo(Multipart partes, String tipo) throws Exception {
        for (int i = 0; i < partes.getCount(); i++) {
            BodyPart parte = partes.getBodyPart(i);
            if (parte.getContent() instanceof Multipart interno) {
                String encontrado = parteDeTipo(interno, tipo);
                if (encontrado != null) return encontrado;
            } else if (parte.isMimeType(tipo) && parte.getFileName() == null) {
                return (String) parte.getContent();
            }
        }
        return null;
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

    private ColaCorreos colaConIds(Long... ids) {
        ColaCorreos cola = mock(ColaCorreos.class);
        when(cola.guardar(any(), any(), any(), any())).thenReturn(ids[0], java.util.Arrays.copyOfRange(ids, 1, ids.length));
        return cola;
    }

    @Test
    void dentroDeUnaTransaccionElCorreoSeGuardaYSaleSoloAlConfirmar() {
        ColaCorreos cola = colaConIds(1L);
        CorreoImplAdaptador adaptador = new CorreoImplAdaptador(mock(ServicioCorreo.class), cola);
        List<AdjuntoCorreo> adjuntos = List.of(adjunto("r.pdf", RUTA_RESOLUCION));
        TransactionSynchronizationManager.initSynchronization();
        try {
            adaptador.enviar("est@unicauca.edu.co", "Asunto", "Texto", adjuntos);
            verify(cola).guardar("est@unicauca.edu.co", "Asunto", "Texto", adjuntos);
            verify(cola, never()).enviarEnSegundoPlano(any());

            for (TransactionSynchronization sincronizacion : TransactionSynchronizationManager.getSynchronizations())
                sincronizacion.afterCommit();
            verify(cola).enviarEnSegundoPlano(1L);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void siLaTransaccionSeRevierteNoSeEnviaElCorreo() {
        ColaCorreos cola = colaConIds(1L);
        CorreoImplAdaptador adaptador = new CorreoImplAdaptador(mock(ServicioCorreo.class), cola);
        TransactionSynchronizationManager.initSynchronization();
        try {
            adaptador.enviar("est@unicauca.edu.co", "Asunto", "Texto", List.of());
            for (TransactionSynchronization sincronizacion : TransactionSynchronizationManager.getSynchronizations())
                sincronizacion.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        verify(cola, never()).enviarEnSegundoPlano(any());
    }

    @Test
    void unFalloAlProgramarElEnvioNoSePropaga() {
        ColaCorreos cola = colaConIds(1L);
        doThrow(new IllegalStateException("sin hilos")).when(cola).enviarEnSegundoPlano(any());

        assertDoesNotThrow(() -> new CorreoImplAdaptador(mock(ServicioCorreo.class), cola).enviar("est@unicauca.edu.co", "Asunto", "Texto", List.of()));
    }

    @Test
    void unFalloAlGuardarElCorreoNoSePropagaNiLoEnvia() {
        ColaCorreos cola = mock(ColaCorreos.class);
        when(cola.guardar(any(), any(), any(), any())).thenThrow(new IllegalStateException("sin base"));

        assertDoesNotThrow(() -> new CorreoImplAdaptador(mock(ServicioCorreo.class), cola).enviar("est@unicauca.edu.co", "Asunto", "Texto", List.of()));
        verify(cola, never()).enviarEnSegundoPlano(any());
    }

    @Test
    void sinDestinatarioNoSeGuardaNiSeEnvia() {
        ColaCorreos cola = mock(ColaCorreos.class);
        CorreoImplAdaptador adaptador = new CorreoImplAdaptador(mock(ServicioCorreo.class), cola);

        adaptador.enviar(null, "Asunto", "Texto", List.of());
        adaptador.enviar("  ", "Asunto", "Texto", List.of());

        verifyNoInteractions(cola);
    }

    @Test
    void sinConexionEnviarAhoraLanzaUnaFallaTransitoria() {
        doThrow(new MailSendException("Mail server connection failed", new jakarta.mail.MessagingException("Could not connect",
                new java.net.UnknownHostException("smtp.gmail.com")))).when(enviador).send(any(MimeMessage.class));

        assertThrows(com.unicauca.cfiet.solicitudes.aplicacion.output.FallaTransitoriaCorreoException.class, () -> servicio.enviarAhora(
                CorreoPreparado.builder().destinatario("est@unicauca.edu.co").asunto("Asunto").texto("Texto").build()));
    }

    @Test
    void unRechazoDelServidorNoEsUnaFallaTransitoria() {
        doThrow(new MailSendException("550 buzon inexistente")).when(enviador).send(any(MimeMessage.class));

        RuntimeException error = assertThrows(MailSendException.class, () -> servicio.enviarAhora(
                CorreoPreparado.builder().destinatario("est@unicauca.edu.co").asunto("Asunto").texto("Texto").build()));
        assertFalse(error instanceof com.unicauca.cfiet.solicitudes.aplicacion.output.FallaTransitoriaCorreoException);
    }

    @Test
    void unAdjuntoGeneradoEnMemoriaNoSeLeeDelAlmacenamiento() throws Exception {
        servicio.enviar("darca@unicauca.edu.co", "DARCA", "Texto", List.of(AdjuntoCorreo.builder()
                .nombre("Radicado-2026-CM-0001.pdf").tipoContenido("application/pdf").contenido(new byte[]{37, 80, 68, 70}).build()));

        MimeMessage mensaje = enviado();
        mensaje.saveChanges();
        List<String> nombres = new ArrayList<>();
        recogerAdjuntos((Multipart) mensaje.getContent(), nombres);
        assertEquals(List.of("Radicado-2026-CM-0001.pdf"), nombres);
        verifyNoInteractions(almacenamiento);
    }

    @Test
    void siUnEnvioFallaLosDemasSalenIgual() {
        doThrow(new MailSendException("buzon lleno")).doNothing().doNothing().when(enviador).send(any(MimeMessage.class));

        assertDoesNotThrow(() -> servicio.enviar("est@unicauca.edu.co", "Notificación de examen supletorio", "Texto", List.of()));
        assertDoesNotThrow(() -> servicio.enviar("darca@unicauca.edu.co", "DARCA - Notificación de examen supletorio - Radicado 2026-ES-0001",
                "Texto", List.of()));
        assertDoesNotThrow(() -> servicio.enviar("prof@unicauca.edu.co", "Notificación de examen supletorio - Radicado 2026-ES-0001",
                "Texto", List.of()));

        verify(enviador, times(3)).send(any(MimeMessage.class));
    }

    @Test
    void laPreparacionCorreAntesDeConfirmarYSuCorreoSaleDespuesDelCommit() {
        ColaCorreos cola = colaConIds(1L, 2L);
        CorreoImplAdaptador correo = new CorreoImplAdaptador(mock(ServicioCorreo.class), cola);
        PreparacionCorreoImplAdaptador preparacion = new PreparacionCorreoImplAdaptador();
        List<String> pasos = new ArrayList<>();
        TransactionSynchronizationManager.initSynchronization();
        try {
            correo.enviar("est@unicauca.edu.co", "Estudiante", "Texto", List.of());
            preparacion.antesDeConfirmar("el correo a DARCA", () -> {
                pasos.add("preparado");
                correo.enviar("darca@unicauca.edu.co", "DARCA", "Texto", List.of());
            });
            assertTrue(pasos.isEmpty());

            TransactionSynchronizationUtils.triggerBeforeCommit(false);
            assertEquals(List.of("preparado"), pasos);
            verify(cola, never()).enviarEnSegundoPlano(any());

            TransactionSynchronizationUtils.triggerAfterCommit();
            verify(cola).enviarEnSegundoPlano(1L);
            verify(cola).enviarEnSegundoPlano(2L);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void siFallaLaPreparacionDeUnCorreoNoSePropagaYLosDemasSalen() {
        ColaCorreos cola = colaConIds(1L, 2L);
        CorreoImplAdaptador correo = new CorreoImplAdaptador(mock(ServicioCorreo.class), cola);
        PreparacionCorreoImplAdaptador preparacion = new PreparacionCorreoImplAdaptador();
        TransactionSynchronizationManager.initSynchronization();
        try {
            correo.enviar("est@unicauca.edu.co", "Estudiante", "Texto", List.of());
            preparacion.antesDeConfirmar("el correo a DARCA", () -> {
                throw new IllegalStateException("pdf roto");
            });
            preparacion.antesDeConfirmar("el correo al profesor", () -> correo.enviar("prof@unicauca.edu.co", "Profesor", "Texto", List.of()));

            assertDoesNotThrow(() -> TransactionSynchronizationUtils.triggerBeforeCommit(false));
            TransactionSynchronizationUtils.triggerAfterCommit();

            verify(cola).enviarEnSegundoPlano(1L);
            verify(cola).enviarEnSegundoPlano(2L);
            verify(cola, times(2)).enviarEnSegundoPlano(any());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void siLaTransaccionSeRevierteNoSePreparaNiSeEnvia() {
        ColaCorreos cola = colaConIds(1L);
        CorreoImplAdaptador correo = new CorreoImplAdaptador(mock(ServicioCorreo.class), cola);
        List<String> pasos = new ArrayList<>();
        TransactionSynchronizationManager.initSynchronization();
        try {
            new PreparacionCorreoImplAdaptador().antesDeConfirmar("el correo a DARCA", () -> {
                pasos.add("preparado");
                correo.enviar("darca@unicauca.edu.co", "DARCA", "Texto", List.of());
            });
            TransactionSynchronizationUtils.triggerAfterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        assertTrue(pasos.isEmpty());
        verify(cola, never()).guardar(any(), any(), any(), any());
        verify(cola, never()).enviarEnSegundoPlano(any());
    }

    @Test
    void sinTransaccionLaPreparacionCorreDeInmediatoYSuFallaNoSePropaga() {
        List<String> pasos = new ArrayList<>();
        PreparacionCorreoImplAdaptador preparacion = new PreparacionCorreoImplAdaptador();

        preparacion.antesDeConfirmar("uno", () -> pasos.add("uno"));
        assertDoesNotThrow(() -> preparacion.antesDeConfirmar("dos", () -> {
            throw new IllegalStateException("roto");
        }));

        assertEquals(List.of("uno"), pasos);
    }

    @Test
    void enviarAhoraUsaElHtmlConNegrillaDentroDelMarcoYElTextoPlano() throws Exception {
        when(almacenamiento.leer(RUTA_RESOLUCION)).thenReturn(new byte[]{1, 2, 3});

        servicio.enviarAhora(CorreoPreparado.builder()
                .destinatario("est@unicauca.edu.co")
                .asunto("Notificación de Cancelación de asignatura")
                .texto("No. 123/178")
                .html("<p style=\"margin:0 0 12px 0\">No. <strong>123</strong>/178</p>")
                .adjuntos(List.of(adjunto("Resolucion.pdf", RUTA_RESOLUCION)))
                .build());

        MimeMessage mensaje = enviado();
        mensaje.saveChanges();
        Multipart partes = (Multipart) mensaje.getContent();
        assertEquals("No. 123/178", parteDeTipo(partes, "text/plain"));
        String html = parteDeTipo(partes, "text/html");
        assertTrue(html.startsWith("<!DOCTYPE html>"));
        assertTrue(html.contains("No. <strong>123</strong>/178"));
        assertEquals("Notificación de Cancelación de asignatura", mensaje.getSubject());
    }

    @Test
    void enviarAhoraSinHtmlLoArmaDelTextoEscapado() throws Exception {
        servicio.enviarAhora(CorreoPreparado.builder().destinatario("darca@unicauca.edu.co").asunto("DARCA").texto("A <b> & C").build());

        MimeMessage mensaje = enviado();
        mensaje.saveChanges();
        assertTrue(parteDeTipo((Multipart) mensaje.getContent(), "text/html").contains("A &lt;b&gt; &amp; C"));
    }

    @Test
    void enviarAhoraPropagaLasFallasParaQueSeReintente() {
        assertThrows(IllegalStateException.class, () -> servicio.enviarAhora(CorreoPreparado.builder().asunto("A").texto("B").build()));
        doThrow(new MailSendException("smtp caído")).when(enviador).send(any(MimeMessage.class));
        assertThrows(MailSendException.class, () -> servicio.enviarAhora(CorreoPreparado.builder().destinatario("a@b.co").asunto("A").texto("B").build()));
        when(almacenamiento.leer(RUTA_ADICIONAL)).thenReturn(null);
        assertThrows(IllegalStateException.class, () -> servicio.enviarAhora(CorreoPreparado.builder().destinatario("a@b.co").asunto("A").texto("B")
                .adjuntos(List.of(adjunto("adicional.pdf", RUTA_ADICIONAL))).build()));
    }
}
