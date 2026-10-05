package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.EstudianteEntidad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EstudianteRepositorio extends JpaRepository<EstudianteEntidad, String> {

    boolean existsByCodigoEstudiantil(String codigoEstudiantil);

    @Query("""
        SELECT e FROM EstudianteEntidad e
        WHERE (:nombre IS NULL OR LOWER(e.usuario.nombres) LIKE LOWER(CONCAT('%', :nombre, '%')))
          AND (:apellido IS NULL OR LOWER(e.usuario.apellidos) LIKE LOWER(CONCAT('%', :apellido, '%')))
          AND (:codigo IS NULL OR LOWER(e.codigoEstudiantil) LIKE LOWER(CONCAT('%', :codigo, '%')))
    """)
    Page<EstudianteEntidad> findByFiltro(@Param("nombre") String nombre,
                                         @Param("apellido") String apellido,
                                         @Param("codigo") String codigo,
                                         Pageable pageable);
}
