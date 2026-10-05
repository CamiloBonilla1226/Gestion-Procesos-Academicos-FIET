package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FuncionarioAcademico;

import java.util.List;

public interface FuncionarioAcademicoGatewayIntPuerto {

    FuncionarioAcademico guardar(FuncionarioAcademico funcionarioAcademico);

    List<FuncionarioAcademico> guardarTodos(List<FuncionarioAcademico> funcionariosAcademicos);

    PaginacionRespuestaDTO<FuncionarioAcademico> getPaginado(int pagina, int tamanio);

    PaginacionRespuestaDTO<FuncionarioAcademico> getPorFiltro(String nombre, String apellido, String dependencia, int pagina, int tamanio);

    FuncionarioAcademico getPorUuid(String uuidUsuario);
}
