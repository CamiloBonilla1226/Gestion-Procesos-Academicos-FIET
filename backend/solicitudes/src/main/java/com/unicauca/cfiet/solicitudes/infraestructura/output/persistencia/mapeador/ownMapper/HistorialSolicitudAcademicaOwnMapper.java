package com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.mapeador.ownMapper;

import com.unicauca.cfiet.solicitudes.dominio.modelos.HistorialSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.HistorialSolicitudAcademicaEntidad;
import com.unicauca.cfiet.solicitudes.infraestructura.output.persistencia.entidades.UsuarioEntidad;
import org.springframework.stereotype.Service;

@Service
public class HistorialSolicitudAcademicaOwnMapper implements OwnMapper<HistorialSolicitudAcademica, HistorialSolicitudAcademicaEntidad> {

    @Override
    public HistorialSolicitudAcademica toDominio(HistorialSolicitudAcademicaEntidad source) {
        if (source == null) return null;
        return HistorialSolicitudAcademica.builder()
                .uuidHistorial(source.getUuidHistorial())
                .solicitudAcademica(source.getSolicitudAcademica() == null ? null : SolicitudAcademica.builder()
                        .uuidSolicitudAcademica(source.getSolicitudAcademica().getUuidSolicitudAcademica())
                        .radicado(source.getSolicitudAcademica().getRadicado())
                        .build())
                .usuario(usuarioDominio(source.getUsuario()))
                .accion(source.getAccion())
                .observaciones(source.getObservaciones())
                .fecha(source.getFecha())
                .build();
    }

    @Override
    public HistorialSolicitudAcademicaEntidad toEntidad(HistorialSolicitudAcademica source) {
        if (source == null) return null;
        return HistorialSolicitudAcademicaEntidad.builder()
                .uuidHistorial(source.getUuidHistorial())
                .accion(source.getAccion())
                .observaciones(source.getObservaciones())
                .fecha(source.getFecha())
                .build();
    }

    private Usuario usuarioDominio(UsuarioEntidad source) {
        if (source == null) return null;
        return Usuario.builder()
                .uuidUsuario(source.getUuidUsuario())
                .nombres(source.getNombres())
                .apellidos(source.getApellidos())
                .username(source.getUsername())
                .build();
    }
}
