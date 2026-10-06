package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.AnexoAcademicoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnexoAcademicoRepositorio extends JpaRepository<AnexoAcademicoEntidad, String> {

    List<AnexoAcademicoEntidad> findBySolicitudAcademicaUuidSolicitudAcademicaOrderByFechaSubidaAsc(String uuidSolicitudAcademica);
}
