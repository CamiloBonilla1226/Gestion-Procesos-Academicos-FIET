package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.UsuarioEntidad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocenteRepositorio extends JpaRepository<UsuarioEntidad, String> {

    @Query(value = """
        SELECT u FROM UsuarioEntidad u JOIN u.roles r
        WHERE r.nombre = :rol AND u.estado = true
          AND (:nombre IS NULL OR LOWER(CONCAT(u.nombres, ' ', u.apellidos)) LIKE LOWER(CONCAT('%', :nombre, '%')))
          AND (:documento IS NULL OR u.numeroDocumento LIKE CONCAT('%', :documento, '%'))
          AND (:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')))
    """, countQuery = """
        SELECT COUNT(u) FROM UsuarioEntidad u JOIN u.roles r
        WHERE r.nombre = :rol AND u.estado = true
          AND (:nombre IS NULL OR LOWER(CONCAT(u.nombres, ' ', u.apellidos)) LIKE LOWER(CONCAT('%', :nombre, '%')))
          AND (:documento IS NULL OR u.numeroDocumento LIKE CONCAT('%', :documento, '%'))
          AND (:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')))
    """)
    Page<UsuarioEntidad> buscarActivosPorRol(@Param("rol") String rol,
                                             @Param("nombre") String nombre,
                                             @Param("documento") String documento,
                                             @Param("username") String username,
                                             Pageable pageable);

    @Query("""
        SELECT u FROM UsuarioEntidad u JOIN u.roles r
        WHERE u.uuidUsuario = :uuidUsuario AND r.nombre = :rol AND u.estado = true
    """)
    Optional<UsuarioEntidad> buscarActivoPorRol(@Param("uuidUsuario") String uuidUsuario, @Param("rol") String rol);
}
