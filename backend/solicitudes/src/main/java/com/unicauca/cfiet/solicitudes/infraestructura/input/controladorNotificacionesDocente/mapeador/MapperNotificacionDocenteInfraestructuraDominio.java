package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorNotificacionesDocente.mapeador;

import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.mapeador.MapperExamenSupletorioInfraestructuraDominio;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorNotificacionesDocente.DTORespuesta.AnexoNotificacionDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorNotificacionesDocente.DTORespuesta.NotificacionDocenteDetalleDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorNotificacionesDocente.DTORespuesta.NotificacionDocenteResumenDTORespuesta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.APROBADA;
import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.RECHAZADA;

@Component
@RequiredArgsConstructor
public class MapperNotificacionDocenteInfraestructuraDominio {
    private static final String RESULTADO_APROBADA = "Aprobada";
    private static final String RESULTADO_RECHAZADA = "Rechazada";

    private final MapperExamenSupletorioInfraestructuraDominio mapperSupletorio;

    public List<NotificacionDocenteResumenDTORespuesta> mapearListaARespuesta(List<DetalleNotificacionDocente> notificaciones) {
        return notificaciones.stream().map(this::mapearResumenARespuesta).toList();
    }

    public NotificacionDocenteResumenDTORespuesta mapearResumenARespuesta(DetalleNotificacionDocente detalle) {
        NotificacionDocente notificacion = detalle.getNotificacion();
        SolicitudAcademica solicitud = detalle.getSolicitudAcademica();
        SolicitudExamenSupletorio supletorio = detalle.getSupletorio();
        return NotificacionDocenteResumenDTORespuesta.builder()
                .uuidNotificacion(notificacion.getUuidNotificacion())
                .titulo(notificacion.getTitulo())
                .uuidSolicitudAcademica(notificacion.getUuidSolicitudAcademica())
                .radicado(solicitud == null ? null : solicitud.getRadicado())
                .estudiante(nombreEstudiante(solicitud))
                .asignatura(nombreAsignatura(supletorio))
                .resultado(resultado(solicitud))
                .fechaCreacion(notificacion.getFechaCreacion())
                .fechaLectura(notificacion.getFechaLectura())
                .leida(notificacion.getFechaLectura() != null)
                .build();
    }

    public NotificacionDocenteDetalleDTORespuesta mapearDetalleARespuesta(DetalleNotificacionDocente detalle) {
        SolicitudAcademica solicitud = detalle.getSolicitudAcademica();
        SolicitudExamenSupletorio supletorio = detalle.getSupletorio();
        Estudiante estudiante = solicitud == null ? null : solicitud.getEstudiante();
        Usuario usuario = estudiante == null ? null : estudiante.getUsuario();
        return NotificacionDocenteDetalleDTORespuesta.builder()
                .notificacion(mapearResumenARespuesta(detalle))
                .tipoSolicitud(solicitud == null || solicitud.getTipoSolicitudAcademica() == null ? null : solicitud.getTipoSolicitudAcademica().getNombre())
                .fechaRadicacion(solicitud == null ? null : solicitud.getFechaCreacion())
                .nombresEstudiante(usuario == null ? null : usuario.getNombres())
                .apellidosEstudiante(usuario == null ? null : usuario.getApellidos())
                .codigoEstudiantil(estudiante == null ? null : estudiante.getCodigoEstudiantil())
                .programaAcademico(estudiante == null || estudiante.getProgramaAcademico() == null ? null : estudiante.getProgramaAcademico().getNombre())
                .asignatura(supletorio == null ? null : mapperSupletorio.mapearAsignaturaARespuesta(supletorio.getAsignaturaMatriculada()))
                .tipoCausa(supletorio == null ? null : mapperSupletorio.nombreCausaDe(supletorio))
                .fechaExamenNoPresentado(supletorio == null ? null : mapperSupletorio.fechaExamenDe(supletorio))
                .cruce(supletorio == null ? null : mapperSupletorio.mapearCruceARespuesta(supletorio.getCruce()))
                .anexos(detalle.getAnexos().stream().map(this::mapearAnexo).toList())
                .build();
    }

    private AnexoNotificacionDTORespuesta mapearAnexo(AnexoAcademico anexo) {
        return AnexoNotificacionDTORespuesta.builder()
                .uuidAnexoAcademico(anexo.getUuidAnexoAcademico())
                .tipoAnexo(anexo.getTipoAnexoAcademico() == null ? null : anexo.getTipoAnexoAcademico().getNombre())
                .nombreArchivo(anexo.getNombreArchivo())
                .tipoArchivo(anexo.getTipoArchivo())
                .fechaSubida(anexo.getFechaSubida())
                .build();
    }

    private String nombreEstudiante(SolicitudAcademica solicitud) {
        if (solicitud == null || solicitud.getEstudiante() == null || solicitud.getEstudiante().getUsuario() == null) return null;
        Usuario usuario = solicitud.getEstudiante().getUsuario();
        return (usuario.getNombres() + " " + usuario.getApellidos()).trim();
    }

    private String nombreAsignatura(SolicitudExamenSupletorio supletorio) {
        if (supletorio == null || supletorio.getAsignaturaMatriculada() == null || supletorio.getAsignaturaMatriculada().getAsignatura() == null)
            return null;
        Asignatura asignatura = supletorio.getAsignaturaMatriculada().getAsignatura();
        return asignatura.getCodigoAsignatura() + " - " + asignatura.getNombreAsignatura();
    }

    private String resultado(SolicitudAcademica solicitud) {
        String codigo = solicitud == null || solicitud.getEtapa() == null ? null : solicitud.getEtapa().getCodigo();
        if (APROBADA.equals(codigo)) return RESULTADO_APROBADA;
        if (RECHAZADA.equals(codigo)) return RESULTADO_RECHAZADA;
        return codigo;
    }
}
