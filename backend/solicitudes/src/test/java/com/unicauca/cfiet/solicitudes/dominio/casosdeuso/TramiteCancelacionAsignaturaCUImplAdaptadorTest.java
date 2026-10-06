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
class TramiteCancelacionAsignaturaCUImplAdaptadorTest {

    private static final String SOLICITUD = "sol-1";
    private static final String TIPO_CA = "tipo-ca";
    private static final String FUNCIONARIO = "fun-1";
    private static final String OTRO_FUNCIONARIO = "fun-2";
    private static final String DECANO = "dec-1";
    private static final String TOKEN_FUNCIONARIO = "token-fun";
    private static final String TOKEN_OTRO_FUNCIONARIO = "token-fun2";
    private static final String TOKEN_DECANO = "token-dec";

    @Mock
    private SolicitudCancelacionAsignaturaGatewayIntPuerto gateway;

    @Mock
    private SolicitudCancelacionMatriculaGatewayIntPuerto asignaturaSolicitudGateway;

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

    private TramiteCancelacionAsignaturaCUImplAdaptador casoDeUso;
    private List<AsignaturaSolicitudAcademica> filas;

    private final TipoSolicitudAcademica asignatura = TipoSolicitudAcademica.builder()
            .uuidTipoSolicitudAcademica(TIPO_CA)
            .nombre("Cancelación de Asignatura")
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

    private AsignaturaSolicitudAcademica fila(String uuid, String uuidMatriculada, String nombre, Boolean cumple) {
        return AsignaturaSolicitudAcademica.builder()
                .uuidAsignaturaSolicitud(uuid)
                .asignaturaMatriculada(AsignaturaMatriculada.builder()
                        .uuidAsignaturaMatriculada(uuidMatriculada)
                        .asignatura(Asignatura.builder().uuidAsignatura("a-" + uuidMatriculada).nombreAsignatura(nombre).build())
                        .grupo("A")
                        .estado("activa")
                        .build())
                .cumpleCondiciones(cumple)
                .build();
    }

    private void conFilas(Boolean cumple1, Boolean cumple2, Boolean cumple3) {
        filas = new ArrayList<>(List.of(fila("as-1", "am-1", "Cálculo I", cumple1), fila("as-2", "am-2", "Física I", cumple2),
                fila("as-3", "am-3", "Química I", cumple3)));
    }

    private void enEtapa(String codigo) {
        when(solicitudGateway.getPorUuid(SOLICITUD)).thenReturn(SolicitudAcademica.builder()
                .uuidSolicitudAcademica(SOLICITUD)
                .radicado("2026-CA-0001")
                .estudiante(Estudiante.builder().uuidUsuario("est-1").build())
                .tipoSolicitudAcademica(asignatura)
                .etapa(EtapaSolicitudAcademica.builder().uuidEtapa("et-" + codigo).codigo(codigo).build())
                .build());
    }

    private void conEscaneo() {
        lenient().when(resolucionGateway.existePorSolicitud(SOLICITUD)).thenReturn(true);
    }

    private EvaluacionAsignatura evaluacion(String uuid, Integer faltas, String nota, String situacion, Boolean cumple, String observacion) {
        return EvaluacionAsignatura.builder()
                .uuidAsignaturaSolicitud(uuid)
                .numeroFaltas(faltas)
                .nota(nota == null ? null : new BigDecimal(nota))
                .uuidSituacionMatricula(situacion)
                .cumpleCondiciones(cumple)
                .observacionEvaluacion(observacion)
                .build();
    }

    private List<EvaluacionAsignatura> evaluacionesValidas() {
        return List.of(evaluacion("as-1", 0, "4.5", "sit-a", true, null),
                evaluacion("as-2", 2, "3.0", "sit-a", true, "Verificado"),
                evaluacion("as-3", 5, "2.0", "sit-b", false, "Nota promedio menor a 3.0"));
    }

    private DecisionAsignatura decision(String uuid, Boolean aprobada, String situacion, String observacion) {
        return DecisionAsignatura.builder().uuidAsignaturaSolicitud(uuid).aprobada(aprobada).uuidSituacionCancelar(situacion)
                .observacionDecision(observacion).build();
    }

    private List<DecisionAsignatura> decisionesValidas() {
        return List.of(decision("as-1", true, "sit-c", null),
                decision("as-2", false, "sit-c", "El soporte no justifica la cancelación"),
                decision("as-3", false, null, "No cumple las condiciones"));
    }

    @BeforeEach
    void setUp() {
        ExcepcionesFormateadorImplAdaptador formateador = new ExcepcionesFormateadorImplAdaptador();
        MaquinaEtapas maquina = new MaquinaEtapas(formateador);
        SolicitudAcademicaCUImplAdaptador solicitudCU = new SolicitudAcademicaCUImplAdaptador(solicitudGateway, estudianteGateway,
                tipoSolicitudGateway, etapaGateway, resolucionGateway, anexoGateway, usuarioGateway, almacenamiento, maquina, formateador, log,
                Clock.fixed(Instant.parse("2026-10-06T15:00:00Z"), ZoneId.of("America/Bogota")));
        casoDeUso = new TramiteCancelacionAsignaturaCUImplAdaptador(gateway, asignaturaSolicitudGateway, solicitudGateway, situacionGateway,
                usuarioGateway, sesionGateway, jwtServicio, solicitudCU, maquina, formateador, log);

        sesion(TOKEN_FUNCIONARIO, usuario(FUNCIONARIO, "Funcionario Académico"));
        sesion(TOKEN_OTRO_FUNCIONARIO, usuario(OTRO_FUNCIONARIO, "Funcionario Académico"));
        sesion(TOKEN_DECANO, usuario(DECANO, "Decano"));
        conFilas(null, null, null);
        lenient().when(gateway.getPorSolicitud(SOLICITUD)).thenAnswer(invocacion -> SolicitudCancelacionAsignatura.builder()
                .solicitudAcademica(SolicitudAcademica.builder().uuidSolicitudAcademica(SOLICITUD).build())
                .motivoCancelacion("Incapacidad")
                .asignaturas(filas)
                .build());
        lenient().when(asignaturaSolicitudGateway.actualizarAsignaturas(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
        lenient().when(situacionGateway.getTodas()).thenReturn(List.of(
                SituacionAcademicaAsignatura.builder().uuidSituacionAcademica("sit-a").codigo("R0").nombre("Primera vez").build(),
                SituacionAcademicaAsignatura.builder().uuidSituacionAcademica("sit-b").codigo("R1").nombre("Segunda vez").build(),
                SituacionAcademicaAsignatura.builder().uuidSituacionAcademica("sit-c").codigo("R2").nombre("Tercera vez").build()));
        lenient().when(etapaGateway.getPorTipoIncluyendoUniversales(TIPO_CA)).thenReturn(EtapaSolicitudAcademicaConstantes.ETAPAS.stream()
                .map(codigo -> EtapaSolicitudAcademica.builder().uuidEtapa("et-" + codigo).codigo(codigo).build())
                .toList());
        lenient().when(anexoGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of());
        lenient().when(solicitudGateway.actualizarEtapa(any(), any())).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private void verificarQueNoSeEscribioNada() {
        verify(solicitudGateway, never()).actualizarEtapa(any(), any());
        verify(asignaturaSolicitudGateway, never()).actualizarAsignaturas(any());
        verify(asignaturaSolicitudGateway, never()).cambiarEstadoAsignaturasMatriculadas(any(), any());
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
        verify(asignaturaSolicitudGateway).actualizarAsignaturas(captor.capture());
        return captor.getValue();
    }

    private ErrorReglaNegocioVioladaExcepcion falla(Runnable accion) {
        return assertThrows(ErrorReglaNegocioVioladaExcepcion.class, accion::run);
    }

    @Test
    void rechazarPorFuncionarioConEscaneoYObservacionPasaARechazada() {
        enEtapa(RADICADA);
        conEscaneo();

        SolicitudCancelacionAsignatura resultado = casoDeUso.rechazarPorFuncionario(SOLICITUD, "No cumple requisitos", TOKEN_FUNCIONARIO);

        assertEquals(RECHAZADA, etapaGuardada());
        assertEquals(RECHAZADA, resultado.getSolicitudAcademica().getEtapa().getCodigo());
        verify(asignaturaSolicitudGateway, never()).actualizarAsignaturas(any());
        verify(asignaturaSolicitudGateway, never()).cambiarEstadoAsignaturasMatriculadas(any(), any());
    }

    @Test
    void rechazarPorFuncionarioSinEscaneoOSinObservacionNoCambiaNada() {
        enEtapa(RADICADA);
        assertTrue(falla(() -> casoDeUso.rechazarPorFuncionario(SOLICITUD, "No cumple", TOKEN_FUNCIONARIO)).getMessage().contains("escaneo de la Resolución"));
        conEscaneo();
        assertTrue(falla(() -> casoDeUso.rechazarPorFuncionario(SOLICITUD, "  ", TOKEN_FUNCIONARIO)).getMessage().contains("observación"));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void rechazarPorFuncionarioConRolEquivocadoNoAsignadoOEtapaEquivocadaSeRechaza() {
        falla(() -> casoDeUso.rechazarPorFuncionario(SOLICITUD, "No cumple", TOKEN_DECANO));
        enEtapa(RADICADA);
        conEscaneo();
        assertTrue(falla(() -> casoDeUso.rechazarPorFuncionario(SOLICITUD, "No cumple", TOKEN_OTRO_FUNCIONARIO)).getMessage().contains(OTRO_FUNCIONARIO));
        enEtapa(EN_REVISION_DECANO);
        falla(() -> casoDeUso.rechazarPorFuncionario(SOLICITUD, "No cumple", TOKEN_FUNCIONARIO));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void unaSolicitudQueNoEsCancelacionDeAsignaturaNoExiste() {
        when(gateway.getPorSolicitud("otra")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.rechazarPorFuncionario("otra", "No", TOKEN_FUNCIONARIO));
        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.enviarRespuesta(null, TOKEN_FUNCIONARIO));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void remitirGuardaLaEvaluacionConLasCondicionesYPasaAEnRevisionDecano() {
        enEtapa(RADICADA);

        SolicitudCancelacionAsignatura resultado = casoDeUso.remitirADecano(SOLICITUD, evaluacionesValidas(), null, TOKEN_FUNCIONARIO);

        assertEquals(EN_REVISION_DECANO, etapaGuardada());
        List<AsignaturaSolicitudAcademica> guardadas = asignaturasGuardadas();
        assertEquals(List.of(true, true, false), guardadas.stream().map(AsignaturaSolicitudAcademica::getCumpleCondiciones).toList());
        assertEquals(new BigDecimal("4.5"), guardadas.get(0).getNota());
        assertEquals(new BigDecimal("3.0"), guardadas.get(1).getNota());
        assertNull(guardadas.get(0).getObservacionEvaluacion());
        assertEquals("Verificado", guardadas.get(1).getObservacionEvaluacion());
        assertEquals("Nota promedio menor a 3.0", guardadas.get(2).getObservacionEvaluacion());
        assertEquals("sit-b", guardadas.get(2).getSituacionMatricula().getUuidSituacionAcademica());
        assertTrue(guardadas.stream().allMatch(a -> a.getAprobadaPorDecano() == null && a.getSituacionCancelar() == null));
        assertEquals(EN_REVISION_DECANO, resultado.getSolicitudAcademica().getEtapa().getCodigo());
        verify(log).crearLog(eq("Evaluar asignaturas de cancelación de asignatura"), contains("2 cumplen"), eq(TOKEN_FUNCIONARIO));
    }

    @Test
    void remitirBorraElEscaneoViejo() {
        enEtapa(RADICADA);
        when(resolucionGateway.getPorSolicitud(SOLICITUD)).thenReturn(ResolucionAcademica.builder().urlArchivo("res/vieja.pdf").build());

        casoDeUso.remitirADecano(SOLICITUD, evaluacionesValidas(), "Revisada", TOKEN_FUNCIONARIO);

        verify(resolucionGateway).eliminarPorSolicitud(SOLICITUD);
        verify(almacenamiento).eliminarTrasConfirmar("res/vieja.pdf");
    }

    @Test
    void remitirConCoberturaIncompletaRepetidaOAjenaNoGuardaNada() {
        enEtapa(RADICADA);
        List<EvaluacionAsignatura> validas = evaluacionesValidas();

        assertTrue(falla(() -> casoDeUso.remitirADecano(SOLICITUD, validas.subList(0, 2), null, TOKEN_FUNCIONARIO)).getMessage().contains("Química I"));
        falla(() -> casoDeUso.remitirADecano(SOLICITUD, null, null, TOKEN_FUNCIONARIO));
        List<EvaluacionAsignatura> repetida = new ArrayList<>(validas);
        repetida.add(evaluacion("as-1", 0, "4.0", "sit-a", true, null));
        assertTrue(falla(() -> casoDeUso.remitirADecano(SOLICITUD, repetida, null, TOKEN_FUNCIONARIO)).getMessage().contains("Cálculo I"));
        List<EvaluacionAsignatura> ajena = new ArrayList<>(validas);
        ajena.add(evaluacion("as-9", 0, "4.0", "sit-a", true, null));
        assertTrue(falla(() -> casoDeUso.remitirADecano(SOLICITUD, ajena, null, TOKEN_FUNCIONARIO)).getMessage().contains("as-9"));
        verificarQueNoSeEscribioNada();
    }

    @ParameterizedTest
    @ValueSource(strings = {"5.1", "-0.1", "4.25"})
    void remitirConNotaFueraDeRangoNoGuardaNada(String nota) {
        enEtapa(RADICADA);

        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.remitirADecano(SOLICITUD, List.of(evaluacion("as-1", 0, nota, "sit-a", true, null),
                evaluacion("as-2", 0, "4.0", "sit-a", true, null), evaluacion("as-3", 0, "4.0", "sit-a", true, null)), null, TOKEN_FUNCIONARIO));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void remitirConFaltasNegativasOSituacionInexistenteNoGuardaNada() {
        enEtapa(RADICADA);

        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.remitirADecano(SOLICITUD, List.of(evaluacion("as-1", -1, "4.0", "sit-a", true, null),
                evaluacion("as-2", 0, "4.0", "sit-a", true, null), evaluacion("as-3", 0, "4.0", "sit-a", true, null)), null, TOKEN_FUNCIONARIO));
        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.remitirADecano(SOLICITUD, List.of(evaluacion("as-1", 0, "4.0", "sit-x", true, null),
                evaluacion("as-2", 0, "4.0", "sit-a", true, null), evaluacion("as-3", 0, "4.0", "sit-a", true, null)), null, TOKEN_FUNCIONARIO));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void remitirConNotaMenorATresMarcadaComoCumpleSeRechazaNombrandoAsignaturaYNota() {
        enEtapa(RADICADA);

        ErrorReglaNegocioVioladaExcepcion error = falla(() -> casoDeUso.remitirADecano(SOLICITUD, List.of(evaluacion("as-1", 0, "4.0", "sit-a", true, null),
                evaluacion("as-2", 0, "2.9", "sit-a", true, null), evaluacion("as-3", 0, "4.0", "sit-a", true, null)), null, TOKEN_FUNCIONARIO));

        assertTrue(error.getMessage().contains("Física I"), error::getMessage);
        assertTrue(error.getMessage().contains("2.9"), error::getMessage);
        verificarQueNoSeEscribioNada();
    }

    @Test
    void remitirConUnaQueNoCumpleSinObservacionSeRechaza() {
        enEtapa(RADICADA);

        ErrorReglaNegocioVioladaExcepcion error = falla(() -> casoDeUso.remitirADecano(SOLICITUD, List.of(evaluacion("as-1", 0, "4.0", "sit-a", true, null),
                evaluacion("as-2", 0, "4.0", "sit-a", true, null), evaluacion("as-3", 0, "4.0", "sit-a", false, "   ")), null, TOKEN_FUNCIONARIO));

        assertTrue(error.getMessage().contains("Química I"));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void remitirSinConfirmarLasCondicionesOConObservacionMuyLargaSeRechaza() {
        enEtapa(RADICADA);

        falla(() -> casoDeUso.remitirADecano(SOLICITUD, List.of(evaluacion("as-1", 0, "4.0", "sit-a", null, null),
                evaluacion("as-2", 0, "4.0", "sit-a", true, null), evaluacion("as-3", 0, "4.0", "sit-a", true, null)), null, TOKEN_FUNCIONARIO));
        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.remitirADecano(SOLICITUD, List.of(evaluacion("as-1", 0, "4.0", "sit-a", true, null),
                evaluacion("as-2", 0, "4.0", "sit-a", true, null), evaluacion("as-3", 0, "4.0", "sit-a", false, "x".repeat(256))), null, TOKEN_FUNCIONARIO));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void remitirSinNingunaAsignaturaQueCumplaIndicaQueDebeRechazar() {
        enEtapa(RADICADA);

        ErrorReglaNegocioVioladaExcepcion error = falla(() -> casoDeUso.remitirADecano(SOLICITUD, List.of(evaluacion("as-1", 0, "2.0", "sit-a", false, "Nota"),
                evaluacion("as-2", 9, "4.0", "sit-a", false, "Repitente"), evaluacion("as-3", 0, "4.0", "sit-a", false, "Co-requisito")), null, TOKEN_FUNCIONARIO));

        assertTrue(error.getMessage().contains("rechazarla"), error::getMessage);
        verificarQueNoSeEscribioNada();
    }

    @Test
    void remitirConRolEquivocadoNoAsignadoOEtapaEquivocadaSeRechazaAntesDeEvaluar() {
        falla(() -> casoDeUso.remitirADecano(SOLICITUD, evaluacionesValidas(), null, TOKEN_DECANO));
        enEtapa(RADICADA);
        assertTrue(falla(() -> casoDeUso.remitirADecano(SOLICITUD, List.of(), null, TOKEN_OTRO_FUNCIONARIO)).getMessage().contains(OTRO_FUNCIONARIO));
        enEtapa(APROBADA_POR_DECANO);
        assertTrue(falla(() -> casoDeUso.remitirADecano(SOLICITUD, List.of(), null, TOKEN_FUNCIONARIO)).getMessage().contains("REMITIR_DECANO"));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void remitirSiFallaElCambioDeEtapaNoGuardaEvaluaciones() {
        enEtapa(RADICADA);
        when(solicitudGateway.actualizarEtapa(any(), any())).thenThrow(new IllegalStateException("fallo"));

        assertThrows(IllegalStateException.class, () -> casoDeUso.remitirADecano(SOLICITUD, evaluacionesValidas(), null, TOKEN_FUNCIONARIO));

        verify(asignaturaSolicitudGateway, never()).actualizarAsignaturas(any());
        verify(log, never()).crearLog(any(), any(), any());
    }

    @Test
    void aprobarGuardaLaDecisionDeCadaAsignaturaYLaSituacionSoloDeLasAprobadas() {
        conFilas(true, true, false);
        enEtapa(EN_REVISION_DECANO);

        SolicitudCancelacionAsignatura resultado = casoDeUso.aprobarPorDecano(SOLICITUD, decisionesValidas(), TOKEN_DECANO);

        assertEquals(APROBADA_POR_DECANO, etapaGuardada());
        List<AsignaturaSolicitudAcademica> guardadas = asignaturasGuardadas();
        assertEquals(List.of(true, false, false), guardadas.stream().map(AsignaturaSolicitudAcademica::getAprobadaPorDecano).toList());
        assertEquals("sit-c", guardadas.get(0).getSituacionCancelar().getUuidSituacionAcademica());
        assertNull(guardadas.get(1).getSituacionCancelar());
        assertNull(guardadas.get(2).getSituacionCancelar());
        assertNull(guardadas.get(0).getObservacionDecision());
        assertEquals("El soporte no justifica la cancelación", guardadas.get(1).getObservacionDecision());
        assertEquals(APROBADA_POR_DECANO, resultado.getSolicitudAcademica().getEtapa().getCodigo());
        verify(asignaturaSolicitudGateway, never()).cambiarEstadoAsignaturasMatriculadas(any(), any());
        verify(log).crearLog(eq("Registrar decisión del Decano por asignatura"), contains("1 asignaturas aprobadas y 2 rechazadas"), eq(TOKEN_DECANO));
    }

    @Test
    void elDecanoNoApruebaUnaAsignaturaQueNoCumple() {
        conFilas(true, true, false);
        enEtapa(EN_REVISION_DECANO);

        ErrorReglaNegocioVioladaExcepcion error = falla(() -> casoDeUso.aprobarPorDecano(SOLICITUD, List.of(decision("as-1", true, "sit-c", null),
                decision("as-2", true, "sit-c", null), decision("as-3", true, "sit-c", null)), TOKEN_DECANO));

        assertTrue(error.getMessage().contains("Química I"), error::getMessage);
        verificarQueNoSeEscribioNada();
    }

    @Test
    void rechazarUnaAsignaturaSinObservacionSeRechaza() {
        conFilas(true, true, false);
        enEtapa(EN_REVISION_DECANO);

        ErrorReglaNegocioVioladaExcepcion error = falla(() -> casoDeUso.aprobarPorDecano(SOLICITUD, List.of(decision("as-1", true, "sit-c", null),
                decision("as-2", false, null, " "), decision("as-3", false, null, "No cumple")), TOKEN_DECANO));

        assertTrue(error.getMessage().contains("Física I"));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void sinNingunaAsignaturaAprobadaIndicaUsarElRechazoDelDecano() {
        conFilas(true, true, false);
        enEtapa(EN_REVISION_DECANO);

        ErrorReglaNegocioVioladaExcepcion error = falla(() -> casoDeUso.aprobarPorDecano(SOLICITUD, List.of(decision("as-1", false, null, "No"),
                decision("as-2", false, null, "No"), decision("as-3", false, null, "No")), TOKEN_DECANO));

        assertTrue(error.getMessage().contains("rechazo del Decano"), error::getMessage);
        verificarQueNoSeEscribioNada();
    }

    @Test
    void aprobarConDecisionVaciaSinSituacionOSituacionInexistenteNoGuardaNada() {
        conFilas(true, true, false);
        enEtapa(EN_REVISION_DECANO);

        falla(() -> casoDeUso.aprobarPorDecano(SOLICITUD, List.of(decision("as-1", null, "sit-c", null),
                decision("as-2", false, null, "No"), decision("as-3", false, null, "No")), TOKEN_DECANO));
        falla(() -> casoDeUso.aprobarPorDecano(SOLICITUD, List.of(decision("as-1", true, null, null),
                decision("as-2", false, null, "No"), decision("as-3", false, null, "No")), TOKEN_DECANO));
        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.aprobarPorDecano(SOLICITUD, List.of(decision("as-1", true, "sit-x", null),
                decision("as-2", false, null, "No"), decision("as-3", false, null, "No")), TOKEN_DECANO));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void aprobarConCoberturaIncompletaRepetidaOAjenaNoGuardaNada() {
        conFilas(true, true, false);
        enEtapa(EN_REVISION_DECANO);
        List<DecisionAsignatura> validas = decisionesValidas();

        falla(() -> casoDeUso.aprobarPorDecano(SOLICITUD, validas.subList(0, 2), TOKEN_DECANO));
        List<DecisionAsignatura> repetida = new ArrayList<>(validas);
        repetida.add(decision("as-2", false, null, "No"));
        falla(() -> casoDeUso.aprobarPorDecano(SOLICITUD, repetida, TOKEN_DECANO));
        List<DecisionAsignatura> ajena = new ArrayList<>(validas);
        ajena.add(decision("as-9", true, "sit-c", null));
        falla(() -> casoDeUso.aprobarPorDecano(SOLICITUD, ajena, TOKEN_DECANO));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void aprobarConRolDeFuncionarioOEtapaEquivocadaSeRechaza() {
        conFilas(true, true, false);
        falla(() -> casoDeUso.aprobarPorDecano(SOLICITUD, decisionesValidas(), TOKEN_FUNCIONARIO));
        enEtapa(RADICADA);
        falla(() -> casoDeUso.aprobarPorDecano(SOLICITUD, decisionesValidas(), TOKEN_DECANO));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void aprobarSiFallaGuardarLasDecisionesPropagaElErrorParaElRollback() {
        conFilas(true, true, false);
        enEtapa(EN_REVISION_DECANO);
        when(asignaturaSolicitudGateway.actualizarAsignaturas(any())).thenThrow(new IllegalStateException("fallo"));

        assertThrows(IllegalStateException.class, () -> casoDeUso.aprobarPorDecano(SOLICITUD, decisionesValidas(), TOKEN_DECANO));

        verify(log, never()).crearLog(eq("Registrar decisión del Decano por asignatura"), any(), any());
    }

    @Test
    void rechazarPorDecanoPasaARechazadaPorDecanoSinMarcarAsignaturas() {
        enEtapa(EN_REVISION_DECANO);

        casoDeUso.rechazarPorDecano(SOLICITUD, "No procede", TOKEN_DECANO);

        assertEquals(RECHAZADA_POR_DECANO, etapaGuardada());
        verify(asignaturaSolicitudGateway, never()).actualizarAsignaturas(any());
    }

    @Test
    void rechazarPorDecanoSinObservacionOConRolEquivocadoNoCambiaNada() {
        falla(() -> casoDeUso.rechazarPorDecano(SOLICITUD, "No", TOKEN_FUNCIONARIO));
        enEtapa(EN_REVISION_DECANO);
        falla(() -> casoDeUso.rechazarPorDecano(SOLICITUD, "", TOKEN_DECANO));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void enviarRespuestaAprobadaSoloCancelaLasAprobadasQueSiganActivas() {
        AsignaturaSolicitudAcademica aprobadaActiva = fila("as-1", "am-1", "Cálculo I", true);
        aprobadaActiva.setAprobadaPorDecano(true);
        AsignaturaSolicitudAcademica rechazada = fila("as-2", "am-2", "Física I", true);
        rechazada.setAprobadaPorDecano(false);
        AsignaturaSolicitudAcademica aprobadaYaPerdida = fila("as-3", "am-3", "Química I", true);
        aprobadaYaPerdida.setAprobadaPorDecano(true);
        aprobadaYaPerdida.getAsignaturaMatriculada().setEstado("perdida");
        AsignaturaSolicitudAcademica aprobadaActiva2 = fila("as-4", "am-4", "Dibujo", true);
        aprobadaActiva2.setAprobadaPorDecano(true);
        filas = new ArrayList<>(List.of(aprobadaActiva, rechazada, aprobadaYaPerdida, aprobadaActiva2));
        enEtapa(APROBADA_POR_DECANO);
        conEscaneo();

        SolicitudCancelacionAsignatura resultado = casoDeUso.enviarRespuesta(SOLICITUD, TOKEN_FUNCIONARIO);

        assertEquals(APROBADA, etapaGuardada());
        verify(asignaturaSolicitudGateway).cambiarEstadoAsignaturasMatriculadas(List.of("am-1", "am-4"), "cancelada");
        assertEquals(List.of("cancelada", "activa", "perdida", "cancelada"), resultado.getAsignaturas().stream()
                .map(a -> a.getAsignaturaMatriculada().getEstado()).toList());
        verify(log).crearLog(eq("Cancelar asignaturas matriculadas"), contains("2 asignaturas"), eq(TOKEN_FUNCIONARIO));
    }

    @Test
    void enviarRespuestaRechazadaDejaTodasActivas() {
        conFilas(true, true, false);
        filas.forEach(fila -> fila.setAprobadaPorDecano(true));
        enEtapa(RECHAZADA_POR_DECANO);
        conEscaneo();

        casoDeUso.enviarRespuesta(SOLICITUD, TOKEN_FUNCIONARIO);

        assertEquals(RECHAZADA, etapaGuardada());
        verify(asignaturaSolicitudGateway, never()).cambiarEstadoAsignaturasMatriculadas(any(), any());
    }

    @Test
    void enviarRespuestaSinEscaneoEtapaEquivocadaORolEquivocadoNoCancelaNada() {
        falla(() -> casoDeUso.enviarRespuesta(SOLICITUD, TOKEN_DECANO));
        enEtapa(APROBADA_POR_DECANO);
        assertTrue(falla(() -> casoDeUso.enviarRespuesta(SOLICITUD, TOKEN_FUNCIONARIO)).getMessage().contains("escaneo"));
        conEscaneo();
        falla(() -> casoDeUso.enviarRespuesta(SOLICITUD, TOKEN_OTRO_FUNCIONARIO));
        enEtapa(EN_REVISION_DECANO);
        falla(() -> casoDeUso.enviarRespuesta(SOLICITUD, TOKEN_FUNCIONARIO));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void enviarRespuestaSiFallaCancelarPropagaElErrorParaElRollback() {
        conFilas(true, true, false);
        filas.get(0).setAprobadaPorDecano(true);
        enEtapa(APROBADA_POR_DECANO);
        conEscaneo();
        doThrow(new IllegalStateException("fallo")).when(asignaturaSolicitudGateway).cambiarEstadoAsignaturasMatriculadas(any(), any());

        assertThrows(IllegalStateException.class, () -> casoDeUso.enviarRespuesta(SOLICITUD, TOKEN_FUNCIONARIO));

        verify(log, never()).crearLog(eq("Cancelar asignaturas matriculadas"), any(), any());
    }
}
