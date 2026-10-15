package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.NotificacionDocenteCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

public class NotificacionDocenteCUImplAdaptador implements NotificacionDocenteCUIntPuerto {
    private static final String USUARIO = "Usuario";
    private static final String USERNAME = "username";
    private static final String NOTIFICACION = "Notificación";
    private static final String ANEXO_ACADEMICO = "Anexo académico";
    private static final String ARCHIVO = "Archivo del anexo académico";

    private final NotificacionDocenteGatewayIntPuerto gateway;
    private final SolicitudAcademicaGatewayIntPuerto solicitudGateway;
    private final SolicitudExamenSupletorioGatewayIntPuerto supletorioGateway;
    private final AnexoAcademicoGatewayIntPuerto anexoGateway;
    private final AlmacenamientoAnexosIntPuerto almacenamiento;
    private final SesionGatewayIntPuerto sesionGateway;
    private final IJwtServicio jwtServicio;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final Clock reloj;

    public NotificacionDocenteCUImplAdaptador(NotificacionDocenteGatewayIntPuerto gateway,
                                              SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                              SolicitudExamenSupletorioGatewayIntPuerto supletorioGateway,
                                              AnexoAcademicoGatewayIntPuerto anexoGateway,
                                              AlmacenamientoAnexosIntPuerto almacenamiento,
                                              SesionGatewayIntPuerto sesionGateway,
                                              IJwtServicio jwtServicio,
                                              ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                              Clock reloj) {
        this.gateway = gateway;
        this.solicitudGateway = solicitudGateway;
        this.supletorioGateway = supletorioGateway;
        this.anexoGateway = anexoGateway;
        this.almacenamiento = almacenamiento;
        this.sesionGateway = sesionGateway;
        this.jwtServicio = jwtServicio;
        this.formateadorExcepciones = formateadorExcepciones;
        this.reloj = reloj;
    }

    @Override
    public List<DetalleNotificacionDocente> getMisNotificaciones(String token) {
        Usuario actor = usuarioDe(token);
        if (!tieneRol(actor, ApplicationConstantes.DOCENTE_ROL))
            return List.of();
        return gateway.getPorDocente(actor.getUuidUsuario()).stream()
                .map(notificacion -> detalle(notificacion, false))
                .toList();
    }

    @Override
    public DetalleNotificacionDocente getNotificacion(String uuidNotificacion, String token) {
        return detalle(notificacionPropia(uuidNotificacion, usuarioDe(token)), true);
    }

    @Override
    public DetalleNotificacionDocente marcarLeida(String uuidNotificacion, String token) {
        NotificacionDocente notificacion = notificacionPropia(uuidNotificacion, usuarioDe(token));
        if (notificacion.getFechaLectura() == null)
            notificacion = gateway.marcarLeida(notificacion.getUuidNotificacion(), LocalDateTime.now(reloj));
        return detalle(notificacion, true);
    }

    @Override
    public ArchivoAdjunto descargarAnexo(String uuidNotificacion, String uuidAnexoAcademico, String token) {
        NotificacionDocente notificacion = notificacionPropia(uuidNotificacion, usuarioDe(token));
        AnexoAcademico anexo = tieneTexto(uuidAnexoAcademico) ? anexoGateway.getPorUuid(uuidAnexoAcademico) : null;
        String uuidSolicitud = anexo == null || anexo.getSolicitudAcademica() == null ? null : anexo.getSolicitudAcademica().getUuidSolicitudAcademica();
        if (!notificacion.getUuidSolicitudAcademica().equals(uuidSolicitud))
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, ANEXO_ACADEMICO, uuidAnexoAcademico));
        byte[] contenido = almacenamiento.leer(anexo.getUrlArchivo());
        if (contenido == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, ARCHIVO, uuidAnexoAcademico));
        return ArchivoAdjunto.builder()
                .nombreOriginal(anexo.getNombreArchivo())
                .tipoContenido(anexo.getTipoArchivo())
                .contenido(contenido)
                .build();
    }

    private NotificacionDocente notificacionPropia(String uuidNotificacion, Usuario actor) {
        NotificacionDocente notificacion = tieneTexto(uuidNotificacion) ? gateway.getPorUuid(uuidNotificacion) : null;
        boolean propia = notificacion != null
                && tieneRol(actor, ApplicationConstantes.DOCENTE_ROL)
                && notificacion.getDocente() != null
                && actor.getUuidUsuario().equals(notificacion.getDocente().getUuidUsuario());
        if (!propia)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, NOTIFICACION, uuidNotificacion));
        return notificacion;
    }

    private DetalleNotificacionDocente detalle(NotificacionDocente notificacion, boolean conAnexos) {
        String uuidSolicitud = notificacion.getUuidSolicitudAcademica();
        return DetalleNotificacionDocente.builder()
                .notificacion(notificacion)
                .solicitudAcademica(solicitudGateway.getPorUuid(uuidSolicitud))
                .supletorio(supletorioGateway.getPorSolicitud(uuidSolicitud))
                .anexos(conAnexos ? anexoGateway.getPorSolicitud(uuidSolicitud) : List.of())
                .build();
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

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
