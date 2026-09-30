package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoUsuario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.TipoUsuarioEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.UsuarioEntidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
@RequiredArgsConstructor
public class TipoUsuarioOwnMapper implements OwnMapper<TipoUsuario, TipoUsuarioEntidad> {
    @Override
    public TipoUsuario toDominio(TipoUsuarioEntidad source) {
        if (source == null) return null;
        return TipoUsuario.builder()
                .uuidTipoUsuario(source.getUuidTipoUsuario())
                .nombre(source.getNombre())
                .usuarios(mapUsuariosDominio(source.getUsuarios()))
                .build();
    }

    @Override
    public TipoUsuarioEntidad toEntidad(TipoUsuario source) {
        if (source == null) return null;
        return TipoUsuarioEntidad.builder()
                .uuidTipoUsuario(source.getUuidTipoUsuario())
                .nombre(source.getNombre())
                .usuarios(mapUsuariosEntidad(source.getUsuarios()))
                .build();
    }

    private List<UsuarioEntidad> mapUsuariosEntidad(List<Usuario> usuarios) {
        if (usuarios == null) return new ArrayList<>();
        List<UsuarioEntidad> response = new ArrayList<>();
        for (Usuario usuario : usuarios)
            response.add(toEntidadSinLogsNiRoles(usuario));
        return response;
    }

    private List<Usuario> mapUsuariosDominio(List<UsuarioEntidad> usuarios) {
        if (usuarios == null) return new ArrayList<>();
        List<Usuario> response = new ArrayList<>();
        for (UsuarioEntidad usuario : usuarios)
            response.add(toDominioSinLogsNiRoles(usuario));
        return response;
    }

    public TipoUsuario toDominioBasic(TipoUsuarioEntidad source) {
        if (source == null) return null;
        return TipoUsuario.builder()
                .uuidTipoUsuario(source.getUuidTipoUsuario())
                .nombre(source.getNombre())
                .usuarios(null)
                .build();
    }

    public TipoUsuarioEntidad toEntidadBasic(TipoUsuario source) {
        if (source == null) return null;
        return TipoUsuarioEntidad.builder()
                .uuidTipoUsuario(source.getUuidTipoUsuario())
                .nombre(source.getNombre())
                .usuarios(null)
                .build();
    }

    public Usuario toDominioSinLogsNiRoles(UsuarioEntidad source) {
        if (source == null) return null;
        return Usuario.builder()
                .uuidUsuario(source.getUuidUsuario())
                .nombres(source.getNombres())
                .apellidos(source.getApellidos())
                .estado(source.getEstado())
                .build();
    }

    public UsuarioEntidad toEntidadSinLogsNiRoles(Usuario source) {
        if (source == null) return null;
        return UsuarioEntidad.builder()
                .uuidUsuario(source.getUuidUsuario())
                .nombres(source.getNombres())
                .apellidos(source.getApellidos())
                .estado(source.getEstado())
                .build();
    }


}