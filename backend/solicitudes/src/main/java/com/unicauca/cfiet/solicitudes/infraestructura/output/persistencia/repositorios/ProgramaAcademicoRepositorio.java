package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.ProgramaAcademicoEntidad;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramaAcademicoRepositorio extends JpaRepository<ProgramaAcademicoEntidad, String> {

    List<ProgramaAcademicoEntidad> findAllByOrderByNombreAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProgramaAcademicoEntidad p WHERE p.uuidProgramaAcademico = :uuid")
    Optional<ProgramaAcademicoEntidad> findParaActualizar(@Param("uuid") String uuid);
}
