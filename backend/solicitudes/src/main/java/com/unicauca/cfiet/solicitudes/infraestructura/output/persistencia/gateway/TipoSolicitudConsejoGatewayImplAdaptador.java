package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudConsejoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudConsejo;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.TipoSolicitudConsejoEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.TipoSolicitudConsejoOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.FuncionarioAcademicoRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.TipoSolicitudAcademicaRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.TipoSolicitudConsejoRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.TipoSolicitudRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TipoSolicitudConsejoGatewayImplAdaptador implements TipoSolicitudConsejoGatewayIntPuerto {
    private final TipoSolicitudConsejoRepositorio repositorio;
    private final TipoSolicitudRepositorio tipoSolicitudRepositorio;
    private final TipoSolicitudAcademicaRepositorio tipoAcademicoRepositorio;
    private final FuncionarioAcademicoRepositorio funcionarioRepositorio;
    private final TipoSolicitudConsejoOwnMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public TipoSolicitudConsejo getPorTipoSolicitud(String uuidTipoSolicitud) {
        if (uuidTipoSolicitud == null) return null;
        return repositorio.findById(uuidTipoSolicitud).map(mapper::toDominio).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public TipoSolicitudConsejo getPorTipoAcademico(String uuidTipoSolicitudAcademica) {
        if (uuidTipoSolicitudAcademica == null) return null;
        return repositorio.findByTipoSolicitudAcademicaUuidTipoSolicitudAcademica(uuidTipoSolicitudAcademica)
                .map(mapper::toDominio)
                .orElse(null);
    }

    @Override
    @Transactional
    public TipoSolicitudConsejo guardar(TipoSolicitudConsejo enlace) {
        TipoSolicitudConsejoEntidad entidad = repositorio.findById(enlace.getUuidTipoSolicitud()).orElse(null);
        if (entidad == null) {
            entidad = mapper.toEntidad(enlace);
            entidad.setNuevo(true);
            entidad.setTipoSolicitud(tipoSolicitudRepositorio.getReferenceById(enlace.getUuidTipoSolicitud()));
        }
        entidad.setTipoSolicitudAcademica(tipoAcademicoRepositorio.getReferenceById(
                enlace.getTipoSolicitudAcademica().getUuidTipoSolicitudAcademica()));
        entidad.setEncargadoAcademico(enlace.getEncargadoAcademico() == null ? null
                : funcionarioRepositorio.getReferenceById(enlace.getEncargadoAcademico().getUuidUsuario()));
        return mapper.toDominio(repositorio.saveAndFlush(entidad));
    }

    @Override
    @Transactional
    public void eliminarPorTipoSolicitud(String uuidTipoSolicitud) {
        repositorio.findById(uuidTipoSolicitud).ifPresent(entidad -> {
            repositorio.delete(entidad);
            repositorio.flush();
        });
    }
}
