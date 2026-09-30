package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoAnexo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitud;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.TipoAnexoEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.TipoSolicitudEntidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
@RequiredArgsConstructor
public class TipoSolicitudOwnMapper implements OwnMapper<TipoSolicitud, TipoSolicitudEntidad> {
    private final TipoAnexoOwnMapper tipoAnexoOwnMapper;
    private final FuncionarioOwnMapper funcionarioOwnMapper;

    @Override
    public TipoSolicitud toDominio(TipoSolicitudEntidad source) {
        if (source == null) return null;
        return TipoSolicitud.builder()
                .uuidTipoSolicitud(source.getUuidTipoSolicitud())
                .nombre(source.getNombre())
                .descripcion(source.getDescripcion())
                .seccion(source.getSeccion())
                .perfilSolicitante(source.getPerfilSolicitante())
                .objFuncionarioEncargado(funcionarioOwnMapper.toDominioSinLogsNiRoles(source.getObjFuncionarioEncargado()))
                .anexos(mapAnexosDominio(source.getAnexos()))
                .build();
    }

    @Override
    public TipoSolicitudEntidad toEntidad(TipoSolicitud source) {
        if (source == null) return null;
        TipoSolicitudEntidad entidad = TipoSolicitudEntidad.builder()
                .uuidTipoSolicitud(source.getUuidTipoSolicitud())
                .nombre(source.getNombre())
                .descripcion(source.getDescripcion())
                .seccion(source.getSeccion())
                .perfilSolicitante(source.getPerfilSolicitante())
                .objFuncionarioEncargado(funcionarioOwnMapper.toEntidadSinLogsNiRoles(source.getObjFuncionarioEncargado()))
                .build();
        entidad.setAnexos(mapAnexosEntidad(source.getAnexos(), entidad));
        return entidad;
    }

    public TipoSolicitud toDominioBasic(TipoSolicitudEntidad source) {
        if (source == null) return null;
        return TipoSolicitud.builder()
                .uuidTipoSolicitud(source.getUuidTipoSolicitud())
                .nombre(source.getNombre())
                .descripcion(source.getDescripcion())
                .seccion(source.getSeccion())
                .perfilSolicitante(source.getPerfilSolicitante())
                .build();
    }

    public TipoSolicitudEntidad toEntidadBasic(TipoSolicitud source) {
        if (source == null) return null;
        return TipoSolicitudEntidad.builder()
                .uuidTipoSolicitud(source.getUuidTipoSolicitud())
                .nombre(source.getNombre())
                .descripcion(source.getDescripcion())
                .seccion(source.getSeccion())
                .perfilSolicitante(source.getPerfilSolicitante())
                .build();
    }

    private List<TipoAnexo> mapAnexosDominio(List<TipoAnexoEntidad> lista) {
        if (lista == null)
            return Collections.emptyList();
        return lista.stream().map(tipoAnexoOwnMapper::toDominioBasic).toList();
    }

    private List<TipoAnexoEntidad> mapAnexosEntidad(List<TipoAnexo> lista, TipoSolicitudEntidad tipoSolicitud) {
        if (lista == null)
            return Collections.emptyList();
        return lista.stream()
                .map(anexo -> {
                    TipoAnexoEntidad entidad = tipoAnexoOwnMapper.toEntidadBasic(anexo);
                    entidad.setObjTipoSolicitud(tipoSolicitud);
                    return entidad;
                })
                .toList();
    }
}