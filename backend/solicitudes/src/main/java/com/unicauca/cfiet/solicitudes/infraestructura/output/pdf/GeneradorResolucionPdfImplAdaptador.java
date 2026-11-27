package com.unicauca.cfiet.solicitudes.infraestructura.output.pdf;

import com.unicauca.cfiet.solicitudes.aplicacion.output.GeneradorResolucionIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.BorradorResolucion;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FilaResolucion;
import com.unicauca.cfiet.solicitudes.dominio.servicios.RedaccionResolucion;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.LayerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GeneradorResolucionPdfImplAdaptador implements GeneradorResolucionIntPuerto {
    private static final String MEMBRETE = "formatos/membrete-fiet.pdf";
    private static final float IZQUIERDA = 68f;
    private static final float DERECHA = 544f;
    private static final float NUMERAL_X = 50f;
    private static final float CITA_X = 82f;
    private static final float ARRIBA = 846f;
    private static final float ABAJO = 108f;
    private static final float TAMANO = 10.5f;
    private static final float INTERLINEA = 12.7f;
    private static final float TAMANO_TABLA = 9f;
    private static final float INTERLINEA_TABLA = 10.6f;
    private static final float TAMANO_PIE = 7f;
    private static final float TABLA_X = 58f;
    private static final float TABLA_ANCHO = 496f;
    private static final float RELLENO = 3f;
    private static final float[] COLUMNAS = {0.25f, 0.10f, 0.10f, 0.19f, 0.14f, 0.22f};
    private static final String[] ENCABEZADOS = {"Asignaturas Solicitadas para Cancelar", "No. Faltas", "Nota",
            "Estado académico de la asignatura al momento de la solicitud", "Estado de la cancelación",
            "Estado académico de la asignatura cuando la matricule de nuevo"};
    private static final Pattern NUMERAL = Pattern.compile("^(\\d+\\.)\\s+(.*)$", Pattern.DOTALL);
    private static final Pattern PARRAFOS = Pattern.compile("\\n\\s*\\n");

    private final PDType1Font normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private final PDType1Font negrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private final PDType1Font cursiva = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
    private final PDType1Font negritaCursiva = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD_OBLIQUE);
    private final Map<Character, Boolean> codificables = new ConcurrentHashMap<>();
    private volatile byte[] membrete;

    private enum Alineacion { IZQUIERDA, CENTRO, JUSTIFICADO }

    private record Parte(String texto, boolean negrita) {
    }

    private record Palabra(List<Parte> partes) {
    }

    @Override
    public byte[] generar(BorradorResolucion borrador) {
        try (PDDocument fondo = Loader.loadPDF(membrete()); PDDocument documento = new PDDocument()) {
            PDFormXObject hoja = new LayerUtility(documento).importPageAsForm(fondo, 0);
            Lienzo lienzo = new Lienzo(documento, hoja, fondo.getPage(0).getMediaBox());
            escribir(lienzo, borrador);
            lienzo.cerrar();
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            documento.save(salida);
            return salida.toByteArray();
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }

    private void escribir(Lienzo lienzo, BorradorResolucion borrador) throws IOException {
        String numero = texto(borrador.getNumero()).isEmpty() ? "____" : texto(borrador.getNumero());
        int anio = borrador.getFecha() == null ? java.time.LocalDate.now().getYear() : borrador.getFecha().getYear();
        lienzo.parrafo("**RESOLUCIÓN No. " + texto(borrador.getPrefijo()) + numero + " DE " + anio + "**", IZQUIERDA, DERECHA, Alineacion.CENTRO, false, TAMANO, null);
        lienzo.parrafo("(" + RedaccionResolucion.fechaCorta(borrador.getFecha()) + ")", IZQUIERDA, DERECHA, Alineacion.CENTRO, false, TAMANO, null);
        lienzo.blanco();
        lienzo.parrafo(texto(borrador.getAsunto()), IZQUIERDA, DERECHA, Alineacion.CENTRO, false, TAMANO, null);
        lienzo.blanco();
        bloques(lienzo, borrador.getPreambulo());
        lienzo.parrafo("**C O N S I D E R A N D O:**", IZQUIERDA, DERECHA, Alineacion.CENTRO, false, TAMANO, null);
        lienzo.blanco();
        bloques(lienzo, borrador.getConsiderandos());
        lienzo.parrafo("En mérito de lo expuesto,", IZQUIERDA, DERECHA, Alineacion.IZQUIERDA, false, TAMANO, null);
        lienzo.parrafo("**R E S U E L V E:**", IZQUIERDA, DERECHA, Alineacion.CENTRO, false, TAMANO, null);
        lienzo.blanco();
        bloques(lienzo, borrador.getArticuloPrimero());
        if (borrador.getFilas() != null && !borrador.getFilas().isEmpty()) {
            tabla(lienzo, borrador.getFilas());
            lienzo.blanco();
        }
        bloques(lienzo, borrador.getArticulosSiguientes());
        lienzo.parrafo(RedaccionResolucion.expedicion(borrador.getFecha()), IZQUIERDA, DERECHA, Alineacion.JUSTIFICADO, false, TAMANO, null);
        lienzo.blanco();
        lienzo.espacio(INTERLINEA * 7);
        lienzo.parrafo("**NOTIFÍQUESE Y CÚMPLASE**", IZQUIERDA, DERECHA, Alineacion.CENTRO, false, TAMANO, null);
        lienzo.blanco();
        lienzo.blanco();
        lienzo.blanco();
        lienzo.parrafo(texto(borrador.getFirmante()), IZQUIERDA, DERECHA, Alineacion.CENTRO, false, TAMANO, null);
        lienzo.parrafo(texto(borrador.getCargoFirmante()), IZQUIERDA, DERECHA, Alineacion.CENTRO, false, TAMANO, null);
        lienzo.blanco();
        lienzo.parrafo("Diligencia de notificación", IZQUIERDA, DERECHA, Alineacion.IZQUIERDA, false, TAMANO, null);
        lienzo.blanco();
        bloques(lienzo, borrador.getDiligencia());
        if (!texto(borrador.getProyecto()).isEmpty())
            lienzo.parrafo("Proyectó: " + texto(borrador.getProyecto()), IZQUIERDA, DERECHA, Alineacion.IZQUIERDA, true, TAMANO_PIE, null);
        if (!texto(borrador.getReviso()).isEmpty())
            lienzo.parrafo("Revisó: " + texto(borrador.getReviso()), IZQUIERDA, DERECHA, Alineacion.IZQUIERDA, true, TAMANO_PIE, null);
        lienzo.blanco();
        lienzo.parrafo("Tramitado en DARCA por: ____________________________ en la Fecha_______________", IZQUIERDA, DERECHA,
                Alineacion.IZQUIERDA, false, TAMANO, null);
    }

    private void bloques(Lienzo lienzo, String contenido) throws IOException {
        String limpio = texto(contenido).replace("\r", "");
        if (limpio.isEmpty()) return;
        for (String bloque : PARRAFOS.split(limpio)) {
            String parrafo = bloque.strip();
            if (parrafo.isEmpty()) continue;
            if (parrafo.startsWith(">")) {
                lienzo.parrafo(parrafo.substring(1).strip(), CITA_X, DERECHA, Alineacion.JUSTIFICADO, true, TAMANO, null);
            } else {
                Matcher numeral = NUMERAL.matcher(parrafo);
                if (numeral.matches())
                    lienzo.parrafo(numeral.group(2), IZQUIERDA, DERECHA, Alineacion.JUSTIFICADO, false, TAMANO, numeral.group(1));
                else
                    lienzo.parrafo(parrafo, IZQUIERDA, DERECHA, Alineacion.JUSTIFICADO, false, TAMANO, null);
            }
            lienzo.blanco();
        }
    }

    private void tabla(Lienzo lienzo, List<FilaResolucion> filas) throws IOException {
        List<String[]> contenido = new ArrayList<>();
        for (FilaResolucion fila : filas)
            contenido.add(new String[]{"**" + texto(fila.getAsignatura()) + "**", texto(fila.getFaltas()), texto(fila.getNota()),
                    texto(fila.getEstadoAlSolicitar()), texto(fila.getEstadoCancelacion()), texto(fila.getEstadoNuevaMatricula())});
        String[] encabezado = new String[ENCABEZADOS.length];
        for (int i = 0; i < ENCABEZADOS.length; i++)
            encabezado[i] = "**" + ENCABEZADOS[i] + "**";
        float altoEncabezado = lienzo.altoFila(encabezado);
        lienzo.espacio(altoEncabezado + lienzo.altoFila(contenido.isEmpty() ? encabezado : contenido.get(0)));
        lienzo.fila(encabezado, altoEncabezado);
        for (String[] fila : contenido) {
            float alto = lienzo.altoFila(fila);
            if (lienzo.y - alto < ABAJO) {
                lienzo.nuevaPagina();
                lienzo.fila(encabezado, altoEncabezado);
            }
            lienzo.fila(fila, alto);
        }
    }

    private List<Palabra> palabras(String contenido) {
        List<Palabra> palabras = new ArrayList<>();
        List<Parte> partes = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean enNegrita = false;
        String limpio = seguro(contenido);
        for (int i = 0; i < limpio.length(); i++) {
            char c = limpio.charAt(i);
            if (c == '*' && i + 1 < limpio.length() && limpio.charAt(i + 1) == '*') {
                if (!actual.isEmpty()) partes.add(new Parte(actual.toString(), enNegrita));
                actual.setLength(0);
                enNegrita = !enNegrita;
                i++;
            } else if (c == ' ') {
                if (!actual.isEmpty()) partes.add(new Parte(actual.toString(), enNegrita));
                actual.setLength(0);
                if (!partes.isEmpty()) palabras.add(new Palabra(partes));
                partes = new ArrayList<>();
            } else {
                actual.append(c);
            }
        }
        if (!actual.isEmpty()) partes.add(new Parte(actual.toString(), enNegrita));
        if (!partes.isEmpty()) palabras.add(new Palabra(partes));
        return palabras;
    }

    private String seguro(String contenido) {
        StringBuilder limpio = new StringBuilder();
        for (char c : contenido.toCharArray()) {
            if (c == '\t') limpio.append(' ');
            else if (c == '\r') continue;
            else limpio.append(codificable(c) ? c : '?');
        }
        return limpio.toString();
    }

    private boolean codificable(char c) {
        return codificables.computeIfAbsent(c, caracter -> {
            try {
                normal.encode(String.valueOf(caracter));
                return true;
            } catch (IllegalArgumentException | IOException error) {
                return false;
            }
        });
    }

    private PDType1Font fuente(boolean conNegrita, boolean conCursiva) {
        if (conNegrita) return conCursiva ? negritaCursiva : negrita;
        return conCursiva ? cursiva : normal;
    }

    private float ancho(Palabra palabra, boolean conCursiva, float tamano) throws IOException {
        float total = 0;
        for (Parte parte : palabra.partes())
            total += fuente(parte.negrita(), conCursiva).getStringWidth(parte.texto()) / 1000f * tamano;
        return total;
    }

    private List<List<Palabra>> envolver(List<Palabra> palabras, float ancho, boolean conCursiva, float tamano) throws IOException {
        List<List<Palabra>> lineas = new ArrayList<>();
        List<Palabra> linea = new ArrayList<>();
        float espacio = normal.getStringWidth(" ") / 1000f * tamano;
        float usado = 0;
        for (Palabra palabra : palabras) {
            float medida = ancho(palabra, conCursiva, tamano);
            float necesario = linea.isEmpty() ? medida : usado + espacio + medida;
            if (!linea.isEmpty() && necesario > ancho) {
                lineas.add(linea);
                linea = new ArrayList<>();
                necesario = medida;
            }
            linea.add(palabra);
            usado = necesario;
        }
        if (!linea.isEmpty()) lineas.add(linea);
        return lineas;
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private byte[] membrete() throws IOException {
        byte[] cargado = membrete;
        if (cargado == null) {
            try (InputStream entrada = new ClassPathResource(MEMBRETE).getInputStream()) {
                cargado = entrada.readAllBytes();
            }
            membrete = cargado;
        }
        return cargado;
    }

    private final class Lienzo {
        private final PDDocument documento;
        private final PDFormXObject hoja;
        private final PDRectangle tamanoPagina;
        private PDPageContentStream trazo;
        private float y;

        private Lienzo(PDDocument documento, PDFormXObject hoja, PDRectangle tamanoPagina) throws IOException {
            this.documento = documento;
            this.hoja = hoja;
            this.tamanoPagina = tamanoPagina;
            nuevaPagina();
        }

        private void nuevaPagina() throws IOException {
            if (trazo != null) trazo.close();
            PDPage pagina = new PDPage(new PDRectangle(tamanoPagina.getWidth(), tamanoPagina.getHeight()));
            documento.addPage(pagina);
            trazo = new PDPageContentStream(documento, pagina);
            trazo.drawForm(hoja);
            y = ARRIBA;
        }

        private void cerrar() throws IOException {
            if (trazo != null) trazo.close();
            trazo = null;
        }

        private void blanco() {
            if (y < ARRIBA) y -= INTERLINEA;
        }

        private void espacio(float alto) throws IOException {
            if (y - alto < ABAJO) nuevaPagina();
        }

        private void parrafo(String contenido, float x, float derecha, Alineacion alineacion, boolean conCursiva, float tamano,
                             String numeral) throws IOException {
            float interlinea = tamano == TAMANO ? INTERLINEA : tamano * 1.25f;
            String[] renglones = texto(contenido).replace("\r", "").split("\n");
            boolean primera = true;
            for (String renglon : renglones) {
                List<List<Palabra>> lineas = envolver(palabras(renglon), derecha - x, conCursiva, tamano);
                if (lineas.isEmpty()) lineas = List.of(List.of());
                for (int i = 0; i < lineas.size(); i++) {
                    espacio(interlinea);
                    y -= interlinea;
                    if (primera && numeral != null)
                        escribirParte(new Parte(seguro(numeral), false), NUMERAL_X, conCursiva, tamano);
                    primera = false;
                    boolean ultima = i == lineas.size() - 1;
                    Alineacion efectiva = alineacion == Alineacion.JUSTIFICADO && ultima ? Alineacion.IZQUIERDA : alineacion;
                    linea(lineas.get(i), x, derecha - x, efectiva, conCursiva, tamano);
                }
            }
        }

        private void linea(List<Palabra> palabras, float x, float ancho, Alineacion alineacion, boolean conCursiva, float tamano) throws IOException {
            if (palabras.isEmpty()) return;
            float espacio = normal.getStringWidth(" ") / 1000f * tamano;
            float medidas = 0;
            for (Palabra palabra : palabras) medidas += ancho(palabra, conCursiva, tamano);
            float hueco = espacio;
            if (alineacion == Alineacion.JUSTIFICADO && palabras.size() > 1)
                hueco = (ancho - medidas) / (palabras.size() - 1);
            float total = medidas + hueco * (palabras.size() - 1);
            float cursor = alineacion == Alineacion.CENTRO ? x + (ancho - total) / 2 : x;
            for (Palabra palabra : palabras) {
                for (Parte parte : palabra.partes())
                    cursor += escribirParte(parte, cursor, conCursiva, tamano);
                cursor += hueco;
            }
        }

        private float escribirParte(Parte parte, float x, boolean conCursiva, float tamano) throws IOException {
            PDType1Font fuente = fuente(parte.negrita(), conCursiva);
            trazo.beginText();
            trazo.setFont(fuente, tamano);
            trazo.newLineAtOffset(x, y);
            trazo.showText(parte.texto());
            trazo.endText();
            return fuente.getStringWidth(parte.texto()) / 1000f * tamano;
        }

        private float altoFila(String[] celdas) throws IOException {
            int maximo = 1;
            for (int i = 0; i < celdas.length; i++) {
                float ancho = TABLA_ANCHO * COLUMNAS[i] - RELLENO * 2;
                maximo = Math.max(maximo, envolver(palabras(celdas[i]), ancho, false, TAMANO_TABLA).size());
            }
            return maximo * INTERLINEA_TABLA + RELLENO * 2 + 2;
        }

        private void fila(String[] celdas, float alto) throws IOException {
            float x = TABLA_X;
            float superior = y;
            trazo.setLineWidth(0.6f);
            for (int i = 0; i < celdas.length; i++) {
                float ancho = TABLA_ANCHO * COLUMNAS[i];
                trazo.addRect(x, superior - alto, ancho, alto);
                trazo.stroke();
                List<List<Palabra>> lineas = envolver(palabras(celdas[i]), ancho - RELLENO * 2, false, TAMANO_TABLA);
                y = superior - RELLENO;
                for (List<Palabra> renglon : lineas) {
                    y -= INTERLINEA_TABLA;
                    linea(renglon, x + RELLENO, ancho - RELLENO * 2, Alineacion.CENTRO, false, TAMANO_TABLA);
                }
                x += ancho;
            }
            y = superior - alto;
        }
    }
}
