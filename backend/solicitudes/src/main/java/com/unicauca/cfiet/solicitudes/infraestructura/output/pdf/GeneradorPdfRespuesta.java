package com.unicauca.cfiet.solicitudes.infraestructura.output.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.unicauca.cfiet.solicitudes.aplicacion.output.GeneradorPdfRespuestaIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaResumenRespuesta;
import com.unicauca.cfiet.solicitudes.dominio.modelos.DatoResumenRespuesta;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ResumenRespuesta;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class GeneradorPdfRespuesta implements GeneradorPdfRespuestaIntPuerto {
    private static final String TITULO = "Respuesta de solicitud académica";
    private static final String FACULTAD = "Facultad de Ingeniería Electrónica y Telecomunicaciones - Universidad del Cauca";
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-CO"));
    private static final Font FUENTE_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15);
    private static final Font FUENTE_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font FUENTE_ETIQUETA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font FUENTE_TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font FUENTE_NOTA = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Color FONDO_ENCABEZADO = new Color(230, 230, 230);
    private static final String SIN_DATO = "-";

    @Override
    public byte[] generar(ResumenRespuesta resumen) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.LETTER, 50, 50, 50, 50);
        try {
            PdfWriter.getInstance(documento, salida);
            documento.open();
            documento.add(new Paragraph(TITULO, FUENTE_TITULO));
            documento.add(new Paragraph(FACULTAD, FUENTE_NOTA));
            documento.add(espacio());
            documento.add(tablaDatos(datosGenerales(resumen)));
            if (!resumen.getAsignaturas().isEmpty()) {
                documento.add(espacio());
                documento.add(new Paragraph("Asignaturas", FUENTE_SUBTITULO));
                documento.add(espacio());
                documento.add(tablaAsignaturas(resumen.getAsignaturas()));
            }
            documento.close();
        } catch (DocumentException error) {
            throw new IllegalStateException("No se pudo generar el PDF de la respuesta " + resumen.getRadicado(), error);
        }
        return salida.toByteArray();
    }

    private List<DatoResumenRespuesta> datosGenerales(ResumenRespuesta resumen) {
        List<DatoResumenRespuesta> datos = new ArrayList<>(List.of(
                dato("Radicado", resumen.getRadicado()),
                dato("Tipo de solicitud", resumen.getTipoSolicitud()),
                dato("Fecha de la respuesta", resumen.getFechaRespuesta() == null ? null : resumen.getFechaRespuesta().format(FECHA)),
                dato("Estudiante", resumen.getEstudiante()),
                dato("Código estudiantil", resumen.getCodigoEstudiantil()),
                dato("Programa", resumen.getPrograma()),
                dato("Estado final", resumen.getEstadoFinal())));
        datos.addAll(resumen.getDetalles());
        return datos;
    }

    private PdfPTable tablaDatos(List<DatoResumenRespuesta> datos) throws DocumentException {
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{1.2f, 2.8f});
        for (DatoResumenRespuesta dato : datos) {
            tabla.addCell(celda(dato.getEtiqueta(), FUENTE_ETIQUETA, true));
            tabla.addCell(celda(dato.getValor(), FUENTE_TEXTO, false));
        }
        return tabla;
    }

    private PdfPTable tablaAsignaturas(List<AsignaturaResumenRespuesta> asignaturas) throws DocumentException {
        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{1f, 2.4f, 1f, 1.8f});
        tabla.setHeaderRows(1);
        for (String encabezado : List.of("Código", "Asignatura", "Decisión", "Situación académica al cancelar"))
            tabla.addCell(celda(encabezado, FUENTE_ETIQUETA, true));
        for (AsignaturaResumenRespuesta asignatura : asignaturas) {
            tabla.addCell(celda(asignatura.getCodigo(), FUENTE_TEXTO, false));
            tabla.addCell(celda(asignatura.getNombre(), FUENTE_TEXTO, false));
            tabla.addCell(celda(asignatura.getDecision(), FUENTE_TEXTO, false));
            tabla.addCell(celda(asignatura.getSituacionAlCancelar(), FUENTE_TEXTO, false));
        }
        return tabla;
    }

    private PdfPCell celda(String texto, Font fuente, boolean resaltada) {
        PdfPCell celda = new PdfPCell(new Phrase(texto == null || texto.isBlank() ? SIN_DATO : texto, fuente));
        celda.setPadding(5);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        if (resaltada)
            celda.setBackgroundColor(FONDO_ENCABEZADO);
        return celda;
    }

    private DatoResumenRespuesta dato(String etiqueta, String valor) {
        return DatoResumenRespuesta.builder().etiqueta(etiqueta).valor(valor).build();
    }

    private Paragraph espacio() {
        return new Paragraph(" ", FUENTE_NOTA);
    }
}
