package com.unicauca.cfiet.solicitudes.infraestructura.output.pdf;

import com.unicauca.cfiet.solicitudes.dominio.modelos.DatosFormatoOficial;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FilaFormatoOficial;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoProcesoAcademico;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ComposicionFormatoOficialImplAdaptadorTest {
    private final ComposicionFormatoOficialImplAdaptador composicion = new ComposicionFormatoOficialImplAdaptador();

    private DatosFormatoOficial asignatura(int filas, boolean conDecision) {
        return DatosFormatoOficial.builder()
                .proceso(TipoProcesoAcademico.CANCELACION_ASIGNATURA)
                .radicado("2026-CA-0007")
                .fecha(LocalDate.of(2026, 11, 26))
                .nombre("Ana María Muñoz Peña")
                .codigo("104618021")
                .programa("Ingeniería de Sistemas")
                .facultad("Ingeniería Electrónica y Telecomunicaciones")
                .semestre("6")
                .cedula("1061700000")
                .correo("anamunoz@unicauca.edu.co")
                .motivo("Problemas de salud que impiden asistir a clase durante el resto del periodo académico")
                .asignaturas(IntStream.range(0, filas).mapToObj(i -> FilaFormatoOficial.builder()
                        .codigo("PIS-00" + i).nombre("Asignatura número " + i).faltas(String.valueOf(i)).nota("3.5")
                        .situacionMatricula("(R1) Cursada por primera vez").situacionCancelar(i == 0 ? "Negada" : "No cursada").build()).toList())
                .datosFacultad(conDecision)
                .revisionAcademica(conDecision ? "Laura Gómez" : null)
                .decisionDecano(conDecision ? Boolean.TRUE : null)
                .build();
    }

    private String texto(byte[] pdf) throws IOException {
        try (PDDocument documento = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(documento);
        }
    }

    @Test
    void elFor84ConSeisAsignaturasLlevaLaHojaDeContinuacion() throws IOException {
        DatosFormatoOficial datos = asignatura(6, false);

        byte[] generado = composicion.generar(datos);

        assertEquals(2, composicion.contarPaginas(generado));
        assertEquals(2, composicion.paginasMinimas(datos));
        String texto = texto(generado);
        assertTrue(texto.contains("Hoja de continuación"));
        assertTrue(texto.contains("PIS-005"));
        assertTrue(texto.contains("Radicado 2026-CA-0007"));
        assertFalse(texto.contains("Laura Gómez"));
    }

    @Test
    void conCincoAsignaturasNoHayHojaDeContinuacion() {
        DatosFormatoOficial datos = asignatura(5, false);

        assertEquals(1, composicion.contarPaginas(composicion.generar(datos)));
        assertEquals(1, composicion.paginasMinimas(datos));
    }

    @Test
    void elCompuestoEstampaLaFacultadYLaDecisionSobreElEscaneoSinCambiarSusPaginas() throws IOException {
        DatosFormatoOficial datos = asignatura(6, true);
        byte[] escaneo = composicion.generar(asignatura(6, false));

        byte[] compuesto = composicion.componer(escaneo, datos, 2.5, -1);

        assertEquals(2, composicion.contarPaginas(compuesto));
        String texto = texto(compuesto);
        assertTrue(texto.contains("Laura Gómez"));
        assertTrue(texto.contains("Negada"));
        assertTrue(texto.contains("X"));
        Path destino = Path.of("target", "formatos-prueba");
        Files.createDirectories(destino);
        Files.write(destino.resolve("FOR-84-compuesto.pdf"), compuesto);
    }

    @Test
    void elFor83YElFor23SeGeneranConSusPaginas() throws IOException {
        DatosFormatoOficial matricula = asignatura(2, true);
        matricula.setProceso(TipoProcesoAcademico.CANCELACION_MATRICULA);
        byte[] for83 = composicion.componer(null, matricula, 0, 0);
        assertEquals(2, composicion.contarPaginas(for83));

        DatosFormatoOficial supletorio = asignatura(0, true);
        supletorio.setProceso(TipoProcesoAcademico.EXAMEN_SUPLETORIO);
        supletorio.setAsignatura("Señales y Sistemas");
        supletorio.setCodigoAsignatura("PIS-004");
        supletorio.setGrupoAsignatura("A");
        supletorio.setFechaExamen("20/11/2026");
        supletorio.setTelefono("3001234567");
        supletorio.setCruceAsignatura("Cálculo III");
        supletorio.setCruceCodigo("PIS-010");
        supletorio.setCruceFecha("20/11/2026");
        supletorio.setCruceHora("14:00");
        byte[] for23 = composicion.componer(null, supletorio, 0, 0);
        assertEquals(1, composicion.contarPaginas(for23));
        assertTrue(texto(for23).contains("Señales y Sistemas"));
        Path destino = Path.of("target", "formatos-prueba");
        Files.createDirectories(destino);
        Files.write(destino.resolve("FOR-83.pdf"), for83);
        Files.write(destino.resolve("FOR-23.pdf"), for23);
    }

    @Test
    void unArchivoQueNoEsPdfNoTienePaginas() {
        assertEquals(-1, composicion.contarPaginas("no es un pdf".getBytes()));
        assertEquals(-1, composicion.contarPaginas(null));
    }
}
