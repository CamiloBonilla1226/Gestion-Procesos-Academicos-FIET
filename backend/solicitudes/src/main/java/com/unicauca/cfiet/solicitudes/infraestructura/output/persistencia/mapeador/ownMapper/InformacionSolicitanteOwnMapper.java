package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.InformacionSolicitante;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.InformacionSolicitanteEntidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Service
@RequiredArgsConstructor
public class InformacionSolicitanteOwnMapper implements OwnMapper<InformacionSolicitante, InformacionSolicitanteEntidad> {

    @Override
    public InformacionSolicitante toDominio(InformacionSolicitanteEntidad source) {
        if (source == null) return null;
        return InformacionSolicitante.builder()
                .uuidInformacionSolicitante(source.getUuidInformacionSolicitante())
                .tipoDocumento(source.getTipoDocumento())
                .numeroDocumento(source.getNumeroDocumento())
                .nombres(source.getNombres())
                .apellidos(source.getApellidos())
                .telefono(source.getTelefono())
                .correoElectronico(source.getCorreoElectronico())
                .build();
    }

    @Override
    public InformacionSolicitanteEntidad toEntidad(InformacionSolicitante source) {
        if (source == null) return null;
        return InformacionSolicitanteEntidad.builder()
                .uuidInformacionSolicitante(source.getUuidInformacionSolicitante())
                .tipoDocumento(source.getTipoDocumento())
                .numeroDocumento(source.getNumeroDocumento())
                .nombres(source.getNombres())
                .apellidos(source.getApellidos())
                .telefono(source.getTelefono())
                .correoElectronico(source.getCorreoElectronico())
                .build();
    }
}
