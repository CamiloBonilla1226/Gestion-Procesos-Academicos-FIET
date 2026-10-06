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
class AnexoAcademicoCUImplAdaptadorTest {

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
    private static final String RUTA = "/api/anexos/sol-1/archivo_20260310.pdf";
    private static final int CINCO_MB = 5 * 1024 * 1024;
    private static final String PAZ_BIBLIOTECAS = "Paz y salvo - División de Bibliotecas";
    private static final String DARCA = "Carné estudiantil o constancia de no trámite - DARCA";
    private static final String FOR_23 = "Formato PM-FO-4-FOR-23 firmado por el docente que orienta la asignatura";
    private static final String JUSTIFICACION = "Soporte de la justificación de la no presentación";
    private static final String CRUCE = "Formato firmado por el docente de la asignatura con la que se cruza";
    private static final String RECIBO = "Recibo de pago";
    private static final String COMPROBANTE = "Comprobante de pago";

    @Mock
    private AnexoAcademicoGatewayIntPuerto anexoGateway;

    @Mock
    private SolicitudAcademicaGatewayIntPuerto solicitudGateway;

    @Mock
    private TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway;

    @Mock
    private TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway;

    @Mock
    private UsuarioGatewayIntPuerto usuarioGateway;

    @Mock
    private AlmacenamientoAnexosIntPuerto almacenamiento;

    @Mock
    private LogCUIntPuerto log;

    private AnexoAcademicoCUImplAdaptador casoDeUso;
    private final List<TipoAnexoAcademico> catalogo = new ArrayList<>();

    private TipoSolicitudAcademica tipo(String uuid, String nombre) {
        return TipoSolicitudAcademica.builder()
                .uuidTipoSolicitudAcademica(uuid)
                .nombre(nombre)
                .funcionarioAcademico(FuncionarioAcademico.builder().uuidUsuario(FUNCIONARIO).build())
                .build();
    }

    private final TipoSolicitudAcademica matricula = tipo(TIPO_CM, "Cancelación de Matrícula");
    private final TipoSolicitudAcademica asignatura = tipo(TIPO_CA, "Cancelación de Asignatura");
    private final TipoSolicitudAcademica supletorio = tipo(TIPO_ES, "Examen Supletorio");

    private TipoAnexoAcademico tipoAnexo(TipoSolicitudAcademica tipo, String nombre, String formatos, boolean obligatorio) {
        TipoAnexoAcademico tipoAnexo = TipoAnexoAcademico.builder()
                .uuidTipoAnexoAcademico("anexo-" + catalogo.size())
                .tipoSolicitudAcademica(tipo)
                .nombre(nombre)
                .formatosPermitidos(formatos)
                .obligatorio(obligatorio)
                .build();
        catalogo.add(tipoAnexo);
        return tipoAnexo;
    }

    private TipoAnexoAcademico porNombre(String nombre) {
        return catalogo.stream().filter(t -> t.getNombre().equals(nombre)).findFirst().orElseThrow();
    }

    private Usuario usuario(String uuid, String rol) {
        return Usuario.builder()
                .uuidUsuario(uuid)
                .roles(new ArrayList<>(List.of(Rol.builder().uuidRol("rol-" + uuid).nombre(rol).estado(true).build())))
                .build();
    }

    private SolicitudAcademica solicitud(TipoSolicitudAcademica tipo, String etapa) {
        SolicitudAcademica solicitud = SolicitudAcademica.builder()
                .uuidSolicitudAcademica(SOLICITUD)
                .radicado("2026-XX-0001")
                .estudiante(Estudiante.builder().uuidUsuario(ESTUDIANTE).build())
                .tipoSolicitudAcademica(tipo)
                .etapa(EtapaSolicitudAcademica.builder().uuidEtapa("etapa-" + etapa).codigo(etapa).build())
                .build();
        lenient().when(solicitudGateway.getPorUuid(SOLICITUD)).thenReturn(solicitud);
        return solicitud;
    }

    private ActorSolicitud actor(String uuid, RolEtiquetaEtapa rol) {
        return ActorSolicitud.builder().uuidUsuario(uuid).rol(rol).build();
    }

    private ActorSolicitud estudiante() {
        return actor(ESTUDIANTE, RolEtiquetaEtapa.ESTUDIANTE);
    }

    private ActorSolicitud funcionario() {
        return actor(FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO);
    }

    private byte[] contenido(byte[] firma, int tamanio) {
        byte[] contenido = Arrays.copyOf(firma, Math.max(tamanio, firma.length));
        Arrays.fill(contenido, firma.length, contenido.length, (byte) 'x');
        return contenido;
    }

    private byte[] pdf(int tamanio) {
        return contenido("%PDF-1.7\n".getBytes(StandardCharsets.US_ASCII), tamanio);
    }

    private byte[] png() {
        return contenido(new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A}, 64);
    }

    private byte[] jpg() {
        return contenido(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0}, 64);
    }

    private ArchivoAdjunto archivo(String nombre, byte[] contenido) {
        return ArchivoAdjunto.builder().nombreOriginal(nombre).tipoContenido("cualquiera").contenido(contenido).build();
    }

    @BeforeEach
    void setUp() {
        casoDeUso = new AnexoAcademicoCUImplAdaptador(anexoGateway, solicitudGateway, tipoSolicitudGateway, tipoAnexoGateway,
                usuarioGateway, almacenamiento, new ExcepcionesFormateadorImplAdaptador(), log,
                Clock.fixed(Instant.parse("2026-03-10T15:00:00Z"), ZoneId.of("America/Bogota")));

        tipoAnexo(matricula, PAZ_BIBLIOTECAS, "pdf", true);
        tipoAnexo(matricula, "Paz y salvo - División de Deportes y Recreación", "pdf", true);
        tipoAnexo(matricula, "Paz y salvo - División de Salud Integral", "pdf", true);
        tipoAnexo(matricula, "Cupón de Confirmación de la Intervención Psicosocial - División de Salud Integral", "pdf", true);
        tipoAnexo(matricula, "Paz y salvo - División Financiera", "pdf", true);
        tipoAnexo(matricula, DARCA, "pdf,jpg,jpeg,png", true);
        tipoAnexo(supletorio, FOR_23, "pdf,jpg,png", true);
        tipoAnexo(supletorio, JUSTIFICACION, "pdf,jpg,png", false);
        tipoAnexo(supletorio, CRUCE, "pdf,jpg,png", false);
        tipoAnexo(supletorio, RECIBO, "pdf", false);
        tipoAnexo(supletorio, COMPROBANTE, "pdf,jpg,png", false);

        lenient().when(tipoAnexoGateway.getPorUuid(anyString())).thenAnswer(invocacion -> catalogo.stream()
                .filter(t -> t.getUuidTipoAnexoAcademico().equals(invocacion.getArgument(0))).findFirst().orElse(null));
        lenient().when(tipoAnexoGateway.getPorTipo(anyString())).thenAnswer(invocacion -> catalogo.stream()
                .filter(t -> t.getTipoSolicitudAcademica().getUuidTipoSolicitudAcademica().equals(invocacion.getArgument(0))).toList());
        lenient().when(usuarioGateway.getUsuario(ESTUDIANTE)).thenReturn(usuario(ESTUDIANTE, "Estudiante"));
        lenient().when(usuarioGateway.getUsuario(OTRO_ESTUDIANTE)).thenReturn(usuario(OTRO_ESTUDIANTE, "Estudiante"));
        lenient().when(usuarioGateway.getUsuario(FUNCIONARIO)).thenReturn(usuario(FUNCIONARIO, "Funcionario Académico"));
        lenient().when(usuarioGateway.getUsuario(OTRO_FUNCIONARIO)).thenReturn(usuario(OTRO_FUNCIONARIO, "Funcionario Académico"));
        lenient().when(usuarioGateway.getUsuario(DECANO)).thenReturn(usuario(DECANO, "Decano"));
        lenient().when(almacenamiento.guardar(anyString(), anyString(), any())).thenReturn(RUTA);
        lenient().when(anexoGateway.guardar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private void verificarQueNoSeGuardoNada() {
        verify(almacenamiento, never()).guardar(anyString(), anyString(), any());
        verify(anexoGateway, never()).guardar(any());
        verify(log, never()).crearLog(any(), any(), any());
    }

    private AnexoAcademico adjuntar(TipoAnexoAcademico tipoAnexo, ArchivoAdjunto archivo, ActorSolicitud actor) {
        return casoDeUso.adjuntarAnexo(SOLICITUD, tipoAnexo == null ? null : tipoAnexo.getUuidTipoAnexoAcademico(), archivo, actor, TOKEN);
    }

    @Test
    void elEstudianteDuenioAdjuntaUnPazYSalvoYQuedaLaFichaCompleta() {
        solicitud(matricula, RADICADA);
        byte[] contenido = pdf(1024);

        AnexoAcademico anexo = adjuntar(porNombre(PAZ_BIBLIOTECAS), archivo("paz y salvo biblioteca.pdf", contenido), estudiante());

        ArgumentCaptor<String> nombreEnDisco = ArgumentCaptor.forClass(String.class);
        verify(almacenamiento).guardar(eq(SOLICITUD), nombreEnDisco.capture(), same(contenido));
        assertEquals(anexo.getUuidAnexoAcademico() + ".pdf", nombreEnDisco.getValue());
        assertTrue(nombreEnDisco.getValue().matches("[0-9a-f\\-]{36}\\.pdf"));
        assertEquals("paz y salvo biblioteca.pdf", anexo.getNombreArchivo());
        assertEquals(RUTA, anexo.getUrlArchivo());
        assertEquals("application/pdf", anexo.getTipoArchivo());
        assertEquals(1024L, anexo.getTamanioBytes());
        assertEquals(ESTUDIANTE, anexo.getUsuario().getUuidUsuario());
        assertEquals(LocalDateTime.of(2026, 3, 10, 10, 0), anexo.getFechaSubida());
        assertEquals(PAZ_BIBLIOTECAS, anexo.getTipoAnexoAcademico().getNombre());
        assertEquals(SOLICITUD, anexo.getSolicitudAcademica().getUuidSolicitudAcademica());
        verify(anexoGateway).guardar(anexo);
    }

    @Test
    void adjuntarUnAnexoEscribeElLogConElRadicadoYLaAccion() {
        solicitud(matricula, RADICADA);

        AnexoAcademico anexo = adjuntar(porNombre(PAZ_BIBLIOTECAS), archivo("paz.pdf", pdf(100)), estudiante());

        ArgumentCaptor<String> resultado = ArgumentCaptor.forClass(String.class);
        verify(log).crearLog(eq("Adjuntar anexo académico"), resultado.capture(), eq(TOKEN));
        assertTrue(resultado.getValue().contains("2026-XX-0001"));
        assertTrue(resultado.getValue().contains("ADJUNTAR_ANEXO"));
        assertTrue(resultado.getValue().contains(PAZ_BIBLIOTECAS));
        assertTrue(resultado.getValue().contains(anexo.getUuidAnexoAcademico()));
    }

    @Test
    void siFallaLaBaseNoSeEscribeElLog() {
        solicitud(matricula, RADICADA);
        when(anexoGateway.guardar(any())).thenThrow(new IllegalStateException("base caida"));

        assertThrows(IllegalStateException.class,
                () -> adjuntar(porNombre(PAZ_BIBLIOTECAS), archivo("paz.pdf", pdf(100)), estudiante()));
        verify(log, never()).crearLog(any(), any(), any());
    }

    @Test
    void elNombreOriginalConRutasPeligrosasSoloConservaElNombreYNoSeUsaEnDisco() {
        solicitud(matricula, RADICADA);

        AnexoAcademico desdeUnix = adjuntar(porNombre(PAZ_BIBLIOTECAS), archivo("../../etc/passwd/paz.pdf", pdf(100)), estudiante());
        AnexoAcademico desdeWindows = adjuntar(porNombre(PAZ_BIBLIOTECAS), archivo("C:\\Windows\\..\\paz2.pdf", pdf(100)), estudiante());

        assertEquals("paz.pdf", desdeUnix.getNombreArchivo());
        assertEquals("paz2.pdf", desdeWindows.getNombreArchivo());
        ArgumentCaptor<String> nombreEnDisco = ArgumentCaptor.forClass(String.class);
        verify(almacenamiento, times(2)).guardar(eq(SOLICITUD), nombreEnDisco.capture(), any());
        nombreEnDisco.getAllValues().forEach(nombre -> assertTrue(nombre.matches("[0-9a-f\\-]{36}\\.pdf")));
    }

    @Test
    void elCarneDeDarcaAceptaJpegConMayusculasYSeGuardaComoJpg() {
        solicitud(matricula, RADICADA);

        AnexoAcademico anexo = adjuntar(porNombre(DARCA), archivo("carne.JPEG", jpg()), estudiante());

        assertEquals("image/jpeg", anexo.getTipoArchivo());
        verify(almacenamiento).guardar(eq(SOLICITUD), eq(anexo.getUuidAnexoAcademico() + ".jpg"), any());
    }

    @Test
    void elCarneDeDarcaAceptaPng() {
        solicitud(matricula, RADICADA);

        assertEquals("image/png", adjuntar(porNombre(DARCA), archivo("carne.png", png()), estudiante()).getTipoArchivo());
    }

    @Test
    void unPazYSalvoNoAceptaImagenes() {
        solicitud(matricula, RADICADA);

        assertThrows(ErrorMalFormatoExcepcion.class, () -> adjuntar(porNombre(PAZ_BIBLIOTECAS), archivo("paz.png", png()), estudiante()));
        assertThrows(ErrorMalFormatoExcepcion.class, () -> adjuntar(porNombre(PAZ_BIBLIOTECAS), archivo("paz.jpg", jpg()), estudiante()));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unArchivoSinExtensionOConExtensionDesconocidaSeRechaza() {
        solicitud(matricula, RADICADA);

        assertThrows(ErrorMalFormatoExcepcion.class, () -> adjuntar(porNombre(DARCA), archivo("carne", pdf(100)), estudiante()));
        assertThrows(ErrorMalFormatoExcepcion.class, () -> adjuntar(porNombre(DARCA), archivo("carne.exe", pdf(100)), estudiante()));
        assertThrows(ErrorMalFormatoExcepcion.class, () -> adjuntar(porNombre(DARCA), archivo("carne.pdf.exe", pdf(100)), estudiante()));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unArchivoCuyoContenidoNoCorrespondeASuExtensionSeRechaza() {
        solicitud(matricula, RADICADA);

        assertThrows(ErrorMalFormatoExcepcion.class, () -> adjuntar(porNombre(PAZ_BIBLIOTECAS), archivo("paz.pdf", png()), estudiante()));
        assertThrows(ErrorMalFormatoExcepcion.class, () -> adjuntar(porNombre(DARCA), archivo("carne.png", jpg()), estudiante()));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unArchivoDeExactamenteCincoMegasSeAcepta() {
        solicitud(matricula, RADICADA);

        assertEquals((long) CINCO_MB, adjuntar(porNombre(PAZ_BIBLIOTECAS), archivo("paz.pdf", pdf(CINCO_MB)), estudiante()).getTamanioBytes());
    }

    @Test
    void unArchivoDeMasDeCincoMegasSeRechaza() {
        solicitud(matricula, RADICADA);

        assertThrows(ErrorMalFormatoExcepcion.class, () -> adjuntar(porNombre(PAZ_BIBLIOTECAS), archivo("paz.pdf", pdf(CINCO_MB + 1)), estudiante()));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unArchivoVacioONuloSeRechaza() {
        solicitud(matricula, RADICADA);

        assertThrows(ErrorMalFormatoExcepcion.class, () -> adjuntar(porNombre(PAZ_BIBLIOTECAS), archivo("paz.pdf", new byte[0]), estudiante()));
        assertThrows(ErrorMalFormatoExcepcion.class, () -> adjuntar(porNombre(PAZ_BIBLIOTECAS), null, estudiante()));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void elSoporteLibreSeAdjuntaEnLasCancelaciones() {
        solicitud(asignatura, RADICADA);

        AnexoAcademico anexo = adjuntar(null, archivo("incapacidad.png", png()), estudiante());

        assertNull(anexo.getTipoAnexoAcademico());
        assertEquals("incapacidad.png", anexo.getNombreArchivo());
    }

    @Test
    void elSupletorioNoAdmiteSoporteLibre() {
        solicitud(supletorio, RADICADA);

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> adjuntar(null, archivo("soporte.pdf", pdf(100)), estudiante()));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unTipoDeAnexoDeOtroProcesoSeRechaza() {
        solicitud(matricula, RADICADA);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> adjuntar(porNombre(FOR_23), archivo("for23.pdf", pdf(100)), estudiante()));
        assertTrue(error.getMessage().contains("no pertenece"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unTipoDeAnexoInexistenteOUnaSolicitudInexistenteSeRechazan() {
        solicitud(matricula, RADICADA);
        when(solicitudGateway.getPorUuid("no-existe")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.adjuntarAnexo(SOLICITUD, "no-existe", archivo("paz.pdf", pdf(100)), estudiante(), TOKEN));
        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.adjuntarAnexo("no-existe", porNombre(PAZ_BIBLIOTECAS).getUuidTipoAnexoAcademico(), archivo("paz.pdf", pdf(100)), estudiante(), TOKEN));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unEstudianteNoAdjuntaEnUnaSolicitudAjena() {
        solicitud(matricula, RADICADA);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> adjuntar(porNombre(PAZ_BIBLIOTECAS), archivo("paz.pdf", pdf(100)), actor(OTRO_ESTUDIANTE, RolEtiquetaEtapa.ESTUDIANTE)));
        assertTrue(error.getMessage().contains("no pertenece"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void elFuncionarioYElDecanoNoAdjuntanLosAnexosDelEstudiante() {
        solicitud(matricula, RADICADA);

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> adjuntar(porNombre(PAZ_BIBLIOTECAS), archivo("paz.pdf", pdf(100)), funcionario()));
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> adjuntar(porNombre(PAZ_BIBLIOTECAS), archivo("paz.pdf", pdf(100)), actor(DECANO, RolEtiquetaEtapa.DECANO)));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void unUsuarioSinElRolDeclaradoNoAdjunta() {
        solicitud(matricula, RADICADA);

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> adjuntar(porNombre(PAZ_BIBLIOTECAS), archivo("paz.pdf", pdf(100)), actor(FUNCIONARIO, RolEtiquetaEtapa.ESTUDIANTE)));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void losAnexosDelEstudianteSoloSeSubenEnRadicada() {
        solicitud(matricula, EN_REVISION_DECANO);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> adjuntar(porNombre(PAZ_BIBLIOTECAS), archivo("paz.pdf", pdf(100)), estudiante()));
        assertTrue(error.getMessage().contains("RADICADA"));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void elFuncionarioAsignadoSubeElReciboTrasLaAprobacionDelDecano() {
        solicitud(supletorio, APROBADA_POR_DECANO);

        AnexoAcademico anexo = adjuntar(porNombre(RECIBO), archivo("recibo.pdf", pdf(200)), funcionario());

        assertEquals(FUNCIONARIO, anexo.getUsuario().getUuidUsuario());
    }

    @Test
    void elReciboNoLoSubeElEstudianteNiUnFuncionarioNoAsignadoNiEnOtraEtapa() {
        solicitud(supletorio, APROBADA_POR_DECANO);
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> adjuntar(porNombre(RECIBO), archivo("recibo.pdf", pdf(200)), estudiante()));
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> adjuntar(porNombre(RECIBO), archivo("recibo.pdf", pdf(200)), actor(OTRO_FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO)));

        solicitud(supletorio, PENDIENTE_PAGO);
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> adjuntar(porNombre(RECIBO), archivo("recibo.pdf", pdf(200)), funcionario()));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void elReciboSoloAceptaPdf() {
        solicitud(supletorio, APROBADA_POR_DECANO);

        assertThrows(ErrorMalFormatoExcepcion.class, () -> adjuntar(porNombre(RECIBO), archivo("recibo.png", png()), funcionario()));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void elEstudianteDuenioSubeElComprobanteEnPendientePago() {
        solicitud(supletorio, PENDIENTE_PAGO);

        assertEquals("image/png", adjuntar(porNombre(COMPROBANTE), archivo("comprobante.png", png()), estudiante()).getTipoArchivo());
    }

    @Test
    void elComprobanteNoSeSubeEnOtraEtapaNiPorOtroActor() {
        solicitud(supletorio, APROBADA_POR_DECANO);
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> adjuntar(porNombre(COMPROBANTE), archivo("c.pdf", pdf(100)), estudiante()));

        solicitud(supletorio, PENDIENTE_PAGO);
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> adjuntar(porNombre(COMPROBANTE), archivo("c.pdf", pdf(100)), funcionario()));
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> adjuntar(porNombre(COMPROBANTE), archivo("c.pdf", pdf(100)), actor(OTRO_ESTUDIANTE, RolEtiquetaEtapa.ESTUDIANTE)));
        verificarQueNoSeGuardoNada();
    }

    @Test
    void siFallaElGuardadoEnBaseSeBorraElArchivoDelDisco() {
        solicitud(matricula, RADICADA);
        RuntimeException fallo = new IllegalStateException("fallo en base");
        when(anexoGateway.guardar(any())).thenThrow(fallo);

        RuntimeException error = assertThrows(RuntimeException.class,
                () -> adjuntar(porNombre(PAZ_BIBLIOTECAS), archivo("paz.pdf", pdf(100)), estudiante()));

        assertSame(fallo, error);
        verify(almacenamiento).eliminar(RUTA);
    }

    @Test
    void siFallanLaBaseYElBorradoSeConservaElErrorOriginal() {
        solicitud(matricula, RADICADA);
        RuntimeException fallo = new IllegalStateException("fallo en base");
        when(anexoGateway.guardar(any())).thenThrow(fallo);
        doThrow(new UncheckedIOException(new IOException("disco"))).when(almacenamiento).eliminar(RUTA);

        RuntimeException error = assertThrows(RuntimeException.class,
                () -> adjuntar(porNombre(PAZ_BIBLIOTECAS), archivo("paz.pdf", pdf(100)), estudiante()));

        assertSame(fallo, error);
        assertEquals(1, error.getSuppressed().length);
    }

    @Test
    void siFallaElDiscoNoSeGuardaLaFicha() {
        solicitud(matricula, RADICADA);
        when(almacenamiento.guardar(anyString(), anyString(), any())).thenThrow(new UncheckedIOException(new IOException("disco lleno")));

        assertThrows(ErrorGenericoExcepcion.class, () -> adjuntar(porNombre(PAZ_BIBLIOTECAS), archivo("paz.pdf", pdf(100)), estudiante()));
        verify(anexoGateway, never()).guardar(any());
        verify(almacenamiento, never()).eliminar(anyString());
    }

    private AnexoAcademico anexoGuardado() {
        AnexoAcademico anexo = AnexoAcademico.builder()
                .uuidAnexoAcademico("anexo-guardado")
                .solicitudAcademica(SolicitudAcademica.builder().uuidSolicitudAcademica(SOLICITUD).build())
                .nombreArchivo("paz.pdf")
                .urlArchivo(RUTA)
                .tipoArchivo("application/pdf")
                .build();
        when(anexoGateway.getPorUuid("anexo-guardado")).thenReturn(anexo);
        return anexo;
    }

    @Test
    void elEstudianteDuenioElFuncionarioAsignadoYElDecanoDescargan() {
        solicitud(matricula, EN_REVISION_DECANO);
        anexoGuardado();
        byte[] contenido = pdf(100);
        when(almacenamiento.leer(RUTA)).thenReturn(contenido);

        for (ActorSolicitud actor : List.of(estudiante(), funcionario(), actor(DECANO, RolEtiquetaEtapa.DECANO))) {
            ArchivoAdjunto descargado = casoDeUso.obtenerAnexo("anexo-guardado", actor);
            assertEquals("paz.pdf", descargado.getNombreOriginal());
            assertEquals("application/pdf", descargado.getTipoContenido());
            assertSame(contenido, descargado.getContenido());
        }
    }

    @Test
    void unEstudianteAjenoOUnFuncionarioNoAsignadoNoDescargan() {
        solicitud(matricula, EN_REVISION_DECANO);
        anexoGuardado();

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.obtenerAnexo("anexo-guardado", actor(OTRO_ESTUDIANTE, RolEtiquetaEtapa.ESTUDIANTE)));
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.obtenerAnexo("anexo-guardado", actor(OTRO_FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO)));
        verify(almacenamiento, never()).leer(anyString());
    }

    @Test
    void unAnexoInexistenteOSinArchivoEnDiscoNoSeDescarga() {
        solicitud(matricula, EN_REVISION_DECANO);
        anexoGuardado();
        when(anexoGateway.getPorUuid("no-existe")).thenReturn(null);
        when(almacenamiento.leer(RUTA)).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.obtenerAnexo("no-existe", estudiante()));
        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.obtenerAnexo("anexo-guardado", estudiante()));
    }

    private List<String> nombres(List<TipoAnexoAcademico> tipos) {
        return tipos.stream().map(TipoAnexoAcademico::getNombre).sorted().toList();
    }

    @Test
    void matriculaExigeSeisYDescuentaLosYaAdjuntados() {
        solicitud(matricula, RADICADA);
        when(anexoGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of());
        assertEquals(6, casoDeUso.verificarAnexosObligatorios(SOLICITUD, null).size());

        when(anexoGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of(
                AnexoAcademico.builder().tipoAnexoAcademico(porNombre(PAZ_BIBLIOTECAS)).build(),
                AnexoAcademico.builder().tipoAnexoAcademico(porNombre(DARCA)).build(),
                AnexoAcademico.builder().tipoAnexoAcademico(null).build()));
        List<TipoAnexoAcademico> faltantes = casoDeUso.verificarAnexosObligatorios(SOLICITUD, CausaSupletorio.CRUCE);

        assertEquals(4, faltantes.size());
        assertFalse(nombres(faltantes).contains(PAZ_BIBLIOTECAS));
        assertFalse(nombres(faltantes).contains(DARCA));
    }

    @Test
    void asignaturaNoExigeAnexos() {
        when(tipoSolicitudGateway.getPorUuid(TIPO_CA)).thenReturn(asignatura);

        assertTrue(casoDeUso.getAnexosObligatorios(TIPO_CA, null).isEmpty());
    }

    @Test
    void supletorioPorOtraCausaExigeElFor23YLaJustificacion() {
        when(tipoSolicitudGateway.getPorUuid(TIPO_ES)).thenReturn(supletorio);

        assertEquals(List.of(FOR_23, JUSTIFICACION).stream().sorted().toList(),
                nombres(casoDeUso.getAnexosObligatorios(TIPO_ES, CausaSupletorio.OTRA)));
    }

    @Test
    void supletorioPorCruceExigeElFor23YElFormatoDelDocenteDelCruce() {
        when(tipoSolicitudGateway.getPorUuid(TIPO_ES)).thenReturn(supletorio);

        assertEquals(List.of(FOR_23, CRUCE).stream().sorted().toList(),
                nombres(casoDeUso.getAnexosObligatorios(TIPO_ES, CausaSupletorio.CRUCE)));
    }

    @Test
    void supletorioDescuentaLoAdjuntadoYNuncaPideReciboNiComprobante() {
        solicitud(supletorio, RADICADA);
        when(anexoGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of(
                AnexoAcademico.builder().tipoAnexoAcademico(porNombre(FOR_23)).build()));

        assertEquals(List.of(JUSTIFICACION), nombres(casoDeUso.verificarAnexosObligatorios(SOLICITUD, CausaSupletorio.OTRA)));
    }

    @Test
    void supletorioSinCausaSeRechaza() {
        when(tipoSolicitudGateway.getPorUuid(TIPO_ES)).thenReturn(supletorio);

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.getAnexosObligatorios(TIPO_ES, null));
    }

    @Test
    void obligatoriosDeUnTipoInexistenteSeRechazan() {
        when(tipoSolicitudGateway.getPorUuid("no-existe")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getAnexosObligatorios("no-existe", null));
    }
}
