package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudSupletorioCruceAsignaturaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudSupletorioCruceAsignaturaRepositorio extends JpaRepository<SolicitudSupletorioCruceAsignaturaEntidad, String> {
}
