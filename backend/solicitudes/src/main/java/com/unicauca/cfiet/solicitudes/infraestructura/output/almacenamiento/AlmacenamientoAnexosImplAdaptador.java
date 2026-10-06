package com.unicauca.cfiet.solicitudes.infraestructura.output.almacenamiento;

import com.unicauca.cfiet.solicitudes.aplicacion.output.AlmacenamientoAnexosIntPuerto;
import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.almacenador.AlmacenadorArchivos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

@Service
public class AlmacenamientoAnexosImplAdaptador implements AlmacenamientoAnexosIntPuerto {
    private static final String CARPETA = "anexos";
    private static final String PREFIJO_URL = "/api/anexos/";
    private static final Pattern UUID = Pattern.compile("[0-9a-fA-F\\-]{36}");
    private static final Pattern NOMBRE_GENERADO = Pattern.compile("[0-9a-fA-F\\-]{36}\\.(pdf|jpg|png)");

    private final AlmacenadorArchivos almacenador;
    private final String basePath;

    public AlmacenamientoAnexosImplAdaptador(AlmacenadorArchivos almacenador, @Value("${app.uploads.base-path}") String basePath) {
        this.almacenador = almacenador;
        this.basePath = basePath;
    }

    @Override
    public String guardar(String uuidSolicitudAcademica, String nombreArchivo, byte[] contenido) {
        if (!UUID.matcher(uuidSolicitudAcademica).matches() || !NOMBRE_GENERADO.matcher(nombreArchivo).matches())
            throw new IllegalArgumentException("Nombre de archivo o carpeta no permitido");
        String prefijo = nombreArchivo.substring(0, nombreArchivo.lastIndexOf('.'));
        try {
            return almacenador.guardarArchivo(uuidSolicitudAcademica, new ArchivoEnMemoria(nombreArchivo, contenido), prefijo);
        } catch (IOException | RuntimeException error) {
            borrarRestos(uuidSolicitudAcademica, prefijo);
            throw error instanceof IOException io ? new UncheckedIOException(io) : (RuntimeException) error;
        }
    }

    @Override
    public byte[] leer(String urlArchivo) {
        Path ruta = rutaEnDisco(urlArchivo);
        if (!Files.isRegularFile(ruta)) return null;
        try {
            return Files.readAllBytes(ruta);
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }

    @Override
    public void eliminar(String urlArchivo) {
        if (urlArchivo == null) return;
        Path ruta = rutaEnDisco(urlArchivo);
        try {
            Files.deleteIfExists(ruta);
            borrarCarpetaSiEstaVacia(ruta.getParent());
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }

    private Path carpetaBase() {
        return Paths.get(basePath, CARPETA).toAbsolutePath().normalize();
    }

    private Path rutaEnDisco(String urlArchivo) {
        if (urlArchivo == null || !urlArchivo.startsWith(PREFIJO_URL))
            throw new IllegalArgumentException("Ruta de anexo no permitida");
        Path base = carpetaBase();
        Path ruta = base.resolve(urlArchivo.substring(PREFIJO_URL.length())).normalize();
        if (!ruta.startsWith(base) || ruta.getParent() == null || !ruta.getParent().getParent().equals(base))
            throw new IllegalArgumentException("Ruta de anexo no permitida");
        return ruta;
    }

    private void borrarRestos(String uuidSolicitudAcademica, String prefijo) {
        Path carpeta = carpetaBase().resolve(uuidSolicitudAcademica);
        if (!Files.isDirectory(carpeta)) return;
        try (DirectoryStream<Path> archivos = Files.newDirectoryStream(carpeta, prefijo + "_*")) {
            for (Path archivo : archivos)
                Files.deleteIfExists(archivo);
            borrarCarpetaSiEstaVacia(carpeta);
        } catch (IOException ignorado) {
        }
    }

    private void borrarCarpetaSiEstaVacia(Path carpeta) throws IOException {
        if (carpeta == null || carpeta.equals(carpetaBase()) || !Files.isDirectory(carpeta)) return;
        try (DirectoryStream<Path> contenido = Files.newDirectoryStream(carpeta)) {
            if (!contenido.iterator().hasNext())
                Files.deleteIfExists(carpeta);
        }
    }
}
