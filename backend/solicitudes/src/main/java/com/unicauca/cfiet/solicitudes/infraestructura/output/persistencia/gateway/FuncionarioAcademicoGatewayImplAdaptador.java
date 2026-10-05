package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.FuncionarioAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FuncionarioAcademico;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.FuncionarioAcademicoEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.FuncionarioAcademicoOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.FuncionarioAcademicoRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FuncionarioAcademicoGatewayImplAdaptador implements FuncionarioAcademicoGatewayIntPuerto {
    private final FuncionarioAcademicoRepositorio repositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final FuncionarioAcademicoOwnMapper mapper;

    @Override
    public FuncionarioAcademico guardar(FuncionarioAcademico funcionarioAcademico) {
        return mapper.toDominio(repositorio.save(aEntidad(funcionarioAcademico)));
    }

    @Override
    public List<FuncionarioAcademico> guardarTodos(List<FuncionarioAcademico> funcionariosAcademicos) {
        List<FuncionarioAcademicoEntidad> entidades = funcionariosAcademicos.stream()
                .map(this::aEntidad)
                .toList();
        return repositorio.saveAll(entidades).stream()
                .map(mapper::toDominio)
                .toList();
    }

    @Override
    public PaginacionRespuestaDTO<FuncionarioAcademico> getPaginado(int pagina, int tamanio) {
        return aRespuesta(repositorio.findAll(paginado(pagina, tamanio)));
    }

    @Override
    public PaginacionRespuestaDTO<FuncionarioAcademico> getPorFiltro(String nombre, String apellido, String dependencia, int pagina, int tamanio) {
        return aRespuesta(repositorio.findByFiltro(limpiar(nombre), limpiar(apellido), limpiar(dependencia), paginado(pagina, tamanio)));
    }

    @Override
    public FuncionarioAcademico getPorUuid(String uuidUsuario) {
        return repositorio.findById(uuidUsuario)
                .map(mapper::toDominio)
                .orElse(null);
    }

    private FuncionarioAcademicoEntidad aEntidad(FuncionarioAcademico funcionarioAcademico) {
        FuncionarioAcademicoEntidad entidad = mapper.toEntidad(funcionarioAcademico);
        entidad.setNuevo(!repositorio.existsById(funcionarioAcademico.getUuidUsuario()));
        entidad.setUsuario(usuarioRepositorio.getReferenceById(funcionarioAcademico.getUuidUsuario()));
        return entidad;
    }

    private String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private Pageable paginado(int pagina, int tamanio) {
        return PageRequest.of(pagina, tamanio, Sort.by("usuario.apellidos", "usuario.nombres").ascending());
    }

    private PaginacionRespuestaDTO<FuncionarioAcademico> aRespuesta(Page<FuncionarioAcademicoEntidad> page) {
        List<FuncionarioAcademico> funcionarios = page.getContent().stream()
                .map(mapper::toDominio)
                .toList();
        return new PaginacionRespuestaDTO<>(funcionarios, page.getTotalElements());
    }
}
