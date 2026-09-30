package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.RespuestaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Respuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.RespuestaEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.RespuestaOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.RespuestaRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Implementación de la fachada con el servicio de persistencia para la gestión de respuestas.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
@RequiredArgsConstructor
public class RespuestaGatewayImplAdaptador implements RespuestaGatewayIntPuerto {
    private final RespuestaRepositorio respuestaRepositorio;
    //Mappers
    private final RespuestaOwnMapper respuestaMapper;

    @Override
    public boolean solicitudTieneRespuesta(String uuidSolicitud) {
        return respuestaRepositorio.existeRespuestaParaSolicitud(uuidSolicitud);
    }

    @Override
    public Respuesta getRespuestaPorSolicitud(String uuidSolicitud) {
        RespuestaEntidad entidad = respuestaRepositorio.obtenerPorSolicitud(uuidSolicitud);
        if(entidad == null)
            return null;
        return respuestaMapper.toDominio(entidad);
    }

    @Override
    public PaginacionRespuestaDTO<Respuesta> getRespuestas(int pagina, int tamanio) {
        Pageable paginado =
                PageRequest.of(
                        pagina,
                        tamanio,
                        Sort.by("fechaCreacion").descending()
                                .and(Sort.by("uuidRespuesta").ascending())
                );
        Page<RespuestaEntidad> page = respuestaRepositorio.findAll(paginado);

        List<Respuesta> lista = page.getContent().stream()
                .map(respuestaMapper::toDominio)
                .toList();

        return new PaginacionRespuestaDTO<>(lista, page.getTotalElements());
    }

    @Override
    public PaginacionRespuestaDTO<Respuesta> getRespuestasPorNombreSolicitud(String nombreSolicitud, int pagina, int tamanio) {
        Pageable paginado =
                PageRequest.of(
                        pagina,
                        tamanio,
                        Sort.by("fechaCreacion").descending()
                                .and(Sort.by("uuidRespuesta").ascending())
                );
        Page<RespuestaEntidad> page = respuestaRepositorio.obtenerPorNombreSolicitud(nombreSolicitud, paginado);

        List<Respuesta> lista = page.getContent().stream()
                .map(respuestaMapper::toDominio)
                .toList();

        return new PaginacionRespuestaDTO<>(lista, page.getTotalElements());
    }

    @Override
    public PaginacionRespuestaDTO<Respuesta> getRespuestasPorFuncionario(String uuidFuncionario, int pagina, int tamanio) {
        Pageable paginado =
                PageRequest.of(
                        pagina,
                        tamanio,
                        Sort.by("fechaCreacion").descending()
                                .and(Sort.by("uuidRespuesta").ascending())
                );
        Page<RespuestaEntidad> page = respuestaRepositorio.obtenerRespuestasPorFuncionario(uuidFuncionario, paginado);

        List<Respuesta> lista = page.getContent().stream()
                .map(respuestaMapper::toDominio)
                .toList();

        return new PaginacionRespuestaDTO<>(lista, page.getTotalElements());
    }

    @Override
    public PaginacionRespuestaDTO<Respuesta> getRespuestasPorFuncionarioNombreSolicitud(String uuidFuncionario, String nombreSolicitud, int pagina, int tamanio) {
        Pageable paginado =
                PageRequest.of(
                        pagina,
                        tamanio,
                        Sort.by("fechaCreacion").descending()
                                .and(Sort.by("uuidRespuesta").ascending())
                );
        Page<RespuestaEntidad> page = respuestaRepositorio.obtenerRespuestasPorFuncionarioYNombreSolicitud(uuidFuncionario, nombreSolicitud, paginado);

        List<Respuesta> lista = page.getContent().stream()
                .map(respuestaMapper::toDominio)
                .toList();

        return new PaginacionRespuestaDTO<>(lista, page.getTotalElements());
    }

    @Override
    public Respuesta getRespuesta(String uuidRespuesta) {
        if(!respuestaRepositorio.existsById(uuidRespuesta))
            return null;

        return respuestaMapper.toDominio(respuestaRepositorio.findById(uuidRespuesta).get());
    }

    @Override
    public Respuesta guardarRespuesta(Respuesta respuesta) {
        RespuestaEntidad entidad = respuestaMapper.toEntidad(respuesta);
        RespuestaEntidad entidadGuardada = respuestaRepositorio.save(entidad);
        return respuestaMapper.toDominio(entidadGuardada);
    }
}
