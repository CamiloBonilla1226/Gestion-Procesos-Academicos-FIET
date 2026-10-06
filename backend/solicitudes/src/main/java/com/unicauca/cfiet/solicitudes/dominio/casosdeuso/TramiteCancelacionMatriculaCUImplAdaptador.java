package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.SolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.TramiteCancelacionMatriculaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EstadoAsignaturaMatriculadaConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
import com.unicauca.cfiet.solicitudes.dominio.servicios.TramiteCancelacion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.util.List;
import java.util.Map;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.APROBADA;

public class TramiteCancelacionMatriculaCUImplAdaptador implements TramiteCancelacionMatriculaCUIntPuerto {
    private static final String CANCELACION_MATRICULA = "Solicitud de cancelación de matrícula";
    private static final String SITUACION_MATRICULA = "la situación en la matrícula";
    private static final String SITUACION_CANCELAR = "la situación al cancelar";

    private final SolicitudCancelacionMatriculaGatewayIntPuerto gateway;
    private final SolicitudAcademicaCUIntPuerto solicitudCU;
    private final TramiteCancelacion tramite;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;

    public TramiteCancelacionMatriculaCUImplAdaptador(SolicitudCancelacionMatriculaGatewayIntPuerto gateway,
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
        this.tramite = new TramiteCancelacion(gateway, solicitudGateway, situacionGateway, usuarioGateway, sesionGateway, jwtServicio,
                maquinaEtapas, formateadorExcepciones);
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
    }

    @Override
    public SolicitudCancelacionMatricula rechazarPorFuncionario(String uuidSolicitudAcademica, String observacion, String token) {
        ActorSolicitud actor = tramite.actorDe(token, RolEtiquetaEtapa.FUNCIONARIO);
        SolicitudCancelacionMatricula cancelacion = cancelacionDe(uuidSolicitudAcademica);
        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.RECHAZAR_FUNCIONARIO, actor, observacion, token);
        return conSolicitud(cancelacion, actualizada);
    }

    @Override
    public SolicitudCancelacionMatricula remitirADecano(String uuidSolicitudAcademica, List<EvaluacionAsignatura> evaluaciones,
                                                        String observacion, String token) {
        ActorSolicitud actor = tramite.actorDe(token, RolEtiquetaEtapa.FUNCIONARIO);
        SolicitudCancelacionMatricula cancelacion = cancelacionDe(uuidSolicitudAcademica);
        SolicitudAcademica solicitud = tramite.solicitudValidada(TipoProcesoAcademico.CANCELACION_MATRICULA, uuidSolicitudAcademica,
                AccionEtapa.REMITIR_DECANO, actor);

        Map<String, SituacionAcademicaAsignatura> catalogo = tramite.catalogoSituaciones();
        Map<String, EvaluacionAsignatura> porAsignatura = tramite.cobertura(cancelacion.getAsignaturas(), solicitud.getRadicado(), evaluaciones,
                EvaluacionAsignatura::getUuidAsignaturaSolicitud);
        for (AsignaturaSolicitudAcademica asignatura : cancelacion.getAsignaturas()) {
            EvaluacionAsignatura evaluacion = porAsignatura.get(asignatura.getUuidAsignaturaSolicitud());
            String nombre = tramite.nombreDe(asignatura);
            asignatura.setNumeroFaltas(tramite.faltasDe(evaluacion.getNumeroFaltas(), nombre));
            asignatura.setNota(tramite.notaDe(evaluacion.getNota(), nombre));
            asignatura.setSituacionMatricula(tramite.situacionDe(catalogo, evaluacion.getUuidSituacionMatricula(), SITUACION_MATRICULA, nombre));
        }

        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.REMITIR_DECANO, actor, observacion, token);
        cancelacion.setAsignaturas(tramite.guardarAsignaturas(cancelacion.getAsignaturas()));
        log.crearLog("Evaluar asignaturas de cancelación de matrícula",
                String.format("Solicitud académica %s, acción %s: %d asignaturas evaluadas", actualizada.getRadicado(),
                        AccionEtapa.REMITIR_DECANO, cancelacion.getAsignaturas().size()),
                token);
        return conSolicitud(cancelacion, actualizada);
    }

    @Override
    public SolicitudCancelacionMatricula aprobarPorDecano(String uuidSolicitudAcademica, List<SituacionCancelarAsignatura> situacionesAlCancelar,
                                                          String token) {
        ActorSolicitud actor = tramite.actorDe(token, RolEtiquetaEtapa.DECANO);
        SolicitudCancelacionMatricula cancelacion = cancelacionDe(uuidSolicitudAcademica);
        SolicitudAcademica solicitud = tramite.solicitudValidada(TipoProcesoAcademico.CANCELACION_MATRICULA, uuidSolicitudAcademica,
                AccionEtapa.APROBAR_DECANO, actor);

        Map<String, SituacionAcademicaAsignatura> catalogo = tramite.catalogoSituaciones();
        Map<String, SituacionCancelarAsignatura> porAsignatura = tramite.cobertura(cancelacion.getAsignaturas(), solicitud.getRadicado(),
                situacionesAlCancelar, SituacionCancelarAsignatura::getUuidAsignaturaSolicitud);
        for (AsignaturaSolicitudAcademica asignatura : cancelacion.getAsignaturas()) {
            SituacionCancelarAsignatura situacion = porAsignatura.get(asignatura.getUuidAsignaturaSolicitud());
            asignatura.setSituacionCancelar(tramite.situacionDe(catalogo, situacion.getUuidSituacionCancelar(), SITUACION_CANCELAR,
                    tramite.nombreDe(asignatura)));
        }

        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.APROBAR_DECANO, actor, null, token);
        cancelacion.setAsignaturas(tramite.guardarAsignaturas(cancelacion.getAsignaturas()));
        log.crearLog("Registrar situación al cancelar",
                String.format("Solicitud académica %s, acción %s: situación al cancelar de %d asignaturas", actualizada.getRadicado(),
                        AccionEtapa.APROBAR_DECANO, cancelacion.getAsignaturas().size()),
                token);
        return conSolicitud(cancelacion, actualizada);
    }

    @Override
    public SolicitudCancelacionMatricula rechazarPorDecano(String uuidSolicitudAcademica, String observacion, String token) {
        ActorSolicitud actor = tramite.actorDe(token, RolEtiquetaEtapa.DECANO);
        SolicitudCancelacionMatricula cancelacion = cancelacionDe(uuidSolicitudAcademica);
        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.RECHAZAR_DECANO, actor, observacion, token);
        return conSolicitud(cancelacion, actualizada);
    }

    @Override
    public SolicitudCancelacionMatricula enviarRespuesta(String uuidSolicitudAcademica, String token) {
        ActorSolicitud actor = tramite.actorDe(token, RolEtiquetaEtapa.FUNCIONARIO);
        SolicitudCancelacionMatricula cancelacion = cancelacionDe(uuidSolicitudAcademica);
        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.ENVIAR_RESPUESTA, actor, null, token);
        if (APROBADA.equals(actualizada.getEtapa().getCodigo())) {
            int canceladas = tramite.cancelarActivas(cancelacion.getAsignaturas());
            log.crearLog("Cancelar asignaturas matriculadas",
                    String.format("Solicitud académica %s aprobada: %d asignaturas matriculadas pasan a %s", actualizada.getRadicado(),
                            canceladas, EstadoAsignaturaMatriculadaConstantes.CANCELADA),
                    token);
        }
        return conSolicitud(cancelacion, actualizada);
    }

    private SolicitudCancelacionMatricula cancelacionDe(String uuidSolicitudAcademica) {
        SolicitudCancelacionMatricula cancelacion = tramite.tieneTexto(uuidSolicitudAcademica) ? gateway.getPorSolicitud(uuidSolicitudAcademica) : null;
        if (cancelacion == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, CANCELACION_MATRICULA, uuidSolicitudAcademica));
        return cancelacion;
    }

    private SolicitudCancelacionMatricula conSolicitud(SolicitudCancelacionMatricula cancelacion, SolicitudAcademica solicitud) {
        cancelacion.setSolicitudAcademica(solicitud);
        return cancelacion;
    }
}
