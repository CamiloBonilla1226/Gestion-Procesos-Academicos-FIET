package com.unicauca.cfiet.solicitudes.infraestructura.output.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

final class AlineacionEscaneo {
    private static final float ESCALA_GRUESA = 0.5f;
    private static final float ESCALA_FINA = 1.5f;
    private static final float ANCHO_ANCLA = 48f;
    private static final float ALTO_ANCLA = 32f;
    private static final float PASO_ANCLA = 12f;
    private static final int COLUMNAS_ANCLA = 4;
    private static final int FILAS_ANCLA = 6;
    private static final double TEXTURA_MINIMA = 0.12;
    private static final int ANCLAS_SUFICIENTES = 14;
    private static final float MARGEN_GRUESO = 45f;
    private static final float MARGEN_FINO = 7f;
    private static final double CORRELACION_MINIMA = 0.45;
    private static final double DIFERENCIA_PICO = 0.03;
    private static final double TOLERANCIA_GRUESA = 10;
    private static final double TOLERANCIA_FINA = 3;
    private static final int ITERACIONES = 400;
    private static final int INLIERS_HOMOGRAFIA = 8;
    private static final int INLIERS_MINIMOS = 4;
    private static final double AREA_MINIMA_HOJA = 0.25;
    private static final double ANGULO_MAXIMO = 35;
    private static final double LADO_MINIMO = 0.4;
    private static final double LADO_MAXIMO = 1.8;
    private static final double DISTANCIA_CANDIDATOS = 12;

    private AlineacionEscaneo() {
    }

    static Homografia alinear(PDDocument escaneo, int paginaEscaneo, PDDocument referencia, int paginaReferencia, float ancho, float alto)
            throws IOException {
        Gris referenciaGruesa = gris(referencia, paginaReferencia, ESCALA_GRUESA);
        Gris referenciaFina = gris(referencia, paginaReferencia, ESCALA_FINA);
        Gris escaneoGrueso = gris(escaneo, paginaEscaneo, ESCALA_GRUESA);
        Gris escaneoFino = gris(escaneo, paginaEscaneo, ESCALA_FINA);
        List<Ancla> anclas = anclas(referenciaFina, alto);
        if (anclas.size() < INLIERS_MINIMOS) return null;

        Resultado mejor = null;
        for (Homografia candidata : candidatas(escaneoGrueso, ancho, alto)) {
            Resultado resultado = ajustar(anclas, referenciaGruesa, escaneoGrueso, candidata, MARGEN_GRUESO, TOLERANCIA_GRUESA, alto);
            if (resultado != null && (mejor == null || resultado.inliers() > mejor.inliers()
                    || (resultado.inliers() == mejor.inliers() && resultado.correlacion() > mejor.correlacion())))
                mejor = resultado;
        }
        if (mejor == null || !geometriaValida(mejor.homografia(), ancho, alto)) return null;
        Resultado fino = ajustar(anclas, referenciaFina, escaneoFino, mejor.homografia(), MARGEN_FINO, TOLERANCIA_FINA, alto);
        if (fino != null && fino.inliers() >= INLIERS_MINIMOS && geometriaValida(fino.homografia(), ancho, alto)) {
            Resultado refinado = ajustar(anclas, referenciaFina, escaneoFino, fino.homografia(), MARGEN_FINO / 2, TOLERANCIA_FINA, alto);
            if (refinado != null && refinado.inliers() >= fino.inliers() && geometriaValida(refinado.homografia(), ancho, alto))
                return refinado.homografia();
            return fino.homografia();
        }
        return mejor.homografia();
    }

    private static List<Homografia> candidatas(Gris escaneo, float ancho, float alto) {
        double[][] pagina = {{0, alto}, {ancho, alto}, {ancho, 0}, {0, 0}};
        List<double[][]> cuadros = new ArrayList<>();
        cuadros.add(pagina);
        double[][] hoja = hoja(escaneo, alto);
        if (hoja != null && distanciaMaxima(hoja, pagina) > DISTANCIA_CANDIDATOS) cuadros.add(hoja);
        List<Homografia> candidatas = new ArrayList<>();
        for (double[][] cuadro : cuadros)
            for (int giro = 0; giro < 4; giro++) {
                double[][] destino = new double[4][];
                for (int j = 0; j < 4; j++) destino[j] = cuadro[(giro + j) % 4];
                Homografia homografia = Homografia.entre(pagina, destino);
                if (homografia != null) candidatas.add(homografia);
            }
        return candidatas;
    }

    private static double distanciaMaxima(double[][] a, double[][] b) {
        double maxima = 0;
        for (int i = 0; i < 4; i++) maxima = Math.max(maxima, Math.hypot(a[i][0] - b[i][0], a[i][1] - b[i][1]));
        return maxima;
    }

    private static double[][] hoja(Gris imagen, float alto) {
        int ancho = imagen.ancho();
        int altoImagen = imagen.alto();
        float[] suave = suavizar(imagen);
        int umbral = otsu(suave);
        boolean[] clara = new boolean[suave.length];
        for (int i = 0; i < suave.length; i++) clara[i] = suave[i] >= umbral;
        boolean[] visto = new boolean[suave.length];
        int[] cola = new int[suave.length];
        int mejorTamano = 0;
        int[] mejorComponente = null;
        for (int inicio = 0; inicio < clara.length; inicio++) {
            if (!clara[inicio] || visto[inicio]) continue;
            int cabeza = 0, fin = 0;
            cola[fin++] = inicio;
            visto[inicio] = true;
            while (cabeza < fin) {
                int actual = cola[cabeza++];
                int x = actual % ancho, y = actual / ancho;
                if (x > 0) fin = visitar(clara, visto, cola, fin, actual - 1);
                if (x < ancho - 1) fin = visitar(clara, visto, cola, fin, actual + 1);
                if (y > 0) fin = visitar(clara, visto, cola, fin, actual - ancho);
                if (y < altoImagen - 1) fin = visitar(clara, visto, cola, fin, actual + ancho);
            }
            if (fin > mejorTamano) {
                mejorTamano = fin;
                mejorComponente = Arrays.copyOf(cola, fin);
            }
        }
        if (mejorComponente == null || mejorTamano < AREA_MINIMA_HOJA * suave.length) return null;
        double[] supIzq = null, supDer = null, infDer = null, infIzq = null;
        double minSuma = Double.MAX_VALUE, maxSuma = -Double.MAX_VALUE, minResta = Double.MAX_VALUE, maxResta = -Double.MAX_VALUE;
        for (int indice : mejorComponente) {
            double x = indice % ancho, y = indice / ancho;
            double suma = x + y, resta = x - y;
            if (suma < minSuma) { minSuma = suma; supIzq = new double[]{x, y}; }
            if (suma > maxSuma) { maxSuma = suma; infDer = new double[]{x, y}; }
            if (resta > maxResta) { maxResta = resta; supDer = new double[]{x, y}; }
            if (resta < minResta) { minResta = resta; infIzq = new double[]{x, y}; }
        }
        double[][] cuadro = {aPuntos(supIzq, imagen.escala(), alto), aPuntos(supDer, imagen.escala(), alto),
                aPuntos(infDer, imagen.escala(), alto), aPuntos(infIzq, imagen.escala(), alto)};
        for (int i = 0; i < 4; i++)
            if (!anguloAceptable(cuadro[(i + 3) % 4], cuadro[i], cuadro[(i + 1) % 4])) return null;
        return cuadro;
    }

    private static double[] aPuntos(double[] pixel, float escala, float alto) {
        return new double[]{(pixel[0] + 0.5) / escala, alto - (pixel[1] + 0.5) / escala};
    }

    private static Resultado ajustar(List<Ancla> anclas, Gris referencia, Gris escaneo, Homografia inicial, float margen,
                                     double tolerancia, float alto) {
        List<double[]> origen = new ArrayList<>();
        List<double[]> destino = new ArrayList<>();
        List<Double> correlaciones = new ArrayList<>();
        for (Ancla ancla : anclas) {
            Coincidencia coincidencia = buscar(ancla, referencia, escaneo, inicial, margen, alto);
            if (coincidencia == null) continue;
            origen.add(coincidencia.plantilla());
            destino.add(coincidencia.escaneo());
            correlaciones.add(coincidencia.correlacion());
        }
        if (origen.size() < INLIERS_MINIMOS) return null;
        boolean[] internos = ransac(origen, destino, tolerancia);
        List<double[]> origenInterno = new ArrayList<>();
        List<double[]> destinoInterno = new ArrayList<>();
        double suma = 0;
        for (int i = 0; i < origen.size(); i++)
            if (internos[i]) {
                origenInterno.add(origen.get(i));
                destinoInterno.add(destino.get(i));
                suma += correlaciones.get(i);
            }
        if (origenInterno.size() < INLIERS_MINIMOS) return null;
        Homografia homografia = Homografia.minimosCuadrados(origenInterno, destinoInterno, origenInterno.size() < INLIERS_HOMOGRAFIA);
        if (homografia == null) return null;
        return new Resultado(homografia, origenInterno.size(), suma / origenInterno.size());
    }

    private static boolean[] ransac(List<double[]> origen, List<double[]> destino, double tolerancia) {
        int n = origen.size();
        Random azar = new Random(20261127L);
        boolean[] mejores = new boolean[n];
        int mejorCantidad = 0;
        boolean afin = n < INLIERS_HOMOGRAFIA;
        int muestra = afin ? 3 : 4;
        for (int iteracion = 0; iteracion < ITERACIONES; iteracion++) {
            List<double[]> o = new ArrayList<>();
            List<double[]> d = new ArrayList<>();
            List<Integer> elegidos = new ArrayList<>();
            while (elegidos.size() < muestra) {
                int indice = azar.nextInt(n);
                if (!elegidos.contains(indice)) elegidos.add(indice);
            }
            for (int indice : elegidos) {
                o.add(origen.get(indice));
                d.add(destino.get(indice));
            }
            Homografia modelo = Homografia.minimosCuadrados(o, d, afin);
            if (modelo == null) continue;
            boolean[] internos = new boolean[n];
            int cantidad = 0;
            for (int i = 0; i < n; i++) {
                double[] proyectado = modelo.aplicar(origen.get(i)[0], origen.get(i)[1]);
                if (Math.hypot(proyectado[0] - destino.get(i)[0], proyectado[1] - destino.get(i)[1]) <= tolerancia) {
                    internos[i] = true;
                    cantidad++;
                }
            }
            if (cantidad > mejorCantidad) {
                mejorCantidad = cantidad;
                mejores = internos;
            }
        }
        return mejores;
    }

    private static Coincidencia buscar(Ancla ancla, Gris referencia, Gris escaneo, Homografia homografia, float margen, float alto) {
        float escala = referencia.escala();
        int x0 = Math.round(ancla.x() * escala);
        int y0 = Math.round((alto - ancla.y() - ALTO_ANCLA) * escala);
        int anchoParche = Math.round(ANCHO_ANCLA * escala);
        int altoParche = Math.round(ALTO_ANCLA * escala);
        int m = Math.max(2, Math.round(margen * escala));
        if (x0 < 0 || y0 < 0 || x0 + anchoParche > referencia.ancho() || y0 + altoParche > referencia.alto()) return null;
        double[] parche = new double[anchoParche * altoParche];
        double media = 0;
        for (int v = 0; v < altoParche; v++)
            for (int u = 0; u < anchoParche; u++) {
                double valor = referencia.valores()[(y0 + v) * referencia.ancho() + x0 + u];
                parche[v * anchoParche + u] = valor;
                media += valor;
            }
        media /= parche.length;
        double norma = 0;
        for (int i = 0; i < parche.length; i++) {
            parche[i] -= media;
            norma += parche[i] * parche[i];
        }
        if (norma < 1e-6) return null;
        norma = Math.sqrt(norma);

        int anchoVentana = anchoParche + 2 * m;
        int altoVentana = altoParche + 2 * m;
        double[] ventana = new double[anchoVentana * altoVentana];
        for (int v = 0; v < altoVentana; v++)
            for (int u = 0; u < anchoVentana; u++) {
                double tx = (x0 - m + u + 0.5) / escala;
                double ty = alto - (y0 - m + v + 0.5) / escala;
                double[] punto = homografia.aplicar(tx, ty);
                ventana[v * anchoVentana + u] = muestrear(escaneo, punto[0] * escaneo.escala() - 0.5, (alto - punto[1]) * escaneo.escala() - 0.5);
            }
        double[] integral = new double[(anchoVentana + 1) * (altoVentana + 1)];
        double[] integralCuadrada = new double[(anchoVentana + 1) * (altoVentana + 1)];
        for (int v = 0; v < altoVentana; v++)
            for (int u = 0; u < anchoVentana; u++) {
                double valor = ventana[v * anchoVentana + u];
                int indice = (v + 1) * (anchoVentana + 1) + u + 1;
                integral[indice] = valor + integral[indice - 1] + integral[indice - anchoVentana - 1] - integral[indice - anchoVentana - 2];
                integralCuadrada[indice] = valor * valor + integralCuadrada[indice - 1] + integralCuadrada[indice - anchoVentana - 1]
                        - integralCuadrada[indice - anchoVentana - 2];
            }
        int lado = 2 * m + 1;
        double[] mapa = new double[lado * lado];
        int total = anchoParche * altoParche;
        double mejor = -2;
        int mejorX = 0, mejorY = 0;
        for (int dy = 0; dy < lado; dy++)
            for (int dx = 0; dx < lado; dx++) {
                double suma = rectangulo(integral, anchoVentana, dx, dy, anchoParche, altoParche);
                double sumaCuadrada = rectangulo(integralCuadrada, anchoVentana, dx, dy, anchoParche, altoParche);
                double varianza = sumaCuadrada - suma * suma / total;
                double correlacion = 0;
                if (varianza > 1e-6) {
                    double cruzada = 0;
                    for (int v = 0; v < altoParche; v++) {
                        int base = (dy + v) * anchoVentana + dx;
                        int fila = v * anchoParche;
                        for (int u = 0; u < anchoParche; u++) cruzada += parche[fila + u] * ventana[base + u];
                    }
                    correlacion = cruzada / (norma * Math.sqrt(varianza));
                }
                mapa[dy * lado + dx] = correlacion;
                if (correlacion > mejor) {
                    mejor = correlacion;
                    mejorX = dx;
                    mejorY = dy;
                }
            }
        if (mejor < CORRELACION_MINIMA) return null;
        int vecindad = Math.max(2, m / 4);
        double segundo = -2;
        for (int dy = 0; dy < lado; dy++)
            for (int dx = 0; dx < lado; dx++)
                if (Math.abs(dx - mejorX) > vecindad || Math.abs(dy - mejorY) > vecindad)
                    segundo = Math.max(segundo, mapa[dy * lado + dx]);
        if (mejor - segundo < DIFERENCIA_PICO) return null;
        double finoX = mejorX + subpixel(mapa, lado, mejorX, mejorY, true);
        double finoY = mejorY + subpixel(mapa, lado, mejorX, mejorY, false);
        double centroX = ancla.x() + ANCHO_ANCLA / 2;
        double centroY = ancla.y() + ALTO_ANCLA / 2;
        double encontradoX = centroX + (finoX - m) / escala;
        double encontradoY = centroY - (finoY - m) / escala;
        return new Coincidencia(new double[]{centroX, centroY}, homografia.aplicar(encontradoX, encontradoY), mejor);
    }

    private static double subpixel(double[] mapa, int lado, int x, int y, boolean horizontal) {
        if (horizontal ? (x == 0 || x == lado - 1) : (y == 0 || y == lado - 1)) return 0;
        double antes = horizontal ? mapa[y * lado + x - 1] : mapa[(y - 1) * lado + x];
        double centro = mapa[y * lado + x];
        double despues = horizontal ? mapa[y * lado + x + 1] : mapa[(y + 1) * lado + x];
        double denominador = antes - 2 * centro + despues;
        if (Math.abs(denominador) < 1e-9) return 0;
        return Math.max(-0.5, Math.min(0.5, 0.5 * (antes - despues) / denominador));
    }

    private static double rectangulo(double[] integral, int anchoVentana, int x, int y, int ancho, int alto) {
        int fila = anchoVentana + 1;
        return integral[(y + alto) * fila + x + ancho] - integral[y * fila + x + ancho] - integral[(y + alto) * fila + x] + integral[y * fila + x];
    }

    private static double muestrear(Gris imagen, double x, double y) {
        if (x < 0 || y < 0 || x > imagen.ancho() - 1 || y > imagen.alto() - 1) return 255;
        int x0 = (int) x, y0 = (int) y;
        int x1 = Math.min(x0 + 1, imagen.ancho() - 1), y1 = Math.min(y0 + 1, imagen.alto() - 1);
        double fx = x - x0, fy = y - y0;
        float[] v = imagen.valores();
        int w = imagen.ancho();
        return (v[y0 * w + x0] * (1 - fx) + v[y0 * w + x1] * fx) * (1 - fy) + (v[y1 * w + x0] * (1 - fx) + v[y1 * w + x1] * fx) * fy;
    }

    private static List<Ancla> anclas(Gris referencia, float alto) {
        float escala = referencia.escala();
        int anchoParche = Math.round(ANCHO_ANCLA * escala);
        int altoParche = Math.round(ALTO_ANCLA * escala);
        int w = referencia.ancho();
        int h = referencia.alto();
        double[] integralX = new double[(w + 1) * (h + 1)];
        double[] integralY = new double[(w + 1) * (h + 1)];
        float[] v = referencia.valores();
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                double gx = x > 0 && x < w - 1 ? v[y * w + x + 1] - v[y * w + x - 1] : 0;
                double gy = y > 0 && y < h - 1 ? v[(y + 1) * w + x] - v[(y - 1) * w + x] : 0;
                int indice = (y + 1) * (w + 1) + x + 1;
                integralX[indice] = gx * gx + integralX[indice - 1] + integralX[indice - w - 1] - integralX[indice - w - 2];
                integralY[indice] = gy * gy + integralY[indice - 1] + integralY[indice - w - 1] - integralY[indice - w - 2];
            }
        float anchoPagina = w / escala;
        float altoPagina = h / escala;
        List<double[]> candidatas = new ArrayList<>();
        double maximo = 0;
        for (float y = 0; y + ALTO_ANCLA <= altoPagina; y += PASO_ANCLA)
            for (float x = 0; x + ANCHO_ANCLA <= anchoPagina; x += PASO_ANCLA) {
                int px = Math.round(x * escala);
                int py = Math.round((altoPagina - y - ALTO_ANCLA) * escala);
                if (px + anchoParche > w || py + altoParche > h || py < 0) continue;
                double energiaX = rectangulo(integralX, w, px, py, anchoParche, altoParche);
                double energiaY = rectangulo(integralY, w, px, py, anchoParche, altoParche);
                double textura = Math.min(energiaX, energiaY) / (anchoParche * altoParche);
                maximo = Math.max(maximo, textura);
                candidatas.add(new double[]{x, y, textura});
            }
        List<Ancla> anclas = enGrilla(candidatas, maximo, anchoPagina, altoPagina, COLUMNAS_ANCLA, FILAS_ANCLA);
        if (anclas.size() >= ANCLAS_SUFICIENTES) return anclas;
        return enGrilla(candidatas, maximo, anchoPagina, altoPagina, 2 * COLUMNAS_ANCLA, 2 * FILAS_ANCLA);
    }

    private static List<Ancla> enGrilla(List<double[]> candidatas, double maximo, float anchoPagina, float altoPagina, int columnas, int filas) {
        double[][] mejores = new double[columnas * filas][];
        for (double[] candidata : candidatas) {
            int columna = Math.min(columnas - 1, (int) ((candidata[0] + ANCHO_ANCLA / 2) / anchoPagina * columnas));
            int fila = Math.min(filas - 1, (int) ((candidata[1] + ALTO_ANCLA / 2) / altoPagina * filas));
            int celda = fila * columnas + columna;
            if (mejores[celda] == null || candidata[2] > mejores[celda][2]) mejores[celda] = candidata;
        }
        List<Ancla> anclas = new ArrayList<>();
        for (double[] mejor : mejores)
            if (mejor != null && mejor[2] >= TEXTURA_MINIMA * maximo) anclas.add(new Ancla((float) mejor[0], (float) mejor[1]));
        return anclas;
    }

    private static boolean geometriaValida(Homografia homografia, float ancho, float alto) {
        double[][] pagina = {{0, alto}, {ancho, alto}, {ancho, 0}, {0, 0}};
        double[][] proyectado = new double[4][];
        for (int i = 0; i < 4; i++) proyectado[i] = homografia.aplicar(pagina[i][0], pagina[i][1]);
        double signo = 0;
        for (int i = 0; i < 4; i++) {
            double[] a = proyectado[i], b = proyectado[(i + 1) % 4], c = proyectado[(i + 2) % 4];
            double cruz = (b[0] - a[0]) * (c[1] - b[1]) - (b[1] - a[1]) * (c[0] - b[0]);
            if (signo != 0 && Math.signum(cruz) != signo) return false;
            signo = Math.signum(cruz);
            double proporcion = Math.hypot(b[0] - a[0], b[1] - a[1]) / Math.hypot(pagina[(i + 1) % 4][0] - pagina[i][0], pagina[(i + 1) % 4][1] - pagina[i][1]);
            if (proporcion < LADO_MINIMO || proporcion > LADO_MAXIMO) return false;
            if (!anguloAceptable(a, b, c)) return false;
        }
        return signo != 0;
    }

    private static boolean anguloAceptable(double[] a, double[] b, double[] c) {
        double[] u = {a[0] - b[0], a[1] - b[1]};
        double[] v = {c[0] - b[0], c[1] - b[1]};
        double largo = Math.hypot(u[0], u[1]) * Math.hypot(v[0], v[1]);
        if (largo < 1e-9) return false;
        double coseno = (u[0] * v[0] + u[1] * v[1]) / largo;
        double angulo = Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, coseno))));
        return Math.abs(angulo - 90) <= ANGULO_MAXIMO;
    }

    private static Gris gris(PDDocument documento, int pagina, float escala) throws IOException {
        BufferedImage render = new PDFRenderer(documento).renderImage(pagina, escala, ImageType.GRAY);
        int ancho = render.getWidth();
        int alto = render.getHeight();
        int[] muestras = render.getRaster().getSamples(0, 0, ancho, alto, 0, (int[]) null);
        float[] valores = new float[muestras.length];
        for (int i = 0; i < muestras.length; i++) valores[i] = muestras[i];
        return new Gris(ancho, alto, escala, valores);
    }

    private static float[] suavizar(Gris imagen) {
        int w = imagen.ancho(), h = imagen.alto();
        float[] v = imagen.valores();
        float[] salida = new float[v.length];
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                float suma = 0;
                int cuenta = 0;
                for (int dy = -2; dy <= 2; dy++)
                    for (int dx = -2; dx <= 2; dx++) {
                        int xx = x + dx, yy = y + dy;
                        if (xx < 0 || yy < 0 || xx >= w || yy >= h) continue;
                        suma += v[yy * w + xx];
                        cuenta++;
                    }
                salida[y * w + x] = suma / cuenta;
            }
        return salida;
    }

    private static int otsu(float[] grises) {
        long[] histograma = new long[256];
        for (float gris : grises) histograma[Math.max(0, Math.min(255, Math.round(gris)))]++;
        long total = grises.length;
        double suma = 0;
        for (int i = 0; i < 256; i++) suma += i * (double) histograma[i];
        double sumaFondo = 0;
        long pesoFondo = 0;
        double mejor = -1;
        int umbral = 128;
        for (int i = 0; i < 256; i++) {
            pesoFondo += histograma[i];
            if (pesoFondo == 0) continue;
            long pesoFrente = total - pesoFondo;
            if (pesoFrente == 0) break;
            sumaFondo += i * (double) histograma[i];
            double mediaFondo = sumaFondo / pesoFondo;
            double mediaFrente = (suma - sumaFondo) / pesoFrente;
            double varianza = (double) pesoFondo * pesoFrente * (mediaFondo - mediaFrente) * (mediaFondo - mediaFrente);
            if (varianza > mejor) {
                mejor = varianza;
                umbral = i + 1;
            }
        }
        return umbral;
    }

    private static int visitar(boolean[] mascara, boolean[] visto, int[] cola, int fin, int vecino) {
        if (visto[vecino] || !mascara[vecino]) return fin;
        visto[vecino] = true;
        cola[fin] = vecino;
        return fin + 1;
    }

    private record Gris(int ancho, int alto, float escala, float[] valores) {
    }

    private record Ancla(float x, float y) {
    }

    private record Coincidencia(double[] plantilla, double[] escaneo, double correlacion) {
    }

    private record Resultado(Homografia homografia, int inliers, double correlacion) {
    }
}
