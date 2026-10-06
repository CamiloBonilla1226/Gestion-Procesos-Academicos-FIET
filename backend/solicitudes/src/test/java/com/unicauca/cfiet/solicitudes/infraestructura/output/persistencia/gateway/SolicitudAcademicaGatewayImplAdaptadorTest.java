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
}
