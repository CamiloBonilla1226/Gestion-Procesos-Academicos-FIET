package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorFuncionariosAcademicos.mapeador;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FuncionarioAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorFuncionariosAcademicos.DTOPeticion.FuncionarioAcademicoActualizarDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorFuncionariosAcademicos.DTOPeticion.FuncionarioAcademicoDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorFuncionariosAcademicos.DTORespuesta.FuncionarioAcademicoDTORespuesta;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MapperFuncionarioAcademicoInfraestructuraDominio {

    public FuncionarioAcademico mapearPeticionAModelo(FuncionarioAcademicoDTOPeticion peticion) {
        Usuario usuario = new Usuario();
        usuario.setNombres(peticion.getNombres());
        usuario.setApellidos(peticion.getApellidos());
        usuario.setEstado(true);
        usuario.setTipoDocumento(peticion.getTipoDocumento());
        usuario.setNumeroDocumento(peticion.getNumeroDocumento());
        usuario.setTelefono(peticion.getTelefono());
        usuario.setCorreoElectronico(peticion.getCorreoElectronico());
        usuario.setUsername(peticion.getUsername());
        usuario.setPassword(peticion.getPassword());
        return FuncionarioAcademico.builder()
                .usuario(usuario)
                .dependencia(peticion.getDependencia())
                .build();
    }

    public FuncionarioAcademico mapearActualizacionAModelo(FuncionarioAcademicoActualizarDTOPeticion peticion) {
        return FuncionarioAcademico.builder()
                .dependencia(peticion.getDependencia())
                .build();
    }

    public FuncionarioAcademicoDTORespuesta mapearModeloARespuesta(FuncionarioAcademico modelo) {
        FuncionarioAcademicoDTORespuesta respuesta = FuncionarioAcademicoDTORespuesta.builder()
                .uuidUsuario(modelo.getUuidUsuario())
                .dependencia(modelo.getDependencia())
                .build();
        Usuario usuario = modelo.getUsuario();
        if (usuario != null) {
            respuesta.setNombres(usuario.getNombres());
            respuesta.setApellidos(usuario.getApellidos());
            respuesta.setEstado(usuario.getEstado());
            respuesta.setTipoDocumento(usuario.getTipoDocumento());
            respuesta.setNumeroDocumento(usuario.getNumeroDocumento());
            respuesta.setTelefono(usuario.getTelefono());
            respuesta.setCorreoElectronico(usuario.getCorreoElectronico());
            respuesta.setUsername(usuario.getUsername());
        }
        return respuesta;
    }

    public List<FuncionarioAcademicoDTORespuesta> mapearModelosARespuesta(List<FuncionarioAcademico> modelos) {
        return modelos.stream().map(this::mapearModeloARespuesta).toList();
    }

    public PaginacionRespuestaDTO<FuncionarioAcademicoDTORespuesta> mapearPaginaARespuesta(PaginacionRespuestaDTO<FuncionarioAcademico> pagina) {
        return new PaginacionRespuestaDTO<>(mapearModelosARespuesta(pagina.getContent()), pagina.getTotalElements());
    }
}
