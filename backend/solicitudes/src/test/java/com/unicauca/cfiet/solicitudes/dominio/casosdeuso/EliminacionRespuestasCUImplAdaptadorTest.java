package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.AlmacenamientoAnexosIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.EliminacionRespuestasGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.IJwtServicio;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SesionGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EstadoNotificacionResolucion;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Rol;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorReglaNegocioVioladaExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EliminacionRespuestasCUImplAdaptadorTest {

    private static final String TOKEN = "tok";
    private static final String FUNCIONARIO = "fa-1";
    private static final LocalDate HOY = LocalDate.of(2026, 10, 21);

    @Mock
    private EliminacionRespuestasGatewayIntPuerto gateway;

    @Mock
    private SesionGatewayIntPuerto sesionGateway;

    @Mock
    private IJwtServicio jwtServicio;

    @Mock
    private AlmacenamientoAnexosIntPuerto almacenamiento;

    @Mock
    private LogCUIntPuerto log;

    private EliminacionRespuestasCUImplAdaptador casoDeUso;

    @BeforeEach
    void setUp() {
        casoDeUso = new EliminacionRespuestasCUImplAdaptador(gateway, sesionGateway, jwtServicio, almacenamiento,
                new ExcepcionesFormateadorImplAdaptador(), log, Clock.fixed(Instant.parse("2026-10-21T20:00:00Z"), ZoneId.of("America/Bogota")));
        lenient().when(jwtServicio.getUsername(TOKEN)).thenReturn("funcionario");
        lenient().when(sesionGateway.getUsuario("funcionario")).thenReturn(usuario("Funcionario Académico"));
    }

    private Usuario usuario(String rol) {
        return Usuario.builder().uuidUsuario(FUNCIONARIO).roles(List.of(Rol.builder().nombre(rol).estado(true).build())).build();
    }

    private String mensaje(Runnable accion) {
        return assertThrows(ErrorReglaNegocioVioladaExcepcion.class, accion::run).getMessage();
    }

    @Test
    void cuentaSoloLasRespuestasCerradasDelFuncionarioSinComunicacionPendienteConElRangoInclusivo() {
        when(gateway.getRespuestasEliminables(any(), any(), any(), any(), any())).thenReturn(List.of("s1", "s2"));

        assertEquals(2, casoDeUso.contarRespuestas(HOY.minusDays(10), HOY, TOKEN));

        verify(gateway).getRespuestasEliminables(FUNCIONARIO, Set.of("APROBADA", "RECHAZADA"),
                Set.of(EstadoNotificacionResolucion.PROGRAMADA, EstadoNotificacionResolucion.EN_PROCESO, EstadoNotificacionResolucion.FALLIDA),
                HOY.minusDays(10).atStartOfDay(), HOY.plusDays(1).atStartOfDay());
        verify(gateway, never()).eliminar(any());
        verifyNoInteractions(almacenamiento, log);
    }

    @Test
    void eliminarRecalculaEnElServidorBorraLosArchivosTrasConfirmarYDejaElLog() {
        when(gateway.getRespuestasEliminables(any(), any(), any(), any(), any())).thenReturn(List.of("s1", "s2"));
        when(gateway.eliminar(List.of("s1", "s2"))).thenReturn(List.of("/api/anexos/s1/a.pdf", "/api/anexos/s2/r.pdf"));

        assertEquals(2, casoDeUso.eliminarRespuestas(HOY.minusDays(3), HOY.minusDays(1), TOKEN));

        InOrder orden = inOrder(gateway, almacenamiento, log);
        orden.verify(gateway).getRespuestasEliminables(eq(FUNCIONARIO), any(), any(), eq(HOY.minusDays(3).atStartOfDay()), eq(HOY.atStartOfDay()));
        orden.verify(gateway).eliminar(List.of("s1", "s2"));
        orden.verify(almacenamiento).eliminarTrasConfirmar("/api/anexos/s1/a.pdf");
        orden.verify(almacenamiento).eliminarTrasConfirmar("/api/anexos/s2/r.pdf");
        orden.verify(log).crearLog(eq("Eliminar respuestas de solicitudes académicas"), contains("2 solicitudes académicas respondidas entre 2026-10-18 y 2026-10-20"), eq(TOKEN));
    }

    @Test
    void sinRespuestasEnElRangoNoBorraNada() {
        when(gateway.getRespuestasEliminables(any(), any(), any(), any(), any())).thenReturn(List.of());

        assertEquals(0, casoDeUso.eliminarRespuestas(HOY, HOY, TOKEN));

        verify(gateway, never()).eliminar(any());
        verifyNoInteractions(almacenamiento, log);
    }

    @Test
    void lasFechasSonObligatoriasNoFuturasYEnOrden() {
        assertEquals(MensajesError.RANGO_FECHAS_REQUERIDO, mensaje(() -> casoDeUso.contarRespuestas(null, HOY, TOKEN)));
        assertEquals(MensajesError.RANGO_FECHAS_REQUERIDO, mensaje(() -> casoDeUso.eliminarRespuestas(HOY, null, TOKEN)));
        assertEquals(String.format(MensajesError.RANGO_FECHAS_FUTURO, "La fecha inicial", HOY.plusDays(1), HOY),
                mensaje(() -> casoDeUso.contarRespuestas(HOY.plusDays(1), HOY.plusDays(2), TOKEN)));
        assertEquals(String.format(MensajesError.RANGO_FECHAS_FUTURO, "La fecha final", HOY.plusDays(1), HOY),
                mensaje(() -> casoDeUso.eliminarRespuestas(HOY, HOY.plusDays(1), TOKEN)));
        assertEquals(String.format(MensajesError.RANGO_FECHAS_INVERTIDO, HOY, HOY.minusDays(1)),
                mensaje(() -> casoDeUso.eliminarRespuestas(HOY, HOY.minusDays(1), TOKEN)));
        verifyNoInteractions(gateway, almacenamiento, log);
    }

    @Test
    void unUsuarioSinElRolDeFuncionarioAcademicoNoVeNiBorraNada() {
        when(sesionGateway.getUsuario("funcionario")).thenReturn(usuario("Funcionario"));

        assertEquals(0, casoDeUso.contarRespuestas(HOY, HOY, TOKEN));
        assertEquals(0, casoDeUso.eliminarRespuestas(HOY, HOY, TOKEN));

        verifyNoInteractions(gateway, almacenamiento, log);
    }
}
