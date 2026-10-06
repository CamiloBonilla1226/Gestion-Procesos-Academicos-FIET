package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorGenericoExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorMalFormatoExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorReglaNegocioVioladaExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.UncheckedIOException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResolucionAcademicaCUImplAdaptadorTest {

    private static final String ESTUDIANTE = "est-1";
    private static final String OTRO_ESTUDIANTE = "est-2";
    private static final String FUNCIONARIO = "fa-1";
    private static final String OTRO_FUNCIONARIO = "fa-2";
    private static final String DECANO = "dec-1";
    private static final String SOLICITUD = "sol-1";
    private static final String TOKEN = "token-jwt";
    private static final String RUTA ="/api/anexos/sol-1/nuevo_20260310100000.pdf";
    private static final String RUTA_ANTERIOR = "/api/anexos/sol-1/viejo_20260301100000.pdf";
    private static final int CINCO_MB = 5 * 1024 * 1024;

    @Mock
    private ResolucionAcademicaGatewayIntPuerto gateway;

    @Mock
    private SolicitudAcademicaGatewayIntPuerto solicitudGateway;

    @Mock
    private UsuarioGatewayIntPuerto usuarioGateway;

    @Mock
    private AlmacenamientoAnexosIntPuerto almacenamiento;

    @Mock
    private LogCUIntPuerto log;

    private ResolucionAcademicaCUImplAdaptador casoDeUso;

    private TipoSolicitudAcademica tipo(String uuid, String nombre) {
        return TipoSolicitudAcademica.builder()
                .uuidTipoSolicitudAcademica(uuid)
                .nombre(nombre)
                .funcionarioAcademico(FuncionarioAcademico.builder().uuidUsuario(FUNCIONARIO).build())
                .build();
    }

    private final TipoSolicitudAcademica matricula = tipo("tipo-cm", "Cancelación de Matrícula");
    private final TipoSolicitudAcademica asignatura = tipo("tipo-ca", "Cancelación de Asignatura");
    private final TipoSolicitudAcademica supletorio = tipo("tipo-es", "Examen Supletorio");

    private Usuario usuario(String uuid, String rol) {
        return Usuario.builder()
                .uuidUsuario(uuid)
                .roles(new ArrayList<>(List.of(Rol.builder().uuidRol("rol-" + uuid).nombre(rol).estado(true).build())))
                .build();
    }

    private void solicitud(TipoSolicitudAcademica tipo, String etapa) {
        SolicitudAcademica solicitud = SolicitudAcademica.builder()
                .uuidSolicitudAcademica(SOLICITUD)
                .radicado("2026-XX-0001")
                .estudiante(Estudiante.builder().uuidUsuario(ESTUDIANTE).build())
                .tipoSolicitudAcademica(tipo)
                .etapa(EtapaSolicitudAcademica.builder().uuidEtapa("etapa-" + etapa).codigo(etapa).build())
                .build();
        lenient().when(solicitudGateway.getPorUuid(SOLICITUD)).thenReturn(solicitud);
    }

    private ActorSolicitud actor(String uuid, RolEtiquetaEtapa rol) {
        return ActorSolicitud.builder().uuidUsuario(uuid).rol(rol).build();
    }

    private ActorSolicitud funcionario() {
        return actor(FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO);
    }

    private byte[] pdf(int tamanio) {
        byte[] firma = "%PDF-1.7\n".getBytes(StandardCharsets.US_ASCII);
        byte[] contenido = Arrays.copyOf(firma, Math.max(tamanio, firma.length));
        Arrays.fill(contenido, firma.length, contenido.length, (byte) 'x');
        return contenido;
    }

    private ArchivoAdjunto archivo(String nombre, byte[] contenido) {
        return ArchivoAdjunto.builder().nombreOriginal(nombre).tipoContenido("cualquiera").contenido(contenido).build();
    }

    private ResolucionAcademica anterior() {
        return ResolucionAcademica.builder()
                .solicitudAcademica(SolicitudAcademica.builder().uuidSolicitudAcademica(SOLICITUD).build())
                .urlArchivo(RUTA_ANTERIOR)
                .nombreArchivo("resolucion vieja.pdf")
                .funcionarioAcademico(FuncionarioAcademico.builder().uuidUsuario(FUNCIONARIO).build())
                .build();
    }

    @BeforeEach
    void setUp() {
        casoDeUso = new ResolucionAcademicaCUImplAdaptador(gateway, solicitudGateway, usuarioGateway, almacenamiento,
                new ExcepcionesFormateadorImplAdaptador(), log, Clock.fixed(Instant.parse("2026-03-10T15:00:00Z"), ZoneId.of("America/Bogota")));
        lenient().when(usuarioGateway.getUsuario(ESTUDIANTE)).thenReturn(usuario(ESTUDIANTE, "Estudiante"));
        lenient().when(usuarioGateway.getUsuario(OTRO_ESTUDIANTE)).thenReturn(usuario(OTRO_ESTUDIANTE, "Estudiante"));
        lenient().when(usuarioGateway.getUsuario(FUNCIONARIO)).thenReturn(usuario(FUNCIONARIO, "Funcionario Académico"));
        lenient().when(usuarioGateway.getUsuario(OTRO_FUNCIONARIO)).thenReturn(usuario(OTRO_FUNCIONARIO, "Funcionario Académico"));
        lenient().when(usuarioGateway.getUsuario(DECANO)).thenReturn(usuario(DECANO, "Decano"));
        lenient().when(almacenamiento.guardar(anyString(), anyString(), any())).thenReturn(RUTA);
        lenient().when(gateway.guardar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private void verificarQueNoSeGuardoNada() {
        verify(almacenamiento, never()).guardar(anyString(), anyString(), any());
        verify(gateway, never()).guardar(any());
        verify(almacenamiento, never()).eliminarTrasConfirmar(any());
        verify(log, never()).crearLog(any(), any(), any());
    }

    @Test
    void adjuntarYReemplazarLaResolucionEscribenElLogConElRadicadoYLaAccion() {
        solicitud(matricula, RADICADA);
        casoDeUso.adjuntarResolucion(SOLICITUD, archivo("resolucion.pdf", pdf(100)), funcionario(), TOKEN);
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(anterior());
        casoDeUso.adjuntarResolucion(SOLICITUD, archivo("resolucion.pdf", pdf(100)), funcionario(), TOKEN);

        ArgumentCaptor<String> resultado = ArgumentCaptor.forClass(String.class);
        verify(log, times(2)).crearLog(eq("Adjuntar Resolución académica"), resultado.capture(), eq(TOKEN));
        assertTrue(resultado.getAllValues().get(0).contains("2026-XX-0001"));
        assertTrue(resultado.getAllValues().get(0).contains("ADJUNTAR_RESOLUCION"));
        assertTrue(resultado.getAllValues().get(1).contains("REEMPLAZAR_RESOLUCION"));
    }

    @Test
    void siFallaLaBaseNoSeEscribeElLog() {
        solicitud(matricula, RADICADA);
        when(gateway.guardar(any())).thenThrow(new IllegalStateException("base caida"));

        assertThrows(IllegalStateException.class,
                () -> casoDeUso.adjuntarResolucion(SOLICITUD, archivo("resolucion.pdf", pdf(100)), funcionario(), TOKEN));
        verify(log, never()).crearLog(any(), any(), any());
    }

    @Test
    void elFuncionarioAsignadoSubeLaResolucionAlRechazarYQuedaLaFicha() {
        solicitud(matricula, RADICADA);
        byte[] contenido = pdf(2048);

        ResolucionAcademica resolucion = casoDeUso.adjuntarResolucion(SOLICITUD, archivo("C:\\escaneos\\Resolucion 015.pdf", contenido), funcionario(), TOKEN);

        ArgumentCaptor<String> nombreEnDisco = ArgumentCaptor.forClass(String.class);
        verify(almacenamiento).guardar(eq(SOLICITUD), nombreEnDisco.capture(), same(contenido));
        assertTrue(nombreEnDisco.getValue().matches("[0-9a-f\\-]{36}\\.pdf"));
        assertEquals(RUTA, resolucion.getUrlArchivo());
        assertEquals("Resolucion 015.pdf", resolucion.getNombreArchivo());
        assertEquals(SOLICITUD, resolucion.getSolicitudAcademica().getUuidSolicitudAcademica());
        assertEquals(FUNCIONARIO, resolucion.getFuncionarioAcademico().getUuidUsuario());
        assertEquals(LocalDateTime.of(2026, 3, 10, 10, 0), resolucion.getFechaSubida());
        verify(almacenamiento, never()).eliminarTrasConfirmar(any());
    }

    @Test
    void tambienSeSubeEnCancelacionDeAsignaturaTrasLaDecisionDelDecano() {
        for (String etapa : List.of(APROBADA_POR_DECANO, RECHAZADA_POR_DECANO)) {
            solicitud(asignatura, etapa);
            assertNotNull(casoDeUso.adjuntarResolucion(SOLICITUD, archivo("resolucion.pdf", pdf(100)), funcionario(), TOKEN), etapa);
        }
        verify(gateway, times(2)).guardar(any());
    }

    @Test
    void elSupletorioNoProduceResolucion() {
        solicitud(supletorio, RADICADA);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.adjuntarResolucion(SOLICITUD, archivo("resolucion.pdf", pdf(100)), funcionario(), TOKEN));

        assertTrue(error.getMessage().contains("no produce Resolución"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unTipoQueNoEsProcesoAcademicoSeRechaza() {
        solicitud(tipo("tipo-otro", "Homologación"), RADICADA);

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.adjuntarResolucion(SOLICITUD, archivo("resolucion.pdf", pdf(100)), funcionario(), TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void soloSeAceptaPdf() {
        solicitud(matricula, RADICADA);
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1, 2};

        assertThrows(ErrorMalFormatoExcepcion.class,
                () -> casoDeUso.adjuntarResolucion(SOLICITUD, archivo("resolucion.png", png), funcionario(), TOKEN));
        assertThrows(ErrorMalFormatoExcepcion.class,
                () -> casoDeUso.adjuntarResolucion(SOLICITUD, archivo("resolucion", pdf(100)), funcionario(), TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unArchivoQueNoEsPdfAunqueSeLlamePdfSeRechaza() {
        solicitud(matricula, RADICADA);

        ErrorMalFormatoExcepcion error = assertThrows(ErrorMalFormatoExcepcion.class,
                () -> casoDeUso.adjuntarResolucion(SOLICITUD, archivo("resolucion.pdf", "MZ ejecutable".getBytes(StandardCharsets.US_ASCII)), funcionario(), TOKEN));

        assertTrue(error.getMessage().contains("no corresponde"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void elTamanioMaximoEsCincoMegas() {
        solicitud(matricula, RADICADA);

        assertNotNull(casoDeUso.adjuntarResolucion(SOLICITUD, archivo("justo.pdf", pdf(CINCO_MB)), funcionario(), TOKEN));
        assertThrows(ErrorMalFormatoExcepcion.class,
                () -> casoDeUso.adjuntarResolucion(SOLICITUD, archivo("grande.pdf", pdf(CINCO_MB + 1)), funcionario(), TOKEN));
        assertThrows(ErrorMalFormatoExcepcion.class,
                () -> casoDeUso.adjuntarResolucion(SOLICITUD, archivo("vacio.pdf", new byte[0]), funcionario(), TOKEN));
        verify(gateway, times(1)).guardar(any());
    }

    @Test
    void soloLaSubeElFuncionarioAsignado() {
        solicitud(matricula, RADICADA);
        ArchivoAdjunto archivo = archivo("resolucion.pdf", pdf(100));

        ErrorReglaNegocioVioladaExcepcion delEstudiante = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.adjuntarResolucion(SOLICITUD, archivo, actor(ESTUDIANTE, RolEtiquetaEtapa.ESTUDIANTE), TOKEN));
        ErrorReglaNegocioVioladaExcepcion delDecano = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.adjuntarResolucion(SOLICITUD, archivo, actor(DECANO, RolEtiquetaEtapa.DECANO), TOKEN));
        ErrorReglaNegocioVioladaExcepcion deOtroFuncionario = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.adjuntarResolucion(SOLICITUD, archivo, actor(OTRO_FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO), TOKEN));
        ErrorReglaNegocioVioladaExcepcion rolFalso = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.adjuntarResolucion(SOLICITUD, archivo, actor(ESTUDIANTE, RolEtiquetaEtapa.FUNCIONARIO), TOKEN));

        assertTrue(delEstudiante.getMessage().contains("funcionario académico asignado"));
        assertTrue(delDecano.getMessage().contains("funcionario académico asignado"));
        assertTrue(deOtroFuncionario.getMessage().contains("no está asignado"));
        assertTrue(rolFalso.getMessage().contains("no tiene el rol"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void noSeSubeEnEtapasDondeNoSePideElEscaneo() {
        for (String etapa : List.of(EN_REVISION_DECANO, PENDIENTE_PAGO, EN_VERIFICACION_PAGO)) {
            solicitud(matricula, etapa);
            ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                    () -> casoDeUso.adjuntarResolucion(SOLICITUD, archivo("resolucion.pdf", pdf(100)), funcionario(), TOKEN), etapa);
            assertTrue(error.getMessage().contains("solo se sube en las etapas"), etapa);
        }
        verificarQueNoSeGuardoNada();
    }

    @Test
    void enEtapaFinalNoSeSubeNiSeReemplaza() {
        lenient().when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(anterior());
        for (String etapa : List.of(APROBADA, RECHAZADA)) {
            solicitud(matricula, etapa);
            ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                    () -> casoDeUso.adjuntarResolucion(SOLICITUD, archivo("resolucion.pdf", pdf(100)), funcionario(), TOKEN), etapa);
            assertTrue(error.getMessage().contains("etapa final"), etapa);
        }
        verificarQueNoSeGuardoNada();
        verify(almacenamiento, never()).eliminar(any());
    }

    @Test
    void subirOtraVezReemplazaLaAnteriorYBorraElArchivoViejoTrasConfirmar() {
        solicitud(matricula, APROBADA_POR_DECANO);
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(anterior());

        ResolucionAcademica resolucion = casoDeUso.adjuntarResolucion(SOLICITUD, archivo("firmada.pdf", pdf(100)), funcionario(), TOKEN);

        assertEquals(RUTA, resolucion.getUrlArchivo());
        assertEquals("firmada.pdf", resolucion.getNombreArchivo());
        verify(gateway).guardar(any());
        verify(almacenamiento).eliminarTrasConfirmar(RUTA_ANTERIOR);
        verify(almacenamiento, never()).eliminar(any());
    }

    @Test
    void siFallaLaBaseSeBorraElArchivoNuevoYSeConservaElAnterior() {
        solicitud(matricula, RADICADA);
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(anterior());
        when(gateway.guardar(any())).thenThrow(new IllegalStateException("base caida"));

        assertThrows(IllegalStateException.class,
                () -> casoDeUso.adjuntarResolucion(SOLICITUD, archivo("resolucion.pdf", pdf(100)), funcionario(), TOKEN));

        verify(almacenamiento).eliminar(RUTA);
        verify(almacenamiento, never()).eliminar(RUTA_ANTERIOR);
        verify(almacenamiento, never()).eliminarTrasConfirmar(any());
    }

    @Test
    void siFallaElDiscoNoQuedaFicha() {
        solicitud(matricula, RADICADA);
        when(almacenamiento.guardar(anyString(), anyString(), any())).thenThrow(new UncheckedIOException(new IOException("disco lleno")));

        assertThrows(ErrorGenericoExcepcion.class,
                () -> casoDeUso.adjuntarResolucion(SOLICITUD, archivo("resolucion.pdf", pdf(100)), funcionario(), TOKEN));

        verify(gateway, never()).guardar(any());
        verify(almacenamiento, never()).eliminarTrasConfirmar(any());
    }

    @Test
    void unaSolicitudInexistenteNoGuardaNada() {
        when(solicitudGateway.getPorUuid("no-existe")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.adjuntarResolucion("no-existe", archivo("resolucion.pdf", pdf(100)), funcionario(), TOKEN));
        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.obtenerResolucion("no-existe", funcionario()));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void elEstudianteDuenioLaDescargaSoloEnEtapaFinal() {
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(anterior());
        when(almacenamiento.leer(RUTA_ANTERIOR)).thenReturn(pdf(50));
        ActorSolicitud estudiante = actor(ESTUDIANTE, RolEtiquetaEtapa.ESTUDIANTE);

        for (String etapa : List.of(RADICADA, APROBADA_POR_DECANO, RECHAZADA_POR_DECANO)) {
            solicitud(matricula, etapa);
            ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                    () -> casoDeUso.obtenerResolucion(SOLICITUD, estudiante), etapa);
            assertTrue(error.getMessage().contains("disponible cuando la solicitud termine"), etapa);
        }
        for (String etapa : List.of(APROBADA, RECHAZADA)) {
            solicitud(matricula, etapa);
            ArchivoAdjunto descarga = casoDeUso.obtenerResolucion(SOLICITUD, estudiante);
            assertEquals("resolucion vieja.pdf", descarga.getNombreOriginal());
            assertEquals("application/pdf", descarga.getTipoContenido());
            assertEquals(50, descarga.getContenido().length);
        }
    }

    @Test
    void otroEstudianteNoLaDescargaNiEnEtapaFinal() {
        solicitud(matricula, APROBADA);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.obtenerResolucion(SOLICITUD, actor(OTRO_ESTUDIANTE, RolEtiquetaEtapa.ESTUDIANTE)));

        assertTrue(error.getMessage().contains("no pertenece"));
        verify(almacenamiento, never()).leer(any());
    }

    @Test
    void elFuncionarioAsignadoYElDecanoLaDescarganEnCualquierEtapa() {
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(anterior());
        when(almacenamiento.leer(RUTA_ANTERIOR)).thenReturn(pdf(50));

        for (String etapa : List.of(RADICADA, EN_REVISION_DECANO, APROBADA_POR_DECANO, APROBADA)) {
            solicitud(matricula, etapa);
            assertNotNull(casoDeUso.obtenerResolucion(SOLICITUD, funcionario()), etapa);
            assertNotNull(casoDeUso.obtenerResolucion(SOLICITUD, actor(DECANO, RolEtiquetaEtapa.DECANO)), etapa);
        }
        solicitud(matricula, RADICADA);
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.obtenerResolucion(SOLICITUD, actor(OTRO_FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO)));
    }

    @Test
    void sinResolucionOSinArchivoEnDiscoLaDescargaNoExiste() {
        solicitud(matricula, APROBADA);
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.obtenerResolucion(SOLICITUD, funcionario()));

        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(anterior());
        when(almacenamiento.leer(RUTA_ANTERIOR)).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.obtenerResolucion(SOLICITUD, funcionario()));
    }
}
