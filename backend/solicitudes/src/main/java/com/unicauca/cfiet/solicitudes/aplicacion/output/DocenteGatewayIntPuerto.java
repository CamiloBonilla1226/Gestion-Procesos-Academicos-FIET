package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Docente;

public interface DocenteGatewayIntPuerto {

    PaginacionRespuestaDTO<Docente> buscar(String nombre, String numeroDocumento, String username, int pagina, int tamanio);

    Docente getActivo(String uuidUsuario);
}
