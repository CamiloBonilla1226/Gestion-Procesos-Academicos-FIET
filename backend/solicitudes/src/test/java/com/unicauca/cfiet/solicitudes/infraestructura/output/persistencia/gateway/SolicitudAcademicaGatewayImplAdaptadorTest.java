package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorReglaNegocioVioladaExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.HistorialSolicitudAcademicaEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudAcademicaEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.HistorialSolicitudAcademicaOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.SolicitudAcademicaOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.*;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudAcademicaGatewayImplAdaptadorTest {

    @Mock
    private SolicitudAcademicaRepositorio repositorio;

    @Mock
    private HistorialSolicitudAcademicaRepositorio historialRepositorio;

    @Mock
    private EstudianteRepositorio estudianteRepositorio;

    @Mock
    private TipoSolicitudAcademicaRepositorio tipoSolicitudRepositorio;

    @Mock
    private EtapaSolicitudAcademicaRepositorio etapaRepositorio;

    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @Mock
    private SolicitudAcademicaOwnMapper mapper;

    @Mock
    private HistorialSolicitudAcademicaOwnMapper historialMapper;

    private SolicitudAcademicaGatewayImplAdaptador gateway;

    @BeforeEach
    void setUp() {
        gateway = new SolicitudAcademicaGatewayImplAdaptador(repositorio, historialRepositorio, estudianteRepositorio,
                tipoSolicitudRepositorio, etapaRepositorio, usuarioRepositorio, mapper, historialMapper,
                new ExcepcionesFormateadorImplAdaptador());
        lenient().when(mapper.toEntidad(any())).thenReturn(new SolicitudAcademicaEntidad());
        lenient().when(historialMapper.toEntidad(any())).thenReturn(new HistorialSolicitudAcademicaEntidad());
    }

    private SolicitudAcademica solicitud() {
        return SolicitudAcademica.builder()
                .uuidSolicitudAcademica("sol-1")
                .radicado("2026-CM-0001")
                .estudiante(Estudiante.builder().uuidUsuario("est-1").build())
                .tipoSolicitudAcademica(TipoSolicitudAcademica.builder().uuidTipoSolicitudAcademica("tipo-cm").build())
                .etapa(EtapaSolicitudAcademica.builder().uuidEtapa("etapa-RADICADA").codigo("RADICADA").build())
                .build();
    }

    private HistorialSolicitudAcademica historial() {
        return HistorialSolicitudAcademica.builder()
                .uuidHistorial("his-1")
                .usuario(Usuario.builder().uuidUsuario("est-1").build())
                .accion("RADICAR")
                .build();
    }

    private DataIntegrityViolationException violacion(String restriccion) {
        String mensaje = "Duplicate entry 'x' for key 'SOLICITUD_ACADEMICA." + restriccion + "'";
        return new DataIntegrityViolationException("could not execute statement",
                new ConstraintViolationException(mensaje, new SQLIntegrityConstraintViolationException(mensaje), restriccion));
    }

    @Test
    void unChoqueDeRadicadoSeVuelveUnErrorDeNegocioSinHistorial() {
        when(repositorio.saveAndFlush(any())).thenThrow(violacion("uk_solacad_radicado"));

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> gateway.crear(solicitud(), historial()));

        assertTrue(error.getMessage().contains("No se pudo generar el radicado, intente de nuevo"));
        verify(repositorio, times(1)).saveAndFlush(any());
        verify(historialRepositorio, never()).saveAndFlush(any());
    }

    @Test
    void elChoqueSeReconoceAunqueSoloLoDigaElMensajeDeLaBase() {
        String mensaje = "Duplicate entry '2026-CM-0001' for key 'SOLICITUD_ACADEMICA.uk_solacad_radicado'";
        when(repositorio.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("could not execute statement",
                new SQLIntegrityConstraintViolationException(mensaje)));

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> gateway.crear(solicitud(), historial()));
        verify(historialRepositorio, never()).saveAndFlush(any());
    }

    @Test
    void otraViolacionDeIntegridadSePropagaSinCambios() {
        DataIntegrityViolationException original = violacion("fk_solacad_estudiante");
        when(repositorio.saveAndFlush(any())).thenThrow(original);

        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> gateway.crear(solicitud(), historial()));

        assertSame(original, error);
        verify(historialRepositorio, never()).saveAndFlush(any());
    }

    @Test
    void sinChoqueSeGuardanLaSolicitudYSuHistorial() {
        SolicitudAcademicaEntidad guardada = new SolicitudAcademicaEntidad();
        when(repositorio.saveAndFlush(any())).thenReturn(guardada);
        when(mapper.toDominio(guardada)).thenReturn(solicitud());

        assertEquals("2026-CM-0001", gateway.crear(solicitud(), historial()).getRadicado());
        verify(historialRepositorio).saveAndFlush(any());
    }

    @Test
    void elRadicadoEnCursoExcluyeLasEtapasFinales() {
        SolicitudAcademicaEntidad enCurso = new SolicitudAcademicaEntidad();
        enCurso.setRadicado("2026-CM-0003");
        List<String> finales = List.of("APROBADA", "RECHAZADA");
        lenient().when(repositorio.findFirstByEstudiante_UuidUsuarioAndTipoSolicitudAcademica_UuidTipoSolicitudAcademicaAndEtapa_CodigoNotInOrderByFechaCreacionDesc(
                "est-1", "tipo-cm", finales)).thenReturn(Optional.of(enCurso));

        assertEquals("2026-CM-0003", gateway.getRadicadoEnCurso("est-1", "tipo-cm", finales));
        assertNull(gateway.getRadicadoEnCurso("est-1", "tipo-ca", finales));
    }

    @Test
    void laUltimaAccionDeCadaSolicitudEsLaMasReciente() {
        List<String> acciones = List.of("APROBAR_DECANO", "RECHAZAR_DECANO");
        when(historialRepositorio.findAccionesPorSolicitudes(List.of("s1", "s2"), acciones)).thenReturn(List.of(
                new Object[]{"s1", "RECHAZAR_DECANO"}, new Object[]{"s2", "APROBAR_DECANO"}, new Object[]{"s1", "APROBAR_DECANO"}));

        assertEquals(java.util.Map.of("s1", "APROBAR_DECANO", "s2", "APROBAR_DECANO"), gateway.getUltimaAccion(List.of("s1", "s2"), acciones));
        assertTrue(gateway.getUltimaAccion(List.of(), acciones).isEmpty());
        verify(historialRepositorio, times(1)).findAccionesPorSolicitudes(any(), any());
    }

    @Test
    void lasSolicitudesConAsignaturaNoAprobadaSalenDelRepositorioYSinUuidsNoConsulta() {
        when(repositorio.findConAsignaturaNoAprobadaPorDecano(List.of("s1", "s2"))).thenReturn(List.of("s2"));

        assertEquals(java.util.Set.of("s2"), gateway.getConAsignaturaNoAprobadaPorDecano(List.of("s1", "s2")));
        assertTrue(gateway.getConAsignaturaNoAprobadaPorDecano(List.of()).isEmpty());
        verify(repositorio, times(1)).findConAsignaturaNoAprobadaPorDecano(any());
    }

    @Test
    void losResultadosPorAsignaturaSeAgrupanPorSolicitudYSoloTrueEsAprobada() {
        when(repositorio.findResultadosAsignaturas(List.of("s1", "s2", "s3"))).thenReturn(List.of(
                new Object[]{"s1", "a1", true},
                new Object[]{"s1", "a2", false},
                new Object[]{"s2", "a3", null},
                new Object[]{"s3", "a4", true}));

        java.util.Map<String, List<com.unicauca.cfiet.solicitudes.dominio.modelos.ResultadoAsignaturaSolicitud>> resultados =
                gateway.getResultadosAsignaturas(List.of("s1", "s2", "s3"));

        assertEquals(List.of("a1=true", "a2=false"), resultados.get("s1").stream().map(r -> r.getAsignaturaSolicitudUuid() + "=" + r.isAprobada()).toList());
        assertEquals(List.of("a3=false"), resultados.get("s2").stream().map(r -> r.getAsignaturaSolicitudUuid() + "=" + r.isAprobada()).toList());
        assertEquals(List.of("a4=true"), resultados.get("s3").stream().map(r -> r.getAsignaturaSolicitudUuid() + "=" + r.isAprobada()).toList());
        assertTrue(gateway.getResultadosAsignaturas(List.of()).isEmpty());
        verify(repositorio, times(1)).findResultadosAsignaturas(any());
    }
}
