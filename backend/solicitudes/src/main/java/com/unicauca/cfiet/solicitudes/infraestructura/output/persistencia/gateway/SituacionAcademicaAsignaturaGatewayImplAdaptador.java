package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.SituacionAcademicaAsignaturaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SituacionAcademicaAsignatura;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.SituacionAcademicaAsignaturaOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.SituacionAcademicaAsignaturaRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SituacionAcademicaAsignaturaGatewayImplAdaptador implements SituacionAcademicaAsignaturaGatewayIntPuerto {
    private final SituacionAcademicaAsignaturaRepositorio repositorio;
    private final SituacionAcademicaAsignaturaOwnMapper mapper;

    @Override
    public List<SituacionAcademicaAsignatura> getTodas() {
        return repositorio.findAllByOrderByCodigoAsc().stream()
                .map(mapper::toDominio)
                .toList();
    }
}
