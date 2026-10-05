package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.FuncionarioAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FuncionarioAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.FuncionarioAcademicoEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.TipoSolicitudAcademicaEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.FuncionarioAcademicoOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.FuncionarioAcademicoRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.TipoSolicitudAcademicaRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FuncionarioAcademicoGatewayImplAdaptador implements FuncionarioAcademicoGatewayIntPuerto {
    private final FuncionarioAcademicoRepositorio repositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final TipoSolicitudAcademicaRepositorio tipoSolicitudRepositorio;
    private final FuncionarioAcademicoOwnMapper mapper;

    @Override
    public FuncionarioAcademico guardar(FuncionarioAcademico funcionarioAcademico) {
        return conTiposSolicitud(List.of(mapper.toDominio(repositorio.save(aEntidad(funcionarioAcademico))))).get(0);
    }

    @Override
    public List<FuncionarioAcademico> guardarTodos(List<FuncionarioAcademico> funcionariosAcademicos) {
        List<FuncionarioAcademicoEntidad> entidades = funcionariosAcademicos.stream()
                .map(this::aEntidad)
                .toList();
        return conTiposSolicitud(repositorio.saveAll(entidades).stream()
                .map(mapper::toDominio)
                .toList());
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
                .map(entidad -> conTiposSolicitud(List.of(mapper.toDominio(entidad))).get(0))
                .orElse(null);
    }

    private FuncionarioAcademicoEntidad aEntidad(FuncionarioAcademico funcionarioAcademico) {
        FuncionarioAcademicoEntidad entidad = mapper.toEntidad(funcionarioAcademico);
        entidad.setNuevo(!repositorio.existsById(funcionarioAcademico.getUuidUsuario()));
        entidad.setUsuario(usuarioRepositorio.getReferenceById(funcionarioAcademico.getUuidUsuario()));
        return entidad;
    }

    private List<FuncionarioAcademico> conTiposSolicitud(List<FuncionarioAcademico> funcionarios) {
        if (funcionarios.isEmpty()) return funcionarios;
        List<String> uuids = funcionarios.stream().map(FuncionarioAcademico::getUuidUsuario).toList();
        Map<String, List<TipoSolicitudAcademica>> tiposPorFuncionario = tipoSolicitudRepositorio
                .findByFuncionarioAcademicoUuidUsuarioInOrderByNombreAsc(uuids).stream()
                .collect(Collectors.groupingBy(
                        tipo -> tipo.getFuncionarioAcademico().getUuidUsuario(),
                        Collectors.mapping(this::tipoSolicitudResumido, Collectors.toList())));
        for (FuncionarioAcademico funcionario : funcionarios)
            funcionario.setTiposSolicitud(new ArrayList<>(tiposPorFuncionario.getOrDefault(funcionario.getUuidUsuario(), List.of())));
        return funcionarios;
    }

    private TipoSolicitudAcademica tipoSolicitudResumido(TipoSolicitudAcademicaEntidad entidad) {
        return TipoSolicitudAcademica.builder()
                .uuidTipoSolicitudAcademica(entidad.getUuidTipoSolicitudAcademica())
                .nombre(entidad.getNombre())
                .descripcion(entidad.getDescripcion())
                .build();
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
        return new PaginacionRespuestaDTO<>(conTiposSolicitud(funcionarios), page.getTotalElements());
    }
}
