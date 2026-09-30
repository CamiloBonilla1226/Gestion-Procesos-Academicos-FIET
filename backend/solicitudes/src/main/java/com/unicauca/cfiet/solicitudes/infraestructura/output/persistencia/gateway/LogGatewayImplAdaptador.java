package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.LogGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Log;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.LogEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.UsuarioEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.LogOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.UsuarioOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.LogRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de la fachada con el servicio de persistencia para la gestión de roles.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
@RequiredArgsConstructor
public class LogGatewayImplAdaptador implements LogGatewayIntPuerto {
    private final LogRepositorio repositorio;
    private final LogOwnMapper logMapper;
    private final UsuarioOwnMapper usuarioMapper;

    @Override
    public Log crearLog(Log log) {
        LogEntidad entidad = logMapper.toEntidad(log);
        LogEntidad guardado = repositorio.save(entidad);
        return logMapper.toDominioBasic(guardado);
    }

    @Override
    public PaginacionRespuestaDTO<Log> getLogs(int pagina, int tamanio) {
        Pageable paginado =
                PageRequest.of(
                        pagina,
                        tamanio,
                        Sort.by("fechaCreacion").descending()
                                .and(Sort.by("uuidLog").ascending())
                );

        Page<LogEntidad> page = repositorio.findAll(paginado);

        List<Log> logs = page.getContent().stream()
                .map(logMapper::toDominio)
                .toList();

        return new PaginacionRespuestaDTO<>(logs, page.getTotalElements());
    }

    @Override
    public List<Log> getLogs() {
        return repositorio.findAll(Sort.by("fechaCreacion").descending())
                .stream()
                .map(logMapper::toDominio)
                .toList();
    }

    @Override
    public PaginacionRespuestaDTO<Log> getLogs(String responsable, String fecha, int pagina, int tamanio) {
        Pageable paginado =
                PageRequest.of(
                        pagina,
                        tamanio,
                        Sort.by("fechaCreacion").descending()
                                .and(Sort.by("uuidLog").ascending())
                );

        Page<LogEntidad> page =
                repositorio.findByResponsableAndFecha(responsable, fecha, paginado);

        List<Log> logs = page.getContent().stream()
                .map(logMapper::toDominio)
                .toList();

        return new PaginacionRespuestaDTO<>(logs, page.getTotalElements());
    }

    @Override
    public Usuario getUsuarioUsername(String username) {
        Optional<UsuarioEntidad> entidad = repositorio.findUsuarioByUsername(username);

        return entidad.map(usuarioMapper::toDominioSinLogsNiRoles)
                .orElse(null);
    }
}