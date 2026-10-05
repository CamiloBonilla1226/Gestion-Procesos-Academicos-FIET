package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AsignaturaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.AsignaturaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Asignatura;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.util.UUID;

public class AsignaturaCUImplAdaptador implements AsignaturaCUIntPuerto {
    private final AsignaturaGatewayIntPuerto gateway;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;
    private static final String ASIGNATURA = "Asignatura";
    private static final String CODIGO = "codigo";

    public AsignaturaCUImplAdaptador(AsignaturaGatewayIntPuerto gateway,
                                     ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                     LogCUIntPuerto log) {
        this.gateway = gateway;
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
    }

    @Override
    public Asignatura crearAsignatura(Asignatura asignatura, String token) {
        if (gateway.existePorCodigo(asignatura.getCodigoAsignatura()))
            lanzarCodigoExiste(asignatura.getCodigoAsignatura());

        asignatura.setUuidAsignatura(UUID.randomUUID().toString());
        Asignatura guardada = gateway.guardar(asignatura);
        log.crearLog("Crear asignatura",
                String.format("Asignatura %s - %s creada con uuid %s",
                        guardada.getCodigoAsignatura(), guardada.getNombreAsignatura(), guardada.getUuidAsignatura()),
                token);
        return guardada;
    }

    @Override
    public PaginacionRespuestaDTO<Asignatura> getAsignaturasPaginado(int pagina, int tamanio) {
        validarPaginacion(pagina, tamanio);
        return gateway.getPaginado(pagina, tamanio);
    }

    @Override
    public PaginacionRespuestaDTO<Asignatura> getAsignaturasPorFiltro(String texto, int pagina, int tamanio) {
        validarPaginacion(pagina, tamanio);
        return gateway.getPorFiltro(texto, pagina, tamanio);
    }

    @Override
    public Asignatura getAsignatura(String uuidAsignatura) {
        return obtenerExistente(uuidAsignatura);
    }

    @Override
    public Asignatura actualizarAsignatura(String uuidAsignatura, Asignatura asignatura, String token) {
        Asignatura actual = obtenerExistente(uuidAsignatura);

        String nuevoCodigo = asignatura.getCodigoAsignatura();
        if (nuevoCodigo != null && !nuevoCodigo.isBlank() && !nuevoCodigo.equals(actual.getCodigoAsignatura())) {
            Asignatura conMismoCodigo = gateway.getPorCodigo(nuevoCodigo);
            if (conMismoCodigo != null && !conMismoCodigo.getUuidAsignatura().equals(uuidAsignatura))
                lanzarCodigoExiste(nuevoCodigo);
            actual.setCodigoAsignatura(nuevoCodigo);
        }

        if (asignatura.getNombreAsignatura() != null && !asignatura.getNombreAsignatura().isBlank())
            actual.setNombreAsignatura(asignatura.getNombreAsignatura());

        Asignatura guardada = gateway.guardar(actual);
        log.crearLog("Actualizar asignatura",
                String.format("Asignatura %s actualizada", guardada.getUuidAsignatura()),
                token);
        return guardada;
    }

    private Asignatura obtenerExistente(String uuidAsignatura) {
        Asignatura asignatura = gateway.getPorUuid(uuidAsignatura);
        if (asignatura == null)
            formateadorExcepciones.lanzarEntidadNoExiste(
                    String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, ASIGNATURA, uuidAsignatura));
        return asignatura;
    }

    private void validarPaginacion(int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 1)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);
    }

    private void lanzarCodigoExiste(String codigo) {
        formateadorExcepciones.lanzarEntidadExiste(
                String.format(MensajesError.ATRIBUTO_UNICO_YA_EXISTE, ASIGNATURA, CODIGO, codigo));
    }
}
