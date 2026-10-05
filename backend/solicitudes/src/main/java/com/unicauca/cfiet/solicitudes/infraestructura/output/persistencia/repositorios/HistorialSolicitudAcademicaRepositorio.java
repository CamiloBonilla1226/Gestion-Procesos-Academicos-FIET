package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.HistorialSolicitudAcademicaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialSolicitudAcademicaRepositorio extends JpaRepository<HistorialSolicitudAcademicaEntidad, String> {

    List<HistorialSolicitudAcademicaEntidad> findBySolicitudAcademicaUuidSolicitudAcademicaOrderByFechaAsc(String uuidSolicitudAcademica);
}
