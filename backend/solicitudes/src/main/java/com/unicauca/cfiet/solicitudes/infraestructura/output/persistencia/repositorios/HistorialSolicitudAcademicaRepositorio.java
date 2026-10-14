package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.HistorialSolicitudAcademicaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface HistorialSolicitudAcademicaRepositorio extends JpaRepository<HistorialSolicitudAcademicaEntidad, String> {

    List<HistorialSolicitudAcademicaEntidad> findBySolicitudAcademicaUuidSolicitudAcademicaOrderByFechaAsc(String uuidSolicitudAcademica);

    @Query("select h.solicitudAcademica.uuidSolicitudAcademica, h.accion from HistorialSolicitudAcademicaEntidad h " +
            "where h.solicitudAcademica.uuidSolicitudAcademica in :uuids and h.accion in :acciones order by h.fecha")
    List<Object[]> findAccionesPorSolicitudes(@Param("uuids") Collection<String> uuids, @Param("acciones") Collection<String> acciones);
}
