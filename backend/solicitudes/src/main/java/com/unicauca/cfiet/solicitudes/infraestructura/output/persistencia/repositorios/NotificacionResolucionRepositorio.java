package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.dominio.modelos.EstadoNotificacionResolucion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.NotificacionResolucionEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface NotificacionResolucionRepositorio extends JpaRepository<NotificacionResolucionEntidad, String> {

    @Query("select count(n) > 0 from NotificacionResolucionEntidad n "
            + "where n.uuidSolicitudAcademica = :uuid and n.fechaLiberacion is null and n.fechaProgramada > :ahora")
    boolean estaRetenida(@Param("uuid") String uuid, @Param("ahora") LocalDateTime ahora);

    @Query("select n.uuidSolicitudAcademica from NotificacionResolucionEntidad n "
            + "where n.uuidSolicitudAcademica in :uuids and n.fechaLiberacion is null and n.fechaProgramada > :ahora")
    List<String> findRetenidas(@Param("uuids") Collection<String> uuids, @Param("ahora") LocalDateTime ahora);

    @Query("select n.uuidSolicitudAcademica from NotificacionResolucionEntidad n "
            + "where (n.estado = :programada and (n.fechaProgramada <= :ahora or n.firmada = true)) "
            + "or (n.estado = :enProceso and n.fechaReclamo < :reclamoVencido) "
            + "order by n.fechaProgramada")
    List<String> findPendientes(@Param("programada") EstadoNotificacionResolucion programada,
                                @Param("enProceso") EstadoNotificacionResolucion enProceso,
                                @Param("ahora") LocalDateTime ahora,
                                @Param("reclamoVencido") LocalDateTime reclamoVencido);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update NotificacionResolucionEntidad n set n.estado = :enProceso, n.fechaReclamo = :ahora, "
            + "n.fechaLiberacion = coalesce(n.fechaLiberacion, :ahora) "
            + "where n.uuidSolicitudAcademica = :uuid and ((n.estado = :programada and (n.fechaProgramada <= :ahora or n.firmada = true)) "
            + "or (n.estado = :enProceso and n.fechaReclamo < :reclamoVencido))")
    int reclamar(@Param("uuid") String uuid,
                 @Param("programada") EstadoNotificacionResolucion programada,
                 @Param("enProceso") EstadoNotificacionResolucion enProceso,
                 @Param("ahora") LocalDateTime ahora,
                 @Param("reclamoVencido") LocalDateTime reclamoVencido);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update NotificacionResolucionEntidad n set n.fechaProgramada = :fecha, n.numeroResolucion = :numero, "
            + "n.anioResolucion = :anio, n.decision = :decision "
            + "where n.uuidSolicitudAcademica = :uuid and n.estado = :programada and n.firmada = false and n.fechaProgramada > :ahora")
    int actualizarProgramacion(@Param("uuid") String uuid,
                               @Param("fecha") LocalDateTime fecha,
                               @Param("numero") String numero,
                               @Param("anio") Integer anio,
                               @Param("decision") String decision,
                               @Param("programada") EstadoNotificacionResolucion programada,
                               @Param("ahora") LocalDateTime ahora);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update NotificacionResolucionEntidad n set n.firmada = true, n.textoDarca = :textoDarca, "
            + "n.fechaLiberacion = coalesce(n.fechaLiberacion, :ahora) "
            + "where n.uuidSolicitudAcademica = :uuid and n.estado = :programada and n.firmada = false and n.fechaProgramada > :ahora")
    int marcarFirmada(@Param("uuid") String uuid,
                      @Param("textoDarca") String textoDarca,
                      @Param("programada") EstadoNotificacionResolucion programada,
                      @Param("ahora") LocalDateTime ahora);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update NotificacionResolucionEntidad n set n.estado = :programada, n.intentos = 0, n.fechaReclamo = null "
            + "where n.uuidSolicitudAcademica = :uuid and n.estado = :fallida")
    int reintentar(@Param("uuid") String uuid,
                   @Param("programada") EstadoNotificacionResolucion programada,
                   @Param("fallida") EstadoNotificacionResolucion fallida);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update NotificacionResolucionEntidad n set n.fechaEnvioEstudiante = :fecha where n.uuidSolicitudAcademica = :uuid")
    int marcarEnvioEstudiante(@Param("uuid") String uuid, @Param("fecha") LocalDateTime fecha);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update NotificacionResolucionEntidad n set n.fechaEnvioDarca = :fecha where n.uuidSolicitudAcademica = :uuid")
    int marcarEnvioDarca(@Param("uuid") String uuid, @Param("fecha") LocalDateTime fecha);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update NotificacionResolucionEntidad n set n.estado = :estado, n.intentos = :intentos, n.ultimoError = :ultimoError, "
            + "n.fechaEnvio = :fechaEnvio, n.fechaReclamo = null "
            + "where n.uuidSolicitudAcademica = :uuid and n.estado = :enProceso")
    int registrarResultado(@Param("uuid") String uuid,
                           @Param("estado") EstadoNotificacionResolucion estado,
                           @Param("intentos") int intentos,
                           @Param("ultimoError") String ultimoError,
                           @Param("fechaEnvio") LocalDateTime fechaEnvio,
                           @Param("enProceso") EstadoNotificacionResolucion enProceso);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from NotificacionResolucionEntidad x where x.uuidSolicitudAcademica in :uuids")
    int eliminarPorSolicitudes(@Param("uuids") Collection<String> uuids);
}
