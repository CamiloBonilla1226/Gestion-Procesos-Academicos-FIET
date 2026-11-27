package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.NotificacionResolucionCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.SolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.TramiteCancelacionAsignaturaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EstadoAsignaturaMatriculadaConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
import com.unicauca.cfiet.solicitudes.dominio.servicios.TramiteCancelacion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.APROBADA;
import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.NO_APROBADAS_CON_DECISION;

public class TramiteCancelacionAsignaturaCUImplAdaptador implements TramiteCancelacionAsignaturaCUIntPuerto {
    private static final String CANCELACION_ASIGNATURA = "Solicitud de cancelación de asignatura";
    private static final String SITUACION_MATRICULA = "la situación en la matrícula";
    private static final String SITUACION_CANCELAR = "la situación al cancelar";
    private static final String DECISION = "la decisión del Decano";
    private static final String DECISION_CONSEJO = "la decisión del Consejo";
    private static final String OBSERVACION_EVALUACION = "La observación de la evaluación";
    private static final String OBSERVACION_DECISION = "La observación de la decisión";
    private static final String NUEVA_MATRICULA = "el estado académico para cuando la matricule de nuevo";
    private static final int MAXIMO_OBSERVACION = 255;

    private final SolicitudCancelacionAsignaturaGatewayIntPuerto gateway;
    private final SolicitudAcademicaCUIntPuerto solicitudCU;
    private final TramiteCancelacion tramite;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;
    private final NotificacionResolucionCUIntPuerto notificacionCU;

    public TramiteCancelacionAsignaturaCUImplAdaptador(SolicitudCancelacionAsignaturaGatewayIntPuerto gateway,
                                                       SolicitudCancelacionMatriculaGatewayIntPuerto asignaturaSolicitudGateway,
                                                       SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                                       SituacionAcademicaAsignaturaGatewayIntPuerto situacionGateway,
                                                       UsuarioActorGatewayIntPuerto usuarioActorGateway,
                                                       SesionGatewayIntPuerto sesionGateway,
                                                       IJwtServicio jwtServicio,
                                                       SolicitudAcademicaCUIntPuerto solicitudCU,
                                                       MaquinaEtapas maquinaEtapas,
                                                       ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                       LogCUIntPuerto log,
                                                       NotificacionResolucionCUIntPuerto notificacionCU) {
        this.gateway = gateway;
        this.solicitudCU = solicitudCU;
        this.tramite = new TramiteCancelacion(asignaturaSolicitudGateway, solicitudGateway, situacionGateway, usuarioActorGateway, sesionGateway,
                jwtServicio, maquinaEtapas, formateadorExcepciones);
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
        this.notificacionCU = notificacionCU;
    }

    @Override
    public SolicitudCancelacionAsignatura rechazarPorFuncionario(String uuidSolicitudAcademica, String observacion, String token) {
        ActorSolicitud actor = tramite.actorDe(token, RolEtiquetaEtapa.FUNCIONARIO);
        SolicitudCancelacionAsignatura cancelacion = cancelacionDe(uuidSolicitudAcademica);
        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.RECHAZAR_FUNCIONARIO, actor, observacion, token);
        return conSolicitud(cancelacion, actualizada);
    }

    @Override
    public SolicitudCancelacionAsignatura remitirADecano(String uuidSolicitudAcademica, List<EvaluacionAsignatura> evaluaciones,
                                                         String observacion, String token) {
        return remitir(uuidSolicitudAcademica, evaluaciones, observacion, token, AccionEtapa.REMITIR_DECANO);
    }

    @Override
    public SolicitudCancelacionAsignatura remitirAConsejo(String uuidSolicitudAcademica, List<EvaluacionAsignatura> evaluaciones,
                                                          String observacion, String token) {
        return remitir(uuidSolicitudAcademica, evaluaciones, observacion, token, AccionEtapa.REMITIR_CONSEJO);
    }

    private SolicitudCancelacionAsignatura remitir(String uuidSolicitudAcademica, List<EvaluacionAsignatura> evaluaciones,
                                                   String observacion, String token, AccionEtapa accion) {
        ActorSolicitud actor = tramite.actorDe(token, RolEtiquetaEtapa.FUNCIONARIO);
        SolicitudCancelacionAsignatura cancelacion = cancelacionDe(uuidSolicitudAcademica);
        SolicitudAcademica solicitud = tramite.solicitudValidada(TipoProcesoAcademico.CANCELACION_ASIGNATURA, uuidSolicitudAcademica,
                accion, actor);

        Map<String, SituacionAcademicaAsignatura> catalogo = tramite.catalogoSituaciones();
        Map<String, EvaluacionAsignatura> porAsignatura = tramite.cobertura(cancelacion.getAsignaturas(), solicitud.getRadicado(), evaluaciones,
                EvaluacionAsignatura::getUuidAsignaturaSolicitud);
        for (AsignaturaSolicitudAcademica asignatura : cancelacion.getAsignaturas()) {
            EvaluacionAsignatura evaluacion = porAsignatura.get(asignatura.getUuidAsignaturaSolicitud());
            String nombre = tramite.nombreDe(asignatura);
            asignatura.setNumeroFaltas(tramite.faltasDe(evaluacion.getNumeroFaltas(), nombre));
            asignatura.setNota(tramite.notaDe(evaluacion.getNota(), nombre));
            asignatura.setSituacionMatricula(tramite.situacionDe(catalogo, evaluacion.getUuidSituacionMatricula(), AmbitoSituacionAcademica.MATRICULA));
            asignatura.setSituacionCancelar(tramite.situacionDe(catalogo, evaluacion.getUuidSituacionCancelar(), AmbitoSituacionAcademica.CANCELACION));
            asignatura.setCumpleCondiciones(null);
            asignatura.setObservacionEvaluacion(tramite.textoAcotado(evaluacion.getObservacionEvaluacion(), MAXIMO_OBSERVACION,
                    OBSERVACION_EVALUACION, nombre));
        }

        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, accion, actor, observacion, token);
        cancelacion.setAsignaturas(tramite.guardarAsignaturas(cancelacion.getAsignaturas()));
        log.crearLog("Evaluar asignaturas de cancelación de asignatura",
                String.format("Solicitud académica %s, acción %s: %d asignaturas evaluadas", actualizada.getRadicado(),
                        accion, cancelacion.getAsignaturas().size()),
                token);
        return conSolicitud(cancelacion, actualizada);
    }

    @Override
    public SolicitudCancelacionAsignatura aprobarPorDecano(String uuidSolicitudAcademica, Boolean aceptarEvaluacion,
                                                           List<DecisionAsignatura> decisiones, String observacion, String token) {
        ActorSolicitud actor = tramite.actorDe(token, RolEtiquetaEtapa.DECANO);
        SolicitudCancelacionAsignatura cancelacion = cancelacionDe(uuidSolicitudAcademica);
        SolicitudAcademica solicitud = tramite.solicitudValidada(TipoProcesoAcademico.CANCELACION_ASIGNATURA, uuidSolicitudAcademica,
                AccionEtapa.APROBAR_DECANO, actor);

        if (Boolean.TRUE.equals(aceptarEvaluacion) || decisiones == null || decisiones.isEmpty())
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.FORMA_APROBACION_DECANO);
        Map<String, DecisionAsignatura> porAsignatura = tramite.cobertura(cancelacion.getAsignaturas(), solicitud.getRadicado(), decisiones,
                DecisionAsignatura::getUuidAsignaturaSolicitud);
        int aprobadas = 0;
        for (AsignaturaSolicitudAcademica asignatura : cancelacion.getAsignaturas()) {
            Boolean aprobada = porAsignatura.get(asignatura.getUuidAsignaturaSolicitud()).getAprobada();
            if (aprobada == null)
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.DATO_ASIGNATURA_FALTANTE, DECISION,
                        tramite.nombreDe(asignatura)));
            if (aprobada)
                aprobadas++;
            else {
                asignatura.setSituacionCancelar(null);
                asignatura.setSituacionNuevaMatricula(null);
            }
            asignatura.setAprobadaPorDecano(aprobada);
            asignatura.setObservacionDecision(null);
        }
        if (aprobadas == 0)
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.NINGUNA_ASIGNATURA_APROBADA);
        if (aprobadas < cancelacion.getAsignaturas().size() && !tramite.tieneTexto(observacion))
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.COMENTARIO_DECISION_PARCIAL_REQUERIDO);

        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.APROBAR_DECANO, actor, observacion, token);
        cancelacion.setAsignaturas(tramite.guardarAsignaturas(cancelacion.getAsignaturas()));
        log.crearLog("Registrar decisión del Decano por asignatura",
                String.format("Solicitud académica %s, acción %s: %d asignaturas aprobadas y %d no aprobadas", actualizada.getRadicado(),
                        AccionEtapa.APROBAR_DECANO, aprobadas, cancelacion.getAsignaturas().size() - aprobadas),
                token);
        return conSolicitud(cancelacion, actualizada);
    }

    @Override
    public SolicitudCancelacionAsignatura decidirPorConsejo(String uuidSolicitudAcademica, List<DecisionAsignatura> decisiones, String token) {
        ActorSolicitud actor = tramite.actorDe(token, RolEtiquetaEtapa.CONSEJO);
        SolicitudCancelacionAsignatura cancelacion = cancelacionDe(uuidSolicitudAcademica);
        if (decisiones == null || decisiones.isEmpty())
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.FORMA_DECISION_CONSEJO);
        SolicitudAcademica solicitud = cancelacion.getSolicitudAcademica();
        Map<String, DecisionAsignatura> porAsignatura = tramite.cobertura(cancelacion.getAsignaturas(),
                solicitud == null ? uuidSolicitudAcademica : solicitud.getRadicado(), decisiones, DecisionAsignatura::getUuidAsignaturaSolicitud);
        int aprobadas = 0;
        for (AsignaturaSolicitudAcademica asignatura : cancelacion.getAsignaturas()) {
            Boolean aprobada = porAsignatura.get(asignatura.getUuidAsignaturaSolicitud()).getAprobada();
            if (aprobada == null)
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.DATO_ASIGNATURA_FALTANTE, DECISION_CONSEJO,
                        tramite.nombreDe(asignatura)));
            if (aprobada)
                aprobadas++;
            else {
                asignatura.setSituacionCancelar(null);
                asignatura.setSituacionNuevaMatricula(null);
            }
            asignatura.setAprobadaPorDecano(aprobada);
            asignatura.setObservacionDecision(null);
        }
        AccionEtapa accion = aprobadas > 0 ? AccionEtapa.APROBAR_CONSEJO : AccionEtapa.NO_APROBAR_CONSEJO;
        tramite.solicitudValidada(TipoProcesoAcademico.CANCELACION_ASIGNATURA, uuidSolicitudAcademica, accion, actor);

        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, accion, actor, null, token);
        cancelacion.setAsignaturas(tramite.guardarAsignaturas(cancelacion.getAsignaturas()));
        log.crearLog("Registrar decisión del Consejo por asignatura",
                String.format("Solicitud académica %s, acción %s: %d asignaturas aprobadas y %d no aprobadas", actualizada.getRadicado(),
                        accion, aprobadas, cancelacion.getAsignaturas().size() - aprobadas),
                token);
        return conSolicitud(cancelacion, actualizada);
    }

    @Override
    public SolicitudCancelacionAsignatura rechazarPorDecano(String uuidSolicitudAcademica, String observacion, String token) {
        ActorSolicitud actor = tramite.actorDe(token, RolEtiquetaEtapa.DECANO);
        SolicitudCancelacionAsignatura cancelacion = cancelacionDe(uuidSolicitudAcademica);
        if (!tramite.tieneTexto(observacion))
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.COMENTARIO_RECHAZO_DECANO_REQUERIDO);
        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.RECHAZAR_DECANO, actor, observacion, token);
        return conSolicitud(cancelacion, actualizada);
    }

    @Override
    public SolicitudCancelacionAsignatura enviarRespuesta(String uuidSolicitudAcademica, String observacion, List<AjusteAsignatura> ajustes,
                                                          ComunicacionRespuesta comunicacion, String token) {
        ActorSolicitud actor = tramite.actorDe(token, RolEtiquetaEtapa.FUNCIONARIO);
        SolicitudCancelacionAsignatura cancelacion = cancelacionDe(uuidSolicitudAcademica);
        SolicitudAcademica solicitud = tramite.solicitudValidada(TipoProcesoAcademico.CANCELACION_ASIGNATURA, uuidSolicitudAcademica,
                AccionEtapa.ENVIAR_RESPUESTA, actor);

        List<AjusteAsignatura> recibidos = ajustes == null ? List.of() : ajustes;
        boolean rechazadaPorDecano = NO_APROBADAS_CON_DECISION.contains(solicitud.getEtapa().getCodigo());
        if (rechazadaPorDecano && !recibidos.isEmpty())
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.AJUSTES_SOLICITUD_RECHAZADA, solicitud.getRadicado()));
        if (!rechazadaPorDecano)
            aplicarAjustes(cancelacion.getAsignaturas(), solicitud.getRadicado(), recibidos);
        boolean hayRechazos = rechazadaPorDecano || cancelacion.getAsignaturas().stream()
                .anyMatch(asignatura -> Boolean.FALSE.equals(asignatura.getAprobadaPorDecano()));
        if (hayRechazos && !tramite.tieneTexto(observacion))
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.OBSERVACION_RESPUESTA_REQUERIDA);
        notificacionCU.validarComunicacion(comunicacion);

        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.ENVIAR_RESPUESTA, actor, observacion, token);
        if (!recibidos.isEmpty()) {
            tramite.guardarAsignaturas(cancelacion.getAsignaturas());
            log.crearLog("Ajustar asignaturas de cancelación de asignatura",
                    String.format("Solicitud académica %s, acción %s: %d asignaturas ajustadas", actualizada.getRadicado(),
                            AccionEtapa.ENVIAR_RESPUESTA, recibidos.size()),
                    token);
        }
        if (APROBADA.equals(actualizada.getEtapa().getCodigo())) {
            int canceladas = tramite.cancelarActivas(cancelacion.getAsignaturas().stream()
                    .filter(asignatura -> Boolean.TRUE.equals(asignatura.getAprobadaPorDecano()))
                    .toList());
            log.crearLog("Cancelar asignaturas matriculadas",
                    String.format("Solicitud académica %s aprobada: %d asignaturas matriculadas pasan a %s", actualizada.getRadicado(),
                            canceladas, EstadoAsignaturaMatriculadaConstantes.CANCELADA),
                    token);
        }
        notificacionCU.programar(actualizada, comunicacion, token);
        return conSolicitud(cancelacion, actualizada);
    }

    private void aplicarAjustes(List<AsignaturaSolicitudAcademica> asignaturas, String radicado, List<AjusteAsignatura> ajustes) {
        Map<String, AsignaturaSolicitudAcademica> propias = new HashMap<>();
        asignaturas.forEach(asignatura -> propias.put(asignatura.getUuidAsignaturaSolicitud(), asignatura));
        Map<String, AjusteAsignatura> porAsignatura = new HashMap<>();
        for (AjusteAsignatura ajuste : ajustes) {
            String uuid = ajuste == null ? null : ajuste.getUuidAsignaturaSolicitud();
            if (uuid == null || !propias.containsKey(uuid))
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ASIGNATURA_AJENA, uuid, radicado));
            if (porAsignatura.put(uuid, ajuste) != null)
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ASIGNATURA_REPETIDA, tramite.nombreDe(propias.get(uuid))));
        }
        Map<String, SituacionAcademicaAsignatura> catalogo = porAsignatura.isEmpty() ? Map.of() : tramite.catalogoSituaciones();
        for (AsignaturaSolicitudAcademica asignatura : asignaturas) {
            String nombre = tramite.nombreDe(asignatura);
            AjusteAsignatura ajuste = porAsignatura.get(asignatura.getUuidAsignaturaSolicitud());
            if (ajuste == null)
                continue;
            String observacionDecision = tramite.textoAcotado(ajuste.getObservacionDecision(), MAXIMO_OBSERVACION, OBSERVACION_DECISION, nombre);
            if (Boolean.TRUE.equals(asignatura.getAprobadaPorDecano())) {
                if (tramite.tieneTexto(ajuste.getUuidSituacionMatricula()))
                    asignatura.setSituacionMatricula(tramite.situacionDe(catalogo, ajuste.getUuidSituacionMatricula(), AmbitoSituacionAcademica.MATRICULA));
                if (tramite.tieneTexto(ajuste.getUuidSituacionCancelar()))
                    asignatura.setSituacionCancelar(tramite.situacionDe(catalogo, ajuste.getUuidSituacionCancelar(), AmbitoSituacionAcademica.CANCELACION));
                if (tramite.tieneTexto(ajuste.getUuidSituacionNuevaMatricula()))
                    asignatura.setSituacionNuevaMatricula(tramite.situacionDe(catalogo, ajuste.getUuidSituacionNuevaMatricula(),
                            AmbitoSituacionAcademica.MATRICULA));
            } else if (tramite.tieneTexto(ajuste.getUuidSituacionMatricula())) {
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.SITUACION_MATRICULA_RECHAZADA, nombre));
            } else if (tramite.tieneTexto(ajuste.getUuidSituacionCancelar())) {
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.SITUACION_CANCELAR_RECHAZADA, nombre));
            } else if (tramite.tieneTexto(ajuste.getUuidSituacionNuevaMatricula())) {
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.SITUACION_NUEVA_MATRICULA_RECHAZADA, nombre));
            }
            asignatura.setObservacionDecision(observacionDecision);
        }
        for (AsignaturaSolicitudAcademica asignatura : asignaturas)
            if (Boolean.TRUE.equals(asignatura.getAprobadaPorDecano()))
                exigirSituaciones(asignatura);
    }

    private void exigirSituaciones(AsignaturaSolicitudAcademica asignatura) {
        String faltante = asignatura.getSituacionMatricula() == null ? SITUACION_MATRICULA
                : asignatura.getSituacionCancelar() == null ? SITUACION_CANCELAR
                : asignatura.getSituacionNuevaMatricula() == null ? NUEVA_MATRICULA : null;
        if (faltante != null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.DATO_ASIGNATURA_FALTANTE, faltante,
                    tramite.nombreDe(asignatura)));
    }

    private SolicitudCancelacionAsignatura cancelacionDe(String uuidSolicitudAcademica) {
        SolicitudCancelacionAsignatura cancelacion = tramite.tieneTexto(uuidSolicitudAcademica) ? gateway.getPorSolicitud(uuidSolicitudAcademica) : null;
        if (cancelacion == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, CANCELACION_ASIGNATURA, uuidSolicitudAcademica));
        return cancelacion;
    }

    private SolicitudCancelacionAsignatura conSolicitud(SolicitudCancelacionAsignatura cancelacion, SolicitudAcademica solicitud) {
        cancelacion.setSolicitudAcademica(solicitud);
        return cancelacion;
    }
}
