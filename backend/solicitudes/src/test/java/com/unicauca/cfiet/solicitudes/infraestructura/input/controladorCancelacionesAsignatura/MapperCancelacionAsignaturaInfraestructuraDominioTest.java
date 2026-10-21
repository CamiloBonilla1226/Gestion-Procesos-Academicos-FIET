package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura;

import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTOPeticion.AjusteAsignaturaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTOPeticion.DecisionAsignaturaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTOPeticion.EvaluacionCancelacionAsignaturaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTORespuesta.AsignaturaSolicitadaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTORespuesta.CancelacionAsignaturaDetalleDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTORespuesta.FormularioCancelacionAsignaturaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.mapeador.MapperCancelacionAsignaturaInfraestructuraDominio;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.mapeador.MapperSolicitudAcademicaInfraestructuraDominio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorMalFormatoExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTOPeticion.ProgramacionResolucionDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTOPeticion.RespuestaCancelacionAsignaturaDTOPeticion;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapperCancelacionAsignaturaInfraestructuraDominioTest {

    private final MapperCancelacionAsignaturaInfraestructuraDominio mapper =
            new MapperCancelacionAsignaturaInfraestructuraDominio(new MapperSolicitudAcademicaInfraestructuraDominio(),
                    new ExcepcionesFormateadorImplAdaptador());

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
                .asignaturaSolicitudUuid("as-1").numeroFaltas("3").nota("2.5").situacionMatriculaUuid("sit-1")
                .cumpleCondiciones(false).observacionEvaluacion("Nota baja").build()));
        List<DecisionAsignatura> decisiones = mapper.mapearDecisiones(List.of(DecisionAsignaturaDTOPeticion.builder()
                .asignaturaSolicitudUuid("as-1").aprobada(false).build()));
        List<AjusteAsignatura> ajustes = mapper.mapearAjustes(List.of(AjusteAsignaturaDTOPeticion.builder()
                .asignaturaSolicitudUuid("as-1").situacionCancelarUuid("sit-2").observacionDecision("No procede").build()));

        EvaluacionAsignatura evaluacion = evaluaciones.get(0);
        assertEquals("as-1", evaluacion.getUuidAsignaturaSolicitud());
        assertEquals("3", evaluacion.getNumeroFaltas());
        assertEquals("2.5", evaluacion.getNota());
        assertEquals("sit-1", evaluacion.getUuidSituacionMatricula());
        assertFalse(evaluacion.getCumpleCondiciones());
        assertEquals("Nota baja", evaluacion.getObservacionEvaluacion());
        DecisionAsignatura decision = decisiones.get(0);
        assertEquals("as-1", decision.getUuidAsignaturaSolicitud());
        assertFalse(decision.getAprobada());
        assertEquals("as-1", ajustes.get(0).getUuidAsignaturaSolicitud());
        assertEquals("sit-2", ajustes.get(0).getUuidSituacionCancelar());
        assertEquals("No procede", ajustes.get(0).getObservacionDecision());
        assertNull(mapper.mapearEvaluaciones(null));
        assertNull(mapper.mapearDecisiones(null));
        assertNull(mapper.mapearAjustes(null));
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
                        .numeroFaltas("1")
                        .nota("3.5")
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
        assertEquals("1", asignatura.getNumeroFaltas());
        assertEquals("3.5", asignatura.getNota());
        assertEquals("R0", asignatura.getSituacionMatricula().getCodigo());
        assertTrue(asignatura.getCumpleCondiciones());
        assertEquals("Cumple", asignatura.getObservacionEvaluacion());
        assertTrue(asignatura.getAprobadaPorDecano());
        assertEquals("Procede", asignatura.getObservacionDecision());
        assertEquals("R1", asignatura.getSituacionCancelar().getCodigo());
        assertFalse(asignatura.getDecisionCambiadaPorDecano());
    }

    @Test
    void laDecisionCambiadaSeCalculaYQuedaNulaSinDecisionOSinEvaluacion() {
        CancelacionAsignaturaDetalleDTORespuesta respuesta = mapper.mapearDetalleARespuesta(DetalleCancelacionAsignatura.builder()
                .detalle(DetalleSolicitudAcademica.builder().solicitudAcademica(SolicitudAcademica.builder().uuidSolicitudAcademica("sol-1").build()).build())
                .asignaturas(List.of(
                        AsignaturaSolicitudAcademica.builder().uuidAsignaturaSolicitud("as-1").cumpleCondiciones(false).aprobadaPorDecano(true).build(),
                        AsignaturaSolicitudAcademica.builder().uuidAsignaturaSolicitud("as-2").cumpleCondiciones(true).aprobadaPorDecano(false).build(),
                        AsignaturaSolicitudAcademica.builder().uuidAsignaturaSolicitud("as-3").cumpleCondiciones(true).build(),
                        AsignaturaSolicitudAcademica.builder().uuidAsignaturaSolicitud("as-4").aprobadaPorDecano(true).build()))
                .build());

        assertEquals(Arrays.asList(true, true, null, null), respuesta.getAsignaturas().stream()
                .map(AsignaturaSolicitadaDTORespuesta::getDecisionCambiadaPorDecano).toList());
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

    @Test
    void laComunicacionDeLaRespuestaLeeLaFechaYHoraLocalYElMensaje() {
        ComunicacionRespuesta comunicacion = mapper.mapearComunicacion(RespuestaCancelacionAsignaturaDTOPeticion.builder()
                .enviarMensaje(true)
                .mensaje("Hola")
                .programacion(ProgramacionResolucionDTOPeticion.builder()
                        .fechaProgramada("2026-10-21T08:30").numeroResolucion("123-26").anioResolucion(2026).decision("aprueba la").build())
                .build());

        assertTrue(comunicacion.getEnviarMensaje());
        assertEquals("Hola", comunicacion.getMensaje());
        assertEquals(java.time.LocalDateTime.of(2026, 10, 21, 8, 30), comunicacion.getProgramacion().getFechaProgramada());
        assertEquals("123-26", comunicacion.getProgramacion().getNumeroResolucion());
        assertNull(mapper.mapearComunicacion(null));
        assertNull(mapper.mapearComunicacion(RespuestaCancelacionAsignaturaDTOPeticion.builder().build()).getProgramacion());
    }

    @Test
    void unaFechaProgramadaMalFormadaEsErrorDeFormato() {
        assertThrows(ErrorMalFormatoExcepcion.class, () -> mapper.mapearProgramacion(ProgramacionResolucionDTOPeticion.builder()
                .fechaProgramada("21/10/2026 08:30").build()));
    }
}
