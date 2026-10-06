package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AnexoAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.SolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorMalFormatoExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorReglaNegocioVioladaExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamenSupletorioCUImplAdaptadorTest {

    private static final String ESTUDIANTE = "est-1";
    private static final String TIPO_ES = "tipo-es";
    private static final String TOKEN = "token-jwt";
    private static final String SOLICITUD = "sol-1";
    private static final String FOR_23 = "an-for23";
    private static final String JUSTIFICACION = "an-just";
    private static final String DOCENTE_CRUCE = "an-doc";
    private static final String RECIBO = "an-recibo";
    private static final String COMPROBANTE = "an-comp";
    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final LocalDate LUNES = LocalDate.of(2026, 10, 5);
    private static final LocalDate JUEVES = LocalDate.of(2026, 10, 8);

    @Mock
    private SolicitudExamenSupletorioGatewayIntPuerto gateway;

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

    private ExamenSupletorioCUImplAdaptador casoDeUso;

    private final TipoSolicitudAcademica supletorio = TipoSolicitudAcademica.builder()
            .uuidTipoSolicitudAcademica(TIPO_ES).nombre("Examen Supletorio").build();
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

    private TipoAnexoAcademico tipoAnexo(String uuid, String nombre, String formatos, boolean obligatorio) {
        return TipoAnexoAcademico.builder().uuidTipoAnexoAcademico(uuid).tipoSolicitudAcademica(supletorio)
                .nombre(nombre).formatosPermitidos(formatos).obligatorio(obligatorio).build();
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

    private AnexoRadicacion anexo(String uuidTipo) {
        return anexo(uuidTipo, uuidTipo + ".pdf", pdf());
    }

    private List<AnexoRadicacion> anexosCruce() {
        return List.of(anexo(FOR_23), anexo(DOCENTE_CRUCE));
    }

    private List<AnexoRadicacion> anexosOtra() {
        return List.of(anexo(FOR_23), anexo(JUSTIFICACION));
    }

    private void hoyEs(LocalDate hoy) {
        casoDeUso = new ExamenSupletorioCUImplAdaptador(gateway, estudianteGateway, usuarioGateway, tipoSolicitudGateway, tipoAnexoGateway,
                solicitudCU, anexoCU, new ExcepcionesFormateadorImplAdaptador(), log,
                Clock.fixed(hoy.atTime(23, 59).atZone(BOGOTA).toInstant(), BOGOTA));
    }

    private SolicitudExamenSupletorio radicarOtra(LocalDate fechaExamen) {
        return casoDeUso.radicarExamenSupletorio(ESTUDIANTE, "am-1", fechaExamen, "otra", null, null, null, anexosOtra(), TOKEN);
    }

    private SolicitudExamenSupletorio radicarCruce(String cruzada, LocalDate fechaCruzada, String hora, List<AnexoRadicacion> anexos) {
        return casoDeUso.radicarExamenSupletorio(ESTUDIANTE, "am-1", LUNES, "cruce", cruzada, fechaCruzada, hora, anexos, TOKEN);
    }

    @BeforeEach
    void setUp() {
        hoyEs(JUEVES);
        lenient().when(usuarioGateway.getUsuario(ESTUDIANTE)).thenReturn(usuario(ESTUDIANTE, "Estudiante"));
        lenient().when(estudianteGateway.getPorUuid(ESTUDIANTE)).thenReturn(Estudiante.builder().uuidUsuario(ESTUDIANTE).build());
        lenient().when(estudianteGateway.getAsignaturasMatriculadas(ESTUDIANTE)).thenReturn(List.of(
                materia("am-1", "Cálculo I", "activa"),
                materia("am-2", "Física I", "activa"),
                materia("am-3", "Álgebra", "cancelada")));
        lenient().when(tipoSolicitudGateway.getTodos()).thenReturn(List.of(matricula, supletorio));
        lenient().when(tipoAnexoGateway.getPorTipo(TIPO_ES)).thenReturn(List.of(
                tipoAnexo(JUSTIFICACION, "Soporte de la justificación de la no presentación", "pdf,jpg,png", false),
                tipoAnexo(DOCENTE_CRUCE, "Formato firmado por el docente de la asignatura con la que se cruza", "pdf,jpg,png", false),
                tipoAnexo(RECIBO, "Recibo de pago", "pdf", false),
                tipoAnexo(COMPROBANTE, "Comprobante de pago", "pdf,jpg,png", false),
                tipoAnexo(FOR_23, "Formato PM-FO-4-FOR-23 firmado por el docente que orienta la asignatura", "pdf,jpg,png", true)));
        lenient().when(solicitudCU.crearSolicitud(ESTUDIANTE, TIPO_ES, TOKEN)).thenReturn(SolicitudAcademica.builder()
                .uuidSolicitudAcademica(SOLICITUD).radicado("2026-ES-0001").tipoSolicitudAcademica(supletorio).build());
        lenient().when(gateway.guardar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private void verificarQueNoSeGuardoNada() {
        verify(solicitudCU, never()).crearSolicitud(any(), any(), any());
        verify(gateway, never()).guardar(any());
        verify(anexoCU, never()).adjuntarAnexo(any(), any(), any(), any(), any());
        verify(log, never()).crearLog(any(), any(), any());
    }

    private SolicitudExamenSupletorio guardada() {
        ArgumentCaptor<SolicitudExamenSupletorio> captor = ArgumentCaptor.forClass(SolicitudExamenSupletorio.class);
        verify(gateway).guardar(captor.capture());
        return captor.getValue();
    }

    private String reglaViolada(Runnable accion) {
        String mensaje = assertThrows(ErrorReglaNegocioVioladaExcepcion.class, accion::run).getMessage();
        verificarQueNoSeGuardoNada();
        return mensaje;
    }

    private String malFormato(Runnable accion) {
        String mensaje = assertThrows(ErrorMalFormatoExcepcion.class, accion::run).getMessage();
        verificarQueNoSeGuardoNada();
        return mensaje;
    }

    @Test
    void radicacionPorCruceGuardaSolicitudEspecializacionCruceAnexosYLogEnOrden() {
        SolicitudExamenSupletorio radicada = radicarCruce(" am-2 ", LocalDate.of(2026, 10, 5), " 08:30 ", anexosCruce());

        InOrder orden = inOrder(solicitudCU, gateway, anexoCU, log);
        orden.verify(solicitudCU).verificarSinSolicitudEnCurso(ESTUDIANTE, supletorio);
        orden.verify(solicitudCU).crearSolicitud(ESTUDIANTE, TIPO_ES, TOKEN);
        orden.verify(gateway).guardar(any());
        orden.verify(anexoCU).adjuntarAnexo(eq(SOLICITUD), eq(FOR_23), any(), argThat(actor ->
                ESTUDIANTE.equals(actor.getUuidUsuario()) && actor.getRol() == RolEtiquetaEtapa.ESTUDIANTE), eq(TOKEN));
        orden.verify(anexoCU).adjuntarAnexo(eq(SOLICITUD), eq(DOCENTE_CRUCE), any(), any(), eq(TOKEN));
        orden.verify(log).crearLog(eq("Radicar examen supletorio"), contains("2026-ES-0001"), eq(TOKEN));

        SolicitudExamenSupletorio guardada = guardada();
        assertEquals(SOLICITUD, guardada.getSolicitudAcademica().getUuidSolicitudAcademica());
        assertEquals("am-1", guardada.getAsignaturaMatriculada().getUuidAsignaturaMatriculada());
        assertEquals(LUNES, guardada.getFechaExamenNoPresentado());
        assertEquals(CausaSupletorio.CRUCE, guardada.getTipoCausa());
        assertNull(guardada.getFechaAcordadaExamen());
        assertEquals("am-2", guardada.getCruce().getAsignaturaMatriculadaCruzada().getUuidAsignaturaMatriculada());
        assertEquals(LocalDate.of(2026, 10, 5), guardada.getCruce().getFechaExamenCruzada());
        assertEquals("08:30", guardada.getCruce().getHoraExamenCruzada());
        assertEquals("2026-ES-0001", radicada.getSolicitudAcademica().getRadicado());
    }

    @ParameterizedTest
    @ValueSource(strings = {"otra", "OTRA", " Otra "})
    void radicacionPorOtraCausaGuardaSinCruceYConElSoporteDeJustificacion(String causa) {
        casoDeUso.radicarExamenSupletorio(ESTUDIANTE, "am-1", LUNES, causa, null, null, " ", anexosOtra(), TOKEN);

        SolicitudExamenSupletorio guardada = guardada();
        assertEquals(CausaSupletorio.OTRA, guardada.getTipoCausa());
        assertNull(guardada.getCruce());
        assertNull(guardada.getFechaAcordadaExamen());
        verify(anexoCU).adjuntarAnexo(eq(SOLICITUD), eq(FOR_23), any(), any(), eq(TOKEN));
        verify(anexoCU).adjuntarAnexo(eq(SOLICITUD), eq(JUSTIFICACION), any(), any(), eq(TOKEN));
        verify(log).crearLog(eq("Radicar examen supletorio"), contains("causa otra con 2 anexos"), eq(TOKEN));
    }

    @Test
    void unUsuarioSinRolEstudianteSeRechaza() {
        when(usuarioGateway.getUsuario(ESTUDIANTE)).thenReturn(usuario(ESTUDIANTE, "Decano"));

        String mensaje = reglaViolada(() -> radicarOtra(LUNES));

        assertEquals(String.format(MensajesError.ACTOR_SIN_ROL, ESTUDIANTE, "Estudiante"), mensaje);
        verify(solicitudCU, never()).verificarSinSolicitudEnCurso(any(), any());
    }

    @Test
    void unEstudianteSinFilaEnEstudianteSeRechaza() {
        when(estudianteGateway.getPorUuid(ESTUDIANTE)).thenReturn(null);

        reglaViolada(() -> radicarOtra(LUNES));
    }

    @Test
    void unUsuarioQueNoExisteSeRechaza() {
        when(usuarioGateway.getUsuario(ESTUDIANTE)).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> radicarOtra(LUNES));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void conUnaSolicitudEnCursoSeRechazaAntesDeValidarLaAsignatura() {
        doThrow(new ErrorReglaNegocioVioladaExcepcion("Ya tienes una solicitud de Examen Supletorio en curso (2026-ES-0001)"))
                .when(solicitudCU).verificarSinSolicitudEnCurso(ESTUDIANTE, supletorio);

        String mensaje = reglaViolada(() -> casoDeUso.radicarExamenSupletorio(ESTUDIANTE, "ajena", LUNES, "otra", null, null, null, anexosOtra(), TOKEN));

        assertTrue(mensaje.contains("en curso"));
        verify(estudianteGateway, never()).getAsignaturasMatriculadas(any());
    }

    @Test
    void sinTipoExamenSupletorioFalla() {
        when(tipoSolicitudGateway.getTodos()).thenReturn(List.of(matricula));

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> radicarOtra(LUNES));
        verificarQueNoSeGuardoNada();
    }

    @ParameterizedTest
    @ValueSource(strings = {"am-de-otro", "no-existe"})
    void unaAsignaturaAjenaOInexistenteDaElMismoMensaje(String uuid) {
        String mensaje = reglaViolada(() -> casoDeUso.radicarExamenSupletorio(ESTUDIANTE, uuid, LUNES, "otra", null, null, null, anexosOtra(), TOKEN));

        assertEquals(String.format(MensajesError.ASIGNATURA_NO_MATRICULADA, uuid, ESTUDIANTE), mensaje);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void sinAsignaturaDelExamenSeRechaza(String uuid) {
        String mensaje = reglaViolada(() -> casoDeUso.radicarExamenSupletorio(ESTUDIANTE, uuid, LUNES, "otra", null, null, null, anexosOtra(), TOKEN));

        assertEquals(MensajesError.ASIGNATURA_SUPLETORIO_REQUERIDA, mensaje);
    }

    @Test
    void unaAsignaturaNoActivaSeRechazaConSuNombreYEstado() {
        String mensaje = reglaViolada(() -> casoDeUso.radicarExamenSupletorio(ESTUDIANTE, "am-3", LUNES, "otra", null, null, null, anexosOtra(), TOKEN));

        assertEquals(String.format(MensajesError.ASIGNATURA_SUPLETORIO_NO_ACTIVA, "SIS-am-3 - Álgebra", "cancelada"), mensaje);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"enfermedad", "cruces"})
    void unaCausaInvalidaSeRechaza(String causa) {
        String mensaje = reglaViolada(() -> casoDeUso.radicarExamenSupletorio(ESTUDIANTE, "am-1", LUNES, causa, null, null, null, anexosOtra(), TOKEN));

        assertEquals(String.format(MensajesError.CAUSA_SUPLETORIO_INVALIDA, causa), mensaje);
    }

    @Test
    void sinFechaDelExamenSeRechaza() {
        assertEquals(MensajesError.FECHA_EXAMEN_REQUERIDA, reglaViolada(() -> radicarOtra(null)));
    }

    @Test
    void unaFechaFuturaSeRechazaConAmbasFechas() {
        String mensaje = reglaViolada(() -> radicarOtra(JUEVES.plusDays(1)));

        assertEquals(String.format(MensajesError.FECHA_EXAMEN_FUTURA, "09/10/2026", "08/10/2026"), mensaje);
    }

    @ParameterizedTest
    @CsvSource({
            "2026-10-05, 2026-10-05",
            "2026-10-05, 2026-10-06",
            "2026-10-05, 2026-10-08",
            "2026-10-03, 2026-10-07",
            "2026-10-04, 2026-10-07",
            "2026-10-01, 2026-10-06",
            "2026-10-02, 2026-10-03",
            "2026-10-02, 2026-10-07"
    })
    void dentroDelPlazoDeTresDiasHabilesSeAcepta(LocalDate examen, LocalDate hoy) {
        hoyEs(hoy);

        radicarOtra(examen);

        assertEquals(examen, guardada().getFechaExamenNoPresentado());
    }

    @ParameterizedTest
    @CsvSource({
            "2026-10-05, 2026-10-09, 05/10/2026, 08/10/2026",
            "2026-10-03, 2026-10-08, 03/10/2026, 07/10/2026",
            "2026-10-04, 2026-10-08, 04/10/2026, 07/10/2026",
            "2026-10-01, 2026-10-07, 01/10/2026, 06/10/2026",
            "2026-10-02, 2026-10-08, 02/10/2026, 07/10/2026",
            "2026-09-21, 2026-10-08, 21/09/2026, 24/09/2026"
    })
    void fueraDelPlazoSeRechazaConLaFechaDelExamenYElUltimoDiaPermitido(LocalDate examen, LocalDate hoy, String fechaExamen, String ultimoDia) {
        hoyEs(hoy);

        String mensaje = reglaViolada(() -> radicarOtra(examen));

        assertEquals(String.format(MensajesError.PLAZO_SUPLETORIO_VENCIDO, fechaExamen, ultimoDia), mensaje);
    }

    @Test
    void elPlazoSeValidaAntesQueLosDatosDeCruce() {
        hoyEs(LocalDate.of(2026, 10, 9));

        String mensaje = reglaViolada(() -> radicarCruce(null, null, null, anexosCruce()));

        assertTrue(mensaje.startsWith("El plazo para pedir el supletorio"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void conCausaCruceLaAsignaturaCruzadaEsObligatoria(String cruzada) {
        assertEquals(MensajesError.ASIGNATURA_CRUZADA_REQUERIDA, reglaViolada(() -> radicarCruce(cruzada, LUNES, "08:00", anexosCruce())));
    }

    @Test
    void laAsignaturaCruzadaDebeSerDistintaDeLaDelExamen() {
        assertEquals(MensajesError.ASIGNATURA_CRUZADA_IGUAL, reglaViolada(() -> radicarCruce(" am-1", LUNES, "08:00", anexosCruce())));
    }

    @Test
    void laAsignaturaCruzadaAjenaDaElMensajeDeNoMatriculada() {
        assertEquals(String.format(MensajesError.ASIGNATURA_NO_MATRICULADA, "am-ajena", ESTUDIANTE),
                reglaViolada(() -> radicarCruce("am-ajena", LUNES, "08:00", anexosCruce())));
    }

    @Test
    void laAsignaturaCruzadaNoActivaSeRechaza() {
        assertEquals(String.format(MensajesError.ASIGNATURA_SUPLETORIO_NO_ACTIVA, "SIS-am-3 - Álgebra", "cancelada"),
                reglaViolada(() -> radicarCruce("am-3", LUNES, "08:00", anexosCruce())));
    }

    @Test
    void conCausaCruceLaFechaDelExamenCruzadoEsObligatoria() {
        assertEquals(MensajesError.DATOS_CRUCE_REQUERIDOS, reglaViolada(() -> radicarCruce("am-2", null, "08:00", anexosCruce())));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void conCausaCruceLaHoraDelExamenCruzadoEsObligatoria(String hora) {
        assertEquals(MensajesError.DATOS_CRUCE_REQUERIDOS, reglaViolada(() -> radicarCruce("am-2", LUNES, hora, anexosCruce())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"8:00", "24:00", "07:60", "07h30", "0730", "ab:cd", "07:30:00"})
    void unaHoraQueNoEsHHmmSeRechaza(String hora) {
        assertEquals(String.format(MensajesError.HORA_CRUCE_MAL_FORMADA, hora), malFormato(() -> radicarCruce("am-2", LUNES, hora, anexosCruce())));
    }

    @Test
    void unaHoraDeMasDeDiezCaracteresSeRechaza() {
        assertEquals(String.format(MensajesError.HORA_CRUCE_MUY_LARGA, 10), malFormato(() -> radicarCruce("am-2", LUNES, "07:30:00 pm", anexosCruce())));
    }

    @Test
    void conCausaOtraNoSeAceptanDatosDeCruce() {
        assertEquals(MensajesError.DATOS_CRUCE_NO_PERMITIDOS, reglaViolada(() -> casoDeUso.radicarExamenSupletorio(
                ESTUDIANTE, "am-1", LUNES, "otra", "am-2", null, null, anexosOtra(), TOKEN)));
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.radicarExamenSupletorio(
                ESTUDIANTE, "am-1", LUNES, "otra", null, LUNES, null, anexosOtra(), TOKEN));
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.radicarExamenSupletorio(
                ESTUDIANTE, "am-1", LUNES, "otra", null, null, "08:00", anexosOtra(), TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void elFor23EsObligatorioConCualquierCausa() {
        String nombreFor23 = "Formato PM-FO-4-FOR-23 firmado por el docente que orienta la asignatura";
        assertEquals(String.format(MensajesError.ANEXOS_OBLIGATORIOS_FALTANTES, nombreFor23),
                reglaViolada(() -> radicarCruce("am-2", LUNES, "08:00", List.of(anexo(DOCENTE_CRUCE)))));
        assertEquals(String.format(MensajesError.ANEXOS_OBLIGATORIOS_FALTANTES, nombreFor23),
                reglaViolada(() -> casoDeUso.radicarExamenSupletorio(ESTUDIANTE, "am-1", LUNES, "otra", null, null, null,
                        List.of(anexo(JUSTIFICACION)), TOKEN)));
    }

    @Test
    void conCausaCruceFaltaElFormatoDelDocenteDeLaAsignaturaCruzada() {
        assertEquals(String.format(MensajesError.ANEXOS_OBLIGATORIOS_FALTANTES, "Formato firmado por el docente de la asignatura con la que se cruza"),
                reglaViolada(() -> radicarCruce("am-2", LUNES, "08:00", List.of(anexo(FOR_23)))));
    }

    @Test
    void conCausaOtraFaltaElSoporteDeLaJustificacionYSinAnexosSeNombranAmbos() {
        assertEquals(String.format(MensajesError.ANEXOS_OBLIGATORIOS_FALTANTES, "Soporte de la justificación de la no presentación"),
                reglaViolada(() -> casoDeUso.radicarExamenSupletorio(ESTUDIANTE, "am-1", LUNES, "otra", null, null, null,
                        List.of(anexo(FOR_23)), TOKEN)));
        assertEquals(String.format(MensajesError.ANEXOS_OBLIGATORIOS_FALTANTES,
                        "Formato PM-FO-4-FOR-23 firmado por el docente que orienta la asignatura; Soporte de la justificación de la no presentación"),
                reglaViolada(() -> casoDeUso.radicarExamenSupletorio(ESTUDIANTE, "am-1", LUNES, "otra", null, null, null, null, TOKEN)));
    }

    @Test
    void conCausaCruceNoSeAceptaElSoporteDeJustificacion() {
        assertEquals(String.format(MensajesError.ANEXO_NO_CORRESPONDE_CAUSA, "Soporte de la justificación de la no presentación", "cruce"),
                reglaViolada(() -> radicarCruce("am-2", LUNES, "08:00", List.of(anexo(FOR_23), anexo(DOCENTE_CRUCE), anexo(JUSTIFICACION)))));
    }

    @Test
    void conCausaOtraNoSeAceptaElFormatoDelDocenteDeLaAsignaturaCruzada() {
        assertEquals(String.format(MensajesError.ANEXO_NO_CORRESPONDE_CAUSA, "Formato firmado por el docente de la asignatura con la que se cruza", "otra"),
                reglaViolada(() -> casoDeUso.radicarExamenSupletorio(ESTUDIANTE, "am-1", LUNES, "otra", null, null, null,
                        List.of(anexo(FOR_23), anexo(JUSTIFICACION), anexo(DOCENTE_CRUCE)), TOKEN)));
    }

    @ParameterizedTest
    @CsvSource({"an-recibo, Recibo de pago", "an-comp, Comprobante de pago"})
    void elReciboYElComprobanteNoSeEntreganAlRadicar(String uuidTipo, String nombre) {
        assertEquals(String.format(MensajesError.ANEXO_NO_PERMITIDO_AL_RADICAR, nombre),
                reglaViolada(() -> casoDeUso.radicarExamenSupletorio(ESTUDIANTE, "am-1", LUNES, "otra", null, null, null,
                        List.of(anexo(FOR_23), anexo(JUSTIFICACION), anexo(uuidTipo)), TOKEN)));
    }

    @Test
    void unAnexoRepetidoSeRechaza() {
        assertEquals(String.format(MensajesError.ANEXO_REPETIDO, "Formato PM-FO-4-FOR-23 firmado por el docente que orienta la asignatura"),
                reglaViolada(() -> casoDeUso.radicarExamenSupletorio(ESTUDIANTE, "am-1", LUNES, "otra", null, null, null,
                        List.of(anexo(FOR_23), anexo(JUSTIFICACION), anexo(FOR_23)), TOKEN)));
    }

    @Test
    void unAnexoDeOtroProcesoSeRechazaConSuNombre() {
        when(tipoAnexoGateway.getPorUuid("an-paz")).thenReturn(TipoAnexoAcademico.builder().uuidTipoAnexoAcademico("an-paz")
                .nombre("Paz y salvo - División Financiera").tipoSolicitudAcademica(matricula).build());

        assertEquals(String.format(MensajesError.ANEXO_DE_OTRO_TIPO, "Paz y salvo - División Financiera", "Examen Supletorio"),
                reglaViolada(() -> casoDeUso.radicarExamenSupletorio(ESTUDIANTE, "am-1", LUNES, "otra", null, null, null,
                        List.of(anexo(FOR_23), anexo(JUSTIFICACION), anexo("an-paz")), TOKEN)));
    }

    @Test
    void unTipoDeAnexoInexistenteSeRechaza() {
        when(tipoAnexoGateway.getPorUuid("an-x")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.radicarExamenSupletorio(ESTUDIANTE, "am-1", LUNES, "otra", null, null, null,
                List.of(anexo(FOR_23), anexo(JUSTIFICACION), anexo("an-x")), TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void unSoporteLibreSinTipoNoSeAcepta(String uuidTipo) {
        assertEquals(String.format(MensajesError.SOPORTE_LIBRE_NO_PERMITIDO, "Examen Supletorio"),
                reglaViolada(() -> casoDeUso.radicarExamenSupletorio(ESTUDIANTE, "am-1", LUNES, "otra", null, null, null,
                        List.of(anexo(FOR_23), anexo(JUSTIFICACION), anexo(uuidTipo, "libre.pdf", pdf())), TOKEN)));
    }

    @Test
    void unArchivoConFormatoNoPermitidoSeRechazaConElNombreDelAnexo() {
        String mensaje = malFormato(() -> casoDeUso.radicarExamenSupletorio(ESTUDIANTE, "am-1", LUNES, "otra", null, null, null,
                List.of(anexo(FOR_23), anexo(JUSTIFICACION, "justificacion.docx", pdf())), TOKEN));

        assertEquals(String.format(MensajesError.FORMATO_ANEXO_NO_PERMITIDO, "docx", "Soporte de la justificación de la no presentación", "pdf,jpg,png"), mensaje);
    }

    @Test
    void unArchivoVacioMuyGrandeOConContenidoFalsoSeRechaza() {
        malFormato(() -> radicarCruce("am-2", LUNES, "08:00", List.of(anexo(FOR_23, "for23.pdf", new byte[0]), anexo(DOCENTE_CRUCE))));
        byte[] grande = Arrays.copyOf(pdf(), 5 * 1024 * 1024 + 1);
        malFormato(() -> radicarCruce("am-2", LUNES, "08:00", List.of(anexo(FOR_23), anexo(DOCENTE_CRUCE, "docente.pdf", grande))));
        assertEquals(String.format(MensajesError.CONTENIDO_ANEXO_NO_COINCIDE, "png"),
                malFormato(() -> radicarCruce("am-2", LUNES, "08:00", List.of(anexo(FOR_23), anexo(DOCENTE_CRUCE, "docente.png", pdf())))));
    }

    @Test
    void siFallaElSegundoAnexoElErrorSePropagaSinEscribirElLog() {
        lenient().when(anexoCU.adjuntarAnexo(eq(SOLICITUD), eq(DOCENTE_CRUCE), any(), any(), eq(TOKEN))).thenThrow(new RuntimeException("disco lleno"));

        RuntimeException error = assertThrows(RuntimeException.class, () -> radicarCruce("am-2", LUNES, "08:00", anexosCruce()));

        assertEquals("disco lleno", error.getMessage());
        verify(anexoCU).adjuntarAnexo(eq(SOLICITUD), eq(FOR_23), any(), any(), eq(TOKEN));
        verify(log, never()).crearLog(any(), any(), any());
    }

    @Test
    void siFallaGuardarLaEspecializacionNoSeSubenAnexos() {
        when(gateway.guardar(any())).thenThrow(new RuntimeException("falla de base"));

        assertThrows(RuntimeException.class, () -> radicarOtra(LUNES));

        verify(anexoCU, never()).adjuntarAnexo(any(), any(), any(), any(), any());
        verify(log, never()).crearLog(any(), any(), any());
    }
}
