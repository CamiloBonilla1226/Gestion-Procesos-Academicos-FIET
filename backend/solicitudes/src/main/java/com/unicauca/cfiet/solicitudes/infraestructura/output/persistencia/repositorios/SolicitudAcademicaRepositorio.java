package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudAcademicaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface SolicitudAcademicaRepositorio extends JpaRepository<SolicitudAcademicaEntidad, String> {

    Optional<SolicitudAcademicaEntidad> findTopByRadicadoStartingWithOrderByRadicadoDesc(String prefijo);

    Optional<SolicitudAcademicaEntidad> findFirstByEstudiante_UuidUsuarioAndTipoSolicitudAcademica_UuidTipoSolicitudAcademicaAndEtapa_CodigoNotInOrderByFechaCreacionDesc(
            String uuidEstudiante, String uuidTipoSolicitudAcademica, Collection<String> etapasFinales);
}
