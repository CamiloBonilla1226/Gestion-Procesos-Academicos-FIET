package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Solicitud;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.SolicitudOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.SolicitudRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Implementación de la fachada con el servicio de persistencia para la gestión de solicitudes.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
@RequiredArgsConstructor
public class SolicitudGatewayImplAdaptador implements SolicitudGatewayIntPuerto {
    private final SolicitudRepositorio repositorio;
    private final SolicitudOwnMapper mapper;

    @Override
    public List<Solicitud> getSolicitudes() {
        List<SolicitudEntidad> entidades =
                repositorio.findAll(Sort.by("fechaCreacion").descending());
        return entidades.stream()
                .map(mapper::toDominio)
                .toList();
    }

    @Override
    public PaginacionRespuestaDTO<Solicitud> getSolicitudes(int pagina, int tamanio) {
        Pageable paginado =
                PageRequest.of(
                        pagina,
                        tamanio,
                        Sort.by("fechaCreacion").descending()
                                .and(Sort.by("uuidSolicitud").ascending())
                );
        Page<SolicitudEntidad> page = repositorio.findAll(paginado);
        List<Solicitud> respuesta = page.getContent().stream()
                .map(mapper::toDominio)
                .toList();

        return new PaginacionRespuestaDTO<>(respuesta, page.getTotalElements());
    }

    @Override
    public PaginacionRespuestaDTO<Solicitud> buscarSolicitudesPorSolicitante(String nombreSolicitud, String solicitante, String estado, int pagina, int tamanio) {
        Pageable paginado =
                PageRequest.of(
                        pagina,
                        tamanio,
                        Sort.by("fechaCreacion").descending()
                                .and(Sort.by("uuidSolicitud").ascending())
                );

        Page<SolicitudEntidad> page =
                repositorio.buscarSolicitudPorSolicitante(nombreSolicitud, solicitante, estado, paginado);

        List<Solicitud> solicitudes = page.getContent().stream()
                .map(mapper::toDominio)
                .toList();

        return new PaginacionRespuestaDTO<>(solicitudes, page.getTotalElements());
    }

    @Override
    public Solicitud getSolicitud(String uuidSolicitud) {
        return repositorio.findById(uuidSolicitud)
                .map(mapper::toDominio)
                .orElse(null);
    }

    @Override
    public Solicitud guardarSolicitud(Solicitud solicitud) {
        SolicitudEntidad entidad = mapper.toEntidad(solicitud);
        SolicitudEntidad guardada = repositorio.save(entidad);
        return mapper.toDominio(guardada);
    }

    @Override
    public PaginacionRespuestaDTO<Solicitud> getSolicitudesPorFuncionario(String uuidFuncionario, int pagina, int tamanio) {
        Pageable paginado =
                PageRequest.of(
                        pagina,
                        tamanio,
                        Sort.by("fechaCreacion").descending()
                                .and(Sort.by("uuidSolicitud").ascending())
                );
        Page<SolicitudEntidad> page =
                repositorio.findByObjFuncionarioUuidUsuario(uuidFuncionario, paginado);

        List<Solicitud> solicitado = page.getContent().stream()
                .map(mapper::toDominio)
                .toList();
        return new PaginacionRespuestaDTO<>(solicitado, page.getTotalElements());
    }

    @Override
    public List<Solicitud> getSolicitudesPorOrdenDelDia(String uuidOrdenDelDia) {
        List<SolicitudEntidad> entidades =
                repositorio.findByObjOrdenDelDiaUuidOrdenDelDia(uuidOrdenDelDia);

        return entidades.stream()
                .map(mapper::toDominio)
                .toList();
    }

    @Override
    public List<Solicitud> getSolicitudesPorEstado(String estado) {
        List<SolicitudEntidad> entidades = repositorio.findByEstadoIgnoreCase(estado);

        return entidades.stream()
                .filter(e -> e.getObjOrdenDelDia() == null)
                .map(mapper::toDominio)
                .toList();
    }

    @Override
    public PaginacionRespuestaDTO<Solicitud> buscarSolicitudesPorNombre(String filtro, int pagina, int tamanio) {
        Pageable paginado =
                PageRequest.of(
                        pagina,
                        tamanio,
                        Sort.by("fechaCreacion").descending()
                                .and(Sort.by("uuidSolicitud").ascending())
                );
        Page<SolicitudEntidad> page = repositorio.buscarPorNombre(filtro, paginado);

        List<Solicitud> resultado = page.getContent().stream()
                .map(mapper::toDominio)
                .toList();

        return new PaginacionRespuestaDTO<>(resultado, page.getTotalElements());
    }

    @Override
    public PaginacionRespuestaDTO<Solicitud> buscarSolicitudesPorNombreYFuncionario(String uuidFuncionario, String filtro, int pagina, int tamanio) {
        Pageable paginado =
                PageRequest.of(
                        pagina,
                        tamanio,
                        Sort.by("fechaCreacion").descending()
                                .and(Sort.by("uuidSolicitud").ascending())
                );

        Page<SolicitudEntidad> page = repositorio.buscarPorNombreYFuncionario(uuidFuncionario, filtro, paginado);

        List<Solicitud> resultado = page.getContent().stream()
                .map(mapper::toDominio)
                .toList();

        return new PaginacionRespuestaDTO<>(resultado, page.getTotalElements());
    }
}
