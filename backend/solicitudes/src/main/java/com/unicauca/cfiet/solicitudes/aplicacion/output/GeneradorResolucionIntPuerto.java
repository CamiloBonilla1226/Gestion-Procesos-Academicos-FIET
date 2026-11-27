package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.BorradorResolucion;

public interface GeneradorResolucionIntPuerto {

    byte[] generar(BorradorResolucion borrador);
}
