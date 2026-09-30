package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoAnexo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitud;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.TipoAnexoEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.TipoSolicitudEntidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
@RequiredArgsConstructor
public class TipoAnexoOwnMapper implements OwnMapper<TipoAnexo, TipoAnexoEntidad> {
    @Override
    public TipoAnexo toDominio(TipoAnexoEntidad source) {
        if (source == null) return null;
        return TipoAnexo.builder()
                .uuidTipoAnexo(source.getUuidTipoAnexo())
                .nombre(source.getNombre())
                .descripcion(source.getDescripcion())
                .formato(source.getFormato())
                .obligatoriedad(source.getObligatoriedad())
                .objTipoSolicitud(toDominioBasic(source.getObjTipoSolicitud()))
                .build();
    }

    @Override
    public TipoAnexoEntidad toEntidad(TipoAnexo source) {
        if (source == null) return null;
        return TipoAnexoEntidad.builder()
                .uuidTipoAnexo(source.getUuidTipoAnexo())
                .nombre(source.getNombre())
                .descripcion(source.getDescripcion())
                .formato(source.getFormato())
                .obligatoriedad(source.getObligatoriedad())
                .objTipoSolicitud(toEntidadBasic(source.getObjTipoSolicitud()))
                .build();
    }

    public TipoAnexo toDominioBasic(TipoAnexoEntidad source) {
        if (source == null) return null;
        return TipoAnexo.builder()
                .uuidTipoAnexo(source.getUuidTipoAnexo())
                .nombre(source.getNombre())
                .descripcion(source.getDescripcion())
                .formato(source.getFormato())
                .obligatoriedad(source.getObligatoriedad())
                .build();
    }

    public TipoAnexoEntidad toEntidadBasic(TipoAnexo source) {
        if (source == null) return null;
        return TipoAnexoEntidad.builder()
                .uuidTipoAnexo(source.getUuidTipoAnexo())
                .nombre(source.getNombre())
                .descripcion(source.getDescripcion())
                .formato(source.getFormato())
                .obligatoriedad(source.getObligatoriedad())
                .build();
    }

    private TipoSolicitud toDominioBasic(TipoSolicitudEntidad source) {
        if (source == null) return null;
        return TipoSolicitud.builder()
                .uuidTipoSolicitud(source.getUuidTipoSolicitud())
                .nombre(source.getNombre())
                .descripcion(source.getDescripcion())
                .seccion(source.getSeccion())
                .perfilSolicitante(source.getPerfilSolicitante())
                .build();
    }

    private TipoSolicitudEntidad toEntidadBasic(TipoSolicitud source) {
        if (source == null) return null;
        return TipoSolicitudEntidad.builder()
                .uuidTipoSolicitud(source.getUuidTipoSolicitud())
                .nombre(source.getNombre())
                .descripcion(source.getDescripcion())
                .seccion(source.getSeccion())
                .perfilSolicitante(source.getPerfilSolicitante())
                .build();
    }
}
