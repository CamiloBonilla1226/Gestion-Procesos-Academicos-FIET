package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioActorGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Rol;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.UsuarioActorRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.UsuarioActorRepositorio.FilaActor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioActorGatewayImplAdaptador implements UsuarioActorGatewayIntPuerto {
    private final UsuarioActorRepositorio repositorio;

    @Override
    public Usuario getUsuario(String uuidUsuario) {
        return aUsuario(repositorio.buscarPorUuid(uuidUsuario));
    }

    @Override
    public Usuario getUsuarioPorUsername(String username) {
        return aUsuario(repositorio.buscarPorUsername(username));
    }

    private Usuario aUsuario(List<FilaActor> filas) {
        if (filas == null || filas.isEmpty())
            return null;
        List<Rol> roles = new ArrayList<>();
        for (FilaActor fila : filas)
            if (fila.getUuidRol() != null)
                roles.add(Rol.builder()
                        .uuidRol(fila.getUuidRol())
                        .nombre(fila.getNombreRol())
                        .descripcion(fila.getDescripcionRol())
                        .estado(fila.getEstadoRol())
                        .build());
        FilaActor primera = filas.get(0);
        return Usuario.builder()
                .uuidUsuario(primera.getUuidUsuario())
                .username(primera.getUsername())
                .roles(roles)
                .logs(new ArrayList<>())
                .build();
    }
}
