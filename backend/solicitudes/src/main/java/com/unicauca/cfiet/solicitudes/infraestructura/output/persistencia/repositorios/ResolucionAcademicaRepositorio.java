package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.ResolucionAcademicaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ResolucionAcademicaRepositorio extends JpaRepository<ResolucionAcademicaEntidad, String> {

    @Query("select r.urlArchivo from ResolucionAcademicaEntidad r where r.uuidSolicitudAcademica in :uuids")
    List<String> findArchivosPorSolicitudes(@Param("uuids") Collection<String> uuids);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ResolucionAcademicaEntidad x where x.uuidSolicitudAcademica in :uuids")
    int eliminarPorSolicitudes(@Param("uuids") Collection<String> uuids);
}
