package com.unicauca.cfiet.solicitudes.dominio.servicios;

import com.unicauca.cfiet.solicitudes.dominio.modelos.ArchivoAdjunto;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorMalFormatoExcepcion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.AnexoAcademicoConstantes.TAMANIO_MAXIMO_BYTES;
import static org.junit.jupiter.api.Assertions.*;

class ValidadorArchivoAdjuntoTest {

    private static final String DOCUMENTO = "Paz y salvo - División Financiera";
    private static final byte[] PDF = "%PDF-1.7\n1 0 obj".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 13};
    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16};

    private final ValidadorArchivoAdjunto validador = new ValidadorArchivoAdjunto(new ExcepcionesFormateadorImplAdaptador());

    private ArchivoAdjunto archivo(String nombre, byte[] contenido) {
        return ArchivoAdjunto.builder().nombreOriginal(nombre).tipoContenido("application/octet-stream").contenido(contenido).build();
    }

    private byte[] pdfDeTamanio(long bytes) {
        byte[] contenido = new byte[(int) bytes];
        Arrays.fill(contenido, (byte) ' ');
        System.arraycopy(PDF, 0, contenido, 0, PDF.length);
        return contenido;
    }

    private String mensaje(Runnable accion) {
        return assertThrows(ErrorMalFormatoExcepcion.class, accion::run).getMessage();
    }

    @Test
    void aceptaUnPdfYDevuelveSuExtension() {
        assertEquals("pdf", validador.validar(archivo("paz y salvo.pdf", PDF), DOCUMENTO, "pdf"));
    }

    @Test
    void aceptaImagenesCuandoElTipoDeAnexoLasPermite() {
        assertEquals("png", validador.validar(archivo("carne.png", PNG), DOCUMENTO, "pdf,jpg,jpeg,png"));
        assertEquals("jpg", validador.validar(archivo("carne.jpg", JPG), DOCUMENTO, "pdf,jpg,jpeg,png"));
    }

    @Test
    void tomaJpegComoJpgEnElArchivoYEnLosFormatos() {
        assertEquals("jpg", validador.validar(archivo("carne.jpeg", JPG), DOCUMENTO, "pdf,jpg"));
        assertEquals("jpg", validador.validar(archivo("carne.jpg", JPG), DOCUMENTO, "pdf,jpeg"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"CARTA.PDF", "carta.Pdf", "carta.pdf  ", "  carta.pdf", "carta. pdf", "carta.PDF "})
    void normalizaExtensionesEnMayusculasOConEspacios(String nombre) {
        assertEquals("pdf", validador.validar(archivo(nombre, PDF), DOCUMENTO, "pdf"));
    }

    @Test
    void normalizaLosFormatosPermitidosConMayusculasYEspacios() {
        assertEquals("png", validador.validar(archivo("carne.png", PNG), DOCUMENTO, " PDF , PNG "));
    }

    @Test
    void tomaSoloElNombreSinLaRutaQueMandaElNavegador() {
        assertEquals("pdf", validador.validar(archivo("C:\\fakepath\\docs.v2\\carta.pdf", PDF), DOCUMENTO, "pdf"));
        assertEquals("pdf", validador.validar(archivo("/home/usuario/fotos.png/carta.pdf", PDF), DOCUMENTO, "pdf"));
    }

    @Test
    void rechazaUnFormatoQueElTipoDeAnexoNoPermite() {
        assertEquals(String.format(MensajesError.FORMATO_ANEXO_NO_PERMITIDO, "png", DOCUMENTO, "pdf"),
                mensaje(() -> validador.validar(archivo("carne.png", PNG), DOCUMENTO, "pdf")));
    }

    @ParameterizedTest
    @CsvSource({"factura.exe,exe", "carta.docx,docx", "carta.pdf.exe,exe", "foto.gif,gif"})
    void rechazaExtensionesFueraDeLaLista(String nombre, String extension) {
        assertEquals(String.format(MensajesError.FORMATO_ANEXO_NO_PERMITIDO, extension, DOCUMENTO, "pdf,jpg,jpeg,png"),
                mensaje(() -> validador.validar(archivo(nombre, PDF), DOCUMENTO, "pdf,jpg,jpeg,png")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"carta", "carta.", ""})
    void rechazaUnArchivoSinExtension(String nombre) {
        assertEquals(String.format(MensajesError.FORMATO_ANEXO_NO_PERMITIDO, "sin extension", DOCUMENTO, "pdf"),
                mensaje(() -> validador.validar(archivo(nombre, PDF), DOCUMENTO, "pdf")));
    }

    @Test
    void rechazaCualquierArchivoSiElTipoNoTraeFormatos() {
        assertEquals(String.format(MensajesError.FORMATO_ANEXO_NO_PERMITIDO, "pdf", DOCUMENTO, null),
                mensaje(() -> validador.validar(archivo("carta.pdf", PDF), DOCUMENTO, null)));
    }

    @Test
    void aceptaUnArchivoDeJustoCincoMegas() {
        assertEquals("pdf", validador.validar(archivo("grande.pdf", pdfDeTamanio(TAMANIO_MAXIMO_BYTES)), DOCUMENTO, "pdf"));
    }

    @Test
    void rechazaUnArchivoDeUnByteMasDeCincoMegas() {
        long tamanio = TAMANIO_MAXIMO_BYTES + 1;

        assertEquals(String.format(MensajesError.ARCHIVO_MUY_GRANDE, tamanio, TAMANIO_MAXIMO_BYTES),
                mensaje(() -> validador.validar(archivo("grande.pdf", pdfDeTamanio(tamanio)), DOCUMENTO, "pdf")));
        assertEquals(5242880L, TAMANIO_MAXIMO_BYTES);
    }

    @Test
    void revisaElTamanioAntesQueElFormato() {
        long tamanio = TAMANIO_MAXIMO_BYTES + 1;

        assertEquals(String.format(MensajesError.ARCHIVO_MUY_GRANDE, tamanio, TAMANIO_MAXIMO_BYTES),
                mensaje(() -> validador.validar(archivo("grande.exe", pdfDeTamanio(tamanio)), DOCUMENTO, "pdf")));
    }

    @Test
    void rechazaUnArchivoVacioNuloOSinContenido() {
        assertEquals(MensajesError.ARCHIVO_VACIO, mensaje(() -> validador.validar(archivo("carta.pdf", new byte[0]), DOCUMENTO, "pdf")));
        assertEquals(MensajesError.ARCHIVO_VACIO, mensaje(() -> validador.validar(archivo("carta.pdf", null), DOCUMENTO, "pdf")));
        assertEquals(MensajesError.ARCHIVO_VACIO, mensaje(() -> validador.validar(null, DOCUMENTO, "pdf")));
    }

    @Test
    void rechazaUnTextoConExtensionPdf() {
        byte[] texto = "Esto es un archivo de texto, no un PDF".getBytes(StandardCharsets.UTF_8);

        assertEquals(String.format(MensajesError.CONTENIDO_ANEXO_NO_COINCIDE, "pdf"),
                mensaje(() -> validador.validar(archivo("carta.pdf", texto), DOCUMENTO, "pdf")));
    }

    @Test
    void rechazaUnContenidoDeOtroFormatoPermitido() {
        assertEquals(String.format(MensajesError.CONTENIDO_ANEXO_NO_COINCIDE, "jpg"),
                mensaje(() -> validador.validar(archivo("foto.jpg", PNG), DOCUMENTO, "pdf,jpg,png")));
        assertEquals(String.format(MensajesError.CONTENIDO_ANEXO_NO_COINCIDE, "png"),
                mensaje(() -> validador.validar(archivo("foto.png", PDF), DOCUMENTO, "pdf,jpg,png")));
        assertEquals(String.format(MensajesError.CONTENIDO_ANEXO_NO_COINCIDE, "pdf"),
                mensaje(() -> validador.validar(archivo("foto.pdf", JPG), DOCUMENTO, "pdf,jpg,png")));
    }

    @Test
    void rechazaUnContenidoMasCortoQueLaFirma() {
        byte[] corto = "%PD".getBytes(StandardCharsets.US_ASCII);

        assertEquals(String.format(MensajesError.CONTENIDO_ANEXO_NO_COINCIDE, "pdf"),
                mensaje(() -> validador.validar(archivo("carta.pdf", corto), DOCUMENTO, "pdf")));
    }

    @Test
    void rechazaUnaExtensionPermitidaSinFirmaConocida() {
        assertEquals(String.format(MensajesError.CONTENIDO_ANEXO_NO_COINCIDE, "docx"),
                mensaje(() -> validador.validar(archivo("carta.docx", PDF), DOCUMENTO, "pdf,docx")));
    }

    @Test
    void nombreOriginalSeguroQuitaRutaYCaracteresDeControl() {
        assertEquals("carta.pdf", validador.nombreOriginalSeguro("../../etc/carta.pdf", "pdf"));
        assertEquals("carta.pdf", validador.nombreOriginalSeguro("C:\\Users\\x\\carta.pdf", "pdf"));
        assertEquals("cartafinal.pdf", validador.nombreOriginalSeguro("carta\u0000\nfinal.pdf", "pdf"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", ".", "..", "../..", "carpeta/"})
    void nombreOriginalSeguroUsaUnNombreGenericoSiNoQuedaNombre(String nombre) {
        assertEquals("documento.png", validador.nombreOriginalSeguro(nombre, "png"));
        assertEquals("documento.pdf", validador.nombreOriginalSeguro(null, "pdf"));
    }

    @Test
    void nombreOriginalSeguroRecortaNombresLargosConservandoLaExtension() {
        String largo = "a".repeat(300) + ".pdf";

        String seguro = validador.nombreOriginalSeguro(largo, "pdf");

        assertEquals(255, seguro.length());
        assertTrue(seguro.endsWith(".pdf"));
    }
}
