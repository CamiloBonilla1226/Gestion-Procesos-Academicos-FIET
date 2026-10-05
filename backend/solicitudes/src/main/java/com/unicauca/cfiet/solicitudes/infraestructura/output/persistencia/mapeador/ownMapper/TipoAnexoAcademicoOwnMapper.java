package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoAnexoAcademico;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.TipoAnexoAcademicoEntidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TipoAnexoAcademicoOwnMapper implements OwnMapper<TipoAnexoAcademico, TipoAnexoAcademicoEntidad> {
    private final TipoSolicitudAcademicaOwnMapper tipoSolicitudMapper;

    @Override
    public TipoAnexoAcademico toDominio(TipoAnexoAcademicoEntidad source) {
        return TipoAnexoAcademico.builder()
                .uuidTipoAnexoAcademico(source.getUuidTipoAnexoAcademico())
                .tipoSolicitudAcademica(tipoSolicitudMapper.toDominio(source.getTipoSolicitudAcademica()))
                .nombre(source.getNombre())
                .formatosPermitidos(source.getFormatosPermitidos())
                .obligatorio(source.getObligatorio())
                .build();
    }

    @Override
    public TipoAnexoAcademicoEntidad toEntidad(TipoAnexoAcademico source) {
        return TipoAnexoAcademicoEntidad.builder()
                .uuidTipoAnexoAcademico(source.getUuidTipoAnexoAcademico())
                .tipoSolicitudAcademica(tipoSolicitudMapper.toEntidad(source.getTipoSolicitudAcademica()))
                .nombre(source.getNombre())
                .formatosPermitidos(source.getFormatosPermitidos())
                .obligatorio(source.getObligatorio())
                .build();
    }
}
