package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.SolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.AlmacenamientoAnexosIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.AnexoAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.CorreoIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.EstudianteGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.EtapaSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ResolucionAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
import com.unicauca.cfiet.solicitudes.dominio.servicios.NotificadorRespuestaFinal;
import com.unicauca.cfiet.solicitudes.dominio.servicios.ValidadorActorSolicitud;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.AnexoAcademicoConstantes.COMPROBANTE_PAGO;
import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.AnexoAcademicoConstantes.DOCUMENTO_ADICIONAL;
import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.AnexoAcademicoConstantes.RECIBO_PAGO;
import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.APROBADA;
import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.RECHAZADA;

public class SolicitudAcademicaCUImplAdaptador implements SolicitudAcademicaCUIntPuerto {
    private static final String SOLICITUD_ACADEMICA = "Solicitud académica";
    private static final String TIPO_SOLICITUD_ACADEMICA = "Tipo de solicitud académica";
    private static final String ESTUDIANTE = "Estudiante";
    private static final String ETAPA = "Etapa de solicitud académica";
    private static final String CODIGO = "codigo";
    private static final int MAXIMO_OBSERVACION = 500;
    private static final List<String> ETAPAS_FINALES = List.of(APROBADA, RECHAZADA);
    private static final Map<TipoProcesoAcademico, String> ASUNTOS_RESPUESTA = Map.of(
            TipoProcesoAcademico.CANCELACION_ASIGNATURA, "Notificación de Cancelación de asignatura",
            TipoProcesoAcademico.CANCELACION_MATRICULA, "Notificación de Cancelación de matrícula",
            TipoProcesoAcademico.EXAMEN_SUPLETORIO, "Notificación de examen supletorio");
    private static final String ENCABEZADO_RESPUESTA = "Popayán, Cauca, %s%n%nSr(a) %s%n"
            + "Estudiante Facultad de Ingeniería Electrónica y Telecomunicaciones%n%n";
    private static final String CUERPO_RESOLUCION = "Se adjunta la resolución correspondiente a su solicitud.";
    private static final String CUERPO_SUPLETORIO = "Su solicitud de examen supletorio con radicado %s ya tiene respuesta. "
            + "Ingrese a la aplicación para consultarla.";
    private static final DateTimeFormatter FECHA_CARTA = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-CO"));
    private static final String TIPO_PDF = "application/pdf";

    private final SolicitudAcademicaGatewayIntPuerto gateway;
    private final EstudianteGatewayIntPuerto estudianteGateway;
    private final TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway;
    private final EtapaSolicitudAcademicaGatewayIntPuerto etapaGateway;
    private final ResolucionAcademicaGatewayIntPuerto resolucionGateway;
    private final AnexoAcademicoGatewayIntPuerto anexoGateway;
    private final AlmacenamientoAnexosIntPuerto almacenamiento;
    private final ValidadorActorSolicitud validadorActor;
    private final MaquinaEtapas maquinaEtapas;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;
    private final Clock reloj;
    private final CorreoIntPuerto correo;
    private final NotificadorRespuestaFinal notificador;

    public SolicitudAcademicaCUImplAdaptador(SolicitudAcademicaGatewayIntPuerto gateway,
                                             EstudianteGatewayIntPuerto estudianteGateway,
                                             TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                             EtapaSolicitudAcademicaGatewayIntPuerto etapaGateway,
                                             ResolucionAcademicaGatewayIntPuerto resolucionGateway,
                                             AnexoAcademicoGatewayIntPuerto anexoGateway,
                                             UsuarioGatewayIntPuerto usuarioGateway,
                                             AlmacenamientoAnexosIntPuerto almacenamiento,
                                             MaquinaEtapas maquinaEtapas,
                                             ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                             LogCUIntPuerto log,
                                             Clock reloj,
                                             CorreoIntPuerto correo,
                                             NotificadorRespuestaFinal notificador) {
        this.gateway = gateway;
        this.estudianteGateway = estudianteGateway;
        this.tipoSolicitudGateway = tipoSolicitudGateway;
        this.etapaGateway = etapaGateway;
        this.resolucionGateway = resolucionGateway;
        this.anexoGateway = anexoGateway;
        this.almacenamiento = almacenamiento;
        this.validadorActor = new ValidadorActorSolicitud(usuarioGateway, formateadorExcepciones);
        this.maquinaEtapas = maquinaEtapas;
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
        this.reloj = reloj;
        this.correo = correo;
        this.notificador = notificador;
    }

    @Override
    public SolicitudAcademica crearSolicitud(String uuidEstudiante, String uuidTipoSolicitudAcademica, String token) {
        Estudiante estudiante = tieneTexto(uuidEstudiante) ? estudianteGateway.getPorUuid(uuidEstudiante) : null;
        if (estudiante == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, ESTUDIANTE, uuidEstudiante));
        TipoSolicitudAcademica tipo = tieneTexto(uuidTipoSolicitudAcademica) ? tipoSolicitudGateway.getPorUuid(uuidTipoSolicitudAcademica) : null;
        if (tipo == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(
                    MensajesError.ENTIDAD_NO_ENCONTRADA, TIPO_SOLICITUD_ACADEMICA, uuidTipoSolicitudAcademica));

        TipoProcesoAcademico proceso = procesoDe(tipo);
        verificarSinSolicitudEnCurso(estudiante.getUuidUsuario(), tipo);
        ResultadoTransicion resultado = maquinaEtapas.siguienteEtapa(proceso, null, AccionEtapa.RADICAR, RolEtiquetaEtapa.ESTUDIANTE);
        EtapaSolicitudAcademica etapa = etapaDe(tipo, resultado.getEtapaSiguiente());
        LocalDateTime ahora = LocalDateTime.now(reloj);

        SolicitudAcademica solicitud = SolicitudAcademica.builder()
                .uuidSolicitudAcademica(UUID.randomUUID().toString())
                .radicado(siguienteRadicado(proceso, ahora.getYear()))
                .estudiante(estudiante)
                .tipoSolicitudAcademica(tipo)
                .etapa(etapa)
                .fechaCreacion(ahora)
                .build();
        SolicitudAcademica creada = gateway.crear(solicitud, historial(solicitud, estudiante.getUuidUsuario(), AccionEtapa.RADICAR, null, ahora));
        log.crearLog("Radicar solicitud académica",
                String.format("Solicitud académica %s radicada (%s), acción %s", creada.getRadicado(), tipo.getNombre(), AccionEtapa.RADICAR),
                token);
        return creada;
    }

    @Override
    public void verificarSinSolicitudEnCurso(String uuidEstudiante, TipoSolicitudAcademica tipo) {
        String enCurso = gateway.getRadicadoEnCurso(uuidEstudiante, tipo.getUuidTipoSolicitudAcademica(), ETAPAS_FINALES);
        if (enCurso != null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.SOLICITUD_EN_CURSO, tipo.getNombre(), enCurso));
    }

    @Override
    public SolicitudAcademica cambiarEtapa(String uuidSolicitudAcademica, AccionEtapa accion, ActorSolicitud actor, String observacionRecibida,
                                           String token) {
        SolicitudAcademica solicitud = tieneTexto(uuidSolicitudAcademica) ? gateway.getPorUuid(uuidSolicitudAcademica) : null;
        if (solicitud == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(
                    MensajesError.ENTIDAD_NO_ENCONTRADA, SOLICITUD_ACADEMICA, uuidSolicitudAcademica));
        if (accion == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.DATOS_TRANSICION_INCOMPLETOS);

        validadorActor.validar(solicitud, actor);
        String observacion = observacionDe(observacionRecibida);

        TipoProcesoAcademico proceso = procesoDe(solicitud.getTipoSolicitudAcademica());
        String etapaAnterior = solicitud.getEtapa().getCodigo();
        ResultadoTransicion resultado = maquinaEtapas.transicionar(
                proceso, etapaAnterior, accion, actor.getRol(), evidencias(solicitud, observacion));
        solicitud.setEtapa(etapaDe(solicitud.getTipoSolicitudAcademica(), resultado.getEtapaSiguiente()));
        boolean remite = accion == AccionEtapa.REMITIR_DECANO;
        ResolucionAcademica escaneo = remite ? resolucionGateway.getPorSolicitud(solicitud.getUuidSolicitudAcademica()) : null;
        List<AnexoAcademico> adicionales = remite ? documentosAdicionales(solicitud.getUuidSolicitudAcademica()) : List.of();

        LocalDateTime ahora = LocalDateTime.now(reloj);
        SolicitudAcademica actualizada = gateway.actualizarEtapa(solicitud, historial(solicitud, actor.getUuidUsuario(), accion, observacion, ahora));
        if (escaneo != null) {
            resolucionGateway.eliminarPorSolicitud(solicitud.getUuidSolicitudAcademica());
            almacenamiento.eliminarTrasConfirmar(escaneo.getUrlArchivo());
        }
        for (AnexoAcademico adicional : adicionales) {
            anexoGateway.eliminar(adicional.getUuidAnexoAcademico());
            almacenamiento.eliminarTrasConfirmar(adicional.getUrlArchivo());
        }
        log.crearLog("Cambiar etapa de solicitud académica",
                String.format("Solicitud académica %s, acción %s: de %s a %s%s%s", solicitud.getRadicado(), accion, etapaAnterior,
                        resultado.getEtapaSiguiente(), escaneo == null ? "" : ", se retiró el escaneo de la Resolución",
                        adicionales.isEmpty() ? "" : ", se retiró el documento adicional"),
                token);
        if (ETAPAS_FINALES.contains(resultado.getEtapaSiguiente()))
            avisarRespuesta(solicitud, proceso, accion, ahora.toLocalDate());
        return actualizada;
    }

    private void avisarRespuesta(SolicitudAcademica solicitud, TipoProcesoAcademico proceso, AccionEtapa accion, LocalDate fecha) {
        Usuario usuario = solicitud.getEstudiante() == null ? null : solicitud.getEstudiante().getUsuario();
        String destinatario = usuario == null ? null : usuario.getCorreoElectronico();
        String uuidSolicitud = solicitud.getUuidSolicitudAcademica();
        boolean supletorio = proceso == TipoProcesoAcademico.EXAMEN_SUPLETORIO;

        List<AdjuntoCorreo> adjuntos = new ArrayList<>();
        ResolucionAcademica resolucion = supletorio ? null : resolucionGateway.getPorSolicitud(uuidSolicitud);
        if (resolucion != null)
            adjuntos.add(AdjuntoCorreo.builder()
                    .nombre("Resolucion-" + solicitud.getRadicado() + ".pdf")
                    .tipoContenido(TIPO_PDF)
                    .urlArchivo(resolucion.getUrlArchivo())
                    .build());
        for (AnexoAcademico adicional : documentosAdicionales(uuidSolicitud))
            adjuntos.add(AdjuntoCorreo.builder()
                    .nombre(adicional.getNombreArchivo())
                    .tipoContenido(adicional.getTipoArchivo())
                    .urlArchivo(adicional.getUrlArchivo())
                    .build());

        String texto = String.format(ENCABEZADO_RESPUESTA, fecha.format(FECHA_CARTA), nombreCompleto(usuario))
                + (supletorio ? String.format(CUERPO_SUPLETORIO, solicitud.getRadicado()) : CUERPO_RESOLUCION);
        correo.enviar(destinatario, ASUNTOS_RESPUESTA.get(proceso), texto, adjuntos);
        notificador.avisarDarca(solicitud, proceso, accion, fecha);
    }

    private List<AnexoAcademico> documentosAdicionales(String uuidSolicitud) {
        return anexoGateway.getPorSolicitud(uuidSolicitud).stream()
                .filter(anexo -> anexo.getTipoAnexoAcademico() != null && DOCUMENTO_ADICIONAL.equals(anexo.getTipoAnexoAcademico().getNombre()))
                .toList();
    }

    private String nombreCompleto(Usuario usuario) {
        if (usuario == null) return "";
        String nombres = usuario.getNombres() == null ? "" : usuario.getNombres().trim();
        String apellidos = usuario.getApellidos() == null ? "" : usuario.getApellidos().trim();
        return (nombres + " " + apellidos).trim();
    }

    private EntregaTransicion evidencias(SolicitudAcademica solicitud, String observacion) {
        String uuidSolicitud = solicitud.getUuidSolicitudAcademica();
        Set<String> anexos = anexoGateway.getPorSolicitud(uuidSolicitud).stream()
                .filter(anexo -> anexo.getTipoAnexoAcademico() != null)
                .map(anexo -> anexo.getTipoAnexoAcademico().getNombre())
                .collect(Collectors.toSet());
        return EntregaTransicion.builder()
                .observacion(observacion)
                .resolucion(resolucionGateway.existePorSolicitud(uuidSolicitud))
                .recibo(anexos.contains(RECIBO_PAGO))
                .comprobante(anexos.contains(COMPROBANTE_PAGO))
                .build();
    }

    private String observacionDe(String observacionRecibida) {
        if (!tieneTexto(observacionRecibida)) return null;
        String observacion = observacionRecibida.trim();
        if (observacion.length() > MAXIMO_OBSERVACION)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.OBSERVACION_MUY_LARGA, MAXIMO_OBSERVACION));
        return observacion;
    }

    private TipoProcesoAcademico procesoDe(TipoSolicitudAcademica tipo) {
        TipoProcesoAcademico proceso = TipoProcesoAcademico.porNombre(tipo.getNombre());
        if (proceso == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.TIPO_SOLICITUD_SIN_PROCESO, tipo.getNombre()));
        return proceso;
    }

    private EtapaSolicitudAcademica etapaDe(TipoSolicitudAcademica tipo, String codigo) {
        EtapaSolicitudAcademica etapa = etapaGateway.getPorTipoIncluyendoUniversales(tipo.getUuidTipoSolicitudAcademica()).stream()
                .filter(e -> codigo.equals(e.getCodigo()))
                .findFirst()
                .orElse(null);
        if (etapa == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO, ETAPA, CODIGO, codigo));
        return etapa;
    }

    private String siguienteRadicado(TipoProcesoAcademico proceso, int anio) {
        String prefijo = String.format("%d-%s-", anio, proceso.getCodigo());
        String ultimo = gateway.getUltimoRadicado(prefijo);
        int consecutivo = ultimo == null ? 1 : Integer.parseInt(ultimo.substring(prefijo.length())) + 1;
        return prefijo + String.format("%04d", consecutivo);
    }

    private HistorialSolicitudAcademica historial(SolicitudAcademica solicitud, String uuidUsuario, AccionEtapa accion,
                                                  String observacion, LocalDateTime fecha) {
        return HistorialSolicitudAcademica.builder()
                .uuidHistorial(UUID.randomUUID().toString())
                .solicitudAcademica(solicitud)
                .usuario(Usuario.builder().uuidUsuario(uuidUsuario).build())
                .accion(accion.name())
                .observaciones(observacion)
                .fecha(fecha)
                .build();
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
