package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AnexoAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaSolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsultaSolicitudAcademicaCUImplAdaptador implements ConsultaSolicitudAcademicaCUIntPuerto {
    private static final String SOLICITUD_ACADEMICA = "Solicitud académica";
    private static final String ANEXO_ACADEMICO = "Anexo académico";
    private static final String USUARIO = "Usuario";
    private static final String USERNAME = "username";
    private static final Map<RolEtiquetaEtapa, String> ROLES = Map.of(
            RolEtiquetaEtapa.ESTUDIANTE, ApplicationConstantes.ESTUDIANTE_ROL,
            RolEtiquetaEtapa.FUNCIONARIO, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL,
            RolEtiquetaEtapa.DECANO, ApplicationConstantes.DECANO);

    private final SolicitudAcademicaGatewayIntPuerto gateway;
    private final EtapaEtiquetaRolGatewayIntPuerto etiquetaGateway;
    private final AnexoAcademicoGatewayIntPuerto anexoGateway;
    private final HistorialSolicitudAcademicaGatewayIntPuerto historialGateway;
    private final ResolucionAcademicaGatewayIntPuerto resolucionGateway;
    private final SesionGatewayIntPuerto sesionGateway;
    private final IJwtServicio jwtServicio;
    private final AnexoAcademicoCUIntPuerto anexoCU;
    private final MaquinaEtapas maquinaEtapas;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;

    public ConsultaSolicitudAcademicaCUImplAdaptador(SolicitudAcademicaGatewayIntPuerto gateway,
                                                     EtapaEtiquetaRolGatewayIntPuerto etiquetaGateway,
                                                     AnexoAcademicoGatewayIntPuerto anexoGateway,
                                                     HistorialSolicitudAcademicaGatewayIntPuerto historialGateway,
                                                     ResolucionAcademicaGatewayIntPuerto resolucionGateway,
                                                     SesionGatewayIntPuerto sesionGateway,
                                                     IJwtServicio jwtServicio,
                                                     AnexoAcademicoCUIntPuerto anexoCU,
                                                     MaquinaEtapas maquinaEtapas,
                                                     ExcepcionesFormateadorIntPuerto formateadorExcepciones) {
        this.gateway = gateway;
        this.etiquetaGateway = etiquetaGateway;
        this.anexoGateway = anexoGateway;
        this.historialGateway = historialGateway;
        this.resolucionGateway = resolucionGateway;
        this.sesionGateway = sesionGateway;
        this.jwtServicio = jwtServicio;
        this.anexoCU = anexoCU;
        this.maquinaEtapas = maquinaEtapas;
        this.formateadorExcepciones = formateadorExcepciones;
    }

    @Override
    public List<ResumenSolicitudAcademica> getBandejaEstudiante(String token) {
        Usuario usuario = usuarioConRol(token, RolEtiquetaEtapa.ESTUDIANTE);
        return resumir(gateway.getPorEstudiante(usuario.getUuidUsuario()), RolEtiquetaEtapa.ESTUDIANTE);
    }

    @Override
    public List<ResumenSolicitudAcademica> getBandejaFuncionario(String token) {
        Usuario usuario = usuarioConRol(token, RolEtiquetaEtapa.FUNCIONARIO);
        return resumir(gateway.getPorFuncionarioAcademico(usuario.getUuidUsuario()), RolEtiquetaEtapa.FUNCIONARIO);
    }

    @Override
    public List<ResumenSolicitudAcademica> getBandejaDecano(String token) {
        usuarioConRol(token, RolEtiquetaEtapa.DECANO);
        return resumir(gateway.getTodas(), RolEtiquetaEtapa.DECANO);
    }

    @Override
    public DetalleSolicitudAcademica getDetalle(String uuidSolicitudAcademica, String token) {
        Acceso acceso = acceso(uuidSolicitudAcademica, token);
        SolicitudAcademica solicitud = acceso.solicitud();
        String uuid = solicitud.getUuidSolicitudAcademica();
        String etapa = solicitud.getEtapa().getCodigo();
        boolean tieneResolucion = resolucionGateway.existePorSolicitud(uuid);
        boolean puedeDescargar = tieneResolucion && (acceso.rol() != RolEtiquetaEtapa.ESTUDIANTE || maquinaEtapas.esFinal(etapa));
        TipoProcesoAcademico proceso = TipoProcesoAcademico.porNombre(solicitud.getTipoSolicitudAcademica().getNombre());
        return DetalleSolicitudAcademica.builder()
                .solicitudAcademica(solicitud)
                .etiqueta(acceso.etiqueta())
                .anexos(anexoGateway.getPorSolicitud(uuid))
                .tieneResolucion(tieneResolucion)
                .puedeDescargarResolucion(puedeDescargar)
                .accionesDisponibles(proceso == null ? List.of() : maquinaEtapas.accionesDisponibles(proceso, etapa, acceso.rol()))
                .build();
    }

    @Override
    public List<HistorialSolicitudAcademica> getHistorial(String uuidSolicitudAcademica, String token) {
        Acceso acceso = acceso(uuidSolicitudAcademica, token);
        return historialGateway.getPorSolicitud(acceso.solicitud().getUuidSolicitudAcademica());
    }

    @Override
    public ActorSolicitud resolverActor(String uuidSolicitudAcademica, String token) {
        return acceso(uuidSolicitudAcademica, token).actor();
    }

    @Override
    public ArchivoAdjunto descargarAnexo(String uuidSolicitudAcademica, String uuidAnexoAcademico, String token) {
        Acceso acceso = acceso(uuidSolicitudAcademica, token);
        AnexoAcademico anexo = tieneTexto(uuidAnexoAcademico) ? anexoGateway.getPorUuid(uuidAnexoAcademico) : null;
        String deLaSolicitud = anexo == null || anexo.getSolicitudAcademica() == null ? null
                : anexo.getSolicitudAcademica().getUuidSolicitudAcademica();
        if (!acceso.solicitud().getUuidSolicitudAcademica().equals(deLaSolicitud))
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, ANEXO_ACADEMICO, uuidAnexoAcademico));
        return anexoCU.obtenerAnexo(uuidAnexoAcademico, acceso.actor());
    }

    private Acceso acceso(String uuidSolicitudAcademica, String token) {
        Usuario usuario = usuarioDe(token);
        SolicitudAcademica solicitud = tieneTexto(uuidSolicitudAcademica) ? gateway.getPorUuid(uuidSolicitudAcademica) : null;
        RolEtiquetaEtapa rol = solicitud == null ? null : rolEn(solicitud, usuario);
        String etiqueta = rol == null || solicitud.getEtapa() == null ? null : etiquetas(rol).get(solicitud.getEtapa().getUuidEtapa());
        if (etiqueta == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(
                    MensajesError.ENTIDAD_NO_ENCONTRADA, SOLICITUD_ACADEMICA, uuidSolicitudAcademica));
        ActorSolicitud actor = ActorSolicitud.builder().uuidUsuario(usuario.getUuidUsuario()).rol(rol).build();
        return new Acceso(solicitud, actor, rol, etiqueta);
    }

    private RolEtiquetaEtapa rolEn(SolicitudAcademica solicitud, Usuario usuario) {
        String uuidUsuario = usuario.getUuidUsuario();
        String duenio = solicitud.getEstudiante() == null ? null : solicitud.getEstudiante().getUuidUsuario();
        if (uuidUsuario.equals(duenio) && tieneRol(usuario, RolEtiquetaEtapa.ESTUDIANTE))
            return RolEtiquetaEtapa.ESTUDIANTE;
        TipoSolicitudAcademica tipo = solicitud.getTipoSolicitudAcademica();
        String asignado = tipo == null || tipo.getFuncionarioAcademico() == null ? null : tipo.getFuncionarioAcademico().getUuidUsuario();
        if (uuidUsuario.equals(asignado) && tieneRol(usuario, RolEtiquetaEtapa.FUNCIONARIO))
            return RolEtiquetaEtapa.FUNCIONARIO;
        if (tieneRol(usuario, RolEtiquetaEtapa.DECANO))
            return RolEtiquetaEtapa.DECANO;
        return null;
    }

    private List<ResumenSolicitudAcademica> resumir(List<SolicitudAcademica> solicitudes, RolEtiquetaEtapa rol) {
        Map<String, String> etiquetas = etiquetas(rol);
        return solicitudes.stream()
                .filter(solicitud -> solicitud.getEtapa() != null && etiquetas.containsKey(solicitud.getEtapa().getUuidEtapa()))
                .map(solicitud -> ResumenSolicitudAcademica.builder()
                        .solicitudAcademica(solicitud)
                        .etiqueta(etiquetas.get(solicitud.getEtapa().getUuidEtapa()))
                        .build())
                .toList();
    }

    private Map<String, String> etiquetas(RolEtiquetaEtapa rol) {
        Map<String, String> etiquetas = new HashMap<>();
        for (EtapaEtiquetaRol fila : etiquetaGateway.getPorRol(rol))
            if (fila.getEtapa() != null)
                etiquetas.put(fila.getEtapa().getUuidEtapa(), fila.getEtiqueta());
        return etiquetas;
    }

    private Usuario usuarioConRol(String token, RolEtiquetaEtapa rol) {
        Usuario usuario = usuarioDe(token);
        if (!tieneRol(usuario, rol))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ACTOR_SIN_ROL, usuario.getUuidUsuario(), ROLES.get(rol)));
        return usuario;
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

    private boolean tieneRol(Usuario usuario, RolEtiquetaEtapa rol) {
        String nombre = ROLES.get(rol);
        return usuario.getRoles() != null && usuario.getRoles().stream().anyMatch(r -> nombre.equals(r.getNombre()));
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    private record Acceso(SolicitudAcademica solicitud, ActorSolicitud actor, RolEtiquetaEtapa rol, String etiqueta) {
    }
}
