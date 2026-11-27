package com.unicauca.cfiet.solicitudes.aplicacion.output;

public interface AlmacenamientoAnexosIntPuerto {

    String guardar(String uuidSolicitudAcademica, String nombreArchivo, byte[] contenido);

    byte[] leer(String urlArchivo);

    void eliminar(String urlArchivo);

    void eliminarTrasConfirmar(String urlArchivo);

    String guardarRespuesta(String uuidRespuesta, String nombreArchivo, byte[] contenido);

    void eliminarRespuestaTrasConfirmar(String uuidRespuesta, String nombreArchivo);
}
