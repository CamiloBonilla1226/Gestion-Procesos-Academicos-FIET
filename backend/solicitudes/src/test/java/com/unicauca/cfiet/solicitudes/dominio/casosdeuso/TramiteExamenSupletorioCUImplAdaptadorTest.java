package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorReglaNegocioVioladaExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TramiteExamenSupletorioCUImplAdaptadorTest {

    private static final String SOLICITUD = "sol-1";
    private static final String TIPO_ES = "tipo-es";
    private static final String FUNCIONARIO = "fun-1";
    private static final String OTRO_FUNCIONARIO = "fun-2";
    private static final String DECANO = "dec-1";
    private static final String ESTUDIANTE = "est-1";
    private static final String OTRO_ESTUDIANTE = "est-2";
    private static final String TOKEN_FUNCIONARIO = "token-fun";
    private static final String TOKEN_OTRO_FUNCIONARIO = "token-fun2";
    private static final String TOKEN_DECANO = "token-dec";
    private static final String TOKEN_ESTUDIANTE = "token-est";
    private static final String TOKEN_OTRO_ESTUDIANTE = "token-est2";
    private static final LocalDate FECHA_EXAMEN = LocalDate.of(2026, 10, 5);

    @Mock
    private SolicitudExamenSupletorioGatewayIntPuerto gateway;

    @Mock
    private SolicitudAcademicaGatewayIntPuerto solicitudGateway;

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

    private TramiteExamenSupletorioCUImplAdaptador casoDeUso;
    private AsignaturaMatriculada asignatura;

    private final TipoSolicitudAcademica supletorio = TipoSolicitudAcademica.builder()
            .uuidTipoSolicitudAcademica(TIPO_ES)
            .nombre("Examen Supletorio")
            .funcionarioAcademico(FuncionarioAcademico.builder().uuidUsuario(FUNCIONARIO).build())
            .build();

    interface Accion {
        SolicitudExamenSupletorio ejecutar(TramiteExamenSupletorioCUImplAdaptador casoDeUso, String token);
    }

    private static Accion rechazarFuncionario(String observacion) {
        return (cu, token) -> cu.rechazarPorFuncionario(SOLICITUD, observacion, token);
    }

    private static Accion remitir(Boolean confirmado) {
        return (cu, token) -> cu.remitirADecano(SOLICITUD, confirmado, "Requisitos revisados", token);
    }

    private static Accion aprobarDecano() {
        return (cu, token) -> cu.aprobarPorDecano(SOLICITUD, null, token);
    }

    private static Accion rechazarDecano(String observacion) {
        return (cu, token) -> cu.rechazarPorDecano(SOLICITUD, observacion, token);
    }

    private static Accion enviarRespuesta() {
        return (cu, token) -> cu.enviarRespuesta(SOLICITUD, token);
    }

    private static Accion enviarRecibo() {
        return (cu, token) -> cu.enviarRecibo(SOLICITUD, token);
    }

    private static Accion subirComprobante() {
        return (cu, token) -> cu.subirComprobante(SOLICITUD, token);
    }

    private static Accion aprobarComprobante(LocalDate fecha) {
        return (cu, token) -> cu.aprobarComprobante(SOLICITUD, fecha, token);
    }

    private static Accion rechazarComprobante(String observacion) {
        return (cu, token) -> cu.rechazarComprobante(SOLICITUD, observacion, token);
    }

    static Stream<Arguments> acciones() {
        return Stream.of(
                Arguments.of("rechazarPorFuncionario", rechazarFuncionario("No cumple"), RADICADA, RECHAZADA, TOKEN_FUNCIONARIO, TOKEN_DECANO),
                Arguments.of("remitirADecano", remitir(true), RADICADA, EN_REVISION_DECANO, TOKEN_FUNCIONARIO, TOKEN_ESTUDIANTE),
                Arguments.of("aprobarPorDecano", aprobarDecano(), EN_REVISION_DECANO, APROBADA_POR_DECANO, TOKEN_DECANO, TOKEN_FUNCIONARIO),
                Arguments.of("rechazarPorDecano", rechazarDecano("No procede"), EN_REVISION_DECANO, RECHAZADA_POR_DECANO, TOKEN_DECANO, TOKEN_FUNCIONARIO),
                Arguments.of("enviarRespuesta", enviarRespuesta(), RECHAZADA_POR_DECANO, RECHAZADA, TOKEN_FUNCIONARIO, TOKEN_DECANO),
                Arguments.of("enviarRecibo", enviarRecibo(), APROBADA_POR_DECANO, PENDIENTE_PAGO, TOKEN_FUNCIONARIO, TOKEN_ESTUDIANTE),
                Arguments.of("subirComprobante", subirComprobante(), PENDIENTE_PAGO, EN_VERIFICACION_PAGO, TOKEN_ESTUDIANTE, TOKEN_FUNCIONARIO),
                Arguments.of("aprobarComprobante", aprobarComprobante(null), EN_VERIFICACION_PAGO, APROBADA, TOKEN_FUNCIONARIO, TOKEN_DECANO),
                Arguments.of("rechazarComprobante", rechazarComprobante("Comprobante ilegible"), EN_VERIFICACION_PAGO, RECHAZADA, TOKEN_FUNCIONARIO, TOKEN_ESTUDIANTE));
    }

    static Stream<Arguments> accionesDelFuncionario() {
        return acciones().filter(argumentos -> TOKEN_FUNCIONARIO.equals(argumentos.get()[4]));
    }

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

    private void enEtapa(String codigo) {
        lenient().when(solicitudGateway.getPorUuid(SOLICITUD)).thenReturn(SolicitudAcademica.builder()
                .uuidSolicitudAcademica(SOLICITUD)
                .radicado("2026-ES-0001")
                .estudiante(Estudiante.builder().uuidUsuario(ESTUDIANTE).build())
                .tipoSolicitudAcademica(supletorio)
                .etapa(EtapaSolicitudAcademica.builder().uuidEtapa("et-" + codigo).codigo(codigo).build())
                .build());
    }

    private AnexoAcademico anexoDeTipo(String nombre) {
        return AnexoAcademico.builder().uuidAnexoAcademico("an-" + nombre)
                .tipoAnexoAcademico(TipoAnexoAcademico.builder().uuidTipoAnexoAcademico("tipo-" + nombre).nombre(nombre).build()).build();
    }

    private void conAnexos(String... nombres) {
        lenient().when(anexoGateway.getPorSolicitud(SOLICITUD)).thenReturn(Stream.of(nombres).map(this::anexoDeTipo).toList());
    }

    @BeforeEach
    void setUp() {
        ExcepcionesFormateadorImplAdaptador formateador = new ExcepcionesFormateadorImplAdaptador();
        MaquinaEtapas maquina = new MaquinaEtapas(formateador);
        SolicitudAcademicaCUImplAdaptador solicitudCU = new SolicitudAcademicaCUImplAdaptador(solicitudGateway, estudianteGateway,
                tipoSolicitudGateway, etapaGateway, resolucionGateway, anexoGateway, usuarioGateway, almacenamiento, maquina, formateador, log,
                Clock.fixed(Instant.parse("2026-10-06T15:00:00Z"), ZoneId.of("America/Bogota")));
        casoDeUso = new TramiteExamenSupletorioCUImplAdaptador(gateway, solicitudGateway, usuarioGateway, sesionGateway, jwtServicio,
                solicitudCU, maquina, formateador, log);

        sesion(TOKEN_FUNCIONARIO, usuario(FUNCIONARIO, "Funcionario Académico"));
        sesion(TOKEN_OTRO_FUNCIONARIO, usuario(OTRO_FUNCIONARIO, "Funcionario Académico"));
        sesion(TOKEN_DECANO, usuario(DECANO, "Decano"));
        sesion(TOKEN_ESTUDIANTE, usuario(ESTUDIANTE, "Estudiante"));
        sesion(TOKEN_OTRO_ESTUDIANTE, usuario(OTRO_ESTUDIANTE, "Estudiante"));
        asignatura = AsignaturaMatriculada.builder().uuidAsignaturaMatriculada("am-1").grupo("A").estado("activa").build();
        lenient().when(gateway.getPorSolicitud(SOLICITUD)).thenAnswer(invocacion -> SolicitudExamenSupletorio.builder()
                .solicitudAcademica(SolicitudAcademica.builder().uuidSolicitudAcademica(SOLICITUD).build())
                .asignaturaMatriculada(asignatura)
                .fechaExamenNoPresentado(FECHA_EXAMEN)
                .tipoCausa(CausaSupletorio.OTRA)
                .build());
        lenient().when(etapaGateway.getPorTipoIncluyendoUniversales(TIPO_ES)).thenReturn(EtapaSolicitudAcademicaConstantes.ETAPAS.stream()
                .map(codigo -> EtapaSolicitudAcademica.builder().uuidEtapa("et-" + codigo).codigo(codigo).build())
                .toList());
        conAnexos("Recibo de pago", "Comprobante de pago");
        lenient().when(solicitudGateway.actualizarEtapa(any(), any())).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private void verificarQueNoSeEscribioNada() {
        verify(solicitudGateway, never()).actualizarEtapa(any(), any());
        verify(gateway, never()).actualizarFechaAcordada(any(), any());
        verify(resolucionGateway, never()).eliminarPorSolicitud(any());
        verify(log, never()).crearLog(any(), any(), any());
    }

    private String etapaGuardada() {
        ArgumentCaptor<SolicitudAcademica> captor = ArgumentCaptor.forClass(SolicitudAcademica.class);
        verify(solicitudGateway).actualizarEtapa(captor.capture(), any());
        return captor.getValue().getEtapa().getCodigo();
    }

    private HistorialSolicitudAcademica historialGuardado() {
        ArgumentCaptor<HistorialSolicitudAcademica> captor = ArgumentCaptor.forClass(HistorialSolicitudAcademica.class);
        verify(solicitudGateway).actualizarEtapa(any(), captor.capture());
        return captor.getValue();
    }

    private String falla(Accion accion, String token) {
        String mensaje = assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> accion.ejecutar(casoDeUso, token)).getMessage();
        verificarQueNoSeEscribioNada();
        return mensaje;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("acciones")
    void cadaAccionPasaALaEtapaSiguienteSinTocarLaAsignaturaNiLaResolucion(String nombre, Accion accion, String origen, String destino,
                                                                           String token, String tokenEquivocado) {
        enEtapa(origen);

        SolicitudExamenSupletorio resultado = accion.ejecutar(casoDeUso, token);

        assertEquals(destino, etapaGuardada());
        assertEquals(destino, resultado.getSolicitudAcademica().getEtapa().getCodigo());
        assertEquals("activa", resultado.getAsignaturaMatriculada().getEstado());
        assertEquals("activa", asignatura.getEstado());
        verify(resolucionGateway, never()).eliminarPorSolicitud(any());
        verify(almacenamiento, never()).eliminarTrasConfirmar(any());
        verify(gateway, never()).actualizarFechaAcordada(any(), any());
        verifyNoInteractions(estudianteGateway);
        verify(log).crearLog(eq("Cambiar etapa de solicitud académica"), contains(destino), eq(token));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("acciones")
    void cadaAccionConRolEquivocadoSeRechazaSinEscribir(String nombre, Accion accion, String origen, String destino,
                                                       String token, String tokenEquivocado) {
        enEtapa(origen);

        falla(accion, tokenEquivocado);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("acciones")
    void cadaAccionEnUnaEtapaEquivocadaSeRechazaSinEscribir(String nombre, Accion accion, String origen, String destino,
                                                           String token, String tokenEquivocado) {
        enEtapa(RADICADA.equals(origen) ? EN_REVISION_DECANO : RADICADA);

        falla(accion, token);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("acciones")
    void ningunaAccionSeAplicaAUnaSolicitudFinal(String nombre, Accion accion, String origen, String destino,
                                                String token, String tokenEquivocado) {
        enEtapa(APROBADA);

        falla(accion, token);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("accionesDelFuncionario")
    void unFuncionarioNoAsignadoSeRechazaEnCadaAccion(String nombre, Accion accion, String origen, String destino,
                                                     String token, String tokenEquivocado) {
        enEtapa(origen);

        assertTrue(falla(accion, TOKEN_OTRO_FUNCIONARIO).contains(OTRO_FUNCIONARIO));
    }

    @Test
    void unEstudianteAjenoNoPuedeSubirElComprobante() {
        enEtapa(PENDIENTE_PAGO);

        assertEquals(String.format(MensajesError.SOLICITUD_AJENA, "2026-ES-0001", OTRO_ESTUDIANTE), falla(subirComprobante(), TOKEN_OTRO_ESTUDIANTE));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void lasAccionesConObservacionObligatoriaLaExigen(String observacion) {
        enEtapa(RADICADA);
        assertTrue(falla(rechazarFuncionario(observacion), TOKEN_FUNCIONARIO).contains("observación"));
        enEtapa(EN_REVISION_DECANO);
        assertTrue(falla(rechazarDecano(observacion), TOKEN_DECANO).contains("observación"));
        enEtapa(EN_VERIFICACION_PAGO);
        assertTrue(falla(rechazarComprobante(observacion), TOKEN_FUNCIONARIO).contains("observación"));
    }

    @Test
    void lasObservacionesQuedanEnElHistorial() {
        enEtapa(EN_VERIFICACION_PAGO);

        casoDeUso.rechazarComprobante(SOLICITUD, "  El valor no coincide  ", TOKEN_FUNCIONARIO);

        assertEquals("El valor no coincide", historialGuardado().getObservaciones());
        assertEquals("RECHAZAR_COMPROBANTE", historialGuardado().getAccion());
    }

    @Test
    void elDecanoApruebaConOSinObservacion() {
        enEtapa(EN_REVISION_DECANO);

        casoDeUso.aprobarPorDecano(SOLICITUD, "Procede", TOKEN_DECANO);

        assertEquals("Procede", historialGuardado().getObservaciones());
        assertEquals(APROBADA_POR_DECANO, etapaGuardada());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = false)
    void remitirSinConfirmarLosRequisitosPideRechazarEnLugarDeRemitir(Boolean confirmado) {
        enEtapa(RADICADA);

        String mensaje = falla(remitir(confirmado), TOKEN_FUNCIONARIO);

        assertEquals(String.format(MensajesError.REQUISITOS_NO_CONFIRMADOS, "2026-ES-0001"), mensaje);
        assertTrue(mensaje.contains("debe rechazar la solicitud en lugar de remitirla"));
    }

    @Test
    void laConfirmacionSeValidaDespuesDeLaEtapa() {
        enEtapa(EN_REVISION_DECANO);

        assertFalse(falla(remitir(false), TOKEN_FUNCIONARIO).contains("en lugar de remitirla"));
    }

    @Test
    void enviarElReciboSinElReciboCargadoSeRechaza() {
        enEtapa(APROBADA_POR_DECANO);
        conAnexos("Formato PM-FO-4-FOR-23 firmado por el docente que orienta la asignatura", "Comprobante de pago");

        assertTrue(falla(enviarRecibo(), TOKEN_FUNCIONARIO).contains("el recibo de pago"));
    }

    @Test
    void subirElComprobanteSinElComprobanteCargadoSeRechaza() {
        enEtapa(PENDIENTE_PAGO);
        conAnexos("Recibo de pago");

        assertTrue(falla(subirComprobante(), TOKEN_ESTUDIANTE).contains("el comprobante de pago"));
    }

    @Test
    void elRechazoDelFuncionarioYLaRespuestaNoExigenEscaneo() {
        enEtapa(RADICADA);
        when(resolucionGateway.existePorSolicitud(SOLICITUD)).thenReturn(false);

        casoDeUso.rechazarPorFuncionario(SOLICITUD, "No cumple", TOKEN_FUNCIONARIO);

        assertEquals(RECHAZADA, etapaGuardada());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-10-05", "2026-10-20"})
    void aprobarElComprobanteGuardaLaFechaAcordadaSiNoEsAnteriorAlExamen(LocalDate fecha) {
        enEtapa(EN_VERIFICACION_PAGO);

        SolicitudExamenSupletorio resultado = casoDeUso.aprobarComprobante(SOLICITUD, fecha, TOKEN_FUNCIONARIO);

        assertEquals(APROBADA, etapaGuardada());
        verify(gateway).actualizarFechaAcordada(SOLICITUD, fecha.atStartOfDay());
        assertEquals(fecha.atStartOfDay(), resultado.getFechaAcordadaExamen());
        verify(log).crearLog(eq("Acordar fecha de examen supletorio"), contains(fecha.getDayOfMonth() + "/10/2026"), eq(TOKEN_FUNCIONARIO));
    }

    @Test
    void unaFechaAcordadaAnteriorAlExamenSeRechazaSinEscribir() {
        enEtapa(EN_VERIFICACION_PAGO);

        assertEquals(String.format(MensajesError.FECHA_ACORDADA_ANTERIOR, "04/10/2026", "05/10/2026"),
                falla(aprobarComprobante(LocalDate.of(2026, 10, 4)), TOKEN_FUNCIONARIO));
    }

    @Test
    void laFechaAcordadaNoSeValidaSiLaEtapaOElActorSonEquivocados() {
        enEtapa(PENDIENTE_PAGO);
        assertFalse(falla(aprobarComprobante(LocalDate.of(2026, 10, 4)), TOKEN_FUNCIONARIO).contains("fecha acordada"));
        enEtapa(EN_VERIFICACION_PAGO);
        falla(aprobarComprobante(LocalDate.of(2026, 10, 4)), TOKEN_DECANO);
    }

    @Test
    void siFallaGuardarLaFechaAcordadaElErrorSePropagaSinElLogDeLaFecha() {
        enEtapa(EN_VERIFICACION_PAGO);
        doThrow(new RuntimeException("falla de base")).when(gateway).actualizarFechaAcordada(any(), any());

        assertThrows(RuntimeException.class, () -> casoDeUso.aprobarComprobante(SOLICITUD, LocalDate.of(2026, 10, 20), TOKEN_FUNCIONARIO));

        verify(log, never()).crearLog(eq("Acordar fecha de examen supletorio"), any(), any());
    }

    @Test
    void siFallaElCambioDeEtapaNoSeGuardaLaFecha() {
        enEtapa(EN_VERIFICACION_PAGO);
        when(solicitudGateway.actualizarEtapa(any(), any())).thenThrow(new RuntimeException("falla de base"));

        assertThrows(RuntimeException.class, () -> casoDeUso.aprobarComprobante(SOLICITUD, LocalDate.of(2026, 10, 20), TOKEN_FUNCIONARIO));

        verify(gateway, never()).actualizarFechaAcordada(any(), any());
        verify(log, never()).crearLog(any(), any(), any());
    }

    @Test
    void enviarRespuestaDesdeAprobadaPorDecanoNoExisteEnElSupletorio() {
        enEtapa(APROBADA_POR_DECANO);

        falla(enviarRespuesta(), TOKEN_FUNCIONARIO);
    }

    @Test
    void unaSolicitudQueNoEsSupletorioRespondeComoInexistente() {
        when(gateway.getPorSolicitud("otra")).thenReturn(null);

        ErrorEntidadNoExisteExcepcion error = assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.enviarRecibo("otra", TOKEN_FUNCIONARIO));

        assertEquals(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, "Solicitud de examen supletorio", "otra"), error.getMessage());
        verificarQueNoSeEscribioNada();
    }

    @Test
    void unUsuarioSinElRolDeLaAccionSeRechazaAntesDeLeerLaSolicitud() {
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.subirComprobante(SOLICITUD, TOKEN_DECANO));

        verify(gateway, never()).getPorSolicitud(any());
        verificarQueNoSeEscribioNada();
    }
}
