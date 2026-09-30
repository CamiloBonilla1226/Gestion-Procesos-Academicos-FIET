package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.OrdenDelDiaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.OrdenDelDia;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.OrdenDelDiaEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.OrdenDelDiaOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.OrdenDelDiaRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Implementación de la fachada con el servicio de persistencia para la gestión de ordenes del día.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
@RequiredArgsConstructor
public class OrdenDelDiaGatewayImplAdaptador implements OrdenDelDiaGatewayIntPuerto {
    private final OrdenDelDiaRepositorio repositorio;
    private final OrdenDelDiaOwnMapper mapper;

    @Override
    public List<OrdenDelDia> getOrdenesDelDia() {
        List<OrdenDelDiaEntidad> entidades =
                repositorio.findAll(Sort.by("fechaCreacion").descending());
        return entidades.stream()
                .map(mapper::toDominio)
                .toList();
    }

    @Override
    public List<OrdenDelDia> getOrdenesDelDiaPorEstado(boolean estado) {
        List<OrdenDelDiaEntidad> entidades = repositorio.findByEstado(estado);
        return entidades.stream()
                .map(mapper::toDominio)
                .toList();
    }

    @Override
    public PaginacionRespuestaDTO<OrdenDelDia> getOrdenesDelDia(int pagina, int tamanio) {
        Pageable paginado =
                PageRequest.of(
                        pagina,
                        tamanio,
                        Sort.by("fechaCreacion").descending()
                                .and(Sort.by("uuidOrdenDelDia").ascending())
                );

        Page<OrdenDelDiaEntidad> page = repositorio.findAll(paginado);
        List<OrdenDelDia> lista = page.getContent().stream()
                .map(mapper::toDominio)
                .toList();
        return new PaginacionRespuestaDTO<>(lista, page.getTotalElements());
    }

    @Override
    public OrdenDelDia getOrdenDelDia(String uuidOrdenDelDia) {
        return repositorio.findById(uuidOrdenDelDia)
                .map(mapper::toDominio)
                .orElse(null);
    }

    @Override
    public OrdenDelDia guardarOrdenDelDia(OrdenDelDia ordenDelDia) {
        OrdenDelDiaEntidad entidad = mapper.toEntidad(ordenDelDia);
        OrdenDelDiaEntidad guardada = repositorio.save(entidad);
        return mapper.toDominio(guardada);
    }

    @Override
    public PaginacionRespuestaDTO<OrdenDelDia> getOrdenesDelDia(String filtro, int pagina, int tamanio) {
        Pageable paginado =
                PageRequest.of(
                        pagina,
                        tamanio,
                        Sort.by("fechaCreacion").descending()
                                .and(Sort.by("uuidOrdenDelDia").ascending())
                );

        Page<OrdenDelDiaEntidad> page =
                repositorio.findByNumeroActaContainingIgnoreCase(filtro, paginado);

        List<OrdenDelDia> lista = page.getContent().stream()
                .map(mapper::toDominio)
                .toList();
        return new PaginacionRespuestaDTO<>(lista, page.getTotalElements());
    }
}
