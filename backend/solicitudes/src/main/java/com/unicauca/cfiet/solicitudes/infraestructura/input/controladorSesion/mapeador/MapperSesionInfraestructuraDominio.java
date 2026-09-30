package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSesion.mapeador;

import com.unicauca.cfiet.solicitudes.dominio.modelos.UsuarioTokenizado;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSesion.DTORespuesta.UsuarioTokenizadoDTORespuesta;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component
public class MapperSesionInfraestructuraDominio {
    private  final ModelMapper mapper;

    public MapperSesionInfraestructuraDominio(@Qualifier("mapeadorSimple") ModelMapper mapper){
        this.mapper = mapper;
    }

    public UsuarioTokenizadoDTORespuesta mapearModeloARespuesta(UsuarioTokenizado modelo){
        return mapper.map(modelo, UsuarioTokenizadoDTORespuesta.class);
    }
}
