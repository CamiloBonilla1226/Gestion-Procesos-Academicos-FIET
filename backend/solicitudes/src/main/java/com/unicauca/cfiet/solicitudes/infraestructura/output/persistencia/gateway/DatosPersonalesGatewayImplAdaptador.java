package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.DatosPersonalesGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.UsuarioEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.DatosPersonalesRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatosPersonalesGatewayImplAdaptador implements DatosPersonalesGatewayIntPuerto {
    private final DatosPersonalesRepositorio repositorio;

    @Override
    public boolean existeNumeroDocumentoEnOtro(String numeroDocumento, String uuidUsuario) {
        return repositorio.existsByNumeroDocumentoAndUuidUsuarioNot(numeroDocumento, uuidUsuario);
    }

    @Override
    public boolean existeCorreoEnOtro(String correoElectronico, String uuidUsuario) {
        return repositorio.existsByCorreoElectronicoAndUuidUsuarioNot(correoElectronico, uuidUsuario);
    }

    @Override
    public void actualizar(String uuidUsuario, Usuario cambios) {
        UsuarioEntidad entidad = repositorio.findById(uuidUsuario).orElse(null);
        if (entidad == null) return;
        if (cambios.getNombres() != null) entidad.setNombres(cambios.getNombres());
        if (cambios.getApellidos() != null) entidad.setApellidos(cambios.getApellidos());
        if (cambios.getTipoDocumento() != null) entidad.setTipoDocumento(cambios.getTipoDocumento());
        if (cambios.getNumeroDocumento() != null) entidad.setNumeroDocumento(cambios.getNumeroDocumento());
        if (cambios.getTelefono() != null) entidad.setTelefono(cambios.getTelefono());
        if (cambios.getCorreoElectronico() != null) entidad.setCorreoElectronico(cambios.getCorreoElectronico());
        repositorio.save(entidad);
    }
}
