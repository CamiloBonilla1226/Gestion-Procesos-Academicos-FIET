package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Asignatura;

public interface AsignaturaCUIntPuerto {

    Asignatura crearAsignatura(Asignatura asignatura, String token);

    PaginacionRespuestaDTO<Asignatura> getAsignaturasPaginado(int pagina, int tamanio);

    PaginacionRespuestaDTO<Asignatura> getAsignaturasPorFiltro(String texto, int pagina, int tamanio);

    Asignatura getAsignatura(String uuidAsignatura);

    Asignatura actualizarAsignatura(String uuidAsignatura, Asignatura asignatura, String token);
}
