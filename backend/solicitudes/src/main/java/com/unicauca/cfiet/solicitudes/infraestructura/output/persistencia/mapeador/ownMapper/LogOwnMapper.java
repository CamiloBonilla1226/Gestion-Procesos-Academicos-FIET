package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.Log;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.LogEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.UsuarioEntidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
@RequiredArgsConstructor
public class LogOwnMapper implements OwnMapper<Log, LogEntidad> {
    @Override
    public Log toDominio(LogEntidad source) {
        return Log.builder()
                .uuidLog(source.getUuidLog())
                .accion(source.getAccion())
                .fecha(source.getFecha())
                .resultado(source.getResultado())
                .objUsuarioLog(
                        source.getObjUsuarioLog() != null
                                ? toDominioSinLogsNiRoles(source.getObjUsuarioLog())
                                : null
                )
                .build();
    }

    @Override
    public LogEntidad toEntidad(Log source) {
        return LogEntidad.builder()
                .uuidLog(source.getUuidLog())
                .accion(source.getAccion())
                .fecha(source.getFecha())
                .resultado(source.getResultado())
                .objUsuarioLog(
                        source.getObjUsuarioLog() != null
                                ? toEntidadSinLogsNiRoles(source.getObjUsuarioLog())
                                : null
                )
                .build();
    }

    public Log toDominioBasic(LogEntidad source) {
        return Log.builder()
                .uuidLog(source.getUuidLog())
                .accion(source.getAccion())
                .fecha(source.getFecha())
                .resultado(source.getResultado())
                .objUsuarioLog(null)
                .build();
    }

    public LogEntidad toEntidadBasic(Log source) {
        return LogEntidad.builder()
                .uuidLog(source.getUuidLog())
                .accion(source.getAccion())
                .fecha(source.getFecha())
                .resultado(source.getResultado())
                .objUsuarioLog(null)
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
