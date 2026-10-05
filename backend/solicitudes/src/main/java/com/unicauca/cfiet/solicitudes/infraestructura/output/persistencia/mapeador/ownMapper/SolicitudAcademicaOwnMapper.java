package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudAcademica;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudAcademicaEntidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SolicitudAcademicaOwnMapper implements OwnMapper<SolicitudAcademica, SolicitudAcademicaEntidad> {
    private final EstudianteOwnMapper estudianteMapper;
    private final TipoSolicitudAcademicaOwnMapper tipoSolicitudMapper;
    private final EtapaSolicitudAcademicaOwnMapper etapaMapper;

    @Override
    public SolicitudAcademica toDominio(SolicitudAcademicaEntidad source) {
        if (source == null) return null;
        return SolicitudAcademica.builder()
                .uuidSolicitudAcademica(source.getUuidSolicitudAcademica())
                .radicado(source.getRadicado())
                .estudiante(source.getEstudiante() == null ? null : estudianteMapper.toDominio(source.getEstudiante()))
                .tipoSolicitudAcademica(tipoSolicitudMapper.toDominio(source.getTipoSolicitudAcademica()))
                .etapa(etapaMapper.toDominio(source.getEtapa()))
                .fechaCreacion(source.getFechaCreacion())
                .build();
    }

    @Override
    public SolicitudAcademicaEntidad toEntidad(SolicitudAcademica source) {
        if (source == null) return null;
        return SolicitudAcademicaEntidad.builder()
                .uuidSolicitudAcademica(source.getUuidSolicitudAcademica())
                .radicado(source.getRadicado())
                .fechaCreacion(source.getFechaCreacion())
                .build();
    }
}
