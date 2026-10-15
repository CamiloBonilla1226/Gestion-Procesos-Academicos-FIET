package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Docente;

public interface DocenteCUIntPuerto {

    PaginacionRespuestaDTO<Docente> buscarDocentes(String nombre, String numeroDocumento, String username, int pagina, int tamanio);
}
