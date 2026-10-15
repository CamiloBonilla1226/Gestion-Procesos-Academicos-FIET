package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.NotificacionDocenteEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificacionDocenteRepositorio extends JpaRepository<NotificacionDocenteEntidad, Long> {

    Optional<NotificacionDocenteEntidad> findByUuidNotificacion(String uuidNotificacion);

    List<NotificacionDocenteEntidad> findByDocenteUuidUsuarioOrderByFechaCreacionDescIdNotificacionDesc(String uuidDocente);
}
