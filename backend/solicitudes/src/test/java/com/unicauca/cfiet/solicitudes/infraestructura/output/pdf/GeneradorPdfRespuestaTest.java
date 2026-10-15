package com.unicauca.cfiet.solicitudes.infraestructura.output.pdf;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaResumenRespuesta;
import com.unicauca.cfiet.solicitudes.dominio.modelos.DatoResumenRespuesta;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ResumenRespuesta;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeneradorPdfRespuestaTest {

    private final GeneradorPdfRespuesta generador = new GeneradorPdfRespuesta();

    private String texto(byte[] pdf) throws Exception {
        PdfReader lector = new PdfReader(pdf);
        try {
            PdfTextExtractor extractor = new PdfTextExtractor(lector);
            StringBuilder texto = new StringBuilder();
            for (int pagina = 1; pagina <= lector.getNumberOfPages(); pagina++)
                texto.append(extractor.getTextFromPage(pagina));
            return sinEspacios(texto.toString());
        } finally {
            lector.close();
        }
    }

    private String sinEspacios(String texto) {
        return texto.replaceAll("\\s+", "");
    }

    private void contiene(String texto, String... esperados) {
        for (String esperado : esperados)
            assertTrue(texto.contains(sinEspacios(esperado)), "falta: " + esperado + " en " + texto);
    }

    private ResumenRespuesta.ResumenRespuestaBuilder base(String tipo, String estado) {
        return ResumenRespuesta.builder()
                .radicado("2026-CA-0007")
                .tipoSolicitud(tipo)
                .fechaRespuesta(LocalDate.of(2026, 10, 15))
                .estudiante("Ana María Muñoz Peña")
                .codigoEstudiantil("104618021")
                .programa("Ingeniería Electrónica y Telecomunicaciones")
                .estadoFinal(estado);
    }

    @Test
    void laCancelacionDeAsignaturaLlevaLosDatosGeneralesYLaTablaConAcentosYEnie() throws Exception {
        ResumenRespuesta resumen = base("Cancelación de Asignatura", "Aprobada")
                .asignaturas(new ArrayList<>(List.of(
                        AsignaturaResumenRespuesta.builder().codigo("IET-001").nombre("Cálculo Diferencial").decision("Aprobada")
                                .situacionAlCancelar("Bajo rendimiento académico").build(),
                        AsignaturaResumenRespuesta.builder().codigo("IET-002").nombre("Diseño Lógico").decision("Rechazada")
                                .situacionAlCancelar("No aplica").build())))
                .build();

        byte[] pdf = generador.generar(resumen);

        assertEquals("%PDF", new String(pdf, 0, 4));
        contiene(texto(pdf), "Respuesta de solicitud académica", "Radicado", "2026-CA-0007", "Tipo de solicitud",
                "Cancelación de Asignatura", "Fecha de la respuesta", "15 de octubre de 2026", "Ana María Muñoz Peña", "Código estudiantil",
                "104618021", "Ingeniería Electrónica y Telecomunicaciones", "Estado final", "Aprobada", "Asignaturas", "Situación académica al cancelar",
                "IET-001", "Cálculo Diferencial", "Bajo rendimiento académico", "IET-002", "Diseño Lógico", "Rechazada", "No aplica");
    }

    @Test
    void losDetallesDelProcesoSeAgreganYLosVaciosSalenComoGuion() throws Exception {
        ResumenRespuesta resumen = base("Examen Supletorio", "Rechazada")
                .programa(null)
                .detalles(new ArrayList<>(List.of(
                        DatoResumenRespuesta.builder().etiqueta("Asignatura del examen").valor("ISI-004 - Señales y Sistemas").build(),
                        DatoResumenRespuesta.builder().etiqueta("Decisión sobre el comprobante").valor("Rechazado").build(),
                        DatoResumenRespuesta.builder().etiqueta("Profesor asignado").valor("Sin asignar").build())))
                .build();

        String texto = texto(generador.generar(resumen));

        contiene(texto, "Asignatura del examen", "ISI-004 - Señales y Sistemas", "Decisión sobre el comprobante", "Rechazado",
                "Profesor asignado", "Sin asignar", "Programa - Estado final");
        assertFalse(texto.contains(sinEspacios("Situación académica al cancelar")));
    }

    @Test
    void noIncluyeObservacionesNiHistorial() throws Exception {
        String texto = texto(generador.generar(base("Cancelación de Matrícula", "Rechazada")
                .detalles(new ArrayList<>(List.of(DatoResumenRespuesta.builder().etiqueta("Decisión de la solicitud").valor("Rechazada").build())))
                .build()));

        contiene(texto, "Decisión de la solicitud");
        assertFalse(texto.toLowerCase().contains("observaci"));
        assertFalse(texto.toLowerCase().contains("historial"));
    }
}
