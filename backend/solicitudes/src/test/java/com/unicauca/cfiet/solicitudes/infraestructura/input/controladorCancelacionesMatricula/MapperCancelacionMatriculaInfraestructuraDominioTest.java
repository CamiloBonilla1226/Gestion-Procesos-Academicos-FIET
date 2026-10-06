package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula;

import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTOPeticion.EvaluacionAsignaturaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTOPeticion.SituacionCancelarDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTORespuesta.AsignaturaCancelacionDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTORespuesta.CancelacionMatriculaDetalleDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTORespuesta.FormularioCancelacionMatriculaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.mapeador.MapperCancelacionMatriculaInfraestructuraDominio;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.mapeador.MapperSolicitudAcademicaInfraestructuraDominio;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapperCancelacionMatriculaInfraestructuraDominioTest {

    private final MapperCancelacionMatriculaInfraestructuraDominio mapper =
            new MapperCancelacionMatriculaInfraestructuraDominio(new MapperSolicitudAcademicaInfraestructuraDominio());

    private MockMultipartFile archivo(String parte, String nombre) {
        return new MockMultipartFile(parte, nombre, "application/pdf", "%PDF-1.7".getBytes(StandardCharsets.US_ASCII));
    }

    @Test
    void cadaParteConUuidEsUnAnexoDeEseTipoYLasDeSoporteVanSinTipo() {
        MultiValueMap<String, MultipartFile> partes = new LinkedMultiValueMap<>();
        partes.add("tipo-1", archivo("tipo-1", "biblioteca.pdf"));
        partes.add("tipo-2", archivo("tipo-2", "deportes.pdf"));
        partes.add("soporte", archivo("soporte", "soporte1.pdf"));
        partes.add("soporte", archivo("soporte", "soporte2.pdf"));

        List<AnexoRadicacion> anexos = mapper.mapearAnexos(partes);

        assertEquals(Arrays.asList("tipo-1", "tipo-2", null, null), anexos.stream().map(AnexoRadicacion::getUuidTipoAnexoAcademico).toList());
        assertEquals(List.of("biblioteca.pdf", "deportes.pdf", "soporte1.pdf", "soporte2.pdf"),
                anexos.stream().map(a -> a.getArchivo().getNombreOriginal()).toList());
        assertEquals("application/pdf", anexos.get(0).getArchivo().getTipoContenido());
        assertEquals(8, anexos.get(0).getArchivo().getContenido().length);
    }

    @Test
    void sinPartesNoHayAnexos() {
        assertTrue(mapper.mapearAnexos(null).isEmpty());
        assertTrue(mapper.mapearAnexos(new LinkedMultiValueMap<>()).isEmpty());
    }

    @Test
    void lasEvaluacionesYSituacionesPasanTalCualAlDominio() {
        List<EvaluacionAsignatura> evaluaciones = mapper.mapearEvaluaciones(List.of(
                EvaluacionAsignaturaDTOPeticion.builder().asignaturaSolicitudUuid("as-1").numeroFaltas(3).nota(new BigDecimal("4.5")).situacionMatriculaUuid("sit-1").build()));
        List<SituacionCancelarAsignatura> situaciones = mapper.mapearSituaciones(List.of(
                SituacionCancelarDTOPeticion.builder().asignaturaSolicitudUuid("as-1").situacionCancelarUuid("sit-2").build()));

        assertEquals("as-1", evaluaciones.get(0).getUuidAsignaturaSolicitud());
        assertEquals(3, evaluaciones.get(0).getNumeroFaltas());
        assertEquals(new BigDecimal("4.5"), evaluaciones.get(0).getNota());
        assertEquals("sit-1", evaluaciones.get(0).getUuidSituacionMatricula());
        assertEquals("sit-2", situaciones.get(0).getUuidSituacionCancelar());
        assertNull(mapper.mapearEvaluaciones(null));
        assertNull(mapper.mapearSituaciones(null));
    }

    @Test
    void elFormularioExponeAnexosYAsignaturasSinUuidsInternosDeMatricula() {
        FormularioCancelacionMatriculaDTORespuesta respuesta = mapper.mapearFormularioARespuesta(FormularioCancelacionMatricula.builder()
                .anexos(List.of(TipoAnexoAcademico.builder().uuidTipoAnexoAcademico("an-1").nombre("Paz y salvo").formatosPermitidos("pdf").obligatorio(true).build()))
                .asignaturas(List.of(AsignaturaMatriculada.builder().uuidAsignaturaMatriculada("am-1")
                        .asignatura(Asignatura.builder().codigoAsignatura("SIS101").nombreAsignatura("Cálculo I").build()).estado("activa").build()))
                .build());

        assertEquals("an-1", respuesta.getAnexosRequeridos().get(0).getUuidTipoAnexoAcademico());
        assertEquals("pdf", respuesta.getAnexosRequeridos().get(0).getFormatosPermitidos());
        assertTrue(respuesta.getAnexosRequeridos().get(0).getObligatorio());
        assertEquals("SIS101", respuesta.getAsignaturas().get(0).getCodigoAsignatura());
        assertEquals("Cálculo I", respuesta.getAsignaturas().get(0).getNombreAsignatura());
    }

    @Test
    void elDetalleTraeLaSolicitudElMotivoYCadaAsignaturaConSusSituaciones() {
        SituacionAcademicaAsignatura r0 = SituacionAcademicaAsignatura.builder().uuidSituacionAcademica("sit-r0").codigo("R0").nombre("Primera vez").build();
        CancelacionMatriculaDetalleDTORespuesta respuesta = mapper.mapearDetalleARespuesta(DetalleCancelacionMatricula.builder()
                .detalle(DetalleSolicitudAcademica.builder()
                        .solicitudAcademica(SolicitudAcademica.builder().uuidSolicitudAcademica("sol-1").radicado("2026-CM-0001").build())
                        .etiqueta("En trámite")
                        .build())
                .motivoCancelacion("Motivo")
                .asignaturas(List.of(AsignaturaSolicitudAcademica.builder()
                        .uuidAsignaturaSolicitud("as-1")
                        .asignaturaMatriculada(AsignaturaMatriculada.builder()
                                .asignatura(Asignatura.builder().codigoAsignatura("SIS101").nombreAsignatura("Cálculo I").build()).build())
                        .numeroFaltas(1)
                        .nota(new BigDecimal("3.5"))
                        .situacionMatricula(r0)
                        .build()))
                .build());

        assertEquals("2026-CM-0001", respuesta.getSolicitud().getRadicado());
        assertEquals("En trámite", respuesta.getSolicitud().getEtiqueta());
        assertEquals("Motivo", respuesta.getMotivoCancelacion());
        AsignaturaCancelacionDTORespuesta asignatura = respuesta.getAsignaturas().get(0);
        assertEquals("as-1", asignatura.getUuidAsignaturaSolicitud());
        assertEquals("SIS101", asignatura.getCodigoAsignatura());
        assertEquals("Cálculo I", asignatura.getNombreAsignatura());
        assertEquals(1, asignatura.getNumeroFaltas());
        assertEquals(new BigDecimal("3.5"), asignatura.getNota());
        assertEquals("R0", asignatura.getSituacionMatricula().getCodigo());
        assertNull(asignatura.getSituacionCancelar());
    }
}
