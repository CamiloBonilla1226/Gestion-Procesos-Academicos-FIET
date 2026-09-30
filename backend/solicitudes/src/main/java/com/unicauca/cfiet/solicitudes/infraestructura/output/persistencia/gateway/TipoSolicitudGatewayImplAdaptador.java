package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitud;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.TipoSolicitudEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.TipoSolicitudOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.TipoSolicitudRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de la fachada con el servicio de persistencia para la gestión de tipos de solicitudes.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
@RequiredArgsConstructor
public class TipoSolicitudGatewayImplAdaptador implements TipoSolicitudGatewayIntPuerto {
    private final TipoSolicitudRepositorio repositorio;
    private final TipoSolicitudOwnMapper mapper;

    @Override
    public List<TipoSolicitud> getTiposSolicitudes() {
        List<TipoSolicitudEntidad> entidades = repositorio.findAll(Sort.by("fechaCreacion").descending());

        return entidades.stream()
                .map(mapper::toDominio)
                .toList();
    }

    @Override
    public PaginacionRespuestaDTO<TipoSolicitud> getTiposSolicitudes(int pagina, int tamanio) {
        Pageable paginado = PageRequest.of(
                pagina,
                tamanio,
                Sort.by("fechaCreacion").descending()
                        .and(Sort.by("uuidTipoSolicitud").ascending())
        );

        Page<TipoSolicitudEntidad> page = repositorio.findAll(paginado);

        List<TipoSolicitud> tipos = page.getContent().stream()
                .map(mapper::toDominio)
                .toList();

        return new PaginacionRespuestaDTO<>(tipos, page.getTotalElements());
    }

    @Override
    public PaginacionRespuestaDTO<TipoSolicitud> getTiposSolicitudes(String nombreSolicitud, String funcionario, int pagina, int tamanio) {
        Pageable paginado = PageRequest.of(
                pagina,
                tamanio,
                Sort.by("fechaCreacion").descending()
                        .and(Sort.by("uuidTipoSolicitud").ascending())
        );
        Page<TipoSolicitudEntidad> page =
                repositorio.findByNombreAndFuncionario(nombreSolicitud, funcionario, paginado);

        List<TipoSolicitud> tipos = page.getContent().stream()
                .map(mapper::toDominio)
                .toList();

        return new PaginacionRespuestaDTO<>(tipos, page.getTotalElements());
    }

    @Override
    public TipoSolicitud getTipoSolicitud(String uuidTipoSolicitud) {
        return repositorio.findById(uuidTipoSolicitud)
                .map(mapper::toDominio)
                .orElse(null);
    }

    @Override
    public TipoSolicitud guardarTipoSolicitud(TipoSolicitud tipoSolicitud) {
        TipoSolicitudEntidad entidad = mapper.toEntidad(tipoSolicitud);
        TipoSolicitudEntidad guardada = repositorio.save(entidad);
        return mapper.toDominio(guardada);
    }

    @Override
    public List<TipoSolicitud> guardarTiposSolicitud(List<TipoSolicitud> tiposSolicitud) {
        List<TipoSolicitudEntidad> entidades = tiposSolicitud.stream()
                .map(mapper::toEntidad)
                .toList();

        List<TipoSolicitudEntidad> guardadas = repositorio.saveAll(entidades);
        return guardadas.stream()
                .map(mapper::toDominio)
                .toList();
    }

    @Override
    public List<TipoSolicitud> getTiposSolicitudesPorPerfil(String perfil) {
        List<TipoSolicitudEntidad> entidades =
                repositorio.findByPerfilSolicitanteIgnoreCase(perfil);

        return entidades.stream()
                .map(mapper::toDominio)
                .toList();
    }

    @Override
    public PaginacionRespuestaDTO<TipoSolicitud> getTiposSolicitudesPorPerfilSolicitante(String perfil, int pagina, int tamanio) {
        Pageable paginado = PageRequest.of(
                pagina,
                tamanio,
                Sort.by("fechaCreacion").descending()
                        .and(Sort.by("uuidTipoSolicitud").ascending())
        );
        Page<TipoSolicitudEntidad> page = repositorio.findByPerfilSolicitanteIgnoreCase(perfil, paginado);

        List<TipoSolicitud> tipos = page.getContent().stream()
                .map(mapper::toDominio)
                .toList();

        return new PaginacionRespuestaDTO<>(tipos, page.getTotalElements());
    }

    @Override
    public PaginacionRespuestaDTO<TipoSolicitud> getTiposSolicitudesPorNombreYPerfilSolicitante(String nombre, String perfil, int pagina, int tamanio) {
        Pageable paginado = PageRequest.of(
                pagina,
                tamanio,
                Sort.by("fechaCreacion").descending()
                        .and(Sort.by("uuidTipoSolicitud").ascending())
        );

        Page<TipoSolicitudEntidad> page = repositorio.findByPerfilSolicitanteAndNombre(perfil, nombre, paginado);

        List<TipoSolicitud> tipos = page.getContent().stream()
                .map(mapper::toDominio)
                .toList();

        return new PaginacionRespuestaDTO<>(tipos, page.getTotalElements());
    }
}
