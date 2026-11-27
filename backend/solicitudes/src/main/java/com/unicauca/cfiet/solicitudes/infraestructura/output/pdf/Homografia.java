package com.unicauca.cfiet.solicitudes.infraestructura.output.pdf;

import org.apache.pdfbox.util.Matrix;

import java.util.List;

record Homografia(double[] h) {

    static Homografia entre(double[][] origen, double[][] destino) {
        return minimosCuadrados(List.of(origen), List.of(destino), false);
    }

    static Homografia minimosCuadrados(List<double[]> origen, List<double[]> destino, boolean afin) {
        int n = origen.size();
        if (n < (afin ? 3 : 4)) return null;
        double[][] normalOrigen = normalizacion(origen);
        double[][] normalDestino = normalizacion(destino);
        int incognitas = afin ? 6 : 8;
        double[][] sistema = new double[incognitas][incognitas + 1];
        for (int i = 0; i < n; i++) {
            double[] p = multiplicar(normalOrigen, origen.get(i));
            double[] q = multiplicar(normalDestino, destino.get(i));
            double[] filaU = afin ? new double[]{p[0], p[1], 1, 0, 0, 0}
                    : new double[]{p[0], p[1], 1, 0, 0, 0, -p[0] * q[0], -p[1] * q[0]};
            double[] filaV = afin ? new double[]{0, 0, 0, p[0], p[1], 1}
                    : new double[]{0, 0, 0, p[0], p[1], 1, -p[0] * q[1], -p[1] * q[1]};
            acumular(sistema, filaU, q[0]);
            acumular(sistema, filaV, q[1]);
        }
        double[] solucion = resolver(sistema, incognitas);
        if (solucion == null) return null;
        double[][] normalizada = {
                {solucion[0], solucion[1], solucion[2]},
                {solucion[3], solucion[4], solucion[5]},
                afin ? new double[]{0, 0, 1} : new double[]{solucion[6], solucion[7], 1}};
        double[][] completa = producto(inversaSemejanza(normalDestino), producto(normalizada, normalOrigen));
        double escala = completa[2][2];
        if (Math.abs(escala) < 1e-12) return null;
        double[] valores = new double[9];
        for (int i = 0; i < 9; i++) valores[i] = completa[i / 3][i % 3] / escala;
        return new Homografia(valores);
    }

    double[] aplicar(double x, double y) {
        double w = h[6] * x + h[7] * y + h[8];
        return new double[]{(h[0] * x + h[1] * y + h[2]) / w, (h[3] * x + h[4] * y + h[5]) / w};
    }

    Matrix local(float x, float y, float ancho, float alto) {
        double[] origen = aplicar(x, y);
        double[] derecha = aplicar(x + ancho, y);
        double[] arriba = aplicar(x, y + alto);
        return new Matrix((float) ((derecha[0] - origen[0]) / ancho), (float) ((derecha[1] - origen[1]) / ancho),
                (float) ((arriba[0] - origen[0]) / alto), (float) ((arriba[1] - origen[1]) / alto),
                (float) origen[0], (float) origen[1]);
    }

    private static void acumular(double[][] sistema, double[] fila, double valor) {
        int n = fila.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) sistema[i][j] += fila[i] * fila[j];
            sistema[i][n] += fila[i] * valor;
        }
    }

    private static double[][] normalizacion(List<double[]> puntos) {
        double mediaX = 0, mediaY = 0;
        for (double[] punto : puntos) {
            mediaX += punto[0];
            mediaY += punto[1];
        }
        mediaX /= puntos.size();
        mediaY /= puntos.size();
        double distancia = 0;
        for (double[] punto : puntos) distancia += Math.hypot(punto[0] - mediaX, punto[1] - mediaY);
        distancia /= puntos.size();
        double escala = distancia < 1e-9 ? 1 : Math.sqrt(2) / distancia;
        return new double[][]{{escala, 0, -escala * mediaX}, {0, escala, -escala * mediaY}, {0, 0, 1}};
    }

    private static double[][] inversaSemejanza(double[][] t) {
        double escala = t[0][0];
        return new double[][]{{1 / escala, 0, -t[0][2] / escala}, {0, 1 / escala, -t[1][2] / escala}, {0, 0, 1}};
    }

    private static double[] multiplicar(double[][] t, double[] punto) {
        return new double[]{t[0][0] * punto[0] + t[0][1] * punto[1] + t[0][2], t[1][0] * punto[0] + t[1][1] * punto[1] + t[1][2]};
    }

    private static double[][] producto(double[][] a, double[][] b) {
        double[][] c = new double[3][3];
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                for (int k = 0; k < 3; k++) c[i][j] += a[i][k] * b[k][j];
        return c;
    }

    private static double[] resolver(double[][] a, int n) {
        for (int columna = 0; columna < n; columna++) {
            int pivote = columna;
            for (int fila = columna + 1; fila < n; fila++)
                if (Math.abs(a[fila][columna]) > Math.abs(a[pivote][columna])) pivote = fila;
            if (Math.abs(a[pivote][columna]) < 1e-12) return null;
            double[] temporal = a[columna];
            a[columna] = a[pivote];
            a[pivote] = temporal;
            for (int fila = 0; fila < n; fila++) {
                if (fila == columna) continue;
                double factor = a[fila][columna] / a[columna][columna];
                for (int k = columna; k <= n; k++) a[fila][k] -= factor * a[columna][k];
            }
        }
        double[] solucion = new double[n];
        for (int i = 0; i < n; i++) solucion[i] = a[i][n] / a[i][i];
        return solucion;
    }
}
