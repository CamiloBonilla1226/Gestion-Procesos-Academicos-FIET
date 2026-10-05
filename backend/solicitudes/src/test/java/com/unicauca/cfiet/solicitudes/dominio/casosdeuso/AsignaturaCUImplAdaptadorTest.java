package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.AsignaturaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Asignatura;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorMalFormatoExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsignaturaCUImplAdaptadorTest {

    private static final String TOKEN = "token";

    @Mock
    private AsignaturaGatewayIntPuerto gateway;

    @Mock
    private LogCUIntPuerto log;

    private AsignaturaCUImplAdaptador casoDeUso;

    @BeforeEach
    void setUp() {
        casoDeUso = new AsignaturaCUImplAdaptador(gateway, new ExcepcionesFormateadorImplAdaptador(), log);
    }

    private Asignatura asignatura(String uuid, String codigo, String nombre) {
        return Asignatura.builder()
                .uuidAsignatura(uuid)
                .codigoAsignatura(codigo)
                .nombreAsignatura(nombre)
                .build();
    }

    @Test
    void crearAsignaturaConCodigoNuevoAsignaUuidGuardaYRegistraLog() {
        when(gateway.existePorCodigo("IS101")).thenReturn(false);
        when(gateway.guardar(any(Asignatura.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Asignatura creada = casoDeUso.crearAsignatura(asignatura(null, "IS101", "Introduccion a la Informatica"), TOKEN);

        ArgumentCaptor<Asignatura> captor = ArgumentCaptor.forClass(Asignatura.class);
        verify(gateway).guardar(captor.capture());
        assertNotNull(captor.getValue().getUuidAsignatura());
        assertDoesNotThrow(() -> UUID.fromString(captor.getValue().getUuidAsignatura()));
        assertEquals("IS101", creada.getCodigoAsignatura());
        verify(log).crearLog(eq("Crear asignatura"), anyString(), eq(TOKEN));
    }

    @Test
    void crearAsignaturaConCodigoExistenteLanzaEntidadExiste() {
        when(gateway.existePorCodigo("IS101")).thenReturn(true);

        assertThrows(ErrorEntidadExisteExcepcion.class,
                () -> casoDeUso.crearAsignatura(asignatura(null, "IS101", "Otra"), TOKEN));

        verify(gateway, never()).guardar(any());
        verifyNoInteractions(log);
    }

    @Test
    void getAsignaturasPaginadoDevuelveLaPaginaDelGateway() {
        PaginacionRespuestaDTO<Asignatura> pagina = new PaginacionRespuestaDTO<>(
                List.of(asignatura("u1", "IS101", "Introduccion")), 1);
        when(gateway.getPaginado(0, 10)).thenReturn(pagina);

        PaginacionRespuestaDTO<Asignatura> respuesta = casoDeUso.getAsignaturasPaginado(0, 10);

        assertEquals(1, respuesta.getContent().size());
        assertEquals(1, respuesta.getTotalElements());
    }

    @Test
    void getAsignaturasPaginadoSinResultadosDevuelvePaginaVaciaSinError() {
        when(gateway.getPaginado(0, 10)).thenReturn(new PaginacionRespuestaDTO<>(List.of(), 0));

        PaginacionRespuestaDTO<Asignatura> respuesta = assertDoesNotThrow(() -> casoDeUso.getAsignaturasPaginado(0, 10));

        assertTrue(respuesta.getContent().isEmpty());
        assertEquals(0, respuesta.getTotalElements());
    }

    @Test
    void getAsignaturasPaginadoConPaginaNegativaLanzaMalFormato() {
        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.getAsignaturasPaginado(-1, 10));
        verify(gateway, never()).getPaginado(anyInt(), anyInt());
    }

    @Test
    void getAsignaturasPaginadoConTamanioCeroLanzaMalFormato() {
        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.getAsignaturasPaginado(0, 0));
        verify(gateway, never()).getPaginado(anyInt(), anyInt());
    }

    @Test
    void getAsignaturasPorFiltroDevuelveLaPaginaDelGateway() {
        PaginacionRespuestaDTO<Asignatura> pagina = new PaginacionRespuestaDTO<>(
                List.of(asignatura("u1", "IS101", "Introduccion")), 1);
        when(gateway.getPorFiltro("intro", 0, 5)).thenReturn(pagina);

        PaginacionRespuestaDTO<Asignatura> respuesta = casoDeUso.getAsignaturasPorFiltro("intro", 0, 5);

        assertEquals("IS101", respuesta.getContent().get(0).getCodigoAsignatura());
    }

    @Test
    void getAsignaturasPorFiltroSinResultadosDevuelvePaginaVaciaSinError() {
        when(gateway.getPorFiltro("nada", 0, 5)).thenReturn(new PaginacionRespuestaDTO<>(List.of(), 0));

        PaginacionRespuestaDTO<Asignatura> respuesta = assertDoesNotThrow(() -> casoDeUso.getAsignaturasPorFiltro("nada", 0, 5));

        assertTrue(respuesta.getContent().isEmpty());
    }

    @Test
    void getAsignaturasPorFiltroConPaginaNegativaLanzaMalFormato() {
        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.getAsignaturasPorFiltro("x", -1, 5));
        verify(gateway, never()).getPorFiltro(any(), anyInt(), anyInt());
    }

    @Test
    void getAsignaturasPorFiltroConTamanioCeroLanzaMalFormato() {
        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.getAsignaturasPorFiltro("x", 0, 0));
        verify(gateway, never()).getPorFiltro(any(), anyInt(), anyInt());
    }

    @Test
    void getAsignaturaExistenteLaDevuelve() {
        when(gateway.getPorUuid("u1")).thenReturn(asignatura("u1", "IS101", "Introduccion"));

        Asignatura encontrada = casoDeUso.getAsignatura("u1");

        assertEquals("IS101", encontrada.getCodigoAsignatura());
    }

    @Test
    void getAsignaturaInexistenteLanzaEntidadNoExiste() {
        when(gateway.getPorUuid("no-existe")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getAsignatura("no-existe"));
    }

    @Test
    void actualizarAsignaturaCambiaCodigoYNombreYRegistraLog() {
        when(gateway.getPorUuid("u1")).thenReturn(asignatura("u1", "IS101", "Introduccion"));
        when(gateway.getPorCodigo("IS102")).thenReturn(null);
        when(gateway.guardar(any(Asignatura.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Asignatura actualizada = casoDeUso.actualizarAsignatura("u1", asignatura(null, "IS102", "Programacion I"), TOKEN);

        assertEquals("u1", actualizada.getUuidAsignatura());
        assertEquals("IS102", actualizada.getCodigoAsignatura());
        assertEquals("Programacion I", actualizada.getNombreAsignatura());
        verify(log).crearLog(eq("Actualizar asignatura"), anyString(), eq(TOKEN));
    }

    @Test
    void actualizarAsignaturaConElMismoCodigoNoLoTomaComoDuplicado() {
        when(gateway.getPorUuid("u1")).thenReturn(asignatura("u1", "IS101", "Introduccion"));
        when(gateway.guardar(any(Asignatura.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Asignatura actualizada = casoDeUso.actualizarAsignatura("u1", asignatura(null, "IS101", "Nuevo nombre"), TOKEN);

        assertEquals("Nuevo nombre", actualizada.getNombreAsignatura());
        verify(gateway, never()).getPorCodigo(anyString());
    }

    @Test
    void actualizarAsignaturaInexistenteLanzaEntidadNoExiste() {
        when(gateway.getPorUuid("no-existe")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.actualizarAsignatura("no-existe", asignatura(null, "IS101", "X"), TOKEN));

        verify(gateway, never()).guardar(any());
        verifyNoInteractions(log);
    }

    @Test
    void actualizarAsignaturaConCodigoDeOtraAsignaturaLanzaEntidadExiste() {
        when(gateway.getPorUuid("u1")).thenReturn(asignatura("u1", "IS101", "Introduccion"));
        when(gateway.getPorCodigo("IS102")).thenReturn(asignatura("u2", "IS102", "Programacion I"));

        assertThrows(ErrorEntidadExisteExcepcion.class,
                () -> casoDeUso.actualizarAsignatura("u1", asignatura(null, "IS102", "Introduccion"), TOKEN));

        verify(gateway, never()).guardar(any());
        verifyNoInteractions(log);
    }
}
