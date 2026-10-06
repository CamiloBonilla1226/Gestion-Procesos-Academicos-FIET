package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudCancelacionAsignaturaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudCancelacionAsignaturaRepositorio extends JpaRepository<SolicitudCancelacionAsignaturaEntidad, String> {
}
