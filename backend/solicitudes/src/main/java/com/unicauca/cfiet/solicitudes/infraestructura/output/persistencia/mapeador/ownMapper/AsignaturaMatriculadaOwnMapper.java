package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaMatriculada;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.AsignaturaMatriculadaEntidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsignaturaMatriculadaOwnMapper implements OwnMapper<AsignaturaMatriculada, AsignaturaMatriculadaEntidad> {
    private final AsignaturaOwnMapper asignaturaMapper;

    @Override
    public AsignaturaMatriculada toDominio(AsignaturaMatriculadaEntidad source) {
        return AsignaturaMatriculada.builder()
                .uuidAsignaturaMatriculada(source.getUuidAsignaturaMatriculada())
                .asignatura(source.getAsignatura() == null ? null : asignaturaMapper.toDominio(source.getAsignatura()))
                .grupo(source.getGrupo())
                .estado(source.getEstado())
                .build();
    }

    @Override
    public AsignaturaMatriculadaEntidad toEntidad(AsignaturaMatriculada source) {
        return AsignaturaMatriculadaEntidad.builder()
                .uuidAsignaturaMatriculada(source.getUuidAsignaturaMatriculada())
                .asignatura(source.getAsignatura() == null ? null : asignaturaMapper.toEntidad(source.getAsignatura()))
                .grupo(source.getGrupo())
                .estado(source.getEstado())
                .build();
    }
}
