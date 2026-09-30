package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.OrdenDelDiaEntidad;
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
public interface OrdenDelDiaRepositorio extends JpaRepository<OrdenDelDiaEntidad, String> {
    @Query("""
        SELECT o FROM OrdenDelDiaEntidad o
        WHERE LOWER(o.numeroActa) LIKE LOWER(CONCAT('%', :filtro, '%'))
    """)
    Page<OrdenDelDiaEntidad> findByNumeroActaContainingIgnoreCase(@Param("filtro") String filtro, Pageable pageable);

    List<OrdenDelDiaEntidad> findByEstado(boolean estado);
}
