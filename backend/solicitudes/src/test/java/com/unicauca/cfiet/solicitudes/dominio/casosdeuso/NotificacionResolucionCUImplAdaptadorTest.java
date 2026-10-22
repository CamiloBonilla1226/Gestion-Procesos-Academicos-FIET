package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ResolucionAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.ComunicacionResolucion;
import com.unicauca.cfiet.solicitudes.dominio.servicios.NotificadorRespuestaFinal;
import com.unicauca.cfiet.solicitudes.infraestructura.output.correo.PreparacionCorreoImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorMalFormatoExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorReglaNegocioVioladaExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionResolucionCUImplAdaptadorTest {

    private static final String SOLICITUD = "sol-1";
    private static final String RADICADO = "2026-CA-0007";
    private static final String FUNCIONARIO = "fa-1";
    private static final String OTRO_FUNCIONARIO = "fa-2";
    private static final String ESTUDIANTE = "est-1";
    private static final String DECANO = "dec-1";
    private static final String CORREO_ESTUDIANTE = "ana@unicauca.edu.co";
    private static final String DARCA = "darca@unicauca.edu.co";
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 20, 10, 0);
    private static final byte[] PDF = {37, 80, 68, 70};

    @Mock
    private NotificacionResolucionGatewayIntPuerto gateway;

    @Mock
    private SolicitudAcademicaGatewayIntPuerto solicitudGateway;

    @Mock
    private SolicitudCancelacionAsignaturaGatewayIntPuerto cancelacionGateway;

    @Mock
    private SesionGatewayIntPuerto sesionGateway;

    @Mock
    private IJwtServicio jwtServicio;

    @Mock
    private ResolucionAcademicaCUIntPuerto resolucionCU;

    @Mock
    private ResolucionAcademicaGatewayIntPuerto resolucionGateway;

    @Mock
    private AnexoAcademicoGatewayIntPuerto anexoGateway;

    @Mock
    private CorreoIntPuerto correo;

    @Mock
    private LogCUIntPuerto log;

    private final List<ConstanciaComunicacion> constancias = new ArrayList<>();
    private final List<Runnable> diferidas = new ArrayList<>();
    private NotificacionResolucionCUImplAdaptador casoDeUso;

    private final GeneradorPdfRespuestaIntPuerto generador = new GeneradorPdfRespuestaIntPuerto() {
        @Override
        public byte[] generar(ResumenRespuesta resumen) {
            return PDF;
        }

        @Override
        public byte[] generarConstancia(ConstanciaComunicacion constancia) {
            constancias.add(constancia);
            return PDF;
        }
    };

    @BeforeEach
    void setUp() {
        ExcepcionesFormateadorImplAdaptador formateador = new ExcepcionesFormateadorImplAdaptador();
        NotificadorRespuestaFinal notificador = new NotificadorRespuestaFinal(cancelacionGateway, mock(SolicitudExamenSupletorioGatewayIntPuerto.class),
                resolucionGateway, anexoGateway, mock(UsuarioGatewayIntPuerto.class), generador, new PreparacionCorreoImplAdaptador(), correo, DARCA);
        FirmaCorreo firma = FirmaCorreo.builder().nombre("CRISTIAN ESTIBEN ILLERA OSPITIA").cargo("Apoyo Administrativo")
                .dependencia("Decanatura").facultad("Facultad de Ingeniería Electrónica y Telecomunicaciones").build();
        casoDeUso = new NotificacionResolucionCUImplAdaptador(gateway, solicitudGateway, cancelacionGateway, sesionGateway, jwtServicio,
                resolucionCU, notificador, generador, correo, new ComunicacionResolucion(firma, "/178"), formateador, log,
                Clock.fixed(Instant.parse("2026-10-20T15:00:00Z"), ZoneId.of("America/Bogota")), (descripcion, tarea) -> diferidas.add(tarea));
        sesion(FUNCIONARIO, "Funcionario Académico");
        sesion(OTRO_FUNCIONARIO, "Funcionario Académico");
        sesion(ESTUDIANTE, "Estudiante");
        sesion(DECANO, "Decano");
        lenient().when(solicitudGateway.getPorUuid(SOLICITUD)).thenReturn(solicitud("Cancelación de Asignatura", APROBADA));
        lenient().when(anexoGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of());
        lenient().when(resolucionGateway.getPorSolicitud(SOLICITUD))
                .thenReturn(ResolucionAcademica.builder().urlArchivo("/api/anexos/sol-1/res.pdf").build());
        lenient().when(cancelacionGateway.getPorSolicitud(SOLICITUD)).thenReturn(SolicitudCancelacionAsignatura.builder()
                .asignaturas(new ArrayList<>(List.of(asignatura("ISI-001", true), asignatura("ISI-002", false)))).build());
    }

    private void sesion(String uuid, String rol) {
        lenient().when(jwtServicio.getUsername("tok-" + uuid)).thenReturn("user-" + uuid);
        lenient().when(sesionGateway.getUsuario("user-" + uuid)).thenReturn(Usuario.builder().uuidUsuario(uuid)
                .roles(new ArrayList<>(List.of(Rol.builder().uuidRol("rol-" + rol).nombre(rol).estado(true).build()))).build());
    }

    private String token(String uuid) {
        return "tok-" + uuid;
    }

    private SolicitudAcademica solicitud(String tipo, String etapa) {
        return SolicitudAcademica.builder()
                .uuidSolicitudAcademica(SOLICITUD)
                .radicado(RADICADO)
                .estudiante(Estudiante.builder().uuidUsuario(ESTUDIANTE).codigoEstudiantil("104618021")
                        .usuario(Usuario.builder().uuidUsuario(ESTUDIANTE).nombres("Ana María").apellidos("Muñoz Peña")
                                .correoElectronico(CORREO_ESTUDIANTE).build())
                        .build())
                .tipoSolicitudAcademica(TipoSolicitudAcademica.builder().uuidTipoSolicitudAcademica("tipo").nombre(tipo)
                        .funcionarioAcademico(FuncionarioAcademico.builder().uuidUsuario(FUNCIONARIO).build()).build())
                .etapa(EtapaSolicitudAcademica.builder().codigo(etapa).build())
                .build();
    }

    private AsignaturaSolicitudAcademica asignatura(String codigo, Boolean aprobada) {
        return AsignaturaSolicitudAcademica.builder()
                .uuidAsignaturaSolicitud("as-" + codigo)
                .asignaturaMatriculada(AsignaturaMatriculada.builder()
                        .asignatura(Asignatura.builder().codigoAsignatura(codigo).nombreAsignatura("Asignatura " + codigo).build()).build())
                .aprobadaPorDecano(aprobada)
                .build();
    }

    private ProgramacionResolucion programacion(LocalDateTime fecha) {
        return ProgramacionResolucion.builder().fechaProgramada(fecha).numeroResolucion(" 123-26 ").anioResolucion(2026).decision("aprueba parcialmente la").build();
    }

    private ComunicacionRespuesta comunicacion(Boolean enviar, String mensaje, ProgramacionResolucion programacion) {
        return ComunicacionRespuesta.builder().enviarMensaje(enviar).mensaje(mensaje).programacion(programacion).build();
    }

    private NotificacionResolucion registro(boolean firmada) {
        return NotificacionResolucion.builder()
                .uuidSolicitudAcademica(SOLICITUD)
                .fechaProgramada(AHORA.minusMinutes(1))
                .estado(EstadoNotificacionResolucion.EN_PROCESO)
                .firmada(firmada)
                .numeroResolucion("123-26")
                .anioResolucion(2026)
                .decision("aprueba parcialmente la")
                .build();
    }

    private void pendiente(NotificacionResolucion registro) {
        when(gateway.getPendientes(AHORA, AHORA.minusMinutes(15))).thenReturn(List.of(SOLICITUD));
        when(gateway.reclamar(SOLICITUD, AHORA, AHORA.minusMinutes(15))).thenReturn(true);
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(registro);
    }

    private String mensaje(Runnable accion) {
        return assertThrows(ErrorReglaNegocioVioladaExcepcion.class, accion::run).getMessage();
    }

    @Test
    void sinProgramacionNoSePuedeEnviarLaRespuesta() {
        assertEquals(MensajesError.PROGRAMACION_RESOLUCION_REQUERIDA, mensaje(() -> casoDeUso.validarComunicacion(null)));
        assertEquals(MensajesError.PROGRAMACION_RESOLUCION_REQUERIDA, mensaje(() -> casoDeUso.validarComunicacion(comunicacion(false, null, null))));
    }

    @Test
    void laFechaProgramadaEsObligatoriaYDebeSerFuturaEnHoraDeColombia() {
        assertEquals(MensajesError.FECHA_PROGRAMADA_REQUERIDA, mensaje(() -> casoDeUso.validarComunicacion(comunicacion(false, "Hola", programacion(null)))));
        assertTrue(mensaje(() -> casoDeUso.validarComunicacion(comunicacion(false, "Hola", programacion(AHORA))))
                .contains("20 de octubre de 2026 a las 10:00"));
        assertTrue(mensaje(() -> casoDeUso.validarComunicacion(comunicacion(false, "Hola", programacion(AHORA.minusDays(1))))).contains("posteriores"));
        assertDoesNotThrow(() -> casoDeUso.validarComunicacion(comunicacion(false, "Hola", programacion(AHORA.plusMinutes(1)))));
    }

    @Test
    void numeroAnioYDecisionSonObligatoriosYAcotados() {
        ProgramacionResolucion sinNumero = programacion(AHORA.plusDays(1));
        sinNumero.setNumeroResolucion(" ");
        assertEquals(MensajesError.NUMERO_RESOLUCION_REQUERIDO, mensaje(() -> casoDeUso.validarComunicacion(comunicacion(false, "Hola", sinNumero))));
        ProgramacionResolucion numeroLargo = programacion(AHORA.plusDays(1));
        numeroLargo.setNumeroResolucion("1".repeat(21));
        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.validarComunicacion(comunicacion(false, "Hola", numeroLargo)));
        ProgramacionResolucion sinAnio = programacion(AHORA.plusDays(1));
        sinAnio.setAnioResolucion(null);
        assertEquals(String.format(MensajesError.ANIO_RESOLUCION_INVALIDO, 2000, 2100),
                mensaje(() -> casoDeUso.validarComunicacion(comunicacion(false, "Hola", sinAnio))));
        ProgramacionResolucion sinDecision = programacion(AHORA.plusDays(1));
        sinDecision.setDecision("");
        assertEquals(MensajesError.DECISION_RESOLUCION_REQUERIDA, mensaje(() -> casoDeUso.validarComunicacion(comunicacion(false, "Hola", sinDecision))));
    }

    @Test
    void elMensajeInicialSiempreEsObligatorioAunqueLaCasillaVengaDesmarcadaYTieneMaximoDosMil() {
        ProgramacionResolucion valida = programacion(AHORA.plusDays(1));
        assertEquals(MensajesError.MENSAJE_INICIAL_REQUERIDO, mensaje(() -> casoDeUso.validarComunicacion(comunicacion(true, "  ", valida))));
        assertEquals(MensajesError.MENSAJE_INICIAL_REQUERIDO, mensaje(() -> casoDeUso.validarComunicacion(comunicacion(false, null, valida))));
        assertEquals(MensajesError.MENSAJE_INICIAL_REQUERIDO, mensaje(() -> casoDeUso.validarComunicacion(comunicacion(null, "", valida))));
        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.validarComunicacion(comunicacion(true, "x".repeat(2001), valida)));
        assertDoesNotThrow(() -> casoDeUso.validarComunicacion(comunicacion(true, "x".repeat(2000), valida)));
        assertDoesNotThrow(() -> casoDeUso.validarComunicacion(comunicacion(false, "x", valida)));
    }

    @Test
    void losTextosDeLaComunicacionYDeDarcaSonOpcionalesPeroSiLleganNoVanVaciosNiPasanDeCuatroMil() {
        ComunicacionRespuesta vacio = comunicacion(true, "Hola", programacion(AHORA.plusDays(1)));
        vacio.setTextoComunicacion(" \n ");
        assertEquals(String.format(MensajesError.TEXTO_COMUNICACION_REQUERIDO, "El texto de la comunicación al estudiante"),
                mensaje(() -> casoDeUso.validarComunicacion(vacio)));
        ComunicacionRespuesta darcaVacio = comunicacion(true, "Hola", programacion(AHORA.plusDays(1)));
        darcaVacio.setTextoDarca("");
        assertEquals(String.format(MensajesError.TEXTO_COMUNICACION_REQUERIDO, "El texto del correo a DARCA"),
                mensaje(() -> casoDeUso.validarComunicacion(darcaVacio)));
        ComunicacionRespuesta largo = comunicacion(true, "Hola", programacion(AHORA.plusDays(1)));
        largo.setTextoDarca("x".repeat(4001));
        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.validarComunicacion(largo));
        ComunicacionRespuesta justo = comunicacion(true, "Hola", programacion(AHORA.plusDays(1)));
        justo.setTextoComunicacion("x".repeat(4000));
        justo.setTextoDarca("x".repeat(4000));
        assertDoesNotThrow(() -> casoDeUso.validarComunicacion(justo));
    }

    @Test
    void programarGuardaElRegistroYEnviaElMensajeInicialSinAdjuntosConElTextoEditado() {
        when(gateway.crear(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        NotificacionResolucion creada = casoDeUso.programar(solicitud("Cancelación de Asignatura", APROBADA),
                comunicacion(true, "Buen día,\r\n<b>venga</b> a la Decanatura\n", programacion(AHORA.plusDays(2))), "token");

        assertEquals(EstadoNotificacionResolucion.PROGRAMADA, creada.getEstado());
        assertEquals("123-26", creada.getNumeroResolucion());
        assertEquals("Buen día,\n<b>venga</b> a la Decanatura", creada.getMensajeInicial());
        assertFalse(creada.isFirmada());
        assertNull(creada.getFechaLiberacion());
        assertTrue(creada.isEditable());
        assertTrue(creada.getTextoComunicacion().contains("RESOLUCIÓN No. {numero}/178 de {anio}"));
        assertTrue(creada.getTextoComunicacion().contains("Se comunica al estudiante, {nombre}, del texto"));
        assertTrue(creada.getTextoComunicacion().endsWith("Cordialmente,\n\nCRISTIAN ESTIBEN ILLERA OSPITIA\nApoyo Administrativo\nDecanatura\n"
                + "Facultad de Ingeniería Electrónica y Telecomunicaciones"));
        assertTrue(creada.getTextoDarca().startsWith("Popayán, Cauca, {fecha}\n\nEl estudiante Ana María Muñoz Peña, con código 104618021, "
                + "solicita cancelar 2 asignaturas: 1 aprobada y 1 rechazada."));
        assertTrue(creada.getTextoDarca().contains("se adjunta la constancia de la comunicación."));
        assertTrue(creada.getTextoDarca().endsWith("La información de la respuesta está en el PDF adjunto."));
        verify(correo).enviar(CORREO_ESTUDIANTE, "Respuesta a su solicitud de cancelación de asignatura - Radicado " + RADICADO,
                "Buen día,\n<b>venga</b> a la Decanatura", List.of());
        verify(log).crearLog(eq("Programar comunicación de la Resolución"), contains("123-26/178 de 2026"), eq("token"));
    }

    @Test
    void elMensajeInicialSaleSiempreAunqueLaCasillaVengaDesmarcada() {
        when(gateway.crear(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        NotificacionResolucion creada = casoDeUso.programar(solicitud("Cancelación de Asignatura", RECHAZADA),
                comunicacion(false, "Acérquese a la Decanatura", programacion(AHORA.plusDays(2))), "token");

        assertEquals("Acérquese a la Decanatura", creada.getMensajeInicial());
        verify(correo).enviar(eq(CORREO_ESTUDIANTE), any(), eq("Acérquese a la Decanatura"), eq(List.of()));
    }

    @Test
    void programarGuardaLosTextosEditadosNormalizados() {
        when(gateway.crear(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
        ComunicacionRespuesta datos = comunicacion(true, "Hola", programacion(AHORA.plusDays(2)));
        datos.setTextoComunicacion("  Resolución {numero} de {anio}\r\npara {nombre}: se {decision}  ");
        datos.setTextoDarca("Popayán, {fecha}\r\n\r\nTexto editado para DARCA");

        NotificacionResolucion creada = casoDeUso.programar(solicitud("Cancelación de Asignatura", RECHAZADA), datos, "token");

        assertEquals("Resolución {numero} de {anio}\npara {nombre}: se {decision}", creada.getTextoComunicacion());
        assertEquals("Popayán, {fecha}\n\nTexto editado para DARCA", creada.getTextoDarca());
    }

    @Test
    void laPlantillaTraeLaFirmaDeLasPropiedadesYLaDecisionPropuestaSegunElDecano() {
        PlantillaNotificacionResolucion plantilla = casoDeUso.getPlantilla(SOLICITUD, token(FUNCIONARIO));

        assertTrue(plantilla.getMensajeInicial().startsWith("Buen día, estudiante,\n\nDando respuesta"));
        assertTrue(plantilla.getMensajeInicial().endsWith("Cordialmente,\n\nCRISTIAN ESTIBEN ILLERA OSPITIA\nApoyo Administrativo\nDecanatura\n"
                + "Facultad de Ingeniería Electrónica y Telecomunicaciones"));
        assertFalse(plantilla.getMensajeInicial().contains("[FIRMA]"));
        assertEquals("aprueba parcialmente la", plantilla.getDecisionPropuesta());
        assertEquals(2026, plantilla.getAnioResolucion());
        assertEquals("/178", plantilla.getSufijoResolucion());
        assertTrue(plantilla.getTextoComunicacion().contains("RESOLUCIÓN No. {numero}/178 de {anio}"));
        assertFalse(plantilla.getTextoComunicacion().contains("{firma}"));
        assertEquals("Ana María Muñoz Peña", plantilla.getNombreEstudiante());
        assertTrue(plantilla.getTextoDarca().startsWith("Popayán, Cauca, {fecha}"));
    }

    @Test
    void laPlantillaDeDarcaResumeLaDecisionFinalAntesDeEnviarLaRespuesta() {
        when(solicitudGateway.getPorUuid(SOLICITUD)).thenReturn(solicitud("Cancelación de Asignatura", APROBADA_POR_DECANO));
        assertTrue(casoDeUso.getPlantilla(SOLICITUD, token(FUNCIONARIO)).getTextoDarca()
                .contains("solicita cancelar 2 asignaturas: 1 aprobada y 1 rechazada. La Resolución se comunicó al estudiante"));

        when(solicitudGateway.getPorUuid(SOLICITUD)).thenReturn(solicitud("Cancelación de Asignatura", RECHAZADA_POR_DECANO));
        assertTrue(casoDeUso.getPlantilla(SOLICITUD, token(FUNCIONARIO)).getTextoDarca()
                .contains("solicita cancelar 2 asignaturas: 0 aprobadas y 2 rechazadas."));
    }

    @Test
    void soloElFuncionarioAsignadoDeUnaCancelacionDeAsignaturaVeOCambiaLaProgramacion() {
        for (String ajeno : List.of(OTRO_FUNCIONARIO, ESTUDIANTE, DECANO)) {
            assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getNotificacion(SOLICITUD, token(ajeno)), ajeno);
            assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getPlantilla(SOLICITUD, token(ajeno)), ajeno);
            assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.reprogramar(SOLICITUD, programacion(AHORA.plusDays(1)), token(ajeno)), ajeno);
            assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.registrarFirma(SOLICITUD, null, null, token(ajeno)), ajeno);
            assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.reintentar(SOLICITUD, token(ajeno)), ajeno);
        }
        when(solicitudGateway.getPorUuid(SOLICITUD)).thenReturn(solicitud("Cancelación de Matrícula", APROBADA));
        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getNotificacion(SOLICITUD, token(FUNCIONARIO)));
        verify(gateway, never()).actualizarProgramacion(any(), any(), any());
        verify(gateway, never()).marcarFirmada(any(), any(), any());
        verify(gateway, never()).reintentar(any());
        verifyNoInteractions(resolucionCU);
    }

    @Test
    void elPanelDiceSiTodaviaSePuedeEditar() {
        NotificacionResolucion futura = registro(false);
        futura.setEstado(EstadoNotificacionResolucion.PROGRAMADA);
        futura.setFechaProgramada(AHORA.plusHours(1));
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(futura);
        assertTrue(casoDeUso.getNotificacion(SOLICITUD, token(FUNCIONARIO)).isEditable());

        futura.setFirmada(true);
        assertFalse(casoDeUso.getNotificacion(SOLICITUD, token(FUNCIONARIO)).isEditable());

        futura.setFirmada(false);
        futura.setFechaProgramada(AHORA.minusMinutes(1));
        assertFalse(casoDeUso.getNotificacion(SOLICITUD, token(FUNCIONARIO)).isEditable());
        assertEquals("/178", casoDeUso.getNotificacion(SOLICITUD, token(FUNCIONARIO)).getSufijoResolucion());
    }

    @Test
    void sinProgramacionElPanelNoTraeNada() {
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(null);
        assertNull(casoDeUso.getNotificacion(SOLICITUD, token(FUNCIONARIO)));
    }

    @Test
    void reprogramarActualizaConCondicionDeEstadoYFecha() {
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(registro(false));
        when(gateway.actualizarProgramacion(eq(SOLICITUD), any(), eq(AHORA))).thenReturn(true);

        casoDeUso.reprogramar(SOLICITUD, programacion(AHORA.plusDays(3)), token(FUNCIONARIO));

        ArgumentCaptor<ProgramacionResolucion> captor = ArgumentCaptor.forClass(ProgramacionResolucion.class);
        verify(gateway).actualizarProgramacion(eq(SOLICITUD), captor.capture(), eq(AHORA));
        assertEquals("123-26", captor.getValue().getNumeroResolucion());
        assertEquals(AHORA.plusDays(3), captor.getValue().getFechaProgramada());
    }

    @Test
    void despuesDeLaFechaONoProgramadaNoSeEditaNiSeRegistraLaFirma() {
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(registro(false));
        when(gateway.actualizarProgramacion(any(), any(), any())).thenReturn(false);
        when(gateway.marcarFirmada(eq(SOLICITUD), any(), eq(AHORA))).thenReturn(false);

        assertEquals(String.format(MensajesError.NOTIFICACION_NO_EDITABLE, RADICADO),
                mensaje(() -> casoDeUso.reprogramar(SOLICITUD, programacion(AHORA.plusDays(1)), token(FUNCIONARIO))));
        assertEquals(String.format(MensajesError.NOTIFICACION_NO_EDITABLE, RADICADO),
                mensaje(() -> casoDeUso.registrarFirma(SOLICITUD, ArchivoAdjunto.builder().build(), null, token(FUNCIONARIO))));
        verifyNoInteractions(resolucionCU);
        verify(log, never()).crearLog(any(), any(), any());
    }

    @Test
    void reprogramarAUnaFechaPasadaSeRechazaSinTocarElRegistro() {
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(registro(false));

        assertTrue(mensaje(() -> casoDeUso.reprogramar(SOLICITUD, programacion(AHORA.minusHours(1)), token(FUNCIONARIO))).contains("posteriores"));
        verify(gateway, never()).actualizarProgramacion(any(), any(), any());
    }

    @Test
    void laFirmaCierraLaComunicacionConElTextoDeDarcaEditadoYReemplazaLaResolucion() {
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(registro(false));
        when(gateway.marcarFirmada(eq(SOLICITUD), any(), eq(AHORA))).thenReturn(true);
        ArchivoAdjunto archivo = ArchivoAdjunto.builder().nombreOriginal("firmada.pdf").contenido(PDF).build();

        casoDeUso.registrarFirma(SOLICITUD, archivo, " Popayán, {fecha}\r\nFirmó en la Decanatura ", token(FUNCIONARIO));

        verify(gateway).marcarFirmada(SOLICITUD, "Popayán, {fecha}\nFirmó en la Decanatura", AHORA);
        ArgumentCaptor<ActorSolicitud> actor = ArgumentCaptor.forClass(ActorSolicitud.class);
        verify(resolucionCU).reemplazarResolucionFirmada(eq(SOLICITUD), same(archivo), actor.capture(), eq(token(FUNCIONARIO)));
        assertEquals(FUNCIONARIO, actor.getValue().getUuidUsuario());
        assertEquals(RolEtiquetaEtapa.FUNCIONARIO, actor.getValue().getRol());
        verify(log).crearLog(eq("Registrar firma de la Resolución"), contains("queda cerrada y se notifica a DARCA"), eq(token(FUNCIONARIO)));
        assertEquals(1, diferidas.size());
        verifyNoInteractions(correo);
    }

    @Test
    void sinTextoNuevoLaFirmaUsaElTextoGuardadoConLaNotaDeLaFirma() {
        NotificacionResolucion guardada = registro(false);
        guardada.setTextoDarca("Popayán, Cauca, {fecha}\n\nResumen. La Resolución se comunicó al estudiante por correo electrónico; "
                + "se adjunta la constancia de la comunicación.\n\nPie");
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(guardada);
        when(gateway.marcarFirmada(eq(SOLICITUD), any(), eq(AHORA))).thenReturn(true);

        casoDeUso.registrarFirma(SOLICITUD, ArchivoAdjunto.builder().build(), null, token(FUNCIONARIO));

        verify(gateway).marcarFirmada(SOLICITUD, "Popayán, Cauca, {fecha}\n\nResumen. El estudiante firmó la Resolución en la Decanatura; "
                + "se adjunta la Resolución firmada.\n\nPie", AHORA);
    }

    @Test
    void laFirmaRechazaUnTextoDeDarcaVacio() {
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(registro(false));

        assertEquals(String.format(MensajesError.TEXTO_COMUNICACION_REQUERIDO, "El texto del correo a DARCA"),
                mensaje(() -> casoDeUso.registrarFirma(SOLICITUD, ArchivoAdjunto.builder().build(), "  ", token(FUNCIONARIO))));
        verify(gateway, never()).marcarFirmada(any(), any(), any());
        verifyNoInteractions(resolucionCU);
    }

    @Test
    void trasConfirmarLaFirmaDarcaRecibeDeInmediatoDosPdfConElTextoEditadoYElEstudianteNada() {
        NotificacionResolucion firmada = registro(true);
        firmada.setEstado(EstadoNotificacionResolucion.PROGRAMADA);
        firmada.setFechaProgramada(AHORA.plusDays(2));
        firmada.setTextoDarca("Popayán, Cauca, {fecha}\n\nEl estudiante firmó.");
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(registro(false), firmada);
        when(gateway.marcarFirmada(eq(SOLICITUD), any(), eq(AHORA))).thenReturn(true);
        casoDeUso.registrarFirma(SOLICITUD, ArchivoAdjunto.builder().build(), "Popayán, Cauca, {fecha}\n\nEl estudiante firmó.", token(FUNCIONARIO));
        when(gateway.reclamar(SOLICITUD, AHORA, AHORA.minusMinutes(15))).thenReturn(true);

        diferidas.get(0).run();

        ArgumentCaptor<CorreoPreparado> enviados = ArgumentCaptor.forClass(CorreoPreparado.class);
        verify(correo, times(1)).enviarAhora(enviados.capture());
        assertEquals(DARCA, enviados.getValue().getDestinatario());
        assertEquals("Popayán, Cauca, 20 de octubre de 2026\n\nEl estudiante firmó.", enviados.getValue().getTexto());
        assertEquals(List.of("Radicado-" + RADICADO + ".pdf", "Resolucion-" + RADICADO + ".pdf"),
                enviados.getValue().getAdjuntos().stream().map(AdjuntoCorreo::getNombre).toList());
        verify(gateway, never()).marcarEnvioEstudiante(any(), any());
        verify(gateway).marcarEnvioDarca(SOLICITUD, AHORA);
        verify(gateway).registrarResultado(SOLICITUD, EstadoNotificacionResolucion.ENVIADA, 1, null, AHORA);
    }

    @Test
    void elPanelOfreceElTextoDeDarcaParaLaFirma() {
        NotificacionResolucion guardada = registro(false);
        guardada.setEstado(EstadoNotificacionResolucion.PROGRAMADA);
        guardada.setFechaProgramada(AHORA.plusDays(1));
        guardada.setTextoDarca("Inicio. La Resolución se comunicó al estudiante por correo electrónico; se adjunta la constancia de la comunicación.");
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(guardada);

        assertEquals("Inicio. El estudiante firmó la Resolución en la Decanatura; se adjunta la Resolución firmada.",
                casoDeUso.getNotificacion(SOLICITUD, token(FUNCIONARIO)).getTextoDarcaFirma());

        guardada.setTextoDarca(null);
        assertTrue(casoDeUso.getNotificacion(SOLICITUD, token(FUNCIONARIO)).getTextoDarcaFirma()
                .contains("El estudiante firmó la Resolución en la Decanatura; se adjunta la Resolución firmada."));
    }

    @Test
    void soloSeReintentaUnaFallida() {
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(registro(false));
        when(gateway.reintentar(SOLICITUD)).thenReturn(false, true);

        assertEquals(String.format(MensajesError.NOTIFICACION_NO_REINTENTABLE, RADICADO), mensaje(() -> casoDeUso.reintentar(SOLICITUD, token(FUNCIONARIO))));
        assertNotNull(casoDeUso.reintentar(SOLICITUD, token(FUNCIONARIO)));
    }

    @Test
    void sinFirmaSalenElCorreoAlEstudianteConNegrillaYDarcaConTresPdf() {
        pendiente(registro(false));

        assertEquals(1, casoDeUso.procesarPendientes());

        ArgumentCaptor<CorreoPreparado> enviados = ArgumentCaptor.forClass(CorreoPreparado.class);
        verify(correo, times(2)).enviarAhora(enviados.capture());
        CorreoPreparado alEstudiante = enviados.getAllValues().get(0);
        assertEquals(CORREO_ESTUDIANTE, alEstudiante.getDestinatario());
        assertEquals("Notificación de Cancelación de asignatura", alEstudiante.getAsunto());
        assertTrue(alEstudiante.getTexto().contains("COMUNICACIÓN ELECTRÓNICA - RESOLUCIÓN No. 123-26/178 de 2026"));
        assertTrue(alEstudiante.getTexto().contains("Se comunica al estudiante, Ana María Muñoz Peña, del texto"));
        assertTrue(alEstudiante.getTexto().contains("la misma por la cual se aprueba parcialmente la cancelación de asignatura"));
        assertTrue(alEstudiante.getHtml().contains("No. <strong>123-26</strong>/178 de <strong>2026</strong>"));
        assertTrue(alEstudiante.getHtml().contains("<strong>Ana María Muñoz Peña</strong>"));
        assertTrue(alEstudiante.getHtml().contains("se <strong>aprueba parcialmente la</strong> cancelación"));
        assertEquals(List.of("Radicado-" + RADICADO + ".pdf", "Resolucion-" + RADICADO + ".pdf"),
                alEstudiante.getAdjuntos().stream().map(AdjuntoCorreo::getNombre).toList());
        CorreoPreparado aDarca = enviados.getAllValues().get(1);
        assertEquals(DARCA, aDarca.getDestinatario());
        assertEquals("DARCA - Notificación de Cancelación de asignatura - Radicado " + RADICADO, aDarca.getAsunto());
        assertEquals(List.of("Radicado-" + RADICADO + ".pdf", "Resolucion-" + RADICADO + ".pdf", "Comunicacion-Estudiante-" + RADICADO + ".pdf"),
                aDarca.getAdjuntos().stream().map(AdjuntoCorreo::getNombre).toList());
        assertEquals(CORREO_ESTUDIANTE, constancias.get(0).getDestinatario());
        assertEquals(AHORA, constancias.get(0).getFechaEnvio());
        assertEquals(alEstudiante.getParrafos(), constancias.get(0).getParrafos());
        verify(gateway).marcarEnvioEstudiante(SOLICITUD, AHORA);
        verify(gateway).marcarEnvioDarca(SOLICITUD, AHORA);
        verify(gateway).registrarResultado(SOLICITUD, EstadoNotificacionResolucion.ENVIADA, 1, null, AHORA);
    }

    @Test
    void conTextosEditadosElEstudianteRecibeElSuyoConNegrillaYDarcaElSuyoConLaFecha() {
        NotificacionResolucion editada = registro(false);
        editada.setTextoComunicacion("Señor(a) {nombre}\n\nResolución {numero}/178 de {anio}: se {decision} cancelación.");
        editada.setTextoDarca("Popayán, Cauca, {fecha}\n\nTexto editado para DARCA.");
        pendiente(editada);

        casoDeUso.procesarPendientes();

        ArgumentCaptor<CorreoPreparado> enviados = ArgumentCaptor.forClass(CorreoPreparado.class);
        verify(correo, times(2)).enviarAhora(enviados.capture());
        CorreoPreparado alEstudiante = enviados.getAllValues().get(0);
        assertEquals("Señor(a) Ana María Muñoz Peña\n\nResolución 123-26/178 de 2026: se aprueba parcialmente la cancelación.", alEstudiante.getTexto());
        assertTrue(alEstudiante.getHtml().contains("Señor(a) <strong>Ana María Muñoz Peña</strong>"));
        assertTrue(alEstudiante.getHtml().contains("<strong>123-26</strong>/178 de <strong>2026</strong>"));
        assertEquals("Popayán, Cauca, 20 de octubre de 2026\n\nTexto editado para DARCA.", enviados.getAllValues().get(1).getTexto());
        assertEquals(alEstudiante.getParrafos(), constancias.get(0).getParrafos());
    }

    @Test
    void unDocumentoAdicionalCargadoAntesNoVaEnNingunoDeLosDosCorreos() {
        pendiente(registro(false));
        lenient().when(anexoGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of(AnexoAcademico.builder().nombreArchivo("acta.pdf")
                .tipoArchivo("application/pdf").urlArchivo("/api/anexos/sol-1/acta.pdf")
                .tipoAnexoAcademico(TipoAnexoAcademico.builder().nombre("Documento adicional de la respuesta").build()).build()));

        casoDeUso.procesarPendientes();

        ArgumentCaptor<CorreoPreparado> enviados = ArgumentCaptor.forClass(CorreoPreparado.class);
        verify(correo, times(2)).enviarAhora(enviados.capture());
        assertEquals(List.of("Radicado-" + RADICADO + ".pdf", "Resolucion-" + RADICADO + ".pdf"),
                enviados.getAllValues().get(0).getAdjuntos().stream().map(AdjuntoCorreo::getNombre).toList());
        assertEquals(List.of("Radicado-" + RADICADO + ".pdf", "Resolucion-" + RADICADO + ".pdf", "Comunicacion-Estudiante-" + RADICADO + ".pdf"),
                enviados.getAllValues().get(1).getAdjuntos().stream().map(AdjuntoCorreo::getNombre).toList());
    }

    @Test
    void conFirmaNoSaleElCorreoAlEstudianteYDarcaRecibeDosPdfSinLaComunicacion() {
        pendiente(registro(true));

        casoDeUso.procesarPendientes();

        ArgumentCaptor<CorreoPreparado> enviados = ArgumentCaptor.forClass(CorreoPreparado.class);
        verify(correo, times(1)).enviarAhora(enviados.capture());
        assertEquals(DARCA, enviados.getValue().getDestinatario());
        assertEquals(List.of("Radicado-" + RADICADO + ".pdf", "Resolucion-" + RADICADO + ".pdf"),
                enviados.getValue().getAdjuntos().stream().map(AdjuntoCorreo::getNombre).toList());
        assertTrue(constancias.isEmpty());
        verify(gateway, never()).marcarEnvioEstudiante(any(), any());
        verify(gateway).registrarResultado(SOLICITUD, EstadoNotificacionResolucion.ENVIADA, 1, null, AHORA);
    }

    @Test
    void siOtroProcesoYaReclamoElRegistroNoSeEnviaNadaDosVeces() {
        when(gateway.getPendientes(AHORA, AHORA.minusMinutes(15))).thenReturn(List.of(SOLICITUD));
        when(gateway.reclamar(SOLICITUD, AHORA, AHORA.minusMinutes(15))).thenReturn(false);

        assertEquals(0, casoDeUso.procesarPendientes());

        verifyNoInteractions(correo);
        verify(gateway, never()).registrarResultado(any(), any(), anyInt(), any(), any());
    }

    @Test
    void siFallaElCorreoAlEstudianteDarcaSaleIgualYQuedaProgramadaConElError() {
        pendiente(registro(false));
        doThrow(new IllegalStateException("buzón lleno")).doNothing().when(correo).enviarAhora(any());

        casoDeUso.procesarPendientes();

        verify(correo, times(2)).enviarAhora(any());
        verify(gateway, never()).marcarEnvioEstudiante(any(), any());
        verify(gateway).marcarEnvioDarca(SOLICITUD, AHORA);
        assertNull(constancias.get(0).getFechaEnvio());
        verify(gateway).registrarResultado(SOLICITUD, EstadoNotificacionResolucion.PROGRAMADA, 1, "Correo al estudiante: buzón lleno", null);
    }

    @Test
    void siFallaDarcaElEstudianteRecibeElSuyoYElReintentoSoloRepiteDarca() {
        pendiente(registro(false));
        doNothing().doThrow(new IllegalStateException("smtp caído")).when(correo).enviarAhora(any());

        casoDeUso.procesarPendientes();

        verify(gateway).marcarEnvioEstudiante(SOLICITUD, AHORA);
        verify(gateway, never()).marcarEnvioDarca(any(), any());
        verify(gateway).registrarResultado(SOLICITUD, EstadoNotificacionResolucion.PROGRAMADA, 1, "Correo a DARCA: smtp caído", null);

        NotificacionResolucion reintento = registro(false);
        reintento.setIntentos(1);
        reintento.setFechaEnvioEstudiante(AHORA.minusMinutes(1));
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(reintento);
        clearInvocations(correo);
        doNothing().when(correo).enviarAhora(any());

        casoDeUso.procesarPendientes();

        ArgumentCaptor<CorreoPreparado> enviados = ArgumentCaptor.forClass(CorreoPreparado.class);
        verify(correo, times(1)).enviarAhora(enviados.capture());
        assertEquals(DARCA, enviados.getValue().getDestinatario());
        assertEquals(AHORA.minusMinutes(1), constancias.get(constancias.size() - 1).getFechaEnvio());
        verify(gateway).registrarResultado(SOLICITUD, EstadoNotificacionResolucion.ENVIADA, 2, null, AHORA);
    }

    @Test
    void alTercerIntentoFallidoQuedaFallidaConElErrorVisible() {
        NotificacionResolucion tercero = registro(true);
        tercero.setIntentos(2);
        pendiente(tercero);
        doThrow(new IllegalStateException("smtp caído")).when(correo).enviarAhora(any());

        casoDeUso.procesarPendientes();

        verify(gateway).registrarResultado(SOLICITUD, EstadoNotificacionResolucion.FALLIDA, 3, "Correo a DARCA: smtp caído", null);
    }

    @Test
    void sinCorreoDelEstudianteSeRegistraElErrorYDarcaSale() {
        pendiente(registro(false));
        SolicitudAcademica sinCorreo = solicitud("Cancelación de Asignatura", APROBADA);
        sinCorreo.getEstudiante().getUsuario().setCorreoElectronico(null);
        when(solicitudGateway.getPorUuid(SOLICITUD)).thenReturn(sinCorreo);
        doThrow(new IllegalStateException("el destinatario no tiene correo registrado")).doNothing().when(correo).enviarAhora(any());

        casoDeUso.procesarPendientes();

        verify(gateway).marcarEnvioDarca(SOLICITUD, AHORA);
        verify(gateway).registrarResultado(eq(SOLICITUD), eq(EstadoNotificacionResolucion.PROGRAMADA), eq(1),
                contains("no tiene correo"), isNull());
    }

    @Test
    void trasUnReinicioSeProcesanLasVencidasYLasQuedaronEnProcesoSinTerminar() {
        when(gateway.getPendientes(AHORA, AHORA.minusMinutes(15))).thenReturn(List.of(SOLICITUD, "sol-2"));
        when(gateway.reclamar(SOLICITUD, AHORA, AHORA.minusMinutes(15))).thenReturn(true);
        when(gateway.reclamar("sol-2", AHORA, AHORA.minusMinutes(15))).thenReturn(false);
        NotificacionResolucion interrumpida = registro(false);
        interrumpida.setFechaEnvioEstudiante(AHORA.minusHours(1));
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(interrumpida);

        assertEquals(1, casoDeUso.procesarPendientes());

        ArgumentCaptor<CorreoPreparado> enviados = ArgumentCaptor.forClass(CorreoPreparado.class);
        verify(correo, times(1)).enviarAhora(enviados.capture());
        assertEquals(DARCA, enviados.getValue().getDestinatario());
        verify(gateway).registrarResultado(SOLICITUD, EstadoNotificacionResolucion.ENVIADA, 1, null, AHORA);
    }

    @Test
    void elProcesamientoNoUsaTokenNiContextoDeSeguridad() {
        pendiente(registro(true));

        casoDeUso.procesarPendientes();

        verifyNoInteractions(jwtServicio, sesionGateway, log);
    }
}
