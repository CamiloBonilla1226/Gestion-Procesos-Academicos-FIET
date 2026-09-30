package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorTiposSolicitud.mapeador;

import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitud;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorTiposSolicitud.DTOPeticion.TipoSolicitudDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorTiposSolicitud.DTORespuesta.TipoSolicitudDTORespuesta;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component
public class MapperTipoSolicitudInfraestructuraDominio {
    private  final ModelMapper mapper;

    public MapperTipoSolicitudInfraestructuraDominio(@Qualifier("mapeadorSimple") ModelMapper mapper){
        this.mapper = mapper;
    }

    public TipoSolicitud mapearPeticionAModelo(TipoSolicitudDTOPeticion peticion){
        return mapper.map(peticion, TipoSolicitud.class);
    }

    public List<TipoSolicitud> mapearPeticionesAModelo(List<TipoSolicitudDTOPeticion> peticiones){
        return mapper.map(peticiones, new TypeToken<List<TipoSolicitud>>(){}.getType());
    }

    public TipoSolicitudDTORespuesta mapearModeloARespuesta(TipoSolicitud modelo){
        return mapper.map(modelo, TipoSolicitudDTORespuesta.class);
    }

    public List<TipoSolicitudDTORespuesta> mapearModelosARespuesta(List<TipoSolicitud> modelos){
        return mapper.map(modelos, new TypeToken<List<TipoSolicitudDTORespuesta>>(){}.getType());
    }
}
