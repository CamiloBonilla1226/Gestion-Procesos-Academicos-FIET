package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaExamenSupletorioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaSolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EstadoAsignaturaMatriculadaConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.AnexoAcademicoConstantes.*;

public class ConsultaExamenSupletorioCUImplAdaptador implements ConsultaExamenSupletorioCUIntPuerto {
    private static final String SOLICITUD_ACADEMICA = "Solicitud académica";
    private static final String TIPO_SOLICITUD_ACADEMICA = "Tipo de solicitud académica";
    private static final String USUARIO = "Usuario";
    private static final String USERNAME = "username";
    private static final String NOMBRE = "nombre";
    private static final List<String> ORDEN_ANEXOS = List.of(FORMATO_FOR_23, SOPORTE_JUSTIFICACION, FORMATO_DOCENTE_CRUCE);
    private static final Map<String, List<CausaSupletorio>> CAUSAS_POR_ANEXO = Map.of(
            FORMATO_FOR_23, List.of(CausaSupletorio.CRUCE, CausaSupletorio.OTRA),
            SOPORTE_JUSTIFICACION, List.of(CausaSupletorio.OTRA),
            FORMATO_DOCENTE_CRUCE, List.of(CausaSupletorio.CRUCE));

    private final SolicitudExamenSupletorioGatewayIntPuerto gateway;
    private final EstudianteGatewayIntPuerto estudianteGateway;
    private final TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway;
    private final TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway;
    private final SesionGatewayIntPuerto sesionGateway;
    private final IJwtServicio jwtServicio;
    private final ConsultaSolicitudAcademicaCUIntPuerto consultaCU;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;

    public ConsultaExamenSupletorioCUImplAdaptador(SolicitudExamenSupletorioGatewayIntPuerto gateway,
                                                   EstudianteGatewayIntPuerto estudianteGateway,
                                                   TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                   TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway,
                                                   SesionGatewayIntPuerto sesionGateway,
                                                   IJwtServicio jwtServicio,
                                                   ConsultaSolicitudAcademicaCUIntPuerto consultaCU,
                                                   ExcepcionesFormateadorIntPuerto formateadorExcepciones) {
        this.gateway = gateway;
        this.estudianteGateway = estudianteGateway;
        this.tipoSolicitudGateway = tipoSolicitudGateway;
        this.tipoAnexoGateway = tipoAnexoGateway;
        this.sesionGateway = sesionGateway;
        this.jwtServicio = jwtServicio;
        this.consultaCU = consultaCU;
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
    public FormularioExamenSupletorio getFormulario(String token) {
        String uuidEstudiante = getEstudianteAutenticado(token);
        TipoSolicitudAcademica tipo = tipoSolicitudGateway.getTodos().stream()
                .filter(t -> TipoProcesoAcademico.porNombre(t.getNombre()) == TipoProcesoAcademico.EXAMEN_SUPLETORIO)
                .findFirst()
                .orElse(null);
        if (tipo == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO,
                    TIPO_SOLICITUD_ACADEMICA, NOMBRE, TipoProcesoAcademico.EXAMEN_SUPLETORIO.getNombre()));
        List<AsignaturaMatriculada> activas = estudianteGateway.getAsignaturasMatriculadas(uuidEstudiante).stream()
                .filter(asignatura -> EstadoAsignaturaMatriculadaConstantes.ACTIVA.equals(asignatura.getEstado()))
                .toList();
        List<AnexoSupletorio> anexos = tipoAnexoGateway.getPorTipo(tipo.getUuidTipoSolicitudAcademica()).stream()
                .filter(tipoAnexo -> CAUSAS_POR_ANEXO.containsKey(tipoAnexo.getNombre()))
                .sorted(Comparator.comparingInt(tipoAnexo -> ORDEN_ANEXOS.indexOf(tipoAnexo.getNombre())))
                .map(tipoAnexo -> AnexoSupletorio.builder()
                        .tipoAnexo(tipoAnexo)
                        .tamanioMaximoBytes(TAMANIO_MAXIMO_BYTES)
                        .causas(CAUSAS_POR_ANEXO.get(tipoAnexo.getNombre()))
                        .build())
                .toList();
        return FormularioExamenSupletorio.builder()
                .asignaturas(activas)
                .causas(List.of(CausaSupletorio.values()))
                .anexos(anexos)
                .plazoDiasHabiles(ExamenSupletorioCUImplAdaptador.DIAS_HABILES_PLAZO)
                .build();
    }

    @Override
    public DetalleExamenSupletorio getDetalle(String uuidSolicitudAcademica, String token) {
        SolicitudExamenSupletorio supletorio = tieneTexto(uuidSolicitudAcademica) ? gateway.getPorSolicitud(uuidSolicitudAcademica) : null;
        if (supletorio == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, SOLICITUD_ACADEMICA, uuidSolicitudAcademica));
        return DetalleExamenSupletorio.builder()
                .detalle(consultaCU.getDetalle(uuidSolicitudAcademica, token))
                .supletorio(supletorio)
                .build();
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
