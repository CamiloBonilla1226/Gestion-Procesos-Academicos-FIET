package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.dominio.modelos.RolEtiquetaEtapa;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.EtapaEtiquetaRolEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.EtapaEtiquetaRolIdEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtapaEtiquetaRolRepositorio extends JpaRepository<EtapaEtiquetaRolEntidad, EtapaEtiquetaRolIdEntidad> {

    @Query("select x from EtapaEtiquetaRolEntidad x join fetch x.etapa e left join fetch e.tipoSolicitudAcademica t " +
            "left join fetch t.funcionarioAcademico f left join fetch f.usuario where x.id.rol = :rol order by e.codigo asc")
    List<EtapaEtiquetaRolEntidad> buscarPorRolConEtapa(@Param("rol") RolEtiquetaEtapa rol);
}
