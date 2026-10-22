package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.mapeador;

import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.ComunicacionResolucion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTOPeticion.AjusteAsignaturaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTOPeticion.DecisionAsignaturaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTOPeticion.EvaluacionCancelacionAsignaturaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTOPeticion.ProgramacionResolucionDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTOPeticion.RespuestaCancelacionAsignaturaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTORespuesta.*;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTORespuesta.SituacionAsignaturaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.mapeador.MapperSolicitudAcademicaInfraestructuraDominio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MapperCancelacionAsignaturaInfraestructuraDominio {
    private static final String SOPORTE = "soporte";
    private static final String FECHA_PROGRAMADA = "la fecha y hora programadas";

    private final MapperSolicitudAcademicaInfraestructuraDominio mapperSolicitud;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;

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
                        .uuidSituacionCancelar(evaluacion.getSituacionCancelarUuid())
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
                        .build())
                .toList();
    }

    public List<AjusteAsignatura> mapearAjustes(List<AjusteAsignaturaDTOPeticion> ajustes) {
        if (ajustes == null) return null;
        return ajustes.stream()
                .map(ajuste -> ajuste == null ? null : AjusteAsignatura.builder()
                        .uuidAsignaturaSolicitud(ajuste.getAsignaturaSolicitudUuid())
                        .uuidSituacionCancelar(ajuste.getSituacionCancelarUuid())
                        .observacionDecision(ajuste.getObservacionDecision())
                        .build())
                .toList();
    }

    public ComunicacionRespuesta mapearComunicacion(RespuestaCancelacionAsignaturaDTOPeticion peticion) {
        if (peticion == null) return null;
        return ComunicacionRespuesta.builder()
                .enviarMensaje(peticion.getEnviarMensaje())
                .mensaje(peticion.getMensaje())
                .textoComunicacion(peticion.getTextoComunicacion())
                .textoDarca(peticion.getTextoDarca())
                .programacion(mapearProgramacion(peticion.getProgramacion()))
                .build();
    }

    public ProgramacionResolucion mapearProgramacion(ProgramacionResolucionDTOPeticion programacion) {
        if (programacion == null) return null;
        return ProgramacionResolucion.builder()
                .fechaProgramada(mapearFechaHora(programacion.getFechaProgramada()))
                .numeroResolucion(programacion.getNumeroResolucion())
                .anioResolucion(programacion.getAnioResolucion())
                .decision(programacion.getDecision())
                .build();
    }

    public ArchivoAdjunto mapearArchivo(MultipartFile archivo) {
        return mapperSolicitud.mapearArchivo(archivo);
    }

    public NotificacionResolucionDTORespuesta mapearNotificacionARespuesta(NotificacionResolucion notificacion) {
        return NotificacionResolucionDTORespuesta.builder()
                .estado(notificacion.getEstado() == null ? null : notificacion.getEstado().name())
                .fechaProgramada(notificacion.getFechaProgramada())
                .firmada(notificacion.isFirmada())
                .numeroResolucion(notificacion.getNumeroResolucion())
                .anioResolucion(notificacion.getAnioResolucion())
                .sufijoResolucion(notificacion.getSufijoResolucion())
                .decision(notificacion.getDecision())
                .mensajeInicial(notificacion.getMensajeInicial())
                .textoComunicacion(notificacion.getTextoComunicacion())
                .textoDarca(notificacion.getTextoDarca())
                .textoDarcaFirma(notificacion.getTextoDarcaFirma())
                .fechaEnvioEstudiante(notificacion.getFechaEnvioEstudiante())
                .fechaEnvioDarca(notificacion.getFechaEnvioDarca())
                .intentos(notificacion.getIntentos())
                .ultimoError(notificacion.getUltimoError())
                .fechaEnvio(notificacion.getFechaEnvio())
                .fechaLiberacion(notificacion.getFechaLiberacion())
                .editable(notificacion.isEditable())
                .build();
    }

    public PlantillaNotificacionResolucionDTORespuesta mapearPlantillaARespuesta(PlantillaNotificacionResolucion plantilla) {
        return PlantillaNotificacionResolucionDTORespuesta.builder()
                .mensajeInicial(plantilla.getMensajeInicial())
                .maximoMensaje(ComunicacionResolucion.MAXIMO_MENSAJE)
                .decisionPropuesta(plantilla.getDecisionPropuesta())
                .anioResolucion(plantilla.getAnioResolucion())
                .sufijoResolucion(plantilla.getSufijoResolucion())
                .textoComunicacion(plantilla.getTextoComunicacion())
                .textoDarca(plantilla.getTextoDarca())
                .maximoTexto(ComunicacionResolucion.MAXIMO_TEXTO)
                .nombreEstudiante(plantilla.getNombreEstudiante())
                .build();
    }

    private LocalDateTime mapearFechaHora(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return LocalDateTime.parse(valor.trim());
        } catch (DateTimeParseException error) {
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.FECHA_HORA_MAL_FORMADA, valor, FECHA_PROGRAMADA));
            return null;
        }
    }

    public FormularioCancelacionAsignaturaDTORespuesta mapearFormularioARespuesta(FormularioCancelacionAsignatura formulario) {
        return FormularioCancelacionAsignaturaDTORespuesta.builder()
                .codigoEstudiantil(formulario.getCodigoEstudiantil())
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
                .decisionCambiadaPorDecano(asignatura.decisionCambiadaPorDecano())
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
