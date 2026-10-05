package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.EtapaSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EtapaSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.EtapaSolicitudAcademicaOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.EtapaSolicitudAcademicaRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EtapaSolicitudAcademicaGatewayImplAdaptador implements EtapaSolicitudAcademicaGatewayIntPuerto {
    private final EtapaSolicitudAcademicaRepositorio repositorio;
    private final EtapaSolicitudAcademicaOwnMapper mapper;

    @Override
    public List<EtapaSolicitudAcademica> getPorTipoIncluyendoUniversales(String uuidTipoSolicitudAcademica) {
        return repositorio.findPorTipoIncluyendoUniversales(uuidTipoSolicitudAcademica).stream()
                .map(mapper::toDominio)
                .toList();
    }
}
