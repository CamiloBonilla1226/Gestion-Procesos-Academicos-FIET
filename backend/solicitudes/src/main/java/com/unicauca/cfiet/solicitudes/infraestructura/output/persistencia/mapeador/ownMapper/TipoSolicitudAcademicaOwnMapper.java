package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.TipoSolicitudAcademicaEntidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TipoSolicitudAcademicaOwnMapper implements OwnMapper<TipoSolicitudAcademica, TipoSolicitudAcademicaEntidad> {
    private final FuncionarioAcademicoOwnMapper funcionarioAcademicoMapper;

    @Override
    public TipoSolicitudAcademica toDominio(TipoSolicitudAcademicaEntidad source) {
        if (source == null) return null;
        return TipoSolicitudAcademica.builder()
                .uuidTipoSolicitudAcademica(source.getUuidTipoSolicitudAcademica())
                .nombre(source.getNombre())
                .descripcion(source.getDescripcion())
                .funcionarioAcademico(source.getFuncionarioAcademico() == null ? null
                        : funcionarioAcademicoMapper.toDominio(source.getFuncionarioAcademico()))
                .build();
    }

    @Override
    public TipoSolicitudAcademicaEntidad toEntidad(TipoSolicitudAcademica source) {
        if (source == null) return null;
        return TipoSolicitudAcademicaEntidad.builder()
                .uuidTipoSolicitudAcademica(source.getUuidTipoSolicitudAcademica())
                .nombre(source.getNombre())
                .descripcion(source.getDescripcion())
                .funcionarioAcademico(source.getFuncionarioAcademico() == null ? null
                        : funcionarioAcademicoMapper.toEntidad(source.getFuncionarioAcademico()))
                .build();
    }
}
