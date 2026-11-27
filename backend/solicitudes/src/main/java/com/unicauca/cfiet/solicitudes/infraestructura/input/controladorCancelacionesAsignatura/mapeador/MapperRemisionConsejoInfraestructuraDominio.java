package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.mapeador;

import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaMatriculada;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.DetalleRemisionConsejo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Estudiante;
import com.unicauca.cfiet.solicitudes.dominio.modelos.OrdenDelDia;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Respuesta;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudCancelacionAsignatura;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTORespuesta.RemisionConsejoDTORespuesta;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MapperRemisionConsejoInfraestructuraDominio {

    public RemisionConsejoDTORespuesta mapear(DetalleRemisionConsejo detalle) {
        SolicitudCancelacionAsignatura cancelacion = detalle.getCancelacion();
        SolicitudAcademica solicitud = cancelacion == null ? null : cancelacion.getSolicitudAcademica();
        Estudiante estudiante = solicitud == null ? null : solicitud.getEstudiante();
        return RemisionConsejoDTORespuesta.builder()
                .uuidSolicitud(detalle.getUuidSolicitud())
                .uuidSolicitudAcademica(solicitud == null ? null : solicitud.getUuidSolicitudAcademica())
                .radicado(solicitud == null ? null : solicitud.getRadicado())
                .etapaCodigo(solicitud == null || solicitud.getEtapa() == null ? null : solicitud.getEtapa().getCodigo())
                .nombresEstudiante(estudiante == null || estudiante.getUsuario() == null ? null : estudiante.getUsuario().getNombres())
                .apellidosEstudiante(estudiante == null || estudiante.getUsuario() == null ? null : estudiante.getUsuario().getApellidos())
                .codigoEstudiantil(estudiante == null ? null : estudiante.getCodigoEstudiantil())
                .motivo(cancelacion == null ? null : cancelacion.getMotivoCancelacion())
                .ordenDelDia(orden(detalle.getOrdenDelDia()))
                .asignaturas(cancelacion == null || cancelacion.getAsignaturas() == null ? List.of()
                        : cancelacion.getAsignaturas().stream().map(this::asignatura).toList())
                .respuesta(respuesta(detalle.getRespuesta()))
                .build();
    }

    private RemisionConsejoDTORespuesta.OrdenDelDia orden(OrdenDelDia orden) {
        if (orden == null)
            return null;
        return RemisionConsejoDTORespuesta.OrdenDelDia.builder()
                .uuidOrdenDelDia(orden.getUuidOrdenDelDia())
                .nombre(orden.getNombre())
                .fecha(orden.getFecha())
                .numeroActa(orden.getNumeroActa())
                .build();
    }

    private RemisionConsejoDTORespuesta.Asignatura asignatura(AsignaturaSolicitudAcademica fila) {
        AsignaturaMatriculada matriculada = fila.getAsignaturaMatriculada();
        boolean conAsignatura = matriculada != null && matriculada.getAsignatura() != null;
        return RemisionConsejoDTORespuesta.Asignatura.builder()
                .asignaturaSolicitudUuid(fila.getUuidAsignaturaSolicitud())
                .codigoAsignatura(conAsignatura ? matriculada.getAsignatura().getCodigoAsignatura() : null)
                .nombreAsignatura(conAsignatura ? matriculada.getAsignatura().getNombreAsignatura() : null)
                .numeroFaltas(fila.getNumeroFaltas())
                .nota(fila.getNota())
                .aprobada(fila.getAprobadaPorDecano())
                .build();
    }

    private RemisionConsejoDTORespuesta.Respuesta respuesta(Respuesta respuesta) {
        if (respuesta == null)
            return null;
        return RemisionConsejoDTORespuesta.Respuesta.builder()
                .uuidRespuesta(respuesta.getUuidRespuesta())
                .tipoRespuesta(respuesta.getTipoRespuesta())
                .consecutivoFiet(respuesta.getConsecutivoFiet())
                .respuestaConsejo(respuesta.getRespuestaConsejo())
                .indicaciones(respuesta.getIndicaciones())
                .build();
    }
}
