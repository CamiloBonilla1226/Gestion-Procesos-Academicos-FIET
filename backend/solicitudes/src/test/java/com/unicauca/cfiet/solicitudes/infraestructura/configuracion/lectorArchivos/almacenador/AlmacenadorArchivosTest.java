package com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.almacenador;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AlmacenadorArchivosTest {

    @TempDir
    Path raiz;

    private Path base;
    private AlmacenadorArchivos almacenador;
    private final String solicitud = UUID.randomUUID().toString();
    private final String anexo = UUID.randomUUID().toString();
    private final byte[] contenido = "%PDF-1.7 contenido".getBytes(StandardCharsets.US_ASCII);

    @BeforeEach
    void setUp() throws IOException {
        base = Files.createDirectories(raiz.resolve("uploads"));
        almacenador = new AlmacenadorArchivos();
        ReflectionTestUtils.setField(almacenador, "basePath", base.toString());
    }

    private MockMultipartFile archivo(String nombreOriginal) {
        return new MockMultipartFile("archivo", nombreOriginal, "application/pdf", contenido);
    }

    private List<Path> archivosBajo(Path carpeta) throws IOException {
        if (!Files.exists(carpeta)) return List.of();
        try (Stream<Path> rutas = Files.walk(carpeta)) {
            return rutas.filter(Files::isRegularFile).toList();
        }
    }

    private Path carpetaSolicitud() {
        return base.resolve("anexos").resolve(solicitud);
    }

    private void soloDentroDeLaCarpetaDeLaSolicitud() throws IOException {
        for (Path archivo : archivosBajo(raiz))
            assertTrue(archivo.toAbsolutePath().normalize().startsWith(carpetaSolicitud().toAbsolutePath().normalize()), archivo.toString());
    }

    @Test
    void guardaConElNombreQueGeneraElSistemaYNoConElDelCliente() throws IOException {
        String url = almacenador.guardarArchivo(solicitud, archivo("Mi carta personal.pdf"), anexo);

        List<Path> guardados = archivosBajo(base);
        assertEquals(1, guardados.size());
        String nombre = guardados.get(0).getFileName().toString();
        assertTrue(nombre.matches(anexo + "_\\d{14}\\.pdf"), nombre);
        assertFalse(nombre.contains("carta"));
        assertEquals(carpetaSolicitud(), guardados.get(0).getParent());
        assertEquals("/api/anexos/" + solicitud + "/" + nombre, url);
        assertArrayEquals(contenido, Files.readAllBytes(guardados.get(0)));
    }

    @Test
    void guardaSinExtensionSiElClienteNoLaManda() throws IOException {
        almacenador.guardarArchivo(solicitud, archivo("carta"), anexo);

        String nombre = archivosBajo(base).get(0).getFileName().toString();
        assertTrue(nombre.matches(anexo + "_\\d{14}"), nombre);
    }

    @ParameterizedTest
    @ValueSource(strings = {"../../../../fuera", "..\\..\\..\\fuera", "/etc/passwd", "C:\\Windows\\fuera", "sub/../../fuera"})
    void limpiaElNombreDelAnexoParaQueNoSalgaDeLaCarpeta(String nombreAnexo) throws IOException {
        String url = almacenador.guardarArchivo(solicitud, archivo("carta.pdf"), nombreAnexo);

        List<Path> guardados = archivosBajo(raiz);
        assertEquals(1, guardados.size());
        assertEquals(carpetaSolicitud(), guardados.get(0).getParent());
        String nombre = guardados.get(0).getFileName().toString();
        assertTrue(nombre.matches("[a-zA-Z0-9_\\-]+_\\d{14}\\.pdf"), nombre);
        assertFalse(url.contains(".."));
    }

    @Test
    void cambiaEspaciosPorGuionBajoEnElNombreDelAnexo() throws IOException {
        almacenador.guardarArchivo(solicitud, archivo("carta.pdf"), "  Paz y salvo financiero  ");

        String nombre = archivosBajo(base).get(0).getFileName().toString();
        assertTrue(nombre.matches("Paz_y_salvo_financiero_\\d{14}\\.pdf"), nombre);
    }

    @ParameterizedTest
    @ValueSource(strings = {"../../../../evil.pdf", "..\\..\\..\\evil.pdf", "/tmp/evil.pdf", "C:\\Windows\\evil.pdf"})
    void unNombreOriginalConRutaNoSacaElArchivoDeLaCarpeta(String nombreOriginal) throws IOException {
        almacenador.guardarArchivo(solicitud, archivo(nombreOriginal), anexo);

        List<Path> guardados = archivosBajo(raiz);
        assertEquals(1, guardados.size());
        assertTrue(guardados.get(0).getFileName().toString().matches(anexo + "_\\d{14}\\.pdf"));
        soloDentroDeLaCarpetaDeLaSolicitud();
    }

    @ParameterizedTest
    @ValueSource(strings = {"../../../../evil", "carta.pdf/../../../evil"})
    void unNombreOriginalConRutaYSinExtensionRealFallaSinEscribirFuera(String nombreOriginal) throws IOException {
        assertThrows(IOException.class, () -> almacenador.guardarArchivo(solicitud, archivo(nombreOriginal), anexo));

        assertTrue(archivosBajo(raiz).isEmpty());
    }

    @Test
    void dosArchivosDelMismoAnexoQuedanEnLaMismaCarpeta() throws IOException {
        almacenador.guardarArchivo(solicitud, archivo("a.pdf"), anexo);
        almacenador.guardarArchivo(solicitud, archivo("b.pdf"), UUID.randomUUID().toString());

        assertEquals(2, archivosBajo(carpetaSolicitud()).size());
        soloDentroDeLaCarpetaDeLaSolicitud();
    }

    @Test
    void guardaLaRespuestaConElUuidDeLaRespuestaComoNombre() throws IOException {
        String respuesta = UUID.randomUUID().toString();

        String url = almacenador.guardarArchivoRespuesta(respuesta, archivo("respuesta firmada.pdf"));

        List<Path> guardados = archivosBajo(base);
        assertEquals(1, guardados.size());
        String nombre = guardados.get(0).getFileName().toString();
        assertTrue(nombre.matches(respuesta + "_\\d{14}\\.pdf"), nombre);
        assertEquals(base.resolve("respuestas").resolve(respuesta), guardados.get(0).getParent());
        assertEquals("/api/respuestas/" + respuesta + "/" + nombre, url);
    }
}
