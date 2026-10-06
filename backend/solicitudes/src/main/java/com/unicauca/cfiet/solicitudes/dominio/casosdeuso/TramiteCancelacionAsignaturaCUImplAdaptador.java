package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.SolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.TramiteCancelacionAsignaturaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EstadoAsignaturaMatriculadaConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
import com.unicauca.cfiet.solicitudes.dominio.servicios.TramiteCancelacion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.APROBADA;

public class TramiteCancelacionAsignaturaCUImplAdaptador implements TramiteCancelacionAsignaturaCUIntPuerto {
    private static final String CANCELACION_ASIGNATURA = "Solicitud de cancelación de asignatura";
    private static final String SITUACION_MATRICULA = "la situación en la matrícula";
    private static final String SITUACION_CANCELAR = "la situación al cancelar";
    private static final String CUMPLE_CONDICIONES = "la confirmación de las condiciones";
    private static final String DECISION = "la decisión del Decano";
    private static final String OBSERVACION_EVALUACION = "La observación de la evaluación";
    private static final String OBSERVACION_DECISION = "La observación de la decisión";
    private static final BigDecimal NOTA_MINIMA_CUMPLE = new BigDecimal("3.0");
    private static final int MAXIMO_OBSERVACION = 255;

    private final SolicitudCancelacionAsignaturaGatewayIntPuerto gateway;
    private final SolicitudAcademicaCUIntPuerto solicitudCU;
    private final TramiteCancelacion tramite;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;

    public TramiteCancelacionAsignaturaCUImplAdaptador(SolicitudCancelacionAsignaturaGatewayIntPuerto gateway,
                                                       SolicitudCancelacionMatriculaGatewayIntPuerto asignaturaSolicitudGateway,
                                                       SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                                       SituacionAcademicaAsignaturaGatewayIntPuerto situacionGateway,
                                                       UsuarioGatewayIntPuerto usuarioGateway,
                                                       SesionGatewayIntPuerto sesionGateway,
                                                       IJwtServicio jwtServicio,
                                                       SolicitudAcademicaCUIntPuerto solicitudCU,
                                                       MaquinaEtapas maquinaEtapas,
                                                       ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                       LogCUIntPuerto log) {
        this.gateway = gateway;
        this.solicitudCU = solicitudCU;
        this.tramite = new TramiteCancelacion(asignaturaSolicitudGateway, solicitudGateway, situacionGateway, usuarioGateway, sesionGateway,
                jwtServicio, maquinaEtapas, formateadorExcepciones);
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
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
        ActorSolicitud actor = tramite.actorDe(token, RolEtiquetaEtapa.FUNCIONARIO);
        SolicitudCancelacionAsignatura cancelacion = cancelacionDe(uuidSolicitudAcademica);
        SolicitudAcademica solicitud = tramite.solicitudValidada(TipoProcesoAcademico.CANCELACION_ASIGNATURA, uuidSolicitudAcademica,
                AccionEtapa.REMITIR_DECANO, actor);

        Map<String, SituacionAcademicaAsignatura> catalogo = tramite.catalogoSituaciones();
        Map<String, EvaluacionAsignatura> porAsignatura = tramite.cobertura(cancelacion.getAsignaturas(), solicitud.getRadicado(), evaluaciones,
                EvaluacionAsignatura::getUuidAsignaturaSolicitud);
        int cumplen = 0;
        for (AsignaturaSolicitudAcademica asignatura : cancelacion.getAsignaturas()) {
            EvaluacionAsignatura evaluacion = porAsignatura.get(asignatura.getUuidAsignaturaSolicitud());
            String nombre = tramite.nombreDe(asignatura);
            asignatura.setNumeroFaltas(tramite.faltasDe(evaluacion.getNumeroFaltas(), nombre));
            asignatura.setNota(tramite.notaDe(evaluacion.getNota(), nombre));
            asignatura.setSituacionMatricula(tramite.situacionDe(catalogo, evaluacion.getUuidSituacionMatricula(), SITUACION_MATRICULA, nombre));
            asignatura.setCumpleCondiciones(cumpleDe(evaluacion, asignatura.getNota(), nombre));
            asignatura.setObservacionEvaluacion(tramite.textoAcotado(evaluacion.getObservacionEvaluacion(), MAXIMO_OBSERVACION,
                    OBSERVACION_EVALUACION, nombre));
            if (!asignatura.getCumpleCondiciones() && asignatura.getObservacionEvaluacion() == null)
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.OBSERVACION_EVALUACION_REQUERIDA, nombre));
            if (asignatura.getCumpleCondiciones())
                cumplen++;
        }
        if (cumplen == 0)
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.NINGUNA_ASIGNATURA_CUMPLE);

        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.REMITIR_DECANO, actor, observacion, token);
        cancelacion.setAsignaturas(tramite.guardarAsignaturas(cancelacion.getAsignaturas()));
        log.crearLog("Evaluar asignaturas de cancelación de asignatura",
                String.format("Solicitud académica %s, acción %s: %d asignaturas evaluadas, %d cumplen las condiciones", actualizada.getRadicado(),
                        AccionEtapa.REMITIR_DECANO, cancelacion.getAsignaturas().size(), cumplen),
                token);
        return conSolicitud(cancelacion, actualizada);
    }

    @Override
    public SolicitudCancelacionAsignatura aprobarPorDecano(String uuidSolicitudAcademica, List<DecisionAsignatura> decisiones, String token) {
        ActorSolicitud actor = tramite.actorDe(token, RolEtiquetaEtapa.DECANO);
        SolicitudCancelacionAsignatura cancelacion = cancelacionDe(uuidSolicitudAcademica);
        SolicitudAcademica solicitud = tramite.solicitudValidada(TipoProcesoAcademico.CANCELACION_ASIGNATURA, uuidSolicitudAcademica,
                AccionEtapa.APROBAR_DECANO, actor);

        Map<String, SituacionAcademicaAsignatura> catalogo = tramite.catalogoSituaciones();
        Map<String, DecisionAsignatura> porAsignatura = tramite.cobertura(cancelacion.getAsignaturas(), solicitud.getRadicado(), decisiones,
                DecisionAsignatura::getUuidAsignaturaSolicitud);
        int aprobadas = 0;
        for (AsignaturaSolicitudAcademica asignatura : cancelacion.getAsignaturas()) {
            DecisionAsignatura decision = porAsignatura.get(asignatura.getUuidAsignaturaSolicitud());
            String nombre = tramite.nombreDe(asignatura);
            if (decision.getAprobada() == null)
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.DATO_ASIGNATURA_FALTANTE, DECISION, nombre));
            String observacionDecision = tramite.textoAcotado(decision.getObservacionDecision(), MAXIMO_OBSERVACION, OBSERVACION_DECISION, nombre);
            if (decision.getAprobada()) {
                if (!Boolean.TRUE.equals(asignatura.getCumpleCondiciones()))
                    formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ASIGNATURA_NO_APROBABLE, nombre));
                asignatura.setSituacionCancelar(tramite.situacionDe(catalogo, decision.getUuidSituacionCancelar(), SITUACION_CANCELAR, nombre));
                aprobadas++;
            } else {
                if (observacionDecision == null)
                    formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.OBSERVACION_DECISION_REQUERIDA, nombre));
                asignatura.setSituacionCancelar(null);
            }
            asignatura.setAprobadaPorDecano(decision.getAprobada());
            asignatura.setObservacionDecision(observacionDecision);
        }
        if (aprobadas == 0)
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.NINGUNA_ASIGNATURA_APROBADA);

        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.APROBAR_DECANO, actor, null, token);
        cancelacion.setAsignaturas(tramite.guardarAsignaturas(cancelacion.getAsignaturas()));
        log.crearLog("Registrar decisión del Decano por asignatura",
                String.format("Solicitud académica %s, acción %s: %d asignaturas aprobadas y %d rechazadas", actualizada.getRadicado(),
                        AccionEtapa.APROBAR_DECANO, aprobadas, cancelacion.getAsignaturas().size() - aprobadas),
                token);
        return conSolicitud(cancelacion, actualizada);
    }

    @Override
    public SolicitudCancelacionAsignatura rechazarPorDecano(String uuidSolicitudAcademica, String observacion, String token) {
        ActorSolicitud actor = tramite.actorDe(token, RolEtiquetaEtapa.DECANO);
        SolicitudCancelacionAsignatura cancelacion = cancelacionDe(uuidSolicitudAcademica);
        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.RECHAZAR_DECANO, actor, observacion, token);
        return conSolicitud(cancelacion, actualizada);
    }

    @Override
    public SolicitudCancelacionAsignatura enviarRespuesta(String uuidSolicitudAcademica, String token) {
        ActorSolicitud actor = tramite.actorDe(token, RolEtiquetaEtapa.FUNCIONARIO);
        SolicitudCancelacionAsignatura cancelacion = cancelacionDe(uuidSolicitudAcademica);
        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.ENVIAR_RESPUESTA, actor, null, token);
        if (APROBADA.equals(actualizada.getEtapa().getCodigo())) {
            int canceladas = tramite.cancelarActivas(cancelacion.getAsignaturas().stream()
                    .filter(asignatura -> Boolean.TRUE.equals(asignatura.getAprobadaPorDecano()))
                    .toList());
            log.crearLog("Cancelar asignaturas matriculadas",
                    String.format("Solicitud académica %s aprobada: %d asignaturas matriculadas pasan a %s", actualizada.getRadicado(),
                            canceladas, EstadoAsignaturaMatriculadaConstantes.CANCELADA),
                    token);
        }
        return conSolicitud(cancelacion, actualizada);
    }

    private Boolean cumpleDe(EvaluacionAsignatura evaluacion, BigDecimal nota, String nombre) {
        Boolean cumple = evaluacion.getCumpleCondiciones();
        if (cumple == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.DATO_ASIGNATURA_FALTANTE, CUMPLE_CONDICIONES, nombre));
        if (cumple && nota.compareTo(NOTA_MINIMA_CUMPLE) < 0)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.NOTA_INSUFICIENTE_CUMPLE, nombre, nota.toPlainString()));
        return cumple;
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
