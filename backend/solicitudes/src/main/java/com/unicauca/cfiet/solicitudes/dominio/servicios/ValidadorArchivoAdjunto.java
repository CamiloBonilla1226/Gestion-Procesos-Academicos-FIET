package com.unicauca.cfiet.solicitudes.dominio.servicios;

import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ArchivoAdjunto;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.AnexoAcademicoConstantes.TAMANIO_MAXIMO_BYTES;

public class ValidadorArchivoAdjunto {
    private static final int MAXIMO_NOMBRE = 255;
    private static final byte[] FIRMA_PDF = {'%', 'P', 'D', 'F', '-'};
    private static final byte[] FIRMA_PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] FIRMA_JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;

    public ValidadorArchivoAdjunto(ExcepcionesFormateadorIntPuerto formateadorExcepciones) {
        this.formateadorExcepciones = formateadorExcepciones;
    }

    public String validar(ArchivoAdjunto archivo, String documento, String formatos) {
        if (archivo == null || archivo.getContenido() == null || archivo.getContenido().length == 0)
            formateadorExcepciones.lanzarMalFormato(MensajesError.ARCHIVO_VACIO);
        long tamanio = archivo.getContenido().length;
        if (tamanio > TAMANIO_MAXIMO_BYTES)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.ARCHIVO_MUY_GRANDE, tamanio, TAMANIO_MAXIMO_BYTES));

        Set<String> permitidos = Arrays.stream(formatos == null ? new String[0] : formatos.split(","))
                .map(this::normalizarExtension)
                .collect(Collectors.toSet());
        String extension = normalizarExtension(extensionDe(archivo.getNombreOriginal()));
        if (extension.isEmpty() || !permitidos.contains(extension))
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.FORMATO_ANEXO_NO_PERMITIDO,
                    extension.isEmpty() ? "sin extension" : extension, documento, formatos));
        if (!firmaCoincide(extension, archivo.getContenido()))
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.CONTENIDO_ANEXO_NO_COINCIDE, extension));
        return extension;
    }

    public String nombreOriginalSeguro(String nombreOriginal, String extension) {
        String nombre = nombreBase(nombreOriginal).replaceAll("\\p{Cntrl}", "");
        if (nombre.isBlank() || nombre.equals(".") || nombre.equals(".."))
            nombre = "documento." + extension;
        return nombre.length() > MAXIMO_NOMBRE ? nombre.substring(nombre.length() - MAXIMO_NOMBRE) : nombre;
    }

    private String extensionDe(String nombreOriginal) {
        String nombre = nombreBase(nombreOriginal);
        int punto = nombre.lastIndexOf('.');
        return punto < 0 ? "" : nombre.substring(punto + 1);
    }

    private String normalizarExtension(String extension) {
        String limpia = extension == null ? "" : extension.trim().toLowerCase(Locale.ROOT);
        return "jpeg".equals(limpia) ? "jpg" : limpia;
    }

    private boolean firmaCoincide(String extension, byte[] contenido) {
        byte[] firma = switch (extension) {
            case "pdf" -> FIRMA_PDF;
            case "png" -> FIRMA_PNG;
            case "jpg" -> FIRMA_JPG;
            default -> null;
        };
        if (firma == null || contenido.length < firma.length) return false;
        for (int i = 0; i < firma.length; i++)
            if (contenido[i] != firma[i]) return false;
        return true;
    }

    private String nombreBase(String nombreOriginal) {
        if (nombreOriginal == null) return "";
        String nombre = nombreOriginal.replace('\\', '/');
        return nombre.substring(nombre.lastIndexOf('/') + 1).trim();
    }
}
