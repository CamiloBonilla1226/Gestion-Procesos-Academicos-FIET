package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.NotificacionResolucionCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ResolucionAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.ComunicacionResolucion;
import com.unicauca.cfiet.solicitudes.dominio.servicios.NotificadorRespuestaFinal;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.APROBADA;
import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.APROBADA_POR_DECANO;

public class NotificacionResolucionCUImplAdaptador implements NotificacionResolucionCUIntPuerto {
    private static final String SOLICITUD_ACADEMICA = "Solicitud académica";
    private static final String USUARIO = "Usuario";
    private static final String USERNAME = "username";
    private static final String TIPO_PDF = "application/pdf";
    private static final String NUMERO = "El número";
    private static final String DECISION = "La decisión";
    private static final String TEXTO_ESTUDIANTE = "El texto de la comunicación al estudiante";
    private static final String TEXTO_DARCA = "El texto del correo a DARCA";
    private static final int MAXIMO_NUMERO = 20;
    private static final int MAXIMO_DECISION = 60;
    private static final int ANIO_MINIMO = 2000;
    private static final int ANIO_MAXIMO = 2100;
    private static final int MAXIMO_INTENTOS = 3;
    private static final int MAXIMO_ERROR = 1000;
    private static final Duration RECLAMO_VENCIDO = Duration.ofMinutes(15);
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy 'a las' HH:mm", Locale.forLanguageTag("es-CO"));

    private final NotificacionResolucionGatewayIntPuerto gateway;
    private final SolicitudAcademicaGatewayIntPuerto solicitudGateway;
    private final SolicitudCancelacionAsignaturaGatewayIntPuerto cancelacionGateway;
    private final SesionGatewayIntPuerto sesionGateway;
    private final IJwtServicio jwtServicio;
    private final ResolucionAcademicaCUIntPuerto resolucionCU;
    private final NotificadorRespuestaFinal notificador;
    private final GeneradorPdfRespuestaIntPuerto generadorPdf;
    private final CorreoIntPuerto correo;
    private final ComunicacionResolucion comunicacion;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;
    private final Clock reloj;
    private final EjecucionDiferidaIntPuerto ejecucionDiferida;

    public NotificacionResolucionCUImplAdaptador(NotificacionResolucionGatewayIntPuerto gateway,
                                                 SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                                 SolicitudCancelacionAsignaturaGatewayIntPuerto cancelacionGateway,
                                                 SesionGatewayIntPuerto sesionGateway,
                                                 IJwtServicio jwtServicio,
                                                 ResolucionAcademicaCUIntPuerto resolucionCU,
                                                 NotificadorRespuestaFinal notificador,
                                                 GeneradorPdfRespuestaIntPuerto generadorPdf,
                                                 CorreoIntPuerto correo,
                                                 ComunicacionResolucion comunicacion,
                                                 ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                 LogCUIntPuerto log,
                                                 Clock reloj,
                                                 EjecucionDiferidaIntPuerto ejecucionDiferida) {
        this.gateway = gateway;
        this.solicitudGateway = solicitudGateway;
        this.cancelacionGateway = cancelacionGateway;
        this.sesionGateway = sesionGateway;
        this.jwtServicio = jwtServicio;
        this.resolucionCU = resolucionCU;
        this.notificador = notificador;
        this.generadorPdf = generadorPdf;
        this.correo = correo;
        this.comunicacion = comunicacion;
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
        this.reloj = reloj;
        this.ejecucionDiferida = ejecucionDiferida;
    }

    @Override
    public void validarComunicacion(ComunicacionRespuesta datos) {
        if (datos == null || datos.getProgramacion() == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.PROGRAMACION_RESOLUCION_REQUERIDA);
        validarProgramacion(datos.getProgramacion(), LocalDateTime.now(reloj));
        mensajeDe(datos);
        textoDe(datos.getTextoComunicacion(), TEXTO_ESTUDIANTE);
        textoDe(datos.getTextoDarca(), TEXTO_DARCA);
    }

    @Override
    public NotificacionResolucion programar(SolicitudAcademica solicitud, ComunicacionRespuesta datos, String token) {
        validarComunicacion(datos);
        ProgramacionResolucion programacion = datos.getProgramacion();
        String mensaje = mensajeDe(datos);
        String textoComunicacion = textoDe(datos.getTextoComunicacion(), TEXTO_ESTUDIANTE);
        String textoDarca = textoDe(datos.getTextoDarca(), TEXTO_DARCA);
        boolean aprobada = solicitud.getEtapa() != null && APROBADA.equals(solicitud.getEtapa().getCodigo());
        LocalDateTime ahora = LocalDateTime.now(reloj);
        NotificacionResolucion creada = gateway.crear(NotificacionResolucion.builder()
                .uuidSolicitudAcademica(solicitud.getUuidSolicitudAcademica())
                .fechaProgramada(programacion.getFechaProgramada())
                .estado(EstadoNotificacionResolucion.PROGRAMADA)
                .firmada(false)
                .numeroResolucion(programacion.getNumeroResolucion().trim())
                .anioResolucion(programacion.getAnioResolucion())
                .decision(programacion.getDecision().trim())
                .mensajeInicial(mensaje)
                .textoComunicacion(textoComunicacion == null ? comunicacion.plantillaComunicacion() : textoComunicacion)
                .textoDarca(textoDarca == null ? notificador.plantillaDarcaResolucion(solicitud, aprobada, false) : textoDarca)
                .intentos(0)
                .fechaCreacion(ahora)
                .build());
        correo.enviar(correoDe(solicitud), comunicacion.asuntoMensajeInicial(solicitud.getRadicado()), mensaje, List.of());
        log.crearLog("Programar comunicación de la Resolución",
                String.format("Solicitud académica %s: comunicación programada para %s, Resolución %s%s de %d, mensaje inicial enviado",
                        solicitud.getRadicado(), programacion.getFechaProgramada(), creada.getNumeroResolucion(), comunicacion.getSufijo(),
                        creada.getAnioResolucion()),
                token);
        return vista(creada, solicitud);
    }

    @Override
    public PlantillaNotificacionResolucion getPlantilla(String uuidSolicitudAcademica, String token) {
        SolicitudAcademica solicitud = solicitudDelFuncionario(uuidSolicitudAcademica, token);
        SolicitudCancelacionAsignatura cancelacion = cancelacionGateway.getPorSolicitud(solicitud.getUuidSolicitudAcademica());
        return PlantillaNotificacionResolucion.builder()
                .mensajeInicial(comunicacion.plantillaMensajeInicial())
                .decisionPropuesta(comunicacion.decisionPropuesta(solicitud.getEtapa() == null ? null : solicitud.getEtapa().getCodigo(),
                        cancelacion == null ? List.of() : cancelacion.getAsignaturas()))
                .anioResolucion(LocalDateTime.now(reloj).getYear())
                .sufijoResolucion(comunicacion.getSufijo())
                .textoComunicacion(comunicacion.plantillaComunicacion())
                .textoDarca(notificador.plantillaDarcaResolucion(solicitud,
                        solicitud.getEtapa() != null && APROBADA_POR_DECANO.equals(solicitud.getEtapa().getCodigo()), false))
                .nombreEstudiante(nombreDe(solicitud))
                .build();
    }

    @Override
    public NotificacionResolucion getNotificacion(String uuidSolicitudAcademica, String token) {
        SolicitudAcademica solicitud = solicitudDelFuncionario(uuidSolicitudAcademica, token);
        return vista(gateway.getPorSolicitud(solicitud.getUuidSolicitudAcademica()), solicitud);
    }

    @Override
    public NotificacionResolucion reprogramar(String uuidSolicitudAcademica, ProgramacionResolucion programacion, String token) {
        SolicitudAcademica solicitud = solicitudDelFuncionario(uuidSolicitudAcademica, token);
        NotificacionResolucion actual = existente(solicitud);
        LocalDateTime ahora = LocalDateTime.now(reloj);
        validarProgramacion(programacion, ahora);
        ProgramacionResolucion limpia = ProgramacionResolucion.builder()
                .fechaProgramada(programacion.getFechaProgramada())
                .numeroResolucion(programacion.getNumeroResolucion().trim())
                .anioResolucion(programacion.getAnioResolucion())
                .decision(programacion.getDecision().trim())
                .build();
        if (!gateway.actualizarProgramacion(actual.getUuidSolicitudAcademica(), limpia, ahora))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.NOTIFICACION_NO_EDITABLE, solicitud.getRadicado()));
        log.crearLog("Reprogramar comunicación de la Resolución",
                String.format("Solicitud académica %s: comunicación programada para %s, Resolución %s%s de %d", solicitud.getRadicado(),
                        limpia.getFechaProgramada(), limpia.getNumeroResolucion(), comunicacion.getSufijo(), limpia.getAnioResolucion()),
                token);
        return vista(gateway.getPorSolicitud(actual.getUuidSolicitudAcademica()), solicitud);
    }

    @Override
    public NotificacionResolucion registrarFirma(String uuidSolicitudAcademica, ArchivoAdjunto archivo, String textoDarca, String token) {
        Usuario funcionario = usuarioDe(token);
        SolicitudAcademica solicitud = solicitudDe(uuidSolicitudAcademica, funcionario);
        NotificacionResolucion actual = existente(solicitud);
        String recibido = textoDe(textoDarca, TEXTO_DARCA);
        String texto = recibido != null ? recibido : notificador.plantillaDarcaFirmada(actual.getTextoDarca());
        if (!gateway.marcarFirmada(actual.getUuidSolicitudAcademica(), texto, LocalDateTime.now(reloj)))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.NOTIFICACION_NO_EDITABLE, solicitud.getRadicado()));
        ActorSolicitud actor = ActorSolicitud.builder().uuidUsuario(funcionario.getUuidUsuario()).rol(RolEtiquetaEtapa.FUNCIONARIO).build();
        resolucionCU.reemplazarResolucionFirmada(solicitud.getUuidSolicitudAcademica(), archivo, actor, token);
        log.crearLog("Registrar firma de la Resolución",
                String.format("Solicitud académica %s: el estudiante firmó la Resolución, la comunicación programada queda cerrada y se notifica a DARCA",
                        solicitud.getRadicado()),
                token);
        String uuid = actual.getUuidSolicitudAcademica();
        ejecucionDiferida.despuesDeConfirmar(String.format("el correo a DARCA de la solicitud %s", solicitud.getRadicado()), () -> procesar(uuid));
        return vista(gateway.getPorSolicitud(uuid), solicitud);
    }

    @Override
    public NotificacionResolucion reintentar(String uuidSolicitudAcademica, String token) {
        SolicitudAcademica solicitud = solicitudDelFuncionario(uuidSolicitudAcademica, token);
        NotificacionResolucion actual = existente(solicitud);
        if (!gateway.reintentar(actual.getUuidSolicitudAcademica()))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.NOTIFICACION_NO_REINTENTABLE, solicitud.getRadicado()));
        log.crearLog("Reintentar comunicación de la Resolución",
                String.format("Solicitud académica %s: la comunicación vuelve a quedar programada", solicitud.getRadicado()),
                token);
        return vista(gateway.getPorSolicitud(actual.getUuidSolicitudAcademica()), solicitud);
    }

    @Override
    public int procesarPendientes() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        int procesadas = 0;
        for (String uuid : gateway.getPendientes(ahora, ahora.minus(RECLAMO_VENCIDO)))
            if (procesar(uuid))
                procesadas++;
        return procesadas;
    }

    private boolean procesar(String uuidSolicitud) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        if (!gateway.reclamar(uuidSolicitud, ahora, ahora.minus(RECLAMO_VENCIDO)))
            return false;
        NotificacionResolucion notificacion = gateway.getPorSolicitud(uuidSolicitud);
        List<String> errores = new ArrayList<>();
        SolicitudAcademica solicitud = null;
        try {
            solicitud = solicitudGateway.getPorUuid(uuidSolicitud);
        } catch (RuntimeException error) {
            errores.add("No se pudo leer la solicitud: " + error.getMessage());
        }
        CorreoPreparado alEstudiante = solicitud == null || notificacion.isFirmada() ? null
                : comunicacion.comunicacionEstudiante(correoDe(solicitud), nombreDe(solicitud), notificacion);
        if (alEstudiante != null && notificacion.getFechaEnvioEstudiante() == null) {
            try {
                alEstudiante.setAdjuntos(adjuntosEstudiante(solicitud, ahora));
                correo.enviarAhora(alEstudiante);
                notificacion.setFechaEnvioEstudiante(LocalDateTime.now(reloj));
                gateway.marcarEnvioEstudiante(uuidSolicitud, notificacion.getFechaEnvioEstudiante());
            } catch (RuntimeException error) {
                errores.add("Correo al estudiante: " + error.getMessage());
            }
        }
        if (solicitud != null && notificacion.getFechaEnvioDarca() == null) {
            try {
                List<AdjuntoCorreo> constancia = alEstudiante == null ? List.of()
                        : List.of(constanciaDe(solicitud, alEstudiante, notificacion.getFechaEnvioEstudiante()));
                correo.enviarAhora(notificador.correoDarcaResolucion(solicitud, ahora.toLocalDate(), notificacion.isFirmada(), constancia,
                        notificacion.getTextoDarca()));
                gateway.marcarEnvioDarca(uuidSolicitud, LocalDateTime.now(reloj));
            } catch (RuntimeException error) {
                errores.add("Correo a DARCA: " + error.getMessage());
            }
        }
        int intentos = notificacion.getIntentos() + 1;
        if (errores.isEmpty())
            gateway.registrarResultado(uuidSolicitud, EstadoNotificacionResolucion.ENVIADA, intentos, null, LocalDateTime.now(reloj));
        else
            gateway.registrarResultado(uuidSolicitud, intentos >= MAXIMO_INTENTOS ? EstadoNotificacionResolucion.FALLIDA
                    : EstadoNotificacionResolucion.PROGRAMADA, intentos, recortar(String.join(" | ", errores)), null);
        return true;
    }

    private NotificacionResolucion vista(NotificacionResolucion notificacion, SolicitudAcademica solicitud) {
        if (notificacion == null) return null;
        notificacion.setEditable(notificacion.getEstado() == EstadoNotificacionResolucion.PROGRAMADA && !notificacion.isFirmada()
                && notificacion.getFechaProgramada() != null && notificacion.getFechaProgramada().isAfter(LocalDateTime.now(reloj)));
        notificacion.setSufijoResolucion(comunicacion.getSufijo());
        String firmada = notificador.plantillaDarcaFirmada(notificacion.getTextoDarca());
        boolean aprobada = solicitud != null && solicitud.getEtapa() != null && APROBADA.equals(solicitud.getEtapa().getCodigo());
        notificacion.setTextoDarcaFirma(firmada != null || solicitud == null ? firmada
                : notificador.plantillaDarcaResolucion(solicitud, aprobada, true));
        return notificacion;
    }

    private List<AdjuntoCorreo> adjuntosEstudiante(SolicitudAcademica solicitud, LocalDateTime ahora) {
        List<AdjuntoCorreo> adjuntos = new ArrayList<>();
        adjuntos.add(notificador.pdfRespuesta(solicitud, ahora.toLocalDate()));
        adjuntos.addAll(notificador.resolucionDe(solicitud));
        return adjuntos;
    }

    private AdjuntoCorreo constanciaDe(SolicitudAcademica solicitud, CorreoPreparado alEstudiante, LocalDateTime fechaEnvio) {
        ConstanciaComunicacion constancia = ConstanciaComunicacion.builder()
                .radicado(solicitud.getRadicado())
                .destinatario(alEstudiante.getDestinatario())
                .asunto(alEstudiante.getAsunto())
                .fechaEnvio(fechaEnvio)
                .parrafos(alEstudiante.getParrafos())
                .build();
        return AdjuntoCorreo.builder()
                .nombre("Comunicacion-Estudiante-" + solicitud.getRadicado() + ".pdf")
                .tipoContenido(TIPO_PDF)
                .contenido(generadorPdf.generarConstancia(constancia))
                .build();
    }

    private void validarProgramacion(ProgramacionResolucion programacion, LocalDateTime ahora) {
        if (programacion == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.PROGRAMACION_RESOLUCION_REQUERIDA);
        LocalDateTime fecha = programacion.getFechaProgramada();
        if (fecha == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.FECHA_PROGRAMADA_REQUERIDA);
        if (!fecha.isAfter(ahora))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.FECHA_PROGRAMADA_PASADA,
                    fecha.format(FECHA_HORA), ahora.format(FECHA_HORA)));
        if (!tieneTexto(programacion.getNumeroResolucion()))
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.NUMERO_RESOLUCION_REQUERIDO);
        if (programacion.getNumeroResolucion().trim().length() > MAXIMO_NUMERO)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.DATO_RESOLUCION_MUY_LARGO, NUMERO, MAXIMO_NUMERO));
        Integer anio = programacion.getAnioResolucion();
        if (anio == null || anio < ANIO_MINIMO || anio > ANIO_MAXIMO)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ANIO_RESOLUCION_INVALIDO, ANIO_MINIMO, ANIO_MAXIMO));
        if (!tieneTexto(programacion.getDecision()))
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.DECISION_RESOLUCION_REQUERIDA);
        if (programacion.getDecision().trim().length() > MAXIMO_DECISION)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.DATO_RESOLUCION_MUY_LARGO, DECISION, MAXIMO_DECISION));
    }

    private String mensajeDe(ComunicacionRespuesta datos) {
        String mensaje = normalizar(datos.getMensaje());
        if (mensaje.isEmpty())
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.MENSAJE_INICIAL_REQUERIDO);
        if (mensaje.length() > ComunicacionResolucion.MAXIMO_MENSAJE)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.MENSAJE_INICIAL_MUY_LARGO, ComunicacionResolucion.MAXIMO_MENSAJE));
        return mensaje;
    }

    private String textoDe(String valor, String campo) {
        if (valor == null) return null;
        String texto = normalizar(valor);
        if (texto.isEmpty())
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.TEXTO_COMUNICACION_REQUERIDO, campo));
        if (texto.length() > ComunicacionResolucion.MAXIMO_TEXTO)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.TEXTO_COMUNICACION_MUY_LARGO, campo, ComunicacionResolucion.MAXIMO_TEXTO));
        return texto;
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.replace("\r\n", "\n").replace('\r', '\n').strip();
    }

    private NotificacionResolucion existente(SolicitudAcademica solicitud) {
        NotificacionResolucion notificacion = gateway.getPorSolicitud(solicitud.getUuidSolicitudAcademica());
        if (notificacion == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA,
                    "Comunicación de la Resolución", solicitud.getUuidSolicitudAcademica()));
        return notificacion;
    }

    private SolicitudAcademica solicitudDelFuncionario(String uuidSolicitudAcademica, String token) {
        return solicitudDe(uuidSolicitudAcademica, usuarioDe(token));
    }

    private SolicitudAcademica solicitudDe(String uuidSolicitudAcademica, Usuario funcionario) {
        SolicitudAcademica solicitud = tieneTexto(uuidSolicitudAcademica) ? solicitudGateway.getPorUuid(uuidSolicitudAcademica) : null;
        TipoSolicitudAcademica tipo = solicitud == null ? null : solicitud.getTipoSolicitudAcademica();
        boolean esCancelacionAsignatura = tipo != null && TipoProcesoAcademico.porNombre(tipo.getNombre()) == TipoProcesoAcademico.CANCELACION_ASIGNATURA;
        String asignado = tipo == null || tipo.getFuncionarioAcademico() == null ? null : tipo.getFuncionarioAcademico().getUuidUsuario();
        boolean esElAsignado = tieneRol(funcionario, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL)
                && funcionario.getUuidUsuario().equals(asignado);
        if (!esCancelacionAsignatura || !esElAsignado)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, SOLICITUD_ACADEMICA, uuidSolicitudAcademica));
        return solicitud;
    }

    private Usuario usuarioDe(String token) {
        String username = jwtServicio.getUsername(token);
        if (!tieneTexto(username))
            formateadorExcepciones.lanzarErrorGenerico(MensajesError.USERNAME_TOKEN);
        Usuario usuario = sesionGateway.getUsuario(username);
        if (usuario == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO, USUARIO, USERNAME, username));
        return usuario;
    }

    private boolean tieneRol(Usuario usuario, String rol) {
        return usuario.getRoles() != null && usuario.getRoles().stream().anyMatch(actual -> rol.equals(actual.getNombre()));
    }

    private String correoDe(SolicitudAcademica solicitud) {
        Usuario usuario = solicitud.getEstudiante() == null ? null : solicitud.getEstudiante().getUsuario();
        return usuario == null ? null : usuario.getCorreoElectronico();
    }

    private String nombreDe(SolicitudAcademica solicitud) {
        Usuario usuario = solicitud.getEstudiante() == null ? null : solicitud.getEstudiante().getUsuario();
        if (usuario == null) return "";
        String nombres = usuario.getNombres() == null ? "" : usuario.getNombres().trim();
        String apellidos = usuario.getApellidos() == null ? "" : usuario.getApellidos().trim();
        return (nombres + " " + apellidos).trim();
    }

    private String recortar(String texto) {
        return texto.length() > MAXIMO_ERROR ? texto.substring(0, MAXIMO_ERROR) : texto;
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
