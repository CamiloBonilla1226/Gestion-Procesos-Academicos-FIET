package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.mapeador;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Asignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaMatriculada;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Estudiante;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ProgramaAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion.AsignaturaMatriculadaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion.EstudianteActualizarDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion.EstudianteDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTORespuesta.AsignaturaMatriculadaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTORespuesta.EstudianteDTORespuesta;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MapperEstudianteInfraestructuraDominio {

    public Estudiante mapearPeticionAModelo(EstudianteDTOPeticion peticion) {
        Usuario usuario = new Usuario();
        usuario.setNombres(peticion.getNombres());
        usuario.setApellidos(peticion.getApellidos());
        usuario.setEstado(true);
        usuario.setTipoDocumento(peticion.getTipoDocumento());
        usuario.setNumeroDocumento(peticion.getNumeroDocumento());
        usuario.setTelefono(peticion.getTelefono());
        usuario.setCorreoElectronico(peticion.getCorreoElectronico());
        usuario.setUsername(peticion.getUsername());
        usuario.setPassword(peticion.getPassword());

        List<AsignaturaMatriculada> materias = new ArrayList<>();
        for (AsignaturaMatriculadaDTOPeticion materia : peticion.getAsignaturas())
            materias.add(mapearMateriaAModelo(materia));

        return Estudiante.builder()
                .usuario(usuario)
                .codigoEstudiantil(peticion.getCodigoEstudiantil())
                .programaAcademico(ProgramaAcademico.builder()
                        .uuidProgramaAcademico(peticion.getUuidProgramaAcademico())
                        .build())
                .semestre(peticion.getSemestre())
                .asignaturasMatriculadas(materias)
                .build();
    }

    public Estudiante mapearActualizacionAModelo(EstudianteActualizarDTOPeticion peticion) {
        Usuario usuario = new Usuario();
        usuario.setNombres(peticion.getNombres());
        usuario.setApellidos(peticion.getApellidos());
        usuario.setTipoDocumento(peticion.getTipoDocumento());
        usuario.setNumeroDocumento(peticion.getNumeroDocumento());
        usuario.setTelefono(peticion.getTelefono());
        usuario.setCorreoElectronico(peticion.getCorreoElectronico());
        return Estudiante.builder()
                .usuario(usuario)
                .codigoEstudiantil(peticion.getCodigoEstudiantil())
                .semestre(peticion.getSemestre())
                .build();
    }

    public AsignaturaMatriculada mapearMateriaAModelo(AsignaturaMatriculadaDTOPeticion peticion) {
        return AsignaturaMatriculada.builder()
                .asignatura(Asignatura.builder()
                        .codigoAsignatura(peticion.getCodigoAsignatura())
                        .build())
                .grupo(peticion.getGrupo())
                .build();
    }

    public EstudianteDTORespuesta mapearModeloARespuesta(Estudiante modelo) {
        EstudianteDTORespuesta respuesta = EstudianteDTORespuesta.builder()
                .uuidUsuario(modelo.getUuidUsuario())
                .codigoEstudiantil(modelo.getCodigoEstudiantil())
                .semestre(modelo.getSemestre())
                .facultad(modelo.getFacultad())
                .asignaturasMatriculadas(mapearMateriasARespuesta(modelo.getAsignaturasMatriculadas()))
                .build();
        ProgramaAcademico programa = modelo.getProgramaAcademico();
        if (programa != null) {
            respuesta.setUuidProgramaAcademico(programa.getUuidProgramaAcademico());
            respuesta.setProgramaAcademico(programa.getNombre());
        }
        Usuario usuario = modelo.getUsuario();
        if (usuario != null) {
            respuesta.setNombres(usuario.getNombres());
            respuesta.setApellidos(usuario.getApellidos());
            respuesta.setEstado(usuario.getEstado());
            respuesta.setTipoDocumento(usuario.getTipoDocumento());
            respuesta.setNumeroDocumento(usuario.getNumeroDocumento());
            respuesta.setTelefono(usuario.getTelefono());
            respuesta.setCorreoElectronico(usuario.getCorreoElectronico());
            respuesta.setUsername(usuario.getUsername());
        }
        return respuesta;
    }

    public AsignaturaMatriculadaDTORespuesta mapearMateriaARespuesta(AsignaturaMatriculada modelo) {
        AsignaturaMatriculadaDTORespuesta respuesta = AsignaturaMatriculadaDTORespuesta.builder()
                .uuidAsignaturaMatriculada(modelo.getUuidAsignaturaMatriculada())
                .grupo(modelo.getGrupo())
                .estado(modelo.getEstado())
                .build();
        Asignatura asignatura = modelo.getAsignatura();
        if (asignatura != null) {
            respuesta.setUuidAsignatura(asignatura.getUuidAsignatura());
            respuesta.setCodigoAsignatura(asignatura.getCodigoAsignatura());
            respuesta.setNombreAsignatura(asignatura.getNombreAsignatura());
        }
        return respuesta;
    }

    public List<AsignaturaMatriculadaDTORespuesta> mapearMateriasARespuesta(List<AsignaturaMatriculada> modelos) {
        if (modelos == null) return new ArrayList<>();
        return modelos.stream().map(this::mapearMateriaARespuesta).toList();
    }

    public PaginacionRespuestaDTO<EstudianteDTORespuesta> mapearPaginaARespuesta(PaginacionRespuestaDTO<Estudiante> pagina) {
        List<EstudianteDTORespuesta> contenido = pagina.getContent().stream()
                .map(this::mapearModeloARespuesta)
                .toList();
        return new PaginacionRespuestaDTO<>(contenido, pagina.getTotalElements());
    }
}
