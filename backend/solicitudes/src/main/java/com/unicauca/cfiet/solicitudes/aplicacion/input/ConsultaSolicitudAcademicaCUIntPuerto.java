package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.ActorSolicitud;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ArchivoAdjunto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.DetalleSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.HistorialSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ResumenSolicitudAcademica;

import java.util.List;

public interface ConsultaSolicitudAcademicaCUIntPuerto {

    List<ResumenSolicitudAcademica> getBandejaEstudiante(String token);

    List<ResumenSolicitudAcademica> getBandejaFuncionario(String token);

    List<ResumenSolicitudAcademica> getBandejaDecano(String token);

    DetalleSolicitudAcademica getDetalle(String uuidSolicitudAcademica, String token);

    List<HistorialSolicitudAcademica> getHistorial(String uuidSolicitudAcademica, String token);

    ActorSolicitud resolverActor(String uuidSolicitudAcademica, String token);

    ArchivoAdjunto descargarAnexo(String uuidSolicitudAcademica, String uuidAnexoAcademico, String token);
}
