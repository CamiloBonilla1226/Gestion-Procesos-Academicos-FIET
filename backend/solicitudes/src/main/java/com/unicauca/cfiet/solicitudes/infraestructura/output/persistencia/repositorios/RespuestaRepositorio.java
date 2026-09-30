package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.RespuestaEntidad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Repository
public interface RespuestaRepositorio extends JpaRepository<RespuestaEntidad, String> {
    @Query("""
        SELECT r
        FROM RespuestaEntidad r
        JOIN r.solicitud s
        WHERE LOWER(s.nombre) LIKE LOWER(CONCAT('%', :nombreSolicitud, '%'))
    """)
    Page<RespuestaEntidad> obtenerPorNombreSolicitud(
            @Param("nombreSolicitud") String nombreSolicitud,
            Pageable pageable
    );

    @Query("""
        SELECT r
        FROM RespuestaEntidad r
        JOIN r.solicitud s
        JOIN s.objFuncionario f
        WHERE f.uuidUsuario = :uuidUsuario
    """)
    Page<RespuestaEntidad> obtenerRespuestasPorFuncionario(
            @Param("uuidUsuario") String uuidUsuario,
            Pageable pageable
    );

    @Query("""
        SELECT r
        FROM RespuestaEntidad r
        JOIN r.solicitud s
        JOIN s.objFuncionario f
        WHERE f.uuidUsuario = :uuidUsuario
          AND LOWER(s.nombre) LIKE LOWER(CONCAT('%', :nombreSolicitud, '%'))
    """)
    Page<RespuestaEntidad> obtenerRespuestasPorFuncionarioYNombreSolicitud(
            @Param("uuidUsuario") String uuidUsuario,
            @Param("nombreSolicitud") String nombreSolicitud,
            Pageable pageable
    );


    @Query("""
        SELECT COUNT(r) > 0
        FROM RespuestaEntidad r
        WHERE r.solicitud.uuidSolicitud = :uuidSolicitud
    """)
    boolean existeRespuestaParaSolicitud(@Param("uuidSolicitud") String uuidSolicitud);

    @Query("""
        SELECT r
        FROM RespuestaEntidad r
        WHERE r.solicitud.uuidSolicitud = :uuidSolicitud
    """)
        RespuestaEntidad obtenerPorSolicitud(@Param("uuidSolicitud") String uuidSolicitud);

}
