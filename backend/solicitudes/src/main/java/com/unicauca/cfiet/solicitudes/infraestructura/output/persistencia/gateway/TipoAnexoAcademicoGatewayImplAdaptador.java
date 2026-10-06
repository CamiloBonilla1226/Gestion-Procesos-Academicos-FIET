package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoAnexoAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoAnexoAcademico;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.TipoAnexoAcademicoOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.TipoAnexoAcademicoRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoAnexoAcademicoGatewayImplAdaptador implements TipoAnexoAcademicoGatewayIntPuerto {
    private final TipoAnexoAcademicoRepositorio repositorio;
    private final TipoAnexoAcademicoOwnMapper mapper;

    @Override
    public List<TipoAnexoAcademico> getPorTipo(String uuidTipoSolicitudAcademica) {
        return repositorio.findByTipoSolicitudAcademicaUuidTipoSolicitudAcademicaOrderByNombreAsc(uuidTipoSolicitudAcademica).stream()
                .map(mapper::toDominio)
                .toList();
    }

    @Override
    public TipoAnexoAcademico getPorUuid(String uuidTipoAnexoAcademico) {
        return repositorio.findById(uuidTipoAnexoAcademico)
                .map(mapper::toDominio)
                .orElse(null);
    }
}
