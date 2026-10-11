package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Asignatura;

import java.util.List;

public interface AsignaturaCUIntPuerto {

    Asignatura crearAsignatura(Asignatura asignatura, String token);

    List<Asignatura> crearAsignaturas(List<Asignatura> asignaturas, String token);

    PaginacionRespuestaDTO<Asignatura> getAsignaturasPaginado(int pagina, int tamanio);

    PaginacionRespuestaDTO<Asignatura> getAsignaturasPorFiltro(String texto, String uuidProgramaAcademico, int pagina, int tamanio);

    List<Asignatura> getAsignaturasPorPrograma(String uuidProgramaAcademico);

    Asignatura getAsignatura(String uuidAsignatura);

    Asignatura actualizarAsignatura(String uuidAsignatura, Asignatura asignatura, String token);
}
