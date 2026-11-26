package com.unicauca.cfiet.solicitudes.infraestructura.output.pdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ComposicionFormatoOficialIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.DatosFormatoOficial;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FilaFormatoOficial;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoProcesoAcademico;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.LayerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.util.Matrix;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ComposicionFormatoOficialImplAdaptador implements ComposicionFormatoOficialIntPuerto {
    private static final Map<TipoProcesoAcademico, String> CODIGOS = Map.of(
            TipoProcesoAcademico.CANCELACION_MATRICULA, "PA-GA-4.2-FOR-14",
            TipoProcesoAcademico.CANCELACION_ASIGNATURA, "PA-GA-4.2-FOR-9",
            TipoProcesoAcademico.EXAMEN_SUPLETORIO, "PM-FO-4-FOR-23");
    private static final String FASE_ESTUDIANTE = "estudiante";
    private static final String FASE_FACULTAD = "facultad";
    private static final String FASE_DECANO = "decano";
    private static final String MARCA = "marca";
    private static final float TAMANO_MINIMO = 6f;
    private static final float INTERLINEA = 1.1f;
    private static final float MARGEN_CAJA = 1.5f;
    private static final double PUNTOS_POR_MM = 72d / 25.4d;
    private static final float ALTO_FILA_CONTINUACION = 24f;
    private static final float ALTO_ENCABEZADO_CONTINUACION = 28f;
    private static final float INICIO_TABLA_CONTINUACION = 110f;
    private static final float ESPACIO_FIRMA_CONTINUACION = 130f;
    private static final String[] ENCABEZADOS = {"Código", "Asignatura", "No. Faltas", "Nota", "En la Matrícula", "Al Cancelar"};
    private static final String[] COLUMNAS = {"asignatura[0].codigo", "asignatura[0].nombre", "facultad[0].faltas", "facultad[0].nota",
            "facultad[0].matricula", "facultad[0].cancelar"};
    private static final String[] CLAVES_FILA = {"asignatura[%d].codigo", "asignatura[%d].nombre", "facultad[%d].faltas", "facultad[%d].nota",
            "facultad[%d].matricula", "facultad[%d].cancelar"};

    private final ObjectMapper lector = new ObjectMapper();
    private final Map<TipoProcesoAcademico, Plantilla> plantillas = new ConcurrentHashMap<>();
    private final PDType1Font fuente = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private final PDType1Font fuenteNegrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    @Override
    public byte[] generar(DatosFormatoOficial datos) {
        Plantilla plantilla = plantilla(datos.getProceso());
        try (PDDocument documento = Loader.loadPDF(plantilla.pdf())) {
            int extra = filasExtra(plantilla, datos);
            if (extra > 0)
                dibujarContinuacion(documento, plantilla, datos, extra);
            Map<String, String> valores = valoresEstudiante(datos);
            for (Campo campo : plantilla.campos())
                if (FASE_ESTUDIANTE.equals(campo.fase()))
                    estampar(documento, campo, valores, 0, 0);
            for (Campo campo : camposContinuacion(plantilla, documento, extra, FASE_ESTUDIANTE))
                estampar(documento, campo, valores, 0, 0);
            return guardar(documento);
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }

    @Override
    public byte[] componer(byte[] escaneo, DatosFormatoOficial datos, double ajusteXmm, double ajusteYmm) {
        Plantilla plantilla = plantilla(datos.getProceso());
        byte[] base = escaneo == null ? generar(datos) : null;
        try (PDDocument documento = escaneo == null ? Loader.loadPDF(base) : normalizar(escaneo, plantilla)) {
            float dx = (float) (ajusteXmm * PUNTOS_POR_MM);
            float dy = (float) (ajusteYmm * PUNTOS_POR_MM);
            Map<String, String> valores = valoresFacultad(datos);
            int extra = filasExtra(plantilla, datos);
            for (Campo campo : plantilla.campos())
                if (!FASE_ESTUDIANTE.equals(campo.fase()))
                    estampar(documento, campo, valores, dx, dy);
            for (Campo campo : camposContinuacion(plantilla, documento, extra, FASE_FACULTAD))
                estampar(documento, campo, valores, dx, dy);
            return guardar(documento);
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }

    @Override
    public int paginasMinimas(DatosFormatoOficial datos) {
        Plantilla plantilla = plantilla(datos.getProceso());
        return plantilla.ultimaPaginaEstudiante() + (filasExtra(plantilla, datos) > 0 ? 1 : 0);
    }

    @Override
    public int contarPaginas(byte[] pdf) {
        if (pdf == null) return -1;
        try (PDDocument documento = Loader.loadPDF(pdf)) {
            return documento.getNumberOfPages();
        } catch (IOException error) {
            return -1;
        }
    }

    private PDDocument normalizar(byte[] escaneo, Plantilla plantilla) throws IOException {
        PDRectangle tamano;
        try (PDDocument base = Loader.loadPDF(plantilla.pdf())) {
            tamano = base.getPage(0).getMediaBox();
        }
        PDDocument destino = new PDDocument();
        try (PDDocument origen = Loader.loadPDF(escaneo)) {
            LayerUtility capas = new LayerUtility(destino);
            for (int i = 0; i < origen.getNumberOfPages(); i++) {
                PDFormXObject forma = capas.importPageAsForm(origen, i);
                PDRectangle caja = forma.getBBox();
                boolean girada = origen.getPage(i).getRotation() % 180 != 0;
                float ancho = girada ? caja.getHeight() : caja.getWidth();
                float alto = girada ? caja.getWidth() : caja.getHeight();
                PDPage pagina = new PDPage(new PDRectangle(tamano.getWidth(), tamano.getHeight()));
                destino.addPage(pagina);
                try (PDPageContentStream contenido = new PDPageContentStream(destino, pagina)) {
                    contenido.saveGraphicsState();
                    contenido.transform(Matrix.getScaleInstance(tamano.getWidth() / ancho, tamano.getHeight() / alto));
                    contenido.drawForm(forma);
                    contenido.restoreGraphicsState();
                }
            }
        } catch (IOException | RuntimeException error) {
            destino.close();
            throw error;
        }
        return destino;
    }

    private int filasExtra(Plantilla plantilla, DatosFormatoOficial datos) {
        if (plantilla.filasAsignaturas() == 0) return 0;
        return Math.min(Math.max(0, datos.getAsignaturas().size() - plantilla.filasAsignaturas()), filasQueCaben(plantilla));
    }

    private int filasQueCaben(Plantilla plantilla) {
        float disponible = plantilla.alto() - INICIO_TABLA_CONTINUACION - ALTO_ENCABEZADO_CONTINUACION - ESPACIO_FIRMA_CONTINUACION;
        return (int) (disponible / ALTO_FILA_CONTINUACION);
    }

    private void dibujarContinuacion(PDDocument documento, Plantilla plantilla, DatosFormatoOficial datos, int extra) throws IOException {
        PDPage pagina = new PDPage(new PDRectangle(plantilla.ancho(), plantilla.alto()));
        documento.addPage(pagina);
        List<float[]> columnas = columnas(plantilla);
        float izquierda = columnas.get(0)[0];
        float derecha = columnas.get(columnas.size() - 1)[1];
        float arriba = plantilla.alto() - INICIO_TABLA_CONTINUACION;
        float abajo = arriba - ALTO_ENCABEZADO_CONTINUACION - extra * ALTO_FILA_CONTINUACION;
        try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina)) {
            escribir(contenido, fuenteNegrita, "Hoja de continuación - " + plantilla.titulo(),
                    new Caja(izquierda, plantilla.alto() - 80, derecha - izquierda, 14), 11, false);
            escribir(contenido, fuente, "Radicado: " + texto(datos.getRadicado()) + "     Estudiante: " + texto(datos.getNombre()),
                    new Caja(izquierda, plantilla.alto() - 98, derecha - izquierda, 12), 9, false);
            contenido.setLineWidth(0.7f);
            contenido.addRect(izquierda, abajo, derecha - izquierda, arriba - abajo);
            float y = arriba - ALTO_ENCABEZADO_CONTINUACION;
            contenido.moveTo(izquierda, y);
            contenido.lineTo(derecha, y);
            for (int i = 1; i < extra; i++) {
                contenido.moveTo(izquierda, y - i * ALTO_FILA_CONTINUACION);
                contenido.lineTo(derecha, y - i * ALTO_FILA_CONTINUACION);
            }
            for (int i = 1; i < columnas.size(); i++) {
                contenido.moveTo(columnas.get(i)[0], arriba);
                contenido.lineTo(columnas.get(i)[0], abajo);
            }
            float firma = abajo - 70;
            contenido.moveTo(izquierda, firma);
            contenido.lineTo(izquierda + 170, firma);
            contenido.stroke();
            for (int i = 0; i < columnas.size(); i++)
                escribir(contenido, fuenteNegrita, ENCABEZADOS[i], new Caja(columnas.get(i)[0] + 1, y + 1, columnas.get(i)[1] - columnas.get(i)[0] - 2,
                        ALTO_ENCABEZADO_CONTINUACION - 2), 8, false);
            escribir(contenido, fuente, "FIRMA DEL ESTUDIANTE", new Caja(izquierda, firma - 13, 170, 11), 9, false);
            escribir(contenido, fuente, "NOMBRE: " + texto(datos.getNombre()), new Caja(izquierda, firma - 26, 300, 11), 9, false);
        }
    }

    private List<float[]> columnas(Plantilla plantilla) {
        List<float[]> columnas = new ArrayList<>();
        for (String clave : COLUMNAS) {
            Campo campo = plantilla.campos().stream().filter(c -> c.clave().equals(clave)).findFirst().orElseThrow();
            columnas.add(new float[]{campo.x() - 1, campo.x() + campo.ancho() + 1});
        }
        return columnas;
    }

    private List<Campo> camposContinuacion(Plantilla plantilla, PDDocument documento, int extra, String fase) {
        if (extra == 0) return List.of();
        List<float[]> columnas = columnas(plantilla);
        int pagina = documento.getNumberOfPages();
        float primera = plantilla.alto() - INICIO_TABLA_CONTINUACION - ALTO_ENCABEZADO_CONTINUACION;
        List<Campo> campos = new ArrayList<>();
        for (int fila = 0; fila < extra; fila++) {
            int indice = plantilla.filasAsignaturas() + fila;
            float y = primera - (fila + 1) * ALTO_FILA_CONTINUACION;
            for (int columna = 0; columna < CLAVES_FILA.length; columna++) {
                String faseColumna = columna < 2 ? FASE_ESTUDIANTE : FASE_FACULTAD;
                if (!faseColumna.equals(fase)) continue;
                float[] limites = columnas.get(columna);
                campos.add(new Campo(String.format(CLAVES_FILA[columna], indice), pagina, limites[0] + 1, y + 0.5f,
                        limites[1] - limites[0] - 2, ALTO_FILA_CONTINUACION - 1, 9, "texto", "izquierda", faseColumna));
            }
        }
        return campos;
    }

    private Map<String, String> valoresEstudiante(DatosFormatoOficial datos) {
        Map<String, String> valores = new HashMap<>();
        if (datos.getFecha() != null) {
            valores.put("fecha.dia", String.valueOf(datos.getFecha().getDayOfMonth()));
            valores.put("fecha.mes", String.format("%02d", datos.getFecha().getMonthValue()));
            valores.put("fecha.anio", String.valueOf(datos.getFecha().getYear()));
        }
        valores.put("radicado", datos.getRadicado());
        valores.put("radicadoMargen", "Radicado " + texto(datos.getRadicado()));
        valores.put("nombre", datos.getNombre());
        valores.put("estudiante", datos.getNombre());
        valores.put("nombreFirma", datos.getNombre());
        valores.put("codigo", datos.getCodigo());
        valores.put("programa", datos.getPrograma());
        valores.put("facultad", datos.getFacultad());
        valores.put("semestre", datos.getSemestre());
        valores.put("cedula", datos.getCedula());
        valores.put("correo", datos.getCorreo());
        valores.put("telefono", datos.getTelefono());
        valores.put("motivo", datos.getMotivo());
        valores.put("asignatura", datos.getAsignatura());
        valores.put("asignatura.codigo", datos.getCodigoAsignatura());
        valores.put("asignatura.grupo", datos.getGrupoAsignatura());
        valores.put("fechaExamen", datos.getFechaExamen());
        valores.put("documentos", datos.getDocumentos());
        valores.put("cruce.asignatura", datos.getCruceAsignatura());
        valores.put("cruce.codigo", datos.getCruceCodigo());
        valores.put("cruce.grupo", datos.getCruceGrupo());
        valores.put("cruce.fecha", datos.getCruceFecha());
        valores.put("cruce.hora", datos.getCruceHora());
        for (int i = 0; i < datos.getAsignaturas().size(); i++) {
            FilaFormatoOficial fila = datos.getAsignaturas().get(i);
            valores.put("asignatura[" + i + "].codigo", fila.getCodigo());
            valores.put("asignatura[" + i + "].nombre", fila.getNombre());
        }
        return valores;
    }

    private Map<String, String> valoresFacultad(DatosFormatoOficial datos) {
        Map<String, String> valores = new HashMap<>();
        if (datos.isDatosFacultad()) {
            valores.put("revisionAcademica", datos.getRevisionAcademica());
            for (int i = 0; i < datos.getAsignaturas().size(); i++) {
                FilaFormatoOficial fila = datos.getAsignaturas().get(i);
                valores.put("facultad[" + i + "].faltas", fila.getFaltas());
                valores.put("facultad[" + i + "].nota", fila.getNota());
                valores.put("facultad[" + i + "].matricula", fila.getSituacionMatricula());
                valores.put("facultad[" + i + "].cancelar", fila.getSituacionCancelar());
            }
        }
        Boolean decision = datos.getDecisionDecano();
        if (decision != null) {
            valores.put(decision ? "decano.aprobadoSi" : "decano.aprobadoNo", "X");
            valores.put(decision ? "decano.aprobado" : "decano.negado", "X");
            valores.put("autorizacion.asignatura", datos.getAsignatura());
            valores.put("autorizacion.grupo", datos.getGrupoAsignatura());
            valores.put("autorizacion.estudiante", datos.getNombre());
            valores.put("autorizacion.codigo", datos.getCodigo());
        }
        return valores;
    }

    private void estampar(PDDocument documento, Campo campo, Map<String, String> valores, float dx, float dy) throws IOException {
        String valor = valores.get(campo.clave());
        if (valor == null || valor.isBlank()) return;
        List<Integer> paginas = new ArrayList<>();
        if (campo.pagina() == 0)
            for (int i = 0; i < documento.getNumberOfPages(); i++) paginas.add(i);
        else if (campo.pagina() <= documento.getNumberOfPages())
            paginas.add(campo.pagina() - 1);
        Caja caja = new Caja(campo.x() + dx, campo.y() + dy, campo.ancho(), campo.alto());
        for (int indice : paginas) {
            PDPage pagina = documento.getPage(indice);
            try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina, PDPageContentStream.AppendMode.APPEND, true, true)) {
                if (MARCA.equals(campo.tipo()))
                    marcar(contenido, caja);
                else
                    escribir(contenido, fuente, valor.trim(), caja, campo.tamanoFuente(), "derecha".equals(campo.alinear()));
            }
        }
    }

    private void marcar(PDPageContentStream contenido, Caja caja) throws IOException {
        float tamano = Math.min(caja.alto() * 1.1f, 10f);
        float ancho = fuenteNegrita.getStringWidth("X") / 1000 * tamano;
        contenido.beginText();
        contenido.setFont(fuenteNegrita, tamano);
        contenido.newLineAtOffset(caja.x() + (caja.ancho() - ancho) / 2, caja.y() + (caja.alto() - tamano * 0.72f) / 2);
        contenido.showText("X");
        contenido.endText();
    }

    private void escribir(PDPageContentStream contenido, PDType1Font tipo, String texto, Caja caja, float tamanoInicial, boolean derecha)
            throws IOException {
        String limpio = compatible(tipo, texto);
        float anchoUtil = caja.ancho() - 2 * MARGEN_CAJA;
        float tamano = tamanoInicial;
        List<String> lineas = partir(tipo, limpio, tamano, anchoUtil);
        while (lineas.size() * tamano * INTERLINEA > caja.alto() + 0.5f && tamano > TAMANO_MINIMO) {
            tamano = Math.max(TAMANO_MINIMO, tamano - 0.5f);
            lineas = partir(tipo, limpio, tamano, anchoUtil);
        }
        int maximo = Math.max(1, (int) ((caja.alto() + 0.5f) / (tamano * INTERLINEA)));
        if (lineas.size() > maximo) {
            lineas = new ArrayList<>(lineas.subList(0, maximo));
            lineas.set(maximo - 1, recortar(tipo, lineas.get(maximo - 1), tamano, anchoUtil));
        }
        float altoBloque = lineas.size() * tamano * INTERLINEA;
        float tope = caja.y() + (caja.alto() + altoBloque) / 2;
        contenido.setFont(tipo, tamano);
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i);
            float ancho = tipo.getStringWidth(linea) / 1000 * tamano;
            float x = derecha ? caja.x() + caja.ancho() - MARGEN_CAJA - ancho : caja.x() + MARGEN_CAJA;
            float base = tope - i * tamano * INTERLINEA - tamano * 0.85f;
            contenido.beginText();
            contenido.newLineAtOffset(x, base);
            contenido.showText(linea);
            contenido.endText();
        }
    }

    private List<String> partir(PDType1Font tipo, String texto, float tamano, float ancho) throws IOException {
        List<String> lineas = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (String palabra : texto.split("\\s+")) {
            String candidata = actual.isEmpty() ? palabra : actual + " " + palabra;
            if (medida(tipo, candidata, tamano) <= ancho) {
                actual = new StringBuilder(candidata);
                continue;
            }
            if (!actual.isEmpty()) lineas.add(actual.toString());
            actual = new StringBuilder();
            for (char caracter : palabra.toCharArray()) {
                if (medida(tipo, actual.toString() + caracter, tamano) > ancho && !actual.isEmpty()) {
                    lineas.add(actual.toString());
                    actual = new StringBuilder();
                }
                actual.append(caracter);
            }
        }
        if (!actual.isEmpty()) lineas.add(actual.toString());
        return lineas.isEmpty() ? List.of("") : lineas;
    }

    private String recortar(PDType1Font tipo, String linea, float tamano, float ancho) throws IOException {
        String recortada = linea;
        while (!recortada.isEmpty() && medida(tipo, recortada + "…", tamano) > ancho)
            recortada = recortada.substring(0, recortada.length() - 1);
        return recortada.stripTrailing() + "…";
    }

    private float medida(PDType1Font tipo, String texto, float tamano) throws IOException {
        return tipo.getStringWidth(texto) / 1000 * tamano;
    }

    private String compatible(PDType1Font tipo, String texto) {
        StringBuilder limpio = new StringBuilder();
        for (int i = 0; i < texto.length(); ) {
            int codigo = texto.codePointAt(i);
            String caracter = new String(Character.toChars(codigo));
            i += Character.charCount(codigo);
            if (Character.isWhitespace(codigo)) {
                limpio.append(' ');
                continue;
            }
            try {
                tipo.encode(caracter);
                limpio.append(caracter);
            } catch (IOException | IllegalArgumentException error) {
                limpio.append('?');
            }
        }
        return limpio.toString();
    }

    private String texto(String valor) {
        return valor == null ? "" : valor;
    }

    private byte[] guardar(PDDocument documento) throws IOException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        documento.save(salida);
        return salida.toByteArray();
    }

    private Plantilla plantilla(TipoProcesoAcademico proceso) {
        return plantillas.computeIfAbsent(proceso, this::cargar);
    }

    private Plantilla cargar(TipoProcesoAcademico proceso) {
        String codigo = CODIGOS.get(proceso);
        try (InputStream pdf = new ClassPathResource("formatos/" + codigo + ".pdf").getInputStream();
             InputStream mapa = new ClassPathResource("formatos/" + codigo + ".campos.json").getInputStream()) {
            byte[] contenido = pdf.readAllBytes();
            MapaCampos campos = lector.readValue(mapa, MapaCampos.class);
            PDRectangle tamano;
            try (PDDocument documento = Loader.loadPDF(contenido)) {
                tamano = documento.getPage(0).getMediaBox();
            }
            return new Plantilla(campos.titulo(), campos.ultimaPaginaEstudiante(), campos.filasAsignaturas(), campos.campos(), contenido,
                    tamano.getWidth(), tamano.getHeight());
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }

    private record Caja(float x, float y, float ancho, float alto) {
    }

    private record Plantilla(String titulo, int ultimaPaginaEstudiante, int filasAsignaturas, List<Campo> campos, byte[] pdf,
                             float ancho, float alto) {
    }

    private record MapaCampos(String codigo, String titulo, int paginas, int ultimaPaginaEstudiante, int filasAsignaturas,
                              List<Campo> campos) {
    }

    private record Campo(String clave, int pagina, float x, float y, float ancho, float alto, float tamanoFuente, String tipo,
                         String alinear, String fase) {
    }
}
