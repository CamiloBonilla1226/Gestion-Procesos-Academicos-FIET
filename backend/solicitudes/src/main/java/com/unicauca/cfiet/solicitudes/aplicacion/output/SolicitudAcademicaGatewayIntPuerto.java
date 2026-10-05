package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.HistorialSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudAcademica;

public interface SolicitudAcademicaGatewayIntPuerto {

    SolicitudAcademica getPorUuid(String uuidSolicitudAcademica);

    String getUltimoRadicado(String prefijo);

    SolicitudAcademica crear(SolicitudAcademica solicitud, HistorialSolicitudAcademica historial);

    SolicitudAcademica actualizarEtapa(SolicitudAcademica solicitud, HistorialSolicitudAcademica historial);
}
