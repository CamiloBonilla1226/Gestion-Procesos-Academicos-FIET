package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.AsignaturaSolicitudAcademicaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AsignaturaSolicitudAcademicaRepositorio extends JpaRepository<AsignaturaSolicitudAcademicaEntidad, String> {

    List<AsignaturaSolicitudAcademicaEntidad> findBySolicitudAcademica_UuidSolicitudAcademica(String uuidSolicitudAcademica);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from AsignaturaSolicitudAcademicaEntidad x where x.solicitudAcademica.uuidSolicitudAcademica in :uuids")
    int eliminarPorSolicitudes(@Param("uuids") Collection<String> uuids);
}
