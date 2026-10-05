package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.mapeador;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Asignatura;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.DTOPeticion.AsignaturaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.DTORespuesta.AsignaturaDTORespuesta;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MapperAsignaturaInfraestructuraDominio {
    private final ModelMapper mapper;

    public MapperAsignaturaInfraestructuraDominio(@Qualifier("mapeadorSimple") ModelMapper mapper) {
        this.mapper = mapper;
    }

    public Asignatura mapearPeticionAModelo(AsignaturaDTOPeticion peticion) {
        return mapper.map(peticion, Asignatura.class);
    }

    public AsignaturaDTORespuesta mapearModeloARespuesta(Asignatura modelo) {
        return mapper.map(modelo, AsignaturaDTORespuesta.class);
    }

    public List<AsignaturaDTORespuesta> mapearModelosARespuesta(List<Asignatura> modelos) {
        return mapper.map(modelos, new TypeToken<List<AsignaturaDTORespuesta>>(){}.getType());
    }

    public PaginacionRespuestaDTO<AsignaturaDTORespuesta> mapearPaginaARespuesta(PaginacionRespuestaDTO<Asignatura> pagina) {
        return new PaginacionRespuestaDTO<>(mapearModelosARespuesta(pagina.getContent()), pagina.getTotalElements());
    }
}
