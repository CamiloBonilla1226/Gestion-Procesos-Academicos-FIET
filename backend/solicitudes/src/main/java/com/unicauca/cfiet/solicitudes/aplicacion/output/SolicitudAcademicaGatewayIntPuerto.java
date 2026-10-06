package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.HistorialSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudAcademica;

import java.util.List;

public interface SolicitudAcademicaGatewayIntPuerto {

    SolicitudAcademica getPorUuid(String uuidSolicitudAcademica);

    String getUltimoRadicado(String prefijo);

    String getRadicadoEnCurso(String uuidEstudiante, String uuidTipoSolicitudAcademica, List<String> etapasFinales);

    SolicitudAcademica crear(SolicitudAcademica solicitud, HistorialSolicitudAcademica historial);

    SolicitudAcademica actualizarEtapa(SolicitudAcademica solicitud, HistorialSolicitudAcademica historial);
}
