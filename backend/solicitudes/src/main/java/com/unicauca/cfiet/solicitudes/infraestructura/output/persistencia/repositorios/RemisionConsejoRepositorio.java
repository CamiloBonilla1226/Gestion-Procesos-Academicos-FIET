package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.RemisionConsejoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface RemisionConsejoRepositorio extends JpaRepository<RemisionConsejoEntidad, String> {
    Optional<RemisionConsejoEntidad> findBySolicitudUuidSolicitud(String uuidSolicitud);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from RemisionConsejoEntidad x where x.uuidSolicitudAcademica in :uuids")
    int eliminarPorSolicitudes(@Param("uuids") Collection<String> uuids);
}
