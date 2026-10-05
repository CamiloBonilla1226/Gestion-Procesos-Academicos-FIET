package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaMatriculada;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Estudiante;

import java.util.List;

public interface EstudianteCUIntPuerto {

    Estudiante crearEstudiante(Estudiante estudiante, String token);

    List<Estudiante> crearEstudiantes(List<Estudiante> estudiantes, String token);

    PaginacionRespuestaDTO<Estudiante> getEstudiantesPaginado(int pagina, int tamanio);

    PaginacionRespuestaDTO<Estudiante> getEstudiantesPorFiltro(String nombre, String apellido, String codigo, int pagina, int tamanio);

    Estudiante getEstudiante(String uuidUsuario);

    List<AsignaturaMatriculada> getMisAsignaturas(String token);

    Estudiante actualizarEstudiante(String uuidUsuario, Estudiante estudiante, String token);

    AsignaturaMatriculada agregarAsignaturaMatriculada(String uuidUsuario, AsignaturaMatriculada asignaturaMatriculada, String token);

    AsignaturaMatriculada cambiarEstadoAsignatura(String uuidUsuario, String uuidAsignaturaMatriculada, String estado, String token);
}
