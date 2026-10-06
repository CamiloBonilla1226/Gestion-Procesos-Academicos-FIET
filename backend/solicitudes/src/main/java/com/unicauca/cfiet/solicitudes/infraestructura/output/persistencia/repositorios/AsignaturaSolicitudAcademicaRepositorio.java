package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.AsignaturaSolicitudAcademicaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsignaturaSolicitudAcademicaRepositorio extends JpaRepository<AsignaturaSolicitudAcademicaEntidad, String> {

    List<AsignaturaSolicitudAcademicaEntidad> findBySolicitudAcademica_UuidSolicitudAcademica(String uuidSolicitudAcademica);
}
