package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios;

import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.UsuarioEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatosPersonalesRepositorio extends JpaRepository<UsuarioEntidad, String> {

    boolean existsByNumeroDocumentoAndUuidUsuarioNot(String numeroDocumento, String uuidUsuario);

    boolean existsByCorreoElectronicoAndUuidUsuarioNot(String correoElectronico, String uuidUsuario);
}
