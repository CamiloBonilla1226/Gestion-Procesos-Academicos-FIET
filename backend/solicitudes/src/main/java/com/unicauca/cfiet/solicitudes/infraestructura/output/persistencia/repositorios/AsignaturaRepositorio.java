package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.AsignaturaEntidad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AsignaturaRepositorio extends JpaRepository<AsignaturaEntidad, String> {

    boolean existsByCodigoAsignatura(String codigoAsignatura);

    Optional<AsignaturaEntidad> findByCodigoAsignatura(String codigoAsignatura);

    @Query("""
        SELECT a FROM AsignaturaEntidad a
        WHERE :texto IS NULL
           OR LOWER(a.codigoAsignatura) LIKE LOWER(CONCAT('%', :texto, '%'))
           OR LOWER(a.nombreAsignatura) LIKE LOWER(CONCAT('%', :texto, '%'))
    """)
    Page<AsignaturaEntidad> findByTexto(@Param("texto") String texto, Pageable pageable);
}
