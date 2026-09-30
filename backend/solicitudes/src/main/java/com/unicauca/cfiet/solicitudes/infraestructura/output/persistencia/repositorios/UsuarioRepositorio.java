package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.RolEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.TipoUsuarioEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.UsuarioEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Repository
public interface UsuarioRepositorio extends JpaRepository <UsuarioEntidad, String> {
    boolean existsByNumeroDocumento(String numeroDocumento);
    boolean existsByCorreoElectronico(String correoElectronico);
    boolean existsByUsername(String username);
    Optional<UsuarioEntidad> findByUsername(String username);

    @Query("from RolEntidad")
    List<RolEntidad> findAllRoles();

    @Query("from TipoUsuarioEntidad")
    List<TipoUsuarioEntidad> findAllTipoUsuario();

    @Query("from TipoUsuarioEntidad t where t.nombre = :nombre")
    Optional<TipoUsuarioEntidad> findTipoUsuarioByNombre(@Param("nombre") String nombre);
}
