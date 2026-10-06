package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudExamenSupletorioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.CruceSupletorio;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudExamenSupletorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudExamenSupletorioEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudSupletorioCruceAsignaturaEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.SolicitudExamenSupletorioOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.SolicitudSupletorioCruceAsignaturaOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.AsignaturaMatriculadaRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.SolicitudAcademicaRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.SolicitudExamenSupletorioRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.SolicitudSupletorioCruceAsignaturaRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SolicitudExamenSupletorioGatewayImplAdaptador implements SolicitudExamenSupletorioGatewayIntPuerto {
    private final SolicitudExamenSupletorioRepositorio repositorio;
    private final SolicitudSupletorioCruceAsignaturaRepositorio cruceRepositorio;
    private final SolicitudAcademicaRepositorio solicitudRepositorio;
    private final AsignaturaMatriculadaRepositorio asignaturaMatriculadaRepositorio;
    private final SolicitudExamenSupletorioOwnMapper mapper;
    private final SolicitudSupletorioCruceAsignaturaOwnMapper cruceMapper;

    @Override
    @Transactional
    public SolicitudExamenSupletorio guardar(SolicitudExamenSupletorio supletorio) {
        String uuidSolicitud = supletorio.getSolicitudAcademica().getUuidSolicitudAcademica();
        SolicitudExamenSupletorioEntidad entidad = mapper.toEntidad(supletorio);
        entidad.setSolicitudAcademica(solicitudRepositorio.getReferenceById(uuidSolicitud));
        entidad.setAsignaturaMatriculada(asignaturaMatriculadaRepositorio.getReferenceById(
                supletorio.getAsignaturaMatriculada().getUuidAsignaturaMatriculada()));
        entidad.setNuevo(true);
        SolicitudExamenSupletorioEntidad guardada = repositorio.saveAndFlush(entidad);

        CruceSupletorio cruce = supletorio.getCruce();
        if (cruce != null) {
            SolicitudSupletorioCruceAsignaturaEntidad cruceEntidad = cruceMapper.toEntidad(cruce);
            cruceEntidad.setSolicitudExamenSupletorio(guardada);
            cruceEntidad.setAsignaturaMatriculadaCruzada(asignaturaMatriculadaRepositorio.getReferenceById(
                    cruce.getAsignaturaMatriculadaCruzada().getUuidAsignaturaMatriculada()));
            cruceEntidad.setNuevo(true);
            cruceRepositorio.saveAndFlush(cruceEntidad);
        }
        return getPorSolicitud(uuidSolicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudExamenSupletorio getPorSolicitud(String uuidSolicitudAcademica) {
        return repositorio.findById(uuidSolicitudAcademica)
                .map(entidad -> {
                    SolicitudExamenSupletorio supletorio = mapper.toDominio(entidad);
                    supletorio.setCruce(cruceRepositorio.findById(uuidSolicitudAcademica).map(cruceMapper::toDominio).orElse(null));
                    return supletorio;
                })
                .orElse(null);
    }
}
