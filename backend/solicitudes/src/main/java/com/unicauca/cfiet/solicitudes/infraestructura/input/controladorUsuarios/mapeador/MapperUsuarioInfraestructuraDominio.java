package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.mapeador;

import com.unicauca.cfiet.solicitudes.dominio.modelos.Funcionario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoUsuario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.UsuarioLiviano;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.DTOPeticion.UsuarioActualizarDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.DTOPeticion.UsuarioDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.DTORespuesta.TipoUsuarioDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.DTORespuesta.UsuarioDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.DTORespuesta.UsuarioLivianoDTORespuesta;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Component
public class MapperUsuarioInfraestructuraDominio {
    private  final ModelMapper mapper;

    public MapperUsuarioInfraestructuraDominio(@Qualifier("mapeadorSimple") ModelMapper mapper){
        this.mapper = mapper;
    }

    public Usuario mapearPeticionAModelo(UsuarioDTOPeticion peticion){
        return mapper.map(peticion, Usuario.class);
    }

    public Usuario mapearPeticionActualizarAModelo(UsuarioActualizarDTOPeticion peticion){
        return mapper.map(peticion, Usuario.class);
    }

    public UsuarioDTORespuesta mapearModeloARespuesta(Usuario modelo){
        return mapper.map(modelo, UsuarioDTORespuesta.class);
    }

    public List<UsuarioLivianoDTORespuesta> mapearModelosARespuestaLiviano(List<UsuarioLiviano> modelos){
        return mapper.map(modelos, new TypeToken<List<UsuarioLivianoDTORespuesta>>(){}.getType());
    }

    public List<UsuarioDTORespuesta> mapearModelosARespuesta(List<Usuario> modelos){
        return mapper.map(modelos, new TypeToken<List<UsuarioLivianoDTORespuesta>>(){}.getType());
    }

    public List<UsuarioLivianoDTORespuesta> mapearModelosARespuestaFuncionario(List<Funcionario> modelos){
        return mapper.map(modelos, new TypeToken<List<UsuarioLivianoDTORespuesta>>(){}.getType());
    }

    public List<Usuario> mapearPeticionesAModelo(List<UsuarioDTOPeticion> peticiones){
        return mapper.map(peticiones, new TypeToken<List<Usuario>>(){}.getType());
    }

    public List<TipoUsuarioDTORespuesta> mapearTipoUsuarioARespuesta(List<TipoUsuario> modelos){
        return mapper.map(modelos, new TypeToken<List<TipoUsuarioDTORespuesta>>(){}.getType());
    }
}
