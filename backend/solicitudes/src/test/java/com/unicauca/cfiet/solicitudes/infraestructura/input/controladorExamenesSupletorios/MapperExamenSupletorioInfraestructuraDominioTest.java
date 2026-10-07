package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios;

import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.DTORespuesta.ExamenSupletorioDetalleDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.DTORespuesta.FormularioExamenSupletorioDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.mapeador.MapperExamenSupletorioInfraestructuraDominio;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.mapeador.MapperSolicitudAcademicaInfraestructuraDominio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorMalFormatoExcepcion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapperExamenSupletorioInfraestructuraDominioTest {

    private final MapperExamenSupletorioInfraestructuraDominio mapper = new MapperExamenSupletorioInfraestructuraDominio(
            new MapperSolicitudAcademicaInfraestructuraDominio(), new ExcepcionesFormateadorImplAdaptador());

    private AsignaturaMatriculada materia(String uuid) {
        return AsignaturaMatriculada.builder().uuidAsignaturaMatriculada(uuid).grupo("B").estado("activa")
                .asignatura(Asignatura.builder().codigoAsignatura("SIS-" + uuid).nombreAsignatura("Materia " + uuid).build()).build();
    }

    private DetalleExamenSupletorio detalle(SolicitudExamenSupletorio supletorio) {
        return DetalleExamenSupletorio.builder()
                .detalle(DetalleSolicitudAcademica.builder()
                        .solicitudAcademica(SolicitudAcademica.builder().uuidSolicitudAcademica("sol-1").radicado("2026-ES-0001").build())
                        .etiqueta("En trámite")
                        .anexos(List.of(AnexoAcademico.builder().uuidAnexoAcademico("an-1").nombreArchivo("for23.pdf")
                                .tipoAnexoAcademico(TipoAnexoAcademico.builder().uuidTipoAnexoAcademico("t-1").nombre("FOR-23").build()).build()))
                        .build())
                .supletorio(supletorio)
                .build();
    }

    @Test
    void lasFechasSeLeenEnFormatoIsoYVaciasQuedanNulas() {
        assertEquals(LocalDate.of(2026, 10, 5), mapper.mapearFecha(" 2026-10-05 ", "la fecha"));
        assertNull(mapper.mapearFecha(null, "la fecha"));
        assertNull(mapper.mapearFecha("  ", "la fecha"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"05/10/2026", "2026-13-01", "ayer", "2026-10-5"})
    void unaFechaMalFormadaEsErrorDeFormato(String valor) {
        ErrorMalFormatoExcepcion error = assertThrows(ErrorMalFormatoExcepcion.class, () -> mapper.mapearFecha(valor, "la fecha del examen"));

        assertTrue(error.getMessage().contains(valor));
        assertTrue(error.getMessage().contains("la fecha del examen"));
    }

    @Test
    void cadaParteDeArchivoUsaSuNombreComoTipoDeAnexo() {
        MultiValueMap<String, MultipartFile> partes = new LinkedMultiValueMap<>();
        partes.add("tipo-for23", new MockMultipartFile("tipo-for23", "for23.pdf", "application/pdf", "%PDF-1.7".getBytes(StandardCharsets.US_ASCII)));
        partes.add("soporte", new MockMultipartFile("soporte", "libre.pdf", "application/pdf", "%PDF-1.7".getBytes(StandardCharsets.US_ASCII)));

        List<AnexoRadicacion> anexos = mapper.mapearAnexos(partes);

        assertEquals(Arrays.asList("tipo-for23", null), anexos.stream().map(AnexoRadicacion::getUuidTipoAnexoAcademico).toList());
        assertTrue(mapper.mapearAnexos(null).isEmpty());
    }

    @Test
    void elFormularioExponeCausasEnMinusculaYCadaAnexoConSusCausas() {
        FormularioExamenSupletorioDTORespuesta respuesta = mapper.mapearFormularioARespuesta(FormularioExamenSupletorio.builder()
                .asignaturas(List.of(materia("am-1")))
                .causas(List.of(CausaSupletorio.CRUCE, CausaSupletorio.OTRA))
                .anexos(List.of(AnexoSupletorio.builder()
                        .tipoAnexo(TipoAnexoAcademico.builder().uuidTipoAnexoAcademico("t-just").nombre("Soporte").formatosPermitidos("pdf,jpg,png").build())
                        .tamanioMaximoBytes(5242880L)
                        .causas(List.of(CausaSupletorio.OTRA))
                        .build()))
                .plazoDiasHabiles(3)
                .build());

        assertEquals("am-1", respuesta.getAsignaturas().get(0).getUuidAsignaturaMatriculada());
        assertEquals("SIS-am-1", respuesta.getAsignaturas().get(0).getCodigoAsignatura());
        assertEquals("B", respuesta.getAsignaturas().get(0).getGrupo());
        assertEquals(List.of("cruce", "otra"), respuesta.getCausas());
        assertEquals("t-just", respuesta.getAnexos().get(0).getUuidTipoAnexoAcademico());
        assertEquals("pdf,jpg,png", respuesta.getAnexos().get(0).getFormatosPermitidos());
        assertEquals(5242880L, respuesta.getAnexos().get(0).getTamanioMaximoBytes());
        assertEquals(List.of("otra"), respuesta.getAnexos().get(0).getCausas());
        assertEquals(3, respuesta.getPlazoDiasHabiles());
    }

    @Test
    void elDetallePorCruceTraeAsignaturaCruceFechasYAnexosSinContenido() {
        ExamenSupletorioDetalleDTORespuesta respuesta = mapper.mapearDetalleARespuesta(detalle(SolicitudExamenSupletorio.builder()
                .asignaturaMatriculada(materia("am-1"))
                .fechaExamenNoPresentado(LocalDate.of(2026, 10, 5))
                .tipoCausa(CausaSupletorio.CRUCE)
                .cruce(CruceSupletorio.builder().asignaturaMatriculadaCruzada(materia("am-2")).fechaExamenCruzada(LocalDate.of(2026, 10, 5))
                        .horaExamenCruzada("14:30").build())
                .fechaAcordadaExamen(LocalDate.of(2026, 10, 20).atStartOfDay())
                .build()));

        assertEquals("2026-ES-0001", respuesta.getSolicitud().getRadicado());
        assertEquals("En trámite", respuesta.getSolicitud().getEtiqueta());
        assertEquals("FOR-23", respuesta.getSolicitud().getAnexos().get(0).getTipoAnexo());
        assertEquals("am-1", respuesta.getAsignatura().getUuidAsignaturaMatriculada());
        assertEquals("cruce", respuesta.getTipoCausa());
        assertEquals("2026-10-05", respuesta.getFechaExamenNoPresentado());
        assertEquals("am-2", respuesta.getCruce().getAsignatura().getUuidAsignaturaMatriculada());
        assertEquals("2026-10-05", respuesta.getCruce().getFechaExamenCruzada());
        assertEquals("14:30", respuesta.getCruce().getHoraExamenCruzada());
        assertEquals("2026-10-20", respuesta.getFechaAcordadaExamen());
    }

    @Test
    void elDetallePorOtraCausaNoTraeCruceNiFechaAcordadaSiNoExiste() {
        ExamenSupletorioDetalleDTORespuesta respuesta = mapper.mapearDetalleARespuesta(detalle(SolicitudExamenSupletorio.builder()
                .asignaturaMatriculada(materia("am-1"))
                .fechaExamenNoPresentado(LocalDate.of(2026, 10, 5))
                .tipoCausa(CausaSupletorio.OTRA)
                .build()));

        assertEquals("otra", respuesta.getTipoCausa());
        assertNull(respuesta.getCruce());
        assertNull(respuesta.getFechaAcordadaExamen());
    }

    @Test
    void elDetalleTraeElCodigoDeLaEtapaSinCambiarLaEtiqueta() {
        DetalleExamenSupletorio detalle = detalle(SolicitudExamenSupletorio.builder().tipoCausa(CausaSupletorio.OTRA).build());
        detalle.getDetalle().getSolicitudAcademica().setEtapa(EtapaSolicitudAcademica.builder().uuidEtapa("e-1").codigo("PENDIENTE_PAGO").build());

        ExamenSupletorioDetalleDTORespuesta respuesta = mapper.mapearDetalleARespuesta(detalle);

        assertEquals("PENDIENTE_PAGO", respuesta.getSolicitud().getEtapaCodigo());
        assertEquals("En trámite", respuesta.getSolicitud().getEtiqueta());
    }
}
