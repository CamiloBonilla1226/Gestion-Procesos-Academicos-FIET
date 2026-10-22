package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudSupletorioCruceAsignaturaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface SolicitudSupletorioCruceAsignaturaRepositorio extends JpaRepository<SolicitudSupletorioCruceAsignaturaEntidad, String> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from SolicitudSupletorioCruceAsignaturaEntidad x where x.uuidSolicitudAcademica in :uuids")
    int eliminarPorSolicitudes(@Param("uuids") Collection<String> uuids);
}
