package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FuncionarioAcademico;

import java.util.List;

public interface FuncionarioAcademicoCUIntPuerto {

    FuncionarioAcademico crearFuncionarioAcademico(FuncionarioAcademico funcionarioAcademico, String token);

    List<FuncionarioAcademico> crearFuncionariosAcademicos(List<FuncionarioAcademico> funcionariosAcademicos, String token);

    PaginacionRespuestaDTO<FuncionarioAcademico> getFuncionariosAcademicosPaginado(int pagina, int tamanio);

    PaginacionRespuestaDTO<FuncionarioAcademico> getFuncionariosAcademicosPorFiltro(String nombre, String apellido, String dependencia, int pagina, int tamanio);

    FuncionarioAcademico getFuncionarioAcademico(String uuidUsuario);

    FuncionarioAcademico actualizarFuncionarioAcademico(String uuidUsuario, FuncionarioAcademico funcionarioAcademico, String token);
}
