package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SituacionAcademicaAsignaturaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SituacionAcademicaAsignaturaRepositorio extends JpaRepository<SituacionAcademicaAsignaturaEntidad, String> {

    List<SituacionAcademicaAsignaturaEntidad> findAllByOrderByCodigoAsc();
}
