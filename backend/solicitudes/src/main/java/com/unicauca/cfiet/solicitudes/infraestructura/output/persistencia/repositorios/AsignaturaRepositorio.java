package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.AsignaturaEntidad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsignaturaRepositorio extends JpaRepository<AsignaturaEntidad, String> {

    Optional<AsignaturaEntidad> findByCodigoAsignatura(String codigoAsignatura);

    @Query(value = """
        SELECT a FROM AsignaturaEntidad a JOIN a.programaAcademico p
        ORDER BY p.sigla, LENGTH(a.codigoAsignatura), a.codigoAsignatura
    """, countQuery = "SELECT COUNT(a) FROM AsignaturaEntidad a")
    Page<AsignaturaEntidad> findOrdenadas(Pageable pageable);

    @Query(value = """
        SELECT a FROM AsignaturaEntidad a JOIN a.programaAcademico p
        WHERE (:uuidPrograma IS NULL OR p.uuidProgramaAcademico = :uuidPrograma)
          AND (:texto IS NULL
           OR LOWER(a.codigoAsignatura) LIKE LOWER(CONCAT('%', :texto, '%'))
           OR LOWER(a.nombreAsignatura) LIKE LOWER(CONCAT('%', :texto, '%')))
        ORDER BY p.sigla, LENGTH(a.codigoAsignatura), a.codigoAsignatura
    """, countQuery = """
        SELECT COUNT(a) FROM AsignaturaEntidad a JOIN a.programaAcademico p
        WHERE (:uuidPrograma IS NULL OR p.uuidProgramaAcademico = :uuidPrograma)
          AND (:texto IS NULL
           OR LOWER(a.codigoAsignatura) LIKE LOWER(CONCAT('%', :texto, '%'))
           OR LOWER(a.nombreAsignatura) LIKE LOWER(CONCAT('%', :texto, '%')))
    """)
    Page<AsignaturaEntidad> findByTextoYPrograma(@Param("texto") String texto,
                                                 @Param("uuidPrograma") String uuidPrograma,
                                                 Pageable pageable);

    @Query("""
        SELECT a FROM AsignaturaEntidad a
        WHERE a.programaAcademico.uuidProgramaAcademico = :uuidPrograma
        ORDER BY LENGTH(a.codigoAsignatura), a.codigoAsignatura
    """)
    List<AsignaturaEntidad> findByPrograma(@Param("uuidPrograma") String uuidPrograma);
}
