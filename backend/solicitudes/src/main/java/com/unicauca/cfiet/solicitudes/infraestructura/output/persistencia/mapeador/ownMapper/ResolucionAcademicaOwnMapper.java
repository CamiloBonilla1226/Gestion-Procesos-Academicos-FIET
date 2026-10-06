package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.FuncionarioAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ResolucionAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudAcademica;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.ResolucionAcademicaEntidad;
import org.springframework.stereotype.Service;

@Service
public class ResolucionAcademicaOwnMapper implements OwnMapper<ResolucionAcademica, ResolucionAcademicaEntidad> {

    @Override
    public ResolucionAcademica toDominio(ResolucionAcademicaEntidad source) {
        if (source == null) return null;
        return ResolucionAcademica.builder()
                .solicitudAcademica(SolicitudAcademica.builder()
                        .uuidSolicitudAcademica(source.getUuidSolicitudAcademica())
                        .radicado(source.getSolicitudAcademica() == null ? null : source.getSolicitudAcademica().getRadicado())
                        .build())
                .urlArchivo(source.getUrlArchivo())
                .nombreArchivo(source.getNombreArchivo())
                .fechaSubida(source.getFechaSubida())
                .funcionarioAcademico(source.getFuncionarioAcademico() == null ? null : FuncionarioAcademico.builder()
                        .uuidUsuario(source.getFuncionarioAcademico().getUuidUsuario())
                        .build())
                .build();
    }

    @Override
    public ResolucionAcademicaEntidad toEntidad(ResolucionAcademica source) {
        if (source == null) return null;
        return ResolucionAcademicaEntidad.builder()
                .uuidSolicitudAcademica(source.getSolicitudAcademica() == null ? null : source.getSolicitudAcademica().getUuidSolicitudAcademica())
                .urlArchivo(source.getUrlArchivo())
                .nombreArchivo(source.getNombreArchivo())
                .fechaSubida(source.getFechaSubida())
                .build();
    }
}
