package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudEntidad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Repository
public interface SolicitudRepositorio extends JpaRepository<SolicitudEntidad, String> {
    @Query("""
    SELECT s FROM SolicitudEntidad s
    WHERE LOWER(s.nombre) LIKE LOWER(CONCAT('%', :filtro, '%'))
    """)
    Page<SolicitudEntidad> buscarPorNombre(
            @Param("filtro") String filtro,
            Pageable pageable
    );

    @Query("""
    SELECT s FROM SolicitudEntidad s
    WHERE s.objFuncionario.uuidUsuario = :uuidUsuario
    AND LOWER(s.nombre) LIKE LOWER(CONCAT('%', :filtro, '%'))
    """)
    Page<SolicitudEntidad> buscarPorNombreYFuncionario(
            @Param("uuidUsuario") String uuidUsuario,
            @Param("filtro") String filtro,
            Pageable pageable
    );

    @Query("""
        SELECT s FROM SolicitudEntidad s
        WHERE 
            (:nombreSolicitud IS NULL OR :nombreSolicitud = '' OR 
                LOWER(s.nombre) LIKE LOWER(CONCAT('%', :nombreSolicitud, '%')))
        AND 
            (:solicitante IS NULL OR :solicitante = '' OR 
                LOWER(
                    REPLACE(
                        CONCAT(
                            s.informacionSolicitante.nombres, 
                            s.informacionSolicitante.apellidos
                        ), 
                        ' ', 
                        ''
                    )
                ) 
                LIKE LOWER(
                    CONCAT(
                        '%', 
                        REPLACE(:solicitante, ' ', ''), 
                        '%'
                    )
                )
            )
        AND
            (:estado IS NULL OR :estado = '' OR 
                LOWER(s.estado) LIKE LOWER(CONCAT('%', :estado, '%'))
            )
        ORDER BY s.fechaCreacion DESC
    """)
        Page<SolicitudEntidad> buscarSolicitudPorSolicitante(
                @Param("nombreSolicitud") String nombreSolicitud,
                @Param("solicitante") String solicitante,
                @Param("estado") String estado,
                Pageable pageable
        );

    List<SolicitudEntidad> findByEstadoIgnoreCase(String estado);

    Page<SolicitudEntidad> findByObjFuncionarioUuidUsuario(
            String uuidUsuario,
            Pageable pageable
    );

    List<SolicitudEntidad> findByObjOrdenDelDiaUuidOrdenDelDia(
            String uuidOrdenDelDia
    );
}
