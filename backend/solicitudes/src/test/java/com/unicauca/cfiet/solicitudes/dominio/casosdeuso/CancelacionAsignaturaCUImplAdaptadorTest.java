package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AnexoAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.SolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.AnexoAcademicoConstantes;
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
class CancelacionAsignaturaCUImplAdaptadorTest {

    private static final String ESTUDIANTE = "est-1";
    private static final String TIPO_CA = "tipo-ca";
    private static final String TOKEN = "token-jwt";
    private static final String SOLICITUD = "sol-1";

    @Mock
    private SolicitudCancelacionAsignaturaGatewayIntPuerto gateway;

    @Mock
    private SolicitudCancelacionMatriculaGatewayIntPuerto asignaturaSolicitudGateway;

    @Mock
    private EstudianteGatewayIntPuerto estudianteGateway;

    @Mock
    private UsuarioGatewayIntPuerto usuarioGateway;

    @Mock
    private TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway;

    @Mock
    private SolicitudAcademicaCUIntPuerto solicitudCU;

    @Mock
    private AnexoAcademicoCUIntPuerto anexoCU;

    @Mock
    private LogCUIntPuerto log;

    private CancelacionAsignaturaCUImplAdaptador casoDeUso;

    private final TipoSolicitudAcademica asignatura = TipoSolicitudAcademica.builder()
            .uuidTipoSolicitudAcademica(TIPO_CA).nombre("Cancelación de Asignatura").build();
    private final TipoSolicitudAcademica matricula = TipoSolicitudAcademica.builder()
            .uuidTipoSolicitudAcademica("tipo-cm").nombre("Cancelación de Matrícula").build();

    private Usuario usuario(String uuid, String rol) {
        return Usuario.builder()
                .uuidUsuario(uuid)
                .roles(new ArrayList<>(List.of(Rol.builder().uuidRol("rol-" + rol).nombre(rol).estado(true).build())))
                .build();
    }

    private AsignaturaMatriculada materia(String uuid, String nombre, String estado) {
        return AsignaturaMatriculada.builder()
                .uuidAsignaturaMatriculada(uuid)
                .asignatura(Asignatura.builder().uuidAsignatura("a-" + uuid).codigoAsignatura("SIS-" + uuid).nombreAsignatura(nombre).build())
                .grupo("A")
                .estado(estado)
                .build();
    }

    private byte[] pdf() {
        byte[] firma = "%PDF-1.7\n".getBytes(StandardCharsets.US_ASCII);
        byte[] contenido = Arrays.copyOf(firma, 200);
        Arrays.fill(contenido, firma.length, contenido.length, (byte) 'x');
        return contenido;
    }

    private AnexoRadicacion soporte(String uuidTipo, String nombre, byte[] contenido) {
        return AnexoRadicacion.builder()
                .uuidTipoAnexoAcademico(uuidTipo)
                .archivo(ArchivoAdjunto.builder().nombreOriginal(nombre).tipoContenido("application/pdf").contenido(contenido).build())
                .build();
    }

    @BeforeEach
    void setUp() {
        casoDeUso = new CancelacionAsignaturaCUImplAdaptador(gateway, asignaturaSolicitudGateway, estudianteGateway, usuarioGateway,
                tipoSolicitudGateway, solicitudCU, anexoCU, new ExcepcionesFormateadorImplAdaptador(), log);
        lenient().when(usuarioGateway.getUsuario(ESTUDIANTE)).thenReturn(usuario(ESTUDIANTE, "Estudiante"));
        lenient().when(estudianteGateway.getPorUuid(ESTUDIANTE)).thenReturn(Estudiante.builder().uuidUsuario(ESTUDIANTE).build());
        lenient().when(estudianteGateway.getAsignaturasMatriculadas(ESTUDIANTE)).thenReturn(List.of(
                materia("am-1", "Cálculo I", "activa"),
                materia("am-2", "Física I", "activa"),
                materia("am-3", "Química I", "activa"),
                materia("am-4", "Álgebra", "cancelada"),
                materia("am-5", "Dibujo", "aprobada"),
                materia("am-6", "Ética", "perdida")));
        lenient().when(tipoSolicitudGateway.getTodos()).thenReturn(List.of(matricula, asignatura));
        lenient().when(solicitudCU.crearSolicitud(ESTUDIANTE, TIPO_CA, TOKEN)).thenReturn(SolicitudAcademica.builder()
                .uuidSolicitudAcademica(SOLICITUD).radicado("2026-CA-0001").tipoSolicitudAcademica(asignatura).build());
        lenient().when(gateway.guardar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
        lenient().when(asignaturaSolicitudGateway.guardarAsignaturas(any(), any())).thenAnswer(invocacion -> invocacion.getArgument(1));
    }

    private void verificarQueNoSeGuardoNada() {
        verify(solicitudCU, never()).crearSolicitud(any(), any(), any());
        verify(gateway, never()).guardar(any());
        verify(asignaturaSolicitudGateway, never()).guardarAsignaturas(any(), any());
        verify(anexoCU, never()).adjuntarAnexo(any(), any(), any(), any(), any());
        verify(log, never()).crearLog(any(), any(), any());
    }

    @SuppressWarnings("unchecked")
    private List<AsignaturaSolicitudAcademica> filasGuardadas() {
        ArgumentCaptor<List<AsignaturaSolicitudAcademica>> filas = ArgumentCaptor.forClass(List.class);
        verify(asignaturaSolicitudGateway).guardarAsignaturas(eq(SOLICITUD), filas.capture());
        return filas.getValue();
    }

    @Test
    void radicacionConUnaAsignaturaGuardaSolicitudEspecializacionFilaYLog() {
        SolicitudCancelacionAsignatura radicada = casoDeUso.radicarCancelacionAsignatura(ESTUDIANTE, "  Incapacidad medica  ",
                List.of("am-2"), null, TOKEN);

        InOrder orden = inOrder(solicitudCU, gateway, asignaturaSolicitudGateway, log);
        orden.verify(solicitudCU).crearSolicitud(ESTUDIANTE, TIPO_CA, TOKEN);
        ArgumentCaptor<SolicitudCancelacionAsignatura> guardada = ArgumentCaptor.forClass(SolicitudCancelacionAsignatura.class);
        orden.verify(gateway).guardar(guardada.capture());
        orden.verify(asignaturaSolicitudGateway).guardarAsignaturas(eq(SOLICITUD), any());
        orden.verify(log).crearLog(eq("Radicar cancelación de asignatura"), contains("2026-CA-0001"), eq(TOKEN));

        assertEquals("Incapacidad medica", guardada.getValue().getMotivoCancelacion());
        assertEquals(SOLICITUD, guardada.getValue().getSolicitudAcademica().getUuidSolicitudAcademica());
        List<AsignaturaSolicitudAcademica> filas = filasGuardadas();
        assertEquals(1, filas.size());
        assertEquals("am-2", filas.get(0).getAsignaturaMatriculada().getUuidAsignaturaMatriculada());
        assertNotNull(filas.get(0).getUuidAsignaturaSolicitud());
        assertNull(filas.get(0).getNumeroFaltas());
        assertNull(filas.get(0).getNota());
        assertNull(filas.get(0).getSituacionMatricula());
        assertNull(filas.get(0).getSituacionCancelar());
        assertEquals("2026-CA-0001", radicada.getSolicitudAcademica().getRadicado());
        assertEquals(1, radicada.getAsignaturas().size());
        verify(anexoCU, never()).adjuntarAnexo(any(), any(), any(), any(), any());
    }

    @Test
    void radicacionConVariasAsignaturasYSoportesGuardaUnaFilaPorAsignaturaYCadaSoporteSinTipo() {
        List<AnexoRadicacion> soportes = List.of(soporte(null, "incapacidad.pdf", pdf()),
                soporte(" ", "certificado.png", new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1}));

        casoDeUso.radicarCancelacionAsignatura(ESTUDIANTE, "Motivo", List.of("am-3", "am-1"), soportes, TOKEN);

        List<AsignaturaSolicitudAcademica> filas = filasGuardadas();
        assertEquals(List.of("am-3", "am-1"), filas.stream().map(f -> f.getAsignaturaMatriculada().getUuidAsignaturaMatriculada()).toList());
        assertNotEquals(filas.get(0).getUuidAsignaturaSolicitud(), filas.get(1).getUuidAsignaturaSolicitud());
        ArgumentCaptor<ActorSolicitud> actor = ArgumentCaptor.forClass(ActorSolicitud.class);
        verify(anexoCU, times(2)).adjuntarAnexo(eq(SOLICITUD), isNull(), any(), actor.capture(), eq(TOKEN));
        assertTrue(actor.getAllValues().stream().allMatch(a -> ESTUDIANTE.equals(a.getUuidUsuario()) && a.getRol() == RolEtiquetaEtapa.ESTUDIANTE));
        verify(log).crearLog(eq("Radicar cancelación de asignatura"), contains("2 asignaturas y 2 soportes"), eq(TOKEN));
    }

    @Test
    void elMotivoEsObligatorio() {
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.radicarCancelacionAsignatura(ESTUDIANTE, null, List.of("am-1"), null, TOKEN));
        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.radicarCancelacionAsignatura(ESTUDIANTE, "   ", List.of("am-1"), null, TOKEN));
        assertTrue(error.getMessage().contains("motivo"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void elMotivoNoSuperaElLargoDeLaColumna() {
        assertThrows(ErrorMalFormatoExcepcion.class,
                () -> casoDeUso.radicarCancelacionAsignatura(ESTUDIANTE, "x".repeat(256), List.of("am-1"), null, TOKEN));
        verificarQueNoSeGuardoNada();

        assertNotNull(casoDeUso.radicarCancelacionAsignatura(ESTUDIANTE, "x".repeat(255), List.of("am-1"), null, TOKEN));
    }

    @Test
    void sinAsignaturasNoSeRadica() {
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.radicarCancelacionAsignatura(ESTUDIANTE, "Motivo", null, null, TOKEN));
        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.radicarCancelacionAsignatura(ESTUDIANTE, "Motivo", List.of(), null, TOKEN));
        assertTrue(error.getMessage().contains("al menos una asignatura"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unaAsignaturaRepetidaSeRechazaNombrandola() {
        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.radicarCancelacionAsignatura(ESTUDIANTE, "Motivo", List.of("am-1", "am-2", "am-1"), null, TOKEN));

        assertTrue(error.getMessage().contains("Cálculo I"), error::getMessage);
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unaAsignaturaAjenaOInexistenteSeRechazaIgualNombrandola() {
        ErrorReglaNegocioVioladaExcepcion ajena = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.radicarCancelacionAsignatura(ESTUDIANTE, "Motivo", List.of("am-1", "am-9"), null, TOKEN));
        ErrorReglaNegocioVioladaExcepcion inexistente = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.radicarCancelacionAsignatura(ESTUDIANTE, "Motivo", List.of("am-x"), null, TOKEN));
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.radicarCancelacionAsignatura(ESTUDIANTE, "Motivo", Arrays.asList("am-1", null), null, TOKEN));

        assertTrue(ajena.getMessage().contains("am-9"));
        assertEquals(ajena.getMessage().replace("am-9", "X"), inexistente.getMessage().replace("am-x", "X"));
        verificarQueNoSeGuardoNada();
    }

    @ParameterizedTest
    @ValueSource(strings = {"am-4", "am-5", "am-6"})
    void unaAsignaturaQueNoEstaActivaSeRechazaNombrandola(String uuid) {
        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.radicarCancelacionAsignatura(ESTUDIANTE, "Motivo", List.of("am-1", uuid), null, TOKEN));

        assertTrue(error.getMessage().contains("SIS-" + uuid), error::getMessage);
        assertTrue(error.getMessage().contains("activas"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unSoporteConTipoSeRechaza() {
        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.radicarCancelacionAsignatura(ESTUDIANTE, "Motivo", List.of("am-1"),
                        List.of(soporte(null, "ok.pdf", pdf()), soporte("anexo-paz", "paz.pdf", pdf())), TOKEN));

        assertTrue(error.getMessage().contains("anexo-paz"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unArchivoInvalidoSeRechazaAntesDeGuardar() {
        ErrorMalFormatoExcepcion firma = assertThrows(ErrorMalFormatoExcepcion.class,
                () -> casoDeUso.radicarCancelacionAsignatura(ESTUDIANTE, "Motivo", List.of("am-1"),
                        List.of(soporte(null, "soporte.pdf", "MZ ejecutable".getBytes(StandardCharsets.US_ASCII))), TOKEN));
        assertTrue(firma.getMessage().contains("no corresponde"));
        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.radicarCancelacionAsignatura(ESTUDIANTE, "Motivo", List.of("am-1"),
                List.of(soporte(null, "programa.exe", pdf())), TOKEN));
        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.radicarCancelacionAsignatura(ESTUDIANTE, "Motivo", List.of("am-1"),
                List.of(soporte(null, "vacio.pdf", new byte[0])), TOKEN));
        byte[] grande = Arrays.copyOf(pdf(), (int) AnexoAcademicoConstantes.TAMANIO_MAXIMO_BYTES + 1);
        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.radicarCancelacionAsignatura(ESTUDIANTE, "Motivo", List.of("am-1"),
                List.of(soporte(null, "grande.pdf", grande)), TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unaSolicitudEnCursoNoDejaNada() {
        when(solicitudCU.crearSolicitud(ESTUDIANTE, TIPO_CA, TOKEN))
                .thenThrow(new ErrorReglaNegocioVioladaExcepcion("Ya tienes una solicitud de Cancelación de Asignatura en curso (2026-CA-0001)..."));

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.radicarCancelacionAsignatura(ESTUDIANTE, "Motivo", List.of("am-1"), List.of(soporte(null, "s.pdf", pdf())), TOKEN));

        verify(gateway, never()).guardar(any());
        verify(asignaturaSolicitudGateway, never()).guardarAsignaturas(any(), any());
        verify(anexoCU, never()).adjuntarAnexo(any(), any(), any(), any(), any());
        verify(log, never()).crearLog(any(), any(), any());
    }

    @Test
    void unUsuarioQueNoEsEstudianteSeRechaza() {
        when(usuarioGateway.getUsuario("dec-1")).thenReturn(usuario("dec-1", "Decano"));
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.radicarCancelacionAsignatura("dec-1", "Motivo", List.of("am-1"), null, TOKEN));

        when(estudianteGateway.getPorUuid(ESTUDIANTE)).thenReturn(null);
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.radicarCancelacionAsignatura(ESTUDIANTE, "Motivo", List.of("am-1"), null, TOKEN));

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.radicarCancelacionAsignatura("nadie", "Motivo", List.of("am-1"), null, TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void sinTipoDeCancelacionDeAsignaturaNoSeRadica() {
        when(tipoSolicitudGateway.getTodos()).thenReturn(List.of(matricula));

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.radicarCancelacionAsignatura(ESTUDIANTE, "Motivo", List.of("am-1"), null, TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unFalloAlGuardarLasAsignaturasSePropagaSinSubirSoportes() {
        when(asignaturaSolicitudGateway.guardarAsignaturas(any(), any())).thenThrow(new IllegalStateException("fallo simulado"));

        assertThrows(IllegalStateException.class, () -> casoDeUso.radicarCancelacionAsignatura(ESTUDIANTE, "Motivo",
                List.of("am-1"), List.of(soporte(null, "s.pdf", pdf())), TOKEN));

        verify(anexoCU, never()).adjuntarAnexo(any(), any(), any(), any(), any());
        verify(log, never()).crearLog(any(), any(), any());
    }

    @Test
    void unFalloEnElSegundoSoporteSePropagaSinLog() {
        when(anexoCU.adjuntarAnexo(eq(SOLICITUD), isNull(), any(), any(), eq(TOKEN)))
                .thenReturn(AnexoAcademico.builder().build())
                .thenThrow(new IllegalStateException("fallo simulado en el segundo soporte"));

        assertThrows(IllegalStateException.class, () -> casoDeUso.radicarCancelacionAsignatura(ESTUDIANTE, "Motivo",
                List.of("am-1"), List.of(soporte(null, "a.pdf", pdf()), soporte(null, "b.pdf", pdf()), soporte(null, "c.pdf", pdf())), TOKEN));

        verify(anexoCU, times(2)).adjuntarAnexo(any(), any(), any(), any(), any());
        verify(log, never()).crearLog(any(), any(), any());
    }
}
