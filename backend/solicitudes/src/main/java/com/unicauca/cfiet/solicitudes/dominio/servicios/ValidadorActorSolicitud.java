package com.unicauca.cfiet.solicitudes.dominio.servicios;

import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ActorSolicitud;
import com.unicauca.cfiet.solicitudes.dominio.modelos.RolEtiquetaEtapa;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.util.Map;

public class ValidadorActorSolicitud {
    private static final String USUARIO = "Usuario";
    private static final Map<RolEtiquetaEtapa, String> ROLES = Map.of(
            RolEtiquetaEtapa.ESTUDIANTE, ApplicationConstantes.ESTUDIANTE_ROL,
            RolEtiquetaEtapa.FUNCIONARIO, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL,
            RolEtiquetaEtapa.DECANO, ApplicationConstantes.DECANO);

    private final UsuarioGatewayIntPuerto usuarioGateway;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;

    public ValidadorActorSolicitud(UsuarioGatewayIntPuerto usuarioGateway, ExcepcionesFormateadorIntPuerto formateadorExcepciones) {
        this.usuarioGateway = usuarioGateway;
        this.formateadorExcepciones = formateadorExcepciones;
    }

    public void validarActorCompleto(ActorSolicitud actor) {
        if (actor == null || actor.getUuidUsuario() == null || actor.getUuidUsuario().isBlank() || actor.getRol() == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.DATOS_TRANSICION_INCOMPLETOS);
    }

    public void validarRol(ActorSolicitud actor) {
        Usuario usuario = usuarioGateway.getUsuario(actor.getUuidUsuario());
        if (usuario == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, USUARIO, actor.getUuidUsuario()));
        String rolRequerido = ROLES.get(actor.getRol());
        boolean tieneRol = usuario.getRoles() != null && usuario.getRoles().stream()
                .anyMatch(rol -> rolRequerido.equals(rol.getNombre()));
        if (!tieneRol)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ACTOR_SIN_ROL, actor.getUuidUsuario(), rolRequerido));
    }

    public void validarAlcance(SolicitudAcademica solicitud, ActorSolicitud actor) {
        if (actor.getRol() == RolEtiquetaEtapa.ESTUDIANTE) {
            String duenio = solicitud.getEstudiante() == null ? null : solicitud.getEstudiante().getUuidUsuario();
            if (!actor.getUuidUsuario().equals(duenio))
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(
                        MensajesError.SOLICITUD_AJENA, solicitud.getRadicado(), actor.getUuidUsuario()));
        }
        if (actor.getRol() == RolEtiquetaEtapa.FUNCIONARIO) {
            TipoSolicitudAcademica tipo = solicitud.getTipoSolicitudAcademica();
            String asignado = tipo.getFuncionarioAcademico() == null ? null : tipo.getFuncionarioAcademico().getUuidUsuario();
            if (!actor.getUuidUsuario().equals(asignado))
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(
                        MensajesError.TIPO_NO_ASIGNADO_FUNCIONARIO, tipo.getNombre(), actor.getUuidUsuario()));
        }
    }

    public void validar(SolicitudAcademica solicitud, ActorSolicitud actor) {
        validarActorCompleto(actor);
        validarRol(actor);
        validarAlcance(solicitud, actor);
    }
}
