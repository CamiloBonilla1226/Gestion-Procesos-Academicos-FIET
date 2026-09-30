package com.unicauca.cfiet.solicitudes.infraestructura.output.formateador;

import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.*;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de lanzar excepciones específicas según el tipo de error
 * ocurrido en la aplicación.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
public class ExcepcionesFormateadorImplAdaptador implements ExcepcionesFormateadorIntPuerto {

    @Override
    public void lanzarErrorGenerico(String mensaje) {
        throw new ErrorGenericoExcepcion(mensaje);
    }

    @Override
    public void lanzarEntidadNoExiste(String mensaje) {
        throw new ErrorEntidadNoExisteExcepcion(mensaje);
    }

    @Override
    public void lanzarEntidadExiste(String mensaje) {
        throw new ErrorEntidadExisteExcepcion(mensaje);
    }

    @Override
    public void lanzarReglaNegocioViolada(String mensaje) {
        throw new ErrorReglaNegocioVioladaExcepcion(mensaje);
    }

    @Override
    public void lanzarCredencialesErroneas(String mensaje) {
        throw new ErrorCredencialesIncorrectasExcepcion(mensaje);
    }

    @Override
    public void lanzarMalFormato(String mensaje) {
        throw new ErrorMalFormatoExcepcion(mensaje);
    }

    @Override
    public void lanzarSinInformacion(String mensaje) {
        throw new ErrorNoInformacionExcepcion(mensaje);
    }

    @Override
    public void lanzarSinAcceso(String mensaje) {
        throw new ErrorSinAccesoExcepcion(mensaje);
    }
}
