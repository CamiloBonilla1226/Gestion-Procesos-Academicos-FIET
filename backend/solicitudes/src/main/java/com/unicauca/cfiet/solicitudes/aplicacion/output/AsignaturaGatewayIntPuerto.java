package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Asignatura;

public interface AsignaturaGatewayIntPuerto {

    boolean existePorCodigo(String codigoAsignatura);

    Asignatura getPorCodigo(String codigoAsignatura);

    Asignatura guardar(Asignatura asignatura);

    PaginacionRespuestaDTO<Asignatura> getPaginado(int pagina, int tamanio);

    PaginacionRespuestaDTO<Asignatura> getPorFiltro(String texto, int pagina, int tamanio);

    Asignatura getPorUuid(String uuidAsignatura);
}
