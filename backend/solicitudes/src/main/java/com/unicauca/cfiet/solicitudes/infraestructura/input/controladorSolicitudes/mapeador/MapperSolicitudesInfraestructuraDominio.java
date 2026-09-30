package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudes.mapeador;

import com.unicauca.cfiet.solicitudes.dominio.modelos.OrdenDelDia;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Solicitud;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudes.DTOPeticion.OrdenDelDiaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudes.DTOPeticion.SolicitudActualizarDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudes.DTOPeticion.SolicitudDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudes.DTOPeticion.SolicitudPublicaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudes.DTORespuesta.OrdenDelDiaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudes.DTORespuesta.SolicitudDTORespuesta;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component
public class MapperSolicitudesInfraestructuraDominio {
    private final ModelMapper mapper;

    public MapperSolicitudesInfraestructuraDominio(@Qualifier("mapeadorSimple") ModelMapper mapper){
        this.mapper = mapper;
    }

    public Solicitud mapearPeticionAModelo(SolicitudDTOPeticion peticion){
        return mapper.map(peticion, Solicitud.class);
    }

    public Solicitud mapearPeticionAModelo(SolicitudActualizarDTOPeticion peticion){
        return mapper.map(peticion, Solicitud.class);
    }

    public Solicitud mapearPeticionAModelo(SolicitudPublicaDTOPeticion peticion){
        return mapper.map(peticion, Solicitud.class);
    }

    public OrdenDelDia mapearPeticionAModelo(OrdenDelDiaDTOPeticion peticion){
        return mapper.map(peticion, OrdenDelDia.class);
    }

    public OrdenDelDiaDTORespuesta mapearModeloARespuesta(OrdenDelDia modelo){
        return mapper.map(modelo, OrdenDelDiaDTORespuesta.class);
    }

    public SolicitudDTORespuesta mapearModeloARespuesta(Solicitud modelo){
        return mapper.map(modelo, SolicitudDTORespuesta.class);
    }

    public List<OrdenDelDiaDTORespuesta> mapearModelosARespuesta(List<OrdenDelDia> modelos){
        return mapper.map(modelos, new TypeToken<List<OrdenDelDiaDTORespuesta>>(){}.getType());
    }

    public List<SolicitudDTORespuesta> mapearModelosARespuestaSolicitud(List<Solicitud> modelos){
        return mapper.map(modelos, new TypeToken<List<SolicitudDTORespuesta>>(){}.getType());
    }

}
