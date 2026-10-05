package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.EtapaSolicitudAcademicaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtapaSolicitudAcademicaRepositorio extends JpaRepository<EtapaSolicitudAcademicaEntidad, String> {

    @Query("""
        SELECT e FROM EtapaSolicitudAcademicaEntidad e
        LEFT JOIN e.tipoSolicitudAcademica t
        WHERE t IS NULL OR t.uuidTipoSolicitudAcademica = :uuidTipo
        ORDER BY e.codigo
    """)
    List<EtapaSolicitudAcademicaEntidad> findPorTipoIncluyendoUniversales(@Param("uuidTipo") String uuidTipo);
}
