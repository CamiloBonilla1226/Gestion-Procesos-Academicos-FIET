package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.RespuestaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.RemisionConsejoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.TramiteCancelacionAsignaturaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.AlmacenamientoAnexosIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.AnexoAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.RemisionConsejoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ResolucionAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.RespuestaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudCancelacionAsignaturaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudConsejoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.AnexoAcademicoConstantes;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.RemisionConsejoConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Anexo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AnexoAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaMatriculada;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.DecisionAsignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.DetalleRemisionConsejo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EvaluacionAsignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.InformacionSolicitante;
import com.unicauca.cfiet.solicitudes.dominio.modelos.OrdenDelDia;
import com.unicauca.cfiet.solicitudes.dominio.modelos.RemisionConsejo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ResolucionAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Respuesta;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SesionConsejo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Solicitud;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudCancelacionAsignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoProcesoAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitud;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudConsejo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.dominio.servicios.RedaccionResolucion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

public class RemisionConsejoCUImplAdaptador implements RemisionConsejoCUIntPuerto {
    private static final String RUTA_ANEXOS = "solicitudes/anexos/";
    private static final String RUTA_RESPUESTAS = "respuestas/";
    private static final String SOLICITUD_CONSEJO = "Solicitud del Consejo";
    private static final String CONSECUTIVO = "El consecutivo";
    private static final String INDICACIONES = "Las indicaciones";
    private static final List<String> EXTENSIONES = List.of("pdf", "jpg", "png");

    private final TramiteCancelacionAsignaturaCUIntPuerto tramiteCU;
    private final TipoSolicitudConsejoGatewayIntPuerto tipoConsejoGateway;
    private final TipoSolicitudAcademicaGatewayIntPuerto tipoAcademicoGateway;
    private final TipoSolicitudGatewayIntPuerto tipoSolicitudGateway;
    private final SolicitudGatewayIntPuerto solicitudGateway;
    private final RemisionConsejoGatewayIntPuerto remisionGateway;
    private final AnexoAcademicoGatewayIntPuerto anexoGateway;
    private final AlmacenamientoAnexosIntPuerto almacenamiento;
    private final SolicitudCancelacionAsignaturaGatewayIntPuerto cancelacionGateway;
    private final RespuestaCUIntPuerto respuestaCU;
    private final RespuestaGatewayIntPuerto respuestaGateway;
    private final ResolucionAcademicaGatewayIntPuerto resolucionGateway;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;
    private final Clock reloj;
    private final String urlAplicacion;

    public RemisionConsejoCUImplAdaptador(TramiteCancelacionAsignaturaCUIntPuerto tramiteCU,
                                          TipoSolicitudConsejoGatewayIntPuerto tipoConsejoGateway,
                                          TipoSolicitudAcademicaGatewayIntPuerto tipoAcademicoGateway,
                                          TipoSolicitudGatewayIntPuerto tipoSolicitudGateway,
                                          SolicitudGatewayIntPuerto solicitudGateway,
                                          RemisionConsejoGatewayIntPuerto remisionGateway,
                                          AnexoAcademicoGatewayIntPuerto anexoGateway,
                                          AlmacenamientoAnexosIntPuerto almacenamiento,
                                          SolicitudCancelacionAsignaturaGatewayIntPuerto cancelacionGateway,
                                          RespuestaCUIntPuerto respuestaCU,
                                          RespuestaGatewayIntPuerto respuestaGateway,
                                          ResolucionAcademicaGatewayIntPuerto resolucionGateway,
                                          ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                          LogCUIntPuerto log,
                                          Clock reloj,
                                          String urlAplicacion) {
        this.tramiteCU = tramiteCU;
        this.tipoConsejoGateway = tipoConsejoGateway;
        this.tipoAcademicoGateway = tipoAcademicoGateway;
        this.tipoSolicitudGateway = tipoSolicitudGateway;
        this.solicitudGateway = solicitudGateway;
        this.remisionGateway = remisionGateway;
        this.anexoGateway = anexoGateway;
        this.almacenamiento = almacenamiento;
        this.cancelacionGateway = cancelacionGateway;
        this.respuestaCU = respuestaCU;
        this.respuestaGateway = respuestaGateway;
        this.resolucionGateway = resolucionGateway;
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
        this.reloj = reloj;
        this.urlAplicacion = urlAplicacion;
    }

    @Override
    public SolicitudCancelacionAsignatura remitir(String uuidSolicitudAcademica, List<EvaluacionAsignatura> evaluaciones,
                                                  String observacion, String token) {
        TipoSolicitud tipo = tipoEnlazado();
        SolicitudCancelacionAsignatura cancelacion = tramiteCU.remitirAConsejo(uuidSolicitudAcademica, evaluaciones, observacion, token);
        SolicitudAcademica solicitud = cancelacion.getSolicitudAcademica();

        Solicitud espejo = Solicitud.builder()
                .uuidSolicitud(UUID.randomUUID().toString())
                .consecutivo(solicitud.getRadicado())
                .nombre(acotar(String.format(RemisionConsejoConstantes.NOMBRE_ESPEJO, solicitud.getRadicado(), nombreEstudiante(solicitud)),
                        RemisionConsejoConstantes.MAXIMO_NOMBRE))
                .descripcion(acotar(String.format(RemisionConsejoConstantes.DESCRIPCION_ESPEJO, asignaturas(cancelacion),
                        cancelacion.getMotivoCancelacion()), RemisionConsejoConstantes.MAXIMO_DESCRIPCION))
                .estado(RemisionConsejoConstantes.ESTADO_PENDIENTE_ORDEN_DEL_DIA)
                .objTipoSolicitud(tipo)
                .uuidTipoSolicitud(tipo.getUuidTipoSolicitud())
                .objFuncionario(tipo.getObjFuncionarioEncargado())
                .build();
        espejo.setInformacionSolicitante(solicitante(solicitud, espejo));
        espejo.setAnexos(copiarAnexos(uuidSolicitudAcademica, espejo));
        Solicitud guardada = solicitudGateway.guardarSolicitud(espejo);

        remisionGateway.guardar(RemisionConsejo.builder()
                .solicitudAcademica(solicitud)
                .uuidSolicitud(guardada.getUuidSolicitud())
                .fechaRemision(LocalDateTime.now(reloj))
                .build());
        log.crearLog("Remitir cancelación de asignatura al Consejo",
                String.format("Solicitud académica %s llevada a la orden del día como la solicitud %s con %d anexos",
                        solicitud.getRadicado(), guardada.getUuidSolicitud(), espejo.getAnexos().size()),
                token);
        return cancelacion;
    }

    @Override
    public DetalleRemisionConsejo getDetalle(String uuidSolicitud) {
        RemisionConsejo remision = uuidSolicitud == null ? null : remisionGateway.getPorSolicitud(uuidSolicitud);
        if (remision == null)
            return null;
        Solicitud espejo = solicitudGateway.getSolicitud(uuidSolicitud);
        return DetalleRemisionConsejo.builder()
                .uuidSolicitud(uuidSolicitud)
                .cancelacion(cancelacionGateway.getPorSolicitud(remision.getSolicitudAcademica().getUuidSolicitudAcademica()))
                .ordenDelDia(espejo == null ? null : espejo.getObjOrdenDelDia())
                .respuesta(respuestaGateway.getRespuestaPorSolicitud(uuidSolicitud))
                .build();
    }

    @Override
    public DetalleRemisionConsejo registrarDecision(String uuidSolicitud, List<DecisionAsignatura> decisiones, String tipoRespuesta,
                                                    String consecutivoFiet, String indicaciones, String token) {
        RemisionConsejo remision = uuidSolicitud == null ? null : remisionGateway.getPorSolicitud(uuidSolicitud);
        if (remision == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, SOLICITUD_CONSEJO, uuidSolicitud));
        Solicitud espejo = solicitudGateway.getSolicitud(uuidSolicitud);
        if (espejo == null || espejo.getObjOrdenDelDia() == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ESPEJO_SIN_ORDEN_DEL_DIA,
                    espejo == null ? uuidSolicitud : espejo.getNombre()));
        String consecutivo = textoAcotado(consecutivoFiet, RemisionConsejoConstantes.MAXIMO_CONSECUTIVO, CONSECUTIVO);
        String indicacion = textoAcotado(indicaciones, RemisionConsejoConstantes.MAXIMO_INDICACIONES, INDICACIONES);

        SolicitudCancelacionAsignatura cancelacion = tramiteCU.decidirPorConsejo(
                remision.getSolicitudAcademica().getUuidSolicitudAcademica(), decisiones, token);
        Respuesta respuesta = respuestaCU.registrarRespuesta(uuidSolicitud, Respuesta.builder()
                .tipoRespuesta(tipoRespuesta == null || tipoRespuesta.isBlank() ? ApplicationConstantes.RESOLUCION : tipoRespuesta.trim())
                .consecutivoFiet(consecutivo)
                .respuestaConsejo(acotar(textoRespuesta(cancelacion, espejo.getObjOrdenDelDia()), RemisionConsejoConstantes.MAXIMO_RESPUESTA))
                .indicaciones(indicacion)
                .build(), token);
        log.crearLog("Registrar decisión del Consejo en la orden del día",
                String.format("Solicitud %s de la orden del día %s, respuesta %s", uuidSolicitud,
                        espejo.getObjOrdenDelDia().getUuidOrdenDelDia(), respuesta.getUuidRespuesta()),
                token);
        return DetalleRemisionConsejo.builder()
                .uuidSolicitud(uuidSolicitud)
                .cancelacion(cancelacion)
                .ordenDelDia(espejo.getObjOrdenDelDia())
                .respuesta(respuesta)
                .build();
    }

    @Override
    public SesionConsejo getSesion(String uuidSolicitudAcademica) {
        RemisionConsejo remision = uuidSolicitudAcademica == null ? null : remisionGateway.getPorSolicitudAcademica(uuidSolicitudAcademica);
        if (remision == null)
            return null;
        Solicitud espejo = solicitudGateway.getSolicitud(remision.getUuidSolicitud());
        OrdenDelDia orden = espejo == null ? null : espejo.getObjOrdenDelDia();
        Respuesta respuesta = respuestaGateway.getRespuestaPorSolicitud(remision.getUuidSolicitud());
        return SesionConsejo.builder()
                .fecha(orden == null ? null : fechaDe(orden.getFecha()))
                .numeroActa(orden == null || orden.getNumeroActa() == null || orden.getNumeroActa().isBlank() ? null : orden.getNumeroActa().trim())
                .consecutivo(respuesta == null || respuesta.getConsecutivoFiet() == null || respuesta.getConsecutivoFiet().isBlank()
                        ? null : respuesta.getConsecutivoFiet().trim())
                .build();
    }

    @Override
    public void cerrar(String uuidSolicitudAcademica, String token) {
        RemisionConsejo remision = uuidSolicitudAcademica == null ? null : remisionGateway.getPorSolicitudAcademica(uuidSolicitudAcademica);
        if (remision == null)
            return;
        Solicitud espejo = solicitudGateway.getSolicitud(remision.getUuidSolicitud());
        if (espejo == null)
            return;
        Respuesta respuesta = respuestaGateway.getRespuestaPorSolicitud(espejo.getUuidSolicitud());
        ResolucionAcademica resolucion = resolucionGateway.getPorSolicitud(uuidSolicitudAcademica);
        if (respuesta != null && resolucion != null) {
            String archivo = null;
            try {
                byte[] contenido = almacenamiento.leer(resolucion.getUrlArchivo());
                if (contenido != null)
                    archivo = almacenamiento.guardarRespuesta(respuesta.getUuidRespuesta(), RemisionConsejoConstantes.NOMBRE_RESOLUCION, contenido);
            } catch (RuntimeException error) {
                archivo = null;
            }
            if (archivo == null)
                formateadorExcepciones.lanzarErrorGenerico(MensajesError.ERROR_GUARDANDO_ARCHIVO);
            String anterior = respuesta.getUrlRespuesta();
            if (anterior != null && !anterior.isBlank())
                almacenamiento.eliminarRespuestaTrasConfirmar(respuesta.getUuidRespuesta(), anterior.substring(anterior.lastIndexOf('/') + 1));
            respuesta.setUrlRespuesta(urlAplicacion + RUTA_RESPUESTAS + respuesta.getUuidRespuesta() + "/" + archivo);
            respuestaGateway.guardarRespuesta(respuesta);
        }
        espejo.setEstado(RemisionConsejoConstantes.ESTADO_RESPONDIDA);
        solicitudGateway.guardarSolicitud(espejo);
        log.crearLog("Cerrar solicitud del Consejo en la orden del día",
                String.format("Solicitud %s respondida%s", espejo.getUuidSolicitud(),
                        respuesta != null && resolucion != null ? " con la Resolución adjunta" : " sin Resolución adjunta"),
                token);
    }

    private String textoRespuesta(SolicitudCancelacionAsignatura cancelacion, OrdenDelDia orden) {
        List<String> aprobadas = new ArrayList<>();
        List<String> negadas = new ArrayList<>();
        for (AsignaturaSolicitudAcademica asignatura : cancelacion.getAsignaturas())
            (Boolean.TRUE.equals(asignatura.getAprobadaPorDecano()) ? aprobadas : negadas).add(nombreAsignatura(asignatura));
        String decision;
        if (negadas.isEmpty())
            decision = String.format(RemisionConsejoConstantes.APRUEBA, String.join(", ", aprobadas));
        else if (aprobadas.isEmpty())
            decision = String.format(RemisionConsejoConstantes.NIEGA, String.join(", ", negadas));
        else
            decision = String.format(RemisionConsejoConstantes.APRUEBA, String.join(", ", aprobadas)) + " y "
                    + String.format(RemisionConsejoConstantes.NIEGA, String.join(", ", negadas));
        String sesion = "";
        if (orden != null && orden.getFecha() != null && !orden.getFecha().isBlank())
            sesion += String.format(RemisionConsejoConstantes.SESION, fechaDe(orden.getFecha()));
        if (orden != null && orden.getNumeroActa() != null && !orden.getNumeroActa().isBlank())
            sesion += String.format(RemisionConsejoConstantes.ACTA, orden.getNumeroActa().trim());
        else if (!sesion.isEmpty())
            sesion += ",";
        SolicitudAcademica solicitud = cancelacion.getSolicitudAcademica();
        return String.format(RemisionConsejoConstantes.TEXTO_RESPUESTA, RemisionConsejoConstantes.CONSEJO, sesion, decision,
                solicitud == null ? "" : solicitud.getRadicado(), solicitud == null ? "" : nombreEstudiante(solicitud));
    }

    private String fechaDe(String fecha) {
        if (fecha == null || fecha.isBlank())
            return null;
        try {
            return RedaccionResolucion.fechaCorta(LocalDate.parse(fecha.trim()));
        } catch (DateTimeParseException error) {
            return fecha.trim();
        }
    }

    private String textoAcotado(String valor, int maximo, String dato) {
        if (valor == null || valor.isBlank())
            return null;
        String limpio = valor.trim();
        if (limpio.length() > maximo)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.TEXTO_COMUNICACION_MUY_LARGO, dato, maximo));
        return limpio;
    }

    private TipoSolicitud tipoEnlazado() {
        String uuidProceso = tipoAcademicoGateway.getTodos().stream()
                .filter(tipo -> TipoProcesoAcademico.porNombre(tipo.getNombre()) == TipoProcesoAcademico.CANCELACION_ASIGNATURA)
                .map(tipo -> tipo.getUuidTipoSolicitudAcademica())
                .findFirst()
                .orElse(null);
        TipoSolicitudConsejo enlace = uuidProceso == null ? null : tipoConsejoGateway.getPorTipoAcademico(uuidProceso);
        TipoSolicitud tipo = enlace == null ? null : tipoSolicitudGateway.getTipoSolicitud(enlace.getUuidTipoSolicitud());
        if (tipo == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.SIN_TIPO_CONSEJO);
        return tipo;
    }

    private InformacionSolicitante solicitante(SolicitudAcademica solicitud, Solicitud espejo) {
        Usuario usuario = solicitud.getEstudiante() == null ? null : solicitud.getEstudiante().getUsuario();
        InformacionSolicitante solicitante = new InformacionSolicitante();
        solicitante.setUuidInformacionSolicitante(UUID.randomUUID().toString());
        solicitante.setSolicitud(espejo);
        if (usuario != null) {
            solicitante.setTipoDocumento(usuario.getTipoDocumento());
            solicitante.setNumeroDocumento(usuario.getNumeroDocumento());
            solicitante.setNombres(usuario.getNombres());
            solicitante.setApellidos(usuario.getApellidos());
            solicitante.setTelefono(usuario.getTelefono());
            solicitante.setCorreoElectronico(usuario.getCorreoElectronico());
        }
        return solicitante;
    }

    private List<Anexo> copiarAnexos(String uuidSolicitudAcademica, Solicitud espejo) {
        List<AnexoAcademico> anexos = anexoGateway.getPorSolicitud(uuidSolicitudAcademica);
        Comparator<AnexoAcademico> porFecha = Comparator.comparing(AnexoAcademico::getFechaSubida, Comparator.nullsFirst(Comparator.naturalOrder()));
        List<Anexo> copias = new ArrayList<>();
        anexos.stream()
                .filter(anexo -> anexo.getTipoAnexoAcademico() != null
                        && AnexoAcademicoConstantes.FORMATO_FIRMADO_ESTUDIANTE.equals(anexo.getTipoAnexoAcademico().getNombre()))
                .max(porFecha)
                .ifPresent(formato -> copias.add(copia(formato, RemisionConsejoConstantes.ANEXO_FORMATO, espejo)));
        List<AnexoAcademico> soportes = anexos.stream()
                .filter(anexo -> anexo.getTipoAnexoAcademico() == null)
                .sorted(porFecha)
                .toList();
        for (int i = 0; i < soportes.size(); i++)
            copias.add(copia(soportes.get(i), String.format(RemisionConsejoConstantes.ANEXO_SOPORTE, i + 1), espejo));
        return copias;
    }

    private Anexo copia(AnexoAcademico anexo, String nombre, Solicitud espejo) {
        String url = null;
        try {
            url = almacenamiento.guardar(espejo.getUuidSolicitud(), UUID.randomUUID() + "." + extensionDe(anexo),
                    almacenamiento.leer(anexo.getUrlArchivo()));
        } catch (RuntimeException error) {
            formateadorExcepciones.lanzarErrorGenerico(MensajesError.ERROR_GUARDANDO_ARCHIVO);
        }
        return Anexo.builder()
                .uuidAnexo(UUID.randomUUID().toString())
                .nombre(nombre)
                .urlAnexo(urlAplicacion + RUTA_ANEXOS + espejo.getUuidSolicitud() + "/" + url.substring(url.lastIndexOf('/') + 1))
                .objSolicitud(espejo)
                .build();
    }

    private String extensionDe(AnexoAcademico anexo) {
        String archivo = anexo.getUrlArchivo() == null ? "" : anexo.getUrlArchivo();
        String extension = archivo.contains(".") ? archivo.substring(archivo.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
        if (extension.equals("jpeg"))
            extension = "jpg";
        return EXTENSIONES.contains(extension) ? extension : "pdf";
    }

    private String nombreEstudiante(SolicitudAcademica solicitud) {
        Usuario usuario = solicitud.getEstudiante() == null ? null : solicitud.getEstudiante().getUsuario();
        if (usuario == null)
            return "";
        return ((usuario.getNombres() == null ? "" : usuario.getNombres()) + " "
                + (usuario.getApellidos() == null ? "" : usuario.getApellidos())).trim();
    }

    private String asignaturas(SolicitudCancelacionAsignatura cancelacion) {
        if (cancelacion.getAsignaturas() == null)
            return "";
        return cancelacion.getAsignaturas().stream()
                .map(this::nombreAsignatura)
                .collect(Collectors.joining(", "));
    }

    private String nombreAsignatura(AsignaturaSolicitudAcademica asignatura) {
        AsignaturaMatriculada matriculada = asignatura.getAsignaturaMatriculada();
        if (matriculada == null || matriculada.getAsignatura() == null)
            return asignatura.getUuidAsignaturaSolicitud();
        return matriculada.getAsignatura().getCodigoAsignatura() + " " + matriculada.getAsignatura().getNombreAsignatura();
    }

    private String acotar(String texto, int maximo) {
        if (texto == null)
            return null;
        String limpio = texto.trim();
        return limpio.length() <= maximo ? limpio : limpio.substring(0, maximo);
    }
}
