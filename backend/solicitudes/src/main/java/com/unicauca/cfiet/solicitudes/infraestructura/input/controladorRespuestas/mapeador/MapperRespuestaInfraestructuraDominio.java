package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorRespuestas.mapeador;

import com.unicauca.cfiet.solicitudes.dominio.modelos.Respuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorRespuestas.DTOPeticion.RespuestaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorRespuestas.DTORespuesta.RespuestaDTORespuesta;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component
public class MapperRespuestaInfraestructuraDominio {
    private final ModelMapper mapper;

    public MapperRespuestaInfraestructuraDominio(@Qualifier("mapeadorSimple") ModelMapper mapper){
        this.mapper = mapper;
    }

    public Respuesta mapearPeticionAModelo(RespuestaDTOPeticion peticion){
        return mapper.map(peticion, Respuesta.class);
    }

    public RespuestaDTORespuesta mapearModeloARespuesta(Respuesta modelo){
        return mapper.map(modelo, RespuestaDTORespuesta.class);
    }

    public List<RespuestaDTORespuesta> mapearModelosARespuesta(List<Respuesta> modelos){
        return mapper.map(modelos, new TypeToken<List<RespuestaDTORespuesta>>(){}.getType());
    }
}
