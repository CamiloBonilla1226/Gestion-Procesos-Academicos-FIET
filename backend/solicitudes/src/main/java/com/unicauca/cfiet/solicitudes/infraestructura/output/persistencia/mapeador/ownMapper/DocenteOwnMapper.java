package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.Docente;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.UsuarioEntidad;
import org.springframework.stereotype.Service;

@Service
public class DocenteOwnMapper implements OwnMapper<Docente, UsuarioEntidad> {

    @Override
    public Docente toDominio(UsuarioEntidad source) {
        if (source == null) return null;
        return Docente.builder()
                .uuidUsuario(source.getUuidUsuario())
                .nombres(source.getNombres())
                .apellidos(source.getApellidos())
                .numeroDocumento(source.getNumeroDocumento())
                .username(source.getUsername())
                .build();
    }

    @Override
    public UsuarioEntidad toEntidad(Docente source) {
        if (source == null) return null;
        return UsuarioEntidad.builder()
                .uuidUsuario(source.getUuidUsuario())
                .nombres(source.getNombres())
                .apellidos(source.getApellidos())
                .numeroDocumento(source.getNumeroDocumento())
                .username(source.getUsername())
                .build();
    }
}
