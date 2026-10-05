package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.output.SituacionAcademicaAsignaturaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SituacionAcademicaAsignatura;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SituacionAcademicaAsignaturaCUImplAdaptadorTest {

    @Mock
    private SituacionAcademicaAsignaturaGatewayIntPuerto gateway;

    private SituacionAcademicaAsignaturaCUImplAdaptador casoDeUso;

    @BeforeEach
    void setUp() {
        casoDeUso = new SituacionAcademicaAsignaturaCUImplAdaptador(gateway);
    }

    @Test
    void getSituacionesDevuelveLasDelGateway() {
        List<SituacionAcademicaAsignatura> situaciones = List.of(
                SituacionAcademicaAsignatura.builder().uuidSituacionAcademica("s1").codigo("R0").nombre("Cursada por primera vez").build());
        when(gateway.getTodas()).thenReturn(situaciones);

        assertSame(situaciones, casoDeUso.getSituaciones());
    }

    @Test
    void getSituacionesSinRegistrosDevuelveListaVacia() {
        when(gateway.getTodas()).thenReturn(List.of());

        assertTrue(casoDeUso.getSituaciones().isEmpty());
    }
}
