package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.HistorialSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudAcademica;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
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
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitudAcademicaGatewayImplAdaptador implements SolicitudAcademicaGatewayIntPuerto {
    private static final String RESTRICCION_RADICADO = "uk_solacad_radicado";

    private final SolicitudAcademicaRepositorio repositorio;
    private final HistorialSolicitudAcademicaRepositorio historialRepositorio;
    private final EstudianteRepositorio estudianteRepositorio;
    private final TipoSolicitudAcademicaRepositorio tipoSolicitudRepositorio;
    private final EtapaSolicitudAcademicaRepositorio etapaRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final SolicitudAcademicaOwnMapper mapper;
    private final HistorialSolicitudAcademicaOwnMapper historialMapper;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;

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
    @Transactional(readOnly = true)
    public String getRadicadoEnCurso(String uuidEstudiante, String uuidTipoSolicitudAcademica, List<String> etapasFinales) {
        return repositorio.findFirstByEstudiante_UuidUsuarioAndTipoSolicitudAcademica_UuidTipoSolicitudAcademicaAndEtapa_CodigoNotInOrderByFechaCreacionDesc(
                        uuidEstudiante, uuidTipoSolicitudAcademica, etapasFinales)
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
        SolicitudAcademicaEntidad guardada = null;
        try {
            guardada = repositorio.saveAndFlush(entidad);
        } catch (DataIntegrityViolationException error) {
            if (!esRadicadoDuplicado(error))
                throw error;
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.RADICADO_NO_GENERADO);
        }
        guardarHistorial(guardada, historial);
        return mapper.toDominio(guardada);
    }

    private boolean esRadicadoDuplicado(Throwable error) {
        for (Throwable causa = error; causa != null; causa = causa.getCause()) {
            if (causa instanceof ConstraintViolationException violacion && mencionaRadicado(violacion.getConstraintName()))
                return true;
            if (mencionaRadicado(causa.getMessage()))
                return true;
        }
        return false;
    }

    private boolean mencionaRadicado(String texto) {
        return texto != null && texto.contains(RESTRICCION_RADICADO);
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
