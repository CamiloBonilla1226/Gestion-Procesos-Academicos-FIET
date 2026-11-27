package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.TipoSolicitudConsejoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoSolicitudConsejoRepositorio extends JpaRepository<TipoSolicitudConsejoEntidad, String> {
    Optional<TipoSolicitudConsejoEntidad> findByTipoSolicitudAcademicaUuidTipoSolicitudAcademica(String uuidTipoSolicitudAcademica);
}
