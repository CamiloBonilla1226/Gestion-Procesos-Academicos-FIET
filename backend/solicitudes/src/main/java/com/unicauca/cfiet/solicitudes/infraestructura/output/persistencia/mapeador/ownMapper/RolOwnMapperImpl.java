package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.Rol;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.RolEntidad;
import org.springframework.stereotype.Service;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
public class RolOwnMapperImpl implements OwnMapper<Rol, RolEntidad>{
    @Override
    public Rol toDominio(RolEntidad source) {
        return Rol.builder()
                .uuidRol(source.getUuidRol())
                .nombre(source.getNombre())
                .descripcion(source.getDescripcion())
                .estado(source.getEstado())
                .build();
    }

    @Override
    public RolEntidad toEntidad(Rol source) {
        return RolEntidad.builder()
                .uuidRol(source.getUuidRol())
                .nombre(source.getNombre())
                .descripcion(source.getDescripcion())
                .estado(source.getEstado())
                .build();
    }
}
