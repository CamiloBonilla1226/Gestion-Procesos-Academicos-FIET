package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.SesionGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.seguridad.jwt.JwtServicio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.UsuarioEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.UsuarioOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * Interface que actua como fachada con la capa de persistencia para la gestión de sesiones.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
@RequiredArgsConstructor
public class SesionGatewayImplAdaptador implements SesionGatewayIntPuerto {
    private final UsuarioRepositorio repositorio;
    private final JwtServicio jwtServicio;
    private final AuthenticationManager authenticationManager;
    private final UsuarioOwnMapper usuarioMapper;

    @Override
    public String login(String username, String password) {
        Optional<UsuarioEntidad> usuario = repositorio.findByUsername(username);
        if(usuario.isPresent()){
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            if(authentication.isAuthenticated())
                return jwtServicio.getToken(usuario.get());
        }
        return null;
    }

    @Override
    public Usuario getUsuario(String username) {
        return repositorio.findByUsername(username)
                .map(usuarioMapper::toDominio)
                .orElse(null);
    }
}

