package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AccesoRecursoConsejoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.IJwtServicio;
import com.unicauca.cfiet.solicitudes.aplicacion.output.RespuestaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SesionGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.InformacionSolicitante;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Respuesta;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Solicitud;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.util.Set;

public class AccesoRecursoConsejoCUImplAdaptador implements AccesoRecursoConsejoCUIntPuerto {
    private static final String USUARIO = "Usuario";
    private static final String USERNAME = "username";
    private static final String SOLICITUD = "Solicitud";
    private static final String RESPUESTA = "Respuesta";
    private static final Set<String> ROLES_INTERNOS = Set.of(
            ApplicationConstantes.SECRETARIO_GENERAL,
            ApplicationConstantes.DECANO,
            ApplicationConstantes.SECRETARIA_DECANATURA_ROL);

    private final UsuarioGatewayIntPuerto usuarioGateway;
    private final SesionGatewayIntPuerto sesionGateway;
    private final SolicitudGatewayIntPuerto solicitudGateway;
    private final RespuestaGatewayIntPuerto respuestaGateway;
    private final IJwtServicio jwtServicio;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;

    public AccesoRecursoConsejoCUImplAdaptador(UsuarioGatewayIntPuerto usuarioGateway,
                                               SesionGatewayIntPuerto sesionGateway,
                                               SolicitudGatewayIntPuerto solicitudGateway,
                                               RespuestaGatewayIntPuerto respuestaGateway,
                                               IJwtServicio jwtServicio,
                                               ExcepcionesFormateadorIntPuerto formateadorExcepciones) {
        this.usuarioGateway = usuarioGateway;
        this.sesionGateway = sesionGateway;
        this.solicitudGateway = solicitudGateway;
        this.respuestaGateway = respuestaGateway;
        this.jwtServicio = jwtServicio;
        this.formateadorExcepciones = formateadorExcepciones;
    }

    @Override
    public Usuario getUsuarioVisible(String uuidUsuario, String token) {
        Usuario actor = usuarioDe(token);
        Usuario usuario = esElMismoOSecretario(actor, uuidUsuario) ? usuarioGateway.getUsuario(uuidUsuario) : null;
        if (usuario == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, USUARIO, uuidUsuario));
        return usuario;
    }

    @Override
    public void verificarCambioPropio(String uuidUsuario, String token) {
        Usuario actor = usuarioDe(token);
        if (!esElMismoOSecretario(actor, uuidUsuario))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.USUARIO_AJENO, actor.getUuidUsuario(), uuidUsuario));
    }

    @Override
    public void verificarAccesoAnexo(String uuidSolicitud, String token) {
        Usuario actor = usuarioDe(token);
        Solicitud solicitud = tieneTexto(uuidSolicitud) ? solicitudGateway.getSolicitud(uuidSolicitud) : null;
        if (solicitud == null || !puedeVer(actor, solicitud))
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, SOLICITUD, uuidSolicitud));
    }

    @Override
    public void verificarAccesoRespuesta(String uuidRespuesta, String token) {
        Usuario actor = usuarioDe(token);
        Respuesta respuesta = tieneTexto(uuidRespuesta) ? respuestaGateway.getRespuesta(uuidRespuesta) : null;
        String uuidSolicitud = respuesta == null || respuesta.getSolicitud() == null ? null : respuesta.getSolicitud().getUuidSolicitud();
        Solicitud solicitud = tieneTexto(uuidSolicitud) ? solicitudGateway.getSolicitud(uuidSolicitud) : null;
        if (solicitud == null || !puedeVer(actor, solicitud))
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, RESPUESTA, uuidRespuesta));
    }

    @Override
    public void verificarActualizacionSolicitud(String uuidSolicitud, String uuidFuncionarioNuevo, String token) {
        Usuario actor = usuarioDe(token);
        Solicitud solicitud = tieneTexto(uuidSolicitud) ? solicitudGateway.getSolicitud(uuidSolicitud) : null;
        if (solicitud == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, SOLICITUD, uuidSolicitud));
        if (tieneRol(actor, ApplicationConstantes.SECRETARIO_GENERAL) || tieneRol(actor, ApplicationConstantes.DECANO))
            return;
        String asignado = funcionarioDe(solicitud);
        if (!tieneRol(actor, ApplicationConstantes.FUNCIONARIO_ROL) || !actor.getUuidUsuario().equals(asignado))
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, SOLICITUD, uuidSolicitud));
        if (tieneTexto(uuidFuncionarioNuevo) && !uuidFuncionarioNuevo.equals(asignado))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.REASIGNACION_NO_PERMITIDA, actor.getUuidUsuario(), uuidSolicitud));
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

    private boolean esElMismoOSecretario(Usuario actor, String uuidUsuario) {
        return actor.getUuidUsuario().equals(uuidUsuario) || tieneRol(actor, ApplicationConstantes.SECRETARIO_GENERAL);
    }

    private boolean puedeVer(Usuario actor, Solicitud solicitud) {
        if (ROLES_INTERNOS.stream().anyMatch(rol -> tieneRol(actor, rol)))
            return true;
        if (tieneRol(actor, ApplicationConstantes.FUNCIONARIO_ROL) && actor.getUuidUsuario().equals(funcionarioDe(solicitud)))
            return true;
        return esSolicitante(actor, solicitud.getInformacionSolicitante());
    }

    private String funcionarioDe(Solicitud solicitud) {
        if (tieneTexto(solicitud.getUuidFuncionario()))
            return solicitud.getUuidFuncionario();
        return solicitud.getObjFuncionario() == null ? null : solicitud.getObjFuncionario().getUuidUsuario();
    }

    private boolean esSolicitante(Usuario actor, InformacionSolicitante solicitante) {
        return solicitante != null
                && tieneTexto(actor.getNumeroDocumento())
                && actor.getNumeroDocumento().equals(solicitante.getNumeroDocumento())
                && actor.getTipoDocumento() != null
                && actor.getTipoDocumento().equals(solicitante.getTipoDocumento());
    }

    private boolean tieneRol(Usuario usuario, String rol) {
        return usuario.getRoles() != null && usuario.getRoles().stream().anyMatch(actual -> rol.equals(actual.getNombre()));
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
