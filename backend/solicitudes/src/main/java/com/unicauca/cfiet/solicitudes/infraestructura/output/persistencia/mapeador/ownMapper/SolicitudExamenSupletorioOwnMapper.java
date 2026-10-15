package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.CausaSupletorio;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudExamenSupletorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudExamenSupletorioEntidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SolicitudExamenSupletorioOwnMapper implements OwnMapper<SolicitudExamenSupletorio, SolicitudExamenSupletorioEntidad> {
    private final AsignaturaMatriculadaOwnMapper asignaturaMatriculadaMapper;
    private final DocenteOwnMapper docenteMapper;

    @Override
    public SolicitudExamenSupletorio toDominio(SolicitudExamenSupletorioEntidad source) {
        if (source == null) return null;
        return SolicitudExamenSupletorio.builder()
                .solicitudAcademica(source.getSolicitudAcademica() == null ? null : SolicitudAcademica.builder()
                        .uuidSolicitudAcademica(source.getSolicitudAcademica().getUuidSolicitudAcademica())
                        .radicado(source.getSolicitudAcademica().getRadicado())
                        .build())
                .asignaturaMatriculada(source.getAsignaturaMatriculada() == null ? null
                        : asignaturaMatriculadaMapper.toDominio(source.getAsignaturaMatriculada()))
                .fechaExamenNoPresentado(source.getFechaExamenNoPresentado() == null ? null : source.getFechaExamenNoPresentado().toLocalDate())
                .tipoCausa(source.getTipoCausa() == null ? null : CausaSupletorio.valueOf(source.getTipoCausa().toUpperCase(Locale.ROOT)))
                .docente(docenteMapper.toDominio(source.getDocente()))
                .build();
    }

    @Override
    public SolicitudExamenSupletorioEntidad toEntidad(SolicitudExamenSupletorio source) {
        if (source == null) return null;
        return SolicitudExamenSupletorioEntidad.builder()
                .uuidSolicitudAcademica(source.getSolicitudAcademica() == null ? null
                        : source.getSolicitudAcademica().getUuidSolicitudAcademica())
                .fechaExamenNoPresentado(source.getFechaExamenNoPresentado() == null ? null : source.getFechaExamenNoPresentado().atStartOfDay())
                .tipoCausa(source.getTipoCausa() == null ? null : source.getTipoCausa().name().toLowerCase(Locale.ROOT))
                .build();
    }
}
