package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.OrdenDelDia;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.OrdenDelDiaEntidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
@RequiredArgsConstructor
public class OrdenDelDiaOwnMapper implements OwnMapper<OrdenDelDia, OrdenDelDiaEntidad> {

    @Override
    public OrdenDelDia toDominio(OrdenDelDiaEntidad source) {
        if (source == null) return null;
        return OrdenDelDia.builder()
                .uuidOrdenDelDia(source.getUuidOrdenDelDia())
                .nombre(source.getNombre())
                .descripcion(source.getDescripcion())
                .ciudad(source.getCiudad())
                .fecha(source.getFecha())
                .horaInicio(source.getHoraInicio())
                .horaFin(source.getHoraFin())
                .lugarReunion(source.getLugarReunion())
                .numeroActa(source.getNumeroActa())
                .estado(source.isEstado())
                .build();
    }

    @Override
    public OrdenDelDiaEntidad toEntidad(OrdenDelDia source) {
        if (source == null) return null;
        return OrdenDelDiaEntidad.builder()
                .uuidOrdenDelDia(source.getUuidOrdenDelDia())
                .nombre(source.getNombre())
                .descripcion(source.getDescripcion())
                .ciudad(source.getCiudad())
                .fecha(source.getFecha())
                .horaInicio(source.getHoraInicio())
                .horaFin(source.getHoraFin())
                .lugarReunion(source.getLugarReunion())
                .numeroActa(source.getNumeroActa())
                .estado(source.isEstado())
                .build();
    }
}