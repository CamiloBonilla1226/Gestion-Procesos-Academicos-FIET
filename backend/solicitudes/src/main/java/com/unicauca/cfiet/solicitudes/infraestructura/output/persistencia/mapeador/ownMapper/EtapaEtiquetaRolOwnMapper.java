package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.EtapaEtiquetaRol;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.EtapaEtiquetaRolEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.EtapaEtiquetaRolIdEntidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EtapaEtiquetaRolOwnMapper implements OwnMapper<EtapaEtiquetaRol, EtapaEtiquetaRolEntidad> {
    private final EtapaSolicitudAcademicaOwnMapper etapaMapper;

    @Override
    public EtapaEtiquetaRol toDominio(EtapaEtiquetaRolEntidad source) {
        return EtapaEtiquetaRol.builder()
                .etapa(etapaMapper.toDominio(source.getEtapa()))
                .rol(source.getId().getRol())
                .etiqueta(source.getEtiqueta())
                .build();
    }

    @Override
    public EtapaEtiquetaRolEntidad toEntidad(EtapaEtiquetaRol source) {
        String uuidEtapa = source.getEtapa() == null ? null : source.getEtapa().getUuidEtapa();
        return EtapaEtiquetaRolEntidad.builder()
                .id(new EtapaEtiquetaRolIdEntidad(uuidEtapa, source.getRol()))
                .etapa(etapaMapper.toEntidad(source.getEtapa()))
                .etiqueta(source.getEtiqueta())
                .build();
    }
}
