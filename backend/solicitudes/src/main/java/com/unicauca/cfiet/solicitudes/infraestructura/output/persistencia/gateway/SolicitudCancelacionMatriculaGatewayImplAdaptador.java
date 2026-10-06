package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudCancelacionMatriculaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SituacionAcademicaAsignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudCancelacionMatricula;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.AsignaturaMatriculadaEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.AsignaturaSolicitudAcademicaEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SituacionAcademicaAsignaturaEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudAcademicaEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.SolicitudCancelacionMatriculaEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.AsignaturaSolicitudAcademicaOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.SolicitudCancelacionMatriculaOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.AsignaturaMatriculadaRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.AsignaturaSolicitudAcademicaRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.SituacionAcademicaAsignaturaRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.SolicitudAcademicaRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.SolicitudCancelacionMatriculaRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitudCancelacionMatriculaGatewayImplAdaptador implements SolicitudCancelacionMatriculaGatewayIntPuerto {
    private final SolicitudCancelacionMatriculaRepositorio repositorio;
    private final AsignaturaSolicitudAcademicaRepositorio asignaturaSolicitudRepositorio;
    private final SolicitudAcademicaRepositorio solicitudRepositorio;
    private final AsignaturaMatriculadaRepositorio asignaturaMatriculadaRepositorio;
    private final SituacionAcademicaAsignaturaRepositorio situacionRepositorio;
    private final SolicitudCancelacionMatriculaOwnMapper mapper;
    private final AsignaturaSolicitudAcademicaOwnMapper asignaturaMapper;

    @Override
    @Transactional
    public SolicitudCancelacionMatricula guardar(SolicitudCancelacionMatricula cancelacion) {
        String uuidSolicitud = cancelacion.getSolicitudAcademica().getUuidSolicitudAcademica();
        SolicitudAcademicaEntidad solicitud = solicitudRepositorio.getReferenceById(uuidSolicitud);
        SolicitudCancelacionMatriculaEntidad entidad = mapper.toEntidad(cancelacion);
        entidad.setSolicitudAcademica(solicitud);
        entidad.setNuevo(true);
        SolicitudCancelacionMatriculaEntidad guardada = repositorio.saveAndFlush(entidad);

        List<AsignaturaSolicitudAcademicaEntidad> filas = new ArrayList<>();
        for (AsignaturaSolicitudAcademica asignatura : cancelacion.getAsignaturas()) {
            AsignaturaSolicitudAcademicaEntidad fila = asignaturaMapper.toEntidad(asignatura);
            fila.setSolicitudAcademica(solicitud);
            fila.setAsignaturaMatriculada(asignaturaMatriculadaRepositorio.getReferenceById(
                    asignatura.getAsignaturaMatriculada().getUuidAsignaturaMatriculada()));
            filas.add(fila);
        }
        List<AsignaturaSolicitudAcademicaEntidad> guardadas = asignaturaSolicitudRepositorio.saveAllAndFlush(filas);
        return conAsignaturas(guardada, guardadas);
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudCancelacionMatricula getPorSolicitud(String uuidSolicitudAcademica) {
        return repositorio.findById(uuidSolicitudAcademica)
                .map(entidad -> conAsignaturas(entidad,
                        asignaturaSolicitudRepositorio.findBySolicitudAcademica_UuidSolicitudAcademica(uuidSolicitudAcademica)))
                .orElse(null);
    }

    @Override
    @Transactional
    public List<AsignaturaSolicitudAcademica> actualizarAsignaturas(List<AsignaturaSolicitudAcademica> asignaturas) {
        List<AsignaturaSolicitudAcademicaEntidad> filas = new ArrayList<>();
        for (AsignaturaSolicitudAcademica asignatura : asignaturas) {
            AsignaturaSolicitudAcademicaEntidad fila = asignaturaSolicitudRepositorio.findById(asignatura.getUuidAsignaturaSolicitud()).orElseThrow();
            fila.setNumeroFaltas(asignatura.getNumeroFaltas());
            fila.setNota(asignatura.getNota());
            fila.setSituacionMatricula(situacion(asignatura.getSituacionMatricula()));
            fila.setSituacionCancelar(situacion(asignatura.getSituacionCancelar()));
            filas.add(fila);
        }
        return asignaturaSolicitudRepositorio.saveAllAndFlush(filas).stream().map(asignaturaMapper::toDominio).toList();
    }

    @Override
    @Transactional
    public void cambiarEstadoAsignaturasMatriculadas(List<String> uuidsAsignaturaMatriculada, String estado) {
        List<AsignaturaMatriculadaEntidad> asignaturas = asignaturaMatriculadaRepositorio.findAllById(uuidsAsignaturaMatriculada);
        asignaturas.forEach(asignatura -> asignatura.setEstado(estado));
        asignaturaMatriculadaRepositorio.saveAllAndFlush(asignaturas);
    }

    private SituacionAcademicaAsignaturaEntidad situacion(SituacionAcademicaAsignatura situacion) {
        return situacion == null ? null : situacionRepositorio.getReferenceById(situacion.getUuidSituacionAcademica());
    }

    private SolicitudCancelacionMatricula conAsignaturas(SolicitudCancelacionMatriculaEntidad entidad,
                                                       List<AsignaturaSolicitudAcademicaEntidad> asignaturas) {
        SolicitudCancelacionMatricula cancelacion = mapper.toDominio(entidad);
        cancelacion.setAsignaturas(asignaturas.stream().map(asignaturaMapper::toDominio).toList());
        return cancelacion;
    }
}
