package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.NotificacionDocenteGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.NotificacionDocente;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.NotificacionDocenteEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper.NotificacionDocenteOwnMapper;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.NotificacionDocenteRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.SolicitudAcademicaRepositorio;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionDocenteGatewayImplAdaptador implements NotificacionDocenteGatewayIntPuerto {
    private final NotificacionDocenteRepositorio repositorio;
    private final SolicitudAcademicaRepositorio solicitudRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final NotificacionDocenteOwnMapper mapper;

    @Override
    @Transactional
    public NotificacionDocente crear(NotificacionDocente notificacion) {
        NotificacionDocenteEntidad entidad = mapper.toEntidad(notificacion);
        entidad.setSolicitudAcademica(solicitudRepositorio.getReferenceById(notificacion.getUuidSolicitudAcademica()));
        entidad.setDocente(usuarioRepositorio.getReferenceById(notificacion.getDocente().getUuidUsuario()));
        NotificacionDocenteEntidad guardada = repositorio.saveAndFlush(entidad);
        notificacion.setIdNotificacion(guardada.getIdNotificacion());
        return notificacion;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionDocente> getPorDocente(String uuidDocente) {
        return repositorio.findByDocenteUuidUsuarioOrderByFechaCreacionDescIdNotificacionDesc(uuidDocente).stream()
                .map(mapper::toDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NotificacionDocente getPorUuid(String uuidNotificacion) {
        return repositorio.findByUuidNotificacion(uuidNotificacion).map(mapper::toDominio).orElse(null);
    }

    @Override
    @Transactional
    public NotificacionDocente marcarLeida(String uuidNotificacion, LocalDateTime fechaLectura) {
        NotificacionDocenteEntidad entidad = repositorio.findByUuidNotificacion(uuidNotificacion).orElseThrow();
        entidad.setFechaLectura(fechaLectura);
        return mapper.toDominio(repositorio.saveAndFlush(entidad));
    }
}
