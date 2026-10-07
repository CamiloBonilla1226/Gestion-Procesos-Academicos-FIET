package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.SolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.TramiteExamenSupletorioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
import com.unicauca.cfiet.solicitudes.dominio.servicios.TramiteSolicitud;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TramiteExamenSupletorioCUImplAdaptador implements TramiteExamenSupletorioCUIntPuerto {
    private static final String EXAMEN_SUPLETORIO = "Solicitud de examen supletorio";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final SolicitudExamenSupletorioGatewayIntPuerto gateway;
    private final SolicitudAcademicaCUIntPuerto solicitudCU;
    private final TramiteSolicitud tramite;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;

    public TramiteExamenSupletorioCUImplAdaptador(SolicitudExamenSupletorioGatewayIntPuerto gateway,
                                                  SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                                  UsuarioGatewayIntPuerto usuarioGateway,
                                                  SesionGatewayIntPuerto sesionGateway,
                                                  IJwtServicio jwtServicio,
                                                  SolicitudAcademicaCUIntPuerto solicitudCU,
                                                  MaquinaEtapas maquinaEtapas,
                                                  ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                  LogCUIntPuerto log) {
        this.gateway = gateway;
        this.solicitudCU = solicitudCU;
        this.tramite = new TramiteSolicitud(solicitudGateway, usuarioGateway, sesionGateway, jwtServicio, maquinaEtapas, formateadorExcepciones);
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
    }

    @Override
    public SolicitudExamenSupletorio rechazarPorFuncionario(String uuidSolicitudAcademica, String observacion, String token) {
        return accion(uuidSolicitudAcademica, AccionEtapa.RECHAZAR_FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO, observacion, token);
    }

    @Override
    public SolicitudExamenSupletorio remitirADecano(String uuidSolicitudAcademica, Boolean requisitosVerificados, String observacion, String token) {
        ActorSolicitud actor = tramite.actorDe(token, RolEtiquetaEtapa.FUNCIONARIO);
        SolicitudExamenSupletorio supletorio = supletorioDe(uuidSolicitudAcademica);
        SolicitudAcademica solicitud = tramite.solicitudValidada(TipoProcesoAcademico.EXAMEN_SUPLETORIO, uuidSolicitudAcademica,
                AccionEtapa.REMITIR_DECANO, actor);
        if (!Boolean.TRUE.equals(requisitosVerificados))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.REQUISITOS_NO_CONFIRMADOS, solicitud.getRadicado()));
        return conSolicitud(supletorio, solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.REMITIR_DECANO, actor, observacion, token));
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
    public SolicitudExamenSupletorio enviarRespuesta(String uuidSolicitudAcademica, String token) {
        return accion(uuidSolicitudAcademica, AccionEtapa.ENVIAR_RESPUESTA, RolEtiquetaEtapa.FUNCIONARIO, null, token);
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
    public SolicitudExamenSupletorio aprobarComprobante(String uuidSolicitudAcademica, LocalDate fechaAcordadaExamen, String token) {
        ActorSolicitud actor = tramite.actorDe(token, RolEtiquetaEtapa.FUNCIONARIO);
        SolicitudExamenSupletorio supletorio = supletorioDe(uuidSolicitudAcademica);
        tramite.solicitudValidada(TipoProcesoAcademico.EXAMEN_SUPLETORIO, uuidSolicitudAcademica, AccionEtapa.APROBAR_COMPROBANTE, actor);
        LocalDate fechaExamen = supletorio.getFechaExamenNoPresentado();
        if (fechaAcordadaExamen != null && fechaExamen != null && fechaAcordadaExamen.isBefore(fechaExamen))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.FECHA_ACORDADA_ANTERIOR,
                    fechaAcordadaExamen.format(FORMATO_FECHA), fechaExamen.format(FORMATO_FECHA)));

        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.APROBAR_COMPROBANTE, actor, null, token);
        if (fechaAcordadaExamen != null) {
            gateway.actualizarFechaAcordada(uuidSolicitudAcademica, fechaAcordadaExamen.atStartOfDay());
            supletorio.setFechaAcordadaExamen(fechaAcordadaExamen.atStartOfDay());
            log.crearLog("Acordar fecha de examen supletorio",
                    String.format("Solicitud académica %s, acción %s: fecha acordada %s", actualizada.getRadicado(),
                            AccionEtapa.APROBAR_COMPROBANTE, fechaAcordadaExamen.format(FORMATO_FECHA)),
                    token);
        }
        return conSolicitud(supletorio, actualizada);
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
