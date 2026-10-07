package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura;

import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTOPeticion.DecisionAsignaturaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTOPeticion.EvaluacionCancelacionAsignaturaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTORespuesta.AsignaturaSolicitadaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTORespuesta.CancelacionAsignaturaDetalleDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTORespuesta.FormularioCancelacionAsignaturaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.mapeador.MapperCancelacionAsignaturaInfraestructuraDominio;
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

class MapperCancelacionAsignaturaInfraestructuraDominioTest {

    private final MapperCancelacionAsignaturaInfraestructuraDominio mapper =
            new MapperCancelacionAsignaturaInfraestructuraDominio(new MapperSolicitudAcademicaInfraestructuraDominio());

    private MockMultipartFile archivo(String parte, String nombre) {
        return new MockMultipartFile(parte, nombre, "application/pdf", "%PDF-1.7".getBytes(StandardCharsets.US_ASCII));
    }

    @Test
    void lasPartesDeSoporteVanSinTipoYOtraParteConservaSuNombreComoTipo() {
        MultiValueMap<String, MultipartFile> partes = new LinkedMultiValueMap<>();
        partes.add("soporte", archivo("soporte", "incapacidad.pdf"));
        partes.add("tipo-1", archivo("tipo-1", "otro.pdf"));

        List<AnexoRadicacion> anexos = mapper.mapearAnexos(partes);

        assertEquals(Arrays.asList(null, "tipo-1"), anexos.stream().map(AnexoRadicacion::getUuidTipoAnexoAcademico).toList());
        assertEquals("incapacidad.pdf", anexos.get(0).getArchivo().getNombreOriginal());
        assertTrue(mapper.mapearAnexos(null).isEmpty());
    }

    @Test
    void lasEvaluacionesYDecisionesPasanTalCualAlDominio() {
        List<EvaluacionAsignatura> evaluaciones = mapper.mapearEvaluaciones(List.of(EvaluacionCancelacionAsignaturaDTOPeticion.builder()
                .asignaturaSolicitudUuid("as-1").numeroFaltas(3).nota(new BigDecimal("2.5")).situacionMatriculaUuid("sit-1")
                .cumpleCondiciones(false).observacionEvaluacion("Nota baja").build()));
        List<DecisionAsignatura> decisiones = mapper.mapearDecisiones(List.of(DecisionAsignaturaDTOPeticion.builder()
                .asignaturaSolicitudUuid("as-1").aprobada(false).situacionCancelarUuid("sit-2").observacionDecision("No procede").build()));

        EvaluacionAsignatura evaluacion = evaluaciones.get(0);
        assertEquals("as-1", evaluacion.getUuidAsignaturaSolicitud());
        assertEquals(3, evaluacion.getNumeroFaltas());
        assertEquals(new BigDecimal("2.5"), evaluacion.getNota());
        assertEquals("sit-1", evaluacion.getUuidSituacionMatricula());
        assertFalse(evaluacion.getCumpleCondiciones());
        assertEquals("Nota baja", evaluacion.getObservacionEvaluacion());
        DecisionAsignatura decision = decisiones.get(0);
        assertEquals("as-1", decision.getUuidAsignaturaSolicitud());
        assertFalse(decision.getAprobada());
        assertEquals("sit-2", decision.getUuidSituacionCancelar());
        assertEquals("No procede", decision.getObservacionDecision());
        assertNull(mapper.mapearEvaluaciones(null));
        assertNull(mapper.mapearDecisiones(null));
        assertNull(mapper.mapearDecisiones(Arrays.asList((DecisionAsignaturaDTOPeticion) null)).get(0));
    }

    @Test
    void elFormularioExponeElUuidDeCadaAsignaturaYElSoporteLibre() {
        FormularioCancelacionAsignaturaDTORespuesta respuesta = mapper.mapearFormularioARespuesta(FormularioCancelacionAsignatura.builder()
                .asignaturas(List.of(AsignaturaMatriculada.builder().uuidAsignaturaMatriculada("am-1").grupo("B")
                        .asignatura(Asignatura.builder().codigoAsignatura("SIS101").nombreAsignatura("Cálculo I").build()).estado("activa").build()))
                .soportes(List.of(SoportePermitido.builder().nombre("Soporte libre").formatosPermitidos("pdf,jpg,jpeg,png")
                        .tamanioMaximoBytes(5242880L).obligatorio(false).build()))
                .build());

        assertEquals("am-1", respuesta.getAsignaturas().get(0).getUuidAsignaturaMatriculada());
        assertEquals("SIS101", respuesta.getAsignaturas().get(0).getCodigoAsignatura());
        assertEquals("Cálculo I", respuesta.getAsignaturas().get(0).getNombreAsignatura());
        assertEquals("B", respuesta.getAsignaturas().get(0).getGrupo());
        assertNull(respuesta.getSoportes().get(0).getUuidTipoAnexoAcademico());
        assertEquals("Soporte libre", respuesta.getSoportes().get(0).getNombre());
        assertEquals("pdf,jpg,jpeg,png", respuesta.getSoportes().get(0).getFormatosPermitidos());
        assertEquals(5242880L, respuesta.getSoportes().get(0).getTamanioMaximoBytes());
        assertFalse(respuesta.getSoportes().get(0).getObligatorio());
    }

    @Test
    void elDetalleTraeCadaAsignaturaConEvaluacionYDecision() {
        SituacionAcademicaAsignatura r0 = SituacionAcademicaAsignatura.builder().uuidSituacionAcademica("sit-r0").codigo("R0").nombre("Primera vez").build();
        SituacionAcademicaAsignatura r1 = SituacionAcademicaAsignatura.builder().uuidSituacionAcademica("sit-r1").codigo("R1").nombre("Segunda vez").build();
        CancelacionAsignaturaDetalleDTORespuesta respuesta = mapper.mapearDetalleARespuesta(DetalleCancelacionAsignatura.builder()
                .detalle(DetalleSolicitudAcademica.builder()
                        .solicitudAcademica(SolicitudAcademica.builder().uuidSolicitudAcademica("sol-1").radicado("2026-CA-0001").build())
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
                        .cumpleCondiciones(true)
                        .observacionEvaluacion("Cumple")
                        .aprobadaPorDecano(true)
                        .observacionDecision("Procede")
                        .situacionCancelar(r1)
                        .build()))
                .build());

        assertEquals("2026-CA-0001", respuesta.getSolicitud().getRadicado());
        assertEquals("Motivo", respuesta.getMotivoCancelacion());
        AsignaturaSolicitadaDTORespuesta asignatura = respuesta.getAsignaturas().get(0);
        assertEquals("as-1", asignatura.getUuidAsignaturaSolicitud());
        assertEquals("SIS101", asignatura.getCodigoAsignatura());
        assertEquals("Cálculo I", asignatura.getNombreAsignatura());
        assertEquals(1, asignatura.getNumeroFaltas());
        assertEquals(new BigDecimal("3.5"), asignatura.getNota());
        assertEquals("R0", asignatura.getSituacionMatricula().getCodigo());
        assertTrue(asignatura.getCumpleCondiciones());
        assertEquals("Cumple", asignatura.getObservacionEvaluacion());
        assertTrue(asignatura.getAprobadaPorDecano());
        assertEquals("Procede", asignatura.getObservacionDecision());
        assertEquals("R1", asignatura.getSituacionCancelar().getCodigo());
    }

    @Test
    void unaAsignaturaSinDatosDeMatriculaSaleSinCodigoNiNombre() {
        CancelacionAsignaturaDetalleDTORespuesta respuesta = mapper.mapearDetalleARespuesta(DetalleCancelacionAsignatura.builder()
                .detalle(DetalleSolicitudAcademica.builder().solicitudAcademica(SolicitudAcademica.builder().uuidSolicitudAcademica("sol-1").build()).build())
                .asignaturas(List.of(AsignaturaSolicitudAcademica.builder().uuidAsignaturaSolicitud("as-1").build()))
                .build());

        assertNull(respuesta.getAsignaturas().get(0).getCodigoAsignatura());
        assertNull(respuesta.getAsignaturas().get(0).getSituacionMatricula());
        assertNull(respuesta.getAsignaturas().get(0).getSituacionCancelar());
    }

    @Test
    void elDetalleTraeElCodigoDeLaEtapaSinCambiarLaEtiqueta() {
        CancelacionAsignaturaDetalleDTORespuesta respuesta = mapper.mapearDetalleARespuesta(DetalleCancelacionAsignatura.builder()
                .detalle(DetalleSolicitudAcademica.builder()
                        .solicitudAcademica(SolicitudAcademica.builder().uuidSolicitudAcademica("sol-1")
                                .etapa(EtapaSolicitudAcademica.builder().uuidEtapa("e-1").codigo("APROBADA_POR_DECANO").build()).build())
                        .etiqueta("Aprobada por el Decano")
                        .build())
                .build());

        assertEquals("APROBADA_POR_DECANO", respuesta.getSolicitud().getEtapaCodigo());
        assertEquals("Aprobada por el Decano", respuesta.getSolicitud().getEtiqueta());
    }
}
