package com.unicauca.cfiet.solicitudes.dominio.servicios;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class ReglasAsignaturaTest {

    @ParameterizedTest
    @CsvSource({"ISI, 1, ISI-001", "IET, 42, IET-042", "TTE, 999, TTE-999", "IAC, 1000, IAC-1000", "IAI, 12345, IAI-12345"})
    void generaElCodigoConSiglaGuionYConsecutivoDeTresDigitosMinimo(String sigla, int consecutivo, String esperado) {
        assertEquals(esperado, ReglasAsignatura.generarCodigo(sigla, consecutivo));
    }

    @Test
    void normalizaElNombreQuitandoEspaciosSobrantes() {
        assertEquals("Calculo Integral I", ReglasAsignatura.normalizarNombre("  Calculo \t Integral   I "));
        assertEquals("", ReglasAsignatura.normalizarNombre(null));
    }

    @Test
    void laClaveDelNombreNoDistingueMayusculasNiEspacios() {
        assertEquals(ReglasAsignatura.claveNombre("Cálculo Integral"), ReglasAsignatura.claveNombre("  CÁLCULO   integral "));
        assertNotEquals(ReglasAsignatura.claveNombre("Calculo I"), ReglasAsignatura.claveNombre("Calculo II"));
    }

    @Test
    void normalizaElCodigoEnMayusculasYSinEspacios() {
        assertEquals("ISI-001", ReglasAsignatura.normalizarCodigo(" isi-001 "));
        assertEquals("", ReglasAsignatura.normalizarCodigo(null));
    }
}
