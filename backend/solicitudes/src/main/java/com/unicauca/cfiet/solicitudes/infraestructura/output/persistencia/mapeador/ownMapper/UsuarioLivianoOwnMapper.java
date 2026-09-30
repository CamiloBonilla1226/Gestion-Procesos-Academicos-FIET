package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.UsuarioLiviano;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.UsuarioLivianoEntidad;
import org.springframework.stereotype.Service;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
public class UsuarioLivianoOwnMapper implements OwnMapper<UsuarioLiviano, UsuarioLivianoEntidad> {
    @Override
    public UsuarioLiviano toDominio(UsuarioLivianoEntidad source) {
        return UsuarioLiviano.builder()
                .uuidUsuario(source.getUuidUsuario())
                .nombres(source.getNombres())
                .apellidos(source.getApellidos())
                .estado(source.getEstado())
                .build();
    }

    @Override
    public UsuarioLivianoEntidad toEntidad(UsuarioLiviano source) {
        return UsuarioLivianoEntidad.builder()
                .uuidUsuario(source.getUuidUsuario())
                .nombres(source.getNombres())
                .apellidos(source.getApellidos())
                .estado(source.getEstado())
                .build();
    }
}
