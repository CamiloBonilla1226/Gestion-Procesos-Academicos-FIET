package com.unicauca.cfiet.solicitudes.aplicacion.output;

/**
 * Interfaz para manejar excepciones.
 * Permite lanzar diferentes tipos de errores de manera uniforme.
 *
 * @author Julian David Camacho Erazo {@literal <jdacamacho@unicauca.edu.co>}
 */
public interface ExcepcionesFormateadorIntPuerto {

    /**
     * Devuelve un mensaje de error genérico.
     *
     * @param mensaje mensaje descriptivo del error
     */
    void lanzarErrorGenerico(String mensaje);

    /**
     * Devuelve un mensaje de error cuando no existe la entidad.
     *
     * @param mensaje mensaje descriptivo del error
     */
    void lanzarEntidadNoExiste(String mensaje);

    /**
     * Devuelve un mensaje de error cuando la entidad ya existe.
     *
     * @param mensaje mensaje descriptivo del error
     */
    void lanzarEntidadExiste(String mensaje);

    /**
     * Devuelve un mensaje de error cuando se viola una regla de negocio.
     *
     * @param mensaje mensaje descriptivo del error
     */
    void lanzarReglaNegocioViolada(String mensaje);

    /**
     * Devuelve un mensaje de error cuando falló la autenticación del usuario.
     *
     * @param mensaje mensaje descriptivo del error
     */
    void lanzarCredencialesErroneas(String mensaje);

    /**
     * Devuelve un mensaje de error cuando un formato es incorrecto.
     *
     * @param mensaje mensaje descriptivo del error
     */
    void lanzarMalFormato(String mensaje);

    /**
     * Devuelve un mensaje de error cuando una consulta no obtuvo información.
     *
     * @param mensaje mensaje descriptivo del error
     */
    void lanzarSinInformacion(String mensaje);

    /**
     * Devuelve un mensaje de error cuando no se tiene acceso a una acción.
     *
     * @param mensaje mensaje descriptivo del error
     */
    void lanzarSinAcceso(String mensaje);
}