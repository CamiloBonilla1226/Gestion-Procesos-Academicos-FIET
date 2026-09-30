package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorRoles.mapeador;

import com.unicauca.cfiet.solicitudes.dominio.modelos.Rol;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorRoles.DTOPeticion.RolDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorRoles.DTORespuesta.RolDTORespuesta;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component
public class MapperRolInfraestructuraDominio {
    private  final ModelMapper mapper;

    public MapperRolInfraestructuraDominio(@Qualifier("mapeadorSimple") ModelMapper mapper){
        this.mapper = mapper;
    }

    public Rol mapearPeticionAModelo(RolDTOPeticion peticion){
        return mapper.map(peticion, Rol.class);
    }

    public RolDTORespuesta mapearModeloARespuesta(Rol modelo){
        return mapper.map(modelo, RolDTORespuesta.class);
    }

    public List<RolDTORespuesta> mapearModelosARespuesta(List<Rol> modelos){
        return mapper.map(modelos, new TypeToken<List<RolDTORespuesta>>(){}.getType());
    }
}
