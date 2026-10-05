package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.TipoSolicitudAcademicaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoSolicitudAcademicaRepositorio extends JpaRepository<TipoSolicitudAcademicaEntidad, String> {

    List<TipoSolicitudAcademicaEntidad> findAllByOrderByNombreAsc();
}
