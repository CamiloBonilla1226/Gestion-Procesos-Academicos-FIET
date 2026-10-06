package com.unicauca.cfiet.solicitudes.infraestructura.output.almacenamiento;

import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.almacenador.AlmacenadorArchivos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AlmacenamientoAnexosImplAdaptadorTest {

    @TempDir
    Path base;

    private AlmacenamientoAnexosImplAdaptador almacenamiento;
    private final String solicitud = UUID.randomUUID().toString();
    private final String anexo = UUID.randomUUID().toString();
    private final byte[] contenido = "%PDF-1.7 contenido".getBytes(StandardCharsets.US_ASCII);

    @BeforeEach
    void setUp() {
        AlmacenadorArchivos almacenador = new AlmacenadorArchivos();
        ReflectionTestUtils.setField(almacenador, "basePath", base.toString());
        almacenamiento = new AlmacenamientoAnexosImplAdaptador(almacenador, base.toString());
    }

    private long archivosEn(Path carpeta) throws IOException {
        if (!Files.exists(carpeta)) return 0;
        try (Stream<Path> archivos = Files.walk(carpeta)) {
            return archivos.filter(Files::isRegularFile).count();
        }
    }

    @Test
    void guardaConElNombreGeneradoLeeYBorraSinDejarCarpetas() throws IOException {
        String url = almacenamiento.guardar(solicitud, anexo + ".pdf", contenido);

        assertTrue(url.startsWith("/api/anexos/" + solicitud + "/" + anexo + "_"));
        assertTrue(url.endsWith(".pdf"));
        Path carpeta = base.resolve("anexos").resolve(solicitud);
        assertEquals(1, archivosEn(carpeta));
        assertArrayEquals(contenido, almacenamiento.leer(url));

        almacenamiento.eliminar(url);

        assertFalse(Files.exists(carpeta));
        assertNull(almacenamiento.leer(url));
    }

    @Test
    void noAceptaNombresNiCarpetasQueNoGeneroElSistema() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> almacenamiento.guardar(solicitud, "../" + anexo + ".pdf", contenido));
        assertThrows(IllegalArgumentException.class, () -> almacenamiento.guardar(solicitud, "paz y salvo.pdf", contenido));
        assertThrows(IllegalArgumentException.class, () -> almacenamiento.guardar(solicitud, anexo + ".exe", contenido));
        assertThrows(IllegalArgumentException.class, () -> almacenamiento.guardar("../../" + solicitud.substring(6), anexo + ".pdf", contenido));
        assertEquals(0, archivosEn(base));
    }

    @Test
    void noLeeNiBorraFueraDeLaCarpetaDeAnexos() throws IOException {
        Path secreto = Files.writeString(base.resolve("secreto.txt"), "no tocar");

        for (String url : new String[]{"/api/anexos/../secreto.txt", "/api/anexos/" + solicitud + "/../../secreto.txt",
                "/api/anexos/secreto.txt", "/otra/ruta/secreto.txt", "C:/secreto.txt"}) {
            assertThrows(IllegalArgumentException.class, () -> almacenamiento.leer(url), url);
            assertThrows(IllegalArgumentException.class, () -> almacenamiento.eliminar(url), url);
        }
        assertTrue(Files.exists(secreto));
    }

    @Test
    void siFallaLaEscrituraNoQuedanRestosEnDisco() throws IOException {
        AlmacenadorArchivos almacenadorQueFalla = mock(AlmacenadorArchivos.class);
        when(almacenadorQueFalla.guardarArchivo(anyString(), any(), anyString())).thenAnswer(invocacion -> {
            Path carpeta = Files.createDirectories(base.resolve("anexos").resolve(solicitud));
            Files.writeString(carpeta.resolve(anexo + "_20260310100000.pdf"), "a medias");
            throw new IOException("disco lleno");
        });
        AlmacenamientoAnexosImplAdaptador conFallo = new AlmacenamientoAnexosImplAdaptador(almacenadorQueFalla, base.toString());

        assertThrows(UncheckedIOException.class, () -> conFallo.guardar(solicitud, anexo + ".pdf", contenido));

        assertEquals(0, archivosEn(base));
        assertFalse(Files.exists(base.resolve("anexos").resolve(solicitud)));
    }

    private void terminarTransaccion(boolean confirmada) {
        try {
            for (TransactionSynchronization sincronizacion : TransactionSynchronizationManager.getSynchronizations()) {
                if (confirmada) sincronizacion.afterCommit();
                sincronizacion.afterCompletion(confirmada ? TransactionSynchronization.STATUS_COMMITTED : TransactionSynchronization.STATUS_ROLLED_BACK);
            }
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void sinTransaccionEliminarTrasConfirmarBorraEnSeguida() throws IOException {
        String url = almacenamiento.guardar(solicitud, anexo + ".pdf", contenido);

        almacenamiento.eliminarTrasConfirmar(url);

        assertEquals(0, archivosEn(base));
    }

    @Test
    void dentroDeUnaTransaccionElArchivoViejoSeBorraSoloAlConfirmar() throws IOException {
        String url = almacenamiento.guardar(solicitud, anexo + ".pdf", contenido);
        TransactionSynchronizationManager.initSynchronization();
        almacenamiento.eliminarTrasConfirmar(url);
        assertEquals(1, archivosEn(base));

        terminarTransaccion(true);

        assertEquals(0, archivosEn(base));
    }

    @Test
    void siLaTransaccionSeRevierteElArchivoViejoSeConservaYElNuevoSeBorra() throws IOException {
        String viejo = almacenamiento.guardar(solicitud, anexo + ".pdf", contenido);
        TransactionSynchronizationManager.initSynchronization();
        String nuevo = almacenamiento.guardar(solicitud, UUID.randomUUID() + ".pdf", contenido);
        almacenamiento.eliminarTrasConfirmar(viejo);

        terminarTransaccion(false);

        assertNotNull(almacenamiento.leer(viejo));
        assertNull(almacenamiento.leer(nuevo));
        assertEquals(1, archivosEn(base));
    }

    @Test
    void siLaTransaccionSeConfirmaElArchivoNuevoSeQueda() throws IOException {
        TransactionSynchronizationManager.initSynchronization();
        String nuevo = almacenamiento.guardar(solicitud, anexo + ".pdf", contenido);

        terminarTransaccion(true);

        assertArrayEquals(contenido, almacenamiento.leer(nuevo));
    }

    @Test
    void borrarUnArchivoQueYaNoExisteNoFalla() {
        assertDoesNotThrow(() -> almacenamiento.eliminar("/api/anexos/" + solicitud + "/" + anexo + "_1.pdf"));
        assertDoesNotThrow(() -> almacenamiento.eliminar(null));
    }
}
