package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.EtapaEtiquetaRolGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EtapaEtiquetaRol;
import com.unicauca.cfiet.solicitudes.dominio.modelos.RolEtiquetaEtapa;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.EtapaEtiquetaRolOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.EtapaEtiquetaRolRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EtapaEtiquetaRolGatewayImplAdaptador implements EtapaEtiquetaRolGatewayIntPuerto {
    private final EtapaEtiquetaRolRepositorio repositorio;
    private final EtapaEtiquetaRolOwnMapper mapper;

    @Override
    public List<EtapaEtiquetaRol> getPorRol(RolEtiquetaEtapa rol) {
        return repositorio.findByIdRolOrderByEtapaCodigoAsc(rol).stream()
                .map(mapper::toDominio)
                .toList();
    }
}
