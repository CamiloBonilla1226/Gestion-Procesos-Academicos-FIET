package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudAcademicaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("select distinct a.solicitudAcademica.uuidSolicitudAcademica from AsignaturaSolicitudAcademicaEntidad a " +
            "where a.solicitudAcademica.uuidSolicitudAcademica in :uuids and a.aprobadaPorDecano = false")
    List<String> findConAsignaturaNoAprobadaPorDecano(@Param("uuids") Collection<String> uuids);

    @Query("select a.solicitudAcademica.uuidSolicitudAcademica, a.uuidAsignaturaSolicitud, a.aprobadaPorDecano " +
            "from AsignaturaSolicitudAcademicaEntidad a where a.solicitudAcademica.uuidSolicitudAcademica in :uuids " +
            "order by a.asignaturaMatriculada.asignatura.codigoAsignatura, a.uuidAsignaturaSolicitud")
    List<Object[]> findResultadosAsignaturas(@Param("uuids") Collection<String> uuids);
}
