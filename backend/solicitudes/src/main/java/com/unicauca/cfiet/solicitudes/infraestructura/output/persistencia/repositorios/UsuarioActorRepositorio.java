package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.UsuarioEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioActorRepositorio extends JpaRepository<UsuarioEntidad, String> {

    interface FilaActor {
        String getUuidUsuario();
        String getUsername();
        String getUuidRol();
        String getNombreRol();
        String getDescripcionRol();
        Boolean getEstadoRol();
    }

    @Query("""
        SELECT u.uuidUsuario AS uuidUsuario, u.username AS username, r.uuidRol AS uuidRol,
               r.nombre AS nombreRol, r.descripcion AS descripcionRol, r.estado AS estadoRol
        FROM UsuarioEntidad u LEFT JOIN u.roles r
        WHERE u.uuidUsuario = :uuidUsuario
    """)
    List<FilaActor> buscarPorUuid(@Param("uuidUsuario") String uuidUsuario);

    @Query("""
        SELECT u.uuidUsuario AS uuidUsuario, u.username AS username, r.uuidRol AS uuidRol,
               r.nombre AS nombreRol, r.descripcion AS descripcionRol, r.estado AS estadoRol
        FROM UsuarioEntidad u LEFT JOIN u.roles r
        WHERE u.username = :username
    """)
    List<FilaActor> buscarPorUsername(@Param("username") String username);
}
