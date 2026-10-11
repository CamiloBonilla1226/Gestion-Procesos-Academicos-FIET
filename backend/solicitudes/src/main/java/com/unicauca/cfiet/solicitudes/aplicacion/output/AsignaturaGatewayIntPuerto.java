package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Asignatura;

import java.util.List;

public interface AsignaturaGatewayIntPuerto {

    Asignatura getPorCodigo(String codigoAsignatura);

    Asignatura guardar(Asignatura asignatura);

    PaginacionRespuestaDTO<Asignatura> getPaginado(int pagina, int tamanio);

    PaginacionRespuestaDTO<Asignatura> getPorFiltro(String texto, String uuidProgramaAcademico, int pagina, int tamanio);

    List<Asignatura> getPorPrograma(String uuidProgramaAcademico);

    Asignatura getPorUuid(String uuidAsignatura);
}
