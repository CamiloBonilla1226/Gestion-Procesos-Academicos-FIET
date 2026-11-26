import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class InspeccionarCampos {

    private static final PrintStream SALIDA = new PrintStream(System.out, true, StandardCharsets.UTF_8);

    public static void main(String[] args) throws IOException {
        try (PDDocument documento = Loader.loadPDF(new File(args[0]))) {
            for (int i = 0; i < documento.getNumberOfPages(); i++) {
                PDRectangle caja = documento.getPage(i).getMediaBox();
                SALIDA.printf("PAGINA %d %.1f x %.1f%n", i + 1, caja.getWidth(), caja.getHeight());
            }
            listarCampos(documento);
            listarPalabras(documento);
            for (int i = 0; i < documento.getNumberOfPages(); i++) {
                listarTrazos(documento.getPage(i), i + 1);
            }
        }
    }

    private static void listarCampos(PDDocument documento) throws IOException {
        PDAcroForm formulario = documento.getDocumentCatalog().getAcroForm();
        if (formulario == null) {
            SALIDA.println("SIN ACROFORM");
            return;
        }
        for (PDField campo : formulario.getFieldTree()) {
            for (PDAnnotationWidget widget : campo.getWidgets()) {
                PDRectangle r = widget.getRectangle();
                int pagina = documento.getPages().indexOf(widget.getPage()) + 1;
                SALIDA.printf("CAMPO %s tipo=%s pag=%d x=%.1f y=%.1f w=%.1f h=%.1f%n", campo.getFullyQualifiedName(),
                        campo.getFieldType(), pagina, r.getLowerLeftX(), r.getLowerLeftY(), r.getWidth(), r.getHeight());
            }
        }
    }

    private static void listarPalabras(PDDocument documento) throws IOException {
        PDFTextStripper lector = new PDFTextStripper() {
            @Override
            protected void writeString(String texto, List<TextPosition> posiciones) {
                float alto = getCurrentPage().getMediaBox().getHeight();
                List<TextPosition> palabra = new ArrayList<>();
                for (TextPosition posicion : posiciones) {
                    if (posicion.getUnicode().isBlank()) {
                        imprimir(palabra, alto);
                        palabra.clear();
                    } else {
                        palabra.add(posicion);
                    }
                }
                imprimir(palabra, alto);
            }

            private void imprimir(List<TextPosition> palabra, float alto) {
                if (palabra.isEmpty()) return;
                StringBuilder texto = new StringBuilder();
                palabra.forEach(p -> texto.append(p.getUnicode()));
                TextPosition primera = palabra.get(0);
                TextPosition ultima = palabra.get(palabra.size() - 1);
                SALIDA.printf("PALABRA pag=%d x=%.1f base=%.1f x2=%.1f t=%.1f [%s]%n", getCurrentPageNo(), primera.getXDirAdj(),
                        alto - primera.getYDirAdj(), ultima.getXDirAdj() + ultima.getWidthDirAdj(), primera.getFontSizeInPt(), texto);
            }
        };
        lector.setSortByPosition(true);
        lector.getText(documento);
    }

    private static void listarTrazos(PDPage pagina, int numero) throws IOException {
        new PDFGraphicsStreamEngine(pagina) {
            private final List<Point2D> puntos = new ArrayList<>();

            @Override
            public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) {
                double x = Math.min(p0.getX(), p2.getX());
                double y = Math.min(p0.getY(), p2.getY());
                SALIDA.printf("RECT pag=%d x=%.1f y=%.1f w=%.1f h=%.1f%n", numero, x, y, Math.abs(p2.getX() - p0.getX()),
                        Math.abs(p2.getY() - p0.getY()));
            }

            @Override
            public void moveTo(float x, float y) {
                puntos.clear();
                puntos.add(new Point2D.Float(x, y));
            }

            @Override
            public void lineTo(float x, float y) {
                Point2D desde = puntos.isEmpty() ? new Point2D.Float(x, y) : puntos.get(puntos.size() - 1);
                SALIDA.printf("LINEA pag=%d x1=%.1f y1=%.1f x2=%.1f y2=%.1f%n", numero, desde.getX(), desde.getY(), x, y);
                puntos.add(new Point2D.Float(x, y));
            }

            @Override
            public void drawImage(PDImage imagen) {
            }

            @Override
            public void clip(int regla) {
            }

            @Override
            public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) {
                puntos.add(new Point2D.Float(x3, y3));
            }

            @Override
            public Point2D getCurrentPoint() {
                return puntos.isEmpty() ? new Point2D.Float() : puntos.get(puntos.size() - 1);
            }

            @Override
            public void closePath() {
            }

            @Override
            public void endPath() {
                puntos.clear();
            }

            @Override
            public void strokePath() {
                puntos.clear();
            }

            @Override
            public void fillPath(int regla) {
                puntos.clear();
            }

            @Override
            public void fillAndStrokePath(int regla) {
                puntos.clear();
            }

            @Override
            public void shadingFill(COSName nombre) {
            }
        }.processPage(pagina);
    }
}
