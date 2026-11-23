package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.CorreoPendienteEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CorreoPendienteRepositorio extends JpaRepository<CorreoPendienteEntidad, Long> {

    @Query("select c.idCorreo from CorreoPendienteEntidad c "
            + "where (c.estado = :pendiente and c.fechaCreacion < :creadoAntesDe) "
            + "or (c.estado = :enviando and c.fechaReclamo < :reclamoVencido) "
            + "order by c.idCorreo")
    List<Long> findPorEnviar(@Param("pendiente") String pendiente,
                             @Param("enviando") String enviando,
                             @Param("creadoAntesDe") LocalDateTime creadoAntesDe,
                             @Param("reclamoVencido") LocalDateTime reclamoVencido);

    @Query("select count(c) from CorreoPendienteEntidad c where c.estado in :estados and c.ultimoError is not null")
    long contarFallidos(@Param("estados") List<String> estados);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update CorreoPendienteEntidad c set c.estado = :enviando, c.fechaReclamo = :ahora "
            + "where c.idCorreo = :id and (c.estado = :pendiente or (c.estado = :enviando and c.fechaReclamo < :reclamoVencido))")
    int reclamar(@Param("id") Long id,
                 @Param("pendiente") String pendiente,
                 @Param("enviando") String enviando,
                 @Param("ahora") LocalDateTime ahora,
                 @Param("reclamoVencido") LocalDateTime reclamoVencido);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update CorreoPendienteEntidad c set c.estado = :enviado, c.intentos = c.intentos + 1, c.ultimoError = null, "
            + "c.adjuntos = null, c.fechaEnvio = :fecha, c.fechaReclamo = null "
            + "where c.idCorreo = :id and c.estado = :enviando")
    int marcarEnviado(@Param("id") Long id,
                      @Param("enviado") String enviado,
                      @Param("enviando") String enviando,
                      @Param("fecha") LocalDateTime fecha);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update CorreoPendienteEntidad c set c.estado = :estado, c.intentos = :intentos, c.ultimoError = :ultimoError, "
            + "c.fechaReclamo = null where c.idCorreo = :id and c.estado = :enviando")
    int registrarFallo(@Param("id") Long id,
                       @Param("estado") String estado,
                       @Param("intentos") int intentos,
                       @Param("ultimoError") String ultimoError,
                       @Param("enviando") String enviando);
}
