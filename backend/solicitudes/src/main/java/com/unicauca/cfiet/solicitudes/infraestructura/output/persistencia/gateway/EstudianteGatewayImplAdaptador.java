package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.EstudianteGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaMatriculada;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Estudiante;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.AsignaturaMatriculadaEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.EstudianteEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.AsignaturaMatriculadaOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.EstudianteOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.AsignaturaMatriculadaRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.AsignaturaRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.EstudianteRepositorio;
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
public class EstudianteGatewayImplAdaptador implements EstudianteGatewayIntPuerto {
    private final EstudianteRepositorio repositorio;
    private final AsignaturaMatriculadaRepositorio asignaturaMatriculadaRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final AsignaturaRepositorio asignaturaRepositorio;
    private final EstudianteOwnMapper mapper;
    private final AsignaturaMatriculadaOwnMapper asignaturaMatriculadaMapper;

    @Override
    public boolean existePorCodigoEstudiantil(String codigoEstudiantil) {
        return repositorio.existsByCodigoEstudiantil(codigoEstudiantil);
    }

    @Override
    public boolean existePorUuid(String uuidUsuario) {
        return repositorio.existsById(uuidUsuario);
    }

    @Override
    public Estudiante guardar(Estudiante estudiante) {
        EstudianteEntidad guardado = repositorio.save(aEntidad(estudiante));
        return mapper.toDominio(guardado);
    }

    @Override
    public List<Estudiante> guardarTodos(List<Estudiante> estudiantes) {
        List<EstudianteEntidad> entidades = estudiantes.stream()
                .map(this::aEntidad)
                .toList();
        return repositorio.saveAll(entidades).stream()
                .map(mapper::toDominio)
                .toList();
    }

    @Override
    public PaginacionRespuestaDTO<Estudiante> getPaginado(int pagina, int tamanio) {
        return aRespuesta(repositorio.findAll(paginado(pagina, tamanio)));
    }

    @Override
    public PaginacionRespuestaDTO<Estudiante> getPorFiltro(String nombre, String apellido, String codigo, int pagina, int tamanio) {
        return aRespuesta(repositorio.findByFiltro(limpiar(nombre), limpiar(apellido), limpiar(codigo), paginado(pagina, tamanio)));
    }

    @Override
    public Estudiante getPorUuid(String uuidUsuario) {
        return repositorio.findById(uuidUsuario)
                .map(mapper::toDominio)
                .orElse(null);
    }

    @Override
    public List<AsignaturaMatriculada> getAsignaturasMatriculadas(String uuidUsuario) {
        return asignaturaMatriculadaRepositorio.findByEstudianteUuidUsuarioOrderByAsignaturaCodigoAsignaturaAsc(uuidUsuario).stream()
                .map(asignaturaMatriculadaMapper::toDominio)
                .toList();
    }

    @Override
    public AsignaturaMatriculada getAsignaturaMatriculada(String uuidUsuario, String uuidAsignaturaMatriculada) {
        return asignaturaMatriculadaRepositorio.findByUuidAsignaturaMatriculadaAndEstudianteUuidUsuario(uuidAsignaturaMatriculada, uuidUsuario)
                .map(asignaturaMatriculadaMapper::toDominio)
                .orElse(null);
    }

    @Override
    public AsignaturaMatriculada guardarAsignaturaMatriculada(String uuidUsuario, AsignaturaMatriculada asignaturaMatriculada) {
        AsignaturaMatriculadaEntidad entidad = aEntidad(asignaturaMatriculada);
        entidad.setEstudiante(repositorio.getReferenceById(uuidUsuario));
        return asignaturaMatriculadaMapper.toDominio(asignaturaMatriculadaRepositorio.save(entidad));
    }

    private EstudianteEntidad aEntidad(Estudiante estudiante) {
        EstudianteEntidad entidad = mapper.toEntidad(estudiante);
        entidad.setNuevo(!repositorio.existsById(estudiante.getUuidUsuario()));
        entidad.setUsuario(usuarioRepositorio.getReferenceById(estudiante.getUuidUsuario()));
        for (AsignaturaMatriculadaEntidad asignatura : entidad.getAsignaturasMatriculadas())
            asignatura.setAsignatura(asignaturaRepositorio.getReferenceById(asignatura.getAsignatura().getUuidAsignatura()));
        return entidad;
    }

    private AsignaturaMatriculadaEntidad aEntidad(AsignaturaMatriculada asignaturaMatriculada) {
        AsignaturaMatriculadaEntidad entidad = asignaturaMatriculadaMapper.toEntidad(asignaturaMatriculada);
        entidad.setAsignatura(asignaturaRepositorio.getReferenceById(asignaturaMatriculada.getAsignatura().getUuidAsignatura()));
        return entidad;
    }

    private String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private Pageable paginado(int pagina, int tamanio) {
        return PageRequest.of(pagina, tamanio, Sort.by("codigoEstudiantil").ascending());
    }

    private PaginacionRespuestaDTO<Estudiante> aRespuesta(Page<EstudianteEntidad> page) {
        List<Estudiante> estudiantes = page.getContent().stream()
                .map(mapper::toDominio)
                .toList();
        return new PaginacionRespuestaDTO<>(estudiantes, page.getTotalElements());
    }
}
