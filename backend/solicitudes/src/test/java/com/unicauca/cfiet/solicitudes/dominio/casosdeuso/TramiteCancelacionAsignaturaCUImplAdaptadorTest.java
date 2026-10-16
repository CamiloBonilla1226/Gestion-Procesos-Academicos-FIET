package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
import com.unicauca.cfiet.solicitudes.dominio.servicios.NotificadorRespuestaFinal;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorMalFormatoExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorReglaNegocioVioladaExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
                .numeroFaltas(faltas == null ? null : String.valueOf(faltas))
                .nota(nota)
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

    private DecisionAsignatura decision(String uuid, Boolean aprobada) {
        return DecisionAsignatura.builder().uuidAsignaturaSolicitud(uuid).aprobada(aprobada).build();
    }

    private List<DecisionAsignatura> decisionesValidas() {
        return List.of(decision("as-1", true), decision("as-2", false), decision("as-3", false));
    }

    private AjusteAsignatura ajuste(String uuid, String situacion, String observacion) {
        return AjusteAsignatura.builder().uuidAsignaturaSolicitud(uuid).uuidSituacionCancelar(situacion).observacionDecision(observacion).build();
    }

    private void conSituacionGuardada(AsignaturaSolicitudAcademica... asignaturas) {
        for (AsignaturaSolicitudAcademica asignatura : asignaturas)
            asignatura.setSituacionCancelar(SituacionAcademicaAsignatura.builder().uuidSituacionAcademica("sit-a").codigo("R0").build());
    }

    private void decididas(Boolean aprobada1, Boolean aprobada2, Boolean aprobada3) {
        filas.get(0).setAprobadaPorDecano(aprobada1);
        filas.get(1).setAprobadaPorDecano(aprobada2);
        filas.get(2).setAprobadaPorDecano(aprobada3);
    }

    @BeforeEach
    void setUp() {
        ExcepcionesFormateadorImplAdaptador formateador = new ExcepcionesFormateadorImplAdaptador();
        MaquinaEtapas maquina = new MaquinaEtapas(formateador);
        SolicitudAcademicaCUImplAdaptador solicitudCU = new SolicitudAcademicaCUImplAdaptador(solicitudGateway, estudianteGateway,
                tipoSolicitudGateway, etapaGateway, resolucionGateway, anexoGateway, usuarioGateway, almacenamiento, maquina, formateador, log,
                Clock.fixed(Instant.parse("2026-10-06T15:00:00Z"), ZoneId.of("America/Bogota")), mock(CorreoIntPuerto.class),
                mock(NotificadorRespuestaFinal.class));
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

    private HistorialSolicitudAcademica historialGuardado() {
        ArgumentCaptor<HistorialSolicitudAcademica> captor = ArgumentCaptor.forClass(HistorialSolicitudAcademica.class);
        verify(solicitudGateway).actualizarEtapa(any(), captor.capture());
        return captor.getValue();
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
    void rechazarPorFuncionarioSinObservacionNoCambiaNada() {
        enEtapa(RADICADA);
        assertTrue(falla(() -> casoDeUso.rechazarPorFuncionario(SOLICITUD, "  ", TOKEN_FUNCIONARIO)).getMessage().contains("observación"));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void rechazarPorFuncionarioSinEscaneoPasaARechazada() {
        enEtapa(RADICADA);

        SolicitudCancelacionAsignatura resultado = casoDeUso.rechazarPorFuncionario(SOLICITUD, "No cumple", TOKEN_FUNCIONARIO);

        assertEquals(RECHAZADA, resultado.getSolicitudAcademica().getEtapa().getCodigo());
        verify(resolucionGateway, never()).eliminarPorSolicitud(any());
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
        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.enviarRespuesta(null, null, null, TOKEN_FUNCIONARIO));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void remitirGuardaLaEvaluacionConLasCondicionesYPasaAEnRevisionDecano() {
        enEtapa(RADICADA);

        SolicitudCancelacionAsignatura resultado = casoDeUso.remitirADecano(SOLICITUD, evaluacionesValidas(), null, TOKEN_FUNCIONARIO);

        assertEquals(EN_REVISION_DECANO, etapaGuardada());
        List<AsignaturaSolicitudAcademica> guardadas = asignaturasGuardadas();
        assertEquals(List.of(true, true, false), guardadas.stream().map(AsignaturaSolicitudAcademica::getCumpleCondiciones).toList());
        assertEquals("4.5", guardadas.get(0).getNota());
        assertEquals("3.0", guardadas.get(1).getNota());
        assertNull(guardadas.get(0).getObservacionEvaluacion());
        assertEquals("Verificado", guardadas.get(1).getObservacionEvaluacion());
        assertEquals("Nota promedio menor a 3.0", guardadas.get(2).getObservacionEvaluacion());
        assertEquals("sit-b", guardadas.get(2).getSituacionMatricula().getUuidSituacionAcademica());
        assertTrue(guardadas.stream().allMatch(a -> a.getAprobadaPorDecano() == null && a.getSituacionCancelar() == null));
        assertEquals(EN_REVISION_DECANO, resultado.getSolicitudAcademica().getEtapa().getCodigo());
        verify(log).crearLog(eq("Evaluar asignaturas de cancelación de asignatura"), contains("2 cumplen"), eq(TOKEN_FUNCIONARIO));
    }

    @Test
    void remitirGuardaLaSituacionAlCancelarSiElFuncionarioLaRegistra() {
        enEtapa(RADICADA);
        List<EvaluacionAsignatura> evaluaciones = evaluacionesValidas();
        evaluaciones.get(0).setUuidSituacionCancelar("sit-b");

        casoDeUso.remitirADecano(SOLICITUD, evaluaciones, null, TOKEN_FUNCIONARIO);

        List<AsignaturaSolicitudAcademica> guardadas = asignaturasGuardadas();
        assertEquals("sit-b", guardadas.get(0).getSituacionCancelar().getUuidSituacionAcademica());
        assertNull(guardadas.get(1).getSituacionCancelar());
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
    void remitirConNotaYFaltasDeTextoPermiteMarcarQueCumple() {
        enEtapa(RADICADA);

        casoDeUso.remitirADecano(SOLICITUD, List.of(evaluacion("as-1", 0, "Sin nota", "sit-a", true, null),
                evaluacion("as-2", 0, "2,5", "sit-a", false, "No alcanza"), evaluacion("as-3", 0, "4,0", "sit-a", true, null)), null, TOKEN_FUNCIONARIO);

        List<AsignaturaSolicitudAcademica> guardadas = asignaturasGuardadas();
        assertEquals("Sin nota", guardadas.get(0).getNota());
        assertTrue(guardadas.get(0).getCumpleCondiciones());
        assertEquals("2.5", guardadas.get(1).getNota());
        assertEquals("4.0", guardadas.get(2).getNota());
    }

    @Test
    void remitirConNotaComaMenorATresMarcadaComoCumpleSeRechaza() {
        enEtapa(RADICADA);

        ErrorReglaNegocioVioladaExcepcion error = falla(() -> casoDeUso.remitirADecano(SOLICITUD, List.of(evaluacion("as-1", 0, "4.0", "sit-a", true, null),
                evaluacion("as-2", 0, "2,9", "sit-a", true, null), evaluacion("as-3", 0, "4.0", "sit-a", true, null)), null, TOKEN_FUNCIONARIO));

        assertTrue(error.getMessage().contains("Física I"), error::getMessage);
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
    void remitirConUnaQueNoCumpleSinObservacionDeEvaluacionPasaAEnRevisionDecano() {
        enEtapa(RADICADA);

        casoDeUso.remitirADecano(SOLICITUD, List.of(evaluacion("as-1", 0, "4.0", "sit-a", true, null),
                evaluacion("as-2", 0, "4.0", "sit-a", true, null), evaluacion("as-3", 0, "4.0", "sit-a", false, "   ")), null, TOKEN_FUNCIONARIO);

        assertEquals(EN_REVISION_DECANO, etapaGuardada());
        List<AsignaturaSolicitudAcademica> guardadas = asignaturasGuardadas();
        assertFalse(guardadas.get(2).getCumpleCondiciones());
        assertNull(guardadas.get(2).getObservacionEvaluacion());
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
    void aprobarGuardaLaDecisionDeCadaAsignaturaYQuitaLaSituacionDeLasRechazadas() {
        conFilas(true, true, false);
        SituacionAcademicaAsignatura r2 = SituacionAcademicaAsignatura.builder().uuidSituacionAcademica("sit-c").codigo("R2").build();
        filas.forEach(fila -> fila.setSituacionCancelar(r2));
        enEtapa(EN_REVISION_DECANO);

        SolicitudCancelacionAsignatura resultado = casoDeUso.aprobarPorDecano(SOLICITUD, null, decisionesValidas(), null, TOKEN_DECANO);

        assertEquals(APROBADA_POR_DECANO, etapaGuardada());
        List<AsignaturaSolicitudAcademica> guardadas = asignaturasGuardadas();
        assertEquals(List.of(true, false, false), guardadas.stream().map(AsignaturaSolicitudAcademica::getAprobadaPorDecano).toList());
        assertEquals("sit-c", guardadas.get(0).getSituacionCancelar().getUuidSituacionAcademica());
        assertNull(guardadas.get(1).getSituacionCancelar());
        assertNull(guardadas.get(2).getSituacionCancelar());
        assertTrue(guardadas.stream().allMatch(a -> a.getObservacionDecision() == null));
        assertEquals(APROBADA_POR_DECANO, resultado.getSolicitudAcademica().getEtapa().getCodigo());
        verify(situacionGateway, never()).getTodas();
        verify(asignaturaSolicitudGateway, never()).cambiarEstadoAsignaturasMatriculadas(any(), any());
        verify(log).crearLog(eq("Registrar decisión del Decano por asignatura"), contains("1 asignaturas aprobadas y 2 rechazadas"), eq(TOKEN_DECANO));
    }

    @Test
    void aceptarLaEvaluacionApruebaExactamenteLasQueCumplen() {
        conFilas(true, false, true);
        enEtapa(EN_REVISION_DECANO);

        casoDeUso.aprobarPorDecano(SOLICITUD, true, null, null, TOKEN_DECANO);

        List<AsignaturaSolicitudAcademica> guardadas = asignaturasGuardadas();
        assertEquals(List.of(true, false, true), guardadas.stream().map(AsignaturaSolicitudAcademica::getAprobadaPorDecano).toList());
        assertTrue(guardadas.stream().allMatch(a -> Boolean.FALSE.equals(a.decisionCambiadaPorDecano())));
        assertEquals(APROBADA_POR_DECANO, etapaGuardada());
    }

    @Test
    void elDecanoPuedeAprobarUnaQueNoCumpleYRechazarUnaQueCumple() {
        conFilas(true, true, false);
        enEtapa(EN_REVISION_DECANO);

        casoDeUso.aprobarPorDecano(SOLICITUD, false, List.of(decision("as-1", true), decision("as-2", false), decision("as-3", true)),
                null, TOKEN_DECANO);

        List<AsignaturaSolicitudAcademica> guardadas = asignaturasGuardadas();
        assertEquals(List.of(true, false, true), guardadas.stream().map(AsignaturaSolicitudAcademica::getAprobadaPorDecano).toList());
        assertEquals(List.of(false, true, true), guardadas.stream().map(AsignaturaSolicitudAcademica::decisionCambiadaPorDecano).toList());
    }

    @Test
    void aprobarSinNingunaFormaOConLasDosSeRechaza() {
        conFilas(true, true, false);
        enEtapa(EN_REVISION_DECANO);

        assertTrue(falla(() -> casoDeUso.aprobarPorDecano(SOLICITUD, null, null, null, TOKEN_DECANO)).getMessage().contains("aceptarEvaluacion"));
        falla(() -> casoDeUso.aprobarPorDecano(SOLICITUD, false, List.of(), null, TOKEN_DECANO));
        falla(() -> casoDeUso.aprobarPorDecano(SOLICITUD, true, decisionesValidas(), null, TOKEN_DECANO));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void aprobarPorDecanoGuardaLaObservacionGeneralOpcionalEnElHistorial() {
        conFilas(true, true, false);
        enEtapa(EN_REVISION_DECANO);

        casoDeUso.aprobarPorDecano(SOLICITUD, null, decisionesValidas(), "Aprobada parcialmente", TOKEN_DECANO);

        assertEquals("Aprobada parcialmente", historialGuardado().getObservaciones());
        assertEquals("APROBAR_DECANO", historialGuardado().getAccion());
    }

    @Test
    void sinNingunaAsignaturaAprobadaIndicaUsarElRechazoDelDecano() {
        conFilas(true, true, false);
        enEtapa(EN_REVISION_DECANO);

        ErrorReglaNegocioVioladaExcepcion error = falla(() -> casoDeUso.aprobarPorDecano(SOLICITUD, null, List.of(decision("as-1", false),
                decision("as-2", false), decision("as-3", false)), null, TOKEN_DECANO));

        assertTrue(error.getMessage().contains("rechazo del Decano"), error::getMessage);
        verificarQueNoSeEscribioNada();
    }

    @Test
    void aprobarConUnaDecisionSinValorNoGuardaNada() {
        conFilas(true, true, false);
        enEtapa(EN_REVISION_DECANO);

        ErrorReglaNegocioVioladaExcepcion error = falla(() -> casoDeUso.aprobarPorDecano(SOLICITUD, null, List.of(decision("as-1", null),
                decision("as-2", true), decision("as-3", false)), null, TOKEN_DECANO));

        assertTrue(error.getMessage().contains("Cálculo I"), error::getMessage);
        verificarQueNoSeEscribioNada();
    }

    @Test
    void aprobarConCoberturaIncompletaRepetidaOAjenaNoGuardaNada() {
        conFilas(true, true, false);
        enEtapa(EN_REVISION_DECANO);
        List<DecisionAsignatura> validas = decisionesValidas();

        assertTrue(falla(() -> casoDeUso.aprobarPorDecano(SOLICITUD, null, validas.subList(0, 2), null, TOKEN_DECANO)).getMessage().contains("Química I"));
        List<DecisionAsignatura> repetida = new ArrayList<>(validas);
        repetida.add(decision("as-2", false));
        falla(() -> casoDeUso.aprobarPorDecano(SOLICITUD, null, repetida, null, TOKEN_DECANO));
        List<DecisionAsignatura> ajena = new ArrayList<>(validas);
        ajena.add(decision("as-9", true));
        falla(() -> casoDeUso.aprobarPorDecano(SOLICITUD, null, ajena, null, TOKEN_DECANO));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void aprobarConRolDeFuncionarioOEtapaEquivocadaSeRechaza() {
        conFilas(true, true, false);
        falla(() -> casoDeUso.aprobarPorDecano(SOLICITUD, null, decisionesValidas(), null, TOKEN_FUNCIONARIO));
        enEtapa(RADICADA);
        falla(() -> casoDeUso.aprobarPorDecano(SOLICITUD, true, null, null, TOKEN_DECANO));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void aprobarSiFallaGuardarLasDecisionesPropagaElErrorParaElRollback() {
        conFilas(true, true, false);
        enEtapa(EN_REVISION_DECANO);
        when(asignaturaSolicitudGateway.actualizarAsignaturas(any())).thenThrow(new IllegalStateException("fallo"));

        assertThrows(IllegalStateException.class, () -> casoDeUso.aprobarPorDecano(SOLICITUD, null, decisionesValidas(), null, TOKEN_DECANO));

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
    void rechazarPorDecanoConRolEquivocadoNoCambiaNada() {
        falla(() -> casoDeUso.rechazarPorDecano(SOLICITUD, "No", TOKEN_FUNCIONARIO));
        verificarQueNoSeEscribioNada();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rechazarPorDecanoSinObservacionTambienPasaARechazadaPorDecano(String observacion) {
        enEtapa(EN_REVISION_DECANO);

        casoDeUso.rechazarPorDecano(SOLICITUD, observacion, TOKEN_DECANO);

        assertEquals(RECHAZADA_POR_DECANO, etapaGuardada());
        assertNull(historialGuardado().getObservaciones());
    }

    @Test
    void enviarRespuestaGuardaElMensajeParaElEstudianteEnElHistorial() {
        conFilas(true, true, false);
        enEtapa(RECHAZADA_POR_DECANO);
        conEscaneo();

        casoDeUso.enviarRespuesta(SOLICITUD, " Revise la Resolucion adjunta ", null, TOKEN_FUNCIONARIO);

        assertEquals("Revise la Resolucion adjunta", historialGuardado().getObservaciones());
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
        conSituacionGuardada(aprobadaActiva, aprobadaYaPerdida, aprobadaActiva2);
        enEtapa(APROBADA_POR_DECANO);
        conEscaneo();

        SolicitudCancelacionAsignatura resultado = casoDeUso.enviarRespuesta(SOLICITUD, "Se cancelan tres asignaturas",
                List.of(ajuste("as-2", null, "El soporte no cubre Física I")), TOKEN_FUNCIONARIO);

        assertEquals(APROBADA, etapaGuardada());
        verify(asignaturaSolicitudGateway).cambiarEstadoAsignaturasMatriculadas(List.of("am-1", "am-4"), "cancelada");
        assertEquals(List.of("cancelada", "activa", "perdida", "cancelada"), resultado.getAsignaturas().stream()
                .map(a -> a.getAsignaturaMatriculada().getEstado()).toList());
        verify(log).crearLog(eq("Cancelar asignaturas matriculadas"), contains("2 asignaturas"), eq(TOKEN_FUNCIONARIO));
    }

    @Test
    void enviarRespuestaConLaEvaluacionAceptadaNoPideAjustesNiObservacion() {
        conFilas(true, true, false);
        decididas(true, true, false);
        filas.get(2).setAprobadaPorDecano(null);
        conSituacionGuardada(filas.get(0), filas.get(1));
        enEtapa(APROBADA_POR_DECANO);
        conEscaneo();

        casoDeUso.enviarRespuesta(SOLICITUD, null, null, TOKEN_FUNCIONARIO);

        assertEquals(APROBADA, etapaGuardada());
        verify(asignaturaSolicitudGateway, never()).actualizarAsignaturas(any());
        verify(asignaturaSolicitudGateway).cambiarEstadoAsignaturasMatriculadas(List.of("am-1", "am-2"), "cancelada");
    }

    @Test
    void enviarRespuestaGuardaLosAjustesDeLasDecisionesCambiadas() {
        conFilas(true, true, false);
        decididas(true, false, true);
        conSituacionGuardada(filas.get(0));
        enEtapa(APROBADA_POR_DECANO);
        conEscaneo();

        casoDeUso.enviarRespuesta(SOLICITUD, "Física I no se cancela", List.of(ajuste("as-2", null, "El Decano no la aprueba"),
                ajuste("as-3", "sit-c", null)), TOKEN_FUNCIONARIO);

        List<AsignaturaSolicitudAcademica> guardadas = asignaturasGuardadas();
        assertEquals("El Decano no la aprueba", guardadas.get(1).getObservacionDecision());
        assertNull(guardadas.get(1).getSituacionCancelar());
        assertEquals("sit-c", guardadas.get(2).getSituacionCancelar().getUuidSituacionAcademica());
        assertEquals("Física I no se cancela", historialGuardado().getObservaciones());
        verify(log).crearLog(eq("Ajustar asignaturas de cancelación de asignatura"), contains("2 asignaturas ajustadas"), eq(TOKEN_FUNCIONARIO));
        verify(asignaturaSolicitudGateway).cambiarEstadoAsignaturasMatriculadas(List.of("am-1", "am-3"), "cancelada");
    }

    @Test
    void enviarRespuestaSinElAjusteDeUnaDecisionCambiadaLaDejaSinSituacion() {
        conFilas(true, true, false);
        decididas(true, false, true);
        enEtapa(APROBADA_POR_DECANO);
        conEscaneo();

        casoDeUso.enviarRespuesta(SOLICITUD, "Respuesta", List.of(ajuste("as-2", null, "No procede")), TOKEN_FUNCIONARIO);

        assertEquals(APROBADA, etapaGuardada());
        assertNull(filas.get(2).getSituacionCancelar());
    }

    @Test
    void elAjusteDeUnaRechazadaNoAdmiteSituacionNiMasDe255() {
        conFilas(true, true, false);
        decididas(true, false, true);
        conSituacionGuardada(filas.get(0));
        enEtapa(APROBADA_POR_DECANO);
        conEscaneo();

        assertEquals(String.format(MensajesError.SITUACION_CANCELAR_RECHAZADA, "Física I"), falla(() -> casoDeUso.enviarRespuesta(SOLICITUD,
                "Respuesta", List.of(ajuste("as-2", "sit-c", "No"), ajuste("as-3", "sit-c", null)), TOKEN_FUNCIONARIO)).getMessage());
        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.enviarRespuesta(SOLICITUD, "Respuesta",
                List.of(ajuste("as-2", null, "x".repeat(256)), ajuste("as-3", "sit-c", null)), TOKEN_FUNCIONARIO));
        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.enviarRespuesta(SOLICITUD, "Respuesta",
                List.of(ajuste("as-2", null, "No"), ajuste("as-3", "sit-x", null)), TOKEN_FUNCIONARIO));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void unaRechazadaCambiadaNoExigeAjusteNiObservacionPropia() {
        conFilas(true, true, false);
        decididas(true, false, true);
        conSituacionGuardada(filas.get(0));
        enEtapa(APROBADA_POR_DECANO);
        conEscaneo();

        casoDeUso.enviarRespuesta(SOLICITUD, "Física I no se cancela", List.of(ajuste("as-3", "sit-c", null)), TOKEN_FUNCIONARIO);

        assertEquals(APROBADA, etapaGuardada());
        List<AsignaturaSolicitudAcademica> guardadas = asignaturasGuardadas();
        assertNull(guardadas.get(1).getObservacionDecision());
        assertEquals("sit-c", guardadas.get(2).getSituacionCancelar().getUuidSituacionAcademica());
    }

    @Test
    void elAjusteDeUnaRechazadaAceptaObservacionVaciaComoNula() {
        conFilas(true, true, false);
        decididas(true, false, true);
        conSituacionGuardada(filas.get(0));
        enEtapa(APROBADA_POR_DECANO);
        conEscaneo();

        casoDeUso.enviarRespuesta(SOLICITUD, "Física I no se cancela", List.of(ajuste("as-2", null, "  "), ajuste("as-3", "sit-c", null)),
                TOKEN_FUNCIONARIO);

        assertNull(asignaturasGuardadas().get(1).getObservacionDecision());
    }

    @Test
    void sinObservacionGeneralUnaRechazadaCambiadaSinAjusteSigueFallando() {
        conFilas(true, true, false);
        decididas(true, false, true);
        conSituacionGuardada(filas.get(0));
        enEtapa(APROBADA_POR_DECANO);
        conEscaneo();

        ErrorReglaNegocioVioladaExcepcion error = falla(() -> casoDeUso.enviarRespuesta(SOLICITUD, "  ",
                List.of(ajuste("as-3", "sit-c", null)), TOKEN_FUNCIONARIO));

        assertEquals(MensajesError.OBSERVACION_RESPUESTA_REQUERIDA, error.getMessage());
        verificarQueNoSeEscribioNada();
    }

    @Test
    void unAjusteAjenoORepetidoSeRechaza() {
        conFilas(true, true, false);
        decididas(true, true, false);
        enEtapa(APROBADA_POR_DECANO);
        conEscaneo();

        assertTrue(falla(() -> casoDeUso.enviarRespuesta(SOLICITUD, "Respuesta", List.of(ajuste("as-9", "sit-c", null)), TOKEN_FUNCIONARIO))
                .getMessage().contains("as-9"));
        assertTrue(falla(() -> casoDeUso.enviarRespuesta(SOLICITUD, "Respuesta", List.of(ajuste("as-1", "sit-c", null),
                ajuste("as-1", "sit-a", null)), TOKEN_FUNCIONARIO)).getMessage().contains("Cálculo I"));
        verificarQueNoSeEscribioNada();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void conAsignaturasRechazadasPorElDecanoLaObservacionGeneralEsObligatoria(String observacion) {
        conFilas(true, true, false);
        decididas(true, true, false);
        conSituacionGuardada(filas.get(0), filas.get(1));
        enEtapa(APROBADA_POR_DECANO);
        conEscaneo();

        ErrorReglaNegocioVioladaExcepcion error = falla(() -> casoDeUso.enviarRespuesta(SOLICITUD, observacion, null, TOKEN_FUNCIONARIO));

        assertEquals(MensajesError.OBSERVACION_RESPUESTA_REQUERIDA, error.getMessage());
        verificarQueNoSeEscribioNada();
    }

    @Test
    void conUnaRechazadaContraLaEvaluacionLaObservacionGeneralTambienEsObligatoria() {
        conFilas(true, true, false);
        decididas(true, false, false);
        conSituacionGuardada(filas.get(0));
        enEtapa(APROBADA_POR_DECANO);
        conEscaneo();

        ErrorReglaNegocioVioladaExcepcion error = falla(() -> casoDeUso.enviarRespuesta(SOLICITUD, null,
                List.of(ajuste("as-2", null, "El Decano no la aprueba")), TOKEN_FUNCIONARIO));

        assertEquals(MensajesError.OBSERVACION_RESPUESTA_REQUERIDA, error.getMessage());
        verificarQueNoSeEscribioNada();
    }

    @Test
    void unaAprobadaSinSituacionAlCancelarSeRespondeIgual() {
        conFilas(true, true, false);
        decididas(true, true, false);
        conSituacionGuardada(filas.get(0));
        enEtapa(APROBADA_POR_DECANO);
        conEscaneo();

        casoDeUso.enviarRespuesta(SOLICITUD, "Química I no procede", null, TOKEN_FUNCIONARIO);

        assertEquals(APROBADA, etapaGuardada());
        assertNull(filas.get(1).getSituacionCancelar());
        verify(asignaturaSolicitudGateway).cambiarEstadoAsignaturasMatriculadas(List.of("am-1", "am-2"), "cancelada");
    }

    @Test
    void laSituacionGuardadaAlRemitirBastaAunqueElAjusteNoLaTraiga() {
        conFilas(true, true, false);
        decididas(true, false, true);
        conSituacionGuardada(filas.get(0), filas.get(2));
        enEtapa(APROBADA_POR_DECANO);
        conEscaneo();

        casoDeUso.enviarRespuesta(SOLICITUD, "Física I no se cancela", List.of(ajuste("as-2", null, "No procede"),
                ajuste("as-3", null, "Aprobada por el Decano")), TOKEN_FUNCIONARIO);

        List<AsignaturaSolicitudAcademica> guardadas = asignaturasGuardadas();
        assertEquals("sit-a", guardadas.get(2).getSituacionCancelar().getUuidSituacionAcademica());
        assertEquals("Aprobada por el Decano", guardadas.get(2).getObservacionDecision());
        assertEquals(APROBADA, etapaGuardada());
    }

    @Test
    void conLaSolicitudRechazadaPorElDecanoLaObservacionEsObligatoriaYNoSeAceptanAjustes() {
        conFilas(true, true, false);
        enEtapa(RECHAZADA_POR_DECANO);
        conEscaneo();

        assertEquals(MensajesError.OBSERVACION_RESPUESTA_REQUERIDA, falla(() -> casoDeUso.enviarRespuesta(SOLICITUD, null, List.of(),
                TOKEN_FUNCIONARIO)).getMessage());
        assertTrue(falla(() -> casoDeUso.enviarRespuesta(SOLICITUD, "No procede", List.of(ajuste("as-1", "sit-c", null)),
                TOKEN_FUNCIONARIO)).getMessage().contains("2026-CA-0001"));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void enviarRespuestaRechazadaDejaTodasActivas() {
        conFilas(true, true, false);
        enEtapa(RECHAZADA_POR_DECANO);
        conEscaneo();

        casoDeUso.enviarRespuesta(SOLICITUD, "El Decano no aprueba la cancelación", null, TOKEN_FUNCIONARIO);

        assertEquals(RECHAZADA, etapaGuardada());
        verify(asignaturaSolicitudGateway, never()).cambiarEstadoAsignaturasMatriculadas(any(), any());
        verify(asignaturaSolicitudGateway, never()).actualizarAsignaturas(any());
    }

    @Test
    void enviarRespuestaSinEscaneoEtapaEquivocadaORolEquivocadoNoCancelaNada() {
        falla(() -> casoDeUso.enviarRespuesta(SOLICITUD, null, null, TOKEN_DECANO));
        enEtapa(APROBADA_POR_DECANO);
        assertTrue(falla(() -> casoDeUso.enviarRespuesta(SOLICITUD, null, null, TOKEN_FUNCIONARIO)).getMessage().contains("escaneo"));
        conEscaneo();
        falla(() -> casoDeUso.enviarRespuesta(SOLICITUD, null, null, TOKEN_OTRO_FUNCIONARIO));
        enEtapa(EN_REVISION_DECANO);
        falla(() -> casoDeUso.enviarRespuesta(SOLICITUD, null, null, TOKEN_FUNCIONARIO));
        verificarQueNoSeEscribioNada();
    }

    @Test
    void enviarRespuestaSiFallaCancelarPropagaElErrorParaElRollback() {
        conFilas(true, true, false);
        filas.get(0).setAprobadaPorDecano(true);
        conSituacionGuardada(filas.get(0));
        enEtapa(APROBADA_POR_DECANO);
        conEscaneo();
        doThrow(new IllegalStateException("fallo")).when(asignaturaSolicitudGateway).cambiarEstadoAsignaturasMatriculadas(any(), any());

        assertThrows(IllegalStateException.class, () -> casoDeUso.enviarRespuesta(SOLICITUD, null, null, TOKEN_FUNCIONARIO));

        verify(log, never()).crearLog(eq("Cancelar asignaturas matriculadas"), any(), any());
    }
}
