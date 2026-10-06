package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudAcademicaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudAcademicaRepositorio extends JpaRepository<SolicitudAcademicaEntidad, String> {

    Optional<SolicitudAcademicaEntidad> findTopByRadicadoStartingWithOrderByRadicadoDesc(String prefijo);

    Optional<SolicitudAcademicaEntidad> findFirstByEstudiante_UuidUsuarioAndTipoSolicitudAcademica_UuidTipoSolicitudAcademicaAndEtapa_CodigoNotInOrderByFechaCreacionDesc(
            String uuidEstudiante, String uuidTipoSolicitudAcademica, Collection<String> etapasFinales);

    List<SolicitudAcademicaEntidad> findByEstudiante_UuidUsuarioOrderByFechaCreacionDescRadicadoDesc(String uuidEstudiante);

    List<SolicitudAcademicaEntidad> findByTipoSolicitudAcademica_FuncionarioAcademico_UuidUsuarioOrderByFechaCreacionDescRadicadoDesc(
            String uuidFuncionarioAcademico);

    List<SolicitudAcademicaEntidad> findAllByOrderByFechaCreacionDescRadicadoDesc();
}
