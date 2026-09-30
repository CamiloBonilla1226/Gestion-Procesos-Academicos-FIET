package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.Funcionario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Log;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Rol;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.FuncionarioEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.LogEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.RolEntidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
@RequiredArgsConstructor
public class FuncionarioOwnMapper implements OwnMapper<Funcionario, FuncionarioEntidad> {
    private final RolOwnMapperImpl rolMapper;
    private final LogOwnMapper logMapper;
    private final TipoUsuarioOwnMapper tipoUsuarioMapper;

    @Override
    public Funcionario toDominio(FuncionarioEntidad source) {
        if (source == null) return null;
        return Funcionario.builder()
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
                        tipoUsuarioMapper.toDominio(source.getObjTipoUsuario())
                )
                .roles(mapRolesDominio(source.getRoles()))
                .logs(mapLogsDominio(source.getLogs()))
                .build();
    }

    @Override
    public FuncionarioEntidad toEntidad(Funcionario source) {
        if (source == null) return null;
        return FuncionarioEntidad.builder()
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
                        tipoUsuarioMapper.toEntidad(source.getObjTipoUsuario())
                )
                .roles(mapRolesEntidad(source.getRoles()))
                .logs(mapLogsEntidad(source.getLogs()))
                .build();
    }

    private List<RolEntidad> mapRolesEntidad(List<Rol> roles) {
        if (roles == null) return Collections.emptyList();
        return roles.stream()
                .map(rolMapper::toEntidad)
                .collect(Collectors.toList());
    }

    private List<Rol> mapRolesDominio(List<RolEntidad> roles) {
        if (roles == null) return Collections.emptyList();
        return roles.stream()
                .map(rolMapper::toDominio)
                .collect(Collectors.toList());
    }

    private List<LogEntidad> mapLogsEntidad(List<Log> logs) {
        if (logs == null) return Collections.emptyList();
        return logs.stream()
                .map(logMapper::toEntidad)
                .collect(Collectors.toList());
    }

    private List<Log> mapLogsDominio(List<LogEntidad> logs) {
        if (logs == null) return Collections.emptyList();
        return logs.stream()
                .map(logMapper::toDominio)
                .collect(Collectors.toList());
    }

    public Funcionario toDominioSinLogsNiRoles(FuncionarioEntidad source) {
        if (source == null) return null;
        return Funcionario.builder()
                .uuidUsuario(source.getUuidUsuario())
                .nombres(source.getNombres())
                .apellidos(source.getApellidos())
                .estado(source.getEstado())
                .correoElectronico(source.getCorreoElectronico())
                .build();
    }

    public FuncionarioEntidad toEntidadSinLogsNiRoles(Funcionario source) {
        if (source == null) return null;
        return FuncionarioEntidad.builder()
                .uuidUsuario(source.getUuidUsuario())
                .nombres(source.getNombres())
                .apellidos(source.getApellidos())
                .estado(source.getEstado())
                .correoElectronico(source.getCorreoElectronico())
                .build();
    }
}
