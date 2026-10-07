package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudExamenSupletorio;

import java.time.LocalDate;

public interface TramiteExamenSupletorioCUIntPuerto {

    SolicitudExamenSupletorio rechazarPorFuncionario(String uuidSolicitudAcademica, String observacion, String token);

    SolicitudExamenSupletorio remitirADecano(String uuidSolicitudAcademica, Boolean requisitosVerificados, String observacion, String token);

    SolicitudExamenSupletorio aprobarPorDecano(String uuidSolicitudAcademica, String observacion, String token);

    SolicitudExamenSupletorio rechazarPorDecano(String uuidSolicitudAcademica, String observacion, String token);

    SolicitudExamenSupletorio enviarRespuesta(String uuidSolicitudAcademica, String token);

    SolicitudExamenSupletorio enviarRecibo(String uuidSolicitudAcademica, String token);

    SolicitudExamenSupletorio subirComprobante(String uuidSolicitudAcademica, String token);

    SolicitudExamenSupletorio aprobarComprobante(String uuidSolicitudAcademica, LocalDate fechaAcordadaExamen, String token);

    SolicitudExamenSupletorio rechazarComprobante(String uuidSolicitudAcademica, String observacion, String token);
}
