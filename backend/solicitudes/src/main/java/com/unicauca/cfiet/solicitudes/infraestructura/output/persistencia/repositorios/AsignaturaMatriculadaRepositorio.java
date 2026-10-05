package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.AsignaturaMatriculadaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsignaturaMatriculadaRepositorio extends JpaRepository<AsignaturaMatriculadaEntidad, String> {

    List<AsignaturaMatriculadaEntidad> findByEstudianteUuidUsuarioOrderByAsignaturaCodigoAsignaturaAsc(String uuidUsuario);

    Optional<AsignaturaMatriculadaEntidad> findByUuidAsignaturaMatriculadaAndEstudianteUuidUsuario(String uuidAsignaturaMatriculada, String uuidUsuario);
}
