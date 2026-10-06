package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudCancelacionAsignaturaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudCancelacionAsignatura;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudCancelacionAsignaturaEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.AsignaturaSolicitudAcademicaOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.SolicitudCancelacionAsignaturaOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.AsignaturaSolicitudAcademicaRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.SolicitudAcademicaRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.SolicitudCancelacionAsignaturaRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SolicitudCancelacionAsignaturaGatewayImplAdaptador implements SolicitudCancelacionAsignaturaGatewayIntPuerto {
    private final SolicitudCancelacionAsignaturaRepositorio repositorio;
    private final SolicitudAcademicaRepositorio solicitudRepositorio;
    private final AsignaturaSolicitudAcademicaRepositorio asignaturaSolicitudRepositorio;
    private final SolicitudCancelacionAsignaturaOwnMapper mapper;
    private final AsignaturaSolicitudAcademicaOwnMapper asignaturaMapper;

    @Override
    @Transactional
    public SolicitudCancelacionAsignatura guardar(SolicitudCancelacionAsignatura cancelacion) {
        SolicitudCancelacionAsignaturaEntidad entidad = mapper.toEntidad(cancelacion);
        entidad.setSolicitudAcademica(solicitudRepositorio.getReferenceById(cancelacion.getSolicitudAcademica().getUuidSolicitudAcademica()));
        entidad.setNuevo(true);
        return mapper.toDominio(repositorio.saveAndFlush(entidad));
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudCancelacionAsignatura getPorSolicitud(String uuidSolicitudAcademica) {
        return repositorio.findById(uuidSolicitudAcademica)
                .map(entidad -> {
                    SolicitudCancelacionAsignatura cancelacion = mapper.toDominio(entidad);
                    cancelacion.setAsignaturas(asignaturaSolicitudRepositorio.findBySolicitudAcademica_UuidSolicitudAcademica(uuidSolicitudAcademica)
                            .stream().map(asignaturaMapper::toDominio).toList());
                    return cancelacion;
                })
                .orElse(null);
    }
}
