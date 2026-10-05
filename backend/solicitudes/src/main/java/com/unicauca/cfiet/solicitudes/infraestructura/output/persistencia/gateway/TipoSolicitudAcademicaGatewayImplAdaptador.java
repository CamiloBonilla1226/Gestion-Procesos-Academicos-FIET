package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.TipoSolicitudAcademicaEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.TipoSolicitudAcademicaOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.FuncionarioAcademicoRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.TipoSolicitudAcademicaRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoSolicitudAcademicaGatewayImplAdaptador implements TipoSolicitudAcademicaGatewayIntPuerto {
    private final TipoSolicitudAcademicaRepositorio repositorio;
    private final FuncionarioAcademicoRepositorio funcionarioRepositorio;
    private final TipoSolicitudAcademicaOwnMapper mapper;

    @Override
    public List<TipoSolicitudAcademica> getTodos() {
        return repositorio.findAllByOrderByNombreAsc().stream()
                .map(mapper::toDominio)
                .toList();
    }

    @Override
    public boolean existePorUuid(String uuidTipoSolicitudAcademica) {
        return repositorio.existsById(uuidTipoSolicitudAcademica);
    }

    @Override
    public TipoSolicitudAcademica getPorUuid(String uuidTipoSolicitudAcademica) {
        return repositorio.findById(uuidTipoSolicitudAcademica)
                .map(mapper::toDominio)
                .orElse(null);
    }

    @Override
    public TipoSolicitudAcademica asignarFuncionarioAcademico(String uuidTipoSolicitudAcademica, String uuidFuncionarioAcademico) {
        TipoSolicitudAcademicaEntidad entidad = repositorio.findById(uuidTipoSolicitudAcademica).orElse(null);
        if (entidad == null) return null;
        entidad.setFuncionarioAcademico(funcionarioRepositorio.getReferenceById(uuidFuncionarioAcademico));
        return mapper.toDominio(repositorio.save(entidad));
    }
}
