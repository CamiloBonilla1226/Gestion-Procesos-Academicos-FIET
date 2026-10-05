package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.FuncionarioAcademicoEntidad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FuncionarioAcademicoRepositorio extends JpaRepository<FuncionarioAcademicoEntidad, String> {

    @Query("""
        SELECT f FROM FuncionarioAcademicoEntidad f
        WHERE (:nombre IS NULL OR LOWER(f.usuario.nombres) LIKE LOWER(CONCAT('%', :nombre, '%')))
          AND (:apellido IS NULL OR LOWER(f.usuario.apellidos) LIKE LOWER(CONCAT('%', :apellido, '%')))
          AND (:dependencia IS NULL OR LOWER(f.dependencia) LIKE LOWER(CONCAT('%', :dependencia, '%')))
    """)
    Page<FuncionarioAcademicoEntidad> findByFiltro(@Param("nombre") String nombre,
                                                   @Param("apellido") String apellido,
                                                   @Param("dependencia") String dependencia,
                                                   Pageable pageable);
}
