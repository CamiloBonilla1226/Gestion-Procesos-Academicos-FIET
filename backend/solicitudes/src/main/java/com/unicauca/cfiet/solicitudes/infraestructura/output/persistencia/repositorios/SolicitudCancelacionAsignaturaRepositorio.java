package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudCancelacionAsignaturaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface SolicitudCancelacionAsignaturaRepositorio extends JpaRepository<SolicitudCancelacionAsignaturaEntidad, String> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from SolicitudCancelacionAsignaturaEntidad x where x.uuidSolicitudAcademica in :uuids")
    int eliminarPorSolicitudes(@Param("uuids") Collection<String> uuids);
}
