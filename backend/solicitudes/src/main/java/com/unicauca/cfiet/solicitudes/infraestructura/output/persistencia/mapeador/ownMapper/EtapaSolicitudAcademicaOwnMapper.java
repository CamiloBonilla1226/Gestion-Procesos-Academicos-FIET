package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.EtapaSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.EtapaSolicitudAcademicaEntidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EtapaSolicitudAcademicaOwnMapper implements OwnMapper<EtapaSolicitudAcademica, EtapaSolicitudAcademicaEntidad> {
    private final TipoSolicitudAcademicaOwnMapper tipoSolicitudMapper;

    @Override
    public EtapaSolicitudAcademica toDominio(EtapaSolicitudAcademicaEntidad source) {
        if (source == null) return null;
        return EtapaSolicitudAcademica.builder()
                .uuidEtapa(source.getUuidEtapa())
                .codigo(source.getCodigo())
                .tipoSolicitudAcademica(tipoSolicitudMapper.toDominio(source.getTipoSolicitudAcademica()))
                .build();
    }

    @Override
    public EtapaSolicitudAcademicaEntidad toEntidad(EtapaSolicitudAcademica source) {
        if (source == null) return null;
        return EtapaSolicitudAcademicaEntidad.builder()
                .uuidEtapa(source.getUuidEtapa())
                .codigo(source.getCodigo())
                .tipoSolicitudAcademica(tipoSolicitudMapper.toEntidad(source.getTipoSolicitudAcademica()))
                .build();
    }
}
