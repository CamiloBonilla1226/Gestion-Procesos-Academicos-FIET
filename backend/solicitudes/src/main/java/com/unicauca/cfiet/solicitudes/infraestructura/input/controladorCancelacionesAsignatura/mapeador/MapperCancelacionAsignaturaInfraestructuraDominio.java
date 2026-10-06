package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.mapeador;

import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTOPeticion.DecisionAsignaturaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTOPeticion.EvaluacionCancelacionAsignaturaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTORespuesta.*;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTORespuesta.SituacionAsignaturaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.mapeador.MapperSolicitudAcademicaInfraestructuraDominio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MapperCancelacionAsignaturaInfraestructuraDominio {
    private static final String SOPORTE = "soporte";

    private final MapperSolicitudAcademicaInfraestructuraDominio mapperSolicitud;

    public List<AnexoRadicacion> mapearAnexos(MultiValueMap<String, MultipartFile> partes) {
        List<AnexoRadicacion> anexos = new ArrayList<>();
        if (partes == null) return anexos;
        for (Map.Entry<String, List<MultipartFile>> parte : partes.entrySet()) {
            String uuidTipo = SOPORTE.equals(parte.getKey()) ? null : parte.getKey();
            for (MultipartFile archivo : parte.getValue())
                anexos.add(AnexoRadicacion.builder().uuidTipoAnexoAcademico(uuidTipo).archivo(mapperSolicitud.mapearArchivo(archivo)).build());
        }
        return anexos;
    }

    public List<EvaluacionAsignatura> mapearEvaluaciones(List<EvaluacionCancelacionAsignaturaDTOPeticion> evaluaciones) {
        if (evaluaciones == null) return null;
        return evaluaciones.stream()
                .map(evaluacion -> evaluacion == null ? null : EvaluacionAsignatura.builder()
                        .uuidAsignaturaSolicitud(evaluacion.getAsignaturaSolicitudUuid())
                        .numeroFaltas(evaluacion.getNumeroFaltas())
                        .nota(evaluacion.getNota())
                        .uuidSituacionMatricula(evaluacion.getSituacionMatriculaUuid())
                        .cumpleCondiciones(evaluacion.getCumpleCondiciones())
                        .observacionEvaluacion(evaluacion.getObservacionEvaluacion())
                        .build())
                .toList();
    }

    public List<DecisionAsignatura> mapearDecisiones(List<DecisionAsignaturaDTOPeticion> decisiones) {
        if (decisiones == null) return null;
        return decisiones.stream()
                .map(decision -> decision == null ? null : DecisionAsignatura.builder()
                        .uuidAsignaturaSolicitud(decision.getAsignaturaSolicitudUuid())
                        .aprobada(decision.getAprobada())
                        .uuidSituacionCancelar(decision.getSituacionCancelarUuid())
                        .observacionDecision(decision.getObservacionDecision())
                        .build())
                .toList();
    }

    public FormularioCancelacionAsignaturaDTORespuesta mapearFormularioARespuesta(FormularioCancelacionAsignatura formulario) {
        return FormularioCancelacionAsignaturaDTORespuesta.builder()
                .asignaturas(formulario.getAsignaturas().stream()
                        .map(matriculada -> AsignaturaElegibleDTORespuesta.builder()
                                .uuidAsignaturaMatriculada(matriculada.getUuidAsignaturaMatriculada())
                                .codigoAsignatura(matriculada.getAsignatura() == null ? null : matriculada.getAsignatura().getCodigoAsignatura())
                                .nombreAsignatura(matriculada.getAsignatura() == null ? null : matriculada.getAsignatura().getNombreAsignatura())
                                .grupo(matriculada.getGrupo())
                                .build())
                        .toList())
                .soportes(formulario.getSoportes().stream()
                        .map(soporte -> SoportePermitidoDTORespuesta.builder()
                                .uuidTipoAnexoAcademico(soporte.getUuidTipoAnexoAcademico())
                                .nombre(soporte.getNombre())
                                .formatosPermitidos(soporte.getFormatosPermitidos())
                                .tamanioMaximoBytes(soporte.getTamanioMaximoBytes())
                                .obligatorio(soporte.getObligatorio())
                                .build())
                        .toList())
                .build();
    }

    public RadicacionCancelacionAsignaturaDTORespuesta mapearRadicacionARespuesta(SolicitudCancelacionAsignatura cancelacion) {
        return RadicacionCancelacionAsignaturaDTORespuesta.builder()
                .uuidSolicitudAcademica(cancelacion.getSolicitudAcademica().getUuidSolicitudAcademica())
                .radicado(cancelacion.getSolicitudAcademica().getRadicado())
                .build();
    }

    public CancelacionAsignaturaDetalleDTORespuesta mapearDetalleARespuesta(DetalleCancelacionAsignatura detalle) {
        return CancelacionAsignaturaDetalleDTORespuesta.builder()
                .solicitud(mapperSolicitud.mapearDetalleARespuesta(detalle.getDetalle()))
                .motivoCancelacion(detalle.getMotivoCancelacion())
                .asignaturas(detalle.getAsignaturas().stream().map(this::mapearAsignaturaARespuesta).toList())
                .build();
    }

    private AsignaturaSolicitadaDTORespuesta mapearAsignaturaARespuesta(AsignaturaSolicitudAcademica asignatura) {
        AsignaturaSolicitadaDTORespuesta respuesta = AsignaturaSolicitadaDTORespuesta.builder()
                .uuidAsignaturaSolicitud(asignatura.getUuidAsignaturaSolicitud())
                .numeroFaltas(asignatura.getNumeroFaltas())
                .nota(asignatura.getNota())
                .situacionMatricula(mapearSituacionARespuesta(asignatura.getSituacionMatricula()))
                .cumpleCondiciones(asignatura.getCumpleCondiciones())
                .observacionEvaluacion(asignatura.getObservacionEvaluacion())
                .aprobadaPorDecano(asignatura.getAprobadaPorDecano())
                .observacionDecision(asignatura.getObservacionDecision())
                .situacionCancelar(mapearSituacionARespuesta(asignatura.getSituacionCancelar()))
                .build();
        AsignaturaMatriculada matriculada = asignatura.getAsignaturaMatriculada();
        if (matriculada != null && matriculada.getAsignatura() != null) {
            respuesta.setCodigoAsignatura(matriculada.getAsignatura().getCodigoAsignatura());
            respuesta.setNombreAsignatura(matriculada.getAsignatura().getNombreAsignatura());
        }
        return respuesta;
    }

    private SituacionAsignaturaDTORespuesta mapearSituacionARespuesta(SituacionAcademicaAsignatura situacion) {
        if (situacion == null) return null;
        return SituacionAsignaturaDTORespuesta.builder()
                .uuidSituacionAcademica(situacion.getUuidSituacionAcademica())
                .codigo(situacion.getCodigo())
                .nombre(situacion.getNombre())
                .build();
    }
}
