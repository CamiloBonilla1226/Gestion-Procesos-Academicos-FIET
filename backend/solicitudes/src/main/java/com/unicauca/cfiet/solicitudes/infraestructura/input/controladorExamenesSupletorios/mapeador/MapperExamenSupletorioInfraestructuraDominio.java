package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.mapeador;

import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.DTORespuesta.*;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.mapeador.MapperSolicitudAcademicaInfraestructuraDominio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MapperExamenSupletorioInfraestructuraDominio {
    private static final String SOPORTE = "soporte";

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

    public LocalDate mapearFecha(String valor, String dato) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException error) {
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.FECHA_MAL_FORMADA, valor, dato));
            return null;
        }
    }

    public FormularioExamenSupletorioDTORespuesta mapearFormularioARespuesta(FormularioExamenSupletorio formulario) {
        return FormularioExamenSupletorioDTORespuesta.builder()
                .asignaturas(formulario.getAsignaturas().stream().map(this::mapearAsignaturaARespuesta).toList())
                .causas(formulario.getCausas().stream().map(this::nombreCausa).toList())
                .anexos(formulario.getAnexos().stream()
                        .map(anexo -> AnexoSupletorioDTORespuesta.builder()
                                .uuidTipoAnexoAcademico(anexo.getTipoAnexo().getUuidTipoAnexoAcademico())
                                .nombre(anexo.getTipoAnexo().getNombre())
                                .formatosPermitidos(anexo.getTipoAnexo().getFormatosPermitidos())
                                .tamanioMaximoBytes(anexo.getTamanioMaximoBytes())
                                .causas(anexo.getCausas().stream().map(this::nombreCausa).toList())
                                .build())
                        .toList())
                .plazoDiasHabiles(formulario.getPlazoDiasHabiles())
                .build();
    }

    public RadicacionExamenSupletorioDTORespuesta mapearRadicacionARespuesta(SolicitudExamenSupletorio supletorio) {
        return RadicacionExamenSupletorioDTORespuesta.builder()
                .uuidSolicitudAcademica(supletorio.getSolicitudAcademica().getUuidSolicitudAcademica())
                .radicado(supletorio.getSolicitudAcademica().getRadicado())
                .build();
    }

    public ExamenSupletorioDetalleDTORespuesta mapearDetalleARespuesta(DetalleExamenSupletorio detalle) {
        SolicitudExamenSupletorio supletorio = detalle.getSupletorio();
        CruceSupletorio cruce = supletorio.getCruce();
        return ExamenSupletorioDetalleDTORespuesta.builder()
                .solicitud(mapperSolicitud.mapearDetalleARespuesta(detalle.getDetalle()))
                .asignatura(mapearAsignaturaARespuesta(supletorio.getAsignaturaMatriculada()))
                .tipoCausa(supletorio.getTipoCausa() == null ? null : nombreCausa(supletorio.getTipoCausa()))
                .fechaExamenNoPresentado(texto(supletorio.getFechaExamenNoPresentado()))
                .cruce(cruce == null ? null : CruceSupletorioDTORespuesta.builder()
                        .asignatura(mapearAsignaturaARespuesta(cruce.getAsignaturaMatriculadaCruzada()))
                        .fechaExamenCruzada(texto(cruce.getFechaExamenCruzada()))
                        .horaExamenCruzada(cruce.getHoraExamenCruzada())
                        .build())
                .fechaAcordadaExamen(texto(supletorio.getFechaAcordadaExamen()))
                .build();
    }

    private AsignaturaSupletorioDTORespuesta mapearAsignaturaARespuesta(AsignaturaMatriculada matriculada) {
        if (matriculada == null) return null;
        Asignatura asignatura = matriculada.getAsignatura();
        return AsignaturaSupletorioDTORespuesta.builder()
                .uuidAsignaturaMatriculada(matriculada.getUuidAsignaturaMatriculada())
                .codigoAsignatura(asignatura == null ? null : asignatura.getCodigoAsignatura())
                .nombreAsignatura(asignatura == null ? null : asignatura.getNombreAsignatura())
                .grupo(matriculada.getGrupo())
                .build();
    }

    private String nombreCausa(CausaSupletorio causa) {
        return causa.name().toLowerCase(Locale.ROOT);
    }

    private String texto(LocalDate fecha) {
        return fecha == null ? null : fecha.toString();
    }

    private String texto(LocalDateTime fecha) {
        return fecha == null ? null : fecha.toLocalDate().toString();
    }
}
