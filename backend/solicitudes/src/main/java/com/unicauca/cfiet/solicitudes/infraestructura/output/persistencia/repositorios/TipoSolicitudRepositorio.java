package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.TipoSolicitudEntidad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Repository
public interface TipoSolicitudRepositorio extends JpaRepository<TipoSolicitudEntidad, String> {
    @Query("""
        SELECT t FROM TipoSolicitudEntidad t
        WHERE 
            (:nombre IS NULL OR :nombre = '' OR 
                LOWER(t.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
        AND 
            (:funcionario IS NULL OR :funcionario = '' OR 
                LOWER(CONCAT(t.objFuncionarioEncargado.nombres, ' ', t.objFuncionarioEncargado.apellidos)) 
                    LIKE LOWER(CONCAT('%', :funcionario, '%')))
        ORDER BY t.nombre ASC
    """)
    Page<TipoSolicitudEntidad> findByNombreAndFuncionario(
            @Param("nombre") String nombreSolicitud,
            @Param("funcionario") String funcionario,
            Pageable pageable);

    @Query("""
        SELECT t FROM TipoSolicitudEntidad t
        WHERE 
            (:nombre IS NULL OR :nombre = '' OR 
                LOWER(t.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
        AND
            (:perfil IS NULL OR :perfil = '' OR 
                LOWER(t.perfilSolicitante) = LOWER(:perfil))
        ORDER BY t.nombre ASC
    """)
    Page<TipoSolicitudEntidad> findByPerfilSolicitanteAndNombre(
            @Param("perfil") String perfilSolicitante,
            @Param("nombre") String nombre,
            Pageable pageable
    );

    Page<TipoSolicitudEntidad> findByPerfilSolicitanteIgnoreCase(
            String perfilSolicitante,
            Pageable pageable
    );

    List<TipoSolicitudEntidad> findByPerfilSolicitanteIgnoreCase(String perfilSolicitante);
}
