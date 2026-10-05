package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.Asignatura;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.AsignaturaEntidad;
import org.springframework.stereotype.Service;

@Service
public class AsignaturaOwnMapper implements OwnMapper<Asignatura, AsignaturaEntidad> {
    @Override
    public Asignatura toDominio(AsignaturaEntidad source) {
        return Asignatura.builder()
                .uuidAsignatura(source.getUuidAsignatura())
                .codigoAsignatura(source.getCodigoAsignatura())
                .nombreAsignatura(source.getNombreAsignatura())
                .build();
    }

    @Override
    public AsignaturaEntidad toEntidad(Asignatura source) {
        return AsignaturaEntidad.builder()
                .uuidAsignatura(source.getUuidAsignatura())
                .codigoAsignatura(source.getCodigoAsignatura())
                .nombreAsignatura(source.getNombreAsignatura())
                .build();
    }
}
