package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaCancelacionMatriculaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaSolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EstadoAsignaturaMatriculadaConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.util.List;

public class ConsultaCancelacionMatriculaCUImplAdaptador implements ConsultaCancelacionMatriculaCUIntPuerto {
    private static final String SOLICITUD_ACADEMICA = "Solicitud académica";
    private static final String TIPO_SOLICITUD_ACADEMICA = "Tipo de solicitud académica";
    private static final String USUARIO = "Usuario";
    private static final String USERNAME = "username";
    private static final String NOMBRE = "nombre";

    private final SolicitudCancelacionMatriculaGatewayIntPuerto gateway;
    private final EstudianteGatewayIntPuerto estudianteGateway;
    private final TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway;
    private final TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway;
    private final SesionGatewayIntPuerto sesionGateway;
    private final IJwtServicio jwtServicio;
    private final ConsultaSolicitudAcademicaCUIntPuerto consultaCU;
    private final MaquinaEtapas maquinaEtapas;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;

    public ConsultaCancelacionMatriculaCUImplAdaptador(SolicitudCancelacionMatriculaGatewayIntPuerto gateway,
                                                       EstudianteGatewayIntPuerto estudianteGateway,
                                                       TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                       TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway,
                                                       SesionGatewayIntPuerto sesionGateway,
                                                       IJwtServicio jwtServicio,
                                                       ConsultaSolicitudAcademicaCUIntPuerto consultaCU,
                                                       MaquinaEtapas maquinaEtapas,
                                                       ExcepcionesFormateadorIntPuerto formateadorExcepciones) {
        this.gateway = gateway;
        this.estudianteGateway = estudianteGateway;
        this.tipoSolicitudGateway = tipoSolicitudGateway;
        this.tipoAnexoGateway = tipoAnexoGateway;
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
    public FormularioCancelacionMatricula getFormulario(String token) {
        String uuidEstudiante = getEstudianteAutenticado(token);
        TipoSolicitudAcademica tipo = tipoSolicitudGateway.getTodos().stream()
                .filter(t -> TipoProcesoAcademico.porNombre(t.getNombre()) == TipoProcesoAcademico.CANCELACION_MATRICULA)
                .findFirst()
                .orElse(null);
        if (tipo == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO,
                    TIPO_SOLICITUD_ACADEMICA, NOMBRE, TipoProcesoAcademico.CANCELACION_MATRICULA.getNombre()));
        List<AsignaturaMatriculada> activas = estudianteGateway.getAsignaturasMatriculadas(uuidEstudiante).stream()
                .filter(asignatura -> EstadoAsignaturaMatriculadaConstantes.ACTIVA.equals(asignatura.getEstado()))
                .toList();
        return FormularioCancelacionMatricula.builder()
                .anexos(tipoAnexoGateway.getPorTipo(tipo.getUuidTipoSolicitudAcademica()))
                .asignaturas(activas)
                .build();
    }

    @Override
    public DetalleCancelacionMatricula getDetalle(String uuidSolicitudAcademica, String token) {
        SolicitudCancelacionMatricula cancelacion = tieneTexto(uuidSolicitudAcademica) ? gateway.getPorSolicitud(uuidSolicitudAcademica) : null;
        if (cancelacion == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, SOLICITUD_ACADEMICA, uuidSolicitudAcademica));
        DetalleSolicitudAcademica detalle = consultaCU.getDetalle(uuidSolicitudAcademica, token);
        RolEtiquetaEtapa rol = consultaCU.resolverActor(uuidSolicitudAcademica, token).getRol();
        boolean ocultarSituacionCancelar = rol == RolEtiquetaEtapa.ESTUDIANTE
                && !maquinaEtapas.esFinal(detalle.getSolicitudAcademica().getEtapa().getCodigo());
        if (ocultarSituacionCancelar)
            cancelacion.getAsignaturas().forEach(asignatura -> asignatura.setSituacionCancelar(null));
        return DetalleCancelacionMatricula.builder()
                .detalle(detalle)
                .motivoCancelacion(cancelacion.getMotivoCancelacion())
                .asignaturas(cancelacion.getAsignaturas())
                .build();
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
