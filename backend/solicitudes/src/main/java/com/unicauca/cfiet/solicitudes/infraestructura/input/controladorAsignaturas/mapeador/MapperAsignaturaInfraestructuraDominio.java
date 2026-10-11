package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.mapeador;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Asignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ProgramaAcademico;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.DTOPeticion.AsignaturaActualizarDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.DTOPeticion.AsignaturaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.DTORespuesta.AsignaturaDTORespuesta;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MapperAsignaturaInfraestructuraDominio {

    public Asignatura mapearPeticionAModelo(AsignaturaDTOPeticion peticion) {
        return Asignatura.builder()
                .nombreAsignatura(peticion.getNombreAsignatura())
                .programaAcademico(ProgramaAcademico.builder()
                        .uuidProgramaAcademico(peticion.getUuidProgramaAcademico())
                        .build())
                .build();
    }

    public Asignatura mapearActualizacionAModelo(AsignaturaActualizarDTOPeticion peticion) {
        return Asignatura.builder()
                .nombreAsignatura(peticion.getNombreAsignatura())
                .build();
    }

    public AsignaturaDTORespuesta mapearModeloARespuesta(Asignatura modelo) {
        AsignaturaDTORespuesta respuesta = AsignaturaDTORespuesta.builder()
                .uuidAsignatura(modelo.getUuidAsignatura())
                .codigoAsignatura(modelo.getCodigoAsignatura())
                .nombreAsignatura(modelo.getNombreAsignatura())
                .build();
        ProgramaAcademico programa = modelo.getProgramaAcademico();
        if (programa != null) {
            respuesta.setUuidProgramaAcademico(programa.getUuidProgramaAcademico());
            respuesta.setProgramaAcademico(programa.getNombre());
        }
        return respuesta;
    }

    public List<AsignaturaDTORespuesta> mapearModelosARespuesta(List<Asignatura> modelos) {
        return modelos.stream().map(this::mapearModeloARespuesta).toList();
    }

    public PaginacionRespuestaDTO<AsignaturaDTORespuesta> mapearPaginaARespuesta(PaginacionRespuestaDTO<Asignatura> pagina) {
        return new PaginacionRespuestaDTO<>(mapearModelosARespuesta(pagina.getContent()), pagina.getTotalElements());
    }
}
