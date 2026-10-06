package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.AnexoAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AnexoAcademico;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.AnexoAcademicoEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.AnexoAcademicoOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.AnexoAcademicoRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.SolicitudAcademicaRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.TipoAnexoAcademicoRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnexoAcademicoGatewayImplAdaptador implements AnexoAcademicoGatewayIntPuerto {
    private final AnexoAcademicoRepositorio repositorio;
    private final SolicitudAcademicaRepositorio solicitudRepositorio;
    private final TipoAnexoAcademicoRepositorio tipoAnexoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final AnexoAcademicoOwnMapper mapper;

    @Override
    @Transactional
    public AnexoAcademico guardar(AnexoAcademico anexo) {
        AnexoAcademicoEntidad entidad = mapper.toEntidad(anexo);
        entidad.setSolicitudAcademica(solicitudRepositorio.getReferenceById(anexo.getSolicitudAcademica().getUuidSolicitudAcademica()));
        entidad.setTipoAnexoAcademico(anexo.getTipoAnexoAcademico() == null ? null
                : tipoAnexoRepositorio.getReferenceById(anexo.getTipoAnexoAcademico().getUuidTipoAnexoAcademico()));
        entidad.setUsuario(usuarioRepositorio.getReferenceById(anexo.getUsuario().getUuidUsuario()));
        return mapper.toDominio(repositorio.saveAndFlush(entidad));
    }

    @Override
    @Transactional(readOnly = true)
    public AnexoAcademico getPorUuid(String uuidAnexoAcademico) {
        return repositorio.findById(uuidAnexoAcademico)
                .map(mapper::toDominio)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnexoAcademico> getPorSolicitud(String uuidSolicitudAcademica) {
        return repositorio.findBySolicitudAcademicaUuidSolicitudAcademicaOrderByFechaSubidaAsc(uuidSolicitudAcademica).stream()
                .map(mapper::toDominio)
                .toList();
    }
}
