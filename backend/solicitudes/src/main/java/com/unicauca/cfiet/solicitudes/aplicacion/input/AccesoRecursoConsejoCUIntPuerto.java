package com.unicauca.cfiet.solicitudes.aplicacion.input;

import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;

public interface AccesoRecursoConsejoCUIntPuerto {

    Usuario getUsuarioVisible(String uuidUsuario, String token);

    void verificarCambioPropio(String uuidUsuario, String token);

    void verificarAccesoAnexo(String uuidSolicitud, String token);

    void verificarAccesoRespuesta(String uuidRespuesta, String token);

    void verificarActualizacionSolicitud(String uuidSolicitud, String uuidFuncionarioNuevo, String token);
}
