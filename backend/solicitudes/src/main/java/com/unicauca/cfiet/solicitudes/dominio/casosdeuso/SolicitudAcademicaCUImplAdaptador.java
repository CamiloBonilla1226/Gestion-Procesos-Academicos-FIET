package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.SolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.EstudianteGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.EtapaSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class SolicitudAcademicaCUImplAdaptador implements SolicitudAcademicaCUIntPuerto {
    private static final String SOLICITUD_ACADEMICA = "Solicitud académica";
    private static final String TIPO_SOLICITUD_ACADEMICA = "Tipo de solicitud académica";
    private static final String ESTUDIANTE = "Estudiante";
    private static final String USUARIO = "Usuario";
    private static final String ETAPA = "Etapa de solicitud académica";
    private static final String CODIGO = "codigo";
    private static final int MAXIMO_OBSERVACION = 500;
    private static final Map<RolEtiquetaEtapa, String> ROLES = Map.of(
            RolEtiquetaEtapa.ESTUDIANTE, ApplicationConstantes.ESTUDIANTE_ROL,
            RolEtiquetaEtapa.FUNCIONARIO, ApplicationConstantes.FUNCIONARIO_ACADEMICO_ROL,
            RolEtiquetaEtapa.DECANO, ApplicationConstantes.DECANO);

    private final SolicitudAcademicaGatewayIntPuerto gateway;
    private final EstudianteGatewayIntPuerto estudianteGateway;
    private final TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway;
    private final EtapaSolicitudAcademicaGatewayIntPuerto etapaGateway;
    private final UsuarioGatewayIntPuerto usuarioGateway;
    private final MaquinaEtapas maquinaEtapas;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final Clock reloj;

    public SolicitudAcademicaCUImplAdaptador(SolicitudAcademicaGatewayIntPuerto gateway,
                                             EstudianteGatewayIntPuerto estudianteGateway,
                                             TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                             EtapaSolicitudAcademicaGatewayIntPuerto etapaGateway,
                                             UsuarioGatewayIntPuerto usuarioGateway,
                                             MaquinaEtapas maquinaEtapas,
                                             ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                             Clock reloj) {
        this.gateway = gateway;
        this.estudianteGateway = estudianteGateway;
        this.tipoSolicitudGateway = tipoSolicitudGateway;
        this.etapaGateway = etapaGateway;
        this.usuarioGateway = usuarioGateway;
        this.maquinaEtapas = maquinaEtapas;
        this.formateadorExcepciones = formateadorExcepciones;
        this.reloj = reloj;
    }

    @Override
    public SolicitudAcademica crearSolicitud(String uuidEstudiante, String uuidTipoSolicitudAcademica) {
        Estudiante estudiante = tieneTexto(uuidEstudiante) ? estudianteGateway.getPorUuid(uuidEstudiante) : null;
        if (estudiante == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, ESTUDIANTE, uuidEstudiante));
        TipoSolicitudAcademica tipo = tieneTexto(uuidTipoSolicitudAcademica) ? tipoSolicitudGateway.getPorUuid(uuidTipoSolicitudAcademica) : null;
        if (tipo == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(
                    MensajesError.ENTIDAD_NO_ENCONTRADA, TIPO_SOLICITUD_ACADEMICA, uuidTipoSolicitudAcademica));

        TipoProcesoAcademico proceso = procesoDe(tipo);
        ResultadoTransicion resultado = maquinaEtapas.siguienteEtapa(proceso, null, AccionEtapa.RADICAR, RolEtiquetaEtapa.ESTUDIANTE);
        EtapaSolicitudAcademica etapa = etapaDe(tipo, resultado.getEtapaSiguiente());
        LocalDateTime ahora = LocalDateTime.now(reloj);

        SolicitudAcademica solicitud = SolicitudAcademica.builder()
                .uuidSolicitudAcademica(UUID.randomUUID().toString())
                .radicado(siguienteRadicado(proceso, ahora.getYear()))
                .estudiante(estudiante)
                .tipoSolicitudAcademica(tipo)
                .etapa(etapa)
                .fechaCreacion(ahora)
                .build();
        return gateway.crear(solicitud, historial(solicitud, estudiante.getUuidUsuario(), AccionEtapa.RADICAR, null, ahora));
    }

    @Override
    public SolicitudAcademica cambiarEtapa(String uuidSolicitudAcademica, AccionEtapa accion, ActorSolicitud actor, EntregaTransicion entrega) {
        SolicitudAcademica solicitud = tieneTexto(uuidSolicitudAcademica) ? gateway.getPorUuid(uuidSolicitudAcademica) : null;
        if (solicitud == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(
                    MensajesError.ENTIDAD_NO_ENCONTRADA, SOLICITUD_ACADEMICA, uuidSolicitudAcademica));
        if (actor == null || !tieneTexto(actor.getUuidUsuario()) || actor.getRol() == null || accion == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.DATOS_TRANSICION_INCOMPLETOS);

        validarRolDelActor(actor);
        validarAlcanceDelActor(solicitud, actor);
        String observacion = observacionDe(entrega);

        TipoProcesoAcademico proceso = procesoDe(solicitud.getTipoSolicitudAcademica());
        ResultadoTransicion resultado = maquinaEtapas.transicionar(
                proceso, solicitud.getEtapa().getCodigo(), accion, actor.getRol(), entrega);
        solicitud.setEtapa(etapaDe(solicitud.getTipoSolicitudAcademica(), resultado.getEtapaSiguiente()));

        LocalDateTime ahora = LocalDateTime.now(reloj);
        return gateway.actualizarEtapa(solicitud, historial(solicitud, actor.getUuidUsuario(), accion, observacion, ahora));
    }

    private void validarRolDelActor(ActorSolicitud actor) {
        Usuario usuario = usuarioGateway.getUsuario(actor.getUuidUsuario());
        if (usuario == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, USUARIO, actor.getUuidUsuario()));
        String rolRequerido = ROLES.get(actor.getRol());
        boolean tieneRol = usuario.getRoles() != null && usuario.getRoles().stream()
                .anyMatch(rol -> rolRequerido.equals(rol.getNombre()));
        if (!tieneRol)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ACTOR_SIN_ROL, actor.getUuidUsuario(), rolRequerido));
    }

    private void validarAlcanceDelActor(SolicitudAcademica solicitud, ActorSolicitud actor) {
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

    private String observacionDe(EntregaTransicion entrega) {
        if (entrega == null || !tieneTexto(entrega.getObservacion())) return null;
        String observacion = entrega.getObservacion().trim();
        if (observacion.length() > MAXIMO_OBSERVACION)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.OBSERVACION_MUY_LARGA, MAXIMO_OBSERVACION));
        return observacion;
    }

    private TipoProcesoAcademico procesoDe(TipoSolicitudAcademica tipo) {
        TipoProcesoAcademico proceso = TipoProcesoAcademico.porNombre(tipo.getNombre());
        if (proceso == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.TIPO_SOLICITUD_SIN_PROCESO, tipo.getNombre()));
        return proceso;
    }

    private EtapaSolicitudAcademica etapaDe(TipoSolicitudAcademica tipo, String codigo) {
        EtapaSolicitudAcademica etapa = etapaGateway.getPorTipoIncluyendoUniversales(tipo.getUuidTipoSolicitudAcademica()).stream()
                .filter(e -> codigo.equals(e.getCodigo()))
                .findFirst()
                .orElse(null);
        if (etapa == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO, ETAPA, CODIGO, codigo));
        return etapa;
    }

    private String siguienteRadicado(TipoProcesoAcademico proceso, int anio) {
        String prefijo = String.format("%d-%s-", anio, proceso.getCodigo());
        String ultimo = gateway.getUltimoRadicado(prefijo);
        int consecutivo = ultimo == null ? 1 : Integer.parseInt(ultimo.substring(prefijo.length())) + 1;
        return prefijo + String.format("%04d", consecutivo);
    }

    private HistorialSolicitudAcademica historial(SolicitudAcademica solicitud, String uuidUsuario, AccionEtapa accion,
                                                  String observacion, LocalDateTime fecha) {
        return HistorialSolicitudAcademica.builder()
                .uuidHistorial(UUID.randomUUID().toString())
                .solicitudAcademica(solicitud)
                .usuario(Usuario.builder().uuidUsuario(uuidUsuario).build())
                .accion(accion.name())
                .observaciones(observacion)
                .fecha(fecha)
                .build();
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
