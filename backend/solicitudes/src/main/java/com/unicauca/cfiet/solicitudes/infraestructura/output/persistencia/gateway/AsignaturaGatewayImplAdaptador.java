package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.AsignaturaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Asignatura;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.AsignaturaEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.AsignaturaOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.AsignaturaRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AsignaturaGatewayImplAdaptador implements AsignaturaGatewayIntPuerto {
    private final AsignaturaRepositorio repositorio;
    private final AsignaturaOwnMapper mapper;

    @Override
    public boolean existePorCodigo(String codigoAsignatura) {
        return repositorio.existsByCodigoAsignatura(codigoAsignatura);
    }

    @Override
    public Asignatura getPorCodigo(String codigoAsignatura) {
        return repositorio.findByCodigoAsignatura(codigoAsignatura)
                .map(mapper::toDominio)
                .orElse(null);
    }

    @Override
    public Asignatura guardar(Asignatura asignatura) {
        AsignaturaEntidad guardada = repositorio.save(mapper.toEntidad(asignatura));
        return mapper.toDominio(guardada);
    }

    @Override
    public PaginacionRespuestaDTO<Asignatura> getPaginado(int pagina, int tamanio) {
        return aRespuesta(repositorio.findAll(paginado(pagina, tamanio)));
    }

    @Override
    public PaginacionRespuestaDTO<Asignatura> getPorFiltro(String texto, int pagina, int tamanio) {
        String filtro = texto == null || texto.isBlank() ? null : texto.trim();
        return aRespuesta(repositorio.findByTexto(filtro, paginado(pagina, tamanio)));
    }

    @Override
    public Asignatura getPorUuid(String uuidAsignatura) {
        return repositorio.findById(uuidAsignatura)
                .map(mapper::toDominio)
                .orElse(null);
    }

    private Pageable paginado(int pagina, int tamanio) {
        return PageRequest.of(pagina, tamanio, Sort.by("codigoAsignatura").ascending());
    }

    private PaginacionRespuestaDTO<Asignatura> aRespuesta(Page<AsignaturaEntidad> page) {
        List<Asignatura> asignaturas = page.getContent().stream()
                .map(mapper::toDominio)
                .toList();
        return new PaginacionRespuestaDTO<>(asignaturas, page.getTotalElements());
    }
}
