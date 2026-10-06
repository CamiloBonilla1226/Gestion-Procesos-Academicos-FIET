package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AnexoAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.SolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorMalFormatoExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorReglaNegocioVioladaExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelacionMatriculaCUImplAdaptadorTest {

    private static final String ESTUDIANTE = "est-1";
    private static final String TIPO_CM = "tipo-cm";
    private static final String TOKEN = "token-jwt";
    private static final String SOLICITUD = "sol-1";
    private static final List<String> OBLIGATORIOS = List.of(
            "Paz y salvo - División de Bibliotecas",
            "Paz y salvo - División de Deportes y Recreación",
            "Paz y salvo - División de Salud Integral",
            "Cupón de Confirmación de la Intervención Psicosocial - División de Salud Integral",
            "Paz y salvo - División Financiera",
            "Carné estudiantil o constancia de no trámite - DARCA");

    @Mock
    private SolicitudCancelacionMatriculaGatewayIntPuerto gateway;

    @Mock
    private EstudianteGatewayIntPuerto estudianteGateway;

    @Mock
    private UsuarioGatewayIntPuerto usuarioGateway;

    @Mock
    private TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway;

    @Mock
    private TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway;

    @Mock
    private SolicitudAcademicaCUIntPuerto solicitudCU;

    @Mock
    private AnexoAcademicoCUIntPuerto anexoCU;

    @Mock
    private LogCUIntPuerto log;

    private CancelacionMatriculaCUImplAdaptador casoDeUso;
    private final List<TipoAnexoAcademico> catalogo = new ArrayList<>();

    private final TipoSolicitudAcademica matricula = TipoSolicitudAcademica.builder()
            .uuidTipoSolicitudAcademica(TIPO_CM).nombre("Cancelación de Matrícula").build();
    private final TipoSolicitudAcademica asignatura = TipoSolicitudAcademica.builder()
            .uuidTipoSolicitudAcademica("tipo-ca").nombre("Cancelación de Asignatura").build();

    private Usuario usuario(String uuid, String rol) {
        return Usuario.builder()
                .uuidUsuario(uuid)
                .roles(new ArrayList<>(List.of(Rol.builder().uuidRol("rol-" + rol).nombre(rol).estado(true).build())))
                .build();
    }

    private AsignaturaMatriculada materia(String uuid, String estado) {
        return AsignaturaMatriculada.builder().uuidAsignaturaMatriculada(uuid).grupo("A").estado(estado).build();
    }

    private byte[] pdf() {
        byte[] firma = "%PDF-1.7\n".getBytes(StandardCharsets.US_ASCII);
        byte[] contenido = Arrays.copyOf(firma, 200);
        Arrays.fill(contenido, firma.length, contenido.length, (byte) 'x');
        return contenido;
    }

    private AnexoRadicacion anexo(String uuidTipo, String nombre, byte[] contenido) {
        return AnexoRadicacion.builder()
                .uuidTipoAnexoAcademico(uuidTipo)
                .archivo(ArchivoAdjunto.builder().nombreOriginal(nombre).tipoContenido("application/pdf").contenido(contenido).build())
                .build();
    }

    private List<AnexoRadicacion> anexosCompletos() {
        List<AnexoRadicacion> anexos = new ArrayList<>();
        for (TipoAnexoAcademico tipoAnexo : catalogo)
            anexos.add(anexo(tipoAnexo.getUuidTipoAnexoAcademico(), tipoAnexo.getUuidTipoAnexoAcademico() + ".pdf", pdf()));
        return anexos;
    }

    @BeforeEach
    void setUp() {
        casoDeUso = new CancelacionMatriculaCUImplAdaptador(gateway, estudianteGateway, usuarioGateway, tipoSolicitudGateway,
                tipoAnexoGateway, solicitudCU, anexoCU, new ExcepcionesFormateadorImplAdaptador(), log);
        for (int i = 0; i < OBLIGATORIOS.size(); i++)
            catalogo.add(TipoAnexoAcademico.builder()
                    .uuidTipoAnexoAcademico("anexo-" + i)
                    .tipoSolicitudAcademica(matricula)
                    .nombre(OBLIGATORIOS.get(i))
                    .formatosPermitidos(i == 5 ? "pdf,jpg,jpeg,png" : "pdf")
                    .obligatorio(true)
                    .build());
        lenient().when(usuarioGateway.getUsuario(ESTUDIANTE)).thenReturn(usuario(ESTUDIANTE, "Estudiante"));
        lenient().when(estudianteGateway.getPorUuid(ESTUDIANTE)).thenReturn(Estudiante.builder().uuidUsuario(ESTUDIANTE).build());
        lenient().when(estudianteGateway.getAsignaturasMatriculadas(ESTUDIANTE))
                .thenReturn(List.of(materia("am-1", "activa"), materia("am-2", "cancelada"), materia("am-3", "activa")));
        lenient().when(tipoSolicitudGateway.getTodos()).thenReturn(List.of(asignatura, matricula));
        lenient().when(tipoAnexoGateway.getPorTipo(TIPO_CM)).thenAnswer(invocacion -> catalogo);
        lenient().when(solicitudCU.crearSolicitud(ESTUDIANTE, TIPO_CM, TOKEN)).thenReturn(SolicitudAcademica.builder()
                .uuidSolicitudAcademica(SOLICITUD).radicado("2026-CM-0001").tipoSolicitudAcademica(matricula).build());
        lenient().when(gateway.guardar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private void verificarQueNoSeGuardoNada() {
        verify(solicitudCU, never()).crearSolicitud(any(), any(), any());
        verify(gateway, never()).guardar(any());
        verify(anexoCU, never()).adjuntarAnexo(any(), any(), any(), any(), any());
        verify(log, never()).crearLog(any(), any(), any());
    }

    @Test
    void radicacionCompletaCreaLaSolicitudGuardaLaCancelacionLosAnexosYElLog() {
        List<AnexoRadicacion> anexos = anexosCompletos();
        anexos.add(anexo(null, "soporte.png", new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1}));

        SolicitudCancelacionMatricula radicada = casoDeUso.radicarCancelacionMatricula(ESTUDIANTE, "  Calamidad domestica  ", anexos, TOKEN);

        InOrder orden = inOrder(solicitudCU, gateway, anexoCU, log);
        orden.verify(solicitudCU).crearSolicitud(ESTUDIANTE, TIPO_CM, TOKEN);
        ArgumentCaptor<SolicitudCancelacionMatricula> guardada = ArgumentCaptor.forClass(SolicitudCancelacionMatricula.class);
        orden.verify(gateway).guardar(guardada.capture());
        orden.verify(anexoCU, times(7)).adjuntarAnexo(eq(SOLICITUD), any(), any(), any(), eq(TOKEN));
        orden.verify(log).crearLog(eq("Radicar cancelación de matrícula"), contains("2026-CM-0001"), eq(TOKEN));

        assertEquals("Calamidad domestica", guardada.getValue().getMotivoCancelacion());
        assertEquals(SOLICITUD, guardada.getValue().getSolicitudAcademica().getUuidSolicitudAcademica());
        assertEquals(List.of("am-1", "am-3"), guardada.getValue().getAsignaturas().stream()
                .map(a -> a.getAsignaturaMatriculada().getUuidAsignaturaMatriculada()).toList());
        guardada.getValue().getAsignaturas().forEach(a -> {
            assertNotNull(a.getUuidAsignaturaSolicitud());
            assertNull(a.getNumeroFaltas());
            assertNull(a.getNota());
            assertNull(a.getSituacionMatricula());
            assertNull(a.getSituacionCancelar());
        });
        assertEquals("2026-CM-0001", radicada.getSolicitudAcademica().getRadicado());

        ArgumentCaptor<ActorSolicitud> actor = ArgumentCaptor.forClass(ActorSolicitud.class);
        ArgumentCaptor<String> tipos = ArgumentCaptor.forClass(String.class);
        verify(anexoCU, times(7)).adjuntarAnexo(eq(SOLICITUD), tipos.capture(), any(), actor.capture(), eq(TOKEN));
        assertTrue(actor.getAllValues().stream().allMatch(a -> ESTUDIANTE.equals(a.getUuidUsuario()) && a.getRol() == RolEtiquetaEtapa.ESTUDIANTE));
        assertEquals(6, tipos.getAllValues().stream().filter(t -> t != null).distinct().count());
        assertTrue(tipos.getAllValues().contains(null));
    }

    @Test
    void elMotivoEsObligatorio() {
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.radicarCancelacionMatricula(ESTUDIANTE, null, anexosCompletos(), TOKEN));
        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.radicarCancelacionMatricula(ESTUDIANTE, "   ", anexosCompletos(), TOKEN));
        assertTrue(error.getMessage().contains("motivo"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void elMotivoNoSuperaElLargoDeLaColumna() {
        assertThrows(ErrorMalFormatoExcepcion.class,
                () -> casoDeUso.radicarCancelacionMatricula(ESTUDIANTE, "x".repeat(256), anexosCompletos(), TOKEN));
        verificarQueNoSeGuardoNada();

        assertNotNull(casoDeUso.radicarCancelacionMatricula(ESTUDIANTE, "x".repeat(255), anexosCompletos(), TOKEN));
    }

    @Test
    void sinAsignaturasActivasNoSeRadica() {
        when(estudianteGateway.getAsignaturasMatriculadas(ESTUDIANTE)).thenReturn(List.of(materia("am-2", "cancelada")));

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.radicarCancelacionMatricula(ESTUDIANTE, "Motivo", anexosCompletos(), TOKEN));

        assertTrue(error.getMessage().contains("asignaturas activas"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void sinNingunaAsignaturaMatriculadaNoSeRadica() {
        when(estudianteGateway.getAsignaturasMatriculadas(ESTUDIANTE)).thenReturn(List.of());

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.radicarCancelacionMatricula(ESTUDIANTE, "Motivo", anexosCompletos(), TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @ParameterizedTest(name = "falta el anexo obligatorio {0}")
    @ValueSource(ints = {0, 1, 2, 3, 4, 5})
    void faltaUnAnexoObligatorioYSeNombra(int faltante) {
        List<AnexoRadicacion> anexos = anexosCompletos();
        anexos.remove(faltante);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.radicarCancelacionMatricula(ESTUDIANTE, "Motivo", anexos, TOKEN));

        assertTrue(error.getMessage().contains(OBLIGATORIOS.get(faltante)), error::getMessage);
        OBLIGATORIOS.stream().filter(nombre -> !nombre.equals(OBLIGATORIOS.get(faltante)))
                .forEach(nombre -> assertFalse(error.getMessage().contains(nombre), nombre));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void sinAnexosSeNombranLosSeisFaltantes() {
        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.radicarCancelacionMatricula(ESTUDIANTE, "Motivo", null, TOKEN));

        OBLIGATORIOS.forEach(nombre -> assertTrue(error.getMessage().contains(nombre), nombre));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unSoporteLibreNoReemplazaUnObligatorio() {
        List<AnexoRadicacion> anexos = anexosCompletos();
        anexos.remove(0);
        anexos.add(anexo(null, "soporte.pdf", pdf()));

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.radicarCancelacionMatricula(ESTUDIANTE, "Motivo", anexos, TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unArchivoQueNoEsPdfSeRechazaAntesDeGuardar() {
        List<AnexoRadicacion> anexos = anexosCompletos();
        anexos.set(0, anexo("anexo-0", "paz.pdf", "MZ ejecutable".getBytes(StandardCharsets.US_ASCII)));

        ErrorMalFormatoExcepcion error = assertThrows(ErrorMalFormatoExcepcion.class,
                () -> casoDeUso.radicarCancelacionMatricula(ESTUDIANTE, "Motivo", anexos, TOKEN));

        assertTrue(error.getMessage().contains("no corresponde"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unFormatoNoPermitidoUnArchivoGrandeOVacioSeRechazan() {
        List<AnexoRadicacion> conPng = anexosCompletos();
        conPng.set(0, anexo("anexo-0", "paz.png", new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1}));
        List<AnexoRadicacion> grande = anexosCompletos();
        grande.set(1, anexo("anexo-1", "paz.pdf", Arrays.copyOf(pdf(), 5 * 1024 * 1024 + 1)));
        List<AnexoRadicacion> vacio = anexosCompletos();
        vacio.set(2, anexo("anexo-2", "paz.pdf", new byte[0]));
        List<AnexoRadicacion> soporteMalo = anexosCompletos();
        soporteMalo.add(anexo(null, "soporte.exe", pdf()));

        for (List<AnexoRadicacion> anexos : List.of(conPng, grande, vacio, soporteMalo))
            assertThrows(ErrorMalFormatoExcepcion.class,
                    () -> casoDeUso.radicarCancelacionMatricula(ESTUDIANTE, "Motivo", anexos, TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unAnexoDeOtroTipoDeSolicitudSeRechaza() {
        List<AnexoRadicacion> anexos = anexosCompletos();
        anexos.add(anexo("anexo-es", "recibo.pdf", pdf()));
        when(tipoAnexoGateway.getPorUuid("anexo-es")).thenReturn(TipoAnexoAcademico.builder()
                .uuidTipoAnexoAcademico("anexo-es").nombre("Recibo de pago").build());

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.radicarCancelacionMatricula(ESTUDIANTE, "Motivo", anexos, TOKEN));

        assertTrue(error.getMessage().contains("Recibo de pago"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unTipoDeAnexoInexistenteSeRechaza() {
        List<AnexoRadicacion> anexos = anexosCompletos();
        anexos.add(anexo("no-existe", "x.pdf", pdf()));

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.radicarCancelacionMatricula(ESTUDIANTE, "Motivo", anexos, TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unaSolicitudEnCursoDetieneLaRadicacionSinGuardarMas() {
        when(solicitudCU.crearSolicitud(ESTUDIANTE, TIPO_CM, TOKEN))
                .thenThrow(new ErrorReglaNegocioVioladaExcepcion("Ya tienes una solicitud de Cancelación de Matrícula en curso (2026-CM-0003)..."));

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.radicarCancelacionMatricula(ESTUDIANTE, "Motivo", anexosCompletos(), TOKEN));

        assertTrue(error.getMessage().contains("en curso"));
        verify(gateway, never()).guardar(any());
        verify(anexoCU, never()).adjuntarAnexo(any(), any(), any(), any(), any());
        verify(log, never()).crearLog(any(), any(), any());
    }

    @Test
    void unUsuarioQueNoEsEstudianteNoRadica() {
        when(usuarioGateway.getUsuario("dec-1")).thenReturn(usuario("dec-1", "Decano"));

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.radicarCancelacionMatricula("dec-1", "Motivo", anexosCompletos(), TOKEN));

        assertTrue(error.getMessage().contains("Estudiante"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unUsuarioConRolEstudianteSinFichaNoRadica() {
        when(usuarioGateway.getUsuario("est-sin-ficha")).thenReturn(usuario("est-sin-ficha", "Estudiante"));
        when(estudianteGateway.getPorUuid("est-sin-ficha")).thenReturn(null);

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.radicarCancelacionMatricula("est-sin-ficha", "Motivo", anexosCompletos(), TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unUsuarioInexistenteNoRadica() {
        when(usuarioGateway.getUsuario("no-existe")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.radicarCancelacionMatricula("no-existe", "Motivo", anexosCompletos(), TOKEN));
        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.radicarCancelacionMatricula(" ", "Motivo", anexosCompletos(), TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void sinTipoCancelacionDeMatriculaConfiguradoNoSeRadica() {
        when(tipoSolicitudGateway.getTodos()).thenReturn(List.of(asignatura));

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.radicarCancelacionMatricula(ESTUDIANTE, "Motivo", anexosCompletos(), TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unFalloAMitadDeLosAnexosSePropagaSinLog() {
        when(anexoCU.adjuntarAnexo(eq(SOLICITUD), any(), any(), any(), eq(TOKEN)))
                .thenReturn(AnexoAcademico.builder().build())
                .thenReturn(AnexoAcademico.builder().build())
                .thenThrow(new IllegalStateException("base caida"));

        assertThrows(IllegalStateException.class,
                () -> casoDeUso.radicarCancelacionMatricula(ESTUDIANTE, "Motivo", anexosCompletos(), TOKEN));

        verify(anexoCU, times(3)).adjuntarAnexo(any(), any(), any(), any(), any());
        verify(log, never()).crearLog(any(), any(), any());
    }

    @Test
    void unFalloAlGuardarLaCancelacionNoSubeAnexos() {
        when(gateway.guardar(any())).thenThrow(new IllegalStateException("base caida"));

        assertThrows(IllegalStateException.class,
                () -> casoDeUso.radicarCancelacionMatricula(ESTUDIANTE, "Motivo", anexosCompletos(), TOKEN));

        verify(anexoCU, never()).adjuntarAnexo(any(), any(), any(), any(), any());
        verify(log, never()).crearLog(any(), any(), any());
    }
}
