package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.SolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.TramiteExamenSupletorioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
import com.unicauca.cfiet.solicitudes.dominio.servicios.TramiteSolicitud;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.APROBADA;

public class TramiteExamenSupletorioCUImplAdaptador implements TramiteExamenSupletorioCUIntPuerto {
    private static final String EXAMEN_SUPLETORIO = "Solicitud de examen supletorio";
    private static final String DOCENTE = "Docente";
    private static final String TITULO_NOTIFICACION = "Examen supletorio %s, radicado %s";
    private static final String RESULTADO_APROBADO = "aprobado";
    private static final String RESULTADO_RECHAZADO = "rechazado";

    private final SolicitudExamenSupletorioGatewayIntPuerto gateway;
    private final DocenteGatewayIntPuerto docenteGateway;
    private final NotificacionDocenteGatewayIntPuerto notificacionGateway;
    private final SolicitudAcademicaCUIntPuerto solicitudCU;
    private final TramiteSolicitud tramite;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;
    private final Clock reloj;

    public TramiteExamenSupletorioCUImplAdaptador(SolicitudExamenSupletorioGatewayIntPuerto gateway,
                                                  SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                                  UsuarioGatewayIntPuerto usuarioGateway,
                                                  SesionGatewayIntPuerto sesionGateway,
                                                  IJwtServicio jwtServicio,
                                                  DocenteGatewayIntPuerto docenteGateway,
                                                  NotificacionDocenteGatewayIntPuerto notificacionGateway,
                                                  SolicitudAcademicaCUIntPuerto solicitudCU,
                                                  MaquinaEtapas maquinaEtapas,
                                                  ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                  LogCUIntPuerto log,
                                                  Clock reloj) {
        this.gateway = gateway;
        this.docenteGateway = docenteGateway;
        this.notificacionGateway = notificacionGateway;
        this.solicitudCU = solicitudCU;
        this.tramite = new TramiteSolicitud(solicitudGateway, usuarioGateway, sesionGateway, jwtServicio, maquinaEtapas, formateadorExcepciones);
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
        this.reloj = reloj;
    }

    @Override
    public SolicitudExamenSupletorio rechazarPorFuncionario(String uuidSolicitudAcademica, String observacion, String token) {
        return accion(uuidSolicitudAcademica, AccionEtapa.RECHAZAR_FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO, observacion, token);
    }

    @Override
    public SolicitudExamenSupletorio remitirADecano(String uuidSolicitudAcademica, String observacion, String token) {
        return accion(uuidSolicitudAcademica, AccionEtapa.REMITIR_DECANO, RolEtiquetaEtapa.FUNCIONARIO, observacion, token);
    }

    @Override
    public SolicitudExamenSupletorio aprobarPorDecano(String uuidSolicitudAcademica, String observacion, String token) {
        return accion(uuidSolicitudAcademica, AccionEtapa.APROBAR_DECANO, RolEtiquetaEtapa.DECANO, observacion, token);
    }

    @Override
    public SolicitudExamenSupletorio rechazarPorDecano(String uuidSolicitudAcademica, String observacion, String token) {
        return accion(uuidSolicitudAcademica, AccionEtapa.RECHAZAR_DECANO, RolEtiquetaEtapa.DECANO, observacion, token);
    }

    @Override
    public SolicitudExamenSupletorio enviarRespuesta(String uuidSolicitudAcademica, String observacion, String token) {
        return accion(uuidSolicitudAcademica, AccionEtapa.ENVIAR_RESPUESTA, RolEtiquetaEtapa.FUNCIONARIO, observacion, token);
    }

    @Override
    public SolicitudExamenSupletorio enviarRecibo(String uuidSolicitudAcademica, String token) {
        return accion(uuidSolicitudAcademica, AccionEtapa.ENVIAR_RECIBO, RolEtiquetaEtapa.FUNCIONARIO, null, token);
    }

    @Override
    public SolicitudExamenSupletorio subirComprobante(String uuidSolicitudAcademica, String token) {
        return accion(uuidSolicitudAcademica, AccionEtapa.SUBIR_COMPROBANTE, RolEtiquetaEtapa.ESTUDIANTE, null, token);
    }

    @Override
    public SolicitudExamenSupletorio aprobarComprobante(String uuidSolicitudAcademica, String observacion, String uuidDocente, String token) {
        ActorSolicitud actor = tramite.actorDe(token, RolEtiquetaEtapa.FUNCIONARIO);
        SolicitudExamenSupletorio supletorio = supletorioDe(uuidSolicitudAcademica);
        tramite.solicitudValidada(TipoProcesoAcademico.EXAMEN_SUPLETORIO, uuidSolicitudAcademica, AccionEtapa.APROBAR_COMPROBANTE, actor);
        if (!tramite.tieneTexto(uuidDocente))
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.DOCENTE_SUPLETORIO_REQUERIDO);
        Docente docente = docenteGateway.getActivo(uuidDocente.trim());
        if (docente == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, DOCENTE, uuidDocente));

        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.APROBAR_COMPROBANTE, actor, observacion, token);
        gateway.asignarDocente(uuidSolicitudAcademica, docente.getUuidUsuario());
        supletorio.setDocente(docente);
        notificacionGateway.crear(NotificacionDocente.builder()
                .uuidNotificacion(UUID.randomUUID().toString())
                .uuidSolicitudAcademica(uuidSolicitudAcademica)
                .docente(docente)
                .titulo(String.format(TITULO_NOTIFICACION, resultadoDe(actualizada), actualizada.getRadicado()))
                .fechaCreacion(LocalDateTime.now(reloj))
                .build());
        log.crearLog("Asignar profesor de examen supletorio",
                String.format("Solicitud académica %s, acción %s: profesor %s %s notificado", actualizada.getRadicado(),
                        AccionEtapa.APROBAR_COMPROBANTE, docente.getNombres(), docente.getApellidos()),
                token);
        return conSolicitud(supletorio, actualizada);
    }

    private String resultadoDe(SolicitudAcademica solicitud) {
        boolean aprobada = solicitud.getEtapa() != null && APROBADA.equals(solicitud.getEtapa().getCodigo());
        return aprobada ? RESULTADO_APROBADO : RESULTADO_RECHAZADO;
    }

    @Override
    public SolicitudExamenSupletorio rechazarComprobante(String uuidSolicitudAcademica, String observacion, String token) {
        return accion(uuidSolicitudAcademica, AccionEtapa.RECHAZAR_COMPROBANTE, RolEtiquetaEtapa.FUNCIONARIO, observacion, token);
    }

    private SolicitudExamenSupletorio accion(String uuidSolicitudAcademica, AccionEtapa accion, RolEtiquetaEtapa rol, String observacion,
                                             String token) {
        ActorSolicitud actor = tramite.actorDe(token, rol);
        SolicitudExamenSupletorio supletorio = supletorioDe(uuidSolicitudAcademica);
        return conSolicitud(supletorio, solicitudCU.cambiarEtapa(uuidSolicitudAcademica, accion, actor, observacion, token));
    }

    private SolicitudExamenSupletorio supletorioDe(String uuidSolicitudAcademica) {
        SolicitudExamenSupletorio supletorio = tramite.tieneTexto(uuidSolicitudAcademica) ? gateway.getPorSolicitud(uuidSolicitudAcademica) : null;
        if (supletorio == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, EXAMEN_SUPLETORIO, uuidSolicitudAcademica));
        return supletorio;
    }

    private SolicitudExamenSupletorio conSolicitud(SolicitudExamenSupletorio supletorio, SolicitudAcademica solicitud) {
        supletorio.setSolicitudAcademica(solicitud);
        return supletorio;
    }
}
