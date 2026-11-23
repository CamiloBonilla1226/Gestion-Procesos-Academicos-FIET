package com.unicauca.cfiet.solicitudes.aplicacion.output;

import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;

public interface UsuarioActorGatewayIntPuerto {

    Usuario getUsuario(String uuidUsuario);

    Usuario getUsuarioPorUsername(String username);
}
