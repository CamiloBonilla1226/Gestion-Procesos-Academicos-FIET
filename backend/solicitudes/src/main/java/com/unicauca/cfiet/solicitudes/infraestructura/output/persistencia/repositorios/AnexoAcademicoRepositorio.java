package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.AnexoAcademicoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AnexoAcademicoRepositorio extends JpaRepository<AnexoAcademicoEntidad, String> {

    List<AnexoAcademicoEntidad> findBySolicitudAcademicaUuidSolicitudAcademicaOrderByFechaSubidaAsc(String uuidSolicitudAcademica);

    @Query("select a.urlArchivo from AnexoAcademicoEntidad a where a.solicitudAcademica.uuidSolicitudAcademica in :uuids")
    List<String> findArchivosPorSolicitudes(@Param("uuids") Collection<String> uuids);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from AnexoAcademicoEntidad x where x.solicitudAcademica.uuidSolicitudAcademica in :uuids")
    int eliminarPorSolicitudes(@Param("uuids") Collection<String> uuids);
}
