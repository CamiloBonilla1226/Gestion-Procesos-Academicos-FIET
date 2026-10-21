package com.unicauca.cfiet.solicitudes.dominio.servicios;

import com.unicauca.cfiet.solicitudes.aplicacion.output.AnexoAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.CorreoIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.GeneradorPdfRespuestaIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.PreparacionCorreoIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ResolucionAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudCancelacionAsignaturaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudExamenSupletorioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.AnexoAcademicoConstantes.DOCUMENTO_ADICIONAL;
import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.APROBADA;

public class NotificadorRespuestaFinal {
    private static final Map<TipoProcesoAcademico, String> ASUNTOS_DARCA = Map.of(
            TipoProcesoAcademico.CANCELACION_ASIGNATURA, "DARCA - Notificación de Cancelación de asignatura - Radicado %s",
            TipoProcesoAcademico.CANCELACION_MATRICULA, "DARCA - Notificación de Cancelación de matrícula - Radicado %s",
            TipoProcesoAcademico.EXAMEN_SUPLETORIO, "DARCA - Notificación de examen supletorio - Radicado %s");
    private static final String ASUNTO_PROFESOR = "Notificación de examen supletorio - Radicado %s";
    private static final String ENCABEZADO = "Popayán, Cauca, %s%n%n";
    private static final String ESTUDIANTE = "El estudiante %s, con código %s, ";
    private static final String PIE_DARCA = "%n%nLa información de la respuesta está en el PDF adjunto.";
    private static final String NOTA_FIRMADA = " El estudiante firmó la Resolución en la Decanatura; se adjunta la Resolución firmada.";
    private static final String NOTA_COMUNICADA = " La Resolución se comunicó al estudiante por correo electrónico; se adjunta la constancia de la comunicación.";
    private static final String CUERPO_PROFESOR = "Sr(a) %s%n%nTiene una solicitud de examen supletorio %s del estudiante %s, con código %s, "
            + "de la asignatura %s, radicado %s.%n%nIngrese a la aplicación para consultarla. La información de la respuesta está en el PDF adjunto.";
    private static final DateTimeFormatter FECHA_CARTA = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-CO"));
    private static final String TIPO_PDF = "application/pdf";
    private static final String APROBADA_TEXTO = "Aprobada";
    private static final String RECHAZADA_TEXTO = "Rechazada";
    private static final String NO_APLICA = "No aplica";
    private static final String ASIGNATURA_EXAMEN = "Asignatura del examen";

    private final SolicitudCancelacionAsignaturaGatewayIntPuerto cancelacionAsignaturaGateway;
    private final SolicitudExamenSupletorioGatewayIntPuerto supletorioGateway;
    private final ResolucionAcademicaGatewayIntPuerto resolucionGateway;
    private final AnexoAcademicoGatewayIntPuerto anexoGateway;
    private final UsuarioGatewayIntPuerto usuarioGateway;
    private final GeneradorPdfRespuestaIntPuerto generadorPdf;
    private final PreparacionCorreoIntPuerto preparacion;
    private final CorreoIntPuerto correo;
    private final String destinatarioDarca;

    public NotificadorRespuestaFinal(SolicitudCancelacionAsignaturaGatewayIntPuerto cancelacionAsignaturaGateway,
                                     SolicitudExamenSupletorioGatewayIntPuerto supletorioGateway,
                                     ResolucionAcademicaGatewayIntPuerto resolucionGateway,
                                     AnexoAcademicoGatewayIntPuerto anexoGateway,
                                     UsuarioGatewayIntPuerto usuarioGateway,
                                     GeneradorPdfRespuestaIntPuerto generadorPdf,
                                     PreparacionCorreoIntPuerto preparacion,
                                     CorreoIntPuerto correo,
                                     String destinatarioDarca) {
        this.cancelacionAsignaturaGateway = cancelacionAsignaturaGateway;
        this.supletorioGateway = supletorioGateway;
        this.resolucionGateway = resolucionGateway;
        this.anexoGateway = anexoGateway;
        this.usuarioGateway = usuarioGateway;
        this.generadorPdf = generadorPdf;
        this.preparacion = preparacion;
        this.correo = correo;
        this.destinatarioDarca = destinatarioDarca;
    }

    public void avisarDarca(SolicitudAcademica solicitud, TipoProcesoAcademico proceso, AccionEtapa accion, LocalDate fecha) {
        preparacion.antesDeConfirmar(String.format("el correo a DARCA de la solicitud %s", solicitud.getRadicado()), () -> {
            ResumenRespuesta resumen = resumen(solicitud, proceso, accion, fecha);
            List<AdjuntoCorreo> adjuntos = new ArrayList<>();
            adjuntos.add(pdf(resumen));
            boolean sinResolucion = proceso == TipoProcesoAcademico.EXAMEN_SUPLETORIO || accion == AccionEtapa.RECHAZAR_FUNCIONARIO;
            if (!sinResolucion)
                adjuntos.addAll(resolucionDe(solicitud));
            if (proceso.admiteDocumentoAdicional())
                adjuntos.addAll(adicionalesDe(solicitud));
            String texto = String.format(ENCABEZADO, fecha.format(FECHA_CARTA)) + frase(resumen, proceso) + String.format(PIE_DARCA);
            correo.enviar(destinatarioDarca, String.format(ASUNTOS_DARCA.get(proceso), solicitud.getRadicado()), texto, adjuntos);
        });
    }

    public CorreoPreparado correoDarcaResolucion(SolicitudAcademica solicitud, LocalDate fecha, boolean firmada, List<AdjuntoCorreo> constancia) {
        TipoProcesoAcademico proceso = TipoProcesoAcademico.CANCELACION_ASIGNATURA;
        ResumenRespuesta resumen = resumen(solicitud, proceso, AccionEtapa.ENVIAR_RESPUESTA, fecha);
        List<AdjuntoCorreo> adjuntos = new ArrayList<>();
        adjuntos.add(pdf(resumen));
        adjuntos.addAll(resolucionDe(solicitud));
        if (!firmada && constancia != null)
            adjuntos.addAll(constancia);
        String texto = String.format(ENCABEZADO, fecha.format(FECHA_CARTA)) + frase(resumen, proceso)
                + (firmada ? NOTA_FIRMADA : NOTA_COMUNICADA) + String.format(PIE_DARCA);
        return CorreoPreparado.builder()
                .destinatario(destinatarioDarca)
                .asunto(String.format(ASUNTOS_DARCA.get(proceso), solicitud.getRadicado()))
                .texto(texto)
                .adjuntos(adjuntos)
                .build();
    }

    public void avisarProfesor(SolicitudAcademica solicitud, AccionEtapa accion, Docente docente, LocalDate fecha) {
        if (docente == null) return;
        preparacion.antesDeConfirmar(String.format("el correo al profesor de la solicitud %s", solicitud.getRadicado()), () -> {
            Usuario usuario = usuarioGateway.getUsuario(docente.getUuidUsuario());
            ResumenRespuesta resumen = resumen(solicitud, TipoProcesoAcademico.EXAMEN_SUPLETORIO, accion, fecha);
            String texto = String.format(ENCABEZADO, fecha.format(FECHA_CARTA)) + String.format(CUERPO_PROFESOR,
                    unir(docente.getNombres(), docente.getApellidos()), resumen.getEstadoFinal().toLowerCase(Locale.ROOT), resumen.getEstudiante(),
                    valor(resumen.getCodigoEstudiantil()), asignaturaDelExamen(resumen), solicitud.getRadicado());
            correo.enviar(usuario == null ? null : usuario.getCorreoElectronico(), String.format(ASUNTO_PROFESOR, solicitud.getRadicado()),
                    texto, List.of(pdf(resumen)));
        });
    }

    public ResumenRespuesta resumen(SolicitudAcademica solicitud, TipoProcesoAcademico proceso, AccionEtapa accion, LocalDate fecha) {
        Estudiante estudiante = solicitud.getEstudiante();
        Usuario usuario = estudiante == null ? null : estudiante.getUsuario();
        boolean aprobada = solicitud.getEtapa() != null && APROBADA.equals(solicitud.getEtapa().getCodigo());
        ResumenRespuesta resumen = ResumenRespuesta.builder()
                .radicado(solicitud.getRadicado())
                .tipoSolicitud(proceso.getNombre())
                .fechaRespuesta(fecha)
                .estudiante(usuario == null ? "" : unir(usuario.getNombres(), usuario.getApellidos()))
                .codigoEstudiantil(estudiante == null ? null : estudiante.getCodigoEstudiantil())
                .programa(estudiante == null || estudiante.getProgramaAcademico() == null ? null : estudiante.getProgramaAcademico().getNombre())
                .estadoFinal(aprobada ? APROBADA_TEXTO : RECHAZADA_TEXTO)
                .build();
        switch (proceso) {
            case CANCELACION_ASIGNATURA -> resumen.setAsignaturas(asignaturasCanceladas(solicitud.getUuidSolicitudAcademica(), aprobada));
            case CANCELACION_MATRICULA -> resumen.getDetalles().add(dato("Decisión de la solicitud", resumen.getEstadoFinal()));
            case EXAMEN_SUPLETORIO -> resumen.getDetalles().addAll(detallesSupletorio(solicitud.getUuidSolicitudAcademica(), accion));
        }
        return resumen;
    }

    private List<AsignaturaResumenRespuesta> asignaturasCanceladas(String uuidSolicitud, boolean solicitudAprobada) {
        SolicitudCancelacionAsignatura cancelacion = cancelacionAsignaturaGateway.getPorSolicitud(uuidSolicitud);
        if (cancelacion == null) return new ArrayList<>();
        return cancelacion.getAsignaturas().stream()
                .map(asignatura -> {
                    boolean aprobada = solicitudAprobada && Boolean.TRUE.equals(asignatura.getAprobadaPorDecano());
                    Asignatura datos = asignaturaDe(asignatura.getAsignaturaMatriculada());
                    return AsignaturaResumenRespuesta.builder()
                            .codigo(datos == null ? null : datos.getCodigoAsignatura())
                            .nombre(datos == null ? null : datos.getNombreAsignatura())
                            .decision(aprobada ? APROBADA_TEXTO : RECHAZADA_TEXTO)
                            .situacionAlCancelar(!aprobada ? NO_APLICA
                                    : asignatura.getSituacionCancelar() == null ? null : asignatura.getSituacionCancelar().getNombre())
                            .build();
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<DatoResumenRespuesta> detallesSupletorio(String uuidSolicitud, AccionEtapa accion) {
        SolicitudExamenSupletorio supletorio = supletorioGateway.getPorSolicitud(uuidSolicitud);
        Asignatura asignatura = supletorio == null ? null : asignaturaDe(supletorio.getAsignaturaMatriculada());
        Docente docente = supletorio == null ? null : supletorio.getDocente();
        return List.of(
                dato(ASIGNATURA_EXAMEN, asignatura == null ? null : unir(asignatura.getCodigoAsignatura(), "-", asignatura.getNombreAsignatura())),
                dato("Decisión sobre el comprobante", decisionComprobante(accion)),
                dato("Profesor asignado", docente == null ? "Sin asignar" : unir(docente.getNombres(), docente.getApellidos())));
    }

    private String decisionComprobante(AccionEtapa accion) {
        if (accion == AccionEtapa.APROBAR_COMPROBANTE) return "Aprobado";
        if (accion == AccionEtapa.RECHAZAR_COMPROBANTE) return "Rechazado";
        return "No aplica, la solicitud se cerró antes del pago";
    }

    private String frase(ResumenRespuesta resumen, TipoProcesoAcademico proceso) {
        boolean aprobada = APROBADA_TEXTO.equals(resumen.getEstadoFinal());
        String estado = resumen.getEstadoFinal().toLowerCase(Locale.ROOT);
        String inicio = String.format(ESTUDIANTE, valor(resumen.getEstudiante()), valor(resumen.getCodigoEstudiantil()));
        return inicio + switch (proceso) {
            case CANCELACION_MATRICULA -> (aprobada ? "cancela su matrícula" : "solicita cancelar su matrícula") + " (" + estado + ").";
            case EXAMEN_SUPLETORIO -> "solicita examen supletorio de la asignatura " + asignaturaDelExamen(resumen) + " (" + estado + ").";
            case CANCELACION_ASIGNATURA -> fraseAsignaturas(resumen.getAsignaturas(), estado);
        };
    }

    private String fraseAsignaturas(List<AsignaturaResumenRespuesta> asignaturas, String estado) {
        if (asignaturas.isEmpty())
            return "solicita cancelar asignaturas (" + estado + ").";
        if (asignaturas.size() == 1) {
            AsignaturaResumenRespuesta asignatura = asignaturas.get(0);
            boolean aprobada = APROBADA_TEXTO.equals(asignatura.getDecision());
            return (aprobada ? "cancela la asignatura " : "solicita cancelar la asignatura ") + valor(asignatura.getNombre())
                    + " (" + asignatura.getDecision().toLowerCase(Locale.ROOT) + ").";
        }
        long aprobadas = asignaturas.stream().filter(asignatura -> APROBADA_TEXTO.equals(asignatura.getDecision())).count();
        long rechazadas = asignaturas.size() - aprobadas;
        return String.format("solicita cancelar %d asignaturas: %d %s y %d %s.", asignaturas.size(),
                aprobadas, aprobadas == 1 ? "aprobada" : "aprobadas", rechazadas, rechazadas == 1 ? "rechazada" : "rechazadas");
    }

    private String asignaturaDelExamen(ResumenRespuesta resumen) {
        return valor(resumen.getDetalles().stream()
                .filter(dato -> ASIGNATURA_EXAMEN.equals(dato.getEtiqueta()))
                .map(DatoResumenRespuesta::getValor)
                .findFirst()
                .orElse(null));
    }

    public List<AdjuntoCorreo> resolucionDe(SolicitudAcademica solicitud) {
        ResolucionAcademica resolucion = resolucionGateway.getPorSolicitud(solicitud.getUuidSolicitudAcademica());
        if (resolucion == null)
            return List.of();
        return List.of(AdjuntoCorreo.builder()
                .nombre("Resolucion-" + solicitud.getRadicado() + ".pdf")
                .tipoContenido(TIPO_PDF)
                .urlArchivo(resolucion.getUrlArchivo())
                .build());
    }

    public AdjuntoCorreo pdfRespuesta(SolicitudAcademica solicitud, LocalDate fecha) {
        return pdf(resumen(solicitud, TipoProcesoAcademico.CANCELACION_ASIGNATURA, AccionEtapa.ENVIAR_RESPUESTA, fecha));
    }

    public List<AdjuntoCorreo> adicionalesDe(SolicitudAcademica solicitud) {
        return anexoGateway.getPorSolicitud(solicitud.getUuidSolicitudAcademica()).stream()
                .filter(anexo -> anexo.getTipoAnexoAcademico() != null && DOCUMENTO_ADICIONAL.equals(anexo.getTipoAnexoAcademico().getNombre()))
                .map(adicional -> AdjuntoCorreo.builder()
                        .nombre(adicional.getNombreArchivo())
                        .tipoContenido(adicional.getTipoArchivo())
                        .urlArchivo(adicional.getUrlArchivo())
                        .build())
                .toList();
    }

    private AdjuntoCorreo pdf(ResumenRespuesta resumen) {
        return AdjuntoCorreo.builder()
                .nombre("Radicado-" + resumen.getRadicado() + ".pdf")
                .tipoContenido(TIPO_PDF)
                .contenido(generadorPdf.generar(resumen))
                .build();
    }

    private Asignatura asignaturaDe(AsignaturaMatriculada matriculada) {
        return matriculada == null ? null : matriculada.getAsignatura();
    }

    private DatoResumenRespuesta dato(String etiqueta, String valor) {
        return DatoResumenRespuesta.builder().etiqueta(etiqueta).valor(valor).build();
    }

    private String valor(String texto) {
        return texto == null || texto.isBlank() ? "sin registrar" : texto;
    }

    private String unir(String... partes) {
        StringBuilder texto = new StringBuilder();
        for (String parte : partes)
            if (parte != null && !parte.isBlank())
                texto.append(texto.isEmpty() ? "" : " ").append(parte.trim());
        return texto.toString();
    }
}
