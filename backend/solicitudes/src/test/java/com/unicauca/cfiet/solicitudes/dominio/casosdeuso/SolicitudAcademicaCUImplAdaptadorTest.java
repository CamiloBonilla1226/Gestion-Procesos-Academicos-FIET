package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.AlmacenamientoAnexosIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.AnexoAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.EstudianteGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.EtapaSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ResolucionAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorMalFormatoExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorReglaNegocioVioladaExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.AnexoAcademicoConstantes.COMPROBANTE_PAGO;
import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.AnexoAcademicoConstantes.RECIBO_PAGO;
import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudAcademicaCUImplAdaptadorTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final String TIPO_CM = "tipo-cm";
    private static final String TIPO_CA = "tipo-ca";
    private static final String TIPO_ES = "tipo-es";
    private static final String ESTUDIANTE = "est-1";
    private static final String OTRO_ESTUDIANTE = "est-2";
    private static final String FUNCIONARIO = "fa-1";
    private static final String OTRO_FUNCIONARIO = "fa-2";
    private static final String DECANO = "dec-1";
    private static final String SOLICITUD = "sol-1";
    private static final String TOKEN = "token-jwt";
    private static final String RUTA_ESCANEO = "/api/anexos/sol-1/escaneo.pdf";

    @Mock
    private SolicitudAcademicaGatewayIntPuerto gateway;

    @Mock
    private EstudianteGatewayIntPuerto estudianteGateway;

    @Mock
    private TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway;

    @Mock
    private EtapaSolicitudAcademicaGatewayIntPuerto etapaGateway;

    @Mock
    private UsuarioGatewayIntPuerto usuarioGateway;

    @Mock
    private ResolucionAcademicaGatewayIntPuerto resolucionGateway;

    @Mock
    private AnexoAcademicoGatewayIntPuerto anexoGateway;

    @Mock
    private AlmacenamientoAnexosIntPuerto almacenamiento;

    @Mock
    private LogCUIntPuerto log;

    private List<EtapaSolicitudAcademica> etapas;

    private SolicitudAcademicaCUImplAdaptador casoDeUso(Clock reloj) {
        ExcepcionesFormateadorImplAdaptador formateador = new ExcepcionesFormateadorImplAdaptador();
        return new SolicitudAcademicaCUImplAdaptador(gateway, estudianteGateway, tipoSolicitudGateway, etapaGateway,
                resolucionGateway, anexoGateway, usuarioGateway, almacenamiento, new MaquinaEtapas(formateador), formateador, log, reloj);
    }

    private ResolucionAcademica escaneo() {
        return ResolucionAcademica.builder()
                .solicitudAcademica(SolicitudAcademica.builder().uuidSolicitudAcademica(SOLICITUD).build())
                .urlArchivo(RUTA_ESCANEO)
                .nombreArchivo("resolucion.pdf")
                .build();
    }

    private AnexoAcademico anexo(String nombreTipo) {
        return AnexoAcademico.builder()
                .uuidAnexoAcademico("anexo-" + nombreTipo)
                .tipoAnexoAcademico(nombreTipo == null ? null : TipoAnexoAcademico.builder().uuidTipoAnexoAcademico("tipo-" + nombreTipo).nombre(nombreTipo).build())
                .build();
    }

    private SolicitudAcademicaCUImplAdaptador casoDeUso() {
        return casoDeUso(reloj("2026-03-10T15:00:00Z"));
    }

    private Clock reloj(String instante) {
        return Clock.fixed(Instant.parse(instante), BOGOTA);
    }

    private TipoSolicitudAcademica tipo(String uuid, String nombre) {
        return TipoSolicitudAcademica.builder()
                .uuidTipoSolicitudAcademica(uuid)
                .nombre(nombre)
                .funcionarioAcademico(FuncionarioAcademico.builder().uuidUsuario(FUNCIONARIO).build())
                .build();
    }

    private TipoSolicitudAcademica tipoMatricula() {
        return tipo(TIPO_CM, "Cancelación de Matrícula");
    }

    private TipoSolicitudAcademica tipoSupletorio() {
        return tipo(TIPO_ES, "Examen Supletorio");
    }

    private EtapaSolicitudAcademica etapa(String codigo) {
        return etapas.stream().filter(e -> e.getCodigo().equals(codigo)).findFirst().orElseThrow();
    }

    private Usuario usuario(String uuid, String rol) {
        return Usuario.builder()
                .uuidUsuario(uuid)
                .roles(new ArrayList<>(List.of(Rol.builder().uuidRol("rol-" + uuid).nombre(rol).estado(true).build())))
                .build();
    }

    private Estudiante estudiante(String uuid) {
        return Estudiante.builder().uuidUsuario(uuid).usuario(Usuario.builder().uuidUsuario(uuid).build()).build();
    }

    private SolicitudAcademica solicitud(TipoSolicitudAcademica tipo, String codigoEtapa) {
        return SolicitudAcademica.builder()
                .uuidSolicitudAcademica(SOLICITUD)
                .radicado("2026-XX-0001")
                .estudiante(estudiante(ESTUDIANTE))
                .tipoSolicitudAcademica(tipo)
                .etapa(etapa(codigoEtapa))
                .fechaCreacion(LocalDateTime.of(2026, 3, 1, 8, 0))
                .build();
    }

    private ActorSolicitud actor(String uuid, RolEtiquetaEtapa rol) {
        return ActorSolicitud.builder().uuidUsuario(uuid).rol(rol).build();
    }

    @BeforeEach
    void setUp() {
        etapas = new ArrayList<>();
        for (String codigo : ETAPAS)
            etapas.add(EtapaSolicitudAcademica.builder().uuidEtapa("etapa-" + codigo).codigo(codigo).build());
        lenient().when(etapaGateway.getPorTipoIncluyendoUniversales(anyString())).thenAnswer(invocacion -> etapas);
        lenient().when(usuarioGateway.getUsuario(ESTUDIANTE)).thenReturn(usuario(ESTUDIANTE, "Estudiante"));
        lenient().when(usuarioGateway.getUsuario(OTRO_ESTUDIANTE)).thenReturn(usuario(OTRO_ESTUDIANTE, "Estudiante"));
        lenient().when(usuarioGateway.getUsuario(FUNCIONARIO)).thenReturn(usuario(FUNCIONARIO, "Funcionario Académico"));
        lenient().when(usuarioGateway.getUsuario(OTRO_FUNCIONARIO)).thenReturn(usuario(OTRO_FUNCIONARIO, "Funcionario Académico"));
        lenient().when(usuarioGateway.getUsuario(DECANO)).thenReturn(usuario(DECANO, "Decano"));
        lenient().when(gateway.crear(any(), any())).thenAnswer(invocacion -> invocacion.getArgument(0));
        lenient().when(gateway.actualizarEtapa(any(), any())).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private void verificarQueNoSeGuardoNada() {
        verify(gateway, never()).crear(any(), any());
        verify(gateway, never()).actualizarEtapa(any(), any());
        verify(resolucionGateway, never()).eliminarPorSolicitud(any());
        verify(almacenamiento, never()).eliminarTrasConfirmar(any());
        verify(log, never()).crearLog(any(), any(), any());
    }

    private void prepararCreacion(TipoSolicitudAcademica tipo) {
        when(estudianteGateway.getPorUuid(ESTUDIANTE)).thenReturn(estudiante(ESTUDIANTE));
        when(tipoSolicitudGateway.getPorUuid(tipo.getUuidTipoSolicitudAcademica())).thenReturn(tipo);
    }

    @Test
    void crearSolicitudQuedaRadicadaConElPrimerRadicadoYUnHistorial() {
        prepararCreacion(tipoMatricula());
        when(gateway.getUltimoRadicado("2026-CM-")).thenReturn(null);

        SolicitudAcademica creada = casoDeUso().crearSolicitud(ESTUDIANTE, TIPO_CM, TOKEN);

        ArgumentCaptor<SolicitudAcademica> solicitud = ArgumentCaptor.forClass(SolicitudAcademica.class);
        ArgumentCaptor<HistorialSolicitudAcademica> historial = ArgumentCaptor.forClass(HistorialSolicitudAcademica.class);
        verify(gateway).crear(solicitud.capture(), historial.capture());
        assertSame(creada, solicitud.getValue());
        assertEquals("2026-CM-0001", creada.getRadicado());
        assertEquals(RADICADA, creada.getEtapa().getCodigo());
        assertEquals("etapa-RADICADA", creada.getEtapa().getUuidEtapa());
        assertEquals(ESTUDIANTE, creada.getEstudiante().getUuidUsuario());
        assertEquals(TIPO_CM, creada.getTipoSolicitudAcademica().getUuidTipoSolicitudAcademica());
        assertEquals(LocalDateTime.of(2026, 3, 10, 10, 0), creada.getFechaCreacion());
        assertNotNull(creada.getUuidSolicitudAcademica());

        assertSame(creada, historial.getValue().getSolicitudAcademica());
        assertEquals(ESTUDIANTE, historial.getValue().getUsuario().getUuidUsuario());
        assertEquals("RADICAR", historial.getValue().getAccion());
        assertNull(historial.getValue().getObservaciones());
        assertEquals(creada.getFechaCreacion(), historial.getValue().getFecha());
        assertNotNull(historial.getValue().getUuidHistorial());
    }

    @Test
    void elConsecutivoSubeSobreElUltimoRadicadoDelAnioYTipo() {
        prepararCreacion(tipoMatricula());
        when(gateway.getUltimoRadicado("2026-CM-")).thenReturn("2026-CM-0041");

        assertEquals("2026-CM-0042", casoDeUso().crearSolicitud(ESTUDIANTE, TIPO_CM, TOKEN).getRadicado());
    }

    @Test
    void elConsecutivoPasaDeTresACuatroCifras() {
        TipoSolicitudAcademica asignatura = tipo(TIPO_CA, "Cancelación de Asignatura");
        prepararCreacion(asignatura);
        when(gateway.getUltimoRadicado("2026-CA-")).thenReturn("2026-CA-0999");

        assertEquals("2026-CA-1000", casoDeUso().crearSolicitud(ESTUDIANTE, TIPO_CA, TOKEN).getRadicado());
    }

    @Test
    void elConsecutivoReiniciaConElAnio() {
        prepararCreacion(tipoMatricula());
        when(gateway.getUltimoRadicado("2027-CM-")).thenReturn(null);

        SolicitudAcademica creada = casoDeUso(reloj("2027-01-01T06:00:00Z")).crearSolicitud(ESTUDIANTE, TIPO_CM, TOKEN);

        assertEquals("2027-CM-0001", creada.getRadicado());
        verify(gateway, never()).getUltimoRadicado("2026-CM-");
    }

    @Test
    void elAnioDelRadicadoSeTomaEnLaHoraDeColombia() {
        prepararCreacion(tipoMatricula());
        when(gateway.getUltimoRadicado("2026-CM-")).thenReturn("2026-CM-0310");

        SolicitudAcademica creada = casoDeUso(reloj("2027-01-01T03:00:00Z")).crearSolicitud(ESTUDIANTE, TIPO_CM, TOKEN);

        assertEquals("2026-CM-0311", creada.getRadicado());
    }

    @Test
    void elConsecutivoEsIndependientePorTipo() {
        prepararCreacion(tipoSupletorio());
        when(gateway.getUltimoRadicado("2026-ES-")).thenReturn(null);

        assertEquals("2026-ES-0001", casoDeUso().crearSolicitud(ESTUDIANTE, TIPO_ES, TOKEN).getRadicado());
        verify(gateway).getUltimoRadicado("2026-ES-");
        verify(gateway, never()).getUltimoRadicado("2026-CM-");
        verify(gateway, never()).getUltimoRadicado("2026-CA-");
    }

    @Test
    void crearConEstudianteInexistenteNoGuardaNada() {
        when(estudianteGateway.getPorUuid("no-existe")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso().crearSolicitud("no-existe", TIPO_CM, TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void crearConTipoInexistenteNoGuardaNada() {
        when(estudianteGateway.getPorUuid(ESTUDIANTE)).thenReturn(estudiante(ESTUDIANTE));
        when(tipoSolicitudGateway.getPorUuid("no-existe")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso().crearSolicitud(ESTUDIANTE, "no-existe", TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void crearConTipoQueNoEsUnProcesoAcademicoNoGuardaNada() {
        prepararCreacion(tipo("tipo-otro", "Homologación"));

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso().crearSolicitud(ESTUDIANTE, "tipo-otro", TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void crearSinLaEtapaRadicadaEnElCatalogoNoGuardaNada() {
        prepararCreacion(tipoMatricula());
        etapas.removeIf(e -> e.getCodigo().equals(RADICADA));

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso().crearSolicitud(ESTUDIANTE, TIPO_CM, TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void funcionarioAsignadoRemiteAlDecanoYQuedaElHistorial() {
        SolicitudAcademica actual = solicitud(tipoMatricula(), RADICADA);
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(actual);

        SolicitudAcademica resultado = casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.REMITIR_DECANO,
                actor(FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO), null, TOKEN);

        ArgumentCaptor<SolicitudAcademica> solicitud = ArgumentCaptor.forClass(SolicitudAcademica.class);
        ArgumentCaptor<HistorialSolicitudAcademica> historial = ArgumentCaptor.forClass(HistorialSolicitudAcademica.class);
        verify(gateway).actualizarEtapa(solicitud.capture(), historial.capture());
        assertEquals(EN_REVISION_DECANO, resultado.getEtapa().getCodigo());
        assertEquals("etapa-EN_REVISION_DECANO", solicitud.getValue().getEtapa().getUuidEtapa());
        assertSame(solicitud.getValue(), historial.getValue().getSolicitudAcademica());
        assertEquals(FUNCIONARIO, historial.getValue().getUsuario().getUuidUsuario());
        assertEquals("REMITIR_DECANO", historial.getValue().getAccion());
        assertNull(historial.getValue().getObservaciones());
        assertEquals(LocalDateTime.of(2026, 3, 10, 10, 0), historial.getValue().getFecha());
    }

    @Test
    void decanoRechazaConObservacionYElHistorialLaGuarda() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoMatricula(), EN_REVISION_DECANO));

        SolicitudAcademica resultado = casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.RECHAZAR_DECANO,
                actor(DECANO, RolEtiquetaEtapa.DECANO), "  No procede  ", TOKEN);

        ArgumentCaptor<HistorialSolicitudAcademica> historial = ArgumentCaptor.forClass(HistorialSolicitudAcademica.class);
        verify(gateway).actualizarEtapa(any(), historial.capture());
        assertEquals(RECHAZADA_POR_DECANO, resultado.getEtapa().getCodigo());
        assertEquals("No procede", historial.getValue().getObservaciones());
        assertEquals(DECANO, historial.getValue().getUsuario().getUuidUsuario());
    }

    @Test
    void elEstudianteDuenioSubeElComprobanteDelSupletorio() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoSupletorio(), PENDIENTE_PAGO));
        when(anexoGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of(anexo(COMPROBANTE_PAGO)));

        SolicitudAcademica resultado = casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.SUBIR_COMPROBANTE,
                actor(ESTUDIANTE, RolEtiquetaEtapa.ESTUDIANTE), null, TOKEN);

        assertEquals(EN_VERIFICACION_PAGO, resultado.getEtapa().getCodigo());
        verify(gateway).actualizarEtapa(any(), any());
    }

    @Test
    void unRolQueNoLeCorrespondeALaTransicionNoGuardaNada() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoMatricula(), RADICADA));

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.REMITIR_DECANO, actor(ESTUDIANTE, RolEtiquetaEtapa.ESTUDIANTE), null, TOKEN));

        assertTrue(error.getMessage().contains("no puede ejecutar"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unUsuarioSinElRolDeclaradoNoGuardaNada() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoMatricula(), EN_REVISION_DECANO));

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.APROBAR_DECANO, actor(FUNCIONARIO, RolEtiquetaEtapa.DECANO), null, TOKEN));

        assertTrue(error.getMessage().contains("no tiene el rol Decano"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unEstudianteNoActuaSobreUnaSolicitudAjena() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoSupletorio(), PENDIENTE_PAGO));

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.SUBIR_COMPROBANTE, actor(OTRO_ESTUDIANTE, RolEtiquetaEtapa.ESTUDIANTE),
                        null, TOKEN));

        assertTrue(error.getMessage().contains("no pertenece"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unFuncionarioNoAsignadoAlTipoNoGuardaNada() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoMatricula(), RADICADA));

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.REMITIR_DECANO, actor(OTRO_FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO), null, TOKEN));

        assertTrue(error.getMessage().contains("no está asignado"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unaTransicionInvalidaNoGuardaNada() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoMatricula(), APROBADA_POR_DECANO));

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.ENVIAR_RECIBO, actor(FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO),
                        null, TOKEN));

        assertTrue(error.getMessage().contains("no está permitida"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unaSolicitudEnEtapaFinalNoGuardaNada() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoSupletorio(), APROBADA));

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.RECHAZAR_COMPROBANTE, actor(FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO),
                        "x", TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void faltaDeRequisitosNoGuardaNada() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoMatricula(), RADICADA));

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.RECHAZAR_FUNCIONARIO, actor(FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO),
                        "No cumple", TOKEN));

        assertTrue(error.getMessage().contains("Resolución"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unaObservacionDemasiadoLargaNoGuardaNada() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoMatricula(), EN_REVISION_DECANO));

        assertThrows(ErrorMalFormatoExcepcion.class,
                () -> casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.RECHAZAR_DECANO, actor(DECANO, RolEtiquetaEtapa.DECANO),
                        "x".repeat(501), TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unaSolicitudInexistenteNoGuardaNada() {
        when(gateway.getPorUuid("no-existe")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso().cambiarEtapa("no-existe", AccionEtapa.APROBAR_DECANO, actor(DECANO, RolEtiquetaEtapa.DECANO), null, TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unActorInexistenteNoGuardaNada() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoMatricula(), EN_REVISION_DECANO));
        when(usuarioGateway.getUsuario("no-existe")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.APROBAR_DECANO, actor("no-existe", RolEtiquetaEtapa.DECANO), null, TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void sinActorOSinAccionNoGuardaNada() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoMatricula(), EN_REVISION_DECANO));
        SolicitudAcademicaCUImplAdaptador casoDeUso = casoDeUso();

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.cambiarEtapa(SOLICITUD, AccionEtapa.APROBAR_DECANO, null, null, TOKEN));
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.cambiarEtapa(SOLICITUD, null, actor(DECANO, RolEtiquetaEtapa.DECANO), null, TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unaEtapaDestinoQueNoEstaEnElCatalogoNoGuardaNada() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoMatricula(), RADICADA));
        etapas.removeIf(e -> e.getCodigo().equals(EN_REVISION_DECANO));

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.REMITIR_DECANO, actor(FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO), null, TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void conLaResolucionEnLaBaseElFuncionarioRechaza() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoMatricula(), RADICADA));
        when(resolucionGateway.existePorSolicitud(SOLICITUD)).thenReturn(true);

        SolicitudAcademica resultado = casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.RECHAZAR_FUNCIONARIO,
                actor(FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO), "No cumple", TOKEN);

        assertEquals(RECHAZADA, resultado.getEtapa().getCodigo());
        verify(resolucionGateway).existePorSolicitud(SOLICITUD);
    }

    @Test
    void enviarLaRespuestaSinResolucionEnLaBaseNoGuardaNada() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoMatricula(), APROBADA_POR_DECANO));
        when(resolucionGateway.existePorSolicitud(SOLICITUD)).thenReturn(false);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.ENVIAR_RESPUESTA, actor(FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO), null, TOKEN));

        assertTrue(error.getMessage().contains("Resolución"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void conElReciboEnLaBaseElFuncionarioEnviaElRecibo() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoSupletorio(), APROBADA_POR_DECANO));
        when(anexoGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of(anexo(RECIBO_PAGO)));

        SolicitudAcademica resultado = casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.ENVIAR_RECIBO,
                actor(FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO), null, TOKEN);

        assertEquals(PENDIENTE_PAGO, resultado.getEtapa().getCodigo());
    }

    @Test
    void unAnexoDeOtroTipoNoCuentaComoRecibo() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoSupletorio(), APROBADA_POR_DECANO));
        when(anexoGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of(anexo(COMPROBANTE_PAGO), anexo(null)));

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.ENVIAR_RECIBO, actor(FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO), null, TOKEN));

        assertTrue(error.getMessage().contains("recibo"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void subirElComprobanteSinComprobanteEnLaBaseNoGuardaNada() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoSupletorio(), PENDIENTE_PAGO));
        when(anexoGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of());

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.SUBIR_COMPROBANTE, actor(ESTUDIANTE, RolEtiquetaEtapa.ESTUDIANTE), null, TOKEN));

        assertTrue(error.getMessage().contains("comprobante"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void laResolucionEnLaBaseNoEximeDeLaObservacion() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoMatricula(), RADICADA));
        when(resolucionGateway.existePorSolicitud(SOLICITUD)).thenReturn(true);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.RECHAZAR_FUNCIONARIO, actor(FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO), "   ", TOKEN));

        assertTrue(error.getMessage().contains("observación"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void remitirAlDecanoConEscaneoBorraLaFichaYElArchivoTrasConfirmar() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoMatricula(), RADICADA));
        when(resolucionGateway.getPorSolicitud(SOLICITUD)).thenReturn(escaneo());

        SolicitudAcademica resultado = casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.REMITIR_DECANO,
                actor(FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO), null, TOKEN);

        assertEquals(EN_REVISION_DECANO, resultado.getEtapa().getCodigo());
        InOrder orden = inOrder(gateway, resolucionGateway, almacenamiento, log);
        orden.verify(gateway).actualizarEtapa(any(), any());
        orden.verify(resolucionGateway).eliminarPorSolicitud(SOLICITUD);
        orden.verify(almacenamiento).eliminarTrasConfirmar(RUTA_ESCANEO);
        orden.verify(log).crearLog(eq("Cambiar etapa de solicitud académica"), contains("se retiró el escaneo"), eq(TOKEN));
        verify(almacenamiento, never()).eliminar(any());
    }

    @Test
    void remitirAlDecanoSinEscaneoNoBorraNada() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoMatricula(), RADICADA));
        when(resolucionGateway.getPorSolicitud(SOLICITUD)).thenReturn(null);

        casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.REMITIR_DECANO, actor(FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO), null, TOKEN);

        verify(gateway).actualizarEtapa(any(), any());
        verify(resolucionGateway, never()).eliminarPorSolicitud(any());
        verify(almacenamiento, never()).eliminarTrasConfirmar(any());
    }

    @Test
    void siElCambioDeEtapaFallaNoSeBorraElEscaneo() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoMatricula(), RADICADA));
        when(resolucionGateway.getPorSolicitud(SOLICITUD)).thenReturn(escaneo());
        when(gateway.actualizarEtapa(any(), any())).thenThrow(new IllegalStateException("base caida"));

        assertThrows(IllegalStateException.class, () -> casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.REMITIR_DECANO,
                actor(FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO), null, TOKEN));

        verify(resolucionGateway, never()).eliminarPorSolicitud(any());
        verify(almacenamiento, never()).eliminarTrasConfirmar(any());
        verify(almacenamiento, never()).eliminar(any());
        verify(log, never()).crearLog(any(), any(), any());
    }

    @Test
    void elFuncionarioQueRechazaConservaElEscaneo() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoMatricula(), RADICADA));
        when(resolucionGateway.existePorSolicitud(SOLICITUD)).thenReturn(true);

        casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.RECHAZAR_FUNCIONARIO, actor(FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO), "No cumple", TOKEN);

        verify(resolucionGateway, never()).getPorSolicitud(any());
        verify(resolucionGateway, never()).eliminarPorSolicitud(any());
        verify(almacenamiento, never()).eliminarTrasConfirmar(any());
    }

    @Test
    void aprobarORechazarEnElDecanoYEnviarLaRespuestaConservanElEscaneo() {
        when(resolucionGateway.existePorSolicitud(SOLICITUD)).thenReturn(true);
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoMatricula(), EN_REVISION_DECANO));
        casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.APROBAR_DECANO, actor(DECANO, RolEtiquetaEtapa.DECANO), null, TOKEN);
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoMatricula(), EN_REVISION_DECANO));
        casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.RECHAZAR_DECANO, actor(DECANO, RolEtiquetaEtapa.DECANO), "No procede", TOKEN);
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoMatricula(), APROBADA_POR_DECANO));
        casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.ENVIAR_RESPUESTA, actor(FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO), null, TOKEN);
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoMatricula(), RECHAZADA_POR_DECANO));
        casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.ENVIAR_RESPUESTA, actor(FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO), null, TOKEN);

        verify(gateway, times(4)).actualizarEtapa(any(), any());
        verify(resolucionGateway, never()).eliminarPorSolicitud(any());
        verify(almacenamiento, never()).eliminarTrasConfirmar(any());
    }

    @Test
    void unaSolicitudEnCursoDelMismoTipoImpideRadicarOtra() {
        prepararCreacion(tipoMatricula());
        when(gateway.getRadicadoEnCurso(ESTUDIANTE, TIPO_CM, List.of(APROBADA, RECHAZADA))).thenReturn("2026-CM-0007");

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso().crearSolicitud(ESTUDIANTE, TIPO_CM, TOKEN));

        assertTrue(error.getMessage().contains("en curso"));
        assertTrue(error.getMessage().contains("2026-CM-0007"));
        assertTrue(error.getMessage().contains("Cancelación de Matrícula"));
        verify(gateway, never()).getUltimoRadicado(any());
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unaSolicitudEnCursoDeOtroTipoNoImpideRadicar() {
        TipoSolicitudAcademica asignatura = tipo(TIPO_CA, "Cancelación de Asignatura");
        prepararCreacion(asignatura);
        lenient().when(gateway.getRadicadoEnCurso(ESTUDIANTE, TIPO_CM, List.of(APROBADA, RECHAZADA))).thenReturn("2026-CM-0007");
        when(gateway.getRadicadoEnCurso(ESTUDIANTE, TIPO_CA, List.of(APROBADA, RECHAZADA))).thenReturn(null);

        SolicitudAcademica creada = casoDeUso().crearSolicitud(ESTUDIANTE, TIPO_CA, TOKEN);

        assertEquals("2026-CA-0001", creada.getRadicado());
        verify(gateway).crear(any(), any());
    }

    @Test
    void unaSolicitudTerminadaDelMismoTipoPermiteRadicarOtra() {
        prepararCreacion(tipoMatricula());
        when(gateway.getUltimoRadicado("2026-CM-")).thenReturn("2026-CM-0007");

        SolicitudAcademica creada = casoDeUso().crearSolicitud(ESTUDIANTE, TIPO_CM, TOKEN);

        ArgumentCaptor<List<String>> excluidas = ArgumentCaptor.forClass(List.class);
        verify(gateway).getRadicadoEnCurso(eq(ESTUDIANTE), eq(TIPO_CM), excluidas.capture());
        assertEquals(List.of(APROBADA, RECHAZADA), excluidas.getValue());
        assertEquals("2026-CM-0008", creada.getRadicado());
    }

    @Test
    void unChoqueDeRadicadoNoSeReintentaYNoDejaLog() {
        prepararCreacion(tipoMatricula());
        when(gateway.crear(any(), any())).thenThrow(new ErrorReglaNegocioVioladaExcepcion("No se pudo generar el radicado, intente de nuevo..."));

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso().crearSolicitud(ESTUDIANTE, TIPO_CM, TOKEN));

        assertTrue(error.getMessage().contains("No se pudo generar el radicado"));
        verify(gateway, times(1)).crear(any(), any());
        verify(gateway, times(1)).getUltimoRadicado(any());
        verify(log, never()).crearLog(any(), any(), any());
    }

    @Test
    void radicarEscribeElLogConElRadicadoYLaAccion() {
        prepararCreacion(tipoMatricula());

        casoDeUso().crearSolicitud(ESTUDIANTE, TIPO_CM, TOKEN);

        ArgumentCaptor<String> resultado = ArgumentCaptor.forClass(String.class);
        verify(log).crearLog(eq("Radicar solicitud académica"), resultado.capture(), eq(TOKEN));
        assertTrue(resultado.getValue().contains("2026-CM-0001"));
        assertTrue(resultado.getValue().contains("RADICAR"));
    }

    @Test
    void cambiarEtapaEscribeElLogConElRadicadoLaAccionYLasEtapas() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(tipoMatricula(), EN_REVISION_DECANO));

        casoDeUso().cambiarEtapa(SOLICITUD, AccionEtapa.APROBAR_DECANO, actor(DECANO, RolEtiquetaEtapa.DECANO), null, TOKEN);

        ArgumentCaptor<String> resultado = ArgumentCaptor.forClass(String.class);
        verify(log).crearLog(eq("Cambiar etapa de solicitud académica"), resultado.capture(), eq(TOKEN));
        assertTrue(resultado.getValue().contains("2026-XX-0001"));
        assertTrue(resultado.getValue().contains("APROBAR_DECANO"));
        assertTrue(resultado.getValue().contains(EN_REVISION_DECANO));
        assertTrue(resultado.getValue().contains(APROBADA_POR_DECANO));
        assertFalse(resultado.getValue().contains("escaneo"));
    }
}
