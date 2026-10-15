package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.DocenteCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.DocenteGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Docente;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

public class DocenteCUImplAdaptador implements DocenteCUIntPuerto {
    private static final int TAMANIO_MAXIMO = 100;

    private final DocenteGatewayIntPuerto gateway;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;

    public DocenteCUImplAdaptador(DocenteGatewayIntPuerto gateway, ExcepcionesFormateadorIntPuerto formateadorExcepciones) {
        this.gateway = gateway;
        this.formateadorExcepciones = formateadorExcepciones;
    }

    @Override
    public PaginacionRespuestaDTO<Docente> buscarDocentes(String nombre, String numeroDocumento, String username, int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 1 || tamanio > TAMANIO_MAXIMO)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);
        return gateway.buscar(limpio(nombre), limpio(numeroDocumento), limpio(username), pagina, tamanio);
    }

    private String limpio(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
