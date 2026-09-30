package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.Anexo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Solicitud;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.AnexoEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudEntidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
@RequiredArgsConstructor
public class AnexoOwnMapper implements OwnMapper<Anexo, AnexoEntidad> {
    @Override
    public Anexo toDominio(AnexoEntidad source) {
        if (source == null) return null;

        return Anexo.builder()
                .uuidAnexo(source.getUuidAnexo())
                .nombre(source.getNombre())
                .urlAnexo(source.getUrlAnexo())
                .objSolicitud(toDominioBasic(source.getObjSolicitud()))
                .build();
    }

    @Override
    public AnexoEntidad toEntidad(Anexo source) {
        if (source == null) return null;

        return AnexoEntidad.builder()
                .uuidAnexo(source.getUuidAnexo())
                .nombre(source.getNombre())
                .urlAnexo(source.getUrlAnexo())
                .objSolicitud(toEntidadBasic(source.getObjSolicitud()))
                .build();
    }

    public Anexo toDominioBasic(AnexoEntidad source) {
        if (source == null) return null;

        return Anexo.builder()
                .uuidAnexo(source.getUuidAnexo())
                .nombre(source.getNombre())
                .urlAnexo(source.getUrlAnexo())
                .build();
    }

    public AnexoEntidad toEntidadBasic(Anexo source) {
        if (source == null) return null;

        return AnexoEntidad.builder()
                .uuidAnexo(source.getUuidAnexo())
                .nombre(source.getNombre())
                .urlAnexo(source.getUrlAnexo())
                .build();
    }

    private Solicitud toDominioBasic(SolicitudEntidad source) {
        if (source == null) return null;
        return Solicitud.builder()
                .uuidSolicitud(source.getUuidSolicitud())
                .consecutivo(source.getConsecutivo())
                .estado(source.getEstado())
                .build();
    }

    private SolicitudEntidad toEntidadBasic(Solicitud source) {
        if (source == null) return null;
        return SolicitudEntidad.builder()
                .uuidSolicitud(source.getUuidSolicitud())
                .consecutivo(source.getConsecutivo())
                .estado(source.getEstado())
                .build();
    }
}