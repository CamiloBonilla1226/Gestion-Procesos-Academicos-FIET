package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudAcademica;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TipoSolicitudAcademicaCUImplAdaptadorTest {

    @Mock
    private TipoSolicitudAcademicaGatewayIntPuerto gateway;

    private TipoSolicitudAcademicaCUImplAdaptador casoDeUso;

    @BeforeEach
    void setUp() {
        casoDeUso = new TipoSolicitudAcademicaCUImplAdaptador(gateway);
    }

    @Test
    void getTiposSolicitudAcademicaDevuelveLosDelGateway() {
        List<TipoSolicitudAcademica> tipos = List.of(
                TipoSolicitudAcademica.builder().uuidTipoSolicitudAcademica("t1").nombre("Cancelación de Asignatura").build());
        when(gateway.getTodos()).thenReturn(tipos);

        assertSame(tipos, casoDeUso.getTiposSolicitudAcademica());
    }

    @Test
    void getTiposSolicitudAcademicaSinRegistrosDevuelveListaVacia() {
        when(gateway.getTodos()).thenReturn(List.of());

        assertTrue(casoDeUso.getTiposSolicitudAcademica().isEmpty());
    }
}
