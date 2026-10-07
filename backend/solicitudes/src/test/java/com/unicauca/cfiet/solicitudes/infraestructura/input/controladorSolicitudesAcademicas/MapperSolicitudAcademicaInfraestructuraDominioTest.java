package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas;

import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta.HistorialSolicitudAcademicaDTORespuesta;
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
