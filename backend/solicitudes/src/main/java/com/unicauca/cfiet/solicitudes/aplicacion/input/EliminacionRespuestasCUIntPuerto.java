package com.unicauca.cfiet.solicitudes.aplicacion.input;

import java.time.LocalDate;

public interface EliminacionRespuestasCUIntPuerto {

    int contarRespuestas(LocalDate desde, LocalDate hasta, String token);

    int eliminarRespuestas(LocalDate desde, LocalDate hasta, String token);
}
