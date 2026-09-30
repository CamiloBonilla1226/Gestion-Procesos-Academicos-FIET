package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.RolGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Rol;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.RolEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.RolOwnMapperImpl;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.RolRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Implementación de la fachada con el servicio de persistencia para la gestión de roles.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
@RequiredArgsConstructor
public class RolGatewayImplAdaptador implements RolGatewayIntPuerto {
    private final RolRepositorio repositorio;
    private final RolOwnMapperImpl mapper;

    @Override
    public List<Rol> getRoles() {
        return repositorio.findAll().stream()
                .map(mapper::toDominio)
                .toList();
    }

    @Override
    public List<Rol> getRoles(int pagina, int tamanio) {
        Pageable paginado = PageRequest.of(pagina, tamanio);
        List<RolEntidad> entidades = repositorio.findAll(paginado).getContent();
        return entidades.stream()
                .map(mapper::toDominio)
                .toList();
    }

    @Override
    public Rol getRol(String uuid) {
        if(repositorio.existsById(uuid)) {
            RolEntidad entidad = repositorio.findById(uuid).get();
            return mapper.toDominio(entidad);
        }
        return null;
    }

    @Override
    public Rol guardarRol(Rol rol) {
        RolEntidad rolGuardar = mapper.toEntidad(rol);
        RolEntidad rolGuardado = repositorio.save(rolGuardar);
        return mapper.toDominio(rolGuardado);
    }
}
