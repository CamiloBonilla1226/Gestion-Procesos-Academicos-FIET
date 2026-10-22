package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.NotificacionDocenteEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificacionDocenteRepositorio extends JpaRepository<NotificacionDocenteEntidad, Long> {

    Optional<NotificacionDocenteEntidad> findByUuidNotificacion(String uuidNotificacion);

    List<NotificacionDocenteEntidad> findByDocenteUuidUsuarioOrderByFechaCreacionDescIdNotificacionDesc(String uuidDocente);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from NotificacionDocenteEntidad x where x.solicitudAcademica.uuidSolicitudAcademica in :uuids")
    int eliminarPorSolicitudes(@Param("uuids") Collection<String> uuids);
}
