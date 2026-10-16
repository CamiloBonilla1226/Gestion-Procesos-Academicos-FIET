package com.unicauca.cfiet.solicitudes.dominio.servicios;

import com.unicauca.cfiet.solicitudes.aplicacion.output.AnexoAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.CorreoIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.GeneradorPdfRespuestaIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ResolucionAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudCancelacionAsignaturaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudExamenSupletorioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.output.correo.PreparacionCorreoImplAdaptador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.APROBADA;
import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.RECHAZADA;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NotificadorRespuestaFinalTest {

    private static final String SOLICITUD = "sol-1";
    private static final String DARCA = "darca@unicauca.edu.co";
    private static final LocalDate FECHA = LocalDate.of(2026, 10, 15);
    private static final byte[] PDF = {37, 80, 68, 70};
    private static final String SALTO = System.lineSeparator();

    private final SolicitudCancelacionAsignaturaGatewayIntPuerto cancelacionGateway = mock(SolicitudCancelacionAsignaturaGatewayIntPuerto.class);
    private final SolicitudExamenSupletorioGatewayIntPuerto supletorioGateway = mock(SolicitudExamenSupletorioGatewayIntPuerto.class);
    private final ResolucionAcademicaGatewayIntPuerto resolucionGateway = mock(ResolucionAcademicaGatewayIntPuerto.class);
    private final AnexoAcademicoGatewayIntPuerto anexoGateway = mock(AnexoAcademicoGatewayIntPuerto.class);
    private final UsuarioGatewayIntPuerto usuarioGateway = mock(UsuarioGatewayIntPuerto.class);
    private final CorreoIntPuerto correo = mock(CorreoIntPuerto.class);
    private final List<ResumenRespuesta> generados = new ArrayList<>();
    private final GeneradorPdfRespuestaIntPuerto generador = resumen -> {
        generados.add(resumen);
        return PDF;
    };

    private NotificadorRespuestaFinal notificador;

    @BeforeEach
    void setUp() {
        notificador = new NotificadorRespuestaFinal(cancelacionGateway, supletorioGateway, resolucionGateway, anexoGateway, usuarioGateway,
                generador, new PreparacionCorreoImplAdaptador(), correo, DARCA);
        when(anexoGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of());
    }

    private SolicitudAcademica solicitud(String radicado, String etapa) {
        return SolicitudAcademica.builder()
                .uuidSolicitudAcademica(SOLICITUD)
                .radicado(radicado)
                .estudiante(Estudiante.builder()
                        .uuidUsuario("est-1")
                        .codigoEstudiantil("104618021")
                        .programaAcademico(ProgramaAcademico.builder().nombre("Ingeniería de Sistemas").sigla("ISI").build())
                        .usuario(Usuario.builder().uuidUsuario("est-1").nombres("Ana María").apellidos("Muñoz Peña")
                                .correoElectronico("est@unicauca.edu.co").build())
                        .build())
                .etapa(EtapaSolicitudAcademica.builder().codigo(etapa).build())
                .build();
    }

    private AsignaturaSolicitudAcademica asignatura(String codigo, String nombre, Boolean aprobadaPorDecano, String situacion) {
        return AsignaturaSolicitudAcademica.builder()
                .uuidAsignaturaSolicitud("as-" + codigo)
                .asignaturaMatriculada(AsignaturaMatriculada.builder()
                        .asignatura(Asignatura.builder().codigoAsignatura(codigo).nombreAsignatura(nombre).build()).build())
                .aprobadaPorDecano(aprobadaPorDecano)
                .situacionCancelar(situacion == null ? null : SituacionAcademicaAsignatura.builder().nombre(situacion).build())
                .build();
    }

    private void cancelacionCon(AsignaturaSolicitudAcademica... asignaturas) {
        when(cancelacionGateway.getPorSolicitud(SOLICITUD)).thenReturn(SolicitudCancelacionAsignatura.builder()
                .asignaturas(new ArrayList<>(List.of(asignaturas))).build());
    }

    private void supletorioCon(Docente docente) {
        when(supletorioGateway.getPorSolicitud(SOLICITUD)).thenReturn(SolicitudExamenSupletorio.builder()
                .asignaturaMatriculada(AsignaturaMatriculada.builder()
                        .asignatura(Asignatura.builder().codigoAsignatura("ISI-004").nombreAsignatura("Señales y Sistemas").build()).build())
                .docente(docente)
                .build());
    }

    private Docente docente() {
        return Docente.builder().uuidUsuario("doc-1").nombres("Luis").apellidos("Ordóñez").build();
    }

    private void conResolucionYAdicional() {
        when(resolucionGateway.getPorSolicitud(SOLICITUD)).thenReturn(ResolucionAcademica.builder().urlArchivo("/api/anexos/sol-1/res.pdf").build());
        when(anexoGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of(
                AnexoAcademico.builder().nombreArchivo("acta.pdf").tipoArchivo("application/pdf").urlArchivo("/api/anexos/sol-1/acta.pdf")
                        .tipoAnexoAcademico(TipoAnexoAcademico.builder().nombre("Documento adicional de la respuesta").build()).build(),
                AnexoAcademico.builder().nombreArchivo("carta.pdf").urlArchivo("/api/anexos/sol-1/carta.pdf")
                        .tipoAnexoAcademico(TipoAnexoAcademico.builder().nombre("Carta de solicitud").build()).build()));
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<List<AdjuntoCorreo>> capturaAdjuntos() {
        return ArgumentCaptor.forClass(List.class);
    }

    @Test
    void darcaRecibeLaCancelacionDeUnaAsignaturaAprobadaConPdfResolucionYDocumentoAdicional() {
        cancelacionCon(asignatura("ISI-001", "Cálculo I", true, "Bajo rendimiento"));
        conResolucionYAdicional();

        notificador.avisarDarca(solicitud("2026-CA-0003", APROBADA), TipoProcesoAcademico.CANCELACION_ASIGNATURA, AccionEtapa.ENVIAR_RESPUESTA, FECHA);

        ArgumentCaptor<String> texto = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<AdjuntoCorreo>> adjuntos = capturaAdjuntos();
        verify(correo, times(1)).enviar(eq(DARCA), eq("DARCA - Notificación de Cancelación de asignatura - Radicado 2026-CA-0003"),
                texto.capture(), adjuntos.capture());
        assertEquals("Popayán, Cauca, 15 de octubre de 2026" + SALTO + SALTO
                + "El estudiante Ana María Muñoz Peña, con código 104618021, cancela la asignatura Cálculo I (aprobada)." + SALTO + SALTO
                + "La información de la respuesta está en el PDF adjunto.", texto.getValue());
        assertEquals(List.of("Radicado-2026-CA-0003.pdf", "Resolucion-2026-CA-0003.pdf", "acta.pdf"),
                adjuntos.getValue().stream().map(AdjuntoCorreo::getNombre).toList());
        assertArrayEquals(PDF, adjuntos.getValue().get(0).getContenido());
        assertEquals("application/pdf", adjuntos.getValue().get(0).getTipoContenido());
        assertEquals("/api/anexos/sol-1/res.pdf", adjuntos.getValue().get(1).getUrlArchivo());

        ResumenRespuesta resumen = generados.get(0);
        assertEquals("2026-CA-0003", resumen.getRadicado());
        assertEquals("Cancelación de Asignatura", resumen.getTipoSolicitud());
        assertEquals(FECHA, resumen.getFechaRespuesta());
        assertEquals("Ana María Muñoz Peña", resumen.getEstudiante());
        assertEquals("104618021", resumen.getCodigoEstudiantil());
        assertEquals("Ingeniería de Sistemas", resumen.getPrograma());
        assertEquals("Aprobada", resumen.getEstadoFinal());
        assertEquals("Bajo rendimiento", resumen.getAsignaturas().get(0).getSituacionAlCancelar());
    }

    @Test
    void conVariasAsignaturasLaFraseCuentaAprobadasYRechazadasYElPdfDetallaCadaUna() {
        cancelacionCon(asignatura("ISI-001", "Cálculo I", true, "Bajo rendimiento"),
                asignatura("ISI-002", "Física I", false, null),
                asignatura("ISI-003", "Álgebra", true, "Calamidad"));

        notificador.avisarDarca(solicitud("2026-CA-0004", APROBADA), TipoProcesoAcademico.CANCELACION_ASIGNATURA, AccionEtapa.ENVIAR_RESPUESTA, FECHA);

        ArgumentCaptor<String> texto = ArgumentCaptor.forClass(String.class);
        verify(correo).enviar(eq(DARCA), any(), texto.capture(), any());
        assertTrue(texto.getValue().contains("con código 104618021, solicita cancelar 3 asignaturas: 2 aprobadas y 1 rechazada."));
        List<AsignaturaResumenRespuesta> asignaturas = generados.get(0).getAsignaturas();
        assertEquals(List.of("ISI-001", "ISI-002", "ISI-003"), asignaturas.stream().map(AsignaturaResumenRespuesta::getCodigo).toList());
        assertEquals(List.of("Aprobada", "Rechazada", "Aprobada"), asignaturas.stream().map(AsignaturaResumenRespuesta::getDecision).toList());
        assertEquals(List.of("Bajo rendimiento", "No aplica", "Calamidad"),
                asignaturas.stream().map(AsignaturaResumenRespuesta::getSituacionAlCancelar).toList());
    }

    @Test
    void elRechazoDelFuncionarioEnAsignaturaMarcaTodasRechazadas() {
        cancelacionCon(asignatura("ISI-001", "Cálculo I", null, "Bajo rendimiento"));

        notificador.avisarDarca(solicitud("2026-CA-0005", RECHAZADA), TipoProcesoAcademico.CANCELACION_ASIGNATURA,
                AccionEtapa.RECHAZAR_FUNCIONARIO, FECHA);

        ArgumentCaptor<String> texto = ArgumentCaptor.forClass(String.class);
        verify(correo).enviar(eq(DARCA), any(), texto.capture(), any());
        assertTrue(texto.getValue().contains("solicita cancelar la asignatura Cálculo I (rechazada)."));
        assertEquals("Rechazada", generados.get(0).getEstadoFinal());
        assertEquals("No aplica", generados.get(0).getAsignaturas().get(0).getSituacionAlCancelar());
    }

    @Test
    void darcaRecibeLaMatriculaRechazadaConLaDecisionDeLaSolicitud() {
        when(resolucionGateway.getPorSolicitud(SOLICITUD)).thenReturn(ResolucionAcademica.builder().urlArchivo("/api/anexos/sol-1/res.pdf").build());

        notificador.avisarDarca(solicitud("2026-CM-0002", RECHAZADA), TipoProcesoAcademico.CANCELACION_MATRICULA, AccionEtapa.ENVIAR_RESPUESTA, FECHA);

        ArgumentCaptor<String> texto = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<AdjuntoCorreo>> adjuntos = capturaAdjuntos();
        verify(correo).enviar(eq(DARCA), eq("DARCA - Notificación de Cancelación de matrícula - Radicado 2026-CM-0002"), texto.capture(),
                adjuntos.capture());
        assertTrue(texto.getValue().contains("El estudiante Ana María Muñoz Peña, con código 104618021, solicita cancelar su matrícula (rechazada)."));
        assertEquals(List.of("Radicado-2026-CM-0002.pdf", "Resolucion-2026-CM-0002.pdf"),
                adjuntos.getValue().stream().map(AdjuntoCorreo::getNombre).toList());
        assertEquals("Decisión de la solicitud", generados.get(0).getDetalles().get(0).getEtiqueta());
        assertEquals("Rechazada", generados.get(0).getDetalles().get(0).getValor());
        assertTrue(generados.get(0).getAsignaturas().isEmpty());
        verifyNoInteractions(cancelacionGateway, supletorioGateway);
    }

    @Test
    void enElRechazoDirectoDarcaRecibeElPdfYElDocumentoAdicionalSinResolucion() {
        conResolucionYAdicional();

        notificador.avisarDarca(solicitud("2026-CM-0009", RECHAZADA), TipoProcesoAcademico.CANCELACION_MATRICULA, AccionEtapa.RECHAZAR_FUNCIONARIO, FECHA);

        ArgumentCaptor<String> texto = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<AdjuntoCorreo>> adjuntos = capturaAdjuntos();
        verify(correo).enviar(eq(DARCA), eq("DARCA - Notificación de Cancelación de matrícula - Radicado 2026-CM-0009"), texto.capture(), adjuntos.capture());
        assertEquals(List.of("Radicado-2026-CM-0009.pdf", "acta.pdf"), adjuntos.getValue().stream().map(AdjuntoCorreo::getNombre).toList());
        assertTrue(texto.getValue().contains("solicita cancelar su matrícula (rechazada)."));
        verify(resolucionGateway, never()).getPorSolicitud(any());
    }

    @Test
    void elRechazoDirectoEnAsignaturaTampocoAdjuntaResolucion() {
        cancelacionCon(asignatura("ISI-001", "Cálculo I", null, null));
        conResolucionYAdicional();

        notificador.avisarDarca(solicitud("2026-CA-0009", RECHAZADA), TipoProcesoAcademico.CANCELACION_ASIGNATURA, AccionEtapa.RECHAZAR_FUNCIONARIO, FECHA);

        ArgumentCaptor<List<AdjuntoCorreo>> adjuntos = capturaAdjuntos();
        verify(correo).enviar(eq(DARCA), any(), any(), adjuntos.capture());
        assertEquals(List.of("Radicado-2026-CA-0009.pdf", "acta.pdf"), adjuntos.getValue().stream().map(AdjuntoCorreo::getNombre).toList());
    }

    @Test
    void sinResolucionCargadaLaRespuestaFinalSaleSoloConElPdf() {
        when(resolucionGateway.getPorSolicitud(SOLICITUD)).thenReturn(null);

        notificador.avisarDarca(solicitud("2026-CM-0010", APROBADA), TipoProcesoAcademico.CANCELACION_MATRICULA, AccionEtapa.ENVIAR_RESPUESTA, FECHA);

        ArgumentCaptor<List<AdjuntoCorreo>> adjuntos = capturaAdjuntos();
        verify(correo).enviar(eq(DARCA), any(), any(), adjuntos.capture());
        assertEquals(List.of("Radicado-2026-CM-0010.pdf"), adjuntos.getValue().stream().map(AdjuntoCorreo::getNombre).toList());
    }

    @Test
    void laMatriculaAprobadaDiceQueCancelaSuMatricula() {
        notificador.avisarDarca(solicitud("2026-CM-0003", APROBADA), TipoProcesoAcademico.CANCELACION_MATRICULA, AccionEtapa.ENVIAR_RESPUESTA, FECHA);

        ArgumentCaptor<String> texto = ArgumentCaptor.forClass(String.class);
        verify(correo).enviar(eq(DARCA), any(), texto.capture(), any());
        assertTrue(texto.getValue().contains("cancela su matrícula (aprobada)."));
    }

    @Test
    void darcaRecibeElSupletorioAprobadoConAsignaturaComprobanteYProfesorSinResolucion() {
        supletorioCon(docente());
        conResolucionYAdicional();

        notificador.avisarDarca(solicitud("2026-ES-0001", APROBADA), TipoProcesoAcademico.EXAMEN_SUPLETORIO, AccionEtapa.APROBAR_COMPROBANTE, FECHA);

        ArgumentCaptor<String> texto = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<AdjuntoCorreo>> adjuntos = capturaAdjuntos();
        verify(correo).enviar(eq(DARCA), eq("DARCA - Notificación de examen supletorio - Radicado 2026-ES-0001"), texto.capture(), adjuntos.capture());
        assertTrue(texto.getValue().contains("solicita examen supletorio de la asignatura ISI-004 - Señales y Sistemas (aprobada)."));
        assertEquals(List.of("Radicado-2026-ES-0001.pdf", "acta.pdf"), adjuntos.getValue().stream().map(AdjuntoCorreo::getNombre).toList());
        verify(resolucionGateway, never()).getPorSolicitud(any());
        List<DatoResumenRespuesta> detalles = generados.get(0).getDetalles();
        assertEquals(List.of("Asignatura del examen", "Decisión sobre el comprobante", "Profesor asignado"),
                detalles.stream().map(DatoResumenRespuesta::getEtiqueta).toList());
        assertEquals(List.of("ISI-004 - Señales y Sistemas", "Aprobado", "Luis Ordóñez"),
                detalles.stream().map(DatoResumenRespuesta::getValor).toList());
    }

    @Test
    void elSupletorioRechazadoAntesDelPagoNoTieneProfesorNiComprobante() {
        supletorioCon(null);

        notificador.avisarDarca(solicitud("2026-ES-0002", RECHAZADA), TipoProcesoAcademico.EXAMEN_SUPLETORIO, AccionEtapa.RECHAZAR_FUNCIONARIO, FECHA);

        List<String> valores = generados.get(0).getDetalles().stream().map(DatoResumenRespuesta::getValor).toList();
        assertEquals("No aplica, la solicitud se cerró antes del pago", valores.get(1));
        assertEquals("Sin asignar", valores.get(2));
    }

    @Test
    void elComprobanteRechazadoQuedaEnElResumen() {
        supletorioCon(null);

        notificador.avisarDarca(solicitud("2026-ES-0003", RECHAZADA), TipoProcesoAcademico.EXAMEN_SUPLETORIO, AccionEtapa.RECHAZAR_COMPROBANTE, FECHA);

        assertEquals("Rechazado", generados.get(0).getDetalles().get(1).getValor());
    }

    @Test
    void elProfesorRecibeSuCorreoConElPdf() {
        supletorioCon(docente());
        when(usuarioGateway.getUsuario("doc-1")).thenReturn(Usuario.builder().uuidUsuario("doc-1").correoElectronico("lordonez@unicauca.edu.co").build());

        notificador.avisarProfesor(solicitud("2026-ES-0001", APROBADA), AccionEtapa.APROBAR_COMPROBANTE, docente(), FECHA);

        ArgumentCaptor<String> texto = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<AdjuntoCorreo>> adjuntos = capturaAdjuntos();
        verify(correo, times(1)).enviar(eq("lordonez@unicauca.edu.co"), eq("Notificación de examen supletorio - Radicado 2026-ES-0001"),
                texto.capture(), adjuntos.capture());
        assertEquals("Popayán, Cauca, 15 de octubre de 2026" + SALTO + SALTO + "Sr(a) Luis Ordóñez" + SALTO + SALTO
                + "Tiene una solicitud de examen supletorio aprobada del estudiante Ana María Muñoz Peña, con código 104618021, "
                + "de la asignatura ISI-004 - Señales y Sistemas, radicado 2026-ES-0001." + SALTO + SALTO
                + "Ingrese a la aplicación para consultarla. La información de la respuesta está en el PDF adjunto.", texto.getValue());
        assertEquals(List.of("Radicado-2026-ES-0001.pdf"), adjuntos.getValue().stream().map(AdjuntoCorreo::getNombre).toList());
        assertArrayEquals(PDF, adjuntos.getValue().get(0).getContenido());
        verify(correo, never()).enviar(eq(DARCA), any(), any(), any());
    }

    @Test
    void sinProfesorNoSeEnviaNadaNiFalla() {
        assertDoesNotThrow(() -> notificador.avisarProfesor(solicitud("2026-ES-0001", RECHAZADA), AccionEtapa.RECHAZAR_COMPROBANTE, null, FECHA));

        verifyNoInteractions(correo, usuarioGateway, supletorioGateway);
    }

    @Test
    void unProfesorSinCorreoSeEntregaSinDestinatarioParaQueSeOmitaYSeRegistre() {
        supletorioCon(docente());
        when(usuarioGateway.getUsuario("doc-1")).thenReturn(Usuario.builder().uuidUsuario("doc-1").build());

        notificador.avisarProfesor(solicitud("2026-ES-0001", APROBADA), AccionEtapa.APROBAR_COMPROBANTE, docente(), FECHA);

        verify(correo).enviar(eq(null), eq("Notificación de examen supletorio - Radicado 2026-ES-0001"), any(), any());
    }

    @Test
    void siFallaElPdfDeDarcaElDelProfesorSaleIgual() {
        GeneradorPdfRespuestaIntPuerto falla = mock(GeneradorPdfRespuestaIntPuerto.class);
        when(falla.generar(any())).thenThrow(new IllegalStateException("pdf roto")).thenReturn(PDF);
        NotificadorRespuestaFinal conFalla = new NotificadorRespuestaFinal(cancelacionGateway, supletorioGateway, resolucionGateway, anexoGateway,
                usuarioGateway, falla, new PreparacionCorreoImplAdaptador(), correo, DARCA);
        supletorioCon(docente());
        when(usuarioGateway.getUsuario("doc-1")).thenReturn(Usuario.builder().correoElectronico("lordonez@unicauca.edu.co").build());
        SolicitudAcademica solicitud = solicitud("2026-ES-0001", APROBADA);

        assertDoesNotThrow(() -> conFalla.avisarDarca(solicitud, TipoProcesoAcademico.EXAMEN_SUPLETORIO, AccionEtapa.APROBAR_COMPROBANTE, FECHA));
        assertDoesNotThrow(() -> conFalla.avisarProfesor(solicitud, AccionEtapa.APROBAR_COMPROBANTE, docente(), FECHA));

        verify(correo, never()).enviar(eq(DARCA), any(), any(), any());
        verify(correo, times(1)).enviar(eq("lordonez@unicauca.edu.co"), any(), any(), any());
    }

    @Test
    void siFallaLaLecturaDelProfesorDarcaRecibeElSuyo() {
        supletorioCon(docente());
        when(usuarioGateway.getUsuario("doc-1")).thenThrow(new IllegalStateException("base caida"));
        SolicitudAcademica solicitud = solicitud("2026-ES-0001", APROBADA);

        assertDoesNotThrow(() -> notificador.avisarProfesor(solicitud, AccionEtapa.APROBAR_COMPROBANTE, docente(), FECHA));
        notificador.avisarDarca(solicitud, TipoProcesoAcademico.EXAMEN_SUPLETORIO, AccionEtapa.APROBAR_COMPROBANTE, FECHA);

        verify(correo, times(1)).enviar(any(), any(), any(), any());
        verify(correo).enviar(eq(DARCA), any(), any(), any());
    }

    @Test
    void elResumenNoIncluyeObservaciones() {
        cancelacionCon(asignatura("ISI-001", "Cálculo I", true, "Bajo rendimiento"));
        SolicitudAcademica solicitud = solicitud("2026-CA-0006", APROBADA);

        ResumenRespuesta resumen = notificador.resumen(solicitud, TipoProcesoAcademico.CANCELACION_ASIGNATURA, AccionEtapa.ENVIAR_RESPUESTA, FECHA);

        assertTrue(resumen.getDetalles().stream().noneMatch(dato -> dato.getEtiqueta().toLowerCase().contains("observaci")));
    }
}
