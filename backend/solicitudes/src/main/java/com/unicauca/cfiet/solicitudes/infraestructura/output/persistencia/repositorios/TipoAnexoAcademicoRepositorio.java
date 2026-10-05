package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.TipoAnexoAcademicoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoAnexoAcademicoRepositorio extends JpaRepository<TipoAnexoAcademicoEntidad, String> {

    List<TipoAnexoAcademicoEntidad> findByTipoSolicitudAcademicaUuidTipoSolicitudAcademicaOrderByNombreAsc(String uuidTipoSolicitudAcademica);
}
