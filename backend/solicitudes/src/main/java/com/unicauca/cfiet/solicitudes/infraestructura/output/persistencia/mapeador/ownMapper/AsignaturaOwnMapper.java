package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.Asignatura;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.AsignaturaEntidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsignaturaOwnMapper implements OwnMapper<Asignatura, AsignaturaEntidad> {
    private final ProgramaAcademicoOwnMapper programaMapper;

    @Override
    public Asignatura toDominio(AsignaturaEntidad source) {
        return Asignatura.builder()
                .uuidAsignatura(source.getUuidAsignatura())
                .codigoAsignatura(source.getCodigoAsignatura())
                .nombreAsignatura(source.getNombreAsignatura())
                .programaAcademico(source.getProgramaAcademico() == null ? null : programaMapper.toDominio(source.getProgramaAcademico()))
                .build();
    }

    @Override
    public AsignaturaEntidad toEntidad(Asignatura source) {
        return AsignaturaEntidad.builder()
                .uuidAsignatura(source.getUuidAsignatura())
                .codigoAsignatura(source.getCodigoAsignatura())
                .nombreAsignatura(source.getNombreAsignatura())
                .programaAcademico(source.getProgramaAcademico() == null ? null : programaMapper.toEntidad(source.getProgramaAcademico()))
                .build();
    }
}
