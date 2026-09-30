package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.LogEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.UsuarioEntidad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Repository
public interface LogRepositorio extends JpaRepository<LogEntidad, String> {
    @Query("from UsuarioEntidad u where u.username = :username")
    Optional<UsuarioEntidad> findUsuarioByUsername(@Param("username") String username);

    @Query("""
        SELECT l FROM LogEntidad l
        WHERE 
            (:responsable IS NULL OR :responsable = '' OR 
                LOWER(CONCAT(l.objUsuarioLog.nombres, ' ', l.objUsuarioLog.apellidos)) 
                    LIKE LOWER(CONCAT('%', :responsable, '%')))
        AND 
            (:fecha IS NULL OR :fecha = '' OR 
                LOWER(l.fecha) LIKE LOWER(CONCAT('%', :fecha, '%')))
        ORDER BY l.fecha DESC
    """)
    Page<LogEntidad> findByResponsableAndFecha(
            @Param("responsable") String responsable,
            @Param("fecha") String fecha,
            Pageable pageable);
}
