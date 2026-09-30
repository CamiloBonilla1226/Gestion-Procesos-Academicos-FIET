package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.*;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.*;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.FuncionarioRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.UsuarioLivianoRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

/**
 * Implementación de la fachada con el servicio de persistencia para la gestión de usuarios.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
@RequiredArgsConstructor
public class UsuarioGatewayImplAdapter implements UsuarioGatewayIntPuerto {
    private final UsuarioLivianoRepositorio repositorioBasico;
    private final UsuarioRepositorio repositorio;
    private final FuncionarioRepositorio repositorioFuncionarios;
    private final UsuarioOwnMapper usuarioMapper;
    private final UsuarioLivianoOwnMapper usuarioLivianoOwnMapper;
    private final FuncionarioOwnMapper funcionarioMapper;
    private final TipoUsuarioOwnMapper tipoUsuarioMapper;
    private final RolOwnMapperImpl rolMapper;

    @Override
    public List<UsuarioLiviano> getUsuarios() {
        List<UsuarioLivianoEntidad> entidades = repositorioBasico.findAll(Sort.by("fechaCreacion").descending());

        return entidades.stream()
                .map(usuarioLivianoOwnMapper::toDominio)
                .toList();
    }

    @Override
    public PaginacionRespuestaDTO<UsuarioLiviano> getUsuarios(int pagina, int tamanio) {
        Pageable paginado = PageRequest.of(
                pagina,
                tamanio,
                Sort.by("fechaCreacion").descending()
                        .and(Sort.by("uuidUsuario").ascending())
        );

        Page<UsuarioLivianoEntidad> page = repositorioBasico.findAll(paginado);

        List<UsuarioLiviano> usuarios = page.getContent().stream()
                .map(usuarioLivianoOwnMapper::toDominio)
                .toList();

        return new PaginacionRespuestaDTO<>(usuarios, page.getTotalElements());
    }

    @Override
    public PaginacionRespuestaDTO<UsuarioLiviano> getUsuariosByNombreCompleto(
            String nombreCompleto, int pagina, int tamanio) {

        Pageable paginado = PageRequest.of(
                pagina,
                tamanio,
                Sort.by("fechaCreacion").descending()
                        .and(Sort.by("uuidUsuario").ascending())
        );

        Page<UsuarioLivianoEntidad> page =
                repositorioBasico.findByNombreCompleto(nombreCompleto, paginado);

        List<UsuarioLiviano> usuarios = page.getContent().stream()
                .map(usuarioLivianoOwnMapper::toDominio)
                .toList();

        return new PaginacionRespuestaDTO<>(usuarios, page.getTotalElements());
    }

    @Override
    public List<Funcionario> getFuncionarios() {
        List<FuncionarioEntidad> entidades = repositorioFuncionarios.findAll();
        return entidades.stream()
                .map(funcionarioMapper::toDominio)
                .toList();
    }

    @Override
    public Usuario getUsuario(String uuid) {
        return repositorio.findById(uuid)
                .map(e -> {
                    if (e instanceof FuncionarioEntidad fe)
                        return funcionarioMapper.toDominio(fe);
                    return usuarioMapper.toDominio(e);
                })
                .orElse(null);
    }

    @Override
    public Usuario guardarUsuario(Usuario usuario) {
        UsuarioEntidad entidad;

        if (usuario instanceof Funcionario f)
            entidad = funcionarioMapper.toEntidad(f);
        else
            entidad = usuarioMapper.toEntidad(usuario);

        UsuarioEntidad guardado = repositorio.save(entidad);

        if (guardado instanceof FuncionarioEntidad fe)
            return funcionarioMapper.toDominio(fe);

        return usuarioMapper.toDominio(guardado);
    }

    @Override
    public List<Usuario> guardarUsuarios(List<Usuario> usuarios) {
        List<UsuarioEntidad> entidades = usuarios.stream()
                .map(u -> (u instanceof Funcionario f)
                        ? funcionarioMapper.toEntidad(f)
                        : usuarioMapper.toEntidad(u))
                .toList();

        List<UsuarioEntidad> guardados = repositorio.saveAll(entidades);

        return guardados.stream()
                .map(e -> {
                    if (e instanceof FuncionarioEntidad fe)
                        return funcionarioMapper.toDominio(fe);
                    return usuarioMapper.toDominio(e);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public boolean existeUsuarioNumeroDocumento(String numeroDocumento) {
        return repositorio.existsByNumeroDocumento(numeroDocumento);
    }

    @Override
    public boolean existeUsuarioCorreo(String correo) {
        return repositorio.existsByCorreoElectronico(correo);
    }

    @Override
    public boolean existeUsuarioUsername(String username) {
        return repositorio.existsByUsername(username);
    }

    @Override
    public List<TipoUsuario> getTiposUsuario() {
        return repositorio.findAllTipoUsuario().stream()
                .map(tipoUsuarioMapper::toDominio)
                .toList();
    }

    @Override
    public TipoUsuario getTipoUsuarioPorNombre(String nombre) {
        return repositorio.findTipoUsuarioByNombre(nombre)
                .map(tipoUsuarioMapper::toDominio)
                .orElse(null);
    }

    @Override
    public List<Rol> getRoles() {
        return repositorio.findAllRoles().stream()
                .map(rolMapper::toDominio)
                .toList();
    }
}
