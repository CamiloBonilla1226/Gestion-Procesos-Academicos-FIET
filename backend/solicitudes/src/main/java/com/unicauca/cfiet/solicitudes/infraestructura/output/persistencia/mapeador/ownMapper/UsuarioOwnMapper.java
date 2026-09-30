package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.Log;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Rol;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.LogEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.RolEntidad;
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
public class UsuarioOwnMapper implements OwnMapper<Usuario, UsuarioEntidad> {
    private final RolOwnMapperImpl rolMapper;
    private final LogOwnMapper logMapper;
    private final TipoUsuarioOwnMapper tipoUsuarioMapper;

    @Override
    public Usuario toDominio(UsuarioEntidad source) {
        return Usuario.builder()
                .uuidUsuario(source.getUuidUsuario())
                .nombres(source.getNombres())
                .apellidos(source.getApellidos())
                .estado(source.getEstado())
                .tipoDocumento(source.getTipoDocumento())
                .numeroDocumento(source.getNumeroDocumento())
                .telefono(source.getTelefono())
                .correoElectronico(source.getCorreoElectronico())
                .username(source.getUsername())
                .password(source.getPassword())
                .objTipoUsuario(
                        tipoUsuarioMapper.toDominioBasic(source.getObjTipoUsuario())
                )
                .roles(mapRolesDominio(source.getRoles()))
                .logs(mapLogsDominio(source.getLogs()))
                .build();
    }

    @Override
    public UsuarioEntidad toEntidad(Usuario source) {
        return UsuarioEntidad.builder()
                .uuidUsuario(source.getUuidUsuario())
                .nombres(source.getNombres())
                .apellidos(source.getApellidos())
                .estado(source.getEstado())
                .tipoDocumento(source.getTipoDocumento())
                .numeroDocumento(source.getNumeroDocumento())
                .telefono(source.getTelefono())
                .correoElectronico(source.getCorreoElectronico())
                .username(source.getUsername())
                .password(source.getPassword())
                .objTipoUsuario(
                        tipoUsuarioMapper.toEntidadBasic(source.getObjTipoUsuario())
                )
                .roles(mapRolesEntidad(source.getRoles()))
                .logs(mapLogsEntidad(source.getLogs()))
                .build();
    }

    private List<RolEntidad> mapRolesEntidad(List<Rol> roles){
        if (roles == null) return new ArrayList<>();
        List<RolEntidad> response = new ArrayList<>();
        for(Rol rol : roles)
            response.add(rolMapper.toEntidad(rol));
        return response;
    }

    private List<Rol> mapRolesDominio(List<RolEntidad> roles){
        if (roles == null) return new ArrayList<>();
        List<Rol> response = new ArrayList<>();
        for(RolEntidad rol : roles)
            response.add(rolMapper.toDominio(rol));
        return response;
    }


    private List<LogEntidad> mapLogsEntidad(List<Log> logs){
        if (logs == null) return new ArrayList<>();
        List<LogEntidad> response = new ArrayList<>();
        for(Log log : logs)
            response.add(logMapper.toEntidadBasic(log));
        return response;
    }

    private List<Log> mapLogsDominio(List<LogEntidad> logs){
        if (logs == null) return new ArrayList<>();
        List<Log> response = new ArrayList<>();
        for(LogEntidad log : logs)
            response.add(logMapper.toDominioBasic(log));
        return response;
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
}
