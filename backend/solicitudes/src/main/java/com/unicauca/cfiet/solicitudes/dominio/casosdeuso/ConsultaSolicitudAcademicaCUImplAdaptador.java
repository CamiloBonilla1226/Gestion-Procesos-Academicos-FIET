package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AnexoAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaSolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.AnexoAcademicoConstantes.DOCUMENTO_ADICIONAL;

public class ConsultaSolicitudAcademicaCUImplAdaptador implements ConsultaSolicitudAcademicaCUIntPuerto {
    private static final String SOLICITUD_ACADEMICA = "Solicitud académica";
    private static final String ANEXO_ACADEMICO = "Anexo académico";
    private static final String USUARIO = "Usuario";
    private static final String USERNAME = "username";
    private static final Map<RolEtiquetaEtapa, String> ROLES = Map.of(
            RolEtiquetaEtapa.ESTUDIANTE, ApplicationConstantes.ESTUDIANTE_ROL,
            RolEtiquetaEtapa.FUNCIONARIO, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL,
            RolEtiquetaEtapa.DECANO, ApplicationConstantes.DECANO);
    private static final Set<AccionEtapa> OCULTAS_ESTUDIANTE =
            EnumSet.of(AccionEtapa.REMITIR_DECANO, AccionEtapa.APROBAR_DECANO, AccionEtapa.RECHAZAR_DECANO);
    private static final Set<AccionEtapa> FILAS_OCULTAS_ESTUDIANTE = EnumSet.of(AccionEtapa.APROBAR_DECANO, AccionEtapa.RECHAZAR_DECANO);
    private static final Set<String> DECISIONES_DECANO =
            Set.of(EtapaSolicitudAcademicaConstantes.APROBADA_POR_DECANO, EtapaSolicitudAcademicaConstantes.RECHAZADA_POR_DECANO);
    private static final Set<String> ETAPAS_RESPONDIDAS =
            Set.of(EtapaSolicitudAcademicaConstantes.APROBADA, EtapaSolicitudAcademicaConstantes.RECHAZADA);
    private static final List<String> ACCIONES_DECANO =List.of(AccionEtapa.APROBAR_DECANO.name(), AccionEtapa.RECHAZAR_DECANO.name());
    private static final Set<AccionEtapa> FILAS_RETENIDAS_ESTUDIANTE = EnumSet.of(AccionEtapa.ENVIAR_RESPUESTA);
    private static final String ETIQUETA_EN_TRAMITE = "En trámite";

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
    private final NotificacionResolucionGatewayIntPuerto notificacionGateway;

    public ConsultaSolicitudAcademicaCUImplAdaptador(SolicitudAcademicaGatewayIntPuerto gateway,
                                                     EtapaEtiquetaRolGatewayIntPuerto etiquetaGateway,
                                                     AnexoAcademicoGatewayIntPuerto anexoGateway,
                                                     HistorialSolicitudAcademicaGatewayIntPuerto historialGateway,
                                                     ResolucionAcademicaGatewayIntPuerto resolucionGateway,
                                                     SesionGatewayIntPuerto sesionGateway,
                                                     IJwtServicio jwtServicio,
                                                     AnexoAcademicoCUIntPuerto anexoCU,
                                                     MaquinaEtapas maquinaEtapas,
                                                     ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                     NotificacionResolucionGatewayIntPuerto notificacionGateway) {
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
        this.notificacionGateway = notificacionGateway;
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
        boolean tieneResolucion = !acceso.retenida() && resolucionGateway.existePorSolicitud(uuid);
        boolean puedeDescargar = tieneResolucion && (acceso.rol() != RolEtiquetaEtapa.ESTUDIANTE || maquinaEtapas.esFinal(etapa));
        TipoProcesoAcademico proceso = TipoProcesoAcademico.porNombre(solicitud.getTipoSolicitudAcademica().getNombre());
        List<AnexoAcademico> anexos = anexoGateway.getPorSolicitud(uuid);
        return DetalleSolicitudAcademica.builder()
                .solicitudAcademica(solicitud)
                .etiqueta(acceso.etiqueta())
                .etapaCodigo(acceso.retenida() ? EtapaSolicitudAcademicaConstantes.EN_REVISION_DECANO : etapaVisible(etapa, acceso.rol()))
                .numeroDocumento(acceso.rol() == RolEtiquetaEtapa.ESTUDIANTE ? null : numeroDocumento(solicitud))
                .anexos(acceso.retenida() ? anexos.stream().filter(anexo -> !esDocumentoAdicional(anexo)).toList() : anexos)
                .tieneResolucion(tieneResolucion)
                .puedeDescargarResolucion(puedeDescargar)
                .accionesDisponibles(proceso == null ? List.of() : maquinaEtapas.accionesDisponibles(proceso, etapa, acceso.rol()))
                .build();
    }

    @Override
    public List<HistorialSolicitudAcademica> getHistorial(String uuidSolicitudAcademica, String token) {
        Acceso acceso = acceso(uuidSolicitudAcademica, token);
        List<HistorialSolicitudAcademica> historial = historialGateway.getPorSolicitud(acceso.solicitud().getUuidSolicitudAcademica());
        TipoSolicitudAcademica tipo = acceso.solicitud().getTipoSolicitudAcademica();
        TipoProcesoAcademico proceso = tipo == null ? null : TipoProcesoAcademico.porNombre(tipo.getNombre());
        if (historial != null && proceso != null)
            asignarEtapas(historial, proceso);
        if (historial == null || acceso.rol() != RolEtiquetaEtapa.ESTUDIANTE)
            return historial;
        historial.stream()
                .filter(fila -> OCULTAS_ESTUDIANTE.contains(accionDe(fila.getAccion())))
                .forEach(fila -> fila.setObservaciones(null));
        List<HistorialSolicitudAcademica> visibles = historial.stream()
                .filter(fila -> !FILAS_OCULTAS_ESTUDIANTE.contains(accionDe(fila.getAccion())))
                .filter(fila -> !acceso.retenida() || !FILAS_RETENIDAS_ESTUDIANTE.contains(accionDe(fila.getAccion())))
                .toList();
        visibles.forEach(fila -> fila.setEtapaCodigo(etapaVisible(fila.getEtapaCodigo(), RolEtiquetaEtapa.ESTUDIANTE)));
        return visibles;
    }

    private String etapaVisible(String codigo, RolEtiquetaEtapa rol) {
        if (rol == RolEtiquetaEtapa.ESTUDIANTE && codigo != null && DECISIONES_DECANO.contains(codigo))
            return EtapaSolicitudAcademicaConstantes.EN_REVISION_DECANO;
        return codigo;
    }

    private void asignarEtapas(List<HistorialSolicitudAcademica> historial, TipoProcesoAcademico proceso) {
        List<HistorialSolicitudAcademica> pendientes = new ArrayList<>(historial);
        String etapa = null;
        boolean avanzo = true;
        while (avanzo && !pendientes.isEmpty()) {
            avanzo = false;
            for (HistorialSolicitudAcademica fila : pendientes) {
                String destino = maquinaEtapas.etapaDestino(proceso, etapa, accionDe(fila.getAccion()));
                if (destino != null) {
                    fila.setEtapaCodigo(destino);
                    pendientes.remove(fila);
                    etapa = destino;
                    avanzo = true;
                    break;
                }
            }
        }
    }

    private AccionEtapa accionDe(String accion) {
        for (AccionEtapa candidata : AccionEtapa.values())
            if (candidata.name().equals(accion))
                return candidata;
        return null;
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
        if (!acceso.solicitud().getUuidSolicitudAcademica().equals(deLaSolicitud) || (acceso.retenida() && esDocumentoAdicional(anexo)))
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, ANEXO_ACADEMICO, uuidAnexoAcademico));
        return anexoCU.obtenerAnexo(uuidAnexoAcademico, acceso.actor());
    }

    private Acceso acceso(String uuidSolicitudAcademica, String token) {
        Usuario usuario = usuarioDe(token);
        SolicitudAcademica solicitud = tieneTexto(uuidSolicitudAcademica) ? gateway.getPorUuid(uuidSolicitudAcademica) : null;
        RolEtiquetaEtapa rol = solicitud == null ? null : rolEn(solicitud, usuario);
        String etiqueta = rol == null || solicitud.getEtapa() == null ? null : etiquetaDe(solicitud, rol, etiquetas(rol));
        if (etiqueta == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(
                    MensajesError.ENTIDAD_NO_ENCONTRADA, SOLICITUD_ACADEMICA, uuidSolicitudAcademica));
        boolean retenida = rol == RolEtiquetaEtapa.ESTUDIANTE && notificacionGateway.estaRetenida(solicitud.getUuidSolicitudAcademica());
        ActorSolicitud actor = ActorSolicitud.builder().uuidUsuario(usuario.getUuidUsuario()).rol(rol).build();
        return new Acceso(solicitud, actor, rol, retenida ? etiquetaEnTramite() : etiqueta, retenida);
    }

    private String etiquetaEnTramite() {
        return etiquetaGateway.getPorRol(RolEtiquetaEtapa.ESTUDIANTE).stream()
                .filter(fila -> fila.getEtapa() != null && EtapaSolicitudAcademicaConstantes.EN_REVISION_DECANO.equals(fila.getEtapa().getCodigo()))
                .map(EtapaEtiquetaRol::getEtiqueta)
                .findFirst()
                .orElse(ETIQUETA_EN_TRAMITE);
    }

    private boolean esDocumentoAdicional(AnexoAcademico anexo) {
        return anexo != null && anexo.getTipoAnexoAcademico() != null && DOCUMENTO_ADICIONAL.equals(anexo.getTipoAnexoAcademico().getNombre());
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
        List<SolicitudAcademica> visibles = solicitudes.stream()
                .filter(solicitud -> solicitud.getEtapa() != null && etiquetas.containsKey(solicitud.getEtapa().getUuidEtapa()))
                .toList();
        Map<String, String> decisiones = rol == RolEtiquetaEtapa.ESTUDIANTE ? Map.of() : decisionesDecano(visibles);
        Map<String, List<ResultadoAsignaturaSolicitud>> resultados = rol == RolEtiquetaEtapa.FUNCIONARIO ? resultadosAsignaturas(visibles) : Map.of();
        Set<String> retenidas = rol == RolEtiquetaEtapa.ESTUDIANTE
                ? notificacionGateway.getRetenidas(visibles.stream().map(SolicitudAcademica::getUuidSolicitudAcademica).toList()) : Set.of();
        String enTramite = retenidas.isEmpty() ? null : etiquetaEnTramite();
        return visibles.stream()
                .map(solicitud -> ResumenSolicitudAcademica.builder()
                        .solicitudAcademica(solicitud)
                        .etiqueta(retenidas.contains(solicitud.getUuidSolicitudAcademica()) ? enTramite : etiquetaDe(solicitud, rol, etiquetas))
                        .etapaCodigo(retenidas.contains(solicitud.getUuidSolicitudAcademica()) ? EtapaSolicitudAcademicaConstantes.EN_REVISION_DECANO
                                : etapaVisible(solicitud.getEtapa().getCodigo(), rol))
                        .numeroDocumento(rol == RolEtiquetaEtapa.ESTUDIANTE ? null : numeroDocumento(solicitud))
                        .decisionDecano(decisiones.get(solicitud.getUuidSolicitudAcademica()))
                        .resultadosAsignaturas(resultados.get(solicitud.getUuidSolicitudAcademica()))
                        .build())
                .toList();
    }

    private Map<String, List<ResultadoAsignaturaSolicitud>> resultadosAsignaturas(List<SolicitudAcademica> solicitudes) {
        List<String> uuids = solicitudes.stream()
                .filter(solicitud -> procesoDe(solicitud) == TipoProcesoAcademico.CANCELACION_ASIGNATURA)
                .filter(solicitud -> ETAPAS_RESPONDIDAS.contains(solicitud.getEtapa().getCodigo()))
                .map(SolicitudAcademica::getUuidSolicitudAcademica)
                .toList();
        if (uuids.isEmpty())
            return Map.of();
        Map<String, List<ResultadoAsignaturaSolicitud>> resultados = gateway.getResultadosAsignaturas(uuids);
        return resultados == null ? Map.of() : resultados;
    }

    private Map<String, String> decisionesDecano(List<SolicitudAcademica> solicitudes) {
        if (solicitudes.isEmpty())
            return Map.of();
        List<String> uuids = solicitudes.stream().map(SolicitudAcademica::getUuidSolicitudAcademica).toList();
        Map<String, String> acciones = gateway.getUltimaAccion(uuids, ACCIONES_DECANO);
        List<String> asignaturasAprobadas = solicitudes.stream()
                .filter(solicitud -> AccionEtapa.APROBAR_DECANO.name().equals(acciones.get(solicitud.getUuidSolicitudAcademica())))
                .filter(solicitud -> procesoDe(solicitud) == TipoProcesoAcademico.CANCELACION_ASIGNATURA)
                .map(SolicitudAcademica::getUuidSolicitudAcademica)
                .toList();
        Set<String> parciales = asignaturasAprobadas.isEmpty() ? Set.of() : gateway.getConAsignaturaNoAprobadaPorDecano(asignaturasAprobadas);
        Map<String, String> decisiones = new HashMap<>();
        acciones.forEach((uuid, accion) -> {
            if (AccionEtapa.RECHAZAR_DECANO.name().equals(accion))
                decisiones.put(uuid, EtapaSolicitudAcademicaConstantes.DECISION_RECHAZADA);
            else if (AccionEtapa.APROBAR_DECANO.name().equals(accion))
                decisiones.put(uuid, parciales.contains(uuid)
                        ? EtapaSolicitudAcademicaConstantes.DECISION_APROBADA_PARCIAL : EtapaSolicitudAcademicaConstantes.DECISION_APROBADA);
        });
        return decisiones;
    }

    private TipoProcesoAcademico procesoDe(SolicitudAcademica solicitud) {
        TipoSolicitudAcademica tipo = solicitud.getTipoSolicitudAcademica();
        return tipo == null ? null : TipoProcesoAcademico.porNombre(tipo.getNombre());
    }

    private String etiquetaDe(SolicitudAcademica solicitud, RolEtiquetaEtapa rol, Map<String, String> etiquetas) {
        String etiqueta = etiquetas.get(solicitud.getEtapa().getUuidEtapa());
        TipoSolicitudAcademica tipo = solicitud.getTipoSolicitudAcademica();
        boolean supletorioAprobado = rol == RolEtiquetaEtapa.FUNCIONARIO
                && EtapaSolicitudAcademicaConstantes.APROBADA_POR_DECANO.equals(solicitud.getEtapa().getCodigo())
                && tipo != null && TipoProcesoAcademico.porNombre(tipo.getNombre()) == TipoProcesoAcademico.EXAMEN_SUPLETORIO;
        return etiqueta != null && supletorioAprobado ? EtapaSolicitudAcademicaConstantes.ETIQUETA_FUNCIONARIO_SUPLETORIO_APROBADO : etiqueta;
    }

    private String numeroDocumento(SolicitudAcademica solicitud) {
        Estudiante estudiante = solicitud.getEstudiante();
        Usuario usuario = estudiante == null ? null : estudiante.getUsuario();
        return usuario == null ? null : usuario.getNumeroDocumento();
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

    private record Acceso(SolicitudAcademica solicitud, ActorSolicitud actor, RolEtiquetaEtapa rol, String etiqueta, boolean retenida) {
    }
}
