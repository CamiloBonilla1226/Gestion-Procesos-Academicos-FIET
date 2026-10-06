package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.CruceSupletorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudSupletorioCruceAsignaturaEntidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SolicitudSupletorioCruceAsignaturaOwnMapper implements OwnMapper<CruceSupletorio, SolicitudSupletorioCruceAsignaturaEntidad> {
    private final AsignaturaMatriculadaOwnMapper asignaturaMatriculadaMapper;

    @Override
    public CruceSupletorio toDominio(SolicitudSupletorioCruceAsignaturaEntidad source) {
        if (source == null) return null;
        return CruceSupletorio.builder()
                .asignaturaMatriculadaCruzada(source.getAsignaturaMatriculadaCruzada() == null ? null
                        : asignaturaMatriculadaMapper.toDominio(source.getAsignaturaMatriculadaCruzada()))
                .fechaExamenCruzada(source.getFechaExamenCruzada() == null ? null : source.getFechaExamenCruzada().toLocalDate())
                .horaExamenCruzada(source.getHoraExamenCruzada())
                .build();
    }

    @Override
    public SolicitudSupletorioCruceAsignaturaEntidad toEntidad(CruceSupletorio source) {
        if (source == null) return null;
        return SolicitudSupletorioCruceAsignaturaEntidad.builder()
                .fechaExamenCruzada(source.getFechaExamenCruzada() == null ? null : source.getFechaExamenCruzada().atStartOfDay())
                .horaExamenCruzada(source.getHoraExamenCruzada())
                .build();
    }
}
