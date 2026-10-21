package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.NotificacionResolucionGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EstadoNotificacionResolucion;
import com.unicauca.cfiet.solicitudes.dominio.modelos.NotificacionResolucion;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ProgramacionResolucion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.NotificacionResolucionEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.NotificacionResolucionOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.NotificacionResolucionRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.SolicitudAcademicaRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static com.unicauca.cfiet.solicitudes.dominio.modelos.EstadoNotificacionResolucion.EN_PROCESO;
import static com.unicauca.cfiet.solicitudes.dominio.modelos.EstadoNotificacionResolucion.FALLIDA;
import static com.unicauca.cfiet.solicitudes.dominio.modelos.EstadoNotificacionResolucion.PROGRAMADA;

@Service
@RequiredArgsConstructor
public class NotificacionResolucionGatewayImplAdaptador implements NotificacionResolucionGatewayIntPuerto {
    private final NotificacionResolucionRepositorio repositorio;
    private final SolicitudAcademicaRepositorio solicitudRepositorio;
    private final NotificacionResolucionOwnMapper mapper;

    @Override
    @Transactional
    public NotificacionResolucion crear(NotificacionResolucion notificacion) {
        NotificacionResolucionEntidad entidad = mapper.toEntidad(notificacion);
        entidad.setNuevo(true);
        entidad.setSolicitudAcademica(solicitudRepositorio.getReferenceById(notificacion.getUuidSolicitudAcademica()));
        return mapper.toDominio(repositorio.saveAndFlush(entidad));
    }

    @Override
    @Transactional(readOnly = true)
    public NotificacionResolucion getPorSolicitud(String uuidSolicitudAcademica) {
        return repositorio.findById(uuidSolicitudAcademica).map(mapper::toDominio).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaRetenida(String uuidSolicitudAcademica) {
        return uuidSolicitudAcademica != null && repositorio.existsByUuidSolicitudAcademicaAndFechaLiberacionIsNull(uuidSolicitudAcademica);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> getRetenidas(List<String> uuidsSolicitud) {
        if (uuidsSolicitud == null || uuidsSolicitud.isEmpty())
            return Set.of();
        return Set.copyOf(repositorio.findRetenidas(uuidsSolicitud));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getPendientes(LocalDateTime ahora, LocalDateTime reclamoVencido) {
        return repositorio.findPendientes(PROGRAMADA, EN_PROCESO, ahora, reclamoVencido);
    }

    @Override
    @Transactional
    public boolean reclamar(String uuidSolicitudAcademica, LocalDateTime ahora, LocalDateTime reclamoVencido) {
        return repositorio.reclamar(uuidSolicitudAcademica, PROGRAMADA, EN_PROCESO, ahora, reclamoVencido) == 1;
    }

    @Override
    @Transactional
    public boolean actualizarProgramacion(String uuidSolicitudAcademica, ProgramacionResolucion programacion, LocalDateTime ahora) {
        return repositorio.actualizarProgramacion(uuidSolicitudAcademica, programacion.getFechaProgramada(), programacion.getNumeroResolucion(),
                programacion.getAnioResolucion(), programacion.getDecision(), PROGRAMADA, ahora) == 1;
    }

    @Override
    @Transactional
    public boolean marcarFirmada(String uuidSolicitudAcademica, LocalDateTime ahora) {
        return repositorio.marcarFirmada(uuidSolicitudAcademica, PROGRAMADA, ahora) == 1;
    }

    @Override
    @Transactional
    public boolean reintentar(String uuidSolicitudAcademica) {
        return repositorio.reintentar(uuidSolicitudAcademica, PROGRAMADA, FALLIDA) == 1;
    }

    @Override
    @Transactional
    public void marcarEnvioEstudiante(String uuidSolicitudAcademica, LocalDateTime fecha) {
        repositorio.marcarEnvioEstudiante(uuidSolicitudAcademica, fecha);
    }

    @Override
    @Transactional
    public void marcarEnvioDarca(String uuidSolicitudAcademica, LocalDateTime fecha) {
        repositorio.marcarEnvioDarca(uuidSolicitudAcademica, fecha);
    }

    @Override
    @Transactional
    public void registrarResultado(String uuidSolicitudAcademica, EstadoNotificacionResolucion estado, int intentos, String ultimoError,
                                   LocalDateTime fechaEnvio) {
        repositorio.registrarResultado(uuidSolicitudAcademica, estado, intentos, ultimoError, fechaEnvio, EN_PROCESO);
    }
}
