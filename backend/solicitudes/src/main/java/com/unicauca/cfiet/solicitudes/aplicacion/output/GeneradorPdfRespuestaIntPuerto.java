package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.ResumenRespuesta;

public interface GeneradorPdfRespuestaIntPuerto {
    byte[] generar(ResumenRespuesta resumen);
}
