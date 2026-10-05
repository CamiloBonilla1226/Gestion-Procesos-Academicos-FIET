package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.HistorialSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.HistorialSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.HistorialSolicitudAcademicaOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.HistorialSolicitudAcademicaRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistorialSolicitudAcademicaGatewayImplAdaptador implements HistorialSolicitudAcademicaGatewayIntPuerto {
    private final HistorialSolicitudAcademicaRepositorio repositorio;
    private final HistorialSolicitudAcademicaOwnMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<HistorialSolicitudAcademica> getPorSolicitud(String uuidSolicitudAcademica) {
        return repositorio.findBySolicitudAcademicaUuidSolicitudAcademicaOrderByFechaAsc(uuidSolicitudAcademica).stream()
                .map(mapper::toDominio)
                .toList();
    }
}
