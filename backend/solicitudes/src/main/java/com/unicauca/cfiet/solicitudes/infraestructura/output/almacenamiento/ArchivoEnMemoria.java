package com.unicauca.cfiet.solicitudes.infraestructura.output.almacenamiento;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ArchivoEnMemoria implements MultipartFile {
    private final String nombreArchivo;
    private final byte[] contenido;

    public ArchivoEnMemoria(String nombreArchivo, byte[] contenido) {
        this.nombreArchivo = nombreArchivo;
        this.contenido = contenido;
    }

    @Override
    public String getName() {
        return nombreArchivo;
    }

    @Override
    public String getOriginalFilename() {
        return nombreArchivo;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return contenido.length == 0;
    }

    @Override
    public long getSize() {
        return contenido.length;
    }

    @Override
    public byte[] getBytes() {
        return contenido.clone();
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(contenido);
    }

    @Override
    public void transferTo(File destino) throws IOException {
        Files.write(destino.toPath(), contenido);
    }
}
