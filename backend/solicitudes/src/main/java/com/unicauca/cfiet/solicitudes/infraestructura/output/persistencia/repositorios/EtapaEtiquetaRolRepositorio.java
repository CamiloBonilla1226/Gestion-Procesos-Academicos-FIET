package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.dominio.modelos.RolEtiquetaEtapa;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.EtapaEtiquetaRolEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.EtapaEtiquetaRolIdEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtapaEtiquetaRolRepositorio extends JpaRepository<EtapaEtiquetaRolEntidad, EtapaEtiquetaRolIdEntidad> {

    List<EtapaEtiquetaRolEntidad> findByIdRolOrderByEtapaCodigoAsc(RolEtiquetaEtapa rol);
}
