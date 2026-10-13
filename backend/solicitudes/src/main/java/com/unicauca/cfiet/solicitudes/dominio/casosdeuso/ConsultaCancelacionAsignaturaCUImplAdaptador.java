package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaCancelacionAsignaturaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaSolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EstadoAsignaturaMatriculadaConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.util.List;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.AnexoAcademicoConstantes.FORMATOS_SOPORTE_LIBRE;
import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.AnexoAcademicoConstantes.TAMANIO_MAXIMO_BYTES;

public class ConsultaCancelacionAsignaturaCUImplAdaptador implements ConsultaCancelacionAsignaturaCUIntPuerto {
    private static final String SOLICITUD_ACADEMICA = "Solicitud académica";
    private static final String USUARIO = "Usuario";
    private static final String USERNAME = "username";
    private static final String SOPORTE_LIBRE = "Soporte libre";

    private final SolicitudCancelacionAsignaturaGatewayIntPuerto gateway;
    private final EstudianteGatewayIntPuerto estudianteGateway;
    private final SesionGatewayIntPuerto sesionGateway;
    private final IJwtServicio jwtServicio;
    private final ConsultaSolicitudAcademicaCUIntPuerto consultaCU;
    private final MaquinaEtapas maquinaEtapas;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;

    public ConsultaCancelacionAsignaturaCUImplAdaptador(SolicitudCancelacionAsignaturaGatewayIntPuerto gateway,
                                                        EstudianteGatewayIntPuerto estudianteGateway,
                                                        SesionGatewayIntPuerto sesionGateway,
                                                        IJwtServicio jwtServicio,
                                                        ConsultaSolicitudAcademicaCUIntPuerto consultaCU,
                                                        MaquinaEtapas maquinaEtapas,
                                                        ExcepcionesFormateadorIntPuerto formateadorExcepciones) {
        this.gateway = gateway;
        this.estudianteGateway = estudianteGateway;
        this.sesionGateway = sesionGateway;
        this.jwtServicio = jwtServicio;
        this.consultaCU = consultaCU;
        this.maquinaEtapas = maquinaEtapas;
        this.formateadorExcepciones = formateadorExcepciones;
    }

    @Override
    public String getEstudianteAutenticado(String token) {
        String username = jwtServicio.getUsername(token);
        if (!tieneTexto(username))
            formateadorExcepciones.lanzarErrorGenerico(MensajesError.USERNAME_TOKEN);
        Usuario usuario = sesionGateway.getUsuario(username);
        if (usuario == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO, USUARIO, USERNAME, username));
        boolean esEstudiante = usuario.getRoles() != null && usuario.getRoles().stream()
                .anyMatch(rol -> ApplicationConstantes.ESTUDIANTE_ROL.equals(rol.getNombre()));
        if (!esEstudiante || estudianteGateway.getPorUuid(usuario.getUuidUsuario()) == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ACTOR_SIN_ROL, usuario.getUuidUsuario(), ApplicationConstantes.ESTUDIANTE_ROL));
        return usuario.getUuidUsuario();
    }

    @Override
    public FormularioCancelacionAsignatura getFormulario(String token) {
        String uuidEstudiante = getEstudianteAutenticado(token);
        List<AsignaturaMatriculada> activas = estudianteGateway.getAsignaturasMatriculadas(uuidEstudiante).stream()
                .filter(asignatura -> EstadoAsignaturaMatriculadaConstantes.ACTIVA.equals(asignatura.getEstado()))
                .toList();
        return FormularioCancelacionAsignatura.builder()
                .asignaturas(activas)
                .soportes(List.of(SoportePermitido.builder()
                        .nombre(SOPORTE_LIBRE)
                        .formatosPermitidos(FORMATOS_SOPORTE_LIBRE)
                        .tamanioMaximoBytes(TAMANIO_MAXIMO_BYTES)
                        .obligatorio(false)
                        .build()))
                .build();
    }

    @Override
    public DetalleCancelacionAsignatura getDetalle(String uuidSolicitudAcademica, String token) {
        SolicitudCancelacionAsignatura cancelacion = tieneTexto(uuidSolicitudAcademica) ? gateway.getPorSolicitud(uuidSolicitudAcademica) : null;
        if (cancelacion == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, SOLICITUD_ACADEMICA, uuidSolicitudAcademica));
        DetalleSolicitudAcademica detalle = consultaCU.getDetalle(uuidSolicitudAcademica, token);
        RolEtiquetaEtapa rol = consultaCU.resolverActor(uuidSolicitudAcademica, token).getRol();
        List<AsignaturaSolicitudAcademica> asignaturas = cancelacion.getAsignaturas();
        if (rol == RolEtiquetaEtapa.ESTUDIANTE) {
            boolean esFinal = maquinaEtapas.esFinal(detalle.getSolicitudAcademica().getEtapa().getCodigo());
            asignaturas = asignaturas.stream().map(asignatura -> vistaEstudiante(asignatura, esFinal)).toList();
        }
        return DetalleCancelacionAsignatura.builder()
                .detalle(detalle)
                .motivoCancelacion(cancelacion.getMotivoCancelacion())
                .asignaturas(asignaturas)
                .build();
    }

    private AsignaturaSolicitudAcademica vistaEstudiante(AsignaturaSolicitudAcademica asignatura, boolean esFinal) {
        return AsignaturaSolicitudAcademica.builder()
                .uuidAsignaturaSolicitud(asignatura.getUuidAsignaturaSolicitud())
                .asignaturaMatriculada(asignatura.getAsignaturaMatriculada())
                .aprobadaPorDecano(esFinal ? asignatura.getAprobadaPorDecano() : null)
                .observacionDecision(esFinal ? asignatura.getObservacionDecision() : null)
                .build();
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
