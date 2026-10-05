package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaMatriculada;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Estudiante;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.AsignaturaMatriculadaEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.EstudianteEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.UsuarioEntidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EstudianteOwnMapper implements OwnMapper<Estudiante, EstudianteEntidad> {
    private final AsignaturaMatriculadaOwnMapper asignaturaMatriculadaMapper;

    @Override
    public Estudiante toDominio(EstudianteEntidad source) {
        return Estudiante.builder()
                .uuidUsuario(source.getUuidUsuario())
                .usuario(usuarioDominio(source.getUsuario()))
                .codigoEstudiantil(source.getCodigoEstudiantil())
                .programaAcademico(source.getProgramaAcademico())
                .semestre(source.getSemestre())
                .facultad(source.getFacultad())
                .asignaturasMatriculadas(asignaturasDominio(source.getAsignaturasMatriculadas()))
                .build();
    }

    @Override
    public EstudianteEntidad toEntidad(Estudiante source) {
        EstudianteEntidad entidad = EstudianteEntidad.builder()
                .uuidUsuario(source.getUuidUsuario())
                .codigoEstudiantil(source.getCodigoEstudiantil())
                .programaAcademico(source.getProgramaAcademico())
                .semestre(source.getSemestre())
                .facultad(source.getFacultad())
                .build();
        if (source.getAsignaturasMatriculadas() != null) {
            for (AsignaturaMatriculada asignatura : source.getAsignaturasMatriculadas()) {
                AsignaturaMatriculadaEntidad asignaturaEntidad = asignaturaMatriculadaMapper.toEntidad(asignatura);
                asignaturaEntidad.setEstudiante(entidad);
                entidad.getAsignaturasMatriculadas().add(asignaturaEntidad);
            }
        }
        return entidad;
    }

    private Usuario usuarioDominio(UsuarioEntidad source) {
        if (source == null) return null;
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
                .build();
    }

    private List<AsignaturaMatriculada> asignaturasDominio(List<AsignaturaMatriculadaEntidad> asignaturas) {
        if (asignaturas == null) return new ArrayList<>();
        List<AsignaturaMatriculada> respuesta = new ArrayList<>();
        for (AsignaturaMatriculadaEntidad asignatura : asignaturas)
            respuesta.add(asignaturaMatriculadaMapper.toDominio(asignatura));
        return respuesta;
    }
}
