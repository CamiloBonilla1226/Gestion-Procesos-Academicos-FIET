package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.dominio.modelos.EstadoNotificacionResolucion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudAcademicaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    @Query("select s.uuidSolicitudAcademica from HistorialSolicitudAcademicaEntidad h join h.solicitudAcademica s "
            + "where s.tipoSolicitudAcademica.funcionarioAcademico.uuidUsuario = :funcionario and s.etapa.codigo in :finales "
            + "and not exists (select n.uuidSolicitudAcademica from NotificacionResolucionEntidad n "
            + "where n.uuidSolicitudAcademica = s.uuidSolicitudAcademica and n.estado in :bloqueantes) "
            + "group by s.uuidSolicitudAcademica "
            + "having max(h.fecha) >= :desde and max(h.fecha) < :hasta")
    List<String> findRespuestasEliminables(@Param("funcionario") String uuidFuncionario,
                                           @Param("finales") Collection<String> finales,
                                           @Param("bloqueantes") Collection<EstadoNotificacionResolucion> bloqueantes,
                                           @Param("desde") LocalDateTime desde,
                                           @Param("hasta") LocalDateTime hasta);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from SolicitudAcademicaEntidad x where x.uuidSolicitudAcademica in :uuids")
    int eliminarPorSolicitudes(@Param("uuids") Collection<String> uuids);
}
