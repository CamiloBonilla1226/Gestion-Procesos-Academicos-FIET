package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.HistorialSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudAcademica;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.HistorialSolicitudAcademicaEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudAcademicaEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.HistorialSolicitudAcademicaOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.SolicitudAcademicaOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.EstudianteRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.EtapaSolicitudAcademicaRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.HistorialSolicitudAcademicaRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.SolicitudAcademicaRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.TipoSolicitudAcademicaRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SolicitudAcademicaGatewayImplAdaptador implements SolicitudAcademicaGatewayIntPuerto {
    private final SolicitudAcademicaRepositorio repositorio;
    private final HistorialSolicitudAcademicaRepositorio historialRepositorio;
    private final EstudianteRepositorio estudianteRepositorio;
    private final TipoSolicitudAcademicaRepositorio tipoSolicitudRepositorio;
    private final EtapaSolicitudAcademicaRepositorio etapaRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final SolicitudAcademicaOwnMapper mapper;
    private final HistorialSolicitudAcademicaOwnMapper historialMapper;

    @Override
    @Transactional(readOnly = true)
    public SolicitudAcademica getPorUuid(String uuidSolicitudAcademica) {
        return repositorio.findById(uuidSolicitudAcademica)
                .map(mapper::toDominio)
                .orElse(null);
    }

    @Override
    public String getUltimoRadicado(String prefijo) {
        return repositorio.findTopByRadicadoStartingWithOrderByRadicadoDesc(prefijo)
                .map(SolicitudAcademicaEntidad::getRadicado)
                .orElse(null);
    }

    @Override
    @Transactional
    public SolicitudAcademica crear(SolicitudAcademica solicitud, HistorialSolicitudAcademica historial) {
        SolicitudAcademicaEntidad entidad = mapper.toEntidad(solicitud);
        entidad.setEstudiante(estudianteRepositorio.getReferenceById(solicitud.getEstudiante().getUuidUsuario()));
        entidad.setTipoSolicitudAcademica(tipoSolicitudRepositorio.getReferenceById(
                solicitud.getTipoSolicitudAcademica().getUuidTipoSolicitudAcademica()));
        entidad.setEtapa(etapaRepositorio.getReferenceById(solicitud.getEtapa().getUuidEtapa()));
        SolicitudAcademicaEntidad guardada = repositorio.saveAndFlush(entidad);
        guardarHistorial(guardada, historial);
        return mapper.toDominio(guardada);
    }

    @Override
    @Transactional
    public SolicitudAcademica actualizarEtapa(SolicitudAcademica solicitud, HistorialSolicitudAcademica historial) {
        SolicitudAcademicaEntidad entidad = repositorio.findById(solicitud.getUuidSolicitudAcademica()).orElseThrow();
        entidad.setEtapa(etapaRepositorio.getReferenceById(solicitud.getEtapa().getUuidEtapa()));
        SolicitudAcademicaEntidad guardada = repositorio.saveAndFlush(entidad);
        guardarHistorial(guardada, historial);
        return mapper.toDominio(guardada);
    }

    private void guardarHistorial(SolicitudAcademicaEntidad solicitud, HistorialSolicitudAcademica historial) {
        HistorialSolicitudAcademicaEntidad entidad = historialMapper.toEntidad(historial);
        entidad.setSolicitudAcademica(solicitud);
        entidad.setUsuario(usuarioRepositorio.getReferenceById(historial.getUsuario().getUuidUsuario()));
        historialRepositorio.saveAndFlush(entidad);
    }
}
