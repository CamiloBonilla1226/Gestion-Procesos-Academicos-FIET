package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.AnexoAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.AnexoAcademicoEntidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnexoAcademicoOwnMapper implements OwnMapper<AnexoAcademico, AnexoAcademicoEntidad> {
    private final TipoAnexoAcademicoOwnMapper tipoAnexoMapper;

    @Override
    public AnexoAcademico toDominio(AnexoAcademicoEntidad source) {
        if (source == null) return null;
        return AnexoAcademico.builder()
                .uuidAnexoAcademico(source.getUuidAnexoAcademico())
                .solicitudAcademica(source.getSolicitudAcademica() == null ? null : SolicitudAcademica.builder()
                        .uuidSolicitudAcademica(source.getSolicitudAcademica().getUuidSolicitudAcademica())
                        .radicado(source.getSolicitudAcademica().getRadicado())
                        .build())
                .tipoAnexoAcademico(source.getTipoAnexoAcademico() == null ? null : tipoAnexoMapper.toDominio(source.getTipoAnexoAcademico()))
                .nombreArchivo(source.getNombreArchivo())
                .urlArchivo(source.getUrlArchivo())
                .tipoArchivo(source.getTipoArchivo())
                .tamanioBytes(source.getTamanioBytes())
                .usuario(source.getUsuario() == null ? null : Usuario.builder()
                        .uuidUsuario(source.getUsuario().getUuidUsuario())
                        .nombres(source.getUsuario().getNombres())
                        .apellidos(source.getUsuario().getApellidos())
                        .build())
                .fechaSubida(source.getFechaSubida())
                .build();
    }

    @Override
    public AnexoAcademicoEntidad toEntidad(AnexoAcademico source) {
        if (source == null) return null;
        return AnexoAcademicoEntidad.builder()
                .uuidAnexoAcademico(source.getUuidAnexoAcademico())
                .nombreArchivo(source.getNombreArchivo())
                .urlArchivo(source.getUrlArchivo())
                .tipoArchivo(source.getTipoArchivo())
                .tamanioBytes(source.getTamanioBytes())
                .fechaSubida(source.getFechaSubida())
                .build();
    }
}
