package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.mapeador;

import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MapperSolicitudAcademicaInfraestructuraDominio {

    public ArchivoAdjunto mapearArchivo(MultipartFile archivo) {
        try {
            return ArchivoAdjunto.builder()
                    .nombreOriginal(archivo.getOriginalFilename())
                    .tipoContenido(archivo.getContentType())
                    .contenido(archivo.getBytes())
                    .build();
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo leer el archivo recibido: " + ex.getMessage(), ex);
        }
    }

    public List<SolicitudAcademicaResumenDTORespuesta> mapearBandejaARespuesta(List<ResumenSolicitudAcademica> resumenes) {
        if (resumenes == null) return new ArrayList<>();
        return resumenes.stream().map(this::mapearResumenARespuesta).toList();
    }

    public SolicitudAcademicaResumenDTORespuesta mapearResumenARespuesta(ResumenSolicitudAcademica resumen) {
        SolicitudAcademica solicitud = resumen.getSolicitudAcademica();
        SolicitudAcademicaResumenDTORespuesta respuesta = SolicitudAcademicaResumenDTORespuesta.builder()
                .uuidSolicitudAcademica(solicitud.getUuidSolicitudAcademica())
                .radicado(solicitud.getRadicado())
                .fechaCreacion(solicitud.getFechaCreacion())
                .etiqueta(resumen.getEtiqueta())
                .build();
        TipoSolicitudAcademica tipo = solicitud.getTipoSolicitudAcademica();
        if (tipo != null) {
            respuesta.setUuidTipoSolicitudAcademica(tipo.getUuidTipoSolicitudAcademica());
            respuesta.setTipoSolicitud(tipo.getNombre());
        }
        Estudiante estudiante = solicitud.getEstudiante();
        if (estudiante != null) {
            respuesta.setCodigoEstudiantil(estudiante.getCodigoEstudiantil());
            Usuario usuario = estudiante.getUsuario();
            if (usuario != null)
                respuesta.setNombreEstudiante(usuario.getNombres() + " " + usuario.getApellidos());
        }
        return respuesta;
    }

    public SolicitudAcademicaDetalleDTORespuesta mapearDetalleARespuesta(DetalleSolicitudAcademica detalle) {
        SolicitudAcademica solicitud = detalle.getSolicitudAcademica();
        SolicitudAcademicaDetalleDTORespuesta respuesta = SolicitudAcademicaDetalleDTORespuesta.builder()
                .uuidSolicitudAcademica(solicitud.getUuidSolicitudAcademica())
                .radicado(solicitud.getRadicado())
                .fechaCreacion(solicitud.getFechaCreacion())
                .etiqueta(detalle.getEtiqueta())
                .estudiante(mapearEstudianteARespuesta(solicitud.getEstudiante()))
                .anexos(detalle.getAnexos() == null ? new ArrayList<>() : detalle.getAnexos().stream().map(this::mapearAnexoARespuesta).toList())
                .tieneResolucion(detalle.isTieneResolucion())
                .puedeDescargarResolucion(detalle.isPuedeDescargarResolucion())
                .accionesDisponibles(detalle.getAccionesDisponibles() == null ? new ArrayList<>()
                        : detalle.getAccionesDisponibles().stream().map(AccionEtapa::name).toList())
                .build();
        TipoSolicitudAcademica tipo = solicitud.getTipoSolicitudAcademica();
        if (tipo != null) {
            respuesta.setUuidTipoSolicitudAcademica(tipo.getUuidTipoSolicitudAcademica());
            respuesta.setTipoSolicitud(tipo.getNombre());
        }
        return respuesta;
    }

    public EstudianteSolicitudDTORespuesta mapearEstudianteARespuesta(Estudiante estudiante) {
        if (estudiante == null) return null;
        EstudianteSolicitudDTORespuesta respuesta = EstudianteSolicitudDTORespuesta.builder()
                .uuidUsuario(estudiante.getUuidUsuario())
                .codigoEstudiantil(estudiante.getCodigoEstudiantil())
                .programaAcademico(estudiante.getProgramaAcademico())
                .semestre(estudiante.getSemestre())
                .build();
        Usuario usuario = estudiante.getUsuario();
        if (usuario != null) {
            respuesta.setNombres(usuario.getNombres());
            respuesta.setApellidos(usuario.getApellidos());
            respuesta.setCorreoElectronico(usuario.getCorreoElectronico());
        }
        return respuesta;
    }

    public AnexoAcademicoDTORespuesta mapearAnexoARespuesta(AnexoAcademico anexo) {
        AnexoAcademicoDTORespuesta respuesta = AnexoAcademicoDTORespuesta.builder()
                .uuidAnexoAcademico(anexo.getUuidAnexoAcademico())
                .nombreArchivo(anexo.getNombreArchivo())
                .tipoArchivo(anexo.getTipoArchivo())
                .tamanioBytes(anexo.getTamanioBytes())
                .fechaSubida(anexo.getFechaSubida())
                .build();
        TipoAnexoAcademico tipoAnexo = anexo.getTipoAnexoAcademico();
        if (tipoAnexo != null) {
            respuesta.setUuidTipoAnexoAcademico(tipoAnexo.getUuidTipoAnexoAcademico());
            respuesta.setTipoAnexo(tipoAnexo.getNombre());
        }
        return respuesta;
    }

    public List<HistorialSolicitudAcademicaDTORespuesta> mapearHistorialARespuesta(List<HistorialSolicitudAcademica> historial) {
        if (historial == null) return new ArrayList<>();
        return historial.stream().map(fila -> {
            HistorialSolicitudAcademicaDTORespuesta respuesta = HistorialSolicitudAcademicaDTORespuesta.builder()
                    .accion(fila.getAccion())
                    .observaciones(fila.getObservaciones())
                    .fecha(fila.getFecha())
                    .build();
            if (fila.getUsuario() != null) {
                respuesta.setNombresUsuario(fila.getUsuario().getNombres());
                respuesta.setApellidosUsuario(fila.getUsuario().getApellidos());
            }
            return respuesta;
        }).toList();
    }

    public ResolucionAcademicaDTORespuesta mapearResolucionARespuesta(ResolucionAcademica resolucion) {
        return ResolucionAcademicaDTORespuesta.builder()
                .uuidSolicitudAcademica(resolucion.getSolicitudAcademica() == null ? null
                        : resolucion.getSolicitudAcademica().getUuidSolicitudAcademica())
                .nombreArchivo(resolucion.getNombreArchivo())
                .fechaSubida(resolucion.getFechaSubida())
                .build();
    }
}
