package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.ResolucionAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ResolucionAcademica;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.ResolucionAcademicaEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.ResolucionAcademicaOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.FuncionarioAcademicoRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.ResolucionAcademicaRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.SolicitudAcademicaRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResolucionAcademicaGatewayImplAdaptador implements ResolucionAcademicaGatewayIntPuerto {
    private final ResolucionAcademicaRepositorio repositorio;
    private final SolicitudAcademicaRepositorio solicitudRepositorio;
    private final FuncionarioAcademicoRepositorio funcionarioRepositorio;
    private final ResolucionAcademicaOwnMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public ResolucionAcademica getPorSolicitud(String uuidSolicitudAcademica) {
        return repositorio.findById(uuidSolicitudAcademica)
                .map(mapper::toDominio)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorSolicitud(String uuidSolicitudAcademica) {
        return repositorio.existsById(uuidSolicitudAcademica);
    }

    @Override
    @Transactional
    public ResolucionAcademica guardar(ResolucionAcademica resolucion) {
        String uuidSolicitud = resolucion.getSolicitudAcademica().getUuidSolicitudAcademica();
        ResolucionAcademicaEntidad entidad = repositorio.findById(uuidSolicitud).orElse(null);
        if (entidad == null) {
            entidad = mapper.toEntidad(resolucion);
            entidad.setNuevo(true);
            entidad.setSolicitudAcademica(solicitudRepositorio.getReferenceById(uuidSolicitud));
        } else {
            entidad.setUrlArchivo(resolucion.getUrlArchivo());
            entidad.setNombreArchivo(resolucion.getNombreArchivo());
            entidad.setFechaSubida(resolucion.getFechaSubida());
        }
        entidad.setFuncionarioAcademico(funcionarioRepositorio.getReferenceById(resolucion.getFuncionarioAcademico().getUuidUsuario()));
        return mapper.toDominio(repositorio.saveAndFlush(entidad));
    }
}
