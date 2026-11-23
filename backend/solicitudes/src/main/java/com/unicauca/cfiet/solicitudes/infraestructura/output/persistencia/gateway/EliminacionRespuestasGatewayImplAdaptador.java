package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.gateway;

import com.unicauca.cfiet.solicitudes.aplicacion.output.EliminacionRespuestasGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EstadoNotificacionResolucion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.repositorios.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EliminacionRespuestasGatewayImplAdaptador implements EliminacionRespuestasGatewayIntPuerto {
    private final SolicitudAcademicaRepositorio solicitudRepositorio;
    private final NotificacionDocenteRepositorio notificacionDocenteRepositorio;
    private final SolicitudSupletorioCruceAsignaturaRepositorio cruceRepositorio;
    private final SolicitudExamenSupletorioRepositorio supletorioRepositorio;
    private final SolicitudCancelacionMatriculaRepositorio cancelacionMatriculaRepositorio;
    private final SolicitudCancelacionAsignaturaRepositorio cancelacionAsignaturaRepositorio;
    private final AsignaturaSolicitudAcademicaRepositorio asignaturaSolicitudRepositorio;
    private final AnexoAcademicoRepositorio anexoRepositorio;
    private final NotificacionResolucionRepositorio notificacionResolucionRepositorio;
    private final ResolucionAcademicaRepositorio resolucionRepositorio;
    private final HistorialSolicitudAcademicaRepositorio historialRepositorio;

    @Override
    @Transactional(readOnly = true)
    public List<String> getRespuestasEliminables(String uuidFuncionario, Set<String> etapasFinales,
                                                 Set<EstadoNotificacionResolucion> estadosBloqueantes, LocalDateTime desde, LocalDateTime hasta) {
        if (uuidFuncionario == null)
            return List.of();
        return solicitudRepositorio.findRespuestasEliminables(uuidFuncionario, etapasFinales, estadosBloqueantes, desde, hasta);
    }

    @Override
    @Transactional
    public List<String> eliminar(List<String> uuidsSolicitud) {
        if (uuidsSolicitud == null || uuidsSolicitud.isEmpty())
            return List.of();
        List<String> archivos = new ArrayList<>(anexoRepositorio.findArchivosPorSolicitudes(uuidsSolicitud));
        archivos.addAll(resolucionRepositorio.findArchivosPorSolicitudes(uuidsSolicitud));
        notificacionDocenteRepositorio.eliminarPorSolicitudes(uuidsSolicitud);
        cruceRepositorio.eliminarPorSolicitudes(uuidsSolicitud);
        supletorioRepositorio.eliminarPorSolicitudes(uuidsSolicitud);
        cancelacionMatriculaRepositorio.eliminarPorSolicitudes(uuidsSolicitud);
        cancelacionAsignaturaRepositorio.eliminarPorSolicitudes(uuidsSolicitud);
        asignaturaSolicitudRepositorio.eliminarPorSolicitudes(uuidsSolicitud);
        anexoRepositorio.eliminarPorSolicitudes(uuidsSolicitud);
        notificacionResolucionRepositorio.eliminarPorSolicitudes(uuidsSolicitud);
        resolucionRepositorio.eliminarPorSolicitudes(uuidsSolicitud);
        historialRepositorio.eliminarPorSolicitudes(uuidsSolicitud);
        solicitudRepositorio.eliminarPorSolicitudes(uuidsSolicitud);
        return archivos;
    }
}
