package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaMatriculada;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Estudiante;

import java.util.List;

public interface EstudianteGatewayIntPuerto {

    boolean existePorCodigoEstudiantil(String codigoEstudiantil);

    boolean existePorUuid(String uuidUsuario);

    Estudiante guardar(Estudiante estudiante);

    List<Estudiante> guardarTodos(List<Estudiante> estudiantes);

    PaginacionRespuestaDTO<Estudiante> getPaginado(int pagina, int tamanio);

    PaginacionRespuestaDTO<Estudiante> getPorFiltro(String nombre, String apellido, String codigo, int pagina, int tamanio);

    Estudiante getPorUuid(String uuidUsuario);

    List<AsignaturaMatriculada> getAsignaturasMatriculadas(String uuidUsuario);

    AsignaturaMatriculada getAsignaturaMatriculada(String uuidUsuario, String uuidAsignaturaMatriculada);

    AsignaturaMatriculada guardarAsignaturaMatriculada(String uuidUsuario, AsignaturaMatriculada asignaturaMatriculada);
}
