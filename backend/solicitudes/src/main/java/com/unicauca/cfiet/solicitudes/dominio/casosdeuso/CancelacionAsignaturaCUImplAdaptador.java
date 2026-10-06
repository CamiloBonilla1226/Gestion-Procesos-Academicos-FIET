package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AnexoAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.CancelacionAsignaturaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.SolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EstadoAsignaturaMatriculadaConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.ValidadorArchivoAdjunto;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.util.*;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.AnexoAcademicoConstantes.FORMATOS_SOPORTE_LIBRE;

public class CancelacionAsignaturaCUImplAdaptador implements CancelacionAsignaturaCUIntPuerto {
    private static final String USUARIO = "Usuario";
    private static final String TIPO_SOLICITUD_ACADEMICA = "Tipo de solicitud académica";
    private static final String NOMBRE = "nombre";
    private static final String SOPORTE_LIBRE = "Soporte libre";
    private static final int MAXIMO_MOTIVO = 255;

    private final SolicitudCancelacionAsignaturaGatewayIntPuerto gateway;
    private final SolicitudCancelacionMatriculaGatewayIntPuerto asignaturaSolicitudGateway;
    private final EstudianteGatewayIntPuerto estudianteGateway;
    private final UsuarioGatewayIntPuerto usuarioGateway;
    private final TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway;
    private final SolicitudAcademicaCUIntPuerto solicitudCU;
    private final AnexoAcademicoCUIntPuerto anexoCU;
    private final ValidadorArchivoAdjunto validadorArchivo;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;

    public CancelacionAsignaturaCUImplAdaptador(SolicitudCancelacionAsignaturaGatewayIntPuerto gateway,
                                                SolicitudCancelacionMatriculaGatewayIntPuerto asignaturaSolicitudGateway,
                                                EstudianteGatewayIntPuerto estudianteGateway,
                                                UsuarioGatewayIntPuerto usuarioGateway,
                                                TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                SolicitudAcademicaCUIntPuerto solicitudCU,
                                                AnexoAcademicoCUIntPuerto anexoCU,
                                                ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                LogCUIntPuerto log) {
        this.gateway = gateway;
        this.asignaturaSolicitudGateway = asignaturaSolicitudGateway;
        this.estudianteGateway = estudianteGateway;
        this.usuarioGateway = usuarioGateway;
        this.tipoSolicitudGateway = tipoSolicitudGateway;
        this.solicitudCU = solicitudCU;
        this.anexoCU = anexoCU;
        this.validadorArchivo = new ValidadorArchivoAdjunto(formateadorExcepciones);
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
    }

    @Override
    public SolicitudCancelacionAsignatura radicarCancelacionAsignatura(String uuidEstudiante, String motivoCancelacion,
                                                                       List<String> uuidsAsignaturaMatriculada,
                                                                       List<AnexoRadicacion> anexos, String token) {
        validarEstudiante(uuidEstudiante);
        String motivo = motivoDe(motivoCancelacion);
        List<AsignaturaMatriculada> elegidas = asignaturasElegidas(uuidEstudiante, uuidsAsignaturaMatriculada);
        TipoSolicitudAcademica tipo = tipoCancelacionAsignatura();
        List<AnexoRadicacion> soportes = anexos == null ? List.of() : anexos;
        validarSoportes(soportes);

        SolicitudAcademica solicitud = solicitudCU.crearSolicitud(uuidEstudiante, tipo.getUuidTipoSolicitudAcademica(), token);
        SolicitudCancelacionAsignatura guardada = gateway.guardar(SolicitudCancelacionAsignatura.builder()
                .solicitudAcademica(solicitud)
                .motivoCancelacion(motivo)
                .build());
        guardada.setAsignaturas(asignaturaSolicitudGateway.guardarAsignaturas(solicitud.getUuidSolicitudAcademica(), elegidas.stream()
                .map(asignatura -> AsignaturaSolicitudAcademica.builder()
                        .uuidAsignaturaSolicitud(UUID.randomUUID().toString())
                        .asignaturaMatriculada(asignatura)
                        .build())
                .toList()));

        ActorSolicitud actor = ActorSolicitud.builder().uuidUsuario(uuidEstudiante).rol(RolEtiquetaEtapa.ESTUDIANTE).build();
        for (AnexoRadicacion soporte : soportes)
            anexoCU.adjuntarAnexo(solicitud.getUuidSolicitudAcademica(), null, soporte.getArchivo(), actor, token);

        log.crearLog("Radicar cancelación de asignatura",
                String.format("Solicitud académica %s, acción RADICAR: cancelación de asignatura con %d asignaturas y %d soportes",
                        solicitud.getRadicado(), elegidas.size(), soportes.size()),
                token);
        guardada.setSolicitudAcademica(solicitud);
        return guardada;
    }

    private void validarEstudiante(String uuidEstudiante) {
        Usuario usuario = tieneTexto(uuidEstudiante) ? usuarioGateway.getUsuario(uuidEstudiante) : null;
        if (usuario == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, USUARIO, uuidEstudiante));
        boolean esEstudiante = usuario.getRoles() != null && usuario.getRoles().stream()
                .anyMatch(rol -> ApplicationConstantes.ESTUDIANTE_ROL.equals(rol.getNombre()));
        if (!esEstudiante || estudianteGateway.getPorUuid(uuidEstudiante) == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ACTOR_SIN_ROL, uuidEstudiante, ApplicationConstantes.ESTUDIANTE_ROL));
    }

    private String motivoDe(String motivoCancelacion) {
        if (!tieneTexto(motivoCancelacion))
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.MOTIVO_CANCELACION_REQUERIDO);
        String motivo = motivoCancelacion.trim();
        if (motivo.length() > MAXIMO_MOTIVO)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.MOTIVO_CANCELACION_MUY_LARGO, MAXIMO_MOTIVO));
        return motivo;
    }

    private List<AsignaturaMatriculada> asignaturasElegidas(String uuidEstudiante, List<String> uuidsAsignaturaMatriculada) {
        if (uuidsAsignaturaMatriculada == null || uuidsAsignaturaMatriculada.isEmpty())
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.SIN_ASIGNATURAS_ELEGIDAS);
        Map<String, AsignaturaMatriculada> matriculadas = new HashMap<>();
        for (AsignaturaMatriculada asignatura : estudianteGateway.getAsignaturasMatriculadas(uuidEstudiante))
            matriculadas.put(asignatura.getUuidAsignaturaMatriculada(), asignatura);

        Map<String, AsignaturaMatriculada> elegidas = new LinkedHashMap<>();
        for (String uuid : uuidsAsignaturaMatriculada) {
            AsignaturaMatriculada asignatura = uuid == null ? null : matriculadas.get(uuid.trim());
            if (asignatura == null)
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ASIGNATURA_NO_MATRICULADA, uuid, uuidEstudiante));
            if (elegidas.containsKey(asignatura.getUuidAsignaturaMatriculada()))
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ASIGNATURA_REPETIDA, nombreDe(asignatura)));
            if (!EstadoAsignaturaMatriculadaConstantes.ACTIVA.equals(asignatura.getEstado()))
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ASIGNATURA_NO_ACTIVA, nombreDe(asignatura), asignatura.getEstado()));
            elegidas.put(asignatura.getUuidAsignaturaMatriculada(), asignatura);
        }
        return new ArrayList<>(elegidas.values());
    }

    private TipoSolicitudAcademica tipoCancelacionAsignatura() {
        TipoSolicitudAcademica tipo = tipoSolicitudGateway.getTodos().stream()
                .filter(t -> TipoProcesoAcademico.porNombre(t.getNombre()) == TipoProcesoAcademico.CANCELACION_ASIGNATURA)
                .findFirst()
                .orElse(null);
        if (tipo == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO,
                    TIPO_SOLICITUD_ACADEMICA, NOMBRE, TipoProcesoAcademico.CANCELACION_ASIGNATURA.getNombre()));
        return tipo;
    }

    private void validarSoportes(List<AnexoRadicacion> soportes) {
        for (AnexoRadicacion soporte : soportes)
            if (soporte != null && tieneTexto(soporte.getUuidTipoAnexoAcademico()))
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.SOLO_SOPORTES_LIBRES, soporte.getUuidTipoAnexoAcademico()));
        for (AnexoRadicacion soporte : soportes)
            validadorArchivo.validar(soporte == null ? null : soporte.getArchivo(), SOPORTE_LIBRE, FORMATOS_SOPORTE_LIBRE);
    }

    private String nombreDe(AsignaturaMatriculada asignatura) {
        Asignatura datos = asignatura.getAsignatura();
        if (datos == null)
            return asignatura.getUuidAsignaturaMatriculada();
        return datos.getCodigoAsignatura() + " - " + datos.getNombreAsignatura();
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
