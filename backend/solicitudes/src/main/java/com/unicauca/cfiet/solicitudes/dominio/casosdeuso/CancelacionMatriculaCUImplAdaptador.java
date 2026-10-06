package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AnexoAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.CancelacionMatriculaCUIntPuerto;
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

public class CancelacionMatriculaCUImplAdaptador implements CancelacionMatriculaCUIntPuerto {
    private static final String USUARIO = "Usuario";
    private static final String TIPO_SOLICITUD_ACADEMICA = "Tipo de solicitud académica";
    private static final String TIPO_ANEXO_ACADEMICO = "Tipo de anexo académico";
    private static final String NOMBRE = "nombre";
    private static final String SOPORTE_LIBRE = "Soporte libre";
    private static final int MAXIMO_MOTIVO = 255;

    private final SolicitudCancelacionMatriculaGatewayIntPuerto gateway;
    private final EstudianteGatewayIntPuerto estudianteGateway;
    private final UsuarioGatewayIntPuerto usuarioGateway;
    private final TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway;
    private final TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway;
    private final SolicitudAcademicaCUIntPuerto solicitudCU;
    private final AnexoAcademicoCUIntPuerto anexoCU;
    private final ValidadorArchivoAdjunto validadorArchivo;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;

    public CancelacionMatriculaCUImplAdaptador(SolicitudCancelacionMatriculaGatewayIntPuerto gateway,
                                               EstudianteGatewayIntPuerto estudianteGateway,
                                               UsuarioGatewayIntPuerto usuarioGateway,
                                               TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                               TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway,
                                               SolicitudAcademicaCUIntPuerto solicitudCU,
                                               AnexoAcademicoCUIntPuerto anexoCU,
                                               ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                               LogCUIntPuerto log) {
        this.gateway = gateway;
        this.estudianteGateway = estudianteGateway;
        this.usuarioGateway = usuarioGateway;
        this.tipoSolicitudGateway = tipoSolicitudGateway;
        this.tipoAnexoGateway = tipoAnexoGateway;
        this.solicitudCU = solicitudCU;
        this.anexoCU = anexoCU;
        this.validadorArchivo = new ValidadorArchivoAdjunto(formateadorExcepciones);
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
    }

    @Override
    public SolicitudCancelacionMatricula radicarCancelacionMatricula(String uuidEstudiante, String motivoCancelacion,
                                                                     List<AnexoRadicacion> anexos, String token) {
        validarEstudiante(uuidEstudiante);
        List<AsignaturaMatriculada> activas = asignaturasActivas(uuidEstudiante);
        String motivo = motivoDe(motivoCancelacion);
        TipoSolicitudAcademica tipo = tipoCancelacionMatricula();
        List<AnexoRadicacion> entregados = anexos == null ? List.of() : anexos;
        validarAnexos(entregados, tipo);

        SolicitudAcademica solicitud = solicitudCU.crearSolicitud(uuidEstudiante, tipo.getUuidTipoSolicitudAcademica(), token);
        SolicitudCancelacionMatricula guardada = gateway.guardar(SolicitudCancelacionMatricula.builder()
                .solicitudAcademica(solicitud)
                .motivoCancelacion(motivo)
                .asignaturas(activas.stream()
                        .map(asignatura -> AsignaturaSolicitudAcademica.builder()
                                .uuidAsignaturaSolicitud(UUID.randomUUID().toString())
                                .asignaturaMatriculada(asignatura)
                                .build())
                        .toList())
                .build());

        ActorSolicitud actor = ActorSolicitud.builder().uuidUsuario(uuidEstudiante).rol(RolEtiquetaEtapa.ESTUDIANTE).build();
        for (AnexoRadicacion anexo : entregados)
            anexoCU.adjuntarAnexo(solicitud.getUuidSolicitudAcademica(), anexo.getUuidTipoAnexoAcademico(), anexo.getArchivo(), actor, token);

        log.crearLog("Radicar cancelación de matrícula",
                String.format("Solicitud académica %s, acción RADICAR: cancelación de matrícula con %d asignaturas activas y %d anexos",
                        solicitud.getRadicado(), activas.size(), entregados.size()),
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

    private List<AsignaturaMatriculada> asignaturasActivas(String uuidEstudiante) {
        List<AsignaturaMatriculada> activas = estudianteGateway.getAsignaturasMatriculadas(uuidEstudiante).stream()
                .filter(asignatura -> EstadoAsignaturaMatriculadaConstantes.ACTIVA.equals(asignatura.getEstado()))
                .toList();
        if (activas.isEmpty())
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.SIN_ASIGNATURAS_ACTIVAS, uuidEstudiante));
        return activas;
    }

    private String motivoDe(String motivoCancelacion) {
        if (!tieneTexto(motivoCancelacion))
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.MOTIVO_CANCELACION_REQUERIDO);
        String motivo = motivoCancelacion.trim();
        if (motivo.length() > MAXIMO_MOTIVO)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.MOTIVO_CANCELACION_MUY_LARGO, MAXIMO_MOTIVO));
        return motivo;
    }

    private TipoSolicitudAcademica tipoCancelacionMatricula() {
        TipoSolicitudAcademica tipo = tipoSolicitudGateway.getTodos().stream()
                .filter(t -> TipoProcesoAcademico.porNombre(t.getNombre()) == TipoProcesoAcademico.CANCELACION_MATRICULA)
                .findFirst()
                .orElse(null);
        if (tipo == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO,
                    TIPO_SOLICITUD_ACADEMICA, NOMBRE, TipoProcesoAcademico.CANCELACION_MATRICULA.getNombre()));
        return tipo;
    }

    private void validarAnexos(List<AnexoRadicacion> entregados, TipoSolicitudAcademica tipo) {
        Map<String, TipoAnexoAcademico> catalogo = new LinkedHashMap<>();
        for (TipoAnexoAcademico tipoAnexo : tipoAnexoGateway.getPorTipo(tipo.getUuidTipoSolicitudAcademica()))
            catalogo.put(tipoAnexo.getUuidTipoAnexoAcademico(), tipoAnexo);

        Set<String> presentes = new HashSet<>();
        for (AnexoRadicacion anexo : entregados) {
            String uuidTipo = anexo == null ? null : anexo.getUuidTipoAnexoAcademico();
            if (tieneTexto(uuidTipo) && !catalogo.containsKey(uuidTipo))
                anexoDeOtroTipo(uuidTipo, tipo);
            if (tieneTexto(uuidTipo))
                presentes.add(uuidTipo);
        }
        List<String> faltantes = catalogo.values().stream()
                .filter(tipoAnexo -> Boolean.TRUE.equals(tipoAnexo.getObligatorio()))
                .filter(tipoAnexo -> !presentes.contains(tipoAnexo.getUuidTipoAnexoAcademico()))
                .map(TipoAnexoAcademico::getNombre)
                .toList();
        if (!faltantes.isEmpty())
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ANEXOS_OBLIGATORIOS_FALTANTES, String.join("; ", faltantes)));

        for (AnexoRadicacion anexo : entregados) {
            TipoAnexoAcademico tipoAnexo = anexo == null ? null : catalogo.get(anexo.getUuidTipoAnexoAcademico());
            validadorArchivo.validar(anexo == null ? null : anexo.getArchivo(),
                    tipoAnexo == null ? SOPORTE_LIBRE : tipoAnexo.getNombre(),
                    tipoAnexo == null ? FORMATOS_SOPORTE_LIBRE : tipoAnexo.getFormatosPermitidos());
        }
    }

    private void anexoDeOtroTipo(String uuidTipoAnexo, TipoSolicitudAcademica tipo) {
        TipoAnexoAcademico otro = tipoAnexoGateway.getPorUuid(uuidTipoAnexo);
        if (otro == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, TIPO_ANEXO_ACADEMICO, uuidTipoAnexo));
        formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ANEXO_DE_OTRO_TIPO, otro.getNombre(), tipo.getNombre()));
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
