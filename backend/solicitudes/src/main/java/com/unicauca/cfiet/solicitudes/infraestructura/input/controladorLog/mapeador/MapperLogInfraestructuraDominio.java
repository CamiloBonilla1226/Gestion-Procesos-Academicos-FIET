package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorLog.mapeador;

import com.unicauca.cfiet.solicitudes.dominio.modelos.Log;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorLog.DTORespuesta.LogDTORespuesta;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component
public class MapperLogInfraestructuraDominio {
    private  final ModelMapper mapper;

    public MapperLogInfraestructuraDominio(@Qualifier("mapeadorSimple") ModelMapper mapper){
        this.mapper = mapper;
    }

    public List<LogDTORespuesta> mapearModelosARespuesta(List<Log> modelos){
        return mapper.map(modelos, new TypeToken<List<LogDTORespuesta>>(){}.getType());
    }
}
