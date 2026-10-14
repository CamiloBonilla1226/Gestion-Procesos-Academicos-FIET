package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas;

import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta.HistorialSolicitudAcademicaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta.ResultadoAsignaturaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta.SolicitudAcademicaDetalleDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta.SolicitudAcademicaResumenDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.mapeador.MapperSolicitudAcademicaInfraestructuraDominio;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapperSolicitudAcademicaInfraestructuraDominioTest {

    private final MapperSolicitudAcademicaInfraestructuraDominio mapper = new MapperSolicitudAcademicaInfraestructuraDominio();

    private SolicitudAcademica solicitud(String uuid, String codigoEtapa) {
        return SolicitudAcademica.builder()
                .uuidSolicitudAcademica(uuid)
                .radicado("2026-CM-" + uuid)
                .fechaCreacion(LocalDateTime.of(2026, 10, 6, 8, 0))
                .tipoSolicitudAcademica(TipoSolicitudAcademica.builder().uuidTipoSolicitudAcademica("tipo-cm").nombre("Cancelación de Matrícula").build())
                .etapa(codigoEtapa == null ? null : EtapaSolicitudAcademica.builder().uuidEtapa("etapa-" + codigoEtapa).codigo(codigoEtapa).build())
                .build();
    }

    @Test
    void cadaFilaDeLaBandejaTraeElCodigoDeLaEtapaYConservaSuEtiqueta() {
        List<SolicitudAcademicaResumenDTORespuesta> bandeja = mapper.mapearBandejaARespuesta(List.of(
                ResumenSolicitudAcademica.builder().solicitudAcademica(solicitud("s1", "RADICADA")).etiqueta("Radicada").build(),
                ResumenSolicitudAcademica.builder().solicitudAcademica(solicitud("s2", "PENDIENTE_PAGO")).etiqueta("Pendiente de pago").build()));

        assertEquals(List.of("RADICADA", "PENDIENTE_PAGO"), bandeja.stream().map(SolicitudAcademicaResumenDTORespuesta::getEtapaCodigo).toList());
        assertEquals(List.of("Radicada", "Pendiente de pago"), bandeja.stream().map(SolicitudAcademicaResumenDTORespuesta::getEtiqueta).toList());
        assertEquals("2026-CM-s1", bandeja.get(0).getRadicado());
        assertEquals("tipo-cm", bandeja.get(0).getUuidTipoSolicitudAcademica());
    }

    @Test
    void elDetalleTraeElCodigoDeLaEtapaYConservaEtiquetaYAcciones() {
        SolicitudAcademicaDetalleDTORespuesta respuesta = mapper.mapearDetalleARespuesta(DetalleSolicitudAcademica.builder()
                .solicitudAcademica(solicitud("s1", "EN_REVISION_DECANO"))
                .etiqueta("Por decidir")
                .accionesDisponibles(List.of(AccionEtapa.APROBAR_DECANO, AccionEtapa.RECHAZAR_DECANO))
                .build());

        assertEquals("EN_REVISION_DECANO", respuesta.getEtapaCodigo());
        assertEquals("Por decidir", respuesta.getEtiqueta());
        assertEquals(List.of("APROBAR_DECANO", "RECHAZAR_DECANO"), respuesta.getAccionesDisponibles());
    }

    @Test
    void unaSolicitudSinEtapaSaleConCodigoNulo() {
        SolicitudAcademicaDetalleDTORespuesta detalle = mapper.mapearDetalleARespuesta(DetalleSolicitudAcademica.builder()
                .solicitudAcademica(solicitud("s1", null)).etiqueta("Radicada").build());
        SolicitudAcademicaResumenDTORespuesta resumen = mapper.mapearResumenARespuesta(ResumenSolicitudAcademica.builder()
                .solicitudAcademica(solicitud("s1", null)).etiqueta("Radicada").build());

        assertNull(detalle.getEtapaCodigo());
        assertNull(resumen.getEtapaCodigo());
        assertEquals("Radicada", detalle.getEtiqueta());
    }

    @Test
    void elCodigoVisibleDelCasoDeUsoTienePrioridadSobreElDeLaEtapa() {
        SolicitudAcademicaDetalleDTORespuesta detalle = mapper.mapearDetalleARespuesta(DetalleSolicitudAcademica.builder()
                .solicitudAcademica(solicitud("s1", "APROBADA_POR_DECANO")).etiqueta("En trámite").etapaCodigo("EN_REVISION_DECANO").build());
        SolicitudAcademicaResumenDTORespuesta resumen = mapper.mapearResumenARespuesta(ResumenSolicitudAcademica.builder()
                .solicitudAcademica(solicitud("s1", "RECHAZADA_POR_DECANO")).etiqueta("En trámite").etapaCodigo("EN_REVISION_DECANO").build());

        assertEquals("EN_REVISION_DECANO", detalle.getEtapaCodigo());
        assertEquals("EN_REVISION_DECANO", resumen.getEtapaCodigo());
        assertEquals("En trámite", detalle.getEtiqueta());
    }

    @Test
    void elNumeroDeDocumentoSaleDelResumenYNoDelUsuarioDeLaSolicitud() {
        SolicitudAcademica solicitud = solicitud("s1", "RADICADA");
        solicitud.setEstudiante(Estudiante.builder().codigoEstudiantil("104622010148")
                .usuario(Usuario.builder().nombres("Laura").apellidos("Muñoz").numeroDocumento("1061789234").build()).build());

        SolicitudAcademicaResumenDTORespuesta conDocumento = mapper.mapearResumenARespuesta(ResumenSolicitudAcademica.builder()
                .solicitudAcademica(solicitud).etiqueta("Pendiente").numeroDocumento("1061789234").build());
        SolicitudAcademicaResumenDTORespuesta sinDocumento = mapper.mapearResumenARespuesta(ResumenSolicitudAcademica.builder()
                .solicitudAcademica(solicitud).etiqueta("En trámite").build());

        assertEquals("1061789234", conDocumento.getNumeroDocumento());
        assertEquals("104622010148", conDocumento.getCodigoEstudiantil());
        assertNull(sinDocumento.getNumeroDocumento());
        assertEquals("Laura Muñoz", sinDocumento.getNombreEstudiante());
    }

    @Test
    void laDecisionDelDecanoDelResumenPasaTalCual() {
        SolicitudAcademica solicitud = solicitud("s1", "APROBADA");

        assertEquals("APROBADA_PARCIAL", mapper.mapearResumenARespuesta(ResumenSolicitudAcademica.builder()
                .solicitudAcademica(solicitud).etiqueta("Respondida").decisionDecano("APROBADA_PARCIAL").build()).getDecisionDecano());
        assertNull(mapper.mapearResumenARespuesta(ResumenSolicitudAcademica.builder()
                .solicitudAcademica(solicitud).etiqueta("Respondida").build()).getDecisionDecano());
    }

    @Test
    void losResultadosPorAsignaturaDelResumenPasanEnOrdenYNuloSiNoHay() {
        SolicitudAcademica solicitud = solicitud("s1", "APROBADA");

        SolicitudAcademicaResumenDTORespuesta conResultados = mapper.mapearResumenARespuesta(ResumenSolicitudAcademica.builder()
                .solicitudAcademica(solicitud).etiqueta("Respondida")
                .resultadosAsignaturas(List.of(new ResultadoAsignaturaSolicitud("a1", true), new ResultadoAsignaturaSolicitud("a2", false)))
                .build());

        assertEquals(List.of("a1", "a2"), conResultados.getResultadosAsignaturas().stream().map(ResultadoAsignaturaDTORespuesta::getAsignaturaSolicitudUuid).toList());
        assertEquals(List.of(true, false), conResultados.getResultadosAsignaturas().stream().map(ResultadoAsignaturaDTORespuesta::isAprobada).toList());
        assertNull(mapper.mapearResumenARespuesta(ResumenSolicitudAcademica.builder()
                .solicitudAcademica(solicitud).etiqueta("Respondida").build()).getResultadosAsignaturas());
    }

    @Test
    void elNumeroDeDocumentoDelDetalleSaleDeLaConsultaYNoDelUsuarioDeLaSolicitud() {
        SolicitudAcademica solicitud = solicitud("s1", "RADICADA");
        solicitud.setEstudiante(Estudiante.builder().codigoEstudiantil("104622010148")
                .usuario(Usuario.builder().nombres("Laura").apellidos("Muñoz").numeroDocumento("1061789234").build()).build());

        SolicitudAcademicaDetalleDTORespuesta conDocumento = mapper.mapearDetalleARespuesta(DetalleSolicitudAcademica.builder()
                .solicitudAcademica(solicitud).etiqueta("Pendiente").numeroDocumento("1061789234").build());
        SolicitudAcademicaDetalleDTORespuesta sinDocumento = mapper.mapearDetalleARespuesta(DetalleSolicitudAcademica.builder()
                .solicitudAcademica(solicitud).etiqueta("En trámite").build());

        assertEquals("1061789234", conDocumento.getNumeroDocumento());
        assertNull(sinDocumento.getNumeroDocumento());
        assertEquals("104622010148", sinDocumento.getEstudiante().getCodigoEstudiantil());
        assertEquals("Laura", sinDocumento.getEstudiante().getNombres());
    }

    @Test
    void elCodigoEstudiantilSaleDelEstudianteDuenoEnElResumenYEnElDetalle() {
        SolicitudAcademica solicitud = solicitud("s1", "RADICADA");
        solicitud.setEstudiante(Estudiante.builder().codigoEstudiantil("104622010148")
                .usuario(Usuario.builder().nombres("Laura").apellidos("Muñoz").build()).build());

        SolicitudAcademicaResumenDTORespuesta resumen = mapper.mapearResumenARespuesta(ResumenSolicitudAcademica.builder()
                .solicitudAcademica(solicitud).etiqueta("Pendiente").numeroDocumento("1061789234").build());
        SolicitudAcademicaDetalleDTORespuesta detalle = mapper.mapearDetalleARespuesta(DetalleSolicitudAcademica.builder()
                .solicitudAcademica(solicitud).etiqueta("Pendiente").numeroDocumento("1061789234").build());
        SolicitudAcademicaDetalleDTORespuesta sinEstudiante = mapper.mapearDetalleARespuesta(DetalleSolicitudAcademica.builder()
                .solicitudAcademica(solicitud("s2", "RADICADA")).etiqueta("Pendiente").build());

        assertEquals("104622010148", resumen.getCodigoEstudiantil());
        assertEquals("104622010148", detalle.getCodigoEstudiantil());
        assertEquals("104622010148", detalle.getEstudiante().getCodigoEstudiantil());
        assertNull(sinEstudiante.getCodigoEstudiantil());
    }

    @Test
    void cadaFilaDelHistorialTraeElCodigoDeLaEtapaALaQueLlevo() {
        List<HistorialSolicitudAcademicaDTORespuesta> historial = mapper.mapearHistorialARespuesta(List.of(
                HistorialSolicitudAcademica.builder().accion("RADICAR").etapaCodigo("RADICADA").observaciones("Radicada").build(),
                HistorialSolicitudAcademica.builder().accion("REMITIR_DECANO").etapaCodigo("EN_REVISION_DECANO")
                        .usuario(Usuario.builder().nombres("Ana").apellidos("Paz").build()).build()));

        assertEquals(List.of("RADICADA", "EN_REVISION_DECANO"), historial.stream().map(HistorialSolicitudAcademicaDTORespuesta::getEtapaCodigo).toList());
        assertEquals(List.of("RADICAR", "REMITIR_DECANO"), historial.stream().map(HistorialSolicitudAcademicaDTORespuesta::getAccion).toList());
        assertEquals("Radicada", historial.get(0).getObservaciones());
        assertEquals("Ana", historial.get(1).getNombresUsuario());
    }
}
