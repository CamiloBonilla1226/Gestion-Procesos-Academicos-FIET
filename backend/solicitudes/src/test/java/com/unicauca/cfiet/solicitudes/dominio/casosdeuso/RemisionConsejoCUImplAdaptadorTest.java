package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.RespuestaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.TramiteCancelacionAsignaturaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.AlmacenamientoAnexosIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.AnexoAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.RemisionConsejoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ResolucionAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.RespuestaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudCancelacionAsignaturaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudConsejoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.AnexoAcademicoConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Anexo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AnexoAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Asignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaMatriculada;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.DecisionAsignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.DetalleRemisionConsejo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Estudiante;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EvaluacionAsignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Funcionario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.OrdenDelDia;
import com.unicauca.cfiet.solicitudes.dominio.modelos.RemisionConsejo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ResolucionAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Respuesta;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SesionConsejo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Solicitud;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudCancelacionAsignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoAnexoAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitud;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudConsejo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorGenericoExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorReglaNegocioVioladaExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemisionConsejoCUImplAdaptadorTest {

    private static final String TOKEN = "token";
    private static final String SOLICITUD = "11111111-1111-1111-1111-111111111111";
    private static final String PROCESO = "proceso-asignatura";
    private static final String TIPO = "tipo-julian";
    private static final String URL_APLICACION = "/api/unicauca/fiet/consejo/";
    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final ZonedDateTime AHORA = ZonedDateTime.of(2026, 11, 27, 10, 0, 0, 0, BOGOTA);

    @Mock
    private TramiteCancelacionAsignaturaCUIntPuerto tramiteCU;

    @Mock
    private TipoSolicitudConsejoGatewayIntPuerto tipoConsejoGateway;

    @Mock
    private TipoSolicitudAcademicaGatewayIntPuerto tipoAcademicoGateway;

    @Mock
    private TipoSolicitudGatewayIntPuerto tipoSolicitudGateway;

    @Mock
    private SolicitudGatewayIntPuerto solicitudGateway;

    @Mock
    private RemisionConsejoGatewayIntPuerto remisionGateway;

    @Mock
    private AnexoAcademicoGatewayIntPuerto anexoGateway;

    @Mock
    private AlmacenamientoAnexosIntPuerto almacenamiento;

    @Mock
    private SolicitudCancelacionAsignaturaGatewayIntPuerto cancelacionGateway;

    @Mock
    private RespuestaCUIntPuerto respuestaCU;

    @Mock
    private RespuestaGatewayIntPuerto respuestaGateway;

    @Mock
    private ResolucionAcademicaGatewayIntPuerto resolucionGateway;

    @Mock
    private LogCUIntPuerto log;

    private RemisionConsejoCUImplAdaptador casoDeUso;
    private final List<EvaluacionAsignatura> evaluaciones = List.of(EvaluacionAsignatura.builder().uuidAsignaturaSolicitud("fila").build());

    @BeforeEach
    void setUp() {
        casoDeUso = new RemisionConsejoCUImplAdaptador(tramiteCU, tipoConsejoGateway, tipoAcademicoGateway, tipoSolicitudGateway,
                solicitudGateway, remisionGateway, anexoGateway, almacenamiento, cancelacionGateway, respuestaCU, respuestaGateway,
                resolucionGateway, new ExcepcionesFormateadorImplAdaptador(), log,
                Clock.fixed(AHORA.toInstant(), BOGOTA), URL_APLICACION);
        lenient().when(tipoAcademicoGateway.getTodos()).thenReturn(List.of(
                TipoSolicitudAcademica.builder().uuidTipoSolicitudAcademica("otro").nombre("Examen Supletorio").build(),
                TipoSolicitudAcademica.builder().uuidTipoSolicitudAcademica(PROCESO).nombre("Cancelación de Asignatura").build()));
    }

    private TipoSolicitud tipoJulian() {
        return TipoSolicitud.builder()
                .uuidTipoSolicitud(TIPO)
                .nombre("Cancelación de asignatura al Consejo")
                .seccion("asuntos pregrado")
                .objFuncionarioEncargado(Funcionario.builder().uuidUsuario("funcionario").build())
                .build();
    }

    private void conTipoEnlazado() {
        when(tipoConsejoGateway.getPorTipoAcademico(PROCESO)).thenReturn(TipoSolicitudConsejo.builder().uuidTipoSolicitud(TIPO).build());
        when(tipoSolicitudGateway.getTipoSolicitud(TIPO)).thenReturn(tipoJulian());
    }

    private SolicitudCancelacionAsignatura cancelacion() {
        Usuario usuario = Usuario.builder().uuidUsuario("estudiante").nombres("Laura").apellidos("Gómez")
                .tipoDocumento("Cédula de ciudadanía").numeroDocumento("1061").telefono("300").correoElectronico("laura@unicauca.edu.co").build();
        SolicitudAcademica solicitud = SolicitudAcademica.builder()
                .uuidSolicitudAcademica(SOLICITUD)
                .radicado("2026-CA-0007")
                .estudiante(Estudiante.builder().uuidUsuario("estudiante").usuario(usuario).build())
                .build();
        AsignaturaSolicitudAcademica fila = AsignaturaSolicitudAcademica.builder()
                .uuidAsignaturaSolicitud("fila")
                .asignaturaMatriculada(AsignaturaMatriculada.builder()
                        .asignatura(Asignatura.builder().codigoAsignatura("PIS-014").nombreAsignatura("Cálculo I").build())
                        .build())
                .build();
        return SolicitudCancelacionAsignatura.builder()
                .solicitudAcademica(solicitud)
                .motivoCancelacion("Calamidad doméstica")
                .asignaturas(List.of(fila))
                .build();
    }

    private AnexoAcademico anexo(String nombreTipo, String url, LocalDateTime fecha) {
        return AnexoAcademico.builder()
                .tipoAnexoAcademico(nombreTipo == null ? null : TipoAnexoAcademico.builder().nombre(nombreTipo).build())
                .urlArchivo(url)
                .fechaSubida(fecha)
                .build();
    }

    @Test
    void sinTipoEnlazadoNoSeRemiteNiSeEscribeNada() {
        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.remitir(SOLICITUD, evaluaciones, null, TOKEN));

        assertEquals(MensajesError.SIN_TIPO_CONSEJO, error.getMessage());
        verifyNoInteractions(tramiteCU, solicitudGateway, remisionGateway, almacenamiento);
    }

    @Test
    void unEnlaceCuyoTipoYaNoExisteTambienSeRechaza() {
        when(tipoConsejoGateway.getPorTipoAcademico(PROCESO)).thenReturn(TipoSolicitudConsejo.builder().uuidTipoSolicitud(TIPO).build());

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.remitir(SOLICITUD, evaluaciones, null, TOKEN));
        verifyNoInteractions(tramiteCU);
    }

    @Test
    void remiteYCreaLaEspejoPendienteConElEstudianteElFormatoVigenteYLosSoportes() {
        conTipoEnlazado();
        SolicitudCancelacionAsignatura cancelacion = cancelacion();
        when(tramiteCU.remitirAConsejo(SOLICITUD, evaluaciones, "obs", TOKEN)).thenReturn(cancelacion);
        String formato = AnexoAcademicoConstantes.FORMATO_FIRMADO_ESTUDIANTE;
        LocalDateTime dia = LocalDateTime.of(2026, 11, 20, 8, 0);
        when(anexoGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of(
                anexo(formato, "/api/anexos/" + SOLICITUD + "/viejo.pdf", dia),
                anexo(null, "/api/anexos/" + SOLICITUD + "/soporte.jpg", dia.plusHours(2)),
                anexo(formato, "/api/anexos/" + SOLICITUD + "/vigente.pdf", dia.plusHours(1)),
                anexo("Formato firmado por el Decano", "/api/anexos/" + SOLICITUD + "/decano.pdf", dia.plusHours(3))));
        when(almacenamiento.leer(anyString())).thenAnswer(invocacion -> ((String) invocacion.getArgument(0)).getBytes());
        when(almacenamiento.guardar(anyString(), anyString(), any())).thenAnswer(invocacion ->
                "/api/anexos/" + invocacion.getArgument(0) + "/" + invocacion.getArgument(1));
        when(solicitudGateway.guardarSolicitud(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        SolicitudCancelacionAsignatura resultado = casoDeUso.remitir(SOLICITUD, evaluaciones, "obs", TOKEN);

        assertSame(cancelacion, resultado);
        ArgumentCaptor<Solicitud> espejo = ArgumentCaptor.forClass(Solicitud.class);
        verify(solicitudGateway).guardarSolicitud(espejo.capture());
        Solicitud guardada = espejo.getValue();
        assertEquals("PENDIENTE AL ORDEN DEL DÍA", guardada.getEstado());
        assertEquals(TIPO, guardada.getObjTipoSolicitud().getUuidTipoSolicitud());
        assertEquals("funcionario", guardada.getObjFuncionario().getUuidUsuario());
        assertEquals("2026-CA-0007", guardada.getConsecutivo());
        assertEquals("Cancelación de asignatura - Radicado 2026-CA-0007 - Laura Gómez", guardada.getNombre());
        assertEquals("Asignaturas: PIS-014 Cálculo I. Motivo: Calamidad doméstica", guardada.getDescripcion());
        assertEquals("1061", guardada.getInformacionSolicitante().getNumeroDocumento());
        assertEquals("laura@unicauca.edu.co", guardada.getInformacionSolicitante().getCorreoElectronico());
        assertSame(guardada, guardada.getInformacionSolicitante().getSolicitud());

        assertEquals(List.of("Formato de la solicitud firmado por el estudiante", "Soporte del motivo 1"),
                guardada.getAnexos().stream().map(Anexo::getNombre).toList());
        verify(almacenamiento).leer("/api/anexos/" + SOLICITUD + "/vigente.pdf");
        verify(almacenamiento).leer("/api/anexos/" + SOLICITUD + "/soporte.jpg");
        verify(almacenamiento, never()).leer("/api/anexos/" + SOLICITUD + "/viejo.pdf");
        verify(almacenamiento, never()).leer("/api/anexos/" + SOLICITUD + "/decano.pdf");
        for (Anexo copia : guardada.getAnexos()) {
            assertTrue(copia.getUrlAnexo().startsWith(URL_APLICACION + "solicitudes/anexos/" + guardada.getUuidSolicitud() + "/"));
            assertSame(guardada, copia.getObjSolicitud());
        }
        assertTrue(guardada.getAnexos().get(1).getUrlAnexo().endsWith(".jpg"));
        verify(almacenamiento, times(2)).guardar(eq(guardada.getUuidSolicitud()), anyString(), any());

        ArgumentCaptor<RemisionConsejo> remision = ArgumentCaptor.forClass(RemisionConsejo.class);
        verify(remisionGateway).guardar(remision.capture());
        assertEquals(guardada.getUuidSolicitud(), remision.getValue().getUuidSolicitud());
        assertEquals(SOLICITUD, remision.getValue().getSolicitudAcademica().getUuidSolicitudAcademica());
        assertEquals(AHORA.toLocalDateTime(), remision.getValue().getFechaRemision());
        verify(log).crearLog(eq("Remitir cancelación de asignatura al Consejo"), contains("2026-CA-0007"), eq(TOKEN));
    }

    private static final String ESPEJO = "22222222-2222-2222-2222-222222222222";

    private RemisionConsejo remision() {
        return RemisionConsejo.builder()
                .solicitudAcademica(SolicitudAcademica.builder().uuidSolicitudAcademica(SOLICITUD).build())
                .uuidSolicitud(ESPEJO)
                .build();
    }

    private Solicitud espejo(OrdenDelDia orden) {
        return Solicitud.builder().uuidSolicitud(ESPEJO).nombre("Cancelación de asignatura - Radicado 2026-CA-0007").estado("AGREGADO EN EL ORDEN DEL DÍA")
                .objOrdenDelDia(orden).build();
    }

    private OrdenDelDia orden() {
        return OrdenDelDia.builder().uuidOrdenDelDia("orden").fecha("2026-11-20").numeroActa("045").build();
    }

    private SolicitudCancelacionAsignatura decidida(boolean... aprobadas) {
        SolicitudCancelacionAsignatura cancelacion = cancelacion();
        List<AsignaturaSolicitudAcademica> filas = new java.util.ArrayList<>();
        for (int i = 0; i < aprobadas.length; i++)
            filas.add(AsignaturaSolicitudAcademica.builder()
                    .uuidAsignaturaSolicitud("fila" + i)
                    .aprobadaPorDecano(aprobadas[i])
                    .asignaturaMatriculada(AsignaturaMatriculada.builder()
                            .asignatura(Asignatura.builder().codigoAsignatura("PIS-01" + i).nombreAsignatura("Materia " + i).build())
                            .build())
                    .build());
        cancelacion.setAsignaturas(filas);
        return cancelacion;
    }

    @Test
    void unaSolicitudQueNoVinoDelConsejoNoTieneDetalle() {
        assertNull(casoDeUso.getDetalle(ESPEJO));
        verifyNoInteractions(solicitudGateway, cancelacionGateway);
    }

    @Test
    void elDetalleTraeLaCancelacionLaOrdenYLaRespuesta() {
        when(remisionGateway.getPorSolicitud(ESPEJO)).thenReturn(remision());
        when(solicitudGateway.getSolicitud(ESPEJO)).thenReturn(espejo(orden()));
        when(cancelacionGateway.getPorSolicitud(SOLICITUD)).thenReturn(cancelacion());
        when(respuestaGateway.getRespuestaPorSolicitud(ESPEJO)).thenReturn(Respuesta.builder().uuidRespuesta("respuesta").build());

        DetalleRemisionConsejo detalle = casoDeUso.getDetalle(ESPEJO);

        assertEquals("2026-CA-0007", detalle.getCancelacion().getSolicitudAcademica().getRadicado());
        assertEquals("045", detalle.getOrdenDelDia().getNumeroActa());
        assertEquals("respuesta", detalle.getRespuesta().getUuidRespuesta());
    }

    @Test
    void sinOrdenDelDiaNoSeRegistraLaDecision() {
        when(remisionGateway.getPorSolicitud(ESPEJO)).thenReturn(remision());
        when(solicitudGateway.getSolicitud(ESPEJO)).thenReturn(espejo(null));

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.registrarDecision(ESPEJO, List.of(), null, null, null, TOKEN));

        assertEquals(String.format(MensajesError.ESPEJO_SIN_ORDEN_DEL_DIA, "Cancelación de asignatura - Radicado 2026-CA-0007"), error.getMessage());
        verifyNoInteractions(tramiteCU, respuestaCU);
    }

    @Test
    void unaSolicitudAjenaAlConsejoNoAdmiteDecision() {
        assertThrows(com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.registrarDecision(ESPEJO, List.of(), null, null, null, TOKEN));
        verifyNoInteractions(tramiteCU, respuestaCU);
    }

    @Test
    void laDecisionParcialEscribeLaRespuestaDeJulianConLaSesionYElActa() {
        List<DecisionAsignatura> decisiones = List.of(DecisionAsignatura.builder().uuidAsignaturaSolicitud("fila0").aprobada(true).build());
        when(remisionGateway.getPorSolicitud(ESPEJO)).thenReturn(remision());
        when(solicitudGateway.getSolicitud(ESPEJO)).thenReturn(espejo(orden()));
        when(tramiteCU.decidirPorConsejo(SOLICITUD, decisiones, TOKEN)).thenReturn(decidida(true, false));
        when(respuestaCU.registrarRespuesta(eq(ESPEJO), any(), eq(TOKEN))).thenAnswer(invocacion -> {
            Respuesta respuesta = invocacion.getArgument(1);
            respuesta.setUuidRespuesta("respuesta");
            return respuesta;
        });

        DetalleRemisionConsejo detalle = casoDeUso.registrarDecision(ESPEJO, decisiones, " ", " 123 ", "Revisar", TOKEN);

        ArgumentCaptor<Respuesta> respuesta = ArgumentCaptor.forClass(Respuesta.class);
        verify(respuestaCU).registrarRespuesta(eq(ESPEJO), respuesta.capture(), eq(TOKEN));
        assertEquals("Resolución", respuesta.getValue().getTipoRespuesta());
        assertEquals("123", respuesta.getValue().getConsecutivoFiet());
        assertEquals("Revisar", respuesta.getValue().getIndicaciones());
        assertEquals("El Consejo de Facultad en la sesión del 20 de noviembre de 2026, acta N° 045, aprueba la cancelación de la(s) asignatura(s) "
                + "PIS-010 Materia 0 y niega la cancelación de la(s) asignatura(s) PIS-011 Materia 1 en la solicitud 2026-CA-0007 de Laura Gómez.",
                respuesta.getValue().getRespuestaConsejo());
        assertEquals("respuesta", detalle.getRespuesta().getUuidRespuesta());
        verify(log).crearLog(eq("Registrar decisión del Consejo en la orden del día"), contains(ESPEJO), eq(TOKEN));
    }

    @Test
    void sinAprobadasLaRespuestaSoloNiega() {
        when(remisionGateway.getPorSolicitud(ESPEJO)).thenReturn(remision());
        when(solicitudGateway.getSolicitud(ESPEJO)).thenReturn(espejo(OrdenDelDia.builder().uuidOrdenDelDia("orden").fecha("martes 3").build()));
        when(tramiteCU.decidirPorConsejo(eq(SOLICITUD), any(), eq(TOKEN))).thenReturn(decidida(false));
        when(respuestaCU.registrarRespuesta(eq(ESPEJO), any(), eq(TOKEN))).thenAnswer(invocacion -> invocacion.getArgument(1));

        casoDeUso.registrarDecision(ESPEJO, List.of(), "Oficio", null, null, TOKEN);

        ArgumentCaptor<Respuesta> respuesta = ArgumentCaptor.forClass(Respuesta.class);
        verify(respuestaCU).registrarRespuesta(eq(ESPEJO), respuesta.capture(), eq(TOKEN));
        assertEquals("Oficio", respuesta.getValue().getTipoRespuesta());
        assertEquals("El Consejo de Facultad en la sesión del martes 3, niega la cancelación de la(s) asignatura(s) PIS-010 Materia 0 "
                + "en la solicitud 2026-CA-0007 de Laura Gómez.", respuesta.getValue().getRespuestaConsejo());
    }

    @Test
    void unConsecutivoMuyLargoSeRechazaAntesDeDecidir() {
        when(remisionGateway.getPorSolicitud(ESPEJO)).thenReturn(remision());
        when(solicitudGateway.getSolicitud(ESPEJO)).thenReturn(espejo(orden()));

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.registrarDecision(ESPEJO, List.of(), null, "1".repeat(1001), null, TOKEN));
        verifyNoInteractions(tramiteCU);
    }

    @Test
    void laSesionTraeFechaActaYConsecutivo() {
        when(remisionGateway.getPorSolicitudAcademica(SOLICITUD)).thenReturn(remision());
        when(solicitudGateway.getSolicitud(ESPEJO)).thenReturn(espejo(orden()));
        when(respuestaGateway.getRespuestaPorSolicitud(ESPEJO)).thenReturn(Respuesta.builder().consecutivoFiet(" 8.4.2-4.10/123 ").build());

        SesionConsejo sesion = casoDeUso.getSesion(SOLICITUD);

        assertEquals("20 de noviembre de 2026", sesion.getFecha());
        assertEquals("045", sesion.getNumeroActa());
        assertEquals("8.4.2-4.10/123", sesion.getConsecutivo());
        assertNull(casoDeUso.getSesion("otra"));
    }

    @Test
    void cerrarAdjuntaLaResolucionALaRespuestaYDejaLaEspejoRespondida() {
        Respuesta respuesta = Respuesta.builder().uuidRespuesta("33333333-3333-3333-3333-333333333333").urlRespuesta("/api/x/respuestas/r/anterior.pdf").build();
        Solicitud espejo = espejo(orden());
        when(remisionGateway.getPorSolicitudAcademica(SOLICITUD)).thenReturn(remision());
        when(solicitudGateway.getSolicitud(ESPEJO)).thenReturn(espejo);
        when(respuestaGateway.getRespuestaPorSolicitud(ESPEJO)).thenReturn(respuesta);
        when(resolucionGateway.getPorSolicitud(SOLICITUD)).thenReturn(ResolucionAcademica.builder().urlArchivo("/api/anexos/" + SOLICITUD + "/res.pdf").build());
        when(almacenamiento.leer("/api/anexos/" + SOLICITUD + "/res.pdf")).thenReturn(new byte[]{1, 2});
        when(almacenamiento.guardarRespuesta(respuesta.getUuidRespuesta(), "resolucion.pdf", new byte[]{1, 2})).thenReturn("nuevo.pdf");

        casoDeUso.cerrar(SOLICITUD, TOKEN);

        assertEquals(URL_APLICACION + "respuestas/" + respuesta.getUuidRespuesta() + "/nuevo.pdf", respuesta.getUrlRespuesta());
        verify(respuestaGateway).guardarRespuesta(respuesta);
        verify(almacenamiento).eliminarRespuestaTrasConfirmar(respuesta.getUuidRespuesta(), "anterior.pdf");
        assertEquals("RESPONDIDA", espejo.getEstado());
        verify(solicitudGateway).guardarSolicitud(espejo);
    }

    @Test
    void cerrarUnaSolicitudQueNoFueAlConsejoNoHaceNada() {
        casoDeUso.cerrar(SOLICITUD, TOKEN);

        verifyNoInteractions(solicitudGateway, respuestaGateway, almacenamiento, log);
    }

    @Test
    void siNoSePuedeCopiarLaResolucionNoSeCierra() {
        when(remisionGateway.getPorSolicitudAcademica(SOLICITUD)).thenReturn(remision());
        when(solicitudGateway.getSolicitud(ESPEJO)).thenReturn(espejo(orden()));
        when(respuestaGateway.getRespuestaPorSolicitud(ESPEJO)).thenReturn(Respuesta.builder().uuidRespuesta("r").build());
        when(resolucionGateway.getPorSolicitud(SOLICITUD)).thenReturn(ResolucionAcademica.builder().urlArchivo("/api/anexos/x/res.pdf").build());

        assertThrows(ErrorGenericoExcepcion.class, () -> casoDeUso.cerrar(SOLICITUD, TOKEN));
        verify(solicitudGateway, never()).guardarSolicitud(any());
    }

    @Test
    void siFallaLaCopiaDeUnAnexoNoSeGuardaLaEspejo() {
        conTipoEnlazado();
        when(tramiteCU.remitirAConsejo(SOLICITUD, evaluaciones, null, TOKEN)).thenReturn(cancelacion());
        when(anexoGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of(anexo(null, "/api/anexos/" + SOLICITUD + "/soporte.pdf", null)));
        when(almacenamiento.leer(anyString())).thenThrow(new IllegalStateException("sin archivo"));

        ErrorGenericoExcepcion error = assertThrows(ErrorGenericoExcepcion.class, () -> casoDeUso.remitir(SOLICITUD, evaluaciones, null, TOKEN));

        assertEquals(MensajesError.ERROR_GUARDANDO_ARCHIVO, error.getMessage());
        verifyNoInteractions(solicitudGateway, remisionGateway);
    }
}
