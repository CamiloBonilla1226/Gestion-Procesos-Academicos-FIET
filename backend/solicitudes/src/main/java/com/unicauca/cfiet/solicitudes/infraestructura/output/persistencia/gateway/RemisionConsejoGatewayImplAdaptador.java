package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.RemisionConsejoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.RemisionConsejo;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.RemisionConsejoEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.RemisionConsejoOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.RemisionConsejoRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.SolicitudAcademicaRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.SolicitudRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemisionConsejoGatewayImplAdaptador implements RemisionConsejoGatewayIntPuerto {
    private final RemisionConsejoRepositorio repositorio;
    private final SolicitudAcademicaRepositorio solicitudAcademicaRepositorio;
    private final SolicitudRepositorio solicitudRepositorio;
    private final RemisionConsejoOwnMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public RemisionConsejo getPorSolicitudAcademica(String uuidSolicitudAcademica) {
        if (uuidSolicitudAcademica == null) return null;
        return repositorio.findById(uuidSolicitudAcademica).map(mapper::toDominio).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public RemisionConsejo getPorSolicitud(String uuidSolicitud) {
        if (uuidSolicitud == null) return null;
        return repositorio.findBySolicitudUuidSolicitud(uuidSolicitud).map(mapper::toDominio).orElse(null);
    }

    @Override
    @Transactional
    public RemisionConsejo guardar(RemisionConsejo remision) {
        String uuidSolicitudAcademica = remision.getSolicitudAcademica().getUuidSolicitudAcademica();
        RemisionConsejoEntidad entidad = repositorio.findById(uuidSolicitudAcademica).orElse(null);
        if (entidad == null) {
            entidad = mapper.toEntidad(remision);
            entidad.setNuevo(true);
            entidad.setSolicitudAcademica(solicitudAcademicaRepositorio.getReferenceById(uuidSolicitudAcademica));
        }
        entidad.setSolicitud(solicitudRepositorio.getReferenceById(remision.getUuidSolicitud()));
        entidad.setFechaRemision(remision.getFechaRemision());
        return mapper.toDominio(repositorio.saveAndFlush(entidad));
    }
}
