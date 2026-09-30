package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.Anexo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Solicitud;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.AnexoEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudEntidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
@RequiredArgsConstructor
public class SolicitudOwnMapper implements OwnMapper<Solicitud, SolicitudEntidad> {
    private final TipoSolicitudOwnMapper tipoSolicitudOwnMapper;
    private final OrdenDelDiaOwnMapper ordenDelDiaOwnMapper;
    private final InformacionSolicitanteOwnMapper informacionSolicitanteOwnMapper;
    private final FuncionarioOwnMapper funcionarioOwnMapper;
    private final AnexoOwnMapper anexoOwnMapper;

    @Override
    public Solicitud toDominio(SolicitudEntidad source) {
        if (source == null) return null;
        return Solicitud.builder()
                .uuidSolicitud(source.getUuidSolicitud())
                .consecutivo(source.getConsecutivo())
                .nombre(source.getNombre())
                .descripcion(source.getDescripcion())
                .estado(source.getEstado())
                .objTipoSolicitud(tipoSolicitudOwnMapper.toDominioBasic(source.getObjTipoSolicitud()))
                .objOrdenDelDia(ordenDelDiaOwnMapper.toDominio(source.getObjOrdenDelDia()))
                .informacionSolicitante(informacionSolicitanteOwnMapper.toDominio(source.getInformacionSolicitante()))
                .objFuncionario(funcionarioOwnMapper.toDominioSinLogsNiRoles(source.getObjFuncionario()))
                .anexos(mapAnexosDominio(source.getAnexos()))
                .build();
    }

    @Override
    public SolicitudEntidad toEntidad(Solicitud source) {
        if (source == null) return null;
        SolicitudEntidad entidad = SolicitudEntidad.builder()
                .uuidSolicitud(source.getUuidSolicitud())
                .consecutivo(source.getConsecutivo())
                .nombre(source.getNombre())
                .descripcion(source.getDescripcion())
                .estado(source.getEstado())
                .objTipoSolicitud(tipoSolicitudOwnMapper.toEntidadBasic(source.getObjTipoSolicitud()))
                .objOrdenDelDia(ordenDelDiaOwnMapper.toEntidad(source.getObjOrdenDelDia()))
                .informacionSolicitante(informacionSolicitanteOwnMapper.toEntidad(source.getInformacionSolicitante()))
                .objFuncionario(funcionarioOwnMapper.toEntidadSinLogsNiRoles(source.getObjFuncionario()))
                .build();
        entidad.setAnexos(mapAnexosEntidad(source.getAnexos(), entidad));
        return entidad;
    }

    private List<Anexo> mapAnexosDominio(List<AnexoEntidad> lista) {
        if (lista == null)
            return Collections.emptyList();
        return lista.stream().map(anexoOwnMapper::toDominioBasic).toList();
    }

    private List<AnexoEntidad> mapAnexosEntidad(List<Anexo> lista, SolicitudEntidad padre) {
        if (lista == null)
            return Collections.emptyList();
        return lista.stream().map(anexo -> {
            AnexoEntidad entidad = anexoOwnMapper.toEntidadBasic(anexo);
            entidad.setObjSolicitud(padre);
            return entidad;
        }).toList();
    }

}