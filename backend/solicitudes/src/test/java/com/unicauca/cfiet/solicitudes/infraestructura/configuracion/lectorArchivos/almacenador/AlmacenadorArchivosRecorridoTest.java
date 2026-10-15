package com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.almacenador;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AlmacenadorArchivosRecorridoTest {

    @TempDir
    Path raiz;

    private Path base;
    private Path secreto;
    private AlmacenadorArchivos almacenador;
    private final String solicitud = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() throws IOException {
        base = Files.createDirectories(raiz.resolve("uploads"));
        Path carpeta = Files.createDirectories(base.resolve("anexos").resolve(solicitud));
        Files.writeString(carpeta.resolve("Carta_20260101000000.pdf"), "%PDF");
        Files.createDirectories(base.resolve("anexos").resolve("otra"));
        Files.writeString(base.resolve("anexos").resolve("otra").resolve("ajeno.pdf"), "%PDF");
        secreto = Files.writeString(raiz.resolve("secreto.txt"), "clave");
        almacenador = new AlmacenadorArchivos();
        ReflectionTestUtils.setField(almacenador, "basePath", base.toString());
    }

    private String comoLlegaAlControlador(String segmentoEnLaUrl) {
        String decodificadoPorSpring = URLDecoder.decode(segmentoEnLaUrl, StandardCharsets.UTF_8);
        return URLDecoder.decode(decodificadoPorSpring, StandardCharsets.UTF_8);
    }

    @Test
    void laConstruccionAnteriorSaliaDeLaCarpetaConDobleCodificacion() throws IOException {
        String nombre = comoLlegaAlControlador("%252e%252e%252f%252e%252e%252f%252e%252e%252fsecreto.txt");
        File anterior = new File(base + "/anexos/" + solicitud + "/" + nombre);

        assertTrue(anterior.isFile());
        assertEquals(secreto.toFile().getCanonicalPath(), anterior.getCanonicalPath());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "..%2F..%2F..%2Fsecreto.txt",
            "%252e%252e%252f%252e%252e%252f%252e%252e%252fsecreto.txt",
            "%2e%2e%2f%2e%2e%2f%2e%2e%2fsecreto.txt",
            "..%5C..%5C..%5Csecreto.txt",
            "%252e%252e%255c%252e%252e%255c%252e%252e%255csecreto.txt",
            "..%2Fotra%2Fajeno.pdf",
            "%252e%252e%252fotra%252fajeno.pdf",
            "..",
            "."
    })
    void ningunNombreSaleDeLaCarpetaDeLaSolicitud(String segmentoEnLaUrl) {
        String nombre = comoLlegaAlControlador(segmentoEnLaUrl);

        assertNull(almacenador.resolverArchivo("anexos", solicitud, nombre), nombre);
    }

    @ParameterizedTest
    @ValueSource(strings = {"..", "../anexos", "..%2F..", "otra/../..", ""})
    void ningunUuidSaleDeLaCarpetaBase(String uuid) {
        String recurso = comoLlegaAlControlador(uuid);

        assertNull(almacenador.resolverArchivo("anexos", recurso, "secreto.txt"), recurso);
    }

    @Test
    void elArchivoLegitimoSeResuelveDentroDeSuCarpeta() throws IOException {
        File archivo = almacenador.resolverArchivo("anexos", solicitud, comoLlegaAlControlador("Carta_20260101000000.pdf"));

        assertNotNull(archivo);
        assertTrue(archivo.isFile());
        assertEquals(base.resolve("anexos").resolve(solicitud).resolve("Carta_20260101000000.pdf").toFile().getCanonicalPath(),
                archivo.getCanonicalPath());
    }

    @Test
    void lasRespuestasUsanSuPropiaCarpeta() throws IOException {
        Path carpeta = Files.createDirectories(base.resolve("respuestas").resolve("resp-1"));
        Files.writeString(carpeta.resolve("resp-1_20260101000000.pdf"), "%PDF");

        assertNotNull(almacenador.resolverArchivo("respuestas", "resp-1", "resp-1_20260101000000.pdf"));
        assertNull(almacenador.resolverArchivo("respuestas", "resp-1", comoLlegaAlControlador("%252e%252e%252f%252e%252e%252fanexos%252f" + solicitud + "%252fCarta_20260101000000.pdf")));
    }
}
