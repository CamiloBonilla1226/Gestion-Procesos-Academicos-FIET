package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TramiteCancelacionMatriculaCUImplAdaptadorTest {

    private static final String SOLICITUD = "sol-1";
    private static final String TIPO_CM = "tipo-cm";
    private static final String FUNCIONARIO = "fun-1";
    private static final String OTRO_FUNCIONARIO = "fun-2";
    private static final String DECANO = "dec-1";
    private static final String TOKEN_FUNCIONARIO = "token-fun";
    private static final String TOKEN_OTRO_FUNCIONARIO = "token-fun2";
    private static final String TOKEN_DECANO = "token-dec";

    @Mock
    private SolicitudCancelacionMatriculaGatewayIntPuerto gateway;

    @Mock
    private SolicitudAcademicaGatewayIntPuerto solicitudGateway;

    @Mock
    private SituacionAcademicaAsignaturaGatewayIntPuerto situacionGateway;

    @Mock
    private UsuarioGatewayIntPuerto usuarioGateway;

    @Mock
    private SesionGatewayIntPuerto sesionGateway;

    @Mock
    private IJwtServicio jwtServicio;

    @Mock
    private EstudianteGatewayIntPuerto estudianteGateway;

    @Mock
    private TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway;

    @Mock
    private EtapaSolicitudAcademicaGatewayIntPuerto etapaGateway;

    @Mock
    private ResolucionAcademicaGatewayIntPuerto resolucionGateway;

    @Mock
    private AnexoAcademicoGatewayIntPuerto anexoGateway;

    @Mock
    private AlmacenamientoAnexosIntPuerto almacenamiento;

    @Mock
    private LogCUIntPuerto log;

    private TramiteCancelacionMatriculaCUImplAdaptador casoDeUso;

    private final TipoSolicitudAcademica matricula = TipoSolicitudAcademica.builder()
            .uuidTipoSolicitudAcademica(TIPO_CM)
            .nombre("Cancelación de Matrícula")
            .funcionarioAcademico(FuncionarioAcademico.builder().uuidUsuario(FUNCIONARIO).build())
            .build();

    private Usuario usuario(String uuid, String rol) {
        return Usuario.builder()
                .uuidUsuario(uuid)
                .roles(new ArrayList<>(List.of(Rol.builder().uuidRol("rol-" + rol).nombre(rol).estado(true).build())))
                .build();
    }

    private void sesion(String token, Usuario usuario) {
        lenient().when(jwtServicio.getUsername(token)).thenReturn("user-" + usuario.getUuidUsuario());
        lenient().when(sesionGateway.getUsuario("user-" + usuario.getUuidUsuario())).thenReturn(usuario);
        lenient().when(usuarioGateway.getUsuario(usuario.getUuidUsuario())).thenReturn(usuario);
    }

    private AsignaturaSolicitudAcademica fila(String uuid, String uuidMatriculada, String nombre) {
        return AsignaturaSolicitudAcademica.builder()
                .uuidAsignaturaSolicitud(uuid)
                .asignaturaMatriculada(AsignaturaMatriculada.builder()
                        .uuidAsignaturaMatriculada(uuidMatriculada)
                        .asignatura(Asignatura.builder().uuidAsignatura("a-" + uuidMatriculada).nombreAsignatura(nombre).build())
                        .grupo("A")
                        .estado("activa")
                        .build())
                .build();
    }

    private SituacionAcademicaAsignatura situacion(String uuid) {
        return SituacionAcademicaAsignatura.builder().uuidSituacionAcademica(uuid).codigo(uuid.toUpperCase()).nombre(uuid).build();
    }

    private void enEtapa(String codigo) {
        when(solicitudGateway.getPorUuid(SOLICITUD)).thenReturn(SolicitudAcademica.builder()
                .uuidSolicitudAcademica(SOLICITUD)
                .radicado("2026-CM-0001")
                .estudiante(Estudiante.builder().uuidUsuario("est-1").build())
                .tipoSolicitudAcademica(matricula)
                .etapa(EtapaSolicitudAcademica.builder().uuidEtapa("et-" + codigo).codigo(codigo).build())
                .build());
    }

    private void conEscaneo() {
        lenient().when(resolucionGateway.existePorSolicitud(SOLICITUD)).thenReturn(true);
    }

    private EvaluacionAsignatura evaluacion(String uuid, Integer faltas, String nota, String situacion) {
        return EvaluacionAsignatura.builder()
                .uuidAsignaturaSolicitud(uuid)
                .numeroFaltas(faltas)
                .nota(nota == null ? null : new BigDecimal(nota))
                .uuidSituacionMatricula(situacion)
                .build();
    }

    private List<EvaluacionAsignatura> evaluacionesCompletas() {
        return List.of(evaluacion("as-1", 0, "4.5", "sit-a"), evaluacion("as-2", 3, "2.0", "sit-c"));
    }

    private SituacionCancelarAsignatura alCancelar(String uuid, String situacion) {
        return SituacionCancelarAsignatura.builder().uuidAsignaturaSolicitud(uuid).uuidSituacionCancelar(situacion).build();
    }

    @BeforeEach
    void setUp() {
        ExcepcionesFormateadorImplAdaptador formateador = new ExcepcionesFormateadorImplAdaptador();
        MaquinaEtapas maquina = new MaquinaEtapas(formateador);
        SolicitudAcademicaCUImplAdaptador solicitudCU = new SolicitudAcademicaCUImplAdaptador(solicitudGateway, estudianteGateway,
                tipoSolicitudGateway, etapaGateway, resolucionGateway, anexoGateway, usuarioGateway, almacenamiento, maquina, formateador, log,
                Clock.fixed(Instant.parse("2026-10-06T15:00:00Z"), ZoneId.of("America/Bogota")));
        casoDeUso = new TramiteCancelacionMatriculaCUImplAdaptador(gateway, solicitudGateway, situacionGateway, usuarioGateway,
                sesionGateway, jwtServicio, solicitudCU, maquina, formateador, log);

        sesion(TOKEN_FUNCIONARIO, usuario(FUNCIONARIO, "Funcionario Académico"));
        sesion(TOKEN_OTRO_FUNCIONARIO, usuario(OTRO_FUNCIONARIO, "Funcionario Académico"));
        sesion(TOKEN_DECANO, usuario(DECANO, "Decano"));
        lenient().when(gateway.getPorSolicitud(SOLICITUD)).thenAnswer(invocacion -> SolicitudCancelacionMatricula.builder()
                .solicitudAcademica(SolicitudAcademica.builder().uuidSolicitudAcademica(SOLICITUD).build())
                .motivoCancelacion("Motivos personales")
                .asignaturas(new ArrayList<>(List.of(fila("as-1", "am-1", "Cálculo I"), fila("as-2", "am-2", "Física I"))))
                .build());
        lenient().when(gateway.actualizarAsignaturas(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
        lenient().when(situacionGateway.getTodas()).thenReturn(List.of(situacion("sit-a"), situacion("sit-b"), situacion("sit-c")));
        lenient().when(etapaGateway.getPorTipoIncluyendoUniversales(TIPO_CM)).thenReturn(EtapaSolicitudAcademicaConstantes.ETAPAS.stream()
                .map(codigo -> EtapaSolicitudAcademica.builder().uuidEtapa("et-" + codigo).codigo(codigo).build())
                .toList());
        lenient().when(anexoGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of());
        lenient().when(solicitudGateway.actualizarEtapa(any(), any())).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private void verificarQueNoSeEscribioNada() {
        verify(solicitudGateway, never()).actualizarEtapa(any(), any());
        verify(gateway, never()).actualizarAsignaturas(any());
        verify(gateway, never()).cambiarEstadoAsignaturasMatriculadas(any(), any());
        verify(resolucionGateway, never()).eliminarPorSolicitud(any());
        verify(log, never()).crearLog(any(), any(), any());
    }

    private String etapaGuardada() {
        ArgumentCaptor<SolicitudAcademica> captor = ArgumentCaptor.forClass(SolicitudAcademica.class);
        verify(solicitudGateway).actualizarEtapa(captor.capture(), any());
        return captor.getValue().getEtapa().getCodigo();
    }

    @SuppressWarnings("unchecked")
    private List<AsignaturaSolicitudAcademica> asignaturasGuardadas() {
        ArgumentCaptor<List<AsignaturaSolicitudAcademica>> captor = ArgumentCaptor.forClass(List.class);
        verify(gateway).actualizarAsignaturas(captor.capture());
        return captor.getValue();
    }

    @Test
    void rechazarPorFuncionarioConEscaneoYObservacionPasaARechazada() {
        enEtapa(RADICADA);
        conEscaneo();

        SolicitudCancelacionMatricula resultado = casoDeUso.rechazarPorFuncionario(SOLICITUD, "No cumple requisitos", TOKEN_FUNCIONARIO);

        assertEquals(RECHAZADA, etapaGuardada());
        assertEquals(RECHAZADA, resultado.getSolicitudAcademica().getEtapa().getCodigo());
        verify(gateway, never()).actualizarAsignaturas(any());
        verify(gateway, never()).cambiarEstadoAsignaturasMatriculadas(any(), any());
    }

    @Test
    void rechazarPorFuncionarioSinEscaneoNoCambiaNada() {
        enEtapa(RADICADA);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.rechazarPorFuncionario(SOLICITUD, "No cumple requisitos", TOKEN_FUNCIONARIO));

        assertTrue(error.getMessage().contains("escaneo de la Resolución"));
        verificarQueNoSeEscribioNada();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void rechazarPorFuncionarioSinObservacionNoCambiaNada(String observacion) {
        enEtapa(RADICADA);
        conEscaneo();

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.rechazarPorFuncionario(SOLICITUD, observacion, TOKEN_FUNCIONARIO));

        assertTrue(error.getMessage().contains("observación"));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void rechazarPorFuncionarioConRolDeDecanoSeRechaza() {
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.rechazarPorFuncionario(SOLICITUD, "No cumple", TOKEN_DECANO));

        verificarQueNoSeEscribioNada();
    }

    @Test
    void rechazarPorFuncionarioNoAsignadoSeRechaza() {
        enEtapa(RADICADA);
        conEscaneo();

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.rechazarPorFuncionario(SOLICITUD, "No cumple", TOKEN_OTRO_FUNCIONARIO));

        assertTrue(error.getMessage().contains(OTRO_FUNCIONARIO));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void rechazarPorFuncionarioEnEtapaEquivocadaSeRechaza() {
        enEtapa(EN_REVISION_DECANO);
        conEscaneo();

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.rechazarPorFuncionario(SOLICITUD, "No cumple", TOKEN_FUNCIONARIO));

        verificarQueNoSeEscribioNada();
    }

    @Test
    void solicitudQueNoEsCancelacionDeMatriculaNoExiste() {
        when(gateway.getPorSolicitud("otra")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.rechazarPorFuncionario("otra", "No cumple", TOKEN_FUNCIONARIO));
        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.enviarRespuesta(null, TOKEN_FUNCIONARIO));

        verificarQueNoSeEscribioNada();
    }

    @Test
    void remitirGuardaLasEvaluacionesYPasaAEnRevisionDecano() {
        enEtapa(RADICADA);

        SolicitudCancelacionMatricula resultado = casoDeUso.remitirADecano(SOLICITUD,
                List.of(evaluacion("as-2", 3, "2", "sit-c"), evaluacion("as-1", 0, "5.0", "sit-a")), null, TOKEN_FUNCIONARIO);

        assertEquals(EN_REVISION_DECANO, etapaGuardada());
        List<AsignaturaSolicitudAcademica> guardadas = asignaturasGuardadas();
        assertEquals(2, guardadas.size());
        assertEquals(0, guardadas.get(0).getNumeroFaltas());
        assertEquals(new BigDecimal("5.0"), guardadas.get(0).getNota());
        assertEquals("sit-a", guardadas.get(0).getSituacionMatricula().getUuidSituacionAcademica());
        assertEquals(3, guardadas.get(1).getNumeroFaltas());
        assertEquals(new BigDecimal("2.0"), guardadas.get(1).getNota());
        assertEquals("sit-c", guardadas.get(1).getSituacionMatricula().getUuidSituacionAcademica());
        assertNull(guardadas.get(0).getSituacionCancelar());
        assertEquals(EN_REVISION_DECANO, resultado.getSolicitudAcademica().getEtapa().getCodigo());
        verify(gateway, never()).cambiarEstadoAsignaturasMatriculadas(any(), any());
        verify(log).crearLog(eq("Evaluar asignaturas de cancelación de matrícula"), contains("2 asignaturas evaluadas"), eq(TOKEN_FUNCIONARIO));
    }

    @Test
    void remitirBorraElEscaneoViejo() {
        enEtapa(RADICADA);
        when(resolucionGateway.getPorSolicitud(SOLICITUD)).thenReturn(ResolucionAcademica.builder().urlArchivo("res/vieja.pdf").build());

        casoDeUso.remitirADecano(SOLICITUD, evaluacionesCompletas(), "Cumple requisitos", TOKEN_FUNCIONARIO);

        verify(resolucionGateway).eliminarPorSolicitud(SOLICITUD);
        verify(almacenamiento).eliminarTrasConfirmar("res/vieja.pdf");
    }

    @Test
    void remitirConUnaAsignaturaSinEvaluarNoGuardaNada() {
        enEtapa(RADICADA);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.remitirADecano(SOLICITUD, List.of(evaluacion("as-1", 0, "4.5", "sit-a")), null, TOKEN_FUNCIONARIO));

        assertTrue(error.getMessage().contains("Física I"));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void remitirSinEvaluacionesNombraTodasLasAsignaturas() {
        enEtapa(RADICADA);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.remitirADecano(SOLICITUD, null, null, TOKEN_FUNCIONARIO));

        assertTrue(error.getMessage().contains("Cálculo I; Física I"));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void remitirConAsignaturaRepetidaNoGuardaNada() {
        enEtapa(RADICADA);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.remitirADecano(SOLICITUD, List.of(evaluacion("as-1", 0, "4.5", "sit-a"),
                        evaluacion("as-2", 1, "3.0", "sit-a"), evaluacion("as-1", 2, "3.5", "sit-a")), null, TOKEN_FUNCIONARIO));

        assertTrue(error.getMessage().contains("Cálculo I"));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void remitirConAsignaturaAjenaNoGuardaNada() {
        enEtapa(RADICADA);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.remitirADecano(SOLICITUD, List.of(evaluacion("as-1", 0, "4.5", "sit-a"),
                        evaluacion("as-2", 1, "3.0", "sit-a"), evaluacion("as-9", 2, "3.5", "sit-a")), null, TOKEN_FUNCIONARIO));

        assertTrue(error.getMessage().contains("as-9"));
        verificarQueNoSeEscribioNada();
    }

    @ParameterizedTest
    @ValueSource(strings = {"5.1", "-0.1", "4.25", "10", "6"})
    void remitirConNotaFueraDeRangoNoGuardaNada(String nota) {
        enEtapa(RADICADA);

        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.remitirADecano(SOLICITUD,
                List.of(evaluacion("as-1", 0, nota, "sit-a"), evaluacion("as-2", 1, "3.0", "sit-a")), null, TOKEN_FUNCIONARIO));

        verificarQueNoSeEscribioNada();
    }

    @Test
    void remitirConFaltasNegativasNoGuardaNada() {
        enEtapa(RADICADA);

        ErrorMalFormatoExcepcion error = assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.remitirADecano(SOLICITUD,
                List.of(evaluacion("as-1", 0, "4.0", "sit-a"), evaluacion("as-2", -1, "3.0", "sit-a")), null, TOKEN_FUNCIONARIO));

        assertTrue(error.getMessage().contains("Física I"));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void remitirConDatosVaciosNoGuardaNada() {
        enEtapa(RADICADA);

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.remitirADecano(SOLICITUD,
                List.of(evaluacion("as-1", null, "4.0", "sit-a"), evaluacion("as-2", 1, "3.0", "sit-a")), null, TOKEN_FUNCIONARIO));
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.remitirADecano(SOLICITUD,
                List.of(evaluacion("as-1", 0, null, "sit-a"), evaluacion("as-2", 1, "3.0", "sit-a")), null, TOKEN_FUNCIONARIO));
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.remitirADecano(SOLICITUD,
                List.of(evaluacion("as-1", 0, "4.0", " "), evaluacion("as-2", 1, "3.0", "sit-a")), null, TOKEN_FUNCIONARIO));

        verificarQueNoSeEscribioNada();
    }

    @Test
    void remitirConSituacionQueNoExisteNoGuardaNada() {
        enEtapa(RADICADA);

        ErrorEntidadNoExisteExcepcion error = assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.remitirADecano(SOLICITUD,
                List.of(evaluacion("as-1", 0, "4.0", "sit-a"), evaluacion("as-2", 1, "3.0", "sit-x")), null, TOKEN_FUNCIONARIO));

        assertTrue(error.getMessage().contains("sit-x"));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void remitirConRolDeDecanoSeRechaza() {
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.remitirADecano(SOLICITUD, evaluacionesCompletas(), null, TOKEN_DECANO));

        verificarQueNoSeEscribioNada();
    }

    @Test
    void remitirPorFuncionarioNoAsignadoSeRechazaAntesDeEvaluar() {
        enEtapa(RADICADA);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.remitirADecano(SOLICITUD, List.of(), null, TOKEN_OTRO_FUNCIONARIO));

        assertTrue(error.getMessage().contains(OTRO_FUNCIONARIO));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void remitirEnEtapaEquivocadaSeRechazaAntesDeEvaluar() {
        enEtapa(APROBADA_POR_DECANO);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.remitirADecano(SOLICITUD, List.of(), null, TOKEN_FUNCIONARIO));

        assertTrue(error.getMessage().contains(AccionEtapa.REMITIR_DECANO.name()));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void remitirSiFallaElCambioDeEtapaNoGuardaEvaluaciones() {
        enEtapa(RADICADA);
        when(solicitudGateway.actualizarEtapa(any(), any())).thenThrow(new IllegalStateException("fallo"));

        assertThrows(IllegalStateException.class, () -> casoDeUso.remitirADecano(SOLICITUD, evaluacionesCompletas(), null, TOKEN_FUNCIONARIO));

        verify(gateway, never()).actualizarAsignaturas(any());
        verify(log, never()).crearLog(any(), any(), any());
    }

    @Test
    void remitirSiFallaGuardarEvaluacionesPropagaElErrorParaElRollback() {
        enEtapa(RADICADA);
        when(gateway.actualizarAsignaturas(any())).thenThrow(new IllegalStateException("fallo"));

        assertThrows(IllegalStateException.class, () -> casoDeUso.remitirADecano(SOLICITUD, evaluacionesCompletas(), null, TOKEN_FUNCIONARIO));

        verify(log, never()).crearLog(eq("Evaluar asignaturas de cancelación de matrícula"), any(), any());
    }

    @Test
    void aprobarPorDecanoGuardaLaSituacionAlCancelarYPasaAAprobadaPorDecano() {
        enEtapa(EN_REVISION_DECANO);

        SolicitudCancelacionMatricula resultado = casoDeUso.aprobarPorDecano(SOLICITUD,
                List.of(alCancelar("as-1", "sit-b"), alCancelar("as-2", "sit-c")), TOKEN_DECANO);

        assertEquals(APROBADA_POR_DECANO, etapaGuardada());
        List<AsignaturaSolicitudAcademica> guardadas = asignaturasGuardadas();
        assertEquals("sit-b", guardadas.get(0).getSituacionCancelar().getUuidSituacionAcademica());
        assertEquals("sit-c", guardadas.get(1).getSituacionCancelar().getUuidSituacionAcademica());
        assertEquals("activa", resultado.getAsignaturas().get(0).getAsignaturaMatriculada().getEstado());
        verify(gateway, never()).cambiarEstadoAsignaturasMatriculadas(any(), any());
    }

    @Test
    void aprobarConCoberturaIncompletaRepetidaOAjenaNoGuardaNada() {
        enEtapa(EN_REVISION_DECANO);

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.aprobarPorDecano(SOLICITUD, List.of(alCancelar("as-1", "sit-b")), TOKEN_DECANO));
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.aprobarPorDecano(SOLICITUD,
                List.of(alCancelar("as-1", "sit-b"), alCancelar("as-1", "sit-b")), TOKEN_DECANO));
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.aprobarPorDecano(SOLICITUD,
                List.of(alCancelar("as-1", "sit-b"), alCancelar("as-2", "sit-b"), alCancelar("as-3", "sit-b")), TOKEN_DECANO));
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.aprobarPorDecano(SOLICITUD,
                List.of(alCancelar("as-1", "sit-b"), alCancelar("as-2", null)), TOKEN_DECANO));
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.aprobarPorDecano(SOLICITUD, null, TOKEN_DECANO));

        verificarQueNoSeEscribioNada();
    }

    @Test
    void aprobarConSituacionQueNoExisteNoGuardaNada() {
        enEtapa(EN_REVISION_DECANO);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.aprobarPorDecano(SOLICITUD,
                List.of(alCancelar("as-1", "sit-b"), alCancelar("as-2", "sit-x")), TOKEN_DECANO));

        verificarQueNoSeEscribioNada();
    }

    @Test
    void aprobarConRolDeFuncionarioSeRechaza() {
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.aprobarPorDecano(SOLICITUD,
                List.of(alCancelar("as-1", "sit-b"), alCancelar("as-2", "sit-c")), TOKEN_FUNCIONARIO));

        verificarQueNoSeEscribioNada();
    }

    @Test
    void aprobarEnEtapaEquivocadaSeRechaza() {
        enEtapa(RADICADA);

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.aprobarPorDecano(SOLICITUD,
                List.of(alCancelar("as-1", "sit-b"), alCancelar("as-2", "sit-c")), TOKEN_DECANO));

        verificarQueNoSeEscribioNada();
    }

    @Test
    void rechazarPorDecanoPasaARechazadaPorDecanoSinTocarSituaciones() {
        enEtapa(EN_REVISION_DECANO);

        casoDeUso.rechazarPorDecano(SOLICITUD, "No procede", TOKEN_DECANO);

        assertEquals(RECHAZADA_POR_DECANO, etapaGuardada());
        verify(gateway, never()).actualizarAsignaturas(any());
        verify(gateway, never()).cambiarEstadoAsignaturasMatriculadas(any(), any());
    }

    @Test
    void rechazarPorDecanoSinObservacionNoCambiaNada() {
        enEtapa(EN_REVISION_DECANO);

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.rechazarPorDecano(SOLICITUD, " ", TOKEN_DECANO));

        verificarQueNoSeEscribioNada();
    }

    @Test
    void rechazarPorDecanoConRolDeFuncionarioOEtapaEquivocadaSeRechaza() {
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.rechazarPorDecano(SOLICITUD, "No", TOKEN_FUNCIONARIO));
        enEtapa(RADICADA);
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.rechazarPorDecano(SOLICITUD, "No", TOKEN_DECANO));

        verificarQueNoSeEscribioNada();
    }

    @Test
    void enviarRespuestaAprobadaCancelaLasAsignaturasMatriculadas() {
        enEtapa(APROBADA_POR_DECANO);
        conEscaneo();

        SolicitudCancelacionMatricula resultado = casoDeUso.enviarRespuesta(SOLICITUD, TOKEN_FUNCIONARIO);

        assertEquals(APROBADA, etapaGuardada());
        verify(gateway).cambiarEstadoAsignaturasMatriculadas(List.of("am-1", "am-2"), "cancelada");
        assertTrue(resultado.getAsignaturas().stream().allMatch(a -> "cancelada".equals(a.getAsignaturaMatriculada().getEstado())));
        verify(log).crearLog(eq("Cancelar asignaturas matriculadas"), contains("2 asignaturas"), eq(TOKEN_FUNCIONARIO));
    }

    @Test
    void enviarRespuestaRechazadaDejaLasAsignaturasActivas() {
        enEtapa(RECHAZADA_POR_DECANO);
        conEscaneo();

        SolicitudCancelacionMatricula resultado = casoDeUso.enviarRespuesta(SOLICITUD, TOKEN_FUNCIONARIO);

        assertEquals(RECHAZADA, etapaGuardada());
        verify(gateway, never()).cambiarEstadoAsignaturasMatriculadas(any(), any());
        assertTrue(resultado.getAsignaturas().stream().allMatch(a -> "activa".equals(a.getAsignaturaMatriculada().getEstado())));
    }

    @Test
    void enviarRespuestaSinEscaneoNoCancelaNada() {
        enEtapa(APROBADA_POR_DECANO);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.enviarRespuesta(SOLICITUD, TOKEN_FUNCIONARIO));

        assertTrue(error.getMessage().contains("escaneo de la Resolución"));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void enviarRespuestaEnEtapaEquivocadaNoCancelaNada() {
        enEtapa(EN_REVISION_DECANO);
        conEscaneo();

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.enviarRespuesta(SOLICITUD, TOKEN_FUNCIONARIO));

        verificarQueNoSeEscribioNada();
    }

    @Test
    void enviarRespuestaConRolDeDecanoOFuncionarioNoAsignadoSeRechaza() {
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.enviarRespuesta(SOLICITUD, TOKEN_DECANO));
        enEtapa(APROBADA_POR_DECANO);
        conEscaneo();
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.enviarRespuesta(SOLICITUD, TOKEN_OTRO_FUNCIONARIO));

        verificarQueNoSeEscribioNada();
    }

    @Test
    void enviarRespuestaSiFallaCancelarLasAsignaturasPropagaElErrorParaElRollback() {
        enEtapa(APROBADA_POR_DECANO);
        conEscaneo();
        doThrow(new IllegalStateException("fallo")).when(gateway).cambiarEstadoAsignaturasMatriculadas(any(), any());

        assertThrows(IllegalStateException.class, () -> casoDeUso.enviarRespuesta(SOLICITUD, TOKEN_FUNCIONARIO));

        verify(log, never()).crearLog(eq("Cancelar asignaturas matriculadas"), any(), any());
    }

    @Test
    void lasAsignaturasSoloSeCancelanAlEnviarLaRespuestaAprobada() {
        enEtapa(RADICADA);
        casoDeUso.remitirADecano(SOLICITUD, evaluacionesCompletas(), null, TOKEN_FUNCIONARIO);
        enEtapa(EN_REVISION_DECANO);
        casoDeUso.aprobarPorDecano(SOLICITUD, List.of(alCancelar("as-1", "sit-b"), alCancelar("as-2", "sit-c")), TOKEN_DECANO);
        verify(gateway, never()).cambiarEstadoAsignaturasMatriculadas(any(), any());

        enEtapa(APROBADA_POR_DECANO);
        conEscaneo();
        casoDeUso.enviarRespuesta(SOLICITUD, TOKEN_FUNCIONARIO);

        verify(gateway, times(1)).cambiarEstadoAsignaturasMatriculadas(List.of("am-1", "am-2"), "cancelada");
    }
}
