package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.UsuarioLivianoEntidad;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Repository
public interface UsuarioLivianoRepositorio extends JpaRepository<UsuarioLivianoEntidad, String> {
    @Query("""
        SELECT u FROM UsuarioLivianoEntidad u
        WHERE LOWER(CONCAT(u.nombres, ' ', u.apellidos)) LIKE LOWER(CONCAT('%', :filtro, '%'))
    """)
    Page<UsuarioLivianoEntidad> findByNombreCompleto(@Param("filtro") String filtro, Pageable pageable);
}
