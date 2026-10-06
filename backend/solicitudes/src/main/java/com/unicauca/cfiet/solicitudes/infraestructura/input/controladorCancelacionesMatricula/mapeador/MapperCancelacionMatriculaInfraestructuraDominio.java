package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.mapeador;

import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTOPeticion.EvaluacionAsignaturaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTOPeticion.SituacionCancelarDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTORespuesta.*;
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
public class MapperCancelacionMatriculaInfraestructuraDominio {
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

    public List<EvaluacionAsignatura> mapearEvaluaciones(List<EvaluacionAsignaturaDTOPeticion> evaluaciones) {
        if (evaluaciones == null) return null;
        return evaluaciones.stream()
                .map(evaluacion -> evaluacion == null ? null : EvaluacionAsignatura.builder()
                        .uuidAsignaturaSolicitud(evaluacion.getAsignaturaSolicitudUuid())
                        .numeroFaltas(evaluacion.getNumeroFaltas())
                        .nota(evaluacion.getNota())
                        .uuidSituacionMatricula(evaluacion.getSituacionMatriculaUuid())
                        .build())
                .toList();
    }

    public List<SituacionCancelarAsignatura> mapearSituaciones(List<SituacionCancelarDTOPeticion> situaciones) {
        if (situaciones == null) return null;
        return situaciones.stream()
                .map(situacion -> situacion == null ? null : SituacionCancelarAsignatura.builder()
                        .uuidAsignaturaSolicitud(situacion.getAsignaturaSolicitudUuid())
                        .uuidSituacionCancelar(situacion.getSituacionCancelarUuid())
                        .build())
                .toList();
    }

    public FormularioCancelacionMatriculaDTORespuesta mapearFormularioARespuesta(FormularioCancelacionMatricula formulario) {
        return FormularioCancelacionMatriculaDTORespuesta.builder()
                .anexosRequeridos(formulario.getAnexos().stream()
                        .map(tipo -> AnexoRequeridoDTORespuesta.builder()
                                .uuidTipoAnexoAcademico(tipo.getUuidTipoAnexoAcademico())
                                .nombre(tipo.getNombre())
                                .formatosPermitidos(tipo.getFormatosPermitidos())
                                .obligatorio(tipo.getObligatorio())
                                .build())
                        .toList())
                .asignaturas(formulario.getAsignaturas().stream()
                        .map(matriculada -> AsignaturaFormularioDTORespuesta.builder()
                                .codigoAsignatura(matriculada.getAsignatura() == null ? null : matriculada.getAsignatura().getCodigoAsignatura())
                                .nombreAsignatura(matriculada.getAsignatura() == null ? null : matriculada.getAsignatura().getNombreAsignatura())
                                .build())
                        .toList())
                .build();
    }

    public RadicacionCancelacionMatriculaDTORespuesta mapearRadicacionARespuesta(SolicitudCancelacionMatricula cancelacion) {
        return RadicacionCancelacionMatriculaDTORespuesta.builder()
                .uuidSolicitudAcademica(cancelacion.getSolicitudAcademica().getUuidSolicitudAcademica())
                .radicado(cancelacion.getSolicitudAcademica().getRadicado())
                .build();
    }

    public CancelacionMatriculaDetalleDTORespuesta mapearDetalleARespuesta(DetalleCancelacionMatricula detalle) {
        return CancelacionMatriculaDetalleDTORespuesta.builder()
                .solicitud(mapperSolicitud.mapearDetalleARespuesta(detalle.getDetalle()))
                .motivoCancelacion(detalle.getMotivoCancelacion())
                .asignaturas(detalle.getAsignaturas().stream().map(this::mapearAsignaturaARespuesta).toList())
                .build();
    }

    private AsignaturaCancelacionDTORespuesta mapearAsignaturaARespuesta(AsignaturaSolicitudAcademica asignatura) {
        AsignaturaCancelacionDTORespuesta respuesta = AsignaturaCancelacionDTORespuesta.builder()
                .uuidAsignaturaSolicitud(asignatura.getUuidAsignaturaSolicitud())
                .numeroFaltas(asignatura.getNumeroFaltas())
                .nota(asignatura.getNota())
                .situacionMatricula(mapearSituacionARespuesta(asignatura.getSituacionMatricula()))
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
