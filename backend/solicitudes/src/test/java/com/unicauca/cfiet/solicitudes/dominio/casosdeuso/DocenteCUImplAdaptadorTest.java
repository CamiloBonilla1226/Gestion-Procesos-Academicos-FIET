package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.output.DocenteGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Docente;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorMalFormatoExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocenteCUImplAdaptadorTest {

    @Mock
    private DocenteGatewayIntPuerto gateway;

    private DocenteCUImplAdaptador casoDeUso;

    @BeforeEach
    void setUp() {
        casoDeUso = new DocenteCUImplAdaptador(gateway, new ExcepcionesFormateadorImplAdaptador());
    }

    @Test
    void buscaConLosFiltrosRecortadosYDevuelveLaPaginaDelGateway() {
        PaginacionRespuestaDTO<Docente> pagina = new PaginacionRespuestaDTO<>(
                List.of(Docente.builder().uuidUsuario("doc-1").nombres("Ana").apellidos("Ruiz").build()), 1);
        when(gateway.buscar("Ana", "106", "aruiz", 0, 10)).thenReturn(pagina);

        PaginacionRespuestaDTO<Docente> resultado = casoDeUso.buscarDocentes(" Ana ", " 106 ", " aruiz ", 0, 10);

        assertSame(pagina, resultado);
    }

    @Test
    void losFiltrosVaciosViajanComoNulos() {
        when(gateway.buscar(null, null, null, 2, 5)).thenReturn(new PaginacionRespuestaDTO<>(List.of(), 0));

        casoDeUso.buscarDocentes("", "   ", null, 2, 5);

        verify(gateway).buscar(null, null, null, 2, 5);
    }

    @ParameterizedTest
    @CsvSource({"-1,10", "0,0", "0,-3", "0,101"})
    void unaPaginacionInvalidaSeRechazaSinConsultar(int pagina, int tamanio) {
        ErrorMalFormatoExcepcion error = assertThrows(ErrorMalFormatoExcepcion.class,
                () -> casoDeUso.buscarDocentes(null, null, null, pagina, tamanio));

        assertEquals(MensajesError.PAGINACION_ERROR, error.getMessage());
        verify(gateway, never()).buscar(any(), any(), any(), anyInt(), anyInt());
    }
}
