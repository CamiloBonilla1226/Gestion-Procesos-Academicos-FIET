package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.SolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.TramiteCancelacionMatriculaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EstadoAsignaturaMatriculadaConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
import com.unicauca.cfiet.solicitudes.dominio.servicios.ValidadorActorSolicitud;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.APROBADA;

public class TramiteCancelacionMatriculaCUImplAdaptador implements TramiteCancelacionMatriculaCUIntPuerto {
    private static final String SOLICITUD_ACADEMICA = "Solicitud académica";
    private static final String CANCELACION_MATRICULA = "Solicitud de cancelación de matrícula";
    private static final String SITUACION_ACADEMICA = "Situación académica de asignatura";
    private static final String USUARIO = "Usuario";
    private static final String USERNAME = "username";
    private static final String NUMERO_FALTAS = "el número de faltas";
    private static final String NOTA = "la nota";
    private static final String SITUACION_MATRICULA = "la situación en la matrícula";
    private static final String SITUACION_CANCELAR = "la situación al cancelar";
    private static final BigDecimal NOTA_MINIMA = BigDecimal.ZERO;
    private static final BigDecimal NOTA_MAXIMA = new BigDecimal("5.0");
    private static final Map<RolEtiquetaEtapa, String> ROLES = Map.of(
            RolEtiquetaEtapa.FUNCIONARIO, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL,
            RolEtiquetaEtapa.DECANO, ApplicationConstantes.DECANO);

    private final SolicitudCancelacionMatriculaGatewayIntPuerto gateway;
    private final SolicitudAcademicaGatewayIntPuerto solicitudGateway;
    private final SituacionAcademicaAsignaturaGatewayIntPuerto situacionGateway;
    private final SesionGatewayIntPuerto sesionGateway;
    private final IJwtServicio jwtServicio;
    private final SolicitudAcademicaCUIntPuerto solicitudCU;
    private final ValidadorActorSolicitud validadorActor;
    private final MaquinaEtapas maquinaEtapas;
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
        this.solicitudGateway = solicitudGateway;
        this.situacionGateway = situacionGateway;
        this.sesionGateway = sesionGateway;
        this.jwtServicio = jwtServicio;
        this.solicitudCU = solicitudCU;
        this.validadorActor = new ValidadorActorSolicitud(usuarioGateway, formateadorExcepciones);
        this.maquinaEtapas = maquinaEtapas;
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
    }

    @Override
    public SolicitudCancelacionMatricula rechazarPorFuncionario(String uuidSolicitudAcademica, String observacion, String token) {
        ActorSolicitud actor = actorDe(token, RolEtiquetaEtapa.FUNCIONARIO);
        SolicitudCancelacionMatricula cancelacion = cancelacionDe(uuidSolicitudAcademica);
        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.RECHAZAR_FUNCIONARIO, actor, observacion, token);
        return conSolicitud(cancelacion, actualizada);
    }

    @Override
    public SolicitudCancelacionMatricula remitirADecano(String uuidSolicitudAcademica, List<EvaluacionAsignatura> evaluaciones,
                                                        String observacion, String token) {
        ActorSolicitud actor = actorDe(token, RolEtiquetaEtapa.FUNCIONARIO);
        SolicitudCancelacionMatricula cancelacion = cancelacionDe(uuidSolicitudAcademica);
        SolicitudAcademica solicitud = solicitudDe(uuidSolicitudAcademica);
        validarTransicion(solicitud, AccionEtapa.REMITIR_DECANO, actor);

        Map<String, SituacionAcademicaAsignatura> catalogo = catalogoSituaciones();
        Map<String, EvaluacionAsignatura> porAsignatura = cobertura(cancelacion, solicitud, evaluaciones,
                EvaluacionAsignatura::getUuidAsignaturaSolicitud);
        for (AsignaturaSolicitudAcademica asignatura : cancelacion.getAsignaturas()) {
            EvaluacionAsignatura evaluacion = porAsignatura.get(asignatura.getUuidAsignaturaSolicitud());
            String nombre = nombreDe(asignatura);
            asignatura.setNumeroFaltas(faltasDe(evaluacion.getNumeroFaltas(), nombre));
            asignatura.setNota(notaDe(evaluacion.getNota(), nombre));
            asignatura.setSituacionMatricula(situacionDe(catalogo, evaluacion.getUuidSituacionMatricula(), SITUACION_MATRICULA, nombre));
        }

        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.REMITIR_DECANO, actor, observacion, token);
        cancelacion.setAsignaturas(gateway.actualizarAsignaturas(cancelacion.getAsignaturas()));
        log.crearLog("Evaluar asignaturas de cancelación de matrícula",
                String.format("Solicitud académica %s, acción %s: %d asignaturas evaluadas", actualizada.getRadicado(),
                        AccionEtapa.REMITIR_DECANO, cancelacion.getAsignaturas().size()),
                token);
        return conSolicitud(cancelacion, actualizada);
    }

    @Override
    public SolicitudCancelacionMatricula aprobarPorDecano(String uuidSolicitudAcademica, List<SituacionCancelarAsignatura> situacionesAlCancelar,
                                                          String token) {
        ActorSolicitud actor = actorDe(token, RolEtiquetaEtapa.DECANO);
        SolicitudCancelacionMatricula cancelacion = cancelacionDe(uuidSolicitudAcademica);
        SolicitudAcademica solicitud = solicitudDe(uuidSolicitudAcademica);
        validarTransicion(solicitud, AccionEtapa.APROBAR_DECANO, actor);

        Map<String, SituacionAcademicaAsignatura> catalogo = catalogoSituaciones();
        Map<String, SituacionCancelarAsignatura> porAsignatura = cobertura(cancelacion, solicitud, situacionesAlCancelar,
                SituacionCancelarAsignatura::getUuidAsignaturaSolicitud);
        for (AsignaturaSolicitudAcademica asignatura : cancelacion.getAsignaturas()) {
            SituacionCancelarAsignatura situacion = porAsignatura.get(asignatura.getUuidAsignaturaSolicitud());
            asignatura.setSituacionCancelar(situacionDe(catalogo, situacion.getUuidSituacionCancelar(), SITUACION_CANCELAR, nombreDe(asignatura)));
        }

        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.APROBAR_DECANO, actor, null, token);
        cancelacion.setAsignaturas(gateway.actualizarAsignaturas(cancelacion.getAsignaturas()));
        log.crearLog("Registrar situación al cancelar",
                String.format("Solicitud académica %s, acción %s: situación al cancelar de %d asignaturas", actualizada.getRadicado(),
                        AccionEtapa.APROBAR_DECANO, cancelacion.getAsignaturas().size()),
                token);
        return conSolicitud(cancelacion, actualizada);
    }

    @Override
    public SolicitudCancelacionMatricula rechazarPorDecano(String uuidSolicitudAcademica, String observacion, String token) {
        ActorSolicitud actor = actorDe(token, RolEtiquetaEtapa.DECANO);
        SolicitudCancelacionMatricula cancelacion = cancelacionDe(uuidSolicitudAcademica);
        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.RECHAZAR_DECANO, actor, observacion, token);
        return conSolicitud(cancelacion, actualizada);
    }

    @Override
    public SolicitudCancelacionMatricula enviarRespuesta(String uuidSolicitudAcademica, String token) {
        ActorSolicitud actor = actorDe(token, RolEtiquetaEtapa.FUNCIONARIO);
        SolicitudCancelacionMatricula cancelacion = cancelacionDe(uuidSolicitudAcademica);
        SolicitudAcademica actualizada = solicitudCU.cambiarEtapa(uuidSolicitudAcademica, AccionEtapa.ENVIAR_RESPUESTA, actor, null, token);
        if (APROBADA.equals(actualizada.getEtapa().getCodigo())) {
            List<String> matriculadas = cancelacion.getAsignaturas().stream()
                    .map(asignatura -> asignatura.getAsignaturaMatriculada().getUuidAsignaturaMatriculada())
                    .toList();
            gateway.cambiarEstadoAsignaturasMatriculadas(matriculadas, EstadoAsignaturaMatriculadaConstantes.CANCELADA);
            cancelacion.getAsignaturas().forEach(asignatura -> asignatura.getAsignaturaMatriculada().setEstado(EstadoAsignaturaMatriculadaConstantes.CANCELADA));
            log.crearLog("Cancelar asignaturas matriculadas",
                    String.format("Solicitud académica %s aprobada: %d asignaturas matriculadas pasan a %s", actualizada.getRadicado(),
                            matriculadas.size(), EstadoAsignaturaMatriculadaConstantes.CANCELADA),
                    token);
        }
        return conSolicitud(cancelacion, actualizada);
    }

    private ActorSolicitud actorDe(String token, RolEtiquetaEtapa rol) {
        String username = jwtServicio.getUsername(token);
        if (!tieneTexto(username))
            formateadorExcepciones.lanzarErrorGenerico(MensajesError.USERNAME_TOKEN);
        Usuario usuario = sesionGateway.getUsuario(username);
        if (usuario == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO, USUARIO, USERNAME, username));
        String nombreRol = ROLES.get(rol);
        boolean tieneRol = usuario.getRoles() != null && usuario.getRoles().stream().anyMatch(r -> nombreRol.equals(r.getNombre()));
        if (!tieneRol)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ACTOR_SIN_ROL, usuario.getUuidUsuario(), nombreRol));
        return ActorSolicitud.builder().uuidUsuario(usuario.getUuidUsuario()).rol(rol).build();
    }

    private SolicitudCancelacionMatricula cancelacionDe(String uuidSolicitudAcademica) {
        SolicitudCancelacionMatricula cancelacion = tieneTexto(uuidSolicitudAcademica) ? gateway.getPorSolicitud(uuidSolicitudAcademica) : null;
        if (cancelacion == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, CANCELACION_MATRICULA, uuidSolicitudAcademica));
        return cancelacion;
    }

    private SolicitudAcademica solicitudDe(String uuidSolicitudAcademica) {
        SolicitudAcademica solicitud = solicitudGateway.getPorUuid(uuidSolicitudAcademica);
        if (solicitud == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, SOLICITUD_ACADEMICA, uuidSolicitudAcademica));
        return solicitud;
    }

    private void validarTransicion(SolicitudAcademica solicitud, AccionEtapa accion, ActorSolicitud actor) {
        validadorActor.validar(solicitud, actor);
        maquinaEtapas.siguienteEtapa(TipoProcesoAcademico.CANCELACION_MATRICULA, solicitud.getEtapa().getCodigo(), accion, actor.getRol());
    }

    private <T> Map<String, T> cobertura(SolicitudCancelacionMatricula cancelacion, SolicitudAcademica solicitud, List<T> recibidas,
                                         Function<T, String> llave) {
        Map<String, AsignaturaSolicitudAcademica> propias = new LinkedHashMap<>();
        for (AsignaturaSolicitudAcademica asignatura : cancelacion.getAsignaturas())
            propias.put(asignatura.getUuidAsignaturaSolicitud(), asignatura);

        Map<String, T> porAsignatura = new HashMap<>();
        for (T recibida : recibidas == null ? List.<T>of() : recibidas) {
            String uuid = recibida == null ? null : llave.apply(recibida);
            if (uuid == null || !propias.containsKey(uuid))
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ASIGNATURA_AJENA, uuid, solicitud.getRadicado()));
            if (porAsignatura.put(uuid, recibida) != null)
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ASIGNATURA_REPETIDA, nombreDe(propias.get(uuid))));
        }
        List<String> faltantes = propias.values().stream()
                .filter(asignatura -> !porAsignatura.containsKey(asignatura.getUuidAsignaturaSolicitud()))
                .map(this::nombreDe)
                .toList();
        if (!faltantes.isEmpty())
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ASIGNATURAS_SIN_EVALUAR, String.join("; ", faltantes)));
        return porAsignatura;
    }

    private Integer faltasDe(Integer numeroFaltas, String asignatura) {
        if (numeroFaltas == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.DATO_ASIGNATURA_FALTANTE, NUMERO_FALTAS, asignatura));
        if (numeroFaltas < 0)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.NUMERO_FALTAS_NO_VALIDO, asignatura));
        return numeroFaltas;
    }

    private BigDecimal notaDe(BigDecimal nota, String asignatura) {
        if (nota == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.DATO_ASIGNATURA_FALTANTE, NOTA, asignatura));
        if (nota.compareTo(NOTA_MINIMA) < 0 || nota.compareTo(NOTA_MAXIMA) > 0 || nota.stripTrailingZeros().scale() > 1)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.NOTA_NO_VALIDA, asignatura));
        return nota.setScale(1);
    }

    private SituacionAcademicaAsignatura situacionDe(Map<String, SituacionAcademicaAsignatura> catalogo, String uuidSituacion,
                                                     String dato, String asignatura) {
        if (!tieneTexto(uuidSituacion))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.DATO_ASIGNATURA_FALTANTE, dato, asignatura));
        SituacionAcademicaAsignatura situacion = catalogo.get(uuidSituacion);
        if (situacion == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, SITUACION_ACADEMICA, uuidSituacion));
        return situacion;
    }

    private Map<String, SituacionAcademicaAsignatura> catalogoSituaciones() {
        Map<String, SituacionAcademicaAsignatura> catalogo = new HashMap<>();
        for (SituacionAcademicaAsignatura situacion : situacionGateway.getTodas())
            catalogo.put(situacion.getUuidSituacionAcademica(), situacion);
        return catalogo;
    }

    private String nombreDe(AsignaturaSolicitudAcademica asignatura) {
        AsignaturaMatriculada matriculada = asignatura.getAsignaturaMatriculada();
        if (matriculada == null || matriculada.getAsignatura() == null)
            return asignatura.getUuidAsignaturaSolicitud();
        return matriculada.getAsignatura().getNombreAsignatura();
    }

    private SolicitudCancelacionMatricula conSolicitud(SolicitudCancelacionMatricula cancelacion, SolicitudAcademica solicitud) {
        cancelacion.setSolicitudAcademica(solicitud);
        return cancelacion;
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
