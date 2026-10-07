package com.unicauca.cfiet.solicitudes.dominio.servicios;

import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.util.Map;

public class TramiteSolicitud {
    private static final String SOLICITUD_ACADEMICA = "Solicitud académica";
    private static final String USUARIO = "Usuario";
    private static final String USERNAME = "username";
    private static final Map<RolEtiquetaEtapa, String> ROLES = Map.of(
            RolEtiquetaEtapa.ESTUDIANTE, ApplicationConstantes.ESTUDIANTE_ROL,
            RolEtiquetaEtapa.FUNCIONARIO, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL,
            RolEtiquetaEtapa.DECANO, ApplicationConstantes.DECANO);

    private final SolicitudAcademicaGatewayIntPuerto solicitudGateway;
    private final SesionGatewayIntPuerto sesionGateway;
    private final IJwtServicio jwtServicio;
    private final ValidadorActorSolicitud validadorActor;
    private final MaquinaEtapas maquinaEtapas;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;

    public TramiteSolicitud(SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                            UsuarioGatewayIntPuerto usuarioGateway,
                            SesionGatewayIntPuerto sesionGateway,
                            IJwtServicio jwtServicio,
                            MaquinaEtapas maquinaEtapas,
                            ExcepcionesFormateadorIntPuerto formateadorExcepciones) {
        this.solicitudGateway = solicitudGateway;
        this.sesionGateway = sesionGateway;
        this.jwtServicio = jwtServicio;
        this.validadorActor = new ValidadorActorSolicitud(usuarioGateway, formateadorExcepciones);
        this.maquinaEtapas = maquinaEtapas;
        this.formateadorExcepciones = formateadorExcepciones;
    }

    public ActorSolicitud actorDe(String token, RolEtiquetaEtapa rol) {
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

    public SolicitudAcademica solicitudValidada(TipoProcesoAcademico proceso, String uuidSolicitudAcademica, AccionEtapa accion,
                                                ActorSolicitud actor) {
        SolicitudAcademica solicitud = solicitudGateway.getPorUuid(uuidSolicitudAcademica);
        if (solicitud == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, SOLICITUD_ACADEMICA, uuidSolicitudAcademica));
        validadorActor.validar(solicitud, actor);
        maquinaEtapas.siguienteEtapa(proceso, solicitud.getEtapa().getCodigo(), accion, actor.getRol());
        return solicitud;
    }

    public boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
