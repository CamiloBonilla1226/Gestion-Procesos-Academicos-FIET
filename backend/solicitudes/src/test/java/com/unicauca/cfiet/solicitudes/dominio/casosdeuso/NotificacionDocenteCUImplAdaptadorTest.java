package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionDocenteCUImplAdaptadorTest {

    private static final String DOCENTE = "doc-1";
    private static final String OTRO_DOCENTE = "doc-2";
    private static final String TOKEN_DOCENTE = "token-doc";
    private static final String TOKEN_OTRO_DOCENTE = "token-doc2";
    private static final String TOKEN_FUNCIONARIO = "token-fun";
    private static final String NOTIFICACION = "not-1";
    private static final String SOLICITUD = "sol-1";
    private static final LocalDateTime CREADA = LocalDateTime.of(2026, 10, 6, 9, 0);

    @Mock
    private NotificacionDocenteGatewayIntPuerto gateway;
    @Mock
    private SolicitudAcademicaGatewayIntPuerto solicitudGateway;
    @Mock
    private SolicitudExamenSupletorioGatewayIntPuerto supletorioGateway;
    @Mock
    private AnexoAcademicoGatewayIntPuerto anexoGateway;
    @Mock
    private AlmacenamientoAnexosIntPuerto almacenamiento;
    @Mock
    private SesionGatewayIntPuerto sesionGateway;
    @Mock
    private IJwtServicio jwtServicio;

    private NotificacionDocenteCUImplAdaptador casoDeUso;

    private Usuario usuario(String uuid, String rol) {
        return Usuario.builder()
                .uuidUsuario(uuid)
                .roles(new ArrayList<>(List.of(Rol.builder().uuidRol("rol-" + rol).nombre(rol).estado(true).build())))
                .build();
    }

    private void sesion(String token, Usuario usuario) {
        lenient().when(jwtServicio.getUsername(token)).thenReturn("user-" + token);
        lenient().when(sesionGateway.getUsuario("user-" + token)).thenReturn(usuario);
    }

    private NotificacionDocente notificacion(String uuid, String docente, LocalDateTime leida) {
        return NotificacionDocente.builder()
                .idNotificacion(1L)
                .uuidNotificacion(uuid)
                .uuidSolicitudAcademica(SOLICITUD)
                .docente(Docente.builder().uuidUsuario(docente).build())
                .titulo("Examen supletorio aprobado, radicado 2026-ES-0001")
                .fechaCreacion(CREADA)
                .fechaLectura(leida)
                .build();
    }

    private AnexoAcademico anexo(String uuid, String solicitud) {
        return AnexoAcademico.builder()
                .uuidAnexoAcademico(uuid)
                .solicitudAcademica(SolicitudAcademica.builder().uuidSolicitudAcademica(solicitud).build())
                .nombreArchivo("formato.pdf")
                .urlArchivo("anexos/" + solicitud + "/" + uuid + ".pdf")
                .tipoArchivo("application/pdf")
                .build();
    }

    @BeforeEach
    void setUp() {
        casoDeUso = new NotificacionDocenteCUImplAdaptador(gateway, solicitudGateway, supletorioGateway, anexoGateway, almacenamiento,
                sesionGateway, jwtServicio, new ExcepcionesFormateadorImplAdaptador(),
                Clock.fixed(Instant.parse("2026-10-07T15:00:00Z"), ZoneId.of("America/Bogota")));
        sesion(TOKEN_DOCENTE, usuario(DOCENTE, "Docente"));
        sesion(TOKEN_OTRO_DOCENTE, usuario(OTRO_DOCENTE, "Docente"));
        sesion(TOKEN_FUNCIONARIO, usuario(DOCENTE, "Funcionario Académico"));
        lenient().when(solicitudGateway.getPorUuid(SOLICITUD)).thenReturn(SolicitudAcademica.builder()
                .uuidSolicitudAcademica(SOLICITUD).radicado("2026-ES-0001").build());
        lenient().when(supletorioGateway.getPorSolicitud(SOLICITUD)).thenReturn(SolicitudExamenSupletorio.builder().build());
        lenient().when(anexoGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of(anexo("an-1", SOLICITUD)));
    }

    private void verificarQueNoSeEscribioNada() {
        verify(gateway, never()).marcarLeida(any(), any());
        verify(almacenamiento, never()).leer(any());
    }

    @Test
    void elDocenteVeSoloSusNotificacionesSinAnexosEnLaLista() {
        when(gateway.getPorDocente(DOCENTE)).thenReturn(List.of(notificacion(NOTIFICACION, DOCENTE, null)));

        List<DetalleNotificacionDocente> lista = casoDeUso.getMisNotificaciones(TOKEN_DOCENTE);

        assertEquals(1, lista.size());
        assertEquals(NOTIFICACION, lista.get(0).getNotificacion().getUuidNotificacion());
        assertEquals("2026-ES-0001", lista.get(0).getSolicitudAcademica().getRadicado());
        assertTrue(lista.get(0).getAnexos().isEmpty());
        verify(gateway, never()).getPorDocente(OTRO_DOCENTE);
        verify(anexoGateway, never()).getPorSolicitud(any());
    }

    @Test
    void unUsuarioSinRolDocenteNoRecibeNotificaciones() {
        assertTrue(casoDeUso.getMisNotificaciones(TOKEN_FUNCIONARIO).isEmpty());

        verify(gateway, never()).getPorDocente(any());
    }

    @Test
    void elDocenteVeElDetalleDeSuNotificacionConLosAnexos() {
        when(gateway.getPorUuid(NOTIFICACION)).thenReturn(notificacion(NOTIFICACION, DOCENTE, null));

        DetalleNotificacionDocente detalle = casoDeUso.getNotificacion(NOTIFICACION, TOKEN_DOCENTE);

        assertEquals(SOLICITUD, detalle.getSolicitudAcademica().getUuidSolicitudAcademica());
        assertNotNull(detalle.getSupletorio());
        assertEquals("an-1", detalle.getAnexos().get(0).getUuidAnexoAcademico());
        verificarQueNoSeEscribioNada();
    }

    @Test
    void otroDocenteRecibeElErrorDeAccesoAjenoComoSiNoExistiera() {
        when(gateway.getPorUuid(NOTIFICACION)).thenReturn(notificacion(NOTIFICACION, DOCENTE, null));

        ErrorEntidadNoExisteExcepcion error = assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.getNotificacion(NOTIFICACION, TOKEN_OTRO_DOCENTE));

        assertEquals(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, "Notificación", NOTIFICACION), error.getMessage());
        verify(solicitudGateway, never()).getPorUuid(any());
        verificarQueNoSeEscribioNada();
    }

    @Test
    void unaNotificacionInexistenteDaElMismoError() {
        when(gateway.getPorUuid("nada")).thenReturn(null);

        ErrorEntidadNoExisteExcepcion error = assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.getNotificacion("nada", TOKEN_DOCENTE));

        assertEquals(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, "Notificación", "nada"), error.getMessage());
    }

    @Test
    void elMismoUsuarioSinRolDocenteNoVeLaNotificacion() {
        when(gateway.getPorUuid(NOTIFICACION)).thenReturn(notificacion(NOTIFICACION, DOCENTE, null));

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getNotificacion(NOTIFICACION, TOKEN_FUNCIONARIO));
    }

    @Test
    void abrirLaNotificacionLaMarcaComoLeidaConLaFechaDelReloj() {
        when(gateway.getPorUuid(NOTIFICACION)).thenReturn(notificacion(NOTIFICACION, DOCENTE, null));
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 7, 10, 0);
        when(gateway.marcarLeida(NOTIFICACION, ahora)).thenReturn(notificacion(NOTIFICACION, DOCENTE, ahora));

        DetalleNotificacionDocente detalle = casoDeUso.marcarLeida(NOTIFICACION, TOKEN_DOCENTE);

        assertEquals(ahora, detalle.getNotificacion().getFechaLectura());
    }

    @Test
    void unaNotificacionYaLeidaConservaSuFechaDeLectura() {
        LocalDateTime primeraLectura = LocalDateTime.of(2026, 10, 6, 11, 0);
        when(gateway.getPorUuid(NOTIFICACION)).thenReturn(notificacion(NOTIFICACION, DOCENTE, primeraLectura));

        DetalleNotificacionDocente detalle = casoDeUso.marcarLeida(NOTIFICACION, TOKEN_DOCENTE);

        assertEquals(primeraLectura, detalle.getNotificacion().getFechaLectura());
        verify(gateway, never()).marcarLeida(any(), any());
    }

    @Test
    void otroDocenteNoPuedeMarcarLaNotificacionAjena() {
        when(gateway.getPorUuid(NOTIFICACION)).thenReturn(notificacion(NOTIFICACION, DOCENTE, null));

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.marcarLeida(NOTIFICACION, TOKEN_OTRO_DOCENTE));

        verificarQueNoSeEscribioNada();
    }

    @Test
    void elDocenteDescargaUnAnexoDeLaSolicitudDeSuNotificacion() {
        when(gateway.getPorUuid(NOTIFICACION)).thenReturn(notificacion(NOTIFICACION, DOCENTE, null));
        when(anexoGateway.getPorUuid("an-1")).thenReturn(anexo("an-1", SOLICITUD));
        when(almacenamiento.leer("anexos/sol-1/an-1.pdf")).thenReturn(new byte[]{1, 2});

        ArchivoAdjunto archivo = casoDeUso.descargarAnexo(NOTIFICACION, "an-1", TOKEN_DOCENTE);

        assertEquals("formato.pdf", archivo.getNombreOriginal());
        assertEquals("application/pdf", archivo.getTipoContenido());
        assertArrayEquals(new byte[]{1, 2}, archivo.getContenido());
    }

    @Test
    void unAnexoDeOtraSolicitudNoSeDescargaPorLaNotificacion() {
        when(gateway.getPorUuid(NOTIFICACION)).thenReturn(notificacion(NOTIFICACION, DOCENTE, null));
        when(anexoGateway.getPorUuid("an-9")).thenReturn(anexo("an-9", "sol-9"));

        ErrorEntidadNoExisteExcepcion error = assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.descargarAnexo(NOTIFICACION, "an-9", TOKEN_DOCENTE));

        assertEquals(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, "Anexo académico", "an-9"), error.getMessage());
        verify(almacenamiento, never()).leer(any());
    }

    @Test
    void otroDocenteNoDescargaAnexosDeLaNotificacionAjena() {
        when(gateway.getPorUuid(NOTIFICACION)).thenReturn(notificacion(NOTIFICACION, DOCENTE, null));

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.descargarAnexo(NOTIFICACION, "an-1", TOKEN_OTRO_DOCENTE));

        verify(anexoGateway, never()).getPorUuid(any());
        verificarQueNoSeEscribioNada();
    }

    @Test
    void unArchivoQueYaNoEstaEnDiscoRespondeComoInexistente() {
        when(gateway.getPorUuid(NOTIFICACION)).thenReturn(notificacion(NOTIFICACION, DOCENTE, null));
        when(anexoGateway.getPorUuid("an-1")).thenReturn(anexo("an-1", SOLICITUD));
        when(almacenamiento.leer(any())).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.descargarAnexo(NOTIFICACION, "an-1", TOKEN_DOCENTE));
    }
}
