package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoAnexoAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoAnexoAcademico;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoAnexoAcademicoCUImplAdaptadorTest {

    @Mock
    private TipoAnexoAcademicoGatewayIntPuerto gateway;

    @Mock
    private TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway;

    private TipoAnexoAcademicoCUImplAdaptador casoDeUso;

    @BeforeEach
    void setUp() {
        casoDeUso = new TipoAnexoAcademicoCUImplAdaptador(gateway, tipoSolicitudGateway, new ExcepcionesFormateadorImplAdaptador());
    }

    @Test
    void getTiposAnexoPorTipoExistenteDevuelveLosDelGateway() {
        List<TipoAnexoAcademico> tipos = List.of(TipoAnexoAcademico.builder()
                .uuidTipoAnexoAcademico("a1").nombre("Paz y salvo").formatosPermitidos("pdf").obligatorio(true).build());
        when(tipoSolicitudGateway.existePorUuid("t1")).thenReturn(true);
        when(gateway.getPorTipo("t1")).thenReturn(tipos);

        assertSame(tipos, casoDeUso.getTiposAnexoPorTipo("t1"));
    }

    @Test
    void getTiposAnexoPorTipoSinAnexosDevuelveListaVacia() {
        when(tipoSolicitudGateway.existePorUuid("t1")).thenReturn(true);
        when(gateway.getPorTipo("t1")).thenReturn(List.of());

        assertTrue(casoDeUso.getTiposAnexoPorTipo("t1").isEmpty());
    }

    @Test
    void getTiposAnexoPorTipoInexistenteLanzaEntidadNoExiste() {
        when(tipoSolicitudGateway.existePorUuid("no-existe")).thenReturn(false);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getTiposAnexoPorTipo("no-existe"));
        verifyNoInteractions(gateway);
    }
}
